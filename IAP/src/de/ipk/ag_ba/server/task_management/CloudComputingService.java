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

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.bson.types.ObjectId;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.datasource.Library;
import de.ipk.ag_ba.datasources.file_system.HsmFileSystemSource;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
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
	
	static HashMap<MongoDB, CloudComputingService> instance =
			new HashMap<MongoDB, CloudComputingService>();
	
	boolean active = false;
	
	private final CloudTaskManager cloudTaskManager;
	
	private CloudComputingService() {
		cloudTaskManager = new CloudTaskManager();
	}
	
	public synchronized static CloudComputingService getInstance(MongoDB m) {
		if (!instance.containsKey(m))
			instance.put(m, new CloudComputingService());
		
		return instance.get(m);
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
			return "Join Compute Grid";
	}
	
	public static void main(String[] args) {
		int l = "***************************************************".length();
		System.out.println("***************************************************");
		System.out.println(fillLen("**", l));
		System.out.println(fillLen("*IAP - Integrated Analysis Platform*", l));
		System.out.println(fillLen("**", l));
		System.out.println(fillLen("*--  Systems Biology Cloud Computing --*", l));
		for (IAP_RELEASE ir : IAP_RELEASE.values())
			System.out.println(fillLen("*-- " + ir.toString() + " --*", l));
		System.out.println(fillLen("**", l));
		System.out.println(fillLen("*(c) 2010-2012 IPK, Group Image Analysis*", l));
		System.out.println(fillLen("**", l));
		System.out.println("***************************************************");
		System.out.println(fillLen("**", l));
		System.out.println(fillLenLA("*  PI: Christian Klukas  *", ".", l, 2));
		System.out.println(fillLenLA("*  Dijun Chen  *", ".", l, 2));
		System.out.println(fillLenLA("*  Alexander Entzian  *", ".", l, 2));
		System.out.println(fillLenLA("*  Jean-Michel Pape  *", ".", l, 2));
		System.out.println(fillLen("**", l));
		System.out.println("***************************************************");
		SystemAnalysis.simulateHeadless = true;
		IAPmain.setRunMode(IAPrunMode.CLOUD_HOST);
		{
			for (MongoDB m : MongoDB.getMongos()) {
				CloudComputingService cc = CloudComputingService.getInstance(m);
				cc.setEnableCalculations(true);
			}
		}
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
			if ((args.length > 0 && args[0].toLowerCase().startsWith("watch")) ||
					(args.length > 1 && args[1].startsWith("watch")) ||
					(args.length > 0 && args[0].toLowerCase().startsWith("watch-cmd")) ||
					(args.length > 1 && args[1].startsWith("watch-cmd"))) {
				if ((args.length > 0 && args[0].toLowerCase().startsWith("watch-cmd")) ||
						(args.length > 1 && args[1].startsWith("watch-cmd"))) {
					System.out.println(": watch-cmd - monitoring the experiment data progress (auto-closing at 2 AM)");
					IAPservice.autoCloseAt(2);
				} else
					System.out.println(": watch - monitoring the experiment data progress");
				try {
					IAPservice.monitorExperimentDataProgress();
				} catch (Exception e) {
					MongoDB.saveSystemErrorMessage("Error monitoring experiment data progress.", e);
				} finally {
					System.exit(0);
				}
			} else
				if (args.length > 0 && args[0].toLowerCase().startsWith("close") ||
						(args.length > 1 && args[1].startsWith("close"))) {
					System.out.println(": close - auto-closing after finishing compute task - " + SystemAnalysis.getNumberOfCPUs());
					IAPmain.setRunMode(IAPrunMode.CLOUD_HOST_BATCH_MODE);
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
											for (MongoDB m : MongoDB.getMongos()) {
												m.batchClearJobs();
												System.out.println(":clear - cleared scheduled jobs in database " + m.getDatabaseName());
											}
											return;
										} catch (Exception e1) {
											e1.printStackTrace();
										}
									} else
										if ((args[0] + "").toLowerCase().startsWith("perf")) {
											try {
												System.out.println(":perf - perform performance test (TestPipelineMaize Copy)");
												
												StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">INFO: LabCube construction", false);
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
														System.out.println(SystemAnalysis.getCurrentTime()
																+ ">'monitor' - Report system info to cloud (join, but don't perform calculations)");
														for (MongoDB m : MongoDB.getMongos()) {
															CloudComputingService cc = CloudComputingService.getInstance(m);
															cc.setEnableCalculations(false);
														}
													}
													
												} else
													if ((args[0] + "").toLowerCase().startsWith("backup")) {
														{
															for (MongoDB m : MongoDB.getMongos()) {
																CloudComputingService cc = CloudComputingService.getInstance(m);
																cc.setEnableCalculations(false);
															}
														}
														
														BackupSupport sb = BackupSupport.getInstance();
														sb.scheduleBackup();
														sb.makeBackup();
													} else
														if ((args[0] + "").toLowerCase().startsWith("close")) {
															// ignore, has been processed at the start of this method
														} else {
															if ((args[0] + "").toLowerCase().equalsIgnoreCase("merge")) {
																for (MongoDB m : MongoDB.getMongos()) {
																	System.out.println(":merge - about to merge temporary data sets in database " + m.getDatabaseName());
																	merge(m, true, null);
																	System.out.println(":merge - ^^^ merged temporary data sets in database " + m.getDatabaseName());
																}
																
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
																System.out
																		.println("   'watch'   - periodically check the weight data for new data and report missing data by mail");
																System.out
																		.println("   'watch-cmd' - same as watch, but auto-closing at 2 AM in the morning (for scripted execution)");
															}
														}
								}
							}
		SystemInfoExt si = new SystemInfoExt();
		System.out.println("CPUs (sockets,physical,logical): " +
				si.getCpuSockets() + "," + si.getCpuPhysicalCores() + "," +
				si.getCpuLogicalCores() + ", using " + SystemAnalysis.getNumberOfCPUs());
		System.out.println("MEMORY: " + SystemAnalysisExt.getPhysicalMemoryInGB() + " GB, using " + SystemAnalysis.getMemoryMB() / 1024 + " GB");
		System.out.println(SystemAnalysis.getCurrentTime() + ">");
		System.out.println(SystemAnalysis.getCurrentTime() + "> INITIALIZE CLOUD TASK MANAGER (T=" + IAPservice.getCurrentTimeAsNiceString() + ")");
		
		StopWatch s = new StopWatch(SystemAnalysis.getCurrentTime() + ">INFO: LabCube construction", false);
		ImageOperation io = new ImageOperation(new int[][] { { 0, 0 } });
		if (io != null)
			s.printTime();
		
		String hsm = IAPmain.getHSMfolder();
		if (hsm != null && new File(hsm).exists()) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">HSM Folder: " + hsm);
			Library lib = new Library();
			HsmFileSystemSource dataSourceHsm = new HsmFileSystemSource(lib,
					IAPoptions.getInstance().getString("ARCHIVE", "title", "HSM Archive"),
					hsm,
					IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
					IAPmain.loadIcon("img/ext/gpl2/Gnome-Media-Tape-64.png"),
					IAPmain.loadIcon("img/ext/folder-remote.png"));
		}
		// register extended hierarchy and loaded image loaders (and more)
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos()) {
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
			
			CloudComputingService cc = CloudComputingService.getInstance(m);
			cc.switchStatus(m);
			System.out.println(SystemAnalysis.getCurrentTime() + ">START CLOUD SERVICE FOR " + m.getPrimaryHandler().getPrefix());
		}
	}
	
	private static String fillLen(String string, int len) {
		while (string.length() < len) {
			string = string.substring(0, 1) + " " + string.substring(1);
			if (string.length() < len)
				string = string.substring(0, string.length() - 1) + " " + string.substring(string.length() - 1, string.length());
		}
		return string;
	}
	
	private static String fillLenLA(String string, String fill, int len, int retainLeft) {
		while (string.length() < len) {
			string = string.substring(0, string.length() - retainLeft) + fill + string.substring(string.length() - retainLeft, string.length());
		}
		return string;
	}
	
	public void setEnableCalculations(boolean enableCloudComputing) {
		cloudTaskManager.setDisableProcess(!enableCloudComputing);
	}
	
	public static void merge(MongoDB m, boolean interactive, BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		try {
			DataMappingTypeManager3D.replaceVantedMappingTypeManager();
			
			ArrayList<ExperimentHeaderInterface> el = m.getExperimentList(null);
			HashSet<TempDataSetDescription> availableTempDatasets = new HashSet<TempDataSetDescription>();
			HashSet<String> processedSubmissionTimes = new HashSet<String>();
			for (ExperimentHeaderInterface i : el) {
				if ((i.getExperimentType() + "").contains("Trash"))
					continue;
				String[] cc = i.getExperimentName().split("§");
				if (i.getImportusergroup() != null && i.getImportusergroup().equals("Temp") &&
						(cc.length == 4 || cc.length == 5)) {
					String className = cc[0];
					String idxCnt = cc[1];
					String partCnt = cc[2];
					String submTime = cc[3];
					String mergeWithDBid = cc.length == 5 ? cc[4] : "";
					if (!processedSubmissionTimes.contains(submTime))
						availableTempDatasets.add(new TempDataSetDescription(
								className, partCnt, submTime, i.getOriginDbId(),
								IAP_RELEASE.getReleaseFromDescription(i), mergeWithDBid));
					processedSubmissionTimes.add(submTime);
				}
			}
			for (TempDataSetDescription tempDataSetDescription : availableTempDatasets) {
				ArrayList<ExperimentHeaderInterface> knownResults = new ArrayList<ExperimentHeaderInterface>();
				HashSet<String> added = new HashSet<String>();
				for (ExperimentHeaderInterface i : el) {
					if (i.getExperimentName() != null && i.getExperimentName().contains("§")) {
						String[] cc = i.getExperimentName().split("§");
						if (i.getImportusergroup().equals("Temp") &&
								(cc.length == 4 || cc.length == 5)) {
							String className = cc[0];
							String partIdx = cc[1];
							String partCnt = cc[2];
							String submTime = cc[3];
							String mergeWithDBid = cc.length == 5 ? cc[4] : "";
							String bcn = tempDataSetDescription.getRemoteCapableAnalysisActionClassName();
							String bpn = tempDataSetDescription.getPartCnt();
							String bst = tempDataSetDescription.getSubmissionTime();
							String bmw = tempDataSetDescription.getMergeWithDBid();
							if (className.equals(bcn)
									&& partCnt.equals(bpn)
									&& submTime.equals(bst)
									&& !added.contains(partIdx)
									&& mergeWithDBid.equals(bmw)) {
								knownResults.add(i);
								added.add(partIdx);
							}
						}
					}
				}
				System.out.println(SystemAnalysis.getCurrentTime() + "> T=" + IAPservice.getCurrentTimeAsNiceString());
				System.out.println(SystemAnalysis.getCurrentTime() + "> TODO: " + tempDataSetDescription.getPartCntI() + ", FINISHED: "
						+ knownResults.size());
				{
					// check if source data is still available
					ExperimentReference eRef = new ExperimentReference(knownResults.iterator().next().getOriginDbId());
					ExperimentHeaderInterface eRefHeader = eRef.getHeader();
					if (eRefHeader == null) {
						// source data set has been deleted meanwhile
						// therefore the analysis results will be deleted, too
						String msg = "Source data set for analysis results has been deleted, about to delete non-relevant result data (" + knownResults.size() + "/" +
								tempDataSetDescription.getPartCntI() + " parts)";
						System.out.println(msg);
						MongoDB.saveSystemMessage(msg);
						for (ExperimentHeaderInterface r : knownResults) {
							m.deleteExperiment(r.getDatabaseId());
						}
						continue;
					}
				}
				ExperimentReference optPreviousResultsToBeMerged = null;
				{
					// check if result data needs to be merged with previous calculation results
					String[] cc = knownResults.iterator().next().getExperimentName().split("§");
					String mergeWithDBid = cc.length == 5 ? cc[4] : "";
					if (!mergeWithDBid.isEmpty() && !mergeWithDBid.equals("null")) {
						ExperimentReference eRef = new ExperimentReference(mergeWithDBid);
						ExperimentHeaderInterface eRefHeader = eRef.getHeader();
						if (eRefHeader == null) {
							// source data set has been deleted meanwhile
							// therefore the analysis results will be deleted, too
							String msg = "Result data set to be merged with analysis results has been deleted, about to delete non-relevant result data ("
									+ knownResults.size() + "/" +
									tempDataSetDescription.getPartCntI() + " parts)";
							System.out.println(msg);
							MongoDB.saveSystemMessage(msg);
							for (ExperimentHeaderInterface r : knownResults) {
								m.deleteExperiment(r.getDatabaseId());
							}
							continue;
						} else {
							optPreviousResultsToBeMerged = eRef;
						}
					}
				}
				
				boolean addNewTasksIfMissing = false;
				Object[] res;
				if (interactive) {
					if (SystemAnalysis.isHeadless()) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">Analyzed experiment: "
								+ new ExperimentReference(knownResults.iterator().next().getOriginDbId()).getHeader().getExperimentName());
						if (tempDataSetDescription.getPartCntI() == knownResults.size()) {
							res = new Object[] { false };
						} else {
							System.out.println(SystemAnalysis.getCurrentTime() + ">Process incomplete data sets? TODO: " + tempDataSetDescription.getPartCntI()
									+ ", FINISHED: "
									+ knownResults.size());
							System.out.println("Add compute tasks for missing data? (ENTER yes/no)");
							String in = SystemAnalysis.getCommandLineInput();
							if (in == null)
								res = null;
							else
								if (in.toUpperCase().contains("Y"))
									res = new Object[] { true };
								else
									res = new Object[] { false };
						}
					} else
						res = MyInputHelper.getInput("<html>Process incomplete data sets? "
								+
								(knownResults.size() > 0 ?
										"<br>Analyzed experiment: "
												+ new ExperimentReference(knownResults.iterator().next().getOriginDbId()).getHeader().getExperimentName()
										: "")
								+ "<br>TODO: " + (tempDataSetDescription.getPartCntI() - 1) + ", FINISHED: "
								+ knownResults.size(), "Add compute tasks?", new Object[] {
								"Add compute tasks for missing data?", addNewTasksIfMissing
						});
				} else {
					// non-interactive
					res = new Object[] { false };
				}
				if (res == null) {
					System.out.println(SystemAnalysis.getCurrentTime() + ">Processing cancelled upon user input.");
					System.exit(1);
				} else {
					if (res[0] instanceof String)
						addNewTasksIfMissing = !((String) res[0]).contains("n");
					else
						addNewTasksIfMissing = (Boolean) res[0];
				}
				if (knownResults.size() < tempDataSetDescription.getPartCntI()) {
					if (addNewTasksIfMissing) {
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
							if (tempDataSetDescription.getReleaseIAP() != null)
								cmd.setCompatibleImageAnalysisPipelineName(tempDataSetDescription.getReleaseIAP());
							else
								cmd.setCompatibleImageAnalysisPipelineName(IAP_RELEASE.RELEASE_IAP_IMAGE_ANALYSIS_MAIZE);
							BatchCmd.enqueueBatchCmd(m, cmd);
							System.out.println("Enqueue: " + jobID);
						}
					}
				} else
					if (knownResults.size() >= tempDataSetDescription.getPartCntI()) {
						try {
							doMerge(m, tempDataSetDescription, knownResults, interactive, optStatus, optPreviousResultsToBeMerged);
						} catch (Exception e) {
							MongoDB.saveSystemErrorMessage("Could not properly merge temporary datasets.", e);
						}
					}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	private static void doMerge(MongoDB m,
			TempDataSetDescription tempDataSetDescription,
			ArrayList<ExperimentHeaderInterface> knownResults, boolean interactive, BackgroundTaskStatusProviderSupportingExternalCall optStatus,
			ExperimentReference optPreviousResultsToBeMerged) throws Exception {
		System.out.println("*****************************");
		System.out.println("MERGE INDEX: " + tempDataSetDescription.getPartCntI() + "/" + tempDataSetDescription.getPartCnt()
				+ ", RESULTS AVAILABLE: " + knownResults.size());
		
		ExperimentInterface e = new Experiment();
		
		if (optPreviousResultsToBeMerged != null) {
			String msg = "Previous analysis results, to be merged with current result, will no be loaded (" + optPreviousResultsToBeMerged.getExperimentName()
					+ ","
					+ optPreviousResultsToBeMerged.getHeader().getImportdate() + ")";
			System.out.println(msg);
			MongoDB.saveSystemMessage(msg);
			e = optPreviousResultsToBeMerged.getData(m);
		}
		
		long tFinish = System.currentTimeMillis();
		final int wl = knownResults.size();
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final Runtime r = Runtime.getRuntime();
		HashMap<ExperimentHeaderInterface, String> experiment2id =
				new HashMap<ExperimentHeaderInterface, String>();
		String originName = null;
		int nnii = 1;
		for (ExperimentHeaderInterface ii : knownResults) {
			experiment2id.put(ii, ii.getDatabaseId());
			if (optStatus != null)
				optStatus.setCurrentStatusText2("Process " + ii.getExperimentName() + " (" + nnii + "/" + tempDataSetDescription.getPartCntI() + ")");
			nnii++;
			if (originName == null) {
				String ori = ii.getOriginDbId();
				ExperimentHeaderInterface oriH = m.getExperimentHeader(new ObjectId(ori));
				String[] cc = ii.getExperimentName().split("§");
				String ana = cc[0];
				ana = ana.substring(ana.lastIndexOf(".") + ".".length());
				originName = ana + ": " + oriH.getExperimentName();
				if (optStatus != null)
					optStatus.setCurrentStatusText1("Merge " + knownResults.size() + " parts (" + originName + ")");
				MongoDB.saveSystemMessage("Start combining analysis results for " + originName + " (" + knownResults.size() + " parts)");
			}
			System.out.print(SystemAnalysis.getCurrentTime() + ">" + r.freeMemory() / 1024 / 1024 + " MB free, " + r.totalMemory() / 1024
					/ 1024
					+ " total MB, " + r.maxMemory() / 1024 / 1024 + " max MB>");
			StopWatch s1 = new StopWatch(">m.getExperiment");
			BackgroundTaskConsoleLogger status = new BackgroundTaskConsoleLogger("", "", true);
			ExperimentInterface ei = m.getExperiment(ii, false, status);
			s1.printTime();
			TreeSet<String> condS = new TreeSet<String>();
			for (SubstanceInterface s : ei) {
				for (ConditionInterface ci : s) {
					condS.add(ci.getConditionName());
				}
			}
			String[] cc = ii.getExperimentName().split("§");
			tso.addInt(1);
			System.out.println(tso.getInt() + "/" + wl + " // dataset: " + cc[1] + "/" + cc[2]);
			if (optStatus != null)
				optStatus.setCurrentStatusValueFine(100d * tso.getInt() / wl);
			// for (String c : condS)
			// System.out.println(">Condition: " + c);
			
			StopWatch s = new StopWatch(">e.addMerge");
			e.addAndMerge(ei);
			s.printTime();
		}
		String sn = tempDataSetDescription.getRemoteCapableAnalysisActionClassName();
		if (sn.indexOf(".") > 0)
			sn = sn.substring(sn.lastIndexOf(".") + 1);
		e.getHeader().setDatabaseId("");
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				ci.setExperimentName(e.getHeader().getExperimentName());
				ci.setExperimentType(IAPexperimentTypes.AnalysisResults + "");
			}
		}
		boolean superMerge = false;
		if (superMerge) {
			System.out.println(SystemAnalysis.getCurrentTime() + ">GET MAPPING PATH OBJECTS...");
			ArrayList<MappingData3DPath> mdpl = MappingData3DPath.get(e, false);
			e.clear();
			System.out.println(SystemAnalysis.getCurrentTime() + ">MERGE " + mdpl.size() + " MAPPING PATH OBJECTS TO EXPERIMENT...");
			e = MappingData3DPath.merge(mdpl, false);
			System.out.println(SystemAnalysis.getCurrentTime() + ">UNIFIED EXPERIMENT CREATED");
		}
		long tStart = tempDataSetDescription.getSubmissionTimeL();
		long tProcessing = tFinish - tStart;
		int nToDo = tempDataSetDescription.getPartCntI();
		int nFinish = knownResults.size();
		e.getHeader().setExperimentname(originName);
		e.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults + "");
		e.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults + "");
		e.getHeader().setRemark(
				e.getHeader().getRemark() +
						" // IAP image analysis release " + tempDataSetDescription.getReleaseIAP() +
						" // " + nFinish + " compute tasks finished // " + nToDo + " jobs scheduled at  " + SystemAnalysis.getCurrentTime(tStart) +
						" // processing time: " +
						SystemAnalysis.getWaitTime(tProcessing) + " // finished: " +
						SystemAnalysis.getCurrentTime() + " // manual merge");
		System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
		System.out.println("> PIPELINE PROCESSING TIME =" + SystemAnalysis.getWaitTime(tProcessing));
		System.out.println("*****************************");
		System.out.println("Merged Experiment: " + e.getName());
		System.out.println("Merged Measurements: " + e.getNumberOfMeasurementValues());
		
		System.out.println("> SAVE COMBINED EXPERIMENT...");
		m.saveExperiment(e, optStatus == null ? new BackgroundTaskConsoleLogger("", "", true) : optStatus, true);
		System.out.println("> COMBINED EXPERIMENT HAS BEEN SAVED");
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved combined experiment " + e.getName());
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Merge-time: " + SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish));
		MongoDB.saveSystemMessage("Saved combined experiment " + e.getName() +
				" merging data took " +
				SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish));
		// System.out.println("> DELETE TEMP DATA IS DISABLED!");
		// System.out.println("> DELETE TEMP DATA...");
		// if (interactive)
		System.out.println("> MARK TEMP DATA AS TRASHED...");
		// else
		// System.out.println("> DELETE TEMP DATA...");
		for (ExperimentHeaderInterface i : knownResults) {
			try {
				if (i.getDatabaseId() != null && i.getDatabaseId().length() > 0) {
					ExperimentHeaderInterface hhh = m.getExperimentHeader(new ObjectId(experiment2id.get(i)));
					// if (interactive)
					m.setExperimentType(hhh, "Trash" + ";" + hhh.getExperimentType());
					
					// else
					// m.deleteExperiment(i.getDatabaseId());
				}
			} catch (Exception err) {
				MongoDB.saveSystemErrorMessage("Could not mark experiment " + i.getExperimentName() +
						" as trashed", err);
				// MongoDB.saveSystemErrorMessage("Could not delete experiment " + i.getExperimentName(), err);
				System.out.println("Could not delete experiment " + i.getExperimentName() + " (" +
						err.getMessage() + ")");
			}
		}
		if (optStatus != null)
			optStatus.setCurrentStatusText1("Saved combined experiment " + e.getName());
		if (optStatus != null)
			optStatus.setCurrentStatusText2("Time to completion: " + SystemAnalysis.getWaitTime(System.currentTimeMillis() - tFinish));
		if (optStatus != null)
			optStatus.setCurrentStatusValueFine(100d);
		System.out.println(SystemAnalysis.getCurrentTime() + "> COMPLETED");
	}
	
	public void switchStatus(MongoDB m) {
		if (!active) {
			// enable server mode
			try {
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
