/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public enum kgmlGraphicsType {
	rectangle, circle, roundrectangle, line;
	
	public static kgmlGraphicsType getGraphicsType(String typeValue) {
		if (typeValue.equals("rectangle"))
			return rectangle;
		if (typeValue.equals("circle"))
			return circle;
		if (typeValue.equals("roundrectangle"))
			return roundrectangle;
		if (typeValue.equals("line"))
			return line;
		return null;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case rectangle:
				return "rectangle";
			case circle:
				return "circle";
			case roundrectangle:
				return "roundrectangle";
			case line:
				return "line";
		}
		return null;
	}
}
