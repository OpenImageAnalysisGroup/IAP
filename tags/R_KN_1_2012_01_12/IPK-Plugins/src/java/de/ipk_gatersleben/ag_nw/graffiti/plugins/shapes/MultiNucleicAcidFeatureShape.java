package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;

public class MultiNucleicAcidFeatureShape
					extends RelativePolyShape
					implements ProvidesAdditonalDrawingShapes {
	ArrayList<Shape> res = null;
	MultiNucleicAcidFeatureShape s = null;
	
	private int iOffX = 10;
	private int iOffY = 10;
	
	public MultiNucleicAcidFeatureShape() {
		addSx = iOffX + 1;
		addSy = iOffY + 1;
		s = new MultiNucleicAcidFeatureShape(true);
		res = new ArrayList<Shape>();
		res.add(s);
	}
	
	public MultiNucleicAcidFeatureShape(boolean multi) {
		offX += iOffX;
		offY += iOffY;
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		points.add(new Vector2d(0, 0));
		points.add(new Vector2d(1, 0));
		points.addAll(getRoundingRightBottom());
		points.addAll(getRoundingLeftBottom());
		return points;
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
