package de.ipk.ag_ba.image.operation;

import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.ASM;
import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.CONTRAST;
import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.CORRELATION;
import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.ENTROPY;
import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.MEAN;
import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.STD;
import static de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures.VARIANCE;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_ASM_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_ASM_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_CONTRAST_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_CONTRAST_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_CORRELATION_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_CORRELATION_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_DERIVATION_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_DERIVATION_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_DISSIMILARITY_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_DISSIMILARITY_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_ENTROPY_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_ENTROPY_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_HOMOGENEITY_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_HOMOGENEITY_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_MEAN_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_MEAN_V;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_VARIANCE_H;
import static de.ipk.ag_ba.image.operation.GLCMTextureFeatures.GLCM_VARIANCE_V;

import java.util.HashMap;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import Jama.Matrix;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 */
public class ImageTexture {
	
	private final int[] img;
	private final int w;
	private final int h;
	
	public HashMap<FirstOrderTextureFeatures, Double> firstOrderFeatures = new HashMap<>();
	public HashMap<GLCMTextureFeatures, Double> glcmFeatures = new HashMap<>();
	
	boolean ignoreBackground = true;
	
	private double[] sqrtList;
	
	/**
	 * Uses only blue channel of input image, this should be an gray-scale image.
	 */
	public ImageTexture(Image inp) {
		int[] pix = inp.getAs1A();
		img = new int[pix.length];
		int i = 0;
		for (int p : pix) {
			img[i++] = p & 0x0000ff;
		}
		w = inp.getWidth();
		h = inp.getHeight();
	}
	
	private void calcSqrtLookUp(int i) {
		sqrtList = new double[i];
		
		BackgroundThreadDispatcher.stream("Sqrt Lookup").processInts(IntStream.range(0, i), (idx) -> {
			sqrtList[idx] = Math.sqrt(idx);
		}, null);
	}
	
	/**
	 * Use array with saved gray-scale values as input.
	 */
	public ImageTexture(int[] inp, int width, int height, boolean ignoreBack) {
		img = inp;
		w = width;
		h = height;
		ignoreBackground = ignoreBack;
	}
	
	public void calcTextureFeatures() {
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		
		for (int idx = 0; idx < w * h; idx++) {
			if (img[idx] != ImageOperation.BACKGROUND_COLORint)
				ds.addValue(img[idx]);
		}
		
		firstOrderFeatures.put(STD, ds.getStandardDeviation());
		firstOrderFeatures.put(MEAN, ds.getMean());
		firstOrderFeatures.put(VARIANCE, ds.getVariance());
		firstOrderFeatures.put(ENTROPY, 255 - entropy());
		firstOrderFeatures.put(CONTRAST, ((ds.getMax() - ds.getMin()) / (ds.getMax() + ds.getMin())) * 255.0); // Michelson contrast
		firstOrderFeatures.put(ASM, secondAngularMoment());
		firstOrderFeatures.put(CORRELATION, correlation(ds.getStandardDeviation(), ds.getMean()));
		
		ds.clear();
	}
	
	/**
	 * 1. Contrast Group:
	 * Contrast (CON)
	 * Dissimilarity (DIS)
	 * Homogeneity (HOM)
	 * 2. Orderliness Group:
	 * ASM
	 * Entropy (ENT)
	 * GLCM Mean
	 * GLCM Variance
	 * Standard Deviation
	 * GLCM Correlation
	 */
	public void calcGLCMTextureFeatures() {
		double[][] glcmV = getGreyLevelCooccurrenceMatrix(false);
		double[][] glcmH = getGreyLevelCooccurrenceMatrix(true);
		
		glcmFeatures.put(GLCM_CONTRAST_V, getGlcmContrast(glcmV));
		glcmFeatures.put(GLCM_CONTRAST_H, getGlcmContrast(glcmH));
		
		glcmFeatures.put(GLCM_DISSIMILARITY_V, getGlcmDissimilarity(glcmV));
		glcmFeatures.put(GLCM_DISSIMILARITY_H, getGlcmDissimilarity(glcmH));
		
		glcmFeatures.put(GLCM_HOMOGENEITY_V, getGlcmHomogeneity(glcmV));
		glcmFeatures.put(GLCM_HOMOGENEITY_H, getGlcmHomogeneity(glcmH));
		
		glcmFeatures.put(GLCM_ASM_V, getGlcmASM(glcmV));
		glcmFeatures.put(GLCM_ASM_H, getGlcmASM(glcmH));
		
		glcmFeatures.put(GLCM_ENTROPY_V, getGlcmEntropy(glcmV));
		glcmFeatures.put(GLCM_ENTROPY_H, getGlcmEntropy(glcmH));
		
		double glcm_mean_v = getGlcmMean(glcmV);
		double glcm_mean_h = getGlcmMean(glcmH);
		glcmFeatures.put(GLCM_MEAN_V, glcm_mean_v);
		glcmFeatures.put(GLCM_MEAN_H, glcm_mean_h);
		
		double glcm_variance_v = getGlcmVariance(glcmV, glcm_mean_v);
		double glcm_variance_h = getGlcmVariance(glcmH, glcm_mean_h);
		glcmFeatures.put(GLCM_VARIANCE_V, glcm_variance_v);
		glcmFeatures.put(GLCM_VARIANCE_H, glcm_variance_h);
		
		double glcm_derivation_v = Math.sqrt(glcm_variance_v);
		double glcm_derivation_h = Math.sqrt(glcm_variance_h);
		glcmFeatures.put(GLCM_DERIVATION_V, glcm_derivation_v);
		glcmFeatures.put(GLCM_DERIVATION_H, glcm_derivation_h);
		
		glcmFeatures.put(GLCM_CORRELATION_V, getGlcmCorrelation(glcmV, glcm_mean_v, glcm_derivation_v, glcm_mean_h, glcm_derivation_h));
		glcmFeatures.put(GLCM_CORRELATION_H, getGlcmCorrelation(glcmH, glcm_mean_v, glcm_derivation_v, glcm_mean_h, glcm_derivation_h));
	}
	
