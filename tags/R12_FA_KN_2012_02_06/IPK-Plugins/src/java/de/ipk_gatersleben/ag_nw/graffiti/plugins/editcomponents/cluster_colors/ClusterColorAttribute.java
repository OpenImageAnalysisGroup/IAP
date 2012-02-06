/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.cluster_colors;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;

import org.Colors;
import org.ErrorMsg;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;

/**
 * All StringAttributes with the name id "chart_colors" will be converted
 * into a ChartColorAttribute (see ChartAttributePlugin, where the mapping
 * information from id to class type is initialized.
 * 
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ClusterColorAttribute extends StringAttribute {
	// format of color string:
	// color-bar1:outline-bar1;color-bar2:outline-bar2;col3:outline3 ...
	private String value;
	public static String attributeName = "cluster_colors";
	public static String attributeFolder = "";
	public static String desc = "<html>Modify the color (fill/first row and outline/second row)<br>of the nodes with cluster information and the color of the nodes in the cluster-graph";
	
	private String notSet = "undefined"; // "0,0,0,255:255,255,255,255;255,0,0,255:0,255,255,255;50,50,0,255:255,55,55,255";
	
	public ClusterColorAttribute() {
		super(attributeName);
		setDescription(desc); // tooltip
		value = notSet;
	}
	
	public ClusterColorAttribute(String id) {
		super(id);
		setDescription(desc); // tooltip
		value = notSet;
	}
	
	public ClusterColorAttribute(String id, String value) {
		super(id);
		this.value = value;
	}
	
	@Override
	public void setDefaultValue() {
		value = notSet;
	}
	
	@Override
	public void setString(String value) {
		assert value != null;
		
		AttributeEvent ae = new AttributeEvent(this);
		callPreAttributeChanged(ae);
		this.value = value;
		callPostAttributeChanged(ae);
	}
	
	@Override
	public String getString() {
		return value;
	}
	
	@Override
	public Object getValue() {
		return value;
	}
	
	@Override
	public Object copy() {
		return new ClusterColorAttribute(this.getId(), this.value);
	}
	
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + value + "\"";
	}
	
	@Override
	public String toXMLString() {
		return getStandardXML(value);
	}
	
	public ArrayList<Color> getClusterColors() {
		return interpreteColorString(0);
	}
	
	public ArrayList<Color> getClusterOutlineColors() {
		return interpreteColorString(1);
	}
	
	public Color getClusterColor(int clusterID) {
		return interpreteColorString(0).get(clusterID);
	}
	
	public Color getClusterOutlineColor(int clusterID) {
		return interpreteColorString(1).get(clusterID);
	}
	
	private ArrayList<Color> interpreteColorString(int type0bar_1outline) {
		if (value == null || value.equals(notSet))
			return null;
		else {
			String[] cols = value.split(";");
			ArrayList<Color> result = new ArrayList<Color>();
			for (int i = 0; i < cols.length; i++) {
				String barCol_outCol = cols[i];
				Color p;
				if (barCol_outCol.length() == 0)
					p = null;
				else {
					String colComp = barCol_outCol.split(":")[type0bar_1outline];
					if (colComp.equals("null") || colComp.equals(notSet))
						p = null;
					else {
						String[] rgba_s = colComp.split(",");
						int[] rgba = new int[rgba_s.length];
						for (int ir = 0; ir < rgba.length; ir++)
							rgba[ir] = Integer.parseInt(rgba_s[ir]);
						p = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
					}
				}
				if (p == null) {
					Color[] defCols = Colors.getColors(cols.length);
					if (type0bar_1outline == 0)
						p = defCols[i];
					else
						p = Color.BLACK;
				}
				result.add(p);
			}
			return result;
		}
	}
	
	private void setColorString(int idx0bar_1outline, int series, Paint newColor) {
		String[] cols = value.split(";");
		
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < cols.length; i++) {
			if (i != series) {
				if (result.length() > 0)
					result.append(";" + cols[i]);
				else
					result.append(cols[i]);
			} else {
				String barCol_outCol = cols[series];
				if (result.length() > 0)
					result.append(";");
				if (idx0bar_1outline == 0)
					result.append(getColorCode(newColor) + ":" + nullForEmpty(barCol_outCol.split(":")[1]));
				else
					result.append(nullForEmpty(barCol_outCol.split(":")[0]) + ":" + getColorCode(newColor));
			}
		}
		value = result.toString();
		// setValue(result);
	}
	
	/**
	 * @param string
	 * @return
	 */
	private String nullForEmpty(String string) {
		if (string == null || string.length() == 0)
			return "null";
		else
			return string;
	}
	
	private String getColorCode(Paint newColor) {
		if (newColor == null)
			return "null";
		else {
			if (newColor instanceof Color) {
				Color c = (Color) newColor;
				return c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "," + c.getAlpha();
			} else
				return "null";
		}
	}
	
	public void trimColorSelection(int clusterCount) {
		ensureMinimumColorSelection(clusterCount);
		boolean set = false;
		if (value.equals(notSet)) {
			value = "null:null";
			set = true;
		}
		if (clusterCount > 0)
			while (value.length() - value.replaceAll(";", "").length() > clusterCount - 1) {
				value = value.substring(0, value.lastIndexOf(";"));
				set = true;
			}
		if (set) {
			try {
				setValue(value);
			} catch (ClassCastException cce) {
				ErrorMsg.addErrorMessage(cce.getLocalizedMessage());
			}
		}
	}
	
	public void ensureMinimumColorSelection(int clusterCount) {
		boolean set = false;
		if (value.equals(notSet)) {
			value = "null:null";
			set = true;
		}
		while (value.length() - value.replaceAll(";", "").length() < clusterCount - 1) {
			value = value + ";null:null";
			set = true;
		}
		if (set) {
			try {
				setValue(value);
			} catch (ClassCastException cce) {
				ErrorMsg.addErrorMessage(cce.getLocalizedMessage());
			}
		}
	}
	
	@Override
	protected void doSetValue(Object o) throws IllegalArgumentException {
		assert o != null;
		
		try {
			value = (String) o;
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	public int getDefinedClusterColorCount() {
		if (value == null || value.equals(notSet))
			return 0;
		else
			return value.length() - value.replaceAll(";", "").length() + 1;
	}
	
	public void setClusterColor(int clusterID, Color color) {
		ensureMinimumColorSelection(clusterID);
		setColorString(0, clusterID, color);
	}
	
	public void setClusterOutlineColor(int clusterID, Color color) {
		ensureMinimumColorSelection(clusterID);
		setColorString(1, clusterID, color);
	}
	
	public static ClusterColorAttribute getDefaultValue(int clusterCount) {
		ClusterColorAttribute cca = new ClusterColorAttribute(attributeName);
		cca.ensureMinimumColorSelection(clusterCount);
		Color[] defCols = Colors.getColors(clusterCount);
		for (int i = 0; i < defCols.length; i++) {
			cca.setClusterColor(i, defCols[i]);
			cca.setClusterOutlineColor(i, Color.BLACK);
		}
		return cca;
	}
	
}