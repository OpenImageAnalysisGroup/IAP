/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
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
 */
public class CloudTaskManager {

	private String hostName;
	private String ip;

	private boolean process = false;

	LinkedHashSet<TaskDescription> runningTasks = new LinkedHashSet<TaskDescription>();

	public CloudTaskManager() {
	}

	Thread timerThread;
	private MongoDB m;

	private void setProcess(boolean process) {
		this.process = process;
		if (timerThread == null) {
			timerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						CloudTaskManager.this.run();
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
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

	public void startWork(MongoDB m) {
		this.m = m;
		setProcess(true);
	}

	public void stopWork() {
		this.m = null;
		setProcess(false);
	}

	private void run() throws Exception {
		try {
			int idx_loop = 0;
			do {
				if (CloudTaskManager.this.process) {
					ArrayList<TaskDescription> commands_to_start = new ArrayList<TaskDescription>();
					long maxDelaySinceLastUpdate = 5000;
					m.batchPingHost(ip);
					for (BatchCmd batch : m.batchGetCommands(maxDelaySinceLastUpdate)) {
						if (batch.getExperimentMongoID() != null && batch.getTargetIPs().contains(ip)) {
							m.batchClaim(batch, CloudAnalysisStatus.STARTING);
							break;
						}
					}
					for (BatchCmd batch : m.batchGetWorkTasksScheduledForStart()) {
						if (batch.getTargetIPs().contains(ip)) {
							if (batch.getExperimentMongoID() != null) {
								ExperimentHeaderInterface header = m.getExperimentHeader(batch.getExperimentMongoID());
								TaskDescription task = new TaskDescription(batch, new ExperimentReference(header), ip);
								commands_to_start.add(task);
								break;
							}
						}
					}
					ArrayList<TaskDescription> del = new ArrayList<TaskDescription>();
					for (TaskDescription td : runningTasks) {
						if (td.analysisFinished()) {
							td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.FINISHED);
							del.add(td);
						} else {
							td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.IN_PROGRESS);
						}
					}
					if (del.size() > 0)
						runningTasks.removeAll(del);

					for (TaskDescription td : commands_to_start) {
						if (!runningTasks.contains(td)) {
							try {
								idx_loop++;
								System.out.println("loop: " + idx_loop + ", start: " + td.toString() + ", status: " + td.getBatchCmd().getRunStatus());
								td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.IN_PROGRESS);
								runningTasks.add(td);
								td.startWork(td.getBatchCmd(), hostName, ip, m);
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
