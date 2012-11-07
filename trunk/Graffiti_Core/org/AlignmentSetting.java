/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org;

import org.graffiti.graphics.GraphicAttributeConstants;

public enum AlignmentSetting {
	HIDDEN,
	AUTO_OUTSIDE, BELOW, ABOVE, RIGHT, LEFT, INSIDEBOTTOM, INSIDETOP,
	INSIDETOPLEFT, INSIDETOPRIGHT, INSIDEBOTTOMLEFT, INSIDEBOTTOMRIGHT,
	INSIDELEFT, INSIDERIGHT,
	CENTERED, BELOWRIGHT, BELOWLEFT, ABOVELEFT, ABOVERIGHT, NEARSOURCE,
	NEARTARGET, BORDER_TOP_LEFT, BORDER_TOP_CENTER,
	BORDER_TOP_RIGHT, BORDER_BOTTOM_LEFT, BORDER_BOTTOM_CENTER,
	BORDER_BOTTOM_RIGHT, BORDER_LEFT_TOP, BORDER_LEFT_CENTER,
	BORDER_LEFT_BOTTOM, BORDER_RIGHT_TOP, BORDER_RIGHT_CENTER,
	BORDER_RIGHT_BOTTOM;
	
	@Override
	public String toString() {
		switch (this) {
			case HIDDEN:
				return "hidden (not shown)";
			case AUTO_OUTSIDE:
				return "outside (automatic)";
			case BELOW:
				return "below";
			case ABOVE:
				return "above";
			case RIGHT:
				return "right";
			case LEFT:
				return "left";
			case INSIDEBOTTOM:
				return "inside, bottom";
			case INSIDETOP:
				return "inside, top";
			case INSIDEBOTTOMLEFT:
				return "inside, bottom-left";
			case INSIDEBOTTOMRIGHT:
				return "inside, bottom-right";
			case INSIDETOPRIGHT:
				return "inside, top-right";
			case INSIDETOPLEFT:
				return "inside, top-left";
			case INSIDELEFT:
				return "inside, left";
			case INSIDERIGHT:
				return "inside, right";
			case CENTERED:
				return "centered";
			case BELOWRIGHT:
				return "below, right";
			case BELOWLEFT:
				return "below, left";
			case ABOVELEFT:
				return "above, left";
			case ABOVERIGHT:
				return "above, right";
			case NEARSOURCE:
				return "nearsource (not impl.)";
			case NEARTARGET:
				return "neartarget (not impl.)";
			case BORDER_TOP_LEFT:
				return "border, top-left";
			case BORDER_TOP_CENTER:
				return "border, top-center";
			case BORDER_TOP_RIGHT:
				return "border, top-right";
			case BORDER_RIGHT_TOP:
				return "border, right-top";
			case BORDER_RIGHT_CENTER:
				return "border, right-center";
			case BORDER_RIGHT_BOTTOM:
				return "border, right-bottom";
			case BORDER_BOTTOM_LEFT:
				return "border, bottom-left";
			case BORDER_BOTTOM_CENTER:
				return "border, bottom-center";
			case BORDER_BOTTOM_RIGHT:
				return "border, bottom-right";
			case BORDER_LEFT_TOP:
				return "border, left-top";
			case BORDER_LEFT_CENTER:
				return "border, left-center";
			case BORDER_LEFT_BOTTOM:
				return "border, left-bottom";
		}
		return null;
		
	}
	
	public String toGMLstring() {
		switch (this) {
			case HIDDEN:
				return "hidden";
			case AUTO_OUTSIDE:
				return "auto_outside";
			case BELOW:
				return GraphicAttributeConstants.BELOW;
			case ABOVE:
				return GraphicAttributeConstants.ABOVE;
			case RIGHT:
				return GraphicAttributeConstants.RIGHT;
			case LEFT:
				return GraphicAttributeConstants.LEFT;
			case INSIDEBOTTOM:
				return GraphicAttributeConstants.INSIDEBOTTOM;
			case INSIDEBOTTOMLEFT:
				return GraphicAttributeConstants.INSIDEBOTTOMLEFT;
			case INSIDEBOTTOMRIGHT:
				return GraphicAttributeConstants.INSIDEBOTTOMRIGHT;
			case INSIDETOP:
				return GraphicAttributeConstants.INSIDETOP;
			case INSIDELEFT:
				return GraphicAttributeConstants.INSIDELEFT;
			case INSIDERIGHT:
				return GraphicAttributeConstants.INSIDERIGHT;
			case INSIDETOPLEFT:
				return GraphicAttributeConstants.INSIDETOPLEFT;
			case INSIDETOPRIGHT:
				return GraphicAttributeConstants.INSIDETOPRIGHT;
			case CENTERED:
				return GraphicAttributeConstants.CENTERED;
			case BELOWRIGHT:
				return GraphicAttributeConstants.BELOWRIGHT;
			case BELOWLEFT:
				return GraphicAttributeConstants.BELOWLEFT;
			case ABOVELEFT:
				return GraphicAttributeConstants.ABOVELEFT;
			case ABOVERIGHT:
				return GraphicAttributeConstants.ABOVERIGHT;
			case NEARSOURCE:
				return "nearsource";
			case NEARTARGET:
				return "neartarget";
			case BORDER_TOP_LEFT:
				return GraphicAttributeConstants.BORDER_TOP_LEFT;
			case BORDER_TOP_CENTER:
				return GraphicAttributeConstants.BORDER_TOP_CENTER;
			case BORDER_TOP_RIGHT:
				return GraphicAttributeConstants.BORDER_TOP_RIGHT;
			case BORDER_RIGHT_TOP:
				return GraphicAttributeConstants.BORDER_RIGHT_TOP;
			case BORDER_RIGHT_CENTER:
				return GraphicAttributeConstants.BORDER_RIGHT_CENTER;
			case BORDER_RIGHT_BOTTOM:
				return GraphicAttributeConstants.BORDER_RIGHT_BOTTOM;
			case BORDER_BOTTOM_LEFT:
				return GraphicAttributeConstants.BORDER_BOTTOM_LEFT;
			case BORDER_BOTTOM_CENTER:
				return GraphicAttributeConstants.BORDER_BOTTOM_CENTER;
			case BORDER_BOTTOM_RIGHT:
				return GraphicAttributeConstants.BORDER_BOTTOM_RIGHT;
			case BORDER_LEFT_TOP:
				return GraphicAttributeConstants.BORDER_LEFT_TOP;
			case BORDER_LEFT_CENTER:
				return GraphicAttributeConstants.BORDER_LEFT_CENTER;
			case BORDER_LEFT_BOTTOM:
				return GraphicAttributeConstants.BORDER_LEFT_BOTTOM;
		}
		return null;
	}
}
