package de.ipk.ag_ba.image.operations.complex_hull;

import java.awt.Rectangle;
import java.awt.geom.Line2D;

/*************************************************************************
 * Compilation: javac Polygon.java
 * Execution: java Polygon
 * An immutable datat type for polygons, possibly intersecting.
 * Centroid calculation assumes polygon is nonempty (o/w area = 0)
 *************************************************************************/

/**
 * @see http://introcs.cs.princeton.edu/35purple/Polygon.java.html
 *      Public domain?!
 *      Removed all unneeded methods, added some new code.
 */
class Polygon {
	private final int N; // number of points in the polygon
	private final Point[] points; // the points, setting p[0] = p[N]
	
	// defensive copy
	public Polygon(Point[] points) {
		N = points.length;
		this.points = new Point[N + 1];
		for (int i = 0; i < N; i++)
			this.points[i] = points[i];
		this.points[N] = points[0];
	}
	
	// return size
	public int size() {
		return N;
	}
	
	// return area of polygon
	public double area() {
		return Math.abs(signedArea());
	}
	
	// return signed area of polygon
	public double signedArea() {
		double sum = 0.0;
		for (int i = 0; i < N; i++) {
			sum = sum + (points[i].x() * points[i + 1].y()) - (points[i].y() * points[i + 1].x());
		}
		return 0.5 * sum;
	}
	
	// are vertices in counterclockwise order?
	// assumes polygon does not intersect itself
	public boolean isCCW() {
		return signedArea() > 0;
	}
	
	// return the centroid of the polygon
	public Point centroid() {
		double cx = 0.0, cy = 0.0;
		for (int i = 0; i < N; i++) {
			cx = cx + (points[i].x() + points[i + 1].x()) * (points[i].y() * points[i + 1].x() - points[i].x() * points[i + 1].y());
			cy = cy + (points[i].y() + points[i + 1].y()) * (points[i].y() * points[i + 1].x() - points[i].x() * points[i + 1].y());
		}
		cx = Math.abs(cx / (6 * area()));
		cy = Math.abs(cy / (6 * area()));
		return new Point(cx, cy);
	}
	
	// does this Polygon contain the point p?
	// if p is on boundary then 0 or 1 is returned, and p is in exactly one point of every partition of plane
	// Reference: http://exaflop.org/docs/cgafaq/cga2.html
	public boolean contains(Point p0) {
		int crossings = 0;
		for (int i = 0; i < N; i++) {
			double slope = (points[i + 1].x() - points[i].x()) / (points[i + 1].y() - points[i].y());
			boolean cond1 = (points[i].y() <= p0.y()) && (p0.y() < points[i + 1].y());
			boolean cond2 = (points[i + 1].y() <= p0.y()) && (p0.y() < points[i].y());
			boolean cond3 = p0.x() < slope * (p0.y() - points[i].y()) + points[i].x();
			if ((cond1 || cond2) && cond3)
				crossings++;
		}
		return (crossings % 2 != 0);
	}
	
	/**
	 * @author klukas
	 */
	public java.awt.Polygon getGraphics2Dpolygon() {
		int[] xpoints = new int[N];
		int[] ypoints = new int[N];
		int npoints = N;
		for (int i = 0; i < N; i++) {
			xpoints[i] = (int) points[i].x;
			ypoints[i] = (int) points[i].y;
		}
		java.awt.Polygon res = new java.awt.Polygon(xpoints, ypoints, npoints);
		return res;
	}
	
	public Circle calculateminimalcircumcircle() {
		if (true)
			return bruteForceMcc();
		
		if (points != null) {
			Line maxSpan = getMaxSpan();
			double maxDist = 0;
			Circle result = null;
			
			for (int index = 0; index < points.length; index++) {
				double ptDistToCenterOfLine = ptDistToCenterOfLine(maxSpan, points[index]);
				if (maxDist < ptDistToCenterOfLine && maxSpan.getlength() / 2 < ptDistToCenterOfLine) {
					maxDist = ptDistToCenterOfLine(maxSpan, points[index]);
					Circle temp = new Circle(maxSpan.p0, maxSpan.p1, points[index]);
					result = temp;
				}
			}
			if (containAllPoints(result)) {
				return result;
			} else {
				return bruteForceMcc();
				// return null;
			}
		} else {
			return null;
		}
	}
	
