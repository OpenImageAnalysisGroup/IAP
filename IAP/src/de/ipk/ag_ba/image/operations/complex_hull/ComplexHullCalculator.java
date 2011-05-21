package de.ipk.ag_ba.image.operations.complex_hull;

import ij.measure.ResultsTable;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

public class ComplexHullCalculator {
	
	private final ImageOperation io;
	
	int[][] borderImage;
	int numberOfHullPoints = 0;
	
	private Polygon polygon;
	
	public ComplexHullCalculator(ImageOperation imageOperation) {
		this.io = imageOperation;
	}
	
	private void calculate() {
		int[][] in = io.getImageAs2array();
		int w = io.getImage().getWidth();
		int h = io.getImage().getHeight();
		borderImage = new int[w][h];
		
		int b = Color.PINK.getRGB();
		
		int backgroundColor = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		io.border().borderDetection(backgroundColor, b, false, in, w, h, borderImage);
		
		int n = 0;
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				if (borderImage[x][y] == b)
					n++;
			}
		
		Point[] points = new Point[n];
		n = 0;
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				if (borderImage[x][y] == b)
					points[n++] = new Point(x, y);
			}
		
		QuickHull qh = new QuickHull();
		numberOfHullPoints = qh.computeHull(points);
		
		Point[] resultPoints = new Point[numberOfHullPoints];
		for (int i = numberOfHullPoints; i < n; i++)
			resultPoints[i] = points[i];
		
		this.polygon = new Polygon(resultPoints);
	}
	
	/**
	 * Calculates the complex hull. The ResultTable-object is extended or newly created and contains
	 * the column "hull.points", containing the number of edge points in the complex hull polygon.
	 * The columns "hull.area" and "hull.signedarea" contain the area size of the hull polygon.
	 * The columns "hull.centroid.x" and "hull.centroid.y" contain the position of the
	 * centroid of the hull polygon.
	 * 
	 * @return A ImageOperation-object, modified according to the given parameters.
	 * @author klukas
	 * @param centroidColor
	 * @param drawCentroid
	 */
	public ImageOperation find(boolean drawInputimage, boolean drawBorder, boolean drawHull, boolean drawCentroid,
			int borderColor, int hullLineColor, int centroidColor) {
		calculate();
		ImageOperation res = new ImageOperation(borderImage);
		ResultsTable rt = io.getResultsTable();
		if (rt == null)
			rt = new ResultsTable();
		rt.incrementCounter();
		rt.addValue("hull.points", numberOfHullPoints);
		
		rt.addValue("hull.area", polygon.area());
		
		rt.addValue("hull.signedarea", polygon.signedArea());
		
		Point centroid = polygon.centroid();
		rt.addValue("hull.centroid.x", centroid.x);
		rt.addValue("hull.centroid.y", centroid.y);
		
		res.setResultsTable(rt);
		return res;
	}
}
