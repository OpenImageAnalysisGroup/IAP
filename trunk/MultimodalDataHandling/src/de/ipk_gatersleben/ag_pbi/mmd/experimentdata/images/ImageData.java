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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images;

import java.util.List;
import java.util.Map;

import org.ErrorMsg;
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

public class ImageData extends NumericMeasurement3D implements BinaryMeasurement, PositionableMeasurement {
	
	private static final String[] additionalAttributeNames = new String[] { "pixelsizex", "pixelsizey", "thickness",
						"id", "labelurl", "positionInUniverse", "rotation" };
	private double pixelsizex;
	private double pixelsizey;
	private double thickness;
	private IOurl url;
	private IOurl labelurl;
	private int id;
	private String positionIn3D;
	private String rotation;
	
	public ImageData(SampleInterface parent) {
		super(parent);
	}
	
	public ImageData(SampleInterface parent, Map<String, Object> map) {
		super(parent, map);
		if (map.containsKey("pixelsizex"))
			setPixelsizeX((Double) map.get("pixelsizex"));
		if (map.containsKey("pixelsizey"))
			setPixelsizeX((Double) map.get("pixelsizey"));
		if (map.containsKey("thickness"))
			setPixelsizeX((Double) map.get("thickness"));
		if (map.containsKey("id"))
			id = (Integer) map.get("id");
		if (map.containsKey("filename"))
			setURL(new IOurl((String) map.get("filename")));
		if (map.containsKey("labelurl"))
			setLabelURL(new IOurl((String) map.get("labelurl")));
	}
	
	@Override
	public IOurl getURL() {
		return url;
	}
	
	@Override
	public String toString() {
		return "Image: " + pixelsizex + "x" + pixelsizey + " " + getURL();
	}
	
	public ImageData(SampleInterface parent, ImageData other) {
		this(parent);
		setRowID(other.getRowID());
		setReplicateID(other.getReplicateID());
		setPixelsizeX(other.getPixelsizeX());
		setPixelsizeY(other.getPixelsizeY());
		setPosition(other.getPosition());
		setPositionUnit(other.getPositionUnit());
		setThickness(other.getThickness());
		setPositionIn3D(other.getPositionIn3D());
		setRotation(other.getRotation());
		setURL(new IOurl(other.getURL()));
		if (other.getLabelURL() != null)
			setLabelURL(new IOurl(other.getLabelURL()));
	}
	
	@Override
	public NumericMeasurementInterface clone(SampleInterface parent) {
		ImageData i = new ImageData(parent);
		i.setRowID(getRowID());
		i.setReplicateID(getReplicateID());
		i.setPixelsizeX(getPixelsizeX());
		i.setPixelsizeY(getPixelsizeY());
		i.setPosition(getPosition());
		i.setPositionUnit(getPositionUnit());
		i.setThickness(getThickness());
		i.setPositionIn3D(getPositionIn3D());
		i.setRotation(getRotation());
		i.setURL(new IOurl(getURL()));
		if (getLabelURL() != null)
			i.setLabelURL(new IOurl(getLabelURL()));
		return i;
	}
	
	public void setRowID(int id) {
		this.id = id;
	}
	
	public int getRowID() {
		return id;
	}
	
	@Override
	public void getString(StringBuilder r) {
		r.append("<image");
		getXMLAttributeString(r);
		r.append(">");
		getStringOfChildren(r);
		r.append("</image>");
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
	public void getXMLAttributeString(StringBuilder r) {
		super.getXMLAttributeString(r);
		Substance.getAttributeString(r, additionalAttributeNames, new Object[] { getPixelsizeX(),
							getPixelsizeY(), getThickness(), getRowID(),
							getLabelURL() == null ? null : getLabelURL().toString(), getPositionIn3D(), getRotation() });
	}
	
	@Override
	public void setAttribute(Attribute attr) {
		if (attr == null || attr.getValue() == null)
			return;
		if (attr.getName().equals(additionalAttributeNames[0]))
			try {
				setPixelsizeX(Double.parseDouble(attr.getValue()));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		else
			if (attr.getName().equals(additionalAttributeNames[1]))
				try {
					setPixelsizeY(Double.parseDouble(attr.getValue()));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			else
				if (attr.getName().equals(additionalAttributeNames[2]))
					try {
						setThickness(Double.parseDouble(attr.getValue()));
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
				
				else
					if (attr.getName().equals(additionalAttributeNames[3]))
						try {
							setRowID(Integer.parseInt(attr.getValue()));
						} catch (Exception e) {
							ErrorMsg.addErrorMessage(e);
						}
					else
						if (attr.getName().equals(additionalAttributeNames[4]))
							setLabelURL(new IOurl(attr.getValue()));
						else
							if (attr.getName().equals(additionalAttributeNames[5]))
								setPositionIn3D(attr.getValue());
							else
								if (attr.getName().equals(additionalAttributeNames[6]))
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
		return MeasurementNodeType.IMAGE;
	}
	
	public void setPixelsizeX(double pixelsizex) {
		this.pixelsizex = pixelsizex;
	}
	
	public double getPixelsizeX() {
		return pixelsizex;
	}
	
	public void setPixelsizeY(double pixelsizey) {
		this.pixelsizey = pixelsizey;
	}
	
	public double getPixelsizeY() {
		return pixelsizey;
	}
	
	@Override
	public void setURL(IOurl url) {
		this.url = url;
	}
	
	public void setThickness(double thickness) {
		this.thickness = thickness;
	}
	
	public double getThickness() {
		return thickness;
	}
	
	@Override
	public boolean equalNumericMeasurement(NumericMeasurementInterface meas) {
		String u1 = getRowID() + " " + getPixelsizeX() + " " + getPixelsizeY() + " " + getThickness() + " " + getURL();
		String u2 = ((ImageData) meas).getRowID() + " " + +((ImageData) meas).getPixelsizeX() + " "
							+ ((ImageData) meas).getPixelsizeY() + " " + ((ImageData) meas).getThickness() + " "
							+ ((ImageData) meas).getURL();
		return super.equalNumericMeasurement(meas) && u1.equals(u2);
	}
	
	@Override
	public void fillAttributeMap(Map<String, Object> attributes) {
		super.fillAttributeMap(attributes);
		int idx = 0;
		attributes.put(additionalAttributeNames[idx++], pixelsizex);
		attributes.put(additionalAttributeNames[idx++], pixelsizex);
		attributes.put(additionalAttributeNames[idx++], thickness);
		attributes.put(additionalAttributeNames[idx++], id);
		if (labelurl != null)
			attributes.put(additionalAttributeNames[idx++], labelurl.toString());
		else
			idx++;
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
	
	public ImageData copy() {
		return new ImageData(this.getParentSample(), this);
	}
	
}