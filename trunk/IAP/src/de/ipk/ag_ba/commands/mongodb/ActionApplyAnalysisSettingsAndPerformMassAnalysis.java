package de.ipk.ag_ba.commands.mongodb;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.StringManipulationTools;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.mongo.RunnableProcessingSubstance;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 */
public class ActionApplyAnalysisSettingsAndPerformMassAnalysis extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentHeaderInterface> experiments;
	
	private final ArrayList<String> errors = new ArrayList<String>();
	private StringBuilder res;
	
	public ActionApplyAnalysisSettingsAndPerformMassAnalysis(String tooltip) {
		super(tooltip);
	}
	
	public ActionApplyAnalysisSettingsAndPerformMassAnalysis(MongoDB m, ArrayList<ExperimentHeaderInterface> experiments) {
		this("<html>" +
				"Apply analysis settings to group of experiments and submit analysis tasks.");
		this.m = m;
		this.experiments = experiments;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		status.setCurrentStatusText1("");
		status.setCurrentStatusText2("");
		errors.clear();
		res = new StringBuilder();
		res.append("<html><head>" +
				"<title>Experiment overview (list of imported image unit configurations)</title><body>" +
				"<h1>Experiment overview (list of imported image unit configurations)</h1>" +
				"<table border='1'>");
		res.append("<tr>" +
				"<th>Experiments</th>" +
				// "<th>Database ID</th>" +
				// "<th>Source</th>" +
				"<th>VIS</th>" +
				"<th>FLUO</th>" +
				"<th>NIR</th>" +
				"<th>IR</th>" +
				"<th>Other</th></tr>");
		try {
			final TreeMap<String, ArrayList<ExperimentHeaderInterface>> config2headers = new TreeMap<String, ArrayList<ExperimentHeaderInterface>>();
			for (ExperimentHeaderInterface eh : ExperimentHeaderService.filterNewest(experiments)) {
				if (eh.getImportusergroup() != null && !eh.getImportusergroup().equalsIgnoreCase("ANALYSIS RESULTS")) {
					try {
						status.setCurrentStatusText1("Process experiment info");
						status.setCurrentStatusText2(eh.getExperimentName());
						final TreeSet<String> vis = new TreeSet<String>();
						final TreeSet<String> flu = new TreeSet<String>();
						final TreeSet<String> nir = new TreeSet<String>();
						final TreeSet<String> ir = new TreeSet<String>();
						final TreeSet<String> other = new TreeSet<String>();
						ThreadSafeOptions invalid = new ThreadSafeOptions();
						RunnableProcessingSubstance visitSubstanceObject = new RunnableProcessingSubstance() {
							@Override
							public void visit(SubstanceInterface substance) {
								if (substance.getInfo() == null || substance.getInfo().trim().isEmpty())
									return;
								ImageConfiguration conf = ImageConfiguration.get(substance.getName());
								switch (conf) {
									case FluoSide:
									case FluoTop:
										flu.add(substance.getInfo());
										break;
									case IrSide:
									case IrTop:
										ir.add(substance.getInfo());
										break;
									case NirSide:
									case NirTop:
										nir.add(substance.getInfo());
										break;
									case RgbSide:
									case RgbTop:
										vis.add(substance.getInfo());
										break;
									case Unknown:
										other.add(substance.getInfo());
										break;
								}
							}
						};
						m.visitExperiment(eh, status, visitSubstanceObject, false, null, null, null, invalid);
						StringBuilder config = new StringBuilder();
						config.append("<td>" + StringManipulationTools.getStringList(vis, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(flu, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(nir, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(ir, ", ") + "</td>");
						config.append("<td>" + StringManipulationTools.getStringList(other, ", ") + "</td>");
						if (vis.size() + flu.size() + nir.size() + ir.size() + other.size() > 0) {
							String s = config.toString();
							if (!config2headers.containsKey(s)) {
								config2headers.put(s, new ArrayList<ExperimentHeaderInterface>());
							}
							config2headers.get(s).add(eh);
						}
					} catch (Exception err) {
						errors.add("Could not process " + eh.getExperimentName() + ": " + err.getMessage());
						err.printStackTrace();
					}
				}
			}
			for (String conf : config2headers.keySet()) {
				ArrayList<ExperimentHeaderInterface> experimentNames = config2headers.get(conf);
				Collections.sort(experimentNames, new Comparator<ExperimentHeaderInterface>() {
					@Override
					public int compare(ExperimentHeaderInterface o1, ExperimentHeaderInterface o2) {
						return o1.getExperimentName().compareTo(o2.getExperimentName());
					}
				});
				ArrayList<ExperimentHeaderInterface> withSettingList = new ArrayList<ExperimentHeaderInterface>();
				ArrayList<ExperimentHeaderInterface> noSettingList = new ArrayList<ExperimentHeaderInterface>();
				for (ExperimentHeaderInterface eh : experimentNames) {
					if (eh.getImportdate() != null && eh.getSettings() != null && !eh.getSettings().isEmpty())
						withSettingList.add(eh);
					if (eh.getSettings() == null || eh.getSettings().isEmpty())
						noSettingList.add(eh);
				}
				if (!withSettingList.isEmpty() && !noSettingList.isEmpty()) {
					// chance to add settings to newer or older datasets without settings
					ArrayList<Object> params = new ArrayList<Object>;

					JComboBox<ExperimentHeaderInterface> hasSettings = new JComboBox<ExperimentHeaderInterface>();
					for (ExperimentHeaderInterface withS : withSettingList)
						hasSettings.addItem(withS);
					
					params.add("Select settings to be copied:");
					params.add(hasSettings);
					
					for (ExperimentHeaderInterface noS : noSettingList) {
						final	JCheckBox analyzeExperiment = new JCheckBox(new AbstractAction("Analyze Experiment") {
							@Override
							public void actionPerformed(ActionEvent e) {
								// TODO Auto-generated method stub
								  
							}
						});
						JCheckBox applySettings = new JCheckBox(new AbstractAction("Assign settings") {
							@Override
							public void actionPerformed(ActionEvent e) {
								analyzeExperiment.setEnabled(true); 
							}
						});
						params.add(TableLayout.getSplitVertical(applySettings, analyzeExperiment, TableLayout.PREFERRED, TableLayout.PREFERRED));
					}
					MyInputHelper res = MyInputHelper.getInput(
							"<html>" + noSettingList.size()+" experiments have no analysis settings applied.<br>" +
								"But there are "+withSettingList.size()+" experiments with the same list of<br>" +
										"imaging configuration settings names which have analysis settings applied.<br>" +
										"<br>" +
										"You may now choose to apply the analysis settings from the selected experiment<br>" +
										"to the experiments with no settings, listed below.", 
							"Apply settings to similar experiments?",
							parameters)
					res.append("<tr><td>" + StringManipulationTools.getStringList(experimentNames, "<br>") + "</td>" + conf + "</tr>");
				}
			}
		} finally {
			res.append("</table></body></html>");
			if (!errors.isEmpty()) {
				String r = res.toString();
				res = new StringBuilder();
				r = r.replaceFirst("table border", "h2>Processing Errors (" + errors.size() + ")</h2>" + StringManipulationTools.getStringList(errors, "<br>")
						+ "<br><br><table border");
				res.append(r);
			}
		}
		status.setCurrentStatusText1("Processing finished");
		status.setCurrentStatusText2("");
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(res.toString());
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Copy Analysis Settings";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Restore-64.png";
	}
	
}
