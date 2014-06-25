/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 10.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.kegg;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class OrganismEntry {
	
	private String shortName;
	private String title;
	private boolean showHierarchy;
	private String hierarchy = "";
	
	public OrganismEntry(String shortName, String title, String hierarchy) {
		this.shortName = shortName;
		this.title = title;
		this.showHierarchy = hierarchy != null && !hierarchy.isEmpty();
		this.hierarchy = hierarchy;
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
	
	public String getHierarchy() {
		return hierarchy;
	}
}
