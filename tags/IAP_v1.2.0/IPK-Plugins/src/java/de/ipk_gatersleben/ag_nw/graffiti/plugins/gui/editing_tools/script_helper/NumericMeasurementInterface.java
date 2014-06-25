package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Map;

import org.jdom.Element;

public interface NumericMeasurementInterface extends Measurement {
	
	public abstract void getString(StringBuilder r);
	
	@Override
	public abstract void getXMLAttributeString(StringBuilder r);
	
	@Override
	public abstract void getStringOfChildren(StringBuilder r);
	
	@Override
	public abstract double getValue();
	
	@Override
	public abstract SampleInterface getParentSample();
	
	public abstract String getUnit();
	
	public abstract void setUnit(String unit);
	
	@Override
	public abstract boolean setData(Element averageElement);
	
	@Override
	public abstract void setAttribute(MyAttribute attr);
	
	@Override
	public abstract void setDataOfChildElement(Element childElement);
	
	public abstract void setValue(double value);
	
	public abstract void setReplicateID(int replicateID);
	
	@Override
	public abstract int getReplicateID();
	
	public abstract void setParentSample(SampleInterface sample);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	@Override
	public abstract void fillAttributeMap(Map<String, Object> attributes);
	
	public abstract void setQualityAnnotation(String optionalQualityAnnotation);
	
	public abstract String getQualityAnnotation();
	
	public NumericMeasurementInterface clone(SampleInterface parent);
}