/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public class SubtypeValue {
	
	private String value;
	
	public SubtypeValue(SubtypeName fromName) {
		assert fromName != SubtypeName.compound;
		assert fromName != SubtypeName.hiddenCompound;
		
		this.value = getValue(fromName);
	}
	
	public SubtypeValue(IdRef fromId) {
		this.value = fromId.toString();
	}
	
	public static String getValue(SubtypeName fromName) {
		if (fromName != null)
			switch (fromName) {
				// case activation : return "-a->";
				case activation:
					return "";
					// case inhibition : return "-i-|";
					// case expression : return "-->";
				case expression:
					return "e";
				case repression:
					return "r";
				case indirectEffect:
					return ".i.>";
				case stateChange:
					return ".s..";
					// case binding_association : return "-b-a-";
				case binding_association:
					return "";
				case dissociation:
					return "-+-";
				case phosphorylation:
					return "+p";
				case dephosphorylation:
					return "-p";
				case glycosylation:
					return "+g";
				case ubiquination:
					return "+u";
				case methylation:
					return "+m";
				case demethylation:
					return "-m";
			}
		return null;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
