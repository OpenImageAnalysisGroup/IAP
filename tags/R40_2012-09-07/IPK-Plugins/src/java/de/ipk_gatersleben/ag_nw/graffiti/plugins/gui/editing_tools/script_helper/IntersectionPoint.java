package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.awt.geom.Point2D;

public class IntersectionPoint {
	
	private final Point2D intersection;
	
	public IntersectionPoint(Point2D intersection) {
		this.intersection = intersection;
	}
	
	public double getX() {
		return intersection.getX();
	}
	
	public double getY() {
		return intersection.getY();
	}
	
}
