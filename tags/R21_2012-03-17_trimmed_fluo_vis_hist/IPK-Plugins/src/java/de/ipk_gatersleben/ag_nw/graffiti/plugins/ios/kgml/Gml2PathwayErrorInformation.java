/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 24.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.attributes.Attributable;

public class Gml2PathwayErrorInformation {
	
	private Gml2PathwayError error;
	private ArrayList<Attributable> errorNodesOrEdges;
	private Attributable errorSource;
	private String description;
	
	public Gml2PathwayErrorInformation(Gml2PathwayError error, Attributable errorSource) {
		this.error = error;
		this.errorSource = errorSource;
	}
	
	public Gml2PathwayErrorInformation(Gml2PathwayError error, ArrayList<Attributable> errorSource) {
		this.error = error;
		this.errorNodesOrEdges = errorSource;;
	}
	
	public Gml2PathwayErrorInformation(Gml2PathwayError error, ArrayList<Attributable> errorSource, String description) {
		this(error, errorSource);
		this.description = description;
	}
	
	public Gml2PathwayError getError() {
		return error;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Collection<Attributable> getCausingGraphElements() {
		Collection<Attributable> result = new ArrayList<Attributable>();
		if (errorNodesOrEdges != null)
			result.addAll(errorNodesOrEdges);
		if (errorSource != null)
			result.add(errorSource);
		return result;
	}
	
	public void printMessageToConsole() {
		System.out.println("ERROR: " + toString());
	}
	
	@Override
	public String toString() {
		if (errorSource != null)
			return error.toString() + " Source: " + errorSource.toString();
		else
			return error.toString() + " Source: n/a";
	}
}
