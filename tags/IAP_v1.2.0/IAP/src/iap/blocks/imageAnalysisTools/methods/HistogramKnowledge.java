package iap.blocks.imageAnalysisTools.methods;

import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author klukas
 */
public class HistogramKnowledge {
	private final int columnCount;
	
	TreeMap<Integer, Integer>[] value2frequencyForInf;
	TreeMap<Integer, Integer>[] value2frequencyForNotInf;
	int[] bestThreshold;
	double[] classificationError;
	boolean[] lessMeansInf;
	
	@SuppressWarnings("unchecked")
	public HistogramKnowledge(int columnCount) {
		this.columnCount = columnCount;
		// initialization of generic array types requires casting and suppressed warnings
		value2frequencyForInf = (TreeMap<Integer, Integer>[]) new TreeMap<?, ?>[columnCount - 1]; // first column is not needed
		value2frequencyForNotInf = (TreeMap<Integer, Integer>[]) new TreeMap<?, ?>[columnCount - 1]; // first column is not needed
		bestThreshold = new int[columnCount - 1];
		lessMeansInf = new boolean[columnCount - 1];
		classificationError = new double[columnCount - 1];
		
		for (int col = 1; col < columnCount; col++) {
			value2frequencyForInf[col - 1] = new TreeMap<Integer, Integer>();
			value2frequencyForNotInf[col - 1] = new TreeMap<Integer, Integer>();
		}
	}
	
	/**
	 * Update frequency tables for the column values (separated for infected / not infected).
	 */
	public synchronized void memorize(double[] properties) {
		boolean infected = properties[0] > 0; // first column is interpreted, here
		for (int column = 1; column < columnCount - 1; column++) { // first column is not a property value
			TreeMap<Integer, Integer> value2frequ = infected ? value2frequencyForInf[column - 1] : value2frequencyForNotInf[column - 1];
			Integer key = new Double(properties[column] * 10).intValue(); // double value with one fractional digit is used as a key
			if (!value2frequ.containsKey(key))
				value2frequ.put(key, 0);
			value2frequ.put(key, value2frequ.get(key) + 1);
		}
	}
	
	boolean analyzed = false;
	
	public void analyzeHistograms() {
		if (analyzed)
			return;
		analyzed = true;
		for (int column = 1; column < columnCount - 1; column++) { // first column is not a property value
		
			TreeMap<Integer, Integer> frInf = value2frequencyForInf[column - 1];
			TreeMap<Integer, Integer> frNotInf = value2frequencyForNotInf[column - 1];
			
			TreeSet<Integer> allKnownValues = new TreeSet<Integer>();
			allKnownValues.addAll(frInf.keySet());
			allKnownValues.addAll(frNotInf.keySet());
			long minimumError = Long.MAX_VALUE;
			Integer minimumErrorValueBorder = -1;
			long numberOfPixels = 0;
			boolean lessThanBorderValueIndicatesInfected = false;
			for (Integer valueBorder : allKnownValues) {
				long beforeBorderInfected = 0;
				long afterBorderInfected = 0;
				long beforeBorderNotInfected = 0;
				long afterBorderNotInfected = 0;
				
				for (Integer key : frInf.keySet()) {
					int n = frInf.get(key);
					if (key < valueBorder)
						beforeBorderInfected += n;
					else
						if (key > valueBorder)
							afterBorderInfected += n;
				}
				for (Integer key : frNotInf.keySet()) {
					int n = frNotInf.get(key);
					if (key < valueBorder)
						beforeBorderNotInfected += n;
					else
						if (key > valueBorder)
							afterBorderNotInfected += n;
				}
				long multiplier = (beforeBorderNotInfected + afterBorderNotInfected) / (beforeBorderInfected + afterBorderInfected);
				beforeBorderInfected *= multiplier;
				afterBorderInfected *= multiplier;
				
				long errorCount;
				boolean lessMeansInf;
				if (beforeBorderInfected > beforeBorderNotInfected) {
					errorCount = beforeBorderNotInfected + afterBorderInfected;
					lessMeansInf = true;
				} else {
					errorCount = beforeBorderInfected + afterBorderNotInfected;
					lessMeansInf = false;
				}
				if (errorCount < minimumError) {
					minimumError = errorCount;
					minimumErrorValueBorder = valueBorder;
					lessThanBorderValueIndicatesInfected = lessMeansInf;
					numberOfPixels = beforeBorderInfected + afterBorderInfected + beforeBorderNotInfected + afterBorderNotInfected;
				}
			}
			bestThreshold[column - 1] = minimumErrorValueBorder;
			lessMeansInf[column - 1] = lessThanBorderValueIndicatesInfected;
			if (minimumError == Long.MAX_VALUE)
				classificationError[column - 1] = 100d;
			else
				classificationError[column - 1] = (int) (minimumError * 100d / numberOfPixels);
			System.out.println("Data of column " + column + " is best divided by threshold " + minimumErrorValueBorder / 10 +
					". In this case the classification error is " +
					(minimumError * 100d / numberOfPixels) + "% [less means infected?: " + lessThanBorderValueIndicatesInfected + "]");
		}
	}
	
