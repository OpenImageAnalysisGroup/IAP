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
 * ----------------
 * MeterLegend.java
 * ----------------
 * (C) Copyright 2000-2004, by Hari and Contributors.
 * Original Author: Hari (ourhari@hotmail.com);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: MeterLegend.java,v 1.1 2011-01-31 09:03:11 klukas Exp $
 * Changes
 * -------
 * 01-Apr-2002 : Version 1, contributed by Hari (DG);
 * 25-Jun-2002 : Updated imports and Javadoc comments (DG);
 * 18-Sep-2002 : Updated for changes to StandardLegend (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 14-Jan-2003 : Changed outer gap to a Spacer (DG);
 * 11-Feb-2003 : Removed constructor in line with changes to StandardLegend class (DG);
 * 07-Apr-2004 : Changed text bounds calculation (DG);
 */

package org.jfree.chart;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.LineMetrics;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.event.LegendChangeEvent;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.ValueDataset;

/**
 * A legend for meter plots.
 */
public class MeterLegend extends StandardLegend {

	/** The legend text. */
	private String legendText;

	/** Show the normal range? */
	private boolean showNormal = true;

	/** Show the warning range? */
	private boolean showWarning = true;

	/** Show the critical range? */
	private boolean showCritical = true;

	/**
	 * The default constructor; sets an empty legend text.
	 */
	public MeterLegend() {
		this("");
	}

	/**
	 * Constructs a new legend.
	 * 
	 * @param legendText
	 *           the legend text.
	 */
	public MeterLegend(String legendText) {
		this.legendText = legendText;
	}

	/**
	 * Creates a new legend.
	 * 
	 * @param chart
	 *           the chart that the legend belongs to.
	 * @param legendText
	 *           the legend text.
	 * @deprecated use the default constructor instead and let JFreeChart manage
	 *             the chart reference
	 */
	public MeterLegend(JFreeChart chart, String legendText) {
		super(chart);
		this.legendText = legendText;
	}

	/**
	 * Returns the legend text.
	 * 
	 * @return the legend text.
	 */
	public String getLegendText() {
		return this.legendText;
	}

	/**
	 * Sets the legend text.
	 * 
	 * @param text
	 *           the new legend text.
	 */
	public void setLegendText(String text) {
		this.legendText = text;
		notifyListeners(new LegendChangeEvent(this));
	}

	/**
	 * Draws the legend.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param available
	 *           the available area.
	 * @return the remaining available drawing area.
	 */
	public Rectangle2D draw(Graphics2D g2, Rectangle2D available) {

		return draw(g2, available, (getAnchor() & HORIZONTAL) != 0, (getAnchor() & INVERTED) != 0);

	}

	/**
	 * Updates the legend information.
	 * 
	 * @param plot
	 *           the plot.
	 * @param data
	 *           the dataset.
	 * @param type
	 *           the type.
	 * @param index
	 *           the index.
	 * @param legendItems
	 *           the legend items.
	 * @param legendItemColors
	 *           the colors.
	 * @return boolean.
	 */
	private boolean updateInformation(MeterPlot plot, ValueDataset data, int type, int index,
													LegendItem[] legendItems, Paint[] legendItemColors) {

		boolean ret = false;
		String label = null;
		// double minValue = 0.0;
		// double maxValue = 0.0;
		Paint paint = null;

		switch (type) {
			case MeterPlot.NORMAL_DATA_RANGE:
				// minValue = plot.getNormalRange().getLowerBound();
				// maxValue = plot.getNormalRange().getUpperBound();
				paint = plot.getNormalPaint();
				label = MeterPlot.NORMAL_TEXT;
				break;
			case MeterPlot.WARNING_DATA_RANGE:
				// minValue = plot.getWarningRange().getLowerBound();
				// maxValue = plot.getWarningRange().getUpperBound();
				paint = plot.getWarningPaint();
				label = MeterPlot.WARNING_TEXT;
				break;
			case MeterPlot.CRITICAL_DATA_RANGE:
				// minValue = plot.getCriticalRange().getLowerBound();
				// maxValue = plot.getCriticalRange().getUpperBound();
				paint = plot.getCriticalPaint();
				label = MeterPlot.CRITICAL_TEXT;
				break;
			case MeterPlot.FULL_DATA_RANGE:
				// minValue = plot.getRange().getLowerBound();
				// maxValue = plot.getRange().getUpperBound();
				paint = MeterPlot.DEFAULT_BACKGROUND_PAINT;
				label = "Meter Graph";
				break;
			default:
				return false;
		}

		// if (minValue != null && maxValue != null) {
		// if (data.getBorderType() == type) {
		label += "  Range: ";
		// + data.getMinimumValue().toString() + " to "
		// + minValue.toString()
		// + "  and  "
		// + maxValue.toString() + " to "
		// + data.getMaximumValue().toString();
		// }
		// else {
		// label += "  Range: " + minValue.toString() + " to " + maxValue.toString();
		// }
		legendItems[index] = new LegendItem(label, label, null, true, null, null, null, null);
		legendItemColors[index] = paint;
		ret = true;
		// }
		return ret;
	}

	/**
	 * Draws the legend.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param available
	 *           the available drawing area.
	 * @param horizontal
	 *           if <code>true</code> draw a horizontal legend.
	 * @param inverted
	 *           ???
	 * @return the remaining available drawing area.
	 */
	protected Rectangle2D draw(Graphics2D g2, Rectangle2D available,
											boolean horizontal, boolean inverted) {

		int legendCount = 0;
		Plot plot = getChart().getPlot();
		if (!(plot instanceof MeterPlot)) {
			throw new IllegalArgumentException("Plot must be MeterPlot");
		}
		MeterPlot meterPlot = (MeterPlot) plot;
		ValueDataset data = meterPlot.getDataset();

		legendCount = 1; // Name of the Chart.
		legendCount++; // Display Full Range
		if (this.showCritical) {
			legendCount++;
		}
		if (this.showWarning) {
			legendCount++;
		}
		if (this.showNormal) {
			legendCount++;
		}

		LegendItem[] legendItems = new LegendItem[legendCount];
		Color[] legendItemColors = new Color[legendCount];

		int currentItem = 0;
		String label = this.legendText
							+ (data.getValue() != null ? ("   Current Value: " + data.getValue().toString()) : "");
		legendItems[currentItem] = new LegendItem(label, label, null, true, null, null, null, null);
		legendItemColors[currentItem] = null; // no color
		currentItem++;
		if (updateInformation(meterPlot, data, MeterPlot.FULL_DATA_RANGE,
							currentItem, legendItems, legendItemColors)) {
			currentItem++;
		}
		if (this.showCritical && updateInformation(meterPlot, data,
							MeterPlot.CRITICAL_DATA_RANGE, currentItem, legendItems, legendItemColors)) {
			currentItem++;
		}
		if (this.showWarning && updateInformation(meterPlot, data,
							MeterPlot.WARNING_DATA_RANGE, currentItem, legendItems, legendItemColors)) {
			currentItem++;
		}
		if (this.showNormal && updateInformation(meterPlot, data,
							MeterPlot.NORMAL_DATA_RANGE, currentItem, legendItems, legendItemColors)) {
			currentItem++;
		}

		if (legendItems != null) {

			Rectangle2D legendArea = new Rectangle2D.Double();
			double availableWidth = available.getWidth();
			double availableHeight = available.getHeight();

			// the translation point for the origin of the drawing system
			Point2D translation = new Point2D.Double();

			// Create buffer for individual rectangles within the legend
			DrawableLegendItem[] items = new DrawableLegendItem[legendItems.length];
			g2.setFont(getItemFont());

			// Compute individual rectangles in the legend, translation point
			// as well as the bounding box for the legend.
			if (horizontal) {
				double xstart = available.getX() + getOuterGap().getLeftSpace(availableWidth);
				double xlimit = available.getMaxX()
											+ getOuterGap().getRightSpace(availableWidth) - 1;
				double maxRowWidth = 0;
				double xoffset = 0;
				double rowHeight = 0;
				double totalHeight = 0;
				boolean startingNewRow = true;

				for (int i = 0; i < legendItems.length; i++) {
					items[i] = createLegendItem(g2, legendItems[i], xoffset, totalHeight);
					if ((!startingNewRow)
										&& (items[i].getX() + items[i].getWidth() + xstart > xlimit)) {
						maxRowWidth = Math.max(maxRowWidth, xoffset);
						xoffset = 0;
						totalHeight += rowHeight;
						i--;
						startingNewRow = true;
					} else {
						rowHeight = Math.max(rowHeight, items[i].getHeight());
						xoffset += items[i].getWidth();
						startingNewRow = false;
					}
				}

				maxRowWidth = Math.max(maxRowWidth, xoffset);
				totalHeight += rowHeight;

				// Create the bounding box
				legendArea = new Rectangle2D.Double(0, 0, maxRowWidth, totalHeight);

				// The yloc point is the variable part of the translation point
				// for horizontal legends. xloc is constant.
				double yloc = (inverted)
									? available.getMaxY() - totalHeight
														- getOuterGap().getBottomSpace(availableHeight)
									: available.getY() + getOuterGap().getTopSpace(availableHeight);
				double xloc = available.getX() + available.getWidth() / 2 - maxRowWidth / 2;

				// Create the translation point
				translation = new Point2D.Double(xloc, yloc);
			} else { // vertical...
				double totalHeight = 0;
				double maxWidth = 0;
				g2.setFont(getItemFont());
				for (int i = 0; i < items.length; i++) {
					items[i] = createLegendItem(g2, legendItems[i], 0, totalHeight);
					totalHeight += items[i].getHeight();
					maxWidth = Math.max(maxWidth, items[i].getWidth());
				}

				// Create the bounding box
				legendArea = new Rectangle2D.Float(0, 0, (float) maxWidth, (float) totalHeight);

				// The xloc point is the variable part of the translation point
				// for vertical legends. yloc is constant.
				double xloc = (inverted)
									? available.getMaxX() - maxWidth - getOuterGap().getRightSpace(availableWidth)
									: available.getX() + getOuterGap().getLeftSpace(availableWidth);
				double yloc = available.getY() + (available.getHeight() / 2) - (totalHeight / 2);

				// Create the translation point
				translation = new Point2D.Double(xloc, yloc);
			}

			// Move the origin of the drawing to the appropriate location
			g2.translate(translation.getX(), translation.getY());

			// Draw the legend's bounding box
			g2.setPaint(getBackgroundPaint());
			g2.fill(legendArea);
			g2.setPaint(getOutlinePaint());
			g2.setStroke(getOutlineStroke());
			g2.draw(legendArea);

			// Draw individual series elements
			for (int i = 0; i < items.length; i++) {
				Color color = legendItemColors[i];
				if (color != null) {
					g2.setPaint(color);
					g2.fill(items[i].getMarker());
				}
				g2.setPaint(getItemPaint());
				g2.drawString(items[i].getItem().getLabel(),
										(float) items[i].getLabelPosition().getX(),
										(float) items[i].getLabelPosition().getY());
			}

			// translate the origin back to what it was prior to drawing the
			// legend
			g2.translate(-translation.getX(), -translation.getY());

			if (horizontal) {
				// The remaining drawing area bounding box will have the same
				// x origin, width and height independent of the anchor's
				// location. The variable is the y coordinate. If the anchor is
				// SOUTH, the y coordinate is simply the original y coordinate
				// of the available area. If it is NORTH, we adjust original y
				// by the total height of the legend and the initial gap.
				double yy = available.getY();
				double yloc = (inverted) ? yy
														: yy + legendArea.getHeight()
																			+ getOuterGap().getBottomSpace(availableHeight);

				// return the remaining available drawing area
				return new Rectangle2D.Double(available.getX(), yloc, availableWidth,
									availableHeight - legendArea.getHeight()
														- getOuterGap().getTopSpace(availableHeight)
														- getOuterGap().getBottomSpace(availableHeight));
			} else {
				// The remaining drawing area bounding box will have the same
				// y origin, width and height independent of the anchor's
				// location. The variable is the x coordinate. If the anchor is
				// EAST, the x coordinate is simply the original x coordinate
				// of the available area. If it is WEST, we adjust original x
				// by the total width of the legend and the initial gap.
				double xloc = (inverted) ? available.getX()
														: available.getX()
																			+ legendArea.getWidth()
																			+ getOuterGap().getLeftSpace(availableWidth)
																			+ getOuterGap().getRightSpace(availableWidth);

				// return the remaining available drawing area
				return new Rectangle2D.Double(xloc, available.getY(),
									availableWidth - legendArea.getWidth()
														- getOuterGap().getLeftSpace(availableWidth)
														- getOuterGap().getRightSpace(availableWidth),
									availableHeight);
			}
		} else {
			return available;
		}
	}

	/**
	 * Creates a legend item
	 * 
	 * @param graphics
	 *           the graphics device.
	 * @param item
	 *           the legend item.
	 * @param x
	 *           the x coordinate.
	 * @param y
	 *           the y coordinate.
	 * @return the legend item.
	 */
	private DrawableLegendItem createLegendItem(Graphics graphics,
																LegendItem item, double x, double y) {

		int innerGap = 2;
		FontMetrics fm = graphics.getFontMetrics();
		LineMetrics lm = fm.getLineMetrics(item.getLabel(), graphics);
		float textHeight = lm.getHeight();

		DrawableLegendItem drawable = new DrawableLegendItem(item);

		float xloc = (float) (x + innerGap + 1.15f * textHeight);
		float yloc = (float) (y + innerGap + (textHeight - lm.getLeading() - lm.getDescent()));

		drawable.setLabelPosition(new Point2D.Float(xloc, yloc));

		float boxDim = textHeight * 0.70f;
		xloc = (float) (x + innerGap + 0.15f * textHeight);
		yloc = (float) (y + innerGap + 0.15f * textHeight);

		drawable.setMarker(new Rectangle2D.Float(xloc, yloc, boxDim, boxDim));

		float width = (float) (drawable.getLabelPosition().getX() - x
											+ fm.stringWidth(item.getLabel()) + 0.5 * textHeight);

		float height = 2 * innerGap + textHeight;
		drawable.setBounds(x, y, width, height);
		return drawable;

	}

}
