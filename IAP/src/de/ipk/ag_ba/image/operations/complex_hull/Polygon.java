package de.ipk.ag_ba.image.operations.complex_hull;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

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
	
	// Reference: E. Welzl, "Smallest enclosing disks (balls and ellipsoids)",Lecture Notes in Computer Science, Vol. 555, 1991, pp. 359-370.
	// code based on http://web.nmsu.edu/~xiumin/project/smallest_enclosing_circle/
	// Compute the Smallest Enclosing Circle of the n points in p,
	// such that the m points in B lie on the boundary of the circle.
	public Circle findSec(int n, Point[] p, int m, Point[] b)
	{
		Circle sec = new Circle();
		
		// Compute the Smallest Enclosing Circle defined by B
		if (m == 1)
		{
			sec = new Circle(b[0]);
		}
		else
			if (m == 2)
			{
				sec = new Circle(b[0], b[1]);
			}
			else
				if (m == 3)
				{
					return new Circle(b[0], b[1], b[2]);
				}
		
		// Check if all the points in p are enclosed
		for (int i = 0; i < n; i++)
		{
			if (sec.contain(p[i]) == false)
			{
				// Compute B <--- B union P[i].
				b[m] = new Point(p[i]);
				// Recurse
				sec = findSec(i, p, m + 1, b);
			}
		}
		
		return sec;
	}
	
	public Circle calculateminimalcircumcircle() {
		Point[] temp = new Point[3];
		return findSec(N, points, 0, temp);
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
	
	public List<java.awt.Point> getPoints() {
		List<java.awt.Point> res = new ArrayList<java.awt.Point>();
		for (Point p : points)
			res.add(new java.awt.Point((int) p.x, (int) p.y));
		return res;
	}
}