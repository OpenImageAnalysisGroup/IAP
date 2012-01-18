/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 13.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.AttributeHelper;
import org.ErrorMsg;
import org.HelperClass;
import org.Vector2d;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.views.defaults.CircleNodeShape;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.RelationType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.kgmlGraphicsType;

public class KeggGmlHelper implements HelperClass {
	
	public static void setKeggId(Graph graph, String id) {
		AttributeHelper.setAttribute(graph, "", "kegg_name", id);
	}
	
	public static void setKeggId(Node n, String id) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_name", id);
	}
	
	public static void setKeggId(Node n, String id, int index) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_name" + index, id);
	}
	
	public static String getKeggId(Graph graph) {
		return (String) AttributeHelper.getAttributeValue(graph, "", "kegg_name", null, "");
	}
	
	public static String getKeggId(Node n) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_name", null, "");
	}
	
	public static ArrayList<IndexAndString> getKeggIds(Node n) {
		return getIndexAndStringValuesFromHashMapAttribute(n, "kegg", "kegg_name");
	}
	
	public static void setKeggOrg(Graph graph, String org) {
		AttributeHelper.setAttribute(graph, "", "kegg_org", org);
	}
	
	public static String getKeggOrg(Graph graph) {
		return (String) AttributeHelper.getAttributeValue(graph, "", "kegg_org", null, "");
	}
	
	public static void setKeggMapNumber(Graph graph, String number) {
		AttributeHelper.setAttribute(graph, "", "kegg_number", number);
	}
	
	public static String getKeggMapNumber(Graph graph) {
		return (String) AttributeHelper.getAttributeValue(graph, "", "kegg_number", null, "");
	}
	
	public static void setKeggTitle(Graph graph, String title) {
		AttributeHelper.setAttribute(graph, "", "kegg_title", title);
	}
	
	public static String getKeggTitle(Graph graph) {
		return (String) AttributeHelper.getAttributeValue(graph, "", "kegg_title", null, "");
	}
	
	public static void setKeggImageUrl(Graph graph, String imageURL) {
		AttributeHelper.setAttribute(graph, "", "kegg_image", imageURL);
	}
	
	public static void setKeggLinkUrl(Graph graph, String linkURL) {
		AttributeHelper.setAttribute(graph, "", "kegg_link", linkURL);
	}
	
	public static String getKeggImageUrl(Graph graph) {
		return (String) AttributeHelper.getAttributeValue(graph, "", "kegg_image", null, "");
	}
	
	public static void setKeggLinkUrl(Node n, String linkURL) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_link", linkURL);
	}
	
	public static void setKeggLinkUrl(Node n, int index, String linkURL) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_link" + index, linkURL);
	}
	
	public static String getKeggImageUrl(Node n) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_image", null, "");
	}
	
	public static String getKeggLinkUrl(Graph graph) {
		return (String) AttributeHelper.getAttributeValue(graph, "", "kegg_link", null, "");
	}
	
	public static String getKeggLinkUrl(Node n) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_link", null, "");
	}
	
	public static String getKeggLinkUrl(Node n, int index) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_link" + index, null, "");
	}
	
	public static void setKeggType(Node n, String type) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_type", type);
	}
	
	public static void setKeggType(Node n, int index, String type) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_type" + index, type);
	}
	
	public static String getKeggType(Node n) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type", null, "");
	}
	
	public static String getKeggType(Node n, int index) {
		return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_type" + index, null, "");
	}
	
	public static void setKeggReaction(Node n, int index, String id) {
		AttributeHelper.setAttribute(n, "kegg", "kegg_reaction" + index, id);
	}
	
	public static void setKeggReactionProduct(Edge e, int index, String id) {
		AttributeHelper.setAttribute(e, "kegg", "kegg_reaction_product" + index, id);
	}
	
	public static void setKeggReactionSubstrate(Edge e, int index, String id) {
		AttributeHelper.setAttribute(e, "kegg", "kegg_reaction_substrate" + index, id);
	}
	
	public static ArrayList<IndexAndString> getKeggReactions(Node n) {
		return getIndexAndStringValuesFromHashMapAttribute(n, "kegg", "kegg_reaction");
	}
	
	public static ArrayList<IndexAndString> getKeggReactionSubstrates(Edge e) {
		return getIndexAndStringValuesFromHashMapAttribute(e, "kegg", "kegg_reaction_substrate");
	}
	
	public static ArrayList<IndexAndString> getKeggReactionProducts(Edge e) {
		return getIndexAndStringValuesFromHashMapAttribute(e, "kegg", "kegg_reaction_product");
	}
	
	public static void setKeggReactionType(Attributable nodeOrEdge, int index, String type) {
		AttributeHelper.setAttribute(nodeOrEdge, "kegg", "kegg_reaction_type" + index, type);
	}
	
	public static String getKeggReactionType(Attributable nodeOrEdge, int index) {
		return (String) AttributeHelper.getAttributeValue(nodeOrEdge, "kegg", "kegg_reaction_type" + index, null, "");
	}
	
	// TODO: Improve performance
	public static ArrayList<IndexAndString> getKeggReactionTypes(Attributable nodeOrEdge) {
		ArrayList<IndexAndString> result = new ArrayList<IndexAndString>();
		try {
			HashMapAttribute hma = (HashMapAttribute) nodeOrEdge.getAttribute("kegg");
			int maxi = hma.getCollection().size();
			for (int i = 0; i < maxi; i++) {
				if (AttributeHelper.hasAttribute(nodeOrEdge, "kegg", "kegg_reaction_type" + i)) {
					String reactionType = (String) AttributeHelper.getAttributeValue(nodeOrEdge, "kegg", "kegg_reaction_type" + i, null, "");
					result.add(new IndexAndString(i, reactionType));
				}
			}
		} catch (AttributeNotFoundException err) {
			// empty
		}
		return result;
	}
	
	public static void setKeggGraphicsTitle(Node n, String name) {
		// TODO: process special characters! which are not allowed in GML
		AttributeHelper.setLabel(n, name);
	}
	
	public static String getKeggGraphicsTitle(Node n) {
		// TODO: process special characters, they are allowed in KGML/XML, but not in GML
		return AttributeHelper.getLabel(n, null);
	}
	
	public static void setKeggGraphicsX(Node n, int xPosition) {
		double y = AttributeHelper.getPositionY(n);
		AttributeHelper.setPosition(n, xPosition, y);
	}
	
	public static int getKeggGraphicsX(Node n) {
		double x = AttributeHelper.getPositionX(n);
		return (int) x;
	}
	
	public static void setKeggGraphicsY(Node n, int yPosition) {
		double x = AttributeHelper.getPositionX(n);
		AttributeHelper.setPosition(n, x, yPosition);
	}
	
	public static int getKeggGraphicsY(Node n) {
		double y = AttributeHelper.getPositionY(n);
		return (int) y;
	}
	
	public static void setKeggGraphicsWidth(Node n, int width) {
		int height = (int) AttributeHelper.getHeight(n);
		AttributeHelper.setSize(n, width, height);
	}
	
	public static int getKeggGraphicsWidth(Node n) {
		double width = AttributeHelper.getWidth(n);
		return (int) width;
	}
	
	public static void setKeggGraphicsHeight(Node n, int height) {
		int width = (int) AttributeHelper.getWidth(n);
		AttributeHelper.setSize(n, width, height);
	}
	
	public static int getKeggGraphicsHeight(Node n) {
		double height = AttributeHelper.getHeight(n);
		return (int) height;
	}
	
	public static void setKeggGraphicsFgColor(Node n, Color fgcolor) {
		AttributeHelper.setLabelColor(-1, n, fgcolor);
	}
	
	public static Color getKeggGraphicsFgColor(Node n) {
		return AttributeHelper.getLabelColor(n);
	}
	
	public static void setKeggGraphicsBgColor(Node n, Color bgcolor) {
		AttributeHelper.setFillColor(n, bgcolor);
	}
	
	public static Color getKeggGraphicsBgColor(Node n) {
		return AttributeHelper.getFillColor(n);
	}
	
	public static void setKeggGraphicsType(Node n, kgmlGraphicsType type) {
		switch (type) {
			case circle:
				AttributeHelper.setShape(n, CircleNodeShape.class.getCanonicalName());
				break;
			case line:
			case rectangle:
				AttributeHelper.setShape(n, RectangleNodeShape.class.getCanonicalName());
				break;
			case roundrectangle:
				AttributeHelper.setShape(n, RectangleNodeShape.class.getCanonicalName());
				AttributeHelper.setRoundedEdges(n, 15d);
				break;
		}
	}
	
	public static kgmlGraphicsType getKeggGraphicsType(Node n) {
		String shape = AttributeHelper.getShape(n);
		if (shape == null)
			return null;
		if (shape.toUpperCase().indexOf("CIRCLE") >= 0)
			return kgmlGraphicsType.circle;
		if (shape.toUpperCase().indexOf("ELLIPSE") >= 0)
			return kgmlGraphicsType.circle;
		if (shape.toUpperCase().indexOf("RECTANGLE") >= 0) {
			double rounding = AttributeHelper.getRoundedEdges(n);
			if (rounding >= 15)
				return kgmlGraphicsType.roundrectangle;
			else
				return kgmlGraphicsType.rectangle;
		}
		return null;
	}
	
	// public static void setKeggMapLink(Node n, String mapID) {
	// AttributeHelper.setAttribute(n, "kegg", "kegg_map_link", mapID);
	// }
	//
	// public static String getKeggMapLink(Node n) {
	// return (String) AttributeHelper.getAttributeValue(n, "kegg", "kegg_map_link", null, "");
	// }
	
	public static void setRelationTypeInformation(Edge e, int index, RelationType rt) {
		AttributeHelper.setAttribute(e, "kegg", "relation_type" + index, rt.toString());
	}
	
	public static ArrayList<IndexAndString> getRelationTypes(Edge e) {
		return getIndexAndStringValuesFromHashMapAttribute(e, "kegg", "relation_type");
	}
	
	public static ArrayList<IndexAndString> getRelationSubTypeNames(Edge e) {
		return getIndexAndStringValuesFromHashMapAttribute(e, "kegg", "relation_subtype");
	}
	
	public static ArrayList<IndexAndString> getRelationSourceAndTargets(Edge e) {
		return getIndexAndStringValuesFromHashMapAttribute(e, "kegg", "relation_src_tgt");
	}
	
	private static ArrayList<IndexAndString> getIndexAndStringValuesFromHashMapAttribute(
						Attributable attributeable, String path, String prefixOfAttributeName) {
		ArrayList<IndexAndString> result = new ArrayList<IndexAndString>();
		try {
			HashMapAttribute hma = (HashMapAttribute) attributeable.getAttribute(path);
			for (Attribute a : hma.getCollection().values()) {
				if (a.getName().startsWith(prefixOfAttributeName)) {
					String indexS = a.getName().substring(prefixOfAttributeName.length());
					try {
						int index = Integer.parseInt(indexS);
						String val = (String) a.getValue();
						if (val != null && val.length() > 0)
							result.add(new IndexAndString(index, val));
					} catch (NumberFormatException nfe) {
						// empty
					}
				}
			}
		} catch (AttributeNotFoundException err) {
			// empty
		}
		return result;
	}
	
	public static String getRelationSourceTarget(Edge e, int index) {
		return (String) AttributeHelper.getAttributeValue(e, "kegg", "relation_src_tgt" + index, null, "");
	}
	
	public static String getRelationSubtypeName(Edge e, int index) {
		return (String) AttributeHelper.getAttributeValue(e, "kegg", "relation_subtype" + index, null, "");
	}
	
	public static void setRelationSubtypeName(Edge e, int index, String subtype) {
		AttributeHelper.setAttribute(e, "kegg", "relation_subtype" + index, subtype);
	}
	
	public static void setKeggGraphicsLineStyleMap(Edge e) {
		AttributeHelper.setDashInfo(e, 10, 5);
	}
	
	public static void setKeggGraphicsLineStyleIndirect(Edge e) {
		AttributeHelper.setDashInfo(e, 10, 10);
	}
	
	public static void setRelationSourceTargetInformation(Edge e, int index, String srcKeggId, String tgtKeggId) {
		AttributeHelper.setAttribute(e, "kegg", "relation_src_tgt" + index, srcKeggId + "/" + tgtKeggId);
	}
	
	/**
	 * Add information about source or target entity which requests a relation and thus the creation of an edge
	 * 
	 * @param e
	 *           The edge which will be processed
	 * @param i
	 *           The index, value needs to correspond to to the method call <code>setRelationTypeInformation</code>
	 * @param e1
	 *           Source or Target entity which requests the edge creation (edge e)
	 * @param e2
	 *           Source or Target entity which requests the edge creation (edge e)
	 * @param errors
	 * @param warnings
	 */
	public static void setRelationSrcTgtInformation(Edge e, int i, IdRef e1, IdRef e2) {
		Node a = e.getSource();
		Node b = e.getTarget();
		String keggIdA = getKeggId(a);
		String keggIdB = getKeggId(b);
		String sourceKeggId = null;
		String targetKeggId = null;
		
		if (keggIdA.equals(e1.getRef().getName().getId())) {
			sourceKeggId = keggIdA;
			targetKeggId = e2.getRef().getName().getId();
		} else
			if (keggIdB.equals(e1.getRef().getName().getId())) {
				sourceKeggId = keggIdB;
				targetKeggId = e2.getRef().getName().getId();
			} else
				if (keggIdA.equals(e2.getRef().getName().getId())) {
					sourceKeggId = e1.getRef().getName().getId();
					targetKeggId = keggIdA;
				} else
					if (keggIdB.equals(e2.getRef().getName().getId())) {
						sourceKeggId = e1.getRef().getName().getId();
						targetKeggId = keggIdB;
					} else {
						// error: neither e1 nor e2 are corresponding to one of the end nodes of e
						ErrorMsg
											.addErrorMessage("Internal Error: Could not find corresponding Source or Target KeggId for end point nodes of edge, representing a relation.");
						sourceKeggId = null;
						targetKeggId = null;
					}
		if (sourceKeggId != null && targetKeggId != null) {
			setRelationSourceTargetInformation(e, i, sourceKeggId, targetKeggId);
		}
	}
	
	public static void setEdgeLabel(Edge e, String label) {
		AttributeHelper.setLabel(e, label);
	}
	
	/**
	 * Find those nodes, which are located inside the "bigNode".
	 * A node is regarded to be "inside" a node, when the upper left point
	 * if the rectangle, defined by the node position and side is inside the
	 * surrounding of the "bigNode", and at the same time the lower-right point
	 * is located also in the bigNode.
	 * 
	 * @param node2lowerRightPos
	 * @param node2upperLeftPos
	 */
	public static Set<Node> getNodesInsideThisNode(Node bigNode, Collection<Node> nodes,
						HashMap<Node, Vector2d> node2upperLeftPos, HashMap<Node, Vector2d> node2lowerRightPos) {
		HashSet<Node> result = new HashSet<Node>();
		for (Node n : nodes)
			if (isInside(bigNode, n, node2upperLeftPos, node2lowerRightPos))
				result.add(n);
		return result;
	}
	
	private static boolean isInside(Node bigNode, Node n, HashMap<Node, Vector2d> node2upperLeftPos, HashMap<Node, Vector2d> node2lowerRightPos) {
		Vector2d upperLeftBN = node2upperLeftPos.get(bigNode);
		Vector2d lowerRightBN = node2lowerRightPos.get(bigNode);
		Vector2d upperLeft = node2upperLeftPos.get(n);
		Vector2d lowerRight = node2lowerRightPos.get(n);
		boolean a = pointInside(upperLeftBN, lowerRightBN, upperLeft);
		boolean b = pointInside(upperLeftBN, lowerRightBN, lowerRight);
		return a && b;
	}
	
	private static boolean pointInside(Vector2d upperLeftBN, Vector2d lowerRightBN, Vector2d testPoint) {
		Vector2d a, b, t;
		a = upperLeftBN;
		b = lowerRightBN;
		t = testPoint;
		return (t.x >= a.x && t.y >= a.y && t.x <= b.x && t.y <= b.y);
	}
	
	static Vector2d getNodePointUL(Node n) {
		int xp = getKeggGraphicsX(n);
		int yp = getKeggGraphicsY(n);
		int w = getKeggGraphicsWidth(n);
		int h = getKeggGraphicsHeight(n);
		return new Vector2d(xp - w / 2d, yp - h / 2d);
	}
	
	static Vector2d getNodePointLR(Node n) {
		Vector2d p = AttributeHelper.getPositionVec2d(n);
		int w = getKeggGraphicsWidth(n);
		int h = getKeggGraphicsHeight(n);
		return new Vector2d(p.x + w / 2d, p.y + h / 2d);
	}
	
	public static boolean getIsPartOfGroup(Node n) {
		String v = (String) AttributeHelper.getAttributeValue(n, "kegg", "grouppart", "no", "no");
		if (v != null && v.equalsIgnoreCase("yes"))
			return true;
		else
			return false;
	}
	
	public static void setIsPartOfGroup(Node n, boolean value) {
		if (value)
			AttributeHelper.setAttribute(n, "kegg", "grouppart", "yes");
		else
			AttributeHelper.setAttribute(n, "kegg", "grouppart", "no");
	}
	
	public static void setKeggGraphicsLineStyleInhibitionArrow(Edge e) {
		AttributeHelper.setArrowhead(e, "org.graffiti.plugins.views.defaults.InhibitorArrowShape");
	}
}
