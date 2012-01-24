/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.add_attributes;

import java.util.ArrayList;
import java.util.Collection;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.DoubleAndSourceList;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.TabStatistics;

public enum AttributeCalculation {
	A_PLUS_B, A_MINUS_B, A_MULT_B, A_DIV_B, A_POW_B, A_POW_INV_B, A_ABS, A, A_SGN,
	A_B_MAX, A_B_MIN, A_LOG, A_LOG10, A_EXP, min_AA, max_AA, avg_AA, rang_AA,
	sum_AA, mult_AA, stddev_AA, ONE_MINUS_A, ONE_DIV_A;
	
	@Override
	public String toString() {
		switch (this) {
			case A_PLUS_B:
				return "A + B";
			case A_MINUS_B:
				return "A - B";
			case A_MULT_B:
				return "A * B";
			case A_DIV_B:
				return "A / B";
			case A_POW_B:
				return "A ^ B";
			case A_POW_INV_B:
				return "A^(1/B)";
			case A_ABS:
				return "|A|";
			case A:
				return "A";
			case A_SGN:
				return "SGN(A)";
			case A_B_MAX:
				return "MAX(A, B)";
			case A_B_MIN:
				return "MIN(A, B)";
			case A_LOG:
				return "LOG(A)";
			case A_LOG10:
				return "LOG10(A)";
			case A_EXP:
				return "EXP(A)";
			case min_AA:
				return "MIN(A1, A2, A3, ...)";
			case max_AA:
				return "MAX(A1, A2, A3, ...)";
			case avg_AA:
				return "AVG(A1, A2, A3, ...)";
			case rang_AA:
				return "RANK(A1, A2, A3, ...)";
			case sum_AA:
				return "A1 + A2 + A3, ...)";
			case mult_AA:
				return "A1 * A2 * A3, ...)";
			case stddev_AA:
				return "STDDEV(A1, A2, A3, ...)";
			case ONE_MINUS_A:
				return "1 - A";
			case ONE_DIV_A:
				return "1 / A";
		}
		return "INTERNAL ERROR, UNKNOWN ENUM CONSTANT!";
	}
	
	public double performOperation(double a, double b) {
		if (requiresA() && Double.isNaN(a))
			return Double.NaN;
		if (requiresB() && Double.isNaN(b))
			return Double.NaN;
		switch (this) {
			case A_PLUS_B:
				return a + b;
			case A_MINUS_B:
				return a - b;
			case A_MULT_B:
				return a * b;
			case A_DIV_B:
				return a / b;
			case A_POW_B:
				return Math.pow(a, b);
			case A_POW_INV_B:
				return Math.pow(a, 1 / b);
			case A_ABS:
				return Math.abs(a);
			case A:
				return a;
			case A_SGN:
				return Math.signum(a);
			case A_B_MAX:
				return getMyMax(a, b);
			case A_B_MIN:
				return getMyMin(a, b);
			case A_LOG:
				return Math.log(a);
			case A_LOG10:
				return Math.log10(a);
			case A_EXP:
				return Math.exp(a);
			case ONE_MINUS_A:
				return 1 - a;
			case ONE_DIV_A:
				return 1 / a;
		}
		ErrorMsg.addErrorMessage("INTERNAL ERROR, UNKNOWN ENUM CONSTANT!");
		return Double.NaN;
	}
	
	private double getMyMax(double a, double b) {
		if (Double.isNaN(a) && Double.isNaN(b))
			return Double.NaN;
		if (Double.isNaN(a))
			return b;
		if (Double.isNaN(b))
			return a;
		return Math.max(a, b);
	}
	
	private double getMyMin(double a, double b) {
		if (Double.isNaN(a) && Double.isNaN(b))
			return Double.NaN;
		if (Double.isNaN(a))
			return b;
		if (Double.isNaN(b))
			return a;
		return Math.min(a, b);
	}
	
