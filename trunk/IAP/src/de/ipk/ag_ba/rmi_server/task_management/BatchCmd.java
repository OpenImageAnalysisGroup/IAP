/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 13, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.rmi_server.task_management;

import java.util.HashSet;
import java.util.Map.Entry;

import org.BackgroundTaskStatusProvider;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class BatchCmd extends BasicDBObject {
	private static final long serialVersionUID = 1L;
	private BackgroundTaskStatusProvider statusProvider;

	public HashSet<String> getTargetIPs() {
		HashSet<String> res = new HashSet<String>();
		for (Entry<String, Object> e : entrySet()) {
			if (e.getKey().startsWith("targetIP"))
				res.add((String) e.getValue());
		}
		return res;
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

	public static void enqueueBatchCmd(BatchCmd cmd) throws Exception {
		// empty
		new MongoDB().batchEnqueue(cmd);
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

	public void setStatusProvider(BackgroundTaskStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}

	public void updateRunningStatus(CloudAnalysisStatus status) {
		new MongoDB().batchClaim(this, status);
		setRunStatus(status);
		// double progress = statusProvider.getCurrentStatusValueFine();
		// statusProvider.getCurrentStatusMessage1();
		// statusProvider.getCurrentStatusMessage2();
		// statusProvider.pluginWaitsForUser();
		// statusProvider.pleaseStop() -->
		// statusProvider.pleaseContinueRun() -->

	}

	public ObjectId getExperimentMongoID() {
		if (getString("experiment") != null)
			return new ObjectId(getString("experiment"));
		else
			return null;
	}

	public void setExperimentMongoID(String id) {
		put("experiment", id);
	}

	public long getLastUpdateTime() {
		return getLong("lastupdate");
	}

	public CloudAnalysisStatus getRunStatus() {
		return CloudAnalysisStatus.valueOf(getString("runstatus"));
	}

	public void setRunStatus(CloudAnalysisStatus status) {
		put("runstatus", status.name());
	}

}
