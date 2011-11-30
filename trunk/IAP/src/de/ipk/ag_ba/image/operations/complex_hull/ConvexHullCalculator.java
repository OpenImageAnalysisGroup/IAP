package de.ipk.ag_ba.image.operations.complex_hull;

import ij.measure.ResultsTable;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;

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

	/**
	 * The imageOperation - ResultTable is retained and extended (if available)
	 * during calculation.
	 */
	public ConvexHullCalculator(ImageOperation imageOperation) {
		this.io = imageOperation;
	}

	private void calculate(int borderColor) {
		int[][] in = io.getImageAs2array();

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
			int hullLineColor, int centroidColor, BlockProperty distHorizontal,
			int realMarkerDist) {

		calculate(borderColor);

		int w = io.getImage().getWidth();
		int h = io.getImage().getHeight();

		if (drawInputimage && drawBorder)
			throw new UnsupportedOperationException(
					"This parameter combination (draw border and draw input image) does not work correctly.");

		if (drawInputimage)
			overDrawBorderImage(w, h, io.getImageAs2array(), borderImage,
					borderColor, drawBorder);

		Point centroid = null;

		ImageOperation res = new ImageOperation(borderImage);
		ResultsTable rt = io.getResultsTable();
		if (rt == null)
			rt = new ResultsTable();

		if (polygon != null) {
			rt.incrementCounter();
			double normFactorArea = distHorizontal != null ? (realMarkerDist * realMarkerDist)
					/ (distHorizontal.getValue() * distHorizontal.getValue())
					: 1;
			double normFactor = distHorizontal != null ? realMarkerDist
					/ distHorizontal.getValue() : 1;

			rt.addValue("hull.points", numberOfHullPoints);
			int filledArea = io.countFilledPixels();
			if (filledArea > 0)
				rt.addValue("compactness.01", 4 * Math.PI
						/ (borderPixels * borderPixels / filledArea));
			rt.addValue("compactness.16",
					(borderPixels * borderPixels / filledArea));
			if (distHorizontal != null) {
				rt.addValue("area.norm", filledArea * normFactorArea);
				double fn = filledArea * normFactorArea;
				// rt.addValue("area.norm3", Math.sqrt(fn * fn * fn));
				rt.addValue("hull.area.norm", polygon.area() * normFactorArea);
				rt.addValue("border.length.norm", borderPixels * normFactor);
			}
			rt.addValue("area", filledArea);
			// rt.addValue("area3",
			// Math.sqrt(filledArea * filledArea * filledArea));
			rt.addValue("hull.area", polygon.area());
			rt.addValue("border.length", borderPixels);

			// rt.addValue("hull.signedarea", polygon.signedArea() *
			// normFactorArea);
			rt.addValue("hull.circularity", circularity());

			Circle circumcircle = polygon.calculateminimalcircumcircle();

			if (circumcircle != null) {
				if (distHorizontal != null) {
					rt.addValue("hull.circumcircle.x.norm", circumcircle.x
							* normFactor);
					rt.addValue("hull.circumcircle.y.norm", circumcircle.y
							* normFactor);
					rt.addValue("hull.circumcircle.d.norm", circumcircle.d
							* normFactor);
				}
				rt.addValue("hull.circumcircle.x", circumcircle.x);
				rt.addValue("hull.circumcircle.y", circumcircle.y);
				rt.addValue("hull.circumcircle.d", circumcircle.d);
			}

			rt.addValue("hull.fillgrade", filledArea / polygon.area());

			centroid = polygon.centroid();

			if (distHorizontal != null) {
				rt.addValue("hull.centroid.x.norm", centroid.x * normFactor);
				rt.addValue("hull.centroid.y.norm", centroid.y * normFactor);
			}
			rt.addValue("hull.centroid.x", centroid.x);
			rt.addValue("hull.centroid.y", centroid.y);
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

		BufferedImage bi = in.getImageAsBufferedImage();

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
