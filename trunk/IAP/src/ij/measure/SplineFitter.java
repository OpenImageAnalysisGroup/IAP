package ij.measure;

/**
 * This class fits a spline function to a set of points.
 * It is based on the InitSpline() and EvalSine() functions from
 * XY (http://www.trilon.com/xv/), an interactive image manipulation
 * program for the X Window System written by John Bradley. Eric Kischell
 * (keesh@ieee.org) converted these functions to Java and inteegrated
 * them into the PolygonRoi class.
 */
public class SplineFitter {
	private double[] y2;
	
	public SplineFitter(int[] x, int[] y, int n) {
		initSpline(x, y, n);
	}
	
	/**
	 * Given arrays of data points x[0..n-1] and y[0..n-1], computes the
	 * values of the second derivative at each of the data points
	 * y2[0..n-1] for use in the splint function.
	 */
	void initSpline(int[] x, int[] y, int n) {
		int i, k;
		double p, qn, sig, un;
		y2 = new double[n]; // cached
		double[] u = new double[n];
		for (i = 1; i < n - 1; i++) {
			// 888 chk for div by 0?
			sig = ((double) x[i] - x[i - 1]) / ((double) x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (((double) y[i + 1] - y[i]) / (x[i + 1] - x[i])) -
					(((double) y[i] - y[i - 1]) / (x[i] - x[i - 1]));
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}
		qn = un = 0.0;
		y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
		for (k = n - 2; k >= 0; k--)
			y2[k] = y2[k] * y2[k + 1] + u[k];
	}
	
	/** Evalutes spline function at given point */
	public double evalSpline(int x[], int y[], int n, double xp) {
		int klo, khi, k;
		double h, b, a;
		klo = 0;
		khi = n - 1;
		while (khi - klo > 1) {
			k = (khi + klo) >> 1;
			if (x[k] > xp)
				khi = k;
			else
				klo = k;
		}
		h = x[khi] - x[klo];
		/* orig code */
		/* if (h==0.0) FatalError("bad xvalues in splint\n"); */
		if (h == 0.0)
			return (0.0); /* arbitr ret for now */
		a = (x[khi] - xp) / h;
		b = (xp - x[klo]) / h;
		
		// should have better err checking
		if (y2 == null)
			return (0.0);
		
		return (a * y[klo] + b * y[khi] + ((a * a * a - a) * y2[klo] + (b * b * b - b) * y2[khi])
				* (h * h) / 6.0);
	}
}
