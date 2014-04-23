/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe;

public class SqlSampleIdTimeUnitInfo {
	int sampleID;
	int timeValue;
	String timeUnit;
	
	public SqlSampleIdTimeUnitInfo(int sampleId, int timeValue, String timeUnit) {
		this.sampleID = sampleId;
		this.timeValue = timeValue;
		this.timeUnit = timeUnit;
	}
}
