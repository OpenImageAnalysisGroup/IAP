package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;
import org.graffiti.plugins.views.defaults.EllipseNodeShape;

public class MultiEllipseShape
					extends EllipseNodeShape
					implements ProvidesAdditonalDrawingShapes {
	ArrayList<Shape> res = null;
	MultiEllipseShape s = null;
	
	private int iOffX = 10;
	private int iOffY = 10;
	
	public MultiEllipseShape() {
		addSx = iOffX + 1;
		addSy = iOffY + 1;
		s = new MultiEllipseShape(true);
		res = new ArrayList<Shape>();
		res.add(s);
	}
	
	public MultiEllipseShape(boolean multi) {
		offX += iOffX;
		offY += iOffY;
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
