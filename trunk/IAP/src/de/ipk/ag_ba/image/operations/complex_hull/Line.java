package de.ipk.ag_ba.image.operations.complex_hull;

public class Line {
	public Point p0;
	public Point p1;
	
	public Point getP0() {
		return p0;
	}
	
	public void setP0(Point p0) {
		this.p0 = p0;
	}
	
	public Point getP1() {
		return p1;
	}
	
	public void setP1(Point p1) {
		this.p1 = p1;
	}
	
	public Line(Point p0, Point p1) {
		this.p0 = p0;
		this.p1 = p1;
	}
	
	public int getlength() {
		return (int) Math.sqrt(((p0.x - p1.x) * (p0.x - p1.x)) + ((p0.y - p1.y) * (p0.y - p1.y)));
	}
}
