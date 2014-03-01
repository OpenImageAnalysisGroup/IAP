package de.ipk.ag_ba.image.operations.complex_hull;

import iap.blocks.data_structures.RunnableOnImage;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
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
	
	private double hullLength;
	
	/**
	 * The imageOperation - ResultTable is retained and extended (if available)
	 * during calculation.
	 */
	public ConvexHullCalculator(ImageOperation imageOperation) {
		this.io = imageOperation;
	}
	
	private void calculate(int borderColor) {
		int[][] in = io.getAs2D();
		
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
		this.hullLength = Double.NaN;
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				if (borderImage[x][y] == b)
					points[n++] = new Point(x, y);
			}
		
		if (n > 0) {
			QuickHull qh = new QuickHull();
			numberOfHullPoints = qh.computeHull(points);
			
			Point[] resultPoints = new Point[numberOfHullPoints];
			for (int i = 0; i < numberOfHullPoints; i++) {
				if (i == 0)
					hullLength = 0;
				else {
					double dist = dist(points[i], points[i - 1]);
					hullLength += dist;
				}
				resultPoints[i] = points[i];
			}
			
			this.polygon = new Polygon(resultPoints);
		} else
			polygon = null;
	}
	
	private double dist(Point a, Point b) {
		return Math.sqrt((b.x - a.x) * (b.y - a.y) + (b.y - a.y) * (b.y - a.y));
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
	
	public ImageOperation find(boolean drawInputimage, boolean drawBorder, boolean drawMinRect,
			boolean drawHull, boolean drawCentroid, int borderColor,
			int hullLineColor, int centroidColor, int minRectColor, Double distHorizontal,
			Double realMarkerDist) {
		
		return find(null, drawInputimage, drawBorder, drawHull, drawInputimage, drawCentroid, drawMinRect, true,
				borderColor, hullLineColor, centroidColor, minRectColor, Color.BLACK.getRGB(), distHorizontal, realMarkerDist);
	}
	
	public ImageOperation find(BlockResultSet br, boolean drawInputimage, boolean drawBorder,
			final boolean drawHull, boolean drawPCLine, final boolean drawCentroid, boolean drawMinRect, boolean drawCircle,
			int borderColor,
			final int hullLineColor,
			final int centroidColor,
			final int circleColor,
			final int minRectColor,
			Double distHorizontal,
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
			overDrawBorderImage(w, h, inDrawing.getAs2D(), borderImage,
					borderColor, drawBorder);
		}
		ImageOperation res = new ImageOperation(borderImage);
		ResultsTableWithUnits rt = io.getResultsTable();
		if (rt == null)
			rt = new ResultsTableWithUnits();
		
		Point centroid = null;
		if (polygon != null) {
			rt.incrementCounter();
			double normFactorArea = distHorizontal != null && realMarkerDist != null ? (realMarkerDist * realMarkerDist)
					/ (distHorizontal * distHorizontal)
					: 1;
			double normFactor = distHorizontal != null && realMarkerDist != null ? realMarkerDist / distHorizontal : 1;
			
			rt.addValue("hull.points", numberOfHullPoints);
			rt.addValue("hull.length", hullLength);
			int filledArea = io.countFilledPixels();
			if (filledArea > 0) {
				rt.addValue("compactness.01", 4 * Math.PI
						/ (borderPixels * borderPixels / filledArea));
				rt.addValue("compactness.16",
						(borderPixels * borderPixels / filledArea));
				rt.addValue("hull.compactness.01", 4 * Math.PI
						/ (hullLength * hullLength / filledArea));
				rt.addValue("hull.compactness.16",
						(hullLength * hullLength / filledArea));
			}
			if (distHorizontal != null) {
				rt.addValue("hull.area.norm", polygon.area() * normFactorArea);
				rt.addValue("border.length.norm", borderPixels * normFactor);
				rt.addValue("hull.length.norm", hullLength * normFactor);
			}
			rt.addValue("hull.area", polygon.area());
			rt.addValue("border.length", borderPixels);
			
			rt.addValue("hull.circularity", circularity());
			
			final Circle circumcircle = polygon.calculateminimalcircumcircle();
			
			if (circumcircle != null) {
				rt.addValue("hull.circumcircle.d", circumcircle.d);
				if (distHorizontal != null) {
					rt.addValue("hull.circumcircle.d.norm", circumcircle.d
							* normFactor);
				}
				if (drawCircle) {
					RunnableOnImage runnableOnMask = new RunnableOnImage() {
						@Override
						public Image postProcess(Image in) {
							return in.io().canvas().drawCircle((int) circumcircle.x, (int) circumcircle.y, (int) (circumcircle.d / 2d), circleColor, 0.5, 1)
									.getImage();
						}
					};
					if (br != null)
						br.addImagePostProcessor(io.getCameraType(), null, runnableOnMask);
				}
			}
			java.awt.geom.Point2D.Double[] mr = null;
			try {
				mr = RotatingCalipers.getMinimumBoundingRectangle(polygon.getPoints());
			} catch (IllegalArgumentException iae) {
				// too few pixels for convex hull calculation
				// System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Could not calculate bounding box: " + iae.getMessage());
			}
			if (mr != null) {
				final Point[] pl = new Point[mr.length];
				int idx = 0;
				for (java.awt.geom.Point2D.Double p : mr)
					pl[idx++] = new Point(p.x, p.y);
				Polygon p = new Polygon(pl);
				rt.addValue("hull.minrectangle.area", p.area(), "px");
				rt.addValue("hull.minrectangle.length.a", pl[0].distEuclid(pl[1]), "px");
				rt.addValue("hull.minrectangle.length.b", pl[1].distEuclid(pl[2]), "px");
				if (distHorizontal != null) {
					rt.addValue("hull.minrectangle.area.norm", p.area() * normFactorArea, "mm^2");
					rt.addValue("hull.minrectangle.length.a.norm", pl[0].distEuclid(pl[1]) * normFactor, "mm");
					rt.addValue("hull.minrectangle.length.b.norm", pl[1].distEuclid(pl[2]) * normFactor, "mm");
				}
				if (drawMinRect && pl.length == 4) {
					RunnableOnImage runnableOnMask = new RunnableOnImage() {
						@Override
						public Image postProcess(Image in) {
							return in.io().canvas()
									.drawLine(pl[0], pl[1], minRectColor, 0.5, 1)
									.drawLine(pl[1], pl[2], minRectColor, 0.5, 1)
									.drawLine(pl[2], pl[3], minRectColor, 0.5, 1)
									.drawLine(pl[3], pl[0], minRectColor, 0.5, 1)
									.getImage();
						}
					};
					br.addImagePostProcessor(io.getCameraType(), null, runnableOnMask);
				}
			}
			
			rt.addValue("hull.fillgrade", filledArea / polygon.area());
			
			centroid = polygon.centroid();
			
			final Line sp = polygon.getMaxSpan();
			if (sp != null) {
				double span = sp.getlength();
				rt.addValue("hull.pc1", span);
				final Span2result span2 = polygon.getMaxSpan2len(sp);
				rt.addValue("hull.pc2", span2.getLengthPC2());
				if (distHorizontal != null) {
					rt.addValue("hull.pc1.norm", span * normFactor);
					rt.addValue("hull.pc2.norm", span2.getLengthPC2() * normFactor);
				}
				
				if (drawPCLine && (new Vector2i(res.getCropRectangle()).getArea() > 50 * 50)) {
					RunnableOnImage roi = new RunnableOnImage() {
						@Override
						public Image postProcess(Image inDrawing) {
							ImageCanvas a = inDrawing.io().canvas().drawLine(sp, Color.BLUE.getRGB(), 0.5, 1);
							if (span2.getP1() != null && span2.getP1l() != null)
								a = a.drawLine(span2.getP1(), span2.getP1l(), Color.ORANGE.getRGB(), 0.5, 1);
							
							if (span2.getP2() != null && span2.getP2l() != null)
								a = a.drawLine(span2.getP2(), span2.getP2l(), Color.ORANGE.getRGB(), 0.5, 1);
							return a.getImage();
						}
					};
					br.addImagePostProcessor(io.getCameraType(), null, roi);
				}
			}
			
			rt.addValue("hull.centroid.x", centroid.x);
			rt.addValue("hull.centroid.y", centroid.y);
		}
		res.setResultsTable(rt);
		
		final Point centroidF = centroid;
		if ((drawHull || drawCentroid) && (new Vector2i(res.getCropRectangle()).getArea() > 50 * 50)) {
			RunnableOnImage roi = new RunnableOnImage() {
				@Override
				public Image postProcess(Image res) {
					return drawHullAndCentroid(drawHull, drawCentroid, res.io(), polygon,
							hullLineColor, centroidF, centroidColor).getImage();
				}
				
			};
			br.addImagePostProcessor(io.getCameraType(), null, roi);
		}
		
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
		float opacity = 0.5f;
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				opacity));
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
