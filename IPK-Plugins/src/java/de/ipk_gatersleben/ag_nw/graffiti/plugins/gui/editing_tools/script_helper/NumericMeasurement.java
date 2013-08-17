package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

public class NumericMeasurement implements NumericMeasurementInterface {
	private double value = Double.NaN;
	private int replicateID;
	private SampleInterface clonedSample;
	private String unit;
	private String quality;
	private String files;
	
	public NumericMeasurement(SampleInterface parent) {
		this.clonedSample = parent;
	}
	
	public NumericMeasurement(SampleInterface parent, Map<?, ?> attributemap) {
		this.clonedSample = parent;
		if (attributemap.containsKey("replicates"))
			setReplicateID((Integer) attributemap.get("replicates"));
		if (attributemap.containsKey("unit"))
			setUnit((String) attributemap.get("unit"));
		if (attributemap.containsKey("value"))
			setValue((Double) attributemap.get("value"));
		if (attributemap.containsKey("quality"))
			setQualityAnnotation((String) attributemap.get("quality"));
		if (attributemap.containsKey("files"))
			setFiles((String) attributemap.get("files"));
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
		clonedSample = copyFrom.getParentSample().clone(clonedCondition);
		clonedSample.setParent(clonedCondition);
		
		setFiles(copyFrom.getFiles());
		synchronized (clonedSubstance) {
			clonedSubstance.add(clonedCondition);
		}
		synchronized (clonedCondition) {
			clonedCondition.add(clonedSample);
		}
		synchronized (clonedSample) {
			clonedSample.add(this);
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
				"replicates", "unit", "quality", "files"
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
		return clonedSample;
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
				setAttribute(a);
			}
		}
		// setDataOfChildElement(..)
		// no children
		return true;
	}
	
	@Override
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		
		String name = attr.getName();
		
		if (name.startsWith("i")) { // name.equals("id")) {
			// ignore ID
			return;
		}
		
		String val = attr.getValue();
		if (val != null && val.contains("~"))
			val = StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#"));
		
		if (name.startsWith("r")) { // equals("replicates")) {
			try {
				if (attr.getValue().length() > 0) {
					setReplicateID(Integer.parseInt(val));
				} else
					setReplicateID(-1);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			if (name.startsWith("u")) {// equals("unit")) {
				try {
					setUnit(val);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			} else
				if (name.startsWith("f")) {// equals("files")) {
					setFiles(val);
				} else
					if (name.startsWith("q")) {// equals("quality")) {
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
		this.quality = quality != null ? quality.intern() : null;
	}
	
	@Override
	public void setParentSample(SampleInterface sample) {
		clonedSample = sample;
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		attributes.put("replicates", getReplicateID());
		attributes.put("unit", getUnit());
		attributes.put("value", getValue());
		attributes.put("quality", getQualityAnnotation());
		attributes.put("files", getFiles());
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		NumericMeasurementInterface m = Experiment.getTypeManager().getNewMeasurement(parent);
		m.setValue(getValue());
		m.setReplicateID(getReplicateID());
		m.setUnit(getUnit());
		m.setQualityAnnotation(getQualityAnnotation());
		m.setFiles(getFiles());
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
		if (getQualityAnnotation() != null && getQualityAnnotation().length() > 0)
			return getValue() + " " + getUnit() + " (" + getQualityAnnotation() + ")";
		else
			return getValue() + " " + getUnit();
	}
}
