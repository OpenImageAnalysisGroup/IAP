/*************************************************************************
 * 
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.mongo.MongoDBhandler;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.ColorHistogram;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.ColorHistogramEntry;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.Geometry;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImage;

/**
 * @author entzian, klukas
 * 
 */
public class PhytochamberTopImageProcessor {

	private BufferedImage inputRGBImage;
	private BufferedImage inputFluorImage;
	private BufferedImage inputNearImage;

	private BufferedImage outputRGBImage;
	private BufferedImage outputFluorImage;
	private BufferedImage outputNearImage;

	private final PhytoTopImageProcessorOptions options = new PhytoTopImageProcessorOptions();

	public static void main(String[] args) throws IOException, Exception {

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

		PhytochamberTopImageProcessor test = new PhytochamberTopImageProcessor(imgVisible, imgFluo, imgNIR);
		test.doPhytoTopImageProcessor();
		// PrintImage.printImage(test.getInitialFluorImageAsBI(), "Anfangsbild");
		test.getResultRgbImage().print("Result RGB-Image");
		test.getResultFluorImage().print("Result Fluor-Image");
		// PrintImage.printImage(test.getResultNearIMageAsBI(),
		// "Result Near-Image");
	}

	public PhytochamberTopImageProcessor(FlexibleImage rgbImage, FlexibleImage fluorImage, FlexibleImage nearIfImage) {

		setRgbImageFromIB(rgbImage.getBufferedImage());
		setFluorImageFromIB(fluorImage.getBufferedImage());
		setNearImageFromIB(nearIfImage.getBufferedImage());

		options.initStandardValues();

		long pixels = rgbImage.getWidth() * fluorImage.getHeight() + fluorImage.getWidth() * fluorImage.getHeight()
				+ nearIfImage.getWidth() * nearIfImage.getHeight();
		System.out.println("Pixels: " + pixels);
	}

	// SET RGB

	private void setRgbImageFromIB(BufferedImage rgbImage) {
		this.inputRGBImage = rgbImage;
	}

	// SET Fluor

	private void setFluorImageFromIB(BufferedImage fluorImage) {
		this.inputFluorImage = fluorImage;
	}

	// SET Near

	private void setNearImageFromIB(BufferedImage nearImage) {
		this.inputNearImage = nearImage;
	}

	// SET Rest

	public void setValuesToStandard() {
		// empty
	}

	public FlexibleImage getInputRgbImage() {
		return new FlexibleImage(inputRGBImage);
	}

	public FlexibleImage getInputFluorImage() {
		return new FlexibleImage(inputFluorImage);
	}

	public FlexibleImage getInitialNearImageAsBI() {
		return new FlexibleImage(inputNearImage);
	}

	public FlexibleImage getResultRgbImage() {
		return new FlexibleImage(outputRGBImage);
	}

	public FlexibleImage getResultFluorImage() {
		return new FlexibleImage(outputFluorImage);
	}

	public FlexibleImage getResultNearImage() {
		return new FlexibleImage(outputNearImage);
	}

	// ########## PUBLIC ##############

	public void doPhytoTopImageProcessor() {
		long t1 = System.currentTimeMillis();

		outputFluorImage = fitFluoImageToRGBImage();
		BufferedImage newOriginalFluorImage = outputFluorImage;

		outputFluorImage = preliminaryWorkFluo(outputFluorImage, ImageConfiguration.FluoTop, options.getFluoEpsilonA(),
				options.getFluoEpsilonB());
		outputRGBImage = preliminaryWorkFluo(inputRGBImage, ImageConfiguration.RgbTop, options.getRgbEpsilonA(),
				options.getRgbEpsilonB());
		outputNearImage = preliminaryWorkFluo(inputNearImage, ImageConfiguration.NirTop, options.getNearEpsilonA(),
				options.getNearEpsilonB());

		BufferedImage newRGBImageMask = modifyMask(outputRGBImage, options.getRgbNumberOfErodeLoops(),
				options.getRgbNumberOfDilateLoops(), ImageConfiguration.RgbTop);
		BufferedImage newFluorImageMask = modifyMask(outputFluorImage, options.getFluoNumberOfErodeLoops(),
				options.getFluoNumberOfDilateLoops(), ImageConfiguration.FluoTop);
		// BufferedImage nearMask = modifyMask(newNearImage,
		// getNearNumberOfErodeLoops(), getNearNumberOfDilateLoops(),
		// ImageConfiguration.NirTop);

		BufferedImage mask = mergeMask(newRGBImageMask, newFluorImageMask);

		outputNearImage = fitMaskToNearImage(mask);

		outputFluorImage = removeImageNoise(outputFluorImage, newOriginalFluorImage, ImageConfiguration.FluoTop);
		outputRGBImage = removeImageNoise(outputRGBImage, inputRGBImage, ImageConfiguration.RgbTop);

		long t2 = System.currentTimeMillis();
		System.out.println("Finished in " + (t2 - t1) + " ms");
	}

