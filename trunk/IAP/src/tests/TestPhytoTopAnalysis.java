package tests;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ipk.ag_ba.image.analysis.phytochamber.PhytoTopImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.phytochamber.PhytochamberTopImageProcessor;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Entzian, Klukas
 */

public class TestPhytoTopAnalysis {
	
	private static IOurl urlVis;
	private static IOurl urlFlu;
	private static IOurl urlNIR;
	
	private static double scale;
	
	private static FlexibleMaskAndImageSet res;
	
	private static boolean debug;
	private static boolean parameterSearch;
	private static boolean debugTakeTimes;
	private static boolean debugOverlayResult;
	
	private static long sleepTime;
	
	enum phytoTyp {
		BIG, SMALL
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);
		
		scale = 1.0;
		sleepTime = 1;
		
		debug = true;
		parameterSearch = true;
		debugTakeTimes = true;
		debugOverlayResult = false;
	}
	
	@AfterClass
	public static void setUpAfterClass() throws Exception {
		try {
			System.out.println("Sleep " + sleepTime + " hours");
			TimeUnit.HOURS.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Before
	public void setUpBeforeEveryTest() throws Exception {
		urlVis = null;
		urlFlu = null;
		urlNIR = null;
		res = null;
	}
	
	@After
	public void doAfterEveryTest() throws Exception {
		res.print("PIPELINE RESULT");
	}
	
	// @Ignore
	@Test
	public void testBigPlants() throws IOException, Exception {
		
		System.out.println("Big-Phytochamber Test");
		
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
		
		// später wieder einfügen
		// if (urlFlu.getInputStream() == null) {
		// InputStream is1 = GravistoService.getResource(TestImagePhyto.class, "rgb_top_day_0_WT01_1385.png").openStream();
		// MongoResourceIOConfigObject config = new MongoResourceIOConfigObject(MeasurementNodeType.IMAGE, DataStorageType.MAIN_STREAM);
		// urlVis = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "rgb_top_day_0_WT01_1385.png", is1, config);
		// System.out.println("Uploaded VIS to local DB: " + urlVis);
		// InputStream is2 = GravistoService.getResource(TestImagePhyto.class, "flu_top_day_0_WT01_1385.png").openStream();
		// urlFlu = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "flu_top_day_0_WT01_1385.png", is2, config);
		// System.out.println("Uploaded FLUO to local DB: " + urlFlu);
		// InputStream is3 = GravistoService.getResource(TestImagePhyto.class, "nir_top_day_0_WT01_1385.png").openStream();
		// urlNIR = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "nir_top_day_0_WT01_1385.png", is3, config);
		// System.out.println("Uploaded NIR to local DB: " + urlNIR);
		// }
		
		FlexibleImage imgFluo = new FlexibleImage(urlFlu);
		FlexibleImage imgVisible = new FlexibleImage(urlVis);
		FlexibleImage imgNIR = new FlexibleImage(urlNIR);
		
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Debug: Using Scale-Factor of " + scale + " to improve performance!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImage();
		}
		
		FlexibleImageSet input = new FlexibleImageSet(imgVisible, imgFluo, imgNIR);
		
		PhytoTopImageProcessorOptions options = new PhytoTopImageProcessorOptions(scale);
		
		options.setDebugTakeTimes(debugTakeTimes);
		options.setDebugOverlayResult(debugOverlayResult);
		
		PhytochamberTopImageProcessor phytoTop = new PhytochamberTopImageProcessor(options);
		
		res = phytoTop.pipeline(
				input, SystemAnalysis.getNumberOfCPUs(), debug ? new FlexibleImageStack() : null, parameterSearch);
		
		// System.out.println("Results:\n" + phytoTop.getSettings().toString());
		compareTestValues(phytoTop.getSettings(), phytoTyp.BIG);
		
	}
	
	// @Ignore
	@Test
	public void testSmallPlants() throws IOException, Exception {
		
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
		
		// später wieder einfügen
		// if (urlFlu.getInputStream() == null) {
		// InputStream is1 = GravistoService.getResource(TestImagePhyto.class, "rgb_top_day_2_H31_02_1229.png").openStream();
		// MongoResourceIOConfigObject config = new MongoResourceIOConfigObject(MeasurementNodeType.IMAGE, DataStorageType.MAIN_STREAM);
		// urlVis = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "rgb_top_day_2_H31_02_1229.png", is1, config);
		// System.out.println("Uploaded VIS to local DB: " + urlVis);
		// InputStream is2 = GravistoService.getResource(TestImagePhyto.class, "flu_top_day_2_H31_02_1229.png").openStream();
		// urlFlu = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "flu_top_day_2_H31_02_1229.png", is2, config);
		// System.out.println("Uploaded FLUO to local DB: " + urlFlu);
		// InputStream is3 = GravistoService.getResource(TestImagePhyto.class, "nir_top_day_2_H31_02_1229.png").openStream();
		// urlNIR = ResourceIOManager.copyDataAndReplaceURLPrefix(target, "nir_top_day_2_H31_02_1229.png", is3, config);
		// System.out.println("Uploaded NIR to local DB: " + urlNIR);
		// }
		
		FlexibleImage imgFluo = new FlexibleImage(urlFlu);
		FlexibleImage imgVisible = new FlexibleImage(urlVis);
		FlexibleImage imgNIR = new FlexibleImage(urlNIR);
		
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Debug: Using Scale-Factor of " + scale + " to improve performance!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImage();
		}
		
		FlexibleImageSet input = new FlexibleImageSet(imgVisible, imgFluo, imgNIR);
		
		PhytoTopImageProcessorOptions options = new PhytoTopImageProcessorOptions(scale);
		options.setDebugTakeTimes(debugTakeTimes);
		options.setDebugOverlayResult(debugOverlayResult);
		
		PhytochamberTopImageProcessor phytoTop = new PhytochamberTopImageProcessor(options);
		
		res = phytoTop.pipeline(input, SystemAnalysis.getNumberOfCPUs(), debug ? new FlexibleImageStack() : null, parameterSearch);
		compareTestValues(phytoTop.getSettings(), phytoTyp.SMALL);
	}
	
	private void compareTestValues(BlockProperties blockProperties, phytoTyp typ) {
		
		double delta = 0.00001;
		System.out.println("Results:\n" + blockProperties.toString());
		
		switch (typ) {
			case BIG:

				Assert.assertEquals(
						"start height BigFluoMask",
						1234,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(),
								blockProperties.getNumberOfBlocksWithThisProperty(PropertyNames.HEIGHT_FLUO_MASK), PropertyNames.HEIGHT_FLUO_MASK).getValue(), delta);
				Assert.assertEquals(
						"start width BigFluoMask",
						1624,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(),
								blockProperties.getNumberOfBlocksWithThisProperty(PropertyNames.WIDTH_FLUO_MASK), PropertyNames.WIDTH_FLUO_MASK).getValue(), delta);
				Assert.assertEquals("end height BigFluoMask", 2056,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.HEIGHT_FLUO_MASK).getValue(), delta);
				Assert.assertEquals("end width BigFluoMask", 2454,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.WIDTH_FLUO_MASK).getValue(), delta);
				Assert.assertEquals(
						"translation X BigFluoMask",
						0.016,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.TRANSLATION_FLUO_X).getValue() * 1000.) / 1000.,
						delta);
				Assert.assertEquals(
						"translation Y BigFluoMask",
						-0.016,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.TRANSLATION_FLUO_Y).getValue() * 1000.) / 1000.,
						delta);
				Assert.assertEquals("scale X BigFluoMask", 1.12,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.SCALING_FLUO_X).getValue() * 100.) / 100.,
						delta);
				Assert.assertEquals("scale Y BigFluoMask", 1.04,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.SCALING_FLUO_Y).getValue() * 100.) / 100.,
						delta);
				Assert.assertEquals(
						"rotation BigFluoMask",
						0.7984,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.ROTATION_FLUO).getValue() * 10000.) / 10000.,
						delta);
				break;
			
			case SMALL:

				Assert.assertEquals(
						"start height SmallFluoMask",
						1234,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(),
								blockProperties.getNumberOfBlocksWithThisProperty(PropertyNames.HEIGHT_FLUO_MASK), PropertyNames.HEIGHT_FLUO_MASK).getValue(), delta);
				Assert.assertEquals(
						"start width SmallFluoMask",
						1624,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(),
								blockProperties.getNumberOfBlocksWithThisProperty(PropertyNames.WIDTH_FLUO_MASK), PropertyNames.WIDTH_FLUO_MASK).getValue(), delta);
				Assert.assertEquals("end height SmallFluoMask", 2056,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.HEIGHT_FLUO_MASK).getValue(), delta);
				Assert.assertEquals("end width SmallFluoMask", 2454,
						blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.WIDTH_FLUO_MASK).getValue(), delta);
				Assert.assertEquals(
						"translation X SmallFluoMask",
						0.016,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.TRANSLATION_FLUO_X).getValue() * 1000.) / 1000.,
						delta);
				Assert.assertEquals(
						"translation Y SmallFluoMask",
						-0.016,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.TRANSLATION_FLUO_Y).getValue() * 1000.) / 1000.,
						delta);
				Assert.assertEquals("scale X SmallFluoMask", 1.12,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.SCALING_FLUO_X).getValue() * 100.) / 100.,
						delta);
				Assert.assertEquals("scale Y SmallFluoMask", 1.12,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.SCALING_FLUO_Y).getValue() * 100.) / 100.,
						delta);
				Assert.assertEquals(
						"rotation SmallFluoMask",
						-2.5296,
						Math.round(blockProperties.getNumericProperty(blockProperties.getBlockPosition(), 1, PropertyNames.ROTATION_FLUO).getValue() * 10000.) / 10000.,
						delta);
				
				break;
		}
		
	}
}
