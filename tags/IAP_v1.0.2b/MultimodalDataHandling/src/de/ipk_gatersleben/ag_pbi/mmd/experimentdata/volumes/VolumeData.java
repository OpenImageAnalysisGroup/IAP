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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

import java.util.List;
import java.util.Map;

import org.graffiti.plugin.io.resources.IOurl;
import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.PositionableMeasurement;

public class VolumeData extends NumericMeasurement3D implements BinaryMeasurement, PositionableMeasurement {
	
	private static final String[] additionalAttributeNames = new String[] {
			"dimensionx", "dimensiony", "dimensionz",
			"voxelsizex", "voxelsizey", "voxelsizez",
			"colordepth", "id", "labelurl",
			"colourarray", "positionInUniverse", "rotation" };
	protected int dimensionx;
	protected int dimensiony;
	protected int dimensionz;
	private double voxelsizex;
	private double voxelsizey;
	private double voxelsizez;
	private IOurl url;
	private IOurl labelurl;
	private String colordepth;
	private int rowID;
	private String colourarray;
	private String positionIn3D;
	private String rotation;
	
	public VolumeData(SampleInterface parent) {
		super(parent);
	}
	
	public VolumeData(SampleInterface parent, VolumeData other) {
		this(parent);
		if (other != null) {
			setReplicateID(other.getReplicateID());
			setDimensionX(other.getDimensionX());
			setDimensionY(other.getDimensionY());
			setDimensionZ(other.getDimensionZ());
			setVoxelsizeX(other.getVoxelsizeX());
			setVoxelsizeY(other.getVoxelsizeY());
			setVoxelsizeZ(other.getVoxelsizeZ());
			setReplicateID(other.getReplicateID());
			setColorDepth(other.getColorDepth());
			setColourArray(other.getColourArray());
			setPositionIn3D(other.getPositionIn3D());
			setRotation(other.getRotation());
			setRowID(other.getRowID());
			setURL(new IOurl(other.getURL()));
			if (other.getLabelURL() != null)
				setLabelURL(new IOurl(other.getLabelURL()));
		}
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		VolumeData v = new VolumeData(parent);
		v.setReplicateID(getReplicateID());
		v.setDimensionX(getDimensionX());
		v.setDimensionY(getDimensionY());
		v.setDimensionZ(getDimensionZ());
		v.setVoxelsizeX(getVoxelsizeX());
		v.setVoxelsizeY(getVoxelsizeY());
		v.setVoxelsizeZ(getVoxelsizeZ());
		v.setReplicateID(getReplicateID());
		v.setColorDepth(getColorDepth());
		v.setColourArray(getColourArray());
		v.setPositionIn3D(getPositionIn3D());
		v.setRotation(getRotation());
		v.setRowID(getRowID());
		v.setURL(new IOurl(getURL()));
		if (getLabelURL() != null)
			v.setLabelURL(new IOurl(getLabelURL()));
		return v;
	}
	
