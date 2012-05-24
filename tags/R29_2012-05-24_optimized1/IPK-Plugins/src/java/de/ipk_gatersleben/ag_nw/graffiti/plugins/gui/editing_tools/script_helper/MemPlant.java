/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.02.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class MemPlant {

	private final String species;
	private final String genotype;
	private final String optVariety;
	private final String optGrowthConditions;
	private final String optTreatment;
	private final int conditionID;

	public MemPlant(int conditionID, String species, String genotype,
			String optVariety, String optGrowthConditions, String optTreatment) {
		this.conditionID = conditionID;
		this.species = species;
		this.genotype = genotype;
		this.optVariety = optVariety;
		this.optGrowthConditions = optGrowthConditions;
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

	public String getGrowthConditions() {
		return optGrowthConditions != null ? optGrowthConditions : "";
	}

	public String getTreatment() {
		return optTreatment != null ? optTreatment : "";
	}

	public int getConditionID() {
		return conditionID;
	}
}
