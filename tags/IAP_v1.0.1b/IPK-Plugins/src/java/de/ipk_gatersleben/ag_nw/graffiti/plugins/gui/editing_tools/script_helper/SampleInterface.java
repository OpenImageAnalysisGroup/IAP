package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

public interface SampleInterface extends MappingDataEntity, Comparable<SampleInterface>,
		Collection<NumericMeasurementInterface> {
	
	public abstract void getString(StringBuilder r);
	
	@Override
	public abstract void getXMLAttributeString(StringBuilder r);
	
	@Override
	public abstract void getStringOfChildren(StringBuilder r);
	
	public abstract ConditionInterface getParentCondition();
	
	public abstract void setSampleTtestInfo(TtestInfo info);
	
	public abstract String getSampleTime();
	
	public abstract Double[] getDataList();
	
	public abstract SampleAverageInterface getSampleAverage();
	
	public abstract void setSampleAverage(SampleAverageInterface average);
	
	public abstract String getTimeUnit();
	
	/**
	 * Get a id, based on experiment name, plant name and genotype, time point
	 * and time unit This can be used to find similar data entries for example
	 * for other substances. If this id and additionally the replicate ID
	 * matches, two values from two substances may be comparable.
	 * 
	 * @return A id, which may be used to compare the data point with other data
	 *         points from other substances.
	 */
	public abstract DataMappingId getFullId();
	
	public abstract int compareTo(SampleInterface sd, boolean ignoreSnapshotFineTime);
	
	public abstract void recalculateSampleAverage();
	
	public abstract TtestInfo getTtestInfo();
	
	@Override
	public abstract boolean setData(Element sampleElement);
	
	public abstract double calcMean();
	
	@Override
	public abstract void setAttribute(Attribute attr);
	
	@Override
	public abstract void setDataOfChildElement(Element childElement);
	
	public abstract void setMeasurementtool(String measurementtool);
	
	public abstract String getMeasurementtool();
	
	public abstract void setTimeUnit(String timeUnit);
	
	public abstract void setTime(int time);
	
	public abstract int getTime();
	
	public abstract void setSampleFineTimeOrRowId(Long rowId);
	
	public abstract Long getSampleFineTimeOrRowId();
	
	public abstract void setTtestInfo(TtestInfo ttestInfo);
	
	public abstract void setParent(ConditionInterface series);
	
	public abstract String getAverageUnit();
	
	@Override
	public abstract boolean add(NumericMeasurementInterface e);
	
	@Override
	public abstract boolean addAll(Collection<? extends NumericMeasurementInterface> c);
	
	@Override
	public abstract void clear();
	
	@Override
	public abstract boolean contains(Object o);
	
	@Override
	public abstract boolean containsAll(Collection<?> c);
	
	@Override
	public abstract boolean isEmpty();
	
	/**
	 * Don't forget to call updateSampleAverage after removing a measurement.
	 */
	@Override
	public abstract boolean remove(Object o);
	
	@Override
	public abstract boolean removeAll(Collection<?> c);
	
	@Override
	public abstract boolean retainAll(Collection<?> c);
	
	@Override
	public abstract int size();
	
	@Override
	public abstract Object[] toArray();
	
	@Override
	public abstract <T> T[] toArray(T[] a);
	
	@Override
	public abstract Iterator<NumericMeasurementInterface> iterator();
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	@Override
	public abstract void fillAttributeMap(Map<String, Object> attributeValueMap);
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
	
	public SampleInterface clone(ConditionInterface parent);
	
	public abstract String getSubstanceNameWithUnit();
}