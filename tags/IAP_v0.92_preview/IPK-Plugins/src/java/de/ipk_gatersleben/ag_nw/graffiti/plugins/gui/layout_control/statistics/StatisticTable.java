/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

public class StatisticTable {
	// t-Verteilung
	
	public static double teinseitig(int df, double t) {
		double p = Tvertp(t, df);
		return p / 2;
	}
	
	public static double tzweiseitig(int df, double t) {
		double p = Tvertp(t, df);
		return p;
	}
	
	public static double Tvertp(double t, int df) {
		// Returns two-tail probability level given t and df.
		double abst = Math.abs(t);
		double tsq = t * t;
		double p;
		if (df == 1)
			p = 1 - 2 * Math.atan(abst) / Math.PI;
		else
			if (df == 2)
				p = 1 - abst / Math.sqrt(tsq + 2);
			else
				if (df == 3)
					p = 1 - 2 * (Math.atan(abst / Math.sqrt(3)) + abst * Math.sqrt(3) / (tsq + 3)) / Math.PI;
				else
					if (df == 4)
						p = 1 - abst * (1 + 2 / (tsq + 4)) / Math.sqrt(tsq + 4);
					else {
						double z = Tvertz(abst, df);
						if (df > 4)
							p = NormalP(z);
						else
							p = NormalP(z);
					}
		return p;
	}
	
	public static double Tvertz(double t, int df) {
		double A9 = df - 0.5;
		double B9 = 48 * A9 * A9;
		double T9 = t * t / df;
		double Z8, P7, B7, z;
		
		if (T9 >= 0.04)
			Z8 = A9 * Math.log(1 + T9);
		else
			Z8 = A9 * (((1 - T9 * 0.75) * T9 / 3 - 0.5) * T9 + 1) * T9;
		P7 = ((0.4 * Z8 + 3.3) * Z8 + 24) * Z8 + 85.5;
		B7 = 0.8 * Math.pow(Z8, 2) + 100 + B9;
		z = (1 + (-P7 / B7 + Z8 + 3) / B9) * Math.sqrt(Z8);
		return z;
	}
	
	public static double tinvers(double p, int df) {
		double a, b, c, d, t, x, y;
		if (df == 1)
			t = Math.cos(p * Math.PI / 2) / Math.sin(p * Math.PI / 2);
		else
			if (df == 2)
				t = Math.sqrt(2 / (p * (2 - p)) - 2);
			else {
				a = 1 / (df - 0.5);
				b = 48 / (a * a);
				c = ((20700 * a / b - 98) * a - 16) * a + 96.36;
				d = ((94.5 / (b + c) - 3) / b + 1) * Math.sqrt(a * Math.PI * 0.5) * df;
				x = d * p;
				y = Math.pow(x, 2 / df);
				if (y > 0.05 + a) {
					x = Normz(0.5 * (1 - p));
					y = x * x;
					if (df < 5)
						c = c + 0.3 * (df - 4.5) * (x + 0.6);
					c = (((0.05 * d * x - 5) * x - 7) * x - 2) * x + b + c;
					y = (((((0.4 * y + 6.3) * y + 36) * y + 94.5) / c - y - 3) / b + 1) * x;
					y = a * y * y;
					if (y > 0.002)
						y = Math.exp(y) - 1;
					else
						y = 0.5 * y * y + y;
					t = Math.sqrt(df * y);
				} else {
					y = ((1 / (((df + 6) / (df * y) - 0.089 * d - 0.822) * (df + 2) * 3)
										+ 0.5 / (df + 4)) * y - 1) * (df + 1) / (df + 2) + 1 / y;
					t = Math.sqrt(df * y);
				}
			}
		return t;
	}
	
	public static double backwardT(double p, int df) {
		if (p <= 0 || p >= 1 || df < 1) {
			return Double.NaN;
		}
		p = (1 - p) * 2;
		double p1 = p;
		double p0 = p;
		double diff = 1;
		double t = 0;
		while (Math.abs(diff) > .0001) {
			t = tinvers(p1, df);
			diff = Tvertp(t, df) - p0;
			p1 -= diff;
		}
		return PosV(t);
	}
	
	public static double PosV(double x) {
		if (x < 0)
			x = -x;
		return x;
	}
	
	// Normalverteilung
	
	public static double NormalP(double z) {
		double d1 = 0.0498673470, d2 = 0.0211410061, d3 = 0.0032776263, d4 = 0.0000380036, d5 = 0.0000488906, d6 = 0.0000053830;
		
		double a = Math.abs(z);
		double p = 1.0 + a * (d1 + a * (d2 + a * (d3 + a * (d4 + a * (d5 + a * d6)))));
		
		p = Math.pow(p, -16);
		return p;
	}
	
	public static double Normz(double p) {
		double a0 = 2.5066282, a1 = -18.6150006, a2 = 41.3911977, a3 = -25.4410605, b1 = -8.4735109, b2 = 23.0833674, b3 = -21.0622410, b4 = 3.1308291, c0 = -2.7871893, c1 = -2.2979648, c2 = 4.8501413, c3 = 2.3212128, d1 = 3.5438892, d2 = 1.6370678, r, z;
		
		if (p > 0.42) {
			r = Math.sqrt(-Math.log(0.5 - p));
			z = (((c3 * r + c2) * r + c1) * r + c0) / ((d2 * r + d1) * r + 1);
		} else {
			r = p * p;
			z = p * (((a3 * r + a2) * r + a1) * r + a0) / ((((b4 * r + b3) * r + b2) * r + b1) * r + 1);
		}
		return z;
	}
	
}