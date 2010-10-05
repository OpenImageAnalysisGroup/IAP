/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package rmi_server.task_management;

import org.BackgroundTaskStatusProvider;
import org.ErrorMsg;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author klukas
 * 
 */
public class TaskDescription {

	private final String cmd;

	private String hostIPs, analysisActionClassName, params, startTime;

	private boolean finished = false;

	private ExperimentReference experimentInput;

	private final String systemIP;

	public static String getTaskDescriptionText(String hostIPs, String task, String params, String startTime) {
		return hostIPs + "$" + task + "$" + params + "$" + startTime;
	}

	public TaskDescription(String cmd, ExperimentReference experiment, String systemIP) {
		this.cmd = cmd;
		this.systemIP = systemIP;
		// ip $ analysisAction.className $ params $ unique key (time stamp)
		try {
			String[] values = cmd.split("\\$");
			if (values.length != 4)
				throw new Exception("Invalid task description");
			hostIPs = values[0];
			analysisActionClassName = values[1];
			params = values[2];
			startTime = values[3];
			this.experimentInput = experiment;
		} catch (Exception e) {
			System.out.println("Invalid task-description: " + cmd);
			hostIPs = null;
			analysisActionClassName = null;
			params = null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		return cmd.equals(((TaskDescription) obj).cmd);
	}

	public boolean isValid() {

		return hostIPs != null && hostIPs.contains(systemIP);
	}

	public void startWork(String hostName, String ip, final String login, final String pass) throws ClassCastException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		final RemoteCapableAnalysisAction action = RemoteAnalysisRepository.getInstance().getNewAnalysisAction(
				analysisActionClassName);
		action.setParams(experimentInput, login, pass, params);

		String[] ips = hostIPs.split(",");
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
					new MongoDB().storeExperiment("dbe3", null, login, pass, experiment, null);
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
}
