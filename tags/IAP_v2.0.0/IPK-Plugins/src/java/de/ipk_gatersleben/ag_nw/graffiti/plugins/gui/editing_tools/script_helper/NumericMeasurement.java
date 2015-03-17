package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

public class NumericMeasurement implements NumericMeasurementInterface {
	public static final String ATTRIBUTE_KEY_FILES = "files";
	public static final String ATTRIBUTE_KEY_QUALITY = "quality";
	public static final String ATTRIBUTE_KEY_VALUE = "value";
	public static final String ATTRIBUTE_KEY_UNIT = "unit";
	public static final String ATTRIBUTE_KEY_REPLICATES = "replicates";
	private static final String ATTRIBUTE_KEY_ID_IGNORED = "id";
	private double value = Double.NaN;
	private int replicateID;
	private SampleInterface parent;
	private String unit;
	private String quality;
	private String files;
	
	public NumericMeasurement(SampleInterface parent) {
		this.parent = parent;
	}
	
	public NumericMeasurement(SampleInterface parent, Map<?, ?> attributemap) {
		this.parent = parent;
		if (attributemap.containsKey(ATTRIBUTE_KEY_REPLICATES))
			setReplicateID((Integer) attributemap.get(ATTRIBUTE_KEY_REPLICATES));
		if (attributemap.containsKey(ATTRIBUTE_KEY_UNIT))
			setUnit((String) attributemap.get(ATTRIBUTE_KEY_UNIT));
		if (attributemap.containsKey(ATTRIBUTE_KEY_VALUE))
			setValue((Double) attributemap.get(ATTRIBUTE_KEY_VALUE));
		if (attributemap.containsKey(ATTRIBUTE_KEY_QUALITY))
			setQualityAnnotation((String) attributemap.get(ATTRIBUTE_KEY_QUALITY));
		if (attributemap.containsKey(ATTRIBUTE_KEY_FILES))
			setFiles((String) attributemap.get(ATTRIBUTE_KEY_FILES));
	}
	
	@Override
	public void setAttributeField(String id, Object value) {
		switch (id) {
			case ATTRIBUTE_KEY_REPLICATES:
				if (value == null)
					setReplicateID(0);
				else
					setReplicateID(Integer.parseInt((String) value));
				return;
			case ATTRIBUTE_KEY_UNIT:
				setUnit((String) value);
			case ATTRIBUTE_KEY_VALUE:
				// if (value == null)
				// setValue(Double.NaN);
				// else
				// setValue(Double.parseDouble((String) value));
				return;
			case ATTRIBUTE_KEY_QUALITY:
				setQualityAnnotation((String) value);
				return;
			case ATTRIBUTE_KEY_FILES:
				setFiles((String) value);
				return;
		}
		throw new RuntimeException("Cant set field " + id + "!");
	}
	
	/**
	 * Builds up a new hierarchy for a newly created MeasurementData. Copies as
	 * much as possible starting from the copyFrom Measurement to the top until
	 * MeasurementData.
	 */
	public NumericMeasurement(Measurement copyFrom, String newSubstanceName, String optNewExperimentName) {
		setValue(copyFrom.getValue());
		setReplicateID(copyFrom.getReplicateID());
		
		if (copyFrom instanceof NumericMeasurement) {
			setQualityAnnotation(((NumericMeasurement) copyFrom).getQualityAnnotation());
			setUnit(((NumericMeasurement) copyFrom).getUnit());
		}
		
		SubstanceInterface clonedSubstance = copyFrom.getParentSample().getParentCondition().getParentSubstance().clone();
		clonedSubstance.setName(newSubstanceName);
		ConditionInterface clonedCondition = copyFrom.getParentSample().getParentCondition().clone(clonedSubstance);
		if (optNewExperimentName != null)
			clonedCondition.setExperimentName(optNewExperimentName);
		clonedCondition.setParent(clonedSubstance);
		parent = copyFrom.getParentSample().clone(clonedCondition);
		parent.setParent(clonedCondition);
		
		setFiles(copyFrom.getFiles());
		synchronized (clonedSubstance) {
			clonedSubstance.add(clonedCondition);
		}
		synchronized (clonedCondition) {
			clonedCondition.add(parent);
		}
		synchronized (parent) {
			parent.add(this);
		}
	}
	
	public static String typeName = "data";
	
