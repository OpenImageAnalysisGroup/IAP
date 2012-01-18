/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 14.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti;

public enum NodeSortCommand {
	dontSort("Do not sort"), sortLabel("Sort by label (A..Z)"), sortLabelInverse("Sort by label (Z..A)"), sortRatio("Sort by ratio (min->max)"), sortRatioInverse(
						"Sort by ratio (max->min)");
	
	private String name;
	
	NodeSortCommand(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
