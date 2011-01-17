package de.ipk.ag_ba.image.utils.tests;

import java.io.IOException;
import java.io.InputStream;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.junit.Test;

import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.phytochamber.PhytochamberTopImageProcessor;
import de.ipk.ag_ba.image.operations.ImageOperation;
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

public class TestPhytoTopAnalysis {
	
	@Test
	public void testBigPlants() throws IOException, Exception {
		System.out.println("Phytochamber Test");
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);
		
		IOurl urlVis = null;
		IOurl urlFlu = null;
		IOurl urlNIR = null;
		
		urlVis = new IOurl(
					"mongo_ba-13.ipk-gatersleben.de_cloud1://6ca4ff9c5def146d4bfa7c8e60fd2d201a2bbeb81df4bf82100179a5b6d9edfa90e07151f847647ea8b5c64a6515fe95ee8d4510268aaa2708a0a572b1d5531b/rgb_top_day_0_WT01_1385.png");
		urlFlu = new IOurl(
					"mongo_ba-13.ipk-gatersleben.de_cloud1://996cfdd21a46131d6ea1c4e083fdadbf63d9f736dd83de1828b03a452f2a1e787c3da9939cd4b1f7a1d86aa5a524df1d2b6c4ce4e86ae0c41c53f62db589a1ce/flu_top_day_0_WT01_1385.png");
		urlNIR = new IOurl(
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
		
		boolean debug = true;
		boolean parameterSearch = true;
		FlexibleMaskAndImageSet res = phytoTop.pipeline(
				input, SystemAnalysis.getNumberOfCPUs(), debug ? new FlexibleImageStack() : null, parameterSearch);
		
		res.print("PIPELINE RESULT");
		
	}
	
	@Test
	public void testSmallPlants() throws IOException, Exception {
		System.out.println("Phytochamber Test");
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);
		
		IOurl urlVis = null;
		IOurl urlFlu = null;
		IOurl urlNIR = null;
		
		System.out.println("Small-Phytochamber Test");
		
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
		
		boolean debug = true;
		boolean parameterSearch = true;
		FlexibleMaskAndImageSet res = phytoTop.pipeline(
				input, SystemAnalysis.getNumberOfCPUs(), debug ? new FlexibleImageStack() : null, parameterSearch);
		
		res.print("PIPELINE RESULT");
		
	}
	
}
