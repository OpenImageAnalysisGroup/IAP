package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.RectangleNodeShape;

public class MultiRectangleShape
					extends RectangleNodeShape
					implements ProvidesAdditonalDrawingShapes {
	ArrayList<Shape> res = null;
	MultiRectangleShape s = null;
	
	private int iOffX = 10;
	private int iOffY = 10;
	
	public MultiRectangleShape() {
		addSx = iOffX + 1;
		addSy = iOffY + 1;
		s = new MultiRectangleShape(true);
		res = new ArrayList<Shape>();
		res.add(s);
	}
	
	public MultiRectangleShape(boolean multi) {
		offX = iOffX;
		offY = iOffY;
	}
	
	@Override
	public Point2D getIntersection(Line2D line) {
		
		Rectangle2D rect = getRealBounds2D();
		double x = rect.getCenterX();
		double y = rect.getCenterY();
		double w = rect.getWidth();
		double h = rect.getHeight();
		x -= iOffX / 2d;
		y -= iOffY / 2d;
		w -= iOffX;
		h -= iOffY;
		rect.setFrame(x - w / 2, y - h / 2, w, h);
		
		double rounding = nodeAttr.getRoundedEdges() / 2;
		
		return RectangleNodeShape.getIntersectionOfRoundRectangleAndLine(line, rect, rounding);
	}
	
	public Collection<Shape> getPostBorderShapes() {
		return null;
	}
	
	@Override
	public void buildShape(NodeGraphicAttribute graphics) {
		if (!graphics.getCollection().containsKey("offX"))
			graphics.add(new IntegerAttribute("offX", iOffX), false);
		iOffX = ((IntegerAttribute) graphics.getAttribute("offX")).getInteger();
		
		if (!graphics.getCollection().containsKey("offY"))
			graphics.add(new IntegerAttribute("offY", iOffY), false);
		iOffY = ((IntegerAttribute) graphics.getAttribute("offY")).getInteger();
		
		addSx = iOffX + 1;
		addSy = iOffY + 1;
		
		if (s == null) {
			offX = iOffX;
			offY = iOffY;
		}
		
		super.buildShape(graphics);
		
		if (s != null) {
			s.buildShape(nodeAttr);
		}
	}
	
	public Collection<Shape> getPreBorderShapes() {
		if (s != null) {
			return res;
		} else
			return null;
	}
	
	@Override
	public int shapeHeightCorrection() {
		return -addSy;
	}
	
	@Override
	public int shapeWidthCorrection() {
		return -addSx;
	}
}
