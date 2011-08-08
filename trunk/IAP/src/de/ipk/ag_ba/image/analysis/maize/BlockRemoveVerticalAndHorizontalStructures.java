package de.ipk.ag_ba.image.analysis.maize;

import java.util.List;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

// this might not be needed
public class BlockRemoveVerticalAndHorizontalStructures extends AbstractBlock {
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask == null)
			return null;
		if (mask.getType() == FlexibleImageType.UNKNOWN) {
			System.out.println("ERROR: Unknown image type!!!");
			return mask;
		}
		if (mask.getType() == FlexibleImageType.NIR)
			return mask;
		if (mask.getType() == FlexibleImageType.FLUO)
			return process(process(mask));
		if (mask.getType() == FlexibleImageType.VIS)
			return process(process(mask));
		
		return mask;
	}
	
	private FlexibleImage process(FlexibleImage mask) {
		int[] img = mask.getAs1A();
		int w = mask.getWidth();
		int h = mask.getHeight();
		int[] filledPixelsPerLine = new int[h];
		int[] filledPixelsPerColumn = new int[w];
		int back = options.getBackground();
		for (int y = 0; y < h; y++) {
			int filled = 0;
			int yw = y * w;
			for (int x = 0; x < w; x++) {
				if (img[x + yw] != back) {
					filled++;
					filledPixelsPerColumn[x] = filledPixelsPerColumn[x] + 1;
				}
			}
			filledPixelsPerLine[y] = filled;
		}
		int n = 20;
		for (int scanBlock = 0; scanBlock < h * 0.1 / n; scanBlock++) {
			double avg = getAvg(filledPixelsPerLine, scanBlock * n, n);
			double stddev = getStdDev(avg, filledPixelsPerLine, scanBlock * n, n);
			double scut = stddev * 1.5;
			for (int i = 0; i < n; i++) {
				int y = scanBlock * n + i;
				int yw = y * w;
				int yw_ = (y - 1) * w;
				if (filledPixelsPerLine[y] - avg > scut) {
					for (int x = 0; x < w; x++) {
						if (y > 1) {
							img[x + yw] = img[x + yw_];
						}
					}
				}
			}
		}
		
		for (int scanBlock = 0; scanBlock < w / n; scanBlock++) {
			if (scanBlock * n > 0.3d * w && scanBlock * n < 0.7d * w)
				continue;
			double avg = getAvg(filledPixelsPerColumn, scanBlock * n, n);
			double stddev = getStdDev(avg, filledPixelsPerColumn, scanBlock * n, n);
			double scut = stddev * 1.5;
			for (int i = 0; i < n; i++) {
				int x = scanBlock * n + i;
				if (filledPixelsPerColumn[x] - avg > scut) {
					for (int y = 0; y < h; y++) {
						if (x > 1) {
							int yw = y * w;
							img[x + yw] = img[x - 1 + yw];
						}
					}
				}
			}
		}
		
		return new FlexibleImage(w, h, img).print("TEST " + System.currentTimeMillis(), false);
	}
	
	private double getAvg(int[] filledPixelsPerLine, int startIndex, int n) {
		double sum = 0;
		for (int idx = startIndex; idx < startIndex + n; idx++)
			sum += filledPixelsPerLine[idx];
		return sum / n;
	}
	
	private double getStdDev(double avg, int[] filledPixelsPerLine, int startIndex, int n) {
		double sumDiff = 0;
		for (int idx = startIndex; idx < startIndex + n; idx++)
			sumDiff += (filledPixelsPerLine[idx] - avg) * (filledPixelsPerLine[idx] - avg);
		double stdDev = Math.sqrt(sumDiff / (n - 1));
		return stdDev;
	}
	
	private void ttt() {
		boolean outlierIdentified;
		do {
			outlierIdentified = false;
			List<Double> values = null; // get list of number of pixels in each line of the image
			// G = (max {|Yi - Yavg|} ) / s
			// Yavg = Average value
			// s = StdDev
			// calculate s (StdDev)
			double sum = 0d;
			double min = Double.MAX_VALUE;
			double max = Double.NEGATIVE_INFINITY;
			Double min_value = null;
			Double max_value = null;
			int n = 0;
			for (Double value : values) {
				sum += value;
				if (value > max) {
					max = value;
					max_value = value;
				}
				if (value < min) {
					min = value;
					min_value = value;
				}
				n++; // System.out.println(value.toString());
			}
			double avg = sum / n;
			double sumDiff = 0;
			for (Double value : values) {
				sumDiff += (value - avg) * (value - avg);
			}
			double stdDev = Math.sqrt(sumDiff / (n - 1));
			double m1 = Math.abs(max - avg);
			double m2 = Math.abs(min - avg);
			double maxYi_Yavg = (m1 > m2 ? m1 : m2);
			boolean isMaxPotentialOutlier = m1 > m2;
			double G = maxYi_Yavg / stdDev;
			// critical region (from Engineering Statistics Handbook)
			// G > (N-1) / N^.5 * ( (t(a/(2N), N-2))^2
			// /
			// (N-2+(t(a/(2N),N-2))^2)
			// )^0.5
			if (n - 2 > 0) {
				// try {
				// td.setDegreesOfFreedom(n - 2);
				// double t1 = td.inverseCumulativeProbability(1 - (1 - alpha) / (2 * n));
				// double testG = (n - 1) / Math.sqrt(n) * Math.sqrt(t1 * t1 / (n - 2 + t1));
				// if (G > testG) {
				// if (isMaxPotentialOutlier)
				// max_value.setIsOutlier(true, removeOutliers);
				// else
				// min_value.setIsOutlier(true, removeOutliers); // not relevant for our problem (!!!! 2011)
				// removedPoints++;
				// outlierIdentified = true;
				// }
				// } catch (MathException e) {
				// ErrorMsg.addErrorMessage(e);
				// }
			}
		} while (outlierIdentified);
		
	}
}
