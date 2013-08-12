/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org;

/*
 * Copyright (c) 2003-207 IPK Gatersleben $Id: AttributeHelper.java,v 1.23
 * 2008/02/12 15:11:17 klukas Exp $
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.color.ColorUtil;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.attributes.ObjectAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.Dash;
import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.DockingAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.EdgeLabelAttribute;
import org.graffiti.graphics.EdgeLabelPositionAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.LineModeAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.plugin.io.resources.IOurl;

/**
 * Attribute Helper Class - Makes it more easy to work with graph/node
 * attributes.
 * 
 * @author Christian Klukas
 * @version $Revision: 1.13 $
 */
public class AttributeHelper implements HelperClass {
	
	private static HashMap<String, String> idToNiceId = new HashMap<String, String>();
	private static HashMap<String, String> idToDeletePath = new HashMap<String, String>();
	private static boolean idInit = false, deletePathInit = false;
	public static String attributeSeparator = String.valueOf(Attribute.SEPARATOR);
	
	public static String id_ttestCircleSize = "ttestCircleSize";
	
	private static String chartAll = "<html><!--a-->Charting <small><font color=\"gray\">(general settings)</font></small>";
	private static String chartAllBars = "<html><!--a-->Charting <small><font color=\"gray\">(bar-charts)</font></small>";
	private static String chartAllLine = "<html><!--a-->Charting <small><font color=\"gray\">(line-charts)</font></small>";
	private static String chartSelN = "<html><!--a-->Charting <small><font color=\"gray\">(selected elements)</font></small>";
	private static String chartDiagram = "<html><!--a-->Charting <small><font color=\"gray\">(Coloring of developmental stages)</font></small>";
	private static String chartHeatMap = "<html><!--a-->Charting <small><font color=\"gray\">(Heatmap Settings)</font></small>";
	
