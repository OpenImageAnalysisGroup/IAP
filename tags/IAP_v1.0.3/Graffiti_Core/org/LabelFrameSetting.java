/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org;

public enum LabelFrameSetting {
	NO_FRAME,
	RECTANGLE, ELLIPSE,
	CAPSULE,
	RECTANGLE_BOTTOM_ROUND, RECTANGLE_ROUNDED, RECTANGLE_CORNER_CUT,
	CIRCLE_HALF_FILLED, CIRCLE_FILLED, CIRCLE,
	PIN,
	SIDE_LINES;
	
	@Override
	public String toString() {
		switch (this) {
			case NO_FRAME:
				return "no frame";
			case RECTANGLE:
				return "rectangle frame";
			case ELLIPSE:
				return "ellipse frame";
			case CAPSULE:
				return "capsule frame";
			case RECTANGLE_ROUNDED:
				return "rectangle rounded f.";
			case RECTANGLE_BOTTOM_ROUND:
				return "rectangle bottom r. f.";
			case RECTANGLE_CORNER_CUT:
				return "rectangle corner cut f.";
			case CIRCLE_HALF_FILLED:
				return "circle half filled f.";
			case CIRCLE_FILLED:
				return "circle filled frame";
			case CIRCLE:
				return "circle frame";
			case PIN:
				return "pin";
			case SIDE_LINES:
				return "side lines frame";
			default:
				return null;
		}
	}
	
	public String toGMLstring() {
		switch (this) {
			case NO_FRAME:
				return "";
			case RECTANGLE:
				return "box";
			case ELLIPSE:
				return "oval";
			case CAPSULE:
				return "capsule";
			case RECTANGLE_BOTTOM_ROUND:
				return "roundrect2";
			case RECTANGLE_ROUNDED:
				return "roundrect";
			case RECTANGLE_CORNER_CUT:
				return "cutrect";
			case CIRCLE_HALF_FILLED:
				return "hcircle";
			case CIRCLE_FILLED:
				return "fcircle";
			case CIRCLE:
				return "circle";
			case PIN:
				return "pin";
			case SIDE_LINES:
				return "lines";
			default:
				return null;
		}
	}
	
	public static LabelFrameSetting getSettingFromString(String s) {
		if (s == null || s.length() == 0)
			return NO_FRAME;
		for (LabelFrameSetting lfs : values()) {
			if (lfs != NO_FRAME && s.contains(lfs.toGMLstring()))
				return lfs;
		}
		return LabelFrameSetting.NO_FRAME;
	}
}
