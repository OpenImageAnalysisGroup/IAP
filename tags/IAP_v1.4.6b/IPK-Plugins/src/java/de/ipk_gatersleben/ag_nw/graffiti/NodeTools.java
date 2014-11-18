/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.AttributeHelper;
import org.Vector2d;
import org.Vector3d;
import org.color.ColorUtil;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.w3c.dom.NodeList;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_label_color.LabelColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.font_settings.FontAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.MyComparableDataPoint;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.XPathHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.NodeCacheEntry;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NodeTools {
	
	public static Vector2d getMaximumXY(Collection nodeList, double factorXY, double minx, double miny,
			boolean includeSizeInformation) {
		return getMaximumXY(nodeList, factorXY, minx, miny, includeSizeInformation, false);
	}
	
	public static Vector2d getMaximumXY(Collection nodeList, double factorXY, double minx, double miny,
			boolean includeSizeInformation, boolean includeInvisibleNodes) {
		
		double maxx = 0, maxy = 0;
		
		Iterator nodeIterator = nodeList.iterator();
		while (nodeIterator.hasNext()) {
			Object nodeOrPatternStruct = nodeIterator.next();
			double x, y;
			double sx = 0, sy = 0;
			Node currentNode = (Node) nodeOrPatternStruct;
			if (!includeInvisibleNodes) {
				if (AttributeHelper.isHiddenGraphElement(currentNode))
					continue;
			}
			x = (int) AttributeHelper.getPositionX(currentNode);
			y = (int) AttributeHelper.getPositionY(currentNode);
			if (includeSizeInformation) {
				Vector2d sz = AttributeHelper.getSize(currentNode);
				sx = sz.x;
				sy = sz.y;
				x += sx / 2;
				y += sy / 2;
			}
			if (x > maxx)
				maxx = x;
			if (y > maxy)
				maxy = y;
		}
		return new Vector2d((maxx * factorXY - minx), (maxy * factorXY - miny));
	}
	
	public static Vector2d getMinimumXY(Collection nodeList, double factorXY, double subx, double suby,
			boolean includeSizeInformation) {
		return getMinimumXY(nodeList, factorXY, subx, suby, includeSizeInformation, false);
	}
	
	public static Vector2d getMinimumXY(Collection nodeList, double factorXY, double subx, double suby,
			boolean includeSizeInformation, boolean includeInvisibleNodes) {
		
		double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE;
		
		Iterator nodeIterator = nodeList.iterator();
		while (nodeIterator.hasNext()) {
			Object nodeOrPatternStruct = nodeIterator.next();
			double x, y;
			double sx = 0, sy = 0;
			Node currentNode = (Node) nodeOrPatternStruct;
			if (!includeInvisibleNodes) {
				if (AttributeHelper.isHiddenGraphElement(currentNode))
					continue;
			}
			x = (int) AttributeHelper.getPositionX(currentNode);
			y = (int) AttributeHelper.getPositionY(currentNode);
			if (includeSizeInformation) {
				Vector2d sz = AttributeHelper.getSize(currentNode);
				sx = (int) sz.x / 2;
				sy = (int) sz.y / 2;
				x -= sx;
				y -= sy;
			}
			if (x < minx)
				minx = x;
			if (y < miny)
				miny = y;
		}
		return new Vector2d((minx * factorXY - subx), (miny * factorXY - suby));
	}
	
	/**
	 * Calculates the center of a number of given nodes (other Graph elements are
	 * ignored). If no nodes are given the position vector contains Double.NaN
	 * values.
	 * 
	 * @author Christian Klukas
	 * @param nodeList
	 *           Node collection to work with. The collection has to contain
	 *           either <code>Node</code> objects, or <code>NodeCacheEntry</code> objects.
	 * @return A <code>Vector2d</code> containing the position of the center.
	 */
	public static Vector2d getCenter(Collection nodeList) {
		double x = 0;
		double y = 0;
		
		Iterator nodeIterator = nodeList.iterator();
		int numberOfSelectedNodes = 0;
		while (nodeIterator.hasNext()) {
			Object nodeOrPatternStruct = nodeIterator.next();
			if (nodeOrPatternStruct instanceof Node) {
				Node currentNode = (Node) nodeOrPatternStruct;
				x += AttributeHelper.getPositionX(currentNode);
				y += AttributeHelper.getPositionY(currentNode);
				numberOfSelectedNodes++;
			}
			if (nodeOrPatternStruct instanceof NodeCacheEntry) {
				NodeCacheEntry currentNode = (NodeCacheEntry) nodeOrPatternStruct;
				x += currentNode.position.x;
				y += currentNode.position.y;
				numberOfSelectedNodes++;
			}
		}
		if (numberOfSelectedNodes != 0) {
			x = x / numberOfSelectedNodes;
			y = y / numberOfSelectedNodes;
		} else {
			x = Double.NaN;
			y = Double.NaN;
		}
		return new Vector2d(x, y);
	}
	
	public static Vector3d getCenter3d(Collection nodeList) {
		double x = 0;
		double y = 0;
		double z = 0;
		
		Iterator nodeIterator = nodeList.iterator();
		int numberOfSelectedNodes = 0;
		while (nodeIterator.hasNext()) {
			Object nodeOrPatternStruct = nodeIterator.next();
			if (nodeOrPatternStruct instanceof Node) {
				Node currentNode = (Node) nodeOrPatternStruct;
				x += AttributeHelper.getPositionX(currentNode);
				y += AttributeHelper.getPositionY(currentNode);
				z += AttributeHelper.getPositionZ(currentNode, false);
				numberOfSelectedNodes++;
			}
			if (nodeOrPatternStruct instanceof NodeCacheEntry) {
				NodeCacheEntry currentNode = (NodeCacheEntry) nodeOrPatternStruct;
				x += currentNode.position.x;
				y += currentNode.position.y;
				z += 0;
				numberOfSelectedNodes++;
			}
		}
		if (numberOfSelectedNodes != 0) {
			x = x / numberOfSelectedNodes;
			y = y / numberOfSelectedNodes;
			z = z / numberOfSelectedNodes;
		} else {
			x = Double.NaN;
			y = Double.NaN;
			z = Double.NaN;
		}
		return new Vector3d(x, y, z);
	}
	
	// /**
	// * Use this method to rotate a given collection of nodes around a point,
	// given by the
	// * vector <code>rC</code>. The angle is given by the value <code>a</code>.
	// * After the rotation operation around the given rotation basis point a
	// translation can be applied with
	// * the parameter <code>t</code>.
	// * This method uses matricies calculation in order to get good speed. (one
	// step calculation of the
	// * node movement)
	// * WARNING: Ihave not tested yet, if it is possible to rotate around a
	// point and at the same time
	// * apply a movement vector (<code>t</code>).
	// * @author Christian Klukas
	// * @param nodes
	// * @param alphaRotation Angle for rotation
	// * @param rotationCenter Center of rotation
	// * @param translation At last a translation can be applied.
	// */
	// public static void transformNodes(Collection nodes, double a, Vector2d rC,
	// Vector2d t) {
	// double calcArray[][] = new double[3][3];
	// calcArray[0][0] = Math.cos(a);
	// calcArray[0][1] = -Math.sin(a);
	// calcArray[0][2] = -rC.x * Math.cos(a) + rC.y * Math.sin(a) + rC.x + t.x;
	//
	// calcArray[1][0] = Math.sin(a);
	// calcArray[1][1] = Math.cos(a);
	// calcArray[1][2] = -rC.x * Math.sin(a) - rC.y * Math.cos(a) + rC.y + t.y;
	//
	// calcArray[2][0] = 0;
	// calcArray[2][1] = 0;
	// calcArray[2][2] = 1;
	//
	// // Matrix calcM = new Matrix(calcArray);
	// // Matrix nodeM = new Matrix(3, 1);
	// // nodeM.set(2, 0, 1);
	// //
	// // Iterator nodeIterator = nodes.iterator();
	// // while (nodeIterator.hasNext()) {
	// // Node currentNode = (Node) nodeIterator.next();
	// // double x = AttributeHelper.getPositionX(currentNode);
	// // double y = AttributeHelper.getPositionY(currentNode);
	// // nodeM.set(0, 0, x);
	// // nodeM.set(1, 0, y);
	// // Matrix result = calcM.times(nodeM);
	// // double newX = result.get(0, 0);
	// // double newY = result.get(1, 0);
	// // AttributeHelper.setPosition(currentNode, newX, newY);
	// // }
	// }
	
	// /**
	// *
	// * @param p Given
	// * @param a Alpha rotation
	// * @param rC Rotation centre
	// * @param t Translation
	// * @return The coordinates of the translated/rotated given coordinates in
	// <code>p</code>
	// */
	// public static Vector2d rotateTranslatePoint(Vector2d p, double a,
	// Vector2d rC, Vector2d t) {
	// double calcArray[][] = new double[3][3];
	// calcArray[0][0] = Math.cos(a);
	// calcArray[0][1] = -Math.sin(a);
	// calcArray[0][2] = -rC.x * Math.cos(a) + rC.y * Math.sin(a) + rC.x + t.x;
	//
	// calcArray[1][0] = Math.sin(a);
	// calcArray[1][1] = Math.cos(a);
	// calcArray[1][2] = -rC.x * Math.sin(a) - rC.y * Math.cos(a) + rC.y + t.y;
	//
	// calcArray[2][0] = 0;
	// calcArray[2][1] = 0;
	// calcArray[2][2] = 1;
	//
	// Matrix calcM = new Matrix(calcArray);
	// Matrix nodeM = new Matrix(3, 1);
	// nodeM.set(2, 0, 1);
	//
	// nodeM.set(0, 0, p.x);
	// nodeM.set(1, 0, p.y);
	// Matrix result = calcM.times(nodeM);
	// double newX = result.get(0, 0);
	// double newY = result.get(1, 0);
	// return new Vector2d(newX, newY);
	// }
	
	public static String getClusterID(GraphElement node, String idIfNoCluster) {
		try {
			return ((String) AttributeHelper.getAttributeValue(node, "cluster", "cluster", idIfNoCluster, null, false));
		} catch (ClassCastException cce) {
			// convert possible older integer id to string id
			Integer oldval = (Integer) AttributeHelper
					.getAttributeValue(node, "cluster", "cluster", null, new Integer(-1));
			if (oldval != null) {
				((CollectionAttribute) node.getAttribute("cluster")).remove("cluster");
				setClusterID(node, oldval.toString());
				return oldval.toString();
			} else
				throw new RuntimeException("Cluster ID in wrong format, coversion failed.");
		}
	}
	
	public static void setClusterID(GraphElement node, String clusterId) {
		if (clusterId != null)
			AttributeHelper.setAttribute(node, "cluster", "cluster", clusterId);
		else
			AttributeHelper.deleteAttribute(node, "cluster", "cluster");
	}
	
	public static List<MyComparableDataPoint> getDataTimePoints(GraphElement n, boolean useSampleAverage) {
		List<SubstanceInterface> mappings = getMappedDataListFromNode(n);
		List<MyComparableDataPoint> mappedData = new ArrayList<MyComparableDataPoint>();
		List<MyComparableDataPoint> result = new ArrayList<MyComparableDataPoint>();
		if (mappings != null) {
			for (SubstanceInterface mapping : mappings) {
				if (useSampleAverage)
					mappedData.addAll(getSortedAverageDataSetValues(mapping, null));
				else
					mappedData.addAll(getSortedDataSetValues(mapping, null));
			}
			
			result.addAll(mappedData);
		}
		return result;
	}
	
	public static List<SubstanceInterface> getMappedDataListFromNode(GraphElement graphElement) {
		return Experiment2GraphHelper.getMappedDataListFromGraphElement(graphElement);
	}
	
	/**
	 * Get a list of <code>MyComparableDataPoint</code> entries from a
	 * xmldata-node.
	 * 
	 * @param xmldata
	 *           A xml data node or a specific xml series data node.
	 * @param optTreatmentFilter
	 * @return A list of <code>MyComparableDataPoint</code> entries.
	 */
	public static List<MyComparableDataPoint> getSortedAverageDataSetValues(SubstanceInterface xmldata, String optTreatmentFilter) {
		return getSortedAverageOrDataSetValues(xmldata, "average", optTreatmentFilter);
	}
	
	public static List<MyComparableDataPoint> getSortedAverageDataSetValues(SubstanceInterface xmldata, boolean removeEmptyConditions) {
		return getSortedAverageOrDataSetValues(xmldata, "average", removeEmptyConditions);
	}
	
	private static List<MyComparableDataPoint> getSortedAverageOrDataSetValues(SubstanceInterface xmldata,
			String avgOrDataIdentifier, boolean removeEmptyConditions) {
		boolean returnAvgValues = avgOrDataIdentifier.equals("average");
		ArrayList<MyComparableDataPoint> ss = new ArrayList<MyComparableDataPoint>();
		ss.addAll(xmldata.getDataPoints(returnAvgValues, removeEmptyConditions));
		return ss;
	}
	
	private static List<MyComparableDataPoint> getSortedAverageOrDataSetValues(SubstanceInterface xmldata,
			String avgOrDataIdentifier, String optTreatmentFilter) {
		
		boolean returnAvgValues = avgOrDataIdentifier.equals("average");
		
		ArrayList<MyComparableDataPoint> ss = new ArrayList<MyComparableDataPoint>();
		
		ss.addAll(xmldata.getDataPoints(returnAvgValues, optTreatmentFilter));
		
		return ss;
	}
	
	public static ArrayList<org.w3c.dom.Node> getSortedLines(NodeList lines) {
		ArrayList<org.w3c.dom.Node> result = new ArrayList<org.w3c.dom.Node>();
		for (int i = 0; i < lines.getLength(); i++) {
			if (lines.item(i).getNodeName().equals("line"))
				result.add(lines.item(i));
		}
		Collections.sort(result, new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				org.w3c.dom.Node a = (org.w3c.dom.Node) o1;
				org.w3c.dom.Node b = (org.w3c.dom.Node) o2;
				String id1 = XPathHelper.getSeriesIDforLine(a);
				String id2 = XPathHelper.getSeriesIDforLine(b);
				if (id1 == null)
					id1 = "";
				if (id2 == null)
					id2 = "";
				try {
					Integer i_id1 = Integer.parseInt(id1);
					Integer i_id2 = Integer.parseInt(id2);
					return i_id1.compareTo(i_id2);
				} catch (NumberFormatException nfe) {
					return id1.compareTo(id2);
				}
			}
		});
		return result;
	}
	
	/**
	 * @param xmldata
	 *           A xml data mapping node or a specific xml line data node
	 * @param optTreatmentFilter
	 * @return
	 */
	public static List<MyComparableDataPoint> getSortedDataSetValues(SubstanceInterface xmldata,
			String optTreatmentFilter) {
		return getSortedAverageOrDataSetValues(xmldata, "data", optTreatmentFilter);
	}
	
	// public synchronized static void setNodeComponentType(GraphElement ge, int
	// chartType0123456) {
	// if (chartType0123456==0)
	// NodeTools.setNodeComponentType(ge, XMLAttribute.nodeTypeChart_hide);
	// else
	// if (chartType0123456==1)
	// NodeTools.setNodeComponentType(ge,
	// XMLAttribute.nodeTypeChart2D_type1_line);
	// else
	// if (chartType0123456==2)
	// NodeTools.setNodeComponentType(ge,
	// XMLAttribute.nodeTypeChart2D_type2_bar);
	// else
	// if (chartType0123456==3)
	// NodeTools.setNodeComponentType(ge,
	// XMLAttribute.nodeTypeChart2D_type3_bar_flat);
	// else
	// if (chartType0123456==4)
	// NodeTools.setNodeComponentType(ge,
	// XMLAttribute.nodeTypeChart2D_type4_pie);
	// else
	// if (chartType0123456==5)
	// NodeTools.setNodeComponentType(ge,
	// XMLAttribute.nodeTypeChart2D_type5_pie3d);
	// else
	// if (chartType0123456==6)
	// NodeTools.setNodeComponentType(ge,
	// XMLAttribute.nodeTypeChart2D_type6_heatmap);
	// else
	// if (chartType0123456==-1)
	// NodeTools.setNodeComponentType(ge, XMLAttribute.nodeTypeChart_auto);
	// else
	// ErrorMsg.addErrorMessage("Internal Error: Invalid Charttype Set: Valid is only type 0-6!");
	// }
	
	public synchronized static void setNodeComponentType(GraphElement ge, String nodeType) {
		String path = GraphicAttributeConstants.GRAPHICS;
		if (!AttributeHelper.hasAttribute(ge, path)) {
			AttributeHelper.addAttributeFolder(ge, path);
		}
		HashMapAttribute a = (HashMapAttribute) AttributeHelper.getAttribute(ge, path);
		
		Attribute ea = a.getCollection().get("component"); // existing attribute;
		if (ea != null)
			ea.setValue(nodeType);
		else {
			ChartAttribute ca = new ChartAttribute("component", nodeType);
			a.add(ca, false);
		}
		// AttributeHelper.setAttribute(graphNode, "graphics", "component",
		// nodeType);
	}
	
	public static String getNodeComponentType(Node node) {
		return (String) AttributeHelper.getAttributeValue(node, GraphicAttributeConstants.GRAPHICS, "component",
				new String("unknown"), null);
	}
	
	public static Font getLabelFont(Node node) {
		return getFontFromAttribute(node, "nodefont");
	}
	
	private static Font getFontFromAttribute(GraphElement ge, String attributeID) {
		Font result;
		try {
			result = ((FontAttribute) ge.getAttribute(attributeID)).getFont();
			// labelColor =
			// ((FontAttribute)node.getAttribute("nodefont")).getColor();
		} catch (AttributeNotFoundException e) {
			ge.addAttribute(new FontAttribute(attributeID), "");
			result = ((FontAttribute) ge.getAttribute(attributeID)).getFont();
			// labelColor =
			// ((FontAttribute)node.getAttribute("nodefont")).getColor();
		}
		return result;
	}
	
	public static Color getLabelColor(Node node) {
		return getColorFromAttribute(node, "nodefont");
	}
	
	private static Color getColorFromAttribute(GraphElement ge, String attributeID) {
		Color result;
		try {
			result = ((FontAttribute) ge.getAttribute(attributeID)).getColor();
		} catch (AttributeNotFoundException e) {
			ge.addAttribute(new FontAttribute(attributeID), "");
			result = ((FontAttribute) ge.getAttribute(attributeID)).getColor();
		}
		return result;
	}
	
	public static Font getChartTitleFont(GraphElement ge) {
		return getFontFromAttribute(ge, "charttitlefont");
	}
	
	public static Paint getChartTitleColor(GraphElement ge) {
		return getColorFromAttribute(ge, "charttitlefont");
	}
	
	public static Color getChartBackgroundColor(GraphElement ge) {
		return getChartBackgroundColor(ge, "");
	}
	
	public static Color getChartBackgroundColor(GraphElement ge, int idx) {
		return getChartBackgroundColor(ge, idx + "");
	}
	
	private static Color getChartBackgroundColor(GraphElement ge, String add) {
		String cbcp = "charting" + Attribute.SEPARATOR + GraphicAttributeConstants.CHARTBACKGROUNDCOLOR + add;
		try {
			LabelColorAttribute colorAtt = null;
			String old_cbcp = "charting" + Attribute.SEPARATOR + "chart_background_color" + add;
			try {
				Attribute a = ge.getAttribute(old_cbcp);
				if (a != null && a instanceof HashMapAttribute) {
					HashMapAttribute ha = (HashMapAttribute) a;
					a.getParent().remove(a.getId());
					Attribute colAtt = StringAttribute.getTypedStringAttribute(
							GraphicAttributeConstants.CHARTBACKGROUNDCOLOR + add, ColorUtil
									.getHexFromColor(getColorFromHashMapAttribute(ha)));
					ge.addAttribute(colAtt, "charting");
				} else {
					a.getParent().remove(a.getId());
				}
			} catch (AttributeNotFoundException err) {
				//
			}
			
			colorAtt = (LabelColorAttribute) ge.getAttribute(cbcp);
			Color resultCol = colorAtt.getColor();
			if (resultCol.getRed() == 0 && resultCol.getGreen() == 0 && resultCol.getBlue() == 0)
				return null;
			else
				return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.CHARTBACKGROUNDCOLOR
					+ add, ColorUtil.getHexFromColor(Color.BLACK));
			ge.addAttribute(newAtt, "charting");
			return null;
		}
	}
	
	public static Color getGridColor(Graph g, Color defaultColor) {
		String cbcp = GraphicAttributeConstants.GRIDCOLOR;
		try {
			LabelColorAttribute colorAtt = (LabelColorAttribute) g.getAttribute(cbcp);
			Color resultCol = colorAtt.getColor();
			return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.GRIDCOLOR, ColorUtil
					.getHexFromColor(defaultColor));
			g.addAttribute(newAtt, "");
			return defaultColor;
		}
	}
	
	public static Color getAxisColor(Graph g, Color defaultColor) {
		String cbcp = GraphicAttributeConstants.AXISCOLOR;
		try {
			LabelColorAttribute colorAtt = (LabelColorAttribute) g.getAttribute(cbcp);
			Color resultCol = colorAtt.getColor();
			return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.AXISCOLOR, ColorUtil
					.getHexFromColor(defaultColor));
			g.addAttribute(newAtt, "");
			return defaultColor;
		}
	}
	
	private static Color getColorFromHashMapAttribute(HashMapAttribute a) {
		IntegerAttribute ir = (IntegerAttribute) a.getAttribute("red");
		IntegerAttribute ig = (IntegerAttribute) a.getAttribute("green");
		IntegerAttribute ib = (IntegerAttribute) a.getAttribute("blue");
		return new Color(ir.getInteger(), ig.getInteger(), ib.getInteger());
	}
	
	public static Color getCategoryBackgroundColorA(Graph g, Color defaultColor) {
		String cbcp = GraphicAttributeConstants.CATEGORY_BACKGROUND_A;
		try {
			LabelColorAttribute colorAtt = (LabelColorAttribute) g.getAttribute(cbcp);
			Color resultCol = colorAtt.getColor();
			return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.CATEGORY_BACKGROUND_A,
					ColorUtil.getHexFromColor(defaultColor));
			g.addAttribute(newAtt, "");
			return defaultColor;
		}
	}
	
	public static Color getCategoryBackgroundColorB(Graph g, Color defaultColor) {
		String cbcp = GraphicAttributeConstants.CATEGORY_BACKGROUND_B;
		try {
			LabelColorAttribute colorAtt = (LabelColorAttribute) g.getAttribute(cbcp);
			Color resultCol = colorAtt.getColor();
			return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.CATEGORY_BACKGROUND_B,
					ColorUtil.getHexFromColor(defaultColor));
			g.addAttribute(newAtt, "");
			return defaultColor;
		}
	}
	
	public static Color getCategoryBackgroundColorC(Graph g, Color defaultColor) {
		String cbcp = GraphicAttributeConstants.CATEGORY_BACKGROUND_C;
		try {
			LabelColorAttribute colorAtt = (LabelColorAttribute) g.getAttribute(cbcp);
			Color resultCol = colorAtt.getColor();
			return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(GraphicAttributeConstants.CATEGORY_BACKGROUND_C,
					ColorUtil.getHexFromColor(defaultColor));
			g.addAttribute(newAtt, "");
			return defaultColor;
		}
	}
	
	public static Color getColorAttributeValue(Graph g, String attName, Color defaultColor) {
		try {
			LabelColorAttribute colorAtt = (LabelColorAttribute) g.getAttribute(attName);
			Color resultCol = colorAtt.getColor();
			return resultCol;
		} catch (AttributeNotFoundException e) {
			Attribute newAtt = StringAttribute.getTypedStringAttribute(attName, ColorUtil.getHexFromColor(defaultColor));
			g.addAttribute(newAtt, "");
			return defaultColor;
		}
	}
	
	public static void setCenter(List<Node> nodeList, Vector2d center) {
		Vector2d currentCenter = getCenter(nodeList);
		double offx = center.x - currentCenter.x;
		double offy = center.y - currentCenter.y;
		for (Node n : nodeList) {
			Vector2d pos = AttributeHelper.getPositionVec2d(n);
			AttributeHelper.setPosition(n, pos.x + offx, pos.y + offy);
			for (Edge e : n.getAllOutEdges()) {
				ArrayList<CoordinateAttribute> cl = AttributeHelper.getEdgeBendCoordinateAttributes(e);
				if (cl != null)
					for (CoordinateAttribute ca : cl) {
						ca.setCoordinate(ca.getX() + offx, ca.getY() + offy);
					}
			}
		}
		
	}
}