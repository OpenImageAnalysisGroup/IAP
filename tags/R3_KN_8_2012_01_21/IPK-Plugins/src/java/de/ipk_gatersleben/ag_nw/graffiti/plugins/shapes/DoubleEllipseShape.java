package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.graphics.DimensionAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.EllipseNodeShape;

public class DoubleEllipseShape
					extends EllipseNodeShape
					implements ProvidesAdditonalDrawingShapes {
	ArrayList<Shape> res = null;
	DoubleEllipseShape s = null;
	
	private int iOffX = 0;
	private int iOffY = 0;
	
	public DoubleEllipseShape() {
		addSx = 0;
		addSy = 0;
		s = new DoubleEllipseShape(true);
		res = new ArrayList<Shape>();
		res.add(s);
	}
	
	public DoubleEllipseShape(boolean multi) {
		offX += iOffX;
		offY += iOffY;
	}
	
	public Collection<Shape> getPostBorderShapes() {
		return null;
	}
	
	@Override
	public void buildShape(NodeGraphicAttribute graphics) {
		this.nodeAttr = graphics;
		
		DimensionAttribute dim = nodeAttr.getDimension();
		double w = dim.getWidth();
		double h = dim.getHeight();
		double rr = graphics.getRoundedEdges() + graphics.getFrameThickness();
		addSx = (int) (rr * 2);
		addSy = (int) (rr * 2);
		if (s != null) {
			w -= rr * 2;
			h -= rr * 2;
			offX = (int) rr;
			offY = (int) rr;
		}
		
		double ft = Math.floor(nodeAttr.getFrameThickness());
		double offset = ft / 2d;
		this.ell2D.setFrame(offset + offX, offset + offY, w, h);
		
		double corwidth = w + ft;
		double corheight = h + ft;
		
		if (Math.floor(offset) == offset) {
			corwidth = w + ft + 1;
			corheight = h + ft + 1;
		}
		
		((RectangularShape) this.thickShape).setFrame(0, 0, corwidth + addSx, corheight + addSy);
		
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
}
