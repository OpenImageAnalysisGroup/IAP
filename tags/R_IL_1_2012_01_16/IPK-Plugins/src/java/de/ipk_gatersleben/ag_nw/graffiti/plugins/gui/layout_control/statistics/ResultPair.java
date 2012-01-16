/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

public class ResultPair {
	
	public Object a;
	public Object b;
	public CorrelationResult correlation;
	
	public ResultPair(Object a, Object b, CorrelationResult correlation) {
		this.a = a;
		this.b = b;
		this.correlation = correlation;
	}
}