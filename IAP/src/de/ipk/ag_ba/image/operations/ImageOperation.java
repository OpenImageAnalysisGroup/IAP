package de.ipk.ag_ba.image.operations;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.plugin.filter.GaussianBlur;
import ij.process.BinaryProcessor;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.image.operations.segmentation.PixelSegmentation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author Entzian, Klukas
 */

public class ImageOperation extends ImageConverter {
	
	private final ImagePlus image;
	
	// private Roi boundingBox;
	
	public ImageOperation(ImagePlus image) {
		this.image = image;
	}
	
	public ImageOperation(BufferedImage image) {
		this(ImageConverter.convertBItoIJ(image));
	}
	
	public ImageOperation(FlexibleImage image) {
		this(image.getAsImagePlus());
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
		image.getProcessor().translate(x, y);
		return new ImageOperation(getImage()).replaceColors(Color.BLACK.getRGB(), PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}
	
	public ImageOperation replaceColors(int search, int replace) {
		int[] a = getImageAs1array();
		int[] b = new int[a.length];
		
		int idx = 0;
		for (int v : a) {
			if (v != search)
				b[idx++] = v;
			else
				b[idx++] = replace;
		}
		return new ImageOperation(b, getImage().getWidth(), getImage().getHeight());
	}
	
	public ImageOperation rotate(double degree) {
		image.getProcessor().rotate(degree);
		return new ImageOperation(getImage()).replaceColors(Color.BLACK.getRGB(), PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}
	
	public ImageOperation scale(double xScale, double yScale) {
		image.getProcessor().scale(xScale, yScale);
		return new ImageOperation(getImage()).replaceColors(Color.BLACK.getRGB(), PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}
	
	public ImageOperation resize(int width, int height) {
		ImageProcessor p = image.getProcessor().resize(width, height);
		image.setProcessor(p);
		
		return new ImageOperation(getImage());
	}
	
	public ImageOperation resize(double factor) {
		return resize((int) (factor * image.getWidth()), (int) (factor * image.getHeight()));
	}
	
	public void threshold(int cutValue) {
		ImageProcessor processor2 = image.getProcessor().convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		byteProcessor.threshold(cutValue);
		image.setProcessor(processor2.convertToRGB());
	}
	
	// private static long iiii = 0;
	
	public ImageOperation applyMask(FlexibleImage mask, int background) {
		
		if (image.getWidth() != mask.getWidth() || image.getHeight() != mask.getHeight()) {
			mask = new ImageOperation(mask).resize(image.getWidth(), image.getHeight()).getImage();
		}
		
		int[] mask1A = mask.getAs1A();
		
		int[] originalImage = ImageConverter.convertIJto1A(image);
		// PrintImage.printImage(image.getBufferedImage(), "IMAGE " + iiii);
		// PrintImage.printImage(mask.getBufferedImage(), "MASK FOR IMAGE " + iiii);
		
		int idx = 0;
		// int background = image.getProcessor().getBackground();
		// int foreground = Color.BLUE.getRGB();
		for (int m : mask.getAs1A()) {
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
		
		FlexibleImage srcImage = new FlexibleImage(image);
		
		if (srcImage.getWidth() != mask.getWidth() || srcImage.getHeight() != mask.getHeight()) {
			srcImage = new ImageOperation(srcImage).resize(mask.getWidth(), mask.getHeight()).getImage();
		}
		
		int[] mask1A = mask.getAs1A();
		
		int[] originalImage = srcImage.getAs1A();
		// PrintImage.printImage(image.getBufferedImage(), "IMAGE " + iiii);
		// PrintImage.printImage(mask.getBufferedImage(), "MASK FOR IMAGE " + iiii);
		
		int idx = 0;
		// int background = image.getProcessor().getBackground();
		// int foreground = Color.BLUE.getRGB();
		for (int m : mask.getAs1A()) {
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
		
		ImageProcessor tempImage = image.getProcessor().createProcessor(image.getProcessor().getWidth(), image.getProcessor().getHeight());
		ImageProcessor p = image.getProcessor();
		for (int j = 0; j < mask.length; j++)
			for (int i = 0; i < mask[j].length; i++)
				tempImage.copyBits(p, i - iM, j - jM, Blitter.MAX);
		
		// for (int i = 0; i < mask.length; i++)
		// for (int j = 0; j < mask[i].length; j++)
		// tempImage.copyBits(processor, j - jM, i - iM, Blitter.MAX);
		
		image.getProcessor().copyBits(tempImage, 0, 0, Blitter.COPY);
	}
	
	/**
	 * Enlarge area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageOperation dilate() { // es wird der 3x3 Minimum-Filter genutzt
		image.getProcessor().dilate();
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
		return erode(image.getProcessor(), mask);
	}
	
	// public void erode2(int [][] mask){
	// image.getProcessor().invert();
	// dilate(mask);
	// image.getProcessor().invert();
	// }
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src="http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode() { // es wird der 3x3 Minimum-Filter genutzt
		return erode(image.getProcessor());
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
		image.getProcessor().dilate();
		image.getProcessor().erode();
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
		image.getProcessor().erode();
		image.getProcessor().dilate();
	}
	
	public void skeletonize() {
		ImageProcessor processor2 = image.getProcessor().convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		byteProcessor.skeletonize();
		image.setProcessor(processor2.convertToRGB());
	}
	
	public void outline(int[][] mask) { // starke Farbübergänge werden als Kante
		// erkannt
		ImageProcessor tempImage = image.getProcessor().duplicate();
		erode(tempImage, mask);
		image.getProcessor().copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		image.getProcessor().invert();
	}
	
	public void outline() {
		ImageProcessor processor2 = image.getProcessor().convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor) processor2);
		byteProcessor.outline();
		image.setProcessor(processor2.convertToRGB());
	}
	
	public void outline2() {
		ImageProcessor tempImage = image.getProcessor().duplicate();
		erode(tempImage);
		image.getProcessor().copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		image.getProcessor().invert();
		
	}
	
	public void gamma(double value) {
		image.getProcessor().gamma(value);
	}
	
	public void drawRect(int leftX, int leftY, int width, int heigh) {
		image.getProcessor().drawRect(leftX, leftY, width, heigh);
		
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
		image.getProcessor().setRoi(rec);
		image.getProcessor().setValue(fillValue);
		image.getProcessor().fill();
		return new ImageOperation(image.getProcessor().getBufferedImage());
	}
	
	public ImageOperation setBackgroundValue(double background) {
		image.getProcessor().setBackgroundValue(background);
		return new ImageOperation(image.getProcessor().getBufferedImage());
	}
	
	public Roi getBoundingBox() {
		// public void boundingBox(int background){
		
		int[][] img = ImageConverter.convertIJto2A(image);
		int top = img.length, left = img[0].length, right = -1, down = -1;
		
		double background = image.getProcessor().getBackgroundValue();
		
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
		// image.getProcessor().setRoi(boundingBox);
		
		return boundingBox;
	}
	
	// public void drawBoundingBox(){
	public void drawBoundingBox(Roi boundingBox) {
		image.getProcessor().setRoi(boundingBox);
		image.getProcessor().draw(boundingBox);
	}
	
	public ImageOperation cutArea(Roi boundingBox) {
		image.getProcessor().setRoi(boundingBox);
		return new ImageOperation(image.getProcessor().crop().getBufferedImage());
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
		
		// Roi boundingBox;
		
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
					newImage[i][j] = (int) image.getProcessor().getBackgroundValue();
			}
		
		return new ImageOperation(ImageConverter.convert2AtoBI(newImage));
	}
	
	public ImageOperation removeSmallClusters() {
		return removeSmallClusters(0.005d);
	}
	
	public ImageOperation removeSmallClusters(double factor) {
		FlexibleImage workImage = new FlexibleImage(image);
		workImage = removeSmallPartsOfImage(workImage, PhenotypeAnalysisTask.BACKGROUND_COLORint,
							(int) (image.getWidth() * image.getHeight() * factor));
		return new ImageOperation(workImage);
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
	// ImageProcessor processor2 = image.getProcessor().convertToByte(true);
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
	// int w = byteimage.getProcessor().getWidth();
	// int h = byteimage.getProcessor().getHeight();
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
	// byteimage.getProcessor().putPixel(u, v, q);
	// }
	// }
	//
	// image.setProcessor(processor2.convertToRGB());
	// }
	// }
	
	static void testPhytokammer(IOurl urlFlu, IOurl urlVis, IOurl urlNIR, BufferedImage imgFluo,
						BufferedImage imgVisible, BufferedImage imgNIR) throws InterruptedException {
		
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
			
			BlockClearBackground.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null,
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
			BlockClearBackground.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null,
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
			BlockClearBackground.clearBackgroundAndInterpretImage(limg, 2, null, null, true, output, null, 1,
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
	
	public static void showTwoImagesAsOne(BufferedImage imgF2, BufferedImage imgV2) {
		
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
		// waitTime(1, TimeUnit.SECONDS);
		
	}
	
	public static void waitTime(int sleepTime, TimeUnit typ) {
		try {
			System.out.println("Sleep " + sleepTime + " " + typ.name());
			typ.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public FlexibleImage getImage() {
		return new FlexibleImage(image);
	}
	
	public ImageOperation invert() {
		image.getProcessor().invert();
		return new ImageOperation(getImage());
	}
	
	public FlexibleImage draw(FlexibleImage fi, int background) {
		int[] img = getImageAs1array();
		int[] over = fi.getAs1A();
		int idx = 0;
		for (int o : over)
			if (o != background)
				img[idx++] = o;
			else
				idx++;
		return new FlexibleImage(img, image.getWidth(), image.getHeight());
	}
	
	public ImageOperation blur(double radius) {
		GaussianBlur gb = new GaussianBlur();
		gb.blurGaussian(image.getProcessor(), radius, radius, 0.001);
		return new ImageOperation(getImage());
	}
	
	public static FlexibleImage removeSmallPartsOfImage(FlexibleImage workImage, int iBackgroundFill, int cutOff) {
		
		int[] rgbArray = workImage.getAs1A();
		int w = workImage.getWidth();
		int h = workImage.getHeight();
		
		int[][] image = new int[w][h];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				if (color != iBackgroundFill) {
					image[x][y] = 1;
				} else {
					image[x][y] = 0;
				}
			}
		}
		
		boolean useVariant1 = true, useVariant2 = false, useVariant3 = false;
		
		// Variante 1
		if (useVariant1) {
			PixelSegmentation ps = new PixelSegmentation(image);
			ps.doPixelSegmentation(1);
			
			int[] clusterSizes = ps.getClusterSizeNormalized(w, h);
			int[] clusterPerimeter = ps.getPerimeter();
			double[] clusterCircleSimilarity = ps.getCircuitRatio();
			
			boolean log2 = false;
			if (log2)
				for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
					if (clusterSizes[clusterID] > 25)
						System.out.println("ID: " + clusterID + ", SIZE: " + clusterSizes[clusterID] + ", PERIMETER: "
											+ clusterPerimeter[clusterID] + ", CIRCLE? " + clusterCircleSimilarity[clusterID] + ", PFLANZE? "
											+ (clusterCircleSimilarity[clusterID] < 0.013));
			
			int[][] mask = ps.getImageMask();
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int clusterID = mask[x][y];
					if (clusterSizes[clusterID] < cutOff)
						rgbArray[x + y * w] = iBackgroundFill;
				}
			}
		}
		
		// Variante 2
		if (useVariant2) {
			
			PixelSegmentation ps = new PixelSegmentation(image);
			ps.doPixelSegmentation(2);
			
			int[] clusterSizes = ps.getClusterSize();
			int[] clusterPerimeter = ps.getPerimeter();
			double[] clusterCircleSimilarity = ps.getCircuitRatio();
			
			boolean log = false;
			if (log)
				for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
					if (clusterSizes[clusterID] > 25)
						System.out.println("ID: " + clusterID + ", SIZE: " + clusterSizes[clusterID] + ", PERIMETER: "
											+ clusterPerimeter[clusterID] + ", CIRCLE? " + clusterCircleSimilarity[clusterID] + ", PFLANZE? "
											+ (clusterCircleSimilarity[clusterID] < 0.013));
			
			int[][] mask = ps.getImageMask();
			// ArrayList<Color> colors = Colors.get(cl);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int clusterID = mask[x][y];
					// rgbArray[x + y * w] = clusterID != 0 ? clusterID :
					// Color.YELLOW.getRGB();
					// rgbArray[x + y * w] = colors.get(clusterID).getRGB();
					
					if (clusterSizes[clusterID] < cutOff) // ||
						// clusterCircleSimilarity[clusterID]
						// > 0.013
						rgbArray[x + y * w] = iBackgroundFill;
					// else if (clusterID != 0)
					// System.out.println("ID: " + clusterID + ", SIZE: " +
					// clusterSizes[clusterID] + ", PERIMETER: "
					// + clusterPerimeter[clusterID] + ", CIRCLE? " +
					// clusterCircleSimilarity[clusterID]);
				}
			}
			PrintImage.printImage(rgbArray, w, h, "variante 2");
			
		}
		
		// Variante 3
		if (useVariant3) {
			PixelSegmentation ps = new PixelSegmentation(image);
			ps.doPixelSegmentation(3);
			
			int[] clusterSizes = ps.getClusterSize();
			int[] clusterPerimeter = ps.getPerimeter();
			double[] clusterCircleSimilarity = ps.getCircuitRatio();
			
			boolean log = false;
			if (log)
				for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
					if (clusterSizes[clusterID] > 25)
						System.out.println("ID: " + clusterID + ", SIZE: " + clusterSizes[clusterID] + ", PERIMETER: "
											+ clusterPerimeter[clusterID] + ", CIRCLE? " + clusterCircleSimilarity[clusterID] + ", PFLANZE? "
											+ (clusterCircleSimilarity[clusterID] < 0.013));
			
			int[][] mask = ps.getImageMask();
			// ArrayList<Color> colors = Colors.get(cl);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int clusterID = mask[x][y];
					// rgbArray[x + y * w] = clusterID != 0 ? clusterID :
					// Color.YELLOW.getRGB();
					// rgbArray[x + y * w] = colors.get(clusterID).getRGB();
					
					if (clusterSizes[clusterID] < cutOff) // ||
						// clusterCircleSimilarity[clusterID]
						// > 0.013
						rgbArray[x + y * w] = iBackgroundFill;
					// else if (clusterID != 0)
					// System.out.println("ID: " + clusterID + ", SIZE: " +
					// clusterSizes[clusterID] + ", PERIMETER: "
					// + clusterPerimeter[clusterID] + ", CIRCLE? " +
					// clusterCircleSimilarity[clusterID]);
				}
			}
			
			PrintImage.printImage(rgbArray, w, h, "variante 3");
			
		}
		return new FlexibleImage(rgbArray, w, h);
	}
	
	/**
	 * Supports transparent background (ImageJ does not support it).
	 */
	public static BufferedImage blur(BufferedImage img) {
		float center = 1f * 1f / 9f;
		float out = 1f * 1f / 9f;
		Kernel kernel = new Kernel(3, 3,
					new float[] {
							out, out, out,
							out, center, out,
							out, out, out });
		BufferedImageOp op = new ConvolveOp(kernel);
		img = op.filter(img, null);
		
		// kernel = new Kernel(3, 3,
		// new float[] {
		// -1, -1, -1,
		// -1, 9, -1,
		// -1, -1, -1 });
		// op = new ConvolveOp(kernel);
		// img = op.filter(img, null);
		
		return img;
	}
	
	public ImageOperation crop() {
		int w = image.getWidth();
		int h = image.getHeight();
		
		int smallestX = Integer.MAX_VALUE;
		int largestX = 0;
		int smallestY = Integer.MAX_VALUE;
		int largestY = 0;
		
		int[][] img = getImageAs2array();
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img[x][y] != PhenotypeAnalysisTask.BACKGROUND_COLORint) {
					if (x < smallestX)
						smallestX = x;
					if (x > largestX)
						largestX = x;
					if (y < smallestY)
						smallestY = y;
					if (y > largestY)
						largestY = y;
				}
			}
		}
		if (largestX > 0) {
			int[][] res = new int[largestX - smallestX + 1][largestY - smallestY + 1];
			for (int x = smallestX; x <= largestX; x++) {
				for (int y = smallestY; y <= largestY; y++) {
					res[x - smallestX][y - smallestY] = img[x][y];
				}
			}
			// FlexibleImage a = new FlexibleImage(image);
			// FlexibleImage b = new FlexibleImage(res);
			// a.print("A");
			// b.print("B");
			return new ImageOperation(new FlexibleImage(res));
		} else
			return this;
	}
}
