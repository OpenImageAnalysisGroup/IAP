package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author klukas
 */
public class StopObject {
	
	private boolean abortWanted;
	
	public StopObject(boolean b) {
		abortWanted = b;
	}
	
	public synchronized boolean shouldStop() {
		return abortWanted;
	}
	
	public synchronized void setStopWanted(boolean b) {
		abortWanted = b;
	}
	
}
