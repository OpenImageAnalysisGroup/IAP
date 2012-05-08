/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import org.graffiti.attributes.Attributable;

public class Gml2PathwayWarningInformation {
	
	private Gml2PathwayWarning warning;
	private Attributable warningSource;
	
	public Gml2PathwayWarningInformation(Gml2PathwayWarning warning, Attributable warningSource) {
		this.warning = warning;
		this.warningSource = warningSource;
	}
	
	public Gml2PathwayWarning getWarning() {
		return warning;
	}
	
	public Attributable getCausingGraphElement() {
		return warningSource;
	}
	
	public void printMessageToConsole() {
		System.out.println("WARNING: " + toString());
	}
	
	@Override
	public String toString() {
		if (warningSource != null)
			return warning.toString() + " Source: " + warningSource.toString();
		else
			return warning.toString() + " Source: n/a";
	}
}
