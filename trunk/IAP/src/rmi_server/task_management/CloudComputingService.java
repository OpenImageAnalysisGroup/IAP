/*******************************************************************************
 * 
 *    Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 * 
 *******************************************************************************/
/*
 * Created on Sep 30, 2010 by Christian Klukas
 */
package rmi_server.task_management;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ErrorMsg;

/**
 * @author klukas
 * 
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

	public void switchStatus(String login, String pass) {
		if (!active) {
			// enable server mode
			try {
				String hostName = InetAddress.getLocalHost().getHostName();
				String ip = InetAddress.getLocalHost().getHostAddress();
				cloudTaskManager.setHostName(hostName);
				cloudTaskManager.setIp(ip);
				cloudTaskManager.startWork(login, pass);
			} catch (UnknownHostException e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else {
			// disable server mode
			cloudTaskManager.stopWork();
		}
		this.active = !active;
	}

}
