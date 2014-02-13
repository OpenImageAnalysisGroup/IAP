package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.MeasurementFilter;
import org.StringManipulationTools;
import org.SystemAnalysis;

public class ExperimentCalculationService {
	
	private final ExperimentInterface experiment;
	
	public ExperimentCalculationService(ExperimentInterface experiment) {
		this.experiment = experiment;
	}
	
	/**
	 * @param treatmentReference
	 *           If this is an empty array, the reference is determined automatically
	 *           (largest value sum for any substance defines the reference).
	 */
	public ExperimentInterface ratioDataset(
			String[] treatmentReference, ConditionFilter cf, MeasurementFilter pf,
			BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		Experiment res = new Experiment();
		HashSet<String> addedSubstances = new HashSet<String>();
		for (SubstanceInterface si : experiment) {
			if (si.getName().startsWith("RESULT_"))
				continue;
			if (optStatus != null)
				optStatus.setCurrentStatusText2(si.getName());
			// omit color histogram values in the blue range
			// only bins < 11 are OK
			try {
				if (si.getName().contains(".histogram."))
					continue;
				if (si.getName().contains(".vis.hue.histogram.ratio.bin.") || si.getName().contains(".vis.hue.histogram.bin.")
						|| si.getName().contains(".vis.normalized.histogram.ratio.bin.")) {
					String b = si.getName().substring(si.getName().indexOf(".bin.") + ".bin.".length());
					b = b.substring(0, b.indexOf("."));
					int bin = Integer.parseInt(b);
					if (bin >= 11)
						continue;
				}
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: can't analyze substance " + si.getName() + " for histogram filtering!");
			}
			boolean varietyMatch = false;
			for (ConditionInterface ci : si) {
				ci.setExperimentHeader(res.getHeader());
				if (cf.filterConditionOut(ci))
					continue;
				boolean reference = false;
				for (String t : treatmentReference) {
					boolean v = ci.getTreatment() != null && ci.getTreatment().toUpperCase().contains(t.toUpperCase());
					if (v)
						reference = true;
					else {
						v = ci.getVariety() != null && ci.getVariety().toUpperCase().contains(t.toUpperCase());
						if (v) {
							reference = true;
							varietyMatch = true;
						}
					}
				}
				if (reference) {
					// nothing to do here
				} else {
					// search for reference
					ConditionInterface ciRef = null;
					for (ConditionInterface ciPotentialRef : si) {
						boolean ref = false;
						for (String t : treatmentReference) {
							boolean v = ciPotentialRef.getTreatment() != null && ciPotentialRef.getTreatment().toUpperCase().contains(t.toUpperCase());
							if (v)
								ref = true;
							else {
								v = ciPotentialRef.getVariety() != null && ciPotentialRef.getVariety().toUpperCase().contains(t.toUpperCase());
								if (v) {
									ref = true;
									varietyMatch = true;
								}
							}
						}
						if (ref) {
							boolean speciesOK = (ci.getSpecies() + "").equals(ciPotentialRef.getSpecies() + "");
							boolean genotypeOK = (ci.getGenotype() + "").equals(ciPotentialRef.getGenotype() + "");
							boolean varietyOK = varietyMatch ? true : (ci.getVariety() + "").equals(ciPotentialRef.getVariety() + "");
							boolean growthConditionsOK = true;// (ci.getGrowthconditions() + "").equals(ciPotentialRef.getGrowthconditions() + "");
							boolean sequenceOK = (ci.getSequence() + "").equals(ciPotentialRef.getSequence() + "");
							boolean allOK = speciesOK && genotypeOK && varietyOK && growthConditionsOK && sequenceOK;
							if (allOK)
								if (ciRef != null)
									throw new UnsupportedOperationException("Found a second possible reference condition (severe internal error).");
								else
									ciRef = ciPotentialRef;
						}
					}
					if (ciRef != null)
						processRef(ciRef, ci, res, si, pf, addedSubstances, varietyMatch);
				}
			}
		}
		res.setHeader(experiment.getHeader().clone());
		res.getHeader().setExperimentname(res.getHeader().getExperimentName() + " \\\\ (analysis of stress-impact)");
		if (optStatus != null)
			optStatus.setCurrentStatusText2("");
		return res;
	}
	
