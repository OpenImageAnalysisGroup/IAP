/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

import java.awt.Color;

import org.color.ColorUtil;

public class kgmlColor {
	
	private Color color;
	
	public kgmlColor(String colorCodeHex) {
		this.color = getColorFromHex(colorCodeHex);
	}
	
	public kgmlColor(Color color) {
		this.color = color;
	}
	
	private Color getColorFromHex(String colorCodeHex) {
		return ColorUtil.getColorFromHex(colorCodeHex);
	}
	
	@Override
	public String toString() {
		return ColorUtil.getHexFromColor(color).toUpperCase();
	}
	
	public static kgmlColor getKgmlColor(String colorValue) {
		if (colorValue == null)
			return null;
		else
			return new kgmlColor(colorValue);
	}
	
	public Color getColor() {
		return color;
	}
	
}
