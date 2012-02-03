package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import org.HelperClass;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.Edge;
import org.graffiti.graphics.CoordinateAttribute;
import org.graffiti.graphics.EdgeGraphicAttribute;

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
	
}
