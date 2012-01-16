/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.10.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import org.ErrorMsg;

public class DoubleAndSourceList {
	
	private Double value;
	private int sourceListIndex01;
	private double rangValue = Double.NaN;
	
	public DoubleAndSourceList(Double value, int sourceListIndex01) {
		this.value = value;
		this.sourceListIndex01 = sourceListIndex01;
	}
	
	public Double getDoubleValue() {
		return value;
	}
	
	public int getSourceListIndex01() {
		return sourceListIndex01;
	}
	
	public double getRangValue() {
		if (Double.isNaN(rangValue))
			ErrorMsg.addErrorMessage("Internal Error: Rang value not set, but requested!");
		return rangValue;
	}
	
	public void setRank(double rang) {
		this.rangValue = rang;
	}
	
}
