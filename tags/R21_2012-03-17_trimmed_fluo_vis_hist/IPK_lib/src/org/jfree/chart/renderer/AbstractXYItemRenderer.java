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
 * AbstractXYItemRenderer.java
 * ---------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Richard Atkinson;
 * Focus Computer Services Limited;
 * Tim Bardzil;
 * $Id: AbstractXYItemRenderer.java,v 1.1 2011-01-31 09:02:48 klukas Exp $
 * Changes:
 * --------
 * 15-Mar-2002 : Version 1 (DG);
 * 09-Apr-2002 : Added a getToolTipGenerator() method reflecting the change in the XYItemRenderer
 * interface (DG);
 * 05-Aug-2002 : Added a urlGenerator member variable to support HTML image maps (RA);
 * 20-Aug-2002 : Added property change events for the tooltip and URL generators (DG);
 * 22-Aug-2002 : Moved property change support into AbstractRenderer class (DG);
 * 23-Sep-2002 : Fixed errors reported by Checkstyle tool (DG);
 * 18-Nov-2002 : Added methods for drawing grid lines (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified initialise(...) return type and drawItem(...) method signature (DG);
 * 15-May-2003 : Modified to take into account the plot orientation (DG);
 * 21-May-2003 : Added labels to markers (DG);
 * 05-Jun-2003 : Added domain and range grid bands (sponsored by Focus Computer Services Ltd) (DG);
 * 27-Jul-2003 : Added getRangeType() to support stacked XY area charts (RA);
 * 31-Jul-2003 : Deprecated all but the default constructor (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 05-Nov-2003 : Fixed marker rendering bug (833623) (DG);
 * 11-Feb-2004 : Updated labelling for markers (DG);
 * 25-Feb-2004 : Added updateCrosshairValues() method. Moved deprecated code to bottom of
 * source file (DG);
 * 16-Apr-2004 : Added support for IntervalMarker in drawRangeMarker() method - thanks to
 * Tim Bardzil (DG);
 * 05-May-2004 : Fixed bug (948310) where interval markers extend beyond axis range (DG);
 * 03-Jun-2004 : Fixed more bugs in drawing interval markers (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.data.XYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.LogContext;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A base class that can be used to create new {@link XYItemRenderer} implementations.
 */
public abstract class AbstractXYItemRenderer extends AbstractRenderer
															implements XYItemRenderer,
																			Cloneable,
																			Serializable {

	/** The plot. */
	private XYPlot plot;

	/** The item label generator for ALL series. */
	private XYLabelGenerator itemLabelGenerator;

	/** A list of item label generators (one per series). */
	private ObjectList itemLabelGeneratorList;

	/** The base item label generator. */
	private XYLabelGenerator baseItemLabelGenerator;

	/** The tool tip generator for ALL series. */
	private XYToolTipGenerator toolTipGenerator;

	/** A list of tool tip generators (one per series). */
	private ObjectList toolTipGeneratorList;

	/** The base tool tip generator. */
	private XYToolTipGenerator baseToolTipGenerator;

	/** The URL text generator. */
	private XYURLGenerator urlGenerator;

	/** Access to logging facilities. */
	private static final LogContext LOGGER = Log.createContext(AbstractXYItemRenderer.class);

	/**
	 * Creates a renderer where the tooltip generator and the URL generator are both <code>null</code>.
	 */
	protected AbstractXYItemRenderer() {
		this.itemLabelGenerator = null;
		this.itemLabelGeneratorList = new ObjectList();
		this.toolTipGenerator = null;
		this.toolTipGeneratorList = new ObjectList();
		this.urlGenerator = null;
	}

	/**
	 * Returns the number of passes through the data that the renderer requires in order to
	 * draw the chart. Most charts will require a single pass, but some require two passes.
	 * 
	 * @return The pass count.
	 */
	public int getPassCount() {
		return 1;
	}

	/**
	 * Returns the plot that the renderer is assigned to.
	 * 
	 * @return The plot.
	 */
	public XYPlot getPlot() {
		return this.plot;
	}

	/**
	 * Sets the plot that the renderer is assigned to.
	 * 
	 * @param plot
	 *           the plot.
	 */
	public void setPlot(XYPlot plot) {
		this.plot = plot;
	}

	/**
	 * Initialises the renderer and returns a state object that should be passed to all
	 * subsequent calls to the drawItem() method.
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
	 * @return the renderer state (never <code>null</code>).
	 */
	public XYItemRendererState initialise(Graphics2D g2,
														Rectangle2D dataArea,
														XYPlot plot,
														XYDataset data,
														PlotRenderingInfo info) {

		XYItemRendererState state = new XYItemRendererState(info);
		return state;

	}

	// LABEL GENERATOR

	/**
	 * Returns the label generator for a data item. This implementation simply passes control to
	 * the {@link #getSeriesLabelGenerator(int)} method. If, for some reason, you want a different
	 * generator for individual items, you can override this method.
	 * 
	 * @param row
	 *           the row index (zero based).
	 * @param column
	 *           the column index (zero based).
	 * @return the generator (possibly <code>null</code>).
	 */
	public XYLabelGenerator getLabelGenerator(int row, int column) {
		return getSeriesLabelGenerator(row);
	}

	/**
	 * Returns the label generator for a series.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @return the generator (possibly <code>null</code>).
	 */
	public XYLabelGenerator getSeriesLabelGenerator(int series) {

		// return the generator for ALL series, if there is one...
		if (this.itemLabelGenerator != null) {
			return this.itemLabelGenerator;
		}

		// otherwise look up the generator table
		XYLabelGenerator generator = (XYLabelGenerator) this.itemLabelGeneratorList.get(series);
		if (generator == null) {
			generator = this.baseItemLabelGenerator;
		}
		return generator;

	}

	/**
	 * Sets the item label generator for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setLabelGenerator(XYLabelGenerator generator) {
		this.itemLabelGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Sets the label generator for a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setSeriesLabelGenerator(int series, XYLabelGenerator generator) {
		this.itemLabelGeneratorList.set(series, generator);
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the base item label generator.
	 * 
	 * @return the generator (possibly <code>null</code>).
	 */
	public XYLabelGenerator getBaseLabelGenerator() {
		return this.baseItemLabelGenerator;
	}

	/**
	 * Sets the base item label generator and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setBaseLabelGenerator(XYLabelGenerator generator) {
		this.baseItemLabelGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	// TOOL TIP GENERATOR

	/**
	 * Returns the tool tip generator for a data item. This implementation simply passes control
	 * to the getSeriesToolTipGenerator() method. If, for some reason, you want a different
	 * generator for individual items, you can override this method.
	 * 
	 * @param row
	 *           the row index (zero based).
	 * @param column
	 *           the column index (zero based).
	 * @return The generator (possibly <code>null</code>).
	 */
	public XYToolTipGenerator getToolTipGenerator(int row, int column) {
		return getSeriesToolTipGenerator(row);
	}

	/**
	 * Returns the tool tip generator for a series.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @return The generator (possibly <code>null</code>).
	 */
	public XYToolTipGenerator getSeriesToolTipGenerator(int series) {

		// return the generator for ALL series, if there is one...
		if (this.toolTipGenerator != null) {
			return this.toolTipGenerator;
		}

		// otherwise look up the generator table
		XYToolTipGenerator generator = (XYToolTipGenerator) this.toolTipGeneratorList.get(series);
		if (generator == null) {
			generator = this.baseToolTipGenerator;
		}
		return generator;

	}

	/**
	 * Sets the tool tip generator for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setToolTipGenerator(XYToolTipGenerator generator) {
		this.toolTipGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Sets the tool tip generator for a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setSeriesToolTipGenerator(int series, XYToolTipGenerator generator) {
		this.toolTipGeneratorList.set(series, generator);
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the base tool tip generator.
	 * 
	 * @return The generator (possibly <code>null</code>).
	 */
	public XYToolTipGenerator getBaseToolTipGenerator() {
		return this.baseToolTipGenerator;
	}

	/**
	 * Sets the base tool tip generator and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setBaseToolTipGenerator(XYToolTipGenerator generator) {
		this.baseToolTipGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	// URL GENERATOR

	/**
	 * Returns the URL generator for HTML image maps.
	 * 
	 * @return the URL generator (possibly <code>null</code>).
	 */
	public XYURLGenerator getURLGenerator() {
		return this.urlGenerator;
	}

	/**
	 * Sets the URL generator for HTML image maps.
	 * 
	 * @param urlGenerator
	 *           the URL generator (<code>null</code> permitted).
	 */
	public void setURLGenerator(XYURLGenerator urlGenerator) {
		this.urlGenerator = urlGenerator;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the range type for the renderer.
	 * <p>
	 * The default implementation returns <code>STANDARD</code>, subclasses may override this behaviour.
	 * <p>
	 * The {@link org.jfree.chart.plot.XYPlot} uses this information when auto-calculating the range for the axis.
	 * 
	 * @return the range type.
	 */
	public RangeType getRangeType() {
		return RangeType.STANDARD;
	}

	/**
	 * Returns the range of values the renderer requires to display all the items from the
	 * specified dataset.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 * @return The range (or <code>null</code> if the dataset is <code>null</code> or empty).
	 */
	public Range getRangeExtent(XYDataset dataset) {
		return DatasetUtilities.getRangeExtent(dataset);
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

		XYPlot xyplot = getPlot();
		if (xyplot != null) {
			XYDataset dataset = xyplot.getDataset(datasetIndex);
			if (dataset != null) {
				String label = dataset.getSeriesName(series);
				String description = label;
				Shape shape = getSeriesShape(series);
				Paint paint = getSeriesPaint(series);
				Paint outlinePaint = getSeriesOutlinePaint(series);
				Stroke stroke = getSeriesStroke(series);

				result = new LegendItem(
									label, description, shape, true, paint, stroke, outlinePaint, stroke
									);
			}

		}

		return result;

	}

	/**
	 * Fills a band between two values on the axis. This can be used to color bands between the
	 * grid lines.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param axis
	 *           the domain axis.
	 * @param dataArea
	 *           the data area.
	 * @param start
	 *           the start value.
	 * @param end
	 *           the end value.
	 */
	public void fillDomainGridBand(Graphics2D g2,
												XYPlot plot,
												ValueAxis axis,
												Rectangle2D dataArea,
												double start, double end) {

		double x1 = axis.valueToJava2D(start, dataArea, plot.getDomainAxisEdge());
		double x2 = axis.valueToJava2D(end, dataArea, plot.getDomainAxisEdge());
		// TODO: need to change the next line to take account of plot orientation...
		Rectangle2D band = new Rectangle2D.Double(
							x1, dataArea.getMinY(), x2 - x1, dataArea.getMaxY() - dataArea.getMinY()
							);
		Paint paint = plot.getDomainTickBandPaint();

		if (paint != null) {
			g2.setPaint(paint);
			g2.fill(band);
		}

	}

	/**
	 * Fills a band between two values on the range axis. This can be used to color bands between
	 * the grid lines.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param axis
	 *           the range axis.
	 * @param dataArea
	 *           the data area.
	 * @param start
	 *           the start value.
	 * @param end
	 *           the end value.
	 */
	public void fillRangeGridBand(Graphics2D g2,
												XYPlot plot,
												ValueAxis axis,
												Rectangle2D dataArea,
												double start, double end) {

		double y1 = axis.valueToJava2D(start, dataArea, plot.getRangeAxisEdge());
		double y2 = axis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());
		// TODO: need to change the next line to take account of the plot orientation
		Rectangle2D band = new Rectangle2D.Double(
							dataArea.getMinX(), y2, dataArea.getWidth(), y1 - y2
							);
		Paint paint = plot.getRangeTickBandPaint();

		if (paint != null) {
			g2.setPaint(paint);
			g2.fill(band);
		}

	}

	/**
	 * Draws a grid line against the range axis.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param axis
	 *           the value axis.
	 * @param dataArea
	 *           the area for plotting data (not yet adjusted for any 3D effect).
	 * @param value
	 *           the value at which the grid line should be drawn.
	 */
	public void drawDomainGridLine(Graphics2D g2,
												XYPlot plot,
												ValueAxis axis,
												Rectangle2D dataArea,
												double value) {

		Range range = axis.getRange();
		if (!range.contains(value)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		double v = axis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());
		Line2D line = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
			}

		Paint paint = plot.getDomainGridlinePaint();
		Stroke stroke = plot.getDomainGridlineStroke();
		g2.setPaint(paint != null ? paint : Plot.DEFAULT_OUTLINE_PAINT);
		g2.setStroke(stroke != null ? stroke : Plot.DEFAULT_OUTLINE_STROKE);
		g2.draw(line);

	}

	/**
	 * Draws a grid line against the range axis.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param axis
	 *           the value axis.
	 * @param dataArea
	 *           the area for plotting data (not yet adjusted for any 3D effect).
	 * @param value
	 *           the value at which the grid line should be drawn.
	 */
	public void drawRangeGridLine(Graphics2D g2,
												XYPlot plot,
												ValueAxis axis,
												Rectangle2D dataArea,
												double value) {

		Range range = axis.getRange();
		if (!range.contains(value)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		Line2D line = null;
		double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
		if (orientation == PlotOrientation.HORIZONTAL) {
			line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
			}

		Paint paint = plot.getRangeGridlinePaint();
		Stroke stroke = plot.getRangeGridlineStroke();
		g2.setPaint(paint != null ? paint : Plot.DEFAULT_OUTLINE_PAINT);
		g2.setStroke(stroke != null ? stroke : Plot.DEFAULT_OUTLINE_STROKE);
		g2.draw(line);

	}

	/**
	 * Draws a vertical line on the chart to represent a 'range marker'.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param domainAxis
	 *           the domain axis.
	 * @param marker
	 *           the marker line.
	 * @param dataArea
	 *           the axis data area.
	 */
	public void drawDomainMarker(Graphics2D g2,
											XYPlot plot,
											ValueAxis domainAxis,
											Marker marker,
											Rectangle2D dataArea) {

		if (marker instanceof ValueMarker) {
			ValueMarker vm = (ValueMarker) marker;
			double value = vm.getValue();
			Range range = domainAxis.getRange();
			if (!range.contains(value)) {
				return;
			}

			double v = domainAxis.valueToJava2D(value, dataArea, plot.getDomainAxisEdge());

			PlotOrientation orientation = plot.getOrientation();
			Line2D line = null;
			if (orientation == PlotOrientation.HORIZONTAL) {
				line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
			} else
				if (orientation == PlotOrientation.VERTICAL) {
					line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
				}

			g2.setPaint(marker.getPaint());
			g2.setStroke(marker.getStroke());
			g2.draw(line);

			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				double[] coordinates = calculateDomainMarkerTextAnchorPoint(
									g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), anchor,
									false
									);
				RefineryUtilities.drawAlignedString(
									label, g2, (float) coordinates[0], (float) coordinates[1],
									marker.getLabelTextAnchor()
									);
			}
		} else
			if (marker instanceof IntervalMarker) {
				IntervalMarker im = (IntervalMarker) marker;
				double start = im.getStartValue();
				double end = im.getEndValue();
				Range range = domainAxis.getRange();
				if (!(range.intersects(start, end))) {
					return;
				}

				// don't draw beyond the axis range...
				start = range.constrain(start);
				end = range.constrain(end);

				double v0 = domainAxis.valueToJava2D(start, dataArea, plot.getDomainAxisEdge());
				double v1 = domainAxis.valueToJava2D(end, dataArea, plot.getDomainAxisEdge());

				PlotOrientation orientation = plot.getOrientation();
				Rectangle2D rect = null;
				if (orientation == PlotOrientation.HORIZONTAL) {
					rect = new Rectangle2D.Double(
										dataArea.getMinX(), Math.min(v0, v1), dataArea.getWidth(), Math.abs(v1 - v0)
										);
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						rect = new Rectangle2D.Double(
											Math.min(v0, v1), dataArea.getMinY(), Math.abs(v1 - v0), dataArea.getHeight()
											);
					}

				g2.setPaint(marker.getPaint());
				g2.fill(rect);

				String label = marker.getLabel();
				RectangleAnchor anchor = marker.getLabelAnchor();
				if (label != null) {
					Font labelFont = marker.getLabelFont();
					g2.setFont(labelFont);
					g2.setPaint(marker.getLabelPaint());
					double[] coordinates = calculateDomainMarkerTextAnchorPoint(
										g2, orientation, dataArea, rect, marker.getLabelOffset(), anchor, true
										);
					RefineryUtilities.drawAlignedString(
										label, g2, (float) coordinates[0], (float) coordinates[1],
										marker.getLabelTextAnchor()
										);
				}

			}

	}

	/**
	 * Calculates the (x, y) coordinates for drawing a marker label.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param orientation
	 *           the plot orientation.
	 * @param dataArea
	 *           the data area.
	 * @param markerArea
	 *           the rectangle surrounding the marker area.
	 * @param markerOffset
	 *           the marker label offset
	 * @param anchor
	 *           the label anchor.
	 * @param inset
	 *           a flag that controls whether the marker label offset is inside or outside the
	 *           marker area.
	 * @return the coordinates for drawing the marker label.
	 */
	private double[] calculateDomainMarkerTextAnchorPoint(Graphics2D g2,
						PlotOrientation orientation,
						Rectangle2D dataArea,
						Rectangle2D markerArea,
						RectangleInsets markerOffset,
						RectangleAnchor anchor,
						boolean inset) {

		double[] result = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			Rectangle2D anchorRect = null;
			if (inset) {
				anchorRect = markerOffset.createInsetRectangle(markerArea, false, true);
			} else {
				anchorRect = markerOffset.createOutsetRectangle(markerArea, false, true);
			}
			result = RectangleAnchor.coordinates(anchorRect, anchor);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				Rectangle2D anchorRect = null;
				if (inset) {
					anchorRect = markerOffset.createInsetRectangle(markerArea, false, true);
				} else {
					anchorRect = markerOffset.createOutsetRectangle(markerArea, false, true);
				}
				result = RectangleAnchor.coordinates(anchorRect, anchor);
			}
		return result;

	}

	/**
	 * Draws a horizontal line across the chart to represent a 'range marker'.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param rangeAxis
	 *           the range axis.
	 * @param marker
	 *           the marker line.
	 * @param dataArea
	 *           the axis data area.
	 */
	public void drawRangeMarker(Graphics2D g2,
											XYPlot plot,
											ValueAxis rangeAxis,
											Marker marker,
											Rectangle2D dataArea) {

		if (marker instanceof ValueMarker) {
			ValueMarker vm = (ValueMarker) marker;
			double value = vm.getValue();
			Range range = rangeAxis.getRange();
			if (!range.contains(value)) {
				return;
			}

			double v = rangeAxis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
			PlotOrientation orientation = plot.getOrientation();
			Line2D line = null;
			if (orientation == PlotOrientation.HORIZONTAL) {
				line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
			} else
				if (orientation == PlotOrientation.VERTICAL) {
					line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
				}
			g2.setPaint(marker.getPaint());
			g2.setStroke(marker.getStroke());
			g2.draw(line);

			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				double[] coordinates = calculateRangeMarkerTextAnchorPoint(
									g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), anchor
									);
				RefineryUtilities.drawAlignedString(
									label, g2, (float) coordinates[0], (float) coordinates[1],
									marker.getLabelTextAnchor()
									);
			}
		} else
			if (marker instanceof IntervalMarker) {

				IntervalMarker im = (IntervalMarker) marker;
				double start = im.getStartValue();
				double end = im.getEndValue();
				Range range = rangeAxis.getRange();
				if (!(range.intersects(start, end))) {
					return;
				}

				// don't draw beyond the axis range...
				start = range.constrain(start);
				end = range.constrain(end);

				double v0 = rangeAxis.valueToJava2D(start, dataArea, plot.getRangeAxisEdge());
				double v1 = rangeAxis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());

				PlotOrientation orientation = plot.getOrientation();
				Rectangle2D rect = null;
				if (orientation == PlotOrientation.HORIZONTAL) {
					rect = new Rectangle2D.Double(
										Math.min(v0, v1), dataArea.getMinY(), Math.abs(v1 - v0), dataArea.getHeight()
										);
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						rect = new Rectangle2D.Double(
											dataArea.getMinX(), Math.min(v0, v1), dataArea.getWidth(), Math.abs(v0 - v1)
											);
					}

				g2.setPaint(marker.getPaint());
				g2.fill(rect);
				String label = marker.getLabel();
				RectangleAnchor anchor = marker.getLabelAnchor();
				if (label != null) {
					Font labelFont = marker.getLabelFont();
					g2.setFont(labelFont);
					g2.setPaint(marker.getLabelPaint());
					double[] coordinates = calculateDomainMarkerTextAnchorPoint(
										g2, orientation, dataArea, rect, marker.getLabelOffset(), anchor, true
										);
					RefineryUtilities.drawAlignedString(
										label, g2, (float) coordinates[0], (float) coordinates[1],
										marker.getLabelTextAnchor()
										);
				}
			}
	}

	/**
	 * Calculates the (x, y) coordinates for drawing a marker label.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param orientation
	 *           the plot orientation.
	 * @param dataArea
	 *           the data area.
	 * @param markerArea
	 *           the marker area.
	 * @param markerOffset
	 *           the marker offset.
	 * @param anchor
	 *           the label anchor.
	 * @return the coordinates for drawing the marker label.
	 */
	private double[] calculateRangeMarkerTextAnchorPoint(Graphics2D g2,
																			PlotOrientation orientation,
																			Rectangle2D dataArea,
																			Rectangle2D markerArea,
																			RectangleInsets markerOffset,
																			RectangleAnchor anchor) {

		double[] result = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			Rectangle2D anchorRect = markerOffset.createOutsetRectangle(markerArea, true, false);
			result = RectangleAnchor.coordinates(anchorRect, anchor);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				Rectangle2D anchorRect = markerOffset.createOutsetRectangle(markerArea, false, true);
				result = RectangleAnchor.coordinates(anchorRect, anchor);
			}
		return result;

	}

	/**
	 * Returns a clone of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the renderer does not support cloning.
	 */
	protected Object clone() throws CloneNotSupportedException {
		AbstractXYItemRenderer clone = (AbstractXYItemRenderer) super.clone();
		// 'plot' : just retain reference, not a deep copy
		if (this.itemLabelGenerator != null && this.itemLabelGenerator instanceof PublicCloneable) {
			PublicCloneable pc = (PublicCloneable) this.itemLabelGenerator;
			clone.itemLabelGenerator = (XYLabelGenerator) pc.clone();
		}
		return clone;
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

		if (!(obj instanceof AbstractXYItemRenderer)) {
			return false;
		}

		AbstractXYItemRenderer renderer = (AbstractXYItemRenderer) obj;
		if (!super.equals(obj)) {
			return false;
		}
		if (!ObjectUtils.equal(this.itemLabelGenerator, renderer.itemLabelGenerator)) {
			return false;
		}
		if (!ObjectUtils.equal(this.urlGenerator, renderer.urlGenerator)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the drawing supplier from the plot.
	 * 
	 * @return The drawing supplier (possibly <code>null</code>).
	 */
	public DrawingSupplier getDrawingSupplier() {
		DrawingSupplier result = null;
		XYPlot p = getPlot();
		if (p != null) {
			result = p.getDrawingSupplier();
		}
		return result;
	}

	/**
	 * Considers the current (x, y) coordinate and updates the crosshair point if it meets the
	 * criteria (usually means the (x, y) coordinate is the closest to the anchor point so far).
	 * 
	 * @param crosshairState
	 *           the crosshair state (<code>null</code> permitted, but the method does
	 *           nothing in that case).
	 * @param x
	 *           the x-value (in data space).
	 * @param y
	 *           the y-value (in data space).
	 * @param transX
	 *           the x-value translated to Java2D space.
	 * @param transY
	 *           the y-value translated to Java2D space.
	 * @param orientation
	 *           the plot orientation (<code>null</code> not permitted).
	 */
	protected void updateCrosshairValues(CrosshairState crosshairState,
														double x, double y, double transX, double transY,
														PlotOrientation orientation) {

		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}

		if (crosshairState != null) {
			// do we need to update the crosshair values?
			if (this.plot.isDomainCrosshairLockedOnData()) {
				if (this.plot.isRangeCrosshairLockedOnData()) {
					// both axes
					crosshairState.updateCrosshairPoint(x, y, transX, transY, orientation);
				} else {
					// just the domain axis...
					crosshairState.updateCrosshairX(x);
				}
			} else {
				if (this.plot.isRangeCrosshairLockedOnData()) {
					// just the range axis...
					crosshairState.updateCrosshairY(y);
				}
			}
		}

	}

	/**
	 * Draws an item label.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param orientation
	 *           the orientation.
	 * @param dataset
	 *           the dataset.
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @param x
	 *           the x coordinate (in Java2D space).
	 * @param y
	 *           the y coordinate (in Java2D space).
	 * @param negative
	 *           indicates a negative value (which affects the item label position).
	 */
	protected void drawItemLabel(Graphics2D g2,
											PlotOrientation orientation,
											XYDataset dataset,
											int series,
											int item,
											double x,
											double y,
											boolean negative) {

		XYLabelGenerator generator = getLabelGenerator(series, item);
		if (generator != null) {
			Font labelFont = getItemLabelFont(series, item);
			Paint paint = getItemLabelPaint(series, item);
			g2.setFont(labelFont);
			g2.setPaint(paint);
			String label = generator.generateLabel(dataset, series, item);

			// get the label position..
			ItemLabelPosition position = null;
			if (!negative) {
				position = getPositiveItemLabelPosition(series, item);
			} else {
				position = getNegativeItemLabelPosition(series, item);
			}

			// work out the label anchor point...
			Point2D anchorPoint = calculateLabelAnchorPoint(
								position.getItemLabelAnchor(), x, y, orientation
								);
			RefineryUtilities.drawRotatedString(
								label, g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(),
								position.getTextAnchor(), position.getRotationAnchor(), position.getAngle()
								);
		}

	}

}
