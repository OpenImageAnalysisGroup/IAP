package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.jdom.Attribute;
import org.jdom.Element;

public class SampleAverage implements SampleAverageInterface {
	private double max, min, stddev, value;
	private int replicates;
	private final SampleInterface parentSample;
	private String ownUnit = null;
	
	public SampleAverage(SampleInterface parent) {
		this.parentSample = parent;
		calculateValuesFromSampleData();
	}
	
	@SuppressWarnings("unchecked")
	public SampleAverage(SampleInterface sample, Map map) {
		this(sample);
		for (Object key : map.keySet()) {
			if (key instanceof String) {
				if (key.equals("value")) {
					setValue((Double) map.get("value"));
				} else
					if (map.get(key) != null)
						setValueFromAttribute(new Attribute((String) key, map.get(key).toString()));
			}
		}
	}
	
	public void getString(StringBuilder r) {
		r.append("<average");
		Substance.getAttributeString(r, new String[] {
				"max", "min", "replicates", "stddev", "unit"
		}, new Object[] {
				max, min, replicates, stddev, getUnit()
		});
		r.append(">" + getValue());
		r.append("</average>");
	}
	
	public double getValue() {
		return value;
	}
	
	public SampleInterface getParentSample() {
		return parentSample;
	}
	
	public void calculateValuesFromSampleData() {
		if (parentSample.size() == 0) {
			setMin(Double.NaN);
			setMax(Double.NaN);
			setStddev(Double.NaN);
			setValue(Double.NaN);
		} else {
			int n = parentSample.size();
			n = 0;
			
			setReplicateId(parentSample.size());
			
			setMax(Double.NEGATIVE_INFINITY);
			setMin(Double.POSITIVE_INFINITY);
			double sum = 0;
			for (NumericMeasurementInterface m : parentSample) {
				double v = m.getValue();
				
				if (Double.isNaN(v))
					continue;
				
				n++;
				if (v < getMin())
					setMin(v);
				if (v > getMax())
					setMax(v);
				sum += v;
			}
			
			if (n == 0) {
				setStddev(Double.NaN);
				setValue(Double.NaN);
				setMin(Double.NaN);
				setMax(Double.NaN);
				return;
			}
			if (n == 1) {
				setStddev(Double.NaN);
				setValue(sum);
				return;
			}
			
			double avg = sum / n;
			double sumDiff = 0;
			for (NumericMeasurementInterface m : parentSample) {
				if (!Double.isNaN(m.getValue()))
					sumDiff += (m.getValue() - avg) * (m.getValue() - avg);
			}
			setStddev(Math.sqrt(sumDiff / (n - 1)));
			setValue(avg);
		}
	}
	
	public double getStdDev() {
		return getStddev();
	}
	
	public String getUnit() {
		if (ownUnit == null)
			return parentSample.getAverageUnit();
		else
			return ownUnit;
	}
	
	@SuppressWarnings("unchecked")
	public boolean setData(Element averageElement) {
		setMin(Double.NaN);
		setMax(Double.NaN);
		setStddev(Double.NaN);
		setValue(Double.NaN);
		try {
			String avg = averageElement.getValue();
			setValue(Double.parseDouble(avg));
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		List attributeList = averageElement.getAttributes();
		for (Object o : attributeList) {
			if (o instanceof Attribute) {
				Attribute a = (Attribute) o;
				setValueFromAttribute(a);
			}
		}
		return true;
	}
	
	private void setValueFromAttribute(Attribute a) {
		if (a.getName().equals("max")) {
			try {
				if (a.getValue().length() > 0)
					setMax(Double.parseDouble(a.getValue()));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else
			if (a.getName().equals("min")) {
				try {
					if (a.getValue().length() > 0)
						setMin(Double.parseDouble(a.getValue()));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			} else
				if (a.getName().equals("replicates")) {
					try {
						setReplicateId(Integer.parseInt(a.getValue()));
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				} else
					if (a.getName().equals("stddev")) {
						try {
							setStddev(Double.parseDouble(a.getValue()));
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					} else
						if (a.getName().equals("unit")) {
							try {
								setUnit(a.getValue());
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						} else
							System.err.println("Internal Error: Unknown Average Attribute: " + a.getName());
	}
	
	public void setUnit(String ownUnit) {
		this.ownUnit = ownUnit;
	}
	
	public void setReplicateId(int replicates) {
		this.replicates = replicates;
	}
	
	public int getReplicateID() {
		return replicates;
	}
	
	public void setMax(double max) {
		this.max = max;
	}
	
	public double getMax() {
		return max;
	}
	
	public void setMin(double min) {
		this.min = min;
	}
	
	public double getMin() {
		return min;
	}
	
	public void setStddev(double stddev) {
		this.stddev = stddev;
	}
	
	public double getStddev() {
		return stddev;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public void getStringOfChildren(StringBuilder r) {
		// ?
	}
	
	public void getXMLAttributeString(StringBuilder r) {
		// ?
	}
	
	public void setAttribute(Attribute attr) {
		// ?
	}
	
	public void setDataOfChildElement(Element childElement) {
		// ?
	}
	
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		attributeValueMap.put("max", getMax());
		attributeValueMap.put("min", getMin());
		attributeValueMap.put("replicates", getReplicateID());
		attributeValueMap.put("stddev", getStddev());
		attributeValueMap.put("unit", getUnit());
		attributeValueMap.put("value", getValue());
	}
	
}
