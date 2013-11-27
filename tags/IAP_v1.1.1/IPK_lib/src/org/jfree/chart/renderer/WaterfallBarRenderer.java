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
 * -------------------------
 * WaterfallBarRenderer.java
 * -------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: Darshan Shah;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: WaterfallBarRenderer.java,v 1.1 2011-01-31 09:02:45 klukas Exp $
 * Changes
 * -------
 * 20-Oct-2003 : Version 1, contributed by Darshan Shah (DG);
 * 06-Nov-2003 : Changed order of parameters in constructor, and added support for
 * GradientPaint (DG);
 * 10-Feb-2004 : Updated drawItem() method to make cut-and-paste overriding easier. Also
 * fixed a bug that meant the minimum bar length was being ignored (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.PaintUtils;
import org.jfree.util.PublicCloneable;

/**
 * A renderer that handles the drawing of waterfall bar charts, for use with the {@link CategoryPlot} class.
 */
public class WaterfallBarRenderer extends BarRenderer
												implements Cloneable, PublicCloneable, Serializable {

	/** The paint used to draw the first bar. */
	private transient Paint firstBarPaint;

	/** The paint used to draw the last bar. */
	private transient Paint lastBarPaint;

	/** The paint used to draw bars having positive values. */
	private transient Paint positiveBarPaint;

	/** The paint used to draw bars having negative values. */
	private transient Paint negativeBarPaint;

	/**
	 * Constructs a new renderer with default values for the bar colors.
	 */
	public WaterfallBarRenderer() {
		this(
							new GradientPaint(
												0.0f, 0.0f, new Color(0x22, 0x22, 0xFF), 0.0f, 0.0f, new Color(0x66, 0x66, 0xFF)
							),
							new GradientPaint(
												0.0f, 0.0f, new Color(0x22, 0xFF, 0x22), 0.0f, 0.0f, new Color(0x66, 0xFF, 0x66)
							),
							new GradientPaint(
												0.0f, 0.0f, new Color(0xFF, 0x22, 0x22), 0.0f, 0.0f, new Color(0xFF, 0x66, 0x66)
							),
							new GradientPaint(
												0.0f, 0.0f, new Color(0xFF, 0xFF, 0x22), 0.0f, 0.0f, new Color(0xFF, 0xFF, 0x66)
							));
	}

	/**
	 * Constructs a new waterfall renderer.
	 * 
	 * @param firstBarPaint
	 *           the color of the first bar.
	 * @param positiveBarPaint
	 *           the color for bars with positive values.
	 * @param negativeBarPaint
	 *           the color for bars with negative values.
	 * @param lastBarPaint
	 *           the color of the last bar.
	 */
	public WaterfallBarRenderer(Paint firstBarPaint,
											Paint positiveBarPaint,
											Paint negativeBarPaint,
											Paint lastBarPaint) {
		super();
		this.firstBarPaint = firstBarPaint;
		this.lastBarPaint = lastBarPaint;
		this.positiveBarPaint = positiveBarPaint;
		this.negativeBarPaint = negativeBarPaint;
		setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.CENTER_VERTICAL));
		setMinimumBarLength(1.0);
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
		return DatasetUtilities.getCumulativeRangeExtent(dataset);
	}

	/**
	 * Returns the paint used to draw the first bar.
	 * 
	 * @return The paint.
	 */
	public Paint getFirstBarPaint() {
		return this.firstBarPaint;
	}

	/**
	 * Sets the paint that will be used to draw the first bar.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setFirstBarPaint(Paint paint) {
		this.firstBarPaint = paint;
	}

	/**
	 * Returns the paint used to draw the last bar.
	 * 
	 * @return The paint.
	 */
	public Paint getLastBarPaint() {
		return this.lastBarPaint;
	}

	/**
	 * Sets the paint that will be used to draw the last bar.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setLastBarPaint(Paint paint) {
		this.lastBarPaint = paint;
	}

	/**
	 * Returns the paint used to draw bars with positive values.
	 * 
	 * @return The paint.
	 */
	public Paint getPositiveBarPaint() {
		return this.positiveBarPaint;
	}

	/**
	 * Sets the paint that will be used to draw bars having positive values.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setPositiveBarPaint(Paint paint) {
		this.positiveBarPaint = paint;
	}

	/**
	 * Returns the paint used to draw bars with negative values.
	 * 
	 * @return The paint.
	 */
	public Paint getNegativeBarPaint() {
		return this.negativeBarPaint;
	}

	/**
	 * Sets the paint that will be used to draw bars having negative values.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setNegativeBarPaint(Paint paint) {
		this.negativeBarPaint = paint;
	}

	/**
	 * Draws the bar for a single (series, category) data item.
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
	 *           the dataset.
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
									CategoryDataset dataset,
									int row,
									int column) {

		double previous = state.getSeriesRunningTotal();
		if (column == dataset.getColumnCount() - 1) {
			previous = 0.0;
		}
		double current = 0.0;
		Number n = dataset.getValue(row, column);
		if (n != null) {
			current = previous + n.doubleValue();
		}
		state.setSeriesRunningTotal(current);

		int seriesCount = getRowCount();
		int categoryCount = getColumnCount();
		PlotOrientation orientation = plot.getOrientation();

		double rectX = 0.0;
		double rectY = 0.0;

		RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();

		// Y0
		double j2dy0 = rangeAxis.valueToJava2D(previous, dataArea, rangeAxisLocation);

		// Y1
		double j2dy1 = rangeAxis.valueToJava2D(current, dataArea, rangeAxisLocation);

		double valDiff = current - previous;
		if (j2dy1 < j2dy0) {
			double temp = j2dy1;
			j2dy1 = j2dy0;
			j2dy0 = temp;
		}

		// BAR WIDTH
		double rectWidth = state.getBarWidth();

		// BAR HEIGHT
		double rectHeight = Math.max(getMinimumBarLength(), Math.abs(j2dy1 - j2dy0));

		if (orientation == PlotOrientation.HORIZONTAL) {
			// BAR Y
			rectY = domainAxis.getCategoryStart(column, getColumnCount(), dataArea,
																domainAxisLocation);
			if (seriesCount > 1) {
				double seriesGap = dataArea.getHeight() * getItemMargin()
												/ (categoryCount * (seriesCount - 1));
				rectY = rectY + row * (state.getBarWidth() + seriesGap);
			} else {
				rectY = rectY + row * state.getBarWidth();
			}

			rectX = j2dy0;
			rectHeight = state.getBarWidth();
			rectWidth = Math.max(getMinimumBarLength(), Math.abs(j2dy1 - j2dy0));

		} else
			if (orientation == PlotOrientation.VERTICAL) {
				// BAR X
				rectX = domainAxis.getCategoryStart(column, getColumnCount(), dataArea,
																domainAxisLocation);

				if (seriesCount > 1) {
					double seriesGap = dataArea.getWidth() * getItemMargin()
												/ (categoryCount * (seriesCount - 1));
					rectX = rectX + row * (state.getBarWidth() + seriesGap);
				} else {
					rectX = rectX + row * state.getBarWidth();
				}

				rectY = j2dy0;
			}
		Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
		Paint seriesPaint = getFirstBarPaint();
		if (column == 0) {
			seriesPaint = getFirstBarPaint();
		} else
			if (column == categoryCount - 1) {
				seriesPaint = getLastBarPaint();
			} else {
				if (valDiff < 0.0) {
					seriesPaint = getNegativeBarPaint();
				} else
					if (valDiff > 0.0) {
						seriesPaint = getPositiveBarPaint();
					} else {
						seriesPaint = getLastBarPaint();
					}
			}
		if (getGradientPaintTransformer() != null && seriesPaint instanceof GradientPaint) {
			GradientPaint gp = (GradientPaint) seriesPaint;
			seriesPaint = getGradientPaintTransformer().transform(gp, bar);
		}
		g2.setPaint(seriesPaint);
		g2.fill(bar);

		// draw the outline...
		if (isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
			Stroke stroke = getItemOutlineStroke(row, column);
			Paint paint = getItemOutlinePaint(row, column);
			if (stroke != null && paint != null) {
				g2.setStroke(stroke);
				g2.setPaint(paint);
				g2.draw(bar);
			}
		}

		CategoryLabelGenerator generator = getLabelGenerator(row, column);
		if (generator != null && isItemLabelVisible(row, column)) {
			drawItemLabel(g2, dataset, row, column, plot, generator, bar, (valDiff < 0.0));
		}

		// collect entity and tool tip information...
		if (state.getInfo() != null) {
			EntityCollection entities = state.getInfo().getOwner().getEntityCollection();
			if (entities != null) {
				String tip = null;
				CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
				if (tipster != null) {
					tip = tipster.generateToolTip(dataset, row, column);
				}
				String url = null;
				if (getItemURLGenerator(row, column) != null) {
					url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
				}
				CategoryItemEntity entity = new CategoryItemEntity(
									bar, tip, url, dataset, row, dataset.getColumnKey(column), column
									);
				entities.addEntity(entity);
			}
		}

	}

	/**
	 * Tests an object for equality with this instance.
	 * 
	 * @param object
	 *           the object.
	 * @return A boolean.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (super.equals(object) && (object instanceof WaterfallBarRenderer)) {

			WaterfallBarRenderer r = (WaterfallBarRenderer) object;
			boolean b0 = PaintUtils.equal(this.firstBarPaint, r.firstBarPaint);
			boolean b1 = PaintUtils.equal(this.lastBarPaint, r.lastBarPaint);
			boolean b2 = PaintUtils.equal(this.positiveBarPaint, r.positiveBarPaint);
			boolean b3 = PaintUtils.equal(this.negativeBarPaint, r.negativeBarPaint);
			return b0 && b1 && b2 && b3;
		}

		return false;

	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the output stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {

		stream.defaultWriteObject();
		SerialUtilities.writePaint(this.firstBarPaint, stream);
		SerialUtilities.writePaint(this.lastBarPaint, stream);
		SerialUtilities.writePaint(this.positiveBarPaint, stream);
		SerialUtilities.writePaint(this.negativeBarPaint, stream);

	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the input stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 * @throws ClassNotFoundException
	 *            if there is a classpath problem.
	 */
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {

		stream.defaultReadObject();
		this.firstBarPaint = SerialUtilities.readPaint(stream);
		this.lastBarPaint = SerialUtilities.readPaint(stream);
		this.positiveBarPaint = SerialUtilities.readPaint(stream);
		this.negativeBarPaint = SerialUtilities.readPaint(stream);

	}

}
