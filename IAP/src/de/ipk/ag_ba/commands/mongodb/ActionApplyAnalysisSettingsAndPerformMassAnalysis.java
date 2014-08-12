package de.ipk.ag_ba.commands.mongodb;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.ErrorMsg;
import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.ActionPerformGridAnalysis;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.navigation_model.RemoteExecutionWrapperAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
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
				"<title>" + getDefaultTitle() + " - Command Results</title><body>" +
				"<h1>" + getDefaultTitle() + " - Command Results</h1>");
		try {
			final boolean enableRemoteTaskExecution = IAPmain.isSettingEnabled(IAPfeature.REMOTE_EXECUTION);
			TreeMap<String, ArrayList<ExperimentHeaderInterface>> config2headers = new TreeMap<String, ArrayList<ExperimentHeaderInterface>>();
			getConfigSets(config2headers);
			for (String conf : config2headers.keySet()) {
				ArrayList<ExperimentHeaderInterface> experimentHeaders = config2headers.get(conf);
				Collections.sort(experimentHeaders, new Comparator<ExperimentHeaderInterface>() {
					@Override
					public int compare(ExperimentHeaderInterface o1, ExperimentHeaderInterface o2) {
						return o1.getExperimentName().compareTo(o2.getExperimentName());
					}
				});
				ArrayList<ExperimentHeaderInterface> withSettingList = new ArrayList<ExperimentHeaderInterface>();
				ArrayList<ExperimentHeaderInterface> noSettingList = new ArrayList<ExperimentHeaderInterface>();
				for (ExperimentHeaderInterface eh : experimentHeaders) {
					if (eh.getImportdate() != null && eh.getSettings() != null && !eh.getSettings().isEmpty()) {
						try {
							ExperimentReference er = new ExperimentReference(eh, m);
							IniIoProvider iniIO = er.getIniIoProvider();
							SystemOptions so = SystemOptions.getInstance(null, iniIO);
							String vv = so.getString("DESCRIPTION", "tuned_for_IAP_version", "(unknown legacy IAP version)");
							boolean vok = ReleaseInfo.IAP_VERSION_STRING.equals(vv);
							if (vok)
								withSettingList.add(eh);
						} catch (Exception err) {
							ErrorMsg.addErrorMessage(err);
						}
					}
					if (eh.getSettings() == null || eh.getSettings().isEmpty())
						noSettingList.add(eh);
				}
				if (!withSettingList.isEmpty() && !noSettingList.isEmpty()) {
					// chance to add settings to newer or older datasets without settings
					ArrayList<Object> params = new ArrayList<Object>();
					
					JComboBox hasSettings = new JComboBox();
					for (ExperimentHeaderInterface withS : withSettingList)
						hasSettings.addItem(new ExperimentHeaderWithCustomToString(withS));
					
					params.add("Copy Source (select 1 of " + withSettingList.size() + ")");
					params.add(hasSettings);
					final HashSet<ExperimentHeaderInterface> assignList = new HashSet<ExperimentHeaderInterface>();
					final HashSet<ExperimentHeaderInterface> analyzeList = new HashSet<ExperimentHeaderInterface>();
					for (final ExperimentHeaderInterface noS : noSettingList) {
						final JCheckBox analyzeExperiment = new JCheckBox(new AbstractAction("Analyze experiment"
								+ (enableRemoteTaskExecution ? "" : " (submission of analysis jobs disabled)")) {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (analyzeList.contains(noS)) {
									analyzeList.remove(noS);
								} else
									analyzeList.add(noS);
							}
						});
						analyzeExperiment.setEnabled(false);
						JCheckBox applySettings = new JCheckBox(new AbstractAction("Assign settings from source") {
							@Override
							public void actionPerformed(ActionEvent e) {
								if (assignList.contains(noS)) {
									assignList.remove(noS);
									analyzeExperiment.setSelected(false);
									analyzeExperiment.setEnabled(false);
									analyzeList.remove(noS);
								} else {
									assignList.add(noS);
									if (enableRemoteTaskExecution)
										analyzeExperiment.setEnabled(true);
								}
							}
						});
						params.add("<html><b>" + noS.getExperimentName() + "</b><br><small>(source: " + noS.getOriginDbId() + ")");
						params.add(
								TableLayout.getSplit(
										TableLayout.getSplitVertical(applySettings, analyzeExperiment, TableLayout.PREFERRED, TableLayout.PREFERRED),
										new JLabel(""), TableLayout.PREFERRED, TableLayout.PREFERRED));
					}
					Object[] userInp = MyInputHelper.getInput(
							"<html>" +
									"A set of experiments (" + (withSettingList.size() + noSettingList.size()) +
									") with common configuration names<br>" +
									"and at least one configuration source has been found.<br>" +
									"(this dialog will pop-up for each identified configuration-set)<br>" +
									"<br>" +
									"The current set of equal setting names is listed here:<br>" +
									"<br>" +
									"<table border='1'><tr>" +
									"<th>VIS</th>" +
									"<th>FLUO</th>" +
									"<th>NIR</th>" +
									"<th>IR</th>" +
									"<th>Other</th></tr>" +
									"<tr>" + StringManipulationTools.stringReplace(conf, ", ", "<br>") + "</tr></table>" +
									"<br><br>" +
									"There is the chance to apply the settings from any of the " + withSettingList.size() + " experiments,<br>" +
									"which have analysis settings applied to " + noSettingList.size() + " experiments with no settings:<br><br>",
							"Apply settings to similar experiments?",
							params.toArray());
					
					ArrayList<String> withSettingsNames = new ArrayList<String>();
					for (ExperimentHeaderInterface withS : withSettingList)
						withSettingsNames.add(withS.getExperimentName());
					ArrayList<String> noSettingsNames = new ArrayList<String>();
					for (ExperimentHeaderInterface noS : noSettingList)
						noSettingsNames.add(noS.getExperimentName());
					res.append("<br>A set of experiments (" + (withSettingList.size() + noSettingList.size()) +
							") with common configuration names and at least one possible configuration source has been found:" +
							"<br>" +
							"<table border='1'><tr>" +
							"<th>VIS</th>" +
							"<th>FLUO</th>" +
							"<th>NIR</th>" +
							"<th>IR</th>" +
							"<th>Other</th></tr>" +
							"<tr>" + StringManipulationTools.stringReplace(conf, ", ", "<br>") + "</tr></table><br>" +
							"Experiments with applied analysis settings: " +
							StringManipulationTools.getStringList(withSettingsNames, ", ") + "<br><br>" +
							"Experiments with no settings: " + StringManipulationTools.getStringList(noSettingsNames, ", ") + "<br><br>");
					if (userInp != null) {
						res.append("<br><b>According to user-action these experiments have been processed as following:</b><br><br>");
						ExperimentHeaderWithCustomToString copySource = (ExperimentHeaderWithCustomToString) hasSettings.getSelectedItem();
						for (ExperimentHeaderInterface noS : noSettingList) {
							res.append("Experiment " + noS.getExperimentName() + ":<br>");
							if (assignList.contains(noS)) {
								try {
									// apply
									String template = copySource.getHeader().getSettings();
									if (template == null || template.isEmpty())
										throw new Exception("Internal Error: tried to use empty source-settings.");
									else {
										noS.setSettings(template);
										m.saveExperimentHeader(noS);
									}
									res.append("&nbsp;&nbsp;&nbsp;Applied settings from " + copySource + ".<br>");
									if (!enableRemoteTaskExecution) {
										res.append("&nbsp;&nbsp;&nbsp;(submission of analysis jobs is disabled within the application settings)<br>");
									} else {
										try {
											// submit
											ExperimentReference er = new ExperimentReference(noS, m);
											String settings = noS.getSettings();
											if (settings != null && !settings.trim().isEmpty()) {
												PipelineDesc pd = new PipelineDesc(null, er.getIniIoProvider(), null, null, null);
												ActionPerformGridAnalysis ga = new ActionPerformGridAnalysis(pd, m, er);
												RemoteExecutionWrapperAction ra = new RemoteExecutionWrapperAction(ga, null);
												Date newestImportDate = null;
												String databaseIdOfNewestResultData = null;
												ra.setNewestAvailableData(newestImportDate, databaseIdOfNewestResultData);
												ra.performActionCalculateResults(null);
												res.append("&nbsp;&nbsp;&nbsp;Submitted analysis jobs.<br>");
											} else {
												res.append("&nbsp;&nbsp;&nbsp;Internal error: analysis settings are empty!<br>");
											}
										} catch (Exception e2) {
											e2.printStackTrace();
											res.append("&nbsp;&nbsp;&nbsp;Tried to submitt analysis jobs. Error: " + e2.getMessage() + "<br>");
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
									res.append("&nbsp;&nbsp;&nbsp;Tried to apply settings from " + copySource + ". Error: " + e.getMessage() + "<br>");
								}
							}
							res.append("<br>");
						}
						res.append("<br><br><hr>");
					} else {
						res.append("<br><b>According to user-action these experiments have not been further processed.</b><br><br><hr>");
					}
				}
			}
		} finally {
			if (!errors.isEmpty()) {
				res.append("<h2>Processing Errors (" + errors.size() + ")</h2>" + StringManipulationTools.getStringList(errors, "<br>"));
			}
		}
		status.setCurrentStatusText1("Processing finished");
		status.setCurrentStatusText2("");
	}
	
	private void getConfigSets(final TreeMap<String, ArrayList<ExperimentHeaderInterface>> config2headers) {
		for (ExperimentHeaderInterface eh : ExperimentHeaderService.filterNewest(experiments)) {
			if (eh.getImportusergroup() != null && !eh.getImportusergroup().equalsIgnoreCase("ANALYSIS RESULTS")
					&& !eh.getImportusergroup().equalsIgnoreCase("TEMP")) {
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
								case VisSide:
								case VisTop:
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
	public String getDefaultTitle() {
		return "Copy Analysis Settings";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Restore-64.png";
	}
	
}