	private Circle bruteForceMcc() {
		Circle result = new Circle(0, 0, Integer.MAX_VALUE);
		
		for (int i = 0; i < points.length; i++) {
			for (int j = i; j < points.length; j++) {
				for (int k = j; k < points.length; k++) {
					Circle temp = new Circle(points[i], points[j], points[k]);
					if (containAllPoints(temp) && temp.d < result.d)
						result = temp;
				}
			}
		}
		return result;
	}
	
	private boolean containAllPoints(Circle input) {
		if (input != null) {
			Point center = new Point(input.x, input.y);
			
			for (int i = 0; i < points.length; i++) {
				double distance = center.distEuclid(points[i]);
				if (distance - input.d / 2 > 1) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int index = 0; index < points.length; index++) {
			sb.append(points[index] + ";");
		}
		sb.append("]");
		return sb.toString();
	}
	
	private double ptDistToCenterOfLine(Line maxSpan, Point point) {
		double midX = (maxSpan.p0.x + maxSpan.p1.x) / 2;
		double midY = (maxSpan.p0.y + maxSpan.p1.y) / 2;
		Point temp = new Point(midX, midY);
		
		return temp.distEuclid(point);
	}
	
	public Line getMaxSpan() {
		double maxDist = 0;
		Line result = null;
		
		for (int i = 0; i < points.length; i++) {
			for (int j = i + 1; j < points.length; j++) {
				if (maxDist < points[i].distEuclid(points[j])) {
					maxDist = points[i].distEuclid(points[j]);
					Line temp = new Line(points[i], points[j]);
					result = temp;
				}
			}
		}
		return result;
	}
	
	/**
	 * this method only works, if the polygon is the convex hull and the points are in order
	 * 
	 * @return
	 */
	public int perimeter() {
		double distance = 0;
		
		for (int index = 0; index < points.length; index++) {
			distance += points[index].distEuclid(points[(index + 1) % points.length]);
		}
		return (int) distance;
	}
	
	public Rectangle getRectangle() {
		int xMin = Integer.MAX_VALUE;
		int xMax = 0;
		int yMin = Integer.MAX_VALUE;
		int yMax = 0;
		for (Point p : points) {
			if (p.x < xMin)
				xMin = (int) p.x;
			if (p.y < yMin)
				yMin = (int) p.y;
			if (p.x > xMax)
				xMax = (int) p.x;
			if (p.y > yMax)
				yMax = (int) p.y;
		}
		Rectangle res = new Rectangle(xMin, yMin, xMax - xMin, yMax - yMin);
		return res;
	}
	
	public Span2result getMaxSpan2len(Line span) {
		// double res = Double.NaN;
		Line2D.Double l = new Line2D.Double(span.getP0().x, span.getP0().y, span.getP1().x, span.getP1().y);
		Point p1 = null, p1l = null;
		double distP1 = Double.NaN;
		Point p2 = null, p2l = null;
		double distP2 = Double.NaN;
		for (int i = 0; i < points.length; i++) {
			double dist = l.ptLineDist(points[i].x, points[i].y);
			// if (Double.isNaN(res) || dist > res)
			// res = dist;
			
			int ccw = l.relativeCCW(points[i].x, points[i].y);
			if (ccw < 0) {
				if ((Double.isNaN(distP1) || dist > distP1) && dist >= 1) {
					p1 = points[i];
					distP1 = dist;
					org.Vector2d vec = new org.Vector2d(l.x2 - l.x1, l.y2 - l.y1);
					vec = vec.scale(1 / vec.distance(0, 0) * distP1);
					vec = vec.rotate(-Math.PI / 2d);
					vec.x += p1.x;
					vec.y += p1.y;
					p1l = new Point(vec.x, vec.y);
				}
			}
			if (ccw > 0) {
				if ((Double.isNaN(distP2) || dist > distP2) && dist >= 1) {
					p2 = points[i];
					distP2 = dist;
					org.Vector2d vec = new org.Vector2d(l.x2 - l.x1, l.y2 - l.y1);
					vec = vec.scale(1 / vec.distance(0, 0) * distP2);
					vec = vec.rotate(Math.PI / 2d);
					vec.x += p2.x;
					vec.y += p2.y;
					p2l = new Point(vec.x, vec.y);
				}
			}
		}
		// (res
		return new Span2result(l.getP1().distance(l.getP2()), p1, p2, p1l, p2l, distP1, distP2);
	}
}