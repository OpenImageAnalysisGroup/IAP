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
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operations.blocks.BlockPipeline;
import de.ipk.ag_ba.mongo.Batch;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author klukas
 */
public class CloudTaskManager {
	
	public static boolean disableWatchDog;
	
	private String hostName;
	
	private boolean process = false;
	
	LinkedHashSet<TaskDescription> runningTasks = new LinkedHashSet<TaskDescription>();
	
	public CloudTaskManager() {
		
	}
	
	Thread timerThread;
	private MongoDB m;
	
	private boolean fixedDisableProcess;
	
	void setProcess(boolean process) {
		this.process = process;
		if (timerThread == null) {
			timerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						CloudTaskManager.this.run();
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Error executing cloud task manager.", e);
						ErrorMsg.addErrorMessage(e);
					}
				}
			});
			timerThread.setName("Compute Grid Job Execution Manager (" + (isProcess() ? "active" : "inactive") + ")");
			timerThread.start();
		}
		timerThread.setName("Compute Grid Job Execution Manager (" + (isProcess() ? "active" : "inactive") + ")");
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
		setProcess(false);
		for (TaskDescription rt : runningTasks) {
			rt.getBatchCmd().getStatusProvider().pleaseStop();
		}
		this.m = null;
	}
	
	private void run() throws Exception {
		if (IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE) {
			installWatchDog();
		}
		
		BackgroundTaskStatusProviderSupportingExternalCallImpl status3provider = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> progress = new ArrayList<String>();
		ThreadSafeOptions progressSum = new ThreadSafeOptions();
		progressSum.setDouble(-1);
		
		Runnable ping = () -> {
			try {
				String l1 = status3provider.getCurrentStatusMessage1();
				if (l1 == null)
					l1 = "";
				String l2 = status3provider.getCurrentStatusMessage2();
				if (l2 == null)
					l2 = "";
				String l3 = status3provider.getCurrentStatusMessage3();
				if (l3 == null)
					l3 = "";
				if (!l2.isEmpty()) {
					if (!l1.isEmpty())
						l1 = l1 + "<br>";
					l1 = l1 + l2;
				}
				if (!l3.isEmpty()) {
					if (!l1.isEmpty())
						l1 = l1 + "<br>";
					l1 = l1 + l3;
				}
				
				Batch.pingHost(m, hostName,
						BlockPipeline.getBlockExecutionsWithinLastMinute(),
						BlockPipeline.getPipelineExecutionsWithinCurrentHour(),
						BackgroundThreadDispatcher.getTaskExecutionsWithinLastMinute(),
						progressSum.getDouble(),
						(runningTasks.isEmpty() && !l1.isEmpty() ? l1 + "<br>"
								: "") +
								(CloudTaskManager.this.process ? "" : "(processing disabled)<br>") +
								(names.size() > 0 ? "Process: " + StringManipulationTools.getStringList(names, ", ") + // "<br>" +
										"" + StringManipulationTools.getStringList(progress, ", ") +
										(progress.size() > 1 ? "<br>" +
												status3provider.getCurrentStatusMessage3() : "")
										: "(no task)"));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		};
		
		long startTime = System.currentTimeMillis();
		boolean disallownewtasks = false; // in batch mode only one task is allowed to be started
		try {
			do {
				names.clear();
				progress.clear();
				try {
					for (TaskDescription td : runningTasks) {
						String name;
						if (td.getBatchCmd() == null) {
							name = "[BatchCmd==null]";
							progress.add("[batch command n/a]");
						} else {
							name = td.getBatchCmd().getExperimentHeader().getExperimentName() + " (" + (td.getBatchCmd().getPartIdx() + 1) + "/"
									+ td.getBatchCmd().getPartCnt() + ")";
							progress.add(td.getBatchCmd().getCurrentStatusMessage3());
						}
						names.add(name);
					}
				} catch (Exception e) {
					MongoDB.saveSystemErrorMessage("Error processing running tasks.", e);
				}
				
				try {
					if (this.process)
						ping.run();
				} catch (Exception e) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Pinging host '" + hostName + "' produced an error: " + e.getMessage());
				}
				
				if (CloudTaskManager.this.process || fixedDisableProcess) {
					ArrayList<TaskDescription> commands_to_start = new ArrayList<TaskDescription>();
					
					status3provider.setCurrentStatusValueFine(progressSum.getDouble());
					
					int maxTasks = SystemOptions.getInstance().getInteger("IAP", "Max-Concurrent-Phenotyping-Tasks", 1);
					if (maxTasks < 1)
						maxTasks = 1;
					int cpuDesire = 0;
					for (TaskDescription t : runningTasks) {
						if (t.getBatchCmd() != null) {
							int tu = t.getBatchCmd().getCpuTargetUtilization();
							if (cpuDesire < Integer.MAX_VALUE) {
								if (tu == Integer.MAX_VALUE)
									cpuDesire = Integer.MAX_VALUE;
								else
									cpuDesire += tu;
							}
						}
					}
					if (!fixedDisableProcess && cpuDesire < maxTasks && !disallownewtasks) {
						if (m == null)
							return;
						if (runningTasks.isEmpty()) {
							Thread.sleep(5000);
							Batch.checkForMergePosibility(m, status3provider, ping);
						}
						
						for (BatchCmd batch : m.batch().getScheduledForStart(maxTasks - cpuDesire)) {
							ExperimentHeaderInterface header = batch.getExperimentHeader();
							if (header != null) {
								TaskDescription task = new TaskDescription(batch, new ExperimentReference(header, m), hostName);
								task.allowSystemExit = maxTasks < 2;
								int tu = batch.getCpuTargetUtilization();
								boolean stop = false;
								if (cpuDesire < Integer.MAX_VALUE) {
									if (tu == Integer.MAX_VALUE) {
										cpuDesire = Integer.MAX_VALUE;
										stop = true;
									} else
										cpuDesire += tu;
								} else
									stop = true;
								if (cpuDesire <= maxTasks || (runningTasks.isEmpty() && commands_to_start.isEmpty()))
									commands_to_start.add(task);
								if (stop)
									break;
							} else {
								// System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: INTERNAL ERROR: BATCH-CMD REFERS TO NULL-EXPERIMENT-ID");
							}
						}
					}
					if (!commands_to_start.isEmpty())
						System.out.println(SystemAnalysis.getCurrentTime() + ">TO BE STARTED: " + commands_to_start.size() +
								" (" + StringManipulationTools.getStringList(commands_to_start, ", ") + ")");
					
					int nn = 0;
					progressSum.setDouble(0);
					{
						ArrayList<TaskDescription> delTaskFromMongo = new ArrayList<TaskDescription>();
						ArrayList<TaskDescription> delTaskFromInternalQueue = new ArrayList<TaskDescription>();
						// System.out.println("RUNNING: " + runningTasks.size());
						for (TaskDescription td : runningTasks) {
							if (td.analysisFinishedComplete()) {
								td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.FINISHED);
								delTaskFromInternalQueue.add(td);
								delTaskFromMongo.add(td);
							} else
								if (td.analysisFinishedIncomplete()) {
									td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.FINISHED_INCOMPLETE);
									delTaskFromInternalQueue.add(td);
								} else {
									if (!td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.IN_PROGRESS))
										td.getBatchCmd().getStatusProvider().pleaseStop();
									progressSum.addDouble(td.getBatchCmd().getCurrentStatusValueFine());
									nn++;
								}
							progressSum.addDouble(td.getBatchCmd().getCurrentStatusValueFine());
							nn++;
						}
						if (delTaskFromInternalQueue.size() > 0)
							runningTasks.removeAll(delTaskFromInternalQueue);
						if (delTaskFromMongo.size() > 0) {
							for (TaskDescription finished : delTaskFromMongo) {
								finished.getBatchCmd().delete(m);
							}
						}
					}
					if (!disallownewtasks)
						for (TaskDescription td : commands_to_start) {
							if (!runningTasks.contains(td)) {
								try {
									if (td.getBatchCmd().updateRunningStatus(m, CloudAnalysisStatus.IN_PROGRESS)) {
										runningTasks.add(td);
										if (IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE)
											disallownewtasks = true;
										final TaskDescription tdf = td;
										Runnable r = new Runnable() {
											@Override
											public void run() {
												try {
													tdf.startWork(tdf.getBatchCmd(), hostName, hostName, m);
												} catch (Exception e) {
													e.printStackTrace();
													MongoDB.saveSystemErrorMessage("Error executing analysis batch task.", e);
												}
											}
										};
										Thread t = new Thread(r, td.getBatchCmd().getRemoteCapableAnalysisActionClassName());
										t.setPriority(Thread.MIN_PRIORITY);
										t.start();
									}
								} catch (Exception e) {
									System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: BATCH-CMD COULD NOT BE STARTED: " + e.getMessage());
									MongoDB.saveSystemErrorMessage("Could not start batch-cmd.", e);
								}
							} else {
								System.out.println(SystemAnalysis.getCurrentTime()
										+ ">INFO: INTERNAL INFO: runningTasks already contains a cmd which was sheduled for start");
							}
						}
					if (nn == 0)
						progressSum.setDouble(-1);
					else
						progressSum.setDouble(progressSum.getDouble() / nn);
				} else {
					// System.out.println(SystemAnalysis.getCurrentTime() + "> Cloud Task Manager: Processing Disabled // " + SystemAnalysis.getCurrentTime());
				}
				Thread.sleep(5000);
				if (IAPmain.getRunMode() == IAPrunMode.CLOUD_HOST_BATCH_MODE && System.currentTimeMillis() - startTime > 1000 * 60 * 10) {
					if (runningTasks.isEmpty() && System.currentTimeMillis() - BlockPipeline.getLastBlockUpdateTime() > 1 * 60 * 1000) {
						Batch.pingHost(m, hostName, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.NaN, "system.exit");
						System.out.println(SystemAnalysis.getCurrentTime() + ">Cluster Execution Mode is active // NO RUNNING TASK");
						System.out.println(SystemAnalysis.getCurrentTime() + ">SYSTEM.EXIT");
						Thread.sleep(9000);
						SystemAnalysis.exit(0);
					}
				} // else
					// System.out.println("> Cloud Task Manager: Running Tasks: " + runningTasks.size() + " // " + SystemAnalysisExt.getCurrentTime());
			} while (true);
		} catch (InterruptedException e) {
			MongoDB.saveSystemErrorMessage("Cloud task manager interrupted exception.", e);
		}
	}
	
	private void installWatchDog() {
		Runnable r = new Runnable() {
			long lastN = -1;
			long lastNt = -1;
			
			@Override
			public void run() {
				try {
					while (true) {
						if (lastNt < 0)
							lastNt = System.currentTimeMillis();
						long nowBE = BlockPipeline.getBlockExecutionsOverall();
						if (nowBE > lastN) {
							lastNt = System.currentTimeMillis();
							lastN = nowBE;
						}
						if (!CloudTaskManager.disableWatchDog)
							if (lastN == nowBE && lastN > 0 && System.currentTimeMillis() - lastNt > 30 * 60 * 1000) {
								Runnable r = new Runnable() {
									@Override
									public void run() {
										try {
											MongoDB.saveSystemErrorMessage("Cluster Execution Mode is active // NO BLOCK EXECUTION WITHIN 30 MIN // SYSTEM.EXIT", null);
											Batch.pingHost(m, hostName, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.NaN,
													"system.exit (no block execution within 30 min)");
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								};
								System.out.println(SystemAnalysis.getCurrentTime() + ">Cluster Execution Mode is active // NO BLOCK EXECUTION WITHIN 30 MIN");
								System.out.println(SystemAnalysis.getCurrentTime() + ">SYSTEM.EXIT");
								Thread msg = new Thread(r);
								msg.setDaemon(true);
								msg.start();
								long startMS = System.currentTimeMillis();
								long l = 0;
								do {
									l++;
									if (l == 1000) {
										l = 0;
									}
									Thread.sleep(10);
								} while (System.currentTimeMillis() - startMS < 9000);
								SystemAnalysis.exit(0);
							}
						Thread.sleep(60 * 1000); // every minute
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(r, "WATCH-DOG KILL TIMER").start();
	}
	
	public void setDisableProcess(boolean fixedDisableProcess) {
		this.fixedDisableProcess = fixedDisableProcess;
	}
	
	public boolean isDisableProcess() {
		return fixedDisableProcess;
	}
	
	public static void enableWatchDog() {
		BlockPipeline.ping();
		disableWatchDog = false;
	}
}
