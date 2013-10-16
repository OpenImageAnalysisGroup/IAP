package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

public interface NumericMeasurementInterface extends MappingDataEntity, Measurement {
	
	public abstract void getString(StringBuilder r);
	
	public abstract void getXMLAttributeString(StringBuilder r);
	
	public abstract void getStringOfChildren(StringBuilder r);
	
	public abstract double getValue();
	
	public abstract SampleInterface getParentSample();
	
	public abstract String getUnit();
	
	public abstract void setUnit(String unit);
	
	public abstract boolean setData(Element averageElement);
	
	public abstract void setAttribute(Attribute attr);
	
	public abstract void setDataOfChildElement(Element childElement);
	
	public abstract void setValue(double value);
	
	public abstract void setReplicateID(int replicateID);
	
	public abstract int getReplicateID();
	
	public abstract void setParentSample(SampleInterface sample);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	public abstract void fillAttributeMap(Map<String, Object> attributes);
	
	public abstract void setQualityAnnotation(String optionalQualityAnnotation);
	
	public abstract String getQualityAnnotation();
	
	public NumericMeasurementInterface clone(SampleInterface parent);
}