	public static String getEncodedUrl(String input) {
		String url = input;
		url = url.replaceAll("&amp;", "&");
		if (url.contains("?") && !url.startsWith("mailto:")) {
			String p1 = url.substring(0, url.indexOf("?"));
			String p2 = url.substring(url.indexOf("?") + "?".length());
			try {
				p2 = java.net.URLEncoder.encode(p2, "UTF-8");
				p2 = StringManipulationTools.stringReplace(p2, "%2B", "+");
			} catch (UnsupportedEncodingException e) {
				ErrorMsg.addErrorMessage("Internal Error: UTF-8 encoding is not supported by runtime environment.");
			}
			url = p1 + "?" + p2;
			url = StringManipulationTools.stringReplace(url, "%3D", "=");
			url = StringManipulationTools.stringReplace(url, "%26", "&");
			url = StringManipulationTools.stringReplace(url, "%2B", "%2B");
		}
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			// String cmd = "open";
			if (url != null && url.contains("?") && url.startsWith("mailto:")) {
				url = url.substring(0, url.indexOf("?"));
			}
		}
		return url;
	}
	
	public static void showInBrowser(String url) {
		if (url == null)
			return;
		System.out.println("Open URL: " + url);
		if (url.indexOf('@') > 0 && url.indexOf(':') == -1)
			url = "mailto:" + url;
		url = getEncodedUrl(url);
		Runtime r = Runtime.getRuntime();
		// this is not save, in case "&amp;" was included before & was exchanged
		// with &amp; by this application
		// the replacement is needed because of the GML file loader, which can
		// not load a file which contains a &, but
		// which can load a file which contains a &amp; in a String attribute
		try {
			String cmd = "rundll32 url.dll,FileProtocolHandler";
			String par = url;
			r.exec(cmd + " " + par);
		} catch (IOException e) {
			try {
				String cmd = "xdg-open";
				String par = url;
				r.exec(new String[] { cmd, par });
			} catch (Exception e20) {
				try {
					String cmd = "gnome-open"; // xdg-open
					String par = url;
					r.exec(new String[] { cmd, par });
				} catch (Exception e2) {
					try {
						String osName = System.getProperty("os.name");
						if (osName.startsWith("Mac OS") || osName.startsWith("Darwin")) {
							String cmd = "open";
							String par = url;
							r.exec(new String[] { cmd, par });
						}
					} catch (Exception e3) {
						try {
							String cmd = "kfmclient exec";
							String par = url;
							r.exec(new String[] { cmd, par });
						} catch (Exception e4) {
							JOptionPane.showMessageDialog(
									null,
									"<html>Error executing command. Error Messages:<p>" + e.getLocalizedMessage()
											+ " (Windows File Open)<p>" + e2.getLocalizedMessage() + " (gnome File Open)<p>"
											+ e3.getLocalizedMessage() + " (Mac OS X File Open)<p>" + e4.getLocalizedMessage()
											+ " (KDE File Open)</html>", "Error opening file", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		}
	}
	
	public static boolean macOSrunning() {
		try {
			String osName = System.getProperty("os.name");
			if (osName.startsWith("Mac OS") || osName.startsWith("Darwin"))
				return true;
			return System.getProperty("mrj.version") != null;
		} catch (Exception ace) {
			return false;
		}
	}
	
	public static void setMacOSsettings(String applicationName) {
		if (!macOSrunning())
			return;
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		// apple.awt.showGrowBox=false
		// System.setProperty("om.apple.macos.smallTabs", "true");
		
		// System.setProperty("apple.awt.window.position.forceSafeCreation",
		// "true");
		
		System.setProperty("apple.awt.showGrowBox", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", applicationName);
		System.setProperty("apple.awt.rendering", "VALUE_RENDER_SPEED");
		System.setProperty("apple.awt.brushMetalLook", "false");
		System.setProperty("com.apple.mrj.application.live-resize", "true");
	}
	
	public static String getDefaultAttributeDescriptionFor(String id, String tabName, Attribute a) {
		
		if (!idInit)
			initNiceIds();
		// look for an attribute name like test1, test2, hallo1, ...
		// the name ends with a number...
		// in this case the number is removed, the lookup for the known
		// "friendly names"
		// is performed. and " "+number is added, meaning test1 could become
		// "Input Value 1"
		String endNumber = "";
		if (id.length() >= 2) {
			char[] name = id.toCharArray();
			int lastDigit = id.length() - 1;
			while (lastDigit >= 0 && Character.isDigit(name[lastDigit]))
				lastDigit--;
			if (!Character.isDigit(name[lastDigit]))
				lastDigit++;
			if (lastDigit < id.length()) {
				endNumber = id.substring(lastDigit);
				id = id.substring(0, lastDigit);
			}
		}
		
		String result;
		String pathUntilNumber = getSubStringUntilNumber(a.getPath());
		if (idToNiceId.containsKey(pathUntilNumber)) {
			if (!pathUntilNumber.equals(a.getPath()))
				result = idToNiceId.get(pathUntilNumber) + getSubStringFromNumber(a.getPath(), ".") + ": " + id;
			else
				result = idToNiceId.get(pathUntilNumber) + getSubStringFromNumber(a.getPath(), ".");
		} else {
			if (idToNiceId.containsKey(tabName + ":" + id))
				result = idToNiceId.get(tabName + ":" + id);
			else
				result = idToNiceId.get(id);
		}
		if (result == null) {
			// if (id.length() >= 1) {
			// id = id.substring(0, 1).toUpperCase() + id.substring(1);
			// }
			String folder = a.getParent() != null && a.getParent().getName() != null ? a.getParent().getName() : tabName;
			result = folder + ": " + id + endNumber;
		} else
			result = result + " " + endNumber;
		if (true) {
			if (endNumber != null && endNumber.length() > 0 && result.contains(":")) {
				int idx = result.indexOf(":");
				// this changes the sorting in the gui view
				while (endNumber.length() < 5)
					endNumber = "0" + endNumber;
				String endS = result.substring(idx + 1);
				endS = StringManipulationTools.stringReplace(endS, " ", "&nbsp;");
				if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
					result = result.substring(0, idx) + " (list):" + "<html><!-- " + endNumber + " -->"
							+ StringManipulationTools.stringReplace(endS, "<html>", "");
				else {
					if (result.substring(0, idx).length() > 0)
						result = result.substring(0, idx) + ":" + "<html><!-- " + endNumber + " -->"
								+ StringManipulationTools.stringReplace(endS, "<html>", "");
					else
						result = "<html><!-- "
								+ endNumber
								+ " -->"
								+ StringManipulationTools.stringReplace(
										StringManipulationTools.stringReplace(endS, "&nbsp;", " "), "<html>", "").trim();
				}
			}
		}
		return result;
	}
	
	private static String getSubStringUntilNumber(String path) {
		if (path != null && path.indexOf("0") > 0)
			return path.substring(0, path.indexOf("0"));
		if (path != null && path.indexOf("1") > 0)
			return path.substring(0, path.indexOf("1"));
		if (path != null && path.indexOf("2") > 0)
			return path.substring(0, path.indexOf("2"));
		if (path != null && path.indexOf("3") > 0)
			return path.substring(0, path.indexOf("3"));
		if (path != null && path.indexOf("4") > 0)
			return path.substring(0, path.indexOf("4"));
		if (path != null && path.indexOf("5") > 0)
			return path.substring(0, path.indexOf("5"));
		if (path != null && path.indexOf("6") > 0)
			return path.substring(0, path.indexOf("6"));
		if (path != null && path.indexOf("7") > 0)
			return path.substring(0, path.indexOf("7"));
		if (path != null && path.indexOf("8") > 0)
			return path.substring(0, path.indexOf("8"));
		if (path != null && path.indexOf("9") > 0)
			return path.substring(0, path.indexOf("9"));
		return path;
	}
	
	private static String getSubStringFromNumber(String path, String div) {
		int lastNumberPos = -1;
		if (path != null && path.lastIndexOf("0") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("0") ? path.lastIndexOf("0") : lastNumberPos);
		if (path != null && path.lastIndexOf("1") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("1") ? path.lastIndexOf("1") : lastNumberPos);
		if (path != null && path.lastIndexOf("2") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("2") ? path.lastIndexOf("2") : lastNumberPos);
		if (path != null && path.lastIndexOf("3") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("3") ? path.lastIndexOf("3") : lastNumberPos);
		if (path != null && path.lastIndexOf("4") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("4") ? path.lastIndexOf("4") : lastNumberPos);
		if (path != null && path.lastIndexOf("5") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("5") ? path.lastIndexOf("5") : lastNumberPos);
		if (path != null && path.lastIndexOf("6") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("6") ? path.lastIndexOf("6") : lastNumberPos);
		if (path != null && path.lastIndexOf("7") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("7") ? path.lastIndexOf("7") : lastNumberPos);
		if (path != null && path.lastIndexOf("8") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("8") ? path.lastIndexOf("8") : lastNumberPos);
		if (path != null && path.lastIndexOf("9") > 0)
			lastNumberPos = (lastNumberPos < path.lastIndexOf("9") ? path.lastIndexOf("9") : lastNumberPos);
		
		if (lastNumberPos < 0)
			return "";
		
		String res = path.substring(lastNumberPos);
		if (div != null && div.length() > 0 && res.indexOf(div) > 0)
			res = res.substring(0, res.indexOf(div));
		return res;
	}
	
	/**
	 * Memorizes an attribute, which's editor may be clicked, resulting in the
	 * deletion
	 * of an attribute (not necessarily the clicked one):
	 * <p>
	 * attributePath:
	 * <ul>
	 * <li>".mapping.measurementdata" -> this attribute will be clickable</li>
	 * <li>".mapping." -> this attribute and all child attributes will be clickable</li>
	 * </ul>
	 * pathToBeDeleted:
	 * <ul>
	 * <li>"mapping" -> attribute ".mapping" (and all child attributes) will be deleted</li>
	 * <li>"graphics.component" -> attribute ".graphics.component" (and all child attributes) will be deleted, but not any other ".graphics." attributes</li>
	 * <li>"mapping$charting" -> attributes ".mapping", ".charting" and all child attributes will be deleted. "$" may be used more than once</li>
	 * </ul>
	 * 
	 * @param attributePath
	 *           The path of the attribute, which will be clickable
	 * @param pathToBeDeleted
	 *           The path(s) of the attribute(s) to be deleted
	 */
	public static void setDeleteableAttribute(String attributePath, String pathToBeDeleted) {
		if (idToDeletePath.containsKey(attributePath))
			idToDeletePath.remove(attributePath);
		idToDeletePath.put(attributePath, pathToBeDeleted);
	}
	
	public static String getToBeDeletedPathFromAttributePath(String attributePath) {
		if (!deletePathInit)
			initDeleteablePaths();
		for (String dp : idToDeletePath.keySet())
			if (attributePath.startsWith(dp))
				return idToDeletePath.get(dp);
		return null;
	}
	
	private static void initDeleteablePaths() {
		setDeleteableAttribute(".cluster.cluster", "cluster.cluster");
		setDeleteableAttribute(".mapping.", "mapping$charting$graphics.component");
		setDeleteableAttribute(".charting.", "mapping$charting$graphics.component");
		setDeleteableAttribute(".graphics.component", "mapping$charting$graphics.component");
		setDeleteableAttribute(".labelgraphics.", "labelgraphics");
		setDeleteableAttribute(".image.", "image");
		setDeleteableAttribute(".pathway_ref_url", "pathway_ref_url");
		setDeleteableAttribute(".url", "url");
		for (int i = 1; i < 100; i++)
			setDeleteableAttribute(".labelgraphics" + i + ".", "labelgraphics" + i);
		for (int i = 0; i < 100; i++)
			setDeleteableAttribute(".graphics.bends.bend" + i + ".", "graphics.bends.bend" + i);
		deletePathInit = true;
	}
	
	/**
	 * Memorize a attribute description, shown in the node, edge and graph tabs,
	 * for display instead of a attribute name. Use "A:B" as the description, to
	 * create a group "A", which contains a item "B", for the given attributeID.
	 * 
	 * @param attributeID
	 *           The name of the attribute
	 * @param description
	 *           The group and "nice name" of the attribute
	 */
	public static void setNiceId(String attributeID, String description) {
		if (idToNiceId.containsKey(attributeID)) {
			// System.out.println("Information: overwriting previous attribute user description for id "+attributeID+"");
			idToNiceId.remove(attributeID);
		}
		idToNiceId.put(attributeID, description);
	}
	
	public static String getNiceIdFromAttributeId(String attributeId) {
		if (!idInit)
			initNiceIds();
		return idToNiceId.get(attributeId);
	}
	
	private static void initNiceIds() {
		
		String disabled = "<html><font color='gray'>&nbsp;";
		idToNiceId.put(id_ttestCircleSize, chartAll + ": T-Test-Marker Size");
		idToNiceId.put("directed", "Network Attributes: Directed Edges");
		idToNiceId.put("chart_colors", chartAll + ": Condition Colors");
		idToNiceId.put("chart_color_line_names", chartAll + ": Global Condition List");
		idToNiceId.put("chartSizeX", chartSelN + ": Diagram Width/Height");
		idToNiceId.put("chartSizeY", chartSelN + ": Diagram Width/Height");
		idToNiceId.put("empty_border_width", chartSelN + ": Space (hor./vert.)");
		idToNiceId.put("empty_border_width_vert", chartSelN + ": Space (hor./vert.)");
		idToNiceId.put("rangeAxis", chartSelN + ": Range Axis Title");
		idToNiceId.put("chartTitle", chartSelN + ": Diagram Title");
		idToNiceId.put("domainAxis", chartSelN + ": Domain Axis Title");
		idToNiceId.put("rangeAxis", chartSelN + ": Range Axis Title");
		idToNiceId.put("substancename", chartSelN + ": Name of Substance");
		idToNiceId.put("Node:frameThickness", "Shape: Border-Width");
		idToNiceId.put("Edge:frameThickness", "Thickness");
		idToNiceId.put("Edge:relation_subtype", "KEGG - Relations: Sub-Type(s)");
		idToNiceId.put("Edge:relation_type", "KEGG - Relations: Type");
		idToNiceId.put("Edge:relation_src_tgt", "KEGG - Relations: Source/Target");
		idToNiceId.put("Node:linemode", "Shape: Border Drawing");
		idToNiceId.put("Edge:linemode", "Drawing");
		idToNiceId.put("component", chartSelN + ": Diagram");
		idToNiceId.put("width", "<html><!--A-->Size");
		idToNiceId.put("height", "<html><!--A-->Size");
		idToNiceId.put("x", "Position");
		idToNiceId.put("y", "Position");
		idToNiceId.put("z_", "Z");
		idToNiceId.put("depth", "Depth");
		idToNiceId.put("mol", "Molecule Structure:3D MOL View");
		idToNiceId.put("rounding", "Shape: Rounded Corners");
		idToNiceId.put("Edge:shape", "Shape");
		idToNiceId.put("Node:shape", "Shape: Shape");
		idToNiceId.put("Node:fill", "Shape: Fill-Color");
		idToNiceId.put("fill", "Fill-Color");
		
		idToNiceId.put("useCustomRange", chartSelN + ":<html>Range Axis: <br>&nbsp;&nbsp;&nbsp;<small>Custom Min/Max");
		idToNiceId.put("useCustomRangeSteps", chartSelN
				+ ":<html>&nbsp;Range Axis: <br>&nbsp;&nbsp;&nbsp;<small><!--A-->Custom Step Size");
		idToNiceId.put("rangeStepSize", chartSelN
				+ ":<html>&nbsp;Range Axis:  <br>&nbsp;&nbsp;&nbsp;<small><!--A-->Step Size");
		
		idToNiceId.put("max_charts_in_column", chartSelN + ": Number of Charts in a Row");
		
		idToNiceId.put("minRange", chartSelN + ":<html>Range Axis: <br>&nbsp;&nbsp;&nbsp;<small>Minimum");
		idToNiceId.put("maxRange", chartSelN + ":<html>Range Axis: <br>&nbsp;&nbsp;&nbsp;<small>Maximum");
		idToNiceId.put("connectPriorItems", chartAllLine + ": No gaps for missing data");
		idToNiceId.put("Node:outline", "Shape: Frame-Color");
		idToNiceId.put("Edge:outline", "Color");
		idToNiceId.put("show_legend", chartSelN + ": Show Legend");
		idToNiceId.put("background_color", chartSelN
				+ ":<html>&nbsp;Background-Color<br>&nbsp;<small><font color=\"gray\">(black=translucent)");
		// for (int i = 0; i < 100; i++)
		// idToNiceId.put("background_color" + i, "Mapping-Background " +
		// chartSelN
		// +
		// ":<html>&nbsp;Background-Color<br>&nbsp;<small><font color=\"gray\">(black=translucent)");
		
		idToNiceId.put("node_showCategoryAxis", chartAll + ": Show Category Labels");
		idToNiceId.put("node_lineChartShowShapes", chartAllLine + ": Show Shapes");
		
		idToNiceId.put("grid_color", chartAll + ": Grid Color");
		idToNiceId.put("axis_color", chartAll + ": Axis Color");
		idToNiceId.put("node_gridWidth", chartAll + ": Grid Line-Width");
		idToNiceId.put("node_axisWidth", chartAll + ": Axis Line-Width");
		idToNiceId.put("node_plotAxisFontSize", chartAll + ": Axis Font Size");
		
		idToNiceId.put("grouppart", "KEGG: Part of Group");
		idToNiceId.put("node_showGridCategory", chartAll + ": Show Category Grid");
		idToNiceId.put("node_showGridRange", chartAll + ": Show Range Grid");
		idToNiceId.put("node_removeEmptyConditions", chartAll + ": Remove Empty Conditions");
		
		idToNiceId.put("node_categoryBackgroundColorA", chartDiagram + ":<html>&nbsp;Background Color left of A");
		idToNiceId.put("node_categoryBackgroundColorB", chartDiagram
				+ ":<html>&nbsp;Background Color<br>&nbsp;<small><font color=\"gray\">(black=translucent)");
		idToNiceId.put("node_categoryBackgroundColorC", chartDiagram + ":<html>&nbsp;Background Color right of B");
		idToNiceId.put("node_categoryBackgroundColorIndexA", chartDiagram
				+ ":<html>&nbsp;Time A<br>&nbsp;<small><font color=\"gray\">(-1 = disabled, 0...x enabled)");
		idToNiceId.put("node_categoryBackgroundColorIndexC", chartDiagram
				+ ":<html>&nbsp;Time B<br>&nbsp;<small><font color=\"gray\">(-1 = disabled, 0...x enabled)");
		
		idToNiceId.put(GraphicAttributeConstants.HEATMAP_LOWER_COL, chartHeatMap + ": Lower Color");
		idToNiceId.put(GraphicAttributeConstants.HEATMAP_UPPER_COL, chartHeatMap + ": Upper Color");
		idToNiceId.put(GraphicAttributeConstants.HEATMAP_MIDDLE_COL, chartHeatMap + ": Middle Color");
		idToNiceId.put("hm_lower_bound", chartHeatMap + ": Lower Bound");
		idToNiceId.put("hm_middle_bound", chartHeatMap + ": Middle Bound");
		idToNiceId.put("hm_upper_bound", chartHeatMap + ": Upper Bound");
		idToNiceId.put("hm_gamma", chartHeatMap + ": Gamma");
		
		idToNiceId.put("node_useLogScaleForRangeAxis", chartAll + ": Use Log Scale for Range Axis");
		idToNiceId.put("node_usePieScale", chartAll + ": Scale Pie-Chart");
		idToNiceId.put("node_chartShapeSize", chartAllLine + ": Shape-Size");
		idToNiceId.put("node_lineChartShowLines", chartAllLine + ": Show Lines");
		idToNiceId.put("node_lineChartShowStdDev", chartAllLine + ": Show Error as vert. Line");
		idToNiceId.put("node_lineChartShowStdDevRangeLine", chartAllLine + ": Show Error as Fill-Range");
		
		idToNiceId.put("node_lineChartFillTimeGaps", chartAllLine + ": Fill Time-Gaps (linear X-Axis)");
		
		idToNiceId.put("node_chartStdDevLineWidth", chartAllLine + ": Error-Bar Line-Thickness");
		idToNiceId.put("node_chartStdDevTopWidth", chartAll + ": Error-Bar Top-Width");
		idToNiceId.put("node_showRangeAxis", chartAll + ": Show Range Labels");
		idToNiceId.put("node_useStdErr", chartAll + ": Show SE instead of SD");
		idToNiceId.put("node_plotOrientationHor", chartAll + ": Horizontal/Vertical");
		idToNiceId.put("node_outlineBorderWidth", chartAll + ":<html>&nbsp;Bar-Outline/<br>&nbsp;Line Thickness");
		idToNiceId.put("node_halfErrorBar", chartAllBars + ": Hide bottom of Error-Bar");
		idToNiceId.put("scatter_showRangeAxis", "Scatter-Plot: Show Y-axis");
		idToNiceId.put("scatter_showLegend", "Scatter-Plot: Show Legend");
		idToNiceId.put("scatter_showTickMarks", "Scatter-Plot: Show X-axis");
		idToNiceId.put("scatter_outlineBorderWidth", "Scatter-Plot: Datapoint Size");
		idToNiceId.put("nodefont", chartSelN + ": Label-Font");
		idToNiceId.put("charttitlefont", chartSelN + ": Diagram Title Font");
		idToNiceId.put("legend_scale", chartSelN + ": Legend Size (scale)");
		idToNiceId.put("node_plotAxisRotation", chartAll + ": Label Rotation (degree)");
		idToNiceId.put("node_plotAxisSteps", chartAll + ": Category Labels-Skip");
		idToNiceId.put("measurementdata", chartSelN + ": Measurement-Data");
		idToNiceId.put("subgraphNodeCount", "Corresponding subgraph: Number of Nodes");
		idToNiceId.put("subgraphEdgeCount", "Corresponding subgraph: Number of Edges");
		idToNiceId.put("kegg_number", "KEGG: Map Number");
		idToNiceId.put("kegg_type", "KEGG: Type");
		idToNiceId.put("kegg_org", "KEGG: Organism");
		idToNiceId.put("kegg_org_os", "KEGG (Organism-Specific): Organism");
		idToNiceId.put("kegg_title", "KEGG: Map Title");
		idToNiceId.put("kegg_reaction", "KEGG: Reaction ID");
		idToNiceId.put("kegg_name", "KEGG: Kegg ID");
		idToNiceId.put("kegg_name_old", "KEGG: Kegg ID (previous)");
		idToNiceId.put("kegg_link", "KEGG: Reference");
		idToNiceId.put("kegg_link_os", "KEGG (Organism-Specific): Reference");
		idToNiceId.put("present", "KEGG: Presence in spec. Pathway");
		idToNiceId.put("url", "Links: Reference URL");
		idToNiceId.put("pathway_ref_url", "Links: Pathway");
		idToNiceId.put("pathway_link_visualization", "Links: Link Visualization");
		idToNiceId.put("xml_url", "KEGG: XML Source");
		idToNiceId.put("xml_url_os", "KEGG (Organism-Specific): XML Source");
		idToNiceId.put("kegg_link_reaction", "KEGG: Reaction Reference");
		idToNiceId.put("kegg_image", "KEGG: Reference Image");
		idToNiceId.put("kegg_image_os", "KEGG (Organism-Specific): Reference Image");
		idToNiceId.put("kegg_map", "KEGG: ID of the map entry");
		idToNiceId.put("kegg_map_link", "KEGG: Pathway");
		idToNiceId.put("kegg_map_link_os", "KEGG (Organism-Specific): Pathway");
		idToNiceId.put("kegg_reaction_type", "KEGG: Reaction Type");
		idToNiceId.put("kegg_reaction_product", "KEGG: Reaction Product");
		idToNiceId.put("kegg_reaction_substrate", "KEGG: Reaction Substrate");
		idToNiceId.put("relVert", "Label:" + disabled + "Vertical Offset");
		idToNiceId.put("relHor", "Label:" + disabled + "Horizontal Offset");
		
		idToNiceId.put("tooltip", "Tooltip");
		
		idToNiceId.put("absHor", "Label:" + disabled + "Horizontal Offset");
		idToNiceId.put("absVert", "Label:" + disabled + "Vertical Offset");
		idToNiceId.put("relAlign", "Label:" + disabled + "Relative Alignment");
		
		idToNiceId.put("oldlabel", "Label: Old Label");
		
		idToNiceId.put(".mapping.chartposition.absVert", chartSelN + " (Chart-Position on Edge): Position (X-offset)");
		idToNiceId.put(".mapping.chartposition.absHor", chartSelN + " (Chart-Position on Edge): Position (Y-offset)");
		idToNiceId.put(".mapping.chartposition.relAlign", chartSelN + " (Chart-Position on Edge): Relative Position");
		idToNiceId.put(".mapping.chartposition.alignSegment", chartSelN + " (Chart-Position on Edge): Alignment Segment");
		
		idToNiceId.put(".graphics.offX", "Shape: Multi-Offset (X)");
		idToNiceId.put(".graphics.offY", "Shape: Multi-Offset (Y)");
		
		idToNiceId.put(".graphics.bends.bend", "Edge-Bend ");
		idToNiceId.put(".graphics.bends.bend.x", "Edge-Bend 0: x");
		idToNiceId.put(".graphics.bends.bend.y", "Edge-Bend 0: y");
		
		idToNiceId.put("localAlign", "Label:" + disabled + "Local Alignment");
		
		idToNiceId.put(".srcLabel.fontName", "Label (Consumption): Font");
		idToNiceId.put(".srcLabel.fontSize", "Label (Consumption): Font-Size");
		idToNiceId.put(".srcLabel.fontStyle", "Label (Consumption): Font-Style");
		idToNiceId.put(".srcLabel.shadowOffset", "Label (Consumption): Shadow-Offset");
		idToNiceId.put(".srcLabel.shadowColor", "Label (Consumption): Shadow-Color");
		idToNiceId.put(".srcLabel.text", "Label (Consumption): Text");
		idToNiceId.put(".srcLabel.color", "Label (Consumption): Color");
		idToNiceId.put(".srcLabel.anchor", "Label (Consumption): Position");
		
		idToNiceId.put(".tgtLabel.fontName", "Label (Production): Font");
		idToNiceId.put(".tgtLabel.fontSize", "Label (Production): Font-Size");
		idToNiceId.put(".tgtLabel.fontStyle", "Label (Production): Font-Style");
		idToNiceId.put(".tgtLabel.shadowOffset", "Label (Production): Shadow-Offset");
		idToNiceId.put(".tgtLabel.shadowColor", "Label (Production): Shadow-Color");
		idToNiceId.put(".tgtLabel.text", "Label (Production): Text");
		idToNiceId.put(".tgtLabel.color", "Label (Production): Color");
		idToNiceId.put(".tgtLabel.anchor", "Label (Production): Position");
		
		idToNiceId.put(".labelgraphics.fontName", "Label: Font");
		idToNiceId.put(".labelgraphics.fontSize", "Label: Font-Size");
		idToNiceId.put(".labelgraphics.fontStyle", "Label: Font-Style");
		idToNiceId.put(".labelgraphics.shadowOffset", "Label: Shadow-Offset");
		idToNiceId.put(".labelgraphics.shadowColor", "Label: Shadow-Color");
		idToNiceId.put(".labelgraphics.text", "Label: Text");
		idToNiceId.put(".labelgraphics.color", "Label: Color");
		idToNiceId.put(".labelgraphics.anchor", "Label: Position");
		
		idToNiceId.put("source", "Docking: Source");
		idToNiceId.put("target", "Docking: Target");
		
		idToNiceId.put("fontName", "Label (Annotation): Font");
		idToNiceId.put("fontSize", "Label (Annotation): Font-Size");
		idToNiceId.put("fontStyle", "Label (Annotation): Font-Style");
		idToNiceId.put("shadowOffset", "Label (Annotation): Shadow-Offset");
		idToNiceId.put("shadowColor", "Label (Annotation): Shadow-Color");
		idToNiceId.put("text", "Label (Annotation): Text");
		idToNiceId.put("color", "Label (Annotation): Color");
		idToNiceId.put("anchor", "Label (Annotation): Position");
		
		// idToNiceId.put("image_url", "Image:<html>&nbsp;URL<br><br>&nbsp;View");
		idToNiceId.put("image_url", "Image:<html>&nbsp;View<br><br>&nbsp;URL");
		idToNiceId.put("image_position", "Image: Position");
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			idToNiceId.put("cluster_colors", "Pathway-Coloring");
			idToNiceId.put("cluster", "Src-Pathway");
			idToNiceId.put("thickness", "Arrow-Size");
			idToNiceId.put("Edge:fill", "Arrow-Color");
		} else {
			String cc = "Cluster-Coloring: ";
			idToNiceId.put("cluster_colors", cc + "Colors");
			idToNiceId.put("clusterbackground_fill_outer_region", cc + "Fill View Completely");
			idToNiceId.put("clusterbackground_space_fill", cc + "Fill Inner Regions");
			idToNiceId.put("clusterbackground_radius", cc + "Radius");
			idToNiceId.put("clusterbackground_low_alpha", cc + "Low Alpha (0...1)");
			idToNiceId.put("clusterbackground_grid", cc + "Grid (>~20)");
			
			idToNiceId.put("arrowtail", "Arrow Tail: Shape");
			idToNiceId.put("arrowhead", "Arrow Head");
			idToNiceId.put("thickness", "Arrow Size");
			idToNiceId.put("Edge:fill", "Arrow Tail: Color");
		}
		
		idToNiceId.put("alignSegment", "Label:" + disabled + "Alignment Segement");
		idToNiceId.put("modelID", "SBML: SBML Model ID");
		idToNiceId.put("sbmlID", "SBML: SBML Element ID");
		idToNiceId.put("reversible", "SBML: Reversible");
		idToNiceId.put("sbmlRole", "SBML: Role");
		idToNiceId.put("formula", "KEGG: Formula");
		idToNiceId.put("mass", "KEGG: Mass");
		idToNiceId.put("correlation_r", "highest correlation (r)");
		idToNiceId.put("correlation_prob", "highest correlation (1-p)");
		
		String nodestat = "<html><!--z-->Node Statistics&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		idToNiceId.put("degree", nodestat + "Degree");
		idToNiceId.put("degree_in", nodestat + "In-Degree");
		idToNiceId.put("degree_out", nodestat + "Out-Degree");
		
		idToNiceId.put("clustering_coeff_undir", nodestat + "Clustering coeff. (undir.)");
		idToNiceId.put("clustering_coeff_dir", nodestat + "Clustering coeff. (dir.)");
		
		String mapstat = "<html><!--z-->Mapping Statistics&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		idToNiceId.put("datamapping_cnt", mapstat + "Number of mappings");
		idToNiceId.put("lines_cnt", mapstat + "Number of lines");
		String samplestat = "<html><!--z-->Sample Statistics&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		idToNiceId.put("sample_cnt", samplestat + "Number of samples");
		idToNiceId.put("significant_different_cnt", samplestat + "Sign. mean-diff. (vs. control)");
		idToNiceId.put("significant_not_different_cnt", samplestat + "Not sign. mean-diff. (vs. control)");
		idToNiceId.put("sample_values_min", samplestat + "Sample average minimum");
		idToNiceId.put("sample_values_avg", samplestat + "Sample average average");
		idToNiceId.put("sample_values_sum", samplestat + "Sample average sum");
		idToNiceId.put("sample_values_max", samplestat + "Sample average maximum");
		idToNiceId.put("sample_stddev_avg", samplestat + "Sample standard deviation average");
		idToNiceId.put("sample_replicate_cnt_min", samplestat + "Replicate count minimum");
		idToNiceId.put("sample_replicate_cnt_max", samplestat + "Replicate count maximum");
		String replicatestat = "<html><!--z-->Values Statistics&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		idToNiceId.put("sample_replicate_values_min", replicatestat + "Minimum of any mapped value");
		idToNiceId.put("sample_replicate_values_max", replicatestat + "Maximum of any mapped value");
		String timestat = "<html><!--z2-->Time-Series Regression&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		idToNiceId.put("samples_different_timepoints_cnt", timestat + "Time points");
		idToNiceId.put("series_alpha_", timestat + "<html>&#945;, series");
		idToNiceId.put("series_alpha_min", timestat + "<html>&#945; (minimum), series");
		idToNiceId.put("series_alpha_avg", timestat + "<html>&#945; (average), series");
		idToNiceId.put("series_alpha_max", timestat + "<html>&#945; (maximum), series");
		idToNiceId.put("series_beta_", timestat + "<html>&#946;, series");
		idToNiceId.put("series_beta_min", timestat + "<html>&#946; (minimum), series");
		idToNiceId.put("series_beta_avg", timestat + "<html>&#946; (average), series");
		idToNiceId.put("series_beta_max", timestat + "<html>&#946; (maximum), series");
		String ratiocalc = "<html><!--z8-->Sample-Ratio Calculation (A/B)&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		idToNiceId.put("sample_ratio_", ratiocalc + "<html>ratio, time p. ");
		idToNiceId.put("sample_ratio_min", ratiocalc + "<html>ratio (minimum)");
		idToNiceId.put("sample_ratio_avg", ratiocalc + "<html>ratio (average)");
		idToNiceId.put("sample_ratio_max", ratiocalc + "<html>ratio (maximum)");
		
		String evaluationSettings = "<html><!--z7-->Evaluation of line data (A, B)&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		
		idToNiceId.put("sample_ratio_name_a", evaluationSettings + "<html>Name of line A");
		idToNiceId.put("sample_ratio_name_b", evaluationSettings + "<html>Name of line B");
		
		String lineCorr = "<html><!--z9-->Correlation Calculation (A,B)&nbsp;<small><font color=\"gray\">(not auto-updated)</font>:";
		
		idToNiceId.put("corr_rank_repl_values_r", lineCorr + "Replicate Values Correlation (rs)");
		idToNiceId.put("corr_rank_sample_avg_over_time_r", lineCorr + "Sample Avg.-Values Correlation (rs)");
		idToNiceId.put("corr_repl_values_r", lineCorr + "Replicate Values Correlation (r)");
		idToNiceId.put("corr_sample_avg_over_time_r", lineCorr + "Sample Avg.-Values Correlation (r)");
		
		idToNiceId.put("corr_rank_repl_values_prob", lineCorr + "Replicate Values Correlation (rs: p-value)");
		idToNiceId.put("corr_rank_sample_avg_over_time_prob", lineCorr + "Sample Avg.-Values Correlation (rs: p-value)");
		idToNiceId.put("corr_repl_values_prob", lineCorr + "Replicate Values Correlation (r: p-value)");
		idToNiceId.put("corr_sample_avg_over_time_prob", lineCorr + "Sample Avg.-Values Correlation (r: p-value)");
		
		idInit = true;
	}
	
	/**
	 * Get the Labelpath
	 * 
	 * @return Label Path
	 */
	public static String getLabelPath() {
		return GraphicAttributeConstants.LABELGRAPHICS;
	}
	
	/**
	 * Sets the Label of a Edge
	 * 
	 * @param edge
	 *           The Node to work with
	 * @param label
	 *           The new label for the node (created if not available)
	 */
	public static void setLabel(Edge edge, String label) {
		if (label == null)
			return;
		setLabel(edge, label, null);
	}
	
	/**
	 * Sets the Label of a Node. If the given label is null, the label attribute
	 * is removed from the node.
	 * 
	 * @param node
	 *           The Node to work with
	 * @param label
	 *           The new label for the node (created if not available)
	 */
	public static void setLabel(Node node, String label) {
		setLabel(node, label, null, null);
	}
	
	/**
	 * Sets the label of an edge
	 * 
	 * @param edge
	 *           - the edge to use
	 * @param label
	 *           - the new label for the edge (created if not available)
	 * @param fontName
	 *           - the name of the font to use with this label
	 */
	public static void setLabel(Edge edge, String label, String fontName) {
		if (label == null)
			return;
		try {
			if (label.contains("\""))
				label = label.replace("\"", "");
			EdgeLabelAttribute labelAttr;
			
			if (hasAttribute(edge, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (EdgeLabelAttribute) edge.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
			} else {
				// no label - associate one
				labelAttr = new EdgeLabelAttribute(GraphicAttributeConstants.LABELGRAPHICS, label);
				edge.addAttribute(labelAttr, GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
			}
			
			if (fontName != null)
				setFont(labelAttr, fontName);
			
			labelAttr.setLabel(label);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Set-Label-Failure: " + ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Sets the label of a node. If the given label is null, the label attribute
	 * is removed from the node.
	 * 
	 * @param node
	 *           - the node to use
	 * @param label
	 *           - the new label for the node (created if not available)
	 * @param fontName
	 *           - the name of the font to use with this label
	 */
	public static void setLabel(GraphElement node, String label, String fontName, String alignment) {
		if (label == null) {
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				NodeLabelAttribute labelAttr;
				labelAttr = (NodeLabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
				labelAttr.getParent().remove(labelAttr);
			}
			return;
		}
		try {
			if (label.contains("\""))
				label = label.replace("\"", "");
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
			} else {
				// no label - associate one
				labelAttr = new NodeLabelAttribute(GraphicAttributeConstants.LABELGRAPHICS, label);
				node.addAttribute(labelAttr, GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
			}
			
			if (fontName != null)
				setFont(labelAttr, fontName);
			
			labelAttr.setLabel(label);
			if (alignment != null)
				labelAttr.setAlignment(alignment);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
		}
	}
	
	public static void setLabel(int idx, GraphElement node, String label, String fontName, String alignment) {
		String index = "" + idx;
		if (idx < 0)
			index = "";
		if (label == null) {
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS + index)) {
				NodeLabelAttribute labelAttr;
				labelAttr = (NodeLabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS + index);
				labelAttr.getParent().remove(labelAttr);
			}
			return;
		}
		try {
			if (label.contains("\""))
				label = label.replace("\"", "");
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS + index)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS + index);
			} else {
				// no label - associate one
				labelAttr = new NodeLabelAttribute(GraphicAttributeConstants.LABELGRAPHICS + index, label);
				node.addAttribute(labelAttr, GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
			}
			
			if (fontName != null)
				setFont(labelAttr, fontName);
			
			labelAttr.setLabel(label);
			if (alignment != null)
				labelAttr.setAlignment(alignment);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
		}
	}
	
	public static void setLabelAlignment(int index, Node node, AlignmentSetting align) {
		try {
			String idx = "" + index;
			if (index < 0)
				idx = "";
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS + idx)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS + idx);
			} else {
				// no label - associate one
				labelAttr = new NodeLabelAttribute(GraphicAttributeConstants.LABELGRAPHICS + idx, "");
				node.addAttribute(labelAttr, GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
			}
			labelAttr.setAlignment(align.toGMLstring());
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
		}
	}
	
	public static AlignmentSetting getLabelAlignment(int index, Node node) {
		try {
			String idx = "" + index;
			if (index < 0)
				idx = "";
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS + idx)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS + idx);
				String align = labelAttr.getAlignment();
				for (AlignmentSetting a : AlignmentSetting.values()) {
					if (a.toGMLstring().equalsIgnoreCase(align))
						return a;
				}
			}
		} catch (Exception ex) {
			// empty
		}
		if (index <= 0)
			return AlignmentSetting.CENTERED;
		else
			return AlignmentSetting.HIDDEN;
	}
	
	public static boolean isLabelAlignmentKnownConstant(int index, Node node) {
		try {
			String idx = "" + index;
			if (index < 0)
				idx = "";
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS + idx)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS + idx);
				String align = labelAttr.getAlignment();
				for (AlignmentSetting a : AlignmentSetting.values()) {
					if (a.toGMLstring().equalsIgnoreCase(align))
						return true;
				}
			}
		} catch (Exception ex) {
			// empty
		}
		return false;
	}
	
	/**
	 * Sets the font to use when the label is drawed.
	 * 
	 * @param label
	 *           - the label
	 * @param fontName
	 *           - the name of the font
	 */
	public static void setFont(LabelAttribute label, String fontName) {
		if (label != null && fontName != null)
			label.setFontName(fontName);
	}
	
	/**
	 * Gets the Label of a Node
	 * 
	 * @param node
	 *           The node to work with
	 * @param defaultReturn
	 *           The standard return value if no label is set
	 * @return The actual label or the <code>labelDefaul</code> value, if the
	 *         label-attribute is not set.
	 */
	public static String getLabel(Attributable node, String defaultReturn) {
		try {
			LabelAttribute labelAttr;
			
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
				if (labelAttr.getLabel() == null)
					return defaultReturn;
				else
					return labelAttr.getLabel();
			} else {
				return defaultReturn;
			}
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
			return defaultReturn;
		}
	}
	
	public static ArrayList<String> getLabels(GraphElement graphElement, boolean includeMainLabel) {
		ArrayList<String> result = new ArrayList<String>();
		
		if (includeMainLabel) {
			String nodeName = getLabel(graphElement, null);
			if (nodeName != null && nodeName.length() > 0)
				;
			result.add(nodeName);
		}
		for (int idx = 1; idx <= graphElement.getAttributes().size(); idx++) {
			String lbl = getLabel(idx, graphElement, null);
			if (lbl != null && lbl.length() > 0)
				;
			result.add(lbl);
		}
		
		return result;
	}
	
	public static ArrayList<String> getLabels(GraphElement graphElement) {
		return getLabels(graphElement, true);
	}
	
	public static String getLabel(int index, Attributable node, String defaultReturn) {
		try {
			LabelAttribute labelAttr;
			
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS + index)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS + index);
				if (labelAttr.getLabel() == null)
					return defaultReturn;
				else
					return labelAttr.getLabel();
			} else {
				return defaultReturn;
			}
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
			return defaultReturn;
		}
	}
	
	/**
	 * Check the existence of a named Attribute
	 * 
	 * @param n
	 *           The node to work with
	 * @param attributeName
	 *           The name of the attribute to be checked
	 * @return True, if the attribute is available for the node <code>n</code>.
	 *         False, if the attribute is not available.
	 */
	public static boolean hasAttribute(Attributable n, String attributeName) {
		try {
			Attribute attr = n.getAttribute(attributeName);
			return attr != null;
		} catch (AttributeNotFoundException err) {
			return false;
		} catch (Exception err2) {
			if (n == null)
				System.err.println("hasAttribute Function called with Null argument for attributeable!");
			if (attributeName == null)
				System.err.println("hasAttribute Function called with Null argument for attributeName!");
			if (n != null && attributeName != null)
				System.err.println("hasAttribute Function received exception while retreiving attribute! ("
						+ err2.getMessage() + ")");
			return false;
		}
	}
	
	public static boolean hasAttribute(Attributable n, String path, String attributeName) {
		return hasAttribute(n, path + Attribute.SEPARATOR + attributeName);
	}
	
	/**
	 * Sets the position of a node
	 * 
	 * @param n
	 *           The node to work with
	 * @param p
	 *           The new position as a <code>Point2D</code>
	 */
	public static void setPosition(Node n, Point2D p) {
		try {
			CoordinateAttribute cn = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			
			cn.setCoordinate(p);
		} catch (AttributeNotFoundException e) {
			addDefaultGraphicsAttributeToNode(n, p.getX(), p.getY());
			/*
			 * CollectionAttribute ca = new HashMapAttribute(
			 * GraphicAttributeConstants.GRAPHICS); n.addAttribute(ca, "");
			 * n.addAttribute(new CoordinateAttribute(
			 * GraphicAttributeConstants.COORDINATE, p),
			 * GraphicAttributeConstants.GRAPHICS);
			 */
			// ErrorMsg.addErrorMessage("Set Position-Failure:
			// "+e.getLocalizedMessage());
		}
	}
	
	/**
	 * Sets the position of a node
	 * 
	 * @param n
	 *           The node to work with
	 * @param p
	 *           The new position as a <code>Point2D</code>
	 */
	public static void setPosition(Node n, double x, double y) {
		try {
			CoordinateAttribute cn = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			
			Point2D p = new Point2D.Double(x, y);
			cn.setCoordinate(p);
		} catch (AttributeNotFoundException e) {
			addDefaultGraphicsAttributeToNode(n, x, y);
		}
	}
	
	public static void setPosition3d(Node n, double x, double y, double z) {
		try {
			CoordinateAttribute cn = (CoordinateAttribute) n.getAttribute(GraphicAttributeConstants.COORD_PATH);
			
			Point2D p = new Point2D.Double(x, y);
			cn.setCoordinate(p);
			setPositionZ(n, z);
		} catch (AttributeNotFoundException e) {
			addDefaultGraphicsAttributeToNode(n, x, y);
			setPositionZ(n, z);
		}
	}
	
	private static void addDefaultGraphicsAttributeToNode(Node n, double x, double y) {
		NodeGraphicAttribute nga = getNodeGraphicsAttribute(x, y);
		n.addAttribute(nga, "");
	}
	
	/**
	 * For getting the x position of a node through attribute access,
	 * 
	 * @param a
	 *           Node to be analyzed.
	 * @return X position of node.
	 */
	public static double getPositionX(Node a) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
		
		return coA.getX();
	}
	
	/**
	 * Returns the position of a node
	 * 
	 * @param a
	 *           The node to be analyzed.
	 * @return The position of the Node (a<code>Point2D</code> structure)
	 */
	public static Point2D getPosition(Node a) {
		try {
			CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			return coA.getCoordinate();
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
		}
		return null;
	}
	
	/**
	 * Returns the position of a node
	 * 
	 * @param a
	 *           The node to be analyzed.
	 * @return The position of the Node (a<code>Vector2d</code> structure)
	 */
	public static Vector2d getPositionVec2d(Node a) {
		try {
			CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Point2D r = coA.getCoordinate();
			return new Vector2d(r.getX(), r.getY());
		} catch (Exception ex) {
		}
		return new Vector2d(Double.NaN, Double.NaN);
	}
	
	/**
	 * Returns the position of a node
	 * 
	 * @param a
	 *           The node to be analyzed.
	 * @return The position of the Node (a<code>Vector2df</code> structure)
	 */
	public static Vector2df getPositionVec2df(Node a) {
		try {
			CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Point2D r = coA.getCoordinate();
			return new Vector2df(r);
		} catch (Exception ex) {
		}
		return new Vector2df(Float.NaN, Float.NaN);
	}
	
	/**
	 * For geting the y position of a node through attribute access,
	 * 
	 * @param a
	 *           Node to be analysed.
	 * @return Y position of node.
	 */
	public static double getPositionY(Node a) {
		CoordinateAttribute coA = (CoordinateAttribute) a.getAttribute(GraphicAttributeConstants.COORD_PATH);
		
		return coA.getY();
	}
	
	/**
	 * Gets the size of the Node.
	 * 
	 * @param myNode
	 * @return Tehe size as a <code>Vector2d</code> structure.
	 */
	public static Vector2d getSize(Node myNode) {
		try {
			double width;
			double height;
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			width = ((Double) da.getValue()).doubleValue();
			da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			height = ((Double) da.getValue()).doubleValue();
			return new Vector2d(width, height);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Get Size-Failure: " + ex.getLocalizedMessage());
		}
		return null;
	}
	
	/**
	 * Sets the size of the Node.
	 * 
	 * @param myNode
	 * @param width
	 *           New width
	 * @param height
	 *           New height
	 */
	public static void setSize(Node myNode, double width, double height) {
		try {
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			da.setDouble(width);
			
			da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			da.setDouble(height);
		} catch (Exception ex) {
		}
	}
	
	public static void setHeight(Node myNode, double height) {
		try {
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			da.setDouble(height);
		} catch (Exception ex) {
		}
	}
	
	public static void setWidth(Node myNode, double width) {
		try {
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			da.setDouble(width);
		} catch (Exception ex) {
		}
	}
	
	/**
	 * Sets the size of the Node.
	 * 
	 * @param myNode
	 * @param width
	 *           New width
	 * @param height
	 *           New height
	 */
	public static void setSize(Node myNode, int width, int height) {
		try {
			setSize(myNode, (double) width, (double) height);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Set-Size-Failure: " + ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Sets the size of the Node.
	 * 
	 * @param myNode
	 * @param newSize
	 *           The new size as a <code>Vector2d</code> struct. X = width, Y =
	 *           height.
	 */
	public static void setSize(Node myNode, Vector2d newSize) {
		try {
			DimensionAttribute da = (DimensionAttribute) myNode.getAttribute(GraphicAttributeConstants.DIM_PATH);
			
			da.setDimension(newSize.x, newSize.y);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Set-Size-Failure: " + ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Sets the fill color of a given <code>Atttibutable</code>.
	 * 
	 * @param attributable
	 *           - the Attributable (Node, Edge, ...) to set the color
	 * @param color
	 *           - the color to set
	 * @see #getFillColor(Attributable)
	 * @see #setOutlineColor(Attributable, Color)
	 * @see #getOutlineColor(Attributable)
	 */
	public static void setFillColor(Attributable attributable, Color color) {
		try {
			ColorAttribute colorAtt = null;
			
			colorAtt = (ColorAttribute) attributable.getAttribute(GraphicAttributeConstants.FILLCOLOR_PATH);
			colorAtt.setColor(color);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Set-Fill-Color-Failure: " + ex.getLocalizedMessage());
		}
	}
	
	/**
	 * Returns the fill color of a given <code>Atttibutable</code>.
	 * 
	 * @param attributable
	 *           - the Attributable (Node, Edge, ...) to get the color from
	 * @see #setFillColor(Attributable, Color)
	 * @see #setOutlineColor(Attributable, Color)
	 * @see #getOutlineColor(Attributable)
	 */
	public static Color getFillColor(Attributable attributable) {
		try {
			ColorAttribute colorAtt = null;
			colorAtt = (ColorAttribute) attributable.getAttribute(GraphicAttributeConstants.FILLCOLOR_PATH);
			return colorAtt.getColor();
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Set-Fill-Color-Failure: " + ex.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * Sets the outline color of a given <code>Atttibutable</code>.
	 * 
	 * @param attributable
	 *           - the Attributable (Node, Edge, ...) to set the color
	 * @param color
	 *           - the color to set
	 * @see #getOutlineColor(Attributable)
	 * @see #setFillColor(Attributable, Color)
	 * @see #getFillColor(Attributable)
	 */
	public static void setOutlineColor(Attributable attributable, Color color) {
		try {
			ColorAttribute colorAtt = null;
			
			if (hasAttribute(attributable, GraphicAttributeConstants.FRAMECOLOR)) {
				colorAtt = (ColorAttribute) attributable.getAttribute(GraphicAttributeConstants.FRAMECOLOR);
			} else {
				colorAtt = (ColorAttribute) attributable.getAttribute(GraphicAttributeConstants.OUTLINE_PATH);
			}
			
			colorAtt.setColor(color);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
		}
	}
	
	/**
	 * Returns the outline color of a given <code>Atttibutable</code>.
	 * 
	 * @param attributable
	 *           - the Attributable (Node, Edge, ...) to get the color from
	 * @see #setOutlineColor(Attributable, Color)
	 * @see #setFillColor(Attributable, Color)
	 * @see #getFillColor(Attributable)
	 */
	public static Color getOutlineColor(Attributable attributable) {
		try {
			ColorAttribute colorAtt = null;
			
			if (hasAttribute(attributable, GraphicAttributeConstants.FRAMECOLOR)) {
				colorAtt = (ColorAttribute) attributable.getAttribute(GraphicAttributeConstants.FRAMECOLOR);
			} else {
				colorAtt = (ColorAttribute) attributable.getAttribute(GraphicAttributeConstants.OUTLINE_PATH);
				
			}
			return colorAtt.getColor();
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Set-OutlineColor-Failure: " + ex.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * SetAttribute, sets or adds a attribute to the given <code>Attributeable</code>. If the <code>attributeValue</code> is from a
	 * known type (Boolean, Byte, Double, Float, Integer, Long, Short or String)
	 * the attribute values are set with "standard attributes". If it has a
	 * unknown type a <code>ObjectAttribute</code> is added or set. These
	 * attributes can not be saved or loaded, so this should be avoided if
	 * possible.
	 * 
	 * @author Christian Klukas
	 */
	public static void setAttribute(Attributable attributable, String path, String attributeName, Object attributeValue) {
		if (!hasAttribute(attributable, path)) {
			addAttributeFolder(attributable, path);
		}
		HashMapAttribute a = (HashMapAttribute) getAttribute(attributable, path);
		if (attributeValue instanceof Attribute) {
			try {
				Attribute b = a.getAttribute(path + attributeSeparator + attributeName);
				b.setValue(((Attribute) attributeValue).getValue());
			} catch (AttributeNotFoundException e) {
				a.add((Attribute) attributeValue);
			}
			return;
		}
		if (attributeValue instanceof Boolean) {
			try {
				attributable.setBoolean(path + attributeSeparator + attributeName,
						((Boolean) attributeValue).booleanValue());
				return;
			} catch (Exception e) {
				
			}
		}
		if (attributeValue instanceof Color) {
			try {
				ColorAttribute colorAtt = null;
				colorAtt = (ColorAttribute) attributable.getAttribute(path + attributeSeparator + attributeName);
				colorAtt.setColor((Color) attributeValue);
				return;
			} catch (Exception e) {
				
			}
		}
		if (attributeValue instanceof Byte) {
			attributable.setByte(path + attributeSeparator + attributeName, ((Byte) attributeValue).byteValue());
			return;
		}
		if (attributeValue instanceof Double) {
			try {
				if (((Double) attributeValue).doubleValue() == Double.NaN)
					return;
				attributable.setDouble(path + attributeSeparator + attributeName, ((Double) attributeValue).doubleValue());
			} catch (IllegalArgumentException e) {
				String s = "" + ((Double) attributeValue).doubleValue();
				if (s.endsWith(".0"))
					s = s.substring(0, s.length() - 2);
				attributable.setString(path + attributeSeparator + attributeName, s);
			}
			return;
		}
		if (attributeValue instanceof Float) {
			if (((Float) attributeValue).floatValue() == Float.NaN)
				return;
			attributable.setFloat(path + attributeSeparator + attributeName, ((Float) attributeValue).floatValue());
			return;
		}
		if (attributeValue instanceof Integer) {
			if (((Integer) attributeValue).intValue() == Integer.MAX_VALUE)
				return;
			attributable.setInteger(path + attributeSeparator + attributeName, ((Integer) attributeValue).intValue());
			return;
		}
		if (attributeValue instanceof Long) {
			if (((Long) attributeValue).intValue() == Long.MAX_VALUE)
				return;
			attributable.setLong(path + attributeSeparator + attributeName, ((Long) attributeValue).longValue());
			return;
		}
		if (attributeValue instanceof Short) {
			if (((Short) attributeValue).shortValue() == Short.MAX_VALUE)
				return;
			attributable.setShort(path + attributeSeparator + attributeName, ((Short) attributeValue).shortValue());
			return;
		}
		if (attributeValue instanceof String) {
			attributable.setString(path + attributeSeparator + attributeName, (String) attributeValue);
			return;
		}
		// If unknown type, then add a ObjectAttribute
		org.graffiti.attributes.ObjectAttribute myNewAttribute;
		try {
			myNewAttribute = (org.graffiti.attributes.ObjectAttribute) a.getAttribute(attributeName);
		} catch (AttributeNotFoundException e) {
			myNewAttribute = new org.graffiti.attributes.ObjectAttribute(attributeName);
			a.add(myNewAttribute, true);
		} catch (ClassCastException cce) {
			a.remove(attributeName);
			myNewAttribute = new org.graffiti.attributes.ObjectAttribute(attributeName);
			a.add(myNewAttribute, true);
		}
		myNewAttribute.setValue(attributeValue);
	}
	
	/**
	 * setAttributeTextField
	 * 
	 * @author Christian Klukas
	 */
	public static void setAttributeTextField(Attributable attributable, String path, String attributeName,
			Object attributeValue) {
		if (!hasAttribute(attributable, path)) {
			addAttributeFolder(attributable, path);
		}
		HashMapAttribute a = (HashMapAttribute) getAttribute(attributable, path);
		
		Attribute myNewAttribute;
		
		try {
			myNewAttribute = a.getAttribute(attributeName);
		} catch (Exception e) {
			myNewAttribute = StringAttribute.getTypedStringAttribute(attributeName);
		}
		myNewAttribute.setValue(attributeValue);
		try {
			a.add(myNewAttribute, false);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static Set<Object> getAttributeValueSet(Collection<Attributable> attributables, String path,
			String attributeName, Object defaultValue, Object resultType, boolean setDefaultIfMissing) {
		HashSet<Object> values = new HashSet<Object>();
		for (Attributable a : attributables) {
			Object o = getAttributeValue(a, path, attributeName, defaultValue, resultType, setDefaultIfMissing);
			values.add(o);
		}
		return values;
	}
	
	public static Object getAttributeValue(Attributable attributable, String path, String attributeName,
			Object defaultValue, Object resultType) {
		return getAttributeValue(attributable, path, attributeName, defaultValue, resultType, true);
	}
	
	/**
	 * Return a Attribute value, if not present it returns the defaultValue.
	 */
	public static Object getAttributeValue(Attributable attributable, String path, String attributeName,
			Object defaultValue, Object resultType, boolean setDefaultIfMissing) {
		
		try {
			HashMapAttribute a = (HashMapAttribute) getAttribute(attributable, path);
			Object res = a.getAttribute(attributeName).getValue();
			if (resultType != null && !res.getClass().equals(resultType.getClass())) {
				if (res instanceof String && !(resultType instanceof Boolean)) {
					res = ObjectAttributeService.createAndInitObjectFromString((String) res);
					if (res == null || !res.getClass().equals(resultType.getClass())) {
						if ((res != null) && (res instanceof String) && resultType instanceof StringAttribute) {
							a.remove(attributeName);
							Object inst = resultType.getClass().newInstance();
							((StringAttribute) inst).setString((String) res);
							a.add((Attribute) inst, true);
							return inst;
						}
						return defaultValue;
					} else {
						a.remove(attributeName);
						ObjectAttribute myNewAttribute = new ObjectAttribute(attributeName);
						myNewAttribute.setValue(res);
						a.add(myNewAttribute, true);
					}
				} else {
					if (res instanceof Float && resultType instanceof Double) {
						float r = ((Float) res).floatValue();
						a.remove(attributeName);
						Double rr = new Double(r);
						setAttribute(attributable, path, attributeName, rr);
						return rr;
					}
					if (res instanceof Integer && resultType instanceof Boolean) {
						int r = ((Integer) res).intValue();
						a.remove(attributeName);
						Boolean rr = new Boolean(r != 0);
						setAttribute(attributable, path, attributeName, rr);
						return rr;
					}
					if (res instanceof Integer && resultType instanceof Double) {
						int r = ((Integer) res).intValue();
						return new Double(r);
					}
					if (res instanceof LinkedHashMap<?, ?> && resultType instanceof Color) {
						LinkedHashMap<?, ?> lhm = (LinkedHashMap<?, ?>) res;
						IntegerAttribute r = (IntegerAttribute) lhm.get("red");
						IntegerAttribute g = (IntegerAttribute) lhm.get("green");
						IntegerAttribute b = (IntegerAttribute) lhm.get("blue");
						return new Color(r.getInteger(), g.getInteger(), b.getInteger());
					}
					if (res instanceof String && resultType instanceof Boolean) {
						String s = (String) res;
						a.remove(attributeName);
						Boolean rr = new Boolean(s.equalsIgnoreCase("TRUE") || s.equalsIgnoreCase("1"));
						setAttribute(attributable, path, attributeName, rr);
						return rr;
					} else {
						ErrorMsg.addErrorMessage("Attribute Type Invalid, is not the same as expected: "
								+ res.getClass().getSimpleName() + " *#* " + resultType.getClass().getSimpleName());
						if (defaultValue != null)
							setAttribute(attributable, path, attributeName, defaultValue);
					}
					return defaultValue;
				}
			}
			return res;
		} catch (Exception e) {
			if (defaultValue != null && setDefaultIfMissing)
				setAttribute(attributable, path, attributeName, defaultValue);
			return defaultValue;
		}
	}
	
	/**
	 * @param attributable
	 * @param attributeName
	 * @return
	 * @author Christian Klukas
	 */
	public static Attribute getAttribute(Attributable attributable, String attributeName) {
		return attributable.getAttribute(attributeName);
	}
	
	/**
	 * @param attributeName
	 * @param attributeValue
	 * @author Christian Klukas
	 */
	public static void addAttributeFolder(Attributable attributeable, String path) {
		CollectionAttribute nc = new HashMapAttribute(path);
		attributeable.addAttribute(nc, "");
	}
	
	// /**
	// * @param node
	// * @return The substance name that is set for the given node.
	// */
	// public static String getSubstanceName(Node node, String defaultName) {
	// String oldSubstanceName = (String) getAttributeValue(node, "data",
	// "substancename",
	// null, new String(""));
	// if (oldSubstanceName!=null)
	// return oldSubstanceName;
	// else
	// return (String) getAttributeValue(node, "charting", "substancename",
	// defaultName, new String(""));
	// }
	
	// /**
	// * @param node
	// * @return The substance name that is set for the given node.
	// */
	// public static void setSubstanceName(Node node, String substanceName) {
	// setAttribute(node, "charting", "substancename", substanceName);
	// }
	
	/**
	 * @param ge
	 * @return
	 */
	public static double getFrameThickNess(GraphElement ge) {
		try {
			DoubleAttribute dblAtt = null;
			dblAtt = (DoubleAttribute) ge.getAttribute(GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR
					+ GraphicAttributeConstants.FRAMETHICKNESS);
			
			return dblAtt.getDouble();
		} catch (Exception ex) {
			return 1;
		}
	}
	
	public static void setFrameThickNess(GraphElement ge, double frameThinkness) {
		try {
			DoubleAttribute dblAtt = null;
			dblAtt = (DoubleAttribute) ge.getAttribute(GraphicAttributeConstants.GRAPHICS + Attribute.SEPARATOR
					+ GraphicAttributeConstants.FRAMETHICKNESS);
			
			dblAtt.setDouble(frameThinkness);
		} catch (Exception ex) {
			
		}
	}
	
	/**
	 * @param node
	 */
	public static void setDefaultGraphicsAttribute(Node node, double x, double y) {
		setNodeGraphicsAttribute(x, y, 3, 20, 20, // 120, 120
				new Color(0, 0, 0, 255), new Color(0, 255, 255, 255), // 100
				node.getAttributes());
	}
	
	/**
	 * @param posx
	 * @param posy
	 * @param frameThickness_3
	 * @param width_25
	 * @param height_25
	 * @param frameColor_0_0_0_255
	 * @param fillColor_0_100_250_100
	 * @param col
	 */
	public static void setNodeGraphicsAttribute(double posx, double posy, double frameThickness_3, double width_25,
			double height_25, Color frameColor_0_0_0_255, Color fillColor_0_100_250_100, CollectionAttribute col) {
		NodeGraphicAttribute graphics = new NodeGraphicAttribute();
		CoordinateAttribute cooAtt = graphics.getCoordinate();
		Point2D pos = new Point2D.Double();
		pos.setLocation(posx, posy);
		cooAtt.setCoordinate(pos);
		col.add(graphics, false);
		
		// setting the graphic attributes to the default values stored
		// in the preferences
		graphics.setFrameThickness(frameThickness_3);
		
		// setting the dimension
		double height = height_25;
		double width = width_25;
		graphics.getDimension().setDimension(
				new Dimension((int) java.lang.Math.round(width), (int) java.lang.Math.round(height)));
		// setting the framecolor
		if (frameColor_0_0_0_255 == null)
			graphics.getFramecolor().setColor(new Color(0, 0, 0, 255));
		else
			graphics.getFramecolor().setColor(frameColor_0_0_0_255);
		// setting the fillcolor
		if (fillColor_0_100_250_100 == null)
			graphics.getFillcolor().setColor(new Color(0, 100, 250, 100));
		else
			graphics.getFillcolor().setColor(fillColor_0_100_250_100);
		// setting the shape
		graphics.setShape("org.graffiti.plugins.views.defaults.RectangleNodeShape");
	}
	
	/**
	 * @param newNode
	 * @param position
	 */
	public static void setDefaultGraphicsAttribute(Node newNode, Point2D position) {
		setDefaultGraphicsAttribute(newNode, position.getX(), position.getY());
	}
	
	public static CollectionAttribute getDefaultGraphicsAttributeForEdge(Color colArrow, Color colLine, boolean directed) {
		CollectionAttribute col = new HashMapAttribute("");
		EdgeGraphicAttribute graphics = getNewEdgeGraphicsAttribute(colArrow, colLine, directed);
		col.add(graphics, false);
		return col;
	}
	
	public static EdgeGraphicAttribute getNewEdgeGraphicsAttribute(Color colArrow, Color colLine, boolean directed) {
		EdgeGraphicAttribute graphics = new EdgeGraphicAttribute();
		DockingAttribute dock = graphics.getDocking();
		dock.setSource("");
		dock.setTarget("");
		
		// setting the graphic attributes to the default values stored
		// in the preferences
		graphics.setThickness(1);
		graphics.setFrameThickness(1);
		
		// setting the framecolor
		graphics.getFramecolor().setColor(colLine);
		
		// setting the fillcolor
		graphics.getFillcolor().setColor(colArrow);
		
		graphics.setShape("org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
		if (directed) {
			graphics.setArrowhead("org.graffiti.plugins.views.defaults.StandardArrowShape");
		}
		// setting the lineMode
		graphics.getLineMode().setDashArray(null);
		graphics.getLineMode().setDashPhase(0.0f);
		return graphics;
	}
	
	/**
	 * Gets the size of the Node.
	 * 
	 * @param myNode
	 * @return Tehe size as a <code>Vector2d</code> structure.
	 */
	public static Dimension getSizeD(Node myNode) {
		try {
			Double width;
			Double height;
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			width = (Double) da.getValue();
			da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			height = (Double) da.getValue();
			return new Dimension(width.intValue(), height.intValue());
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Get Size-Failure: " + ex.getLocalizedMessage());
		}
		return null;
	}
	
	public static Vector3d getSize3D(Node myNode, double defaultDepth, boolean setDefault) {
		Vector2d size = getSize(myNode);
		return new Vector3d(size.x, size.y, getDepth(myNode, defaultDepth, setDefault));
	}
	
	public static double getWidth(Node myNode) {
		try {
			double width;
			// double height;
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			width = ((Double) da.getValue()).doubleValue();
			return width;
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Get Size-Failure: " + ex.getLocalizedMessage());
		}
		return Double.NaN;
	}
	
	public static double getHeight(Node myNode) {
		try {
			double height;
			DoubleAttribute da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			da = (DoubleAttribute) myNode.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			height = ((Double) da.getValue()).doubleValue();
			return height;
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage("Get Size-Failure: " + ex.getLocalizedMessage());
		}
		return Double.NaN;
	}
	
	/**
	 * @param node
	 * @param oldPos
	 */
	public static void setPosition(Node node, Vector2d position) {
		try {
			CoordinateAttribute cn = (CoordinateAttribute) node.getAttribute(GraphicAttributeConstants.COORD_PATH);
			
			Point2D p = new Point2D.Double(position.x, position.y);
			cn.setCoordinate(p);
		} catch (AttributeNotFoundException e) {
			addDefaultGraphicsAttributeToNode(node, position.x, position.y);
			// ErrorMsg.addErrorMessage("Set Position-Failure: "
			// + e.getLocalizedMessage());
		}
	}
	
	public static void setToolTipText(Attributable atta, String statusText) {
		setAttribute(atta, "", "tooltip", statusText);
	}
	
	public static String getToolTipText(GraphElement atta) {
		String res = (String) getAttributeValue(atta, "", "tooltip", null, new String(""));
		if (res != null && res.length() == 0)
			return null;
		else
			return res;
	}
	
	public static void setBorderWidth(Node node, double frameThickness) {
		NodeGraphicAttribute na = (NodeGraphicAttribute) node.getAttribute(GraphicAttributeConstants.GRAPHICS);
		na.setFrameThickness(frameThickness);
	}
	
	public static void setBorderWidth(GraphElement graphElement, double frameThickness) {
		if (graphElement instanceof Node)
			setBorderWidth((Node) graphElement, frameThickness);
		else
			setBorderWidth((Edge) graphElement, frameThickness);
	}
	
	public static void setBorderWidth(Edge edge, double frameThickness) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null)
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
		ega.setFrameThickness(frameThickness);
	}
	
	public static void setArrowSize(Edge edge, double arrowSize) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null)
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
		ega.setThickness(arrowSize);
	}
	
	public static double getArrowSize(Edge edge) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null)
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
		return ega.getThickness();
	}
	
	/**
	 * @param attr
	 * @param hexStr
	 *           A color defined like #FFFFFF (must be 6 characters (hex) for the
	 *           color components)
	 */
	public static void setFillColorHEX(Attributable attr, String hexStr) {
		if (hexStr.startsWith("#") && hexStr.length() == "#FFFFFF".length()) {
			hexStr = hexStr.substring(1);
			BigInteger biR = new BigInteger(hexStr.substring(0, 2), 16);
			BigInteger biG = new BigInteger(hexStr.substring(2, 4), 16);
			BigInteger biB = new BigInteger(hexStr.substring(4, 6), 16);
			Color c = new Color(biR.intValue(), biG.intValue(), biB.intValue());
			setFillColor(attr, c);
		} else {
			ErrorMsg.addErrorMessage("Invalid Color-Hex-Code: " + hexStr);
		}
	}
	
	/**
	 * @param attr
	 * @param hexStr
	 *           A color defined like #FFFFFF (must be 6 characters (hex) for the
	 *           color components)
	 */
	public static void setOutlineColorHEX(Attributable attr, String hexStr) {
		if (hexStr.startsWith("#") && hexStr.length() == "#FFFFFF".length()) {
			hexStr = hexStr.substring(1);
			BigInteger biR = new BigInteger(hexStr.substring(0, 2), 16);
			BigInteger biG = new BigInteger(hexStr.substring(2, 4), 16);
			BigInteger biB = new BigInteger(hexStr.substring(4, 6), 16);
			Color c = new Color(biR.intValue(), biG.intValue(), biB.intValue());
			setOutlineColor(attr, c);
		} else {
			ErrorMsg.addErrorMessage("Invalid Color-Hex-Code: " + hexStr);
		}
	}
	
	public static void setSBMLid(GraphElement ge, String id) {
		AttributeHelper.setAttribute(ge, "sbml", "sbmlID", id);
	}
	
	public static String getSBMLid(GraphElement ge) {
		return (String) AttributeHelper.getAttributeValue(ge, "sbml", "sbmlID", "", null);
	}
	
	public static void setSBMLmodelID(GraphElement ge, String modelID) {
		AttributeHelper.setAttribute(ge, "sbml", "modelID", modelID);
	}
	
	public static void setSBMLreversibleReaction(GraphElement ge, String reactionReversible) {
		AttributeHelper.setAttribute(ge, "sbml", "reversible", reactionReversible);
	}
	
	public static boolean isSBMLreversibleReaction(GraphElement ge) {
		if (!hasAttribute(ge, "sbml", "reversible"))
			setAttribute(ge, "sbml", "reversible", true);
		Boolean val = (Boolean) getAttributeValue(ge, "sbml", "reversible", true, new Boolean(true), false);
		if (val == null || val.booleanValue())
			return true;
		else {
			return false;
		}
	}
	
	public static boolean isSBMLreaction(GraphElement ge) {
		return hasAttribute(ge, "sbml" + attributeSeparator + "reversible");
	}
	
	public static void setSBMLrole(GraphElement ge, String role) {
		AttributeHelper.setAttribute(ge, "sbml", "sbmlRole", role);
	}
	
	public static String getSBMLrole(GraphElement ge) {
		return (String) AttributeHelper.getAttributeValue(ge, "sbml", "sbmlRole", "", "");
	}
	
	// public static void setSBMLlabel(GraphElement ge, String sbmlEncodedLabel)
	// {
	// assert(ge instanceof Node || ge instanceof Edge);
	// sbmlEncodedLabel = ErrorMsg.stringReplace(sbmlEncodedLabel, "_minus_",
	// "-");
	// sbmlEncodedLabel = ErrorMsg.stringReplace(sbmlEncodedLabel, "_space_", "
	// ");
	// sbmlEncodedLabel = ErrorMsg.stringReplace(sbmlEncodedLabel, "_alpha_",
	// "&alpha;");
	// sbmlEncodedLabel = ErrorMsg.stringReplace(sbmlEncodedLabel, "_beta_",
	// "&beta;");
	// if (ge instanceof Node)
	// setLabel((Node)ge, sbmlEncodedLabel);
	// if (ge instanceof Edge)
	// setLabel((Edge)ge, sbmlEncodedLabel);
	// }
	
	public static void setRoundedEdges(Node node, double rounding) {
		NodeGraphicAttribute na = (NodeGraphicAttribute) node.getAttribute(GraphicAttributeConstants.GRAPHICS);
		na.setRoundedEdges(rounding);
	}
	
	public static double getRoundedEdges(Node node) {
		try {
			NodeGraphicAttribute na = (NodeGraphicAttribute) node.getAttribute(GraphicAttributeConstants.GRAPHICS);
			return na.getRoundedEdges();
		} catch (AttributeNotFoundException e) {
			return 0;
		}
	}
	
	public static void setKEGGhiddenCompoundInformation(Edge e, String compID) {
		AttributeHelper.setAttribute(e, "kegg", "kegg_hidden_compound", compID);
	}
	
	private static HashMap<String, ImageIcon> attributeIconCache = new HashMap<String, ImageIcon>();
	
	public static ImageIcon getDefaultAttributeIconFor(String id) {
		// System.out.println(id);
		if (!attributeIconCache.containsKey(id)) {
			ClassLoader cl = AttributeHelper.class.getClassLoader();
			String path = AttributeHelper.class.getPackage().getName().replace('.', '/');
			URL url = cl.getResource(path + "/images/" + id + ".png");
			ImageIcon ic;
			if (url != null)
				ic = new ImageIcon(url);
			else
				ic = null;
			attributeIconCache.put(id, ic);
		}
		return attributeIconCache.get(id);
	}
	
	// private static final Color[] knownColors = new Color[] {
	// Color.BLUE,
	// Color.CYAN,
	// Color.GREEN,
	// Color.MAGENTA,
	// Color.ORANGE,
	// Color.PINK,
	// Color.RED,
	// Color.YELLOW
	// };
	
	// private static final String[] knownColorNames = new String[] {
	// "blue",
	// "cyan",
	// "green",
	// "magenta",
	// "orange",
	// "pink",
	// "red",
	// "yellow"
	// };
	
	private static final String[] knownColorNames = { "alice blue", // F0F8FF
			"antique white", // FAEBD7
			"aqua", // 00FFFF
			"aquamarine", // 7FFFD4
			"azure", // F0FFFF
			"beige", // F5F5DC
			"bisque", // FFE4C4
			"black", // 000000
			"blanchedalmond", // FFEBCD
			"blue", // 0000FF
			"blue violet", // 8A2BE2
			"brown", // A52A2A
			"burlywood", // DEB887
			"cadet blue", // 5F9EA0
			"chartreuse", // 7FFF00
			"chocolate", // D2691E
			"coral", // FF7F50
			"corn flower blue", // 6495ED
			"cornsilk", // FFF8DC
			"crimson", // DC143C
			"cyan", // 00FFFF
			"dark blue", // 00008B
			"dark cyan", // 008B8B
			"dark golden rod", // B8860B
			"dark gray", // A9A9A9
			"dark green", // 006400
			"dark khaki", // BDB76B
			"dark magenta", // 8B008B
			"dark olive green", // 556B2F
			"dark orange", // FF8C00
			"dark orchid", // 9932CC
			"dark red", // 8B0000
			"dark salmon", // E9967A
			"dark sea green", // 8FBC8F
			"dark slate blue", // 483D8B
			"dark slate gray", // 2F4F4F
			"dark turquoise", // 00CED1
			"dark violet", // 9400D3
			"deep pink", // FF1493
			"deep sky blue", // 00BFFF
			"dim gray", // 696969
			"dodger blue", // 1E90FF
			"firebrick", // B22222
			"floral white", // FFFAF0 16775920
			"forest green", // 228B22
			"fuchsia", // FF00FF
			"gainsboro", // DCDCDC
			"ghost white", // F8F8FF
			"gold", // FFD700
			"golden rod", // DAA520
			"gray", // 808080
			"green", // 008000
			"green yellow", // ADFF2F
			"honey dew", // F0FFF0
			"hot pink", // FF69B4
			"indian red", // CD5C5C
			"indigo", // 4B0082
			"ivory", // FFFFF0
			"khaki", // F0E68C
			"lavender", // E6E6FA
			"lavenderblush", // FFF0F5
			"lawn green", // 7CFC00
			"lemon chiffon", // FFFACD
			"light blue", // ADD8E6
			"light coral", // F08080
			"light cyan", // E0FFFF
			"light golden rod yellow", // FAFAD2
			"light green", // 90EE90
			"light grey", // D3D3D3
			"light pink", // FFB6C1
			"light salmon", // FFA07A
			"light sea green", // 20B2AA
			"light sky blue", // 87CEFA
			"light slate gray", // 778899
			"light steel blue", // B0C4DE
			"light yellow", // FFFFE0
			"lime", // 00FF00
			"lime green", // 32CD32
			"linen", // FAF0E6
			"magenta", // FF00FF
			"maroon", // 800000
			"medium aquamarine", // 66CDAA
			"medium blue", // 0000CD
			"medium orchid", // BA55D3
			"medium purple", // 9370DB
			"medium sea green", // 3CB371
			"medium slate blue", // 7B68EE
			"medium spring green", // 00FA9A
			"medium turquoise", // 48D1CC
			"medium violet red", // C71585
			"midnight blue", // 191970
			"mint cream", // F5FFFA
			"misty rose", // FFE4E1
			"moccasin", // FFE4B5
			"navajo white", // FFDEAD
			"navy", // 000080
			"oldlace", // FDF5E6
			"olive", // 808000
			"olivedrab", // 6B8E23
			"orange", // FFA500
			"orange red", // FF4500
			"orchid", // DA70D6
			"pale golden rod", // EEE8AA
			"pale green", // 98FB98
			"paleturquoise", // AFEEEE
			"paleviolet red", // DB7093
			"papaya whip", // FFEFD5
			"peach puff", // FFDAB9
			"peru", // CD853F
			"pink", // FFC0CB
			"plum", // DDA0DD
			"powder blue", // B0E0E6
			"purple", // 800080
			"red", // FF0000
			"rosy brown", // BC8F8F
			"royal blue", // 4169E1
			"saddle brown", // 8B4513
			"salmon", // FA8072
			"sandy brown", // F4A460
			"sea green", // 2E8B57
			"seashell", // FFF5EE
			"sienna", // A0522D
			"silver", // C0C0C0
			"sky blue", // 87CEEB
			"slate blue", // 6A5ACD
			"slate gray", // 708090
			"snow", // FFFAFA 16775930
			"spring green", // 00FF7F
			"steel blue", // 4682B4
			"tan", // D2B48C
			"teal", // 008080
			"thistle", // D8BFD8
			"tomato", // FF6347
			"turquoise", // 40E0D0
			"violet", // EE82EE
			"wheat", // F5DEB3
			"white", // FFFFFF 16777215
			"white smoke", // F5F5F5
			"yellow", // FFFF00
			"yellow green" // 9ACD32
	};
	
	public static final Color[] knownColors = { new Color(0xFFF0F8FF), // aliceblue
			new Color(0xFFFAEBD7), // antiquewhite
			new Color(0xFF00FFFF), // aqua
			new Color(0xFF7FFFD4), // aquamarine
			new Color(0xFFF0FFFF), // azure
			new Color(0xFFF5F5DC), // beige
			new Color(0xFFFFE4C4), // bisque
			new Color(0xFF000000), // black
			new Color(0xFFFFEBCD), // blanchedalmond
			new Color(0xFF0000FF), // blue
			new Color(0xFF8A2BE2), // blueviolet
			new Color(0xFFA52A2A), // brown
			new Color(0xFFDEB887), // burlywood
			new Color(0xFF5F9EA0), // cadetblue
			new Color(0xFF7FFF00), // chartreuse
			new Color(0xFFD2691E), // chocolate
			new Color(0xFFFF7F50), // coral
			new Color(0xFF6495ED), // cornflowerblue
			new Color(0xFFFFF8DC), // cornsilk
			new Color(0xFFDC143C), // crimson
			new Color(0xFF00FFFF), // cyan
			new Color(0xFF00008B), // darkblue
			new Color(0xFF008B8B), // darkcyan
			new Color(0xFFB8860B), // darkgoldenrod
			new Color(0xFFA9A9A9), // darkgray
			new Color(0xFF006400), // darkgreen
			new Color(0xFFBDB76B), // darkkhaki
			new Color(0xFF8B008B), // darkmagenta
			new Color(0xFF556B2F), // darkolivegreen
			new Color(0xFFFF8C00), // darkorange
			new Color(0xFF9932CC), // darkorchid
			new Color(0xFF8B0000), // darkred
			new Color(0xFFE9967A), // darksalmon
			new Color(0xFF8FBC8F), // darkseagreen
			new Color(0xFF483D8B), // darkslateblue
			new Color(0xFF2F4F4F), // darkslategray
			new Color(0xFF00CED1), // darkturquoise
			new Color(0xFF9400D3), // darkviolet
			new Color(0xFFFF1493), // deeppink
			new Color(0xFF00BFFF), // deepskyblue
			new Color(0xFF696969), // dimgray
			new Color(0xFF1E90FF), // dodgerblue
			new Color(0xFFB22222), // firebrick
			new Color(0xFFFFFAF0), // floralwhite
			new Color(0xFF228B22), // forestgreen
			new Color(0xFFFF00FF), // fuchsia
			new Color(0xFFDCDCDC), // gainsboro
			new Color(0xFFF8F8FF), // ghostwhite
			new Color(0xFFFFD700), // gold
			new Color(0xFFDAA520), // goldenrod
			new Color(0xFF808080), // gray
			new Color(0xFF008000), // green
			new Color(0xFFADFF2F), // greenyellow
			new Color(0xFFF0FFF0), // honeydew
			new Color(0xFFFF69B4), // hotpink
			new Color(0xFFCD5C5C), // indianred
			new Color(0xFF4B0082), // indigo
			new Color(0xFFFFFFF0), // ivory
			new Color(0xFFF0E68C), // khaki
			new Color(0xFFE6E6FA), // lavender
			new Color(0xFFFFF0F5), // lavenderblush
			new Color(0xFF7CFC00), // lawngreen
			new Color(0xFFFFFACD), // lemonchiffon
			new Color(0xFFADD8E6), // lightblue
			new Color(0xFFF08080), // lightcoral
			new Color(0xFFE0FFFF), // lightcyan
			new Color(0xFFFAFAD2), // lightgoldenrodyellow
			new Color(0xFF90EE90), // lightgreen
			new Color(0xFFD3D3D3), // lightgrey
			new Color(0xFFFFB6C1), // lightpink
			new Color(0xFFFFA07A), // lightsalmon
			new Color(0xFF20B2AA), // lightseagreen
			new Color(0xFF87CEFA), // lightskyblue
			new Color(0xFF778899), // lightslategray
			new Color(0xFFB0C4DE), // lightsteelblue
			new Color(0xFFFFFFE0), // lightyellow
			new Color(0xFF00FF00), // lime
			new Color(0xFF32CD32), // limegreen
			new Color(0xFFFAF0E6), // linen
			new Color(0xFFFF00FF), // magenta
			new Color(0xFF800000), // maroon
			new Color(0xFF66CDAA), // mediumaquamarine
			new Color(0xFF0000CD), // mediumblue
			new Color(0xFFBA55D3), // mediumorchid
			new Color(0xFF9370DB), // mediumpurple
			new Color(0xFF3CB371), // mediumseagreen
			new Color(0xFF7B68EE), // mediumslateblue
			new Color(0xFF00FA9A), // mediumspringgreen
			new Color(0xFF48D1CC), // mediumturquoise
			new Color(0xFFC71585), // mediumvioletred
			new Color(0xFF191970), // midnightblue
			new Color(0xFFF5FFFA), // mintcream
			new Color(0xFFFFE4E1), // mistyrose
			new Color(0xFFFFE4B5), // moccasin
			new Color(0xFFFFDEAD), // navajowhite
			new Color(0xFF000080), // navy
			new Color(0xFFFDF5E6), // oldlace
			new Color(0xFF808000), // olive
			new Color(0xFF6B8E23), // olivedrab
			new Color(0xFFFFA500), // orange
			new Color(0xFFFF4500), // orangered
			new Color(0xFFDA70D6), // orchid
			new Color(0xFFEEE8AA), // palegoldenrod
			new Color(0xFF98FB98), // palegreen
			new Color(0xFFAFEEEE), // paleturquoise
			new Color(0xFFDB7093), // palevioletred
			new Color(0xFFFFEFD5), // papayawhip
			new Color(0xFFFFDAB9), // peachpuff
			new Color(0xFFCD853F), // peru
			new Color(0xFFFFC0CB), // pink
			new Color(0xFFDDA0DD), // plum
			new Color(0xFFB0E0E6), // powderblue
			new Color(0xFF800080), // purple
			new Color(0xFFFF0000), // red
			new Color(0xFFBC8F8F), // rosybrown
			new Color(0xFF4169E1), // royalblue
			new Color(0xFF8B4513), // saddlebrown
			new Color(0xFFFA8072), // salmon
			new Color(0xFFF4A460), // sandybrown
			new Color(0xFF2E8B57), // seagreen
			new Color(0xFFFFF5EE), // seashell
			new Color(0xFFA0522D), // sienna
			new Color(0xFFC0C0C0), // silver
			new Color(0xFF87CEEB), // skyblue
			new Color(0xFF6A5ACD), // slateblue
			new Color(0xFF708090), // slategray
			new Color(0xFFFFFAFA), // snow
			new Color(0xFF00FF7F), // springgreen
			new Color(0xFF4682B4), // steelblue
			new Color(0xFFD2B48C), // tan
			new Color(0xFF008080), // teal
			new Color(0xFFD8BFD8), // thistle
			new Color(0xFFFF6347), // tomato
			new Color(0xFF40E0D0), // turquoise
			new Color(0xFFEE82EE), // violet
			new Color(0xFFF5DEB3), // wheat
			new Color(0xFFFFFFFF), // white
			new Color(0xFFF5F5F5), // whitesmoke
			new Color(0xFFFFFF00), // yellow
			new Color(0xFF9ACD32), // yellowgreen
	};
	
	public static Color getColorFrom3floatValues0to1(String color, Color ifUnkown) {
		String[] col = color.split(" ");
		if (col.length != 3)
			return ifUnkown;
		String r = col[0];
		String g = col[1];
		String b = col[2];
		try {
			float rd = Float.parseFloat(r);
			float gd = Float.parseFloat(g);
			float bd = Float.parseFloat(b);
			return new Color(rd, gd, bd);
		} catch (NumberFormatException nfe) {
			ErrorMsg.addErrorMessage(nfe);
			return ifUnkown;
		}
	}
	
	public static Color getColorFromName(String name, Color ifUnkown) {
		int i = 0;
		for (String cname : knownColorNames) {
			if (cname.equalsIgnoreCase(name))
				return knownColors[i];
			i++;
		}
		name = StringManipulationTools.stringReplace(name, "0", "");
		name = StringManipulationTools.stringReplace(name, "1", "");
		name = StringManipulationTools.stringReplace(name, "2", "");
		name = StringManipulationTools.stringReplace(name, "3", "");
		name = StringManipulationTools.stringReplace(name, "4", "");
		name = StringManipulationTools.stringReplace(name, "5", "");
		name = StringManipulationTools.stringReplace(name, "6", "");
		name = StringManipulationTools.stringReplace(name, "7", "");
		name = StringManipulationTools.stringReplace(name, "8", "");
		name = StringManipulationTools.stringReplace(name, "9", "");
		name = StringManipulationTools.stringReplace(name, " ", "");
		i = 0;
		for (String cname : knownColorNames) {
			cname = StringManipulationTools.stringReplace(cname, " ", "");
			if (cname.equalsIgnoreCase(name))
				return knownColors[i];
			i++;
		}
		return ifUnkown;
	}
	
	public static String getColorName(Color attrColor) {
		if (attrColor == null)
			return "not set (null)";
		
		int nearest = -1;
		int i = 0;
		double diff = Double.MAX_VALUE;
		for (Color testColor : knownColors) {
			double tDiff = ColorUtil.deltaE2000(attrColor, testColor);
			if (tDiff < diff) {
				nearest = i;
				diff = tDiff;
			}
			i++;
		}
		if (nearest >= 0) {
			return knownColorNames[nearest];
		} else
			return "";
	}
	
	public static void setDashInfo(GraphElement edgeOrNode, float a, float b) {
		setDashInfo(edgeOrNode, new float[] { a, b });
	}
	
	public static void setDashInfo(GraphElement edgeOrNode, float[] values) {
		Dash dash = new Dash();
		dash.setDashArray(values);
		dash.setDashPhase(0f);
		if (edgeOrNode instanceof Edge) {
			Edge edge = (Edge) edgeOrNode;
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(EdgeGraphicAttribute.GRAPHICS);
			LineModeAttribute lma = new LineModeAttribute(GraphicAttributeConstants.LINEMODE, dash);
			ega.setLineMode(lma);
		}
		if (edgeOrNode instanceof Node) {
			Node node = (Node) edgeOrNode;
			NodeGraphicAttribute nga = (NodeGraphicAttribute) node.getAttribute(NodeGraphicAttribute.GRAPHICS);
			LineModeAttribute lma = new LineModeAttribute(GraphicAttributeConstants.LINEMODE, dash);
			nga.setLineMode(lma);
		}
	}
	
	public static float[] getDashInfo(Edge edge) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(EdgeGraphicAttribute.GRAPHICS);
		return ega.getLineMode().getDashArray();
	}
	
	public static CollectionAttribute getDefaultGraphicsAttributeForNode(Vector2d position) {
		return getDefaultGraphicsAttributeForNode(position.x, position.y);
	}
	
	public static CollectionAttribute getDefaultGraphicsAttributeForNode(double x, double y) {
		CollectionAttribute col = new HashMapAttribute("");
		NodeGraphicAttribute graphics = getNodeGraphicsAttribute(x, y);
		col.add(graphics, false);
		return col;
	}
	
	public static CollectionAttribute getDefaultGraphicsAttributeForKeggNode(double x, double y) {
		CollectionAttribute col = new HashMapAttribute("");
		NodeGraphicAttribute graphics = getNodeGraphicsAttribute(x, y);
		graphics.setShape(GraphicAttributeConstants.ELLIPSE_CLASSNAME);
		col.add(graphics, false);
		return col;
	}
	
	private static NodeGraphicAttribute getNodeGraphicsAttribute(double x, double y) {
		NodeGraphicAttribute graphics = new NodeGraphicAttribute();
		CoordinateAttribute cooAtt = graphics.getCoordinate();
		cooAtt.setCoordinate(new Point2D.Double(x, y));
		// setting the graphic attributes to the default values stored
		// in the preferences
		graphics.setFrameThickness(2);
		// setting the dimension
		graphics.getDimension().setDimension(new Dimension(25, 25));
		graphics.setRoundedEdges(5);
		// setting the framecolor
		graphics.getFramecolor().setColor(Color.BLACK);
		// setting the fillcolor
		graphics.getFillcolor().setColor(Color.WHITE);
		// setting the shape
		graphics.setShape("org.graffiti.plugins.views.defaults.RectangleNodeShape");
		// setting the lineMode
		graphics.getLineMode().setDashArray(null);
		graphics.getLineMode().setDashPhase(0.0f);
		return graphics;
	}
	
	private static HashSet<String> knownNotExistingHelpTopics = new HashSet<String>();
	private static HashMap<String, String> helpTopics = null;
	
	public static String getHelpTopicFor(String tabName, String groupName) {
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			return null;
		if (helpTopics == null)
			initHelpTopics();
		String g = (tabName + ": " + groupName).toUpperCase();
		if (knownNotExistingHelpTopics.contains(g))
			return null;
		String r = helpTopics.get(g);
		if (r == null) {
			System.out.println("Missing Help: " + g);
			knownNotExistingHelpTopics.add(g);
		}
		return r;
	}
	
	private static void initHelpTopics() {
		helpTopics = new HashMap<String, String>();
		
		helpTopics.put("NETWORK: GRAPH", "panel_graph_graph");
		helpTopics.put("NETWORK: NETWORK ATTRIBUTES", "panel_graph_graph");
		helpTopics
				.put("NETWORK: CHARTING <SMALL><FONT COLOR=\"GRAY\">(ALL NODES)</FONT></SMALL>", "panel_graph_charting");
		helpTopics.put("NETWORK: CHARTING <SMALL><FONT COLOR=\"GRAY\">(ALL LINE-CHARTS)</FONT></SMALL>",
				"panel_graph_chartinglines");
		helpTopics.put("NETWORK: SCATTER-PLOT", "panel_graph_scatterplot");
		helpTopics.put("NETWORK: KEGG", "panel_graph_kegg");
		
		helpTopics.put("NODE: LABEL", "panel_node_label");
		helpTopics.put("NODE: KEGG", "panel_node_kegg");
		helpTopics.put("NODE: NODE ATTRIBUTES", "panel_node_node");
		helpTopics.put("NODE: CHARTING <SMALL><FONT COLOR=\"GRAY\">(SELECTED NODES)</FONT></SMALL>",
				"panel_node_charting");
		
		helpTopics.put("EDGE: EDGE ATTRIBUTES", "panel_edge_edge");
		helpTopics.put("EDGE: KEGG", "panel_edge_kegg");
		helpTopics.put("EDGE: LABEL", "panel_edge_label");
		String paramDialog = ": PARAMETER DIALOG";
		helpTopics.put("CIRCLE" + paramDialog, "layout_circle");
		helpTopics.put("CIRCLE (MIN. CROSSINGS)" + paramDialog, "layout_circle");
		helpTopics.put("GRID LAYOUT" + paramDialog, "layout_grid");
		helpTopics.put("RADIAL TREE" + paramDialog, "layoutmenu_layout");
		helpTopics.put("REMOVE NODE-OVERLAP" + paramDialog, "layoutmenu_removeoverlapp");
		helpTopics.put("TREE LAYOUT" + paramDialog, "layoutmenu_layout");
		helpTopics.put("CLUSTER LAYOUT PARAMETERS" + paramDialog, "layoutmenu_layoutcluster");
		helpTopics.put("INTRODUCE BENDS PARAMETERS" + paramDialog, "edgesmenu_addbends");
		helpTopics.put("INPUT TARGET URL" + paramDialog, "editmenu_refurl");
		helpTopics.put("SELECT NODES" + paramDialog, "editmenu_seldatanodes");
		helpTopics.put("CLUSTER DETECTION - TRAIN SOM" + paramDialog, "analysismenu_som");
		helpTopics.put("CLUSTER NODE-COLORING" + paramDialog, "analysismenu_clusternodecoloring");
		helpTopics.put("David et al. Quicktest".toUpperCase() + paramDialog, "analysismenu_david");
		helpTopics.put("Grubbs' Test".toUpperCase() + paramDialog, "analysismenu_grubbs");
		helpTopics.put("AVERAGE SUBSTANCE-LEVEL > BACKGROUND COLOR" + paramDialog, "nodesmenu_levelbackground");
	}
	
	public static void copyReplaceStringAttribute(Attributable a, String path, String name, String copyTo,
			String searchString, String replaceString) {
		if (hasAttribute(a, path, name)) {
			String attVal = (String) getAttributeValue(a, path, name, "", "", false);
			if (attVal.length() > 0) {
				attVal = StringManipulationTools.stringReplace(attVal, searchString, replaceString);
				setAttribute(a, path, copyTo, attVal);
			}
		}
	}
	
	public static void setLabel(GraphElement ge, String label) {
		if (ge instanceof Node)
			setLabel((Node) ge, label);
		else
			if (ge instanceof Edge)
				setLabel((Edge) ge, label);
			else
				ErrorMsg.addErrorMessage("Set Label only works on Node or Edge! (internal error)");
	}
	
	public static String getLabelPosition(Attributable attributable) {
		try {
			Node node = (Node) attributable;
			NodeLabelAttribute labelAttr;
			
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (NodeLabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
				return labelAttr.getAlignment();
			} else {
				return null;
			}
		} catch (Exception ex) {
		}
		return null;
	}
	
	public static void setLabelPosition(Node node, String pos) {
		try {
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (LabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
			} else {
				// no label - associate one
				labelAttr = new NodeLabelAttribute(GraphicAttributeConstants.LABELGRAPHICS, "");
				node.addAttribute(labelAttr, GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
			}
			labelAttr.setAlignment(pos);
		} catch (Exception ex) {
			ErrorMsg.addErrorMessage(ex);
		}
	}
	
	/**
	 * Get NodeLabelAttribute (if available)
	 * 
	 * @param index
	 *           Use -1 to get main label, use 0..99 to get annotation labels
	 * @param node
	 *           Node to be processed
	 * @return NodeLabelAttribute, if present, otherwise null.
	 */
	public static NodeLabelAttribute getLabel(int index, Node node) {
		try {
			String idx = "" + index;
			if (index < 0)
				idx = "";
			NodeLabelAttribute labelAttr = (NodeLabelAttribute) node.getAttribute(GraphicAttributeConstants.LABELGRAPHICS
					+ idx);
			return labelAttr;
		} catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Get EdgeLabelAttribute (if available)
	 * 
	 * @param index
	 *           Use -1 to get main label, use 0..99 to get annotation labels
	 * @param edge
	 *           Edge to be processed
	 * @return EdgeLabelAttribute, if present, otherwise null.
	 */
	public static EdgeLabelAttribute getLabel(int index, Edge edge) {
		try {
			String idx = "" + index;
			if (index < 0)
				idx = "";
			EdgeLabelAttribute labelAttr = (EdgeLabelAttribute) edge.getAttribute(GraphicAttributeConstants.LABELGRAPHICS
					+ idx);
			return labelAttr;
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static void setLabelFrameStyle(int index, Node n, LabelFrameSetting setting) {
		LabelAttribute la = getLabel(index, n);
		if (la != null) {
			String currentStyle = la.getFontStyle();
			if (currentStyle == null)
				currentStyle = "";
			for (LabelFrameSetting lfs : LabelFrameSetting.values()) {
				if (lfs.toGMLstring().length() == 0)
					continue;
				if (currentStyle.contains(lfs.toGMLstring() + ",")) {
					currentStyle = StringManipulationTools.stringReplace(currentStyle, lfs.toGMLstring() + ",", "");
				} else
					if (currentStyle.contains("," + lfs.toGMLstring())) {
						currentStyle = StringManipulationTools.stringReplace(currentStyle, "," + lfs.toGMLstring(), "");
					} else
						if (currentStyle.equals(lfs.toGMLstring())) {
							currentStyle = "";;
						}
			}
			if (setting != LabelFrameSetting.NO_FRAME) {
				if (currentStyle.length() > 0)
					currentStyle = currentStyle + "," + setting.toGMLstring();
				else
					currentStyle = setting.toGMLstring();
			}
			currentStyle = StringManipulationTools.stringReplace(currentStyle, " ", "");
			
			la.setFontStyle(currentStyle);
		}
	}
	
	/**
	 * @deprecated Use {@link StringManipulationTools#formatNumber(double,String)} instead
	 */
	public static String formatNumber(double d, String pattern) {
		return StringManipulationTools.formatNumber(d, pattern);
	}
	
	public static void setDashArray(Edge edge, float[] dashArray) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(EdgeGraphicAttribute.GRAPHICS);
		ega.getLineMode().setDashArray(dashArray);
	}
	
	public static void setDashArray(Edge edge, float[] dashArray, float dashphase) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(EdgeGraphicAttribute.GRAPHICS);
		ega.getLineMode().setDashArray(dashArray);
		ega.getLineMode().setDashPhase(dashphase);
	}
	
	public static void setShapeEllipse(Node node) {
		NodeGraphicAttribute nga = (NodeGraphicAttribute) node.getAttribute(NodeGraphicAttribute.GRAPHICS);
		nga.setShape(GraphicAttributeConstants.ELLIPSE_CLASSNAME);
	}
	
	public static void setShapeRectangle(Node node) {
		NodeGraphicAttribute nga = (NodeGraphicAttribute) node.getAttribute(NodeGraphicAttribute.GRAPHICS);
		nga.setShape(GraphicAttributeConstants.RECTANGLE_CLASSNAME);
	}
	
	public static void setShape(Node node, String knownShapeClassName) {
		NodeGraphicAttribute nga = (NodeGraphicAttribute) node.getAttribute(NodeGraphicAttribute.GRAPHICS);
		nga.setShape(knownShapeClassName);
	}
	
	public static void setShape(Edge edge, String knownShapeClassName) {
		EdgeGraphicAttribute nga = (EdgeGraphicAttribute) edge.getAttribute(NodeGraphicAttribute.GRAPHICS);
		nga.setShape(knownShapeClassName);
	}
	
	public static String getShape(Node node) {
		try {
			NodeGraphicAttribute nga = (NodeGraphicAttribute) node.getAttribute(NodeGraphicAttribute.GRAPHICS);
			return nga.getShape();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return null;
		}
	}
	
	/**
	 * Assign a arrow shape in the opposite direction.of an edge.
	 * 
	 * @param edge
	 * @param show
	 *           If set to true, the edge will have on both ends an arrow
	 */
	public static void setArrowtail(Edge edge, boolean show) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null) {
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
			ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		}
		if (show)
			ega.setArrowtail("org.graffiti.plugins.views.defaults.StandardArrowShape");
		else
			ega.setArrowtail("");
	}
	
	public static void setArrowhead(Edge edge, boolean show) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null) {
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
			ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		}
		if (show)
			ega.setArrowhead("org.graffiti.plugins.views.defaults.StandardArrowShape");
		else
			ega.setArrowhead("");
	}
	
	public static String getArrowhead(Edge edge) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null) {
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
			ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		}
		return ega.getArrowhead();
	}
	
	public static String getArrowtail(Edge edge) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null) {
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
			ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		}
		return ega.getArrowtail();
	}
	
	public static void setArrowhead(Edge edge, String knownShapeClassName) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null) {
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
			ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		}
		if (ega != null)
			ega.setArrowhead(knownShapeClassName);
	}
	
	public static void setArrowtail(Edge edge, String knownShapeClassName) {
		EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (ega == null) {
			edge.addAttribute(getDefaultGraphicsAttributeForEdge(Color.BLACK, Color.BLACK, edge.isDirected()),
					GraphicAttributeConstants.GRAPHICS);
			ega = (EdgeGraphicAttribute) edge.getAttribute(GraphicAttributeConstants.GRAPHICS);
		}
		if (ega != null)
			ega.setArrowtail(knownShapeClassName);
	}
	
	/**
	 * Removes a single attribute or a number of attributes from the
	 * Attributable. In case the name ends with "*", all attributes with a name,
	 * which starts with the same string, will be removed. Otherwise a single
	 * attribute (if available) with the exact name as given, is removed.
	 * 
	 * @param attributable
	 * @param path
	 * @param name
	 *           A name of a single attribute "test" or a group of attributes
	 *           "test*" (test1, test2, testA, testABC, ...).
	 * @return True, if the removal was successful. Fals, if a error has occured,
	 *         which is added to the system log.
	 */
	public static boolean deleteAttribute(Attributable attributable, String path, String name) {
		try {
			HashMapAttribute a = (HashMapAttribute) getAttribute(attributable, path);
			if (name.endsWith("*")) {
				name = name.substring(0, name.length() - "*".length());
				HashSet<String> del = new HashSet<String>();
				for (String attname : a.getCollection().keySet()) {
					if (attname.startsWith(name))
						del.add(attname);
				}
				for (String d : del)
					a.remove(d);
			} else
				a.remove(name);
			return true;
		} catch (AttributeNotFoundException anf) {
			//
			return false;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return false;
		}
	}
	
	public static void setLabelColor(int index, Node n, Color color) {
		String idx = "" + index;
		if (index < 0)
			idx = "";
		try {
			HashMapAttribute l = (HashMapAttribute) n.getAttribute(NodeLabelAttribute.LABEL_ATTRIBUTE_PATH + "."
					+ NodeLabelAttribute.LABELGRAPHICS + idx);
			StringAttribute o = (StringAttribute) l.getAttribute("color");
			o.setString(ColorUtil.getHexFromColor(color));
		} catch (AttributeNotFoundException e) {
			setLabel(n, getLabel(n, ""));
			HashMapAttribute l = (HashMapAttribute) n.getAttribute(NodeLabelAttribute.LABEL_ATTRIBUTE_PATH + "."
					+ NodeLabelAttribute.LABELGRAPHICS + idx);
			StringAttribute o = (StringAttribute) l.getAttribute("color");
			o.setString(ColorUtil.getHexFromColor(color));
		}
	}
	
	public static Color getLabelColor(Node n) {
		try {
			HashMapAttribute l = (HashMapAttribute) n.getAttribute(NodeLabelAttribute.LABEL_ATTRIBUTE_PATH + "."
					+ NodeLabelAttribute.LABELGRAPHICS);
			StringAttribute o = (StringAttribute) l.getAttribute("color");
			return ColorUtil.getColorFromHex(o.getString());
		} catch (Exception e) {
			return Color.black;
		}
	}
	
	public static void addEdgeBend(Edge edge, Vector2d point) {
		addEdgeBend(edge, point.x, point.y);
	}
	
	@SuppressWarnings("unchecked")
	public static void addEdgeBends(Edge edge, Collection<Vector2d> points) {
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			for (Vector2d point : points) {
				int cnt = ega.getBendCount() + 1;
				ega.getBends().add(new CoordinateAttribute("bend" + cnt, point.x, point.y), true);
			}
		} catch (Exception e) {
			((LinkedHashMapAttribute) edge.getAttribute("graphics.bends")).setCollection(new HashMap());
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			for (Vector2d point : points) {
				int cnt = ega.getBendCount() + 1;
				ega.getBends().add(new CoordinateAttribute("bend" + cnt, point.x, point.y), true);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static CoordinateAttribute addEdgeBend(Edge edge, double x, double y) {
		CoordinateAttribute result = null;
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			int cnt = ega.getBendCount() + 1;
			boolean added = false;
			while (!added) {
				try {
					Attribute a = ega.getBends().getAttribute("bend" + cnt);
					if (a != null) {
						// empty (avoid unused warning)
					}
					cnt++;
				} catch (AttributeNotFoundException nfe) {
					result = new CoordinateAttribute("bend" + cnt, x, y);
					ega.getBends().add(result, true);
					added = true;
				}
			}
		} catch (Exception e) {
			((LinkedHashMapAttribute) edge.getAttribute("graphics.bends")).setCollection(new HashMap());
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			result = new CoordinateAttribute("bend1", x, y);
			ega.getBends().add(result, true);
		}
		return result;
	}
	
	public static CoordinateAttribute addEdgeBend(Edge edge, double x, double y, boolean safeAdd) {
		if (safeAdd) {
			for (Vector2d bend : getEdgeBends(edge)) {
				if (bend.distance(x, y) < 20)
					return null;
			}
			return addEdgeBend(edge, x, y);
		} else
			return addEdgeBend(edge, x, y);
	}
	
	public static ArrayList<Vector2d> getEdgeBends(Edge edge) {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			for (Attribute a : ega.getBends().getCollection().values()) {
				CoordinateAttribute ca = (CoordinateAttribute) a;
				result.add(new Vector2d(ca.getX(), ca.getY()));
			}
		} catch (AttributeNotFoundException e) {
			// no bends
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	public static ArrayList<CoordinateAttribute> getEdgeBendCoordinateAttributes(Edge edge) {
		ArrayList<CoordinateAttribute> result = new ArrayList<CoordinateAttribute>();
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			for (Attribute a : ega.getBends().getCollection().values()) {
				CoordinateAttribute ca = (CoordinateAttribute) a;
				result.add(ca);
			}
		} catch (Exception e) {
			// ingore here
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static void removeEdgeBends(Edge edge) {
		try {
			((LinkedHashMapAttribute) edge.getAttribute("graphics.bends")).setCollection(new HashMap());
		} catch (AttributeNotFoundException nfe) {
			// empty
		}
	}
	
	private static HashMap<String, String> edge_shapes = initializeEdgeShapes();
	public static String preFilePath = "filepath|";
	
	public static void setEdgeBendStyle(Edge edge, String shape) {
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			boolean found = false;
			for (String knownShape : edge_shapes.values()) {
				if (knownShape.toUpperCase().indexOf(shape.toUpperCase()) >= 0) {
					ega.setShape(knownShape);
					found = true;
					break;
				}
			}
			if (!found)
				ega.setShape(shape);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static HashMap<String, String> getEdgeShapes() {
		return edge_shapes;
	}
	
	public static boolean addEdgeShape(String description, String className) {
		if (edge_shapes.containsKey(description))
			return false;
		else {
			edge_shapes.put(description, className);
			return true;
		}
	}
	
	private static HashMap<String, String> initializeEdgeShapes() {
		HashMap<String, String> standardShapes = new HashMap<String, String>();
		standardShapes.put("Straight Line", "org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
		standardShapes.put("Segmented Line", "org.graffiti.plugins.views.defaults.PolyLineEdgeShape");
		standardShapes.put("Quadratic Spline", "org.graffiti.plugins.views.defaults.QuadCurveEdgeShape");
		standardShapes.put("Smooth Line", "org.graffiti.plugins.views.defaults.SmoothLineEdgeShape");
		return standardShapes;
	}
	
	public static String getEdgeBendStyle(Edge edge) {
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			return ega.getShape();
		} catch (Exception e) {
			return "org.graffiti.plugins.views.defaults.PolyLineEdgeShape";
		}
	}
	
	public static ArrayList<String> getWebPageContent(String urlText) {
		ArrayList<String> result = new ArrayList<String>();
		try {
			urlText = getEncodedUrl(urlText);
			// Create a URL for the desired page
			URL url = new URL(urlText);
			
			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				// str is one line of text; readLine() strips the newline
				// character(s)
				result.add(str);
			}
			in.close();
		} catch (MalformedURLException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		}
		return result;
	}
	
	public static String getNiceEdgeOrNodeLabel(GraphElement ge, String nodeLabelIfMissing) {
		if (ge instanceof Edge) {
			Edge e = (Edge) ge;
			return AttributeHelper.getLabel(e,
					AttributeHelper.getLabel(e.getSource(), nodeLabelIfMissing) + (e.isDirected() ? "->" : "--")
							+ AttributeHelper.getLabel(e.getTarget(), nodeLabelIfMissing));
		} else
			return getLabel(ge, nodeLabelIfMissing);
	}
	
	public static String getSaveAttributeName(String attributeName) {
		if (attributeName == null)
			return null;
		return StringManipulationTools.stringReplace(attributeName, " ", "_");
	}
	
	/**
	 * If setDefault is true, values will be initilized, saved and returned as 0.
	 * Behaviour may be different between different getPosition* methods. (AAA)
	 * 
	 * @param node
	 * @param setDefault
	 * @return Position vector (3D)
	 */
	public static Vector3d getPositionVec3d(Node node, boolean setDefault) {
		try {
			CoordinateAttribute coA = (CoordinateAttribute) node.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Point2D r = coA.getCoordinate();
			double z = getPositionZ(node, setDefault);
			return new Vector3d(r.getX(), r.getY(), z);
		} catch (Exception ex) {
			if (setDefault) {
				setPosition(node, 0, 0);
				setPositionZ(node, 0);
			}
			return new Vector3d(0, 0, 0);
		}
	}
	
	public static Vector3d getPositionVec3d(Node node, double zReturnIfNotAvailable, boolean setDefaultZ) {
		try {
			CoordinateAttribute coA = (CoordinateAttribute) node.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Point2D r = coA.getCoordinate();
			double z = getPositionZ(node, zReturnIfNotAvailable, setDefaultZ);
			return new Vector3d(r.getX(), r.getY(), z);
		} catch (Exception ex) {
		}
		return new Vector3d(Double.NaN, Double.NaN, Double.NaN);
	}
	
	public static double getPositionZ(Node node, boolean setDefault) {
		double z = (Double) getAttributeValue(node, "graphics", "z_", new Double(0), new Double(0), setDefault);
		return z;
	}
	
	public static double getPositionZ(Node node, double defaultReturn, boolean setDefault) {
		double z = (Double) getAttributeValue(node, "graphics", "z_", defaultReturn, new Double(0), setDefault);
		return z;
	}
	
	public static void setPositionZ(Node node, double z) {
		setAttribute(node, "graphics", "z_", z);
	}
	
	public static double getDepth(Node node, double defaultReturn, boolean setDefault) {
		double z = (Double) getAttributeValue(node, "graphics", "depth", defaultReturn, new Double(0), setDefault);
		return z;
	}
	
	public static void setDepth(Node node, double depth) {
		setAttribute(node, "graphics", "depth", depth);
	}
	
	public static boolean isHiddenGraphElement(GraphElement ge) {
		if (ge == null)
			return true;
		
		if (ge instanceof Node)
			return getWidth((Node) ge) < 0;
		
		if (ge instanceof Edge)
			return getFrameThickNess(ge) < 0;
		
		return false;
	}
	
	public static void switchVisibilityOfChildElements(Collection<Node> nodes) {
		if (nodes == null || nodes.size() <= 0)
			return;
		
		Node firstNode = nodes.iterator().next();
		try {
			firstNode.getGraph().getListenerManager().transactionStarted(nodes);
			
			LinkedHashSet<GraphElement> childElements = new LinkedHashSet<GraphElement>();
			for (Node node : nodes)
				getChildElements(node, childElements);
			boolean modeSet = false;
			boolean doHide = false;
			for (GraphElement ge : childElements) {
				if (!modeSet) {
					doHide = !isHiddenGraphElement(ge);
					modeSet = true;
				}
				if (modeSet) {
					setHidden(doHide, ge);
				}
			}
		} finally {
			firstNode.getGraph().getListenerManager().transactionFinished(nodes);
		}
	}
	
	public static void setVisibilityOfChildElements(Collection<Node> nodes, boolean doHide) {
		LinkedHashSet<GraphElement> childElements = new LinkedHashSet<GraphElement>();
		for (Node node : nodes)
			getChildElements(node, childElements);
		for (GraphElement ge : childElements) {
			setHidden(doHide, ge);
		}
	}
	
	/**
	 * Return nodes and edges, traceable from the given node. Does not include
	 * the given node in the result set. Directionality of edge directions is
	 * taken into account if the edges are directed.
	 * 
	 * @param node
	 *           start node
	 * @param result
	 *           nodes and edges, traceable from the given node
	 */
	private static void getChildElements(Node node, Set<GraphElement> result) {
		if (node != null && result != null) {
			result.addAll(node.getUndirectedEdges());
			result.addAll(node.getDirectedOutEdges());
			for (Node n : node.getUndirectedNeighbors()) {
				if (result.contains(n))
					continue;
				result.add(n);
				getChildElements(n, result);
			}
			for (Node n : node.getOutNeighbors()) {
				if (result.contains(n))
					continue;
				result.add(n);
				getChildElements(n, result);
			}
		}
	}
	
	public static void setHidden(boolean doHide, GraphElement ge) {
		if (ge == null)
			return;
		
		if (ge instanceof Node) {
			if (doHide)
				setWidth((Node) ge, -Math.abs(getWidth((Node) ge)));
			else
				setWidth((Node) ge, Math.abs(getWidth((Node) ge)));
			return;
		}
		if (ge instanceof Edge) {
			if (doHide)
				setFrameThickNess(ge, -Math.abs(getFrameThickNess(ge)));
			else
				setFrameThickNess(ge, Math.abs(getFrameThickNess(ge)));
		}
	}
	
	public static void setHidden(boolean doHide, Node n, boolean processOutEdges, boolean processInEdges,
			boolean processUndirEdges) {
		setHidden(doHide, n);
		if (processOutEdges)
			for (Edge e : n.getAllOutEdges())
				setHidden(doHide, e);
		if (processInEdges)
			for (Edge e : n.getAllInEdges())
				setHidden(doHide, e);
		if (processUndirEdges)
			for (Edge e : n.getUndirectedEdges())
				setHidden(doHide, e);
	}
	
	public static void setHidden(Collection<GraphElement> graphElements, boolean doHide) {
		for (GraphElement ge : graphElements)
			setHidden(doHide, ge);
	}
	
	public static void setHidden(Set<GraphElement> graphElements, boolean doHide) {
		for (GraphElement ge : graphElements)
			setHidden(doHide, ge);
	}
	
	public static String getStringList(String[] elements, String div) {
		if (elements == null || elements.length <= 0)
			return "";
		else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < elements.length; i++) {
				sb.append(elements[i]);
				if (i < elements.length - 1)
					sb.append(div);
			}
			return sb.toString();
		}
	}
	
	public static String getStringList(File[] elements, String div) {
		if (elements == null || elements.length <= 0)
			return "";
		else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < elements.length; i++) {
				sb.append(elements[i].getName());
				if (i < elements.length - 1)
					sb.append(div);
			}
			return sb.toString();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String getStringList(Collection elements, String div) {
		ArrayList<Object> al = new ArrayList<Object>();
		for (Object o : elements)
			al.add(o);
		return StringManipulationTools.getStringList(al, div);
	}
	
	/**
	 * @deprecated Use {@link StringManipulationTools#getStringList(ArrayList,String)} instead
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static String getStringList(ArrayList elements, String div) {
		return StringManipulationTools.getStringList(elements, div);
	}
	
	public static void setReferenceURL(Attributable ge, String url) {
		AttributeHelper.setAttribute(ge, "", "url", url);
	}
	
	public static String getReferenceURL(Attributable ge) {
		return (String) AttributeHelper.getAttributeValue(ge, "", "url", null, "");
	}
	
	public static void setPathwayReference(GraphElement ge, String url) {
		setAttribute(ge, "", "pathway_ref_url", preFilePath + url);
	}
	
	public static void setPathwayReference(Attributable a, int idx, String url) {
		setAttribute(a, "", "pathway_ref_url" + idx, preFilePath + url);
	}
	
	public static void removePathwayReferences(Attributable a, boolean includeNonIndexed) {
		if (includeNonIndexed)
			deleteAttribute(a, "", "pathway_ref_url");
		deleteAttribute(a, "", "pathway_ref_url*");
	}
	
	public static String getPathwayReference(Attributable a) {
		String ref = (String) getAttributeValue(a, "", "pathway_ref_url", null, "");
		if (ref != null && ref.startsWith(preFilePath))
			return ref.substring(preFilePath.length());
		return ref;
	}
	
	public static ArrayList<String> getPathwayReferences(Attributable a, boolean includeOnlyIndexed) {
		ArrayList<String> result = new ArrayList<String>();
		Map<String, Attribute> rr = a.getAttributes().getCollection();
		for (Entry<String, Attribute> e : rr.entrySet()) {
			if (!e.getKey().startsWith("pathway_ref_url"))
				continue;
			if ((!includeOnlyIndexed) || (includeOnlyIndexed && e.getKey().length() > "pathway_ref_url".length())) {
				result.add((String) e.getValue().getValue());
			}
		}
		return result;
	}
	
	public static double getHeatMapLowerBound(Graph graph) {
		return ((Double) getAttributeValue(graph, "", "hm_lower_bound", new Double(-2), new Double(-2))).doubleValue();
	}
	
	public static double getHeatMapMiddleBound(Graph graph) {
		return ((Double) getAttributeValue(graph, "", "hm_middle_bound", new Double(0d), new Double(0))).doubleValue();
	}
	
	public static double getHeatMapUpperBound(Graph graph) {
		return ((Double) getAttributeValue(graph, "", "hm_upper_bound", new Double(2d), new Double(2d))).doubleValue();
	}
	
	public static double getHeatMapGamma(Graph graph) {
		return ((Double) getAttributeValue(graph, "", "hm_gamma", new Double(1d), new Double(1))).doubleValue();
	}
	
	public static void moveGraph(Graph graph, int x, int y) {
		if (graph == null || graph.getNumberOfNodes() <= 0)
			return;
		for (Node n : graph.getNodes()) {
			Vector2d pos = getPositionVec2d(n);
			pos.x += x;
			pos.y += y;
			setPosition(n, pos);
		}
		for (Edge e : graph.getEdges()) {
			for (CoordinateAttribute ca : getEdgeBendCoordinateAttributes(e)) {
				ca.setCoordinate(ca.getX() + x, ca.getY() + y);
			}
		}
	}
	
	public static boolean linuxRunning() {
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		if (os != null && os.toUpperCase().contains("LINUX")) {
			return true;
		} else
			if (os != null && os.toUpperCase().contains("UNIX")) {
				return true;
			} else
				return false;
	}
	
	public static boolean windowsRunning() {
		Properties p = System.getProperties();
		String os = (String) p.get("os.name");
		if (os != null && os.toUpperCase().contains("WINDOWS")) {
			return true;
		} else
			return false;
	}
	
	public static String getShapeClassFromShapeName(String s) {
		if (s.equals("oval"))
			return "org.graffiti.plugins.views.defaults.EllipseNodeShape";
		if (s.equals("circle"))
			return "org.graffiti.plugins.views.defaults.CircleNodeShape";
		if (s.equals("rectangle"))
			return "org.graffiti.plugins.views.defaults.RectangleNodeShape";
		if (s.equals("diamond"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.DiamondShape";
		if (s.equals("tag"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagRightShape";
		if (s.equals("tagl"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagLeftShape";
		if (s.equals("tagu"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagUpShape";
		if (s.equals("tagd"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TagDownShape";
		if (s.equals("observable"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ObservableShape";
		if (s.equals("pertubation"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.PertubationShape";
		if (s.equals("complex"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ComplexShape";
		if (s.equals("skewrectr"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.RightSkewedRectShape";
		if (s.equals("skewrectl"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.LeftSkewedRectShape";
		if (s.equals("receptord"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ReceptorDownShape";
		if (s.equals("receptoru"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ReceptorUpShape";
		if (s.equals("receptorl"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ReceptorLeftShape";
		if (s.equals("receptorr"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.ReceptorRightShape";
		if (s.equals("nucleic"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.NucleicAcidFeatureShape";
		if (s.equals("truncprotein"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TruncProteinShape";
		if (s.equals("sourcesink"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.SourceSinkShape";
		if (s.equals("transition"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.TransitionShape";
		if (s.equals("multinucleic"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiNucleicAcidFeatureShape";
		if (s.equals("multirectangle"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiRectangleShape";
		if (s.equals("mulitoval"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiEllipseShape";
		if (s.equals("doubleoval"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.DoubleEllipseShape";
		if (s.equals("paper"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.PaperShape";
		if (s.equals("multicomplex"))
			return "de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes.MultiComplexShape";
		
		return s;
	}
	
	public static String[] getShapeDescritions() {
		return new String[] { "Rectangle", "Circle", "Ellipse", "Rhombus", "Tag (right)", "Tag (left)", "Tag (up)",
				"Tag (down)", "Observable", "Pertubation", "Complex", "Skewed Rect (right)", "Skewed Rect (left)",
				"Receptor (down)", "Receptor (up)", "Receptor (left)", "Receptor (right)", "Nucleic Acid Feature",
				"Truncated Protein", "Source or Sink", "Transition", "Multi Nucleic Acid Feature", "Multi Rectangle",
				"Multi Oval", "Double Oval", "Paper", "Multi Complex" };
	}
	
	public static String getShapeClassFromDescription(String desc) {
		int i = 0;
		for (String s : getShapeDescritions()) {
			if (s.equals(desc))
				return getShapeClasses()[i];
			i++;
		}
		return null;
	}
	
	public static String[] getShapeClasses() {
		return new String[] { "rectangle", "circle", "oval", "diamond", "tag", "tagl", "tagu", "tagd", "observable",
				"pertubation", "complex", "skewrectr", "skewrectl", "receptord", "receptoru", "receptorl", "receptorr",
				"nucleic", "truncprotein", "sourcesink", "transition", "multinucleic", "multirectangle", "mulitoval",
				"doubleoval", "paper", "multicomplex" };
	}
	
	public static String getLabelConsumption(Edge e, String returnIfNull) {
		try {
			Attribute a = e.getAttribute("srcLabel");
			if (a instanceof EdgeLabelAttribute) {
				String lbl = ((EdgeLabelAttribute) a).getLabel();
				if (lbl == null)
					return returnIfNull;
				else
					return lbl;
			} else
				return returnIfNull;
		} catch (Exception err) {
			return returnIfNull;
		}
	}
	
	public static String getLabelProduction(Edge e, String returnIfNull) {
		try {
			Attribute a = e.getAttribute("tgtLabel");
			if (a instanceof EdgeLabelAttribute) {
				String lbl = ((EdgeLabelAttribute) a).getLabel();
				if (lbl == null)
					return returnIfNull;
				else
					return lbl;
			} else
				return returnIfNull;
		} catch (Exception err) {
			return returnIfNull;
		}
	}
	
	public static void setLabelConsumption(Edge e, String srcLabel) {
		Attribute a = null;
		try {
			a = e.getAttribute("srcLabel");
		} catch (Exception err) {
			// empty
		}
		if (a != null && (srcLabel == null || srcLabel.length() <= 0)) {
			e.removeAttribute("srcLabel");
			return;
		}
		if (srcLabel != null && srcLabel.length() > 0) {
			EdgeLabelAttribute ls = null;
			if (a == null || !(a instanceof EdgeLabelAttribute)) {
				if (a != null)
					e.removeAttribute("srcLabel");
				a = new EdgeLabelAttribute("srcLabel");
				ls = (EdgeLabelAttribute) a;
				ls.setFontStyle("box");
				EdgeLabelPositionAttribute posS = new EdgeLabelPositionAttribute("position", 0.333, 0, 0, -8);
				ls.add(posS);
				e.addAttribute(a, "");
			} else
				ls = (EdgeLabelAttribute) a;
			if (srcLabel != null)
				ls.setLabel(srcLabel);
		}
	}
	
	public static void setLabelProduction(Edge e, String tgtLabel) {
		Attribute a = null;
		try {
			a = e.getAttribute("tgtLabel");
		} catch (Exception err) {
			// empty
		}
		if (a != null && (tgtLabel == null || tgtLabel.length() <= 0)) {
			e.removeAttribute("tgtLabel");
			return;
		}
		if (tgtLabel != null && tgtLabel.length() > 0) {
			EdgeLabelAttribute ls = null;
			if (a == null || !(a instanceof EdgeLabelAttribute)) {
				if (a != null)
					e.removeAttribute("tgtLabel");
				a = new EdgeLabelAttribute("tgtLabel");
				ls = (EdgeLabelAttribute) a;
				ls.setFontStyle("box");
				EdgeLabelPositionAttribute posS = new EdgeLabelPositionAttribute("position", 0.666, 0, 0, -8);
				ls.add(posS);
				e.addAttribute(a, "");
			} else
				ls = (EdgeLabelAttribute) a;
			ls.setLabel(tgtLabel);
		}
	}
	
	public static Rectangle2D.Double getNodeRectangle(Node node) {
		Rectangle2D.Double result = new Rectangle2D.Double();
		Vector2d left = AttributeHelper.getPositionVec2d(node);
		Vector2d size = AttributeHelper.getSize(node);
		left.x -= size.x / 2d;
		left.y -= size.y / 2d;
		result.setFrame(left.x, left.y, size.x, size.y);
		return result;
	}
	
	public static Rectangle getNodeRectangleAWT(Node node) {
		Rectangle result = new Rectangle();
		Vector2d left = AttributeHelper.getPositionVec2d(node);
		Vector2d size = AttributeHelper.getSize(node);
		left.x -= size.x / 2d;
		left.y -= size.y / 2d;
		result.setFrame(left.x, left.y, size.x, size.y);
		return result;
	}
	
	public static Color getColorFromAttribute(Attributable attr, String path, String name, Color defaultValue) {
		return ColorUtil.getColorFromHex((String) getAttributeValue(attr, path, name,
				ColorUtil.getHexFromColor(defaultValue), "", false));
	}
	
	public static void setColorFromAttribute(Attributable attr, String path, String name, Color value) {
		setAttribute(attr, path, name, ColorUtil.getHexFromColor(value));
		
	}
	
	public static Date getDateFromString(String value) {
		try {
			return new SimpleDateFormat().parse(value);
		} catch (Exception e) {
			// System.err.println("Not a valid date: " + value);
			return new Date();
		}
	}
	
	@Deprecated
	public static String getMD5fromFile(String filename) throws Exception {
		File f = new File(filename);
		return getMD5fromFile(f);
	}
	
	@Deprecated
	public static String getMD5fromFile(File f) throws Exception {
		return getMD5fromInputStream(new FileInputStream(f), null);
	}
	
	@Deprecated
	public static String getMD5fromInputStream(InputStream is) throws Exception {
		return getMD5fromInputStream(is, null);
	}
	
	@Deprecated
	public static String getMD5fromInputStream(InputStream is, ObjectRef optFileSize) throws Exception {
		if (is == null)
			return null;
		String res = null;
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] buffer = new byte[1024 * 1024];
		int read = 0;
		long len = 0;
		try {
			while ((read = is.read(buffer)) > 0) {
				len += read;
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			res = output;
			if (optFileSize != null)
				optFileSize.addLong(len);
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		return res;
	}
	
	/**
	 * If date is null, the current date is used!
	 */
	public static String getDateString(Date date) {
		if (date == null) {
			date = new Date();
		}
		return new SimpleDateFormat().format(date);
	}
	
	public static HashSet<String> getFuzzyLabels(String label) {
		if (label == null)
			return new HashSet<String>();
		else {
			HashSet<String> lbls = new HashSet<String>();
			lbls.add(label);
			lbls.add(label.trim());
			lbls.add(StringManipulationTools.removeHTMLtags(label));
			lbls.add(StringManipulationTools.removeHTMLtags(StringManipulationTools.stringReplace(label, "<br>", " ")));
			return lbls;
		}
	}
	
	public static void showInFileBrowser(String folder, String fileName) throws Exception {
		String fn = File.separator + fileName;
		if (fileName == null)
			fn = "";
		if (SystemAnalysis.isWindowsRunning()) {
			// explorer.exe /select,"file" ==> select in file explorer
			Runtime r = Runtime.getRuntime();
			fileName = StringManipulationTools.stringReplace(fileName, " ", " ");
			r.exec(System.getenv("windir") + "\\explorer.exe /select,\"" + folder + fn + "\"");
			// r.exec(new String[] { System.getenv("windir") + "\\explorer.exe", "/select,\"" + fileName + "\"" },
			// null, new File(folder));
		} else {
			if (SystemAnalysis.isMacRunning()) {
				// open -R ==> reveal in Finder
				Runtime r = Runtime.getRuntime();
				String p = folder + fn;
				r.exec(new String[] { "open", "-R", p }, null, null);
			} else {
				System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Open folder, containing file " + fn);
				showInBrowser(folder);// + fn);
			}
		}
	}
	
	public static void showInBrowser(IOurl url) {
		showInBrowser(url + "");
	}
}
