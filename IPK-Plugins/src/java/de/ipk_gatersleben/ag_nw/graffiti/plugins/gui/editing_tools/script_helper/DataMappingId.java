/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class DataMappingId implements Comparable<DataMappingId> {
	
	protected String sid;
	protected String expName;
	protected String seriesName; // species and genotype
	protected String species;
	protected String genotype;
	protected String sampleId;
	protected String timeP;
	protected String timeU;
	protected Integer replicateId;
	protected int seriesId;
	
	public DataMappingId(int seriesId, String expName, String seriesName, String species, String genotype,
						String sampleId, String timeP, String timeU, Integer replicateId) {
		this.seriesId = seriesId;
		this.expName = expName;
		this.seriesName = seriesName;
		this.species = species;
		this.genotype = genotype;
		this.sampleId = sampleId;
		this.timeP = timeP;
		this.timeU = timeU;
		this.replicateId = replicateId;
		String d = "_�_";
		this.sid = expName + d + seriesName + d + timeP + d + timeU + d + replicateId;
	}
	
	public static DataMappingId getEmptyDataMappingWithoutReplicateInformation(
						int seriesId,
						String expName, String seriesName,
						String species, String genotype,
						String rowId,
						String timeP, String timeU) {
		return new DataMappingId(seriesId, expName, seriesName, species, genotype, rowId, timeP, timeU, null);
	}
	
	public DataMappingId getFullDataMappingIdForReplicate(int replicateId) {
		return new DataMappingId(seriesId, expName, seriesName, species, genotype, sampleId, timeP, timeU, replicateId);
	}
	
	@Override
	public boolean equals(Object obj) {
		return sid.equals(((DataMappingId) obj).sid);
	}
	
	@Override
	public int hashCode() {
		return sid.hashCode();
	}
	
	@Override
	public String toString() {
		return sid;
	}
	
	public String getExperimentName() {
		return expName;
	}
	
	public String getSeriesName() {
		return seriesName;
	}
	
	public int getTimePoint() {
		return Integer.parseInt(timeP);
	}
	
	public String getTimeUnit() {
		return timeU;
	}
	
	public Integer getReplicateId() {
		return replicateId;
	}
	
	public String getSpecies() {
		return species;
	}
	
	public String getGenoType() {
		return genotype;
	}
	
	public int compareTo(DataMappingId dmi) {
		if (dmi.seriesId > seriesId)
			return -1;
		if (dmi.seriesId < seriesId)
			return 1;
		return dmi.sid.compareTo(sid);
	}
}