	private boolean requiresA() {
		if (this == A_B_MAX || this == A_B_MIN)
			return false;
		return true;
	}
	
	public boolean requiresMultipleA() {
		if (this == min_AA || this == max_AA || this == avg_AA || this == stddev_AA ||
							this == rang_AA || this == sum_AA || this == mult_AA)
			return true;
		return false;
	}
	
	private boolean requiresB() {
		switch (this) {
			case A_PLUS_B:
				return true;
			case A_MINUS_B:
				return true;
			case A_MULT_B:
				return true;
			case A_DIV_B:
				return true;
			case A_POW_B:
				return true;
			case A_POW_INV_B:
				return true;
			case A_ABS:
				return false;
			case A:
				return false;
			case A_SGN:
				return false;
			case A_B_MAX:
				return false;
			case A_B_MIN:
				return false;
			case A_LOG:
				return false;
			case A_LOG10:
				return false;
			case A_EXP:
				return false;
			case min_AA:
				return false;
			case max_AA:
				return false;
			case avg_AA:
				return false;
			case rang_AA:
				return false;
			case sum_AA:
				return false;
			case mult_AA:
				return false;
			case stddev_AA:
				return false;
			case ONE_MINUS_A:
				return false;
			case ONE_DIV_A:
				return false;
		}
		ErrorMsg.addErrorMessage("INTERNAL ERROR, UNKNOWN ENUM CONSTANT!");
		return false;
	}
	
	public boolean transformsDataList() {
		switch (this) {
			case rang_AA:
				return true;
		}
		return false;
	}
	
	public double performOperation(Collection<Double> values) {
		if (values.size() <= 0)
			return Double.NaN;
		if (this == min_AA)
			return getMyMin(values);
		if (this == max_AA)
			return getMyMax(values);
		if (this == avg_AA)
			return getMyAvg(values);
		if (this == sum_AA)
			return getMySum(values);
		if (this == mult_AA)
			return getMyMult(values);
		if (this == stddev_AA)
			return getMyStdDev(values);
		ErrorMsg.addErrorMessage("INTERNAL ERROR, UNKNOWN ENUM CONSTANT!");
		return Double.NaN;
	}
	
	private double getMyMin(Collection<Double> values) {
		double min = Double.MAX_VALUE;
		for (double v : values) {
			if (v < min)
				min = v;
		}
		return min;
	}
	
	private double getMyMax(Collection<Double> values) {
		double max = Double.NEGATIVE_INFINITY;
		for (double v : values) {
			if (v > max)
				max = v;
		}
		return max;
	}
	
	private double getMyAvg(Collection<Double> values) {
		double sum = 0;
		for (double v : values) {
			sum += v;
		}
		return sum / values.size();
	}
	
	private double getMySum(Collection<Double> values) {
		double sum = 0;
		for (double v : values) {
			sum += v;
		}
		return sum;
	}
	
	private double getMyMult(Collection<Double> values) {
		double p = 1;
		for (double v : values) {
			p *= v;
		}
		return p;
	}
	
	private double getMyStdDev(Collection<Double> values) {
		double sum = 0;
		double n = values.size();
		for (double v : values) {
			sum += v;
		}
		double avg = sum / n;
		double sumDiff = 0;
		for (Double v : values) {
			sumDiff += (v - avg) * (v - avg);
		}
		double stdDev = Math.sqrt(sumDiff / (n - 1));
		return stdDev;
	}
	
	public ArrayList<Double> performMultipleAandSingleAoperation(ArrayList<Double> values) {
		DoubleAndSourceList[] res = TabStatistics.getRankValues(values);
		ArrayList<Double> result = new ArrayList<Double>();
		for (Double v : values) {
			for (DoubleAndSourceList dasl : res) {
				if (Math.abs(dasl.getDoubleValue() - v) < TabStatistics.epsilon) {
					result.add(dasl.getRangValue());
					break;
				}
			}
		}
		return result;
	}
}
