/*
 * Created on 03.08.2005 by Christian Klukas
 */
package org.graffiti.plugins.ios.importers;

import java.util.Stack;

import org.AttributeHelper;
import org.ErrorMsg;
import org.HelperClass;
import org.graffiti.attributes.AbstractAttribute;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.HashMapAttribute;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugins.ios.exporters.gml.GMLWriter;

public class TypedAttributeService implements HelperClass {
	public static Graph createTypedHashMapAttributes(Graph g) {
		// AbstractAttribute.getTypedAttribute("id");
		for (Node n : g.getNodes()) {
			processAttributable(n, true);
			try {
				IntegerAttribute ia = (IntegerAttribute) n.getAttribute("zlevel");
				n.setViewID(ia.getInteger());
				n.removeAttribute("zlevel");
			} catch (Exception e) {
			}
			try {
				n.removeAttribute("label");
			} catch (Exception e) {
			}
		}
		for (Edge e : g.getEdges()) {
			processAttributable(e, false);
			try {
				IntegerAttribute ia = (IntegerAttribute) e.getAttribute("zlevel");
				e.setViewID(ia.getInteger());
				e.removeAttribute("zlevel");
			} catch (Exception err) {
			}
			try {
				e.removeAttribute("label");
			} catch (Exception err) {
			}
		}
		return g;
	}
	
	private static void processAttributable(Attributable attr, boolean isNode) {
		CollectionAttribute ca = attr.getAttributes();
		Stack<CollectionAttribute> catts = new Stack<CollectionAttribute>();
		catts.push(ca);
		Stack<Attribute> toDo = new Stack<Attribute>();
		while (!catts.empty()) {
			CollectionAttribute ccc = catts.pop();
			// System.out.println("Process: "+ccc.getId());
			for (Attribute a : ccc.getCollection().values()) {
				if (a instanceof CollectionAttribute) {
					if (AbstractAttribute.isTypedAttributeFromID(a.getId(), isNode))
						toDo.add(a);
					catts.push((CollectionAttribute) a);
				} else {
					if (a instanceof StringAttribute) {
					}
					// System.out.println("> "+a.getId()+" ("+a.getDescription()+")");
					/*
					 * if (a.getId().equalsIgnoreCase("framethickness")) {
					 * System.out.println("VALUE: "+((DoubleAttribute)a).getValue().toString());
					 * }
					 */
				}
			}
		}
		while (!toDo.empty()) {
			Attribute untypedAttribute = toDo.pop();
			// System.out.println("["+toDo.size()+"] Covert "+untypedAttribute.getId()+" ("+untypedAttribute.getDescription()+")");
			Attribute typedAttribute = AbstractAttribute.getTypedAttribute(untypedAttribute.getId(), untypedAttribute.getAttributable() instanceof Node);
			if (typedAttribute != null) {
				HashMapAttribute ha = (HashMapAttribute) untypedAttribute;
				CollectionAttribute pa = ha.getParent();
				pa.remove(ha);
				try {
					((HashMapAttribute) typedAttribute).setCollection(ha.getCollection());
					pa.add(typedAttribute, false);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}
		try {
			StringAttribute a = (StringAttribute) attr.getAttributes().getAttribute("label");
			// System.out.println("Convert label property: "+a.getId());
			String oldLabel = a.getString();
			if (attr instanceof Edge)
				AttributeHelper.setLabel((Edge) attr, oldLabel);
			else
				AttributeHelper.setLabel((Node) attr, oldLabel);
			attr.removeAttribute("label");
		} catch (Exception err) {
			
		}
		try {
			HashMapAttribute charting = (HashMapAttribute) attr.getAttributes().getAttribute("charting");
			StringAttribute a = (StringAttribute) charting.getAttribute("substancename");
			// System.out.println("Convert label property: "+a.getId());
			if (attr instanceof Edge)
				AttributeHelper.setLabel((Edge) attr, a.getString());
			else {
				AttributeHelper.setLabel((Node) attr, a.getString());
				AttributeHelper.getLabel(-1, (Node) attr).setAlignment("t");
			}
			a.getParent().remove(a);
		} catch (Exception err) {
			
		}
		try {
			HashMapAttribute charting = (HashMapAttribute) attr.getAttributes().getAttribute("data");
			StringAttribute a = (StringAttribute) charting.getAttribute("substancename");
			// System.out.println("Convert label property: "+a.getId());
			if (attr instanceof Edge)
				AttributeHelper.setLabel((Edge) attr, a.getString());
			else {
				AttributeHelper.setLabel((Node) attr, a.getString());
				AttributeHelper.getLabel(-1, (Node) attr).setAlignment("t");
			}
			a.getParent().remove(a);
		} catch (Exception err) {
			
		}
		if (attr instanceof Edge) {
			try {
				Integer i1 = (Integer) AttributeHelper.getAttributeValue(attr, "graphics", "quadcurve", null, 1);
				Integer i2 = (Integer) AttributeHelper.getAttributeValue(attr, "graphics", "straight", null, 1);
				if (i1 != null && ((int) i1) != 0)
					AttributeHelper.setShape((Edge) attr, "org.graffiti.plugins.views.defaults.QuadCurveEdgeShape");
				if (i2 != null && ((int) i2) != 0)
					AttributeHelper.setShape((Edge) attr, "org.graffiti.plugins.views.defaults.StraightLineEdgeShape");
				AttributeHelper.deleteAttribute(attr, "graphics", "quadcurve");
				AttributeHelper.deleteAttribute(attr, "graphics", "straight");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (attr instanceof Edge) {
			try {
				String s1 = (String) AttributeHelper.getAttributeValue(attr, "graphics", "arrowheadstyle", null, "", false);
				String s2 = (String) AttributeHelper.getAttributeValue(attr, "graphics", "arrowtailstyle", null, "", false);
				if (s1 != null && s1.length() > 0) {
					String ss1 = GMLWriter.getArrowShapeClassNameFromGMLarrowStyle(s1);
					if (ss1 != null && ss1.length() > 0)
						AttributeHelper.setArrowhead((Edge) attr, ss1);
				}
				if (s2 != null && s2.length() > 0) {
					String ss2 = GMLWriter.getArrowShapeClassNameFromGMLarrowStyle(s2);
					if (ss2 != null && ss2.length() > 0)
						AttributeHelper.setArrowtail((Edge) attr, ss2);
				}
				AttributeHelper.deleteAttribute(attr, "graphics", "arrowheadstyle");
				AttributeHelper.deleteAttribute(attr, "graphics", "arrowtailstyle");
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
	}
}
