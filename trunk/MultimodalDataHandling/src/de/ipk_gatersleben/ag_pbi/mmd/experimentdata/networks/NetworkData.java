/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.networks;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.io.resources.IOurl;
import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MyAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.PositionableMeasurement;

public class NetworkData extends NumericMeasurement3D
		implements BinaryMeasurement, PositionableMeasurement {
	
	private String name;
	private String group;
	private String source;
	private IOurl url;
	private String positionIn3D;
	private String rotation;
	private IOurl labelurl;
	
	private static final String[] attributeNames = new String[] {
			"name", "group", "source", "positionInUniverse", "rotation", "labelurl" };
	
	public NetworkData(SampleInterface parent) {
		super(parent);
	}
	
	public NetworkData(SampleInterface parent, NetworkData other) {
		this(parent);
		setReplicateID(other.getReplicateID());
		setGroup(other.getGroup());
		setName(other.getName());
		setSource(other.getSource());
		setPositionIn3D(other.getPositionIn3D());
		setRotation(other.getRotation());
		setURL(new IOurl(other.getURL()));
		if (other.getLabelURL() != null)
			setLabelURL(new IOurl(other.getLabelURL()));
	}
	
	public NetworkData(SampleInterface parent, Map<String, Object> map) {
		super(parent, map);
		if (map.containsKey("filename")) {
			setURL(new IOurl((String) map.get("filename")));
		}
		for (Object k : map.keySet()) {
			if (k instanceof String) {
				String s = (String) k;
				if (s.equals("filename"))
					continue;
				if (map.get(k) != null)
					setAttribute(new MyAttribute(s, map.get(k) + ""));
				// else
				// System.err.println("Error: Network Attribute-Value is Null for '" + s + "'.");
			}
		}
	}
	
	@Override
	public String toString() {
		return "Network: " + getURL();
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		super.fillAttributeMap(attributes);
		int idx = 0;
		attributes.put(attributeNames[idx++], getName());
		attributes.put(attributeNames[idx++], getGroup());
		attributes.put(attributeNames[idx++], getSource());
		attributes.put(attributeNames[idx++], getPositionIn3D());
		attributes.put(attributeNames[idx++], getRotation());
		attributes.put(attributeNames[idx++], getLabelURL());
		attributes.put("filename", url.toString());
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		NetworkData n = new NetworkData(parent);
		n.setReplicateID(getReplicateID());
		n.setGroup(getGroup());
		n.setName(getName());
		n.setSource(getSource());
		n.setPositionIn3D(getPositionIn3D());
		n.setRotation(getRotation());
		n.setURL(new IOurl(getURL()));
		if (getLabelURL() != null)
			n.setLabelURL(new IOurl(getLabelURL()));
		return n;
	}
	
	public static String typeName = "network";
	
	@Override
	public void getString(StringBuilder r) {
		r.append("<" + typeName);
		getXMLAttributeString(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</" + typeName + ">");
	}
	
	@Override
	public void getStringOfChildren(StringBuilder r) {
		r.append(getURL());
	}
	
	@Override
	public double getValue() {
		return Double.NaN;
	}
	
	@Override
	public IOurl getURL() {
		return url;
	}
	
	@Override
	public void getXMLAttributeString(StringBuilder r) {
		Substance.getAttributeString(r, attributeNames, new Object[] { getName(), getGroup(), getSource(), getPositionIn3D(), getRotation(),
				getLabelURL() == null ? null : getLabelURL().toString() });
	}
	
	@Override
	public void setAttribute(MyAttribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		if (attr.getName().equals(attributeNames[0]))
			setName(attr.getValue());
		else
			if (attr.getName().equals(attributeNames[1]))
				setGroup(attr.getValue());
			else
				if (attr.getName().equals(attributeNames[2]))
					setSource(attr.getValue());
				else
					if (attr.getName().equals(attributeNames[3]))
						setPositionIn3D(attr.getValue());
					else
						if (attr.getName().equals(attributeNames[4]))
							setRotation(attr.getValue());
						else
							if (attr.getName().equals(attributeNames[5]))
								setLabelURL(new IOurl(attr.getValue()));
	}
	
	@Override
	public boolean setData(Element averageElement) {
		setURL(new IOurl(averageElement.getValue()));
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
	public void setDataOfChildElement(Element childElement) {
		// no children
	}
	
	@Override
	public MeasurementNodeType getType() {
		return MeasurementNodeType.NETWORK;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getGroup() {
		return group;
	}
	
	@Override
	public void setURL(IOurl url) {
		this.url = url;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return source;
	}
	
	@Override
	public boolean equalNumericMeasurement(NumericMeasurementInterface meas) {
		String u1 = getName() + " " + getSource() + " " + getGroup() + " " + getURL() + " " + getLabelURL();
		String u2 = ((NetworkData) meas).getName() + " " + ((NetworkData) meas).getSource() + " "
				+ ((NetworkData) meas).getGroup() + " " + ((NetworkData) meas).getURL() + " " + ((NetworkData) meas).getLabelURL();
		return u1.equals(u2);
	}
	
	@Override
	public IOurl getLabelURL() {
		return labelurl;
	}
	
	@Override
	public void setLabelURL(IOurl url) {
		this.labelurl = url;
	}
	
	public ExperimentInterface getOmicsFromNetwork() {
		return getOmicsFromNetwork(getURL());
	}
	
	public static ExperimentInterface getOmicsFromNetwork(IOurl url) {
		Experiment list = new Experiment();
		Graph network = null;
		try {
			network = MainFrame.getInstance().getGraph(url, url.getFileName());
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			network = null;
		}
		if (network != null)
			for (GraphElement ed : network.getGraphElements())
				list.addAndMerge(new GraphElementHelper(ed).getDataMappings());
		return list;
	}
	
	@Override
	public String getPositionIn3D() {
		return positionIn3D;
	}
	
	@Override
	public void setPositionIn3D(String positionIn3D) {
		this.positionIn3D = positionIn3D;
	}
	
	@Override
	public String getRotation() {
		return rotation;
	}
	
	@Override
	public void setRotation(String rotation) {
		this.rotation = rotation;
	}
}
