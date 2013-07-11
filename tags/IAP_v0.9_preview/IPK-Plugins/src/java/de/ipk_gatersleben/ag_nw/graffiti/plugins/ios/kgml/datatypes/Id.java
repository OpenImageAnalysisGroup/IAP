/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;

public class Id {
	protected String id;
	
	public Id(String id) {
		this.id = id;
	}
	
	public Id() {
		this(Pathway.getNextID() + "");
	}
	
	public static Id getId(String idValue) {
		return new Id(idValue);
	}
	
	public String getValue() {
		return id;
	}
	
	public void setValue(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return id;
	}
	
	public boolean matches(String valueIdRef) {
		return id.equals(valueIdRef);
	}
}
