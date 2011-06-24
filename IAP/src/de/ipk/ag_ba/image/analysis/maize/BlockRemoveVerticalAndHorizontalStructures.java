package de.ipk.ag_ba.image.analysis.maize;

import java.util.List;

// this might not be needed
public class BlockRemoveVerticalAndHorizontalStructures {
	
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
