package de.ipk.ag_ba.gui.webstart;

public enum IAPrunMode {
	CONSOLE, CLOUD_HOST_BATCH_MODE, WEB, CLOUD_HOST, SWING_MAIN, SWING_APPLET, UNKNOWN;
	
	public boolean isSwing() {
		return this == SWING_MAIN || this == IAPrunMode.SWING_APPLET;
	}
}
