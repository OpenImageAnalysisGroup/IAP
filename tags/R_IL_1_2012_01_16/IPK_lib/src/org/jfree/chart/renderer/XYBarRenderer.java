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
 * XYBarRenderer.java
 * ------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Richard Atkinson;
 * Christian W. Zuckschwerdt;
 * Bill Kelemen;
 * $Id: XYBarRenderer.java,v 1.1 2011-01-31 09:02:48 klukas Exp $
 * Changes
 * -------
 * 13-Dec-2001 : Version 1, makes VerticalXYBarPlot class redundant (DG);
 * 23-Jan-2002 : Added DrawInfo parameter to drawItem(...) method (DG);
 * 09-Apr-2002 : Removed the translated zero from the drawItem method. Override the initialise()
 * method to calculate it (DG);
 * 24-May-2002 : Incorporated tooltips into chart entities (DG);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 05-Aug-2002 : Small modification to drawItem method to support URLs for HTML image maps (RA);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem(...) method signature (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 24-Aug-2003 : Added null checks in drawItem (BK);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Added renderer state (DG);
 * 05-Dec-2003 : Changed call to obtain outline paint (DG);
 * 10-Feb-2004 : Added state class, updated drawItem() method to make cut-and-paste overriding
 * easier, and replaced property change with RendererChangeEvent (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 26-Apr-2004 : Added gradient paint transformer (DG);
 * 19-May-2004 : Fixed bug (879709) with bar zero value for secondary axis (DG);
 */

package org.jfree.chart.renderer;

import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.IntervalXYDataset;
import org.jfree.data.XYDataset;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.PublicCloneable;

/**
 * A renderer that draws bars on an {@link XYPlot} (requires an {@link IntervalXYDataset}).
 * <P>
 * This renderer does not include any code for calculating the crosshair point.
 */
public class XYBarRenderer extends AbstractXYItemRenderer
									implements XYItemRenderer,
													Cloneable,
													PublicCloneable,
													Serializable {

	/**
	 * The state class used by this renderer.
	 */
	class XYBarRendererState extends XYItemRendererState {

		/** Zero on the range axis in Java 2D space. */
		private double g2Zero;

		/**
		 * Creates a new state object.
		 * 
		 * @param info
		 *           the plot rendering info.
		 */
		public XYBarRendererState(PlotRenderingInfo info) {
			super(info);
		}

		/**
		 * Returns the range axis zero in Java 2D space.
		 * 
		 * @return The range axis zero.
		 */
		public double getG2Zero() {
			return this.g2Zero;
		}

		/**
		 * Sets the range axis zero in Java2D space.
		 * 
		 * @param value
		 *           the value.
		 */
		public void setG2Zero(double value) {
			this.g2Zero = value;
		}
	}

	/** Percentage margin (to reduce the width of bars). */
	private double margin;

	/** An optional class used to transform gradient paint objects to fit each bar. */
	private GradientPaintTransformer gradientPaintTransformer;

	/**
	 * The default constructor.
	 */
	public XYBarRenderer() {
		this(0.0);
	}

	/**
	 * Constructs a new renderer.
	 * 
	 * @param margin
	 *           the percentage amount to trim from the width of each bar.
	 */
	public XYBarRenderer(double margin) {
		super();
		this.margin = margin;
		this.gradientPaintTransformer = new StandardGradientPaintTransformer();
	}

	/**
	 * Returns the margin which is a percentage amount by which the bars are trimmed.
	 * 
	 * @return The margin.
	 */
	public double getMargin() {
		return this.margin;
	}

	/**
	 * Sets the percentage amount by which the bars are trimmed and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param margin
	 *           the new margin.
	 */
	public void setMargin(double margin) {
		this.margin = margin;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Returns the gradient paint transformer (an object used to transform gradient paint objects
	 * to fit each bar.
	 * 
	 * @return A transformer (<code>null</code> possible).
	 */
	public GradientPaintTransformer getGradientPaintTransformer() {
		return this.gradientPaintTransformer;
	}

	/**
	 * Sets the gradient paint transformer and sends a {@link RendererChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param transformer
	 *           the transformer (<code>null</code> permitted).
	 */
	public void setGradientPaintTransformer(GradientPaintTransformer transformer) {
		this.gradientPaintTransformer = transformer;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Initialises the renderer and returns a state object that should be passed to all subsequent
	 * calls to the drawItem() method. Here we calculate the Java2D y-coordinate for zero, since
	 * all the bars have their bases fixed at zero.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the area inside the axes.
	 * @param plot
	 *           the plot.
	 * @param dataset
	 *           the data.
	 * @param info
	 *           an optional info collection object to return data back to the caller.
	 * @return A state object.
	 */
	public XYItemRendererState initialise(Graphics2D g2,
														Rectangle2D dataArea,
														XYPlot plot,
														XYDataset dataset,
														PlotRenderingInfo info) {

		XYBarRendererState state = new XYBarRendererState(info);
		ValueAxis rangeAxis = plot.getRangeAxisForDataset(plot.indexOf(dataset));
		state.setG2Zero(rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge()));
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

		IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

		Paint seriesOutlinePaint = getItemOutlinePaint(series, item);

		Number valueNumber = intervalDataset.getYValue(series, item);
		if (valueNumber == null) {
			return;
		}

		double translatedValue = rangeAxis.valueToJava2D(
							valueNumber.doubleValue(), dataArea, plot.getRangeAxisEdge()
							);

		RectangleEdge location = plot.getDomainAxisEdge();
		Number startXNumber = intervalDataset.getStartXValue(series, item);
		if (startXNumber == null) {
			return;
		}
		double translatedStartX = domainAxis.valueToJava2D(
							startXNumber.doubleValue(), dataArea, location
							);

		Number endXNumber = intervalDataset.getEndXValue(series, item);
		if (endXNumber == null) {
			return;
		}
		double translatedEndX = domainAxis.valueToJava2D(
							endXNumber.doubleValue(), dataArea, location
							);

		XYBarRendererState s = (XYBarRendererState) state;
		double g2Zero = s.getG2Zero();
		double translatedWidth = Math.max(1, Math.abs(translatedEndX - translatedStartX));
		double translatedHeight = Math.abs(translatedValue - g2Zero);

		if (getMargin() > 0.0) {
			double cut = translatedWidth * getMargin();
			translatedWidth = translatedWidth - cut;
			translatedStartX = translatedStartX + cut / 2;
		}

		Rectangle2D bar = null;
		PlotOrientation orientation = plot.getOrientation();
		if (orientation == PlotOrientation.HORIZONTAL) {
			bar = new Rectangle2D.Double(
								Math.min(g2Zero, translatedValue), translatedEndX,
								translatedHeight, translatedWidth
								);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				bar = new Rectangle2D.Double(
									translatedStartX, Math.min(g2Zero, translatedValue),
									translatedWidth, translatedHeight
									);
			}

		Paint itemPaint = getItemPaint(series, item);
		if (getGradientPaintTransformer() != null && itemPaint instanceof GradientPaint) {
			GradientPaint gp = (GradientPaint) itemPaint;
			itemPaint = getGradientPaintTransformer().transform(gp, bar);
		}
		g2.setPaint(itemPaint);
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
