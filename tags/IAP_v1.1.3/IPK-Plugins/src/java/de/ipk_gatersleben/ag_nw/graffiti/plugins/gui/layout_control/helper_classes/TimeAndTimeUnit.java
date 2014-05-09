/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.06.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes;

public class TimeAndTimeUnit {
	
	private String time;
	private String timeAndUnit;
	private String unit;
	
	public TimeAndTimeUnit(String time, String unit) {
		this.time = time;
		this.unit = unit;
		this.timeAndUnit = unit + " " + time;
	}
	
	public Double getTime() {
		return Double.parseDouble(time);
	}
	
	@Override
	public String toString() {
		if (timeAndUnit.equals("-1 -1"))
			return XPathHelper.noGivenTimeStringConstant;
		else
			return timeAndUnit;
	}
	
	public String getTimeUnit() {
		return unit;
	}
}
