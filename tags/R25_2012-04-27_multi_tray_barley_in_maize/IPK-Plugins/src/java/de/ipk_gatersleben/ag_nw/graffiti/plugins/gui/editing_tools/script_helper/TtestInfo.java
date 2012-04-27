package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import org.ErrorMsg;

public enum TtestInfo {
	EMPTY, REFERENCE, H0, H1;
	
	public static TtestInfo getValueFromString(String value) {
		if (value == null || value.length() == 0)
			return EMPTY;
		if (value.equals("reference"))
			return REFERENCE;
		if (value.equals("H0"))
			return H0;
		if (value.equals("H1"))
			return H1;
		return EMPTY;
	}
	
	public String toString() {
		switch (this) {
			case EMPTY:
				return "";
			case REFERENCE:
				return "reference";
			case H0:
				return "H0";
			case H1:
				return "H1";
			default:
				ErrorMsg.addErrorMessage("Internal Error: Unknown TtestInfo enum value");
				return null;
		}
	}
}
