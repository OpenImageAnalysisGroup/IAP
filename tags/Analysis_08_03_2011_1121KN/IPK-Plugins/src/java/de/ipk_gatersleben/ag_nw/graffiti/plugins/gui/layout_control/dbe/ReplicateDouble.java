/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 11.07.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

public class ReplicateDouble {
	private double measurementValue;
	private String replicateNumber;
	private String optQualityAnnotation;
	
	public ReplicateDouble(Object measurementValue, String replicateNumber, String optQualityAnnotation) {
		if (measurementValue == null || (!(measurementValue instanceof Double)))
			measurementValue = new Double(Double.NaN);
		this.setMeasurementValue((Double) measurementValue);
		this.setReplicateNumber(replicateNumber);
	}
	
	public void setMeasurementValue(double measurementValue) {
		this.measurementValue = measurementValue;
	}
	
	public double getMeasurementValue() {
		return measurementValue;
	}
	
	public void setReplicateNumber(String replicateNumber) {
		this.replicateNumber = replicateNumber;
	}
	
	public String getOptionalQualityAnnotation() {
		return optQualityAnnotation;
	}
	
	public String getReplicateNumber() {
		if (replicateNumber.endsWith(".0"))
			return replicateNumber.substring(0, replicateNumber.length() - 2);
		else
			return replicateNumber;
	}
	
	public double doubleValue() {
		return measurementValue;
	}
}