	/**
	 * Calculate probability of infection, based on given property vector and known frequencies.
	 * 
	 * @return [prob infected, prob not infected]
	 */
	public double[] recall(double[] properties) {
		boolean useThreshold = true;
		if (useThreshold) {
			return useThresholds(properties);
		} else
			return analyzeHistogramProbabilities(properties);
	}
	
	private double[] useThresholds(double[] properties) {
		double[] res = new double[2];
		res[0] = 0; // infected
		res[1] = 0; // not infected
		for (int column = 1; column < columnCount - 1; column++) { // first column is not a property value
			int thresh = bestThreshold[column - 1];
			boolean lmi = lessMeansInf[column - 1];
			double val = properties[column] * 10;
			double classificationErr = classificationError[column - 1];
			double add = (100 - classificationErr) / 100;
			add = add * add;
			if (val < thresh) {
				if (lmi)
					res[0] += add;
				else
					res[1] += add;
			}
			if (val > thresh) {
				if (lmi)
					res[1] += add;
				else
					res[0] += add;
			}
		}
		return res;
	}
	
	private double[] analyzeHistogramProbabilities(double[] properties) {
		double[] res = new double[2];
		res[0] = 0; // infected
		res[1] = 0; // not infected
		for (int column = 1; column < columnCount - 1; column++) { // first column is not a property value
		
			TreeMap<Integer, Integer> frInf = value2frequencyForInf[column - 1];
			TreeMap<Integer, Integer> frNotInf = value2frequencyForNotInf[column - 1];
			
			int valueBorder = new Double(properties[column] * 10).intValue(); // double value with one fractional digit is used as a key
			long infectedCount = 0;
			int notInfectedCount = 0;
			
			long beforeBorderInfected = 0;
			long afterBorderInfected = 0;
			long beforeBorderNotInfected = 0;
			long afterBorderNotInfected = 0;
			
			boolean v2 = false;
			if (v2) {
				for (Integer key : frInf.keySet()) {
					if (key < valueBorder * 0.95d || key > valueBorder * 1.05d)
						continue;
					int n = frInf.get(key);
					infectedCount += n;
				}
				for (Integer key : frNotInf.keySet()) {
					if (key < valueBorder * 0.95d || key > valueBorder * 1.05d)
						continue;
					int n = frNotInf.get(key);
					notInfectedCount += n;
				}
				res[0] += infectedCount;
				res[1] += notInfectedCount;
			} else {
				for (Integer key : frInf.keySet()) {
					int n = frInf.get(key);
					if (key < valueBorder)
						beforeBorderInfected += n;
					else
						if (key > valueBorder)
							afterBorderInfected += n;
				}
				for (Integer key : frNotInf.keySet()) {
					int n = frNotInf.get(key);
					if (key < valueBorder)
						beforeBorderNotInfected += n;
					else
						if (key > valueBorder)
							afterBorderNotInfected += n;
				}
				res[0] += beforeBorderInfected / (double) (beforeBorderInfected + afterBorderInfected);
				res[1] += beforeBorderNotInfected / (double) (beforeBorderNotInfected + afterBorderNotInfected);
			}
		}
		res[0] = res[0] / (columnCount - 1);
		res[1] = res[1] / (columnCount - 1);
		return res;
	}
}
