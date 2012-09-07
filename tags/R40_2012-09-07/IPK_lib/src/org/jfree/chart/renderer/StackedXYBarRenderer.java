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
 * ------------------
 * StackedXYBarRenderer.java
 * ------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: Andreas Schroeder;
 * Contributor(s): -;
 * $Id: StackedXYBarRenderer.java,v 1.1 2011-01-31 09:02:45 klukas Exp $
 * Changes
 * -------
 * 01-Apr-2004 : Version 1 (AS);
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.Range;
import org.jfree.data.TableXYDataset;
import org.jfree.data.XYDataset;
import org.jfree.ui.RectangleEdge;

/**
 * A bar renderer that displays the series items stacked.
 * The dataset used together with this renderer must be a {@link org.jfree.data.IntervalXYDataset} and a {@link org.jfree.data.TableXYDataset}. For example, the
 * dataset class {@link org.jfree.data.CategoryTableXYDataset} implements both interfaces.
 * 
 * @author andreas.schroeder
 */
public class StackedXYBarRenderer extends XYBarRenderer {
	/**
	 * creates a new renderer.
	 */
	public StackedXYBarRenderer() {
		super();
	}

	/**
	 * creates a new renderer.
	 * 
	 * @param margin
	 *           the percentual amount of the bars tha are cut away.
	 */
	public StackedXYBarRenderer(double margin) {
		super(margin);
	}

	/**
	 * Internal class - not really needed for this renderer.
	 * 
	 * @author andreas.schroeder
	 */
	static class StackedXYBarRendererState extends XYItemRendererState {
		/**
		 * @param info
		 *           the plot rendering info.
		 */
		public StackedXYBarRendererState(PlotRenderingInfo info) {
			super(info);
		}
	}

	/**
	 * Returns the range type.
	 * 
	 * @return the range type.
	 */
	public RangeType getRangeType() {
		return RangeType.STACKED;
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
		return DatasetUtilities.getStackedRangeExtent((TableXYDataset) dataset);
	}

	/**
	 * Initialises the renderer and returns a state object that should be passed to all subsequent
	 * calls to the drawItem() method. Here there is nothing to do.
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
	 * @return a state object.
	 */
	public XYItemRendererState initialise(
						Graphics2D g2,
						Rectangle2D dataArea,
						XYPlot plot,
						XYDataset data,
						PlotRenderingInfo info) {
		return new StackedXYBarRendererState(info);
	}

	/**
	 * Draws the visual representation of a single data item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the area within which the plot is being drawn.
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
	public void drawItem(
						Graphics2D g2,
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
		if (!(dataset instanceof IntervalXYDataset && dataset instanceof TableXYDataset)) {
			String message = "dataset (type " + dataset.getClass().getName() + ") has wrong type:";
			boolean and = false;
			if (!IntervalXYDataset.class.isAssignableFrom(dataset.getClass())) {
				message += " it is no IntervalXYDataset";
				and = true;
			}
			if (!TableXYDataset.class.isAssignableFrom(dataset.getClass())) {
				if (and) {
					message += " and";
				}
				message += " it is no TableXYDataset";
			}

			throw new IllegalArgumentException(message);
		}

		IntervalXYDataset intervalData = (IntervalXYDataset) dataset;

		Paint seriesPaint = getItemPaint(series, item);
		Paint seriesOutlinePaint = getItemOutlinePaint(series, item);

		// Get height adjustment based on stack and translate to Java2D values
		double ph = this.getPreviousHeight(dataset, series, item);

		Number valueNumber = intervalData.getYValue(series, item);
		if (valueNumber == null) {
			return;
		}

		double translatedStartY = rangeAxis.valueToJava2D(ph, dataArea, plot.getRangeAxisEdge());

		double translatedEndY = rangeAxis.valueToJava2D(
							valueNumber.doubleValue() + ph, dataArea, plot.getRangeAxisEdge()
							);

		RectangleEdge location = plot.getDomainAxisEdge();
		Number startXNumber = intervalData.getStartXValue(series, item);
		if (startXNumber == null) {
			return;
		}
		double translatedStartX =
							domainAxis.valueToJava2D(startXNumber.doubleValue(), dataArea, location);

		Number endXNumber = intervalData.getEndXValue(series, item);
		if (endXNumber == null) {
			return;
		}
		double translatedEndX =
							domainAxis.valueToJava2D(endXNumber.doubleValue(), dataArea, location);

		double translatedWidth = Math.max(1, Math.abs(translatedEndX - translatedStartX));
		double translatedHeight = Math.abs(translatedEndY - translatedStartY);
		if (getMargin() > 0.0) {
			double cut = translatedWidth * getMargin();
			translatedWidth = translatedWidth - cut;
			translatedStartX = translatedStartX + cut / 2;
		}

		Rectangle2D bar = null;
		PlotOrientation orientation = plot.getOrientation();
		if (orientation == PlotOrientation.HORIZONTAL) {
			bar = new Rectangle2D.Double(
								Math.min(translatedStartY, translatedEndY),
								translatedEndX,
								translatedHeight,
								translatedWidth
								);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				bar = new Rectangle2D.Double(
									translatedStartX,
									Math.min(translatedStartY, translatedEndY),
									translatedWidth,
									translatedHeight
									);
			}

		g2.setPaint(seriesPaint);
		g2.fill(bar);
		if (Math.abs(translatedEndX - translatedStartX) > 3) {
			g2.setStroke(getItemStroke(series, item));
			g2.setPaint(seriesOutlinePaint);
			g2.draw(bar);
		}

		// add an entity for the item...
		if (info != null) {
			EntityCollection entities = info.getOwner().getEntityCollection();
			if (entities != null) {
				String tip = null;
				XYToolTipGenerator generator = getToolTipGenerator(series, item);
				if (generator != null) {
					tip = generator.generateToolTip(dataset, series, item);
				}
				String url = null;
				if (getURLGenerator() != null) {
					url = getURLGenerator().generateURL(dataset, series, item);
				}
				XYItemEntity entity = new XYItemEntity(bar, dataset, series, item, tip, url);
				entities.addEntity(entity);
			}
		}
	}

	/**
	 * Calculates the stacked value of the all series up to, but not including <code>series</code> for the specified category, <code>category</code>. It returns
	 * 0.0 if <code>series</code> is the first series, i.e. 0.
	 * 
	 * @param data
	 *           the data.
	 * @param series
	 *           the series.
	 * @param index
	 *           the index.
	 * @return double returns a cumulative value for all series' values up to
	 *         but excluding <code>series</code> for <code>index</code>.
	 */
	protected double getPreviousHeight(XYDataset data, int series, int index) {
		double result = 0.0;

		Number tmp;
		for (int i = 0; i < series; i++) {
			tmp = data.getYValue(i, index);
			if (tmp != null) {
				result += tmp.doubleValue();
			}
		}
		return result;
	}
}
