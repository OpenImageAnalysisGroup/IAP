package iap.blocks.image_analysis_tools.imageJ.externalPlugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.Undo;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Arrays;

/**
 * <p>
 * This is a plugin version of Tom Gibara's implementation of the Canny edge detection algorithm in Java, available at:
 * </p>
 * http://www.tomgibara.com/computer-vision/canny-edge-detector
 * <p>
 * <em>This software has been released into the public domain.
 * <strong>Please read the notes in this source file for additional information.
 * </strong></em>
 * </p>
 * <p>
 * This class provides a configurable implementation of the Canny edge detection algorithm. This classic algorithm has a number of shortcomings, but remains an
 * effective tool in many scenarios. <em>This class is designed
 * for single threaded use only.</em>
 * </p>
 * <p>
 * For a more complete understanding of this edge detector's parameters consult an explanation of the algorithm.
 * </p>
 * 
 * @author Tom Gibara
 */

public class CannyEdgeDetector implements PlugIn {
	
	// statics
	
	private final static float GAUSSIAN_CUT_OFF = 0.005f;
	private final static float MAGNITUDE_SCALE = 100F;
	private final static float MAGNITUDE_LIMIT = 1000F;
	private final static int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);
	
	// fields
	
	private int height;
	private int width;
	private int picsize;
	private int[] data;
	private int[] magnitude;
	private ImagePlus sourceImage;
	
	private static float sGaussianKernelRadius = 2f;
	private static float sLowThreshold = 2.5f;
	private static float sHighThreshold = 7.5f;
	
	private float gaussianKernelRadius = 2f;
	private float lowThreshold = 2.5f;
	private float highThreshold = 7.5f;
	private int gaussianKernelWidth = 16;
	private boolean contrastNormalized;
	
	private float[] xConv;
	private float[] yConv;
	private float[] xGradient;
	private float[] yGradient;
	
	@Override
	public void run(String arg) {
		sourceImage = IJ.getImage();
		if (sourceImage.getStackSize() > 1) {
			IJ.error("This plugin does not work with stacks");
			return;
		}
		if (!showDialog())
			return;
		Undo.setup(Undo.TYPE_CONVERSION, sourceImage);
		process();
		ImageProcessor ip = new FloatProcessor(sourceImage.getWidth(), sourceImage.getHeight(), data);
		ip = ip.convertToByte(false);
		if (!Prefs.blackBackground)
			ip.invertLut();
		sourceImage.setProcessor(ip);
	}
	
	public ImagePlus process(ImagePlus imp) {
		sourceImage = imp;
		process();
		ImageProcessor ip = new FloatProcessor(imp.getWidth(), imp.getHeight(), data);
		ip = ip.convertToByte(false);
		return new ImagePlus("Edges_" + imp.getTitle(), ip);
	}
	
	private boolean showDialog() {
		if (!IJ.isMacro()) {
			gaussianKernelRadius = sGaussianKernelRadius;
			lowThreshold = sLowThreshold;
			highThreshold = sHighThreshold;
		}
		GenericDialog gd = new GenericDialog("Canny Edge Detector");
		gd.addNumericField("Gaussian kernel radius:", gaussianKernelRadius, 1);
		gd.addNumericField("Low threshold:", lowThreshold, 1);
		gd.addNumericField("High threshold:", highThreshold, 1);
		// gd.addNumericField("Gaussian Kernel width:", gaussianKernelWidth, 0);
		gd.addCheckbox("Normalize contrast ", contrastNormalized);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		gaussianKernelRadius = (float) gd.getNextNumber();
		if (gaussianKernelRadius < 0.1f)
			gaussianKernelRadius = 0.1f;
		lowThreshold = (float) gd.getNextNumber();
		if (lowThreshold < 0.1f)
			lowThreshold = 0.1f;
		highThreshold = (float) gd.getNextNumber();
		if (highThreshold < 0.1f)
			highThreshold = 0.1f;
		// gaussianKernelWidth = (int)gd.getNextNumber();
		contrastNormalized = gd.getNextBoolean();
		if (!IJ.isMacro()) {
			sGaussianKernelRadius = gaussianKernelRadius;
			sLowThreshold = lowThreshold;
			sHighThreshold = highThreshold;
		}
		return true;
	}
	
	/**
	 * Sets the low threshold for hysteresis. Suitable values for this parameter
	 * must be determined experimentally for each application. It is nonsensical
	 * (though not prohibited) for this value to exceed the high threshold value.
	 * 
	 * @param threshold
	 *           a low hysteresis threshold
	 */
	public void setLowThreshold(float threshold) {
		if (threshold < 0)
			throw new IllegalArgumentException();
		lowThreshold = threshold;
	}
	
	/**
	 * Sets the high threshold for hysteresis. Suitable values for this
	 * parameter must be determined experimentally for each application. It is
	 * nonsensical (though not prohibited) for this value to be less than the
	 * low threshold value.
	 * 
	 * @param threshold
	 *           a high hysteresis threshold
	 */
	public void setHighThreshold(float threshold) {
		if (threshold < 0)
			throw new IllegalArgumentException();
		highThreshold = threshold;
	}
	
	/**
	 * The number of pixels across which the Gaussian kernel is applied.
	 * This implementation will reduce the radius if the contribution of pixel
	 * values is deemed negligable, so this is actually a maximum radius.
	 * 
	 * @param gaussianKernelWidth
	 *           a radius for the convolution operation in
	 *           pixels, at least 2.
	 */
	public void setGaussianKernelWidth(int gaussianKernelWidth) {
		if (gaussianKernelWidth < 2)
			throw new IllegalArgumentException();
		this.gaussianKernelWidth = gaussianKernelWidth;
	}
	
	/**
	 * Sets the radius of the Gaussian convolution kernel used to smooth the
	 * source image prior to gradient calculation.
	 * 
	 * @param gaussianKernelRadius
	 *           a Gaussian kernel radius in pixels, must exceed 0.1f.
	 */
	public void setGaussianKernelRadius(float gaussianKernelRadius) {
		if (gaussianKernelRadius < 0.1f)
			throw new IllegalArgumentException();
		this.gaussianKernelRadius = gaussianKernelRadius;
	}
	
	/**
	 * Sets whether the contrast is normalized
	 * 
	 * @param contrastNormalized
	 *           true if the contrast should be normalized,
	 *           false otherwise
	 */
	public void setContrastNormalized(boolean contrastNormalized) {
		this.contrastNormalized = contrastNormalized;
	}
	
	public void process() {
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
		picsize = width * height;
		initArrays();
		readLuminance();
		if (contrastNormalized)
			normalizeContrast();
		computeGradients(gaussianKernelRadius, gaussianKernelWidth);
		int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
		int high = Math.round(highThreshold * MAGNITUDE_SCALE);
		performHysteresis(low, high);
		thresholdEdges();
	}
	
	// private utility methods
	
	private void initArrays() {
		if (data == null || picsize != data.length) {
			data = new int[picsize];
			magnitude = new int[picsize];
			
			xConv = new float[picsize];
			yConv = new float[picsize];
			xGradient = new float[picsize];
			yGradient = new float[picsize];
		}
	}
	
	// NOTE: The elements of the method below (specifically the technique for
	// non-maximal suppression and the technique for gradient computation)
	// are derived from an implementation posted in the following forum (with the
	// clear intent of others using the code):
	// http://forum.java.sun.com/thread.jspa?threadID=546211&start=45&tstart=0
	// My code effectively mimics the algorithm exhibited above.
	// Since I don't know the providence of the code that was posted it is a
	// possibility (though I think a very remote one) that this code violates
	// someone's intellectual property rights. If this concerns you feel free to
	// contact me for an alternative, though less efficient, implementation.
	
	private void computeGradients(float kernelRadius, int kernelWidth) {
		
		// generate the gaussian convolution masks
		float kernel[] = new float[kernelWidth];
		float diffKernel[] = new float[kernelWidth];
		int kwidth;
		for (kwidth = 0; kwidth < kernelWidth; kwidth++) {
			float g1 = gaussian(kwidth, kernelRadius);
			if (g1 <= GAUSSIAN_CUT_OFF && kwidth >= 2)
				break;
			float g2 = gaussian(kwidth - 0.5f, kernelRadius);
			float g3 = gaussian(kwidth + 0.5f, kernelRadius);
			kernel[kwidth] = (g1 + g2 + g3) / 3f / (2f * (float) Math.PI * kernelRadius * kernelRadius);
			diffKernel[kwidth] = g3 - g2;
		}
		
		int initX = kwidth - 1;
		int maxX = width - (kwidth - 1);
		int initY = width * (kwidth - 1);
		int maxY = width * (height - (kwidth - 1));
		
		// perform convolution in x and y directions
		for (int x = initX; x < maxX; x++) {
			for (int y = initY; y < maxY; y += width) {
				int index = x + y;
				float sumX = data[index] * kernel[0];
				float sumY = sumX;
				int xOffset = 1;
				int yOffset = width;
				for (; xOffset < kwidth;) {
					sumY += kernel[xOffset] * (data[index - yOffset] + data[index + yOffset]);
					sumX += kernel[xOffset] * (data[index - xOffset] + data[index + xOffset]);
					yOffset += width;
					xOffset++;
				}
				
				yConv[index] = sumY;
				xConv[index] = sumX;
			}
			
		}
		
		for (int x = initX; x < maxX; x++) {
			for (int y = initY; y < maxY; y += width) {
				float sum = 0f;
				int index = x + y;
				for (int i = 1; i < kwidth; i++)
					sum += diffKernel[i] * (yConv[index - i] - yConv[index + i]);
				
				xGradient[index] = sum;
			}
			
		}
		
		for (int x = kwidth; x < width - kwidth; x++) {
			for (int y = initY; y < maxY; y += width) {
				float sum = 0.0f;
				int index = x + y;
				int yOffset = width;
				for (int i = 1; i < kwidth; i++) {
					sum += diffKernel[i] * (xConv[index - yOffset] - xConv[index + yOffset]);
					yOffset += width;
				}
				
				yGradient[index] = sum;
			}
			
		}
		
		initX = kwidth;
		maxX = width - kwidth;
		initY = width * kwidth;
		maxY = width * (height - kwidth);
		for (int x = initX; x < maxX; x++) {
			for (int y = initY; y < maxY; y += width) {
				int index = x + y;
				int indexN = index - width;
				int indexS = index + width;
				int indexW = index - 1;
				int indexE = index + 1;
				int indexNW = indexN - 1;
				int indexNE = indexN + 1;
				int indexSW = indexS - 1;
				int indexSE = indexS + 1;
				
				float xGrad = xGradient[index];
				float yGrad = yGradient[index];
				float gradMag = hypot(xGrad, yGrad);
				
				// perform non-maximal supression
				float nMag = hypot(xGradient[indexN], yGradient[indexN]);
				float sMag = hypot(xGradient[indexS], yGradient[indexS]);
				float wMag = hypot(xGradient[indexW], yGradient[indexW]);
				float eMag = hypot(xGradient[indexE], yGradient[indexE]);
				float neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
				float seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
				float swMag = hypot(xGradient[indexSW], yGradient[indexSW]);
				float nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);
				float tmp;
				/*
				 * An explanation of what's happening here, for those who want
				 * to understand the source: This performs the "non-maximal
				 * supression" phase of the Canny edge detection in which we
				 * need to compare the gradient magnitude to that in the
				 * direction of the gradient; only if the value is a local
				 * maximum do we consider the point as an edge candidate.
				 * We need to break the comparison into a number of different
				 * cases depending on the gradient direction so that the
				 * appropriate values can be used. To avoid computing the
				 * gradient direction, we use two simple comparisons: first we
				 * check that the partial derivatives have the same sign (1)
				 * and then we check which is larger (2). As a consequence, we
				 * have reduced the problem to one of four identical cases that
				 * each test the central gradient magnitude against the values at
				 * two points with 'identical support'; what this means is that
				 * the geometry required to accurately interpolate the magnitude
				 * of gradient function at those points has an identical
				 * geometry (upto right-angled-rotation/reflection).
				 * When comparing the central gradient to the two interpolated
				 * values, we avoid performing any divisions by multiplying both
				 * sides of each inequality by the greater of the two partial
				 * derivatives. The common comparand is stored in a temporary
				 * variable (3) and reused in the mirror case (4).
				 */
				if (xGrad * yGrad <= 0 /* (1) */
						? Math.abs(xGrad) >= Math.abs(yGrad) /* (2) */
								? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * neMag - (xGrad + yGrad) * eMag) /* (3) */
										&& tmp > Math.abs(yGrad * swMag - (xGrad + yGrad) * wMag) /* (4) */
								: (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * neMag - (yGrad + xGrad) * nMag) /* (3) */
										&& tmp > Math.abs(xGrad * swMag - (yGrad + xGrad) * sMag) /* (4) */
						: Math.abs(xGrad) >= Math.abs(yGrad) /* (2) */
								? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * seMag + (xGrad - yGrad) * eMag) /* (3) */
										&& tmp > Math.abs(yGrad * nwMag + (xGrad - yGrad) * wMag) /* (4) */
								: (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * seMag + (yGrad - xGrad) * sMag) /* (3) */
										&& tmp > Math.abs(xGrad * nwMag + (yGrad - xGrad) * nMag) /* (4) */
				) {
					magnitude[index] = gradMag >= MAGNITUDE_LIMIT ? MAGNITUDE_MAX : (int) (MAGNITUDE_SCALE * gradMag);
					// NOTE: The orientation of the edge is not employed by this
					// implementation. It is a simple matter to compute it at
					// this point as: Math.atan2(yGrad, xGrad);
				} else {
					magnitude[index] = 0;
				}
			}
		}
	}
	
	// NOTE: It is quite feasible to replace the implementation of this method
	// with one which only loosely approximates the hypot function. I've tested
	// simple approximations such as Math.abs(x) + Math.abs(y) and they work fine.
	private float hypot(float x, float y) {
		return (float) Math.hypot(x, y);
	}
	
	private float gaussian(float x, float sigma) {
		return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
	}
	
	private void performHysteresis(int low, int high) {
		// NOTE: this implementation reuses the data array to store both
		// luminance data from the image, and edge intensity from the processing.
		// This is done for memory efficiency, other implementations may wish
		// to separate these functions.
		Arrays.fill(data, 0);
		
		int offset = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (data[offset] == 0 && magnitude[offset] >= high) {
					follow(x, y, offset, low);
				}
				offset++;
			}
		}
	}
	
	private void follow(int x1, int y1, int i1, int threshold) {
		int x0 = x1 == 0 ? x1 : x1 - 1;
		int x2 = x1 == width - 1 ? x1 : x1 + 1;
		int y0 = y1 == 0 ? y1 : y1 - 1;
		int y2 = y1 == height - 1 ? y1 : y1 + 1;
		
		data[i1] = magnitude[i1];
		for (int x = x0; x <= x2; x++) {
			for (int y = y0; y <= y2; y++) {
				int i2 = x + y * width;
				if ((y != y1 || x != x1)
						&& data[i2] == 0
						&& magnitude[i2] >= threshold) {
					follow(x, y, i2, threshold);
					return;
				}
			}
		}
	}
	
	private void thresholdEdges() {
		for (int i = 0; i < picsize; i++) {
			data[i] = data[i] > 0 ? 255 : 0;
			// data[i] = data[i] > 0 ? -1 : 0xff000000;
		}
	}
	
	private int luminance(float r, float g, float b) {
		// return Math.round(0.333f * r + 0.333f * g + 0.333f * b);
		return Math.round(0.299f * r + 0.587f * g + 0.114f * b);
	}
	
	private void readLuminance() {
		ImageProcessor ip = sourceImage.getProcessor();
		ip = ip.convertToByte(true);
		for (int i = 0; i < ip.getPixelCount(); i++)
			data[i] = ip.get(i);
	}
	
	private void normalizeContrast() {
		int[] histogram = new int[256];
		for (int i = 0; i < data.length; i++) {
			histogram[data[i]]++;
		}
		int[] remap = new int[256];
		int sum = 0;
		int j = 0;
		for (int i = 0; i < histogram.length; i++) {
			sum += histogram[i];
			int target = sum * 255 / picsize;
			for (int k = j + 1; k <= target; k++) {
				remap[k] = i;
			}
			j = target;
		}
		
		for (int i = 0; i < data.length; i++) {
			data[i] = remap[data[i]];
		}
	}
	
}
