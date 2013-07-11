package de.ipk.ag_ba.image.operations.complex_hull;

/**
 * @see http://www.iti.fh-flensburg.de/lang/algorithmen/geo/quickhull.htm
 *      Public domain information ?!
 */
class QuickHull {
	private Point[] p;
	private int n;
	private int h;
	
	private final static double eps = 1e-3;
	
	public int computeHull(Point[] p_) {
		p = p_;
		n = p.length;
		h = 0;
		quickHull();
		return h;
	}
	
	private void quickHull() {
		exchange(0, indexOfLowestPoint());
		h++;
		Line g = new Line(p[0], p[0].moved(-eps, 0));
		computeHullPoints(g, 1, n - 1);
	}
	
	private void computeHullPoints(Line g, int lo, int hi) {
		if (lo > hi)
			return;
		int k = indexOfFurthestPoint(g, lo, hi);
		Line g0 = new Line(g.p0, p[k]);
		Line g1 = new Line(p[k], g.p1);
		exchange(k, hi);
		
		int i = partition(g0, lo, hi - 1);
		// alle Punkte von lo bis i-1 liegen rechts von g0
		// alle Punkte von i bis hi-1 liegen links von g0
		computeHullPoints(g0, lo, i - 1);
		
		// alle eben rekursiv erzeugten Punkte liegen
		// auf dem Hüllpolygonzug vor p[hi]
		exchange(hi, i);
		exchange(i, h);
		h++;
		
		int j = partition(g1, i + 1, hi);
		// alle Punkte von i+1 bis j-1 liegen rechts von g1,
		// alle Punkte von j bis hi liegen im Inneren
		computeHullPoints(g1, i + 1, j - 1);
	}
	
	private int indexOfLowestPoint() {
		int i, min = 0;
		for (i = 1; i < n; i++)
			if (p[i].y < p[min].y || p[i].y == p[min].y && p[i].x < p[min].x)
				min = i;
		return min;
	}
	
	private void exchange(int i, int j) {
		Point t = p[i];
		p[i] = p[j];
		p[j] = t;
	}
	
	private int indexOfFurthestPoint(Line g, int lo, int hi) {
		int i, f = lo;
		double d, mx = 0;
		for (i = lo; i <= hi; i++) {
			d = -p[i].area2(g);
			if (d > mx || d == mx && p[i].x > p[f].x) {
				mx = d;
				f = i;
			}
		}
		return f;
	}
	
	private int partition(Line g, int lo, int hi) {
		int i = lo, j = hi;
		while (i <= j) {
			while (i <= j && p[i].isRightOf(g))
				i++;
			while (i <= j && !p[j].isRightOf(g))
				j--;
			if (i <= j)
				exchange(i++, j--);
		}
		return i;
	}
	
} // end class QuickHull