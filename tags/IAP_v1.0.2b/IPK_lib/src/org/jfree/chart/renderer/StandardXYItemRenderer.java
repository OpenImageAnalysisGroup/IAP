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
 * StandardXYItemRenderer.java
 * ---------------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Mark Watson (www.markwatson.com);
 * Jonathan Nash;
 * Andreas Schneider;
 * Norbert Kiesel (for TBD Networks);
 * Christian W. Zuckschwerdt;
 * Bill Kelemen;
 * Nicolas Brodu (for Astrium and EADS Corporate Research Center);
 * $Id: StandardXYItemRenderer.java,v 1.1 2011-01-31 09:02:46 klukas Exp $
 * Changes:
 * --------
 * 19-Oct-2001 : Version 1, based on code by Mark Watson (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 21-Dec-2001 : Added working line instance to improve performance (DG);
 * 22-Jan-2002 : Added code to lock crosshairs to data points. Based on code by Jonathan Nash (DG);
 * 23-Jan-2002 : Added DrawInfo parameter to drawItem(...) method (DG);
 * 28-Mar-2002 : Added a property change listener mechanism so that the renderer no longer needs to
 * be immutable (DG);
 * 02-Apr-2002 : Modified to handle null values (DG);
 * 09-Apr-2002 : Modified draw method to return void. Removed the translated zero from the
 * drawItem method. Override the initialise() method to calculate it (DG);
 * 13-May-2002 : Added code from Andreas Schneider to allow changing shapes/colors per item (DG);
 * 24-May-2002 : Incorporated tooltips into chart entities (DG);
 * 25-Jun-2002 : Removed redundant code (DG);
 * 05-Aug-2002 : Incorporated URLs for HTML image maps into chart entities (RA);
 * 08-Aug-2002 : Added discontinuous lines option contributed by Norbert Kiesel (DG);
 * 20-Aug-2002 : Added user definable default values to be returned by protected methods unless
 * overridden by a subclass (DG);
 * 23-Sep-2002 : Updated for changes in the XYItemRenderer interface (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem(...) method signature (DG);
 * 15-May-2003 : Modified to take into account the plot orientation (DG);
 * 29-Jul-2003 : Amended code that doesn't compile with JDK 1.2.2 (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 24-Aug-2003 : Added null/NaN checks in drawItem (BK);
 * 08-Sep-2003 : Fixed serialization (NB);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 21-Jan-2004 : Override for getLegendItem() method (DG);
 * 27-Jan-2004 : Moved working line into state object (DG);
 * 10-Feb-2004 : Changed drawItem() method to make cut-and-paste overriding easier (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState. Renamed XYToolTipGenerator
 * --> XYItemLabelGenerator (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.BooleanList;
import org.jfree.util.PublicCloneable;

/**
 * Standard item renderer for an {@link XYPlot}. This class can draw (a) shapes at
 * each point, or (b) lines between points, or (c) both shapes and lines.
 */
public class StandardXYItemRenderer extends AbstractXYItemRenderer
												implements XYItemRenderer,
																Cloneable,
																PublicCloneable,
																Serializable {

	/** Constant for the type of rendering (shapes only). */
	public static final int SHAPES = 1;

	/** Constant for the type of rendering (lines only). */
	public static final int LINES = 2;

	/** Constant for the type of rendering (shapes and lines). */
	public static final int SHAPES_AND_LINES = SHAPES | LINES;

	/** Constant for the type of rendering (images only). */
	public static final int IMAGES = 4;

	/** Constant for the type of rendering (discontinuous lines). */
	public static final int DISCONTINUOUS = 8;

	/** Constant for the type of rendering (discontinuous lines). */
	public static final int DISCONTINUOUS_LINES = LINES | DISCONTINUOUS;

	/** A flag indicating whether or not shapes are drawn at each XY point. */
	private boolean plotShapes;

	/** A flag indicating whether or not lines are drawn between XY points. */
	private boolean plotLines;

	/** A flag indicating whether or not images are drawn between XY points. */
	private boolean plotImages;

	/** A flag controlling whether or not discontinuous lines are used. */
	private boolean plotDiscontinuous;

	/** Threshold for deciding when to discontinue a line. */
	private double gapThreshold = 1.0;

	/** A flag that controls whether or not shapes are filled for ALL series. */
	private Boolean shapesFilled;

	/** A table of flags that control (per series) whether or not shapes are filled. */
	private BooleanList seriesShapesFilled;

	/** The default value returned by the getShapeFilled(...) method. */
	private Boolean defaultShapesFilled;

	/**
	 * Constructs a new renderer.
	 */
	public StandardXYItemRenderer() {
		this(LINES, null);
	}

	/**
	 * Constructs a new renderer.
	 * <p>
	 * To specify the type of renderer, use one of the constants: SHAPES, LINES or SHAPES_AND_LINES.
	 * 
	 * @param type
	 *           the type.
	 */
	public StandardXYItemRenderer(int type) {
		this(type, null);
	}

	/**
	 * Constructs a new renderer.
	 * <p>
	 * To specify the type of renderer, use one of the constants: SHAPES, LINES or SHAPES_AND_LINES.
	 * 
	 * @param type
	 *           the type of renderer.
	 * @param toolTipGenerator
	 *           the item label generator (<code>null</code> permitted).
	 */
	public StandardXYItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
		this(type, toolTipGenerator, null);
	}

	/**
	 * Constructs a new renderer.
	 * <p>
	 * To specify the type of renderer, use one of the constants: SHAPES, LINES or SHAPES_AND_LINES.
	 * 
	 * @param type
	 *           the type of renderer.
	 * @param toolTipGenerator
	 *           the item label generator (<code>null</code> permitted).
	 * @param urlGenerator
	 *           the URL generator.
	 */
	public StandardXYItemRenderer(int type,
												XYToolTipGenerator toolTipGenerator,
												XYURLGenerator urlGenerator) {

		super();
		setToolTipGenerator(toolTipGenerator);
		setURLGenerator(urlGenerator);
		if ((type & SHAPES) != 0) {
			this.plotShapes = true;
		}
		if ((type & LINES) != 0) {
			this.plotLines = true;
		}
		if ((type & IMAGES) != 0) {
			this.plotImages = true;
		}
		if ((type & DISCONTINUOUS) != 0) {
			this.plotDiscontinuous = true;
		}
		// this.line = new Line2D.Double(0.0, 0.0, 0.0, 0.0);

		this.shapesFilled = null;
		this.seriesShapesFilled = new BooleanList();
		this.defaultShapesFilled = Boolean.TRUE;

	}

	/**
	 * Returns true if shapes are being plotted by the renderer.
	 * 
	 * @return <code>true</code> if shapes are being plotted by the renderer.
	 */
	public boolean getPlotShapes() {
		return this.plotShapes;
	}

	/**
	 * Sets the flag that controls whether or not a shape is plotted at each data point.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setPlotShapes(boolean flag) {
		if (this.plotShapes != flag) {
			this.plotShapes = flag;
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// SHAPES FILLED

	/**
	 * Returns the flag used to control whether or not the shape for an item is filled.
	 * <p>
	 * The default implementation passes control to the <code>getSeriesShapesFilled</code> method. You can override this method if you require different
	 * behaviour.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return A boolean.
	 */
	public boolean getItemShapeFilled(int series, int item) {
		return getSeriesShapesFilled(series);
	}

	/**
	 * Returns the flag used to control whether or not the shapes for a series are filled.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return A boolean.
	 */
	public boolean getSeriesShapesFilled(int series) {

		// return the overall setting, if there is one...
		if (this.shapesFilled != null) {
			return this.shapesFilled.booleanValue();
		}

		// otherwise look up the paint table
		Boolean flag = this.seriesShapesFilled.getBoolean(series);
		if (flag != null) {
			return flag.booleanValue();
		} else {
			return this.defaultShapesFilled.booleanValue();
		}

	}

	/**
	 * Sets the 'shapes filled' for ALL series.
	 * 
	 * @param filled
	 *           the flag.
	 */
	public void setShapesFilled(boolean filled) {
		if (filled) {
			setShapesFilled(Boolean.TRUE);
		} else {
			setShapesFilled(Boolean.FALSE);
		}
		// setShapesFilled(Boolean.valueOf(filled));
	}

	/**
	 * Sets the 'shapes filled' for ALL series.
	 * 
	 * @param filled
	 *           the flag (<code>null</code> permitted).
	 */
	public void setShapesFilled(Boolean filled) {
		this.shapesFilled = filled;
	}

	/**
	 * Sets the 'shapes filled' flag for a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param flag
	 *           the flag.
	 */
	public void setSeriesShapesFilled(int series, Boolean flag) {
		this.seriesShapesFilled.setBoolean(series, flag);
	}

	/**
	 * Returns the default 'shape filled' attribute.
	 * 
	 * @return The default flag.
	 */
	public Boolean getDefaultShapesFilled() {
		return this.defaultShapesFilled;
	}

	/**
	 * Sets the default 'shapes filled' flag.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setDefaultShapesFilled(Boolean flag) {
		this.defaultShapesFilled = flag;
	}

	/**
	 * Returns true if lines are being plotted by the renderer.
	 * 
	 * @return <code>true</code> if lines are being plotted by the renderer.
	 */
	public boolean getPlotLines() {
		return this.plotLines;
	}

	/**
	 * Sets the flag that controls whether or not a line is plotted between each data point.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setPlotLines(boolean flag) {
		if (this.plotLines != flag) {
			this.plotLines = flag;
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the gap threshold for discontinuous lines.
	 * 
	 * @return the gap threshold.
	 */
	public double getGapThreshold() {
		return this.gapThreshold;
	}

	/**
	 * Sets the gap threshold for discontinuous lines.
	 * 
	 * @param t
	 *           the threshold.
	 */
	public void setGapThreshold(double t) {
		this.gapThreshold = t;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns true if images are being plotted by the renderer.
	 * 
	 * @return <code>true</code> if images are being plotted by the renderer.
	 */
	public boolean getPlotImages() {
		return this.plotImages;
	}

	/**
	 * Sets the flag that controls whether or not an image is drawn at each data point.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setPlotImages(boolean flag) {
		if (this.plotImages != flag) {
			this.plotImages = flag;
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns true if lines should be discontinuous.
	 * 
	 * @return <code>true</code> if images are being plotted by the renderer.
	 */
	public boolean getPlotDiscontinuous() {
		return this.plotDiscontinuous;
	}

	/**
	 * Returns a legend item for a series.
	 * 
	 * @param datasetIndex
	 *           the dataset index (zero-based).
	 * @param series
	 *           the series index (zero-based).
	 * @return a legend item for the series.
	 */
	public LegendItem getLegendItem(int datasetIndex, int series) {

		LegendItem result = null;

		XYPlot plot = getPlot();
		if (plot != null) {
			XYDataset dataset = plot.getDataset(datasetIndex);

			if (dataset != null) {
				String label = dataset.getSeriesName(series);
				String description = label;
				Shape shape = getSeriesShape(series);
				boolean shapeFilled = getSeriesShapesFilled(series);
				Paint paint = getSeriesPaint(series);
				Paint outlinePaint = getSeriesOutlinePaint(series);
				Stroke stroke = getSeriesStroke(series);

				result = new LegendItem(
									label, description, shape, shapeFilled, paint, stroke, outlinePaint, stroke
									);
			}

		}

		return result;

	}

	/**
	 * Draws the visual representation of a single data item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the area within which the data is being drawn.
	 * @param info
	 *           collects information about the drawing.
	 * @param plot
	 *           the plot (can be used to obtain standard color information etc).
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 * @param dataset
	 *           the dataset.
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @param crosshairState
	 *           crosshair information for the plot (<code>null</code> permitted).
	 * @param pass
	 *           the pass index.
	 */
	public void drawItem(Graphics2D g2,
									XYItemRendererState state,
									Rectangle2D dataArea,
									PlotRenderingInfo info,
									XYPlot plot,
									ValueAxis domainAxis,
									ValueAxis rangeAxis,
									XYDataset dataset,
									int series,
									int item,
									CrosshairState crosshairState,
									int pass) {

		// setup for collecting optional entity info...
		Shape entityArea = null;
		EntityCollection entities = null;
		if (info != null) {
			entities = info.getOwner().getEntityCollection();
		}

		PlotOrientation orientation = plot.getOrientation();
		Paint paint = getItemPaint(series, item);
		Stroke seriesStroke = getItemStroke(series, item);
		g2.setPaint(paint);
		g2.setStroke(seriesStroke);

		// get the data point...
		Number x1n = dataset.getXValue(series, item);
		Number y1n = dataset.getYValue(series, item);
		if (y1n == null || x1n == null) {
			return;
		}

		double x1 = x1n.doubleValue();
		double y1 = y1n.doubleValue();
		final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

		if (getPlotLines()) {

			if (item > 0) {
				// get the previous data point...
				Number x0n = dataset.getXValue(series, item - 1);
				Number y0n = dataset.getYValue(series, item - 1);
				if (y0n != null && x0n != null) {
					double x0 = x0n.doubleValue();
					double y0 = y0n.doubleValue();
					boolean drawLine = true;
					if (getPlotDiscontinuous()) {
						// only draw a line if the gap between the current and previous data
						// point is within the threshold
						int numX = dataset.getItemCount(series);
						double minX = dataset.getXValue(series, 0).doubleValue();
						double maxX = dataset.getXValue(series, numX - 1).doubleValue();
						drawLine = (x1 - x0) <= ((maxX - minX) / numX * getGapThreshold());
					}
					if (drawLine) {
						double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
						double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

						// only draw if we have good values
						if (Double.isNaN(transX0) || Double.isNaN(transY0)
											|| Double.isNaN(transX1) || Double.isNaN(transY1)) {
							return;
						}

						if (orientation == PlotOrientation.HORIZONTAL) {
							state.workingLine.setLine(transY0, transX0, transY1, transX1);
						} else
							if (orientation == PlotOrientation.VERTICAL) {
								state.workingLine.setLine(transX0, transY0, transX1, transY1);
							}

						if (state.workingLine.intersects(dataArea)) {
							g2.draw(state.workingLine);
						}
					}
				}
			}
		}

		if (getPlotShapes()) {

			Shape shape = getItemShape(series, item);
			if (orientation == PlotOrientation.HORIZONTAL) {
				shape = createTransformedShape(shape, transY1, transX1);
			} else
				if (orientation == PlotOrientation.VERTICAL) {
					shape = createTransformedShape(shape, transX1, transY1);
				}
			if (shape.intersects(dataArea)) {
				if (getItemShapeFilled(series, item)) {
					g2.fill(shape);
				} else {
					g2.draw(shape);
				}
			}
			entityArea = shape;

		}

		if (getPlotImages()) {
			// use shape scale with transform??
			// double scale = getShapeScale(plot, series, item, transX1, transY1);
			Image image = getImage(plot, series, item, transX1, transY1);
			if (image != null) {
				Point hotspot = getImageHotspot(plot, series, item, transX1, transY1, image);
				g2.drawImage(
									image, (int) (transX1 - hotspot.getX()), (int) (transY1 - hotspot.getY()), null
									);
				entityArea = new Rectangle2D.Double(
									transX1 - hotspot.getX(), transY1 - hotspot.getY(),
									image.getWidth(null), image.getHeight(null)
									);
			}

		}

		// draw the item label if there is one...
		if (isItemLabelVisible(series, item)) {
			drawItemLabel(
								g2, orientation, dataset, series, item, transX1, transY1, (y1 < 0.0));
		}

		updateCrosshairValues(crosshairState, x1, y1, transX1, transY1, orientation);

		// add an entity for the item...
		if (entities != null) {
			if (entityArea == null) {
				entityArea = new Rectangle2D.Double(transX1 - 2, transY1 - 2, 4, 4);
			}
			String tip = null;
			XYToolTipGenerator generator = getToolTipGenerator(series, item);
			if (generator != null) {
				tip = generator.generateToolTip(dataset, series, item);
			}
			String url = null;
			if (getURLGenerator() != null) {
				url = getURLGenerator().generateURL(dataset, series, item);
			}
			XYItemEntity entity = new XYItemEntity(entityArea, dataset, series, item, tip, url);
			entities.addEntity(entity);
		}

	}

	/**
	 * Tests this renderer for equality with another object.
	 * 
	 * @param obj
	 *           the object.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof StandardXYItemRenderer) {
			StandardXYItemRenderer r = (StandardXYItemRenderer) obj;
			if (super.equals(obj)) {
				boolean b0 = (this.plotShapes == r.plotShapes);
				boolean b1 = (this.plotLines == r.plotLines);
				boolean b2 = (this.plotImages == r.plotImages);
				boolean b3 = (this.plotDiscontinuous == r.plotDiscontinuous);
				boolean b4 = (this.gapThreshold == r.gapThreshold);
				// boolean b5 = (this.defaultShapeFilled == r.defaultShapeFilled);
				return b0 && b1 && b2 && b3 && b4;
			}
		}

		return false;

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////
	// PROTECTED METHODS
	// These provide the opportunity to subclass the standard renderer and create custom effects.
	// //////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the image used to draw a single data item.
	 * 
	 * @param plot
	 *           the plot (can be used to obtain standard color information etc).
	 * @param series
	 *           the series index.
	 * @param item
	 *           the item index.
	 * @param x
	 *           the x value of the item.
	 * @param y
	 *           the y value of the item.
	 * @return the image.
	 */
	protected Image getImage(Plot plot, int series, int item, double x, double y) {
		// should this be added to the plot as well ?
		// return plot.getShape(series, item, x, y, scale);
		// or should this be left to the user - like this:
		return null;
	}

	/**
	 * Returns the hotspot of the image used to draw a single data item.
	 * The hotspot is the point relative to the top left of the image
	 * that should indicate the data item. The default is the center of the
	 * image.
	 * 
	 * @param plot
	 *           the plot (can be used to obtain standard color information etc).
	 * @param image
	 *           the image (can be used to get size information about the image)
	 * @param series
	 *           the series index
	 * @param item
	 *           the item index
	 * @param x
	 *           the x value of the item
	 * @param y
	 *           the y value of the item
	 * @return the hotspot used to draw the data item.
	 */
	protected Point getImageHotspot(Plot plot, int series, int item,
												double x, double y, Image image) {

		int height = image.getHeight(null);
		int width = image.getWidth(null);
		return new Point(width / 2, height / 2);

	}

	/**
	 * Returns a clone of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the renderer cannot be cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