	@Override
	public void getString(StringBuilder r) {
		r.append("<" + typeName);
		getXMLAttributeString(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</" + typeName + ">");
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, new String[] {
				ATTRIBUTE_KEY_REPLICATES, ATTRIBUTE_KEY_UNIT, ATTRIBUTE_KEY_QUALITY, ATTRIBUTE_KEY_FILES
		}, new Object[] {
				replicateID, unit, quality, files
		});
	}
	
	@Override
	public void getStringOfChildren(StringBuilder r) {
		r.append(getValue());
	}
	
	@Override
	public double getValue() {
		return value;
	}
	
	@Override
	public SampleInterface getParentSample() {
		return parent;
	}
	
	@Override
	public String getUnit() {
		return unit;
	}
	
	@Override
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@Override
	public boolean setData(Element averageElement) {
		try {
			String avg = averageElement.getValue();
			setValue(Double.parseDouble(avg));
		} catch (Exception e) {
			setValue(Double.NaN);
			ErrorMsg.addErrorMessage(e);
		}
		
		List<?> attributeList = averageElement.getAttributes();
		for (Object o : attributeList) {
			if (o instanceof Attribute) {
				Attribute a = (Attribute) o;
				setAttribute(new MyAttribute(a));
			}
		}
		// setDataOfChildElement(..)
		// no children
		return true;
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		
		String name = attr.getName();
		
		if (name.equals(ATTRIBUTE_KEY_ID_IGNORED)) {
			// ignore ID
			return;
		}
		
		String val = attr.getValue();
		if (val != null && val.contains("~"))
			val = StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#"));
		
		if (name.equals(ATTRIBUTE_KEY_REPLICATES)) {
			try {
				if (attr.getValue().length() > 0) {
					setReplicateID(Integer.parseInt(val));
				} else
					setReplicateID(-1);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			if (name.equals(ATTRIBUTE_KEY_UNIT)) {
				try {
					setUnit(val);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			} else
				if (name.equals(ATTRIBUTE_KEY_FILES)) {
					setFiles(val);
				} else
					if (name.equals(ATTRIBUTE_KEY_QUALITY)) {
						setQualityAnnotation(val);
					}
	}
	
	@Override
	public void setDataOfChildElement(Element childElement) {
		// no children
	}
	
	@Override
	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public void setReplicateID(int replicateID) {
		this.replicateID = replicateID;
	}
	
	@Override
	public int getReplicateID() {
		return replicateID;
	}
	
	@Override
	public String getQualityAnnotation() {
		return quality;
	}
	
	@Override
	public void setQualityAnnotation(String quality) {
		this.quality = quality;
	}
	
	@Override
	public void setParentSample(SampleInterface sample) {
		parent = sample;
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		attributes.put(ATTRIBUTE_KEY_REPLICATES, replicateID);
		attributes.put(ATTRIBUTE_KEY_UNIT, unit);
		attributes.put(ATTRIBUTE_KEY_VALUE, value);
		attributes.put(ATTRIBUTE_KEY_QUALITY, quality);
		attributes.put(ATTRIBUTE_KEY_FILES, files);
	}
	
	@Override
	public Object getAttributeField(String id) {
		switch (id) {
			case ATTRIBUTE_KEY_REPLICATES:
				return replicateID;
			case ATTRIBUTE_KEY_UNIT:
				return unit;
			case ATTRIBUTE_KEY_VALUE:
				return value;
			case ATTRIBUTE_KEY_QUALITY:
				return quality;
			case ATTRIBUTE_KEY_FILES:
				return files;
		}
		throw new UnsupportedOperationException("Can't return field value from id '" + id + "'!");
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		NumericMeasurementInterface m = Experiment.typemanager.getNewMeasurement(parent);
		m.setValue(value);
		m.setReplicateID(replicateID);
		m.setUnit(unit);
		m.setQualityAnnotation(quality);
		m.setFiles(files);
		return m;
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
	public String toString() {
		String sd = "";
		if (getParentSample().getSampleAverage() != null && !Double.isNaN(getParentSample().getSampleAverage().getStdDev()))
			sd = "  +/-" + (getParentSample().getSampleAverage().getStdDev());
		if (getQualityAnnotation() != null && getQualityAnnotation().length() > 0)
			return getValue() + sd + " " + (getUnit() != null ? getUnit() : "") + " ("
					+ getQualityAnnotation() + ")";
		else
			return getValue() + sd + " " + (getUnit() != null ? getUnit() : "");
	}
	
	public static NumericMeasurement getSimple(Double value) {
		NumericMeasurement res = new NumericMeasurement(null);
		res.value = value;
		return res;
	}
}
