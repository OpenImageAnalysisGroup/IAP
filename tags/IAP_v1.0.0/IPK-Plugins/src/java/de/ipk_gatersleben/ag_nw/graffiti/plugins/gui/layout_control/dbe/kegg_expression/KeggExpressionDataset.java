/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression;

import java.util.ArrayList;

public class KeggExpressionDataset {
	
	private String name, optOrganismName;
	private ArrayList<KeggExpressionDatapoint> datapoints = new ArrayList<KeggExpressionDatapoint>();
	private boolean isTrueKeggExpressionControlTargetFormat;
	
	// String organism, String submitter, String created
	public KeggExpressionDataset(String name, boolean isKeggExpressionControlTargetFormat, String optOrganismName) {
		this.name = name;
		this.optOrganismName = optOrganismName;
		this.isTrueKeggExpressionControlTargetFormat = isKeggExpressionControlTargetFormat;
	}
	
	public void addDatapoint(
						String geneId,
						Double cSig, Double cBgk,
						Double tSig, Double tBgk,
						Double c, Double t,
						Double optX, Double optY,
						String optRawTextFileInformationSignalQualityPMA) {
		datapoints.add(new KeggExpressionDatapoint(geneId, cSig, cBgk, tSig, tBgk, c, t, optX, optY,
							optRawTextFileInformationSignalQualityPMA));
	}
	
	public String getFileName() {
		return name;
	}
	
	public ArrayList<KeggExpressionDatapoint> getDataPoints() {
		return datapoints;
	}
	
	public boolean isTrueKeggExpressionFormatControlTarget() {
		return isTrueKeggExpressionControlTargetFormat;
	}
	
	public String getOrganismName() {
		return optOrganismName;
	}
	
}
