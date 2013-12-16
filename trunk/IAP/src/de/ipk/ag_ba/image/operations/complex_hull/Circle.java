package de.ipk.ag_ba.image.operations.complex_hull;

public class Circle {
	
	double x;
	double y;
	double d;
	
	/**
	 * @param x
	 *           x coordinate of the center
	 * @param y
	 *           y coordinate of the center
	 * @param d
	 *           diameter of the circle
	 */
	public Circle(int x, int y, int d) {
		this.x = x;
		this.y = y;
		this.d = d;
	}
	
	public Circle(Point x, Point y, Point z) {
		double a = x.x;
		double b = x.y;
		double c = y.x;
		double d = y.y;
		double e = z.x;
		double f = z.y;
		
		b = -1.0f * b;
		d = -1.0f * d;
		f = -1.0f * f;
		double k = ((sq(a) + sq(b)) * (e - c)
				+ (sq(c) + sq(d)) * (a - e) + (sq(e) + sq(f)) * (c - a)) /
				(2.0f * (b * (e - c) + d * (a - e) + f * (c - a)));
		double h = ((sq(a) + sq(b)) * (f - d) + (sq(c) + sq(d)) * (b - f) +
				(sq(e) + sq(f)) * (d - b)) / (2.0f * (a * (f - d) + c * (b - f) + e * (d - b)));
		
		double r = ((float) Math.sqrt(sq(a - h) + sq(b - k)));
		
		this.x = h;
		this.y = -1 * k;
		this.d = 2 * r;
	}
	
	public Circle() {
		x = 0;
		y = 0;
		d = 0;
	}
	
	public Circle(Point point) {
		x = point.x;
		y = point.y;
		d = 0;
	}
	
	public Circle(Point point, Point point2) {
		Point temp = point.midPoint(point2);
		x = temp.x;
		y = temp.y;
		d = point.distEuclid(point2);
	}
	
	private double sq(double x) {
		return (x * x);
	}
	
	public double area() {
		return Math.PI * sq(d / 2);
	}
	
	@Override
	public String toString() {
		return "circle: x: " + x + ", y: " + y + ", d: " + d;
	}
	
	// if on border, point is inside
	public boolean contain(Point p) {
		boolean inside = false;
		Point midPoint = new Point(x, y);
		double dist = midPoint.distEuclid(p);
		if (dist <= d / 2)
			inside = true;
		return inside;
	}
}
