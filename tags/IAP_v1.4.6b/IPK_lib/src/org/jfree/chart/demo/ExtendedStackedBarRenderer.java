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
 * -------------------------------
 * ExtendedStackedBarRenderer.java
 * -------------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: ExtendedStackedBarRenderer.java,v 1.1 2011-01-31 09:01:49 klukas Exp $
 * Changes
 * -------
 * 27-Nov-2003 : Version 1 (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 06-Feb-2004 : Added format attribute for totals (DG);
 */

package org.jfree.chart.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.CategoryItemRendererState;
import org.jfree.chart.renderer.StackedBarRenderer;
import org.jfree.data.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

/**
 * An extension of the {@link StackedBarRenderer} that can draw positive and negative totals at
 * the top and bottom of the stacked bars.
 */
public class ExtendedStackedBarRenderer extends StackedBarRenderer {

	/** Show positive label? */
	private boolean showPositiveTotal = true;

	/** Show negative label? */
	private boolean showNegativeTotal = true;

	/** Font for labels. */
	private Font totalLabelFont = new Font("SansSerif", Font.PLAIN, 10);

	/** Formatter for total. */
	private NumberFormat totalFormatter;

	/**
	 * Creates a new renderer.
	 */
	public ExtendedStackedBarRenderer() {
		super();
		this.totalFormatter = NumberFormat.getInstance();
	}

	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available *
	// * to purchase from Object Refinery Limited: *
	// * *
	// * http://www.object-refinery.com/jfreechart/guide.html *
	// * *
	// * Sales are used to provide funding for the JFreeChart project - please *
	// * support us so that we can continue developing free software. *
	// ****************************************************************************

	/**
	 * Returns the total formatter.
	 * 
	 * @return the total formatter (never <code>null</code>).
	 */
	public NumberFormat getTotalFormatter() {
		return this.totalFormatter;
	}

	/**
	 * Sets the total formatter.
	 * 
	 * @param format
	 *           the formatter (<code>null</code> not permitted).
	 */
	public void setTotalFormatter(final NumberFormat format) {
		if (format == null) {
			throw new IllegalArgumentException("Null format not permitted.");
		}
		this.totalFormatter = format;
	}

	/**
	 * Draws a stacked bar for a specific item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the plot area.
	 * @param plot
	 *           the plot.
	 * @param domainAxis
	 *           the domain (category) axis.
	 * @param rangeAxis
	 *           the range (value) axis.
	 * @param dataset
	 *           the data.
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 */
	public void drawItem(final Graphics2D g2,
									final CategoryItemRendererState state,
									final Rectangle2D dataArea,
									final CategoryPlot plot,
									final CategoryAxis domainAxis,
									final ValueAxis rangeAxis,
									final CategoryDataset dataset,
									final int row,
									final int column) {

		// nothing is drawn for null values...
		final Number dataValue = dataset.getValue(row, column);
		if (dataValue == null) {
			return;
		}

		final double value = dataValue.doubleValue();

		final PlotOrientation orientation = plot.getOrientation();
		final double barW0 = domainAxis.getCategoryMiddle(
							column, getColumnCount(), dataArea, plot.getDomainAxisEdge()
							) - state.getBarWidth() / 2.0;

		double positiveBase = 0.0;
		double negativeBase = 0.0;

		for (int i = 0; i < row; i++) {
			final Number v = dataset.getValue(i, column);
			if (v != null) {
				final double d = v.doubleValue();
				if (d > 0) {
					positiveBase = positiveBase + d;
				} else {
					negativeBase = negativeBase + d;
				}
			}
		}

		final double translatedBase;
		final double translatedValue;
		final RectangleEdge location = plot.getRangeAxisEdge();
		if (value > 0.0) {
			translatedBase = rangeAxis.valueToJava2D(positiveBase, dataArea, location);
			translatedValue = rangeAxis.valueToJava2D(positiveBase + value, dataArea, location);
		} else {
			translatedBase = rangeAxis.valueToJava2D(negativeBase, dataArea, location);
			translatedValue = rangeAxis.valueToJava2D(negativeBase + value, dataArea, location);
		}
		final double barL0 = Math.min(translatedBase, translatedValue);
		final double barLength = Math.max(Math.abs(translatedValue - translatedBase),
												getMinimumBarLength());

		Rectangle2D bar = null;
		if (orientation == PlotOrientation.HORIZONTAL) {
			bar = new Rectangle2D.Double(barL0, barW0, barLength, state.getBarWidth());
		} else {
			bar = new Rectangle2D.Double(barW0, barL0, state.getBarWidth(), barLength);
		}
		final Paint seriesPaint = getItemPaint(row, column);
		g2.setPaint(seriesPaint);
		g2.fill(bar);
		if (isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
			g2.setStroke(getItemStroke(row, column));
			g2.setPaint(getItemOutlinePaint(row, column));
			g2.draw(bar);
		}

		final CategoryLabelGenerator generator = getLabelGenerator(row, column);
		if (generator != null && isItemLabelVisible(row, column)) {
			drawItemLabel(g2, dataset, row, column, plot, generator, bar, (value < 0.0));
		}

		if (value > 0.0) {
			if (this.showPositiveTotal) {
				if (isLastPositiveItem(dataset, row, column)) {
					g2.setPaint(Color.black);
					g2.setFont(this.totalLabelFont);
					final double total = calculateSumOfPositiveValuesForCategory(dataset, column);
					RefineryUtilities.drawRotatedString(
										this.totalFormatter.format(total), g2,
										(float) bar.getCenterX(),
										(float) (bar.getMinY() - 4.0),
										TextAnchor.BOTTOM_CENTER,
										TextAnchor.BOTTOM_CENTER,
										0.0
										);
				}
			}
		} else {
			if (this.showNegativeTotal) {
				if (isLastNegativeItem(dataset, row, column)) {
					g2.setPaint(Color.black);
					g2.setFont(this.totalLabelFont);
					final double total = calculateSumOfNegativeValuesForCategory(dataset, column);
					RefineryUtilities.drawRotatedString(
										String.valueOf(total), g2,
										(float) bar.getCenterX(),
										(float) (bar.getMaxY() + 4.0),
										TextAnchor.TOP_CENTER,
										TextAnchor.TOP_CENTER,
										0.0
										);
				}
			}
		}

		// collect entity and tool tip information...
		if (state.getInfo() != null) {
			final EntityCollection entities = state.getInfo().getOwner().getEntityCollection();
			if (entities != null) {
				String tip = null;
				final CategoryToolTipGenerator tipster = getToolTipGenerator(row, column);
				if (tipster != null) {
					tip = tipster.generateToolTip(dataset, row, column);
				}
				String url = null;
				if (getItemURLGenerator(row, column) != null) {
					url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
				}
				final CategoryItemEntity entity = new CategoryItemEntity(
									bar, tip, url, dataset, row, dataset.getColumnKey(column), column
									);
				entities.addEntity(entity);
			}
		}

	}

	/**
	 * Returns true if the specified item is the last positive value for that category.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @param row
	 *           the row (series).
	 * @param column
	 *           the column (category).
	 * @return a boolean.
	 */
	private boolean isLastPositiveItem(final CategoryDataset dataset,
													final int row,
													final int column) {
		boolean result = true;
		Number dataValue = dataset.getValue(row, column);
		if (dataValue == null) {
			return false; // value is null
		}
		for (int r = row + 1; r < dataset.getRowCount(); r++) {
			dataValue = dataset.getValue(r, column);
			if (dataValue != null) {
				result = result && (dataValue.doubleValue() <= 0.0);
			}
		}
		return result;
	}

	/**
	 * Returns true if the specified item is the last negative value for that category.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @param row
	 *           the row (series).
	 * @param column
	 *           the column (category).
	 * @return a boolean.
	 */
	private boolean isLastNegativeItem(final CategoryDataset dataset,
													final int row,
													final int column) {
		boolean result = true;
		Number dataValue = dataset.getValue(row, column);
		if (dataValue == null) {
			return false; // value is null
		}
		for (int r = row + 1; r < dataset.getRowCount(); r++) {
			dataValue = dataset.getValue(r, column);
			if (dataValue != null) {
				result = result && (dataValue.doubleValue() >= 0.0);
			}
		}
		return result;
	}

	/**
	 * Calculates the sum of the positive values within a category.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @param column
	 *           the column (category).
	 * @return the sum of the positive values.
	 */
	private double calculateSumOfPositiveValuesForCategory(final CategoryDataset dataset,
																				final int column) {
		double result = 0.0;
		for (int r = 0; r < dataset.getRowCount(); r++) {
			final Number dataValue = dataset.getValue(r, column);
			if (dataValue != null) {
				final double v = dataValue.doubleValue();
				if (v > 0.0) {
					result = result + v;
				}
			}
		}
		return result;
	}

	/**
	 * Calculates the sum of the negative values within a category.
	 * 
	 * @param dataset
	 *           the dataset.
	 * @param column
	 *           the column (category).
	 * @return the sum of the negative values.
	 */
	private double calculateSumOfNegativeValuesForCategory(final CategoryDataset dataset,
																				final int column) {
		double result = 0.0;
		for (int r = 0; r < dataset.getRowCount(); r++) {
			final Number dataValue = dataset.getValue(r, column);
			if (dataValue != null) {
				final double v = dataValue.doubleValue();
				if (v < 0.0) {
					result = result + v;
				}
			}
		}
		return result;
	}

}
