package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.awt.geom.Line2D;
import java.util.TreeMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.graffiti.util.GraphicHelper;

public class LinearRegressionModel {
	
	private final SimpleRegression sr;
	
	public LinearRegressionModel(TreeMap<Integer, Double> xy) {
		sr = new SimpleRegression();
		for (Integer x : xy.keySet())
			sr.addData(x.doubleValue(), xy.get(x));
	}
	
	public double getErrorSquareSum() {
		return sr.getSumSquaredErrors();
	}
	
	public IntersectionPoint intersect(LinearRegressionModel m2) {
		Line2D.Double line1 = new Line2D.Double(sr.getXbar(), sr.getYbar(), sr.getXbar() + 1, sr.getYbar() + sr.getSlope());
		Line2D.Double line2 = new Line2D.Double(m2.sr.getXbar(), m2.sr.getYbar(), m2.sr.getXbar() + 1, m2.sr.getYbar() + m2.sr.getSlope());
		
		return new IntersectionPoint(GraphicHelper.getIntersection(line1, line2));
	}
	
	public double getM() {
		return sr.getSlope();
	}
}
