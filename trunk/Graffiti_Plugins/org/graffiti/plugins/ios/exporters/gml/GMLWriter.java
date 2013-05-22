// ==============================================================================
//
// GMLWriter.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: GMLWriter.java,v 1.3 2013-05-22 15:16:29 klukas Exp $

package org.graffiti.plugins.ios.exporters.gml;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.AttributeHelper;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeManager;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.BooleanAttribute;
import org.graffiti.attributes.ByteAttribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.DoubleAttribute;
import org.graffiti.attributes.FloatAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.LongAttribute;
import org.graffiti.attributes.ShortAttribute;
import org.graffiti.attributes.SortedCollectionAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.graphics.ColorAttribute;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.LineModeAttribute;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.SupportsWriterOutput;
import org.graffiti.plugins.editcomponents.defaults.EdgeArrowShapeEditComponent;

/**
 * Provides a GML writer. This writer does not yet support all GML features
 * yet. See http://infosun.fmi.uni-passau.de/Graphlet/GML/ for more details
 * 
 * @version $Revision: 1.3 $
 */
public class GMLWriter
		implements OutputSerializer, SupportsWriterOutput {
	// ~ Static fields/initializers =============================================
	
	private final static String TAB = "  ";
	
	// ~ Instance fields ========================================================
	
	/** DOCUMENT ME! */
	private final static String eol = System.getProperty("line.separator");
	
	// /**
	// * A collection of attribute paths that should not be written explicitly.
	// * Sensible e.g. for attributes that have already been given special
	// * treatment.
	// */
	// private Collection<String> dontWriteAttrs;
	
	/**
	 * A map of attributes, which should be written to the stream. This is
	 * something like a filter and a mapping from graffiti collection
	 * attributes to GML attributes.
	 */
	private final Map<String, String> attMapping;
	
	/**
	 * A map of collection attributes, which should be written to the stream.
	 * This is something like a filter and a mapping from graffiti collection
	 * attributes to GML (hierarchial) attributes.
	 */
	private final Map<String, String> colMapping;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new GML writer.
	 */
	public GMLWriter() {
		colMapping = new HashMap<String, String>();
		
		colMapping.put("", ""); // the root collection attribute
		colMapping.put("graphics", "graphics");
		
		// colMapping.put("label", "label");
		// colMapping.put("LabelGraphics", "LabelGraphics");
		attMapping = new HashMap<String, String>();
		attMapping.put("version", "version");
		// attMapping.put("directed", "directed");
		attMapping.put("x", "x");
		attMapping.put("y", "y");
		attMapping.put("frameThickness", "width");
		attMapping.put("shape", "type");
		attMapping.put("outline", "outline");
		attMapping.put("arrowtail", "arrow");
		attMapping.put("arrowhead", "arrow");
		
		// dontWriteAttrs = new ArrayList<String>();
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.coordinate.x");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.coordinate.y");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.dimension.width");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.dimension.height");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.backgroundImage");
		
		AttributeManager.getInstance().addUnwrittenAttribute(".labelgraphics.position");
		AttributeManager.getInstance().addUnwrittenAttribute(".labelgraphics.text");
		AttributeManager.getInstance().addUnwrittenAttribute(".label");
		
		// dontWriteAttrs.add(".graphics.frameThickness");
		// dontWriteAttrs.add(".graphics.shape");
		// dontWriteAttrs.add(".graphics.arrowtail");
		// dontWriteAttrs.add(".graphics.arrowhead");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.fill");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.outline");
		AttributeManager.getInstance().addUnwrittenAttribute(".graphics.bends");
		// dontWriteAttrs.add(".tooltip");
		AttributeManager.getInstance().addUnwrittenAttribute(".id");
		
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @see org.graffiti.plugin.io.Serializer#getExtensions()
	 */
	public String[] getExtensions() {
		return new String[] { ".gml" };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.io.Serializer#getFileTypeDescriptions()
	 */
	public String[] getFileTypeDescriptions() {
		return new String[] { "GML" };
	}
	
	public void write(Writer o, Graph g) throws IOException {
		// write the graph's open tag
		
		// String name = "";
		// if (MainFrame.getInstance() != null)
		// name = " with " + MainFrame.getInstance().getTitle();
		// o.append("# generated" + name + " at " + new Date() + eol);
		
		o.append("graph [" + eol);
		
		// write the graph
		writeGraph(o, g);
		
		// write the nodes
		HashMap<Node, Integer> node2id = writeNodes(o, g);
		
		// write the edges
		writeEdges(o, g, node2id);
		
		// write the graph's close tag
		o.append("]" + eol);
		o.flush();
	}
	
	/**
	 * @see org.graffiti.plugin.io.OutputSerializer#write(OutputStream, Graph)
	 */
	public void write(OutputStream o, Graph g)
			throws IOException {
		PrintStream p;
		try {
			p = new PrintStream(o, true, StringManipulationTools.Unicode);
		} catch (UnsupportedEncodingException e) {
			ErrorMsg.addErrorMessage(e);
			p = new PrintStream(o, false);
		}
		String name = "";
		if (MainFrame.getInstance() != null)
			name = " with " + MainFrame.getInstance().getTitle();
		p.println("# generated" + name + " at " + new Date());
		
		// write the graph's open tag
		p.println("graph [");
		
		// write the graph
		writeGraph(p, g);
		
		// write the nodes
		HashMap<Node, Integer> node2id = writeNodes(p, g);
		
		// write the edges
		writeEdges(p, g, node2id);
		
		// write the graph's close tag
		p.println("]");
		p.close();
	}
	
	/**
	 * This method does not actually write the hierarchy to the stream, but
	 * stores it into a StringBuffer. That is the means to remove empty and
	 * unnecessary sub structures. An example is the coordinate attribute: All
	 * sub attributes (x, y) of coordinate have already been written somewhere
	 * else. Therefore, the coordinate attribute itself will be empty and can
	 * be ommitted. This can be seen only after having checked all sub-
	 * attributess, though.
	 * 
	 * @param a
	 *           the collection attribute to get the attribute from.
	 * @param level
	 *           the indentation level.
	 * @return the sub herarchy starting at Attribute a
	 */
	private StringBuffer getWrittenAttributeHierarchy(Attribute a, int level) {
		StringBuffer sb = new StringBuffer();
		if (a.getId().equalsIgnoreCase("directed"))
			return sb;
		
		if (AttributeManager.getInstance().getUnwrittenAttributes().contains(a.getPath())) {
			return sb;
		}
		
		if (a instanceof CollectionAttribute) {
			CollectionAttribute c = (CollectionAttribute) a;
			
			if (c instanceof LabelAttribute) {
				try {
					LabelAttribute la = (LabelAttribute) c;
					if (la.getName().equalsIgnoreCase("labelgraphics")) {
						if (la.getLabel() != null) {
							sb.append(createTabs(level) + "label \"" +
									la.getLabel() + "\"");
							sb.append(eol);
						}
					}
				} catch (AttributeNotFoundException anfe) {
					warning(anfe.getMessage());
				}
			}
			
			if (colMapping.containsKey(c.getId())) {
				Map<String, Attribute> m = c.getCollection();
				
				sb.append(createTabs(level));
				sb.append(c.getId() + " [");
				sb.append(eol);
				
				// TODO: add a Graffiti to GML mapping class instead.
				if (c.getId().equals("graphics"))
					writeDefaultGraphicsAttributes(level, sb, c);
				
				for (Attribute subCol : m.values()) {
					sb.append(getWrittenAttributeHierarchy(subCol, level + 1));
				}
				
				sb.append(createTabs(level));
				sb.append("]");
				sb.append(eol);
			} else {
				// warning("did not write: " + c.getPath());
				Map<?, ?> m = c.getCollection();
				
				if (!m.isEmpty()) {
					StringBuffer sub = new StringBuffer();
					
					for (Iterator<?> i = m.values().iterator(); i.hasNext();) {
						Attribute subCol = (Attribute) i.next();
						sub.append(getWrittenAttributeHierarchy(subCol,
								level + 1));
					}
					
					if (!sub.toString().equals("")) {
						sb.append(createTabs(level));
						sb.append(c.getId() + " [");
						sb.append(eol);
						
						sb.append(sub);
						
						sb.append(createTabs(level));
						sb.append("]");
						sb.append(eol);
					}
				}
			}
		} else
			if (a instanceof StringAttribute) {
				// TODO: add a Graffiti to GML mapping class instead.
				if (attMapping.containsKey(a.getId())) {
					if ("shape".equals(a.getId())) {
						writeShapeAttribute(a, level, sb);
					} else
						if ("arrowhead".equals(a.getId())) {
							writeArrowHeadAttribute(a, level, sb);
						} else
							if ("arrowtail".equals(a.getId())) {
								writeArrowTailAttribute(a, level, sb);
							} else {
								sb.append(createTabs(level));
								sb.append(a.getId() + " \"" + encodeBadCharacters(a.getValue()) + "\"");
								sb.append(eol);
							}
				} else {
					if ((a != null) && (a.getValue() != null) && !a.getValue().equals("")) {
						sb.append(createTabs(level));
						sb.append(a.getId() + " \"" + encodeBadCharacters(a.getValue()) + "\"");
						sb.append(eol);
					}
					
					// warning("did not write: " + a.getPath());
				}
			} else {
				if (a instanceof BooleanAttribute) {
					sb.append(createTabs(level));
					sb.append(a.getId() + " " +
							(((BooleanAttribute) a).getBoolean() ? "\"true\"" : "\"false\""));
					sb.append(eol);
				} else
					if (a instanceof ByteAttribute ||
							a instanceof IntegerAttribute || a instanceof LongAttribute ||
							a instanceof ShortAttribute) {
						sb.append(createTabs(level));
						sb.append(a.getId() + " " + a.getValue().toString());
						sb.append(eol);
					} else
						if (a instanceof DoubleAttribute ||
								a instanceof FloatAttribute) {
							sb.append(createTabs(level));
							
							String val = a.getValue().toString();
							// sb.append(a.getId() + " " +val.substring(0, Math.min(17, val.length())));
							sb.append(a.getId() + " " + val);
							sb.append(eol);
						} else
							if (a instanceof LineModeAttribute) {
								LineModeAttribute lma = (LineModeAttribute) a;
								float[] da = lma.getDashArray();
								float dp = lma.getDashPhase();
								if (da != null) {
									sb.append(createTabs(level));
									sb.append("linemode \"" + getValues(da, " ") + " " + dp + "\"");
									sb.append(eol);
								}
							} else
								if (attMapping.containsKey(a.getId())) {
									sb.append(createTabs(level));
									sb.append(attMapping.get(a.getId()) + " " + a.getValue());
									sb.append(eol);
								} else {
									warning("did not write complex attribute: " + a.getPath());
								}
			}
		
		return sb;
	}
	
	public static String encodeBadCharacters(Object val) {
		if (val == null || !(val instanceof String))
			return "";
		String value = (String) val;
		value = StringManipulationTools.stringReplace(value, "\"", "\\\"");
		return value;
	}
	
	public static String decodeBadCharacters(Object val) {
		if (val == null || !(val instanceof String))
			return "";
		String value = (String) val;
		value = StringManipulationTools.stringReplace(value, "\\\"", "\"");
		return value;
	}
	
	public static String getValues(float[] da, String space) {
		String result = "";
		if (da != null) {
			for (int i = 0; i < da.length; i++) {
				result += da[i] + (i < da.length - 1 ? space : "");
			}
		}
		return result;
	}
	
	/**
	 * @param a
	 * @param level
	 * @param sb
	 */
	private void writeArrowTailAttribute(Attribute a, int level, StringBuffer sb) {
		if (!a.getValue().equals("")) {
			try {
				if (((StringAttribute) a.getParent().getAttribute(GraphicAttributeConstants.ARROWHEAD)).getString().equals("")) {
					sb.append(createTabs(level));
					sb.append("arrow \"first\"");
					sb.append(eol);
					String classInfo = ((StringAttribute) a).getString();
					sb.append(getGMLarrowStyleOutputFromClassName(createTabs(level) + "arrowtailstyle", classInfo));
				} else {
					// "both" will be / has been written in
					// "arrowhead" case below
					String classInfo = ((StringAttribute) a).getString();
					sb.append(getGMLarrowStyleOutputFromClassName(createTabs(level) + "arrowtailstyle", classInfo));
				}
			} catch (AttributeNotFoundException anfe) {
				sb.append(createTabs(level));
				sb.append("arrow \"first\"");
				sb.append(eol);
			}
		}
	}
	
	/**
	 * @param a
	 * @param level
	 * @param sb
	 */
	private void writeArrowHeadAttribute(Attribute a, int level, StringBuffer sb) {
		if (!a.getValue().equals("")) {
			sb.append(createTabs(level));
			
			try {
				if (((StringAttribute) a.getParent().getAttribute(GraphicAttributeConstants.ARROWTAIL)).getString()
						.equals("")) {
					sb.append("arrow \"last\"");
					sb.append(eol);
				} else {
					sb.append("arrow \"both\"");
					sb.append(eol);
				}
				String classInfo = ((StringAttribute) a).getString();
				sb.append(getGMLarrowStyleOutputFromClassName(createTabs(level) + "arrowheadstyle", classInfo));
				
			} catch (AttributeNotFoundException anfe) {
				sb.append("arrow \"last\"");
				sb.append(eol);
			}
		} else {
			try {
				if (((StringAttribute) a.getParent().getAttribute(GraphicAttributeConstants.ARROWTAIL)).getString()
						.equals("")) {
					sb.append(createTabs(level));
					sb.append("arrow \"none\"");
					sb.append(eol);
				}
			} catch (AttributeNotFoundException anfe) {
				sb.append(createTabs(level));
				sb.append("arrow \"none\"");
				sb.append(eol);
			}
		}
	}
	
	private static String getGMLarrowStyleOutputFromClassName(String what, String classInfo) {
		String style = "";
		if (classInfo.equals(EdgeArrowShapeEditComponent.circleArrow))
			style = "circle";
		if (classInfo.equals(EdgeArrowShapeEditComponent.standardArrowLeft))
			style = "halfleft";
		if (classInfo.equals(EdgeArrowShapeEditComponent.standardArrowRight))
			style = "halfright";
		if (classInfo.equals(EdgeArrowShapeEditComponent.circleConnectArrow))
			style = "narrowCircle";
		if (classInfo.equals(EdgeArrowShapeEditComponent.diamondArrow))
			style = "diamond";
		if (classInfo.equals(EdgeArrowShapeEditComponent.inhibitorArrow))
			style = "inhibitor";
		if (classInfo.equals(EdgeArrowShapeEditComponent.absoluteInhibitorArrow))
			style = "absoluteinhibitor";
		if (classInfo.equals(EdgeArrowShapeEditComponent.absoluteStimulationArrow))
			style = "absolutestimulation";
		if (classInfo.equals(EdgeArrowShapeEditComponent.assignmentArrow))
			style = "assignment";
		if (classInfo.equals(EdgeArrowShapeEditComponent.thinStandardArrow))
			style = "stimulation";
		if (classInfo.equals(EdgeArrowShapeEditComponent.thinDiamondArrow))
			style = "modulation";
		if (classInfo.equals(EdgeArrowShapeEditComponent.thinCircleArrow))
			style = "catalysis";
		if (classInfo.equals(EdgeArrowShapeEditComponent.triggerArrow))
			style = "trigger";
		if (classInfo.equals(EdgeArrowShapeEditComponent.assignmentArrow))
			style = "assignment";
		if (style.length() > 0)
			return what + " \"" + style + "\"" + eol;
		else
			return "";
	}
	
	public static String getArrowShapeClassNameFromGMLarrowStyle(String arrowStyle) {
		String res = EdgeArrowShapeEditComponent.standardArrow;
		if (arrowStyle.equals("circle"))
			res = EdgeArrowShapeEditComponent.circleArrow;
		if (arrowStyle.equals("halfleft"))
			res = EdgeArrowShapeEditComponent.standardArrowLeft;
		if (arrowStyle.equals("halfright"))
			res = EdgeArrowShapeEditComponent.standardArrowRight;
		if (arrowStyle.equals("narrowCircle"))
			res = EdgeArrowShapeEditComponent.circleConnectArrow;
		if (arrowStyle.equals("diamond"))
			res = EdgeArrowShapeEditComponent.diamondArrow;
		if (arrowStyle.equals("inhibitor"))
			res = EdgeArrowShapeEditComponent.inhibitorArrow;
		if (arrowStyle.equals("absoluteinhibitor"))
			res = EdgeArrowShapeEditComponent.absoluteInhibitorArrow;
		if (arrowStyle.equals("absolutestimulation"))
			res = EdgeArrowShapeEditComponent.absoluteStimulationArrow;
		if (arrowStyle.equals("asignment"))
			res = EdgeArrowShapeEditComponent.assignmentArrow;
		if (arrowStyle.equals("stimulation"))
			res = EdgeArrowShapeEditComponent.thinStandardArrow;
		if (arrowStyle.equals("modulation"))
			res = EdgeArrowShapeEditComponent.thinDiamondArrow;
		if (arrowStyle.equals("catalysis"))
			res = EdgeArrowShapeEditComponent.thinCircleArrow;
		if (arrowStyle.equals("trigger"))
			res = EdgeArrowShapeEditComponent.triggerArrow;
		if (arrowStyle.equals("assignment"))
			res = EdgeArrowShapeEditComponent.assignmentArrow;
		return res;
	}
	
	/**
	 * @param a
	 * @param level
	 * @param sb
	 */
	private void writeShapeAttribute(Attribute a, int level, StringBuffer sb) {
		// if("org.graffiti.plugins.views.defaults.StraightLineEdgeShape".equals(
		// a.getValue()))
		// {
		// sb.append("\"line\"");
		// }
		// else
		if ("org.graffiti.plugins.views.defaults.SmoothLineEdgeShape".equals(
				a.getValue())) {
			sb.append(createTabs(level) + "smooth 1" + eol);
		} else
			if ("org.graffiti.plugins.views.defaults.QuadCurveEdgeShape".equals(
					a.getValue())) {
				sb.append(createTabs(level) + "quadcurve 1" + eol);
			} else
				if ("org.graffiti.plugins.views.defaults.StraightLineEdgeShape".equals(
						a.getValue())) {
					if (a.getAttributable() != null && a.getAttributable() instanceof Edge) {
						Edge e = (Edge) a.getAttributable();
						if (AttributeHelper.getEdgeBends(e).size() > 0)
							sb.append(createTabs(level) + "straight 1" + eol);
					}
				} else {
					if ("org.graffiti.plugins.views.defaults.RectangleNodeShape".equals(
							a.getValue())) {
						sb.append(createTabs(level) +
								attMapping.get(a.getId()) + " ");
						sb.append("\"rectangle\"");
						sb.append(eol);
					} else
						if ("org.graffiti.plugins.views.defaults.CircleNodeShape".equals(
								a.getValue())) {
							sb.append(createTabs(level) +
									attMapping.get(a.getId()) + " ");
							sb.append("\"oval\"");
							sb.append(eol);
						} else
							if ("org.graffiti.plugins.views.defaults.EllipseNodeShape".equals(
									a.getValue())) {
								sb.append(createTabs(level) +
										attMapping.get(a.getId()) + " ");
								sb.append("\"oval\"");
								sb.append(eol);
							} else {
								if (a.getValue() != null && (a.getValue() instanceof String)) {
									sb.append(createTabs(level) + attMapping.get(a.getId()) + " ");
									sb.append("\"" + (String) a.getValue() + "\"");
									sb.append(eol);
								}
							}
				}
	}
	
	/**
	 * @param level
	 * @param sb
	 * @param c
	 */
	private void writeDefaultGraphicsAttributes(int level, StringBuffer sb, CollectionAttribute c) {
		try {
			sb.append(createTabs(level + 1) + "x " +
					c.getAttribute("coordinate.x").getValue());
			sb.append(eol);
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		}
		
		try {
			sb.append(createTabs(level + 1) + "y " +
					c.getAttribute("coordinate.y").getValue());
			sb.append(eol);
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		}
		
		try {
			sb.append(createTabs(level + 1) + "w " +
					c.getAttribute("dimension.width").getValue());
			sb.append(eol);
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		}
		
		try {
			sb.append(createTabs(level + 1) + "h " +
					c.getAttribute("dimension.height").getValue());
			sb.append(eol);
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		}
		
		try {
			sb.append(createTabs(level + 1) + "fill \"" +
					colToHex(c.getAttribute(
							GraphicAttributeConstants.FILLCOLOR)) +
					"\"");
			sb.append(eol);
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		}
		
		try {
			sb.append(createTabs(level + 1) + "outline \"" +
					colToHex(c.getAttribute(
							GraphicAttributeConstants.FRAMECOLOR)) +
					"\"");
			sb.append(eol);
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		}
		
		try {
			SortedCollectionAttribute bends = (SortedCollectionAttribute) c.getAttribute(
					"bends");
			
			if (!bends.isEmpty()) {
				sb.append(createTabs(level + 1) + "Line [" + eol);
				sb.append(createTabs(level + 2) +
						"point [ x 0.0 y 0.0 ]" + eol);
				
				for (Iterator<?> iter = bends.getCollection().values()
						.iterator(); iter.hasNext();) {
					try {
						CoordinateAttribute coord = (CoordinateAttribute) iter.next();
						sb.append(createTabs(level + 2) +
								"point [ x ");
						sb.append(coord.getX());
						sb.append(" y ");
						sb.append(coord.getY());
						sb.append(" ]");
						sb.append(eol);
					} catch (ClassCastException cce) {
						// ignore wrong type
					}
				}
				
				sb.append(createTabs(level + 2) +
						"point [ x 0.0 y 0.0 ]" + eol);
				sb.append(createTabs(level + 1) + "]" + eol);
			}
		} catch (AttributeNotFoundException anfe) {
			// warning(anfe.getMessage());
		} catch (ClassCastException cce) {
			// warning(anfe.getMessage());
		}
	}
	
	/**
	 * Converts the given color attribute into a hex string. Returns <code>#000000</code>, if the given color attribute could not be
	 * converted.
	 * 
	 * @param colorAtt
	 *           DOCUMENT ME!
	 * @return a hex string representing the value of the given color
	 *         attribute. e.g.: &quot;#FFFFFF&quot; or &quot;#00AAEE&quot;.
	 */
	private String colToHex(Attribute colorAtt) {
		// String color = "";
		try {
			Color c = ((ColorAttribute) colorAtt).getColor();
			
			String r = Integer.toHexString(c.getRed());
			String g = Integer.toHexString(c.getGreen());
			String b = Integer.toHexString(c.getBlue());
			
			if (r.length() < 2)
				r = "0" + r;
			
			if (g.length() < 2)
				g = "0" + g;
			
			if (b.length() < 2)
				b = "0" + b;
			
			return "#" + (r + g + b).toUpperCase();
		} catch (Exception e) {
			return "#000000";
		}
	}
	
	/**
	 * Creates and returns TAB + TAB + ... + TAB (level).
	 * 
	 * @param level
	 *           the indentation level.
	 * @return a string, of level TAB.
	 */
	private String createTabs(int level) {
		StringBuffer b = new StringBuffer();
		
		for (int i = 0; i < level; i++) {
			b.append(TAB);
		}
		
		return b.toString();
	}
	
	/**
	 * Prints the given warning to system.out.
	 * 
	 * @param msg
	 *           the warning msg.
	 */
	private void warning(String msg) {
		System.out.println("Warning: " + msg);
		ErrorMsg.addErrorMessage("Warning: GML export not complete: " + msg);
	}
	
	/**
	 * Writes the attribute hierarchy of the specified attributable.
	 * 
	 * @param p
	 *           the print stream to write to.
	 * @param a
	 *           the attributable to read the attributes from.
	 * @param level
	 *           the indentation level.
	 */
	private void writeAttributable(PrintStream p, Attributable a, int level) {
		CollectionAttribute c = a.getAttributes();
		
		// if (colMapping.containsKey(c.getId())) {
		Map<?, ?> m = c.getCollection();
		
		for (Iterator<?> i = m.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			
			Attribute subCol = c.getAttribute(id);
			p.print(getWrittenAttributeHierarchy(subCol, level + 1));
		}
		
		// } else {
		// System.out.println("Warning: did not write: " + c.getPath());
		// }
	}
	
	private void writeAttributable(Writer o, Attributable a, int level) throws IOException {
		CollectionAttribute c = a.getAttributes();
		Map<?, ?> m = c.getCollection();
		for (Iterator<?> i = m.keySet().iterator(); i.hasNext();) {
			String id = (String) i.next();
			
			Attribute subCol = c.getAttribute(id);
			o.append(getWrittenAttributeHierarchy(subCol, level + 1));
		}
	}
	
	/**
	 * Writes the edge of the given graph to the given print stream.
	 * 
	 * @param p
	 *           the stream to write to.
	 * @param g
	 *           the graph to get the data from.
	 * @param nodeIds
	 *           the ordered list of node ids.
	 */
	private void writeEdges(PrintStream p, Graph g, HashMap<Node, Integer> node2id) {
		int idx = 1;
		for (Edge e : g.getEdges()) {
			if (e.getSource() == null || e.getTarget() == null ||
					!node2id.containsKey(e.getSource())
					|| !node2id.containsKey(e.getTarget())) {
				ErrorMsg.addErrorMessage("Internal error: Invalid graph edge: " + e.toString() + ", source or target of edge is NULL or not element of graph.");
				continue;
			}
			p.println(createTabs(1) + "edge [");
			p.println(createTabs(2) + "id " + idx);
			
			int viewID = e.getViewID();
			if (viewID != 0) {
				p.println(createTabs(2) + "zlevel " + viewID + eol);
			}
			
			p.println(createTabs(2) + "source " + node2id.get(e.getSource()));
			
			p.println(createTabs(2) + "target " + node2id.get(e.getTarget()));
			
			writeAttributable(p, e, 1);
			
			p.println(createTabs(1) + "]");
			idx++;
		}
	}
	
	private void writeEdges(Writer o, Graph g, HashMap<Node, Integer> node2id) throws IOException {
		int idx = 1;
		for (Edge e : g.getEdges()) {
			if (e.getSource() == null || e.getTarget() == null ||
					!node2id.containsKey(e.getSource())
					|| !node2id.containsKey(e.getTarget())) {
				ErrorMsg.addErrorMessage("Internal error: Invalid graph edge: " + e.toString() + ", source or target of edge is NULL or not element of graph.");
				continue;
			}
			o.append(createTabs(1) + "edge [" + eol);
			o.append(createTabs(2) + "id " + idx + eol);
			
			int viewID = e.getViewID();
			if (viewID != 0) {
				o.append(createTabs(2) + "zlevel " + viewID + eol);
			}
			
			o.append(createTabs(2) + "source " + node2id.get(e.getSource()) + eol);
			
			o.append(createTabs(2) + "target " + node2id.get(e.getTarget()) + eol);
			
			writeAttributable(o, e, 1);
			
			o.append(createTabs(1) + "]" + eol);
			idx++;
		}
	}
	
	/**
	 * Method writeGraph.
	 * 
	 * @param p
	 * @param g
	 */
	private void writeGraph(PrintStream p, Graph g) {
		writeAttributable(p, g, 0);
		p.println(createTabs(1) + "directed " + (g.isDirected() ? "1" : "0"));
	}
	
	private void writeGraph(Writer o, Graph g) throws IOException {
		writeAttributable(o, g, 0);
		o.append(createTabs(1) + "directed " + (g.isDirected() ? "1" : "0") + eol);
	}
	
	/**
	 * Writes the nodes of the given graph to the given print stream.
	 * 
	 * @param p
	 *           the stream to write to.
	 * @param g
	 *           the graph to get the data from.
	 * @return the ordered array list of nodes.
	 */
	private HashMap<Node, Integer> writeNodes(PrintStream p, Graph g) {
		HashMap<Node, Integer> node2id = new HashMap<Node, Integer>();
		
		int idx = 1;
		for (Node n : g.getNodes()) {
			node2id.put(n, idx);
			p.println(createTabs(1) + "node [");
			p.println(createTabs(2) + "id " + idx);
			int viewID = n.getViewID();
			if (viewID != 0) {
				p.println(createTabs(2) + "zlevel " + viewID + eol);
			}
			writeAttributable(p, n, 1);
			p.println(createTabs(1) + "]");
			idx++;
		}
		return node2id;
	}
	
	private HashMap<Node, Integer> writeNodes(Writer o, Graph g) throws IOException {
		HashMap<Node, Integer> node2id = new HashMap<Node, Integer>();
		
		int idx = 1;
		for (Node n : g.getNodes()) {
			node2id.put(n, idx);
			o.append(createTabs(1) + "node [" + eol);
			o.append(createTabs(2) + "id " + idx + eol);
			int viewID = n.getViewID();
			if (viewID != 0) {
				o.append(createTabs(2) + "zlevel " + viewID + eol);
			}
			writeAttributable(o, n, 1);
			o.append(createTabs(1) + "]" + eol);
			idx++;
		}
		return node2id;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
