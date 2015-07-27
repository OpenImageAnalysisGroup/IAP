package de.ipk.ag_ba.commands.mongodb;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import org.ErrorMsg;
import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.ActionPerformGridAnalysis;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.navigation_model.RemoteExecutionWrapperAction;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;

/**
 * @author Christian Klukas
 */
public class ActionPerformMassAnalysis extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentHeaderInterface> experiments;
	
	private final ArrayList<String> errors = new ArrayList<String>();
	private StringBuilder res;
	
	public ActionPerformMassAnalysis(String tooltip) {
		super(tooltip);
	}
	
	public ActionPerformMassAnalysis(MongoDB m, ArrayList<ExperimentHeaderInterface> experiments) {
		this("<html>" +
				"Submit multiple analysis tasks.");
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
			ArrayList<ExperimentHeaderInterface> experimentHeaders = new ArrayList<ExperimentHeaderInterface>();
			getConfigSets(experimentHeaders);
			Collections.sort(experimentHeaders, new Comparator<ExperimentHeaderInterface>() {
				@Override
				public int compare(ExperimentHeaderInterface o1, ExperimentHeaderInterface o2) {
					return o1.getExperimentName().compareTo(o2.getExperimentName());
				}
			});
			ArrayList<ExperimentHeaderInterface> withSettingList = new ArrayList<ExperimentHeaderInterface>();
			for (ExperimentHeaderInterface eh : experimentHeaders) {
				if (eh.getImportdate() != null && eh.getSettings() != null && !eh.getSettings().isEmpty()) {
					try {
						ExperimentReferenceInterface er = new ExperimentReference(eh, m);
						IniIoProvider iniIO = er.getIniIoProvider();
						SystemOptions so = SystemOptions.getInstance(null, iniIO);
						String vv = so.getString("DESCRIPTION", "tuned_for_IAP_version", "(unknown legacy IAP version)");
						boolean vok = vv != null && vv.length() >= 3 && ReleaseInfo.IAP_VERSION_STRING.substring(0, 2).equals(vv.substring(0, 2));
						if (vok)
							withSettingList.add(eh);
					} catch (Exception err) {
						ErrorMsg.addErrorMessage(err);
					}
				}
			}
			if (!withSettingList.isEmpty()) {
				// chance to add settings to newer or older datasets without settings
				ArrayList<Object> params = new ArrayList<Object>();
				
				final HashSet<ExperimentHeaderInterface> analyzeList = new HashSet<ExperimentHeaderInterface>();
				for (final ExperimentHeaderInterface noS : withSettingList) {
					ExperimentReferenceInterface er = new ExperimentReference(noS, m);
					IniIoProvider iniIO = er.getIniIoProvider();
					SystemOptions so = SystemOptions.getInstance(null, iniIO);
					String vv = so.getString("DESCRIPTION", "tuned_for_IAP_version", "(unknown legacy IAP version)");
					boolean vok = ReleaseInfo.IAP_VERSION_STRING.substring(0, 2).equals(vv.substring(0, 2));
					
					final JCheckBox analyzeExperiment = new JCheckBox(new AbstractAction("Submit analysis jobs, pipeline tuned for IAP " + vv
							+ (vok ? "" : "(not tuned for IAP " + ReleaseInfo.IAP_VERSION_STRING + ")")
							+ (enableRemoteTaskExecution ? "" : " (submission of analysis jobs disabled)")) {
						@Override
						public void actionPerformed(ActionEvent e) {
							if (analyzeList.contains(noS)) {
								analyzeList.remove(noS);
							} else
								analyzeList.add(noS);
						}
					});
					params.add(noS.getExperimentName());
					params.add(analyzeExperiment);
				}
				Object[] userInp = MyInputHelper.getInput(
						"<html>" +
								"A set of experiments analysis configurations<br>" +
								"has been found.<br>",
						"Analyze Experiments?",
						params.toArray());
				
				ArrayList<String> withSettingsNames = new ArrayList<String>();
				for (ExperimentHeaderInterface withS : withSettingList)
					withSettingsNames.add(withS.getExperimentName());
				
				res.append("<br>A set of experiments (" + withSettingList.size() +
						") with analysis settings has been found:" +
						StringManipulationTools.getStringList(withSettingsNames, ", ") + "<br><br>");
				if (userInp != null) {
					res.append("<br><b>According to user-action these experiments have been processed as following:</b><br><br>");
					for (ExperimentHeaderInterface noS : withSettingList) {
						res.append("Experiment " + noS.getExperimentName() + ":<br>");
						if (analyzeList.contains(noS)) {
							try {
								if (!enableRemoteTaskExecution) {
									res.append("&nbsp;&nbsp;&nbsp;(submission of analysis jobs is disabled within the application settings)<br>");
								} else {
									try {
										// submit
										ExperimentReferenceInterface er = new ExperimentReference(noS, m);
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
								res.append("&nbsp;&nbsp;&nbsp;Tried to submit analysis jobs for experiment " + noS.getExperimentName() + ". Error: "
										+ e.getMessage() + "<br>");
							}
						}
						res.append("<br>");
					}
					res.append("<br><br><hr>");
				} else {
					res.append("<br><b>According to user-action these experiments have not been further processed.</b><br><br><hr>");
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
	
	private void getConfigSets(final ArrayList<ExperimentHeaderInterface> config2headers) {
		for (ExperimentHeaderInterface eh : ExperimentHeaderService.filterNewest(experiments)) {
			if (eh.getImportusergroup() != null && !eh.getImportusergroup().equalsIgnoreCase("ANALYSIS RESULTS")
					&& !eh.getImportusergroup().equalsIgnoreCase("TEMP")) {
				try {
					status.setCurrentStatusText1("Submit analysis jobs for");
					status.setCurrentStatusText2(eh.getExperimentName());
					config2headers.add(eh);
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
		return "Analyse Multiple Experiments";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Restore-64.png";
	}
	
}
