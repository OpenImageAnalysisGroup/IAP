/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ---------------------------
 * StatisticalBarRenderer.java
 * ---------------------------
 * (C) Copyright 2002-2004, by Pascal Collet and Contributors.
 * Original Author: Pascal Collet;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Christian W. Zuckschwerdt;
 * $Id: StatisticalBarRenderer.java,v 1.1 2011-01-31 09:02:49 klukas Exp $
 * Changes
 * -------
 * 21-Aug-2002 : Version 1, contributed by Pascal Collet (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 24-Oct-2002 : Changes to dataset interface (DG);
 * 05-Nov-2002 : Base dataset is now TableDataset not CategoryDataset (DG);
 * 05-Feb-2003 : Updates for new DefaultStatisticalCategoryDataset (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 06-Oct-2003 : Corrected typo in exception message (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import org.BioStatisticalCategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.ui.FloatDimension;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * A renderer that handles the drawing a bar plot where
 * each bar has a mean value and a standard deviation line.
 * 
 * @author Pascal Collet
 */
public class StatisticalBarRenderer extends BarRenderer
												implements CategoryItemRenderer,
																Cloneable, PublicCloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public StatisticalBarRenderer() {
		super();
	}

	/**
	 * Draws the bar with its standard deviation line range for a single (series, category) data
	 * item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the data area.
	 * @param plot
	 *           the plot.
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 * @param data
	 *           the data.
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 */
	public void drawItem(Graphics2D g2,
									CategoryItemRendererState state,
									Rectangle2D dataArea,
									CategoryPlot plot,
									CategoryAxis domainAxis,
									ValueAxis rangeAxis,
									CategoryDataset data,
									int row,
									int column) {

		// defensive check
		if (!(data instanceof StatisticalCategoryDataset)) {
			throw new IllegalArgumentException("StatisticalBarRenderer.drawCategoryItem()"
								+ " : the data should be of type StatisticalCategoryDataset only.");
		}
		StatisticalCategoryDataset statData = (StatisticalCategoryDataset) data;

		PlotOrientation orientation = plot.getOrientation();
		if (orientation == PlotOrientation.HORIZONTAL) {
			drawHorizontalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, statData,
											row, column);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				drawVerticalItem(g2, state, dataArea, plot, domainAxis, rangeAxis, statData,
										row, column);
			}
	}

	/**
	 * Draws an item for a plot with a horizontal orientation.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the data area.
	 * @param plot
	 *           the plot.
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 * @param dataset
	 *           the data.
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 */
	protected void drawHorizontalItem(Graphics2D g2,
													CategoryItemRendererState state,
													Rectangle2D dataArea,
													CategoryPlot plot,
													CategoryAxis domainAxis,
													ValueAxis rangeAxis,
													StatisticalCategoryDataset dataset,
													int row,
													int column) {

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();

		// BAR Y
		double rectY = domainAxis.getCategoryStart(column, getColumnCount(), dataArea,
																	xAxisLocation);

		int seriesCount = getRowCount();
		int categoryCount = getColumnCount();
		if (seriesCount > 1) {
			double seriesGap = dataArea.getHeight() * getItemMargin()
											/ (categoryCount * (seriesCount - 1));
			rectY = rectY + row * (state.getBarWidth() + seriesGap);
		} else {
			rectY = rectY + row * state.getBarWidth();
		}

		// BAR X
		Number meanValue = dataset.getMeanValue(row, column);

		double value = meanValue.doubleValue();
		double base = 0.0;
		double lclip = getLowerClip();
		double uclip = getUpperClip();

		if (uclip <= 0.0) { // cases 1, 2, 3 and 4
			if (value >= uclip) {
				return; // bar is not visible
			}
			base = uclip;
			if (value <= lclip) {
				value = lclip;
			}
		} else
			if (lclip <= 0.0) { // cases 5, 6, 7 and 8
				if (value >= uclip) {
					value = uclip;
				} else {
					if (value <= lclip) {
						value = lclip;
					}
				}
			} else { // cases 9, 10, 11 and 12
				if (value <= lclip) {
					return; // bar is not visible
				}
				base = getLowerClip();
				if (value >= uclip) {
					value = uclip;
				}
			}

		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
		double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
		double rectX = Math.min(transY2, transY1);

		double rectHeight = state.getBarWidth();
		double rectWidth = Math.abs(transY2 - transY1);

		Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
		Paint seriesPaint = getItemPaint(row, column);
		g2.setPaint(seriesPaint);
		g2.fill(bar);
		// if (state.getBarWidth() > 3) {
		g2.setStroke(getItemStroke(row, column));
		g2.setPaint(getItemOutlinePaint(row, column));
		g2.draw(bar);
		// }

		// standard deviation lines

		double valueDelta = Double.NaN;
		Number num = dataset.getStdDevValue(row, column);
		if (num != null)
			valueDelta = num.doubleValue();
		if (!Double.isNaN(valueDelta)) {
			double highVal = rangeAxis.valueToJava2D(
								meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation
								);
			double lowVal = rangeAxis.valueToJava2D(
								meanValue.doubleValue()
													- (dataset.drawOnlyTopOfErrorBar() ? 0 : valueDelta)
								, dataArea, yAxisLocation
								);

			Line2D line = null;
			line = new Line2D.Double(lowVal, rectY + rectHeight / 2.0d,
												highVal, rectY + rectHeight / 2.0d);
			g2.draw(line);

			double devLine = dataset.getErrorBarLen();
			line = new Line2D.Double(highVal, rectY + rectHeight / 2d - devLine,
												highVal, rectY + rectHeight / 2d + devLine);
			g2.draw(line);
			if (!dataset.drawOnlyTopOfErrorBar()) {
				line = new Line2D.Double(lowVal, rectY + rectHeight / 2d - devLine,
													lowVal, rectY + rectHeight / 2d + devLine);
				g2.draw(line);
			}
		}

		// check ttest info
		if (dataset instanceof BioStatisticalCategoryDataset) {
			BioStatisticalCategoryDataset bds = (BioStatisticalCategoryDataset) dataset;
			boolean isRef = bds.getTTestIsRef(row, column);
			boolean isH1 = bds.getTTestIsH1(row, column);
			if (isRef && isH1) {
				// no t test was done for this data point
			} else {
				float x_ = (float) (bar.getBounds().x + bar.getWidth());
				float y_ = (float) bar.getCenterY();
				if (isRef) {
					// don't mark the reference bar
				} else {
					if (!isH1) {
					} else {
						int sxx = 10;
						int sxy = 10;
						g2.fill(new Ellipse2D.Float(x_ - sxx, y_ - sxy, sxx * 2, sxy * 2));
					}
				}
			}
		}
	}

	/**
	 * Draws an item for a plot with a vertical orientation.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the data area.
	 * @param plot
	 *           the plot.
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 * @param dataset
	 *           the data.
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 */
	protected void drawVerticalItem(Graphics2D g2,
												CategoryItemRendererState state,
												Rectangle2D dataArea,
												CategoryPlot plot,
												CategoryAxis domainAxis,
												ValueAxis rangeAxis,
												StatisticalCategoryDataset dataset,
												int row,
												int column) {

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();

		// BAR X
		double rectX = domainAxis.getCategoryStart(column, getColumnCount(), dataArea,
																	xAxisLocation);

		int seriesCount = getRowCount();
		int categoryCount = getColumnCount();
		if (seriesCount > 1) {
			double seriesGap = dataArea.getWidth() * getItemMargin()
											/ (categoryCount * (seriesCount - 1));
			rectX = rectX + row * (state.getBarWidth() + seriesGap);
		} else {
			rectX = rectX + row * state.getBarWidth();
		}

		// BAR Y
		Number meanValue = dataset.getMeanValue(row, column);
		if (meanValue == null)
			return;
		double value = meanValue.doubleValue();
		double base = 0.0;
		double lclip = getLowerClip();
		double uclip = getUpperClip();

		if (uclip <= 0.0) { // cases 1, 2, 3 and 4
			if (value >= uclip) {
				return; // bar is not visible
			}
			base = uclip;
			if (value <= lclip) {
				value = lclip;
			}
		} else
			if (lclip <= 0.0) { // cases 5, 6, 7 and 8
				if (value >= uclip) {
					value = uclip;
				} else {
					if (value <= lclip) {
						value = lclip;
					}
				}
			} else { // cases 9, 10, 11 and 12
				if (value <= lclip) {
					return; // bar is not visible
				}
				base = getLowerClip();
				if (value >= uclip) {
					value = uclip;
				}
			}

		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
		double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
		double rectY = Math.min(transY2, transY1);

		double rectWidth = state.getBarWidth();
		double rectHeight = Math.abs(transY2 - transY1);

		Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
		Paint seriesPaint = getItemPaint(row, column);
		g2.setPaint(seriesPaint);
		g2.fill(bar);
		// if (state.getBarWidth() > 3) {
		g2.setStroke(getItemStroke(row, column));
		g2.setPaint(getItemOutlinePaint(row, column));
		g2.draw(bar);
		// }

		// standard deviation lines
		double valueDelta = Double.NaN;
		Number num = dataset.getStdDevValue(row, column);
		if (num != null)
			valueDelta = num.doubleValue();
		double highVal;
		if (!Double.isNaN(valueDelta)) {
			highVal = rangeAxis.valueToJava2D(
								meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation
								);
			double lowVal = rangeAxis.valueToJava2D(
								meanValue.doubleValue() - (dataset.drawOnlyTopOfErrorBar() ? 0 : valueDelta), dataArea, yAxisLocation
								);

			Line2D line = null;
			line = new Line2D.Double(rectX + rectWidth / 2.0d, lowVal,
												rectX + rectWidth / 2.0d, highVal);
			g2.draw(line);
			double devLine = dataset.getErrorBarLen();
			line = new Line2D.Double(rectX + rectWidth / 2.0d - devLine, highVal,
												rectX + rectWidth / 2.0d + devLine, highVal);
			g2.draw(line);
			if (!dataset.drawOnlyTopOfErrorBar()) {
				line = new Line2D.Double(rectX + rectWidth / 2.0d - devLine, lowVal,
													rectX + rectWidth / 2.0d + devLine, lowVal);
				g2.draw(line);
			}
		} else {
			highVal = rangeAxis.valueToJava2D(
								meanValue.doubleValue() + 0, dataArea, yAxisLocation
								);
		}
		// check ttest info
		if (dataset instanceof BioStatisticalCategoryDataset) {
			BioStatisticalCategoryDataset bds = (BioStatisticalCategoryDataset) dataset;
			boolean isRef = bds.getTTestIsRef(row, column);
			boolean isH1 = bds.getTTestIsH1(row, column);
			if (isRef && isH1) {
				// no t test was done for this data point
			} else {
				float x_ = (float) bar.getCenterX();
				float y_ = bar.getBounds().y;
				if (isRef) {
					// don't mark the reference bar
				} else {
					if (!isH1) {
					} else {
						float markSize = bds.getTtestMarkCircleSize();
						float sxx = markSize;
						float sxy = markSize;
						Color blackPaint = Color.BLACK;
						if (seriesPaint instanceof Color) {
							Color serColor = (Color) seriesPaint;
							if (serColor.getRGB() == Color.BLACK.getRGB()
												||
												(serColor.getRed() < 60 &&
																	serColor.getGreen() < 60 &&
												serColor.getBlue() < 60
												)) {
								blackPaint = Color.WHITE;
							}
						}
						// CK 2005 //
						// g2.setPaint(Color.YELLOW);
						if (value + valueDelta > uclip * 0.5) {
							// g2.fill(new Ellipse2D.Float(x_-sxx, y_-sxy, sxx*2, sxy*2));
							g2.setPaint(blackPaint);
							// g2.fill(new Ellipse2D.Float(x_-sxx, (float)(y_-3*sxy+bar.getHeight()), sxx*2, sxy*2));
							g2.fill(getTTestShape(x_ - sxx, (float) (y_ - 3 * sxy + bar.getHeight()), sxx * 2, sxy * 2));
						} else {
							// g2.fill(new Ellipse2D.Float(x_-sxx, y_-sxy, sxx*2, sxy*2));
							g2.setPaint(Color.BLACK);
							// g2.fill(new Ellipse2D.Float(x_-sxx, (float)(highVal-3*sxy), sxx*2, sxy*2));
							g2.fill(getTTestShape(x_ - sxx, (float) (highVal - 3 * sxy), sxx * 2, sxy * 2));
						}
					}
				}
			}
		}
	}

	public static Shape getTTestShape(float x, float y, float width, float height) {
		/*
		 * Polygon pBig = getPentaShape(x,y,width,height, false);
		 * float fak = 0.45f;
		 * Polygon pSmall = getPentaShape(x+(width*(1f-fak)/2f),2+y+(height*(1f-fak)/2f),width*fak,height*fak, true);
		 * Polygon result = new Polygon();
		 * for (int i=0; i<pBig.npoints; i++) {
		 * result.addPoint(pBig.xpoints[i], pBig.ypoints[i]);
		 * result.addPoint(pSmall.xpoints[i], pSmall.ypoints[i]);
		 * }
		 * return result;
		 */

		GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO);
		ArrayList<FloatDimension> ps = getPentaShapeDimension(x, y, width, height, false);
		boolean first = true;
		for (FloatDimension ft : ps) {
			if (first) {
				path.moveTo((float) ft.getWidth(), (float) ft.getHeight());
				first = false;
			} else {
				path.lineTo((float) ft.getWidth(), (float) ft.getHeight());
			}
		}
		path.closePath();
		return path;

	}

	private static ArrayList<FloatDimension> getPentaShapeDimension(
						float x, float y, float width, float height, boolean inversed) {
		ArrayList<FloatDimension> result = new ArrayList<FloatDimension>();

		float x0 = x;
		float x1 = x + width * 0.19098f;
		float x2 = x + width * 0.5f;
		float x3 = x + width * 0.809f;
		float x4 = x + width;
		float y0 = y;
		float y1 = y + height * 0.38196f;
		float y2 = y + height;

		if (inversed) {
			float ty = y0;
			y0 = y2;
			y2 = ty;
			y1 = y + height * (1f - 0.38196f);
			// 1, 4, 2, 5, 3
			result.add(new FloatDimension(x0, y1)); // 1
			result.add(new FloatDimension(x4, y1)); // 2
			result.add(new FloatDimension(x1, y2)); // 3
			result.add(new FloatDimension(x2, y0)); // 4
			result.add(new FloatDimension(x3, y2)); // 5
			// result.add(new FloatDimension(x0, y1));
		} else {
			// 1, 3, 5, 2, 4
			result.add(new FloatDimension(x0, y1)); // 1
			result.add(new FloatDimension(x4, y1)); // 2
			result.add(new FloatDimension(x1, y2)); // 3
			result.add(new FloatDimension(x2, y0)); // 4
			result.add(new FloatDimension(x3, y2)); // 5
			// result.add(new FloatDimension(x0, y1));
		}
		return result;
	}

	private Polygon getPentaShape(float x, float y, float width, float height, boolean inversed) {
		Polygon polygon = new Polygon();
		int x0 = (int) x;
		int x1 = (int) (x + width * 0.19098);
		int x2 = (int) (x + width * 0.5);
		int x3 = (int) (x + width * 0.809);
		int x4 = (int) (x + width);
		int y0 = (int) y;
		int y1 = (int) (y + height * 0.38196f);
		int y2 = (int) (y + height);

		if (inversed) {
			int ty = y0;
			y0 = y2;
			y2 = ty;
			y1 = (int) (y + height * (1f - 0.38196f));
			polygon.addPoint(x0, y1); // 1
			polygon.addPoint(x2, y0); // 4
			polygon.addPoint(x4, y1); // 2
			polygon.addPoint(x3, y2); // 5
			polygon.addPoint(x1, y2); // 3
			polygon.addPoint(x0, y1);
		} else {
			polygon.addPoint(x0, y1); // 1
			polygon.addPoint(x1, y2); // 3
			polygon.addPoint(x3, y2); // 5
			polygon.addPoint(x4, y1); // 2
			polygon.addPoint(x2, y0); // 4
			polygon.addPoint(x0, y1);
		}
		return polygon;
	}
}
