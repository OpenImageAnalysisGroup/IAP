/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Oct 13, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.server.task_management;

/**
 * @author klukas
 */
public enum CloudAnalysisStatus {
	SCHEDULED, STARTING, IN_PROGRESS, FINISHED, FINISHED_INCOMPLETE, ARCHIVED;
	
	public String toNiceString() {
		switch (this) {
			case FINISHED:
				return "finished";
			case FINISHED_INCOMPLETE:
				return "incomplete";
			case IN_PROGRESS:
				return "in progress";
			case SCHEDULED:
				return "scheduled";
			case STARTING:
				return "starting";
			case ARCHIVED:
				return "deactivated";
		}
		return "unknown";
	}
}
