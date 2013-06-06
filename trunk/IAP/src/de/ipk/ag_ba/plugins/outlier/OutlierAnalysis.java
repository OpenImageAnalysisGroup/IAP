package de.ipk.ag_ba.plugins.outlier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.apache.commons.math3.stat.inference.TTest;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_actions.ParameterOptions;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class OutlierAnalysis extends AbstractNavigationAction implements ActionDataProcessing {
	
	private ExperimentReference experimentReference;
	private String selTimepoint;
	private double threshold = 0.00010;
	protected ArrayList<String> calculationResults = new ArrayList<String>();
	private int plantIDcount;
	private int traitCount;
	private int maxDescCount;
	
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
		TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile =
				new TreeMap<String, LinkedHashMap<String, Boolean>>();
		ArrayList<Double> numericValues = new ArrayList<Double>();
		ArrayList<String> plantIDs = new ArrayList<String>();
		HashSet<String> allPlantIDs = new HashSet<String>();
		maxDescCount = 0;
		if (selTimepoint != null) {
			ExperimentInterface exp = experimentReference.getData(status);
			traitCount = 0;
			int idx = 0;
			for (SubstanceInterface si : exp) {
				String trait = si.getName();
				if (trait.endsWith(".norm"))
					continue;
				if (trait.startsWith("RESULT_"))
					continue;
				if (trait.startsWith("mark"))
					continue;
				if (trait.contains("section"))
					continue;
				if (trait.contains("histogram"))
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
				for (ConditionInterface ci : si) {
					for (SampleInterface sai : ci) {
						if (sai.getSampleTime().equals(selTimepoint)) {
							numericValues.clear();
							plantIDs.clear();
							for (NumericMeasurementInterface nmi : sai) {
								if (nmi instanceof NumericMeasurement) {
									double val = nmi.getValue();
									if (!Double.isNaN(val) && !Double.isInfinite(val) && nmi.getQualityAnnotation() != null && !nmi.getQualityAnnotation().isEmpty()) {
										numericValues.add(val);
										plantIDs.add(nmi.getQualityAnnotation());
									}
								}
							}
							if (plantIDs.size() > 3) {
								int i = 0;
								for (String p : plantIDs) {
									allPlantIDs.add(p);
									double val = numericValues.get(i);
									double[] otherVals = new double[plantIDs.size() - 1];
									int oi = 0, filled = 0;
									for (Double v : numericValues) {
										if (oi != i) {
											otherVals[filled++] = v;
										}
										oi++;
									}
									i++;
									double sum = 0;
									for (double v : otherVals) {
										sum += v;
									}
									try {
										double mean = sum / otherVals.length;
										double p_val = new TTest().tTest(val, otherVals);
										if (p_val < threshold) {
											if (!plantid2property2topOrLowPercentile.containsKey(p))
												plantid2property2topOrLowPercentile.put(p, new LinkedHashMap<String, Boolean>());
											plantid2property2topOrLowPercentile.get(p).put(si.getName(), val > mean);
											if (plantid2property2topOrLowPercentile.get(p).size() > maxDescCount)
												maxDescCount = plantid2property2topOrLowPercentile.get(p).size();
										}
									} catch (Exception e) {
										System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: T-test calculation problem: " + e.getMessage());
									}
								}
							}
						}
					}
				}
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
	
	private void processResults(TreeMap<String, LinkedHashMap<String, Boolean>> plantid2property2topOrLowPercentile) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><table>");
		sb.append("<tr><th colspan='" + (maxDescCount + 1) + "'>Processed " + traitCount + " traits and " + plantIDcount + " plant IDs</th></tr>");
		sb.append("<tr><th>Plant ID</td><th colspan='" + maxDescCount + "'>Traits</th></tr>");
		for (String p : plantid2property2topOrLowPercentile.keySet()) {
			sb.append("<tr><td>" + p + "</td>");
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
		return "Identify Outliers";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Zoom-Fit-Best-64.png";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
}
