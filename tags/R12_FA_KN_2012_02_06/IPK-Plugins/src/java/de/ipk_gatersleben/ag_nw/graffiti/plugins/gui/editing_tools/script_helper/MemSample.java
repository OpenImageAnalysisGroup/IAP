/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class MemSample {
	
	private final double value;
	private final int replicate;
	private final long plantID;
	private final String optTimeUnit;
	private final Integer optTimeValueForComparison;
	private Long optFineTimeValueForComparison;
	private String optQualityAnnotation;
	private Double position;
	
	public MemSample(double value, int replicate, long plantID, String unit, String optTimeUnit, Integer optTimeValueForComparision) {
		this.value = value;
		this.replicate = replicate;
		this.plantID = plantID;
		this.optTimeUnit = optTimeUnit;
		this.optTimeValueForComparison = optTimeValueForComparision;
	}
	
	public MemSample(double value, String replicateAndOptQualityAnnotation, long plantID, String unit, String optTimeUnit,
			String optTimeValueAndFineTimeValueForComparison) {
		this.value = value;
		this.replicate = Integer.parseInt(replicateAndOptQualityAnnotation.split("/", 2)[0]);
		this.setOptQualityAnnotation((replicateAndOptQualityAnnotation.split(";", 3)[0]).split("/", 2)[1]);
		String pos = replicateAndOptQualityAnnotation.split(";", 3)[1];
		if (pos != null && pos.length() > 0)
			this.setPosition(Double.parseDouble(pos));
		this.plantID = plantID;
		this.optTimeUnit = optTimeUnit;
		this.optTimeValueForComparison = Integer.parseInt(optTimeValueAndFineTimeValueForComparison.split(";", 2)[0]);
		this.optFineTimeValueForComparison = Long.parseLong(optTimeValueAndFineTimeValueForComparison.split(";", 2)[1]);
	}
	
	public long getPlantID() {
		return plantID;
	}
	
	public int getReplicateID() {
		return replicate;
	}
	
	public Integer getTime() {
		return optTimeValueForComparison;
	}
	
	public Long getOptTimeValueFine() {
		return optFineTimeValueForComparison;
	}
	
	public double getValue() {
		return value;
	}
	
	public String getTimeUnit() {
		return optTimeUnit;
	}
	
	public String getOptQualityAnnotation() {
		return optQualityAnnotation;
	}
	
	private void setOptQualityAnnotation(String optQualityAnnotation) {
		this.optQualityAnnotation = optQualityAnnotation;
	}
	
	public Double getPosition() {
		return position;
	}
	
	private void setPosition(Double position) {
		this.position = position;
	}
}