	private void processRef(ConditionInterface ciRef, ConditionInterface ci, ExperimentInterface res,
			SubstanceInterface si, MeasurementFilter pf, HashSet<String> addedSubstances, boolean varietyMatch) {
		// found reference for "ci"
		TreeSet<Integer> timePointsAvailForBoth = new TreeSet<Integer>();
		for (SampleInterface sample : ci)
			timePointsAvailForBoth.add(sample.getTime());
		for (SampleInterface sample : ciRef)
			if (!timePointsAvailForBoth.contains(sample.getTime()))
				timePointsAvailForBoth.remove(sample.getTime());
		
		// for each "Day (Int)" the average sample value is calculated for
		// this condition and its reference condition
		HashMap<Integer, Double> ciSum = new HashMap<Integer, Double>();
		HashMap<Integer, ArrayList<Double>> ciValues = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, Integer> ciN = new HashMap<Integer, Integer>();
		HashMap<Integer, SampleInterface> ciSampleExample = new HashMap<Integer, SampleInterface>();
		HashMap<Integer, NumericMeasurementInterface> ciValueExample = new HashMap<Integer, NumericMeasurementInterface>();
		for (SampleInterface sample : ci) {
			if (timePointsAvailForBoth.contains(sample.getTime())) {
				Double sampleValue = sample.getSampleAverage().getValue(pf);
				if (sampleValue == null || Double.isNaN(sampleValue) || Double.isInfinite(sampleValue))
					continue;
				if (!ciSampleExample.containsKey(sample.getTime()))
					ciSampleExample.put(sample.getTime(), sample);
				if (!ciValueExample.containsKey(sample.getTime())) {
					if (sample.size() > 0) {
						NumericMeasurementInterface value = sample.iterator().next();
						ciValueExample.put(sample.getTime(), value);
					}
				}
				Double s = ciSum.get(sample.getTime());
				if (s == null)
					s = 0d;
				s += sampleValue;
				ciSum.put(sample.getTime(), s);
				if (!ciN.containsKey(sample.getTime())) {
					ciN.put(sample.getTime(), 1);
					ciValues.put(sample.getTime(), new ArrayList<Double>());
				} else
					ciN.put(sample.getTime(), ciN.get(sample.getTime()) + 1);
				ciValues.get(sample.getTime()).add(sampleValue);
			}
		}
		
		HashMap<Integer, Double> ciRefSum = new HashMap<Integer, Double>();
		HashMap<Integer, ArrayList<Double>> ciRefValues = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, Integer> ciRefN = new HashMap<Integer, Integer>();
		for (SampleInterface sample : ciRef) {
			if (timePointsAvailForBoth.contains(sample.getTime())) {
				Double sampleValue = sample.getSampleAverage().getValue(pf);
				if (sampleValue == null || Double.isNaN(sampleValue) || Double.isInfinite(sampleValue))
					continue;
				Double s = ciRefSum.get(sample.getTime());
				if (s == null)
					s = 0d;
				s += sampleValue;
				ciRefSum.put(sample.getTime(), s);
				if (!ciRefN.containsKey(sample.getTime())) {
					ciRefN.put(sample.getTime(), 1);
					ciRefValues.put(sample.getTime(), new ArrayList<Double>());
				} else
					ciRefN.put(sample.getTime(), ciRefN.get(sample.getTime()) + 1);
				ciRefValues.get(sample.getTime()).add(sampleValue);
			}
		}
		SubstanceInterface newS = null;
		for (SubstanceInterface s : res) {
			if (s.getName().equals(si.getName())) {
				newS = s;
				break;
			}
		}
		if (newS == null) {
			newS = si.clone();
			// System.out.println("New substance: "+newS.getName());
		}
		ConditionInterface newC = ci.clone(newS);
		
		// divide sample average by average of reference condition
		// and add the (transformed) result to the result experiment
		for (Integer time : timePointsAvailForBoth) {
			if (ciRefSum.get(time) == null || ciSum.get(time) == null)
				continue;
			double ciAVG = ciSum.get(time) / ciN.get(time);
			double ciRefAVG = ciRefSum.get(time) / ciRefN.get(time);
			double ratio = ciAVG / ciRefAVG;
			double stdDev = 0d;
			{
				int n = ciValues.get(time).size();
				if (n > 1) {
					double avg = ciAVG;
					double sumDiff = 0;
					for (Double v : ciValues.get(time)) {
						sumDiff += (v - avg) * (v - avg);
					}
					stdDev = Math.sqrt(sumDiff / (n - 1));
				}
			}
			double stdDevRef = 0d;
			{
				int n = ciRefValues.get(time).size();
				if (n > 1) {
					double avg = ciRefAVG;
					double sumDiff = 0;
					for (Double v : ciRefValues.get(time)) {
						sumDiff += (v - avg) * (v - avg);
					}
					stdDevRef = Math.sqrt(sumDiff / (n - 1));
				}
			}
			
			// http://www.cartage.org.lb/en/themes/sciences/chemistry/miscellenous/helpfile/erroranalysis/multiplicationdivision/multiplicationdivision.htm
			double a = (stdDev / ciAVG) * (stdDev / ciAVG);
			double b = (stdDevRef / ciRefAVG) * (stdDevRef / ciRefAVG);
			double ratioStdDev = Math.sqrt(a + b) * ratio;
			if (!Double.isNaN(ratio) && !Double.isInfinite(ratio)) {
				if (varietyMatch) {
					String newVariety = ci.getVariety() + " / " + ciRef.getVariety();
					newC.setVariety(newVariety);
				} else {
					String newTreatment = ci.getTreatment() + " / " + ciRef.getTreatment();
					newC.setTreatment(newTreatment);
				}
				SampleInterface newSample = ciSampleExample.get(time).clone(newC);
				NumericMeasurementInterface newValue = ciValueExample.get(time).clone(newSample);
				newSample = newValue.getParentSample();
				newValue.setValue(ratio);
				newValue.setUnit(null);
				newValue.setQualityAnnotation(null);
				newSample.setSampleFineTimeOrRowId(null);
				newSample.add(newValue);
				newSample.setSampleAverage(null);
				newSample.getSampleAverage().setValue(ratio);
				newSample.getSampleAverage().setStddev(ratioStdDev);
				newSample.getSampleAverage().setUnit(null);
				newC.add(newSample);
			}
		}
		if (newC.size() > 0) {
			if (!addedSubstances.contains(newS.getName())) {
				res.add(newS);
				addedSubstances.add(newS.getName());
			}
			newS.add(newC);
		} // else
			// System.out.println("No ratio data for substance "+newS.getName());
	}
	
