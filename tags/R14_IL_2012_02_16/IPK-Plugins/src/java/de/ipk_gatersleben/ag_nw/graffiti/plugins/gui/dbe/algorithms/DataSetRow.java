/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.XPathHelper;

public class DataSetRow {
	
	public int seriesId, replicateID;
	public String rowLabel, experimentName,
						substanceName, mapping, species, genotype, treatment,
						timeS, timeUnit, unit;
	public Double value;
	
	public DataSetRow(String rowLabel,
						String experimentName,
						String substanceName,
						String mapping,
						String species, String genotype, String treatment,
						int seriesId,
						String timeS, String timeUnit, int replicateID, Double value, String unit) {
		this.rowLabel = rowLabel;
		this.experimentName = experimentName;
		this.substanceName = substanceName;
		this.mapping = mapping;
		this.species = species;
		this.genotype = genotype;
		this.treatment = treatment;
		this.seriesId = seriesId;
		this.timeS = timeS;
		this.timeUnit = timeUnit;
		this.replicateID = replicateID;
		this.unit = unit;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return rowLabel + "\t" +
							"\"" + experimentName + "\"" + "\t" +
							"\"" + substanceName + "\"" + "\t" +
							mapping + "\t" +
							"\"" + species + "\"" + "\t" +
							"\"" + genotype + "\"" + "\t" +
							"\"" + treatment + "\"" + "\t" +
							"\"" + seriesId + "\"" + "\t" +
							timeS + "\t" +
							timeUnit + "\t" +
							replicateID + "\t" +
							getNumeric(value) + "\t" +
							"\"" + unit + "\"";
	}
	
	private String getNumeric(Double d) {
		return d.toString();
	}
	
	public static String getHeading() {
		return "\tSubstance\tExperiment\tMapping\tSpecies\tGenotype\tTreatment\tSeries ID\tTime\tTimeUnit\tReplicate\tValue\tUnit";
	}
	
	public String getSeriesFromSpeciesAndGenotype() {
		return getSeriesInformation(species, genotype, treatment);
	}
	
	public static String getSeriesInformation(String linename, String linegenotype, String linetreatment) {
		return XPathHelper.getSeriesNameFromSpeciesGenotypeAndTreatment(linename, linegenotype, linetreatment);
	}
	
	public void setSpeciesGenotypeAndTreatment(String seriesNameContainingSpeciesGenotypeAndTreatment) {
		String serie = seriesNameContainingSpeciesGenotypeAndTreatment;
		this.species = getSpeciesFromSeriesInformation(serie);
		this.genotype = getGenotypeFromSeriesInformation(serie);
		this.treatment = getTreatementFromSeriesInformation(serie);
	}
	
	public static String getSpeciesFromSeriesInformation(String serie) {
		if (serie.indexOf("/") <= 0 && serie.indexOf("(") <= 0)
			return serie;
		else {
			if (serie.indexOf("/") >= 0)
				return serie.substring(0, serie.indexOf("/")).trim();
			else {
				if (serie.indexOf("(") >= 0)
					return serie.substring(0, serie.indexOf("(")).trim();
				else
					return serie;
			}
		}
	}
	
	public static String getGenotypeFromSeriesInformation(String serie) {
		if (serie.indexOf("/") <= 0)
			return "";
		else {
			serie = serie.substring(serie.indexOf("/") + 1);
			if (serie.indexOf("(") > 0)
				return serie.substring(0, serie.indexOf("(")).trim();
			else
				return serie.trim();
		}
	}
	
	public static String getTreatementFromSeriesInformation(String serie) {
		if (serie.indexOf("(") > 0 && serie.indexOf(")") > serie.indexOf("("))
			return serie.substring(serie.indexOf("(") + 1, serie.indexOf(")")).trim();
		else
			return "";
	}
}
