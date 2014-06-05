package de.ipk.ag_ba.image.operations.complex_hull;

import java.awt.geom.Point2D;

import org.graffiti.plugins.views.defaults.Point2Dfix;

/**
 * @see http://www.iti.fh-flensburg.de/lang/algorithmen/geo/polygon.htm
 *      Public domain information ?!
 */
public class Point {
	public double x, y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Point(Point p) {
		this(p.x, p.y);
	}
	
	public Point(java.awt.Point p) {
		this(p.x, p.y);
	}
	
	public Point relTo(Point p) {
		return new Point(x - p.x, y - p.y);
	}
	
	public Point midPoint(Point p) {
		return new Point((x + p.x) / 2, (y + p.y) / 2);
	}
	
	public void makeRelTo(Point p) {
		x -= p.x;
		y -= p.y;
	}
	
	public Point moved(double x0, double y0) {
		return new Point(x + x0, y + y0);
	}
	
	public Point reversed() {
		return new Point(-x, -y);
	}
	
	public boolean isLower(Point p) {
		return y < p.y || y == p.y && x < p.x;
	}
	
	public double mdist() // Manhattan-Distanz
	{
		return Math.abs(x) + Math.abs(y);
	}
	
	public double mdist(Point p) {
		return relTo(p).mdist();
	}
	
	public boolean isFurther(Point p) {
		return mdist() > p.mdist();
	}
	
	public boolean isBetween(Point p0, Point p1) {
		return p0.mdist(p1) >= mdist(p0) + mdist(p1);
	}
	
	public double cross(Point p) {
		return x * p.y - p.x * y;
	}
	
	public boolean isLess(Point p) {
		double f = cross(p);
		return f > 0 || f == 0 && isFurther(p);
	}
	
	public double area2(Point p0, Point p1) {
		return p0.relTo(this).cross(p1.relTo(this));
	}
	
	public double area2(Line g) {
		return area2(g.p0, g.p1);
	}
	
	public boolean isRightOf(Line g) {
		return area2(g) < 0;
	}
	
	public boolean isConvex(Point p0, Point p1) {
		double f = area2(p0, p1);
		return f < 0 || f == 0 && !isBetween(p0, p1);
	}
	
	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
	
	public double distEuclid(Point p) {
		return Math.sqrt((x - p.x) * (x - p.x) + (y - p.y) * (y - p.y));
	}
	
	public Point2D toPoint2D() {
		return new Point2Dfix(x, y);
	}
	
	@Override
	public String toString() {
		return "x: " + x + ", y: " + y;
	}
	
} // end class Point