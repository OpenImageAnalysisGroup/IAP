/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Sep 2, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.server.analysis;

/**
 * @author klukas
 */
public enum ImageConfiguration {
	FluoSide, FluoTop,
	VisSide, VisTop,
	NirTop, NirSide,
	IrSide, IrTop,
	Unknown;
	
	private static ImageConfiguration guess(String name) {
		if (name == null || name.length() == 0)
			return Unknown;
		name = name.toUpperCase();
		if (name.equals("DUMMY SUBSTANCE"))
			return ImageConfiguration.VisTop;
		if (name.contains("FLU") && name.contains("TOP"))
			return ImageConfiguration.FluoTop;
		if (name.contains("FLU"))
			return ImageConfiguration.FluoSide;
		
		if (name.contains("VIS") && name.contains("TOP"))
			return ImageConfiguration.VisTop;
		if (name.contains("VIS"))
			return ImageConfiguration.VisSide;
		if (name.contains("RGB") && name.contains("TOP"))
			return ImageConfiguration.VisTop;
		if (name.contains("RGB"))
			return ImageConfiguration.VisSide;
		
		if (name.contains("NIR") && name.contains("TOP"))
			return ImageConfiguration.NirTop;
		if (name.contains("NIR"))
			return ImageConfiguration.NirSide;
		
		if (name.contains("IR") && name.contains("TOP"))
			return ImageConfiguration.IrTop;
		if (name.contains("IR"))
			return ImageConfiguration.IrSide;
		
		return Unknown;
	}
	
	public boolean isSide() {
		switch (this) {
			case VisSide:
			case FluoSide:
			case NirSide:
			case IrSide:
				return true;
			case VisTop:
			case FluoTop:
			case NirTop:
			case IrTop:
				return false;
			case Unknown:
				throw new UnsupportedOperationException(
						"ERROR: Can't decide if image is taken from side or top configuration as image configuration is undefined (Unknown)!");
		}
		throw new UnsupportedOperationException(
				"ERROR: NO CASE MATCH (Internal Error 1, Error 2: Can't decide if image is taken from side or top configuration as image configuration is undefined (Unknown)!");
	}
	
	public static ImageConfiguration get(String substanceName) {
		return guess(substanceName);
	}
}
