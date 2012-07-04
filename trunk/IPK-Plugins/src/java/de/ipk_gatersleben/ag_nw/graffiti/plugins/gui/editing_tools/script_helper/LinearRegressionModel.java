package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.awt.geom.Line2D;
import java.util.TreeMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.graffiti.util.GraphicHelper;

public class LinearRegressionModel {
	
	private SimpleRegression sr;
	private TreeMap<Integer, Double> values;
	
	public LinearRegressionModel(TreeMap<Integer, Double> xy, boolean horizontalLine) {
		if (!horizontalLine) {
			sr = new SimpleRegression(false);
			for (Integer x : xy.keySet())
				sr.addData(x.doubleValue(), xy.get(x));
		} else
			values = xy;
	}
	
	public double getErrorSquareSum() {
		if (sr!=null)
			return sr.getSumSquaredErrors();
		else {
			if (values.size()>0) {
				double errorSqareSum = 0;
				double avg = getAverageY();
				for (Double v : values.values()) {
					errorSqareSum+=(v-avg)*(v-avg);
				}
				return errorSqareSum;
			} else
				return 0;
		}
	}
	
	public IntersectionPoint intersect(LinearRegressionModel m2) {
		Line2D.Double line1;
		if (sr!=null)
			line1 = new Line2D.Double(sr.getXbar(), sr.getYbar(), sr.getXbar() + 1, sr.getYbar() + sr.getSlope());
		else
			line1 = new Line2D.Double(0, getAverageY(),  1, getAverageY());
		
		Line2D.Double line2;
		if (sr!=null)
			line2= new Line2D.Double(m2.sr.getXbar(), m2.sr.getYbar(), m2.sr.getXbar() + 1, m2.sr.getYbar() + m2.sr.getSlope());
		else
			line2= new Line2D.Double(0, m2.getAverageY(), 1, m2.getAverageY());
		
		return new IntersectionPoint(GraphicHelper.getIntersection(line1, line2));
	}
	
	public double getM() {
		if (sr!=null)
			return sr.getSlope();
		else
			return 0;
	}

	public double getAverageY() {
		if (sr!=null)
			return sr.getYbar();
		else {
			int n = values.size();
			double sum = 0;
			if (n>0) {
				for (Double v : values.values())
					sum+=v;
				double avg = sum/n;
				return avg;
			} else
				return Double.NaN;
		}
	}
}
