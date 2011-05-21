package de.ipk.ag_ba.image.operations.complex_hull;

import ij.measure.ResultsTable;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * Detects the border pixels of the image objects. Uses the coordinates of the border pixels to calculate
 * the complex hull for this object.
 * Use method find() to calculate the complex hull and to get the numeric results (contained in the ResultsTable object
 * of the ImageOperation).
 * 
 * @author klukas
 */
public class ComplexHullCalculator {
	
	private final ImageOperation io;
	
	int[][] borderImage;
	int numberOfHullPoints = 0;
	
	private Polygon polygon;
	
	/**
	 * The imageOperation - ResultTable is retained and extended (if available) during calculation.
	 */
	public ComplexHullCalculator(ImageOperation imageOperation) {
		this.io = imageOperation;
	}
	
	private void calculate(int borderColor) {
		int[][] in = io.getImageAs2array();
		
		int w = io.getImage().getWidth();
		int h = io.getImage().getHeight();
		
		int backgroundColor = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		
		for (int x = 0; x < w; x++) {
			in[x][0] = backgroundColor;
			in[x][h - 1] = backgroundColor;
		}
		
		for (int y = 0; y < h; y++) {
			in[0][y] = backgroundColor;
			in[w - 1][y] = backgroundColor;
		}
		
		borderImage = new int[w][h];
		
		int b = borderColor;
		
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
		for (int i = 0; i < numberOfHullPoints; i++)
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
		
		calculate(borderColor);
		
		int w = io.getImage().getWidth();
		int h = io.getImage().getHeight();
		
		if (drawInputimage && drawBorder)
			throw new UnsupportedOperationException("This parameter combination (draw border and draw input image) does not work correctly.");
		
		if (drawInputimage)
			overDrawBorderImage(w, h, io.getImageAs2array(), borderImage, borderColor, drawBorder);
		
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
		
		if (drawHull || drawCentroid)
			res = drawHullAndCentroid(drawHull, drawCentroid, res, polygon, hullLineColor, centroid, centroidColor);
		
		return res;
	}
	
	private static ImageOperation drawHullAndCentroid(boolean drawHull, boolean drawCentroid,
			ImageOperation in, Polygon polygon, int hullLineColor, Point centroid, int centroidColor) {
		
		BufferedImage bi = in.getImageAsBufferedImage();
		
		Graphics2D g2d = (Graphics2D) bi.getGraphics();
		
		if (drawHull)
			drawHull(g2d, polygon, 2, hullLineColor);
		
		if (drawCentroid)
			drawCross(g2d, centroid, 40, 4, centroidColor);
		
		return new ImageOperation(bi, in.getResultsTable());
	}
	
	private static void drawHull(Graphics2D g2d, Polygon polygon, int lineWidth, int hullLineColor) {
		g2d.setStroke(new BasicStroke(lineWidth));
		g2d.setPaint(new Color(hullLineColor));
		g2d.drawPolygon(polygon.getGraphics2Dpolygon());
	}
	
	private static void drawCross(Graphics2D g2d, Point pos, int lineLength, int lineWidth, int centroidColor) {
		g2d.setStroke(new BasicStroke(lineWidth));
		g2d.setPaint(new Color(centroidColor));
		g2d.drawLine((int) (pos.x - lineLength / 2), (int) pos.y, (int) (pos.x + lineLength / 2), (int) pos.y);
		g2d.drawLine((int) pos.x, (int) (pos.y - lineLength / 2), (int) pos.x, (int) (pos.y + lineLength / 2));
	}
	
	private static void overDrawBorderImage(int w, int h, int[][] image, int[][] borderImage, int borderColor, boolean drawBorder) {
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				int p_image = image[x][y];
				if (drawBorder) {
					int p_border = image[x][y];
					if (p_border != borderColor)
						borderImage[x][y] = p_image;
					else
						borderImage[x][y] = borderColor;
				} else {
					borderImage[x][y] = p_image;
				}
			}
	}
}
