/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Sep 2, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.gui.navigation_actions;

/**
 * @author klukas
 */
public enum ImageConfiguration {
	FluoSide("fluo.side", "fluo_side_image_unit", "FluoSide"),
	FluoTop("fluo.top", "fluo_top_image_unit", "FluoTop"),
	RgbSide("vis.side", "rgb.side", "rgb_side_image_unit", "rgbside"), RgbTop("vis.top", "rgb.top", "rgb_top_image_unit",
						"RgbTop"), NirTop("nir.top", "nir_top_image_unit", "NirTop"), NirSide("nir.side", "nir_side_image_unit"), Unknown(
						"unknown");
	
	private String name, name2, name3, name4;
	
	ImageConfiguration(String name) {
		this.name = name;
	}
	
	ImageConfiguration(String name, String name2) {
		this.name = name;
		this.name2 = name2;
	}
	
	ImageConfiguration(String name, String name2, String name3) {
		this.name = name;
		this.name2 = name2;
		this.name3 = name3;
	}
	
	ImageConfiguration(String name, String name2, String name3, String name4) {
		this.name = name;
		this.name2 = name2;
		this.name3 = name3;
		this.name4 = name4;
	}
	
	public static ImageConfiguration get(String name) {
		for (ImageConfiguration i : values()) {
			if (name.equalsIgnoreCase(i.name) || name.equalsIgnoreCase(i.name2) || name.equalsIgnoreCase(i.name3)
								|| name.equalsIgnoreCase(i.name4))
				return i;
		}
		return Unknown;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
