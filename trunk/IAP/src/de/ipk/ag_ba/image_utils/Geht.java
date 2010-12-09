/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.mongo.MongoDB;
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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author entzian
 */
public class Geht {

	private BufferedImage rgbImage;
	private BufferedImage fluorImage;
	private BufferedImage nearImage;

	private BufferedImage newRGBImage;
	private BufferedImage newFluorImage;
	private BufferedImage newNearImage;

	private int rotationAngle;
	private double scaleX;
	private double scaleY;
	private int translateX;
	private int translateY;

	private double fluoEpsilonA;
	private double fluoEpsilonB;
	private double rgbEpsilonB;
	private double rgbEpsilonA;
	private double nearEpsilonA;
	private double nearEpsilonB;

	private int rgbNumberOfErodeLoops;
	private int rgbNumberOfDilateLoops;
	private int fluoNumberOfErodeLoops;
	private int fluoNumberOfDilateLoops;
	private int nearNumberOfErodeLoops;
	private int nearNumberOfDilateLoops;

	private int background;

	public static void main(String[] args) throws IOException, Exception {

		System.out.println("Phytochamber Test");

		IOurl urlFlu = new IOurl("mongo_ba-13.ipk-gatersleben.de://26b7e285fae43dac107016afb4dc2841/WT01_1385");
		IOurl urlVis = new IOurl("mongo_ba-13.ipk-gatersleben.de://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
		IOurl urlNIR = new IOurl("mongo_ba-13.ipk-gatersleben.de://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");

		// IOurl urlFlu = new IOurl("file:///Users/entzian/Desktop/test.png");
		// IOurl urlVis = new IOurl("file:///Users/entzian/Desktop/test.png");
		// IOurl urlNIR = new IOurl("file:///Users/entzian/Desktop/test.png");

		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos())
			for (ResourceIOHandler io : m.getHandlers())
				ResourceIOManager.registerIOHandler(io);

		BufferedImage imgFluo = ImageIO.read(urlFlu.getInputStream());
		BufferedImage imgVisible = ImageIO.read(urlVis.getInputStream());
		BufferedImage imgNIR = ImageIO.read(urlNIR.getInputStream());

		double scale = 1.0;
		if (Math.abs(scale - 1) > 0.0001) {
			System.out.println("Scaling!");
			imgFluo = new ImageOperation(imgFluo).resize(scale).getImageAsBufferedImage();
			imgVisible = new ImageOperation(imgVisible).resize(scale).getImageAsBufferedImage();
		}

		System.out.println("Process...");

		Geht test = new Geht(imgVisible, imgFluo, imgNIR);
		test.doPhytoTopImageProcessor();
		System.out.println("fertig!");
		// PrintImage.printImage(test.getInitialFluorImageAsBI(), "Anfangsbild");
		// PrintImage.printImage(test.getResultRgbImageAsBI(),
		// "Result RGB-Image");
		// PrintImage.printImage(test.getResultFluorImageAsBI(),
		// "Result Fluor-Image");
		// PrintImage.printImage(test.getResultNearIMageAsBI(),
		// "Result Near-Image");
	}

	public Geht(BufferedImage rgbImage, BufferedImage fluorImage, BufferedImage nearIfImage) {

		setRgbImageFromIB(rgbImage);
		setFluorImageFromIB(fluorImage);
		setNearImageFromIB(nearIfImage);

		initStandardValues();

		long pixels = rgbImage.getWidth() * fluorImage.getHeight() + fluorImage.getWidth() * fluorImage.getHeight() + nearIfImage.getWidth()
							* nearIfImage.getHeight();
		System.out.println("Pixels: " + pixels);
	}

	public Geht(int[] rgbImage, int rgbWidth, int rgbHeight, int[] fluorImage, int fluoWidth, int fluoHeight, int[] nearIfImage,
						int nearWidth, int nearHeight) {
		this(ImageConverter.convert1AtoBI(rgbWidth, rgbHeight, rgbImage), ImageConverter.convert1AtoBI(fluoWidth, fluoHeight, fluorImage), ImageConverter
							.convert1AtoBI(nearWidth, nearHeight, nearIfImage));
	}

	public Geht(int[][] rgbImage, IOurl urlRGB, int[][] fluorImage, IOurl urlFluo, int[][] nearIfImage, IOurl urlNear) {
		this(ImageConverter.convert2AtoBI(rgbImage), ImageConverter.convert2AtoBI(fluorImage), ImageConverter.convert2AtoBI(nearIfImage));
	}

