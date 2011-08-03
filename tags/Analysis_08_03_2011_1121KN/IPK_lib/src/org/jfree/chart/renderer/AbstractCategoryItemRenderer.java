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
 * ---------------------------------
 * AbstractCategoryItemRenderer.java
 * ---------------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Richard Atkinson;
 * $Id: AbstractCategoryItemRenderer.java,v 1.1 2011-01-31 09:02:46 klukas Exp $
 * Changes:
 * --------
 * 29-May-2002 : Version 1 (DG);
 * 06-Jun-2002 : Added accessor methods for the tool tip generator (DG);
 * 11-Jun-2002 : Made constructors protected (DG);
 * 26-Jun-2002 : Added axis to initialise method (DG);
 * 05-Aug-2002 : Added urlGenerator member variable plus accessors (RA);
 * 22-Aug-2002 : Added categoriesPaint attribute, based on code submitted by Janet Banks.
 * This can be used when there is only one series, and you want each category
 * item to have a different color (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 29-Oct-2002 : Fixed bug where background image for plot was not being drawn (DG);
 * 05-Nov-2002 : Replaced references to CategoryDataset with TableDataset (DG);
 * 26-Nov 2002 : Replaced the isStacked() method with getRangeType() (DG);
 * 09-Jan-2003 : Renamed grid-line methods (DG);
 * 17-Jan-2003 : Moved plot classes into separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 12-May-2003 : Modified to take into account the plot orientation (DG);
 * 12-Aug-2003 : Very minor javadoc corrections (DB)
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 05-Nov-2003 : Fixed marker rendering bug (833623) (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 11-Feb-2004 : Modified labelling for markers (DG);
 * 12-Feb-2004 : Updated clone() method (DG);
 * 15-Apr-2004 : Created a new CategoryToolTipGenerator interface (DG);
 * 05-May-2004 : Fixed bug (948310) where interval markers extend outside axis range (DG);
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * An abstract base class that you can use to implement a new {@link CategoryItemRenderer}.
 * <p>
 * When you create a new {@link CategoryItemRenderer} you are not required to extend this class, but it makes the job easier.
 */
public abstract class AbstractCategoryItemRenderer extends AbstractRenderer
																	implements CategoryItemRenderer,
																					Cloneable,
																					Serializable {

	/** The plot that the renderer is assigned to. */
	private CategoryPlot plot;

	/** The label generator for ALL series. */
	private CategoryLabelGenerator labelGenerator;

	/** A list of item label generators (one per series). */
	private ObjectList labelGeneratorList;

	/** The base item label generator. */
	private CategoryLabelGenerator baseLabelGenerator;

	/** The tool tip generator for ALL series. */
	private CategoryToolTipGenerator toolTipGenerator;

	/** A list of tool tip generators (one per series). */
	private ObjectList toolTipGeneratorList;

	/** The base tool tip generator. */
	private CategoryToolTipGenerator baseToolTipGenerator;

	/** The URL generator. */
	private CategoryURLGenerator itemURLGenerator;

	/** A list of item label generators (one per series). */
	private ObjectList itemURLGeneratorList;

	/** The base item label generator. */
	private CategoryURLGenerator baseItemURLGenerator;

	/** The number of rows in the dataset (temporary record). */
	private transient int rowCount;

	/** The number of columns in the dataset (temporary record). */
	private transient int columnCount;

	/**
	 * Creates a new renderer with no tool tip generator and no URL generator.
	 * <P>
	 * The defaults (no tool tip or URL generators) have been chosen to minimise the processing required to generate a default chart. If you require tool tips or
	 * URLs, then you can easily add the required generators.
	 */
	protected AbstractCategoryItemRenderer() {
		this.labelGenerator = null;
		this.labelGeneratorList = new ObjectList();
		this.toolTipGenerator = null;
		this.toolTipGeneratorList = new ObjectList();
		this.itemURLGenerator = null;
		this.itemURLGeneratorList = new ObjectList();
	}

	/**
	 * Returns the plot that the renderer has been assigned to (where <code>null</code> indicates
	 * that the renderer is not currently assigned to a plot).
	 * 
	 * @return The plot (possibly <code>null</code>).
	 */
	public CategoryPlot getPlot() {
		return this.plot;
	}

	/**
	 * Sets the plot that the renderer has been assigned to. This method is usually called
	 * by the {@link CategoryPlot}, in normal usage you shouldn't need to call this method
	 * directly.
	 * 
	 * @param plot
	 *           the plot (<code>null</code> not permitted).
	 */
	public void setPlot(CategoryPlot plot) {
		if (plot == null) {
			throw new IllegalArgumentException("Null 'plot' argument.");
		}
		this.plot = plot;
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
	public CategoryLabelGenerator getLabelGenerator(int row, int column) {
		return getSeriesLabelGenerator(row);
	}

	/**
	 * Returns the label generator for a series.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @return the generator (possibly <code>null</code>).
	 */
	public CategoryLabelGenerator getSeriesLabelGenerator(int series) {

		// return the generator for ALL series, if there is one...
		if (this.labelGenerator != null) {
			return this.labelGenerator;
		}

		// otherwise look up the generator table
		CategoryLabelGenerator generator = (CategoryLabelGenerator) this.labelGeneratorList.get(series);
		if (generator == null) {
			generator = this.baseLabelGenerator;
		}
		return generator;

	}

	/**
	 * Sets the label generator for ALL series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setLabelGenerator(CategoryLabelGenerator generator) {
		this.labelGenerator = generator;
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
	public void setSeriesLabelGenerator(int series, CategoryLabelGenerator generator) {
		this.labelGeneratorList.set(series, generator);
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the base label generator.
	 * 
	 * @return The generator (possibly <code>null</code>).
	 */
	public CategoryLabelGenerator getBaseLabelGenerator() {
		return this.baseLabelGenerator;
	}

	/**
	 * Sets the base label generator and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setBaseLabelGenerator(CategoryLabelGenerator generator) {
		this.baseLabelGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	// TOOL TIP GENERATOR

	/**
	 * Returns the tool tip generator that should be used for the specified item. This
	 * method looks up the generator using the "three-layer" approach outlined in the
	 * general description of this interface. You can override this method if you want
	 * to return a different generator per item.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The generator (possibly <code>null</code>).
	 */
	public CategoryToolTipGenerator getToolTipGenerator(int row, int column) {

		CategoryToolTipGenerator result = null;
		if (this.toolTipGenerator != null) {
			result = this.toolTipGenerator;
		} else {
			result = getSeriesToolTipGenerator(row);
			if (result == null) {
				result = this.baseToolTipGenerator;
			}
		}
		return result;
	}

	/**
	 * Returns the tool tip generator that will be used for ALL items in the dataset (the
	 * "layer 0" generator).
	 * 
	 * @return A tool tip generator (possibly <code>null</code>).
	 */
	public CategoryToolTipGenerator getToolTipGenerator() {
		return this.toolTipGenerator;
	}

	/**
	 * Sets the tool tip generator for ALL series and sends a {@link org.jfree.chart.event.RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setToolTipGenerator(CategoryToolTipGenerator generator) {
		this.toolTipGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the tool tip generator for the specified series (a "layer 1" generator).
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The tool tip generator (possibly <code>null</code>).
	 */
	public CategoryToolTipGenerator getSeriesToolTipGenerator(int series) {
		return (CategoryToolTipGenerator) this.toolTipGeneratorList.get(series);
	}

	/**
	 * Sets the tool tip generator for a series and sends a {@link org.jfree.chart.event.RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setSeriesToolTipGenerator(int series, CategoryToolTipGenerator generator) {
		this.toolTipGeneratorList.set(series, generator);
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the base tool tip generator (the "layer 2" generator).
	 * 
	 * @return The tool tip generator (possibly <code>null</code>).
	 */
	public CategoryToolTipGenerator getBaseToolTipGenerator() {
		return this.baseToolTipGenerator;
	}

	/**
	 * Sets the base tool tip generator and sends a {@link org.jfree.chart.event.RendererChangeEvent} to all registered listeners.
	 * 
	 * @param generator
	 *           the generator (<code>null</code> permitted).
	 */
	public void setBaseToolTipGenerator(CategoryToolTipGenerator generator) {
		this.baseToolTipGenerator = generator;
		notifyListeners(new RendererChangeEvent(this));
	}

	// URL GENERATOR

	/**
	 * Returns the URL generator for a data item. This method just calls the
	 * getSeriesItemURLGenerator method, but you can override this behaviour if you want to.
	 * 
	 * @param row
	 *           the row index (zero based).
	 * @param column
	 *           the column index (zero based).
	 * @return The URL generator.
	 */
	public CategoryURLGenerator getItemURLGenerator(int row, int column) {
		return getSeriesItemURLGenerator(row);
	}

	/**
	 * Returns the URL generator for a series.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @return The URL generator for the series.
	 */
	public CategoryURLGenerator getSeriesItemURLGenerator(int series) {

		// return the generator for ALL series, if there is one...
		if (this.itemURLGenerator != null) {
			return this.itemURLGenerator;
		}

		// otherwise look up the generator table
		CategoryURLGenerator generator = (CategoryURLGenerator) this.itemURLGeneratorList.get(series);
		if (generator == null) {
			generator = this.baseItemURLGenerator;
		}
		return generator;

	}

	/**
	 * Sets the item URL generator for ALL series.
	 * 
	 * @param generator
	 *           the generator.
	 */
	public void setItemURLGenerator(CategoryURLGenerator generator) {
		this.itemURLGenerator = generator;
	}

	/**
	 * Sets the URL generator for a series.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @param generator
	 *           the generator.
	 */
	public void setSeriesItemURLGenerator(int series, CategoryURLGenerator generator) {
		this.itemURLGeneratorList.set(series, generator);
	}

	/**
	 * Returns the base item URL generator.
	 * 
	 * @return The item URL generator.
	 */
	public CategoryURLGenerator getBaseItemURLGenerator() {
		return this.baseItemURLGenerator;
	}

	/**
	 * Sets the base item URL generator.
	 * 
	 * @param generator
	 *           the item URL generator.
	 */
	public void setBaseItemURLGenerator(CategoryURLGenerator generator) {
		this.baseItemURLGenerator = generator;
	}

	/**
	 * Returns the number of rows in the dataset. This value is updated in the {@link AbstractCategoryItemRenderer#initialise} method.
	 * 
	 * @return the row count.
	 */
	public int getRowCount() {
		return this.rowCount;
	}

	/**
	 * Returns the number of columns in the dataset. This value is updated in the {@link AbstractCategoryItemRenderer#initialise} method.
	 * 
	 * @return the column count.
	 */
	public int getColumnCount() {
		return this.columnCount;
	}

	/**
	 * Initialises the renderer and returns a state object that will be used for the
	 * remainder of the drawing process for a single chart. The state object allows
	 * for the fact that the renderer may be used simultaneously by multiple threads (each
	 * thread will work with a separate state object).
	 * <P>
	 * Stores a reference to the {@link PlotRenderingInfo} object (which might be <code>null</code>), and then sets the useCategoriesPaint flag according to the
	 * special case conditions a) there is only one series and b) the categoriesPaint array is not null.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the data area.
	 * @param plot
	 *           the plot.
	 * @param rendererIndex
	 *           the renderer index.
	 * @param info
	 *           an object for returning information about the structure of the plot
	 *           (<code>null</code> permitted).
	 * @return the renderer state.
	 */
	public CategoryItemRendererState initialise(Graphics2D g2,
																Rectangle2D dataArea,
																CategoryPlot plot,
																int rendererIndex,
																PlotRenderingInfo info) {

		setPlot(plot);
		CategoryDataset data = plot.getDataset(rendererIndex);
		if (data != null) {
			this.rowCount = data.getRowCount();
			this.columnCount = data.getColumnCount();
		} else {
			this.rowCount = 0;
			this.columnCount = 0;
		}
		return new CategoryItemRendererState(info);

	}

	/**
	 * Returns the range of values the renderer requires to display all the items from the
	 * specified dataset.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 * @return The range (or <code>null</code> if the dataset is <code>null</code> or empty).
	 */
	public Range getRangeExtent(CategoryDataset dataset) {
		return DatasetUtilities.getRangeExtent(dataset);
	}

	/**
	 * Draws a background for the data area. The default implementation just gets the plot to
	 * draw the outline, but some renderers will override this behaviour.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param dataArea
	 *           the data area.
	 */
	public void drawBackground(Graphics2D g2,
											CategoryPlot plot,
											Rectangle2D dataArea) {

		plot.drawBackground(g2, dataArea);

	}

	/**
	 * Draws an outline for the data area. The default implementation just gets the plot to
	 * draw the outline, but some renderers will override this behaviour.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param dataArea
	 *           the data area.
	 */
	public void drawOutline(Graphics2D g2,
										CategoryPlot plot,
										Rectangle2D dataArea) {

		plot.drawOutline(g2, dataArea);

	}

	/**
	 * Draws a grid line against the domain axis.
	 * <P>
	 * Note that this default implementation assumes that the horizontal axis is the domain axis. If this is not the case, you will need to override this method.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param dataArea
	 *           the area for plotting data (not yet adjusted for any 3D effect).
	 * @param value
	 *           the Java2D value at which the grid line should be drawn.
	 */
	public void drawDomainGridline(Graphics2D g2,
												CategoryPlot plot,
												Rectangle2D dataArea,
												double value) {

		Line2D line = null;
		PlotOrientation orientation = plot.getOrientation();

		if (orientation == PlotOrientation.HORIZONTAL) {
			line = new Line2D.Double(dataArea.getMinX(), value, dataArea.getMaxX(), value);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				line = new Line2D.Double(value, dataArea.getMinY(), value, dataArea.getMaxY());
			}

		Paint paint = plot.getDomainGridlinePaint();
		if (paint == null) {
			paint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
		}
		g2.setPaint(paint);

		Stroke stroke = plot.getDomainGridlineStroke();
		if (stroke == null) {
			stroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
		}
		g2.setStroke(stroke);
		try {
			g2.draw(line);
		} catch (Exception err) {
			// ignore
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
	public void drawRangeGridline(Graphics2D g2,
												CategoryPlot plot,
												ValueAxis axis,
												Rectangle2D dataArea,
												double value) {

		Range range = axis.getRange();
		if (!range.contains(value)) {
			return;
		}

		PlotOrientation orientation = plot.getOrientation();
		double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
		Line2D line = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
			}

		Paint paint = plot.getRangeGridlinePaint();
		if (paint == null) {
			paint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
		}
		g2.setPaint(paint);

		Stroke stroke = plot.getRangeGridlineStroke();
		if (stroke == null) {
			stroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
		}
		g2.setStroke(stroke);

		g2.draw(line);

	}

	/**
	 * Draws a marker for the range axis.
	 * 
	 * @param g2
	 *           the graphics device (not <code>null</code>).
	 * @param plot
	 *           the plot (not <code>null</code>).
	 * @param axis
	 *           the range axis (not <code>null</code>).
	 * @param marker
	 *           the marker to be drawn (not <code>null</code>).
	 * @param dataArea
	 *           the area inside the axes (not <code>null</code>).
	 */
	public void drawRangeMarker(Graphics2D g2,
											CategoryPlot plot,
											ValueAxis axis,
											Marker marker,
											Rectangle2D dataArea) {

		if (marker instanceof ValueMarker) {
			ValueMarker vm = (ValueMarker) marker;
			double value = vm.getValue();
			Range range = axis.getRange();

			if (!range.contains(value)) {
				return;
			}

			PlotOrientation orientation = plot.getOrientation();
			double v = axis.valueToJava2D(value, dataArea, plot.getRangeAxisEdge());
			Line2D line = null;
			if (orientation == PlotOrientation.HORIZONTAL) {
				line = new Line2D.Double(v, dataArea.getMinY(), v, dataArea.getMaxY());
			} else
				if (orientation == PlotOrientation.VERTICAL) {
					line = new Line2D.Double(dataArea.getMinX(), v, dataArea.getMaxX(), v);
				}

			g2.setPaint(marker.getOutlinePaint());
			g2.setStroke(marker.getOutlineStroke());
			try {
				g2.draw(line);
			} catch (Exception e) {
				// empty
			}
			String label = marker.getLabel();
			RectangleAnchor anchor = marker.getLabelAnchor();
			if (label != null) {
				Font labelFont = marker.getLabelFont();
				g2.setFont(labelFont);
				g2.setPaint(marker.getLabelPaint());
				double[] coordinates = calculateRangeMarkerTextAnchorPoint(
									g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), anchor
									);
				// g2.drawString(label, (int) coordinates[0], (int) coordinates[1]);
				RefineryUtilities.drawAlignedString(
									label, g2, (float) coordinates[0], (float) coordinates[1], TextAnchor.CENTER
									);
			}
		} else
			if (marker instanceof IntervalMarker) {

				IntervalMarker im = (IntervalMarker) marker;
				double start = im.getStartValue();
				double end = im.getEndValue();
				Range range = axis.getRange();
				if (!(range.intersects(start, end))) {
					return;
				}

				// don't draw beyond the axis range...
				start = range.constrain(start);
				end = range.constrain(end);

				double v0 = axis.valueToJava2D(start, dataArea, plot.getRangeAxisEdge());
				double v1 = axis.valueToJava2D(end, dataArea, plot.getRangeAxisEdge());

				PlotOrientation orientation = plot.getOrientation();
				Rectangle2D rect = null;
				if (orientation == PlotOrientation.HORIZONTAL) {
					rect = new Rectangle2D.Double(
										v0, dataArea.getMinY(), v1 - v0, dataArea.getHeight()
										);
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						rect = new Rectangle2D.Double(
											dataArea.getMinX(), Math.min(v0, v1), dataArea.getWidth(), Math.abs(v1 - v0)
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
					double[] coordinates = calculateRangeMarkerTextAnchorPoint(
										g2, orientation, dataArea, rect, marker.getLabelOffset(), anchor
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
	 *           the rectangle surrounding the marker.
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
	 * Returns a legend item for a series.
	 * 
	 * @param datasetIndex
	 *           the dataset index (zero-based).
	 * @param series
	 *           the series index (zero-based).
	 * @return the legend item.
	 */
	public LegendItem getLegendItem(int datasetIndex, int series) {

		CategoryPlot cp = getPlot();
		if (cp == null) {
			return null;
		}

		CategoryDataset dataset;
		dataset = cp.getDataset(datasetIndex);
		String label = dataset.getRowKey(series).toString();
		String description = label;
		Shape shape = getSeriesShape(series);
		Paint paint = getSeriesPaint(series);
		Paint outlinePaint = getSeriesOutlinePaint(series);
		Stroke stroke = getSeriesStroke(series);

		return new LegendItem(
							label, description, shape, true, paint, stroke, outlinePaint, stroke);

	}

	/**
	 * Tests this renderer for equality with another object.
	 * 
	 * @param obj
	 *           the object.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object obj) {

		boolean result = super.equals(obj);

		if (obj instanceof AbstractCategoryItemRenderer) {
			AbstractCategoryItemRenderer r = (AbstractCategoryItemRenderer) obj;

			boolean b0 = ObjectUtils.equal(this.labelGenerator, r.labelGenerator);
			boolean b1 = ObjectUtils.equal(this.labelGeneratorList, r.labelGeneratorList);
			boolean b2 = ObjectUtils.equal(this.baseLabelGenerator, r.baseLabelGenerator);
			boolean b3 = ObjectUtils.equal(this.toolTipGenerator, r.toolTipGenerator);
			boolean b4 = ObjectUtils.equal(this.toolTipGeneratorList, r.toolTipGeneratorList);
			boolean b5 = ObjectUtils.equal(this.baseToolTipGenerator, r.baseToolTipGenerator);
			boolean b6 = ObjectUtils.equal(this.itemURLGenerator, r.itemURLGenerator);
			boolean b7 = ObjectUtils.equal(this.itemURLGeneratorList, r.itemURLGeneratorList);
			boolean b8 = ObjectUtils.equal(this.baseItemURLGenerator, r.baseItemURLGenerator);

			result = b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8;
		}

		return result;

	}

	/**
	 * Returns a hash code for the renderer.
	 * 
	 * @return The hash code.
	 */
	public int hashCode() {
		int result = super.hashCode();
		return result;
	}

	/**
	 * Returns the drawing supplier from the plot.
	 * 
	 * @return The drawing supplier (possibly <code>null</code>).
	 */
	public DrawingSupplier getDrawingSupplier() {
		DrawingSupplier result = null;
		CategoryPlot cp = getPlot();
		if (cp != null) {
			result = cp.getDrawingSupplier();
		}
		return result;
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
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 * @param x
	 *           the x coordinate (in Java2D space).
	 * @param y
	 *           the y coordinate (in Java2D space).
	 * @param negative
	 *           indicates a negative value (which affects the item label position).
	 */
	protected void drawItemLabel(Graphics2D g2,
											PlotOrientation orientation,
											CategoryDataset dataset,
											int row, int column,
											double x, double y,
											boolean negative) {

		CategoryLabelGenerator generator = getLabelGenerator(row, column);
		if (generator != null) {
			Font labelFont = getItemLabelFont(row, column);
			Paint paint = getItemLabelPaint(row, column);
			g2.setFont(labelFont);
			g2.setPaint(paint);
			String label = generator.generateLabel(dataset, row, column);

			// get the label anchor..
			ItemLabelPosition position = null;
			if (!negative) {
				position = getPositiveItemLabelPosition(row, column);
			} else {
				position = getNegativeItemLabelPosition(row, column);
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

	/**
	 * Returns an independent copy of the renderer.
	 * <p>
	 * The <code>plot</code> reference is shallow copied.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            can be thrown if one of the objects belonging to the
	 *            renderer does not support cloning (for example, an item label generator).
	 */
	public Object clone() throws CloneNotSupportedException {

		AbstractCategoryItemRenderer clone = (AbstractCategoryItemRenderer) super.clone();

		if (this.labelGenerator != null) {
			if (this.labelGenerator instanceof PublicCloneable) {
				PublicCloneable pc = (PublicCloneable) this.labelGenerator;
				clone.labelGenerator = (CategoryLabelGenerator) pc.clone();
			} else {
				throw new CloneNotSupportedException("ItemLabelGenerator not cloneable.");
			}
		}

		if (this.labelGeneratorList != null) {
			clone.labelGeneratorList = (ObjectList) this.labelGeneratorList.clone();
		}

		if (this.baseLabelGenerator != null) {
			if (this.baseLabelGenerator instanceof PublicCloneable) {
				PublicCloneable pc = (PublicCloneable) this.baseLabelGenerator;
				clone.baseLabelGenerator = (CategoryLabelGenerator) pc.clone();
			} else {
				throw new CloneNotSupportedException("ItemLabelGenerator not cloneable.");
			}
		}

		if (this.toolTipGenerator != null) {
			if (this.toolTipGenerator instanceof PublicCloneable) {
				PublicCloneable pc = (PublicCloneable) this.toolTipGenerator;
				clone.toolTipGenerator = (CategoryToolTipGenerator) pc.clone();
			} else {
				throw new CloneNotSupportedException("Tool tip generator not cloneable.");
			}
		}

		if (this.toolTipGeneratorList != null) {
			clone.toolTipGeneratorList = (ObjectList) this.toolTipGeneratorList.clone();
		}

		if (this.baseToolTipGenerator != null) {
			if (this.baseToolTipGenerator instanceof PublicCloneable) {
				PublicCloneable pc = (PublicCloneable) this.baseToolTipGenerator;
				clone.baseToolTipGenerator = (CategoryToolTipGenerator) pc.clone();
			} else {
				throw new CloneNotSupportedException("Base tool tip generator not cloneable.");
			}
		}

		if (this.itemURLGenerator != null) {
			clone.itemURLGenerator = (CategoryURLGenerator) this.itemURLGenerator.clone();
		}

		if (this.itemURLGeneratorList != null) {
			clone.itemURLGeneratorList = (ObjectList) this.itemURLGeneratorList.clone();
		}

		if (this.baseItemURLGenerator != null) {
			clone.baseItemURLGenerator = (CategoryURLGenerator) this.baseItemURLGenerator.clone();
		}

		return clone;
	}

	/**
	 * Returns a domain axis for a plot.
	 * 
	 * @param plot
	 *           the plot.
	 * @param index
	 *           the axis index.
	 * @return A domain axis.
	 */
	protected CategoryAxis getDomainAxis(CategoryPlot plot, int index) {
		CategoryAxis result = plot.getDomainAxis(index);
		if (result == null) {
			result = plot.getDomainAxis();
		}
		return result;
	}

	/**
	 * Returns a range axis for a plot.
	 * 
	 * @param plot
	 *           the plot.
	 * @param index
	 *           the axis index (<code>null</code> for the primary axis).
	 * @return A range axis.
	 */
	protected ValueAxis getRangeAxis(CategoryPlot plot, int index) {
		ValueAxis result = plot.getRangeAxis(index);
		if (result == null) {
			result = plot.getRangeAxis();
		}
		return result;
	}

}
