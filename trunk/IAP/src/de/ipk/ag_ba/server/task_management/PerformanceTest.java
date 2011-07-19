package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ReleaseInfo;
import org.junit.AfterClass;
import org.junit.Test;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessorMaizeAnalysis;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClusters;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockRemoveSmallClustersOnFluo;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.server.task_management.PerformanceTestImages.ImageNames;

public class PerformanceTest {
	
	private FlexibleMaskAndImageSet res;
	private FlexibleImageStack debugStack;
	double scale = 1.0;
	static boolean sleep = false;
	
	@Test
	public void testPipeline() throws IOException, Exception {
		
		// calculate LAB cube here to not skew calculation of pipeline processing time
		if (ImageOperation.labCube == null)
			System.out.println("LAB cube could not be initialized (impossible internal error)");
		
		FlexibleImage imgVis = PerformanceTestImages.getImage(ImageNames.MAIZE_VIS_SIDE_BELONG_TO_REFERENCE_1386);
		FlexibleImage imgFluo = PerformanceTestImages.getImage(ImageNames.MAIZE_FLU_SIDE_BELONG_TO_REFERENCE_1386);
		FlexibleImage imgNir = PerformanceTestImages.getImage(ImageNames.MAIZE_NIR_SIDE_BELONG_TO_REFERENCE_1386);
		
		FlexibleImage imgVisRef = PerformanceTestImages.getImage(ImageNames.MAIZE_VIS_SIDE_REFERENCE_1386);
		FlexibleImage imgFluoRef = PerformanceTestImages.getImage(ImageNames.MAIZE_FLU_SIDE_REFERENCE_1386);
		FlexibleImage imgNirRef = PerformanceTestImages.getImage(ImageNames.MAIZE_NIR_SIDE_REFERENCE_1386);
		StopWatch school;
		boolean oldschool = false;
		if (oldschool) {
			BlockRemoveSmallClustersOnFluo.ngUse = false;
			BlockRemoveSmallClusters.ngUse = false;
			FlexibleImage fc = imgFluo.copy();
			FlexibleImage nc = imgFluoRef.copy();
			school = new StopWatch("oldschool");
			testSide("oldschool", imgVis, imgVisRef, fc, nc, imgNir, imgNirRef, "1");
			school.printTime();
		}
		BlockRemoveSmallClustersOnFluo.ngUse = true;
		BlockRemoveSmallClusters.ngUse = true;
		school = new StopWatch("newschool");
		testSide("newschool", imgVis, imgVisRef, imgFluo, imgFluoRef, imgNir, imgNirRef, "1");
		school.printTime();
	}
	
	public void testSide(String debugInfo, FlexibleImage imgVis, FlexibleImage imgVisRef, FlexibleImage imgFluo, FlexibleImage imgFluoRef, FlexibleImage imgNir,
			FlexibleImage imgNirRef, String name) throws IOException, Exception, InstantiationException, IllegalAccessException, InterruptedException,
			FileNotFoundException {
		
		System.out.println("\n" + "TestMaizePipline - Side");
		
		final FlexibleImageSet input = new FlexibleImageSet(imgVis, imgFluo, imgNir);
		
		final FlexibleImageSet ref_input = new FlexibleImageSet(imgVisRef, imgFluoRef, imgNirRef);
		
		ImageProcessorOptions options = new ImageProcessorOptions(scale);
		
		options.clearAndAddBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, true);
		options.setCameraPosition(CameraPosition.SIDE);
		// options.clearAndAddDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK, 0.5);
		ImageProcessorMaizeAnalysis pipeline = new ImageProcessorMaizeAnalysis();
		
		debugStack = null;// new FlexibleImageStack();
		
		IAPservice s = new IAPservice();
		if (s.hashCode() == 0)
			System.out.println("///");
		
		res = pipeline.pipeline(options, input, ref_input, 2, debugStack);
		
		if (debugStack != null) {
			res.save(ReleaseInfo.getDesktopFolder() + File.separator + "testTestPipelineMaizeSide" + name + "_" + debugInfo + ".tiff");
			pipeline.getSettings().printAnalysisResults();
			debugStack.print("Result " + debugInfo, getReRunCode(input, ref_input), "re_Run");
			debugStack.saveAsLayeredTif(new File(ReleaseInfo.getDesktopFolder() + File.separator + "maizeSide_debugstack" + name + "_" + debugInfo + ".tiff"));
		}
	}
	
	private Runnable getReRunCode(final FlexibleImageSet input, final FlexibleImageSet ref_input) {
		Runnable reRun = new Runnable() {
			public void run() {
				ImageProcessorOptions options = new ImageProcessorOptions(scale);
				options.clearAndAddBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, true);
				options.setCameraPosition(CameraPosition.SIDE);
				ImageProcessorMaizeAnalysis pipeline = new ImageProcessorMaizeAnalysis();
				debugStack = new FlexibleImageStack();
				try {
					res = pipeline.pipeline(options, input, ref_input, 2, debugStack);
					pipeline.getSettings().printAnalysisResults();
					if (debugStack != null)
						debugStack.print("Res");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		return reRun;
		
	}
	
	public void testTop(FlexibleImage imgVis, FlexibleImage imgVisRef, FlexibleImage imgFluo, FlexibleImage imgFluoRef, FlexibleImage imgNir,
			FlexibleImage imgNirRef) throws IOException, Exception, InstantiationException, IllegalAccessException, InterruptedException, FileNotFoundException {
		
		System.out.println("\n" + "TestMaizePipline - Top");
		
		FlexibleImageSet input = new FlexibleImageSet(imgVis, imgFluo, imgNir);
		
		FlexibleImageSet ref_input = new FlexibleImageSet(imgVisRef, imgFluoRef, imgNirRef);
		
		ImageProcessorOptions options = new ImageProcessorOptions(scale);
		
		options.clearAndAddBooleanSetting(Setting.DEBUG_OVERLAY_RESULT_IMAGE, true);
		options.setCameraPosition(CameraPosition.TOP);
		// options.clearAndAddDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK, 0.5);
		ImageProcessorMaizeAnalysis maize = new ImageProcessorMaizeAnalysis();
		
		debugStack = new FlexibleImageStack();
		
		res = maize.pipeline(options, input, ref_input, 2, debugStack);
		
		res.save(ReleaseInfo.getDesktopFolder() + File.separator + "testTestPipelineMaizeTop.tiff");
		
		if (debugStack != null)
			debugStack.saveAsLayeredTif(new File(ReleaseInfo.getDesktopFolder() + File.separator + "maizeTop_debugstack.tiff"));
	}
	
	@AfterClass
	public static void setUpAfterClass() throws Exception {
		if (sleep)
			Thread.sleep(1000 * 60 * 10);
	}
}