	private double getGlcmCorrelation(double[][] m, double glcm_mean_v, double glcm_derivation_v, double glcm_mean_h, double glcm_derivation_h) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += m[x][y] * (((x - glcm_mean_v) * (y - glcm_mean_h)) / Math.sqrt(glcm_derivation_v * glcm_derivation_h));
			}
		}
		
		return sum;
	}
	
	/**
	 * Variance calculated using i or j gives the same result, since the GLCM is symmetrical.
	 */
	private double getGlcmVariance(double[][] m, double glcm_mean) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += m[x][y] * ((x - glcm_mean) * (x - glcm_mean));
			}
		}
		
		return sum;
	}
	
	/**
	 * Cause of sym glcm one side calculation is ok.
	 */
	private double getGlcmMean(double[][] m) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += x * m[x][y];
			}
		}
		
		return sum;
	}
	
	private double getGlcmEntropy(double[][] m) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				if (m[x][y] == 0)
					continue;
				sum += m[x][y] * -Math.log(m[x][y]);
			}
		}
		
		return sum;
	}
	
	/**
	 * Here energy = sqrt(ASM)
	 */
	private double getGlcmASM(double[][] m) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += m[x][y] * m[x][y];
			}
		}
		
		return Math.sqrt(sum);
	}
	
	private double getGlcmHomogeneity(double[][] m) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += m[x][y] / (1 + ((x - y) * (x - y)));
			}
		}
		
		return sum;
	}
	
	private double getGlcmDissimilarity(double[][] m) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += Math.abs(x - y) * m[x][y];
			}
		}
		
		return sum;
	}
	
	private double getGlcmContrast(double[][] m) {
		double sum = 0.0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += ((x - y) * (x - y)) * m[x][y];
			}
		}
		
		return sum;
	}
	
	/**
	 * http://www.fp.ucalgary.ca/mhallbey/GLCM_as_probability.htm
	 * 1. Create a framework matrix
	 * 2. Decide on the spatial relation between the reference and neighbour pixel
	 * 3. Count the occurrences and fill in the framework matrix
	 * 4. Add the matrix to its transpose to make it symmetrical
	 * 5. Normalize the matrix to turn it into probabilities.
	 */
	private double[][] getGreyLevelCooccurrenceMatrix(boolean horizontal) {
		double[][] symMatrix = getSymetricalFrameworkMatrix(horizontal);
		
		// normalize
		double[][] normSym = normalize(symMatrix);
		
		return normSym;
	}
	
	private double[][] normalize(double[][] m) {
		int sum = 0;
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				sum += m[x][y];
			}
		}
		
		double[][] res = new double[m.length][m[0].length];
		for (int x = 0; x < m.length; x++) {
			for (int y = 0; y < m[0].length; y++) {
				res[x][y] = m[x][y] / sum;
			}
		}
		
		return res;
	}
	
	private double[][] getSymetricalFrameworkMatrix(boolean horizontal) {
		double[][] frameworkMatrix = new double[256][256];
		
		for (int idx = 0; idx < w * h; idx++) {
			// skip border
			if ((idx + 1 % w) == 0)
				continue;
			int x = -1, y = -1;
			if (horizontal) {
				if (idx + 1 < w * h) {
					x = img[idx];
					y = img[idx + 1];
				}
			} else {
				if (idx + w < w * h) {
					x = img[idx];
					y = img[idx + w];
				}
			}
			
			if (x == ImageOperation.BACKGROUND_COLORint || y == ImageOperation.BACKGROUND_COLORint)
				continue;
			
			if (x != -1 && y != -1)
				frameworkMatrix[x][y]++;
		}
		// make symmetrical sym = fm + transpose(fm)
		Matrix fm = new Matrix(frameworkMatrix);
		return fm.plus(fm.transpose()).getArray();
	}
	
	private double correlation(double std, double mean) {
		double res = 0.0;
		for (int i = 0; i < img.length; i++) {
			res = (img[i] - std) / mean;
		}
		return res;
	}
	
	private double secondAngularMoment() {
		double sum = 0.0;
		for (int i = 0; i < img.length; i++) {
			if (img[i] == ImageOperation.BACKGROUND_COLORint)
				continue;
			sum += img[i] * img[i];
		}
		return sum;
	}
	
	/**
	 * -sum(p.*log2(p)) http://www.mathworks.de/help/images/ref/entropy.html
	 */
	private double entropy() {
		double sum = 0.0;
		int[] hist = new int[256];
		
		for (int i : img) {
			if (i == ImageOperation.BACKGROUND_COLORint || i < 0)
				continue;
			hist[i]++;
		}
		
		for (int i : hist) {
			double log2 = (Math.log(i) / Math.log(2));
			if (Double.isInfinite(log2))
				continue;
			sum += i * log2;
		}
		
		return -sum;
	}
}