	/**
	 * split condition time courses in three optimal segments and calculate the linear models for them
	 * calculate the two intersection points and the m-value
	 * from the intersections points the X axis is the time,
	 * time point 1 (x) determines the beginning of stress reaction
	 * time point 2 (x) determines the beginning of recovery
	 * time point 2 (y) determines the maximum stress reaction (Einbruch von 0...100%)
	 * m2 defines the speed of stress reaction
	 * m3 defines the speed of stress recovery
	 * typical line shape: (segments 1, 2 and 3):
	 * _11111111_________________3
	 * _________2_____________3
	 * ___________2_________3
	 * _____________2____3
	 * _______________2
	 */
	
	public void fitThreeStepLinearModel(String... substances) {
		String timeUnit = null;
		long artificialSampleFineTime = 0;
		ArrayList<SubstanceInterface> subList = new ArrayList<SubstanceInterface>(experiment);
		for (SubstanceInterface s : subList) {
			for (String valid : substances) {
				if (s.getName().equals(valid)) {
					// at least 4 time points need to be available,
					// to define three line segments
					for (ConditionInterface ci : s) {
						TreeSet<Integer> days = new TreeSet<Integer>();
						for (SampleInterface si : ci) {
							days.add(si.getTime());
							if (timeUnit == null)
								timeUnit = si.getTimeUnit();
						}
						
						if (days.size() < 9)
							continue;
						HashMap<Integer, Integer> idx2day = new HashMap<Integer, Integer>();
						int i = 0;
						for (int d : days)
							idx2day.put(i++, d);
						// try out any possible (sensible) combination of line segment lengths
						// each segments needs at least 3 time points (minimum for linear regression)
						LinearRegressionModel bestM1 = null;
						LinearRegressionModel bestM2 = null;
						LinearRegressionModel bestM3 = null;
						IntersectionPoint bestP1 = null;
						IntersectionPoint bestP2 = null;
						TreeSet<Integer> bestDaysD1 = null;
						TreeSet<Integer> bestDaysD2 = null;
						TreeSet<Integer> bestDaysD3 = null;
						double lowestErrorSum = Double.MAX_VALUE;
						for (int stressStartTimeIndex = 0; stressStartTimeIndex < days.size() - 6; stressStartTimeIndex++) {
							for (int stressEndTimeIndex = stressStartTimeIndex + 3; stressEndTimeIndex < days.size() - 3; stressEndTimeIndex++) {
								TreeSet<Integer> d1 = getDays(idx2day, 0, stressStartTimeIndex);
								TreeSet<Integer> d2 = getDays(idx2day, stressStartTimeIndex, stressEndTimeIndex);
								TreeSet<Integer> d3 = getDays(idx2day, stressEndTimeIndex, days.size());
								if (d1.size() > 2 && d2.size() > 2 && d3.size() > 2) {
									// fit day index 1 to startIdx ==> line segment 1 (normal growth phase)
									LinearRegressionModel m1 = new LinearRegressionModel(getValues(ci, d1), true);
									// fit startIdx to endIdx ==> line segment 2 (stress phase)
									LinearRegressionModel m2 = new LinearRegressionModel(getValues(ci, d2), false);
									// fit endIdx to days.size()-1 ==> line segment 3 (stress recovery phase)
									LinearRegressionModel m3 = new LinearRegressionModel(getValues(ci, d3), false);
									IntersectionPoint p1 = m1.intersect(m2);
									IntersectionPoint p2 = m2.intersect(m3);
									double squareSumError = m1.getErrorSquareSum() + m2.getErrorSquareSum() + m3.getErrorSquareSum();
									if (squareSumError < lowestErrorSum) {
										bestM1 = m1;
										bestM2 = m2;
										bestM3 = m3;
										bestP1 = p1;
										bestP2 = p2;
										bestDaysD1 = d1;
										bestDaysD2 = d2;
										bestDaysD3 = d3;
										lowestErrorSum = squareSumError;
									}
								}
							}
						}
						if (bestM1 != null) {
							// construct result data points
							System.out.println("Best days D1: " + StringManipulationTools.getStringList(bestDaysD1, ","));
							System.out.println("Best days D2: " + StringManipulationTools.getStringList(bestDaysD2, ","));
							System.out.println("Best days D3: " + StringManipulationTools.getStringList(bestDaysD3, ","));
							double timeOfStressStart = bestP1.getX();
							double timeOfRecoveryStart = bestP2.getX();
							double worstStressExtend = bestP2.getY(); // 1 ==> no stress reaction, 0.5 ==> stress reaction result is 50% of analyzed property in
																					// comparison to reference
							double stressReactionSpeed = bestM2.getM();
							double stressRecoverySpeed = bestM3.getM();
							double noStressGrowthDifference = bestM1.getM();
							double noStressRatioDifference = bestM1.getAverageY();
							
							SubstanceInterface sNoStressInter = addOrCreateSubstance("lm3s_nostress_ratio." + s.getName());
							ConditionInterface cNSSI = addOrCreateCondition(sNoStressInter, ci);
							addOrCreateSampleAndAddValue(cNSSI, Integer.MAX_VALUE, timeUnit, noStressRatioDifference, "%", artificialSampleFineTime++);
							
							SubstanceInterface sStressStart = addOrCreateSubstance("lm3s_stress_start." + s.getName());
							ConditionInterface cSST = addOrCreateCondition(sStressStart, ci);
							addOrCreateSampleAndAddValue(cSST, Integer.MAX_VALUE, timeUnit, timeOfStressStart, timeUnit, artificialSampleFineTime++);
							
							SubstanceInterface sStressSpeed = addOrCreateSubstance("lm3s_stress_slope." + s.getName());
							ConditionInterface cSSP = addOrCreateCondition(sStressSpeed, ci);
							addOrCreateSampleAndAddValue(cSSP, Integer.MAX_VALUE, timeUnit, stressReactionSpeed, "%/" + timeUnit, artificialSampleFineTime++);
							
							SubstanceInterface sStressExtend = addOrCreateSubstance("lm3s_stress_max_extend." + s.getName());
							ConditionInterface cSSE = addOrCreateCondition(sStressExtend, ci);
							addOrCreateSampleAndAddValue(cSSE, Integer.MAX_VALUE, timeUnit, worstStressExtend, "%", artificialSampleFineTime++);
							
							SubstanceInterface sRecoveryStart = addOrCreateSubstance("lm3s_recovery_start." + s.getName());
							ConditionInterface cRST = addOrCreateCondition(sRecoveryStart, ci);
							addOrCreateSampleAndAddValue(cRST, Integer.MAX_VALUE, timeUnit, timeOfRecoveryStart, timeUnit, artificialSampleFineTime++);
							
							SubstanceInterface sRecoverySpeed = addOrCreateSubstance("lm3s_recovery_slope." + s.getName());
							ConditionInterface cRSP = addOrCreateCondition(sRecoverySpeed, ci);
							addOrCreateSampleAndAddValue(cRSP, Integer.MAX_VALUE, timeUnit, stressRecoverySpeed, "%/" + timeUnit, artificialSampleFineTime++);
						}
					}
				}
			}
		}
	}
	
