package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Map;

import org.MeasurementFilter;
import org.jdom.Element;

public interface SampleAverageInterface extends Measurement {
	
	public abstract void getString(StringBuilder r);
	
	public abstract double getValue(MeasurementFilter pf);
	
	@Override
	public abstract SampleInterface getParentSample();
	
	public abstract void calculateValuesFromSampleData();
	
	public abstract double getStdDev();
	
	public abstract String getUnit();
	
	@Override
	public abstract boolean setData(Element averageElement);
	
	public abstract void setUnit(String ownUnit);
	
	public abstract void setReplicateId(int replicates);
	
	@Override
	public abstract int getReplicateID();
	
	public abstract void setMax(double max);
	
	public abstract double getMax();
	
	public abstract void setMin(double min);
	
	public abstract double getMin();
	
	public abstract void setStddev(double stddev);
	
	public abstract double getStddev();
	
	public abstract void setValue(double value);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#getStringOfChildren(java.lang.StringBuilder)
	 */
	@Override
	public abstract void getStringOfChildren(StringBuilder r);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#getXMLAttributeString(java.lang.StringBuilder)
	 */
	@Override
	public abstract void getXMLAttributeString(StringBuilder r);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#setAttribute(org.jdom.Attribute)
	 */
	@Override
	public abstract void setAttribute(MyAttribute attr);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#setDataOfChildElement(org.jdom.Element)
	 */
	@Override
	public abstract void setDataOfChildElement(Element childElement);
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .AttributeValuePairSupport#fillAttributeMap(java.util.Map)
	 */
	@Override
	public abstract void fillAttributeMap(Map<String, Object> attributeValueMap);
	
}