package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

public class Sample implements SampleInterface {
	
	public static final String ATTRIBUTE_KEY_ID = "id";
	public static final String ATTRIBUTE_KEY_MEASUREMENTTOOL = "measurementtool";
	public static final String ATTRIBUTE_KEY_TIME = "time";
	public static final String ATTRIBUTE_KEY_UNIT = "unit";
	public static final String ATTRIBUTE_KEY_TTEST = "ttest";
	public static final String ATTRIBUTE_KEY_FILES = "files";
	
	public static final String UNSPECIFIED_TIME_STRING = "-1";
	
	/**
	 * If list of state variables is modified/extended, check and modify equals,
	 * hashCode and eventually compareTo method implementation.
	 */
	private String measurementtool;
	private Long rowId;
	public int time = -1;
	private String timeUnit = "-1";
	private TtestInfo ttestInfo = TtestInfo.EMPTY;
	String files;
	
	private final Collection<NumericMeasurementInterface> measurements = new LinkedList<NumericMeasurementInterface>();
	
	private SampleAverageInterface sampleAverage;
	private ConditionInterface parent;
	
	public Sample(ConditionInterface parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return timeUnit + " " + time;
	}
	
	private static final String[] attributeNames = new String[] { ATTRIBUTE_KEY_ID, ATTRIBUTE_KEY_MEASUREMENTTOOL,
			ATTRIBUTE_KEY_TIME, ATTRIBUTE_KEY_UNIT, ATTRIBUTE_KEY_TTEST, ATTRIBUTE_KEY_FILES };
	
	private Object[] getAttributeValues() {
		return new Object[] { getSampleFineTimeOrRowId(), getMeasurementtool(), getTime(), getTimeUnit(),
				getTtestInfo().toString(), getFiles() };
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		Object[] values = getAttributeValues();
		int idx = 0;
		for (String name : attributeNames) {
			attributeValueMap.put(name, values[idx++]);
		}
	}
	
	@Override
	public Object getAttributeField(String id) {
		switch (id) {
			case ATTRIBUTE_KEY_ID:
				return getSampleFineTimeOrRowId();
			case ATTRIBUTE_KEY_MEASUREMENTTOOL:
				return getMeasurementtool();
			case ATTRIBUTE_KEY_TIME:
				return getTime();
			case ATTRIBUTE_KEY_UNIT:
				return getTimeUnit();
			case ATTRIBUTE_KEY_TTEST:
				return getTtestInfo();
			case ATTRIBUTE_KEY_FILES:
				return getFiles();
		}
		throw new UnsupportedOperationException("Can't return field value from id '" + id + "'!");
	}
	
