/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.ErrorMsg;
import org.SystemAnalysis;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class CloudTaskManager {
	
	private String hostName;
	
	private boolean process = false;
	
	LinkedHashSet<TaskDescription> runningTasks = new LinkedHashSet<TaskDescription>();
	
	public CloudTaskManager() {
	}
	
	Thread timerThread;
	private MongoDB m;
	
	private boolean autoClose;
	
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
	
	public void startWork(MongoDB m) {
		this.m = m;
		setProcess(true);
	}
	
	public void stopWork() {
		this.m = null;
		setProcess(false);
	}
	
	private void run() throws Exception {
		long startTime = System.currentTimeMillis();
		try {
			double progressSum = -1;
			do {
				if (CloudTaskManager.this.process) {
					ArrayList<TaskDescription> commands_to_start = new ArrayList<TaskDescription>();
					
					m.batchPingHost(hostName,
							BlockPipeline.getBlockExecutionsWithinLastMinute(),
							BlockPipeline.getPipelineExecutionsWithinCurrentHour(),
							BackgroundThreadDispatcher.getTaskExecutionsWithinLastMinute(),
							progressSum);
					
					int maxTasks = SystemAnalysis.getNumberOfCPUs() >= 4 ? 4 : 2;
					if (maxTasks < 1)
						maxTasks = 1;
					
					if (runningTasks.size() < maxTasks) {
						if (m == null)
							return;
						for (BatchCmd batch : m.batchGetWorkTasksScheduledForStart(maxTasks - runningTasks.size())) {
							if (batch.getExperimentMongoID() != null) {
								ExperimentHeaderInterface header = m.getExperimentHeader(batch.getExperimentMongoID());
								TaskDescription task = new TaskDescription(batch, new ExperimentReference(header), hostName);
								commands_to_start.add(task);
							}
						}
					}
					
					int nn = 0;
					progressSum = 0;
					ArrayList<TaskDescription> del = new ArrayList<TaskDescription>();
					// System.out.println("RUNNING: " + runningTasks.size());
					for (TaskDescription td : runningTasks) {
						if (td.analysisFinished()) {
							td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.FINISHED);
							del.add(td);
						} else {
							td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.IN_PROGRESS);
							progressSum += td.getBatchCmd().getCurrentStatusValueFine();
							nn++;
						}
						progressSum += td.getBatchCmd().getCurrentStatusValueFine();
						nn++;
					}
					if (del.size() > 0)
						runningTasks.removeAll(del);
					
					for (TaskDescription td : commands_to_start) {
						if (!runningTasks.contains(td)) {
							try {
								td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.IN_PROGRESS);
								runningTasks.add(td);
								td.setSystemExitAfterCompletion(autoClose);
								td.startWork(td.getBatchCmd(), hostName, hostName, m);
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
					}
					if (nn == 0)
						progressSum = -1;
					else
						progressSum /= (nn);
				} else {
					System.out.println(SystemAnalysisExt.getCurrentTime() + "> Cloud Task Manager: Processing Disabled // " + SystemAnalysisExt.getCurrentTime());
				}
				Thread.sleep(1000);
				if (autoClose && System.currentTimeMillis() - startTime > 10000) {
					if (runningTasks.isEmpty()) {
						System.out.println(SystemAnalysisExt.getCurrentTime() + "> Cluster Execution Mode is active // NO RUNNING TASK");
						System.out.println(SystemAnalysisExt.getCurrentTime() + "> SYSTEM.EXIT");
						System.exit(0);
					}
				} // else
					// System.out.println("> Cloud Task Manager: Running Tasks: " + runningTasks.size() + " // " + SystemAnalysisExt.getCurrentTime());
			} while (true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setClusterExecutionModeSingleTaskAndExit(boolean autoClose) {
		this.autoClose = autoClose;
		IAPservice.setCloudExecutionMode(autoClose);
	}
}
