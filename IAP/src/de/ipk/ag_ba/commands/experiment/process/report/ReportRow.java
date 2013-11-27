/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.commands.experiment.process.report;

/**
 * @author klukas
 */
public class ReportRow {
	
	private int replicateID;
	private String condition;
	private String experimentname;
	private String sampleTime;
	
	public void setCarrier(int replicateID) {
		this.setReplicateID(replicateID);
	}
	
	public void setPlant(String condition) {
		this.setCondition(condition);
	}
	
	public void setExperiment(String experimentname) {
		this.setExperimentname(experimentname);
	}
	
	public void setTime(String sampleTime) {
		this.setSampleTime(sampleTime);
	}

	public void setReplicateID(int replicateID) {
		this.replicateID = replicateID;
	}

	public int getReplicateID() {
		return replicateID;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getCondition() {
		return condition;
	}

	public void setExperimentname(String experimentname) {
		this.experimentname = experimentname;
	}

	public String getExperimentname() {
		return experimentname;
	}

	public void setSampleTime(String sampleTime) {
		this.sampleTime = sampleTime;
	}

	public String getSampleTime() {
		return sampleTime;
	}
}
