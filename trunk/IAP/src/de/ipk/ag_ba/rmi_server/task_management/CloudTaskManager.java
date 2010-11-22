/*******************************************************************************
 * 
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 * 
 */
public class CloudTaskManager {

	private String hostName;
	private String ip;
	private String login;
	private String pass;

	private boolean process = false;

	LinkedHashSet<TaskDescription> runningTasks = new LinkedHashSet<TaskDescription>();

	public CloudTaskManager() {
	}

	Thread timerThread;

	private void setProcess(boolean process) {
		this.process = process;
		if (timerThread == null) {
			timerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					CloudTaskManager.this.run();
				}
			});
			timerThread.setName("CloudTaskManager (" + (isProcess() ? "active" : "inactive") + ")");
			timerThread.start();
		}
		timerThread.setName("CloudTaskManager (" + (isProcess() ? "active" : "inactive") + ")");
	}

	private boolean isProcess() {
		return process;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void startWork(String login, String pass) {
		this.login = login;
		this.pass = pass;
		setProcess(true);
	}

	public void stopWork() {
		this.login = null;
		this.pass = null;
		setProcess(false);
	}

	private void run() {
		try {
			do {
				if (CloudTaskManager.this.process) {
					ArrayList<TaskDescription> commands = new ArrayList<TaskDescription>();
					long lastUpdate = 5000;
					new MongoDB().batchPingHost(ip);
					for (BatchCmd batch : new MongoDB().batchGetCommands(lastUpdate)) {
						if (batch.getTargetIPs().contains(ip)) {
							new MongoDB().batchClaim(batch, ip, CloudAnalysisStatus.STARTING);
						}
					}
					for (BatchCmd batch : new MongoDB().batchGetWorkTasksScheduledForStart()) {
						if (batch.getTargetIPs().contains(ip)) {
							ExperimentHeaderInterface header = new MongoDB().getExperimentHeader(batch.getExperimentMongoID());
							TaskDescription task = new TaskDescription(batch, new ExperimentReference(header), ip);
							commands.add(task);
						}
					}
					ArrayList<TaskDescription> del = new ArrayList<TaskDescription>();
					for (TaskDescription td : runningTasks) {
						if (td.analysisFinished()) {
							td.getBatchCmd().updateRunningStatus(CloudAnalysisStatus.FINISHED);
							del.add(td);
						} else {
							td.getBatchCmd().updateRunningStatus(CloudAnalysisStatus.IN_PROGRESS);
						}
					}
					if (del.size() > 0)
						runningTasks.removeAll(del);

					for (TaskDescription td : commands) {
						if (!runningTasks.contains(td)) {
							try {
								runningTasks.add(td);
								td.startWork(td.getBatchCmd(), hostName, ip, login, pass);
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
					}
				}
				Thread.sleep(1000);
			} while (true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