	public void doImageLayering() {
		doImageLayering(inputFluorImage, inputRGBImage, LayeringTyp.ROW_IMAGE);
	}

	public void doImageLayering(LayeringTyp typ) {
		doImageLayering(inputFluorImage, inputRGBImage, typ);
	}

	public static void doImageLayering(BufferedImage firstImage, BufferedImage secondImage, LayeringTyp typ) {
		imageLayering(firstImage, secondImage, typ);
	}

	// ########## PRIVATE ############

	private BufferedImage fitFluoImageToRGBImage() {
		ImageOperation io = new ImageOperation(inputFluorImage);

		io.resize(inputRGBImage.getWidth(), inputRGBImage.getHeight());
		io.scale(options.getScaleX(), options.getScaleY());
		io.translate(options.getTranslateX(), options.getTranslateY());
		io.rotate(options.getRotationAngle());

		return io.getImageAsBufferedImage();

	}

	private BufferedImage preliminaryWorkFluo(BufferedImage workImage, ImageConfiguration cameraTyp, double epsilonA,
			double epsiolonB) {

		SubstanceInterface substance = new Substance();
		substance.setName(cameraTyp.toString());
		ConditionInterface condition = new Condition(substance);
		Sample sample = new Sample(condition);
		LoadedImage limg = new LoadedImage(sample, workImage);
		limg.setURL(new IOurl(""));
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, null, null, output, null,
				epsilonA, epsiolonB);

