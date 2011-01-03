package de.ipk.ag_ba.image_utils;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.process.BinaryProcessor;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

public class ImageOperation extends ImageConverter {
	
	private final ImagePlus image;
	private ImageProcessor processor;
	
	// private Roi boundingBox;
	
	public ImageOperation(ImagePlus image) {
		this.image = image;
		this.processor = image.getProcessor();
	}
	
	public ImageOperation(BufferedImage image) {
		this(ImageConverter.convertBItoIJ(image));
	}
	
	public ImageOperation(FlexibleImage image) {
		this(image.getConvertAsImagePlus());
	}
	
	public ImageOperation(int[] image, int width, int height) {
		this(ImageConverter.convert1AtoIJ(width, height, image));
	}
	
	public ImageOperation(int[][] image) {
		this(ImageConverter.convert2AtoIJ(image));
	}
	
	// public ImageOperation(float[][] image) {
	// this(new ImagePlus("JImage", new FloatProcessor(image)));
	// }
	
	public ImageOperation translate(double x, double y) {
		processor.translate(x, y);
		return new ImageOperation(getImage());
	}
	
	public ImageOperation rotate(double degree) {
		processor.rotate(degree);
		return new ImageOperation(getImage());
	}
	
	public ImageOperation scale(double xScale, double yScale) {
		processor.scale(xScale, yScale);
		return new ImageOperation(getImage());
	}
	
	public ImageOperation resize(int width, int height) {
		processor = processor.resize(width, height);
		image.setProcessor(processor);
		
		return new ImageOperation(getImage());
		// return this;
	}
	
	public ImageOperation resize(double factor) {
		return resize((int) (factor * image.getWidth()), (int) (factor * image.getHeight()));
	}
	
	public void threshold(int cutValue) {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		byteProcessor.threshold(cutValue);
		image.setProcessor(processor2.convertToRGB());
	}
	
	// private static long iiii = 0;
	
	public ImageOperation applyMask(FlexibleImage mask, int background) {
		
		if (image.getWidth() != mask.getWidth() || image.getHeight() != mask.getHeight()) {
			mask = new ImageOperation(mask).resize(image.getWidth(), image.getHeight()).getImage();
		}
		
		int[] mask1A = mask.getConvertAs1A();
		
		int[] originalImage = ImageConverter.convertIJto1A(image);
		// PrintImage.printImage(image.getBufferedImage(), "IMAGE " + iiii);
		// PrintImage.printImage(mask.getBufferedImage(), "MASK FOR IMAGE " + iiii);
		
		int idx = 0;
		// int background = processor.getBackground();
		// int foreground = Color.BLUE.getRGB();
		for (int m : mask.getConvertAs1A()) {
			if (m == background)
				mask1A[idx] = background;
			else
				mask1A[idx] = originalImage[idx];
			idx++;
		}
		
		// PrintImage.printImage(mask1A, image.getWidth(), image.getHeight());
		
		return new ImageOperation(mask1A, mask.getWidth(), mask.getHeight());
		// int idx = 0;
		// for (int m : io.getImageAs1array()) {
		// if (m == background)
		// newImage1A[idx] = background;
		// else
		// newImage1A[idx] = originalImage1A[idx];
		// idx++;
	}
	
	public ImageOperation applyMask2(FlexibleImage mask, int background) {
		
		if (image.getWidth() != mask.getWidth() || image.getHeight() != mask.getHeight()) {
			mask = new ImageOperation(mask).resize(image.getWidth(), image.getHeight()).getImage();
		}
		
		int[] mask1A = mask.getConvertAs1A();
		
		int[] originalImage = ImageConverter.convertIJto1A(image);
		// PrintImage.printImage(image.getBufferedImage(), "IMAGE " + iiii);
		// PrintImage.printImage(mask.getBufferedImage(), "MASK FOR IMAGE " + iiii);
		
		int idx = 0;
		// int background = processor.getBackground();
		// int foreground = Color.BLUE.getRGB();
		for (int m : mask.getConvertAs1A()) {
			if (m == background)
				mask1A[idx] = background;
			else
				mask1A[idx] = originalImage[idx];
			idx++;
		}
		
		// PrintImage.printImage(mask1A, image.getWidth(), image.getHeight());
		
		return new ImageOperation(mask1A, mask.getWidth(), mask.getHeight());
		// int idx = 0;
		// for (int m : io.getImageAs1array()) {
		// if (m == background)
		// newImage1A[idx] = background;
		// else
		// newImage1A[idx] = originalImage1A[idx];
		// idx++;
	}
	
