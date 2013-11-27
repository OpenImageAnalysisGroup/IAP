/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public enum ReactionType {
	reversible, irreversible;
	
	public static ReactionType getReactiontype(String typeValue) {
		if (typeValue == null)
			return null;
		if (typeValue.equals("reversible"))
			return reversible;
		if (typeValue.equals("irreversible"))
			return irreversible;
		return null;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case reversible:
				return "reversible";
			case irreversible:
				return "irreversible";
		}
		return null;
	}
}
