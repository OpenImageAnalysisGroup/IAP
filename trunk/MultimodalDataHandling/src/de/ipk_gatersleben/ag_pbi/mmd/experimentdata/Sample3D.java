/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks.NetworkData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public class Sample3D extends Sample {
	
	private String component = null;
	
	// private final ArrayList<NumericMeasurementInterface> binaryMeasurements;
	
	public Sample3D(ConditionInterface parent) {
		super(parent);
		// binaryMeasurements = new ArrayList<NumericMeasurementInterface>();
	}
	
	@SuppressWarnings("rawtypes")
	public Sample3D(Condition3D parent, Map map) {
		this(parent);
		for (Object k : map.keySet()) {
			if (k instanceof String) {
				String key = (String) k;
				if (key.equals("average"))
					continue;
				if (key.equals("images"))
					continue;
				if (key.equals("volumes"))
					continue;
				if (key.equals("networks"))
					continue;
				if (key.equals("measurements"))
					continue;
				Object o = map.get(key);
				if (o == null)
					continue;
				if (o instanceof String) {
					if (!((String) o).isEmpty())
						setAttribute(new Attribute(key, (String) o));
				} else
					setAttribute(new Attribute(key, o + ""));
			}
		}
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		super.getXMLAttributeString(r);
		Substance.getAttributeString(r, new String[] { "component" }, new String[] { getComponent() });
	}
	
	public String getComponent() {
		if (component != null && component.equalsIgnoreCase(""))
			component = null;
		return component;
	}
	
	/**
	 * @param type
	 *           Desired measurement type or NULL in case all measurements should be returned.
	 * @return Measurements of defined type, or all measurements, if type is unspecified (NULL).
	 */
	public Collection<NumericMeasurementInterface> getMeasurements(MeasurementNodeType... type) {
		
		ArrayList<NumericMeasurementInterface> list = new ArrayList<NumericMeasurementInterface>();
		if (type == null || type.length == 0 || (type.length == 1 && type[0] == null))
			for (NumericMeasurementInterface m : this)
				list.add(m);
		else
			for (NumericMeasurementInterface m : this)
				typeLoop: for (MeasurementNodeType t : type)
					if (((NumericMeasurement3D) m).getType() == t) {
						list.add(m);
						break typeLoop;
					}
		
		return list;
	}
	
	// public ArrayList<NumericMeasurementInterface> getAllMeasurements() {
	// ArrayList<NumericMeasurementInterface> allMeasurements = new ArrayList<NumericMeasurementInterface>();
	// allMeasurements.addAll(this);
	// allMeasurements.addAll(binaryMeasurements);
	// return allMeasurements;
	// }
	//
	// public ArrayList<NumericMeasurementInterface> getBinaryMeasurements() {
	// return binaryMeasurements;
	// }
	
	@Override
	public boolean setData(Element sampleElement) {
		List<?> attributeList = sampleElement.getAttributes();
		for (Object o : attributeList) {
			if (o instanceof Attribute) {
				Attribute a = (Attribute) o;
				setAttribute(a);
			}
		}
		List<?> childrenList = sampleElement.getChildren();
		for (Object o : childrenList) {
			if (o instanceof Element) {
				Element childElement = (Element) o;
				if (childElement.getName().equals("volume")) {
					VolumeData v = new VolumeData(this);
					if (v.setData(childElement))
						add(v);
				} else
					if (childElement.getName().equals("image")) {
						ImageData i = new ImageData(this);
						if (i.setData(childElement))
							add(i);
					} else
						if (childElement.getName().equals("network")) {
							NetworkData n = new NetworkData(this);
							if (n.setData(childElement))
								add(n);
						} else
							super.setDataOfChildElement(childElement);
			}
		}
		return true;
	}
	
	@Override
	public void setDataOfChildElement(Element childElement) {
		if (childElement.getName().equals("network")) {
			NetworkData nd = new NetworkData(this);
			if (nd.setData(childElement))
				add(nd);
		} else
			if (childElement.getName().equals("image")) {
				ImageData id = new ImageData(this);
				if (id.setData(childElement))
					add(id);
			} else
				if (childElement.getName().equals("volume")) {
					VolumeData vd = new VolumeData(this);
					if (vd.setData(childElement))
						add(vd);
				} else
					super.setDataOfChildElement(childElement);
		
	}
	
	@Override
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null || attr.getValue().length() == 0)
			return;
		if (attr.getName().equals("component"))
			setComponent(attr.getValue());
		else
			super.setAttribute(attr);
	}
	
	@Override
	public int compareTo(SampleInterface sd) {
		if (sd instanceof Sample3D) {
			int res = super.compareTo(sd);
			if (res != 0)
				return res;
			else {
				String u3 = getComponent();
				String u4 = ((Sample3D) sd).getComponent();
				u3 = (u3 != null ? u3 : "");
				u4 = (u4 != null ? u4 : "");
				return u3.compareTo(u4);
			}
		} else
			return super.compareTo(sd);
		
	}
	
	// @Override
	// public boolean remove(Object o) {
	// return super.remove(o) || binaryMeasurements.remove(o);
	// }
	
	public void setComponent(String component) {
		this.component = component;
	}
	
	// @Override
	// public boolean add(NumericMeasurementInterface m) {
	// if (m instanceof VolumeData || m instanceof NetworkData || m instanceof ImageData)
	// return binaryMeasurements.add(m);
	// else
	// return super.add(m);
	// }
	//
	public String getName() {
		return getTime() + " " + getTimeUnit() + (getComponent() != null ? " " + getComponent() : "");
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		super.fillAttributeMap(attributeValueMap);
		if (component != null)
			attributeValueMap.put("component", component);
	}
	
	// @Override
	// public boolean equals(Object obj) {
	// if (obj == null)
	// return false;
	// if (!(obj instanceof Sample3D))
	// return false;
	// boolean s = super.equals(obj);
	// if (!s)
	// return false;
	//
	// if (component == null && ((Sample3D) obj).component == null)
	// return true;
	// if (component == null || ((Sample3D) obj).component == null)
	// return false;
	// return component.equals(((Sample3D) obj).component);
	// }
	//
	// @Override
	// public int hashCode() {
	// return (super.hashCode() + component).hashCode();
	// }
	//
	@Override
	public String toString() {
		if (component != null && component.length() > 0)
			return getTimeUnit() + " " + getTime() + " " + component;
		else
			return super.toString();
	}
	
	@Override
	public SampleInterface clone(ConditionInterface parent) {
		Sample3D s = (Sample3D) super.clone(parent);
		s.setComponent(getComponent());
		return s;
	}
}
