/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe;

public class SqlMeasurementData {
	public int sampleId, substanceId, measurementId;
	public double measurementValue;
	int replicateId;
	
	public SqlMeasurementData(
						int sampleId, int substanceId, int measurementId,
						double measurementValue,
						int replicateId) {
		this.sampleId = sampleId;
		this.substanceId = substanceId;
		this.measurementId = measurementId;
		this.measurementValue = measurementValue;
		this.replicateId = replicateId;
	}
}
