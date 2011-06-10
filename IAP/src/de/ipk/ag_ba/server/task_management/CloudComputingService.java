/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.net.UnknownHostException;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.postgresql.LemnaTecFTPhandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_pbi.mmd.MultimodalDataHandlingAddon;

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
		System.out.println("*        IAP - Integrated Analysis Platform       *");
		System.out.println("*     (c) 2010-2011 IPK, Group Image Analysis     *");
		System.out.println("***************************************************");
		System.out.println("*                                                 *");
		System.out.println("* - PI: Dr. Christian Klukas -------------------- *");
		System.out.println("* - Alexander Entzian --------------------------- *");
		System.out.println("*                                                 *");
		System.out.println("***************************************************");
		System.out.println("> SYSTEM ANALYSIS");
		if (args.length > 0 && args[0].contains("full")) {
			System.out.println(": detected command line parameter - enabling full CPU utilization");
			SystemAnalysis.setUseFullCpuPower(true);
		}
		if (args.length > 0 && args[0].contains("half")) {
			System.out.println(": detected command line parameter - enabling half CPU utilization");
			SystemAnalysis.setUseHalfCpuPower(true);
		}
		if (args.length > 0) {
			try {
				Integer i = Integer.parseInt(args[0]);
				System.out.println(": detected command line parameter - using " + i + " CPUs");
				SystemAnalysis.setUseCpu(i);
			} catch (Exception e) {
				System.out.println(": unknown command line parameter");
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
