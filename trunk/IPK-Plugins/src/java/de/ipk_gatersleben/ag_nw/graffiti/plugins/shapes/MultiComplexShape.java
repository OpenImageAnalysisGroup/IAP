package de.ipk_gatersleben.ag_nw.graffiti.plugins.shapes;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collection;

import org.Vector2d;
import org.graffiti.attributes.IntegerAttribute;
import org.graffiti.graphics.NodeGraphicAttribute;
import org.graffiti.plugin.view.ProvidesAdditonalDrawingShapes;

public class MultiComplexShape
					extends RelativePolyShape
					implements ProvidesAdditonalDrawingShapes {
	
	ArrayList<Shape> res = null;
	MultiComplexShape s = null;
	
	private int iOffX = 10;
	private int iOffY = 10;
	
	public MultiComplexShape() {
		
		this.addSx = this.iOffX + 1;
		this.addSy = this.iOffY + 1;
		this.s = new MultiComplexShape(true);
		this.res = new ArrayList<Shape>();
		this.res.add(this.s);
		
	}
	
	public MultiComplexShape(boolean multi) {
		
		this.offX += this.iOffX;
		this.offY += this.iOffY;
		
	}
	
	@Override
	protected Collection<Vector2d> getRelativePointPositions() {
		
		Collection<Vector2d> points = new ArrayList<Vector2d>();
		double off = 0.05;
		double offA = off;
		double offB = (1 - off);
		
		points.add(new Vector2d(0, -offA));
		points.add(new Vector2d(offA, 0));
		points.add(new Vector2d(offB, 0));
		points.add(new Vector2d(1, -offA));
		points.add(new Vector2d(1, -offB));
		points.add(new Vector2d(offB, 1));
		points.add(new Vector2d(offA, 1));
		points.add(new Vector2d(0, -offB));
		return points;
		
	}
	
	@SuppressWarnings("nls")
	@Override
	public void buildShape(NodeGraphicAttribute graphics) {
		
		if (!graphics.getCollection().containsKey("offX"))
			graphics.add(new IntegerAttribute("offX", this.iOffX), false);
		this.iOffX = ((IntegerAttribute) graphics.getAttribute("offX")).getInteger();
		
		if (!graphics.getCollection().containsKey("offY"))
			graphics.add(new IntegerAttribute("offY", this.iOffY), false);
		this.iOffY = ((IntegerAttribute) graphics.getAttribute("offY")).getInteger();
		
		this.addSx = this.iOffX + 1;
		this.addSy = this.iOffY + 1;
		
		if (this.s == null) {
			this.offX = this.iOffX;
			this.offY = this.iOffY;
		}
		
		super.buildShape(graphics);
		
		if (this.s != null) {
			this.s.buildShape(this.nodeAttr);
		}
		
	}
	
	@Override
	public Collection<Shape> getPreBorderShapes() {
		
		if (this.s != null)
			return this.res;
		return null;
		
	}
	
	@Override
	public Collection<Shape> getPostBorderShapes() {
		
		return null;
		
	}
	
	@Override
	public int shapeHeightCorrection() {
		
		return -this.addSy;
		
	}
	
	@Override
	public int shapeWidthCorrection() {
		
		return -this.addSx;
		
	}
	
}