	/**
	 * Enlarge area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public void dilate(int[][] mask) {
		int jM = (mask.length - 1) / 2;
		int iM = (mask[0].length - 1) / 2;
		
		ImageProcessor tempImage = processor.createProcessor(processor.getWidth(), processor.getHeight());
		
		for (int j = 0; j < mask.length; j++)
			for (int i = 0; i < mask[j].length; i++)
				tempImage.copyBits(processor, i - iM, j - jM, Blitter.MAX);
		
		// for (int i = 0; i < mask.length; i++)
		// for (int j = 0; j < mask[i].length; j++)
		// tempImage.copyBits(processor, j - jM, i - iM, Blitter.MAX);
		
		processor.copyBits(tempImage, 0, 0, Blitter.COPY);
	}
	
	/**
	 * Enlarge area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageOperation dilate() { // es wird der 3x3 Minimum-Filter genutzt
		processor.dilate();
		return this;
	}
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode(ImageProcessor temp, int[][] mask) {
		temp.invert();
		dilate(mask);
		temp.invert();
		return this;
	}
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode(ImageProcessor temp) {
		temp.erode();
		return this;
	}
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode(int[][] mask) {
		return erode(processor, mask);
	}
	
	// public void erode2(int [][] mask){
	// processor.invert();
	// dilate(mask);
	// processor.invert();
	// }
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode() { // es wird der 3x3 Minimum-Filter genutzt
		return erode(processor);
	}
	
	/**
	 * Dilation, then erosion. Removes small holes in the image.
	 * <p>
	 * The closing of the dark-blue shape (union of two squares) by a disk, resulting in the union of the dark-blue shape and the light-blue areas.:<br>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/2/2e/Closing.png/220px-Closing.png" >
	 */
	public void closing(int[][] mask) {
		dilate(mask);
		erode(mask);
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask.
	 * <p>
	 * The closing of the dark-blue shape (union of two squares) by a disk, resulting in the union of the dark-blue shape and the light-blue areas:<br>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/2/2e/Closing.png/220px-Closing.png" >
	 */
	public void closing() { // es wird der 3x3 Minimum-Filter genutzt
		processor.dilate();
		processor.erode();
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask.
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public void opening(int[][] mask) {
		erode(mask);
		dilate(mask);
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask.
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public void opening() { // es wird der 3x3 Minimum-Filter genutzt
		processor.erode();
		processor.dilate();
	}
	
	public void skeletonize() {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		byteProcessor.skeletonize();
		image.setProcessor(processor2.convertToRGB());
	}
	
	public void outline(int[][] mask) { // starke Farbübergänge werden als Kante
		// erkannt
		ImageProcessor tempImage = processor.duplicate();
		erode(tempImage, mask);
		processor.copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		processor.invert();
	}
	
	public void outline() {
		ImageProcessor processor2 = processor.convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		byteProcessor.outline();
		image.setProcessor(processor2.convertToRGB());
	}
	
	public void outline2() {
		ImageProcessor tempImage = processor.duplicate();
		erode(tempImage);
		processor.copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		processor.invert();
		
	}
	
	public void gamma(double value) {
		processor.gamma(value);
	}
	
	public void drawRect(int leftX, int leftY, int width, int heigh) {
		processor.drawRect(leftX, leftY, width, heigh);
		
	}
	
	public ImageOperation drawAndFillRect(int leftX, int leftY, int[][] fillValue) {
		
		int width = fillValue.length;
		int height = fillValue[0].length;
		
		int[][] bigImage = ImageConverter.convertIJto2A(image);
		
		for (int x = leftX; x < leftX + width && x < bigImage.length; x++)
			for (int y = leftY; y < leftY + height && y < bigImage[0].length; y++)
				bigImage[x][y] = fillValue[x - leftX][y - leftY];
		
		return new ImageOperation(ImageConverter.convert2AtoIJ(bigImage));
	}
	
	public ImageOperation drawAndFillRect(int leftX, int leftY, int width, int height, int fillValue) {
		Roi rec = new Roi(leftX, leftY, width, height);
		processor.setRoi(rec);
		processor.setValue(fillValue);
		processor.fill();
		return new ImageOperation(processor.getBufferedImage());
	}
	
	public ImageOperation setBackgroundValue(double background) {
		processor.setBackgroundValue(background);
		return new ImageOperation(processor.getBufferedImage());
	}
	
	public Roi getBoundingBox() {
		// public void boundingBox(int background){
		
		int[][] img = ImageConverter.convertIJto2A(image);
		int top = img.length, left = img[0].length, right = -1, down = -1;
		
		double background = processor.getBackgroundValue();
		
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[0].length; j++) {
				if (img[i][j] != background) {
					if (i < top)
						top = i;
					
					if (j < left)
						left = j;
				} else {
					if (j > 0)
						if (img[i][j - 1] != background && j > left && j > right) {
							right = j;
						}
					if (i > 0)
						if (img[i - 1][j] != background && i > top && i > down) {
							down = i;
						}
				}
			}
		}
		
		if (right == -1)
			right = img[0].length;
		if (down == -1)
			down = img.length;
		
		// System.out.println("BoundingBox");
		// System.out.println("top: " + top);
		// System.out.println("left: " + left);
		// System.out.println("down-top: " + (down - top));
		// System.out.println("right-left: " + (right - left));
		
		Roi boundingBox = new Roi(top, left, down - top, right - left);
		// boundingBox = new Roi(top, left, down-top, right-left);
		// processor.setRoi(boundingBox);
		
		return boundingBox;
	}
	
	// public void drawBoundingBox(){
	public void drawBoundingBox(Roi boundingBox) {
		processor.setRoi(boundingBox);
		processor.draw(boundingBox);
	}
	
	public ImageOperation cutArea(Roi boundingBox) {
		processor.setRoi(boundingBox);
		return new ImageOperation(processor.crop().getBufferedImage());
	}
	
	public Dimension2D centerOfGravity() {
		
		int[][] img = ImageConverter.convertIJto2A(image);
		
		// int [][] img = new int[5][5];
		// for(int i = 0 ; i < 5; i++)
		// for(int j = 0; j < 5; j++)
		// img[i][j] = i*j;
		
		// int[][] img = { { 1, 1, 1, 1, 1, 1, 1 },
		// { 1, 1, 1, 1, 1, 1, 1 },
		// { 1, 1, 1, 1, 1, 1, 1 },
		// { 1, 1, 1, 1, 1, 1, 1 },
		// { 1, 1, 1, 1, 1, 1, 1 }};
		
		// int[][] img = { { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 }};
		
		// int[][] img = { { 0, 0, 1, 1, 1, 0, 0 },
		// { 0, 0, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 }};
		
		// int[][] img = { { 0, 0, 1, 1, 1, 0, 0 },
		// { 0, 0, 1, 1, 1, 0, 0 },
		// { 0, 0, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 }};
		
		// int[][] img = { { 0, 0, 1, 1, 1, 0, 0 },
		// { 0, 0, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 1, 1, 0, 0 },
		// { 1, 1, 1, 0, 0, 0, 0 },
		// { 1, 1, 1, 0, 0, 0, 0 }};
		
		// int[][] img = { { 0, 0, 0, 0, 1, 1, 1 },
		// { 0, 0, 0, 0, 1, 1, 1 },
		// { 1, 1, 1, 0, 1, 1, 1 },
		// { 1, 1, 1, 0, 0, 0, 0 },
		// { 1, 1, 1, 0, 0, 0, 0 }};
		
		// int[][] img = { { 0, 0, 0, 0, 0, 0, 0 },
		// { 0, 0, 0, 0, 0, 0, 0 },
		// { 1, 1, 1, 0, 1, 1, 1 },
		// { 1, 1, 1, 0, 1, 1, 1 },
		// { 1, 1, 1, 0, 1, 1, 1 }};
		//
		// int[][] img = { { 0, 1, 1, 0, 1, 0, 1, 0 },
		// { 1, 1, 0, 0, 1, 1, 1, 0 },
		// { 0, 1, 1, 1, 1, 0, 1, 0 },
		// { 0, 0, 0, 0, 0, 1, 1, 0 },
		// { 0, 1, 1, 1, 0, 0, 0, 1 },
		// { 1, 1, 1, 1, 1, 0, 0, 0 } };
		//
		
		// PrintImage.printImage(getImageAs2array(), PrintOption.CONSOLE);
		// threshold(254);
		// PrintImage.printImage(getImageAs2array(), PrintOption.CONSOLE);
		double area = 0;
		double firstMomentOfAreaJ = 0;
		double firstMomentOfAreaI = 0;
		double centreOfGravityJ = 0;
		double centreOfGravityI = 0;
		Dimension2D centerPoint = null;
		
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[0].length; j++) {
				area = area + img[i][j];
				firstMomentOfAreaI = firstMomentOfAreaI + (double) (i + 1) * img[i][j];
				firstMomentOfAreaJ = firstMomentOfAreaJ + (double) (j + 1) * img[i][j];
			}
		}
		
