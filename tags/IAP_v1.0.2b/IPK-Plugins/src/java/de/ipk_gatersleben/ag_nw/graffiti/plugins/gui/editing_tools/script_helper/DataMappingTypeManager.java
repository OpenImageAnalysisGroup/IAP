package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public class DataMappingTypeManager implements DataMappingTypeManagerInterface {
	
	@Override
	public SampleInterface getNewSample(ConditionInterface condition) {
		return new Sample(condition);
	}
	
	@Override
	public NumericMeasurementInterface getNewMeasurement(SampleInterface sample) {
		return new NumericMeasurement(sample);
	}
	
	@Override
	public ConditionInterface getNewCondition(SubstanceInterface md) {
		return new Condition(md);
	}
	
	@Override
	public SubstanceInterface getNewSubstance() {
		return new Substance();
	}
	
	@Override
	public SampleAverageInterface getNewSampleAverage(SampleInterface sample) {
		return new SampleAverage(sample);
	}
	
	@Override
	public NumericMeasurementInterface getNewMeasurementOfType(String type, SampleInterface sample) {
		if (type != null && type.equals(NumericMeasurement.typeName))
			return getNewMeasurement(sample);
		else
			return null;
	}
	
	@Override
	public boolean isKnownMeasurementType(String type) {
		return type.equals(NumericMeasurement.typeName);
	}
}
