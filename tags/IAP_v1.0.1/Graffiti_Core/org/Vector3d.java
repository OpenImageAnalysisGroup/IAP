/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 28.10.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org;

import java.awt.geom.Point2D;

/**
 * @author Christian Klukas
 *         To change the template for this generated type comment go to
 *         Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Vector3d {
	
	public double x;
	public double y;
	public double z;
	
	public Vector3d(double a, double b, double c) {
		x = a;
		y = b;
		z = c;
	}
	
	@Override
	public String toString() {
		return "Vector3d[" + x + ", " + y + ", " + z + "]";
	}
	
	/**
	 * @param position
	 */
	public Vector3d(Point2D position) {
		x = position.getX();
		y = position.getY();
		z = 0;
	}
	
	public Vector3d(Vector3d position) {
		x = position.x;
		y = position.y;
		z = position.z;
	}
	
	public Point2D getPoint2D() {
		return new Point2D.Double(x, y);
	}
	
}
