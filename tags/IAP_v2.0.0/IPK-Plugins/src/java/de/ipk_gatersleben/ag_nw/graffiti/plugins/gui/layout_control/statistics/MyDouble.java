/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 21.09.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

// using this class makes sure, that indexOf operations for a array
// do not compare the double value to return the index, but the actual
// object reference index
public class MyDouble {
	
	private Double d;
	private int series, idx;
	
	public MyDouble(double d, int series, int idx) {
		this.d = d;
		this.series = series;
		this.idx = idx;
	}
	
	public double doubleValue() {
		return d.doubleValue();
	}
	
	public int getSeries() {
		return series;
	}
	
	public int getIdx() {
		return idx;
	}
}
