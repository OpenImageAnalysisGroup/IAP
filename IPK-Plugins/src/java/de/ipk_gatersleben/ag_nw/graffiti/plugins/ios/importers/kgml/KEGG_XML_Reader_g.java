/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kgml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.DockingAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.plugin.io.AbstractInputSerializer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author Christian Klukas
 *         (edited version without VANTED class dependencies)
 *         (c) 2004-2006 IPK-Gatersleben
 */
public class KEGG_XML_Reader_g extends AbstractInputSerializer {
	
	private String fileNameExt = ".kgml";
	
	public KEGG_XML_Reader_g() {
		super();
	}
	
	public void read(String filename, Graph g) throws IOException {
		super.read(filename, g);
	}
	
	public void read(InputStream in, Graph g) throws IOException {
		Graph myGraph = g;
		InputStream inpStream = in;
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc;
			try {
				doc = builder.build(inpStream);
				Element kegg = doc.getRootElement();
				readNetwork(myGraph, kegg);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (inpStream != null)
				inpStream.close();
		}
	}
	
	private void processAttributes(Attributable attr, Element kegg,
						String... xmlAttributeNames) {
		for (int i = 0; i < xmlAttributeNames.length; i++)
			processAttribute(attr, kegg, xmlAttributeNames[i], "kegg_"
								+ xmlAttributeNames[i]);
	}
	
	private void processAttribute(Attributable attr, Element kegg,
						String xmlAttributeName, String gmlAttributeName) {
		if (kegg.getAttribute(xmlAttributeName) != null) {
			String attributeValue = kegg.getAttributeValue(xmlAttributeName);
			attributeValue = attributeValue.replaceAll("&", "&amp;");
			setAttribute(attr, "kegg", gmlAttributeName, attributeValue);
		}
	}
	
	private String attributeSeparator = String.valueOf(Attribute.SEPARATOR);
	
	private boolean hasAttribute(Attributable n, String attributeName) {
		try {
			Attribute attr = n.getAttribute(attributeName);
			return attr != null;
		} catch (AttributeNotFoundException err) {
			return false;
		}
	}
	
	private void addAttributeFolder(Attributable attributeable, String path) {
		CollectionAttribute nc = new HashMapAttribute(path);
		attributeable.addAttribute(nc, "");
	}
	
	private Attribute getAttribute(Attributable attributable,
						String attributeName) {
		return attributable.getAttribute(attributeName);
	}
	
