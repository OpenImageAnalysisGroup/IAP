/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public class MapOrg {
	private String org;
	
	public MapOrg(String org) {
		assert org != null;
		this.org = org;
	}
	
	public boolean isReferencePathway() {
		return org.equalsIgnoreCase("map") || org.equalsIgnoreCase("ko");
	}
	
	public boolean isReferencePathwayUsingECids() {
		return org.equalsIgnoreCase("map");
	}
	
	public boolean isReferencePathwayUsingKOids() {
		return org.equalsIgnoreCase("ko");
	}
	
	public boolean isHumanPathwayUsingOMIMids() {
		return org.equalsIgnoreCase("mim");
	}
	
	@Override
	public String toString() {
		return org;
	}
}