	// ########## SET ###############

	public void setRotationAngle(int rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	public void setScaleX(double scaleX) {
		this.scaleX = scaleX;
	}

	public void setScaleY(double scaleY) {
		this.scaleY = scaleY;
	}

	public void setScaleXY(double scaleX, double scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	public void setTranslateX(int translateX) {
		this.translateX = translateX;
	}

	public void setTranslateY(int translateY) {
		this.translateY = translateY;
	}

	public void setTranslateXY(int translateX, int translateY) {
		this.translateX = translateX;
		this.translateY = translateY;
	}

	// SET Epsilon

	public void setFluoEpsilonA(double fluoEpsilonA) {
		this.fluoEpsilonA = fluoEpsilonA;
	}

	public void setFluoEpsilonB(double fluoEpsilonB) {
		this.fluoEpsilonB = fluoEpsilonB;
	}

	public void setFluoEpsilonAB(double fluoEpsilonA, double fluoEpsilonB) {
		this.fluoEpsilonA = fluoEpsilonA;
		this.fluoEpsilonB = fluoEpsilonB;
	}

	public void setRgbEpsilonB(double rgbEpsilonB) {
		this.rgbEpsilonB = rgbEpsilonB;
	}

	public void setRgbEpsilonA(double rgbEpsilonA) {
		this.rgbEpsilonA = rgbEpsilonA;
	}

	public void setRgbEpsilonAB(double rgbEpsilonA, double rgbEpsilonB) {
		this.rgbEpsilonA = rgbEpsilonA;
		this.rgbEpsilonB = rgbEpsilonB;
	}

	public void setNearEpsilonA(double nearEpsilonA) {
		this.nearEpsilonA = nearEpsilonA;
	}

	public void setNearEpsilonB(double nearEpsilonB) {
		this.nearEpsilonB = nearEpsilonB;
	}

	public void setNearEpsilonAB(double nearEpsilonA, double nearEpsilonB) {
		this.nearEpsilonA = nearEpsilonA;
		this.nearEpsilonB = nearEpsilonB;
	}

	public void setRgbNumberOfErodeLoops(int rgbNumberOfErodeLoops) {
		this.rgbNumberOfErodeLoops = rgbNumberOfErodeLoops;
	}

	public void setRgbNumberOfDilateLoops(int rgbNumberOfDilateLoops) {
		this.rgbNumberOfDilateLoops = rgbNumberOfDilateLoops;
	}

	public void setFluoNumberOfErodeLoops(int fluoNumberOfErodeLoops) {
		this.fluoNumberOfErodeLoops = fluoNumberOfErodeLoops;
	}

	public void setFluoNumberOfDilateLoops(int fluoNumberOfDilateLoops) {
		this.fluoNumberOfDilateLoops = fluoNumberOfDilateLoops;
	}

	public void setNearNumberOfErodeLoops(int nearNumberOfErodeLoops) {
		this.nearNumberOfErodeLoops = nearNumberOfErodeLoops;
	}

	public void setNearNumberOfDilateLoops(int nearNumberOfDilateLoops) {
		this.nearNumberOfDilateLoops = nearNumberOfDilateLoops;
	}

	public void setBackground(int background) {
		this.background = background;
	}

	// SET RGB

	public void setRgbImageFromIB(BufferedImage rgbImage) {
		this.rgbImage = rgbImage;
	}

	public void setRgbImageFrom1A(int[] rgbImage, int width, int height) {
		this.rgbImage = ImageConverter.convert1AtoBI(width, height, rgbImage);
	}

	public void setRgbImageFrom2A(int[][] rgbImage) {
		this.rgbImage = ImageConverter.convert2AtoBI(rgbImage);
	}

	// SET Fluor

	public void setFluorImageFromIB(BufferedImage fluorImage) {
		this.fluorImage = fluorImage;
	}

	public void setFluorImageFrom1A(int[] fluoImage, int width, int height) {
		this.fluorImage = ImageConverter.convert1AtoBI(width, height, fluoImage);
	}

	public void setFluorImageFrom2A(int[][] fluoImage) {
		this.fluorImage = ImageConverter.convert2AtoBI(fluoImage);
	}

	// SET Near

	public void setNearImageFromIB(BufferedImage nearImage) {
		this.nearImage = nearImage;
	}

	public void setNearImageFrom1A(int[] nearImage, int width, int height) {
		this.nearImage = ImageConverter.convert1AtoBI(width, height, nearImage);
	}

	public void setNearImageFrom2A(int[][] nearImage) {
		this.nearImage = ImageConverter.convert2AtoBI(nearImage);
	}

	// SET Rest

	public void setValuesToStandard() {
		// empty
	}

	// ########## GET #############

	public int getRotationAngle() {
		return rotationAngle;
	}

	public double getScaleX() {
		return scaleX;
	}

	public double getScaleY() {
		return scaleY;
	}

	public int getTranslateX() {
		return translateX;
	}

	public int getTranslateY() {
		return translateY;
	}

	// GET Epsilon

	public double getRgbEpsilonA() {
		return rgbEpsilonA;
	}

	public double getRgbEpsilonB() {
		return rgbEpsilonB;
	}

	public double getFluoEpsilonB() {
		return fluoEpsilonB;
	}

	public double getFluoEpsilonA() {
		return fluoEpsilonA;
	}

	public double getNearEpsilonB() {
		return nearEpsilonB;
	}

	public double getNearEpsilonA() {
		return nearEpsilonA;
	}

	// GET Number of Erode und Dilate loops

	public int getNearNumberOfDilateLoops() {
		return nearNumberOfDilateLoops;
	}

	public int getNearNumberOfErodeLoops() {
		return nearNumberOfErodeLoops;
	}

	public int getFluoNumberOfDilateLoops() {
		return fluoNumberOfDilateLoops;
	}

	public int getFluoNumberOfErodeLoops() {
		return fluoNumberOfErodeLoops;
	}

	public int getRgbNumberOfDilateLoops() {
		return rgbNumberOfDilateLoops;
	}

	public int getRgbNumberOfErodeLoops() {
		return rgbNumberOfErodeLoops;
	}

	public int getBackground() {
		return background;
	}

	// GET Initial RGB Image

	public int getInitialRgbImageWidth() {
		return rgbImage.getWidth();
	}

	public int getInitialRgbImageHeight() {
		return rgbImage.getHeight();
	}

	public int[] getInitialRgbImageAs1A() {
		return ImageConverter.convertBIto1A(rgbImage);
	}

	public int[][] getInitialRgbImageAs2A() {
		return ImageConverter.convertBIto2A(rgbImage);
	}

	public BufferedImage getInitialRgbImageAsBI() {
		return rgbImage;
	}

	// GET Initial Fluo Image

	public int getInitialFluoImageWidth() {
		return fluorImage.getWidth();
	}

	public int getInitialFluoImageHeight() {
		return fluorImage.getHeight();
	}

	public int[] getInitialFluorImageAs1A() {
		return ImageConverter.convertBIto1A(fluorImage);
	}

	public int[][] getInitialFluorImageAs2A() {
		return ImageConverter.convertBIto2A(fluorImage);
	}

	public BufferedImage getInitialFluorImageAsBI() {
		return fluorImage;
	}

	// GET Initial Near Image

	public int getInitialNearImageWidth() {
		return nearImage.getWidth();
	}

	public int getInitialNearImageHeight() {
		return nearImage.getHeight();
	}

	public int[] getInitialNearImageAs1A() {
		return ImageConverter.convertBIto1A(nearImage);
	}

	public int[][] getInitialNearImageAs2A() {
		return ImageConverter.convertBIto2A(nearImage);
	}

	public BufferedImage getInitialNearImageAsBI() {
		return nearImage;
	}

	// GET Result RGB IMage

	public BufferedImage getResultRgbImageAsBI() {
		return newRGBImage;
	}

	public int[] getResultRgbIMageAs1A() {
		return ImageConverter.convertBIto1A(newRGBImage);
	}

	public int[][] getResultRgbIMageAs2A() {
		return ImageConverter.convertBIto2A(newRGBImage);
	}

	// GET Result Fluo Image

	public BufferedImage getResultFluorImageAsBI() {
		return newFluorImage;
	}

	public int[] getResultFluorIMageAs1A() {
		return ImageConverter.convertBIto1A(newFluorImage);
	}

	public int[][] getResultFluorIMageAs2A() {
		return ImageConverter.convertBIto2A(newFluorImage);
	}

	// GET Result Near Image

	public BufferedImage getResultNearImageAsBI() {
		return newNearImage;
	}

	public int[] getResultNearIMageAs1A() {
		return ImageConverter.convertBIto1A(newNearImage);
	}

	public int[][] getResultNearIMageAs2A() {
		return ImageConverter.convertBIto2A(newNearImage);
	}

	// ########## PUBLIC ##############

	public void doPhytoTopImageProcessor() {
		long t1 = System.currentTimeMillis();

		newFluorImage = fitFluoImageToRGBImage();
		BufferedImage newOriginalFluorImage = newFluorImage;

		newFluorImage = preliminaryWorkFluo(newFluorImage, ImageConfiguration.FluoTop, getFluoEpsilonA(), getFluoEpsilonB());
		// PrintImage.printImage(newFluorImage);
		ImageOperation save = new ImageOperation(newFluorImage);
		// save.rotate(3);
		// save.saveImage("/Users/entzian/Desktop/zweiteBild.png");

		// save = new ImageOperation(rgbImage);
		// save.saveImage("/Users/entzian/Desktop/ersteRGBBild.png");

		newRGBImage = preliminaryWorkFluo(rgbImage, ImageConfiguration.RgbTop, getRgbEpsilonA(), getRgbEpsilonB());
		// newNearImage = preliminaryWorkFluo(nearImage, ImageConfiguration.NirTop, getNearEpsilonA(), getNearEpsilonB());

		// save = new ImageOperation(newRGBImage);
		// save.saveImage("/Users/entzian/Desktop/zweiteRGBBild.png");

		BufferedImage newRGBImageMask = modifyMask(newRGBImage, getRgbNumberOfErodeLoops(), getRgbNumberOfDilateLoops(), ImageConfiguration.RgbTop);
		BufferedImage newFluorImageMask = modifyMask(newFluorImage, getFluoNumberOfErodeLoops(), getFluoNumberOfDilateLoops(), ImageConfiguration.FluoTop);
		// save = new ImageOperation(newFluorImageMask);
		// save.rotate(3);
		// save.saveImage("/Users/entzian/Desktop/dritteBild.png");

		// save = new ImageOperation(newRGBImageMask);
		// save.saveImage("/Users/entzian/Desktop/dritteRGBBild.png");

		// PrintImage.printImage(newFluorImageMask);
		// BufferedImage nearMask = modifyMask(newNearImage,
		// getNearNumberOfErodeLoops(), getNearNumberOfDilateLoops(),
		// ImageConfiguration.NirTop);

		BufferedImage mask = mergeMask(newRGBImageMask, newFluorImageMask);
		// PrintImage.printImage(mask);

		// save = new ImageOperation(newFluorImage);
		// save.rotate(3);
		// save.saveImage("/Users/entzian/Desktop/viertesBild.png");

		// save = new ImageOperation(newRGBImage);
		// save.saveImage("/Users/entzian/Desktop/viertesRGBBild.png");

		// newNearImage = fitMaskToNearImage(mask);

		newFluorImage = removeImageNoise(newFluorImage, newOriginalFluorImage, ImageConfiguration.FluoTop);
		// save = new ImageOperation(newFluorImage);
		// save.rotate(3);
		// save.saveImage("/Users/entzian/Desktop/fuenftesBild.png");

		// PrintImage.printImage(newFluorImage);
		newRGBImage = removeImageNoise(newRGBImage, rgbImage, ImageConfiguration.RgbTop);

		// save = new ImageOperation(newRGBImage);
		// save.saveImage("/Users/entzian/Desktop/fuenftesRGBBild.png");

		long t2 = System.currentTimeMillis();
		System.out.println("Finished in " + (t2 - t1) + " ms");
	}

	public void doImageLayering() {
		doImageLayering(getInitialFluorImageAsBI(), getInitialRgbImageAsBI(), LayeringTyp.ROW_IMAGE);
	}

	public void doImageLayering(LayeringTyp typ) {
		doImageLayering(getInitialFluorImageAsBI(), getInitialRgbImageAsBI(), typ);
	}

	public void doImageLayering(BufferedImage firstImage, BufferedImage secondImage, LayeringTyp typ) {
		imageLayering(firstImage, secondImage, typ);
	}

	// ############## PRINT ##################

	// public void printImage(BufferedImage image) {
	// printImageGS(image, "Bild");
	// }
	//
	// public void printImageGS(BufferedImage image, String text) {
	// GravistoService.showImage(image, text);
	// }
	//
	// public void printImageJI(BufferedImage image) {
	// printImageJI(image, "Image");
	// }
	//
	// public void printImageJI(BufferedImage image, String text) {
	//
	// ImagePlus img = ImageConverter.convertBItoIJ(image);
	// img.show(text);
	// }

	// ########## PRIVATE ############

	private void initStandardValues() {
		setRotationAngle(-3);

		setScaleX(0.95);
		setScaleY(0.87);

		setTranslateX(0);
		setTranslateY(-15);

		setRgbEpsilonA(2.5);
		setRgbEpsilonB(2.5);

		setFluoEpsilonA(0.5);
		setFluoEpsilonB(0.5);

		setNearEpsilonA(0.5);
		setNearEpsilonB(1.0);

		setRgbNumberOfErodeLoops(2);
		setRgbNumberOfDilateLoops(5);
		setFluoNumberOfErodeLoops(4);
		setFluoNumberOfDilateLoops(20);
		setNearNumberOfErodeLoops(0);
		setNearNumberOfDilateLoops(0);

		setBackground(PhenotypeAnalysisTask.BACKGROUND_COLOR.getRGB());
	}

	private BufferedImage fitFluoImageToRGBImage() {
		ImageOperation io = new ImageOperation(getInitialFluorImageAsBI());

		io.resize(rgbImage.getWidth(), rgbImage.getHeight());
		io.scale(scaleX, scaleY);
		io.translate(translateX, translateY);
		// io.saveImage("/Users/entzian/Desktop/ersteBild.png");
		io.rotate(rotationAngle);

		return io.getImageAsBufferedImage();

	}

	public BufferedImage tryFitFluoImageToRGBImage() {

		rotationAngle = 0;
		translateX = 0;
		translateY = 0;
		// PrintImage.printImage(getInitialFluorImageAsBI());
		ImageOperation io = new ImageOperation(getInitialFluorImageAsBI());

		io.resize(rgbImage.getWidth(), rgbImage.getHeight());
		io.scale(scaleX, scaleY);
		io.translate(translateX, translateY);
		io.rotate(rotationAngle);

		return io.getImageAsBufferedImage();

	}

	private BufferedImage preliminaryWorkFluo(BufferedImage workImage, ImageConfiguration cameraTyp, double epsilonA, double epsiolonB) {

		SubstanceInterface substance = new Substance();
		substance.setName(cameraTyp.toString());
		ConditionInterface condition = new Condition(substance);
		Sample sample = new Sample(condition);
		LoadedImage limg = new LoadedImage(sample, workImage);
		limg.setURL(new IOurl(""));
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null, epsilonA, epsiolonB);

		return workImage;

	}

	private BufferedImage modifyMask(BufferedImage workImage, int NumberOfErodeLoops, int NumberOfDilateLoops, ImageConfiguration typ) {

		ImageOperation io = new ImageOperation(workImage);
		for (int i = 0; i < NumberOfErodeLoops; i++)
			io.erode();
		for (int i = 0; i < NumberOfDilateLoops; i++)
			io.dilate();

		return io.getImageAsBufferedImage();
	}

	private BufferedImage mergeMask(BufferedImage rgbMask, BufferedImage fluoMask) {

		int[] rgbImage1A = ImageConverter.convertBIto1A(newRGBImage);
		int[] fluoImage1A = ImageConverter.convertBIto1A(newFluorImage);

		MaskOperation o = new MaskOperation(new FlexibleImage(newRGBImage), new FlexibleImage(fluoMask), null, getBackground(), 1);
		o.mergeMasks();

		// PrintImage.printImage(ImageConverter.convert1ABto1A(o.getMask()), rgbMask.getWidth(), rgbMask.getHeight());

		// ImageOperation save = new ImageOperation(ImageConverter.convert1ABtoBI(rgbMask.getWidth(), rgbMask.getHeight(), o.getMask()));
		// save.rotate(3);
		// save.saveImage("/Users/entzian/Desktop/beideMaskenBild.png");

		// modify source images according to merged mask
		int i = 0;
		for (int m : o.getMask()) {
			if (m == 0) {
				rgbImage1A[i] = getBackground();
				fluoImage1A[i] = getBackground();
			}
			i++;
		}

		newRGBImage = ImageConverter.convert1AtoBI(rgbMask.getWidth(), rgbMask.getHeight(), rgbImage1A);
		newFluorImage = ImageConverter.convert1AtoBI(fluoMask.getWidth(), fluoMask.getHeight(), fluoImage1A);

		return newRGBImage;

		// todo rotate and try...
		// int largestResultImageSize = -1;
		// double bestAngle = 0, bestScale = 1;
		// MaskOperation bestMask = null;

		// // try different rotations
		// for (double angle = -5; angle <= 5; angle += 0.5) {
		//
		// ImageOperation io = new ImageOperation(fluoMask);
		// io.rotate(angle);
		// BufferedImage rotFluo = io.getImageAsBufferedImage();
		//
		// MaskOperation o = new MaskOperation(rgbMask, rotFluo, getBackground());
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
		// MaskOperation o = new MaskOperation(rgbMask, fluoMask, getBackground());
		// o.mergeMasks();
		// bestMask = o;
		// }
		//
		// if (Math.abs(bestAngle) > 0.001) {
		// System.out.println("Detected plant rotation within snapshot: " + bestAngle + " degree");
		// {
		// // rotate modified source image
		// ImageOperation io = new ImageOperation(newFluorImage);
		// io.rotate(bestAngle);
		// newFluorImage = io.getImageAsBufferedImage();
		// }
		// {
		// // rotate mask
		// ImageOperation io = new ImageOperation(fluoMask);
		// io.rotate(bestAngle);
		// fluoMask = io.getImageAsBufferedImage();
		// }
		// }
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
		// MaskOperation o = new MaskOperation(rgbMask, rotFluo, getBackground());
		// o.mergeMasks();
		//
		// if (o.getModifiedPixels() > largestResultImageSize) {
		// bestScale = scale;
		// largestResultImageSize = o.getModifiedPixels();
		// bestMask = o;
		// }
		// }
		// if (Math.abs(bestScale - 1) > 0.001) {
		// System.out.println("Detected difference of scaling in comparison to pre-defined scaling. Difference: " + (int) (bestScale * 100) + " %");
		// ImageOperation io = new ImageOperation(newFluorImage);
		// io.scale(bestScale, bestScale * yscale);
		// newFluorImage = io.getImageAsBufferedImage();
		// }

		// MaskOperation o = new MaskOperation(rgbMask, rotFluo, getBackground());
		// o.mergeMasks();
		//
		// int[] rgbImage1A = ImageConverter.convertBIto1A(newRGBImage);
		//
		// int[] fluoImage1A = ImageConverter.convertBIto1A(newFluorImage);
		//
		// // modify source images according to merged mask
		// int i = 0;
		// for (int m : bestMask.getMask()) {
		// if (m == 0) {
		// rgbImage1A[i] = getBackground();
		// fluoImage1A[i] = getBackground();
		// }
		// i++;
		// }
		//
		// newRGBImage = ImageConverter.convert1AtoBI(rgbMask.getWidth(), rgbMask.getHeight(), rgbImage1A);
		// newFluorImage = ImageConverter.convert1AtoBI(fluoMask.getWidth(), fluoMask.getHeight(), fluoImage1A);
		//
		// return newRGBImage;
	}

	private BufferedImage fitMaskToNearImage(BufferedImage mask) {

		int[] nearMask1A = ImageConverter.convertBIto1A(newNearImage);

		ImageOperation io = new ImageOperation(mask);
		io.resize(newNearImage.getWidth(), newNearImage.getHeight());
		io.rotate(-9);

		int i = 0;
		for (int m : io.getImageAs1array()) {
			if (m == background) {
				nearMask1A[i] = background;
			}
			i++;
		}

		return ImageConverter.convert1AtoBI(newNearImage.getWidth(), newNearImage.getHeight(), nearMask1A);
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
		for (int m : io.getImageAs1array()) {
			if (m == background)
				newImage1A[idx] = background;
			else
				newImage1A[idx] = originalImage1A[idx];
			idx++;
		}

		return ImageConverter.convert1AtoBI(workImage.getWidth(), workImage.getHeight(), newImage1A);
	}

	private void imageLayering(BufferedImage firstImage, BufferedImage secondImage, LayeringTyp typ) {

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
		PrintImage.printImage(secondImage, "imageLayering 'geht'");
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
