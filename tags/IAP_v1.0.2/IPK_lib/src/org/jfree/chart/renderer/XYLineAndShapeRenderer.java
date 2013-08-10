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
 * XYLineAndShapeRenderer.java
 * ---------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: XYLineAndShapeRenderer.java,v 1.1 2011-01-31 09:02:47 klukas Exp $
 * Changes:
 * --------
 * 27-Jan-2004 : Version 1 (DG);
 * 10-Feb-2004 : Minor change to drawItem() method to make cut-and-paste overriding easier (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.BooleanList;
import org.jfree.util.BooleanUtils;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A renderer that can be used with the {@link XYPlot} class.
 */
public class XYLineAndShapeRenderer extends AbstractXYItemRenderer
												implements XYItemRenderer,
																Cloneable,
																PublicCloneable,
																Serializable {

	/** A flag that controls whether or not lines are visible for ALL series. */
	private Boolean linesVisible;

	/** A table of flags that control (per series) whether or not lines are visible. */
	private BooleanList seriesLinesVisible;

	/** The default value returned by the getLinesVisible() method. */
	private boolean defaultLinesVisible;

	/** A flag that controls whether or not shapes are visible for ALL series. */
	private Boolean shapesVisible;

	/** A table of flags that control (per series) whether or not shapes are visible. */
	private BooleanList seriesShapesVisible;

	/** The default value returned by the getShapeVisible() method. */
	private boolean defaultShapesVisible;

	/** A flag that controls whether or not shapes are filled for ALL series. */
	private Boolean shapesFilled;

	/** A table of flags that control (per series) whether or not shapes are filled. */
	private BooleanList seriesShapesFilled;

	/** The default value returned by the getShapeFilled(...) method. */
	private boolean defaultShapesFilled;

	/** A flag that controls whether outlines are drawn for filled shapes. */
	private boolean drawOutlines;

	/** A flag that controls whether the outline paint is used for drawing shape outlines. */
	private boolean useOutlinePaint;

	/**
	 * Creates a new renderer with default settings.
	 */
	public XYLineAndShapeRenderer() {

		this.linesVisible = null;
		this.seriesLinesVisible = new BooleanList();
		this.defaultLinesVisible = true;

		this.shapesVisible = null;
		this.seriesShapesVisible = new BooleanList();
		this.defaultShapesVisible = true;

		this.shapesFilled = null;
		this.seriesShapesFilled = new BooleanList();
		this.defaultShapesFilled = true;

		this.drawOutlines = false; // don't draw outlines for filled shapes
		this.useOutlinePaint = false; // use item paint for outlines, not outline paint

	}

	/**
	 * Returns the number of passes through the data that the renderer requires in order to
	 * draw the chart. Most charts will require a single pass, but some require two passes.
	 * 
	 * @return The pass count.
	 */
	public int getPassCount() {
		return 2;
	}

	// LINES VISIBLE

	/**
	 * Returns the flag used to control whether or not the shape for an item is visible.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return A boolean.
	 */
	public boolean getItemLineVisible(int series, int item) {
		Boolean flag = this.linesVisible;
		if (flag == null) {
			flag = getSeriesLinesVisible(series);
		}
		if (flag != null) {
			return flag.booleanValue();
		} else {
			return this.defaultLinesVisible;
		}
	}

	/**
	 * Returns a flag that controls whether or not lines are drawn for ALL series. If this
	 * flag is <code>null</code>, then the "per series" settings will apply.
	 * 
	 * @return A flag (possibly <code>null</code>).
	 */
	public Boolean getLinesVisible() {
		return this.linesVisible;
	}

	/**
	 * Sets a flag that controls whether or not lines are drawn between the items in ALL series,
	 * and sends a {@link RendererChangeEvent} to all registered listeners. You need to set this
	 * to <code>null</code> if you want the "per series" settings to apply.
	 * 
	 * @param visible
	 *           the flag (<code>null</code> permitted).
	 */
	public void setLinesVisible(Boolean visible) {
		this.linesVisible = visible;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Sets a flag that controls whether or not lines are drawn between the items in ALL series,
	 * and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param visible
	 *           the flag.
	 */
	public void setLinesVisible(boolean visible) {
		setLinesVisible(BooleanUtils.valueOf(visible));
	}

	/**
	 * Returns the flag used to control whether or not the lines for a series are visible.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The flag (possibly <code>null</code>).
	 */
	public Boolean getSeriesLinesVisible(int series) {
		return this.seriesLinesVisible.getBoolean(series);
	}

	/**
	 * Sets the 'lines visible' flag for a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param flag
	 *           the flag (<code>null</code> permitted).
	 */
	public void setSeriesLinesVisible(int series, Boolean flag) {
		this.seriesLinesVisible.setBoolean(series, flag);
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Sets the 'lines visible' flag for a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param visible
	 *           the flag.
	 */
	public void setSeriesLinesVisible(int series, boolean visible) {
		setSeriesLinesVisible(series, BooleanUtils.valueOf(visible));
	}

	/**
	 * Returns the default 'lines visible' attribute.
	 * 
	 * @return The default flag.
	 */
	public boolean getDefaultLinesVisible() {
		return this.defaultLinesVisible;
	}

	/**
	 * Sets the default 'lines visible' flag.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setDefaultLinesVisible(boolean flag) {
		this.defaultLinesVisible = flag;
		notifyListeners(new RendererChangeEvent(this));
	}

	// SHAPES VISIBLE

	/**
	 * Returns the flag used to control whether or not the shape for an item is visible.
	 * <p>
	 * The default implementation passes control to the <code>getSeriesShapesVisible</code> method. You can override this method if you require different
	 * behaviour.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @return A boolean.
	 */
	public boolean getItemShapeVisible(int series, int item) {
		Boolean flag = this.shapesVisible;
		if (flag == null) {
			flag = getSeriesShapesVisible(series);
		}
		if (flag != null) {
			return flag.booleanValue();
		} else {
			return this.defaultShapesVisible;
		}
	}

	/**
	 * Returns the flag that controls whether the shapes are visible for the items in
	 * ALL series.
	 * 
	 * @return The flag (possibly <code>null</code>).
	 */
	public Boolean getShapesVisible() {
		return this.shapesVisible;
	}

	/**
	 * Sets the 'shapes visible' for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param visible
	 *           the flag (<code>null</code> permitted).
	 */
	public void setShapesVisible(Boolean visible) {
		this.shapesVisible = visible;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Sets the 'shapes visible' for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param visible
	 *           the flag.
	 */
	public void setShapesVisible(boolean visible) {
		setShapesVisible(BooleanUtils.valueOf(visible));
	}

	/**
	 * Returns the flag used to control whether or not the shapes for a series are visible.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return A boolean.
	 */
	public Boolean getSeriesShapesVisible(int series) {
		return this.seriesShapesVisible.getBoolean(series);
	}

	/**
	 * Sets the 'shapes visible' flag for a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param visible
	 *           the flag.
	 */
	public void setSeriesShapesVisible(int series, boolean visible) {
		setSeriesShapesVisible(series, BooleanUtils.valueOf(visible));
	}

	/**
	 * Sets the 'shapes visible' flag for a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param flag
	 *           the flag.
	 */
	public void setSeriesShapesVisible(int series, Boolean flag) {
		this.seriesShapesVisible.setBoolean(series, flag);
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the default 'shape visible' attribute.
	 * 
	 * @return The default flag.
	 */
	public boolean getDefaultShapesVisible() {
		return this.defaultShapesVisible;
	}

	/**
	 * Sets the default 'shapes visible' flag.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setDefaultShapesVisible(boolean flag) {
		this.defaultShapesVisible = flag;
		notifyListeners(new RendererChangeEvent(this));
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
		Boolean flag = this.shapesFilled;
		if (flag == null) {
			flag = getSeriesShapesFilled(series);
		}
		if (flag != null) {
			return flag.booleanValue();
		} else {
			return this.defaultShapesVisible;
		}
	}

	/**
	 * Sets the 'shapes filled' for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param filled
	 *           the flag.
	 */
	public void setShapesFilled(boolean filled) {
		setShapesFilled(BooleanUtils.valueOf(filled));
	}

	/**
	 * Sets the 'shapes filled' for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param filled
	 *           the flag (<code>null</code> permitted).
	 */
	public void setShapesFilled(Boolean filled) {
		this.shapesFilled = filled;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the flag used to control whether or not the shapes for a series are filled.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return A boolean.
	 */
	public Boolean getSeriesShapesFilled(int series) {
		return this.seriesShapesFilled.getBoolean(series);
	}

	/**
	 * Sets the 'shapes filled' flag for a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param flag
	 *           the flag.
	 */
	public void setSeriesShapesFilled(int series, boolean flag) {
		setSeriesShapesFilled(series, BooleanUtils.valueOf(flag));
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
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the default 'shape filled' attribute.
	 * 
	 * @return The default flag.
	 */
	public boolean getDefaultShapesFilled() {
		return this.defaultShapesFilled;
	}

	/**
	 * Sets the default 'shapes filled' flag.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setDefaultShapesFilled(boolean flag) {
		this.defaultShapesFilled = flag;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns <code>true</code> if outlines should be drawn for filled shapes, and <code>false</code> otherwise.
	 * 
	 * @return A boolean.
	 */
	public boolean getDrawOutlines() {
		return this.drawOutlines;
	}

	/**
	 * Sets the flag that controls whether outlines are drawn for filled shapes, and sends
	 * a {@link RendererChangeEvent} to all registered listeners.
	 * <P>
	 * In some cases, shapes look better if they do NOT have an outline, but this flag allows you to set your own preference.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setDrawOutlines(boolean flag) {
		this.drawOutlines = flag;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns <code>true</code> if the renderer should use the outline paint setting to draw
	 * shape outlines, and <code>false</code> if it should just use the regular paint.
	 * 
	 * @return A boolean.
	 */
	public boolean getUseOutlinePaint() {
		return this.useOutlinePaint;
	}

	/**
	 * Sets the flag that controls whether the outline paint is used to draw shape outlines, and
	 * sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setUseOutlinePaint(boolean flag) {
		this.useOutlinePaint = flag;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Initialises the renderer.
	 * <P>
	 * This method will be called before the first item is rendered, giving the renderer an opportunity to initialise any state information it wants to maintain.
	 * The renderer can do nothing if it chooses.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the area inside the axes.
	 * @param plot
	 *           the plot.
	 * @param data
	 *           the data.
	 * @param info
	 *           an optional info collection object to return data back to the caller.
	 * @return The renderer state.
	 */
	public XYItemRendererState initialise(Graphics2D g2,
														Rectangle2D dataArea,
														XYPlot plot,
														XYDataset data,
														PlotRenderingInfo info) {

		XYItemRendererState state = new XYItemRendererState(info);
		return state;

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

		if (pass == 0) {
			if (getItemLineVisible(series, item) && item > 0) {
				// get the previous data point...
				Number x0n = dataset.getXValue(series, item - 1);
				Number y0n = dataset.getYValue(series, item - 1);
				if (y0n != null && x0n != null) {
					double x0 = x0n.doubleValue();
					double y0 = y0n.doubleValue();
					double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
					double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

					// only draw if we have good values
					if (Double.isNaN(transX0) || Double.isNaN(transY0)
										|| Double.isNaN(transX1) || Double.isNaN(transY1)) {
						return;
					}

					PlotOrientation orientation = plot.getOrientation();
					if (orientation == PlotOrientation.HORIZONTAL) {
						state.workingLine.setLine(transY0, transX0, transY1, transX1);
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							state.workingLine.setLine(transX0, transY0, transX1, transY1);
						}

					if (state.workingLine.intersects(dataArea)) {
						g2.setStroke(getItemStroke(series, item));
						g2.setPaint(getItemPaint(series, item));
						g2.draw(state.workingLine);
					}
				}
			}
		} else
			if (pass == 1) {
				if (getItemShapeVisible(series, item)) {
					Shape shape = getItemShape(series, item);
					PlotOrientation orientation = plot.getOrientation();
					if (orientation == PlotOrientation.HORIZONTAL) {
						shape = createTransformedShape(shape, transY1, transX1);
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							shape = createTransformedShape(shape, transX1, transY1);
						}
					if (shape.intersects(dataArea)) {
						if (getItemShapeFilled(series, item)) {
							g2.setPaint(getItemPaint(series, item));
							g2.fill(shape);
							if (getDrawOutlines()) {
								if (getUseOutlinePaint()) {
									g2.setPaint(getItemOutlinePaint(series, item));
								} else {
									g2.setPaint(getItemPaint(series, item));
								}
								g2.draw(shape);
							}
						} else {
							if (getUseOutlinePaint()) {
								g2.setPaint(getItemOutlinePaint(series, item));
							} else {
								g2.setPaint(getItemPaint(series, item));
							}
							g2.draw(shape);
						}
					}
				}
			}

	}

	/**
	 * Returns a clone of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the clone cannot be created.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
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

		if (obj instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) obj;
			if (super.equals(obj)) {
				boolean b0 = ObjectUtils.equal(this.linesVisible, r.linesVisible);
				boolean b1 = ObjectUtils.equal(this.seriesLinesVisible, r.seriesLinesVisible);
				boolean b2 = (this.defaultLinesVisible == r.defaultLinesVisible);
				boolean b3 = ObjectUtils.equal(this.shapesVisible, r.shapesVisible);
				boolean b4 = ObjectUtils.equal(this.seriesShapesVisible, r.seriesShapesVisible);
				boolean b5 = (this.defaultShapesVisible == r.defaultShapesVisible);
				boolean b6 = ObjectUtils.equal(this.shapesFilled, r.shapesFilled);
				boolean b7 = ObjectUtils.equal(this.seriesShapesFilled, r.seriesShapesFilled);
				boolean b8 = (this.defaultShapesFilled == r.defaultShapesFilled);
				boolean b9 = (this.drawOutlines == r.drawOutlines);
				boolean b10 = (this.useOutlinePaint == r.useOutlinePaint);
				return b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9 && b10;
			}
		}

		return false;

	}

}
