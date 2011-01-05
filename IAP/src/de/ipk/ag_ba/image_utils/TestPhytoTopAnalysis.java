package de.ipk.ag_ba.image_utils;

import java.io.IOException;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.junit.Test;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

public class TestPhytoTopAnalysis {
	
	@Test
	public void test() throws IOException, Exception {
		
		// ToDo move to Unit test
		
		System.out.println("Phytochamber Test");
		
		// IOurl urlFlu = new IOurl("mongo_ba-13.ipk-gatersleben.de://26b7e285fae43dac107016afb4dc2841/WT01_1385");
		// IOurl urlVis = new IOurl("mongo_ba-13.ipk-gatersleben.de://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
		// IOurl urlNIR = new IOurl("mongo_ba-13.ipk-gatersleben.de://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");
		
		IOurl urlFlu = new IOurl(
				"mongo_ba-13.ipk-gatersleben.de_cloud1://996cfdd21a46131d6ea1c4e083fdadbf63d9f736dd83de1828b03a452f2a1e787c3da9939cd4b1f7a1d86aa5a524df1d2b6c4ce4e86ae0c41c53f62db589a1ce/flu_top_day_0_WT01_1385.png");
		IOurl urlVis = new IOurl(
				"mongo_ba-13.ipk-gatersleben.de_cloud1://6ca4ff9c5def146d4bfa7c8e60fd2d201a2bbeb81df4bf82100179a5b6d9edfa90e07151f847647ea8b5c64a6515fe95ee8d4510268aaa2708a0a572b1d5531b/rgb_top_day_0_WT01_1385.png");
		IOurl urlNIR = new IOurl(
				"mongo_ba-13.ipk-gatersleben.de_cloud1://84ba53b9380344ab33bef908e78274e4fbb1d3381519e95e0a8b0c3b27c617de608c4e6ade3123a6140070251cac75205979a398bff7a06510cbf2239750c5cd/nir_top_day_0_WT01_1385.png");
		
		// IOurl urlVis = new IOurl("file:///E:/austausch/Desktop/Bilder5/ersteRGBBild.png");
		// IOurl urlFlu = new IOurl("file:///E:/austausch/Desktop/bilder2/ersteBild.png");
		// IOurl urlNIR = new IOurl("file:///E:/austausch/Desktop/bilder2/ersteBild.png");
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);
		
		FlexibleImage imgFluo = new FlexibleImage(urlFlu);
		FlexibleImage imgVisible = new FlexibleImage(urlVis);
		FlexibleImage imgNIR = new FlexibleImage(urlNIR);
		
		double scale = 0.5;
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Debug: Using Scale-Factor of " + scale + " to improve performance!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImage();
		}
		
		FlexibleImageSet input = new FlexibleImageSet(imgVisible, imgFluo, imgNIR);
		PhytoTopImageProcessorOptions options = new PhytoTopImageProcessorOptions(scale);
		
		options.setDebugTakeTimes(true);
		options.setDebugOverlayResult(false);
		
		PhytochamberTopImageProcessor test = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input, input), options);
		// test.setValuesToStandard(scale);
		// test.clearBackground().getImages().getVis().print("Visible Test 1");
		
		FlexibleImageSet res = test.pipeline(SystemAnalysis.getNumberOfCPUs()).getImages();
		
		FlexibleImageStack result = new FlexibleImageStack();
		result.addImage("RGB Result", res.getVis());
		result.addImage("Fluo Result", res.getFluo());
		result.addImage("Nir Result", res.getNir());
		result.print("RESULT");
	}
}
