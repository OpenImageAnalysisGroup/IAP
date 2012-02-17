/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg_organisms.OrganismInfo;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class OrganismEntry {
	
	private String shortName;
	private String title;
	private boolean showHierarchy;
	private String hierarchy = "";
	
	public OrganismEntry(String shortName, String title) {
		this.shortName = shortName;
		this.title = title;
		this.showHierarchy = true;
		if (showHierarchy) {
			hierarchy = OrganismInfo.getOrganismHierarchyInfo(
								shortName, "/", title);
		}
	}
	
	public OrganismEntry(boolean showHierarchy, String shortName, String title) {
		this.shortName = shortName;
		this.title = title;
		this.showHierarchy = showHierarchy;
		if (showHierarchy) {
			hierarchy = OrganismInfo.getOrganismHierarchyInfo(shortName, "/", title);
		}
	}
	
	@Override
	public String toString() {
		if (showHierarchy) {
			if (hierarchy == null || hierarchy.length() <= 0)
				return "- " + title + " - " + shortName;
			else
				return "<html><font color='gray'><small>" + hierarchy + ":</small></font> "
									+ title + "<font color='gray'><small> (" + shortName + ")";
		} else {
			return title + " - " + shortName;
		}
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getDefinition() {
		return title;
	}
}
