/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.webstart.IAPmain;
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
		System.out.println("> SYSTEM ANALYSIS");
		if (args.length > 0 && args[0].contains("full")) {
			System.out.println(": full - enabling full CPU utilization - " + SystemAnalysis.getNumberOfCPUs());
			SystemAnalysis.setUseFullCpuPower(true);
		}
		if (args.length > 0 && args[0].contains("half")) {
			SystemAnalysis.setUseHalfCpuPower(true);
			System.out.println(": half - enabling half CPU utilization - " + SystemAnalysis.getNumberOfCPUs());
		}
		if (args.length > 0) {
			try {
				Integer i = Integer.parseInt(args[0]);
				System.out.println(": " + args[0] + " - using " + i + " CPUs");
				SystemAnalysis.setUseCpu(i);
			} catch (Exception e) {
				if ((args[0] + "").equalsIgnoreCase("clear")) {
					try {
						MongoDB.getDefaultCloud().batchClearJobs();
						System.out.println(":clear - cleared scheduled jobs in database " + MongoDB.getDefaultCloud().getDatabaseName());
						return;
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} else
					if ((args[0] + "").equalsIgnoreCase("merge")) {
						merge();
						return;
					} else {
						System.out.println(": Valid command line parameters:");
						System.out.println("   'half'  - use half of the CPUs");
						System.out.println("   'full'  - use all of the CPUs");
						System.out.println("   'nnn'   - use specified number of CPUs");
						System.out.println("   'clear' - clear scheduled tasks");
						System.out.println("   'merge' - in case of error (merge interrupted previously), merge temporary results");
					}
			}
		}
		SystemInfoExt si = new SystemInfoExt();
		System.out.println("CPUs (sockets,physical,logical): " +
				si.getCpuSockets() + "," + si.getCpuPhysicalCores() + "," +
				si.getCpuLogicalCores() + ", using " + SystemAnalysis.getNumberOfCPUs());
		System.out.println("MEMORY: " + SystemAnalysisExt.getPhysicalMemoryInGB() + " GB, using " + SystemAnalysis.getMemoryMB() / 1024 + " GB");
		System.out.println(">");
		System.out.println("> INITIALIZE CLOUD TASK MANAGER (T=" + IAPservice.getCurrentTimeAsNiceString() + ")");
		
		// register extended hierarchy and loaded image loaders (and more)
		new MultimodalDataHandlingAddon();
		
		ResourceIOManager.registerIOHandler(new LemnaTecFTPhandler());
		for (MongoDB m : MongoDB.getMongos()) {
			for (ResourceIOHandler handler : m.getHandlers())
				ResourceIOManager.registerIOHandler(handler);
			
			CloudComputingService cc = new CloudComputingService();
			cc.switchStatus(m);
			System.out.println("START CLOUD SERVICE FOR " + m.getPrimaryHandler().getPrefix());
		}
		
	}
	
	private static void merge() {
		try {
			DataMappingTypeManager3D.replaceVantedMappingTypeManager();
			
			MongoDB m = MongoDB.getDefaultCloud();
			ArrayList<ExperimentHeaderInterface> knownResults = new ArrayList<ExperimentHeaderInterface>();
			ArrayList<ExperimentHeaderInterface> el = m.getExperimentList(null);
			HashSet<TempDataSetDescription> availableTempDatasets = new HashSet<TempDataSetDescription>();
			for (ExperimentHeaderInterface i : el) {
				String[] cc = i.getExperimentName().split("§");
				if (i.getImportusergroup().equals("Temp") && cc.length == 4) {
					String className = cc[0];
					String partCnt = cc[2];
					String submTime = cc[3];
					availableTempDatasets.add(new TempDataSetDescription(className, partCnt, submTime));
				}
			}
			for (TempDataSetDescription cmd : availableTempDatasets) {
				for (ExperimentHeaderInterface i : el) {
					if (i.getExperimentName() != null && i.getExperimentName().contains("§")) {
						String[] cc = i.getExperimentName().split("§");
						if (i.getImportusergroup().equals("Temp") && cc.length == 4) {
							String className = cc[0];
							String partCnt = cc[2];
							String submTime = cc[3];
							String bcn = cmd.getRemoteCapableAnalysisActionClassName();
							String bpn = cmd.getPartCnt();
							String bst = cmd.getSubmissionTime();
							if (className.equals(bcn)
										&& partCnt.equals(bpn)
										&& submTime.equals(bst)) {
								knownResults.add(i);
							}
						}
					}
				}
				System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
				System.out.println("> TODO: " + cmd.getPartCnt() + ", FINISHED: " + knownResults.size());
				if (knownResults.size() >= cmd.getPartCntI()) {
					System.out.println("*****************************");
					System.out.println("MERGE RESULTS:");
					System.out.println("TODO: " + cmd.getPartCntI() + ", RESULTS FINISHED: " + knownResults.size());
					Experiment e = new Experiment();
					long tFinish = System.currentTimeMillis();
					for (ExperimentHeaderInterface i : knownResults) {
						ExperimentInterface ei = m.getExperiment(i);
						if (ei.getNumberOfMeasurementValues() > 0)
							System.out.println("Measurements: " + ei.getNumberOfMeasurementValues());
						e.addAndMerge(ei);
						System.out.println("*****************************");
					}
					String sn = cmd.getRemoteCapableAnalysisActionClassName();
					if (sn.indexOf(".") > 0)
						sn = sn.substring(sn.lastIndexOf(".") + 1);
					e.getHeader().setExperimentname(sn + ": " + "manual merge at " + SystemAnalysisExt.getCurrentTime());
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
					e = (Experiment) MappingData3DPath.merge(mdpl, false);
					long tStart = cmd.getSubmissionTimeL();
					long tProcessing = tFinish - tStart;
					long minutes = tProcessing / 1000 / 60;
					e.getHeader().setRemark(
								e.getHeader().getRemark() + " // processing time (min): " + minutes + " // finished: " + SystemAnalysisExt.getCurrentTime());
					System.out.println("> T=" + IAPservice.getCurrentTimeAsNiceString());
					System.out.println("> PIPELINE PROCESSING TIME (min)=" + minutes);
					System.out.println("*****************************");
					System.out.println("Merged Experiment: " + e.getName());
					System.out.println("Merged Measurements: " + e.getNumberOfMeasurementValues());
					
					System.out.println("> SAVE COMBINED EXPERIMENT...");
					m.saveExperiment(e, new BackgroundTaskConsoleLogger("", "", true));
					System.out.println("> DELETE TEMP DATA...");
					for (ExperimentHeaderInterface i : knownResults)
						m.deleteExperiment(i.getDatabaseId());
					System.out.println("> COMPLETED");
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
	
}
