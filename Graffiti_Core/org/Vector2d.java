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
	
	public Vector2d(Vector2d p1, Vector2d p2) {
		x = (p1.x + p2.x) / 2d;
		y = (p1.y + p2.y) / 2d;
	}
	
	public Vector2d(Point2D p1, Point2D p2) {
		x = p1.getX() - p2.getX();
		y = p1.getY() - p2.getY();
	}
	
	public Point2D getPoint2D() {
		return new Point2D.Double(x, y);
	}
	
	public double distance(Vector2d point) {
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
	
	public Vector2d getOrthogonal() {
		return new Vector2d(-y, x);
	}
	
	public Vector2d scale(double scalingFactor) {
		return new Vector2d(x * scalingFactor, y * scalingFactor);
	}
}