	private void addOrCreateSampleAndAddValue(ConditionInterface c, int time, String timeUnit, double value, String unit, long artificialSampleFineTime) {
		SampleInterface ss = null;
		// for (SampleInterface si : c)
		// if (si.getTime() == time) {
		// ss = si;
		// break;
		// }
		if (ss == null) {
			ss = new Sample(c);
			ss.setTime(time);
			ss.setTimeUnit(timeUnit);
			ss.setSampleFineTimeOrRowId(artificialSampleFineTime);
			c.add(ss);
		}
		NumericMeasurementInterface n = new NumericMeasurement(ss);
		n.setValue(value);
		n.setUnit(unit);
		ss.add(n);
	}
	
	private ConditionInterface addOrCreateCondition(SubstanceInterface s, ConditionInterface templateForNew) {
		for (ConditionInterface ci : s)
			if (ci.compareTo(templateForNew) == 0)
				return ci;
		ConditionInterface c = templateForNew.clone(s);
		s.add(c);
		return c;
	}
	
	private SubstanceInterface addOrCreateSubstance(String name) {
		for (SubstanceInterface si : experiment)
			if (si.getName().equals(name))
				return si;
		SubstanceInterface s = new Substance();
		s.setName(name);
		experiment.add(s);
		return s;
	}
	
