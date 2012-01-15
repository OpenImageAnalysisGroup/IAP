/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class TimeAndPlantName {
	
	private String plant;
	private int time;
	
	public TimeAndPlantName(String plant, int time) {
		this.plant = plant;
		this.time = time;
	}
	
	public String getPlant() {
		return plant;
	}
	
	public int getTime() {
		return time;
	}
	
	@Override
	public String toString() {
		return plant + " [t=" + time + "]";
	}
}
