package iap.blocks.extraction;

import java.util.LinkedList;

import org.ErrorMsg;
import org.GapList;
import org.apache.commons.math3.distribution.TDistribution;

/**
 * @author klukas
 */
public class Outlier {
	
	public static int doGrubbsTest(GapList<Numeric> numericValues, double alpha, LinkedList<Numeric> lowerValues, LinkedList<Numeric> upperValues) {
		boolean outlierIdentified;
		int removedPoints = 0;
		do {
			outlierIdentified = false;
			// G = (max {|Yi - Yavg|} ) / s
			// Yavg = Average value
			// s = StdDev
			// calculate s (StdDev)
			double sum = 0d;
			double min = Double.MAX_VALUE;
			double max = Double.NEGATIVE_INFINITY;
			int indexOfMinValue = -1;
			int indexOfMaxValue = -1;
			int n = 0;
			for (Numeric nm : numericValues) {
				Double value = nm.getValue();
				sum += value;
				if (value < min) {
					min = value;
					indexOfMinValue = n;
				}
				if (value > max) {
					max = value;
					indexOfMaxValue = n;
				}
				n++;
			}
			double avg = sum / n;
			double sumDiff = 0;
			for (Numeric nm : numericValues) {
				Double value = nm.getValue();
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
				try {
					TDistribution td = new TDistribution(n - 2);
					double t1 = td.inverseCumulativeProbability(1 - (1 - alpha) / (2 * n));
					double testG = (n - 1) / Math.sqrt(n) * Math.sqrt(t1 * t1 / (n - 2 + t1));
					if (G > testG) {
						if (isMaxPotentialOutlier) {
							Numeric nm = numericValues.remove(indexOfMaxValue);
							if (upperValues != null) {
								synchronized (upperValues) {
									upperValues.add(nm);
								}
							}
						} else {
							Numeric nm = numericValues.remove(indexOfMinValue);
							if (lowerValues != null) {
								synchronized (lowerValues) {
									lowerValues.add(nm);
								}
							}
						}
						
						removedPoints++;
						outlierIdentified = true;
					}
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		} while (outlierIdentified);
		return removedPoints;
	}
	
}
