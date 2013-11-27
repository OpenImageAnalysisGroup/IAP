/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;

public class IdRef extends Id {
	
	private Entry ref;
	
	public IdRef(Entry ref, String id) {
		super(id);
		this.ref = ref;
	}
	
	public IdRef(String id) {
		super(id);
	}
	
	public void setRef(Entry ref) {
		this.ref = ref;
	}
	
	public Entry getRef() {
		return ref;
	}
	
	public static IdRef getId(String idValue) {
		try {
			return new IdRef(idValue);
		} catch (NumberFormatException nfe) {
			ErrorMsg.addErrorMessage(nfe);
			return null;
		}
	}
	
	@Override
	public String toString() {
		return id + "";
	}
}
