package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

public class Sample implements SampleInterface {
	
	public static final String UNSPECIFIED_TIME_STRING = "-1";
	
	/**
	 * If list of state variables is modified/extended, check and modify equals,
	 * hashCode and eventually compareTo method implementation.
	 */
	private String measurementtool;
	private long rowId;
	private int time = -1;
	private String timeUnit = "-1";
	private TtestInfo ttestInfo = TtestInfo.EMPTY;
	
	private final Collection<NumericMeasurementInterface> measurements = new ArrayList<NumericMeasurementInterface>();
	
	private SampleAverageInterface sampleAverage;
	private ConditionInterface parent;
	
	public Sample(ConditionInterface parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return timeUnit + " " + time;
	}
	
	private static final String[] attributeNames = new String[] { "id", "measurementtool", "time", "unit", "ttest" };
	
	public void getString(StringBuilder r) {
		r.append("<sample");
		getXMLAttributeString(r);
		r.append(">");
		if (sampleAverage == null && size() > 0)
			recalculateSampleAverage();
		if (sampleAverage != null)
			sampleAverage.getString(r);
		getStringOfChildren(r);
		r.append("</sample>");
	}
	
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, attributeNames, getAttributeValues());
	}
	
	private Object[] getAttributeValues() {
		return new Object[] { getRowId(), getMeasurementtool(), getTime(), getTimeUnit(),
							getTtestInfo().toString() };
	}
	
	public void getStringOfChildren(StringBuilder r) {
		for (NumericMeasurementInterface m : this)
			m.getString(r);
	}
	
	public ConditionInterface getParentCondition() {
		return parent;
	}
	
	public void setSampleTtestInfo(TtestInfo info) {
		setTtestInfo(info);
	}
	
	public String getSampleTime() {
		return getTimeUnit() + " " + getTime();
	}
	
	public Double[] getDataList() {
		Double[] result = new Double[size()];
		int idx = 0;
		for (NumericMeasurementInterface m : this)
			result[idx++] = m.getValue();
		return result;
	}
	
	public SampleAverageInterface getSampleAverage() {
		if (sampleAverage == null)
			recalculateSampleAverage();
		return sampleAverage;
	}
	
	public void setSampleAverage(SampleAverageInterface average) {
		sampleAverage = average;
	}
	
	// public Collection<NumericMeasurement> getMeasurements() {
	// return measurements;
	// }
	//
	// public void addMeasurement(NumericMeasurement m) {
	// getMeasurements().add(m);
	// }
	
	public String getTimeUnit() {
		if (timeUnit == null)
			timeUnit = Sample.UNSPECIFIED_TIME_STRING;
		return timeUnit;
	}
	
	/**
	 * Get a id, based on experiment name, plant name and genotype, time point
	 * and time unit This can be used to find similar data entries for example
	 * for other substances. If this id and additionally the replicate ID
	 * matches, two values from two substances may be comparable.
	 * 
	 * @return A id, which may be used to compare the data point with other data
	 *         points from other substances.
	 */
	public DataMappingId getFullId() {
		int seriesId = parent.getSeriesId();
		String expName = parent.getExperimentName();
		String seriesName = parent.getConditionName();
		String species = parent.getSpecies();
		String genotype = parent.getGenotype();
		String rowId = getRowId() + "";
		String timeP = getTime() + "";
		String timeU = getTimeUnit();
		
		return DataMappingId.getEmptyDataMappingWithoutReplicateInformation(seriesId, expName, seriesName, species,
							genotype, rowId, timeP, timeU);
	}
	
	public void recalculateSampleAverage() {
		if (sampleAverage == null) {
			if (size() > 0)
				sampleAverage = Experiment.getTypeManager().getNewSampleAverage(this);
		} else
			sampleAverage.calculateValuesFromSampleData();
	}
	
	public TtestInfo getTtestInfo() {
		return ttestInfo;
	}
	
	@SuppressWarnings("unchecked")
	public boolean setData(Element sampleElement) {
		List attributeList = sampleElement.getAttributes();
		for (Object o : attributeList) {
			if (o instanceof Attribute) {
				Attribute a = (Attribute) o;
				setAttribute(a);
			}
		}
		List childrenList = sampleElement.getChildren();
		for (Object o : childrenList) {
			if (o instanceof Element) {
				Element childElement = (Element) o;
				setDataOfChildElement(childElement);
			}
		}
		return true;
	}
	
	public double calcMean() {
		return sampleAverage.getValue();
	}
	
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		
		if (attr.getName().equals("id")) {
			try {
				if (attr.getValue().length() > 0)
					setRowId(Long.parseLong(attr.getValue()));
				else
					setRowId(-1);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			if (attr.getName().equals("measurementtool"))
				setMeasurementtool(attr.getValue());
			else
				if (attr.getName().equals("ttest"))
					setTtestInfo(TtestInfo.getValueFromString(attr.getValue()));
				else
					if (attr.getName().equals("unit"))
						setTimeUnit(attr.getValue());
					else
						if (attr.getName().equals("time"))
							try {
								setTime(Integer.parseInt(attr.getValue()));
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						else
							System.err.println("Internal Error: Unknown Sample Attribute: " + attr.getName());
	}
	
	public void setDataOfChildElement(Element childElement) {
		if (childElement.getName().equals("average")) {
			SampleAverageInterface s = Experiment.getTypeManager().getNewSampleAverage(this);
			if (s.setData(childElement))
				sampleAverage = s;
		} else
			if (childElement.getName().equals("data")) {
				NumericMeasurementInterface m = Experiment.getTypeManager().getNewMeasurement(this);
				if (m.setData(childElement))
					add(m);
			}
	}
	
	public void setMeasurementtool(String measurementtool) {
		this.measurementtool = measurementtool;
	}
	
	public String getMeasurementtool() {
		return measurementtool;
	}
	
	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}
	
	public void setTime(int time) {
		this.time = time;
	}
	
	public int getTime() {
		return time;
	}
	
	public void setRowId(long rowId) {
		this.rowId = rowId;
	}
	
	public long getRowId() {
		return rowId;
	}
	
	public void setTtestInfo(TtestInfo ttestInfo) {
		this.ttestInfo = ttestInfo;
	}
	
	public void setParent(ConditionInterface series) {
		parent = series;
		
	}
	
	public String getAverageUnit() {
		Collection<NumericMeasurementInterface> col = this;
		if (col == null || col.size() <= 0)
			return null;
		else
			return col.iterator().next().getUnit();
	}
	
	/*
	 * Delegate Methods
	 */

	public boolean add(NumericMeasurementInterface e) {
		return measurements.add(e);
	}
	
	public boolean addAll(Collection<? extends NumericMeasurementInterface> c) {
		return measurements.addAll(c);
	}
	
	public void clear() {
		measurements.clear();
	}
	
	public boolean contains(Object o) {
		return measurements.contains(o);
	}
	
	public boolean containsAll(Collection<?> c) {
		return measurements.containsAll(c);
	}
	
	public boolean isEmpty() {
		return measurements.isEmpty();
	}
	
	/**
	 * Don't forget to call updateSampleAverage after removing a measurement.
	 */
	public boolean remove(Object o) {
		if (o instanceof NumericMeasurement)
			((NumericMeasurement) o).setParentSample(null);
		return measurements.remove(o);
	}
	
	public boolean removeAll(Collection<?> c) {
		return measurements.removeAll(c);
	}
	
	public boolean retainAll(Collection<?> c) {
		return measurements.retainAll(c);
	}
	
	public int size() {
		return measurements.size();
	}
	
	public Object[] toArray() {
		return measurements.toArray();
	}
	
	public <T> T[] toArray(T[] a) {
		return measurements.toArray(a);
	}
	
	public Iterator<NumericMeasurementInterface> iterator() {
		return measurements.iterator();
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		Object[] values = getAttributeValues();
		int idx = 0;
		for (String name : attributeNames) {
			attributeValueMap.put(name, values[idx++]);
		}
	}
	
	@Override
	public int compareTo(SampleInterface sd) {
		String u1 = getTimeUnit();
		String u2 = sd.getTimeUnit();
		u1 = (u1 != null ? u1 : "");
		u2 = (u2 != null ? u2 : "");
		int res = u1.compareTo(u2);
		if (res != 0)
			return res;
		else
			return getTime() < sd.getTime() ? -1 : (getTime() == sd.getTime() ? 0 : 1);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof Sample))
			return false;
		String s1 = measurementtool + ";" + rowId + ";" + time + ";" + timeUnit + ";" + ttestInfo.name();
		Sample s = (Sample) obj;
		String s2 = s.measurementtool + ";" + s.rowId + ";" + s.time + ";" + s.timeUnit + ";" + s.ttestInfo.name();
		return s1.equals(s2);
	}
	
	@Override
	public int hashCode() {
		String s1 = measurementtool + ";" + rowId + ";" + time + ";" + timeUnit + ";" + ttestInfo.name();
		return s1.hashCode();
	}
	
	public SampleInterface clone(ConditionInterface parent) {
		SampleInterface s = Experiment.getTypeManager().getNewSample(parent);
		s.setMeasurementtool(getMeasurementtool());
		s.setRowId(getRowId());
		s.setTime(getTime());
		s.setTimeUnit(getTimeUnit());
		s.setTtestInfo(getTtestInfo());
		return s;
	}
	
}
