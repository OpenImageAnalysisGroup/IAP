/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Nov 9, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.navigation_actions;

/**
 * @author klukas
 */
public class ReportRow {

	private int replicateID;
	private String condition;
	private String experimentname;
	private String sampleTime;

	public void setCarrier(int replicateID) {
		this.replicateID = replicateID;
	}

	public void setPlant(String condition) {
		this.condition = condition;
	}

	public void setExperiment(String experimentname) {
		this.experimentname = experimentname;
	}

	public void setTime(String sampleTime) {
		this.sampleTime = sampleTime;
	}
}
