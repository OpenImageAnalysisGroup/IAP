package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.Attribute;
import org.jdom.Element;

public class NumericMeasurement implements NumericMeasurementInterface {
	private double value;
	private int replicateID;
	private SampleInterface parentSample;
	private String unit;
	private String quality;
	
	public NumericMeasurement(SampleInterface parent) {
		this.parentSample = parent;
	}
	
	public NumericMeasurement(SampleInterface parent, Map<?, ?> attributemap) {
		this.parentSample = parent;
		if (attributemap.containsKey("replicates"))
			setReplicateID((Integer) attributemap.get("replicates"));
		if (attributemap.containsKey("unit"))
			setUnit((String) attributemap.get("unit"));
		if (attributemap.containsKey("value"))
			setValue((Double) attributemap.get("value"));
		if (attributemap.containsKey("quality"))
			setQualityAnnotation((String) attributemap.get("quality"));
	}
	
	/**
	 * Builds up a new hierarchy for a newly created MeasurementData. Copies as
	 * much as possible starting from the copyFrom Measurement to the top until
	 * MeasurementData.
	 */
	public NumericMeasurement(Measurement copyFrom, String newSubstanceName, String optNewExperimentName) {
		setValue(copyFrom.getValue());
		setReplicateID(copyFrom.getReplicateID());
		
		SubstanceInterface md = copyFrom.getParentSample().getParentCondition().getParentSubstance().clone();
		md.setName(newSubstanceName);
		ConditionInterface series = copyFrom.getParentSample().getParentCondition().clone(md);
		if (optNewExperimentName != null)
			series.setExperimentName(optNewExperimentName);
		series.setParent(md);
		parentSample = copyFrom.getParentSample().clone(series);
		parentSample.setParent(series);
	}
	
	public void getString(StringBuilder r) {
		r.append("<data");
		getXMLAttributeString(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</data>");
	}
	
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, new String[] {
					"replicates", "unit", "quality"
			}, new Object[] {
					replicateID, unit, quality
			});
	}
	
	public void getStringOfChildren(StringBuilder r) {
		r.append(getValue());
	}
	
	public double getValue() {
		return value;
	}
	
	public SampleInterface getParentSample() {
		return parentSample;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public boolean setData(Element averageElement) {
		setValue(Double.NaN);
		try {
			String avg = averageElement.getValue();
			setValue(Double.parseDouble(avg));
		} catch (Exception e) {
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
	
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		attr.setValue(StringManipulationTools.htmlToUnicode(attr.getValue().replaceAll("~", "&#")));
		
		if (attr.getName().equals("replicates")) {
			try {
				if (attr.getValue().length() > 0)
					setReplicateID(Integer.parseInt(attr.getValue()));
				else
					setReplicateID(-1);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			if (attr.getName().equals("unit")) {
				try {
					setUnit(attr.getValue());
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			} else
				if (attr.getName().equals("id")) {
					// ignore ID
				} else
					if (attr.getName().equals("quality")) {
						setQualityAnnotation(attr.getValue());
					} else
						System.err.println("Internal Error: Unknown Data Attribute: " + attr.getName());
	}
	
	public void setDataOfChildElement(Element childElement) {
		// no children
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public void setReplicateID(int replicateID) {
		this.replicateID = replicateID;
	}
	
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
	
	public void setParentSample(SampleInterface sample) {
		parentSample = sample;
	}
	
	public void fillAttributeMap(Map<String, Object> attributes) {
		attributes.put("replicates", getReplicateID());
		attributes.put("unit", getUnit());
		attributes.put("value", getValue());
		attributes.put("quality", getQualityAnnotation());
	}
	
	public NumericMeasurementInterface clone(SampleInterface parent) {
		NumericMeasurementInterface m = Experiment.getTypeManager().getNewMeasurement(parent);
		m.setValue(getValue());
		m.setReplicateID(getReplicateID());
		m.setUnit(getUnit());
		m.setQualityAnnotation(getQualityAnnotation());
		return m;
	}
	
}
