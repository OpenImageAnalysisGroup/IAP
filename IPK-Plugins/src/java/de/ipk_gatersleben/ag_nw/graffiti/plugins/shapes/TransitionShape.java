package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

public class TransitionShape
					extends RectangleNodeShape
					implements ProvidesAdditonalDrawingShapes {
	
	public Collection<Shape> getPostBorderShapes() {
		Rectangle2D r = getBounds2D();
		Shape shapeA = null;
		Shape shapeB = null;
		double rounding = super.getRounding();
		if (rounding <= -1) {
			// vertical transition
			rounding = -rounding;
			shapeA = new Line2D.Double(r.getCenterX(), r.getMinY() - rounding, r.getCenterX(), r.getMinY());
			shapeB = new Line2D.Double(r.getCenterX(), r.getMaxY() - 1, r.getCenterX(), r.getMaxY() + rounding);
		} else
			if (rounding >= 1) {
				// horizontal transition
				shapeA = new Line2D.Double(r.getMinX() - rounding, r.getCenterY(), r.getMinX(), r.getCenterY());
				shapeB = new Line2D.Double(r.getMaxX() - 1, r.getCenterY(), r.getMaxX() + rounding, r.getCenterY());
			} else
				return null;
		ArrayList<Shape> result = new ArrayList<Shape>();
		result.add(shapeA);
		result.add(shapeB);
		return result;
	}
	
	public Collection<Shape> getPreBorderShapes() {
		return null;
	}
	
	@Override
	protected double getRounding() {
		return 0;
	}
	
	@Override
	public double getXexcess() {
		double rounding = super.getRounding();
		if (rounding > 0)
			return rounding + arrowWidth;
		else
			return 0;
	}
	
	@Override
	public double getYexcess() {
		double rounding = super.getRounding();
		if (rounding < 0)
			return -rounding + arrowWidth;
		else
			return 0;
	}
}
