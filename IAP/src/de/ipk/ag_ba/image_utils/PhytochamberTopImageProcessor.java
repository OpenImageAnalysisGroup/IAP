/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import info.StopWatch;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author entzian, klukas
 */
public class PhytochamberTopImageProcessor {

	/**
	 * The unmodified input images.
	 */
	private FlexibleImageSet input;

	private final PhytoTopImageProcessorOptions options;

	private final boolean takeTimes = true;

	public PhytochamberTopImageProcessor(FlexibleImageSet input) {
		this.input = input;

		options = new PhytoTopImageProcessorOptions();
		options.initStandardValues();
	}

	public void setValuesToStandard() {
		options.initStandardValues();
	}

	public PhytochamberTopImageProcessor pipeline(int maxThreadsPerImage) {
		long pixels = this.input.getPixelCount();

		StopWatch w = debugStart("Phytochamber snapshot analysis of " + pixels + " pixels");

		FlexibleImage mask = new PhytochamberTopImageProcessor(input).equalize().clearBackground(maxThreadsPerImage).enlargeMask().mergeMask();

		PhytochamberTopImageProcessor result = new PhytochamberTopImageProcessor(input).postProcessMask(mask).applyMaskTo(input);

		debugEnd(w);
		input = null;
		return result;
	}

	public FlexibleImageSet getImages() {
		return input;
	}

	private PhytochamberTopImageProcessor applyMaskTo(FlexibleImageSet images) {
		PhytochamberTopImageProcessor res = applyMasks(images, input);
		input = null;
		return res;
	}

	public PhytochamberTopImageProcessor equalize() {
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(input.equalize());
		input = null;
		return res;
	}

	/**
	 * @return Modified images according to the given masks.
	 */
	private PhytochamberTopImageProcessor applyMasks(FlexibleImageSet images, FlexibleImageSet masks) {
		StopWatch w = debugStart("applyMasks");
		FlexibleImage vis = new ImageOperation(images.getVis()).applyMask(masks.getVis(), options.getBackground()).getImage();
		FlexibleImage fluo = new ImageOperation(images.getFluo()).applyMask(masks.getFluo(), options.getBackground()).getImage();
		FlexibleImage nir = new ImageOperation(images.getNir()).applyMask(masks.getNir(), options.getBackground()).getImage();
		debugEnd(w);
		return new PhytochamberTopImageProcessor(new FlexibleImageSet(vis, fluo, nir));
	}

