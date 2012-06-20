/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 05.08.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

public class SetAbleBoolean {
	boolean value = false;
	
	public SetAbleBoolean(boolean initValue) {
		value = initValue;
	}
	
	public void setValue(boolean value) {
		this.value = value;
	}
	
	public boolean getValue() {
		return value;
	}
}