	private void setAttribute(Attributable attributable, String path,
						String attributeName, Object attributeValue) {
		if (!hasAttribute(attributable, path)) {
			addAttributeFolder(attributable, path);
		}
		HashMapAttribute a = (HashMapAttribute) getAttribute(attributable, path);
		if (attributeValue instanceof Attribute) {
			try {
				Attribute b = a.getAttribute(path + attributeSeparator
									+ attributeName);
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
		if (attributeValue instanceof Byte) {
			attributable.setByte(path + attributeSeparator + attributeName,
								((Byte) attributeValue).byteValue());
			return;
		}
		if (attributeValue instanceof Double) {
			if (((Double) attributeValue).doubleValue() == Double.NaN)
				return;
			attributable.setDouble(path + attributeSeparator + attributeName,
								((Double) attributeValue).doubleValue());
			return;
		}
		if (attributeValue instanceof Float) {
			if (((Float) attributeValue).floatValue() == Float.NaN)
				return;
			attributable.setFloat(path + attributeSeparator + attributeName,
								((Float) attributeValue).floatValue());
			return;
		}
		if (attributeValue instanceof Integer) {
			if (((Integer) attributeValue).intValue() == Integer.MAX_VALUE)
				return;
			attributable.setInteger(path + attributeSeparator + attributeName,
								((Integer) attributeValue).intValue());
			return;
		}
		if (attributeValue instanceof Long) {
			if (((Long) attributeValue).intValue() == Long.MAX_VALUE)
				return;
			attributable.setLong(path + attributeSeparator + attributeName,
								((Long) attributeValue).longValue());
			return;
		}
		if (attributeValue instanceof Short) {
			if (((Short) attributeValue).shortValue() == Short.MAX_VALUE)
				return;
			attributable.setShort(path + attributeSeparator + attributeName,
								((Short) attributeValue).shortValue());
			return;
		}
		if (attributeValue instanceof String) {
			attributable.setString(path + attributeSeparator + attributeName,
								(String) attributeValue);
			return;
		}
		org.graffiti.attributes.ObjectAttribute myNewAttribute;
		try {
			myNewAttribute = (org.graffiti.attributes.ObjectAttribute) a
								.getAttribute(attributeName);
		} catch (AttributeNotFoundException e) {
			myNewAttribute = new org.graffiti.attributes.ObjectAttribute(
								attributeName);
			a.add(myNewAttribute, true);
		} catch (ClassCastException cce) {
			a.remove(attributeName);
			myNewAttribute = new org.graffiti.attributes.ObjectAttribute(
								attributeName);
			a.add(myNewAttribute, true);
		}
		myNewAttribute.setValue(attributeValue);
	}
	
	private void readEntryNodes(Graph myGraph, Element kegg,
						HashMap<Integer, Node> mapID2GraphNode,
						HashMap<String, ArrayList<Node>> entryName2GraphNode) {
		List<?> entries = kegg.getChildren("entry");
		for (int i = 0; i < entries.size(); i++) {
			Element entry = (Element) entries.get(i);
			Element graphicsEntry = entry.getChild("graphics");
			Node newNode = myGraph.addNode();
			int mapid = Integer.parseInt(entry.getAttributeValue("id"));
			mapID2GraphNode.put(new Integer(mapid), newNode);
			setDefaultGraphicsAttribute(newNode, -1, -1);
			processAttributes(newNode, entry, "type", "type", "link", "reaction",
								"map");
			setLabel(newNode, entry.getAttributeValue("name"));
			if (graphicsEntry != null)
				processGraphicsEntry(newNode, graphicsEntry);
			String key;
			if (entry.getAttribute("reaction") != null) {
				key = entry.getAttributeValue("reaction");
			} else {
				key = entry.getAttributeValue("name");
			}
			String[] keys = key.split(" ");
			for (String key2 : keys) {
				if (!entryName2GraphNode.containsKey(key2))
					entryName2GraphNode.put(key2, new ArrayList<Node>());
				ArrayList<Node> currentList = entryName2GraphNode.get(key2);
				currentList.add(newNode);
			}
			if (entry.getAttributeValue("type").equalsIgnoreCase("map")) {
				setFillColor(newNode, Color.WHITE);
				StringAttribute ua = new StringAttribute("kegg_map_link", entry
									.getAttributeValue("name"));
				newNode.addAttribute(ua, "kegg");
				String title = getLabel(newNode, "");
				if (title.startsWith("TITLE:")) {
					setLabel(newNode, title.substring("TITLE:".length())
										.toUpperCase());
					setFillColor(newNode, Color.WHITE);
					setBorderWidth(newNode, 5d);
				} else {
					setBorderWidth(newNode, 0.5d);
				}
			} else {
				processAttributes(newNode, entry, "name");
				setBorderWidth(newNode, 1d);
			}
		}
	}
	
	private CollectionAttribute getDefaultGraphicsAttributeForEdge(
						Color colArrow, Color colLine, boolean directed) {
		CollectionAttribute col = new HashMapAttribute("");
		EdgeGraphicAttribute graphics = getNewEdgeGraphicsAttribute(colArrow,
							colLine, directed);
		col.add(graphics, false);
		return col;
	}
	
	private void setBorderWidth(Node node, double frameThickness) {
		NodeGraphicAttribute na = (NodeGraphicAttribute) node
							.getAttribute(GraphicAttributeConstants.GRAPHICS);
		na.setFrameThickness(frameThickness);
	}
	
	private EdgeGraphicAttribute getNewEdgeGraphicsAttribute(Color colArrow,
						Color colLine, boolean directed) {
		EdgeGraphicAttribute graphics = new EdgeGraphicAttribute();
		DockingAttribute dock = graphics.getDocking();
		dock.setSource("");
		dock.setTarget("");
		graphics.setThickness(1);
		graphics.setFrameThickness(1);
		graphics.getFramecolor().setColor(colLine);
		graphics.getFillcolor().setColor(colArrow);
		graphics.setShape("org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
		if (directed) {
			graphics.setArrowhead("org.graffiti.plugins.views.defaults.StandardArrowShape");
		}
		graphics.getLineMode().setDashArray(null);
		graphics.getLineMode().setDashPhase(0.0f);
		return graphics;
	}
	
	private String getLabel(Attributable node, String defaultReturn) {
		try {
			LabelAttribute labelAttr;
			
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (LabelAttribute) node
									.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
				return labelAttr.getLabel();
			} else {
				return defaultReturn;
			}
		} catch (Exception ex) {
			return defaultReturn;
		}
	}
	
	private void setDefaultGraphicsAttribute(Node node, double x, double y) {
		setNodeGraphicsAttribute(x, y, 3, 20, 20,
							new Color(0, 0, 0, 255), new Color(0, 255, 255, 255),
							node.getAttributes());
	}
	
	private void setFillColor(Attributable attributable, Color color) {
		try {
			ColorAttribute colorAtt = null;
			
			colorAtt = (ColorAttribute) attributable
								.getAttribute(GraphicAttributeConstants.FILLCOLOR_PATH);
			colorAtt.setColor(color);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setNodeGraphicsAttribute(double posx, double posy,
						double frameThickness_3, double width_25, double height_25,
						Color frameColor_0_0_0_255, Color fillColor_0_100_250_100,
						CollectionAttribute col) {
		NodeGraphicAttribute graphics = new NodeGraphicAttribute();
		CoordinateAttribute cooAtt = graphics.getCoordinate();
		Point2D pos = new Point2D.Double();
		pos.setLocation(posx, posy);
		cooAtt.setCoordinate(pos);
		col.add(graphics, false);
		
		graphics.setFrameThickness(frameThickness_3);
		double height = height_25;
		double width = width_25;
		graphics.getDimension().setDimension(
							new Dimension((int) java.lang.Math.round(height),
												(int) java.lang.Math.round(width)));
		graphics.getFramecolor().setColor(frameColor_0_0_0_255);
		graphics.getFillcolor().setColor(fillColor_0_100_250_100);
		graphics.setShape("org.graffiti.plugins.views.defaults.RectangleNodeShape");
	}
	
	private void setLabel(Node node, String label) {
		if (label == null) {
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				NodeLabelAttribute labelAttr;
				labelAttr = (NodeLabelAttribute) node
									.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
				labelAttr.getParent().remove(labelAttr);
			}
			return;
		}
		try {
			LabelAttribute labelAttr;
			if (hasAttribute(node, GraphicAttributeConstants.LABELGRAPHICS)) {
				labelAttr = (LabelAttribute) node
									.getAttribute(GraphicAttributeConstants.LABELGRAPHICS);
			} else {
				labelAttr = new NodeLabelAttribute(
									GraphicAttributeConstants.LABELGRAPHICS, label);
				node.addAttribute(labelAttr,
									GraphicAttributeConstants.LABEL_ATTRIBUTE_PATH);
			}
			labelAttr.setLabel(label);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void processGraphicsEntry(Node newNode, Element xmlGraphicsEntry) {
		if (xmlGraphicsEntry.getAttributeValue("name") != null) {
			setLabel(newNode, xmlGraphicsEntry.getAttributeValue("name"));
		}
		NodeGraphicAttribute na = (NodeGraphicAttribute) newNode
							.getAttribute(GraphicAttributeConstants.GRAPHICS);
		if (xmlGraphicsEntry.getAttributeValue("type") != null) {
			String type = xmlGraphicsEntry.getAttributeValue("type");
			if (type.equalsIgnoreCase("rectangle"))
				na.setShape(GraphicAttributeConstants.RECTANGLE_CLASSNAME);
			else
				if (type.equalsIgnoreCase("circle")) {
					na.setShape(GraphicAttributeConstants.ELLIPSE_CLASSNAME);
					setLabel(newNode, xmlGraphicsEntry.getAttributeValue("name"));
				} else
					if (type.equalsIgnoreCase("roundrectangle"))
						na.setShape(GraphicAttributeConstants.RECTANGLE_CLASSNAME);
					else
						na.setShape(GraphicAttributeConstants.RECTANGLE_CLASSNAME);
		} else
			na.setShape(GraphicAttributeConstants.RECTANGLE_CLASSNAME);
		double x = Double.parseDouble(xmlGraphicsEntry.getAttributeValue("x"));
		double y = Double.parseDouble(xmlGraphicsEntry.getAttributeValue("y"));
		setFillColorHEX(newNode, xmlGraphicsEntry.getAttributeValue("bgcolor"));
		setOutlineColorHEX(newNode, xmlGraphicsEntry.getAttributeValue("fgcolor"));
		double sx;
		if (xmlGraphicsEntry.getAttribute("width") != null)
			sx = Double.parseDouble(xmlGraphicsEntry.getAttributeValue("width"));
		else
			sx = 45;
		double sy;
		if (xmlGraphicsEntry.getAttribute("height") != null)
			sy = Double.parseDouble(xmlGraphicsEntry.getAttributeValue("height"));
		else
			sy = 17;
		
		setPosition(newNode, x, y);
		setSize(newNode, sx, sy);
	}
	
	private void setPosition(Node n, double x, double y) {
		try {
			CoordinateAttribute cn = (CoordinateAttribute) n
								.getAttribute(GraphicAttributeConstants.COORD_PATH);
			
			Point2D p = new Point2D.Double(x, y);
			cn.setCoordinate(p);
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void setSize(Node myNode, double width, double height) {
		try {
			DoubleAttribute da = (DoubleAttribute) myNode
								.getAttribute(GraphicAttributeConstants.DIMW_PATH);
			
			da.setDouble(width);
			
			da = (DoubleAttribute) myNode
								.getAttribute(GraphicAttributeConstants.DIMH_PATH);
			da.setDouble(height);
		} catch (Exception ex) {
		}
	}
	
	private void setFillColorHEX(Attributable attr, String hexStr) {
		if (hexStr.startsWith("#") && hexStr.length() == "#FFFFFF".length()) {
			hexStr = hexStr.substring(1);
			BigInteger biR = new BigInteger(hexStr.substring(0, 2), 16);
			BigInteger biG = new BigInteger(hexStr.substring(2, 4), 16);
			BigInteger biB = new BigInteger(hexStr.substring(4, 6), 16);
			Color c = new Color(biR.intValue(), biG.intValue(), biB.intValue());
			setFillColor(attr, c);
		}
	}
	
	private void readNetwork(Graph myGraph, Element kegg) {
		processAttributes(myGraph, kegg, "name", "org", "number", "title",
							"image", "link");
		HashMap<Integer, Node> mapID2GraphNode = new HashMap<Integer, Node>();
		HashMap<String, ArrayList<Node>> entryName2GraphNode = new HashMap<String, ArrayList<Node>>();
		readEntryNodes(myGraph, kegg, mapID2GraphNode, entryName2GraphNode);
		myGraph.numberGraphElements();
		ArrayList<Edge> reactionEdges = readReactionNodes(myGraph, kegg,
							entryName2GraphNode);
		readRelationNodes(myGraph, kegg, mapID2GraphNode, reactionEdges);
	}
	
	private void setOutlineColorHEX(Attributable attr, String hexStr) {
		if (hexStr.startsWith("#") && hexStr.length() == "#FFFFFF".length()) {
			hexStr = hexStr.substring(1);
			BigInteger biR = new BigInteger(hexStr.substring(0, 2), 16);
			BigInteger biG = new BigInteger(hexStr.substring(2, 4), 16);
			BigInteger biB = new BigInteger(hexStr.substring(4, 6), 16);
			Color c = new Color(biR.intValue(), biG.intValue(), biB.intValue());
			setOutlineColor(attr, c);
		}
	}
	
	private void setOutlineColor(Attributable attributable, Color color) {
		try {
			ColorAttribute colorAtt = null;
			if (hasAttribute(attributable, GraphicAttributeConstants.FRAMECOLOR)) {
				colorAtt = (ColorAttribute) attributable
									.getAttribute(GraphicAttributeConstants.FRAMECOLOR);
			} else {
				colorAtt = (ColorAttribute) attributable
									.getAttribute(GraphicAttributeConstants.OUTLINE_PATH);
			}
			
			colorAtt.setColor(color);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private Node getNearestNode(Node reactionNode, ArrayList<Node> nodeList) {
		if (nodeList == null)
			return null;
		
		if (nodeList.size() == 1)
			return nodeList.iterator().next();
		
		double distance = Double.MAX_VALUE;
		Node resultNode = null;
		Vector2d posA = getPositionVec2d(reactionNode);
		for (Node testNode : nodeList) {
			Vector2d posB = getPositionVec2d(testNode);
			double currDist = Math.sqrt((posA.x - posB.x) * (posA.x - posB.x)
								+ (posA.y - posB.y) * (posA.y - posB.y));
			if (currDist < distance) {
				resultNode = testNode;
				distance = currDist;
			}
		}
		return resultNode;
	}
	
	private Vector2d getPositionVec2d(Node a) {
		try {
			CoordinateAttribute coA = (CoordinateAttribute) a
								.getAttribute(GraphicAttributeConstants.COORD_PATH);
			Point2D r = coA.getCoordinate();
			return new Vector2d(r.getX(), r.getY());
		} catch (Exception ex) {
		}
		return new Vector2d(Double.NaN, Double.NaN);
	}
	
	private Edge addEdgeIfNotExistant(Graph graph, Node nodeA, Node nodeB,
						boolean directed, CollectionAttribute graphicsAttributeForEdge) {
		return graph.addEdge(nodeA, nodeB, directed, graphicsAttributeForEdge);
	}
	
	private void readRelationNodes(Graph myGraph, Element kegg,
						HashMap<Integer, Node> mapID2GraphNode, ArrayList<Edge> reactionEdges) {
		List<?> entries = kegg.getChildren("relation");
		for (int i = 0; i < entries.size(); i++) {
			Element entry = (Element) entries.get(i);
			int entry1 = Integer.parseInt(entry.getAttributeValue("entry1"));
			int entry2 = Integer.parseInt(entry.getAttributeValue("entry2"));
			Node e1 = mapID2GraphNode.get(new Integer(entry1));
			Node e2 = mapID2GraphNode.get(new Integer(entry2));
			if (e1 == null || e2 == null)
				continue;
			ArrayList<NodePair> edgesToBeAdded = new ArrayList<NodePair>();
			String resultLabel = "";
			for (Object subTypeObj : entry.getChildren("subtype")) {
				Element subType = (Element) subTypeObj;
				if (subType.getAttributeValue("name").equalsIgnoreCase(
									"hidden compound")) {
					subType.getAttributeValue("value");
					addEdgeIfNotExistant(myGraph, e1, e2, false,
										getDefaultGraphicsAttributeForEdge(Color.BLACK,
															Color.BLACK, false));
				} else
					if (subType.getAttributeValue("name").equalsIgnoreCase(
										"compound")) {
						int compID = Integer
											.parseInt(subType.getAttributeValue("value"));
						Node compNode = mapID2GraphNode.get(new Integer(compID));
						edgesToBeAdded.add(new NodePair(e1, compNode));
						
						NodePair np = new NodePair(compNode, e2);
						edgesToBeAdded.add(np);
					} else {
						String val = subType.getAttributeValue("value");
						if (resultLabel.length() > 0)
							resultLabel = resultLabel + ", " + val;
						else
							resultLabel = val;
					}
			}
			for (NodePair np : edgesToBeAdded) {
				Edge newEdge = null;
				if (!((entry.getAttribute("type") != null) && (entry
									.getAttributeValue("type").equalsIgnoreCase("maplink"))))
					for (Edge re : reactionEdges) {
						if ((re.getSource() == np.a && re.getTarget() == np.b)
											|| (re.getSource() == np.b && re.getTarget() == np.a)) {
							newEdge = re;
							break;
						}
					}
				if (newEdge == null)
					newEdge = addEdgeIfNotExistant(myGraph, np.a, np.b, false,
										getDefaultGraphicsAttributeForEdge(Color.BLACK,
															Color.BLACK, false));
				processAttributes(newEdge, entry, "type");
				isMapLink(newEdge);
				if (resultLabel.length() > 0)
					setAttribute(newEdge, "kegg", "kegg_subtype_values", resultLabel);
			}
		}
	}
	
	private String getKeggType(GraphElement ge, String resultIfNotAvailable) {
		return (String) getAttributeValue(ge, "kegg", "kegg_type",
							resultIfNotAvailable);
	}
	
	private Object getAttributeValue(Attributable attributable, String path,
						String attributeName, Object defaultValue) {
		
		try {
			HashMapAttribute a = (HashMapAttribute) getAttribute(attributable,
								path);
			Object res = a.getAttribute(attributeName).getValue();
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return defaultValue;
		}
	}
	
	private boolean isMapLink(Edge e) {
		String keggType = getKeggType(e, null);
		if (keggType != null && keggType.equalsIgnoreCase("maplink"))
			return true;
		else
			return false;
	}
	
	private ArrayList<Edge> readReactionNodes(Graph myGraph, Element kegg,
						HashMap<String, ArrayList<Node>> entryName2GraphNode) {
		ArrayList<Edge> reactionEdges = new ArrayList<Edge>();
		List<?> entries = kegg.getChildren("reaction");
		for (int i = 0; i < entries.size(); i++) {
			Element entry = (Element) entries.get(i);
			
			String reactionName = entry.getAttributeValue("name");
			String reactionType = entry.getAttributeValue("type");
			ArrayList<Node> reactionNodeList = entryName2GraphNode
								.get(reactionName);
			if (reactionNodeList == null || reactionNodeList.size() == 0)
				continue;
			for (Node reactionNode : reactionNodeList) {
				setAttribute(reactionNode, "kegg", "kegg_link_reaction",
									new String(
														"http://www.genome.jp/dbget-bin/www_bget?reaction+"
																			+ reactionName.substring("rn:".length())));
				processAttribute(reactionNode, entry, "type", "kegg_reaction_type");
				reactionType
									.equalsIgnoreCase("irreversible");
				boolean reversible = reactionType.equalsIgnoreCase("reversible");
				Color arrowColor = Color.BLACK;
				List<?> substrates = entry.getChildren("substrate");
				for (int si = 0; si < substrates.size(); si++) {
					Element substrate = (Element) substrates.get(si);
					String substrateName = substrate.getAttributeValue("name");
					Node substrateNode = getNearestNode(reactionNode,
										entryName2GraphNode.get(substrateName));
					if (substrateNode == null) {
					} else {
						Edge e;
						if (reversible)
							e = addEdgeIfNotExistant(myGraph, reactionNode,
												substrateNode, true,
												getDefaultGraphicsAttributeForEdge(arrowColor,
																	arrowColor, true));
						else
							e = addEdgeIfNotExistant(myGraph, substrateNode,
												reactionNode, false,
												getDefaultGraphicsAttributeForEdge(arrowColor,
																	arrowColor, false));
						reactionEdges.add(e);
						setAttribute(e, "kegg", "kegg_link_reaction", new String(
											"http://www.genome.jp/dbget-bin/www_bget?reaction+"
																+ reactionName.substring("rn:".length())));
						processAttribute(e, entry, "type", "kegg_reaction_type");
						
					}
				}
				arrowColor = Color.BLACK;
				List<?> products = entry.getChildren("product");
				for (int pi = 0; pi < products.size(); pi++) {
					Element product = (Element) products.get(pi);
					String productName = product.getAttributeValue("name");
					Node productNode = getNearestNode(reactionNode,
										entryName2GraphNode.get(productName));
					if (productNode == null) {
					} else {
						Edge e;
						if (reversible)
							e = addEdgeIfNotExistant(myGraph, reactionNode,
												productNode, true,
												getDefaultGraphicsAttributeForEdge(arrowColor,
																	arrowColor, true));
						else
							e = addEdgeIfNotExistant(myGraph, reactionNode,
												productNode, true,
												getDefaultGraphicsAttributeForEdge(arrowColor,
																	arrowColor, true));
						reactionEdges.add(e);
						setAttribute(e, "kegg", "kegg_link_reaction", new String(
											"http://www.genome.jp/dbget-bin/www_bget?reaction+"
																+ reactionName.substring("rn:".length())));
						processAttribute(e, entry, "type", "kegg_reaction_type");
					}
				}
			}
		}
		return reactionEdges;
	}
	
	public String[] getExtensions() {
		return new String[] { fileNameExt };
	}
	
	public String[] getFileTypeDescriptions() {
		return new String[] { "KGML File" };
	}
	
	public void read(Reader in, Graph g) throws Exception {
		Graph myGraph = g;
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc;
			try {
				doc = builder.build(in);
				Element kegg = doc.getRootElement();
				readNetwork(myGraph, kegg);
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (in != null)
				in.close();
		}
	}
}