	/**
	 * Erode/delate/erode several times, depending on image type.
	 * 
	 * @param mask
	 *           A single mask, used as input.
	 * @return Creates a set of images, slightly different masks for each image
	 *         type.
	 */
	private PhytochamberTopImageProcessor postProcessMask(FlexibleImage mask) {
		StopWatch w = debugStart("postProcessMask");
		FlexibleImage maskVis = postProcessMask(mask, ImageConfiguration.RgbTop);
		FlexibleImage maskFluo = postProcessMask(mask, ImageConfiguration.FluoTop);
		FlexibleImage maskNir = mask;// input.getNir();
		debugEnd(w);
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleImageSet(maskVis, maskFluo, maskNir));
		input = null;
		return res;
	}

	private FlexibleImage postProcessMask(FlexibleImage mask,
						ImageConfiguration typ) {

		ImageOperation maskIo = new ImageOperation(mask);

		switch (typ) {

			case RgbTop:

				for (int ii = 0; ii < 6; ii++)
					maskIo.erode();
				for (int ii = 0; ii < 8; ii++)
					maskIo.dilate();
				for (int ii = 0; ii < 2; ii++)
					maskIo.erode();
				break;

			case FluoTop:

				for (int ii = 0; ii < 5; ii++)
					maskIo.erode();
				for (int ii = 0; ii < 5; ii++)
					maskIo.dilate();
				maskIo.closing();
				break;
		}

		// int idx = 0;
		// int background = options.getBackground();
		// int foreground = Color.BLUE.getRGB();
		// for (int m : maskIo.getImageAs1array()) {
		// if (m == background)
		// mask1A[idx] = background;
		// else
		// mask1A[idx] = foreground;
		// idx++;
		// }
		//
		// return new FlexibleImage(ImageConverter.convert1AtoBI(mask.getWidth(), mask.getHeight(), mask1A));
		return new FlexibleImage(maskIo.getImageAsBufferedImage());
	}

	/**
	 * Enlarges the given masks (using several erode/dilate operations).
	 * These operations modify the images, so that the result images may only be
	 * used for mask operations, but not as result images. <br>
	 * 
	 * @param mask
	 *           The input images are treated as masks and should contain cleared
	 *           background (see {@link #clearBackground(FlexibleImageSet)}).
	 * @return A set of enlarged mask images.
	 *         <p>
	 *         <img src="http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	private PhytochamberTopImageProcessor enlargeMask() {
		StopWatch w = debugStart("enlargeMask");
		BufferedImage enlargedRgbMask = enlargeMask(input.getVis(), options.getRgbNumberOfErodeLoops(),
							options.getRgbNumberOfDilateLoops(), ImageConfiguration.RgbTop);
		BufferedImage enlargedFluoMask = enlargeMask(input.getFluo(), options.getFluoNumberOfErodeLoops(),
							options.getFluoNumberOfDilateLoops(), ImageConfiguration.FluoTop);

		debugEnd(w);
		// PrintImage.printImage(enlargedRgbMask, "enlarged RGB mask");
		// PrintImage.printImage(enlargedFluoMask, "enlarged FLUO mask");
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleImageSet(enlargedRgbMask, enlargedFluoMask, input.getNir()
							.getBufferedImage()));
		input = null;
		return res;
	}

	/**
	 * Uses LAB color classification, to categorize the input as foreground /
	 * background. Then small parts
	 * of the image are removed (noise), using the PixelSegmentation algorithm.
	 * 
	 * @param in
	 *           The set of input images (RGB images).
	 * @return A set of images which may be used as a mask.
	 */
	private PhytochamberTopImageProcessor clearBackground(final int maxThreadsPerImage) {
		StopWatch w = debugStart("clearBackground");

		final FlexibleImageSet res = new FlexibleImageSet();
		boolean clearFluo = false;
		MyThread a = null;
		if (!clearFluo)
			res.set(new FlexibleImage(input.getNir().getBufferedImage(), FlexibleImageType.NIR));
		else
			a = BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					BufferedImage clearNirImage = clearBackground(input.getNir().getBufferedImage(),
										ImageConfiguration.NirTop,
										options.getNearEpsilonA(), options.getNearEpsilonB(), maxThreadsPerImage);
					res.set(new FlexibleImage(clearNirImage, FlexibleImageType.NIR));
				}
			}, "clear NIR", 1);
		MyThread b = BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				BufferedImage clrearRgbImage = clearBackground(input.getVis().getBufferedImage(),
									ImageConfiguration.RgbTop,
									options.getRgbEpsilonA(), options.getRgbEpsilonB(), maxThreadsPerImage);
				res.set(new FlexibleImage(clrearRgbImage, FlexibleImageType.VIS));
			}
		}, "clear RGB", 1);
		MyThread c = BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				BufferedImage clearFluorImage = clearBackground(input.getFluo().getBufferedImage(),
									ImageConfiguration.FluoTop,
									options.getFluoEpsilonA(), options.getFluoEpsilonB(), maxThreadsPerImage);
				res.set(new FlexibleImage(clearFluorImage, FlexibleImageType.FLUO));
			}
		}, "clear FLUO", 1);

		BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c });

		debugEnd(w);
		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(res);
		input = null;
		return rrr;
	}

	public void debugOverlayImages() {
		debugOverlayImages(input.getVis(), input.getFluo(), LayeringTyp.ROW_IMAGE);
	}

	public void debugOverlayImages(LayeringTyp typ) {
		debugOverlayImages(input.getVis(), input.getFluo(), typ);
	}

	private static void debugOverlayImages(FlexibleImage a, FlexibleImage b, LayeringTyp typ) {

		BufferedImage firstImage = a.getBufferedImage();
		BufferedImage secondImage = ImageConverter.copy(b.getBufferedImage());

		for (int x = 0; x < firstImage.getWidth(); x++) {
			for (int y = 0; y < firstImage.getHeight(); y++) {
				if (typ == LayeringTyp.RED_BLUE_IMAGE) {
					Color f = new Color(firstImage.getRGB(x, y));
					Color f2 = new Color(secondImage.getRGB(x, y));
					Color f3 = new Color(f2.getRed(), 0, f.getBlue());
					secondImage.setRGB(x, y, f3.getRGB());
				} else {
					if ((y / 3) % 2 == 0)
						secondImage.setRGB(x, y, firstImage.getRGB(x, y));
					else
						secondImage.setRGB(x, y, secondImage.getRGB(x, y));
				}
			}
		}

		// GravistoService.showImage(secondImage, "ImageLayering");
		PrintImage.printImage(secondImage, "debug overlay images, secondImage");
	}

	// ########## PRIVATE ############

	private BufferedImage clearBackground(BufferedImage workImage, ImageConfiguration cameraTyp, double epsilonA,
						double epsiolonB, int maxThreadsPerImage) {

		SubstanceInterface substance = new Substance();
		substance.setName(cameraTyp.toString());
		ConditionInterface condition = new Condition(substance);
		Sample sample = new Sample(condition);
		LoadedImage limg = new LoadedImage(sample, workImage);
		limg.setURL(new IOurl(""));
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, maxThreadsPerImage, null, null, true, null, null, output, null,
							epsilonA, epsiolonB);

		return workImage;

	}

	private BufferedImage enlargeMask(FlexibleImage workImage, int numberOfErodeLoops, int numberOfDilateLoops,
						ImageConfiguration typ) {

		ImageOperation io = new ImageOperation(workImage);
		for (int i = 0; i < numberOfErodeLoops; i++)
			io.erode();
		for (int i = 0; i < numberOfDilateLoops; i++)
			io.dilate();

		return io.getImageAsBufferedImage();
	}

	/**
	 * Merges the given masks. Only parts which are confirmed as non-background
	 * in all input images are retained in the result 1/0 mask.
	 * 
	 * @param mask
	 *           The input masks (should contain cleared background).
	 * @return A single 1/0 mask.
	 */
	private FlexibleImage mergeMask() {
		MaskOperation o = new MaskOperation(input.getVis(), input.getFluo(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		// PrintImage.printImage(ImageConverter.convert1AtoBI(input.getLargestWidth(), input.getLargestHeight(), o.getMask()), "merged mask");
		FlexibleImage res = new FlexibleImage(o.getMask(), input.getLargestWidth(), input.getLargestHeight());
		input = null;
		return res;
	}

	// private FlexibleImageSet mergeEnlargedMasks(FlexibleImageSet mask) {
	//
	// int largestResultImageSize = -1;
	// double bestAngle = 0, bestScale = 1;
	// MaskOperation bestMask = null;
	//
	// // try different rotations
	// for (double angle = -5; angle <= 5; angle += 0.5) {
	//
	// ImageOperation io = new ImageOperation(mask.getFluo());
	// io.rotate(angle);
	// FlexibleImage rotFluo = io.getImage();
	//
	// MaskOperation o = new MaskOperation(mask.getVis(), rotFluo,
	// options.getBackground());
	// o.mergeMasks();
	//
	// if (o.getModifiedPixels() > largestResultImageSize) {
	// bestAngle = angle;
	// largestResultImageSize = o.getModifiedPixels();
	// bestMask = o;
	// }
	// }
	//
	// if (bestMask == null) {
	// MaskOperation o = new MaskOperation(mask.getVis(), mask.getFluo(),
	// options.getBackground());
	// o.mergeMasks();
	// bestMask = o;
	// }
	//
	// BufferedImage rotatedFluoImage = fluoMask;
	//
	// if (Math.abs(bestAngle) > 0.001) {
	// System.out.println("Detected plant rotation within snapshot: " + bestAngle
	// + " degree");
	//
	// ImageOperation io = new ImageOperation(fluoMask);
	// io.rotate(bestAngle);
	// rotatedFluoImage = io.getImageAsBufferedImage();
	// }
	//
	// FlexibleImage resNIR = applyMergedMaskToNIRimage(input.getNir(),
	// combinedMask);
	//
	// largestResultImageSize = 0;
	// // try different scaling
	//
	// // setScaleX(0.95);
	// // setScaleY(0.87);
	// double yscale = 0.915789473684211;
	// for (double scale = 0.7; scale <= 1.3; scale += 0.03) {
	//
	// ImageOperation io = new ImageOperation(fluoMask);
	// io.scale(scale, scale * yscale);
	// // io.scale(scaleX * scale, scaleY * scale);
	// BufferedImage rotFluo = io.getImageAsBufferedImage();
	//
	// MaskOperation o = new MaskOperation(rgbMask, rotFluo,
	// options.getBackground());
	// o.mergeMasks();
	//
	// if (o.getModifiedPixels() > largestResultImageSize) {
	// bestScale = scale;
	// largestResultImageSize = o.getModifiedPixels();
	// bestMask = o;
	// }
	// }
	// if (Math.abs(bestScale - 1) > 0.001) {
	// System.out.println("Detected difference of scaling in comparison to pre-defined scaling. Difference: "
	// + (int) (bestScale * 100) + " %");
	// ImageOperation io = new ImageOperation(outputFluorImage);
	// io.scale(bestScale, bestScale * yscale);
	// outputFluorImage = io.getImageAsBufferedImage();
	// }
	//
	// int[] rgbImage1A = ImageConverter.convertBIto1A(outputRGBImage);
	//
	// int[] fluoImage1A = ImageConverter.convertBIto1A(outputFluorImage);
	//
	// // modify source images according to merged mask
	// int i = 0;
	// int background = options.getBackground();
	// for (int m : bestMask.getMaskAs1Array()) {
	// if (m == 0) {
	// rgbImage1A[i] = background;
	// fluoImage1A[i] = background;
	// }
	// i++;
	// }
	//
	// output = rgbImage1A;
	// output = fluoImage1A;
	//
	// return new FlexibleImageSet(new
	// FlexibleImage(ImageConverter.convert1AtoBI(rgbMask.getWidth(),
	// rgbMask.getHeight(), bestMask.getMask())), new
	// FlexibleImage(rotatedFluoImage), mask.getNir());
	//
	// }

	private StopWatch debugStart(String task) {
		if (takeTimes)
			return new StopWatch("phytochamberTopImageProcessor: " + task);
		else
			return null;
	}

	private void debugEnd(StopWatch w) {
		if (w != null)
			w.printTime();
	}

	public static void main(String[] args) throws IOException, Exception {

		// ToDo move to Unit test

		System.out.println("Phytochamber Test");

		IOurl urlFlu = new IOurl("mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
		IOurl urlVis = new IOurl("mongo://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
		IOurl urlNIR = new IOurl("mongo://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");

		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		ResourceIOManager.registerIOHandler(new MongoDBhandler());

		FlexibleImage imgFluo = new FlexibleImage(urlFlu);
		FlexibleImage imgVisible = new FlexibleImage(urlVis);
		FlexibleImage imgNIR = new FlexibleImage(urlNIR);

		double scale = 1.0;
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Scaling!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImage();
		}

		System.out.println("Process...");

		FlexibleImageSet input = new FlexibleImageSet(imgVisible, imgFluo, imgNIR);
		PhytochamberTopImageProcessor test = new PhytochamberTopImageProcessor(input);

		// test.clearBackground().getImages().getVis().print("Visible Test 1");

		FlexibleImageSet res = test.pipeline(SystemAnalysis.getNumberOfCPUs()).getImages();
		//
		// // PrintImage.printImage(test.getInitialFluorImageAsBI(), "Anfangsbild");
		res.getVis().print("Result RGB-Image");
		res.getFluo().print("Result Fluor-Image");
		// PrintImage.printImage(test.getResultNearIMageAsBI(),
		// "Result Near-Image");
	}
}
