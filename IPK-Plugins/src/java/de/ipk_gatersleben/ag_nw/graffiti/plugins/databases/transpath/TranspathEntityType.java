/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

public interface TranspathEntityType {
	public String getKey();
	
	public void processXMLentityValue(String environment, String value);
}
