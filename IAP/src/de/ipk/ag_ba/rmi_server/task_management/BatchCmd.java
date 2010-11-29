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

	public BatchCmd() {
		// empty
	}

	public static void enqueueBatchCmd(HashSet<String> targetIPs, String remoteCapableAnalysisActionClassName,
						String remoteCapableAnalysisActionParams, String experimentInputMongoID) {
		// empty
		new MongoDB().batchEnqueue(targetIPs, remoteCapableAnalysisActionClassName, remoteCapableAnalysisActionParams, experimentInputMongoID);
	}

	public String getRemoteCapableAnalysisActionClassName() {
		return getString("command");
	}

	public String getRemoteCapableAnalysisActionParams() {
		return getString("parameters");
	}

	public long getSubmissionTime() {
		return getLong("submission");
	}

	public void setStatusProvider(BackgroundTaskStatusProvider statusProvider) {
		this.statusProvider = statusProvider;
	}

	public void updateRunningStatus(CloudAnalysisStatus status) {
		double progress = statusProvider.getCurrentStatusValueFine();
		statusProvider.getCurrentStatusMessage1();
		statusProvider.getCurrentStatusMessage2();
		statusProvider.pluginWaitsForUser();
		// statusProvider.pleaseStop() -->
		// statusProvider.pleaseContinueRun() -->

	}

	public ObjectId getExperimentMongoID() {
		return new ObjectId(getString("experiment"));
	}

	public long getLastUpdateTime() {
		return getLong("lastupdate");
	}

	public CloudAnalysisStatus getRunStatus() {
		return CloudAnalysisStatus.valueOf(getString("runstatus"));
	}
}