	public VolumeData(SampleInterface parent, Map<String, Object> map) {
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
					setAttribute(new Attribute(s, map.get(k) + ""));
				// else
				// System.err.println("Error: Volume Attribute-Value is Null for '" + s + "'.");
			}
		}
	}
	
	private void setRowID(int rowID) {
		this.rowID = rowID;
	}
	
	private int getRowID() {
		return rowID;
	}
	
	public static String typeName = "volume";
	
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
		super.getXMLAttributeString(r);
		Substance.getAttributeString(r, additionalAttributeNames, new Object[] { getDimensionX(),
				getDimensionY(), getDimensionZ(), getVoxelsizeX(), getVoxelsizeY(),
				getVoxelsizeZ(), getColorDepth(), getRowID(),
				getLabelURL() == null ? null : getLabelURL().toString(), getColourArray(), getPositionIn3D(), getRotation() });
	}
	
	@Override
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		else
			if (attr.getName().equals(additionalAttributeNames[0]))
				setDimensionX(Integer.parseInt(attr.getValue()));
			else
				if (attr.getName().equals(additionalAttributeNames[1]))
					setDimensionY(Integer.parseInt(attr.getValue()));
				else
					if (attr.getName().equals(additionalAttributeNames[2]))
						setDimensionZ(Integer.parseInt(attr.getValue()));
					else
						if (attr.getName().equals(additionalAttributeNames[3]))
							setVoxelsizeX(Double.parseDouble(attr.getValue()));
						else
							if (attr.getName().equals(additionalAttributeNames[4]))
								setVoxelsizeY(Double.parseDouble(attr.getValue()));
							else
								if (attr.getName().equals(additionalAttributeNames[5]))
									setVoxelsizeZ(Double.parseDouble(attr.getValue()));
								else
									if (attr.getName().equals(additionalAttributeNames[6]))
										setColorDepth(attr.getValue());
									else
										if (attr.getName().equals(additionalAttributeNames[7]))
											setRowID(Integer.parseInt(attr.getValue()));
										else
											if (attr.getName().equals(additionalAttributeNames[8]))
												setLabelURL(new IOurl(attr.getValue()));
											else
												if (attr.getName().equals(additionalAttributeNames[9]))
													setColourArray(attr.getValue());
												else
													if (attr.getName().equals(additionalAttributeNames[10]))
														setPositionIn3D(attr.getValue());
													else
														if (attr.getName().equals(additionalAttributeNames[11]))
															setRotation(attr.getValue());
														else
															super.setAttribute(attr);
	}
	
	@Override
	public boolean setData(Element averageElement) {
		setURL(new IOurl(averageElement.getValue()));
		
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
	public void setDataOfChildElement(Element childElement) {
		// no children
	}
	
	@Override
	public MeasurementNodeType getType() {
		return MeasurementNodeType.VOLUME;
	}
	
	public void setDimensionZ(int dimensionz) {
		this.dimensionz = dimensionz;
	}
	
	public int getDimensionZ() {
		return dimensionz;
	}
	
	public void setDimensionY(int dimensiony) {
		this.dimensiony = dimensiony;
	}
	
	public int getDimensionY() {
		return dimensiony;
	}
	
	public void setDimensionX(int dimensionx) {
		this.dimensionx = dimensionx;
	}
	
	public int getDimensionX() {
		return dimensionx;
	}
	
	public void setVoxelsizeX(double voxelsizex) {
		this.voxelsizex = voxelsizex;
	}
	
	public double getVoxelsizeX() {
		return voxelsizex;
	}
	
	public void setVoxelsizeY(double voxelsizey) {
		this.voxelsizey = voxelsizey;
	}
	
	public double getVoxelsizeY() {
		return voxelsizey;
	}
	
	public void setVoxelsizeZ(double voxelsizez) {
		this.voxelsizez = voxelsizez;
	}
	
	public double getVoxelsizeZ() {
		return voxelsizez;
	}
	
	@Override
	public void setURL(IOurl url) {
		this.url = url;
	}
	
	public void setColorDepth(String colordepth) {
		this.colordepth = colordepth;
	}
	
	public String getColorDepth() {
		return colordepth;
	}
	
	@Override
	public boolean equalNumericMeasurement(NumericMeasurementInterface meas) {
		String u1 = getRowID() + " " + " " + getDimensionX() + " " + getDimensionY() + " " + getDimensionZ() + " "
				+ getVoxelsizeX() + " " + getVoxelsizeY() + " " + getVoxelsizeZ() + " " + getColorDepth() + " " + getURL();
		String u2 = ((VolumeData) meas).getRowID() + " " + +((VolumeData) meas).getDimensionX() + " "
				+ ((VolumeData) meas).getDimensionY() + " " + ((VolumeData) meas).getDimensionZ() + " "
				+ ((VolumeData) meas).getVoxelsizeX() + " " + ((VolumeData) meas).getVoxelsizeY() + " "
				+ ((VolumeData) meas).getVoxelsizeZ() + " " + ((VolumeData) meas).getColorDepth() + " "
				+ ((VolumeData) meas).getURL();
		return super.equalNumericMeasurement(meas) && u1.equals(u2);
	}
	
	@Override
	public String toString() {
		return "Volume: " + dimensionx + "x" + dimensiony + "x" + dimensionz;
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		super.fillAttributeMap(attributes);
		int idx = 0;
		attributes.put(additionalAttributeNames[idx++], dimensionx);
		attributes.put(additionalAttributeNames[idx++], dimensiony);
		attributes.put(additionalAttributeNames[idx++], dimensionz);
		attributes.put(additionalAttributeNames[idx++], voxelsizex);
		attributes.put(additionalAttributeNames[idx++], voxelsizey);
		attributes.put(additionalAttributeNames[idx++], voxelsizez);
		attributes.put(additionalAttributeNames[idx++], colordepth);
		attributes.put(additionalAttributeNames[idx++], rowID);
		if (labelurl != null)
			attributes.put(additionalAttributeNames[idx++], labelurl.toString());
		else
			idx++;
		attributes.put(additionalAttributeNames[idx++], colourarray);
		attributes.put(additionalAttributeNames[idx++], positionIn3D);
		attributes.put(additionalAttributeNames[idx++], rotation);
		attributes.put("filename", url.toString());
	}
	
	@Override
	public IOurl getLabelURL() {
		return labelurl;
	}
	
	@Override
	public void setLabelURL(IOurl url) {
		labelurl = url;
	}
	
	public String getColourArray() {
		return colourarray;
	}
	
	public void setColourArray(String colourarray) {
		this.colourarray = colourarray;
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
