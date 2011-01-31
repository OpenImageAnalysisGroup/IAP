/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class MemSample {
	
	private double value;
	private int replicate;
	private int plantID;
	private String optTimeUnit;
	private Integer optTimeValueForComparison;
	
	public MemSample(double value, int replicate, int plantID, String unit, String optTimeUnit, Integer optTimeValueForComparision) {
		this.value = value;
		this.replicate = replicate;
		this.plantID = plantID;
		this.optTimeUnit = optTimeUnit;
		this.optTimeValueForComparison = optTimeValueForComparision;
	}
	
	public double getPlantID() {
		return plantID;
	}
	
	public double getReplicateID() {
		return replicate;
	}
	
	public Double getTime() {
		return new Double(optTimeValueForComparison);
	}
	
	public double getValue() {
		return value;
	}
	
	public String getTimeUnit() {
		return optTimeUnit;
	}
}
