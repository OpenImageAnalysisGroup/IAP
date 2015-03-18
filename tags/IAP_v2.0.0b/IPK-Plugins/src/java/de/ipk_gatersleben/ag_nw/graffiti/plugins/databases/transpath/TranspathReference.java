/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

public class TranspathReference extends TranspathEntity {
	
	public String ID, TITLE, REFERENCE, AUTHORS, PUBMEDID, COMPONENT;
	
	public String getKey() {
		return ID;
	}
	
	public String getXMLstartEndEntity() {
		return "Reference";
	}
}
