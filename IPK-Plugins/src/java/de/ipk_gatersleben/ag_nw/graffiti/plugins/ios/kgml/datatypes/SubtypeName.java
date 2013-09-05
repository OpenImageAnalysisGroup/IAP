/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

import org.ErrorMsg;

public enum SubtypeName {
	compound, hiddenCompound, activation, inhibition, expression, repression,
	indirectEffect, stateChange, binding_association, dissociation, phosphorylation,
	dephosphorylation, glycosylation, ubiquination, methylation, demethylation, indirect, state, missing,
	missing_interaction;
	
	@Override
	public String toString() {
		if (this == hiddenCompound)
			return "hidden compound";
		if (this == indirectEffect)
			return "indirect effect";
		if (this == stateChange)
			return "state change";
		if (this == binding_association)
			return "binding/association";
		if (this == missing_interaction)
			return "missing interaction";
		return super.toString();
	}
	
	public static SubtypeName getSubtypeName(String value) {
		for (SubtypeName sn : SubtypeName.values()) {
			if (sn.toString().equals(value))
				return sn;
		}
		if (value.equals("missing interaction"))
			return missing_interaction;
		if (value.equals("binding/association"))
			return binding_association;
		ErrorMsg.addErrorMessage("Unknown Subtype: " + value);
		return null;
	}
}
