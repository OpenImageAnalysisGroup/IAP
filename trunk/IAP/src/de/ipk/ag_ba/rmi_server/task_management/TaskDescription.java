/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.Set;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
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
			startTime = cmd.getSubmissionTime();
			this.experimentInput = experiment;
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
		return cmd != null && cmd.getTargetIPs().contains(systemIP);
	}

	public void startWork(BatchCmd batch, String hostName, String ip, final String login, final String pass)
						throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		final RemoteCapableAnalysisAction action = RemoteAnalysisRepository.getInstance().getNewAnalysisAction(
							analysisActionClassName);
		action.setParams(experimentInput, login, pass, params);

		Set<String> ips = cmd.getTargetIPs();
		int thisHostID = 0;
		int numberOfHosts = 0;
		int ii = 0;
		for (String s : ips) {
			if (systemIP.equals(s)) {
				thisHostID = ii;
			}
			if (s.length() > 0)
				numberOfHosts++;
			ii++;
		}

		final int FthisHostID = thisHostID;
		final int FnumberOfHosts = numberOfHosts;

		final BackgroundTaskStatusProvider statusProvider = action.getStatusProvider();

		batch.setStatusProvider(statusProvider);

		RunnableWithMappingData resultReceiver = new RunnableWithMappingData() {
			private ExperimentInterface experiment;

			@Override
			public void run() {
				// store dataset in mongo
				System.out.println("Received result:");
				System.out.println(experiment.toString());
				experiment.getHeader().setExperimentname(
									cmd + "§" + FthisHostID + "§" + FnumberOfHosts + "§" + experiment.getName());
				try {
					// todo: only store result if batch claim is still valid and
					// connected to system IP
					new MongoDB().saveExperiment("dbe3", null, login, pass, experiment, null);
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
		action.setWorkingSet(thisHostID, numberOfHosts, resultReceiver);
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
