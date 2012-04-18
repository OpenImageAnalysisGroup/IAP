package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class ExperimentCalculationService {
	
	private final ExperimentInterface experiment;
	
	public ExperimentCalculationService(ExperimentInterface experiment) {
		this.experiment = experiment;
	}
	
	public ExperimentInterface ratioDataset(String treatmentReference) {
		Experiment res = new Experiment();
		res.setHeader(experiment.getHeader().clone());
		res.getHeader().setExperimentname(res.getHeader().getExperimentName() + " (STRESS RATIO)");
		for (SubstanceInterface si : experiment) {
			for (ConditionInterface ci : si) {
				boolean reference = ci.getTreatment() != null && ci.getTreatment().contains(treatmentReference);
				if (reference) {
					// nothing to do here
				} else {
					// search for reference
					for (ConditionInterface ciPotentialRef : si) {
						boolean ref = ciPotentialRef.getTreatment() != null && ciPotentialRef.getTreatment().contains(treatmentReference);
						if (ref) {
							boolean speciesOK = (ci.getSpecies() + "").equals(ciPotentialRef.getSpecies() + "");
							boolean genotypeOK = (ci.getGenotype() + "").equals(ciPotentialRef.getGenotype() + "");
							boolean varietyOK = (ci.getVariety() + "").equals(ciPotentialRef.getVariety() + "");
							boolean growthConditionsOK = (ci.getGrowthconditions() + "").equals(ciPotentialRef.getGrowthconditions() + "");
							boolean sequenceOK = (ci.getSequence() + "").equals(ciPotentialRef.getSequence() + "");
							boolean allOK = speciesOK && genotypeOK && varietyOK && growthConditionsOK && sequenceOK;
							if (allOK) {
								// found reference for "ci"
								ConditionInterface ciRef = ciPotentialRef;
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
										SubstanceInterface newS = si.clone();
										res.add(newS);
										ConditionInterface newC = ci.clone(newS);
										newS.add(newC);
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
							}
						}
					}
				}
			}
		}
		return res;
	}
}
