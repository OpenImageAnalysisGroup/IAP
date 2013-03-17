/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 13, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public class BatchCmd extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	private BackgroundTaskStatusProviderSupportingExternalCall statusProvider;
	private ExperimentHeaderInterface header;
	
	public HashSet<String> getTargetIPs() {
		HashSet<String> res = new HashSet<String>();
		for (Entry<String, Object> e : entrySet()) {
			if (e.getKey().startsWith("targetIP"))
				res.add((String) e.getValue());
		}
		return res;
	}
	
	public BackgroundTaskStatusProviderSupportingExternalCall getStatusProvider() {
		return statusProvider;
	}
	
	public void setTargetIPs(HashSet<String> ips) {
		int idx = 1;
		for (String v : ips) {
			put("targetIP" + idx, v);
			idx++;
		}
	}
	
	public BatchCmd() {
		// empty
	}
	
	public static void enqueueBatchCmd(MongoDB m, BatchCmd cmd) throws Exception {
		m.batch().enqueue(cmd);
	}
	
	public String getRemoteCapableAnalysisActionClassName() {
		return getString("command");
	}
	
	public void setRemoteCapableAnalysisActionClassName(String command) {
		put("command", command);
	}
	
	public String getRemoteCapableAnalysisActionParams() {
		return getString("parameters");
	}
	
	public void setRemoteCapableAnalysisActionParams(String parameters) {
		put("parameters", parameters);
	}
	
	public long getSubmissionTime() {
		if (get("submission") != null)
			return getLong("submission");
		else
			return System.currentTimeMillis();
	}
	
	public void setSubmissionTime(long submission) {
		put("submission", submission);
	}
	
	public void setStatusProvider(BackgroundTaskStatusProviderSupportingExternalCall statusProvider) {
		this.statusProvider = statusProvider;
	}
	
	public boolean updateRunningStatus(MongoDB m, CloudAnalysisStatus status) {
		if (statusProvider != null) {
			put("progress", statusProvider.getCurrentStatusValueFine());
			put("line1", statusProvider.getCurrentStatusMessage1());
			put("line2", statusProvider.getCurrentStatusMessage2());
			put("line3", "<br>" + statusProvider.getCurrentStatusMessage3());
			put("waitsForUser", statusProvider.pluginWaitsForUser());
			
			if (statusProvider.getCurrentStatusValue() < 100) {
				BatchCmd bcmd = m.batch().getCommand(this);
				if (bcmd == null)
					statusProvider.pleaseStop();
			}
		}
		if (m.batch().claim(this, status, true)) {
			setRunStatus(status);
			return true;
		}
		return false;
		// statusProvider.pleaseStop() -->
		// statusProvider.pleaseContinueRun() -->
	}
	
	public ExperimentHeaderInterface getExperimentHeader() {
		String expId = getString("experiment");
		if (expId != null) {
			if (header == null)
				header = new ExperimentReference(expId).getHeader();
			return header;
		} else
			return null;
	}
	
	public void setExperimentMongoID(String id) {
		put("experiment", id);
	}
	
	public String getExperimentDatabaseId() {
		return getString("experiment");
	}
	
	public long getLastUpdateTime() {
		return getLong("lastupdate");
	}
	
	public CloudAnalysisStatus getRunStatus() {
		for (CloudAnalysisStatus cas : CloudAnalysisStatus.values())
			if (cas.toNiceString().equals(getString("runstatus")))
				return cas;
		return CloudAnalysisStatus.valueOf(getString("runstatus"));
	}
	
	public void setRunStatus(CloudAnalysisStatus status) {
		put("runstatus", status.name());
	}
	
	public double getCurrentStatusValueFine() {
		try {
			Double d = getDouble("progress");
			if (d == null)
				return -1;
			else
				return d;
		} catch (NullPointerException npe) {
			return -1;
		}
	}
	
	public String getCurrentStatusMessage1() {
		return getString("line1");
	}
	
	public String getCurrentStatusMessage2() {
		return getString("line2");
	}
	
	public String getCurrentStatusMessage3() {
		return getString("line3");
	}
	
	public void setSubTaskInfo(int id, int cnt) {
		put("part_idx", id);
		put("part_cnt", cnt);
	}
	
	public int getPartIdx() {
		return getInt("part_idx", 0);
	}
	
	public int getPartCnt() {
		return getInt("part_cnt", 1);
	}
	
	public void setOwner(String hostName) {
		put("owner", hostName);
	}
	
	public String getOwner() {
		return getString("owner");
	}
	
	public static ArrayList<BasicDBObject> getRunstatusMatchers(CloudAnalysisStatus starting) {
		ArrayList<BasicDBObject> res = new ArrayList<BasicDBObject>();
		res.add(new BasicDBObject("runstatus", starting.toString()));
		return res;
	}
	
	public int getCpuTargetUtilization() {
		return getInt("cpu_utilization", 1);
	}
	
	public void setCpuTargetUtilization(int cpu_util) {
		put("cpu_utilization", cpu_util);
	}
	
	/**
	 * See and use SystemAnalysis.getOperatingSystem()
	 */
	public void setOperatingSystem(String operatingSystemOrNullForEvery) {
		put("operating_system", operatingSystemOrNullForEvery);
	}
	
	/**
	 * See and use SystemAnalysis.getOperatingSystem()
	 */
	public String getDesiredOperatingSystem() {
		return getString("operating_system", null);
	}
	
	public void setNewstAvailableData(Date newestImportDate, String databaseIdOfNewestResultData) {
		if (newestImportDate != null && databaseIdOfNewestResultData != null && !databaseIdOfNewestResultData.isEmpty()) {
			put("available_result_part_db_id", databaseIdOfNewestResultData);
			put("available_result_import_date", newestImportDate);
		}
	}
	
	public Date getAvailableResultImportDate() {
		return getDate("available_result_import_date", null);
	}
	
	public String getAvailableResultDatabaseId() {
		return getString("available_result_part_db_id", null);
	}
	
	public boolean desiredOperatingSystemMatchesCurrentOperatingSystem() {
		return getDesiredOperatingSystem() == null
				|| getDesiredOperatingSystem().equals(SystemAnalysis.getOperatingSystem());
	}
	
	public String getNiceOwner() {
		String hostInfo = getOwner();
		try {
			String[] hhh = hostInfo.split("_", 2);
			String host = hhh[0];
			return "" + host + " (up " + SystemAnalysis.getCurrentTime(Long.parseLong(hhh[1])) + ")";
		} catch (Exception e) {
			return hostInfo;
		}
	}
}
