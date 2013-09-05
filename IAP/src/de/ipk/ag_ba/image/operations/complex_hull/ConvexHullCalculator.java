package de.ipk.ag_ba.image.operations.complex_hull;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.ipk.ag_ba.image.operation.ImageCanvas;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Detects the border pixels of the image objects. Uses the coordinates of the
 * border pixels to calculate the complex hull for this object. Use method
 * find() to calculate the complex hull and to get the numeric results
 * (contained in the ResultsTable object of the ImageOperation).
 * 
 * @author klukas
 */
public class ConvexHullCalculator {
	
	private final ImageOperation io;
	
	int[][] borderImage;
	int numberOfHullPoints = 0;
	
	private Polygon polygon;
	
	private int borderPixels;
	
	private Image customImage;
	
	/**
	 * The imageOperation - ResultTable is retained and extended (if available)
	 * during calculation.
	 */
	public ConvexHullCalculator(ImageOperation imageOperation) {
		this.io = imageOperation;
	}
	
	private void calculate(int borderColor) {
		int[][] in = io.getImageAs2dArray();
		
		int w = io.getImage().getWidth();
		int h = io.getImage().getHeight();
		
		int backgroundColor = ImageOperation.BACKGROUND_COLORint;
		
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
		
		borderPixels = io.border().borderDetection(backgroundColor, b, false,
				in, w, h, borderImage);
		
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
		
		if (n > 0) {
			QuickHull qh = new QuickHull();
			numberOfHullPoints = qh.computeHull(points);
			
			Point[] resultPoints = new Point[numberOfHullPoints];
			for (int i = 0; i < numberOfHullPoints; i++)
				resultPoints[i] = points[i];
			
			this.polygon = new Polygon(resultPoints);
		} else
			polygon = null;
	}
	
	public ConvexHullCalculator setCustomBackgroundImageForDrawing(Image customImage) {
		this.customImage = customImage;
		return this;
	}
	
	/**
	 * Calculates the complex hull. The ResultTable-object is extended or newly
	 * created and contains the column "hull.points", containing the number of
	 * edge points in the complex hull polygon. The columns "hull.area" and
	 * "hull.signedarea" contain the area size of the hull polygon. The columns
	 * "hull.centroid.x" and "hull.centroid.y" contain the position of the
	 * centroid of the hull polygon.
	 * 
	 * @param centroidColor
	 * @param drawCentroid
	 * @return A ImageOperation-object, modified according to the given
	 *         parameters.
	 * @author klukas
	 * @param distHorizontal
	 * @param realDist
	 */
	
	public ImageOperation find(boolean drawInputimage, boolean drawBorder,
			boolean drawHull, boolean drawCentroid, int borderColor,
			int hullLineColor, int centroidColor, Double distHorizontal,
			Double realMarkerDist) {
		
		return find(drawInputimage, drawBorder, drawHull, drawInputimage, drawCentroid, borderColor,
				hullLineColor, centroidColor, distHorizontal, realMarkerDist);
	}
	
