package de.ipk.ag_ba.image.operations;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.ResultsTable;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ImageCalculator;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.MaximumFinder;
import ij.plugin.filter.UnsharpMask;
import ij.process.BinaryProcessor;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import info.StopWatch;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Line2D.Double;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.ObjectRef;
import org.ReleaseInfo;
import org.SystemAnalysis;
import org.Vector2d;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operations.blocks.cmds.BlockClearBackground;
import de.ipk.ag_ba.image.operations.complex_hull.ConvexHullCalculator;
import de.ipk.ag_ba.image.operations.intensity.IntensityAnalysis;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;
import de.ipk.ag_ba.image.operations.segmentation.PixelSegmentation;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.mongo.IAPservice;
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
 * A number of commonly used image operation commands.
 * 
 * @author Entzian, Klukas, Pape
 */

public class ImageOperation {
	
	protected final ImagePlus image;
	protected ResultsTable rt;
	public final static float[][][] labCube = getLabCube();
	
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
		if (width == image.getWidth() && height == image.getHeight())
			return this;
		if (width > 1 && height > 1) {
			ImageProcessor p = image.getProcessor().resize(width, height);
			image.setProcessor(p);
		}
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
		
		int[] in = getImageAs1array(); // gamma(0.1) // 99999999999999999999999999999999
		int w = image.getWidth();
		int h = image.getHeight();
		int idx = 0;
		for (int c : in) {
			if (c == background) {
				in[idx++] = background;
				continue;
			}
			
			int rf = (c & 0xff0000) >> 16;
			int gf = (c & 0x00ff00) >> 8;
			// int bf = (c & 0x0000ff);
			
			// float[] hsbvals = Color.RGBtoHSB(rf, gf, bf, null);
			//
			// int rgb = Color.HSBtoRGB(hsbvals[0], hsbvals[1], (float) (hsbvals[2]));
			// int r = (rgb & 0xff0000) >> 16;
			// int g = (rgb & 0x00ff00) >> 8;
			
			// float intensityA = 1 - r * max(r, g) / ((255 * 255) + 255 * g);
			
			float intensity = 1 - rf / (float) ((255) + gf);
			
			if (intensity > 210f / 255f)
				intensity = 1;
			
			int i = (int) (intensity * 255d);
			// in[x][y] = new Color(intensity, intensity, intensity).getRGB();
			in[idx++] = (0xFF << 24 | (i & 0xFF) << 16) | ((i & 0xFF) << 8) | ((i & 0xFF) << 0);
		}
		return new ImageOperation(in, w, h); // new ImageOperation(new FlexibleImage(in)).enhanceContrast();// .dilate();
	}
	
	private float max(int r, int g) {
		return r > g ? r : g;
	}
	
	public ImageOperation convertFluo2intensityOldRGBbased() {
		int background = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		int[] in = getImageAs1array();
		int idx = 0;
		for (int c : in) {
			if (c == background) {
				in[idx++] = background;
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
				in[idx++] = background;
			} else {
				intensity = 1 - intensity;
				int i = (int) (intensity * 255 + 0.5);
				in[idx++] = (0xFF << 24 | (i & 0xFF) << 16) | ((i & 0xFF) << 8) | ((i & 0xFF) << 0);
			}
		}
		return new ImageOperation(new FlexibleImage(in, image.getWidth(), image.getHeight()));// .dilate();
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
	
	public ImageOperation gamma(double value) {
		image.getProcessor().gamma(value);
		return new ImageOperation(image.getProcessor().getBufferedImage());
	}
	
	public void drawRect(int leftX, int leftY, int width, int heigh) {
		image.getProcessor().drawRect(leftX, leftY, width, heigh);
	}
	
	public void fillRect(int leftX, int leftY, int width, int heigh) {
		image.getProcessor().fill(new Roi(leftX, leftY, width, heigh));
	}
	
	public ImageOperation drawAndFillRect(int leftX, int leftY,
			int[][] fillValue) {
		
		int width = fillValue.length;
		int height = fillValue[0].length;
		
		int[] bigImage = ImageConverter.convertIJto1A(image);
		
		int ww = image.getWidth();
		
		for (int x = leftX; x < leftX + width; x++)
			for (int y = leftY; y < leftY + height; y++)
				bigImage[x + y * ww] = fillValue[x - leftX][y - leftY];
		
		return new ImageOperation(ImageConverter.convert1AtoIJ(ww, image.getHeight(), bigImage));
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
		int w = image.getWidth();
		int h = image.getHeight();
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
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
		int w = image.getWidth();
		int h = image.getHeight();
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				boolean inside = center.distance(x, y) < d;
				if (!inside)
					imgArr[x][y] = iBackgroundFill;
			}
		return new ImageOperation(imgArr);
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
	
	public ImageOperation removeSmallClusters(boolean nextGeneration, ObjectRef optClusterSizeReturn) {
		return removeSmallClusters(nextGeneration, 0.005d, CameraPosition.TOP, optClusterSizeReturn);
	}
	
	public ImageOperation removeSmallClusters(boolean nextGeneration, double factor, CameraPosition typ,
			ObjectRef optClusterSizeReturn) {
		return removeSmallClusters(nextGeneration, factor, NeighbourhoodSetting.NB4, typ,
				optClusterSizeReturn);
	}
	
	public ImageOperation removeSmallClusters(boolean nextGeneration, double factor,
			NeighbourhoodSetting nb, CameraPosition typ,
			ObjectRef optClusterSizeReturn) {
		FlexibleImage workImage = new FlexibleImage(image);
		workImage = removeSmallPartsOfImage(nextGeneration, workImage,
				PhenotypeAnalysisTask.BACKGROUND_COLORint,
				(int) (image.getWidth() * image.getHeight() * factor), (image.getWidth() / 100) * 2, nb, typ,
				optClusterSizeReturn);
		return new ImageOperation(workImage);
	}
	
	public ImageOperation findEdge() {
		image.getProcessor().findEdges();
		return new ImageOperation(image); // .getProcessor().getBufferedImage()
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
	
	public ImageOperation printImage(String title, boolean doIt) {
		if (doIt)
			new FlexibleImage(image).copy().print(title);
		return this;
	}
	
	public ImageOperation printImage(String title) {
		return printImage(title, true);
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
		Prefs.setThreads(1);
		GaussianBlur gb = new GaussianBlur();
		gb.blurGaussian(image.getProcessor(), radius, radius, 0.001);
		return this;// new ImageOperation(new FlexibleImage(getImage().getAs2A()));
	}
	
	/**
	 * @param workImage
	 * @param iBackgroundFill
	 * @param cutOffMinimumArea
	 *           always used on Top images
	 * @param cutOffMinimumDimension
	 *           can be used on side images, clusters with min width or height < cutOffMinimumDimension will be removed
	 * @param nb
	 * @param typ
	 * @param optClusterSizeReturn
	 * @return
	 */
	public static FlexibleImage removeSmallPartsOfImage(
			boolean nextGeneration,
			FlexibleImage workImage, int iBackgroundFill,
			int cutOffMinimumArea, int cutOffMinimumDimension, NeighbourhoodSetting nb, CameraPosition typ,
			ObjectRef optClusterSizeReturn) {
		
		if (cutOffMinimumArea < 1) {
			System.out.println("WARNING: Too low minimum pixel size for object removal: " + cutOffMinimumArea + ". Set to 1.");
			cutOffMinimumArea = 1;
		}
		
		if (cutOffMinimumDimension < 1) {
			System.out.println("WARNING: Too low minimum pixel size for object removal: " + cutOffMinimumDimension + ". Set to 1.");
			cutOffMinimumDimension = 1;
		}
		
		Segmentation ps;
		if (nextGeneration)
			ps = new ClusterDetection(workImage, PhenotypeAnalysisTask.BACKGROUND_COLORint);
		else
			ps = new PixelSegmentation(workImage, NeighbourhoodSetting.NB4);
		ps.detectClusters();
		
		int[] clusterSizes = null;
		int[] clusterDimensions = null;
		
		if (optClusterSizeReturn != null)
			optClusterSizeReturn.setObject(ps.getClusterSize());
		clusterDimensions = ps.getClusterDimensionMinWH();
		
		Vector2d[] clusterCenter = ps.getClusterCenterPoints();
		Vector2d[] clusterDimensions2d = ps.getClusterDimension();
		clusterSizes = ps.getClusterSize();
		
		HashSet<Integer> toBeDeletedClusterIDs = new HashSet<Integer>();
		if (typ == CameraPosition.TOP) {
			List<LargeCluster> largeClusters = new ArrayList<LargeCluster>();
			for (int index = 0; index < clusterSizes.length; index++) {
				if (clusterDimensions[index] >= cutOffMinimumDimension) {
					LargeCluster lc = new LargeCluster(clusterDimensions2d[index], clusterCenter[index], clusterSizes[index], index);
					largeClusters.add(lc);
				}
			}
			
			Collections.sort(largeClusters);
			if (largeClusters.size() > 1) {
				final LargeCluster largest = largeClusters.get(0);
				Rectangle2D largestBounding = largest.getBoundingBox(cutOffMinimumDimension * 5);
				Collections.sort(largeClusters, new Comparator<LargeCluster>() {
					
					@Override
					public int compare(LargeCluster o1, LargeCluster o2) {
						double d1 = largest.distanceTo(o1);
						double d2 = largest.distanceTo(o2);
						return d1 > d2 ? 1 : -1;
					}
				});
				for (LargeCluster lc : largeClusters) {
					if (lc != largeClusters) {
						if (!lc.intersects(largestBounding)) {
							toBeDeletedClusterIDs.add(lc.getIndex());
						} else {
							largestBounding.add(lc.getBoundingBox(cutOffMinimumDimension * 5));
						}
					}
				}
			}
		}
		
		int[] rgbArray = workImage.getAs1A();
		int[] mask = ps.getImageClusterIdMask();
		for (int idx = 0; idx < rgbArray.length; idx++) {
			int clusterID = mask[idx];
			if (clusterID >= 0
					&&
					(clusterDimensions[clusterID] < cutOffMinimumDimension || (clusterDimensions[clusterID] >= cutOffMinimumDimension && toBeDeletedClusterIDs
							.contains(clusterID))))
				rgbArray[idx] = iBackgroundFill;
		}
		
		int w = workImage.getWidth();
		int h = workImage.getHeight();
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
			int upperValueOfB, int background, CameraPosition typ) {
		
		int width = image.getProcessor().getWidth();
		int height = image.getProcessor().getHeight();
		
		int[] resultImage = new int[width * height];
		int[] img2d = getImageAs1array();
		
		doThresholdLAB(width, height, img2d, resultImage, lowerValueOfL, upperValueOfL, lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB, background, typ);
		
		return new ImageOperation(resultImage, width, height);
	}
	
	public static void doThresholdLAB(int width, int height, int[] img2d, int[] resultImage,
			int lowerValueOfL, int upperValueOfL,
			int lowerValueOfA, int upperValueOfA,
			int lowerValueOfB, int upperValueOfB,
			int background, CameraPosition typ) {
		
		doThresholdLAB(width, height, img2d, resultImage,
				new int[] { lowerValueOfL }, new int[] { upperValueOfL }, new int[] { lowerValueOfA }, new int[] { upperValueOfA }, new int[] { lowerValueOfB },
				new int[] { upperValueOfB }, background, typ);
		
	}
	
	public static void doThresholdLAB(int width, int height, int[] img2d, int[] resultImage,
			int[] lowerValueOfL, int[] upperValueOfL,
			int[] lowerValueOfA, int[] upperValueOfA,
			int[] lowerValueOfB, int[] upperValueOfB,
			int background, CameraPosition typ) {
		int c, x, y = 0;
		int r, g, b;
		int Li, ai, bi;
		int maxDiffAleftBright, maxDiffArightBleft;
		
		if (typ == CameraPosition.SIDE) {
			maxDiffAleftBright = 7;
			maxDiffArightBleft = 7;
		} else {
			maxDiffAleftBright = 11; // 15
			maxDiffArightBleft = 7;
		}
		
		for (y = 0; y < height; y++) {
			int yw = y * width;
			for (x = 0; x < width; x++) {
				int off = x + yw;
				c = img2d[off];
				
				// RGB to XYZ
				r = ((c & 0xff0000) >> 16);
				g = ((c & 0x00ff00) >> 8);
				b = (c & 0x0000ff);
				
				Li = (int) ImageOperation.labCube[r][g][b];
				ai = (int) ImageOperation.labCube[r][g][b + 256];
				bi = (int) ImageOperation.labCube[r][g][b + 512];
				
				boolean found = false;
				for (int idx = 0; idx < lowerValueOfA.length; idx++) {
					if (resultImage[off] != background) {
						if (((Li > lowerValueOfL[idx]) && (Li < upperValueOfL[idx]) && (ai > lowerValueOfA[idx]) && (ai < upperValueOfA[idx])
								&& (bi > lowerValueOfB[idx]) && (bi < upperValueOfB[idx])) && !
								isGray(Li, ai, bi, maxDiffAleftBright, maxDiffArightBleft)) {
							resultImage[off] = img2d[off];
							found = true;
							break;
						}
					}
				}
				if (!found)
					resultImage[off] = background;
			}
		}
	}
	
	public static float[][][] getLabCube() {
		StopWatch s = new StopWatch("lab_cube", true);
		final float step = 1f / 255f;
		final float[][][] result = new float[256][256][256 * 3];
		ExecutorService executor = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs());
		
		for (int rr = 0; rr < 256; rr++) {
			final int r = rr;
			final float rd = (r / 255f);
			
			executor.submit(new Runnable() {
				
				@Override
				public void run() {
					float gd = -step;
					float X, Y, Z, fX, fY, fZ;
					float La, aa, bb;
					final double ot = 1d / 3d;
					final float cont = 16f / 116f;
					for (int g = 0; g < 256; g++) {
						gd += step;
						float bd = -step;
						for (int b = 0; b < 256; b++) {
							
							bd += step;
							
							// white reference D65 PAL/SECAM
							X = 0.430587f * rd + 0.341545f * gd + 0.178336f * bd;
							Y = 0.222021f * rd + 0.706645f * gd + 0.0713342f * bd;
							Z = 0.0201837f * rd + 0.129551f * gd + 0.939234f * bd;
							// var_X = X / 95.047 //Observer = 2, Illuminant = D65
							// var_Y = Y / 100.000
							// var_Z = Z / 108.883
							
							// XYZ to Lab
							if (X > 0.008856)
								fX = (float) Math.pow(X, ot);
							// fX = IAPservice.cubeRoots[(int) (1000 * X)];
							else
								fX = (7.78707f * X) + cont;// 7.7870689655172
								
							if (Y > 0.008856)
								fY = (float) Math.pow(Y, ot);
							// fY = IAPservice.cubeRoots[(int) (1000 * Y)];
							else
								fY = (7.78707f * Y) + cont;
							
							if (Z > 0.008856)
								fZ = (float) Math.pow(Z, ot);
							// fZ = IAPservice.cubeRoots[(int) (1000 * Z)];
							else
								fZ = (7.78707f * Z) + cont;
							
							La = ((116 * fY) - 16) * 2.55f;
							aa = 1.0625f * (500f * (fX - fY)) + 128f;
							bb = 1.0625f * (200f * (fY - fZ)) + 128f;
							
							result[r][g][b] = La;
							result[r][g][b + 256] = aa;
							result[r][g][b + 512] = bb;
						}
					}
				}
			}
					);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s.printTime();
		return result;
	}
	
	public static double MathPow(double v, double ot) {
		// if (v < 0 || v > 1.1) {
		// System.out.println("TODO: " + v);
		// return Math.pow(v, ot);
		// } else
		return IAPservice.cubeRoots[(int) (1000 * v)];
	}
	
	public static float[] getCubeRoots(float lo, float up, int n) {
		float[] res = new float[n + 1];
		float sq = 1f / 3f;
		for (int i = 0; i <= n; i++) {
			float x = lo + i * (up - lo) / n;
			res[i] = (float) Math.pow(x, sq);
		}
		return res;
	}
	
	private static boolean isGray(int li, int ai, int bi, int maxDiffAleftBright, int maxDiffArightBleft) {
		ai = ai - 127;
		bi = bi - 127;
		boolean aNoColor, bNoColor;
		
		if (ai < 0)
			aNoColor = -ai < maxDiffAleftBright;
		else
			aNoColor = ai < maxDiffArightBleft;
		
		if (bi < 0)
			bNoColor = -bi < maxDiffArightBleft;
		else
			bNoColor = bi < maxDiffAleftBright;
		
		return aNoColor && bNoColor;
	}
	
	public Lab getLABAverage(int[][] img2d, int x1, int y1, int w, int h) {
		int c, x, y = 0;
		int r, g, b;
		double La, aa, bb;
		int Li, ai, bi;
		
		double sumL = 0;
		double sumA = 0;
		double sumB = 0;
		
		int count = 0;
		
		for (x = x1; x < x1 + w; x++) {
			for (y = y1; y < y1 + h; y++) {
				
				c = img2d[x][y];
				
				r = ((c & 0xff0000) >> 16); // R 0..1
				g = ((c & 0x00ff00) >> 8); // G 0..1
				b = (c & 0x0000ff); // B 0..1
				
				Li = (int) ImageOperation.labCube[r][g][b];
				ai = (int) ImageOperation.labCube[r][g][b + 256];
				bi = (int) ImageOperation.labCube[r][g][b + 512];
				
				sumL += Li;
				sumA += ai;
				sumB += bi;
				
				count++;
			}
		}
		return new Lab(sumL / count, sumA / count, sumB / count);
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
	 * (Source of javadoc: ImageJ java doc)
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
				// p.getBufferedImage() ==> image (ck, 25.6.11)
				return new ImageOperation(image, (ResultsTable) rt.clone());
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
	 * (Source of javadoc: ImageJ java doc)
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
	 * (Source of javadoc: ImageJ java doc)
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
		
		// image ==> byteProcessor.getBufferedImage() (ck, 26.6.11)
		return new ImageOperation(image);
	}
	
	public ImageOperation enhanceContrast() {
		ContrastEnhancer ce = new ContrastEnhancer();
		ce.equalize(image);
		
		return new ImageOperation(image);
	}
	
	public ImageOperation convertBinary2rgb() {
		int[] bi = ImageConverter.convertBIto1A(image.getProcessor().getBufferedImage());
		return new ImageOperation(new FlexibleImage(bi, image.getWidth(), image.getHeight()));
	}
	
	/**
	 * If a pixel value (only RGB-Blue!) is below the threshold, the background color is applied, otherwise the foreground color.
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
	
	public ArrayList<MarkerPair> searchBlueMarkers(double options, CameraPosition typ) {
		BlueMarkerFinder bmf = new BlueMarkerFinder(getImage(), options, typ);
		
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
		int[] originalArray = imageInput.getAs1A();
		int[] resultMask = getImageAs1array();
		int w = imageInput.getWidth();
		int h = imageInput.getHeight();
		int idx = 0;
		for (int c : resultMask) {
			if (c != background)
				resultMask[idx] = originalArray[idx++];
		}
		return new ImageOperation(new FlexibleImage(resultMask, w, h));
	}
	
	/**
	 * Process the image from its outside, e.g. floodfill and detection of outside borders
	 * 
	 * @return {@link BorderImageOperation} a helper object for performing several commands.
	 */
	public BorderImageOperation border() {
		return new BorderImageOperation(this);
	}
	
	/**
	 * Copy the image into a new image, which size is increased according to the specified bordersize.
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
	
	/**
	 * @return top, bottom, left, right
	 */
	public TopBottomLeftRight getExtremePoints(int background) {
		int[] img1d = getImageAs1array();
		
		int top = Integer.MAX_VALUE;
		int bottom = 0;
		int left = Integer.MAX_VALUE;
		int right = 0;
		
		boolean isin = false;
		int w = image.getWidth();
		int x = 0;
		int y = 0;
		for (int c : img1d) {
			if (c != background) {
				if (x > right)
					right = x;
				if (x < left)
					left = x;
				if (y < top)
					top = y;
				if (y > bottom)
					bottom = y;
				isin = true;
			}
			x++;
			if (x == w) {
				y++;
				x = 0;
			}
		}
		if (isin) {
			return new TopBottomLeftRight(top, bottom, left, right);
		} else
			return null;
	}
	
	/**
	 * @return {@link ImageComparator} a helper object containing several functions related to image comparison.
	 */
	public ImageComparator compare() {
		return new ImageComparator(getImage());
	}
	
	/**
	 * All Pixels will be count, which are not equal to the background color (PhenotypeAnalysisTask.BACKGROUND_COLORint).
	 * 
	 * @return Number of non-background pixels.
	 */
	public int countFilledPixels() {
		int res = 0;
		int background = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		int width = image.getWidth();
		int height = image.getHeight();
		int[] img1d = getImageAs1array();
		
		for (int c : img1d) {
			if (c != background)
				res++;
		}
		return res;
	}
	
	/**
	 * The sum of the intensities of non-background pixels will be calculated. The intensity (0..1) of the red channel is analyzed.
	 * 
	 * @param b
	 * @return
	 */
	public double intensitySumOfChannelRed(boolean performGrayScale) {
		double res = 0;
		int background = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		int[] img2d = getImageAs1array();
		
		int[] grayScale = img2d;
		if (performGrayScale)
			grayScale = convert2Grayscale().getImageAs1array();
		
		int idx = 0;
		for (int c : img2d) {
			if (c != background) {
				// res++;
				int cg = grayScale[idx];
				double rf = ((cg & 0xff0000) >> 16) / 255.0; // R 0..1
				res += rf;
			}
			idx++;
		}
		return res;
	}
	
	/**
	 * @param n
	 *           Number of classes in a histogram.
	 * @return {@link IntensityAnalysis} a helper object for calculating histograms.
	 */
	public IntensityAnalysis intensity(int n) {
		return new IntensityAnalysis(this, n);
	}
	
	public ImageOperation clearImageLeft(double cutoff, int background) {
		int[] img2d = getImageAs1array();
		int w = image.getWidth();
		int h = image.getHeight();
		int threshold = (int) cutoff;
		
		for (int y = 0; y < image.getHeight(); y++) {
			int yw = y * w;
			for (int x = 0; x < threshold; x++) {
				img2d[x + yw] = background;
			}
		}
		return new ImageOperation(img2d, w, h);
	}
	
	public ImageOperation clearImageRight(double threshold, int background) {
		int[] img2d = getImageAs1array();
		int w = image.getWidth();
		int h = image.getHeight();
		for (int y = 0; y < image.getHeight(); y++) {
			int yw = y * w;
			for (int x = (int) threshold; x < image.getWidth(); x++) {
				img2d[x + yw] = background;
			}
		}
		return new ImageOperation(img2d, w, h);
	}
	
	public ImageOperation clearImageAbove(double threshold, int background) {
		int[] img2d = getImageAs1array();
		int w = image.getWidth();
		int end = w * (int) threshold;
		int idx = 0;
		while (idx < end) {
			img2d[idx++] = background;
		}
		return new ImageOperation(img2d, w, image.getHeight());
	}
	
	public ImageOperation clearImageBottom(int threshold, int background) {
		int[] img2d = getImageAs1array();
		int w = image.getWidth();
		int h = image.getHeight();
		for (int y = threshold; y < h; y++) {
			int yw = y * w;
			for (int x = 0; x < w; x++) {
				img2d[x + yw] = background;
			}
		}
		return new ImageOperation(img2d, w, h);
	}
	
	/**
	 * Overwrites the borders of the image with the background color. The width of the border
	 * lines may be specified using the parameter bb.
	 */
	public ImageOperation border(int bb) {
		int[] in = getImageAs1array();
		
		int w = getImage().getWidth();
		int h = getImage().getHeight();
		
		int backgroundColor = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		
		if (h > bb)
			for (int x = 0; x < w; x++) {
				for (int d = 0; d < bb; d++) {
					in[x + d * w] = backgroundColor;
					in[x + (h - 1 - d) * w] = backgroundColor;
				}
			}
		
		if (w > bb)
			for (int y = 0; y < h; y++) {
				for (int d = 0; d < bb; d++) {
					in[d + y * w] = backgroundColor;
					in[w - 1 - d + y * w] = backgroundColor;
				}
			}
		
		return new ImageOperation(in, w, h);
	}
	
	/**
	 * Image channels are multiplied by factors {1, 2, 3} (a factor for each channel).
	 * 
	 * @param factors
	 *           - for 3 Channels of an 24 bit image
	 * @return
	 */
	public ImageOperation multiplicateImageChannelsWithFactors(double[] factors) {
		int[] img2d = getImageAs1array();
		int width = getImage().getWidth();
		int height = getImage().getHeight();
		double rf, gf, bf;
		int[] result = new int[width * height];
		int idx = 0;
		double rfff = factors[0];
		double gfff = factors[1];
		double bfff = factors[2];
		for (int c : img2d) {
			
			rf = ((c & 0xff0000) >> 16);
			gf = ((c & 0x00ff00) >> 8);
			bf = (c & 0x0000ff);
			
			int r = (int) (rf * rfff);
			int g = (int) (gf * gfff);
			int b = (int) (bf * bfff);
			
			if (r > 255)
				r = 255;
			if (g > 255)
				g = 255;
			if (b > 255)
				b = 255;
			
			result[idx++] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
		}
		return new ImageOperation(result, width, height);
	}
	
	/**
	 * Sum of all RGB channels of an area(x, y, width, height) in an image, under conditions in LAB
	 * 
	 * @param LThresh
	 *           minimal brightness
	 * @param ABThresh
	 *           minimal A and B value
	 */
	public double[] getRGBAverage(int x1, int y1, int w, int h, int LThresh, int ABThresh, boolean mode) {
		int r, g, b;
		double Li, ai, bi;
		
		// sums of RGB
		double sumR = 0;
		double sumG = 0;
		double sumB = 0;
		
		int count = 0;
		
		int[] img1d = getImageAs1array();
		
		for (int c : img1d) {
			// RGB to XYZ
			r = (c & 0xff0000) >> 16;
			g = (c & 0x00ff00) >> 8;
			b = c & 0x0000ff;
			
			Li = ImageOperation.labCube[r][g][b];
			ai = ImageOperation.labCube[r][g][b + 256];
			bi = ImageOperation.labCube[r][g][b + 512];
			
			// sum under following conditions
			if (mode) {
				if (Li > LThresh && (ai - 127 < ABThresh || -ai + 127 < ABThresh) && (bi - 127 < ABThresh || -bi + 127 < ABThresh)) {
					sumR += r / 255d;
					sumG += g / 255d;
					sumB += b / 255d;
					count++;
				}
			}
			if (mode == false) {
				if (Li < LThresh && (ai - 127 < ABThresh || -ai + 127 < ABThresh) && (bi - 127 < ABThresh || -bi + 127 < ABThresh)) {
					sumR += r / 255d;
					sumG += g / 255d;
					sumB += b / 255d;
					count++;
				}
			}
		}
		return new double[] { sumR / count, sumG / count, sumB / count };
	}
	
	public ImageOperation drawMarkers(ArrayList<MarkerPair> numericResult) {
		ImageOperation a = new ImageOperation(this.getImage());
		int s = 30;
		image.setColor(Color.YELLOW);
		for (MarkerPair mp : numericResult)
			if (mp.getLeft() != null)
				a.fillRect((int) (mp.getLeft().x - s), (int) (mp.getLeft().y - s), s * 2, s * 2);
		image.setColor(Color.RED);
		for (MarkerPair mp : numericResult)
			if (mp.getRight() != null)
				a.fillRect((int) (mp.getRight().x - s), (int) (mp.getRight().y - s), s * 2, s * 2);
		return a;
	}
	
	// maybe extract this functions in their own class color/white balancing
	/**
	 * intensity balancing for a rgb-image, pixels {r,g,b} is the reference which will be normalized to the brightness.
	 * e.g.: the pixel with the values of {x,y,z} should be normalized to brightness white (value of 255 in rgb-image),
	 * so all pixels were multiplicated by the calculated factor 255/{x,y,z}
	 * 
	 * @author pape
	 */
	public ImageOperation imageBalancing(int brightness, double[] rgbInfo) {
		if (image == null)
			return null;
		double r = brightness / rgbInfo[0];
		double g = brightness / rgbInfo[1];
		double b = brightness / rgbInfo[2];
		double[] factors = { r, g, b };
		ImageOperation io = new ImageOperation(image);
		return io.multiplicateImageChannelsWithFactors(factors);
	}
	
	public ImageOperation medianFilter32Bit(int repeat) {
		for (int i = 0; i < repeat; i++)
			image.getProcessor().medianFilter();
		
		return new ImageOperation(getImage());
	}
	
	public ImageOperation unsharpedMask(FlexibleImage inp, double weight, double sigma) {
		double[] fac = { weight, weight, weight };
		FlexibleImage blured = new ImageOperation(image).blur(sigma).multiplicateImageChannelsWithFactors(fac).getImage();
		blured.print("blured");
		return new ImageOperation(inp).printImage("orig").subtractImages(blured, "").printImage("sub");
	}
	
	public ImageOperation unsharpenMask(float weight, double sigma) {
		UnsharpMask um = new UnsharpMask();
		
		float[] channelR = getImage().getFloatChannel(Channel.R);
		float[] channelG = getImage().getFloatChannel(Channel.G);
		float[] channelB = getImage().getFloatChannel(Channel.B);
		
		float[][] channels = new float[][] { channelR, channelG, channelB };
		int w = getImage().getWidth();
		int h = getImage().getHeight();
		for (float[] channel : channels) {
			FloatProcessor fp = new FloatProcessor(w, h, channel, null);
			fp.snapshot();
			um.run(fp);
		}
		
		return new ImageOperation(new FlexibleImage(w, h, channelR, channelG, channelB));
	}
	
	/**
	 * Substracts the given image2 from the stored image. Processing is based on the LAB colorspace. The
	 * difference is divided by 2 and then "visualized".
	 * 
	 * @param image2
	 * @return Difference image (differences (half of it) are stored inside the LAB color space).
	 */
	public ImageOperation subtractImages(FlexibleImage image2) {
		int w = getImage().getWidth();
		int h = getImage().getHeight();
		double[][] labImage1 = getImage().getLab(false);
		double[][] labImage2 = image2.getLab(false);
		for (int idx = 0; idx < w * h; idx++) {
			double lDiff = labImage1[0][idx] - labImage2[0][idx];
			double aDiff = labImage1[1][idx] - labImage2[1][idx];
			double bDiff = labImage1[2][idx] - labImage2[2][idx];
			// if (lDiff < 0)
			// lDiff = 255 - lDiff;
			if (aDiff < 0)
				aDiff = -aDiff;
			if (bDiff < 0)
				bDiff = -bDiff;
			
			labImage1[0][idx] = 80 + lDiff; // 80 * (labImage1[0][idx] + labImage2[0][idx]) / 2 / 255d +
			labImage1[1][idx] = aDiff / 255d + 1;
			labImage1[2][idx] = bDiff / 255d + 1;
		}
		return new FlexibleImage(w, h, labImage1).getIO();
	}
	
	public ImageOperation copyImagesParts(double factorH, double factorW) {
		int w = image.getWidth();
		int h = image.getHeight();
		int[][] img2a = getImageAs2array();
		int[][] res = img2a;
		
		for (int x = (int) (w * factorW); x < w - (w * factorW); x++) {
			
			int y1 = (int) (h * 0.05);
			int y2 = (int) (h * factorH);
			
			double l1 = getLabAverage(x, y1, 0);
			double l2 = getLabAverage(x, y2, 0);
			
			double a1 = getLabAverage(x, y1, 1);
			double a2 = getLabAverage(x, y2, 1);
			
			double b1 = getLabAverage(x, y1, 2);
			double b2 = getLabAverage(x, y2, 2);
			
			double y1_ = y1 - h / 2;
			double y2_ = y2 - h / 2;
			
			double cl = getC(y1_, y2_, l1, l2, h);
			double ml = getM(y1_, y2_, l1, l2);
			
			double ca = getC(y1_, y2_, a1, a2, h);
			double ma = getM(y1_, y2_, a1, a2);
			
			double cb = getC(y1_, y2_, b1, b2, h);
			double mb = getM(y1_, y2_, b1, b2);
			
			for (int y = y2; y < h - (h * factorH) - 1; y++) {
				double lGradient = ml * (y - h / 2d) * (y - h / 2d) + cl;
				double aGradient = ma * (y - h / 2d) * (y - h / 2d) + ca;
				double bGradient = mb * (y - h / 2d) * (y - h / 2d) + cb;
				
				// if (x == w * factorW - x % 20)
				// res[x][y] = new Color_CIE_Lab(lx, ax, bx).getRGB();
				// res[x][y] = new Color_CIE_Lab(l2, a2, b2).getRGB();
				res[x][y] = new Color_CIE_Lab(lGradient, aGradient, bGradient).getRGB();
			}
		}
		return new ImageOperation(res);
	}
	
	/**
	 * Calculate the Average of L, a or b value from a 8 neighbourhood of p
	 * 
	 * @param p
	 * @param mode
	 *           0 = L, 1 = a, 2 = b
	 * @return
	 */
	private double getLabAverage(int x, int y, int colorMode) {
		double average = 0;
		int[] img2a = getImageAs1array();
		int w = image.getWidth();
		int h = image.getHeight();
		if (x > 1 && y > 1 && x < w - 1 && y < h - 1) {
			if (colorMode == 0) {
				average += new Color_CIE_Lab(img2a[x + y * w], false).getL();
				average += new Color_CIE_Lab(img2a[x - 1 + y * w], false).getL();
				average += new Color_CIE_Lab(img2a[x + (y - 1) * w], false).getL();
				average += new Color_CIE_Lab(img2a[x - 1 + (y - 1) * w], false).getL();
				average += new Color_CIE_Lab(img2a[x + 1 + y * w], false).getL();
				average += new Color_CIE_Lab(img2a[x + (y + 1) * w], false).getL();
				average += new Color_CIE_Lab(img2a[x + 1 + (y + 1) * w], false).getL();
				average += new Color_CIE_Lab(img2a[x + 1 + (y - 1) * w], false).getL();
				average += new Color_CIE_Lab(img2a[x - 1 + (y + 1) * w], false).getL();
			}
			if (colorMode == 1) {
				average += new Color_CIE_Lab(img2a[x + y * w], false).getA();
				average += new Color_CIE_Lab(img2a[x - 1 + y * w], false).getA();
				average += new Color_CIE_Lab(img2a[x + (y - 1) * w], false).getA();
				average += new Color_CIE_Lab(img2a[x - 1 + (y - 1) * w], false).getA();
				average += new Color_CIE_Lab(img2a[x + 1 + y * w], false).getA();
				average += new Color_CIE_Lab(img2a[x + (y + 1) * w], false).getA();
				average += new Color_CIE_Lab(img2a[x + 1 + (y + 1) * w], false).getA();
				average += new Color_CIE_Lab(img2a[x + 1 + (y - 1) * w], false).getA();
				average += new Color_CIE_Lab(img2a[x - 1 + (y + 1) * w], false).getA();
			}
			if (colorMode == 2) {
				average += new Color_CIE_Lab(img2a[x + y * w], false).getB();
				average += new Color_CIE_Lab(img2a[x - 1 + y * w], false).getB();
				average += new Color_CIE_Lab(img2a[x + (y - 1) * w], false).getB();
				average += new Color_CIE_Lab(img2a[x - 1 + (y - 1) * w], false).getB();
				average += new Color_CIE_Lab(img2a[x + 1 + y * w], false).getB();
				average += new Color_CIE_Lab(img2a[x + (y + 1) * w], false).getB();
				average += new Color_CIE_Lab(img2a[x + 1 + (y + 1) * w], false).getB();
				average += new Color_CIE_Lab(img2a[x + 1 + (y - 1) * w], false).getB();
				average += new Color_CIE_Lab(img2a[x - 1 + (y + 1) * w], false).getB();
			}
			average = average / 9d;
		} else {
			if (colorMode == 0)
				average = new Color_CIE_Lab(img2a[x + y * w], false).getL();
			if (colorMode == 1)
				average = new Color_CIE_Lab(img2a[x + y * w], false).getA();
			if (colorMode == 2)
				average = new Color_CIE_Lab(img2a[x + y * w], false).getB();
		}
		return average;
	}
	
	private double getM(double y1, double y2, double l1, double l2) {
		// see WolframAlpha: solve(l_1 = m *(y_1 -h/2)² +c | l_2 = m *(y_2 -h/2)² +c, m)
		return (l1 - l2) / (y1 * y1 - y2 * y2);
	}
	
	private double getC(double y1, double y2, double l1, double l2, double h) {
		// see WolframAlpha: solve(l_1 = m *(y_1 -h/2)² +c | l_2 = m *(y_2 -h/2)² +c, m)
		return (l2 * y1 * y1 - l1 * y2 * y2) / (y1 * y1 - y2 * y2);
	}
}
