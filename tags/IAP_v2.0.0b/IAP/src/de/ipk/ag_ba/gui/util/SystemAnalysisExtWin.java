package de.ipk.ag_ba.gui.util;

public class SystemAnalysisExtWin {
	
	public static Integer getIdleTimeMillis() {
		try {
			return Win32IdleTime.getIdleTimeMillisWin32();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Integer getUptimeMillis() {
		try {
			return Win32IdleTime.getTickCountWin32();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Integer getLastUserInputTimeMillis() {
		try {
			return Win32IdleTime.getLastUserInputTimeWin32();
		} catch (Exception e) {
			return null;
		}
	}
}