		return workImage;

	}

	private BufferedImage modifyMask(BufferedImage workImage, int NumberOfErodeLoops, int NumberOfDilateLoops,
			ImageConfiguration typ) {

		ImageOperation io = new ImageOperation(workImage);
		for (int i = 0; i < NumberOfErodeLoops; i++)
			io.erode();
		for (int i = 0; i < NumberOfDilateLoops; i++)
			io.dilate();

		return io.getImageAsBufferedImage();
	}

	private BufferedImage mergeMask(BufferedImage rgbMask, BufferedImage fluoMask) {

		// todo rotate and try...
		int largestResultImageSize = -1;
		double bestAngle = 0, bestScale = 1;
		MaskOperation bestMask = null;

		// try different rotations
		for (double angle = -5; angle <= 5; angle += 0.5) {

			ImageOperation io = new ImageOperation(fluoMask);
			io.rotate(angle);
			BufferedImage rotFluo = io.getImageAsBufferedImage();

			MaskOperation o = new MaskOperation(rgbMask, rotFluo, options.getBackground());
			o.doMerge();

			if (o.getModifiedPixels() > largestResultImageSize) {
				bestAngle = angle;
				largestResultImageSize = o.getModifiedPixels();
				bestMask = o;
			}
		}

		if (bestMask == null) {
			MaskOperation o = new MaskOperation(rgbMask, fluoMask, options.getBackground());
			o.doMerge();
			bestMask = o;
		}

		if (Math.abs(bestAngle) > 0.001) {
			System.out.println("Detected plant rotation within snapshot: " + bestAngle + " degree");
			{
				// rotate modified source image
				ImageOperation io = new ImageOperation(outputFluorImage);
				io.rotate(bestAngle);
				outputFluorImage = io.getImageAsBufferedImage();
			}
			{
				// rotate mask
				ImageOperation io = new ImageOperation(fluoMask);
				io.rotate(bestAngle);
				fluoMask = io.getImageAsBufferedImage();
			}
		}
		largestResultImageSize = 0;
		// try different scaling

		// setScaleX(0.95);
		// setScaleY(0.87);
		double yscale = 0.915789473684211;
		for (double scale = 0.7; scale <= 1.3; scale += 0.03) {

			ImageOperation io = new ImageOperation(fluoMask);
			io.scale(scale, scale * yscale);
			// io.scale(scaleX * scale, scaleY * scale);
			BufferedImage rotFluo = io.getImageAsBufferedImage();

			MaskOperation o = new MaskOperation(rgbMask, rotFluo, options.getBackground());
			o.doMerge();

			if (o.getModifiedPixels() > largestResultImageSize) {
				bestScale = scale;
				largestResultImageSize = o.getModifiedPixels();
				bestMask = o;
			}
		}
		if (Math.abs(bestScale - 1) > 0.001) {
			System.out.println("Detected difference of scaling in comparison to pre-defined scaling. Difference: "
					+ (int) (bestScale * 100) + " %");
			ImageOperation io = new ImageOperation(outputFluorImage);
			io.scale(bestScale, bestScale * yscale);
			outputFluorImage = io.getImageAsBufferedImage();
		}

		int[] rgbImage1A = ImageConverter.convertBIto1A(outputRGBImage);

		int[] fluoImage1A = ImageConverter.convertBIto1A(outputFluorImage);

		// modify source images according to merged mask
		int i = 0;
		int background = options.getBackground();
		for (int m : bestMask.getMaskAs1Array()) {
			if (m == 0) {
				rgbImage1A[i] = background;
				fluoImage1A[i] = background;
			}
			i++;
		}

		outputRGBImage = ImageConverter.convert1AtoBI(rgbMask.getWidth(), rgbMask.getHeight(), rgbImage1A);
		outputFluorImage = ImageConverter.convert1AtoBI(fluoMask.getWidth(), fluoMask.getHeight(), fluoImage1A);

		return outputRGBImage;
	}

	private BufferedImage fitMaskToNearImage(BufferedImage mask) {

		int[] nearMask1A = ImageConverter.convertBIto1A(outputNearImage);

		ImageOperation io = new ImageOperation(mask);
		io.resize(outputNearImage.getWidth(), outputNearImage.getHeight());
		io.rotate(-9);

		int i = 0;
		int background = options.getBackground();
		for (int m : io.getImageAs1array()) {
			if (m == background) {
				nearMask1A[i] = background;
			}
			i++;
		}

		return ImageConverter.convert1AtoBI(outputNearImage.getWidth(), outputNearImage.getHeight(), nearMask1A);
	}

	private BufferedImage removeImageNoise(BufferedImage workImage, BufferedImage originalImage, ImageConfiguration typ) {

		int[] newImage1A = ImageConverter.convertBIto1A(workImage);
		int[] originalImage1A = ImageConverter.convertBIto1A(originalImage);

		ImageOperation io = new ImageOperation(workImage);

		switch (typ) {

		case RgbTop:

			for (int ii = 0; ii < 6; ii++)
				io.erode();
			for (int ii = 0; ii < 8; ii++)
				io.dilate();
			for (int ii = 0; ii < 2; ii++)
				io.erode();
			break;

		case FluoTop:

			for (int ii = 0; ii < 5; ii++)
				io.erode();
			for (int ii = 0; ii < 5; ii++)
				io.dilate();
			io.closing();
			break;
		}

		int idx = 0;
		int background = options.getBackground();
		for (int m : io.getImageAs1array()) {
			if (m == background)
				newImage1A[idx] = background;
			else
				newImage1A[idx] = originalImage1A[idx];
			idx++;
		}

		return ImageConverter.convert1AtoBI(workImage.getWidth(), workImage.getHeight(), newImage1A);
	}

	private static void imageLayering(BufferedImage firstImage, BufferedImage secondImage, LayeringTyp typ) {

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
		PrintImage.printImage(secondImage);
	}

	public ArrayList<NumericMeasurementInterface> doAnalyseResultImages(LoadedImage limg, String experimentNameExtension) {
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();

		BufferedImage b = limg.getLoadedImage();
		int w = b.getWidth();
		int h = b.getHeight();
		int[] arrayRGB = ImageConverter.convertBIto1A(b);
		int iBackgroundFill = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		Geometry g = detectGeometry(w, h, arrayRGB, iBackgroundFill, limg);

		NumericMeasurement m;
		boolean calcHistogram = false;
		if (calcHistogram) {
			ColorHistogram histogram = new ColorHistogram(10);
			histogram.countColorPixels(arrayRGB);
			double pixelCount = histogram.getNumberOfFilledPixels();
			for (ColorHistogramEntry che : histogram.getColorEntries()) {
				String sn = limg.getSubstanceName();
				int pos = sn.indexOf(".");
				if (pos > 0)
					sn = sn.substring(0, pos);
				m = new NumericMeasurement(limg, sn + "-r: " + che.getColorDisplayName(), limg.getParentSample()
						.getParentCondition().getExperimentName()
						+ " (" + experimentNameExtension + ")");
				m.setValue(che.getNumberOfPixels() / pixelCount);
				m.setUnit("proportion");
				output.add(m);

				m = new NumericMeasurement(limg, sn + "-a: " + che.getColorDisplayName(), limg.getParentSample()
						.getParentCondition().getExperimentName()
						+ " (" + experimentNameExtension + ")");
				m.setValue(pixelCount);
				m.setUnit("pixels");
				output.add(m);
			}
		}
		if (!limg.getSubstanceName().toUpperCase().contains("TOP")) {
			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": height", limg.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + experimentNameExtension + ")");
			m.setValue(h - g.getTop());
			m.setUnit("pixel");
			output.add(m);

			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": width", limg.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + experimentNameExtension + ")");
			m.setValue(h - g.getLeft() - (h - g.getRight()));
			m.setUnit("pixel");
			output.add(m);
		}
		m = new NumericMeasurement(limg, limg.getSubstanceName() + ": filled pixels", limg.getParentSample()
				.getParentCondition().getExperimentName()
				+ " (" + experimentNameExtension + ")");
		m.setValue(g.getFilledPixels());
		m.setUnit("pixel");
		output.add(m);

		// m = new NumericMeasurement(limg, "filled (percent) ("
		// +
		// limg.getParentSample().getParentCondition().getParentSubstance().getName()
		// + ")", limg.getParentSample()
		// .getParentCondition().getExperimentName()
		// + " (" + getName() + ")");
		// m.setValue((double) g.getFilledPixels() / (w * h) * 100d);
		// m.setUnit("%");
		// output.add(m);

		boolean red = false;
		if (red) {
			int redLine = Color.RED.getRGB();

			int o = g.getTop() * w;
			int lww = 20;
			if (g.getTop() < lww + 1)
				o = 8 * w;
			for (int x = 0; x < w; x++) {
				if (o + x + w >= arrayRGB.length)
					continue;
				for (int ii = lww; ii > 0; ii--)
					if (o + x - ii * w >= 0)
						arrayRGB[o + x - ii * w] = redLine;
				// rgbArray[o + x] = redLine;
			}
			for (int y = 0; y < h; y++) {
				o = g.getLeft() + y * w;
				if (o - 1 < 0)
					continue;
				if (o + 1 >= h)
					continue;
				arrayRGB[o - 1] = redLine;
				arrayRGB[o] = redLine;
				arrayRGB[o + 1] = redLine;
				o = g.getRight() + y * w;
				if (o - 1 >= 0)
					arrayRGB[o - 1] = redLine;
				arrayRGB[o] = redLine;
				arrayRGB[o + 1] = redLine;
			}
		}
		return output;
	}

	private static Geometry detectGeometry(int w, int h, int[] rgbArray, int iBackgroundFill, LoadedImage limg) {

		int left = w;
		int right = 0;
		int top = h;

		for (int x = 0; x < w; x++)
			for (int y = h - 1; y > 0; y--) {
				int o = x + y * w;
				if (y > h * 0.95) {
					rgbArray[o] = iBackgroundFill;
					continue;
				}
				if (rgbArray[o] == iBackgroundFill)
					continue;

				if (rgbArray[o] != iBackgroundFill) {
					if (x < left)
						left = x;
					if (x > right)
						right = x;
					if (y < top)
						top = y;
				}
			}

		long filled = 0;
		for (int x = 0; x < w; x++) {
			for (int y = h - 1; y > 0; y--) {
				int o = x + y * w;
				if (rgbArray[o] != iBackgroundFill) {
					filled++;
				}
			}
		}

		return new Geometry(top, left, right, filled);
	}
}
