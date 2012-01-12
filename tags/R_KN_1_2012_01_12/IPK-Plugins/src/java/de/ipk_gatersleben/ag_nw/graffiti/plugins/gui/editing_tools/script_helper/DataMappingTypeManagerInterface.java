package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public interface DataMappingTypeManagerInterface {
	
	public ConditionInterface getNewCondition(SubstanceInterface substance);
	
	public SampleInterface getNewSample(ConditionInterface condition);
	
	public NumericMeasurementInterface getNewMeasurement(SampleInterface sample);
	
	public SubstanceInterface getNewSubstance();
	
	public SampleAverage getNewSampleAverage(SampleInterface sample);
}