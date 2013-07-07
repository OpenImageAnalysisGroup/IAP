package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class DataMappingTypeManager implements DataMappingTypeManagerInterface {
	
	public SampleInterface getNewSample(ConditionInterface condition) {
		return new Sample(condition);
	}
	
	public NumericMeasurementInterface getNewMeasurement(SampleInterface sample) {
		return new NumericMeasurement(sample);
	}
	
	public ConditionInterface getNewCondition(SubstanceInterface md) {
		return new Condition(md);
	}
	
	public SubstanceInterface getNewSubstance() {
		return new Substance();
	}
	
	@Override
	public SampleAverage getNewSampleAverage(SampleInterface sample) {
		return new SampleAverage(sample);
	}
}
