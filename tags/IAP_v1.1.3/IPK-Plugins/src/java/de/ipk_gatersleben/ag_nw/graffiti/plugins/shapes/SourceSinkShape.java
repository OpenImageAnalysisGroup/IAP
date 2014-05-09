package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.CircleNodeShape;

public class SourceSinkShape
					extends CircleNodeShape
					implements ProvidesAdditonalDrawingShapes {
	
	public Collection<Shape> getPostBorderShapes() {
		Rectangle2D r = getBounds2D();
		Shape shape = new Line2D.Double(r.getMinX() + 1, r.getMaxY() - 1, r.getMaxX() - 1, r.getMinY() + 1);
		ArrayList<Shape> result = new ArrayList<Shape>();
		result.add(shape);
		return result;
	}
	
	public Collection<Shape> getPreBorderShapes() {
		return null;
	}
}
