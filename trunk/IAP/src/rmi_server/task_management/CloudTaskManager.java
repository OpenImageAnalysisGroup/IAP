/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package rmi_server.task_management;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions.PhenotypeAnalysisAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_ba.mongo.MongoDB;
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
					try {
						String time = System.currentTimeMillis() + "";
						mainLoop: do {
							if (CloudTaskManager.this.process) {
								ArrayList<TaskDescription> commands = new ArrayList<TaskDescription>();
								for (ExperimentHeaderInterface eh : new MongoDB().getExperimentList()) {
									String sequence = eh.getSequence();
									if (sequence == null)
										sequence = "";
									if (eh.getExperimentType() != null && !eh.getExperimentType().contains("Trash")
											&& eh.getImportusername() != null && eh.getImportusername().equals("klukas")
											&& eh.getExperimentname().contains("Lermon") && !eh.getExperimentname().contains("§"))
										sequence += "§"
												+ TaskDescription.getTaskDescriptionText("194.94.140.224",
														PhenotypeAnalysisAction.class.getCanonicalName(), "", time);
									if (sequence.contains("§")) {
										String cmd = sequence.substring(sequence.lastIndexOf("§") + "§".length());
										if (cmd.length() > 0) {
											TaskDescription task = new TaskDescription(cmd, new ExperimentReference(eh), ip);
											if (task.isValid()) {
												commands.add(task);
											}
										}
									}
								}
								ArrayList<TaskDescription> del = new ArrayList<TaskDescription>();
								for (TaskDescription td : runningTasks) {
									if (td.analysisFinished()) {
										del.add(td);
									}
								}
								if (del.size() > 0)
									runningTasks.removeAll(del);
								for (TaskDescription td : commands) {
									if (!runningTasks.contains(td)) {
										try {
											runningTasks.add(td);
											td.startWork(hostName, ip, login, pass);
											break mainLoop;
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
}
