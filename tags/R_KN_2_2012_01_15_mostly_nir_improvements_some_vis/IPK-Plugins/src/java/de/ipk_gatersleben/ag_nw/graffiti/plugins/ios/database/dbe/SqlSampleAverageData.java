/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe;

public class SqlSampleAverageData {
	int sampleID, substanceID, replicateCnt;
	double min, max, stdDev, avg;
	
	public SqlSampleAverageData(int sampleID, int substanceID, Integer replicateCnt, Double min, Double max, Double stdDev,
						Double avg) {
		this.sampleID = sampleID;
		this.substanceID = substanceID;
		this.replicateCnt = replicateCnt;
		this.min = min;
		this.max = max;
		this.stdDev = stdDev;
		this.avg = avg;
	}
	
}
