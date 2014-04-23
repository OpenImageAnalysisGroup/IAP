/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.07.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.HashSet;

public enum SearchType {
	searchString,
	searchInteger,
	searchDouble,
	searchBoolean,
	searchColor;
	
	@Override
	public String toString() {
		if (equals(SearchType.searchInteger))
			return "Number [int]";
		if (equals(SearchType.searchString))
			return "Text";
		if (equals(SearchType.searchDouble))
			return "Number [double]";
		if (equals(SearchType.searchBoolean))
			return "Boolean";
		if (equals(SearchType.searchColor))
			return "Color";
		return "Unknown";
	}
	
	public static HashSet<SearchType> getSetOfSearchTypes() {
		HashSet<SearchType> result = new HashSet<SearchType>();
		for (SearchType st : values()) {
			if (st != SearchType.searchColor)
				result.add(st);
		}
		return result;
	}
	
	public static HashSet<SearchType> getSetOfNumericSearchTypes() {
		HashSet<SearchType> result = new HashSet<SearchType>();
		result.add(searchInteger);
		result.add(searchDouble);
		return result;
	}
	
	public static HashSet<SearchType> getSetOfColorSearchTypes() {
		HashSet<SearchType> result = new HashSet<SearchType>();
		result.add(searchColor);
		return result;
	}
}
