package de.ipk.ag_ba.image.operations;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.MaximumFinder;
import ij.process.BinaryProcessor;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.ObjectRef;
import org.ReleaseInfo;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.image.operations.complex_hull.ConvexHullCalculator;
import de.ipk.ag_ba.image.operations.intensity.IntensityAnalysis;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;
import de.ipk.ag_ba.image.operations.segmentation.PixelSegmentation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author Entzian, Klukas, Pape
 */

public class ImageOperation {
	
	protected final ImagePlus image;
	protected ResultsTable rt;
	
	// private Roi boundingBox;
	
	public ImageOperation(ImagePlus image) {
		this.image = image;
	}
	
	public ImageOperation(ImagePlus image, ResultsTable resultTable) {
		this.image = image;
		this.rt = resultTable;
	}
	
	public ImageOperation(BufferedImage image) {
		this(ImageConverter.convertBItoIJ(image));
	}
	
	public ImageOperation(FlexibleImage image) {
		this(image.getAsImagePlus());
	}
	
	public ImageOperation(FlexibleImage image, ResultsTable resultTable) {
		this(image.getAsImagePlus(), resultTable);
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
	
	public ImageOperation(BufferedImage bufferedImage, ResultsTable rt) {
		this(bufferedImage);
		setResultsTable(rt);
	}
	
	/**
	 * Moves the image content. New clear regions are recolored to the
	 * background color.
	 */
	public ImageOperation translate(double x, double y) {
		image.getProcessor().translate(x, y);
		return new ImageOperation(getImage())
				.replaceColors(Color.BLACK.getRGB(),
						PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}
	
	public ImageOperation replaceColors(int search, int replace) {
		int[] source = getImageAs1array();
		int[] target = new int[source.length];
		
		int idx = 0;
		for (int v : source) {
			if (v != search)
				target[idx++] = v;
			else
				target[idx++] = replace;
		}
		return new ImageOperation(target, getImage().getWidth(), getImage()
				.getHeight());
	}
	
	/**
	 * Rotates the image content. New clear regions are recolored to the
	 * background color.
	 */
	public ImageOperation rotate(double degree) {
		image.getProcessor().rotate(degree);
		return new ImageOperation(getImage())
				.replaceColors(Color.BLACK.getRGB(),
						PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}
	
	/**
	 * Scales the image content. New clear regions are recolored to the
	 * background color.
	 */
	
	public ImageOperation scale(double xScale, double yScale) {
		image.getProcessor().scale(xScale, yScale);
		return new ImageOperation(getImage())
				.replaceColors(Color.BLACK.getRGB(),
						PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}
	
	/**
	 * Scales the image itself. See method scale to scale the content but not the image itself.
	 */
	public ImageOperation resize(int width, int height) {
		ImageProcessor p = image.getProcessor().resize(width, height);
		image.setProcessor(p);
		
		return new ImageOperation(getImage());
	}
	
	/**
	 * Scales the image itself according to the given factor.
	 */
	public ImageOperation resize(double factor) {
		return resize((int) (factor * image.getWidth()),
				(int) (factor * image.getHeight()));
	}
	
	public ImageOperation convertFluo2intensity() {
		int background = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		int[][] in = getImageAs2array();
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++) {
				int c = in[x][y];
				if (c == background) {
					in[x][y] = background;
					continue;
				}
				float rf = ((c & 0xff0000) >> 16);
				float gf = ((c & 0x00ff00) >> 8);
				// float bf = (float) ((c & 0x0000ff) / 255.0); // B 0..1
				float intensity = (2 * rf - gf) / 510f;
				if (intensity < 0)
					intensity = 0;
				// intensity = 1 - intensity;
				if (intensity < 44f / 255f) {
					in[x][y] = background;
				} else {
					intensity = 1 - intensity;
					in[x][y] = new Color(intensity, intensity, intensity, 1f).getRGB();
				}
			}
		return new ImageOperation(new FlexibleImage(in));// .dilate();
	}
	
	public ConvexHullCalculator hull() {
		return new ConvexHullCalculator(this);
	}
	
	// public void threshold(int cutValue) {
	// ImageProcessor processor2 = image.getProcessor().convertToByte(true);
	// ByteProcessor byteProcessor = new BinaryProcessor(
	// (ByteProcessor) processor2);
	// byteProcessor.threshold(cutValue);
	// image.setProcessor(processor2.convertToRGB());
	// }
	
	/**
	 * Copies the content of the stored image of this operation onto the given
	 * mask (and returns the result). Pixels where the mask has not the
	 * background color are set according to the source image. Pixels with
	 * background color are not modified.
	 * 
	 * @param mask
	 *           The mask which is used as a template.
	 * @param background
	 *           The color which is used to determine which parts of the mask
	 *           are considered as background (empty), all other pixels are
	 *           considered as foreground.
	 * @return The source image, filtered by the given mask.
	 */
	public ImageOperation applyMask_ResizeMaskIfNeeded(FlexibleImage mask,
			int background) {
		
		if (image.getWidth() != mask.getWidth()
				|| image.getHeight() != mask.getHeight()) {
			mask = new ImageOperation(mask).resize(image.getWidth(),
					image.getHeight()).getImage();
		}
		
		int[] maskPixels = mask.getAs1A();
		int[] originalImage = ImageConverter.convertIJto1A(image);
		
		int idx = 0;
		for (int maskPixel : maskPixels) {
			if (maskPixel != background)
				maskPixels[idx] = originalImage[idx];
			idx++;
		}
		
		return new ImageOperation(maskPixels, mask.getWidth(), mask.getHeight());
	}
	
	/**
	 * Copies the content of the stored image of this operation onto the given
	 * mask (and returns the result). Pixels where the mask has not the
	 * background color are set according to the source image. Pixels with
	 * background color are not modified.
	 * 
	 * @param mask
	 *           The mask which is used as a template.
	 * @param background
	 *           The color which is used to determine which parts of the mask
	 *           are considered as background (empty), all other pixels are
	 *           considered as foreground.
	 * @return The source image, filtered by the given mask.
	 */
	public ImageOperation applyMask_ResizeSourceIfNeeded(FlexibleImage mask,
			int background) {
		
		FlexibleImage srcImage = new FlexibleImage(image);
		
		// if the source image size is not equal to the given mask, the source
		// image is resized
		// ToDo: think or tests, shouldn't the mask be resized?
		if (srcImage.getWidth() != mask.getWidth()
				|| srcImage.getHeight() != mask.getHeight()) {
			srcImage = new ImageOperation(srcImage).resize(mask.getWidth(),
					mask.getHeight()).getImage();
		}
		
		int[] maskPixels = mask.getAs1A();
		int[] originalImage = srcImage.getAs1A();
		
		int idx = 0;
		for (int maskPixel : maskPixels) {
			if (maskPixel != background)
				maskPixels[idx] = originalImage[idx];
			idx++;
		}
		
		return new ImageOperation(maskPixels, mask.getWidth(), mask.getHeight());
	}
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public void erode(int[][] mask) {
		int jM = (mask.length - 1) / 2;
		int iM = (mask[0].length - 1) / 2;
		
		ImageProcessor tempImage = image.getProcessor().createProcessor(
				image.getProcessor().getWidth(),
				image.getProcessor().getHeight());
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
	 * Enlarge area of mask. es wird der 3x3 Minimum-Filter genutzt
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageOperation dilate() {
		image.getProcessor().dilate();
		return this;
	}
	
	/**
	 * Enlarge area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageOperation dilate(ImageProcessor temp, int[][] mask) {
		temp.invert();
		erode(mask);
		temp.invert();
		return this;
	}
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode(ImageProcessor temp) {
		temp.erode();
		return this;
	}
	
	/**
	 * Enlarge area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/8/8d/Dilation.png/220px-Dilation.png" >
	 */
	public ImageOperation dilate(int[][] mask) {
		return dilate(image.getProcessor(), mask);
	}
	
	// public void erode2(int [][] mask){
	// image.getProcessor().invert();
	// dilate(mask);
	// image.getProcessor().invert();
	// }
	
	/**
	 * Reduce area of mask.
	 * <p>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/3/3a/Erosion.png/220px-Erosion.png" >
	 */
	public ImageOperation erode() { // es wird der 3x3 Minimum-Filter genutzt
		return erode(image.getProcessor());
	}
	
	/**
	 * Dilation, then erosion. Removes small holes in the image.
	 * <p>
	 * The closing of the dark-blue shape (union of two squares) by a disk, resulting in the union of the dark-blue shape and the light-blue areas.:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/2/2e/Closing.png/220px-Closing.png" >
	 */
	public void closing(int[][] mask) {
		dilate(mask);
		erode(mask);
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask.
	 * <p>
	 * The closing of the dark-blue shape (union of two squares) by a disk, resulting in the union of the dark-blue shape and the light-blue areas:<br>
	 * <br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/commons/a/a2/MorphologicalClosing.png" >
	 */
	public void closing() { // es wird der 3x3 Minimum-Filter genutzt
		image.getProcessor().dilate();
		image.getProcessor().erode();
	}
	
	public ImageOperation closing(int m, int n) { // es wird der 3x3 Minimum-Filter genutzt
		for (int i = 0; i < m; i++)
			image.getProcessor().dilate();
		for (int j = 0; j < n; j++)
			image.getProcessor().erode();
		
		return new ImageOperation(image);
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask.
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public void opening(int[][] mask) {
		erode(mask);
		dilate(mask);
	}
	
	public ImageOperation opening(int[][] mask, int n) {
		for (int i = 0; i < n; i++)
			erode(mask);
		for (int i = 0; i < n; i++)
			dilate(mask);
		return this;
	}
	
	public ImageOperation opening(int[][] mask, int n1, int n2) {
		for (int i = 0; i < n1; i++)
			erode(mask);
		for (int i = 0; i < n2; i++)
			dilate(mask);
		return this;
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask. es wird der
	 * 3x3 Minimum-Filter genutzt
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public void opening() {
		image.getProcessor().erode();
		image.getProcessor().dilate();
	}
	
	/**
	 * Erosion, then dilation. Removes small objects in the mask. es wird der
	 * 3x3 Minimum-Filter genutzt
	 * <p>
	 * The erosion of the dark-blue square by a disk, resulting in the light-blue square:<br>
	 * <img src= "http://upload.wikimedia.org/wikipedia/en/thumb/c/c1/Opening.png/220px-Opening.png" >
	 */
	public ImageOperation opening(int n) {
		for (int i = 0; i < n; i++)
			image.getProcessor().erode();
		for (int i = 0; i < n; i++)
			image.getProcessor().dilate();
		return this;
	}
	
	public ImageOperation opening(int n1, int n2) {
		for (int i = 0; i < n1; i++)
			image.getProcessor().erode();
		for (int i = 0; i < n2; i++)
			image.getProcessor().dilate();
		return this;
	}
	
	public ImageOperation skeletonize() {
		ImageProcessor processor2 = image.getProcessor().convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) processor2);
		byteProcessor.skeletonize();
		
		return new ImageOperation(byteProcessor.getBufferedImage());
	}
	
	public void outline(int[][] mask) { // starke Farbübergänge werden als
		// Kante
		// erkannt
		ImageProcessor tempImage = image.getProcessor().duplicate();
		dilate(tempImage, mask);
		image.getProcessor().copyBits(tempImage, 0, 0, Blitter.DIFFERENCE);
		image.getProcessor().invert();
	}
	
	public void outline() {
		ImageProcessor processor2 = image.getProcessor().convertToByte(true);
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) processor2);
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
	
	public ImageOperation drawAndFillRect(int leftX, int leftY,
			int[][] fillValue) {
		
		int width = fillValue.length;
		int height = fillValue[0].length;
		
		int[][] bigImage = ImageConverter.convertIJto2A(image);
		
		for (int x = leftX; x < leftX + width && x < bigImage.length; x++)
			for (int y = leftY; y < leftY + height && y < bigImage[0].length; y++)
				bigImage[x][y] = fillValue[x - leftX][y - leftY];
		
		return new ImageOperation(ImageConverter.convert2AtoIJ(bigImage));
	}
	
	public ImageOperation drawAndFillRect(int leftX, int leftY, int width,
			int height, int fillValue) {
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
						if (img[i][j - 1] != background && j > left
								&& j > right) {
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
	
	/**
	 * Needs to be tested. Where and how to use.
	 * 
	 * @author entzian
	 */
	public ImageOperation cutAreaBoundingBox(Roi boundingBox) {
		image.getProcessor().setRoi(boundingBox);
		return new ImageOperation(image.getProcessor().crop()
				.getBufferedImage());
	}
	
	public ImageOperation cutArea(int bx, int by, int bw, int bh,
			int iBackgroundFill) {
		return cutArea(bx, by, bw, bh, iBackgroundFill, true);
	}
	
	public ImageOperation cutArea(int bx, int by, int bw, int bh,
			int iBackgroundFill, boolean clearOutsideTrue_insideFalse) {
		int[][] imgArr = new FlexibleImage(image).getAs2A();
		int bx2 = bx + bw;
		int by2 = by + bh;
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++) {
				boolean inside = x >= bx && x < bx2 && y >= by && y < by2;
				if (clearOutsideTrue_insideFalse) {
					if (!inside)
						imgArr[x][y] = iBackgroundFill;
				} else {
					if (inside)
						imgArr[x][y] = iBackgroundFill;
				}
			}
		return new ImageOperation(imgArr);
	}
	
	public ImageOperation cutAreaCircle(int bx, int by, int d,
			int iBackgroundFill) {
		int[][] imgArr = new FlexibleImage(image).getAs2A();
		Vector2d center = new Vector2d(bx, by);
		for (int x = 0; x < image.getWidth(); x++)
			for (int y = 0; y < image.getHeight(); y++) {
				boolean inside = center.distance(x, y) < d;
				if (!inside)
					imgArr[x][y] = iBackgroundFill;
			}
		return new ImageOperation(imgArr);
	}
	
	@Deprecated
	public Dimension2D getDiameter() {
		
		Dimension2D dimension = null;
		double minArea = java.lang.Double.MAX_VALUE;
		
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
				if (i > (img.length * 0.5) && j > (img[0].length * 0.5)
						&& i < (img.length * 1.5) && j < (img[0].length * 1.5)) {
					newImage[i][j] = img[(i - (img.length / 2) - 1)][(j
							- (img[0].length / 2) - 1)];
				} else
					newImage[i][j] = (int) image.getProcessor()
							.getBackgroundValue();
			}
		
		return new ImageOperation(ImageConverter.convert2AtoBI(newImage));
	}
	
	public ImageOperation removeSmallClusters(ObjectRef optClusterSizeReturn) {
		return removeSmallClusters(0.005d, CameraTyp.TOP, optClusterSizeReturn);
	}
	
	public ImageOperation removeSmallClusters(double factor, CameraTyp typ,
			ObjectRef optClusterSizeReturn) {
		return removeSmallClusters(factor, NeighbourhoodSetting.NB4, typ,
				optClusterSizeReturn);
	}
	
	public ImageOperation removeSmallClusters(double factor,
			NeighbourhoodSetting nb, CameraTyp typ,
			ObjectRef optClusterSizeReturn) {
		FlexibleImage workImage = new FlexibleImage(image);
		workImage = removeSmallPartsOfImage(workImage,
				PhenotypeAnalysisTask.BACKGROUND_COLORint,
				(int) (image.getWidth() * image.getHeight() * factor), nb, typ,
				optClusterSizeReturn);
		return new ImageOperation(workImage);
	}
	
	public ImageOperation findEdge() {
		image.getProcessor().findEdges();
		return new ImageOperation(image.getProcessor().getBufferedImage());
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
		new FlexibleImage(image).print(SystemAnalysisExt.getCurrentTime());
	}
	
	public ImageOperation printImage(String title) {
		new FlexibleImage(image).copy().print(title);
		return this;
	}
	
	// ############# save ######################
	
	/**
	 * Saves the file on the users desktop.
	 */
	public ImageOperation saveImageOnDesktop(String fileName) {
		return saveImage(ReleaseInfo.getDesktopFolder() + File.separator + fileName);
	}
	
	/**
	 * Saves the image as an PNG.
	 * 
	 * @param fileName
	 * @return
	 */
	public ImageOperation saveImage(String fileName) {
		saveImage(fileName, SaveImage.PNG);
		return this;
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
	// ByteProcessor byteProcessor = new BinaryProcessor((ByteProcessor)
	// processor2);
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
	
	static void testPhytokammer(IOurl urlFlu, IOurl urlVis, IOurl urlNIR,
			BufferedImage imgFluo, BufferedImage imgVisible,
			BufferedImage imgNIR) throws InterruptedException {
		
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
			
			BlockClearBackground.clearBackgroundAndInterpretImage(limg, 2,
					null, null, true, output, null, 0.5, 0.5);
			
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
			BlockClearBackground.clearBackgroundAndInterpretImage(limg, 2,
					null, null, true, output, null, 2.5, 2.5);
			
			// MainFrame.showMessageWindow("RgbTop Clean", new JLabel(
			// new ImageIcon(limg.getLoadedImage())));
		}
		
		int[] rgbImage = ImageConverter.convertBIto1A(imgVisible);
		fluoImage = ImageConverter.convertBIto1A(imgFluo);
		
		// modify masks
		ImageOperation ioR = new ImageOperation(rgbImage,
				imgVisible.getWidth(), imgVisible.getHeight());
		ioR.erode().erode();
		ioR.dilate().dilate().dilate().dilate().dilate();
		
		ImageOperation ioF = new ImageOperation(fluoImage, imgFluo.getWidth(),
				imgFluo.getHeight());
		for (int i = 0; i < 4; i++)
			ioF.erode();
		for (int i = 0; i < 20; i++)
			ioF.dilate();
		
		int[] rgbImageM = ioR.getImageAs1array();
		int[] fluoImageM = ioF.getImageAs1array();
		
		BufferedImage imgFluoTest = ImageConverter.convert1AtoBI(
				imgFluo.getWidth(), imgFluo.getHeight(), fluoImageM);
		ImagePlus imgFFTest = ImageConverter.convertBItoIJ(imgFluoTest);
		imgFFTest.show("Fluorescence Vorstufe1");
		
		// merge infos of both masks
		int background = PhenotypeAnalysisTask.BACKGROUND_COLOR.getRGB();
		MaskOperation o = new MaskOperation(ioR.getImage(), ioF.getImage(),
				null, background, 1);
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
			BlockClearBackground.clearBackgroundAndInterpretImage(limg, 2,
					null, null, true, output, null, 1, 0.5);
			
			int[] nirImage = ImageConverter.convertBIto1A(imgNIR);
			
			// process NIR
			
			MainFrame.showMessageWindow("NIR Source", new JLabel(new ImageIcon(
					imgNIR)));
			
			int[] mask = rgbImage;
			// resize mask
			ImageOperation ioo = new ImageOperation(mask,
					imgVisible.getWidth(), imgVisible.getHeight());
			ioo.resize(imgNIR.getWidth(), imgNIR.getHeight());
			ioo.rotate(-9);
			i = 0;
			for (int m : ioo.getImageAs1array()) {
				if (m == background) {
					nirImage[i] = background;
				}
				i++;
			}
			imgNIR = ImageConverter.convert1AtoBI(imgNIR.getWidth(),
					imgNIR.getHeight(), nirImage);
		}
		
		{ // fluo störungen beseitigen
			ImageOperation ioFF = new ImageOperation(fluoImage,
					imgFluo.getWidth(), imgFluo.getHeight());
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
			ImageOperation ioFF = new ImageOperation(rgbImage,
					imgVisible.getWidth(), imgVisible.getHeight());
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
		
		imgVisible = ImageConverter.convert1AtoBI(imgVisible.getWidth(),
				imgVisible.getHeight(), rgbImage);
		imgFluo = ImageConverter.convert1AtoBI(imgFluo.getWidth(),
				imgFluo.getHeight(), fluoImage);
		
		ImagePlus imgVV = ImageConverter.convertBItoIJ(imgVisible);
		ImagePlus imgFF = ImageConverter.convertBItoIJ(imgFluo);
		ImagePlus imgNN = ImageConverter.convertBItoIJ(imgNIR);
		
		imgVV.show("Visible");
		imgFF.show("Fluorescence");
		imgNN.show("NIR");
	}
	
	public static void showTwoImagesAsOne_resize(BufferedImage imgF2,
			BufferedImage imgV2, boolean saveImage) {
		ImageOperation resizeImage1 = new ImageOperation(imgF2);
		resizeImage1.resize(0.7);
		imgF2 = resizeImage1.getImageAsBufferedImage();
		
		ImageOperation resizeImage2 = new ImageOperation(imgV2);
		resizeImage2.resize(0.7);
		imgV2 = resizeImage2.getImageAsBufferedImage();
		showTwoImagesAsOne(imgF2, imgV2, saveImage);
	}
	
	public static void showTwoImagesAsOne(BufferedImage imgF2,
			BufferedImage imgV2, boolean saveImage) {
		
		// imgF2 = ImageConverter.convert1AtoBI(imgF2.getWidth(),
		// imgF2.getHeight(), ImageConverter.convertBIto1A(imgF2));
		// imgV2 = ImageConverter.convert1AtoBI(imgV2.getWidth(),
		// imgV2.getHeight(), ImageConverter.convertBIto1A(imgV2));
		
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
		if (saveImage) {
			ImageOperation save = new ImageOperation(imgF2);
			save.saveImage("/Users/" + System.getProperty("user.name")
					+ "/Desktop/overlayImage.png");
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
		return new ImageOperation(new FlexibleImage(getImage().getAs2A()));
	}
	
	public static FlexibleImage removeSmallPartsOfImage(
			FlexibleImage workImage, int iBackgroundFill,
			int cutOffMinimumArea, NeighbourhoodSetting nb, CameraTyp typ,
			ObjectRef optClusterSizeReturn) {
		
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
		
		if (useVariant1) {
			PixelSegmentation ps = new PixelSegmentation(image, nb);
			ps.doPixelSegmentation(1);
			
			int[] clusterSizes = new int[ps.getClusterSize().length];
			
			if (optClusterSizeReturn != null)
				optClusterSizeReturn.setObject(clusterSizes);
			
			switch (typ) {
				case TOP:
					clusterSizes = ps.getClusterSizeNormalized(w, h);
					break;
				
				case SIDE:
					clusterSizes = ps.getClusterSize();
					break;
			}
			
			boolean log2 = false;
			if (log2) {
				int[] clusterPerimeter = ps.getPerimeter();
				double[] clusterCircleSimilarity = ps.getCircuitRatio();
				
				for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
					if (clusterSizes[clusterID] > 25)
						System.out.println("ID: " + clusterID + ", SIZE: "
								+ clusterSizes[clusterID] + ", PERIMETER: "
								+ clusterPerimeter[clusterID] + ", CIRCLE? "
								+ clusterCircleSimilarity[clusterID]
								+ ", PFLANZE? "
								+ (clusterCircleSimilarity[clusterID] < 0.013));
			}
			
			int[][] mask = ps.getImageMask();
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int clusterID = mask[x][y];
					if (clusterSizes[clusterID] < cutOffMinimumArea)
						rgbArray[x + y * w] = iBackgroundFill;
				}
			}
		}
		
		// Variante 2
		if (useVariant2) {
			
			PixelSegmentation ps = new PixelSegmentation(image, nb);
			ps.doPixelSegmentation(2);
			
			int[] clusterSizes = ps.getClusterSize();
			int[] clusterPerimeter = ps.getPerimeter();
			double[] clusterCircleSimilarity = ps.getCircuitRatio();
			
			boolean log = false;
			if (log)
				for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
					if (clusterSizes[clusterID] > 25)
						System.out.println("ID: " + clusterID + ", SIZE: "
								+ clusterSizes[clusterID] + ", PERIMETER: "
								+ clusterPerimeter[clusterID] + ", CIRCLE? "
								+ clusterCircleSimilarity[clusterID]
								+ ", PFLANZE? "
								+ (clusterCircleSimilarity[clusterID] < 0.013));
			
			int[][] mask = ps.getImageMask();
			// ArrayList<Color> colors = Colors.get(cl);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int clusterID = mask[x][y];
					// rgbArray[x + y * w] = clusterID != 0 ? clusterID :
					// Color.YELLOW.getRGB();
					// rgbArray[x + y * w] = colors.get(clusterID).getRGB();
					
					if (clusterSizes[clusterID] < cutOffMinimumArea) // ||
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
			PixelSegmentation ps = new PixelSegmentation(image, nb);
			ps.doPixelSegmentation(3);
			
			int[] clusterSizes = ps.getClusterSize();
			int[] clusterPerimeter = ps.getPerimeter();
			double[] clusterCircleSimilarity = ps.getCircuitRatio();
			
			boolean log = false;
			if (log)
				for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
					if (clusterSizes[clusterID] > 25)
						System.out.println("ID: " + clusterID + ", SIZE: "
								+ clusterSizes[clusterID] + ", PERIMETER: "
								+ clusterPerimeter[clusterID] + ", CIRCLE? "
								+ clusterCircleSimilarity[clusterID]
								+ ", PFLANZE? "
								+ (clusterCircleSimilarity[clusterID] < 0.013));
			
			int[][] mask = ps.getImageMask();
			// ArrayList<Color> colors = Colors.get(cl);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int clusterID = mask[x][y];
					// rgbArray[x + y * w] = clusterID != 0 ? clusterID :
					// Color.YELLOW.getRGB();
					// rgbArray[x + y * w] = colors.get(clusterID).getRGB();
					
					if (clusterSizes[clusterID] < cutOffMinimumArea) // ||
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
		Kernel kernel = new Kernel(3, 3, new float[] { out, out, out, out,
				center, out, out, out, out });
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
			int[][] res = new int[largestX - smallestX + 1][largestY
					- smallestY + 1];
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
	
	public ImageOperation crop(double pLeft, double pRight, double pTop,
			double pBottom) {
		int w = image.getWidth();
		int h = image.getHeight();
		
		int smallestX = (int) (w * pLeft);
		int largestX = (int) (w * (1 - pRight));
		int smallestY = (int) (h * pTop);
		int largestY = (int) (h * (1 - pBottom));
		
		int[][] img = getImageAs2array();
		
		int[][] res = new int[largestX - smallestX + 1][largestY - smallestY
				+ 1];
		for (int x = smallestX; x <= largestX; x++) {
			for (int y = smallestY; y <= largestY; y++) {
				res[x - smallestX][y - smallestY] = img[x][y];
			}
		}
		
		return new ImageOperation(new FlexibleImage(res));
	}
	
	public ImageOperation filterByHSV_value(double t, int clearColor) {
		int[] pixels = getImageAs1array();
		float[] hsb = new float[3];
		for (int index = 0; index < pixels.length; index++) {
			int rgb = pixels[index];
			// int a = ((rgb >> 24) & 0xff);
			int r = ((rgb >> 16) & 0xff);
			int g = ((rgb >> 8) & 0xff);
			int b = (rgb & 0xff);
			
			Color.RGBtoHSB(r, g, b, hsb);
			
			if (hsb[2] < t)
				pixels[index] = clearColor;
		}
		return new ImageOperation(pixels, getImage().getWidth(), getImage()
				.getHeight());
	}
	
	/**
	 * converts RGB image into the Lab colorspace under conditions:
	 * 
	 * @param lowerValueOfL
	 *           - lower border L
	 * @param upperValueOfL
	 *           - upper border L
	 * @param lowerValueOfA
	 *           - ...
	 * @param upperValueOfA
	 * @param lowerValueOfB
	 * @param upperValueOfB
	 * @param background
	 *           - Background-Color
	 * @return
	 */
	public ImageOperation thresholdLAB(int lowerValueOfL, int upperValueOfL, int lowerValueOfA, int upperValueOfA, int lowerValueOfB,
			int upperValueOfB, int background) {
		
		int width = image.getProcessor().getWidth();
		int height = image.getProcessor().getHeight();
		
		int[][] resultImage = new int[image.getWidth()][image.getHeight()];
		int[][] img2d = getImageAs2array();
		
		doThresholdLAB(width, height, img2d, resultImage, lowerValueOfL, upperValueOfL, lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB, background);
		
		return new ImageOperation(resultImage);
	}
	
	public static void doThresholdLAB(int width, int height, int[][] img2d, int[][] resultImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB,
			int upperValueOfB, int background) {
		int c, x, y = 0;
		double rf, gf, bf;
		double X, Y, Z, fX, fY, fZ;
		double La, aa, bb;
		double ot = 1 / 3.0, cont = 16 / 116.0;
		int Li, ai, bi;
		
		for (y = 0; y < height; y++) {
			for (x = 0; x < width; x++) {
				c = img2d[x][y];
				
				// RGB to XYZ
				rf = ((c & 0xff0000) >> 16) / 255.0; // R 0..1
				gf = ((c & 0x00ff00) >> 8) / 255.0; // G 0..1
				bf = (c & 0x0000ff) / 255.0; // B 0..1
				
				// white reference D65 PAL/SECAM
				X = 0.430587 * rf + 0.341545 * gf + 0.178336 * bf;
				Y = 0.222021 * rf + 0.706645 * gf + 0.0713342 * bf;
				Z = 0.0201837 * rf + 0.129551 * gf + 0.939234 * bf;
				// var_X = X / 95.047 //Observer = 2, Illuminant = D65
				// var_Y = Y / 100.000
				// var_Z = Z / 108.883
				
				// XYZ to Lab
				if (X > 0.008856)
					fX = Math.pow(X, ot);
				else
					fX = (7.78707 * X) + cont;// 7.7870689655172
					
				if (Y > 0.008856)
					fY = Math.pow(Y, ot);
				else
					fY = (7.78707 * Y) + cont;
				
				if (Z > 0.008856)
					fZ = Math.pow(Z, ot);
				else
					fZ = (7.78707 * Z) + cont;
				
				La = (116 * fY) - 16;
				aa = 500 * (fX - fY);
				bb = 200 * (fY - fZ);
				
				// Lab rescaled to the 0..255 range
				// a* and b* range from -120 to 120 in the 8 bit space
				La = La * 2.55;
				aa = Math.floor((1.0625 * aa + 128) + 0.5);
				bb = Math.floor((1.0625 * bb + 128) + 0.5);
				
				// bracketing
				Li = (int) (La < 0 ? 0 : (La > 255 ? 255 : La));
				ai = (int) (aa < 0 ? 0 : (aa > 255 ? 255 : aa));
				bi = (int) (bb < 0 ? 0 : (bb > 255 ? 255 : bb));
				
				if ((Li > lowerValueOfL) && (Li < upperValueOfL) && (ai > lowerValueOfA) && (ai < upperValueOfA)
						&& (bi > lowerValueOfB) && (bi < upperValueOfB)) {
					resultImage[x][y] = img2d[x][y];
				} else {
					resultImage[x][y] = background;
				}
			}
		}
	}
	
	public ImageOperation medianFilter32Bit() {
		image.getProcessor().medianFilter();
		
		return new ImageOperation(getImage());
	}
	
	public ImageOperation medianFilter8Bit() {
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) image.getProcessor().convertToByte(false));
		byteProcessor.medianFilter();
		
		return new ImageOperation(getImage());
	}
	
	/**
	 * @return 8Bit grayscale image
	 */
	public ImageOperation convert2Grayscale() {
		ij.process.ImageConverter co = new ij.process.ImageConverter(image);
		co.convertToGray8();
		
		return new ImageOperation(getImage());
	}
	
	/**
	 * (Source: ImageJ java doc)
	 * Here the processing is done: Find the maxima of an image (does not find minima).
	 * 
	 * @param ip
	 *           The input image
	 * @param tolerance
	 *           Height tolerance: maxima are accepted only if protruding more than this value
	 *           from the ridge to a higher maximum
	 * @param threshold
	 *           minimum height of a maximum (uncalibrated); for no minimum height set it to
	 *           ImageProcessor.NO_THRESHOLD
	 * @param outputType
	 *           What to mark in output image: SINGLE_POINTS, IN_TOLERANCE or SEGMENTED.
	 *           No output image is created for output types POINT_SELECTION, LIST and COUNT.
	 * @param excludeOnEdges
	 *           Whether to exclude edge maxima
	 * @param isEDM
	 *           Whether the image is a float Euclidian Distance Map
	 * @return A new byteProcessor with a normal (uninverted) LUT where the marked points
	 *         are set to 255 (Background 0). Pixels outside of the roi of the input ip are not set.
	 *         Returns null if outputType does not require an output or if cancelled by escape
	 */
	public ImageOperation findMax(double tolerance, double threshold,
			int outputType, boolean excludeOnEdges, boolean isEDM) {
		
		MaximumFinder find = new MaximumFinder();
		ResultsTable rt = ResultsTable.getResultsTable();
		synchronized (rt) {
			rt.reset();
			ByteProcessor p = find.findMaxima(image.getProcessor(), tolerance,
					threshold, outputType, excludeOnEdges, isEDM);
			
			if (!(outputType == MaximumFinder.COUNT || outputType == MaximumFinder.LIST || outputType == MaximumFinder.POINT_SELECTION)) {
				return new ImageOperation(p.getBufferedImage(), (ResultsTable) rt.clone());
			} else {
				setResultsTable((ResultsTable) rt.clone());
				return this;
			}
			
		}
	}
	
	public ImageOperation findMax() {
		return findMax(255 / 2);
	}
	
	/**
	 * (Source: ImageJ java doc)
	 * Here the processing is done: Find the maxima of an image (does not find minima).
	 * 
	 * @param tolerance
	 *           Height tolerance: maxima are accepted only if protruding more than this value
	 *           from the ridge to a higher maximum
	 */
	public ImageOperation findMax(double tolerance) {
		return findMax(tolerance, MaximumFinder.COUNT);
	}
	
	/**
	 * (Source: ImageJ java doc)
	 * Here the processing is done: Find the maxima of an image (does not find minima).
	 * 
	 * @param tolerance
	 *           Height tolerance: maxima are accepted only if protruding more than this value
	 *           from the ridge to a higher maximum
	 * @param outputType
	 *           What to mark in output image: SINGLE_POINTS, IN_TOLERANCE or SEGMENTED.
	 *           No output image is created for output types POINT_SELECTION, LIST and COUNT.
	 */
	public ImageOperation findMax(double tolerance, int outputType) {
		return findMax(tolerance, ImageProcessor.NO_THRESHOLD, outputType, false, false);
	}
	
	// public ImageOperation maxium() {
	// MaximumFinder mf = new MaximumFinder();
	//
	// mf.findMaxima(image.getProcessor(), 50, ImageProcessor.NO_THRESHOLD,
	// MaximumFinder.COUNT, true, false);
	//
	// // System.out.println("resultTable2: " + rt.getValue("Count",
	// rt.getCounter() - 1));
	//
	// return new ImageOperation(mf.findMaxima(image.getProcessor(), 50,
	// ImageProcessor.NO_THRESHOLD, MaximumFinder.SEGMENTED, true,
	// false).getBufferedImage());
	//
	// }
	
	/**
	 * Creates binary image (foreground / background).
	 * 
	 * @param cutValue
	 *           Pixel values <= this parameter become background (black|0),
	 *           pixel color values > are turned into foreground (white|255).
	 * @return Binary image.
	 * @author pape
	 */
	public ImageOperation threshold(int cutValue) {
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) image.getProcessor());
		byteProcessor.threshold(cutValue);
		
		return new ImageOperation(byteProcessor.getBufferedImage());
	}
	
	public ImageOperation convertBinary2rgb() {
		int[][] bi = ImageConverter.convertBIto2A(image.getProcessor().getBufferedImage());
		return new ImageOperation(new FlexibleImage(bi));
	}
	
	/**
	 * If pixel (only RGB-Blue!) is below the threshold, the background color is applied, otherwise the foreground color.
	 */
	public ImageOperation threshold(int threshold, int background, int foreground) {
		int[] pixels = getImageAs1array();
		for (int index = 0; index < pixels.length; index++) {
			int rgb = pixels[index];
			// int a = ((rgb >> 24) & 0xff);
			// int r = ((rgb >> 16) & 0xff);
			// int g = ((rgb >> 8) & 0xff);
			int b = (rgb & 0xff);
			
			if (b < threshold)
				pixels[index] = background;
			else
				pixels[index] = foreground;
		}
		return new ImageOperation(pixels, getImage().getWidth(), getImage()
				.getHeight());
	}
	
	public void setResultsTable(ResultsTable rt) {
		this.rt = rt;
	}
	
	public ResultsTable getResultsTable() {
		return rt;
	}
	
	public ImageOperation drawLine(Vector2d centroid, double resultAngle,
			int subX, int addX, Color color, float width) {
		BufferedImage bi = getImage().getAsBufferedImage();
		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		
		g2.translate(centroid.x, centroid.y);
		g2.rotate(resultAngle / 180 * Math.PI);
		g2.setStroke(new BasicStroke(width));
		g2.setColor(color);
		
		g2.drawLine(subX, 0, addX, 0);
		
		return new ImageOperation(bi);
	}
	
	public ImageOperation drawLine(int x1, int y1, int x2, int y2, Color color, float width) {
		BufferedImage bi = getImage().getAsBufferedImage();
		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		
		g2.setStroke(new BasicStroke(width));
		g2.setColor(color);
		g2.drawLine(x1, y1, x2, y2);
		
		return new ImageOperation(bi);
	}
	
	public ArrayList<MarkerPair> searchBlueMarkers() {
		BlueMarkerFinder bmf = new BlueMarkerFinder(getImage());
		
		bmf.findCoordinates(PhenotypeAnalysisTask.BACKGROUND_COLORint);
		
		ArrayList<MarkerPair> mergedCoordinates = bmf
				.getResultCoordinates((int) (getImage().getHeight() * 0.05d));
		
		return mergedCoordinates;
	}
	
	public MainAxisCalculationResult calculateTopMainAxis(Vector2d centroid, int step, int background) {
		
		int[][] img = getImageAs2array();
		
		DistanceSumAndPixelCount minResult = new DistanceSumAndPixelCount(java.lang.Double.MAX_VALUE, 0);
		
		for (int angle = 0; angle <= 180; angle += step) {
			double m = Math.tan(angle / 180d * Math.PI);
			
			Line2D.Double line = null;
			
			if (angle != 90)
				line = new Line2D.Double(centroid.x, centroid.y,
						centroid.x + 1, centroid.y + m);
			else
				line = new Line2D.Double(centroid.x, centroid.y, centroid.x,
						centroid.y + m);
			DistanceSumAndPixelCount r = distancePointsToLine(img, line, background);
			r.setAngle(angle);
			if (r.getDistanceSum() < minResult.getDistanceSum())
				minResult = r;
		}
		FlexibleImage imageResult = new FlexibleImage(img);
		return new MainAxisCalculationResult(imageResult, minResult, centroid);
	}
	
	/**
	 * calculates distance between a line and pixels of an image
	 * 
	 * @param img
	 * @param line
	 * @param background
	 * @return
	 */
	private DistanceSumAndPixelCount distancePointsToLine(int[][] img, Double line, int background) {
		double dist = 0;
		int pixelCount = 0;
		for (int x = 0; x < img.length; x++) {
			for (int y = 0; y < img[0].length; y++) {
				if (img[x][y] != background) {
					dist += line.ptLineDist(x, y);
					pixelCount++;
				}
			}
		}
		return new DistanceSumAndPixelCount(dist, pixelCount);
	}
	
	/**
	 * Calculates the center of mass. Works only on binary source images.
	 * 
	 * @param backgroundColor
	 * @return Center of mass.
	 */
	public Vector2d getCentroid(int backgroundColor) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[][] image2d = getImageAs2array();
		
		int black = backgroundColor;
		
		int area = 0;
		int positionx = 0;
		int positiony = 0;
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (image2d[x][y] != black) {
					positionx = positionx + x;
					positiony = positiony + y;
					area++;
				}
			}
		}
		if (area > 0)
			return new Vector2d(positionx / area, positiony / area);
		else
			return null;
	}
	
	public MainAxisCalculationResult calculateTopMainAxis(int background) {
		Vector2d r = getCentroid(background);
		if (r != null)
			return calculateTopMainAxis(r, 20, background);
		else
			return null;
	}
	
	/**
	 * logical operations with two images
	 * 
	 * @param input
	 * @param param
	 *           "Subtract create" = image - input
	 * @return
	 */
	public ImageOperation subtractImages(FlexibleImage input, String param) {
		ImageCalculator ic = new ImageCalculator();
		ImagePlus result = ic.run(param, image, input.getAsImagePlus());
		return new ImageOperation(result);
	}
	
	public ImageOperation getOriginalImageFromMask(FlexibleImage imageInput, int background) {
		int[][] originalArray = imageInput.getAs2A();
		int[][] resultMask = getImageAs2array();
		
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				
				if (resultMask[x][y] != background)
					resultMask[x][y] = originalArray[x][y];
				
			}
		}
		return new ImageOperation(new FlexibleImage(resultMask));
	}
	
	public BorderImageOperation border() {
		return new BorderImageOperation(this);
	}
	
	/**
	 * copy the image into a new image, wich is increase about bordersize
	 * 
	 * @param input
	 * @param bordersize
	 * @param translatex
	 * @param translatey
	 * @param background
	 *           - color of the border
	 * @return
	 */
	public ImageOperation addBorder(int bordersize, int translatex, int translatey, int background) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[][] img2d = getImageAs2array();
		int[][] result = new int[width + (2 * bordersize)][height + (2 * bordersize)];
		
		result = fillArray(result, background);
		
		for (int x = bordersize + translatex; x < (width + translatex); x++) {
			for (int y = bordersize + translatey; y < (height + translatey); y++) {
				if (x - bordersize - translatex >= 0 && y - bordersize - translatey >= 0 && x >= 0 && y >= 0)
					result[x][y] = img2d[x - bordersize - translatex][y - bordersize - translatey];
			}
		}
		return new ImageOperation(result);
	}
	
	public int[][] fillArray(int[][] result, int background) {
		for (int x = 0; x < result.length; x++)
			for (int y = 0; y < result[0].length; y++)
				result[x][y] = background;
		return result;
	}
	
	public Point calculateWidthAndHeight(int background) {
		int[][] img2d = getImageAs2array();
		
		int minX = Integer.MAX_VALUE;
		int maxX = 0;
		int minY = Integer.MAX_VALUE;
		int maxY = 0;
		boolean isin = false;
		
		for (int x = 0; x < img2d.length; x++) {
			for (int y = 0; y < img2d[0].length; y++) {
				if (img2d[x][y] != background && img2d[x][y] != Color.BLACK.getRGB()) {
					if (x < minX)
						minX = x;
					if (x > maxX)
						maxX = x;
					if (y < minY)
						minY = y;
					if (y > maxY)
						maxY = y;
					isin = true;
				}
			}
		}
		// System.out.println(new Point(maxX - minX, maxY - minY).toString());
		if (isin)
			return new Point(maxX - minX, maxY - minY);
		else
			return null;
	}
	
	public ImageOperation clearImageBelowYvalue(int threshold, int background) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] img2d = getImageAs2array();
		
		for (int x = 0; x < width; x++) {
			for (int y = threshold; y < height; y++) {
				img2d[x][y] = background;
			}
		}
		return new ImageOperation(img2d);
	}
	
	public ImageComparator compare() {
		return new ImageComparator(getImage());
	}
	
	public int countFilledPixels() {
		int res = 0;
		int background = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		int width = image.getWidth();
		int height = image.getHeight();
		int[][] img2d = getImageAs2array();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (img2d[x][y] != background)
					res++;
			}
		}
		return res;
	}
	
	/**
	 * @param n
	 *           number of classes in histogram
	 * @return
	 */
	public IntensityAnalysis intensity(int n) {
		return new IntensityAnalysis(this, n);
	}
	
	public ImageOperation rgbChannelWeighting() {
		
		int[] pixels = getImageAs1array();
		int[] result = new int[pixels.length];
		boolean flag = true;
		int index = 0;
		for (int c : pixels) {
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);
			
			// min: -4*255, max: 4*255
			// --> Range: 8*255, Offset: 4*255
			int intensity = 4 * g - (3 * b) - r;
			float i = (intensity + 4 * 255) / (float) (8 * 255);
			if (i > 130f / 255f)
				result[index] = new Color(i, i, i).getRGB();
			else {
				flag = true;
				if (flag)
					result[index] = Color.BLUE.getRGB();
				else
					result[index] = c;
				flag = !flag;
			}
			index++;
		}
		return new ImageOperation(new FlexibleImage(result, image.getWidth(), image.getHeight()));
	}
	
}
