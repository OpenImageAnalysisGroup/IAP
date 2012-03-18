/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Reaction;

public class KeggId {
	private String id;
	private Entry entryRef;
	private Pathway pathwayRef;
	private Reaction reactionRef;
	
	public KeggId(String id) {
		assert id != null;
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean isPathwayMap() {
		return id.startsWith("path:");
	}
	
	public boolean isKoOrthologGroup() {
		return id.startsWith("ko:");
	}
	
	public boolean isEcEnzyme() {
		return id.startsWith("ec:");
	}
	
	public boolean isCpdChemicalCompound() {
		return id.startsWith("cpd:");
	}
	
	public boolean isGlGlycan() {
		return id.startsWith("gl:") || id.startsWith("glycan:");
	}
	
	public boolean isGroupComplexOfKOs() {
		return id.startsWith("group:");
	}
	
	public boolean isReaction() {
		return id.startsWith("rn:");
	}
	
	public Entry getEntry() {
		return entryRef;
	}
	
	public boolean isGeneProductOfGivenOrganism() {
		// TODO: difficult to check, would need to check for correct organism code
		return id.contains(":");
	}
	
	public void setReference(Entry entryRef) {
		this.entryRef = entryRef;
	}
	
	public void setReference(Pathway pathwayRef) {
		this.pathwayRef = pathwayRef;
	}
	
	public void setReference(Reaction reactionRef) {
		this.reactionRef = reactionRef;
	}
	
	public static KeggId getKeggId(String id) {
		if (id == null)
			return null;
		else
			return new KeggId(id);
	}
	
	public Reaction getReferenceReaction() {
		return reactionRef;
	}
	
	public Pathway getReferencePathway() {
		return pathwayRef;
	}
	
	public Entry getReference() {
		return entryRef;
	}
	
	public Object getIdGlycanProcessed() {
		if (id.startsWith("glycan:"))
			return "gl:" + id.substring("glycan:".length());
		else
			return id;
	}
}