		if (area != 0) {
			centreOfGravityJ = (firstMomentOfAreaJ / area) - 1;
			centreOfGravityI = (firstMomentOfAreaI / area) - 1;
			
		}
		
		// drawAndFillRect((int) centreOfGravityI - 5, (int) centreOfGravityJ - 5, 10, 10, 200);
		
		// System.out.println("SchwerpunktX: " + centreOfGravityI);
		// System.out.println("SchwerpunktY: " + centreOfGravityJ);
		
		centerPoint.setSize(centreOfGravityI, centreOfGravityJ);
		return centerPoint;
		// return new Dimension2D(centreOfGravityI, centreOfGravityJ);
		// return new Point2D.Double(centreOfGravityI, centreOfGravityJ);
	}
	
	public Dimension2D getDiameter() {
		
		Dimension2D dimension = null;
		double minArea = 1000000000000000.0;
		
		double width = 0, hight = 0;
		
		Roi boundingBox;
		
		for (int i = 0; i < 1; i++) {
			rotate(i);
			
			Rectangle rect = getBoundingBox().getBounds();
			width = rect.getWidth();
			hight = rect.getHeight();
			if (minArea > (width * hight)) {
				// minArea = width*hight;
				dimension.setSize(width, hight);
			}
			
		}
		
		// System.out.println("minArea: " + minArea);
		
		return dimension;
	}
	
	public ImageOperation enlarge() {
		
		int[][] img = ImageConverter.convertIJto2A(image);
		int[][] newImage = new int[2 * img.length][2 * img[0].length];
		
		for (int i = 0; i < newImage.length; i++)
			for (int j = 0; j < newImage[0].length; j++) {
				if (i > (img.length * 0.5) && j > (img[0].length * 0.5) && i < (img.length * 1.5) && j < (img[0].length * 1.5)) {
					newImage[i][j] = img[(i - (img.length / 2) - 1)][(j - (img[0].length / 2) - 1)];
				} else
					newImage[i][j] = (int) processor.getBackgroundValue();
			}
		
		return new ImageOperation(ImageConverter.convert2AtoBI(newImage));
	}
	
	public ImageOperation removeSmallClusters() {
		int[] arrayRGB = ImageConverter.convertIJto1A(image);
		PhenotypeAnalysisTask.removeSmallPartsOfImage(image.getWidth(), image.getHeight(), arrayRGB, PhenotypeAnalysisTask.BACKGROUND_COLORint,
							(int) (image.getWidth() * image.getHeight() * 0.005d));
		return new ImageOperation(arrayRGB, image.getWidth(), image.getHeight());
	}
	
	// ################## get... ###################
	
	public int[] getImageAs1array() {
		return ImageConverter.convertIJto1A(image);
	}
	
	public int[][] getImageAs2array() {
		return ImageConverter.convertIJto2A(image);
	}
	
	public BufferedImage getImageAsBufferedImage() {
		return ImageConverter.convertIJtoBI(image);
	}
	
	public ImagePlus getImageAsImagePlus() {
		return image;
	}
	
	// ############# print #####################
	
	public void printImage() {
		image.updateAndDraw();
		image.show();
	}
	
	// ############# save ######################
	
	public void saveImage(String pfad) {
		saveImage(pfad, SaveImage.PNG);
	}
	
	public void saveImage(String pfad, SaveImage typ) {
		
		switch (typ) {
			case TIFF:
				new FileSaver(image).saveAsTiff(pfad);
				break;
			case TIFF_STACK:
				new FileSaver(image).saveAsTiffStack(pfad);
				break;
			case PNG:
				new FileSaver(image).saveAsPng(pfad);
				break;
			case JPG:
				new FileSaver(image).saveAsJpeg(pfad);
				break;
			case GIF:
				new FileSaver(image).saveAsGif(pfad);
				break;
			default:
				new FileSaver(image).saveAsPng(pfad);
				break;
		}
		
	}
	
	// private void blur() {
	// ImageProcessor processor2 = processor.convertToByte(true);
	// ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
	//
	// boolean newTest = true;
	//
	// if (newTest) {
	// IOurl test = new IOurl("file:///Users/entzian/Desktop/test.png");
	// try {
	// BufferedImage imgTest = ImageIO.read(test.getInputStream());
	// PrintImage.printImage(imgTest);
	// ImageOperation io = new ImageOperation(imgTest);
	// // io.drawRect(3, 3, 10, 10);
	// // io.drawAndFillRect(3, 3, 10, 10, 0);
	// // io.setBackgroundValue(-1);
	// // Roi testRoi = io.boundingBox();
	// // io.drawBoundingBox(testRoi);
	// // io.cutArea(testRoi);
	//
	// io.centerOfGravity();
	// PrintImage.printImage(io.getImageAsBufferedImage());
	//
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// } else {
	//
	// int w = byteProcessor.getWidth();
	// int h = byteProcessor.getHeight();
	// ByteProcessor copy = (ByteProcessor) processor2.duplicate();
	// for (int v = 1; v <= h - 2; v++) {
	// for (int u = 1; u <= w - 2; u++) {
	// // compute filter result for position (u,v)
	// int sum = 0;
	// for (int j = -1; j <= 1; j++) {
	// for (int i = -1; i <= 1; i++) {
	// int p = copy.getPixel(u + i, v + j);
	// sum = sum + p;
	// }
	// }
	// int q = (int) (sum / 9.0);
	// byteProcessor.putPixel(u, v, q);
	// }
	// }
	//
	// image.setProcessor(processor2.convertToRGB());
	// }
	// }
	
	private static void testPhytokammer(IOurl urlFlu, IOurl urlVis, IOurl urlNIR, BufferedImage imgFluo,
						BufferedImage imgVisible, BufferedImage imgNIR) {
		
		int[] fluoImageOriginal = ImageConverter.convertBIto1A(imgFluo);
		int[] rgbImageOriginal = ImageConverter.convertBIto1A(imgVisible);
		int[] nirImageOriginal = ImageConverter.convertBIto1A(imgNIR);
		
		int[] fluoImage;
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		{
			SubstanceInterface substance = new Substance();
			substance.setName(ImageConfiguration.FluoTop.toString());
			ConditionInterface condition = new Condition(substance);
			Sample sample = new Sample(condition);
			LoadedImage limg = new LoadedImage(sample, imgFluo);
			limg.setURL(urlFlu);
			
			PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null,
								0.5, 0.5);
			
			// MainFrame
			// .showMessageWindow("FluoTop Clean", new JLabel(
			// new ImageIcon(limg.getLoadedImage())));
		}
		{
			SubstanceInterface substance = new Substance();
			substance.setName(ImageConfiguration.RgbTop.toString());
			ConditionInterface condition = new Condition(substance);
			Sample sample = new Sample(condition);
			LoadedImage limg = new LoadedImage(sample, imgVisible);
			limg.setURL(urlVis);
			PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null,
								2.5, 2.5);
			
			// MainFrame.showMessageWindow("RgbTop Clean", new JLabel(
			// new ImageIcon(limg.getLoadedImage())));
		}
		
		int[] rgbImage = ImageConverter.convertBIto1A(imgVisible);
		fluoImage = ImageConverter.convertBIto1A(imgFluo);
		
		// modify masks
		ImageOperation ioR = new ImageOperation(rgbImage, imgVisible.getWidth(), imgVisible.getHeight());
		ioR.erode().erode();
		ioR.dilate().dilate().dilate().dilate().dilate();
		
		ImageOperation ioF = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
		for (int i = 0; i < 4; i++)
			ioF.erode();
		for (int i = 0; i < 20; i++)
			ioF.dilate();
		
		int[] rgbImageM = ioR.getImageAs1array();
		int[] fluoImageM = ioF.getImageAs1array();
		
		BufferedImage imgFluoTest = ImageConverter.convert1AtoBI(imgFluo.getWidth(), imgFluo.getHeight(), fluoImageM);
		ImagePlus imgFFTest = ImageConverter.convertBItoIJ(imgFluoTest);
		imgFFTest.show("Fluorescence Vorstufe1");
		
		// merge infos of both masks
		int background = PhenotypeAnalysisTask.BACKGROUND_COLOR.getRGB();
		MaskOperation o = new MaskOperation(ioR.getImage(), ioF.getImage(), null, background, 1);
		o.mergeMasks();
		
		// modify source images according to merged mask
		int i = 0;
		for (int m : o.getMask()) {
			if (m == 0) {
				rgbImage[i] = background;
				fluoImage[i] = background;
			}
			i++;
		}
		
		// BufferedImage imgFluoTest2 =
		// ImageConverter.convert1AtoBI(imgFluo.getWidth(),imgFluo.getHeight(),
		// fluoImage);
		// ImagePlus imgFFTest2 = ImageConverter.convertBItoIJ(imgFluoTest2);
		// imgFFTest2.show("Fluorescence Vorstufe2");
		
		{
			
			SubstanceInterface substance = new Substance();
			substance.setName(ImageConfiguration.NirTop.toString());
			ConditionInterface condition = new Condition(substance);
			Sample sample = new Sample(condition);
			LoadedImage limg = new LoadedImage(sample, imgNIR);
			limg.setURL(urlNIR);
			PhenotypeAnalysisTask.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null, 1,
								0.5);
			
			int[] nirImage = ImageConverter.convertBIto1A(imgNIR);
			
			// process NIR
			
			MainFrame.showMessageWindow("NIR Source", new JLabel(new ImageIcon(imgNIR)));
			
			int[] mask = rgbImage;
			// resize mask
			ImageOperation ioo = new ImageOperation(mask, imgVisible.getWidth(), imgVisible.getHeight());
			ioo.resize(imgNIR.getWidth(), imgNIR.getHeight());
			ioo.rotate(-9);
			i = 0;
			for (int m : ioo.getImageAs1array()) {
				if (m == background) {
					nirImage[i] = background;
				}
				i++;
			}
			imgNIR = ImageConverter.convert1AtoBI(imgNIR.getWidth(), imgNIR.getHeight(), nirImage);
		}
		
		{ // fluo störungen beseitigen
			ImageOperation ioFF = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
			for (int ii = 0; ii < 5; ii++)
				ioFF.erode();
			for (int ii = 0; ii < 5; ii++)
				ioFF.dilate();
			ioFF.closing();
			
			int idx = 0;
			for (int m : ioFF.getImageAs1array()) {
				if (m == background)
					fluoImage[idx] = background;
				else
					fluoImage[idx] = fluoImageOriginal[idx];
				idx++;
			}
		}
		
		{ // rgb störungen beseitigen
			ImageOperation ioFF = new ImageOperation(rgbImage, imgVisible.getWidth(), imgVisible.getHeight());
			for (int ii = 0; ii < 6; ii++)
				ioFF.erode();
			for (int ii = 0; ii < 8; ii++)
				ioFF.dilate();
			for (int ii = 0; ii < 2; ii++)
				ioFF.erode();
			// for (int ii=0; ii<1; ii++)
			// ioFF.erode(new int [][]
			// {{0,0,1,0,0},{0,1,1,1,0},{1,1,1,1,1},{0,1,1,1,0},{0,0,1,0,0}});
			
			int idx = 0;
			for (int m : ioFF.getImageAs1array()) {
				if (m == background)
					rgbImage[idx] = background;
				else
					rgbImage[idx] = rgbImageOriginal[idx];
				idx++;
			}
		}
		
		imgVisible = ImageConverter.convert1AtoBI(imgVisible.getWidth(), imgVisible.getHeight(), rgbImage);
		imgFluo = ImageConverter.convert1AtoBI(imgFluo.getWidth(), imgFluo.getHeight(), fluoImage);
		
		ImagePlus imgVV = ImageConverter.convertBItoIJ(imgVisible);
		ImagePlus imgFF = ImageConverter.convertBItoIJ(imgFluo);
		ImagePlus imgNN = ImageConverter.convertBItoIJ(imgNIR);
		
		imgVV.show("Visible");
		imgFF.show("Fluorescence");
		imgNN.show("NIR");
	}
	
	private static void showTwoImagesAsOne(BufferedImage imgF2, BufferedImage imgV2) {
		
		imgF2 = ImageConverter.convert1AtoBI(imgF2.getWidth(), imgF2.getHeight(), ImageConverter.convertBIto1A(imgF2));
		imgV2 = ImageConverter.convert1AtoBI(imgV2.getWidth(), imgV2.getHeight(), ImageConverter.convertBIto1A(imgV2));
		
		for (int x = 0; x < imgV2.getWidth(); x++) {
			for (int y = 0; y < imgV2.getHeight(); y++) {
				boolean twoInOne = false;
				if (twoInOne) {
					Color f = new Color(imgV2.getRGB(x, y));
					Color f2 = new Color(imgF2.getRGB(x, y));
					Color f3 = new Color(f2.getRed(), 0, f.getBlue());
					
					imgF2.setRGB(x, y, f3.getRGB());
				} else {
					if ((y / 3) % 2 == 0)
						imgF2.setRGB(x, y, imgV2.getRGB(x, y));
					else
						imgF2.setRGB(x, y, imgF2.getRGB(x, y));
				}
			}
		}
		
		GravistoService.showImage(imgF2, "Vergleich");
	}
	
	public FlexibleImage getImage() {
		return new FlexibleImage(image);
	}
	
	public static void main(String[] args) {
		
		boolean newTest = true;
		
		if (newTest) {
			
			int[][] img = { { 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 },
								{ 1, 1, 1, 1, 1, 1, 1 } };
			
			IOurl test = new IOurl("file:///Users/entzian/Desktop/test.png");
			try {
				BufferedImage imgTest = ImageIO.read(test.getInputStream());
				// imgTest = ImageConverter.convert2AtoBI(img);
				// PrintImage.printImage(imgTest, PrintOption.CONSOLE);
				PrintImage.printImage(imgTest, "entzian main 1");
				ImageOperation io = new ImageOperation(imgTest);
				// io.drawRect(3, 3, 10, 10);
				// io.drawAndFillRect(3, 3, 10, 10, 0);
				// io.setBackgroundValue(-1);
				// Roi testRoi = io.boundingBox();
				// io.drawBoundingBox(testRoi);
				// io.cutArea(testRoi);
				// io.drawBoundingBox(io.getBoundingBox());
				// PrintImage.printImage(io.getImageAsBufferedImage());
				// Dimension2D testPoint = io.enlarge().getDiameter();
				// io.cutArea(io.boundingBox());
				
				PrintImage.printImage(io.enlarge().getImageAsBufferedImage(), "entzian main 2");
				// io.centerOfGravity();
				PrintImage.printImage(io.getImageAsBufferedImage(), "entzian main 3");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			
			boolean tempTests = false;
			if (tempTests) {
				try {
					IOurl test;
					boolean zeigen1 = false;
					
					if (zeigen1)
						test = new IOurl("file:///Users/entzian/Desktop/test.png");
					else
						test = new IOurl("file:///Users/entzian/Desktop/nir_test.png");
					
					// IOurl test = new
					// IOurl("mongo://26b7e285fae43dac107016afb4dc2841/WT01_1385");
					// ResourceIOManager.registerIOHandler(new MongoDBhandler());
					
					// ResourceIOManager.registerIOHandler(new
					// LemnaTecFTPhandler());
					BufferedImage imgTest = ImageIO.read(test.getInputStream());
					
					double scale = 2.0;
					if (Math.abs(scale - 1) > 0.0001) {
						System.out.println("Scaling!");
						imgTest = new ImageOperation(imgTest).resize(scale).getImageAsBufferedImage();
					}
					
					GravistoService.showImage(imgTest, "Ausgang");
					
					int[] fluoImage = ImageConverter.convertBIto1A(imgTest);
					
					if (zeigen1) {
						
						ImageOperation io1 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io1.dilate();
						GravistoService.showImage(io1.getImageAsBufferedImage(), "Dilation");
						
						ImageOperation io2 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io2.erode();
						GravistoService.showImage(io2.getImageAsBufferedImage(), "Erode");
						
						// ImageOperation io2 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io2.erode(new int[][] {{1,1,1},{1,1,1},{1,1,1}});
						// GravistoService.showImage(io2.getImageAsBufferedImage(),
						// "Erode1");
						//
						// ImageOperation io2_1 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io2_1.erode2(new int[][] {{1,1,1},{1,1,1},{1,1,1}});
						// GravistoService.showImage(io2_1.getImageAsBufferedImage(),
						// "Erode2");
						
						ImageOperation io3 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io3.opening();
						GravistoService.showImage(io3.getImageAsBufferedImage(), "Opening");
						
						ImageOperation io4 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io4.closing();
						GravistoService.showImage(io4.getImageAsBufferedImage(), "Closing");
						
						ImageOperation io5 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io5.outline();
						GravistoService.showImage(io5.getImageAsBufferedImage(), "Outline");
						
						ImageOperation io5_2 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io5_2.outline2();
						GravistoService.showImage(io5_2.getImageAsBufferedImage(), "Outline2");
						
						ImageOperation io5_1 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io5_1.outline(new int[][] { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } });
						GravistoService.showImage(io5_1.getImageAsBufferedImage(), "Outline1");
						
						// ImageOperation io5_3 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io5_3.outline(new int[][] {{0,1,0},{1,1,1},{0,1,0}});
						// GravistoService.showImage(io5_3.getImageAsBufferedImage(),
						// "Outline1_2");
						//
						// ImageOperation io5_4 = new ImageOperation(fluoImage,
						// imgTest.getWidth(), imgTest.getHeight());
						// io5_4.outline(new int[][] {{0,1,0},{1,2,1},{0,1,0}});
						// GravistoService.showImage(io5_4.getImageAsBufferedImage(),
						// "Outline1_3");
						
						ImageOperation io6 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io6.skeletonize();
						GravistoService.showImage(io6.getImageAsBufferedImage(), "Skeletonize");
						
						ImageOperation io7 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io7.threshold(254);
						GravistoService.showImage(io7.getImageAsBufferedImage(), "Threshold");
						
						ImageOperation io8 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						io8.gamma(2.0);
						GravistoService.showImage(io8.getImageAsBufferedImage(), "Gamma");
						
					} else {
						
						ImageOperation io10 = new ImageOperation(fluoImage, imgTest.getWidth(), imgTest.getHeight());
						
						// io10.skeletonize();
						io10.outline(new int[][] { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } });
						io10.gamma(3.5);
						for (int i = 0; i < 1; i++)
							io10.dilate();
						// io10.threshold(153);
						// io10.opening(new int[][] { { 1, 1, 1 }, { 1, 1, 1 },
						// { 1, 1, 1 } });
						
						// io10.blur();
						
						GravistoService.showImage(io10.getImageAsBufferedImage(), "Ergebnis");
						
					}
					// io.printImage();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else {
				
				try {
					IOurl urlFlu = new IOurl("mongo_ba-13.ipk-gatersleben.de://26b7e285fae43dac107016afb4dc2841/WT01_1385");
					IOurl urlVis = new IOurl("mongo_ba-13.ipk-gatersleben.de://12b6db018fddf651b414b652fc8f3d8d/WT01_1385");
					IOurl urlNIR = new IOurl("mongo_ba-13.ipk-gatersleben.de://c72e4fcc141b8b2a97851ab2fde8106a/WT01_1385");
					
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
					
					// resize
					int[] fluoImage = ImageConverter.convertBIto1A(imgFluo);
					
					ImageOperation io = new ImageOperation(fluoImage, imgFluo.getWidth(), imgFluo.getHeight());
					io.resize(imgVisible.getWidth(), imgVisible.getHeight());
					io.scale(0.95, 0.87);
					io.translate(0, -15 * scale);
					io.rotate(-3);
					
					imgFluo = ImageConverter
										.convert1AtoBI(imgVisible.getWidth(), imgVisible.getHeight(), io.getImageAs1array());
					
					boolean mergeImages = false;
					
					if (mergeImages)
						showTwoImagesAsOne(imgFluo, imgVisible);
					
					boolean test = true;
					if (test) {
						testPhytokammer(urlFlu, urlVis, urlNIR, imgFluo, imgVisible, imgNIR);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public ImageOperation invert() {
		processor.invert();
		return new ImageOperation(getImage());
	}
	
	public FlexibleImage draw(FlexibleImage fi, int background) {
		int[] img = getImageAs1array();
		int[] over = fi.getConvertAs1A();
		int idx = 0;
		for (int o : over)
			if (o != background)
				img[idx++] = o;
			else
				idx++;
		return new FlexibleImage(img, image.getWidth(), image.getHeight());
	}
}
