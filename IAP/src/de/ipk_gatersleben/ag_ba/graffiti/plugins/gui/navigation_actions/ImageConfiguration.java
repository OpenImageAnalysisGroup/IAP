/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Sep 2, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

/**
 * @author klukas
 * 
 */
public enum ImageConfiguration {
	FluoSide("FluoSide"), FluoTop("FluoTop"), RgbSide("RgbSide"), RgbTop("RgbTop");

	private String name;

	ImageConfiguration(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
