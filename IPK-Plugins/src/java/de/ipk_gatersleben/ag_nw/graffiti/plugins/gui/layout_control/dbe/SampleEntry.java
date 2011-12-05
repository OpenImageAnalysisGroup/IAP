/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;

public class SampleEntry {
	
	private final int time;
	private final Long optFineTime;
	private final String timeUnit;
	private final String measurementUnit;
	private final ArrayList<ReplicateDouble> measurementValues;
	
	public SampleEntry(int time, Long optFineTime, String timeUnit, String measurementUnit,
			ArrayList<ReplicateDouble> measurementValues) {
		this.time = time;
		this.optFineTime = optFineTime;
		this.timeUnit = timeUnit;
		this.measurementUnit = measurementUnit;
		this.measurementValues = measurementValues;
	}
	
	public int getTime() {
		return time;
	}
	
	public Long getOptFineTime() {
		return optFineTime;
	}
	
	public String getTimeUnit() {
		return timeUnit;
	}
	
	public ArrayList<ReplicateDouble> getMeasurementValues() {
		return measurementValues;
	}
	
	public double getMinimum() {
		return ExperimentData.getMinimum(getMeasurementValues());
	}
	
	public double getMaximum() {
		return ExperimentData.getMaximum(getMeasurementValues());
	}
	
	public double getStddev() {
		return ExperimentData.getStddev(getMeasurementValues());
	}
	
	public double getAverage() {
		return ExperimentData.getAverage(getMeasurementValues());
	}
	
	public String getMeasurementUnit() {
		return measurementUnit;
	}
}
