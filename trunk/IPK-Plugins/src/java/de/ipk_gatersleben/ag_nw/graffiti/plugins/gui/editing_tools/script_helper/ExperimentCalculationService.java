package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class ExperimentCalculationService {
	
	private final ExperimentInterface experiment;
	
	public ExperimentCalculationService(ExperimentInterface experiment) {
		this.experiment = experiment;
	}
	
	public ExperimentInterface ratioDataset(String treatmentReference, ConditionFilter cf) {
		Experiment res = new Experiment();
		res.setHeader(experiment.getHeader().clone());
		res.getHeader().setExperimentname(res.getHeader().getExperimentName() + " (STRESS RATIO)");
		for (SubstanceInterface si : experiment) {
			for (ConditionInterface ci : si) {
				// if (cf.filterConditionOut(ci))
				// continue;
				boolean reference = ci.getTreatment() != null && ci.getTreatment().contains(treatmentReference);
				if (reference) {
					// nothing to do here
				} else {
					// search for reference
					ConditionInterface ciRef = null;
					for (ConditionInterface ciPotentialRef : si) {
						boolean ref = ciPotentialRef.getTreatment() != null && ciPotentialRef.getTreatment().contains(treatmentReference);
						if (ref) {
							boolean speciesOK = (ci.getSpecies() + "").equals(ciPotentialRef.getSpecies() + "");
							boolean genotypeOK = (ci.getGenotype() + "").equals(ciPotentialRef.getGenotype() + "");
							boolean varietyOK = (ci.getVariety() + "").equals(ciPotentialRef.getVariety() + "");
							boolean growthConditionsOK = (ci.getGrowthconditions() + "").equals(ciPotentialRef.getGrowthconditions() + "");
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
						processRef(ciRef, ci, res, si);
				}
			}
		}
		return res;
	}
	
	private void processRef(ConditionInterface ciRef, ConditionInterface ci, ExperimentInterface res,
			SubstanceInterface si) {
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
				Double v = sample.getSampleAverage().getValue();
				if (v == null || Double.isNaN(v) || Double.isInfinite(v))
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
				s += v;
				ciSum.put(sample.getTime(), s);
				if (!ciN.containsKey(sample.getTime())) {
					ciN.put(sample.getTime(), 1);
					ciValues.put(sample.getTime(), new ArrayList<Double>());
				} else
					ciN.put(sample.getTime(), ciN.get(sample.getTime()) + 1);
				ciValues.get(sample.getTime()).add(s);
			}
		}
		
		HashMap<Integer, Double> ciRefSum = new HashMap<Integer, Double>();
		HashMap<Integer, ArrayList<Double>> ciRefValues = new HashMap<Integer, ArrayList<Double>>();
		HashMap<Integer, Integer> ciRefN = new HashMap<Integer, Integer>();
		for (SampleInterface sample : ciRef) {
			if (timePointsAvailForBoth.contains(sample.getTime())) {
				Double v = sample.getSampleAverage().getValue();
				if (v == null || Double.isNaN(v) || Double.isInfinite(v))
					continue;
				Double s = ciRefSum.get(sample.getTime());
				if (s == null)
					s = 0d;
				s += sample.getSampleAverage().getValue();
				ciRefSum.put(sample.getTime(), s);
				if (!ciRefN.containsKey(sample.getTime())) {
					ciRefN.put(sample.getTime(), 1);
					ciRefValues.put(sample.getTime(), new ArrayList<Double>());
				} else
					ciRefN.put(sample.getTime(), ciRefN.get(sample.getTime()) + 1);
				ciRefValues.get(sample.getTime()).add(s);
			}
		}
		SubstanceInterface newS = null;
		for (SubstanceInterface s : res) {
			if (s.getName().equals(si.getName())) {
				newS = s;
				break;
			}
		}
		if (newS == null)
			newS = si.clone();
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
			double ratioStdDev = Math.sqrt(a + b);
			if (!Double.isNaN(ratio) && !Double.isInfinite(ratio)) {
				String newTreatment = ci.getTreatment() + " / " + ciRef.getTreatment();
				newC.setTreatment(newTreatment);
				SampleInterface newSample = ciSampleExample.get(time).clone(newC);
				newC.add(newSample);
				NumericMeasurementInterface newValue = ciValueExample.get(time).clone(newSample);
				newSample.add(newValue);
				newValue.setValue(ratio);
				newSample.getSampleAverage().setValue(ratio);
				newSample.getSampleAverage().setStddev(ratioStdDev);
			}
		}
		if (newC.size() > 0) {
			res.add(newS);
			newS.add(newC);
		}
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
		String timeUnit = "";
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
						double lowestErrorSum = Double.MAX_VALUE;
						for (int stressStartTimeIndex = 0; stressStartTimeIndex < days.size() - 6; stressStartTimeIndex++) {
							for (int stressEndTimeIndex = stressStartTimeIndex + 3; stressEndTimeIndex < days.size() - 3; stressEndTimeIndex++) {
								TreeSet<Integer> d1 = getDays(idx2day, 0, stressStartTimeIndex);
								TreeSet<Integer> d2 = getDays(idx2day, stressStartTimeIndex, stressEndTimeIndex);
								TreeSet<Integer> d3 = getDays(idx2day, stressEndTimeIndex, days.size());
								if (d1.size() > 2 && d2.size() > 2 && d3.size() > 2) {
									// fit day index 1 to startIdx ==> line segment 1 (normal growth phase)
									LinearRegressionModel m1 = new LinearRegressionModel(getValues(ci, d1));
									// fit startIdx to endIdx ==> line segment 2 (stress phase)
									LinearRegressionModel m2 = new LinearRegressionModel(getValues(ci, d2));
									// fit endIdx to days.size()-1 ==> line segment 3 (stress recovery phase)
									LinearRegressionModel m3 = new LinearRegressionModel(getValues(ci, d3));
									IntersectionPoint p1 = m1.intersect(m2);
									IntersectionPoint p2 = m2.intersect(m3);
									double squareSumError = m1.getErrorSquareSum() + m2.getErrorSquareSum() + m3.getErrorSquareSum();
									if (squareSumError < lowestErrorSum) {
										bestM1 = m1;
										bestM2 = m2;
										bestM3 = m3;
										bestP1 = p1;
										bestP2 = p2;
										lowestErrorSum = squareSumError;
									}
								}
							}
						}
						if (bestM1 != null) {
							// construct result data points
							double timeOfStressStart = bestP1.getX();
							double timeOfRecoveryStart = bestP2.getX();
							double worstStressExtend = bestP2.getY(); // 1 ==> no stress reaction, 0.5 ==> stress reaction result is 50% of analyzed property in
																					// comparison to reference
							double stressReactionSpeed = bestM2.getM();
							double stressRecoverySpeed = bestM3.getM();
							
							SubstanceInterface sStressStart = addOrCreateSubstance("lm3s_stress_start" + s.getName());
							ConditionInterface cSST = addOrCreateCondition(sStressStart, ci);
							addOrCreateSampleAndAddValue(cSST, -1, "-1", timeOfStressStart, timeUnit);
							
							SubstanceInterface sStressSpeed = addOrCreateSubstance("lm3s_stress_speed" + s.getName());
							ConditionInterface cSSP = addOrCreateCondition(sStressSpeed, ci);
							addOrCreateSampleAndAddValue(cSSP, -1, "-1", stressReactionSpeed, "%/" + timeUnit);
							
							SubstanceInterface sStressExtend = addOrCreateSubstance("lm3s_stress_extend" + s.getName());
							ConditionInterface cSSE = addOrCreateCondition(sStressExtend, ci);
							addOrCreateSampleAndAddValue(cSSE, -1, "-1", worstStressExtend, "%");
							
							SubstanceInterface sRecoveryStart = addOrCreateSubstance("lm3s_recovery_start" + s.getName());
							ConditionInterface cRST = addOrCreateCondition(sRecoveryStart, ci);
							addOrCreateSampleAndAddValue(cRST, -1, "-1", timeOfRecoveryStart, timeUnit);
							
							SubstanceInterface sRecoverySpeed = addOrCreateSubstance("lm3s_recovery_speed" + s.getName());
							ConditionInterface cRSP = addOrCreateCondition(sRecoverySpeed, ci);
							addOrCreateSampleAndAddValue(cRSP, -1, "-1", stressRecoverySpeed, "%/" + timeUnit);
						}
					}
				}
			}
		}
	}
	
	private void addOrCreateSampleAndAddValue(ConditionInterface c, int time, String timeUnit, double value, String unit) {
		SampleInterface ss = null;
		for (SampleInterface si : c)
			if (si.getTime() == time) {
				ss = si;
				break;
			}
		if (ss == null) {
			ss = new Sample(c);
			ss.setTime(time);
			ss.setTimeUnit(timeUnit);
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
}