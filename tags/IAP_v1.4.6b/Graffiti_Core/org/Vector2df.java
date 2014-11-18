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
public class Vector2df {
	
	public float x;
	public float y;
	
	public Vector2df(float a, float b) {
		x = a;
		y = b;
	}
	
	@Override
	public String toString() {
		return "Vector2df[" + x + ", " + y + "]";
	}
	
	/**
	 * @param position
	 */
	public Vector2df(Point2D position) {
		x = (float) position.getX();
		y = (float) position.getY();
	}
	
	public Vector2df(Vector2df position) {
		x = position.x;
		y = position.y;
	}
	
	public Vector2df(Vector2d position) {
		x = (float) position.x;
		y = (float) position.y;
	}
	
	public Point2D getPoint2D() {
		return new Point2D.Float(x, y);
	}
	
}
