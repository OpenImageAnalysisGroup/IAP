/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ErrorMsg;

import de.ipk.ag_ba.mongo.MongoDB;

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
	
	public void switchStatus(MongoDB m) {
		if (!active) {
			// enable server mode
			try {
				m.batchClearJobs();
				String hostName = InetAddress.getLocalHost().getHostName();
				String ip = InetAddress.getLocalHost().getHostAddress();
				cloudTaskManager.setHostName(hostName);
				cloudTaskManager.setIp(ip);
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
