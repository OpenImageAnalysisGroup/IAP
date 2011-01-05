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
	
	// public PhytochamberTopImageProcessor(FlexibleMaskAndImageSet workset) {
	// this(workset, new PhytoTopImageProcessorOptions());
	// }
	
	public PhytochamberTopImageProcessor(FlexibleMaskAndImageSet workset, PhytoTopImageProcessorOptions options) {
		this.input = workset;
		this.options = options;
	}
	
	public void setValuesToStandard(double scale) {
		options.initStandardValues(scale);
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
			result = new PhytochamberTopImageProcessor(workset, options).clearBackground(maxThreadsPerImage).morpholigicOperatorsToInitinalImage()
					.equalize().automaticTranslation().automaticScale().automaticRotation().enlargeMask().mergeMask()
								.applyMask().postProcessResultImages(false).postProcessResultImages(true).transferMaskToImageSet(); // removeSmallClusters().
			
		} else {
			result = new PhytochamberTopImageProcessor(workset, options).clearBackground(maxThreadsPerImage).equalize().enlargeMask().mergeMask().applyMask()
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
				input.getImages().invert().draw(input.getMasks(), options.getBackground()), null), options);
	}
	
	private PhytochamberTopImageProcessor resetMasksToNull() {
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), null), options);
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
		return new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks), options);
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
							new FlexibleMaskAndImageSet(input.getImages(), input.getMasks().equalize()), options);
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
		return new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(images, processedImagesAreNewMasks), options);
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
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks), options);
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
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks), options);
		input = null;
		return res;
	}
	
	private FlexibleImage morpholigicOperatorsToInitinalImageProcess(FlexibleImage srcImage, FlexibleImage workImage, ImageConfiguration typ) {
		
		ImageOperation maskIo = new ImageOperation(workImage);
		
		switch (typ) {
			
			case RgbTop:
				for (int ii = 0; ii < options.getDilateRgbTop(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeRgbTop(); ii++)
					maskIo.erode();
				
				break;
			
			case FluoTop:
				for (int ii = 0; ii < options.getDilateFluoTop(); ii++)
					maskIo.dilate();
				
				for (int ii = 0; ii < options.getErodeFluoTop(); ii++)
					maskIo.erode();
				
			case NirTop:
				for (int ii = 0; ii < options.getClosingNirTop(); ii++)
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
					for (int ii = 0; ii < options.getPostProcessDilateRgbTop(); ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < options.getPostProcessErodeRgbTop(); ii++)
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
					for (int ii = 0; ii < options.getPostProcessDilateFluoTop(); ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < options.getPostProcessErodeFluoTop(); ii++)
						maskIo.erode();
				
				// for (int ii = 0; ii < 5; ii++)
				// maskIo.erode();
				// for (int ii = 0; ii < 5; ii++)
				// maskIo.dilate();
				// maskIo.closing();
				break;
			
			case NirTop:
				if (enlarge)
					for (int ii = 0; ii < options.getPostProcessDilateNirTop(); ii++)
						maskIo.dilate();
				if (!enlarge)
					for (int ii = 0; ii < options.getPostProcessErodeNirTop(); ii++)
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
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMask), options);
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
		
		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), processedMasks), options);
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
		FlexibleImageSet mergedMasks;
		
		if (options.isProcessNir())
			mergedMasks = new FlexibleImageSet(input.getMasks().getVis(), img, img);
		else
			mergedMasks = new FlexibleImageSet(input.getMasks().getVis(), img, input.getMasks().getVis());
		
		debugEnd(w, mergedMasks);
		PhytochamberTopImageProcessor res = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(input.getImages(), mergedMasks), options);
		input = null;
		return res;
	}
	
	private PhytochamberTopImageProcessor automaticSearch(MorphologicalOperationEnumeration typ) {
		
		ObjectRef resultValues = new ObjectRef();
		Vector2d t;
		FlexibleImage fluoMask = null;
		FlexibleImage fluoImage = null;
		FlexibleImage nirMask = null;
		FlexibleImage nirImage = null;
		
		switch (typ) {
			case translation:
				fluoMask = automaticProcessIntervallSearch(input.getMasks().getFluo(), input.getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				fluoImage = new ImageOperation(input.getImages().getFluo()).translate(t.x, t.y).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(input.getMasks().getNir(), input.getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					nirImage = new ImageOperation(input.getImages().getNir()).translate(t.x, t.y).getImage();
				}
				break;
			
			case rotation:
				fluoMask = automaticProcessIntervallSearch(input.getMasks().getFluo(), input.getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				fluoImage = new ImageOperation(input.getImages().getFluo()).rotate(t.x).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(input.getMasks().getNir(), input.getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					nirImage = new ImageOperation(input.getImages().getNir()).rotate(t.x).getImage();
				}
				break;
			
			case scale:
				fluoMask = automaticProcessIntervallSearch(input.getMasks().getFluo(), input.getMasks().getVis(), resultValues, typ);
				t = (Vector2d) resultValues.getObject();
				fluoImage = new ImageOperation(input.getImages().getFluo()).scale(t.x, t.y).getImage();
				
				if (options.isProcessNir()) {
					nirMask = automaticProcessIntervallSearch(input.getMasks().getNir(), input.getMasks().getVis(), resultValues, typ);
					t = (Vector2d) resultValues.getObject();
					nirImage = new ImageOperation(input.getImages().getNir()).scale(t.x, t.y).getImage();
				}
				break;
		}
		
		FlexibleImageSet processedImages = new FlexibleImageSet(input.getImages().getVis(), fluoImage, nirImage);
		FlexibleImageSet processedMasks = new FlexibleImageSet(input.getMasks().getVis(), fluoMask, nirMask);
		
		PhytochamberTopImageProcessor rrr = new PhytochamberTopImageProcessor(new FlexibleMaskAndImageSet(processedImages, processedMasks), options);
		input = null;
		
		return rrr;
	}
	
	private PhytochamberTopImageProcessor automaticScale() {
		StopWatch w = debugStart("automatic scale");
		PhytochamberTopImageProcessor rrr = automaticSearch(MorphologicalOperationEnumeration.scale);
		debugEnd(w, null);
		return rrr;
	}
	
	private PhytochamberTopImageProcessor automaticTranslation() {
		StopWatch w = debugStart("automatic translation");
		PhytochamberTopImageProcessor rrr = automaticSearch(MorphologicalOperationEnumeration.translation);
		debugEnd(w, null);
		return rrr;
	}
	
	private PhytochamberTopImageProcessor automaticRotation() {
		StopWatch w = debugStart("automatic rotate");
		PhytochamberTopImageProcessor rrr = automaticSearch(MorphologicalOperationEnumeration.rotation);
		debugEnd(w, null);
		return rrr;
	}
	
	private FlexibleImage automaticProcessIntervallSearch(FlexibleImage workMask, FlexibleImage visImage, ObjectRef resultValue,
			MorphologicalOperationEnumeration typ) {
		
		double bestValueX = 0;
		double bestValueY = 0;
		
		if (typ == MorphologicalOperationEnumeration.rotation) {
			bestValueX = automaticIntervallSearchPartly(workMask, visImage, typ);
		} else {
			bestValueX = automaticIntervallSearchPartly(workMask, visImage, 0, true, typ);
			bestValueY = automaticIntervallSearchPartly(workMask, visImage, bestValueX, false, typ);
			
		}
		
		return automaticSearchValueApplyToMaskAndReturn(workMask, visImage, resultValue, bestValueX, bestValueY, typ);
	}
	
	private double automaticIntervallSearchPartly(FlexibleImage workMask, FlexibleImage visImage, MorphologicalOperationEnumeration typ) {
		
		return automaticIntervallSearchPartly(workMask, visImage, 0, true, typ);
	}
	
	private double automaticIntervallSearchPartly(FlexibleImage workMask, FlexibleImage visImage, double bestOtherValue,
			boolean typOfProcess, MorphologicalOperationEnumeration typ) {
		
		double borderLeft = 0;
		double borderRight = 0;
		double intervallTeiler = 1.0;
		
		switch (typ) {
			case translation:
				borderLeft = -20;
				borderRight = 20;
				intervallTeiler = 0.2;
				break;
			
			case scale:
				borderLeft = 0.8;
				borderRight = 1.2;
				intervallTeiler = 0.1;
				break;
			
			case rotation:
				borderLeft = -5.0;
				borderRight = 5.0;
				intervallTeiler = 0.1;
				break;
		}
		
		double intervallLength = Math.abs(borderLeft - borderRight);
		double intervallSteps = intervallLength * intervallTeiler;
		
		switch (typ) {
			case translation:
				return translationRecursive(workMask, visImage, borderLeft, borderRight, 0.0, -100000000000.0, intervallTeiler, intervallSteps, 0,
						bestOtherValue, typOfProcess);
				
			case scale:
				return scaleRecursive(workMask, visImage, borderLeft, borderRight, 0.0, -100000000000.0, intervallTeiler, intervallSteps, 0,
						bestOtherValue, typOfProcess);
				
			case rotation:
				return rotationRecursive(workMask, visImage, borderLeft, borderRight, 0.0, -100000000000.0, intervallTeiler, intervallSteps, 0);
				
			default:
				return 0.0;
		}
	}
	
	private FlexibleImage automaticSearchValueApplyToMaskAndReturn(FlexibleImage workMask, FlexibleImage visImage, ObjectRef resultValue, double bestValueX,
			double bestValueY, MorphologicalOperationEnumeration typ) {
		
		if (bestValueX != 0 && bestValueY != 0) {
			ImageOperation io = new ImageOperation(workMask);
			
			switch (typ) {
				case scale:
					io.scale(bestValueX, bestValueY);
					System.out.println("Best scal is X = " + bestValueX + ", Y = " + bestValueY);
					break;
				
				case translation:
					io.translate(bestValueX, bestValueY);
					System.out.println("Best translation is X = " + bestValueX + ", Y = " + bestValueY);
					break;
				
				case rotation:
					io.rotate(bestValueX);
					System.out.println("Best rotation is X = " + bestValueX);
					break;
				
				default:
					break;
			}
			
			debugOverlayImages(io.getImage(), visImage, LayeringTyp.ROW_IMAGE);
			Vector2d t = new Vector2d(bestValueX, bestValueY);
			resultValue.setObject(t);
			
			return io.getImage();
		} else {
			switch (typ) {
				case scale:
					System.out.println("Best scal is X = 0, Y = 0");
					break;
				
				case translation:
					System.out.println("Best translation is X = 0, Y = 0");
					break;
				
				case rotation:
					System.out.println("Best rotation is X = 0");
					break;
				
				default:
					break;
			}
			
			Vector2d t = new Vector2d(0d, 0d);
			resultValue.setObject(t);
			return workMask;
		}
		
	}
	
	// ############################### HIer weiter !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	
	private double recursivePass(FlexibleImage workMask, FlexibleImage visImage, double borderLeft, double borderRight, double bestSearchValue,
			double bestValue, double intervallTeiler, double intervallSteps, int zaehler, double bestValueOfOtherScale, boolean isFirstValue,
			MorphologicalOperationEnumeration typ) {
		
		double value = -1;
		
		for (double pass = borderLeft; pass <= borderRight; pass += intervallSteps) {
			
			if (typ == MorphologicalOperationEnumeration.rotation) {
				if (Math.ceil(pass - 1) > 0.00001) // if(pass == 0) break;
					break;
				searchSuitableValue(workMask, visImage, pass, 0, typ);
				
			} else {
				
				if (typ == MorphologicalOperationEnumeration.scale) {
					if (Math.ceil(pass - 1) > 0.00001) // if(pass == 1) break;
						break;
				} else {
					if (Math.ceil(pass - 1) > 0.00001) // if(pass == 0) break;
						break;
				}
				
				if (isFirstValue)
					value = searchSuitableValue(workMask, visImage, pass, 0, typ);
				else
					value = searchSuitableValue(workMask, visImage, bestValueOfOtherScale, pass, typ);
				break;
			}
			
			zaehler++;
			
			if (value > bestValue) {
				bestValue = value;
				bestSearchValue = pass;
			}
		}
		
		return 0.0;
	}
	
	private double scaleRecursive(FlexibleImage workMask, FlexibleImage visImage, double borderLeft, double borderRight, double bestScale,
			double bestValue, double intervallTeiler, double intervallSteps, int zaehler, double bestValueOfOtherScale, boolean typOfScale) {
		
		double value = -1;
		int zaehler2 = 0;
		
		for (double scale = borderLeft; scale <= borderRight; scale += intervallSteps) {
			if (scale == 1)
				break;
			
			zaehler++;
			zaehler2++;
			if (typOfScale)
				value = searchSuitableValue(workMask, visImage, scale, 0, MorphologicalOperationEnumeration.scale);
			else
				value = searchSuitableValue(workMask, visImage, bestValueOfOtherScale, scale, MorphologicalOperationEnumeration.scale);
			
			if (value > bestValue) {
				bestValue = value;
				bestScale = scale;
			}
		}
		
		double newBorderLeft = bestScale - intervallSteps;
		double newBorderRight = bestScale + intervallSteps;
		double newIntervallTeiler = intervallTeiler * 2;
		double newIntervallLength = Math.abs(newBorderLeft - newBorderRight);
		
		double newIntervallSteps = newIntervallLength * newIntervallTeiler;
		
		if (newIntervallSteps < 1.0 || newIntervallTeiler > 1.0) {
			System.out.println("zaehler automatische Skalierung: " + zaehler);
			return bestScale;
		} else
			return translationRecursive(workMask, visImage, newBorderLeft + newIntervallSteps, newBorderRight - newIntervallSteps, bestScale, bestValue,
					newIntervallTeiler, newIntervallSteps, zaehler, bestValueOfOtherScale, typOfScale);
		
	}
	
	private double translationRecursive(FlexibleImage workMask, FlexibleImage visImage, double borderLeft, double borderRight, double bestTranslation,
			double bestValue, double intervallTeiler, double intervallSteps, int zaehler, double bestValueOfOtherTranslation, boolean typOfTranslatiion) {
		
		double value = -1;
		int zaehler2 = 0;
		
		for (double translation = borderLeft; translation <= borderRight; translation += intervallSteps) {
			if (translation == 0.0)
				break;
			
			zaehler++;
			zaehler2++;
			if (typOfTranslatiion)
				value = searchSuitableValue(workMask, visImage, translation, 0, MorphologicalOperationEnumeration.translation);
			else
				value = searchSuitableValue(workMask, visImage, bestValueOfOtherTranslation, translation, MorphologicalOperationEnumeration.translation);
			
			if (value > bestValue) {
				bestValue = value;
				bestTranslation = translation;
			}
		}
		double newBorderLeft = bestTranslation - intervallSteps;
		double newBorderRight = bestTranslation + intervallSteps;
		double newIntervallTeiler = intervallTeiler * 2;
		double newIntervallLength = Math.abs(newBorderLeft - newBorderRight);
		double newIntervallSteps = newIntervallLength * newIntervallTeiler;
		
		if (newIntervallSteps < 1.0 || newIntervallTeiler > 1.0) {
			System.out.println("zaehler automatische Translation: " + zaehler);
			return bestTranslation;
		} else
			return translationRecursive(workMask, visImage, newBorderLeft + newIntervallSteps, newBorderRight - newIntervallSteps, bestTranslation, bestValue,
					newIntervallTeiler, newIntervallSteps, zaehler, bestValueOfOtherTranslation, typOfTranslatiion);
		
	}
	
	private double rotationRecursive(FlexibleImage workMask, FlexibleImage visImage, double borderLeft, double borderRight, double bestAngle, double bestValue,
			double intervallTeiler, double intervallSteps, int zaehler) {
		
		// double intervallLength = Math.ceil(Math.abs(borderLeft - borderRight));
		// double intervallSteps = intervallLength/intervallTeiler;
		double value = -1;
		
		// System.out.println("borderLeft: " + borderLeft);
		// System.out.println("borderRigth: " + borderRight);
		// System.out.println("intervallSteps: " + intervallSteps);
		
		int zaehler2 = 0;
		
		for (double angle = borderLeft; angle <= borderRight; angle += intervallSteps) {
			if (angle == 0.0)
				break;
			
			zaehler++;
			zaehler2++;
			value = searchSuitableValue(workMask, visImage, angle, 0, MorphologicalOperationEnumeration.rotation);
			if (value > bestValue) {
				bestValue = value;
				bestAngle = angle;
			}
		}
		// System.out.println("zaehler2: " + zaehler2);
		// System.out.println("bestAngle: " + bestAngle);
		double newBorderLeft = bestAngle - intervallSteps;
		double newBorderRight = bestAngle + intervallSteps;
		
		// System.out.println("newBorderLeft: " + newBorderLeft);
		// System.out.println("newBorderRight: " + newBorderRight);
		
		double newIntervallTeiler = intervallTeiler * 2;
		// System.out.println("newIntervallTeiler: " + newIntervallTeiler);
		// double newIntervallLength = Math.ceil(Math.abs(newBorderLeft - newBorderRight));
		double newIntervallLength = Math.abs(newBorderLeft - newBorderRight);
		// System.out.println("newIntervallLength: " + newIntervallLength);
		double newIntervallSteps = newIntervallLength * newIntervallTeiler;
		
		if (newIntervallSteps < 0.01 || newIntervallTeiler > 1.0) {
			System.out.println("zaehler automatische Rotation: " + zaehler);
			return bestAngle;
		} else
			return rotationRecursive(workMask, visImage, newBorderLeft + newIntervallSteps, newBorderRight - newIntervallSteps, bestAngle, bestValue,
					newIntervallTeiler, newIntervallSteps, zaehler);
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
		
		double scale = 1.0;
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Scaling!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImage();
		}
		
		System.out.println("Process...");
		
		FlexibleImageSet input = new FlexibleImageSet(imgVisible, imgFluo, imgNIR);
		PhytoTopImageProcessorOptions options = new PhytoTopImageProcessorOptions(scale);
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
