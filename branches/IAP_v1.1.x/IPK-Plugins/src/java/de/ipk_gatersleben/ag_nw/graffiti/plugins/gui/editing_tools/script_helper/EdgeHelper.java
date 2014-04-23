package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.HelperClass;
import org.Vector2d;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.LinkedHashMapAttribute;
import org.graffiti.graph.Edge;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;

import de.ipk_gatersleben.ag_nw.graffiti.AttributeConstants;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

public class EdgeHelper implements HelperClass {
	
	public static void moveBends(Edge edge, double offX, double offY) {
		try {
			EdgeGraphicAttribute ega = (EdgeGraphicAttribute) edge.getAttribute("graphics");
			for (Attribute a : ega.getBends().getCollection().values()) {
				CoordinateAttribute ca = (CoordinateAttribute) a;
				ca.setX(ca.getX() + offX);
				ca.setY(ca.getY() + offY);
			}
		} catch (Exception e) {
			return;
		}
	}
	
	public static boolean hasMappingData(Edge e) {
		try {
			Attribute a = e.getAttribute(Experiment2GraphHelper.mapFolder + Attribute.SEPARATOR + Experiment2GraphHelper.mapVarName);
			return a != null;
		} catch (AttributeNotFoundException anfe) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static void moveBends(Edge e, double moveX, double moveY, HashMap<CoordinateAttribute, Vector2d> bends2newPositions) {
		LinkedHashMapAttribute ha = null;
		try {
			ha = ((LinkedHashMapAttribute) e.getAttribute(AttributeConstants.BENDS));
		} catch (Exception err) {
			// empty
		}
		if (ha == null)
			return;
		Map<?, ?> m = ha.getCollection();
		for (Iterator<?> bi = m.entrySet().iterator(); bi.hasNext();) {
			// transform bends
			Map.Entry en = (Entry<?, ?>) bi.next();
			CoordinateAttribute co = (CoordinateAttribute) en.getValue();
			bends2newPositions.put(co, new Vector2d(co.getX() + moveX, co.getY() + moveY));
		}
	}
	
}
