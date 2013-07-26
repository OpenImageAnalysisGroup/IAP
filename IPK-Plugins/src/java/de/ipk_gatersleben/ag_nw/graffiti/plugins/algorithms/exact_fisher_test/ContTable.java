/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.exact_fisher_test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;

import org.ErrorMsg;

public class ContTable {
	
	private int a, b, c, d, n;
	
	@Override
	public String toString() {
		return "a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + ", n=" + n;
	}
	
	public ContTable(int a, int b, int c, int d, boolean swapIfNeeded) {
		// System.out.println(a+"  "+b+"  "+(a+b));
		// System.out.println(c+"  "+d+"  "+(c+d));
		// System.out.println((a+c)+"  "+(b+d)+"  "+(a+b+c+d));
		
		if (a + b > c + d) {
			int t = a;
			a = c;
			c = t;
			t = b;
			b = d;
			d = t;
		}
		
		if (swapIfNeeded) {
			ContTable c1 = new ContTable(a, b, c, d, false);
			ContTable c2 = new ContTable(b, a, d, c, false);
			FisherProbability result1 = c1.getOneAndTwoSidedFisherProbability(false);
			FisherProbability result2 = c2.getOneAndTwoSidedFisherProbability(false);
			
			if (result1.getOneSidedD() > result2.getOneSidedD()) {
				this.a = a;
				this.b = b;
				this.c = c;
				this.d = d;
			} else {
				this.a = b;
				this.b = a;
				this.c = d;
				this.d = c;
			}
		} else {
			this.a = b;
			this.b = a;
			this.c = d;
			this.d = c;
		}
		this.n = a + b + c + d;
	}
	
	/**
	 * One and two sided Fisher test
	 * Calculate the probability of given arrangement or any more
	 * extreme arrangement.
	 * The p-value for the same or a stronger association.
	 * 
	 * @param calculationCache
	 * @return P one and two sided
	 */
	public FisherProbability getOneAndTwoSidedFisherProbability(boolean alsoCalcTwoSided) {
		ArrayList<BigDecimal> results = new ArrayList<BigDecimal>();
		p_oneSidedSum(a, results);
		BigDecimal res = new BigDecimal(0);
		BigDecimal p_this_table = null;
		for (BigDecimal d : results) {
			if (p_this_table == null)
				p_this_table = d; // first value is probability for given table
			res = res.add(d);
		}
		
		BigDecimal res2 = new BigDecimal(0);
		if (alsoCalcTwoSided) {
			int ta = a + 1;
			int tb = b - 1;
			int tc = c - 1;
			int td = d + 1;
			while (ta >= 0 && tb >= 0) {
				BigDecimal r = getPvalueForGivenTable(ta, tb, tc, td);
				// System.out.println("P("+ta+","+tb+","+tc+","+td+")="+r.doubleValue()+
				// " (p<="+p_this_table.doubleValue()+"?: "+(r.compareTo(p_this_table)<=0 ? "YES" : "NO"));
				ta = ta + 1;
				tb = tb - 1;
				tc = tc - 1;
				td = td + 1;
				if (r.compareTo(p_this_table) <= 0)
					res2 = res2.add(r);
			}
			res2 = res2.add(res);
		}
		return new FisherProbability(res, res2, alsoCalcTwoSided);
	}
	
