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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ObjectRef;
import org.ReleaseInfo;
import org.SystemAnalysis;
import org.Vector2d;
import org.Vector2i;
import org.graffiti.editor.GravistoService;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.operations.complex_hull.ConvexHullCalculator;
import de.ipk.ag_ba.image.operations.intensity.IntensityAnalysis;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;
import de.ipk.ag_ba.image.operations.segmentation.PixelSegmentation;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

/**
 * A number of commonly used image operation commands.
 * 
 * @author Entzian, Klukas, Pape
 */

public class ImageOperation {
	protected final ImagePlus image;
	protected ResultsTable rt;
	public static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 255);
	public static final int BACKGROUND_COLORint = ImageOperation.BACKGROUND_COLOR.getRGB();
	
	/**
	 * L:[0.0,254.6050567626953]<br>
	 * A:[26.135635375976562,225.2710723876953]<br>
	 * B:[8.081741333007812,222.49612426757812]
	 */
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
						ImageOperation.BACKGROUND_COLORint);
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
						ImageOperation.BACKGROUND_COLORint);
	}
	
	/**
	 * Scales the image content. New clear regions are recolored to the
	 * background color.
	 */
	
	public ImageOperation scale(double xScale, double yScale) {
		image.getProcessor().scale(xScale, yScale);
		return new ImageOperation(getImage())
				.replaceColors(Color.BLACK.getRGB(),
						ImageOperation.BACKGROUND_COLORint);
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
	
	public ImageOperation convertFluo2intensity(FluoAnalysis type) {
		int background = ImageOperation.BACKGROUND_COLORint;
		
		int[] in = getImageAs1array(); // gamma(0.1) // 99999999999999999999999999999999
		int w = image.getWidth();
		int h = image.getHeight();
		int idx = 0;
		float[] hsb = new float[3];
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
			
			float intensity = Float.NaN;
			
			intensity = 1 - rf / (float) ((255) + gf);
			if (intensity > 210f / 255f)
				intensity = 1;
			else
				switch (type) {
					case CLASSIC:
						intensity = intensity / 0.825f;
						break;
					case CHLOROPHYL:
						Color.RGBtoHSB(rf, gf, 0, hsb);
						hsb[2] = rf / 255f;
						intensity = 1 - (1 - distanceToRed(hsb[0])) * (hsb[2]);
						break;
					case PHENOL:
						Color.RGBtoHSB(rf, gf, 0, hsb);
						hsb[2] = rf / 255f;
						intensity = 1 - distanceToRed(hsb[0]) * (hsb[2]);
						break;
					default:
						throw new UnsupportedOperationException("INTERNAL ERROR: Invalid Fluo Analysis Mode");
				}
			
			int i = (int) (intensity * 255d);
			// in[x][y] = new Color(intensity, intensity, intensity).getRGB();
			in[idx++] = (0xFF << 24 | (i & 0xFF) << 16) | ((i & 0xFF) << 8) | ((i & 0xFF) << 0);
		}
		return new ImageOperation(in, w, h); // new ImageOperation(new FlexibleImage(in)).enhanceContrast();// .dilate();
	}
	
	/**
	 * @return 0 ==> Yellow, 1 ==> Red, 0.5 ==> Orange
	 */
	public static float distanceToRed(float f) {
		float red = 0;
		float yellow = 60f / 360f;
		if (f < yellow) { // 0 - 0.16)
			red = f / yellow;
		} else { // 0.16 - 1
			f -= yellow;
			red = 1 - (f / (1 - yellow));
		}
		return red;
	}
	
	private float max(int r, int g) {
		return r > g ? r : g;
	}
	
	public ImageOperation convertFluo2intensityOldRGBbased() {
		int background = ImageOperation.BACKGROUND_COLORint;
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
		return new ImageOperation(new FlexibleImage(image.getWidth(), image.getHeight(), in));// .dilate();
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
	public ImageOperation applyMask_ResizeSourceIfNeeded(int[] mask, int maskW, int maskH,
			int background) {
		
		ImageOperation io = this;
		// if the source image size is not equal to the given mask, the source
		// image is resized
		if (image.getWidth() != maskW
				|| image.getHeight() != maskH) {
			io = resize(maskW, maskH);
		}
		
		int[] maskPixels = mask;
		int[] originalImage = io.getImageAs1array();
		
		int idx = 0;
		for (int maskPixel : maskPixels) {
			if (maskPixel != background)
				maskPixels[idx] = originalImage[idx];
			idx++;
		}
		
		return new ImageOperation(maskPixels, maskW, maskH);
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
		
		ImageOperation io = this;
		// if the source image size is not equal to the given mask, the source
		// image is resized
		if (image.getWidth() != mask.getWidth()
				|| image.getHeight() != mask.getHeight()) {
			io = resize(mask.getWidth(), mask.getHeight());
		}
		
		int[] maskPixels = mask.getAs1A();
		int[] originalImage = io.getImageAs1array();
		
		int idx = 0;
		for (int maskPixel : maskPixels) {
			if (maskPixel != background)
				maskPixels[idx] = originalImage[idx];
			idx++;
		}
		
		return new ImageOperation(maskPixels, mask.getWidth(), mask.getHeight());
	}
	
	public ImageOperation erodeRetainingLines() {
		int[][] img = getImageAs2array();
		int w = image.getWidth();
		int h = image.getHeight();
		int[][] res = new int[w][h];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int neighCnt = 0;
				if (img[x][y] == ImageOperation.BACKGROUND_COLORint) {
					res[x][y] = ImageOperation.BACKGROUND_COLORint;
				} else {
					for (int xd = -1; xd <= 1; xd++)
						for (int yd = -1; yd <= 1; yd++) {
							if (img[x + xd][y + yd] != ImageOperation.BACKGROUND_COLORint)
								neighCnt++;
						}
					if (neighCnt > 3 && neighCnt < 9)
						res[x][y] = ImageOperation.BACKGROUND_COLORint;
					else
						res[x][y] = img[x][y];
				}
			}
		}
		return new ImageOperation(res);
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
	
	public ImageOperation dilate(int n) {
		for (int i = 1; i <= n; i++)
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
	
	public ImageOperation erode(int n) { // es wird der 3x3 Minimum-Filter genutzt
		for (int i = 0; i < n; i++)
			image.getProcessor().erode();
		return new ImageOperation(image);
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
	 * INFO: Works on colorful image.
	 * 
	 * @author pape, klukas
	 */
	public ImageOperation dilatationColorImage() {
		int[][] src_image = getImageAs2array();
		int w = src_image.length;
		int h = src_image[0].length;
		int[][] image_result = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (src_image[x][y] != ImageOperation.BACKGROUND_COLORint) {
					for (int xd = -1; xd <= 1; xd++) {
						for (int yd = -1; yd <= 1; yd++) {
							if (x + xd <= w - 1 && y + yd <= h - 1)
								if (image_result[x + xd][y + yd] == ImageOperation.BACKGROUND_COLORint)
									image_result[x + xd][y + yd] = src_image[x][y];
						}
					}
				}
			}
		}
		return new ImageOperation(getImageAs2array());
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
		boolean fast_but_incorrect = false;
		if (fast_but_incorrect) {
			ImageOperation io = erodeNG(n1);
			io = io.dilateNG(n2);
			return io;
		} else {
			for (int i = 0; i < n1; i++)
				image.getProcessor().erode();
			for (int i = 0; i < n2; i++)
				image.getProcessor().dilate();
			return this;
		}
	}
	
	/**
	 * increase area of mask
	 * 
	 * @param n
	 *           number of times the code should be run
	 */
	@Deprecated
	public ImageOperation dilateNG(int n) {
		int[] imagePixels = getImageAs1array();
		int back = ImageOperation.BACKGROUND_COLORint;
		int w = image.getWidth();
		int h = image.getHeight();
		for (int i = 0; i < n; i++) {
			int p;
			// run through from left top to bottom right and
			// add one pixel left to any non-background pixel and one pixel
			// above any non-background pixel
			// the first line of pixels may be omitted, as above there can't
			// be pixels to be set
			for (int idx = w + 1; idx < imagePixels.length; idx++) {
				p = imagePixels[idx];
				if (p != back) {
					imagePixels[idx - w] = p;
					if ((idx - 1) % w != 0)
						imagePixels[idx - 1] = p;
				}
			}
			// run through from bottom right to top left (reverse)
			// add one pixel right to any non-background pixel and one pixel
			// below any non-background pixel
			for (int idx = imagePixels.length - 1 - w - 1; idx >= 0; idx--) {
				p = imagePixels[idx];
				if (p != back) {
					imagePixels[idx + w] = p;
					if (idx % w != 0)
						imagePixels[idx + 1] = p;
				}
			}
		}
		return new ImageOperation(imagePixels, w, h);
	}
	
	/**
	 * increase area of mask
	 * 
	 * @param n
	 *           number of times the code should be run
	 */
	@Deprecated
	public ImageOperation dilateNG(int n, FlexibleImage inputImageForNewPixels) {
		int[] imagePixels = getImageAs1array();
		int[] inputImagePixels = inputImageForNewPixels.getAs1A();
		int back = ImageOperation.BACKGROUND_COLORint;
		int w = image.getWidth();
		int h = image.getHeight();
		for (int i = 0; i < n; i++) {
			int p;
			// run through from left top to bottom right and
			// add one pixel left to any non-background pixel and one pixel
			// above any non-background pixel
			// the first line of pixels may be omitted, as above there can't
			// be pixels to be set
			for (int idx = w + 1; idx < imagePixels.length; idx++) {
				p = imagePixels[idx];
				if (p != back) {
					imagePixels[idx - w] = inputImagePixels[idx - w];;
					if ((idx - 1) % w != 0)
						imagePixels[idx - 1] = inputImagePixels[idx - 1];
				}
			}
			// run through from bottom right to top left (reverse)
			// add one pixel right to any non-background pixel and one pixel
			// below any non-background pixel
			for (int idx = imagePixels.length - 1 - w - 1; idx >= 0; idx--) {
				p = imagePixels[idx];
				if (p != back) {
					imagePixels[idx + w] = inputImagePixels[idx + w];;
					if (idx % w != 0)
						imagePixels[idx + 1] = inputImagePixels[idx + 1];;
				}
			}
		}
		return new ImageOperation(imagePixels, w, h);
	}
	
	/**
	 * reduce area of mask
	 * 
	 * @param n
	 *           number of times the code should be run
	 */
	private ImageOperation erodeNG(int n) {
		// todo this command does not work correctly, yet
		int[] imagePixels = getImageAs1array();
		int back = ImageOperation.BACKGROUND_COLORint;
		int w = image.getWidth();
		int h = image.getHeight();
		for (int i = 0; i < n; i++) {
			// remove any pixel that has not 4 neighbors
			
			// set any pixel value to the number of neighbors
			// later all pixels with less than 4 neighbors are set to background
			int x = 0;
			int y = 0;
			for (int idx = w + 1; idx < imagePixels.length; idx++) {
				if (x > 0 && x != w - 1 && y != 0 && y < h - 1) {
					int p = imagePixels[idx];
					if (p != back) {
						int nc = 0;
						if (imagePixels[idx - w] != back) // above
							nc++;
						if (imagePixels[idx - 1] != back) // left
							nc++;
						if (imagePixels[idx + 1] != back) // right
							nc++;
						if (imagePixels[idx + w] != back) // below
							nc++;
						imagePixels[idx] = nc;
					}
				}
				x++;
				if (x == w - 1) {
					x = 0;
					y++;
				}
			}
			for (int idx = w + 1; idx < imagePixels.length; idx++) {
				int p = imagePixels[idx];
				if (p != back) {
					if (p < 4)
						imagePixels[idx] = back;
				}
			}
		}
		return new ImageOperation(imagePixels, w, h);
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
	
	/**
	 * WARNING: NON-STANDARD METHOD (no return)
	 */
	@Deprecated
	public void drawRect(int leftX, int leftY, int width, int heigh) {
		image.getProcessor().drawRect(leftX, leftY, width, heigh);
	}
	
	@Deprecated
	public void fillRect(int leftX, int leftY, int width, int heigh) {
		image.getProcessor().fill(new Roi(leftX, leftY, width, heigh));
	}
	
	public ImageOperation fillRect2(int leftX, int leftY, int width, int heigh) {
		image.getProcessor().fill(new Roi(leftX, leftY, width, heigh));
		return new ImageOperation(image.getProcessor().getBufferedImage());
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
	
	/**
	 * Warning: PROBABLY NOT WORKING CORRECTLY
	 * CK
	 */
	@Deprecated
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
		return removeSmallClusters(nextGeneration, factor, (image.getWidth() / 100) * 2, NeighbourhoodSetting.NB4, typ,
				optClusterSizeReturn);
	}
	
	public ImageOperation removeSmallClusters(boolean nextGeneration, double cutOffPercentageOfImage, double cutoffDimension,
			NeighbourhoodSetting nb, CameraPosition typ,
			ObjectRef optClusterSizeReturn) {
		return removeSmallClusters(nextGeneration, cutOffPercentageOfImage, cutoffDimension, nb, typ, optClusterSizeReturn);
	}
	
	public ImageOperation removeSmallClusters(boolean nextGeneration, double cutOffPercentageOfImage, int cutOffVertHorOfImage,
			NeighbourhoodSetting nb, CameraPosition typ,
			ObjectRef optClusterSizeReturn) {
		boolean considerArea = false;
		return removeSmallClusters(nextGeneration, cutOffPercentageOfImage, cutOffVertHorOfImage, nb, typ, optClusterSizeReturn, considerArea);
	}
	
	public ImageOperation removeSmallClusters(boolean nextGeneration, double cutOffPercentageOfImage, int cutOffVertHorOfImage,
			NeighbourhoodSetting nb, CameraPosition typ,
			ObjectRef optClusterSizeReturn, boolean considerArea) {
		FlexibleImage workImage = new FlexibleImage(image);
		workImage = removeSmallPartsOfImage(nextGeneration, workImage,
				ImageOperation.BACKGROUND_COLORint,
				(int) (image.getWidth() * image.getHeight() * cutOffPercentageOfImage), cutOffVertHorOfImage, nb, typ,
				optClusterSizeReturn, considerArea);
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
	
	public ImageOperation print(String title, boolean doIt) {
		if (doIt)
			new FlexibleImage(image).copy().print(title);
		return this;
	}
	
	public ImageOperation print(String title) {
		return print(title, true);
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
		return new FlexibleImage(image.getWidth(), image.getHeight(), img);
	}
	
	public ImageOperation blur(double radius) {
		boolean oldStyle = true;
		if (oldStyle) {
			Prefs.setThreads(1);
			GaussianBlur gb = new GaussianBlur();
			gb.blurGaussian(image.getProcessor(), radius, radius, 0.001);
			return this;// new ImageOperation(new FlexibleImage(getImage().getAs2A()));
		} else {
			int sidePixels = (int) (radius * 2d);
			if (sidePixels < 3)
				sidePixels = 3;
			int[] img = getImageAs1array();
			int[] imgR = new int[img.length];
			int[] imgG = new int[img.length];
			int[] imgB = new int[img.length];
			int w = image.getWidth();
			int h = image.getHeight();
			for (int i = 0; i < img.length; i++) {
				int p = img[i];
				imgR[i] = (p & 0xff0000) >> 16;
				imgG[i] = (p & 0x00ff00) >> 8;
				imgB[i] = (p & 0x0000ff);
			}
			int sideL = -sidePixels / 2;
			int sideR = sidePixels / 2;
			int sumR, sumG, sumB;
			for (int i = 0; i < img.length; i++) {
				sumR = 0;
				sumG = 0;
				sumB = 0;
				int n = 0;
				for (int y = sideL; y < sideR; y++) {
					int yw = y * w;
					for (int x = sideL; x < sideR; x++) {
						int idx = i + x + yw;
						if (idx < 0)
							idx = 0;
						else
							if (idx >= img.length)
								idx = img.length - 1;
						sumR += imgR[idx];
						sumG += imgG[idx];
						sumB += imgB[idx];
						n++;
					}
				}
				sumR /= n;
				img[i] = (0xFF << 24 | (sumR & 0xFF) << 16) | ((sumG & 0xFF) << 8) | ((sumB & 0xFF) << 0);
			}
			return new ImageOperation(img, w, h);
		}
	}
	
	public static FlexibleImage removeSmallPartsOfImage(
			boolean nextGeneration,
			FlexibleImage workImage, int iBackgroundFill,
			int cutOffMinimumArea, int cutOffMinimumDimension, NeighbourhoodSetting nb, CameraPosition typ,
			ObjectRef optClusterSizeReturn) {
		boolean considerArea = false;
		return removeSmallPartsOfImage(nextGeneration, workImage, iBackgroundFill, cutOffMinimumArea, cutOffMinimumDimension, nb, typ, optClusterSizeReturn,
				considerArea);
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
			ObjectRef optClusterSizeReturn,
			boolean considerArea) {
		
		if (cutOffMinimumArea < 1) {
			System.out.println("WARNING: Too low minimum pixel size for object removal: " + cutOffMinimumArea + ". Set to 1.");
			cutOffMinimumArea = 1;
		}
		
		if (cutOffMinimumDimension < 1) {
			System.out.println("WARNING: Too low minimum pixel size for object removal: " + cutOffMinimumDimension + ". Set to 1.");
			cutOffMinimumDimension = 1;
		}
		
		if (!considerArea)
			cutOffMinimumArea = 0;
		
		Segmentation ps;
		
		if (nextGeneration)
			ps = new ClusterDetection(workImage, ImageOperation.BACKGROUND_COLORint);
		else
			ps = new PixelSegmentation(workImage, NeighbourhoodSetting.NB4);
		try {
			ps.detectClusters();
		} catch (ArrayIndexOutOfBoundsException er) {
			if (nextGeneration) {
				System.out.println("WARNING/ERROR: NEXT GENERATION CLUSTER DETECTION: ARRAY-INDEX-OUT-OF-BOUNDS EXCEPTION - retry with older code");
				ps = new PixelSegmentation(workImage, NeighbourhoodSetting.NB4);
				ps.detectClusters();
			} else
				throw er;
		}
		
		int[] clusterSizes = null;
		int[] clusterDimensionMinWH = null;
		
		if (optClusterSizeReturn != null)
			optClusterSizeReturn.setObject(ps.getClusterSize());
		clusterDimensionMinWH = ps.getClusterDimensionMinWH();
		
		Vector2i[] clusterCenter = ps.getClusterCenterPoints();
		Vector2i[] clusterDimensions2d = ps.getClusterDimension();
		clusterSizes = ps.getClusterSize();
		
		boolean[] toBeDeletedClusterIDs = new boolean[clusterCenter.length];
		
		// HashSet<Integer> toBeDeletedClusterIDs = new HashSet<Integer>();
		if (typ == CameraPosition.TOP) {
			List<LargeCluster> largeClusters = new ArrayList<LargeCluster>();
			if (clusterSizes != null)
				for (int index = 0; index < clusterSizes.length; index++) {
					if (clusterDimensionMinWH[index] >= cutOffMinimumDimension) {
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
							toBeDeletedClusterIDs[lc.getIndex()] = true;
							// toBeDeletedClusterIDs.add(lc.getIndex());
						} else {
							largestBounding.add(lc.getBoundingBox(cutOffMinimumDimension * 5));
						}
					}
				}
			}
		}
		
		int[] rgbArray = workImage.getAs1A();
		int[] mask = ps.getImageClusterIdMask();
		if (clusterDimensionMinWH != null && clusterDimensionMinWH.length > 0)
			for (int idx = 0; idx < rgbArray.length; idx++) {
				int clusterID = mask[idx];
				if (clusterID >= 0
						&&
						(
								(
								clusterDimensionMinWH[clusterID] < cutOffMinimumDimension
								|| clusterSizes[clusterID] <= cutOffMinimumArea
								)
								|| (clusterDimensionMinWH[clusterID] >= cutOffMinimumDimension &&
						// toBeDeletedClusterIDs.contains(clusterID)
						toBeDeletedClusterIDs[clusterID]
								)))
					rgbArray[idx] = iBackgroundFill;
			}
		
		int w = workImage.getWidth();
		int h = workImage.getHeight();
		return new FlexibleImage(w, h, rgbArray);
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
				if (img[x][y] != ImageOperation.BACKGROUND_COLORint) {
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
			int upperValueOfB, int background, CameraPosition typ, boolean maize) {
		
		int width = image.getProcessor().getWidth();
		int height = image.getProcessor().getHeight();
		
		int[] resultImage = new int[width * height];
		int[] img2d = getImageAs1array();
		
		thresholdLAB(width, height, img2d, resultImage, lowerValueOfL, upperValueOfL, lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB, background, typ, maize);
		
		return new ImageOperation(resultImage, width, height);
	}
	
	public static void thresholdLAB(int width, int height, int[] img2d, int[] resultImage,
			int lowerValueOfL, int upperValueOfL,
			int lowerValueOfA, int upperValueOfA,
			int lowerValueOfB, int upperValueOfB,
			int background, CameraPosition typ,
			boolean maize) {
		
		thresholdLAB3(width, height, img2d, resultImage,
					lowerValueOfL, upperValueOfL, lowerValueOfA, upperValueOfA, lowerValueOfB,
					upperValueOfB, background, typ, maize, false, null);
		
	}
	
	/**
	 * A method with the same name (without the "unclear2") exists,
	 * it is unclear if there is a difference.
	 * 
	 * @param oi
	 */
	public static FlexibleImage thresholdLAB3(int width, int height, int[] imagePixels, int[] resultImage,
			int lowerValueOfL, int upperValueOfL,
			int lowerValueOfA, int upperValueOfA,
			int lowerValueOfB, int upperValueOfB,
			int background, CameraPosition typ, boolean maize, boolean replaceBlueStick, int[][] oi) {
		int c, x, y = 0;
		int r, g, b;
		int Li, ai, bi;
		int maxDiffAleftBright, maxDiffArightBleft;
		
		if (typ == CameraPosition.SIDE) {
			maxDiffAleftBright = maize ? 7 : 3;
			maxDiffArightBleft = maize ? 7 : 3;
		} else {
			maxDiffAleftBright = maize ? 11 : 3; // 15
			maxDiffArightBleft = maize ? 7 : 3;
		}
		
		for (y = 0; y < height; y++) {
			int yw = y * width;
			for (x = 0; x < width; x++) {
				int off = x + yw;
				c = imagePixels[off];
				
				r = ((c & 0xff0000) >> 16);
				g = ((c & 0x00ff00) >> 8);
				b = (c & 0x0000ff);
				
				Li = (int) ImageOperation.labCube[r][g][b];
				ai = (int) ImageOperation.labCube[r][g][b + 256];
				bi = (int) ImageOperation.labCube[r][g][b + 512];
				
				if (resultImage[off] != background && (((Li > lowerValueOfL) && (Li < upperValueOfL) && (ai > lowerValueOfA) && (ai < upperValueOfA)
								&& (bi > lowerValueOfB) && (bi < upperValueOfB)) && !isGray(Li, ai, bi, maxDiffAleftBright, maxDiffArightBleft))) {
					resultImage[off] = imagePixels[off];
				} else {
					if (replaceBlueStick && maize && typ == CameraPosition.SIDE) {
						boolean backFound = false;
						boolean greenFound = false;
						int green = Color.GREEN.getRGB();
						for (int xd = 1; xd < 15; xd++) {
							off = x + xd + yw;
							
							if (off < 0 || off >= imagePixels.length)
								break;
							c = imagePixels[off];
							
							if (c == background) {
								backFound = true;
								break;
							} else {
								r = ((c & 0xff0000) >> 16);
								g = ((c & 0x00ff00) >> 8);
								b = (c & 0x0000ff);
								
								Li = (int) ImageOperation.labCube[r][g][b];
								ai = (int) ImageOperation.labCube[r][g][b + 256];
								bi = (int) ImageOperation.labCube[r][g][b + 512];
								
								if (ai < 120 && Math.abs(bi - 127) < 10) {
									greenFound = true;
									green = c;
									break;
								}
							}
						}
						
						boolean backFoundL = false;
						boolean greenFoundL = false;
						for (int xd = -1; xd > -15; xd--) {
							off = x + xd + yw;
							if (off < 0 || off >= imagePixels.length)
								break;
							c = imagePixels[off];
							
							if (c == background) {
								backFoundL = true;
								break;
							} else {
								r = ((c & 0xff0000) >> 16);
								g = ((c & 0x00ff00) >> 8);
								b = (c & 0x0000ff);
								
								Li = (int) ImageOperation.labCube[r][g][b];
								ai = (int) ImageOperation.labCube[r][g][b + 256];
								bi = (int) ImageOperation.labCube[r][g][b + 512];
								
								if (ai < 120 && Math.abs(bi - 127) < 10) {
									greenFoundL = true;
									green = c;
									break;
								}
							}
							off = x + yw;
							if (greenFound || greenFoundL) {
								c = imagePixels[off];
								r = ((c & 0xff0000) >> 16);
								g = ((c & 0x00ff00) >> 8);
								b = (c & 0x0000ff);
								oi[x][y] = new Color(r, b, g).getRGB();
							} else
								resultImage[off] = background;
						}
					} else
						resultImage[off] = background;
				}
			}
		}
		if (oi != null)
			return new FlexibleImage(oi);
		else
			return null;
	}
	
	public static float[][][] getLabCube() {
		if (IAPservice.getCurrentTimeAsNiceString() == null)
			System.out.println();
		StopWatch s = new StopWatch("lab_cube", false);
		final float step = 1f / 255f;
		final float[][][] result = new float[256][256][256 * 3];
		ExecutorService executor = Executors.newFixedThreadPool(SystemAnalysis.getNumberOfCPUs() <= 6 ? SystemAnalysis.getNumberOfCPUs() : 6);
		final float cont = 16f / 116f;
		for (int rr = 0; rr < 256; rr++) {
			final int red = rr;
			final float rd = (red / 255f);
			
			Runnable r = new Runnable() {
				@Override
				public void run() {
					float gd = -step;
					float X, Y, Z, fX, fY, fZ;
					float La, aa, bb;
					float[] p;
					for (int green = 0; green < 256; green++) {
						gd += step;
						float bd = -step;
						// moved outside of most inner loop to increase performance:
						float aXa = 1000 * (0.430587f * rd + 0.341545f * gd);
						float aYa = 1000 * (0.222021f * rd + 0.706645f * gd);
						float aZa = 1000 * (0.0201837f * rd + 0.129551f * gd);
						p = result[red][green];
						
						for (int blue = 0; blue < 256; blue++) {
							
							bd += step;
							
							boolean old = true;
							
							if (old) {
								// white reference D65 PAL/SECAM
								X = 0.430587f * rd + 0.341545f * gd + 0.178336f * bd;
								Y = 0.222021f * rd + 0.706645f * gd + 0.0713342f * bd;
								Z = 0.0201837f * rd + 0.129551f * gd + 0.939234f * bd;
								
								// XYZ to Lab
								if (X > 0.008856)
									fX = IAPservice.cubeRoots[(int) (1000 * X)];
								else
									fX = (7.78707f * X) + cont;// 7.7870689655172
									
								if (Y > 0.008856)
									fY = IAPservice.cubeRoots[(int) (1000 * Y)];
								else
									fY = (7.78707f * Y) + cont;
								
								if (Z > 0.008856)
									fZ = IAPservice.cubeRoots[(int) (1000 * Z)];
								else
									fZ = (7.78707f * Z) + cont;
								
								La = ((116 * fY) - 16) * 2.55f;
								aa = 1.0625f * (500f * (fX - fY)) + 128f;
								bb = 1.0625f * (200f * (fY - fZ)) + 128f;
							} else {
								// white reference D65 PAL/SECAM
								X = aXa + 178.336f * bd;
								Y = aYa + 71.3342f * bd;
								Z = aZa + 939.234f * bd;
								
								fX = IAPservice.cubeRoots[(int) X];
								fY = IAPservice.cubeRoots[(int) Y];
								fZ = IAPservice.cubeRoots[(int) Z];
								
								La = ((116 * fY) - 16) * 2.55f;
								aa = 1.0625f * (500f * (fX - fY)) + 128f;
								bb = 1.0625f * (200f * (fY - fZ)) + 128f;
							}
							p[blue] = La;
							p[blue + 256] = aa;
							p[blue + 512] = bb;
						}
					}
				}
			};
			executor.submit(r);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		s.printTime();
		analyzeCube(result);
		return result;
	}
	
	private static void analyzeCube(float[][][] labCube) {
		double lmi = java.lang.Double.MAX_VALUE, lma = 0, ami = java.lang.Double.MAX_VALUE, ama = 0, bmi = java.lang.Double.MAX_VALUE, bma = 0;
		for (int r = 0; r < 255; r++)
			for (int g = 0; g < 255; g++)
				for (int b = 0; b < 255; b++) {
					float lf = labCube[r][g][b];
					float af = labCube[r][g][b + 256];
					float bf = labCube[r][g][b + 512];
					
					lmi = lf < lmi ? lf : lmi;
					ami = af < ami ? af : ami;
					bmi = bf < bmi ? bf : bmi;
					
					lma = lf > lma ? lf : lma;
					ama = af > ama ? af : ama;
					bma = bf > bma ? bf : bma;
				}
		System.out.println("L:[" + lmi + "," + lma + "]");
		System.out.println("A:[" + ami + "," + ama + "]");
		System.out.println("B:[" + bmi + "," + bma + "]");
	}
	
	public static double MathPow(double v, double ot) {
		// if (v < 0 || v > 1.1) {
		// System.out.println("TODO: " + v);
		// return Math.pow(v, ot);
		// } else
		return IAPservice.cubeRoots[(int) (1000 * v)];
	}
	
	private static boolean isGray(int li, int ai, int bi, int maxDiffAleftBright, int maxDiffArightBleft) {
		ai = ai - 127;
		boolean aNoColor, bNoColor;
		
		if (ai < 0)
			aNoColor = -ai < maxDiffAleftBright;
		else
			aNoColor = ai < maxDiffArightBleft;
		
		if (aNoColor) {
			bi = bi - 127;
			if (bi < 0)
				bNoColor = -bi < maxDiffArightBleft;
			else
				bNoColor = bi < maxDiffAleftBright;
			
			return bNoColor;
		} else
			return false;
	}
	
	public Lab getLABAverage(int[][] img2d, int x1, int y1, int w, int h) {
		int c, x, y = 0;
		int r, g, b;
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
		return medianFilter32Bit(true);
	}
	
	public ImageOperation medianFilter32Bit(boolean performanceOptimized) {
		if (performanceOptimized) {
			int[] img = getImageAs1array();
			int w = image.getWidth();
			int h = image.getHeight();
			int[] out = new int[img.length];
			int last = img.length - w;
			boolean jmp = true;
			for (int i = 0; i < img.length; i++) {
				if (i > w && i < last) {
					int center = img[i];
					int above = img[i - w];
					int left = img[i - 1];
					int right = img[i + 1];
					int below = img[i + w];
					if (jmp)
						out[i] = median(center, above, left, right, below);
					else
						out[i] = findMedianOf9(new int[] { center, above, left, right, below });
				} else
					out[i] = img[i];
			}
			return new ImageOperation(out, w, h);
		} else {
			image.getProcessor().medianFilter();
			return new ImageOperation(getImage());
		}
	}
	
	public ImageOperation medianFilter32BitvariableMask(int n) {
		int[] img = getImageAs1array();
		int w = image.getWidth();
		int h = image.getHeight();
		int[] out = new int[img.length];
		int[] mask = new int[(2 * n + 1) * (2 * n + 1)];
		for (int i = 0; i < img.length; i++) {
			int filled = 0;
			for (int x = -n; x < n; x++)
				for (int y = -n; y < n; y++) {
					int xCurrent = i % w;
					int yCurrent = i / w;
					int ii = i;
					if (xCurrent <= n)
						ii += n - xCurrent;
					if (xCurrent >= w - n)
						ii -= xCurrent - w + n;
					if (yCurrent <= n)
						ii += w * n - yCurrent;
					if (yCurrent >= h - n)
						ii -= (yCurrent - h + n) * w;
					int index = ii + x + y * w;
					if (index < 0)
						index = 0;
					if (index >= img.length)
						index = img.length - 1;
					int pixel = img[index];
					mask[filled++] = pixel;
				}
			out[i] = median(mask);
		}
		return new ImageOperation(out, w, h);
	}
	
	private final int findMedianOf9(int[] values) {
		// Finds the 5th largest of 9 values
		for (int i = 1; i <= 4; i++) {
			int max = 0;
			int mj = 1;
			for (int j = 1; j <= 9; j++)
				if (values[j] > max) {
					max = values[j];
					mj = j;
				}
			values[mj] = 0;
		}
		int max = 0;
		for (int j = 1; j <= 9; j++)
			if (values[j] > max)
				max = values[j];
		return max;
	}
	
	private int median(int center, int above, int left, int right, int below) {
		int[] temp = { center, above, left, right, below };
		java.util.Arrays.sort(temp);
		return temp[2];
	}
	
	private int median(int[] temp) {
		java.util.Arrays.sort(temp);
		return temp[(temp.length + 1) / 2 - 1];
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
	public ImageOperation thresholdCreateBinaryImage(int cutValue) {
		ByteProcessor byteProcessor = new BinaryProcessor(
				(ByteProcessor) image.getProcessor());
		byteProcessor.threshold(cutValue);
		
		// image ==> byteProcessor.getBufferedImage() (ck, 26.6.11)
		return new ImageOperation(image);
	}
	
	public ImageOperation thresholdBlueHigherThan(int threshold) {
		int[] res = getImageAs1array();
		int b;
		int back = ImageOperation.BACKGROUND_COLORint;
		int idx = 0;
		for (int c : res) {
			if (c == back) {
				idx++;
				continue;
			}
			b = (c & 0x0000ff);
			if (b > threshold)
				res[idx] = back;
			idx++;
		}
		return new ImageOperation(res, image.getWidth(), image.getHeight());
	}
	
	public ImageOperation thresholdGrayClearLowerThan(int threshold, int back) {
		int[] res = getImageAs1array();
		int b;
		int idx = 0;
		for (int c : res) {
			if (c == back) {
				idx++;
				continue;
			}
			b = (c & 0x0000ff);
			if (b < threshold)
				res[idx] = back;
			idx++;
		}
		return new ImageOperation(res, image.getWidth(), image.getHeight());
	}
	
	public ImageOperation thresholdClearBlueBetween(int thresholdStart, int thresholdEnd) {
		int[] res = getImageAs1array();
		int b;
		int back = ImageOperation.BACKGROUND_COLORint;
		int idx = 0;
		for (int c : res) {
			if (c == back) {
				idx++;
				continue;
			}
			b = (c & 0x0000ff);
			if (b >= thresholdStart && b <= thresholdEnd)
				res[idx] = back;
			idx++;
		}
		return new ImageOperation(res, image.getWidth(), image.getHeight());
	}
	
	public ImageOperation enhanceContrast() {
		ContrastEnhancer ce = new ContrastEnhancer();
		ce.equalize(image);
		
		return new ImageOperation(image);
	}
	
	public ImageOperation convertBinary2rgb() {
		int[] bi = ImageConverter.convertBIto1A(image.getProcessor().getBufferedImage());
		return new ImageOperation(new FlexibleImage(image.getWidth(), image.getHeight(), bi));
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
	
	public ArrayList<MarkerPair> searchBlueMarkers(double options, CameraPosition typ, boolean maize) {
		BlueMarkerFinder bmf = new BlueMarkerFinder(getImage(), options, typ, maize);
		
		bmf.findCoordinates(ImageOperation.BACKGROUND_COLORint);
		
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
			else
				idx++;
		}
		return new ImageOperation(new FlexibleImage(w, h, resultMask));
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
	 * @param borderColor
	 *           - color of the border
	 * @return
	 */
	public ImageOperation addBorder(int bordersize, int translatex, int translatey, int borderColor) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		int[][] img2d = getImageAs2array();
		int[][] result = new int[width + (2 * bordersize)][height + (2 * bordersize)];
		
		result = fillArray(result, borderColor);
		
		for (int xt = bordersize + translatex; xt < (width + bordersize + translatex); xt++) {
			for (int yt = bordersize + translatey; yt < (height + bordersize + translatey); yt++) {
				if (xt - bordersize - translatex >= 0 && yt - bordersize - translatey >= 0 && xt >= 0 && yt >= 0)
					result[xt][yt] = img2d[xt - bordersize - translatex][yt - bordersize - translatey];
			}
		}
		return new ImageOperation(result);
	}
	
	public static int[][] fillArray(int[][] result, int background) {
		for (int x = 0; x < result.length; x++)
			for (int y = 0; y < result[0].length; y++)
				result[x][y] = background;
		return result;
	}
	
	public static int[] fillArray(int[] result, int background) {
		for (int x = 0; x < result.length; x++)
			result[x] = background;
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
	
	public int countFilledPixels() {
		int back = ImageOperation.BACKGROUND_COLORint;
		return countFilledPixels(back);
	}
	
	/**
	 * All Pixels will be count, which are not equal to the background color (PhenotypeAnalysisTask.BACKGROUND_COLORint).
	 * 
	 * @return Number of non-background pixels.
	 */
	public int countFilledPixels(int back) {
		int res = 0;
		int background = back;
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
	public double intensitySumOfChannel(boolean performGrayScale, boolean red, boolean green, boolean blue) {
		double res = 0;
		int background = ImageOperation.BACKGROUND_COLORint;
		int[] img2d = getImageAs1array();
		
		int[] grayScaledIfNeeded;
		if (performGrayScale)
			grayScaledIfNeeded = convert2Grayscale().getImageAs1array();
		else
			grayScaledIfNeeded = img2d;
		
		int idx = 0;
		for (int c : img2d) {
			if (c != background) {
				int cg = grayScaledIfNeeded[idx];
				if (red) {
					double rf = ((cg & 0xff0000) >> 16) / 255.0; // B 0..1
					res += rf;
				}
				if (green) {
					double rf = ((cg & 0x00ff00) >> 8) / 255.0; // B 0..1
					res += rf;
				}
				if (blue) {
					double rf = ((cg & 0x0000ff)) / 255.0; // B 0..1
					res += rf;
				}
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
			if (threshold > img2d.length)
				threshold = img2d.length;
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
			if (threshold < 0)
				threshold = 0;
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
		
		int backgroundColor = ImageOperation.BACKGROUND_COLORint;
		
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
	public ImageOperation multiplicateImageChannelsWithFactors(double[] factorsTop, double[] factorsBottom) {
		int[][] img2d = getImageAs2array();
		int width = getImage().getWidth();
		int height = getImage().getHeight();
		double rf, gf, bf;
		int[][] result = new int[width][height];
		for (int y = 0; y < height; y++) {
			double rfff = (factorsBottom[0] - factorsTop[0]) / height * y + factorsTop[0];
			double gfff = (factorsBottom[1] - factorsTop[1]) / height * y + factorsTop[1];
			double bfff = (factorsBottom[2] - factorsTop[2]) / height * y + factorsTop[2];
			for (int x = 0; x < width; x++) {
				int c = img2d[x][y];
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
				result[x][y] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
			}
		}
		return new ImageOperation(result);
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
	public float[] getRGBAverage(int x1, int y1, int w, int h, int LThresh, int ABThresh, boolean mode) {
		return getRGBAverage(x1, y1, w, h, LThresh, ABThresh, mode, 0);
	}
	
	private float[] getRGBAverage(int x1, int y1, int w, int h, int LThresh, int ABThresh, boolean searchWhiteTrue, int recursion) {
		int r, g, b, c, temp = 0;
		float Li, ai, bi;
		
		// sums of RGB
		int sumR = 0;
		int sumG = 0;
		int sumB = 0;
		
		int count = 0;
		
		int[][] img2d = getImageAs2array();
		float[] p;
		for (int x = x1; x < x1 + w; x++) {
			for (int y = y1; y < y1 + h; y++) {
				
				c = img2d[x][y];
				if (c != -2049)
					temp++;
				r = (c & 0xff0000) >> 16;
				g = (c & 0x00ff00) >> 8;
				b = c & 0x0000ff;
				p = ImageOperation.labCube[r][g];
				Li = p[b];
				ai = p[b + 256];
				bi = p[b + 512];
				
				// sum under following conditions
				if (searchWhiteTrue) {
					if (Li > LThresh && (ai - 127 < ABThresh || -ai + 127 < ABThresh) && (bi - 127 < ABThresh || -bi + 127 < ABThresh)) {
						sumR += r;
						sumG += g;
						sumB += b;
						count++;
					}
				} else {
					if (Li < LThresh && (ai - 127 < ABThresh || -ai + 127 < ABThresh) && (bi - 127 < ABThresh || -bi + 127 < ABThresh)) {
						sumR += r;
						sumG += g;
						sumB += b;
						count++;
					}
				}
			}
		}
		// System.out.println("temp :: " + temp);
		if (count < w * h * 0.1 && recursion < 30) {
			return getRGBAverage(x1, y1, w, h, LThresh * 2, (int) (ABThresh * 1.1), searchWhiteTrue, recursion + 1);
		}
		if (count > 0)
			return new float[] { sumR / 255f / count, sumG / 255f / count, sumB / 255f / count };
		else
			return new float[] { 1, 1, 1 };
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
		ImageOperation res = null;
		if (rgbInfo.length > 3 && rgbInfo.length <= 6) {
			double r1 = brightness / rgbInfo[0];
			double g1 = brightness / rgbInfo[1];
			double b1 = brightness / rgbInfo[2];
			double r2 = brightness / rgbInfo[3];
			double g2 = brightness / rgbInfo[4];
			double b2 = brightness / rgbInfo[5];
			double[] factorsTop = { r1, g1, b1 };
			double[] factorsBottom = { r2, g2, b2 };
			// System.out.println("balance factors: " + r + " " + g + " " + b);
			ImageOperation io = new ImageOperation(image);
			res = io.multiplicateImageChannelsWithFactors(factorsTop, factorsBottom);
			if (r1 + g1 + b1 + r2 + g2 + b2 > 120) {
				res = res.blur(10);
				res = res.multiplyHSV(2, 1.4, 0.9);
			}
		}
		if (rgbInfo.length > 6) {
			double[] factorsTopRight = { brightness / rgbInfo[0] };
			double[] factorsBottomLeft = { brightness / rgbInfo[3] };
			double[] factorsCenter = { brightness / rgbInfo[6] };
			
			ImageOperation io = new ImageOperation(image);
			res = io.rmCircleShade(factorsTopRight, factorsBottomLeft, factorsCenter);
		}
		if (rgbInfo.length <= 3) {
			double r = brightness / rgbInfo[0];
			double g = brightness / rgbInfo[1];
			double b = brightness / rgbInfo[2];
			double[] factors = { r, g, b };
			// System.out.println("balance factors: " + r + " " + g + " " + b);
			ImageOperation io = new ImageOperation(image);
			res = io.multiplicateImageChannelsWithFactors(factors);
			if (r + g + b > 60) {
				res = res.blur(10);
				res = res.multiplyHSV(2, 1.4, 0.9);
			}
		}
		return res;
	}
	
	private ImageOperation rmCircleShade(double[] factorsTopRight, double[] factorsBottomLeft, double[] factorsCenter) {
		int[][] img = getImageAs2array();
		int w = img.length;
		int h = img[0].length;
		int cx = w / 2;
		int cy = h / 2;
		int maxDistToCenter = (int) Math.sqrt(cx * cx + cy * cy);
		int distToCenter, pix;
		double fac;
		int[][] res = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				distToCenter = (int) Math.sqrt((cx - x) * (cx - x) + y * y);
				pix = img[x][y] & 0x0000ff;
				if (y <= h / 2)
					fac = ((factorsTopRight[0] - factorsCenter[0]) / (double) maxDistToCenter * distToCenter + factorsTopRight[0]);
				else
					fac = ((factorsBottomLeft[0] - factorsCenter[0]) / (double) maxDistToCenter * distToCenter + factorsBottomLeft[0]);
				pix = (int) (pix * fac);
				if (pix > 255)
					pix = 255;
				
				res[x][y] = (0xFF << 24 | (pix & 0xFF) << 16) | ((pix & 0xFF) << 8) | ((pix & 0xFF) << 0);
			}
		}
		return new ImageOperation(res);
	}
	
	private ImageOperation multiplyHSV(double hf, double sf, double vf) {
		int[] image = getImageAs1array();
		int width = getImage().getWidth();
		int height = getImage().getHeight();
		float[] hsbvals = new float[3];
		for (int idx = 0; idx < image.length; idx++) {
			int c = image[idx];
			int r = (c & 0xff0000) >> 16;
			int g = (c & 0x00ff00) >> 8;
			int b = (c & 0x0000ff);
			Color.RGBtoHSB(r, g, b, hsbvals);
			double blue = 0.8;
			hsbvals[0] = (float) ((hsbvals[0] - blue) * hf + blue);
			boolean left = (idx % width) < width / 2;
			if (left)
				hsbvals[1] *= sf * 1.5;
			else
				hsbvals[1] *= sf;
			if (left)
				hsbvals[0] = (hsbvals[1]);
			else
				hsbvals[0] = hsbvals[1];
			if (left)
				hsbvals[2] *= vf * 0.95;
			else
				hsbvals[2] *= vf;
			
			hsbvals[2] = (float) ((hsbvals[2] - 0.7) * 1.5 + 0.5);
			
			if (hsbvals[1] < 0)
				hsbvals[1] = 0;
			if (hsbvals[1] > 1)
				hsbvals[1] = 1;
			if (hsbvals[2] < 0)
				hsbvals[2] = 0;
			if (hsbvals[2] > 1)
				hsbvals[2] = 1;
			image[idx] = Color.HSBtoRGB(hsbvals[0], hsbvals[1], hsbvals[2]);
		}
		return new ImageOperation(image, width, height);
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
		return new ImageOperation(inp).print("orig").subtractImages(blured, "").print("sub");
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
		float[][] labImage1 = getImage().getLab(false);
		float[][] labImage2 = image2.getLab(false);
		for (int idx = 0; idx < w * h; idx++) {
			float lDiff = labImage1[0][idx] - labImage2[0][idx];
			float aDiff = labImage1[1][idx] - labImage2[1][idx];
			float bDiff = labImage1[2][idx] - labImage2[2][idx];
			// if (lDiff < 0)
			// lDiff = 255 - lDiff;
			if (aDiff < 0)
				aDiff = -aDiff;
			if (bDiff < 0)
				bDiff = -bDiff;
			
			labImage1[0][idx] = 80 + lDiff; // 80 * (labImage1[0][idx] + labImage2[0][idx]) / 2 / 255d +
			labImage1[1][idx] = aDiff / 255f + 1;
			labImage1[2][idx] = bDiff / 255f + 1;
		}
		return new FlexibleImage(w, h, labImage1).getIO();
	}
	
	public ImageOperation subtractGrayImages(FlexibleImage image2) {
		int w = getImage().getWidth();
		int h = getImage().getHeight();
		int[] img1 = getImageAs1array();
		int[] img2 = image2.getAs1A();
		int[] res = new int[img1.length];
		
		for (int idx = 0; idx < w * h; idx++) {
			int c1 = img1[idx];
			int c2 = img2[idx];
			int b1 = (c1 & 0x0000ff);
			int b2 = (c2 & 0x0000ff);
			
			int b = b1 - b2 + 127;
			if (b < 0)
				b = 0;
			if (b > 255)
				b = 255;
			int r = Math.abs(b - 127) + 127;
			int g = Math.abs(b - 127) + 127;
			res[idx] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
			
		}
		return new FlexibleImage(w, h, res).getIO();
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
	
	public ImageOperation dilateHorizontal(int maskWidth) {
		int[] img = getImageAs1array();
		int w = image.getWidth();
		int h = image.getHeight();
		int[] out = new int[img.length];
		int offX = maskWidth / 2;
		for (int i = 0; i < img.length; i++) {
			int cnt = 0;
			int color = ImageOperation.BACKGROUND_COLORint;
			for (int xdiff = -offX; xdiff <= offX; xdiff++) {
				int ii = i + xdiff;
				if (ii < 0)
					ii = 0;
				if (ii >= img.length)
					ii = img.length - 1;
				if (img[ii] != ImageOperation.BACKGROUND_COLORint) {
					cnt++;
					color = Color.GREEN.getRGB();
				}
			}
			if (cnt > 0) {
				out[i] = color;
			} else
				out[i] = img[i];
		}
		return new ImageOperation(out, w, h);
	}
	
	public ImageCanvas getCanvas() {
		return new ImageCanvas(getImage());
	}
	
	public ImageOperation copyOnImage(FlexibleImage image2) {
		return copyOnImage(image2, 2);
	}
	
	public ImageOperation copyOnImage(FlexibleImage image2, int size) {
		int[][] res = getImageAs2array();
		int[][] skelImg = image2.getAs2A();
		int w = image.getWidth();
		int h = image.getHeight();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (skelImg[x][y] != SkeletonProcessor2d.background) {
					int v = skelImg[x][y];
					int r = size;
					if (v == SkeletonProcessor2d.colorEndpoints)
						r = 18;
					if (v == SkeletonProcessor2d.colorBranches)
						r = 3;
					if (v == SkeletonProcessor2d.colorBloomEndpoint)
						r = 20;
					for (int diffX = -r; diffX < r; diffX++)
						for (int diffY = -r; diffY < r; diffY++) {
							if ((v == SkeletonProcessor2d.colorEndpoints || v == SkeletonProcessor2d.colorBloomEndpoint) &&
									((diffX * diffX + diffY * diffY) <= 12 * 12)) // ||
								// (diffX * diffX + diffY * diffY) >= 20 * 20)
								continue;
							if (x - diffX >= 0 && y - diffY >= 0 && x - diffX < w && y - diffY < h)
								res[x - diffX][y - diffY] = v;// avg(v, plantImg[index - diffX + w * diffY]);
						}
				}
			}
		}
		return new ImageOperation(res);
	}
	
	public ImageOperation copy() {
		return new ImageOperation(getImage().copy());
	}
	
	/**
	 * @param n
	 *           - size of the local region to detect threshold
	 * @param assumedBackground
	 * @return
	 * @author pape
	 */
	public ImageOperation adaptiveThresholdForGrayscaleImage(int n, int assumedBackground, int newForeground) {
		int[][] img = getImageAs2array();
		int w = image.getWidth();
		int h = image.getHeight();
		int[][] out = new int[w][h];
		int x, y, thresh, pix, temp = 0;
		double K = 0.05d;
		int[] mean = new int[n * n];
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				// Check the local neighbourhood
				for (int k = 0; k < n; k++) {
					for (int l = 0; l < n; l++) {
						x = i - ((int) (n / 2)) + k;
						y = j - ((int) (n / 2)) + l;
						if (x > 0 && x < w && y > 0 && y < h) {
							temp = img[x][y] & 0x0000ff;
							mean[k * n + l] = temp;
						} else
							mean[k * n + l] = assumedBackground;
					}
				}
				// Find the threshold value
				// thresh = mean(mean);
				// thresh = median(mean);
				thresh = (int) (mean(mean) * (1 + K * ((img[i][j] & 0x0000ff) / assumedBackground - 1))); // http://www.dfki.uni-kl.de/~shafait/papers/Shafait-efficient-binarization-SPIE08.pdf
				
				pix = img[i][j] & 0x0000ff;
				if (pix > thresh) {
					out[i][j] = newForeground;
				} else {
					out[i][j] = img[i][j];
				}
			}
		}
		return new ImageOperation(out);
	}
	
	private int mean(int[] temp) {
		int sum = 0;
		for (int i = 0; i < temp.length; i++)
			sum += temp[i];
		return (int) (sum / (double) temp.length);
	}
}
