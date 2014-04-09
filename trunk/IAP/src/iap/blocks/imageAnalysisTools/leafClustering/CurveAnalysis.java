package iap.blocks.imageAnalysisTools.leafClustering;

import ij.util.Tools;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.optimization.fitting.PolynomialFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 */
public class CurveAnalysis {
	static double[] data;
	
	public CurveAnalysis(double[] data) {
		this.data = data;
	}
	
	public static PolynomialFunction fitPolynom(int degree) {
		PolynomialFitter pf = new PolynomialFitter(degree, new LevenbergMarquardtOptimizer());
		for (int idx = 0; idx < data.length; idx++) {
			pf.addObservedPoint(idx, data[idx]);
		}
		double[] best = pf.fit();
		final PolynomialFunction fitted = new PolynomialFunction(best);
		return fitted;
	}
	
	// TODO fix
	public static void plotFunction(PolynomialFunction poly, int len) {
		// if (poly.getClass() == PolynomialFunction.class)
		double[] knots = new double[len];
		for (int idx = 0; idx < len; idx++)
			knots[idx] = poly.value(idx);
		
		Image plot = new Image(len, 100, Color.WHITE.getRGB());
		ImageCanvas canvas = plot.io().canvas();
		for (int x = 0; x < len; x++) {
			int y = (int) knots[x];
			if (y > 0)
				canvas.fillCircle(x, y, 3, Color.BLACK.getRGB(), 0.5);
		}
		try {
			canvas.getImage().show("poly");
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	@Deprecated
	public LinkedList<Integer> getLocalMinMax(int scansize) {
		
		double[] firstDerivative = derivate(data, scansize);
		double[] secondDerivative = derivate(firstDerivative, scansize);
		
		LinkedList<Integer> maxMin = new LinkedList<Integer>();
		
		int idx = 0;
		for (double d : secondDerivative) {
			if (Math.abs(d) < 0.1)
				maxMin.add(idx);
			idx++;
		}
		
		return maxMin;
	}
	
	// derivation using central differences
	private static double[] derivate(double[] data, int smoothFactor) {
		int dataLength = data.length;
		double[] derivations = new double[dataLength];
		double frac = Math.pow(10.0, 2);
		for (int idx = 0; idx < dataLength; idx++) {
			int back = (((idx - smoothFactor * 2) % dataLength) + dataLength) % dataLength;
			int ahead = (idx + smoothFactor * 2) % dataLength;
			double derivative = (data[ahead] - data[back]) / smoothFactor;
			derivations[idx] = Math.round(frac * derivative) / frac;
		}
		return derivations;
	}
	
	// TODO : fix for finding max and min
	public static int[] findMaxima(double[] data, double minSizeOfPeak, int distBetweenPeaks) {
		double start = 0;
		double temp = 0;
		int tempMaxPos = 0;
		
		int listsize = data.length;
		int distToBefore = Integer.MAX_VALUE;
		int listIndexBefore = -1;
		
		int[] positions;
		LinkedList<Integer> peakList = new LinkedList<Integer>();
		
		for (double d : data) {
			System.out.println(d);
		}
		
		for (int idxFeature = 0; idxFeature < listsize; idxFeature++) {
			temp = data[idxFeature];
			
			// found peak
			if (start == 0 && temp > 0) {
				start = temp;
				int idxIn = 1;
				double tempMax = data[idxFeature];
				while (temp >= minSizeOfPeak && idxIn < listsize) {
					if ((idxFeature + idxIn) % listsize < 0)
						System.out.println("error");
					temp = data[(idxFeature + idxIn) % listsize];
					if (temp > tempMax) {
						tempMax = data[(idxFeature + idxIn) % listsize];
						tempMaxPos = (idxFeature + idxIn) % listsize;
					}
					idxIn++;
				}
				
				// min size of a peak
				if (idxIn > 1) {
					if (listIndexBefore != -1)
						distToBefore = (idxFeature + idxIn) - listIndexBefore;
					if (distToBefore > distBetweenPeaks) {
						peakList.add(tempMaxPos);
						listIndexBefore = idxFeature + idxIn;
					}
				}
				// If over i > listsize, delete first one because it is maybe no maxima otherwise it is re-added.
				if (idxFeature + idxIn >= listsize) {
					if (!peakList.isEmpty())
						peakList.remove(0);
					break;
				}
				start = 0;
				idxFeature = idxFeature + idxIn;
			}
		}
		
		positions = new int[peakList.size()];
		int idx = 0;
		for (int i : peakList) {
			positions[idx] = i;
			idx++;
		}
		return positions;
	}
	
	/**
	 * From ImageJ:
	 * Calculates peak positions of 1D array N.Vischer, 13-sep-2013
	 * 
	 * @param data
	 *           Array containing peaks.
	 * @param tolerance
	 *           Depth of a qualified valley must exceed tolerance.
	 *           Tolerance must be >= 0. Flat tops are marked at their centers.
	 * @param excludeOnEdges
	 *           If 'true', a peak is only
	 *           accepted if it is separated by two qualified valleys. If 'false', a peak
	 *           is also accepted if separated by one qualified valley and by a border.
	 * @return Positions of peaks, sorted with decreasing amplitude
	 */
	public static int[] findMaximaIJ(double[] data, double tolerance, boolean excludeOnEdges) {
		boolean includeEdge = !excludeOnEdges;
		int len = data.length;
		if (len == 0)
			return new int[0];
		if (tolerance < 0)
			tolerance = 0;
		int[] maxPositions = new int[len];
		double max = data[0];
		double min = data[0];
		int maxPos = 0;
		int lastMaxPos = -1;
		boolean leftValleyFound = includeEdge;
		int maxCount = 0;
		for (int jj = 1; jj < len; jj++) {
			double val = data[jj];
			if (val > min + tolerance)
				leftValleyFound = true;
			if (val > max && leftValleyFound) {
				max = val;
				maxPos = jj;
			}
			if (leftValleyFound)
				lastMaxPos = maxPos;
			if (val < max - tolerance && leftValleyFound) {
				maxPositions[maxCount] = maxPos;
				maxCount++;
				leftValleyFound = false;
				min = val;
				max = val;
			}
			if (val < min) {
				min = val;
				if (!leftValleyFound)
					max = val;
			}
		}
		if (includeEdge) {
			if (maxCount > 0 && maxPositions[maxCount - 1] != lastMaxPos)
				maxPositions[maxCount++] = lastMaxPos;
			if (maxCount == 0 && max - min >= tolerance)
				maxPositions[maxCount++] = lastMaxPos;
		}
		int[] cropped = new int[maxCount];
		System.arraycopy(maxPositions, 0, cropped, 0, maxCount);
		maxPositions = cropped;
		double[] maxValues = new double[maxCount];
		for (int jj = 0; jj < maxCount; jj++) {
			int pos = maxPositions[jj];
			double midPos = pos;
			while (pos < len - 1 && data[pos] == data[pos + 1]) {
				midPos += 0.5;
				pos++;
			}
			maxPositions[jj] = (int) midPos;
			maxValues[jj] = data[maxPositions[jj]];
		}
		int[] rankPositions = Tools.rank(maxValues);
		int[] returnArr = new int[maxCount];
		for (int jj = 0; jj < maxCount; jj++) {
			int pos = maxPositions[rankPositions[jj]];
			returnArr[maxCount - jj - 1] = pos;// use descending order
		}
		return returnArr;
	}
	
	/**
	 * Returns minimum positions of array xx, sorted with decreasing strength
	 */
	public static int[] findMinimaIJ(double[] xx, double tolerance, boolean includeEdges) {
		int len = xx.length;
		double[] negArr = new double[len];
		for (int jj = 0; jj < len; jj++)
			negArr[jj] = -xx[jj];
		int[] minPositions = findMaximaIJ(negArr, tolerance, includeEdges);
		return minPositions;
	}
	
	/**
	 * Method to summarize max if positions are < maxDiff.
	 */
	public static int[] summarizeMaxima(int[] peaks, int length, int maxDiff) {
		LinkedList<Integer> newPeaks = new LinkedList<Integer>();
		int peaksLength = peaks.length;
		Arrays.sort(peaks);
		for (int idx = 0; idx < peaksLength; idx++) {
			int temp = peaks[idx];
			int count = 1, idx2 = (idx + 1) % peaksLength, diff = Math.abs(temp - peaks[idx2]), sum = temp;
			while (diff < maxDiff) {
				temp = peaks[idx2];
				sum = sum + temp;
				idx2 = (idx2 + 1) % peaksLength;
				diff = Math.abs(temp - peaks[idx2]);
				count++;
				if (count > (length * 2))
					return peaks;
			}
			newPeaks.add(sum / count);
			idx = idx + count - 1;
			
			// check if last and first are the same peak
			if ((temp + maxDiff) >= length) {
				if (newPeaks.getFirst() - newPeaks.getLast() < maxDiff) {
					int val;
					if ((newPeaks.getFirst() + length - newPeaks.getLast()) > 0)
						val = (int) ((newPeaks.getFirst() + length - newPeaks.getLast()) / 2d);
					else
						val = (int) ((newPeaks.getFirst() + length + newPeaks.getLast()) / 2d);
					newPeaks.pollFirst();
					newPeaks.pollLast();
					if (val > length) {
						val = val - length;
						newPeaks.addLast(val);
					} else
						newPeaks.addFirst(val);
				}
			}
		}
		
		int[] ret = new int[newPeaks.size()];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = newPeaks.get(i).intValue();
		}
		return ret;
	}
}
