/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.10.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kgml;

import java.awt.geom.Point2D;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Vector2d {
	
	public double x;
	
	public double y;
	
	public Vector2d(double a, double b) {
		x = a;
		y = b;
	}
	
	@Override
	public String toString() {
		return "Vector2d[" + x + ", " + y + "]";
	}
	
	/**
	 * @param position
	 */
	public Vector2d(Point2D position) {
		x = position.getX();
		y = position.getY();
	}
	
	public Vector2d(Vector2d position) {
		x = position.x;
		y = position.y;
	}
	
}
