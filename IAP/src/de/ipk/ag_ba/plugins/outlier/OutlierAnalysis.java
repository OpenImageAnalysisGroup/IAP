package de.ipk.ag_ba.plugins.outlier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.ErrorMsg;
import org.apache.commons.math3.distribution.TDistribution;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.plugins.outlier.data.Handler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author Christian Klukas
 */
public class OutlierAnalysis extends AbstractNavigationAction implements ActionDataProcessing {
	
	private ExperimentReference experimentReference;
	private String selTimepoint;
	private double threshold = 0.01;
	protected ArrayList<String> calculationResults = new ArrayList<String>();
	private int plantIDcount;
	private int traitCount;
	private int maxDescCount;
	
	private final HashMap<String, IOurl> visTopPlantID2url = new HashMap<String, IOurl>();
	private final HashMap<String, IOurl> visSidePlantID2url = new HashMap<String, IOurl>();
	
	public OutlierAnalysis(String tooltip) {
		super(tooltip);
	}
	
	public OutlierAnalysis() {
		this("Identify and describe outlier plants");
	}
	
	@Override
	public ParameterOptions getParameters() {
		try {
			ArrayList<String> times = new ArrayList<String>();
			for (String t : Experiment.getTimes(experimentReference.getData()))
				times.add(0, t);
			return new ParameterOptions("Select the desired experiment day:",
					new Object[] {
							"Timepoint", times,
							"p-value*100", threshold
					});
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	@Override
	public void setParameters(Object[] parameters) {
		if (parameters != null) {
			selTimepoint = (String) parameters[0];
			threshold = (Double) parameters[1];
		} else {
			selTimepoint = null;
			threshold = 0.01;
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		calculationResults.clear();
		
		String savedSelTime = selTimepoint;
		
		StringBuilder sb = new StringBuilder();
		sb.append("<table><tr><th>Time</th><th>Number of outlier plants</th></tr>");
		
		for (String t : Experiment.getTimes(experimentReference.getData())) {
			status.setCurrentStatusText1("Analyze Outlier Counts");
			status.setCurrentStatusText2(t);
			selTimepoint = t;
			TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile =
					new TreeMap<String, LinkedHashMap<String, Boolean>>();
			processData(plantid2property2topOrLowPercentile);
			sb.append("<tr><td>" + t + "</td><td>" + plantid2property2topOrLowPercentile.size());
		}
		sb.append("</table>");
		calculationResults.clear();
		calculationResults.add(sb.toString());
		
		selTimepoint = savedSelTime;
		TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile =
				new TreeMap<String, LinkedHashMap<String, Boolean>>();
		processData(plantid2property2topOrLowPercentile);
	}
	
	private void processData(TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile) throws Exception {
		ArrayList<Double> numericValues = new ArrayList<Double>();
		ArrayList<String> plantIDs = new ArrayList<String>();
		HashSet<String> allPlantIDs = new HashSet<String>();
		visTopPlantID2url.clear();
		visSidePlantID2url.clear();
		maxDescCount = 0;
		if (selTimepoint != null) {
			ExperimentInterface exp = experimentReference.getData(status);
			traitCount = 0;
			int idx = 0;
			for (SubstanceInterface si : exp) {
				String trait = si.getName();
				if (trait.equals("side.vis") || trait.equals("top.vis") || trait.equals("vis.side") || trait.equals("vis.top"))
					for (ConditionInterface ci : si) {
						for (SampleInterface sai : ci) {
							if (sai.getSampleTime().equals(selTimepoint)) {
								for (NumericMeasurementInterface nmi : sai) {
									if (nmi instanceof ImageData) {
										ImageData id = (ImageData) nmi;
										if (trait.contains("side"))
											if (!visSidePlantID2url.containsKey(nmi.getQualityAnnotation()))
												visSidePlantID2url.put(nmi.getQualityAnnotation(), id.getURL());
										if (trait.contains("top"))
											if (!visTopPlantID2url.containsKey(nmi.getQualityAnnotation()))
												visTopPlantID2url.put(nmi.getQualityAnnotation(), id.getURL());
									}
								}
							}
						}
					}
			}
			for (SubstanceInterface si : exp) {
				String trait = si.getName();
				if (trait.endsWith(".norm"))
					continue;
				if (trait.endsWith(".sum"))
					continue;
				if (trait.contains(".marker."))
					continue;
				if (trait.startsWith("RESULT_"))
					continue;
				if (trait.startsWith("mark"))
					continue;
				if (trait.contains("section"))
					continue;
				if (trait.contains(".filled."))
					continue;
				if (trait.contains("histogram"))
					continue;
				if (trait.endsWith("_before"))
					continue;
				if (trait.endsWith("_sum"))
					continue;
				if (trait.contains("water"))
					continue;
				if (trait.contains("curling.n"))
					continue;
				traitCount++;
				idx++;
				if (status != null) {
					status.setCurrentStatusValueFine(100d * idx / traitCount);
					status.setCurrentStatusText1("Process trait");
					status.setCurrentStatusText2(trait);
				}
				HashMap<String, ArrayList<Double>> plantId2values = new HashMap<String, ArrayList<Double>>();
				for (ConditionInterface ci : si) {
					for (SampleInterface sai : ci) {
						if (sai.getSampleTime().equals(selTimepoint)) {
							for (NumericMeasurementInterface nmi : sai) {
								if (nmi instanceof NumericMeasurement) {
									double val = nmi.getValue();
									if (!Double.isNaN(val) && !Double.isInfinite(val) && nmi.getQualityAnnotation() != null && !nmi.getQualityAnnotation().isEmpty()) {
										if (!plantId2values.containsKey(nmi.getQualityAnnotation()))
											plantId2values.put(nmi.getQualityAnnotation(), new ArrayList<Double>());
										plantId2values.get(nmi.getQualityAnnotation()).add(val);
									}
								}
							}
						}
					}
				}
				numericValues.clear();
				plantIDs.clear();
				for (String plantID : plantId2values.keySet()) {
					numericValues.add(median(plantId2values.get(plantID)));
					plantIDs.add(plantID);
				}
				
				grubbsTest(plantIDs, trait, numericValues, plantid2property2topOrLowPercentile, threshold);
				
			}
			processResults(plantid2property2topOrLowPercentile);
			
			if (status != null) {
				status.setCurrentStatusValueFine(100d);
				status.setCurrentStatusText1("Processing finished");
				status.setCurrentStatusText2("");
			}
			
		}
		plantIDcount = allPlantIDs.size();
	}
	
	private Double median(ArrayList<Double> m) {
		java.util.Collections.sort(m);
		int middle = m.size() / 2;
		if (m.size() % 2 == 1) {
			return m.get(middle);
		} else {
			return (m.get(middle - 1) + m.get(middle)) / 2.0;
		}
	}
	
	/**
	 * @return Number of removed outliers.
	 */
	private int grubbsTest(ArrayList<String> plantIDs, String trait, ArrayList<Double> numericValues,
			TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile, double alpha) {
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
			for (Double value : numericValues) {
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
			for (Double value : numericValues) {
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
							numericValues.remove(indexOfMaxValue);
						} else {
							plantID = plantIDs.get(indexOfMinValue);
							plantIDs.remove(indexOfMinValue);
							numericValues.remove(indexOfMinValue);
						}
						if (!plantid2property2topOrLowPercentile.containsKey(plantID))
							plantid2property2topOrLowPercentile.put(plantID, new LinkedHashMap<String, Boolean>());
						plantid2property2topOrLowPercentile.get(plantID).put(trait, isMaxPotentialOutlier);
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
	
	private void processResults(TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile) {
		StringBuilder sb = new StringBuilder();
		if (plantid2property2topOrLowPercentile.size() > 0) {
			sb.append("<html><table>");
			sb.append("<tr><th colspan='" + (maxDescCount + 2) + "'>Processed " + traitCount + " traits and found " + plantIDcount
					+ " plant IDs with outlier trait values</th></tr>");
			sb.append("<tr><th>" + plantid2property2topOrLowPercentile.size() + " Plants</td><th>Side View</th><th>Top View</th><th colspan='" + maxDescCount
					+ "'>Traits</th></tr>");
			for (String p : plantid2property2topOrLowPercentile.keySet()) {
				sb.append("<tr><td>" + p + "</td>");
				if (visSidePlantID2url.containsKey(p))
					sb.append("<td><img src='data:" + visSidePlantID2url.get(p).toString() + "'/></td>");
				else
					sb.append("<td><td/>");
				if (visTopPlantID2url.containsKey(p))
					sb.append("<td><img src='data:" + visTopPlantID2url.get(p).toString() + "'/></td>");
				else
					sb.append("<td><td/>");
				LinkedHashMap<String, Boolean> traitChange = plantid2property2topOrLowPercentile.get(p);
				for (String trait : traitChange.keySet()) {
					if (traitChange.get(trait))
						sb.append("<td bgcolor='#FFEEEE'>high " + trait + "</td>");
					else
						sb.append("<td bgcolor='#EEEEFF'>low " + trait + "</td>");
				}
				sb.append("</tr>");
			}
			sb.append("</table>");
			calculationResults.add(sb.toString());
		} else
			calculationResults.add("Found no plants with outlier traits for selected day and alpha level.");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		Handler.install();
		return new MainPanelComponent(calculationResults);
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experimentReference = experimentReference;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Extreme Traits";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-System-search.png";
	}
}
