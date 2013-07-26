/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

class KgmlIdGenerator {
	
	private int currentID = 1;
	
	public synchronized int getNextID() {
		int result = currentID;
		currentID++;
		return result;
	}
	
	public void reset() {
		currentID = 1;
	}
}