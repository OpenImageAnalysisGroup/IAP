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
 */
public class Vector2i {
	public int x;
	public int y;
	
	public Vector2i() {
		// empty
	}
	
	public Vector2i(int a, int b) {
		x = a;
		y = b;
	}
	
	@Override
	public String toString() {
		return "Vector2i[" + x + ", " + y + "]";
	}
	
	public Vector2i(Vector2i position) {
		x = position.x;
		y = position.y;
	}
	
	public Vector2i(Vector2i p1, Vector2i p2) {
		x = (p1.x + p2.x) / 2;
		y = (p1.y + p2.y) / 2;
	}
	
	public Point2D getPoint2D() {
		return new Point2D.Double(x, y);
	}
	
	public double distance(Vector2i point) {
		return Math.sqrt((point.x - x) * (point.x - x) + (point.y - y) * (point.y - y));
	}
	
	public void applyGrid(int xg, int yg) {
		x = x - (x % xg);
		y = y - (y % yg);
	}
	
	public double minXY() {
		if (x < y)
			return x;
		else
			return y;
	}
	
	public double maxXY() {
		if (x > y)
			return x;
		else
			return y;
	}
	
	public double distance(double px, double py) {
		return Math.sqrt((px - x) * (px - x) + (py - y) * (py - y));
	}
	
	public Vector2i getOrthogonal() {
		return new Vector2i(-y, x);
	}
	
	public Vector2i scale(double scalingFactor) {
		return new Vector2i((int) (x * scalingFactor), (int) (y * scalingFactor));
	}
}