	@Override
	public void getString(StringBuilder r) {
		r.append("<sample");
		getXMLAttributeString(r);
		r.append(">");
		if (sampleAverage == null && size() > 0)
			recalculateSampleAverage(false);
		if (sampleAverage != null)
			sampleAverage.getString(r);
		getStringOfChildren(r);
		r.append("</sample>");
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, attributeNames, getAttributeValues());
	}
	
	@Override
	public void getStringOfChildren(StringBuilder r) {
		for (NumericMeasurementInterface m : this)
			m.getString(r);
	}
	
	@Override
	public ConditionInterface getParentCondition() {
		return parent;
	}
	
	@Override
	public void setSampleTtestInfo(TtestInfo info) {
		setTtestInfo(info);
	}
	
	@Override
	public String getSampleTime() {
		return getTimeUnit() + " " + getTime();
	}
	
	@Override
	public Double[] getDataList() {
		Double[] result = new Double[size()];
		int idx = 0;
		for (NumericMeasurementInterface m : this)
			result[idx++] = m.getValue();
		return result;
	}
	
	@Override
	public SampleAverageInterface getSampleAverage() {
		if (sampleAverage == null || Double.isNaN(sampleAverage.getValue()))
			recalculateSampleAverage(false);
		return sampleAverage;
	}
	
	@Override
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
	
	@Override
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
	@Override
	public DataMappingId getFullId(boolean includeTime) {
		int seriesId = parent.getSeriesId();
		String expName = parent.getExperimentName();
		String seriesName = parent.getConditionName();
		String species = parent.getSpecies();
		String genotype = parent.getGenotype();
		String rowId = includeTime ? getSampleFineTimeOrRowId() + "" : "";
		String timeP = includeTime ? getTime() + "" : "";
		String timeU = includeTime ? getTimeUnit() : "";
		
		return DataMappingId.getEmptyDataMappingWithoutReplicateInformation(seriesId, expName, seriesName, species,
				genotype, rowId, timeP, timeU);
	}
	
	@Override
	public void recalculateSampleAverage(boolean onlyIfNull) {
		if (sampleAverage == null) {
			if (size() > 0)
				sampleAverage = Experiment.getTypeManager().getNewSampleAverage(this);
			if (sampleAverage != null)
				sampleAverage.calculateValuesFromSampleData();
		} else
			if (!onlyIfNull)
				sampleAverage.calculateValuesFromSampleData();
	}
	
	@Override
	public TtestInfo getTtestInfo() {
		return ttestInfo;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean setData(Element sampleElement) {
		List attributeList = sampleElement.getAttributes();
		for (Object o : attributeList) {
			if (o instanceof Attribute) {
				Attribute a = (Attribute) o;
				setAttribute(new MyAttribute(a));
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
	
	@Override
	public double calcMean() {
		return sampleAverage.getValue();
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		String val = attr != null ? attr.getValue() : null;
		if (val == null)
			return;
		if (val.length() > 0 && val.contains("~"))
			attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		else
			attr.setValue(val);
		
		if (attr.getName().equals(ATTRIBUTE_KEY_ID)) {
			// try {
			if (attr.getValue().length() > 0 && !attr.getValue().equals("-1")) {
				if (attr.getValue().length() > 9)
					setSampleFineTimeOrRowId(Long.parseLong(attr.getValue()));
				else
					setSampleFineTimeOrRowId((long) Integer.parseInt(attr.getValue()));
			} else
				setSampleFineTimeOrRowId(-1l);
			// } catch (Exception e) {
			// ErrorMsg.addErrorMessage(e);
			// }
		} else
			if (attr.getName().equals(ATTRIBUTE_KEY_MEASUREMENTTOOL))
				setMeasurementtool(attr.getValue());
			else
				if (attr.getName().equals(ATTRIBUTE_KEY_TTEST))
					setTtestInfo(TtestInfo.getValueFromString(attr.getValue()));
				else
					if (attr.getName().equals(ATTRIBUTE_KEY_FILES))
						setFiles(attr.getValue());
					else
						if (attr.getName().equals(ATTRIBUTE_KEY_UNIT))
							setTimeUnit(attr.getValue());
						else
							if (attr.getName().equals(ATTRIBUTE_KEY_TIME))
								// try {
								setTime(Integer.parseInt(attr.getValue()));
							// } catch (Exception e) {
							// ErrorMsg.addErrorMessage(e);
							// }
							else
								System.err.println("Internal Error: Unknown Sample Attribute: " + attr.getName());
	}
	
	@Override
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
	
	@Override
	public void setMeasurementtool(String measurementtool) {
		this.measurementtool = measurementtool != null ? measurementtool.intern() : null;
	}
	
	@Override
	public String getMeasurementtool() {
		return measurementtool;
	}
	
	@Override
	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;// != null ? timeUnit.intern() : null;
	}
	
	@Override
	public void setTime(int time) {
		this.time = time;
	}
	
	@Override
	public int getTime() {
		return time;
	}
	
	@Override
	public void setSampleFineTimeOrRowId(Long rowId) {
		this.rowId = rowId;
	}
	
	@Override
	public Long getSampleFineTimeOrRowId() {
		return rowId;
	}
	
	@Override
	public void setTtestInfo(TtestInfo ttestInfo) {
		this.ttestInfo = ttestInfo;
	}
	
	@Override
	public void setParent(ConditionInterface series) {
		parent = series;
		
	}
	
	@Override
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
	
	@Override
	public boolean add(NumericMeasurementInterface e) {
		if (e == null)
			return false;
		else
			return measurements.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends NumericMeasurementInterface> c) {
		boolean allTrue = true;
		for (NumericMeasurementInterface nme : c)
			if (add(nme))
				allTrue = false;
		return allTrue;
	}
	
	@Override
	public void clear() {
		measurements.clear();
	}
	
	@Override
	public boolean contains(Object o) {
		return measurements.contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return measurements.containsAll(c);
	}
	
	@Override
	public boolean isEmpty() {
		return measurements.isEmpty();
	}
	
	/**
	 * Don't forget to call updateSampleAverage after removing a measurement.
	 */
	@Override
	public boolean remove(Object o) {
		((NumericMeasurementInterface) o).setParentSample(null);
		return measurements.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o : c) {
			((NumericMeasurementInterface) o).setParentSample(null);
		}
		return measurements.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		for (NumericMeasurementInterface n : measurements)
			n.setParentSample(null);
		boolean res = measurements.retainAll(c);
		for (NumericMeasurementInterface n : measurements)
			n.setParentSample(this);
		return res;
	}
	
	@Override
	public int size() {
		return measurements.size();
	}
	
	@Override
	public Object[] toArray() {
		return measurements.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		return measurements.toArray(a);
	}
	
	@Override
	public Iterator<NumericMeasurementInterface> iterator() {
		return measurements.iterator();
	}
	
	@Override
	public int compareTo(SampleInterface sd, boolean ignoreSnapshotFineTime) {
		if (getTime() < sd.getTime())
			return -1;
		if (getTime() > sd.getTime())
			return 1;
		if (!ignoreSnapshotFineTime) {
			Long ft = getSampleFineTimeOrRowId();
			if (ft == null)
				return compareTo(sd);
			int rr = ft.compareTo(sd.getSampleFineTimeOrRowId());
			if (rr != 0)
				return rr;
		}
		return compareTo(sd);
	}
	
	@Override
	public int compareTo(SampleInterface sd) {
		if (getTime() < sd.getTime())
			return -1;
		if (getTime() > sd.getTime())
			return 1;
		String u1 = getTimeUnit();
		String u2 = sd.getTimeUnit();
		u1 = (u1 != null ? u1 : "");
		u2 = (u2 != null ? u2 : "");
		return u1.compareTo(u2);
	}
	
	// @Override
	// public boolean equals(Object obj) {
	// if (obj == null)
	// return false;
	// if (!(obj instanceof Sample))
	// return false;
	// Sample s = (Sample) obj;
	//
	// if (rowId != s.rowId || time != s.time)
	// return false;
	//
	// if (ttestInfo != s.ttestInfo)
	// return false;
	//
	// String s1 = measurementtool + ";" + timeUnit;
	// String s2 = s.measurementtool + ";" + s.timeUnit;
	// return s1.equals(s2);
	// }
	//
	// @Override
	// public int hashCode() {
	// String s1 = measurementtool + ";" + rowId + ";" + time + ";" + timeUnit + ";" + ttestInfo.name();
	// return s1.hashCode();
	// }
	//
	@Override
	public SampleInterface clone(ConditionInterface parent) {
		SampleInterface s = Experiment.getTypeManager().getNewSample(parent);
		s.setMeasurementtool(getMeasurementtool());
		s.setSampleFineTimeOrRowId(getSampleFineTimeOrRowId());
		s.setTime(getTime());
		s.setTimeUnit(getTimeUnit());
		s.setTtestInfo(getTtestInfo());
		s.setFiles(getFiles());
		if (getSampleAverage() != null)
			s.setSampleAverage(getSampleAverage().clone());
		else
			s.setSampleAverage(null);
		return s;
	}
	
	@Override
	public String getSubstanceNameWithUnit() {
		String sub = getParentCondition().getParentSubstance().getName();
		
		String unit = null;
		for (NumericMeasurementInterface nmi : this) {
			unit = nmi.getUnit();
			if (unit != null)
				break;
		}
		if (unit != null && unit.length() > 0)
			sub += " (" + unit + ")";
		return sub;
	}
	
	@Override
	public String getFiles() {
		return files;
	}
	
	@Override
	public void setFiles(String files) {
		this.files = files;
	}
	
	@Override
	public void setAttributeField(String id, Object value) {
		throw new RuntimeException("not yet implemented!");
	}
}
