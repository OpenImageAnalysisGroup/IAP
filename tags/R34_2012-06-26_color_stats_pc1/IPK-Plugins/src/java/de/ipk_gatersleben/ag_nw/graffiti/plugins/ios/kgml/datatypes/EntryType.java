/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public enum EntryType {
	ortholog, enzyme, gene, genes, group, compound, map, unspecified, undefined, hiddenCompound, other;
	
	public static EntryType getEntryType(String entryType) {
		if (entryType.equals("ortholog"))
			return EntryType.ortholog;
		if (entryType.equals("enzyme"))
			return EntryType.enzyme;
		if (entryType.equals("gene"))
			return EntryType.gene;
		if (entryType.equals("genes"))
			return EntryType.genes;
		if (entryType.equals("genes (obsolete)"))
			return EntryType.genes;
		if (entryType.equals("group"))
			return EntryType.group;
		if (entryType.equals("compound"))
			return EntryType.compound;
		if (entryType.equals("hidden compound"))
			return EntryType.hiddenCompound;
		if (entryType.equals("other"))
			return EntryType.other;
		if (entryType.equals("map"))
			return EntryType.map;
		if (entryType.equals(""))
			return EntryType.unspecified;
		if (entryType.equals("- unspecified -"))
			return EntryType.unspecified;
		if (entryType.equals("undefined"))
			return EntryType.undefined;
		for (EntryType et : values())
			if (et.getDescription().equals(entryType))
				return et;
		return null;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case hiddenCompound:
				return "hidden compound";
			case unspecified:
				return "";
		}
		return super.toString();
	}
	
	public String getDescription() {
		switch (this) {
			case unspecified:
				return "- unspecified -";
			case genes:
				return "genes (obsolete)";
		}
		return toString();
	}
}
