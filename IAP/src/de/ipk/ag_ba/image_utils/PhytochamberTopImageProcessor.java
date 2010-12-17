/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import info.StopWatch;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import org.ObjectRef;
import org.Vector2d;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.mongo.MongoDB;
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

	private final boolean debugTakeTimes = true;
	private final boolean debugPrintEachStep = false;
	private final boolean debugVIS = false, debugFLUO = false, debugNIR = false;
	private final boolean debugDisableOverlay = true;

	private FlexibleMaskAndImageSet input;

	private final PhytoTopImageProcessorOptions options;
	private static FlexibleImageStack debugStack;
	private static int debugStackWidth = 1680;

	public PhytochamberTopImageProcessor(FlexibleMaskAndImageSet workset) {
		this.input = workset;

		options = new PhytoTopImageProcessorOptions();
		options.initStandardValues();
	}

	public void setValuesToStandard() {
		options.initStandardValues();
	}

	public PhytochamberTopImageProcessor pipeline(int maxThreadsPerImage) {
		// input = input.resize(0.25, 0.25, 1);
		long pixels = input.getImages().getPixelCount();

		PhytochamberTopImageProcessor.debugStack = new FlexibleImageStack();

		StopWatch w = debugStart("Phytochamber snapshot analysis of " + pixels + " pixels");

		FlexibleMaskAndImageSet workset = new FlexibleMaskAndImageSet(input.getImages().copy(), input.getImages().copy()); // resize(0.5, 0.5, 1)

		PhytochamberTopImageProcessor result = null;

		boolean automaticRTS = true;
		if (automaticRTS) {
			// result = new PhytochamberTopImageProcessor(workset).clearBackground(maxThreadsPerImage).equalize().automaticRotation().automaticTranslation()
			// .automaticScalation().enlargeMask().mergeMask().applyMask()
			// .resetMasksToNull().postProcessResultImages(false).removeSmallClusters().postProcessResultImages(true);
			result = new PhytochamberTopImageProcessor(workset).clearBackground(maxThreadsPerImage).morpholigicOperatorsToInitinalImage().equalize()
					.automaticTranslation().automaticScale()
									.automaticRotation()
					.enlargeMask().mergeMask()
								.applyMask().postProcessResultImages(false).postProcessResultImages(true).transferMaskToImageSet(); // removeSmallClusters().

		} else {
			result = new PhytochamberTopImageProcessor(workset).clearBackground(maxThreadsPerImage).equalize().enlargeMask().mergeMask().applyMask()
								.resetMasksToNull()
								.postProcessResultImages(false).removeSmallClusters().postProcessResultImages(true);

		}

		if (debugStack != null)
			debugStack.addImage("RESULT", result.input.getOverviewImage(debugStackWidth));

		debugEnd(w, result.getImages());

		if (debugStack != null)
			debugStack.print("Debug Result Overview");

		input = null;
		return result;
	}

	private PhytochamberTopImageProcessor transferMaskToImageSet() {
		return new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(
				input.getImages().invert().draw(input.getMasks(), options.getBackground()), null));
	}

	private PhytochamberTopImageProcessor resetMasksToNull() {
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), null));
		input = null;
		return res;
	}

	private PhytochamberTopImageProcessor removeSmallClusters() {
		StopWatch w = debugStart("removeSmallClusters");
		FlexibleImage vis = new ImageOperation(input.getMasks().getVis()).removeSmallClusters().getImage();
		FlexibleImage fluo = new ImageOperation(input.getMasks().getFluo()).removeSmallClusters().getImage();
		FlexibleImage nir = new ImageOperation(input.getMasks().getNir()).removeSmallClusters().getImage();
		FlexibleImageSet processedMasks = new FlexibleImageSet(vis, fluo, nir);
		debugEnd(w, processedMasks);
		return new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks));
	}

	public FlexibleImageSet getImages() {
		return input.getImages();
	}

	private PhytochamberTopImageProcessor applyMask() {
		// input.getImages().getVis().print("VIS INPUT FOR APPLY");
		// input.getImages().getFluo().print("FLUO INPUT FOR APPLY");
		PhytochamberTopImageProcessor res = applyMasks(input.getImages(), input.getMasks());
		input = null;
		return res;
	}

	public PhytochamberTopImageProcessor equalize() {
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(
							new FlexibleMaskAndImageSet(input.getImages(), input.getMasks().equalize()));
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
		FlexibleImageSet processedImagesAreNewMasks = new FlexibleImageSet(vis, fluo, nir);
		debugEnd(w, processedImagesAreNewMasks);
		return new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(images, processedImagesAreNewMasks));
	}

	/**
	 * Erode/delate/erode several times, depending on image type.
	 */
	private PhytochamberTopImageProcessor postProcessResultImages(boolean enlarge) {
		StopWatch w = debugStart("postProcessMask");
		FlexibleImage resVis = postProcessResultImage(input.getImages().getVis(), input.getMasks().getVis(), ImageConfiguration.RgbTop, enlarge);
		FlexibleImage resFluo = postProcessResultImage(input.getImages().getFluo(), input.getMasks().getFluo(), ImageConfiguration.FluoTop, enlarge);
		FlexibleImage resNir = postProcessResultImage(input.getImages().getNir(), input.getMasks().getNir(), ImageConfiguration.NirTop, enlarge);
		FlexibleImageSet processedMasks = new FlexibleImageSet(resVis, resFluo, resNir);
		debugEnd(w, processedMasks);
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks));
		input = null;
		return res;
	}

	private PhytochamberTopImageProcessor morpholigicOperatorsToInitinalImage() {
		StopWatch w = debugStart("morpholigicOperatorsToInitinalImage");
		FlexibleImage resVis = morpholigicOperatorsToInitinalImageProcess(input.getImages().getVis(), input.getMasks().getVis(), ImageConfiguration.RgbTop);
		FlexibleImage resFluo = morpholigicOperatorsToInitinalImageProcess(input.getImages().getFluo(), input.getMasks().getFluo(), ImageConfiguration.FluoTop);
		FlexibleImage resNir = morpholigicOperatorsToInitinalImageProcess(input.getImages().getNir(), input.getMasks().getNir(), ImageConfiguration.NirTop);
		FlexibleImageSet processedMasks = new FlexibleImageSet(resVis, resFluo, resNir);
		debugEnd(w, processedMasks);
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks));
		input = null;
		return res;
	}

	private FlexibleImage morpholigicOperatorsToInitinalImageProcess(FlexibleImage srcImage, FlexibleImage workImage, ImageConfiguration typ) {

		ImageOperation maskIo = new ImageOperation(workImage);

		switch (typ) {

			case RgbTop:
				for (int ii = 0; ii < 20; ii++)
					maskIo.dilate();

				for (int ii = 0; ii < 3; ii++)
					maskIo.erode();

				break;

			case FluoTop:
				for (int ii = 0; ii < 2; ii++)
					maskIo.dilate();

				for (int ii = 0; ii < 1; ii++)
					maskIo.erode();

			case NirTop:
				for (int ii = 0; ii < 1; ii++)
					maskIo.closing();
				break;

		}
		return new ImageOperation(srcImage).applyMask2(maskIo.getImage(), options.getBackground()).getImage();
		// return new ImageOperation(workImage).getImage();
	}

	private FlexibleImage postProcessResultImage(FlexibleImage srcImage, FlexibleImage finalImage,
						ImageConfiguration typ, boolean enlarge) {

		ImageOperation maskIo = new ImageOperation(finalImage);

		switch (typ) {

			case RgbTop:
				if (enlarge)
					for (int ii = 0; ii < 1; ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < 1; ii++)
						maskIo.erode();

				// for (int ii = 0; ii < 6; ii++)
				// maskIo.erode();
				// for (int ii = 0; ii < 8; ii++)
				// maskIo.dilate();
				// for (int ii = 0; ii < 2; ii++)
				// maskIo.erode();
				break;

			case FluoTop:
				if (enlarge)
					for (int ii = 0; ii < 1; ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < 1; ii++)
						maskIo.erode();

				// for (int ii = 0; ii < 5; ii++)
				// maskIo.erode();
				// for (int ii = 0; ii < 5; ii++)
				// maskIo.dilate();
				// maskIo.closing();
				break;

			case NirTop:
				if (enlarge)
					for (int ii = 0; ii < 1; ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < 1; ii++)
						maskIo.erode();
				break;

		}

		// return new ImageOperation(finalImage).applyMask(maskIo.getImage(), options.getBackground()).getImage();
		return new ImageOperation(srcImage).applyMask2(maskIo.getImage(), options.getBackground()).getImage();
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
		BufferedImage enlargedRgbMask = enlargeMask(input.getMasks().getVis(), options.getRgbNumberOfErodeLoops(),
							options.getRgbNumberOfDilateLoops(), ImageConfiguration.RgbTop);
		BufferedImage enlargedFluoMask = enlargeMask(input.getMasks().getFluo(), options.getFluoNumberOfErodeLoops(),
							options.getFluoNumberOfDilateLoops(), ImageConfiguration.FluoTop);
		FlexibleImageSet processedMask = new FlexibleImageSet(enlargedRgbMask, enlargedFluoMask, input.getMasks().getNir()
							.getBufferedImage());
		debugEnd(w, processedMask);
		// PrintImage.printImage(enlargedRgbMask, "enlarged RGB mask");
		// PrintImage.printImage(enlargedFluoMask, "enlarged FLUO mask");
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMask));
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

		final FlexibleImageSet processedMasks = new FlexibleImageSet();
		boolean clearNir = false;
		MyThread a = null;
		if (!clearNir)
			processedMasks.set(new FlexibleImage(input.getMasks().getNir().getBufferedImage(), FlexibleImageType.NIR));
		else
			a = BackgroundThreadDispatcher.addTask(new Runnable() {
				@Override
				public void run() {
					BufferedImage clearNirImage = clearBackground(input.getMasks().getNir().getBufferedImage(),
										ImageConfiguration.NirTop,
										options.getNearEpsilonA(), options.getNearEpsilonB(), maxThreadsPerImage);
					processedMasks.set(new FlexibleImage(clearNirImage, FlexibleImageType.NIR));
				}
			}, "clear NIR", 1);
		MyThread b = BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				BufferedImage clrearRgbImage = clearBackground(input.getMasks().getVis().getBufferedImage(),
									ImageConfiguration.RgbTop,
									options.getRgbEpsilonA(), options.getRgbEpsilonB(), maxThreadsPerImage);
				processedMasks.set(new FlexibleImage(clrearRgbImage, FlexibleImageType.VIS));
			}
		}, "clear RGB", 1);
		MyThread c = BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				BufferedImage clearFluorImage = clearBackground(input.getMasks().getFluo().getBufferedImage(),
									ImageConfiguration.FluoTop,
									options.getFluoEpsilonA(), options.getFluoEpsilonB(), maxThreadsPerImage);
				processedMasks.set(new FlexibleImage(clearFluorImage, FlexibleImageType.FLUO));
			}
		}, "clear FLUO", 1);

		BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c });

		debugEnd(w, processedMasks);

		// PrintImage.printImage(res.getVis());

		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks));
		input = null;
		return rrr;
	}

	public void debugOverlayImages() {
		debugOverlayImages(input.getMasks().getVis(), input.getMasks().getFluo(), LayeringTyp.ROW_IMAGE);
	}

	public void debugOverlayImages(LayeringTyp typ) {
		debugOverlayImages(input.getMasks().getVis(), input.getMasks().getFluo(), typ);
	}

	private void debugOverlayImages(FlexibleImage a, FlexibleImage b, LayeringTyp typ) {

		if (debugDisableOverlay)
			return;

		@SuppressWarnings("unused")
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
		PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, maxThreadsPerImage, null, null, true, output, null,
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
	private PhytochamberTopImageProcessor mergeMask() {
		StopWatch w = debugStart("mergeMask");
		MaskOperation o = new MaskOperation(input.getMasks().getVis(), input.getMasks().getFluo(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		FlexibleImage img = new FlexibleImage(o.getMask(), input.getMasks().getLargestWidth(), input.getMasks().getLargestHeight());
		FlexibleImageSet mergedMasks = new FlexibleImageSet(input.getMasks().getVis(), img, input.getMasks().getVis());
		debugEnd(w, mergedMasks);
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), mergedMasks));
		input = null;
		return res;
	}

	// private PhytochamberTopImageProcessor mergeEnlargedMasks() {
	//
	// PhytochamberTopImageProcessor newMaskImage = automaticRotation().automaticTranslation().automaticScalation();
	//
	// // input Originalbilder
	// int[] rgbImage1A = input.getVis().getConvertAs1A();
	// int[] fluoImage1A = input.getFluo().getConvertAs1A();
	// int[] nirImage1A = input.getNir().getConvertAs1A();
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
	// return null;
	//
	// }

	private PhytochamberTopImageProcessor automaticScale() {
		StopWatch w = debugStart("automatic scale");

		ObjectRef scaleResult = new ObjectRef();
		FlexibleImage scaledFluoMask = automaticScalationProcess(input.getMasks().getFluo(), input.getMasks().getVis(), scaleResult);
		Vector2d t = (Vector2d) scaleResult.getObject();
		FlexibleImage scaledFluoImage = new ImageOperation(input.getImages().getFluo()).scale(t.x, t.y).getImage();

		scaleResult = new ObjectRef();
		FlexibleImage scaledNirMask = automaticScalationProcess(input.getMasks().getNir(), input.getMasks().getVis(), scaleResult);
		t = (Vector2d) scaleResult.getObject();
		FlexibleImage scaledNirImage = new ImageOperation(input.getImages().getNir()).scale(t.x, t.y).getImage();

		FlexibleImageSet processedImages = new FlexibleImageSet(input.getImages().getVis(), scaledFluoImage, scaledNirImage);
		FlexibleImageSet processedMasks = new FlexibleImageSet(input.getMasks().getVis(), scaledFluoMask, scaledNirMask);

		debugEnd(w, null);

		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(processedImages, processedMasks));
		input = null;
		return rrr;
	}

	private PhytochamberTopImageProcessor automaticTranslation() {
		StopWatch w = debugStart("automatic translation");

		ObjectRef translateResult = new ObjectRef();
		FlexibleImage translatedFluoMask = automaticTranslationProcess(input.getMasks().getFluo(), input.getMasks().getVis(), translateResult);
		Vector2d t = (Vector2d) translateResult.getObject();
		FlexibleImage translatedFluoImage = new ImageOperation(input.getImages().getFluo()).translate(t.x, t.y).getImage();

		translateResult = new ObjectRef();
		FlexibleImage translatedNirMask = automaticTranslationProcess(input.getMasks().getNir(), input.getMasks().getVis(), translateResult);
		t = (Vector2d) translateResult.getObject();
		FlexibleImage translatedNirImage = new ImageOperation(input.getImages().getNir()).translate(t.x, t.y).getImage();

		FlexibleImageSet processedMasks = new FlexibleImageSet(input.getMasks().getVis(), translatedFluoMask, translatedNirMask);
		FlexibleImageSet processeImages = new FlexibleImageSet(input.getImages().getVis(), translatedFluoImage, translatedNirImage);

		debugEnd(w, null);

		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(processeImages, processedMasks));
		input = null;
		return rrr;
	}

	private PhytochamberTopImageProcessor automaticRotation() {
		StopWatch w = debugStart("automatic rotate");

		ObjectRef rotationResult = new ObjectRef();
		// FlexibleImage rotatedFluoMask = automaticRotationProcess(input.getMasks().getFluo(), input.getMasks().getVis(), rotationResult);
		FlexibleImage rotatedFluoMask = automaticRotationProcessIntervallSearch(input.getMasks().getFluo(), input.getMasks().getVis(), rotationResult);
		Double t = (Double) rotationResult.getObject();
		// input.getImages().getFluo().print("ABOUT TO ROTATE FLUO " + t + " degrees");
		FlexibleImage rotatedFluoImage = new ImageOperation(input.getImages().getFluo()).rotate(t).getImage();
		// rotatedFluoImage.print("RESULT OF ROTATION FLUO " + t + " degrees");

		rotationResult = new ObjectRef();
		// FlexibleImage rotatedNirMask = automaticRotationProcess(input.getMasks().getNir(), input.getMasks().getVis(), rotationResult);
		FlexibleImage rotatedNirMask = automaticRotationProcessIntervallSearch(input.getMasks().getNir(), input.getMasks().getVis(), rotationResult);
		t = (Double) rotationResult.getObject();
		FlexibleImage rotatedNirImage = new ImageOperation(input.getImages().getNir()).rotate(t).getImage();

		FlexibleImageSet processedMasks = new FlexibleImageSet(input.getMasks().getVis(), rotatedFluoMask, rotatedNirMask);
		FlexibleImageSet processeImages = new FlexibleImageSet(input.getImages().getVis(), rotatedFluoImage, rotatedNirImage);
		debugEnd(w, null);

		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(processeImages, processedMasks));
		input = null;
		return rrr;
	}

	private FlexibleImage automaticScalationProcess(FlexibleImage workMask, FlexibleImage visImage, ObjectRef scaleResultVector2d) {
		int largestResultImageSize = -1;
		double bestValueX = 1, bestValueY = 1;
		FlexibleImage transImage = null;

		// try different scale operations
		for (double x = 0.8; x <= 1.2; x += 0.05) {
			if (x == 1)
				break;
			ImageOperation io = new ImageOperation(workMask);
			io.scale(x, 1);
			transImage = io.getImage();
			MaskOperation o = new MaskOperation(visImage, transImage, null,
									options.getBackground(), Color.GRAY.getRGB());
			o.mergeMasks();

			int mod = o.getUnknownMeasurementValuePixels();

			if (mod > largestResultImageSize) {
				bestValueX = x;
				bestValueY = 0;
				largestResultImageSize = o.getUnknownMeasurementValuePixels();
			}
		}
		largestResultImageSize = -1;
		for (double y = 0.8; y <= 1.2; y += 0.05) {
			if (bestValueX == 1 && y == 1)
				break;
			ImageOperation io = new ImageOperation(workMask);
			io.scale(bestValueX, y);
			transImage = io.getImage();
			MaskOperation o = new MaskOperation(visImage, transImage, null,
								options.getBackground(), Color.GRAY.getRGB());
			o.mergeMasks();

			int mod = o.getUnknownMeasurementValuePixels();

			if (mod > largestResultImageSize) {
				bestValueY = y;
				largestResultImageSize = o.getUnknownMeasurementValuePixels();
			}
		}

		debugOverlayImages(workMask, visImage, LayeringTyp.ROW_IMAGE);

		if (Math.abs(bestValueX - 1) > 0.001 || Math.abs(bestValueY - 1) > 0.001) {
			System.out.println("Detected plant scaled within snapshot: x = " + bestValueX
								+ " y = " + bestValueY);

			ImageOperation io = new ImageOperation(workMask);
			io.scale(bestValueX, bestValueY);

			debugOverlayImages(io.getImage(), visImage, LayeringTyp.ROW_IMAGE);

			scaleResultVector2d.setObject(new Vector2d(bestValueX, bestValueY));
			return io.getImage();
		} else
			System.out.println("Scale 1/1 is best.");

		scaleResultVector2d.setObject(new Vector2d(1, 1));

		return workMask;
	}

	private FlexibleImage automaticScalationProcessOld(FlexibleImage workMask, FlexibleImage visImage, ObjectRef scaleResultVector2d) {
		int largestResultImageSize = -1;
		double bestValueX = 0, bestValueY = 0;
		FlexibleImage transImage = null;

		// try different scale operations
		for (double x = 0.8; x <= 1.2; x += 0.01) {
			for (double y = 0.8; y <= 1.2; y += 0.01) {
				if (x == 0 && y == 0)
					break;
				ImageOperation io = new ImageOperation(workMask);
				io.scale(x, y);
				transImage = io.getImage();
				MaskOperation o = new MaskOperation(visImage, transImage, null,
									options.getBackground(), Color.GRAY.getRGB());
				o.mergeMasks();

				if (o.getUnknownMeasurementValuePixels() > largestResultImageSize) {
					bestValueX = x;
					bestValueY = y;
					largestResultImageSize = o.getUnknownMeasurementValuePixels();
				}
			}
		}

		if (Math.abs(bestValueX) > 0.001 && Math.abs(bestValueY) > 0.001) {
			System.out.println("Detected plant scaled within snapshot: x = " + bestValueX
								+ " y = " + bestValueY);

			ImageOperation io = new ImageOperation(workMask);
			io.scale(bestValueX, bestValueY);
			scaleResultVector2d.setObject(new Vector2d(bestValueX, bestValueY));
			return io.getImage();
		}

		scaleResultVector2d.setObject(new Vector2d(0, 0));

		return workMask;
	}

	private FlexibleImage automaticTranslationProcess(FlexibleImage workMask, FlexibleImage visImage, ObjectRef translateResult) {

		int largestResultImageSize = -1;
		double bestValueX = 0, bestValueY = 0;
		FlexibleImage transImage = null;

		// try different translation
		for (double x = -10; x <= 10; x += 1) {
			if (x == 0)
				break;
			ImageOperation io = new ImageOperation(workMask);
			io.translate(x, 0);
			transImage = io.getImage();
			MaskOperation o = new MaskOperation(visImage, transImage, null,
									options.getBackground(), Color.GRAY.getRGB());
			o.mergeMasks();

			if (o.getUnknownMeasurementValuePixels() > largestResultImageSize) {
				bestValueX = x;
				largestResultImageSize = o.getUnknownMeasurementValuePixels();
			}
		}

		largestResultImageSize = -1;

		for (double y = -10; y <= -10; y += 1) {
			if (bestValueX == 0 && y == 0)
				break;
			ImageOperation io = new ImageOperation(workMask);
			io.translate(bestValueX, y);
			transImage = io.getImage();
			MaskOperation o = new MaskOperation(visImage, transImage, null,
								options.getBackground(), Color.GRAY.getRGB());
			o.mergeMasks();

			if (o.getUnknownMeasurementValuePixels() > largestResultImageSize) {
				bestValueY = y;
				largestResultImageSize = o.getUnknownMeasurementValuePixels();
			}
		}

		if (Math.abs(bestValueX) > 0.001 || Math.abs(bestValueY) > 0.001) {
			System.out.println("Detected plant translated within snapshot: x = " + bestValueX
								+ " y = " + bestValueY);

			ImageOperation io = new ImageOperation(workMask);
			io.translate(bestValueX, bestValueY);
			translateResult.setObject(new Vector2d(bestValueX, bestValueY));
			return io.getImage();
		} else {
			System.out.println("Translate 0/0 is best.");
			translateResult.setObject(new Vector2d(0, 0));
			return workMask;
		}

	}

	private FlexibleImage automaticRotationProcess(FlexibleImage workMask, FlexibleImage visImage, ObjectRef rotationResult) {

		int largestResultImageSize = -1;
		double bestAngle = 0;
		FlexibleImage rotImage = null;

		debugOverlayImages(workMask, visImage, LayeringTyp.ROW_IMAGE);

		// try different rotations
		for (double angle = -3; angle <= 3; angle += 0.5) {
			if (angle == 0)
				break;

			ImageOperation io = new ImageOperation(workMask);
			io.rotate(angle);
			rotImage = io.getImage();
			MaskOperation o = new MaskOperation(visImage, rotImage, null,
								options.getBackground(), Color.GRAY.getRGB());
			o.mergeMasks();

			if (o.getUnknownMeasurementValuePixels() > largestResultImageSize) {
				bestAngle = angle;
				largestResultImageSize = o.getUnknownMeasurementValuePixels();
			}
		}

		if (Math.abs(bestAngle) > 0.001) {
			System.out.println("Detected plant rotation within snapshot: " + bestAngle
								+ " degree");

			ImageOperation io = new ImageOperation(workMask);
			io.rotate(bestAngle);
			debugOverlayImages(io.getImage(), visImage, LayeringTyp.ROW_IMAGE);
			rotationResult.setObject(bestAngle);
			return io.getImage();
		} else {
			System.out.println("Best rotation is 0.");
			rotationResult.setObject(0d);
			return workMask;
		}

	}

	private FlexibleImage automaticRotationProcessIntervallSearch(FlexibleImage workMask, FlexibleImage visImage, ObjectRef rotationResult) {

		int borderLeft = -3;
		int borderRight = 3;
		double angle = 0;

		angle = rotationRecursive(workMask, visImage, borderLeft, borderRight, angle, -100000000000.0);

		if (angle != 0) {
			ImageOperation io = new ImageOperation(workMask);
			io.rotate(angle);
			debugOverlayImages(io.getImage(), visImage, LayeringTyp.ROW_IMAGE);
			rotationResult.setObject(angle);
			return io.getImage();
		} else {
			System.out.println("Best rotation is 0.");
			rotationResult.setObject(0d);
			return workMask;
		}
	}

	private double rotationRecursive(FlexibleImage workMask, FlexibleImage visImage, double borderLeft, double borderRight, double bestAngle, double bestValue) {

		double intervallLaenge = Math.ceil(Math.abs(borderLeft - borderRight));
		double value = -1;

		for (double angle = borderLeft; angle <= borderRight; angle += intervallLaenge / 10) {

			value = searchSuitableValue(workMask, visImage, angle, 0, MorphologicalOperationEnumeration.rotation);
			if (value > bestValue) {
				bestValue = value;
				bestAngle = angle;
			}
		}
		if (intervallLaenge / 10 < 0.01)
			return bestAngle;
		else
			return rotationRecursive(workMask, visImage, bestValue - ((intervallLaenge / 10) + (intervallLaenge / 10 / 10)), bestValue
					+ ((intervallLaenge / 10) + (intervallLaenge / 10 / 10)), bestAngle, bestValue);
	}

	private double searchSuitableValue(FlexibleImage workMask, FlexibleImage visImage, double valueX, double valueY, MorphologicalOperationEnumeration typ) {
		ImageOperation io = new ImageOperation(workMask);
		FlexibleImage changedMask = null;

		switch (typ) {
			case translation:
				io.translate(valueX, valueY);
				break;

			case scale:
				io.scale(valueX, valueY);
				break;

			case rotation:
				io.rotate(valueX);
				break;

			default:
				break;
		}

		changedMask = io.getImage();

		MaskOperation o = new MaskOperation(visImage, changedMask, null,
							options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		return o.getUnknownMeasurementValuePixels();
	}

	// private void test() {
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
	// }

	//
	//
	// int largestResultImageSize = -1;
	// double bestAngle = 0, bestScale = 1;
	// MaskOperation bestMask = null;
	//
	// FlexibleImage rotImage = null;
	//
	// // try different rotations
	// for (double angle = -5; angle <= 5; angle += 0.1) {
	//
	// ImageOperation io = new ImageOperation(image);
	// io.rotate(angle);
	//
	// MaskOperation o = new MaskOperation(input.getVis(), rotImage, null,
	// options.getBackground(), Color.GRAY.getRGB());
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

	private StopWatch debugStart(String task) {
		if (debugStack != null)
			debugStack.addImage("Input for " + task, input.getOverviewImage(debugStackWidth));
		if (debugTakeTimes) {
			if (debugPrintEachStep)
				if (input.getMasks() != null)
					input.getMasks().getFluo().print("Mask-Input for step: " + task);
				else
					input.getImages().getFluo().print("Image-Input for step: " + task);
			return new StopWatch("phytochamberTopImageProcessor: " + task);
		} else
			return null;
	}

	private void debugEnd(StopWatch w, FlexibleImageSet img) {
		if (w != null) {
			w.printTime();
			if (img != null) {
				if (debugVIS)
					new ImageOperation(img.getVis()).getImage().print("VIS:" + w.getDescription());
				if (debugFLUO)
					new ImageOperation(img.getFluo()).getImage().print("FLUO:" + w.getDescription());
				if (debugNIR)
					new ImageOperation(img.getNir()).getImage().print("NIR:" + w.getDescription());
			}
		}
	}

	public static void main(String[] args) throws IOException, Exception {

		// ToDo move to Unit test

		System.out.println("Phytochamber Test");

		IOurl urlFlu = new IOurl("mongo_ba-13.ipk-gatersleben.de://26b7e285fae43dac107016afb4dc2841/WT01_1385");
		IOurl urlVis = new IOurl("mongo_ba-13.ipk-gatersleben.de://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
		IOurl urlNIR = new IOurl("mongo_ba-13.ipk-gatersleben.de://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");

		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);

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
		PhytochamberTopImageProcessor test = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input, input));

		// test.clearBackground().getImages().getVis().print("Visible Test 1");

		FlexibleImageSet res = test.pipeline(SystemAnalysis.getNumberOfCPUs()).getImages();

		FlexibleImageStack result = new FlexibleImageStack();
		result.addImage("RGB Result", res.getVis());
		result.addImage("Fluo Result", res.getFluo());
		result.addImage("Nir Result", res.getNir());

		result.print("RESULT");

	}
}