	private BigDecimal p_values_one_sided(int m, ArrayList<BigDecimal> results) {
		if (m < 0) {
			ErrorMsg.addErrorMessage("Error P(m), m<0!");
			return null;
		}
		if (m == 0) {
			BigDecimal o1 = i(a + b);
			BigDecimal o2 = i(c + d);
			BigDecimal o3 = i(a + c);
			BigDecimal o4 = i(b + d);
			BigDecimal o = o1.multiply(o2.multiply(o3.multiply(o4)));
			
			BigDecimal u1 = i(n);
			BigDecimal u2 = i(a);
			BigDecimal u3 = i(b);
			BigDecimal u4 = i(c);
			BigDecimal u5 = i(d);
			BigDecimal u = u1.multiply(u2.multiply(u3.multiply(u4.multiply(u5))));
			try {
				BigDecimal result = o.divide(u, new MathContext(100));
				results.add(result);
				return result;
			} catch (ArithmeticException e) {
				ErrorMsg.addErrorMessage(e);
				results.add(BigDecimal.ZERO);
				return BigDecimal.ZERO;
			}
		} else {
			double l = (a - m + 1) * (d - m + 1) / (double) (b + m) / (c + m);
			try {
				BigDecimal result = p_oneSidedSum(m - 1, results).multiply(new BigDecimal(l));
				results.add(result);
				return result;
			} catch (StackOverflowError soe) {
				ErrorMsg.addErrorMessage("Stack overflow error at " + m);
				return null;
			}
		}
	}
	
	public static BigDecimal getPvalueForGivenTable(int a, int b, int c, int d) {
		BigDecimal o1 = iCC(a + b);
		BigDecimal o2 = iCC(c + d);
		BigDecimal o3 = iCC(a + c);
		BigDecimal o4 = iCC(b + d);
		BigDecimal o = o1.multiply(o2.multiply(o3.multiply(o4)));
		
		int n = a + b + c + d;
		BigDecimal u1 = iCC(n);
		BigDecimal u2 = iCC(a);
		BigDecimal u3 = iCC(b);
		BigDecimal u4 = iCC(c);
		BigDecimal u5 = iCC(d);
		BigDecimal u = u1.multiply(u2.multiply(u3.multiply(u4.multiply(u5))));
		try {
			BigDecimal result = o.divide(u, new MathContext(100));
			return result;
		} catch (ArithmeticException e) {
			ErrorMsg.addErrorMessage(e);
			return BigDecimal.ZERO;
		}
	}
	
	private BigDecimal p_oneSidedSum(int m, ArrayList<BigDecimal> results) {
		if (m < 0) {
			ErrorMsg.addErrorMessage("Error P(m), m<0!");
			return null;
		}
		if (m == 0) {
			return p_values_one_sided(m, results);
		} else {
			try {
				BigDecimal result = p_values_one_sided(0, results);
				for (int mm = 1; mm <= m; mm++) {
					double l = (a - mm + 1) * (d - mm + 1) / (double) (b + mm) / (c + mm);
					result = result.multiply(new BigDecimal(l));
					results.add(result);
				}
				return result;
			} catch (StackOverflowError soe) {
				ErrorMsg.addErrorMessage("Stack overflow error at " + m);
				return null;
			}
		}
	}
	
	private static BigDecimal i(int n) {
		BigDecimal res = i(new BigDecimal(n));
		return res;
	}
	
	private static BigDecimal bdz = new BigDecimal(0);
	private static BigDecimal bdp1 = new BigDecimal(1);
	private static BigDecimal bdm1 = new BigDecimal(-1);
	
	private static HashMap<BigDecimal, BigDecimal> cachedCalculations = new HashMap<BigDecimal, BigDecimal>();
	
	private static BigDecimal iCC(int n) {
		return iCC(new BigDecimal(n));
	}
	
	/**
	 * @param n
	 * @return n!
	 */
	private static BigDecimal iCC(BigDecimal n) {
		if (n.compareTo(bdz) == 0)
			return bdp1;
		if (cachedCalculations.containsKey(n)) {
			BigDecimal r;
			synchronized (cachedCalculations) {
				r = cachedCalculations.get(n);
			}
			return r;
		} else {
			BigDecimal res = n.multiply(i(n.add(bdm1)));
			synchronized (cachedCalculations) {
				cachedCalculations.put(n, res);
			}
			return res;
		}
	}
	
	private static BigDecimal i(BigDecimal n) {
		if (n.compareTo(bdz) == 0)
			return bdp1;
		BigDecimal res = n;
		while (n.compareTo(bdp1) > 0) { // n>0
			n = n.add(bdm1); // n=n-1
			res = res.multiply(n); // res = res * i
		}
		return res;
	}
	
}
