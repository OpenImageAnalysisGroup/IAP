/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class MemPlant {
	
	private String species;
	private String genotype;
	private String optVariety;
	private String optTreatment;
	
	public MemPlant(String species, String genotype, String optVariety, String optGrowthConditions, String optTreatment) {
		this.species = species;
		this.genotype = genotype;
		this.optVariety = optVariety;
		this.optTreatment = optTreatment;
	}
	
	public String getSpecies() {
		return species;
	}
	
	public String getVariety() {
		return optVariety != null ? optVariety : "";
	}
	
	public String getGenotype() {
		return genotype;
	}
	
	public String getTreatment() {
		return optTreatment != null ? optTreatment : "";
	}
}
