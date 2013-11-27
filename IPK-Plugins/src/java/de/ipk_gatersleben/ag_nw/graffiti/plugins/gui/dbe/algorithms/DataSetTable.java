/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import java.util.ArrayList;

public class DataSetTable {
	
	ArrayList<DataSetRow> dataSetRows = new ArrayList<DataSetRow>();
	
	public void addRow(
						String rowLabel,
						String experimentName,
						String substanceName,
						String mapping,
						String species,
						String genotype,
						String treatment,
						int seriesId,
						String timeS,
						String timeUnit,
						int replicateID,
						Double value,
						String unit) {
		dataSetRows.add(
							new DataSetRow(
												rowLabel,
												experimentName,
												substanceName,
												mapping,
												species,
												genotype,
												treatment,
												seriesId,
												timeS,
												timeUnit,
												replicateID,
												value,
												unit));
	}
	
	public ArrayList<DataSetRow> getRows() {
		return dataSetRows;
	}
	
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append(DataSetRow.getHeading() + "\n");
		
		for (DataSetRow dsr : dataSetRows)
			res.append(dsr.toString() + "\n");
		
		return res.toString();
	}
}
