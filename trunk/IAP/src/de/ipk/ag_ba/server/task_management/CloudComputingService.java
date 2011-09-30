/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import info.StopWatch;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.actions.Library;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.BackgroundTaskConsoleLogger;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.DataMappingTypeManager3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

/**
 * @author klukas
 */
public class CloudComputingService {
	
	static CloudComputingService instance = null;
	
	boolean active = false;
	
	private final CloudTaskManager cloudTaskManager;
	
	private CloudComputingService() {
		cloudTaskManager = new CloudTaskManager();
	}
	
	public synchronized static CloudComputingService getInstance() {
		if (instance == null)
			instance = new CloudComputingService();
		return instance;
	}
	
	private void checkStatus() {
		// todo
	}
	
	public String getStatusImageName() {
		checkStatus();
		if (active)
			return "img/ext/network-workgroup-power.png";
		else
			return "img/ext/network-workgroup.png";
	}
	
	public String getTaskNameEnableOrDisableActionText() {
		checkStatus();
		if (active)
			return "Shutdown This Node";
		else
			return "Join Compute Cloud";
	}
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		System.out.println("***************************************************");
		System.out.println("*                                                 *");
		System.out.println("*        IAP - Integrated Analysis Platform       *");
		System.out.println("*                                                 *");
		System.out.println("*      --  Systems Biology Cloud Computing --     *");
		System.out.println("*                                                 *");
		System.out.println("*     (c) 2010-2011 IPK, Group Image Analysis     *");
		System.out.println("*                                                 *");
		System.out.println("***************************************************");
		System.out.println("*                                                 *");
		System.out.println("*  PI: Dr. Christian Klukas ..................... *");
		System.out.println("*  Alexander Entzian ............................ *");
		System.out.println("*  Jean-Michel Pape ............................. *");
		System.out.println("*                                                 *");
		System.out.println("***************************************************");
		System.out.println(SystemAnalysisExt.getCurrentTime() + "> SYSTEM ANALYSIS");
		SystemAnalysis.simulateHeadless = true;
		boolean clusterExecutionMode = false;
		{
			CloudComputingService cc = CloudComputingService.getInstance();
			cc.setEnableCalculations(true);
		}
		System.out.println(SystemAnalysisExt.getCurrentTime() + ">DISABLE SUB-TASK MULTITHREADING");
		if (args.length > 0 && args[0].toLowerCase().startsWith("info")) {
			SystemInfoExt info = new SystemInfoExt();
			System.out.println("Sockets        : " + info.getCpuSockets());
			System.out.println("Cores p. sock. : " + info.getPhysicalCoresPerSocket());
			System.out.println("Physical Cores : " + info.getCpuPhysicalCores());
			System.out.println("Logical Cores  : " + info.getCpuLogicalCores());
			System.out.println("Log./phys. core: " + info.getHyperThreadingFactor());
			System.out.println("CPUs (avail.)  : " + info.getCpuCountAvailable());
			System.out.println("Phys. mem. (GB): " + info.getPhysicalMemoryInGB());
			System.out.println("System load    : " + StringManipulationTools.formatNumber(info.getLoad(), "#.#"));
			System.exit(0);
		} else
			if (args.length > 0 && args[0].toLowerCase().startsWith("close") ||
					(args.length > 1 && args[1].startsWith("close"))) {
				System.out.println(": close - auto-closing after finishing compute task - " + SystemAnalysis.getNumberOfCPUs());
				clusterExecutionMode = true;
			} else
				if (args.length > 0 && args[0].toLowerCase().contains("full")) {
					System.out.println(": full - enabling full CPU utilization - " + SystemAnalysis.getNumberOfCPUs());
					SystemAnalysis.setUseFullCpuPower(true);
				} else
					if (args.length > 0 && args[0].contains("half")) {
						SystemAnalysis.setUseHalfCpuPower(true);
						System.out.println(": half - enabling half CPU utilization - " + SystemAnalysis.getNumberOfCPUs());
					} else
						if (args.length > 0) {
							try {
								Integer i = Integer.parseInt(args[0]);
								System.out.println(": " + args[0] + " - using " + i + " CPUs");
								SystemAnalysis.setUseCpu(i);
							} catch (Exception e) {
								if ((args[0] + "").toLowerCase().equalsIgnoreCase("clear")) {
									try {
										MongoDB.getDefaultCloud().batchClearJobs();
										System.out.println(":clear - cleared scheduled jobs in database " + MongoDB.getDefaultCloud().getDatabaseName());
										return;
									} catch (Exception e1) {
										e1.printStackTrace();
									}
								} else
									if ((args[0] + "").toLowerCase().startsWith("perf")) {
										try {
											System.out.println(":perf - perform performance test (TestPipelineMaize Copy)");
											
											StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">INFO: LabCube construction", false);
											ImageOperation io = new ImageOperation(new int[][] { { 0, 0 } });
											if (io != null)
												s.printTime();
											
											StopWatch sw = new StopWatch("IAP performance test", false);
											PerformanceTest p = new PerformanceTest();
											p.testPipeline();
											System.out.println();
											sw.printTime();
											Thread.sleep(5 * 60 * 1000);
										} catch (Exception e1) {
											e1.printStackTrace();
										}
										System.exit(0);
									} else
										if ((args[0] + "").toLowerCase().startsWith("back") && !(args[0] + "").toLowerCase().startsWith("backup")) {
											BackupSupport sb = BackupSupport.getInstance();
											sb.makeBackup();
											System.exit(0);
										} else
											if ((args[0] + "").toLowerCase().startsWith("monitor")) {
												{
													CloudComputingService cc = CloudComputingService.getInstance();
													cc.setEnableCalculations(false);
												}
												
											} else
												if ((args[0] + "").toLowerCase().startsWith("backup")) {
													{
														CloudComputingService cc = CloudComputingService.getInstance();
														cc.setEnableCalculations(false);
													}
													
													BackupSupport sb = BackupSupport.getInstance();
													sb.scheduleBackup();
													sb.makeBackup();
												} else
													if ((args[0] + "").toLowerCase().startsWith("close")) {
														// ignore, has been processed at the start of this method
													} else {
														if ((args[0] + "").toLowerCase().equalsIgnoreCase("merge")) {
															merge();
															return;
														} else {
															System.out.println(": Valid command line parameters:");
															System.out.println("   'half'    - use half of the CPUs");
															System.out.println("   'full'    - use all of the CPUs");
															System.out.println("   'nnn'     - use specified number of CPUs");
															System.out.println("   'clear'   - clear scheduled tasks");
															System.out.println("   'merge'   - in case of error (merge interrupted previously), merge temporary results");
															System.out.println("   'perf'    - perform performance test");
															System.out.println("   'close'   - close after task completion (cluster execution mode)");
															System.out.println("   'info'    - Show CPU info");
															System.out.println("   'monitor' - Report system info to cloud (join, but don't perform calculations)");
															System.out.println("   'back'    - perform LemnaTec to HSM backup now");
															System.out.println("   'backup'  - perform LemnaTec to HSM backup now, and then every midnight");
														}
													}
							}
						}
		SystemInfoExt si = new SystemInfoExt();
		System.out.println("CPUs (sockets,physical,logical): " +
				si.getCpuSockets() + "," + si.getCpuPhysicalCores() + "," +
				si.getCpuLogicalCores() + ", using " + SystemAnalysis.getNumberOfCPUs());
		System.out.println("MEMORY: " + SystemAnalysisExt.getPhysicalMemoryInGB() + " GB, using " + SystemAnalysis.getMemoryMB() / 1024 + " GB");
		System.out.println(SystemAnalysisExt.getCurrentTime() + ">");
		System.out.println(SystemAnalysisExt.getCurrentTime() + "> INITIALIZE CLOUD TASK MANAGER (T=" + IAPservice.getCurrentTimeAsNiceString() + ")");
		
		StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">INFO: LabCube construction", false);
		ImageOperation io = new ImageOperation(new int[][] { { 0, 0 } });
		if (io != null)
			s.printTime();
		
		String hsm = IAPmain.getHSMfolder();
		if (hsm != null && new File(hsm).exists()) {
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">HSM Folder: " + hsm);
			Library lib = new Library();
			HsmFileSystemSource dataSourceHsm = new HsmFileSystemSource(lib, "HSM Archive", hsm,
					IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
					IAPmain.loadIcon("img/ext/folder-remote.png"));
		}
		// register extended hierarchy and loaded image loaders (and more)
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos()) {
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
			
			CloudComputingService cc = CloudComputingService.getInstance();
			cc.setClusterExecutionModeSingleTaskAndExit(clusterExecutionMode);
			cc.switchStatus(m);
			System.out.println("START CLOUD SERVICE FOR " + m.getPrimaryHandler().getPrefix());
		}
	}
	
	public void setEnableCalculations(boolean enableCloudComputing) {
		cloudTaskManager.setDisableProcess(!enableCloudComputing);
	}
	
	public void setClusterExecutionModeSingleTaskAndExit(boolean autoClose) {
		cloudTaskManager.setClusterExecutionModeSingleTaskAndExit(autoClose);
	}
	
	private static void merge() {
		try {
			DataMappingTypeManager3D.replaceVantedMappingTypeManager();
			
			final MongoDB m = MongoDB.getDefaultCloud();
			ArrayList<ExperimentHeaderInterface> el = m.getExperimentList(null);
			HashSet<TempDataSetDescription> availableTempDatasets = new HashSet<TempDataSetDescription>();
			HashSet<String> processedSubmissionTimes = new HashSet<String>();
			for (ExperimentHeaderInterface i : el) {
				String[] cc = i.getExperimentName().split("§");
				if (i.getImportusergroup() != null && i.getImportusergroup().equals("Temp") && cc.length == 4) {
					String className = cc[0];
					String idxCnt = cc[1];
					String partCnt = cc[2];
					String submTime = cc[3];
					if (!processedSubmissionTimes.contains(submTime))
						availableTempDatasets.add(new TempDataSetDescription(className, partCnt, submTime, i.getOriginDbId()));
					processedSubmissionTimes.add(submTime);
				}
			}
			for (TempDataSetDescription tempDataSetDescription : availableTempDatasets) {
				ArrayList<ExperimentHeaderInterface> knownResults = new ArrayList<ExperimentHeaderInterface>();
				HashSet<String> added = new HashSet<String>();
				for (ExperimentHeaderInterface i : el) {
					if (i.getExperimentName() != null && i.getExperimentName().contains("§")) {
						String[] cc = i.getExperimentName().split("§");
						if (i.getImportusergroup().equals("Temp") && cc.length == 4) {
							String className = cc[0];
							String partIdx = cc[1];
							String partCnt = cc[2];
							String submTime = cc[3];
							String bcn = tempDataSetDescription.getRemoteCapableAnalysisActionClassName();
							String bpn = tempDataSetDescription.getPartCnt();
							String bst = tempDataSetDescription.getSubmissionTime();
							if (className.equals(bcn)
									&& partCnt.equals(bpn)
									&& submTime.equals(bst)
									&& !added.contains(partIdx)) {
								knownResults.add(i);
								added.add(partIdx);
							}
						}
					}
				}
				System.out.println(SystemAnalysisExt.getCurrentTime() + "> T=" + IAPservice.getCurrentTimeAsNiceString());
				System.out.println(SystemAnalysisExt.getCurrentTime() + "> TODO: " + tempDataSetDescription.getPartCnt() + ", FINISHED: " + knownResults.size());
				if (knownResults.size() + 1 < tempDataSetDescription.getPartCntI()) {
					// not everything has been computed (internal error)
					TreeSet<Integer> jobIDs = new TreeSet<Integer>();
					{
						int idx = 0;
						while (idx < tempDataSetDescription.getPartCntI()) {
							if (!added.contains(idx + "")) {
								System.out.println("Missing: " + idx);
								jobIDs.add(idx++);
							} else
								idx++;
						}
					}
					for (int jobID : jobIDs) {
						BatchCmd cmd = new BatchCmd();
						cmd.setRunStatus(CloudAnalysisStatus.SCHEDULED);
						cmd.setSubmissionTime(tempDataSetDescription.getSubmissionTimeL());
						cmd.setTargetIPs(new HashSet<String>());
						cmd.setSubTaskInfo(jobID, tempDataSetDescription.getPartCntI());
						cmd.setRemoteCapableAnalysisActionClassName(tempDataSetDescription.getRemoteCapableAnalysisActionClassName());
						cmd.setRemoteCapableAnalysisActionParams("");
						cmd.setExperimentMongoID(tempDataSetDescription.getOriginDBid());
						BatchCmd.enqueueBatchCmd(MongoDB.getDefaultCloud(), cmd);
						System.out.println("Enqueue: " + jobID);
					}
				} else
					if (knownResults.size() + 1 >= tempDataSetDescription.getPartCntI()) {
						System.out.println("*****************************");
						System.out.println("MERGE INDEX: " + tempDataSetDescription.getPartCntI() + "/" + tempDataSetDescription.getPartCnt()
								+ ", RESULTS AVAILABLE: " + knownResults.size());
						final Experiment e = new Experiment();
						long tFinish = System.currentTimeMillis();
						final int wl = knownResults.size();
						final ThreadSafeOptions tso = new ThreadSafeOptions();
						final Runtime r = Runtime.getRuntime();
						ExecutorService es = Executors.newFixedThreadPool(2);
						HashMap<ExperimentHeaderInterface, String> experiment2id =
								new HashMap<ExperimentHeaderInterface, String>();
						for (ExperimentHeaderInterface ii : knownResults) {
							experiment2id.put(ii, ii.getDatabaseId());
							final ExperimentHeaderInterface i = ii;
							Runnable rr = new Runnable() {
								@Override
								public void run() {
									System.out.print(SystemAnalysisExt.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024
											/ 1024
											+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
									ExperimentInterface ei = m.getExperiment(i);
									String[] cc = i.getExperimentName().split("§");
									synchronized (tso) {
										tso.addInt(1);
										if (tso.getInt() != 1) {
											// weight_before, water_weight, water_sum
											ArrayList<SubstanceInterface> del = new ArrayList<SubstanceInterface>();
											for (SubstanceInterface si : ei) {
												if (si.getName().equals("weight_before") || si.getName().equals("water_weight") || si.getName().equals("water_sum"))
													del.add(si);
											}
											for (SubstanceInterface d : del)
												ei.remove(d);
										}
									}
									System.out.print(tso.getInt() + "/" + wl + " // dataset: " + cc[1] + "/" + cc[2]);
									// + ": "+ ei.getNumberOfMeasurementValues());
									// int mv;
									synchronized (e) {
										StopWatch s = new StopWatch(">e.addMerge");
										e.addAndMerge(ei);
										// mv = e.getNumberOfMeasurementValues();
										s.printTime();
										// ExperimentHeaderInterface t = ei.getHeader();
										// ei.clear();
										// ei.setHeader(t);
									}
									// System.out.print(" ==> ");
									// System.out.println(mv + " // job submission: "
									// + SystemAnalysisExt.getCurrentTime(Long.parseLong(cc[3]))
									// + " // storage time: "
									// + SystemAnalysisExt.getCurrentTime(ei.getHeader().getStorageTime().getTime()));
								}
							};
							es.execute(rr);
						}
						es.shutdown();
						es.awaitTermination(31, TimeUnit.DAYS);
						String sn = tempDataSetDescription.getRemoteCapableAnalysisActionClassName();
						if (sn.indexOf(".") > 0)
							sn = sn.substring(sn.lastIndexOf(".") + 1);
						e.getHeader().setExperimentname(sn + ": " + e.getName() + ", manual merge at " + SystemAnalysisExt.getCurrentTime());
						e.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults);
						e.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults);
						e.getHeader().setDatabaseId("");
						for (SubstanceInterface si : e) {
							for (ConditionInterface ci : si) {
								ci.setExperimentName(e.getHeader().getExperimentName());
								ci.setExperimentType(IAPexperimentTypes.AnalysisResults);
							}
						}
						ArrayList<MappingData3DPath> mdpl = MappingData3DPath.get(e);
						e.clear();
						e.addAndMerge(MappingData3DPath.merge(mdpl, false));
						long tStart = tempDataSetDescription.getSubmissionTimeL();
						long tProcessing = tFinish - tStart;
						long minutes = tProcessing / 1000 / 60;
						e.getHeader().setRemark(
								e.getHeader().getRemark() + " // processing time (min): " + minutes + " // finished: " +
										SystemAnalysisExt.getCurrentTime());
						System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
						System.out.println("> PIPELINE PROCESSING TIME (min)=" + minutes);
						System.out.println("*****************************");
						System.out.println("Merged Experiment: " + e.getName());
						System.out.println("Merged Measurements: " + e.getNumberOfMeasurementValues());
						
						System.out.println("> SAVE COMBINED EXPERIMENT...");
						m.saveExperiment(e, new BackgroundTaskConsoleLogger("", "", true));
						// System.out.println("> DELETE TEMP DATA IS DISABLED!");
						System.out.println("> DELETE TEMP DATA...");
						for (ExperimentHeaderInterface i : knownResults) {
							try {
								if (i.getDatabaseId() != null && i.getDatabaseId().length() > 0)
									m.deleteExperiment(experiment2id.get(i));
								
							} catch (Exception err) {
								System.out.println("Could not delete experiment " + i.getExperimentName() + " (" +
										err.getMessage() + ")");
							}
						}
						System.out.println(SystemAnalysisExt.getCurrentTime() + "> COMPLETED");
					}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public void switchStatus(MongoDB m) {
		if (!active) {
			// enable server mode
			try {
				if (IAPmain.isSettingEnabled(IAPfeature.DELETE_CLOUD_JOBS_AND_TEMP_DATA_UPON_CLOUD_START)) {
					m.batchClearJobs();
					for (ExperimentHeaderInterface ei : m.getExperimentList(null)) {
						if (ei.getExperimentName() == null || ei.getExperimentName().length() == 0 || ei.getExperimentName().contains("§"))
							m.deleteExperiment(ei.getDatabaseId());
					}
				}
				cloudTaskManager.setHostName(SystemAnalysisExt.getHostName());
				cloudTaskManager.startWork(m);
			} catch (UnknownHostException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else {
			// disable server mode
			cloudTaskManager.stopWork();
		}
		this.active = !active;
	}
	
	public boolean getIsCalculationPossible() {
		return !cloudTaskManager.isDisableProces();
	}
	
}
