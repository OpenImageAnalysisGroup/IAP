/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.ArrayList;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 */
public class TaskDescription {
	
	private final BatchCmd cmd;
	
	private String analysisActionClassName, params;
	private long startTime;
	
	private boolean finished = false;
	
	private ExperimentReference experimentInput;
	
	private final String systemIP;
	
	public static String getTaskDescriptionText(String hostIPs, String task, String params, String startTime) {
		return task + "$" + params + "$" + startTime;
	}
	
	public TaskDescription(BatchCmd cmd, ExperimentReference experiment, String systemIP) {
		this.cmd = cmd;
		this.systemIP = systemIP;
		// ip $ analysisAction.className $ params $ unique key (time stamp)
		try {
			analysisActionClassName = cmd.getRemoteCapableAnalysisActionClassName();
			params = cmd.getRemoteCapableAnalysisActionParams();
			this.experimentInput = experiment;
			startTime = cmd.getSubmissionTime();
		} catch (Exception e) {
			System.out.println("Invalid task-description: " + cmd);
			analysisActionClassName = null;
			params = null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		return cmd.equals(((TaskDescription) obj).cmd);
	}
	
	public boolean isValid() {
		return cmd != null && (cmd.getTargetIPs().isEmpty() || cmd.getTargetIPs().contains(systemIP));
	}
	
	public void startWork(final BatchCmd batch, String hostName, String ip, final MongoDB m)
						throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		final RemoteCapableAnalysisAction action = RemoteAnalysisRepository.getInstance().getNewAnalysisAction(
							analysisActionClassName);
		action.setParams(experimentInput, m, params);
		
		final BackgroundTaskStatusProvider statusProvider = action.getStatusProvider();
		
		batch.setStatusProvider(statusProvider);
		
		RunnableWithMappingData resultReceiver = new RunnableWithMappingData() {
			private ExperimentInterface experiment;
			
			@Override
			public void run() {
				// store dataset in mongo
				// System.out.println(experiment.toString());
				experiment.getHeader().setExperimentname(
									cmd.getRemoteCapableAnalysisActionClassName() + "§" + batch.getPartIdx() + "§" + batch.getPartCnt() + "§" + experiment.getName());
				experiment.getHeader().setImportusergroup("Temp");
				System.out.println("Received result: " + experiment.getName());
				try {
					if (m.batchGetCommand(batch).getOwner().equals(SystemAnalysis.getHostName())) {
						m.batchClearJob(batch);
						m.saveExperiment(experiment, null);
						ArrayList<ExperimentHeaderInterface> knownResults = new ArrayList<ExperimentHeaderInterface>();
						for (ExperimentHeaderInterface i : m.getExperimentList()) {
							if (i.getExperimentname() != null && i.getExperimentname().contains("§")) {
								String c = i.getExperimentname().substring(0, i.getExperimentname().indexOf("§"));
								if (c.equals(cmd.getRemoteCapableAnalysisActionClassName()))
									knownResults.add(i);
							}
						}
						System.out.println("TODO: " + batch.getPartCnt() + ", FINISHED: " + knownResults.size());
						if (knownResults.size() == batch.getPartCnt()) {
							System.out.println("*****************************");
							System.out.println("MERGE RESULTS:");
							Experiment e = new Experiment();
							for (ExperimentHeaderInterface i : knownResults) {
								ExperimentInterface ei = m.getExperiment(i);
								e.addAll(ei);
								m.deleteExperiment(i.getExcelfileid());
								System.out.println("Measurements: " + Experiment.getNumberOfMeasurementValues(ei));
								System.out.println("*****************************");
							}
							String sn = cmd.getRemoteCapableAnalysisActionClassName();
							if (sn.indexOf(".") > 0)
								sn = sn.substring(sn.lastIndexOf(".") + 1);
							e.getHeader().setExperimentname(sn + ": " + experimentInput.getExperimentName());
							e.getHeader().setImportusergroup("Cloud Analysis Results");
							e.getHeader().setExcelfileid("");
							for (SubstanceInterface si : e) {
								for (ConditionInterface ci : si) {
									ci.setExperimentName(e.getHeader().getExperimentname());
								}
							}
							System.out.println("*****************************");
							System.out.println("Merged Experiment: " + e.getName());
							System.out.println("Merged Measurements: " + Experiment.getNumberOfMeasurementValues(e));
							m.saveExperiment(e, new BackgroundTaskConsoleLogger("", "", true));
						}
					} else
						System.out.println("Information: Batch command, processed by " + SystemAnalysis.getHostName()
								+ " has been claimed by " + batch.getOwner()
								+ ". Therefore analysis result is not saved.");
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				finished = true;
			}
			
			@Override
			public void setExperimenData(ExperimentInterface experiment) {
				this.experiment = experiment;
			}
		};
		action.setWorkingSet(cmd.getPartIdx(), cmd.getPartCnt(), resultReceiver);
		BackgroundTaskHelper.issueSimpleTask("Batch: " + analysisActionClassName + " (start: " + startTime + ")",
							"Initializing", new Runnable() {
								@Override
								public void run() {
									try {
										action.performActionCalculateResults(null);
									} catch (Exception e) {
										ErrorMsg.addErrorMessage(e);
									}
								}
							}, null, statusProvider, 0);
	}
	
	public boolean analysisFinished() {
		return finished;
	}
	
	public BatchCmd getBatchCmd() {
		return cmd;
	}
}
