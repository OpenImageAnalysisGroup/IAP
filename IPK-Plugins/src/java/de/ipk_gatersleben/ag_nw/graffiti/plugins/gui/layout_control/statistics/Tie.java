/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.10.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

public class Tie {
	
	private int vielfachheit;
	private double rankValue;
	private double value;
	
	public Tie(double rankValue, int cntVielfachheit, double value) {
		this.vielfachheit = cntVielfachheit;
		this.rankValue = rankValue;
		this.value = value;
	}
	
	public int getVielfachheit() {
		return vielfachheit;
	}
	
	public double getRankValue() {
		return rankValue;
	}
	
	public double getValue() {
		return value;
	}
}
