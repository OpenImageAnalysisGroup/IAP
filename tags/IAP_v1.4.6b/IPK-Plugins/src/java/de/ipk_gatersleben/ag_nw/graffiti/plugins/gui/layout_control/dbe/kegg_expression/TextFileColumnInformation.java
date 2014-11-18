/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.kegg_expression;

public class TextFileColumnInformation {
	private int signalColumn;
	private Integer optDetectionColumn;
	private String columnName;
	
	public TextFileColumnInformation(String columnName, int signalColumn, Integer optDetectionColumn) {
		this.columnName = columnName;
		this.signalColumn = signalColumn;
		this.optDetectionColumn = optDetectionColumn;
		if (columnName.endsWith(".0")) {
			this.columnName = this.columnName.substring(0, this.columnName.length() - ".0".length());
		}
	}
	
	public int getSignalColumn() {
		return signalColumn;
	}
	
	public Integer getDetectionColumn() {
		return optDetectionColumn;
	}
	
	public String getName() {
		return columnName;
	}
	
}