	public ImageOperation find(boolean drawInputimage, boolean drawBorder,
			boolean drawHull, boolean drawPCLine, boolean drawCentroid, int borderColor,
			int hullLineColor, int centroidColor, Double distHorizontal,
			Double realMarkerDist) {
		
		calculate(borderColor);
		
		int w = io.getImage().getWidth();
		int h = io.getImage().getHeight();
		
		if (drawInputimage && drawBorder)
			throw new UnsupportedOperationException(
					"This parameter combination (draw border and draw input image) does not work correctly.");
		
		if (drawInputimage) {
			ImageOperation inDrawing = customImage != null ? customImage.io() : io;
			// FlexibleImage border = new FlexibleImage(borderImage).copy();
			overDrawBorderImage(w, h, inDrawing.getImageAs2dArray(), borderImage,
					borderColor, drawBorder);
		}
		Point centroid = null;
		
		ImageOperation res = new ImageOperation(borderImage);
		ResultsTableWithUnits rt = io.getResultsTable();
		if (rt == null)
			rt = new ResultsTableWithUnits();
		
		if (polygon != null) {
			rt.incrementCounter();
			double normFactorArea = distHorizontal != null && realMarkerDist != null ? (realMarkerDist * realMarkerDist)
					/ (distHorizontal * distHorizontal)
					: 1;
			double normFactor = distHorizontal != null && realMarkerDist != null ? realMarkerDist / distHorizontal : 1;
			
			rt.addValue("hull.points", numberOfHullPoints);
			int filledArea = io.countFilledPixels();
			if (filledArea > 0)
				rt.addValue("compactness.01", 4 * Math.PI
						/ (borderPixels * borderPixels / filledArea));
			rt.addValue("compactness.16",
					(borderPixels * borderPixels / filledArea));
			if (distHorizontal != null) {
				rt.addValue("hull.area.norm", polygon.area() * normFactorArea);
				rt.addValue("border.length.norm", borderPixels * normFactor);
			}
			rt.addValue("hull.area", polygon.area());
			rt.addValue("border.length", borderPixels);
			
			rt.addValue("hull.circularity", circularity());
			
			Circle circumcircle = polygon.calculateminimalcircumcircle();
			
			if (circumcircle != null) {
				if (distHorizontal != null) {
					rt.addValue("hull.circumcircle.d.norm", circumcircle.d
							* normFactor);
				}
				rt.addValue("hull.circumcircle.d", circumcircle.d);
			}
			
			rt.addValue("hull.fillgrade", filledArea / polygon.area());
			
			centroid = polygon.centroid();
			
			Line sp = polygon.getMaxSpan();
			if (sp != null) {
				double span = sp.getlength();
				rt.addValue("hull.pc1", span);
				Span2result span2 = polygon.getMaxSpan2len(sp);
				rt.addValue("hull.pc2", span2.getLengthPC2());
				if (distHorizontal != null) {
					rt.addValue("hull.pc1.norm", span * normFactor);
					rt.addValue("hull.pc2.norm", span2.getLengthPC2() * normFactor);
				}
				
				if (drawPCLine) {
					Image inDrawing = res.getImage();
					ImageCanvas a = inDrawing.io().canvas().drawLine(sp, Color.BLUE.getRGB(), 0.5, 1);
					if (span2.getP1() != null && span2.getP1l() != null) {
						a = a.drawLine(span2.getP1(), span2.getP1l(), Color.ORANGE.getRGB(), 0.5, 1);
						// a = a.drawCircle((int) span2.getP1().x, (int) span2.getP1().y, 25, Color.RED.getRGB(), 0, 2);
					}
					
					if (span2.getP2() != null && span2.getP2l() != null) {
						a = a.drawLine(span2.getP2(), span2.getP2l(), Color.ORANGE.getRGB(), 0.5, 1);
						// a = a.drawCircle((int) span2.getP2().x, (int) span2.getP2().y, 25, Color.ORANGE.getRGB(), 0, 2);
					}
					res = new ImageOperation(a.getImage(),
							res.getResultsTable());
				}
			}
			
			// rt.addValue("hull.centroid.x", centroid.x);
			// rt.addValue("hull.centroid.y", centroid.y);
		}
		res.setResultsTable(rt);
		
		if (drawHull || drawCentroid)
			res = drawHullAndCentroid(drawHull, drawCentroid, res, polygon,
					hullLineColor, centroid, centroidColor);
		
		return res;
	}
	
	/**
	 * @return index of circularity [0-1], for index 1 the polygon is a circle
	 */
	private double circularity() {
		return 4.0
				* Math.PI
				* (polygon.area() / (polygon.perimeter() * polygon.perimeter()));
	}
	
	private static ImageOperation drawHullAndCentroid(boolean drawHull,
			boolean drawCentroid, ImageOperation in, Polygon polygon,
			int hullLineColor, Point centroid, int centroidColor) {
		
		// in = in.getCanvas().fillRectOutside(polygon.getRectangle(),
		// ImageOperation.BACKGROUND_COLORint).getImage().getIO();
		
		BufferedImage bi = in.getAsBufferedImage();
		
		Graphics2D g2d = (Graphics2D) bi.getGraphics();
		
		if (drawHull && polygon != null)
			drawHull(g2d, polygon, 4, hullLineColor);
		
		if (drawCentroid && centroid != null)
			drawCross(g2d, centroid, 50, 5, centroidColor);
		
		return new ImageOperation(bi, in.getResultsTable());
	}
	
	private static void drawHull(Graphics2D g2d, Polygon polygon,
			int lineWidth, int hullLineColor) {
		g2d.setStroke(new BasicStroke(lineWidth));
		g2d.setPaint(new Color(hullLineColor));
		float opacity = 0.5f;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				opacity));
		g2d.drawPolygon(polygon.getGraphics2Dpolygon());
	}
	
	private static void drawCross(Graphics2D g2d, Point pos, int lineLength,
			int lineWidth, int centroidColor) {
		g2d.setStroke(new BasicStroke(lineWidth));
		g2d.setPaint(new Color(centroidColor));
		g2d.drawLine((int) (pos.x - lineLength / 2), (int) pos.y,
				(int) (pos.x + lineLength / 2), (int) pos.y);
		g2d.drawLine((int) pos.x, (int) (pos.y - lineLength / 2), (int) pos.x,
				(int) (pos.y + lineLength / 2));
	}
	
	private static void overDrawBorderImage(int w, int h, int[][] image,
			int[][] borderImage, int borderColor, boolean drawBorder) {
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
