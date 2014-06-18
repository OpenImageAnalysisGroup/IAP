package de.ipk.ag_ba.plugins.outlier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.ErrorMsg;
import org.apache.commons.math3.distribution.TDistribution;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 */
public class OutlierAnalysisGlobal {
	
	private final ExperimentReference experimentReference;
	private double threshold = 0.01;
	private final boolean global;
	private final HashSet<NumericMeasurement> lowerValues;
	private final HashSet<NumericMeasurement> upperValues;
	private final HashSet<SampleInterface> lowerSamples;
	private final HashSet<SampleInterface> upperSamples;
	private final boolean median = false;
	private final boolean processAveragePlantData;
	
	public OutlierAnalysisGlobal(double threshold, boolean considerCondition,
			ExperimentReference experimentReference,
			HashSet<NumericMeasurement> lower, HashSet<NumericMeasurement> upper,
			HashSet<SampleInterface> lowerSamples, HashSet<SampleInterface> upperSamples,
			boolean processAverageData) {
		this.threshold = threshold;
		this.experimentReference = experimentReference;
		this.lowerValues = lower;
		this.upperValues = upper;
		this.lowerSamples = lowerSamples;
		this.upperSamples = upperSamples;
		processAveragePlantData = processAverageData;
		this.global = !considerCondition;
	}
	
	public void analyse() throws Exception {
		
		for (String t : Experiment.getTimes(experimentReference.getData())) {
			processData(t);
		}
	}
	
	private void processData(String selTimepoint) throws Exception {
		ArrayList<NumericMeasurement> numericValues = new ArrayList<NumericMeasurement>();
		ArrayList<String> plantIDs = new ArrayList<String>();
		if (selTimepoint != null) {
			ExperimentInterface exp = experimentReference.getData(null);
			for (SubstanceInterface si : exp) {
				String trait = si.getName();
				
				HashMap<String, ArrayList<NumericMeasurement>> plantId2values = new HashMap<String, ArrayList<NumericMeasurement>>();
				for (ConditionInterface ci : si) {
					if (!global)
						plantId2values.clear();
					for (SampleInterface sai : ci) {
						if (sai.getSampleTime().equals(selTimepoint)) {
							for (NumericMeasurementInterface nmi : sai) {
								if (nmi instanceof NumericMeasurement) {
									double val = nmi.getValue();
									if (!Double.isNaN(val) && !Double.isInfinite(val) && nmi.getQualityAnnotation() != null && !nmi.getQualityAnnotation().isEmpty()) {
										if (!plantId2values.containsKey(nmi.getQualityAnnotation()))
											plantId2values.put(nmi.getQualityAnnotation(), new ArrayList<NumericMeasurement>());
										plantId2values.get(nmi.getQualityAnnotation()).add((NumericMeasurement) nmi);
									}
								}
							}
						}
					}
					if (!global)
						processGT(numericValues, plantIDs, trait, plantId2values);
				}
				if (global)
					processGT(numericValues, plantIDs, trait, plantId2values);
			}
			// processResults(plantid2property2topOrLowPercentile);
		}
	}
	
	private void processGT(ArrayList<NumericMeasurement> numericValues, ArrayList<String> plantIDs, String trait,
			HashMap<String, ArrayList<NumericMeasurement>> plantId2values) {
		numericValues.clear();
		plantIDs.clear();
		for (String plantID : plantId2values.keySet()) {
			if (processAveragePlantData) {
				numericValues.add(NumericMeasurement.getSimple(median ? median(plantId2values.get(plantID)) : average(plantId2values.get(plantID))));
				plantIDs.add(plantID);
			} else {
				for (NumericMeasurement nm : plantId2values.get(plantID)) {
					numericValues.add(nm);
					plantIDs.add(plantID);
				}
			}
		}
		
		grubbsTest(plantIDs, trait, numericValues, threshold, plantId2values);
	}
	
	private Double median(ArrayList<NumericMeasurement> nml) {
		ArrayList<Double> m = new ArrayList<Double>();
		for (NumericMeasurement nm : nml)
			m.add(nm.getValue());
		java.util.Collections.sort(m);
		int middle = m.size() / 2;
		if (m.size() % 2 == 1) {
			return m.get(middle);
		} else {
			return (m.get(middle - 1) + m.get(middle)) / 2.0;
		}
	}
	
	private Double average(ArrayList<NumericMeasurement> nml) {
		double sum = 0;
		int n = 0;
		for (NumericMeasurement nm : nml) {
			n++;
			sum += nm.getValue();
		}
		return sum / n;
	}
	
	/**
	 * @param plantId2values
	 * @return Number of removed outliers.
	 */
	private int grubbsTest(ArrayList<String> plantIDs, String trait, ArrayList<NumericMeasurement> numericValues,
			double alpha, HashMap<String, ArrayList<NumericMeasurement>> plantId2values) {
		HashSet<NumericMeasurement> allInValues = new HashSet<NumericMeasurement>();
		if (!processAveragePlantData) {
			for (NumericMeasurement nm : numericValues)
				allInValues.add(nm);
		}
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
			for (NumericMeasurement nm : numericValues) {
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
			for (NumericMeasurement nm : numericValues) {
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
						String plantID;
						if (isMaxPotentialOutlier) {
							plantID = plantIDs.get(indexOfMaxValue);
							plantIDs.remove(indexOfMaxValue);
							NumericMeasurement nm = numericValues.remove(indexOfMaxValue);
							if (!processAveragePlantData)
								upperValues.add(nm);
						} else {
							plantID = plantIDs.get(indexOfMinValue);
							plantIDs.remove(indexOfMinValue);
							NumericMeasurement nm = numericValues.remove(indexOfMinValue);
							if (!processAveragePlantData)
								lowerValues.add(nm);
						}
						if (processAveragePlantData) {
							for (NumericMeasurement nmi : plantId2values.get(plantID)) {
								if (isMaxPotentialOutlier)
									upperSamples.add(nmi.getParentSample());
								else
									lowerSamples.add(nmi.getParentSample());
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
