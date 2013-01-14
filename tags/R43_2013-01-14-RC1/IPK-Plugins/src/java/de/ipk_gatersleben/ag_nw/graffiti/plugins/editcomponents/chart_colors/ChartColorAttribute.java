/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.AttributeHelper;
import org.Colors;
import org.ErrorMsg;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;
import org.graffiti.graph.Graph;

/**
 * All StringAttributes with the name id "chart_colors" will be converted
 * into a ChartColorAttribute (see ChartAttributePlugin, where the mapping
 * information from id to class type is initialized.
 * 
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ChartColorAttribute extends StringAttribute {
	// format of color string:
	// color-bar1:outline-bar1;color-bar2:outline-bar2;col3:outline3 ...
	private String value;
	public static String attributeName = "chart_colors";
	public static String attributeFolder = "";
	
	private final String notSet = "undefined"; // "0,0,0,255:255,255,255,255;255,0,0,255:0,255,255,255;50,50,0,255:255,55,55,255";
	
	public ChartColorAttribute() {
		super(attributeName);
		value = notSet;
	}
	
	public ChartColorAttribute(String id) {
		super(id);
		setDescription("Modify the colors of bars and lines in the charts"); // tooltip
		value = notSet;
	}
	
	public ChartColorAttribute(String id, String value) {
		super(id);
		this.value = value;
	}
	
	@Override
	public void setDefaultValue() {
		value = notSet;
	}
	
	public static ChartColorAttribute getAttribute(Graph graph) {
		ChartColorAttribute chartColorAttribute = (ChartColorAttribute) AttributeHelper.getAttributeValue(
							graph, ChartColorAttribute.attributeFolder,
							ChartColorAttribute.attributeName,
							new ChartColorAttribute(), new ChartColorAttribute(), false);
		return chartColorAttribute;
	}
	
	public static boolean hasAttribute(Graph graph) {
		return AttributeHelper.hasAttribute(graph, ChartColorAttribute.attributeFolder, ChartColorAttribute.attributeName);
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
		return new ChartColorAttribute(this.getId(), this.value);
	}
	
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + value + "\"";
	}
	
	@Override
	public String toXMLString() {
		return getStandardXML(value);
	}
	
	public ArrayList<Color> getSeriesColors(Collection<String> idList) {
		return getSeriesOrOutlineColors(idList, 0);
	}
	
	private ArrayList<Color> getSeriesOrOutlineColors(Collection<String> idList, int zeroInner_oneOutline) {
		HashSet<String> innerIds = new HashSet<String>();
		ArrayList<String> innerIdsAL = new ArrayList<String>();
		String curVal = "";
		if (getAttributable() != null)
			curVal = (String) AttributeHelper.getAttributeValue(getAttributable(), "", "chart_color_line_names", "", "");
		String[] values = curVal.split(";");
		for (String v : values)
			if (v.length() > 0 && !innerIds.contains(v)) {
				innerIds.add(v);
				innerIdsAL.add(v);
			}
		
		for (String o : idList) {
			if (o != null && o.indexOf("§") > 0)
				o = o.substring(0, o.lastIndexOf("§"));
			String v = o;
			if (v.length() > 0 && !innerIds.contains(v)) {
				innerIds.add(v);
				innerIdsAL.add(v);
			}
		}
		ensureMinimumColorSelection(innerIds.size());
		ArrayList<Color> colors = interpreteColorString(zeroInner_oneOutline);
		ArrayList<Color> result = new ArrayList<Color>();
		HashMap<String, Color> id2col = new HashMap<String, Color>();
		int i = 0;
		for (String id : innerIdsAL) {
			Color c;
			try {
				c = colors.get(i++);
			} catch (Exception e) {
				c = Color.yellow;
			}
			id2col.put(id, c);
		}
		
		for (String o : idList) {
			if (o != null && o.indexOf("§") > 0)
				o = o.substring(0, o.lastIndexOf("§"));
			result.add(id2col.get(o));
		}
		
		StringBuilder sb = new StringBuilder();
		for (String v : innerIdsAL)
			sb.append(v + ";");
		if (getAttributable() != null) {
			if (getAttributable() instanceof Graph) {
				boolean mod = ((Graph) getAttributable()).isModified();
				AttributeHelper.setAttribute(getAttributable(), "", "chart_color_line_names", sb.toString());
				if (!mod)
					((Graph) getAttributable()).setModified(false);
			} else
				AttributeHelper.setAttribute(getAttributable(), "", "chart_color_line_names", sb.toString());
		}
		
		return result;
	}
	
	public ArrayList<Color> getSeriesOutlineColors(Collection<String> idList) {
		return getSeriesOrOutlineColors(idList, 1);
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
				result.add(p);
			}
			return result;
		}
	}
	
	private void setColorString(int idx0bar_1outline, int series, Paint newColor) {
		String[] cols = value.split(";");
		while (cols.length < series) {
			value += ";null:null";
			cols = value.split(";");
		}
		
		if (cols[series].indexOf(":") < 0)
			cols[series] = "null:null";
		if (idx0bar_1outline == 0) {
			cols[series] = getColorCode(newColor) + ":" + nullForEmpty(cols[series].split(":")[1]);
		} else
			cols[series] = nullForEmpty(cols[series].split(":")[0]) + ":" + getColorCode(newColor);
		
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < cols.length; i++) {
			result.append(cols[i]);
			if (i < cols.length - 1)
				result.append(";");
		}
		value = result.toString();
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
	
	public void ensureMinimumColorSelection(int barCount) {
		boolean set = false;
		ArrayList<Color> colorList = Colors.getGrayColors(barCount);
		int i = 0;
		if (value.equals(notSet)) {
			// value="null:null";
			Color newColor = colorList.get(i++);
			value = getColorCode(newColor) + ":" + getColorCode(Color.BLACK) + "";
			set = true;
		}
		while (value.length() - value.replaceAll(";", "").length() < barCount - 1) {
			// value=value+";null:null";
			Color newColor = colorList.get(i++);
			value = value + ";" + getColorCode(newColor) + ":" + getColorCode(Color.BLACK) + "";
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
	
	public int getDefinedBarCount() {
		if (value == null || value.equals(notSet))
			return 0;
		else
			return value.length() - value.replaceAll(";", "").length() + 1;
	}
	
	public void setSeriesColor(int series, Color color) {
		ensureMinimumColorSelection(series);
		setColorString(0, series, color);
	}
	
	public void setSeriesOutlineColor(int series, Color color) {
		ensureMinimumColorSelection(series);
		setColorString(1, series, color);
	}
	
	public ArrayList<String> getIdList(int minimumReturn) {
		ArrayList<String> innerIds = new ArrayList<String>();
		String curVal = (String) AttributeHelper.getAttributeValue(getAttributable(), "", "chart_color_line_names", "", "");
		String[] values = curVal.split(";");
		for (String v : values)
			if (v.length() > 0)
				innerIds.add(v);
		
		while (innerIds.size() < minimumReturn)
			innerIds.add("");
		return innerIds;
	}
	
	public Color getSeriesColor(String serie, Color returnIfNotDefined) {
		String curVal = (String) AttributeHelper.getAttributeValue(getAttributable(), "", "chart_color_line_names", "", "");
		String[] values = curVal.split(";");
		int idx = -1;
		int i = 0;
		for (String v : values) {
			if (v.equals(serie)) {
				idx = i;
				break;
			}
			i++;
		}
		if (i < 0)
			return returnIfNotDefined;
		ArrayList<Color> colors = interpreteColorString(0);
		return colors.get(idx);
	}
	
	public Color getSeriesOutlineColor(String serie, Color returnIfNotDefined) {
		String curVal = (String) AttributeHelper.getAttributeValue(getAttributable(), "", "chart_color_line_names", "", "");
		String[] values = curVal.split(";");
		int idx = -1;
		int i = 0;
		for (String v : values) {
			if (v.equals(serie)) {
				idx = i;
				break;
			}
			i++;
		}
		if (i < 0)
			return returnIfNotDefined;
		ArrayList<Color> colors = interpreteColorString(1);
		return colors.get(idx);
	}
	
	public void setSeriesColor(String serie, Color color) {
		String curVal = (String) AttributeHelper.getAttributeValue(getAttributable(), "", "chart_color_line_names", "", "");
		String[] values = curVal.split(";");
		int idx = -1;
		int i = 0;
		for (String v : values) {
			if (v.equals(serie)) {
				idx = i;
				break;
			}
			i++;
		}
		if (i < 0) {
			curVal = curVal + ";" + serie;
			idx = i;
		}
		setColorString(0, idx, color);
	}
}