	private TreeMap<Integer, Double> getValues(ConditionInterface ci, TreeSet<Integer> d1) {
		TreeMap<Integer, Double> resS = new TreeMap<Integer, Double>();
		TreeMap<Integer, Integer> resN = new TreeMap<Integer, Integer>();
		for (SampleInterface si : ci)
			if (d1.contains(si.getTime())) {
				if (!resS.containsKey(si.getTime())) {
					resS.put(si.getTime(), 0d);
					resN.put(si.getTime(), 0);
				}
				resS.put(si.getTime(), resS.get(si.getTime()) + si.getSampleAverage().getValue());
				resN.put(si.getTime(), resN.get(si.getTime()) + 1);
			}
		TreeMap<Integer, Double> res = new TreeMap<Integer, Double>();
		for (Integer time : resS.keySet())
			res.put(time, resS.get(time) / resN.get(time));
		return res;
	}
	
	private TreeSet<Integer> getDays(HashMap<Integer, Integer> idx2day, int start_incl, int end_excl) {
		TreeSet<Integer> res = new TreeSet<Integer>();
		for (int idx : idx2day.keySet())
			if (idx >= start_incl && idx < end_excl)
				res.add(idx2day.get(idx));
		return res;
	}
	
	public ArrayList<SampleInterface> getSamplesFromYesterDay() {
		return null;
	}
	
	public static int getMinuteOfDayFromSampleTime(int time, Long sampleFineTimeOrRowId) {
		return 0;
	}
}