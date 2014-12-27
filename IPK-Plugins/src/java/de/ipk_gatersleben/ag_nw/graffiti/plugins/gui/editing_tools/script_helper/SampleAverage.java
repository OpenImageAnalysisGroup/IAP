package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.MeasurementFilter;
import org.jdom.Attribute;
import org.jdom.Element;

public class SampleAverage implements SampleAverageInterface {
	private double max, min, stddev, value;
	private int replicates;
	private final SampleInterface parentSample;
	private String ownUnit = null;
	
	@Override
	public String toString() {
		return "[average=" + value + ", stddev=" + stddev + ", replicate=" + replicates + " / " + parentSample + "]";
	}
	
	public SampleAverage(SampleInterface parent) {
		this.parentSample = parent;
		calculateValuesFromSampleData();
	}
	
	@Override
	public SampleAverage clone() {
		SampleAverage sa = new SampleAverage(parentSample);
		sa.max = max;
		sa.min = min;
		sa.stddev = stddev;
		sa.value = value;
		sa.replicates = replicates;
		sa.ownUnit = ownUnit;
		return sa;
	}
	
	@SuppressWarnings("unchecked")
	public SampleAverage(SampleInterface sample, Map map) {
		this(sample);
		for (Object key : map.keySet()) {
			if (map.get(key) == null)
				continue;
			if (key.equals("value")) {
				setValue((Double) map.get("value"));
			} else
				if (map.get(key) != null) {
					if (map.get(key) instanceof Double)
						setValueFromAttribute((String) key, (Double) map.get(key), null);
					else {
						if (map.get(key) instanceof Integer)
							setValueFromAttribute((String) key, null, (Integer) map.get(key));
						else {
							String ss = map.get(key).toString();
							if (!ss.isEmpty())
								setValueFromAttribute(new MyAttribute((String) key, ss));
						}
					}
				}
		}
	}
	
	@Override
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
	
	@Override
	public double getValue() {
		return value;
	}
	
	@Override
	public double getValue(MeasurementFilter pf) {
		if (parentSample.size() == 0) {
			return Double.NaN;
		} else {
			int n = 0;
			double sum = 0;
			for (NumericMeasurementInterface m : parentSample) {
				if (pf.filterOut(m.getQualityAnnotation(), getParentSample().getTime()))
					continue;
				double v = m.getValue();
				if (Double.isNaN(v))
					continue;
				n++;
				sum += v;
			}
			
			if (n == 0) {
				return Double.NaN;
			}
			return sum / n;
		}
	}
	
	@Override
	public SampleInterface getParentSample() {
		return parentSample;
	}
	
	@Override
	public void calculateValuesFromSampleData() {
		if (parentSample.size() == 0) {
			setMin(Double.NaN);
			setMax(Double.NaN);
			setStddev(Double.NaN);
			setValue(Double.NaN);
		} else {
			int n = 0;
			
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
	
	@Override
	public double getStdDev() {
		return getStddev();
	}
	
	@Override
	public String getUnit() {
		if (ownUnit == null)
			return parentSample.getAverageUnit();
		else
			return ownUnit;
	}
	
	@Override
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
				setValueFromAttribute(new MyAttribute(a));
			}
		}
		return true;
	}
	
	private void setValueFromAttribute(MyAttribute a) {
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
	
	private void setValueFromAttribute(String name, Double value, Integer intValue) {
		if (value == null)
			return;
		if (name.equals("max")) {
			setMax(value);
		} else
			if (name.equals("min")) {
				setMin(value);
			} else
				if (name.equals("replicates")) {
					setReplicateId(intValue);
				} else
					if (name.equals("stddev")) {
						setStddev(value);
					} else
						System.err.println("Internal Error: Unknown Average Attribute: " + name);
	}
	
	@Override
	public void setUnit(String ownUnit) {
		this.ownUnit = ownUnit != null ? ownUnit.intern() : null;
	}
	
	@Override
	public void setReplicateId(int replicates) {
		this.replicates = replicates;
	}
	
	@Override
	public int getReplicateID() {
		return replicates;
	}
	
	@Override
	public void setMax(double max) {
		this.max = max;
	}
	
	@Override
	public double getMax() {
		return max;
	}
	
	@Override
	public void setMin(double min) {
		this.min = min;
	}
	
	@Override
	public double getMin() {
		return min;
	}
	
	@Override
	public void setStddev(double stddev) {
		this.stddev = stddev;
	}
	
	@Override
	public double getStddev() {
		return stddev;
	}
	
	@Override
	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public void getStringOfChildren(StringBuilder r) {
		// ?
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		// ?
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		// ?
	}
	
	@Override
	public void setDataOfChildElement(Element childElement) {
		// ?
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		attributeValueMap.put("max", getMax());
		attributeValueMap.put("min", getMin());
		attributeValueMap.put("replicates", getReplicateID());
		attributeValueMap.put("stddev", getStddev());
		attributeValueMap.put("unit", getUnit());
		attributeValueMap.put("value", getValue());
	}
	
	@Override
	public Object getAttributeField(String id) {
		throw new UnsupportedOperationException("not yet implemented for this data structure!");
	}
	
	@Override
	public String getFiles() {
		throw new UnsupportedOperationException("Not implemented for this data type");
	}
	
	@Override
	public void setFiles(String files) {
		throw new UnsupportedOperationException("Not implemented for this data type");
	}
	
}
