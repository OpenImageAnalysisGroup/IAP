/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute;

import java.io.StringReader;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.event.AttributeEvent;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class XMLAttribute extends StringAttribute {
	
	private ExperimentInterface mappingData;
	static final String divChar = "@";
	public static final String nodeTypeDefault = "default";
	// public static String nodeTypeChart_hide_desc = "Hidden";
	// public static String nodeTypeChart_auto_desc = "Automatic";
	// public static String nodeTypeChart_legend_only_desc = "Legend";
	// public static String nodeType5_pie3d_desc = "Pie Chart (skewed)";
	// public static String nodeType4_pie_desc = "Pie Chart";
	// public static String nodeType3_bar_flat_desc = "Bar Chart (flat)";
	// public static String nodeType2_bar_desc = "Bar Chart";
	// public static String nodeType1_line_desc = "Line Chart";
	// public static String nodeTypeChart_hide = "hidden";
	// public static String nodeTypeChart_auto = "auto";
	// public static String nodeTypeChart_legend_only = "legend";
	// public static String nodeTypeChart2D_type6_heatmap = "heatmap";
	// public static String nodeTypeChart2D_type5_pie3d = "chart2d_type5";
	// public static String nodeTypeChart2D_type4_pie = "chart2d_type4";
	// public static String nodeTypeChart2D_type3_bar_flat = "chart2d_type3";
	// public static String nodeTypeChart2D_type2_bar = "chart2d_type2";
	// public static String nodeTypeChart2D_type1_line = "chart2d_type1";
	public static final String nodeTypeFastSimple = "fast";
	public static final String nodeTypeSubstrate = "substrate";
	
	public XMLAttribute() {
		super();
	}
	
	public XMLAttribute(String id) {
		super(id);
		// setDescription("Show XML Mapping Data"); // tooltip
	}
	
	public XMLAttribute(String id, String value) {
		super(id);
		setString(value);
	}
	
	public XMLAttribute(String id, ExperimentInterface mappingDataList) {
		super(id);
		mappingData = mappingDataList;
	}
	
	@Override
	public void setDefaultValue() {
		mappingData = new Experiment();
	}
	
	public ExperimentInterface getMappedData() {
		return mappingData;
	}
	
	@Override
	public void setString(final String value) {
		doSetValue(value);
	}
	
	private void doSetValue(String value) {
		synchronized (this) {
			assert value != null;
			
			AttributeEvent ae = new AttributeEvent(this);
			callPreAttributeChanged(ae);
			if (mappingData == null)
				mappingData = new Experiment();
			else
				mappingData.clear();
			String myString = value;
			while (myString.indexOf(divChar) > 0) {
				String xmlString = myString.substring(0, myString.indexOf(divChar));
				if (xmlString.length() > 0) {
					if (!xmlString.startsWith("<"))
						xmlString = StringManipulationTools.htmlToUnicode(xmlString.replaceAll("~", "&#"));
					myString = myString.substring(myString.indexOf(divChar) + 1);
					SAXBuilder builder = new SAXBuilder();
					try {
						StringReader sr = new StringReader(xmlString);
						Document doc = builder.build(sr);
						SubstanceInterface m = Experiment.getTypeManager().getNewSubstance();
						if (m.setMappedData(doc.getRootElement(), null))
							mappingData.add(m);
					} catch (Exception e) {
						System.out.println("---------------------------");
						System.out.println("Parse Error for: " + xmlString);
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
			callPostAttributeChanged(ae);
		}
	}
	
	@Override
	public String getString() {
		if (mappingData == null)
			return "";
		StringBuffer res = new StringBuffer(1000);
		for (SubstanceInterface m : mappingData) {
			res.append(m.getXMLstring());
			res.append(divChar);
		}
		return res.toString();
	}
	
	@Override
	public Object getValue() {
		return getString();
	}
	
	@Override
	public Object copy() {
		return new XMLAttribute(this.getId(), getMappedData());
	}
	
	public Integer getMappingDataListSize() {
		if (mappingData == null)
			return 0;
		else
			return mappingData.size();
	}
	
	@Override
	public String toString(int n) {
		return getSpaces(n) + getId() + " = \"" + getString() + "\"";
	}
	
	@Override
	public String toXMLString() {
		return getStandardXML(getString());
	}
	
	@Override
	protected void doSetValue(Object o) throws IllegalArgumentException {
		assert o != null;
		
		try {
			setString((String) o);
		} catch (ClassCastException cce) {
			throw new IllegalArgumentException("Invalid value type.");
		}
	}
	
	public void addData(SubstanceInterface m) {
		if (mappingData == null)
			mappingData = new Experiment();
		mappingData.add(m);
	}
	
	// public void setData(ArrayList<org.w3c.dom.Node> mappedDataList) {
	// mappingData.clear();
	// if (mappedDataList!=null)
	// for (org.w3c.dom.Node n : mappedDataList)
	// mappingData.add(new Mapping(n));
	// }
	
}