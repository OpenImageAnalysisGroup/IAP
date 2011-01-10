/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.phytochamber;

import java.io.IOException;
import java.io.InputStream;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockApplyMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockAutomaticParameterSearchRotation;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockAutomaticParameterSearchScaling;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockAutomaticParameterSearchTranslation;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEnlargeMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockEqualize;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMergeMask;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockMorphologicalOperations;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockPostProcessEdgeErodeEnlarge;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockPostProcessEdgeErodeReduce;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockTransferImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.image.utils.tests.images.phyto1.TestImagePhyto;
import de.ipk.ag_ba.mongo.DataStorageType;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoResourceIOConfigObject;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;

/**
 * @author entzian, klukas
 */
public class PhytochamberTopImageProcessor {

	private final PhytoTopImageProcessorOptions options;

	public PhytochamberTopImageProcessor(PhytoTopImageProcessorOptions options) {
		this.options = options;
	}

	public void setValuesToStandard(double scale) {
		options.initStandardValues(scale);
	}

	public FlexibleMaskAndImageSet pipeline(FlexibleImageSet input, int maxThreadsPerImage, FlexibleImageStack debugStack, boolean automaticParameterSearch)
			throws InstantiationException, IllegalAccessException {
		if (debugStack != null)
			options.setDebugTakeTimes(true);

		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input, input.copy());

		FlexibleMaskAndImageSet result = null;

		BlockPipeline p = new BlockPipeline(options);

		if (automaticParameterSearch) {
			p.add(BlockClearBackground.class);
			p.add(BlockMorphologicalOperations.class);
			p.add(BlockEqualize.class);
			p.add(BlockAutomaticParameterSearchTranslation.class);
			p.add(BlockAutomaticParameterSearchScaling.class);
			p.add(BlockAutomaticParameterSearchRotation.class);
			p.add(BlockEnlargeMask.class);
			p.add(BlockMergeMask.class);
			p.add(BlockApplyMask.class);
			p.add(BlockPostProcessEdgeErodeReduce.class);
			p.add(BlockPostProcessEdgeErodeEnlarge.class);
			p.add(BlockTransferImageSet.class);
		} else {
			p.add(BlockClearBackground.class);
			p.add(BlockEqualize.class);
			p.add(BlockEnlargeMask.class);
			p.add(BlockMergeMask.class);
			p.add(BlockApplyMask.class);
			p.add(BlockPostProcessEdgeErodeReduce.class);
			p.add(BlockRemoveSmallClusters.class);
			p.add(BlockPostProcessEdgeErodeEnlarge.class);
			p.add(BlockTransferImageSet.class);
		}

		result = p.execute(workset, debugStack);

		if (debugStack != null) {
			debugStack.addImage("RESULT", result.getOverviewImage(options.getDebugStackWidth()));
			debugStack.print("Debug Result Overview");
		}

		return result;
	}

	// ToDo move to Unit test
	public static void main(String[] args) throws IOException, Exception {
		System.out.println("Phytochamber Test");

		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);

		IOurl urlVis = new IOurl(
				"mongo_ba-13.ipk-gatersleben.de_cloud1://6ca4ff9c5def146d4bfa7c8e60fd2d201a2bbeb81df4bf82100179a5b6d9edfa90e07151f847647ea8b5c64a6515fe95ee8d4510268aaa2708a0a572b1d5531b/rgb_top_day_0_WT01_1385.png");
		IOurl urlFlu = new IOurl(
				"mongo_ba-13.ipk-gatersleben.de_cloud1://996cfdd21a46131d6ea1c4e083fdadbf63d9f736dd83de1828b03a452f2a1e787c3da9939cd4b1f7a1d86aa5a524df1d2b6c4ce4e86ae0c41c53f62db589a1ce/flu_top_day_0_WT01_1385.png");
		IOurl urlNIR = new IOurl(
				"mongo_ba-13.ipk-gatersleben.de_cloud1://84ba53b9380344ab33bef908e78274e4fbb1d3381519e95e0a8b0c3b27c617de608c4e6ade3123a6140070251cac75205979a398bff7a06510cbf2239750c5cd/nir_top_day_0_WT01_1385.png");

		String target = "mongo_localhost_localCloud1";
		if (urlFlu.getInputStream() == null) {
			urlVis = new IOurl(
					target
							+ "://6ca4ff9c5def146d4bfa7c8e60fd2d201a2bbeb81df4bf82100179a5b6d9edfa90e07151f847647ea8b5c64a6515fe95ee8d4510268aaa2708a0a572b1d5531b/rgb_top_day_0_WT01_1385.png");
			urlFlu = new IOurl(
					target
							+ "://996cfdd21a46131d6ea1c4e083fdadbf63d9f736dd83de1828b03a452f2a1e787c3da9939cd4b1f7a1d86aa5a524df1d2b6c4ce4e86ae0c41c53f62db589a1ce/flu_top_day_0_WT01_1385.png");
			urlNIR = new IOurl(
					target
							+ "://84ba53b9380344ab33bef908e78274e4fbb1d3381519e95e0a8b0c3b27c617de608c4e6ade3123a6140070251cac75205979a398bff7a06510cbf2239750c5cd/nir_top_day_0_WT01_1385.png");
		}
		// if not available, store files in DB
		if (urlFlu.getInputStream() == null) {
			InputStream is1 = GravistoService.getResource(TestImagePhyto.class, "rgb_top_day_0_WT01_1385.png").openStream();
			MongoResourceIOConfigObject config = new MongoResourceIOConfigObject(MeasurementNodeType.IMAGE, DataStorageType.MAIN_STREAM);
			urlVis = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "rgb_top_day_0_WT01_1385.png", is1, config);
			System.out.println("Uploaded VIS to local DB: " + urlVis);
			InputStream is2 = GravistoService.getResource(TestImagePhyto.class, "flu_top_day_0_WT01_1385.png").openStream();
			urlFlu = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "flu_top_day_0_WT01_1385.png", is2, config);
			System.out.println("Uploaded FLUO to local DB: " + urlFlu);
			InputStream is3 = GravistoService.getResource(TestImagePhyto.class, "nir_top_day_0_WT01_1385.png").openStream();
			urlNIR = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "nir_top_day_0_WT01_1385.png", is3, config);
			System.out.println("Uploaded NIR to local DB: " + urlNIR);
		}

		FlexibleImage imgFluo = new FlexibleImage(urlFlu);
		FlexibleImage imgVisible = new FlexibleImage(urlVis);
		FlexibleImage imgNIR = new FlexibleImage(urlNIR);

		double scale = 1;
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Debug: Using Scale-Factor of " + scale + " to improve performance!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImage();
		}

		FlexibleImageSet input = new FlexibleImageSet(imgVisible, imgFluo, imgNIR);

		PhytoTopImageProcessorOptions options = new PhytoTopImageProcessorOptions(scale);
		options.setDebugTakeTimes(true);
		options.setDebugOverlayResult(false);

		PhytochamberTopImageProcessor phytoTop = new PhytochamberTopImageProcessor(options);

		boolean debug = false;
		boolean parameterSearch = false;
		FlexibleMaskAndImageSet res = phytoTop.pipeline(
				input, SystemAnalysis.getNumberOfCPUs(), debug ? new FlexibleImageStack() : null, parameterSearch);

		res.print("PIPELINE RESULT");
	}
}
