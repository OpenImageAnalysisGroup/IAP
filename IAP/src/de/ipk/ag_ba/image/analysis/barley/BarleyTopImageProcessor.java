/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.analysis.barley;

import java.io.IOException;
import java.io.InputStream;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import tests.images.phyto1.TestImagePhyto;
import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.phytochamber.PhytochamberTopImageProcessor;
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
import de.ipk.ag_ba.mongo.DataStorageType;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.MongoResourceIOConfigObject;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;

/**
 * @author entzian, klukas
 */
public class BarleyTopImageProcessor {
	
	private final PhytoTopImageProcessorOptions options;
	
	public BarleyTopImageProcessor(PhytoTopImageProcessorOptions options) {
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
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);
		
		IOurl urlVis = null;
		IOurl urlFlu = null;
		IOurl urlNIR = null;
		
		boolean isSmallBarley = false;
		
		if (isSmallBarley) {
			System.out.println("Small-Barley Test");
			
			urlVis = new IOurl(
					"mongo_ba-13.ipk-gatersleben.de_cloud1://bcc5acb0f93b36e5ca20e7c3e6820ac15c136957ffd216d8d751e9ad98ce89f4af2728c6a1326856d7b0404654be8fdfd023d7dbf775bc013cd275969fff39c0/H31_02_1229.png");
			urlFlu = new IOurl(
					"mongo_ba-13.ipk-gatersleben.de_cloud1://978b5eca5217382462b2b5b6485a684115d81788526998cb9421715285c904b854d43c3b38a09050e080525d10ad890dd5a272e2861ee1e13d6b88450d09e867/H31_02_1229.png");
			urlNIR = new IOurl(
					"mongo_ba-13.ipk-gatersleben.de_cloud1://e433e4462175d185e8444c5b106d703162ccd30852510421c52bf1bcda6a5ed98767dc3af3e81f9d5ae1f3a8cb7abbf7a8778b9395dda08ff4fc15d61072f200/H31_02_1229.png");
			
			String target = "mongo_localhost_localCloud1";
			if (urlFlu.getInputStream() == null) {
				urlVis = new IOurl(
						target
								+ "://bcc5acb0f93b36e5ca20e7c3e6820ac15c136957ffd216d8d751e9ad98ce89f4af2728c6a1326856d7b0404654be8fdfd023d7dbf775bc013cd275969fff39c0/H31_02_1229.png");
				urlFlu = new IOurl(
						target
								+ "://978b5eca5217382462b2b5b6485a684115d81788526998cb9421715285c904b854d43c3b38a09050e080525d10ad890dd5a272e2861ee1e13d6b88450d09e867/H31_02_1229.png");
				urlNIR = new IOurl(
						target
								+ "://e433e4462175d185e8444c5b106d703162ccd30852510421c52bf1bcda6a5ed98767dc3af3e81f9d5ae1f3a8cb7abbf7a8778b9395dda08ff4fc15d61072f200/H31_02_1229.png");
			}
			// if not available, store files in DB
			if (urlFlu.getInputStream() == null) {
				InputStream is1 = GravistoService.getResource(TestImagePhyto.class, "rgb_top_day_2_H31_02_1229.png").openStream();
				MongoResourceIOConfigObject config = new MongoResourceIOConfigObject(MeasurementNodeType.IMAGE, DataStorageType.MAIN_STREAM);
				urlVis = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "rgb_top_day_2_H31_02_1229.png", is1, config);
				System.out.println("Uploaded VIS to local DB: " + urlVis);
				InputStream is2 = GravistoService.getResource(TestImagePhyto.class, "flu_top_day_2_H31_02_1229.png").openStream();
				urlFlu = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "flu_top_day_2_H31_02_1229.png", is2, config);
				System.out.println("Uploaded FLUO to local DB: " + urlFlu);
				InputStream is3 = GravistoService.getResource(TestImagePhyto.class, "nir_top_day_2_H31_02_1229.png").openStream();
				urlNIR = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "nir_top_day_2_H31_02_1229.png", is3, config);
				System.out.println("Uploaded NIR to local DB: " + urlNIR);
			}
			
		} else {
			
			System.out.println("Barley Test");
			
			urlVis = new IOurl(
						"mongo_ba-13.ipk-gatersleben.de_cloud1://bcc5acb0f93b36e5ca20e7c3e6820ac15c136957ffd216d8d751e9ad98ce89f4af2728c6a1326856d7b0404654be8fdfd023d7dbf775bc013cd275969fff39c0/H31_02_1229.png");
			urlFlu = new IOurl(
						"mongo_ba-13.ipk-gatersleben.de_cloud1://978b5eca5217382462b2b5b6485a684115d81788526998cb9421715285c904b854d43c3b38a09050e080525d10ad890dd5a272e2861ee1e13d6b88450d09e867/H31_02_1229.png");
			urlNIR = new IOurl(
						"mongo_ba-13.ipk-gatersleben.de_cloud1://e433e4462175d185e8444c5b106d703162ccd30852510421c52bf1bcda6a5ed98767dc3af3e81f9d5ae1f3a8cb7abbf7a8778b9395dda08ff4fc15d61072f200/H31_02_1229.png");
			
			String target = "mongo_localhost_localCloud1";
			if (urlFlu.getInputStream() == null) {
				urlVis = new IOurl(
							target
									+ "://bcc5acb0f93b36e5ca20e7c3e6820ac15c136957ffd216d8d751e9ad98ce89f4af2728c6a1326856d7b0404654be8fdfd023d7dbf775bc013cd275969fff39c0/H31_02_1229.png");
				urlFlu = new IOurl(
							target
									+ "://978b5eca5217382462b2b5b6485a684115d81788526998cb9421715285c904b854d43c3b38a09050e080525d10ad890dd5a272e2861ee1e13d6b88450d09e867/H31_02_1229.png");
				urlNIR = new IOurl(
							target
									+ "://e433e4462175d185e8444c5b106d703162ccd30852510421c52bf1bcda6a5ed98767dc3af3e81f9d5ae1f3a8cb7abbf7a8778b9395dda08ff4fc15d61072f200/H31_02_1229.png");
			}
			// if not available, store files in DB
			if (urlFlu.getInputStream() == null) {
				InputStream is1 = GravistoService.getResource(TestImagePhyto.class, "rgb_top_day_2_H31_02_1229.png").openStream();
				MongoResourceIOConfigObject config = new MongoResourceIOConfigObject(MeasurementNodeType.IMAGE, DataStorageType.MAIN_STREAM);
				urlVis = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "rgb_top_day_2_H31_02_1229.png", is1, config);
				System.out.println("Uploaded VIS to local DB: " + urlVis);
				InputStream is2 = GravistoService.getResource(TestImagePhyto.class, "flu_top_day_2_H31_02_1229.png").openStream();
				urlFlu = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "flu_top_day_2_H31_02_1229.png", is2, config);
				System.out.println("Uploaded FLUO to local DB: " + urlFlu);
				InputStream is3 = GravistoService.getResource(TestImagePhyto.class, "nir_top_day_2_H31_02_1229.png").openStream();
				urlNIR = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "nir_top_day_2_H31_02_1229.png", is3, config);
				System.out.println("Uploaded NIR to local DB: " + urlNIR);
			}
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
		
		PhytochamberTopImageProcessor imageProcessorTop = new PhytochamberTopImageProcessor(options);
		
		boolean debug = true;
		boolean parameterSearch = false;
		FlexibleMaskAndImageSet res = imageProcessorTop.pipeline(
				input, SystemAnalysis.getNumberOfCPUs(), debug ? new FlexibleImageStack() : null, parameterSearch);
		
		res.print("PIPELINE RESULT");
	}
}
