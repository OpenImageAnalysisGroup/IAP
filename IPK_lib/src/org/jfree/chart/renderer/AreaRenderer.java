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
 * -----------------
 * AreaRenderer.java
 * -----------------
 * (C) Copyright 2002-2004, by Jon Iles and Contributors.
 * Original Author: Jon Iles;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Christian W. Zuckschwerdt;
 * $Id: AreaRenderer.java,v 1.1 2011-01-31 09:02:47 klukas Exp $
 * Changes:
 * --------
 * 21-May-2002 : Version 1, contributed by John Iles (DG);
 * 29-May-2002 : Now extends AbstractCategoryItemRenderer (DG);
 * 11-Jun-2002 : Updated Javadoc comments (DG);
 * 25-Jun-2002 : Removed unnecessary imports (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 10-Oct-2002 : Added constructors and basic entity support (DG);
 * 24-Oct-2002 : Amendments for changes in CategoryDataset interface and CategoryToolTipGenerator
 * interface (DG);
 * 05-Nov-2002 : Replaced references to CategoryDataset with TableDataset (DG);
 * 06-Nov-2002 : Renamed drawCategoryItem(...) --> drawItem(...) and now using axis for
 * category spacing. Renamed AreaCategoryItemRenderer --> AreaRenderer (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 10-Apr-2003 : Changed CategoryDataset to KeyedValues2DDataset in drawItem(...) method (DG);
 * 12-May-2003 : Modified to take into account the plot orientation (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 07-Oct-2003 : Added renderer state (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

/**
 * A category item renderer that draws area charts.
 * <p>
 * You can use this renderer with the {@link org.jfree.chart.plot.CategoryPlot} class.
 * 
 * @author Jon Iles
 */
public class AreaRenderer extends AbstractCategoryItemRenderer
									implements Cloneable, PublicCloneable, Serializable {

	/** A flag that controls how the ends of the areas are drawn. */
	private AreaRendererEndType endType;

	/**
	 * Creates a new renderer.
	 */
	public AreaRenderer() {
		super();
		this.endType = AreaRendererEndType.TAPER;
	}

	/**
	 * Returns a token that controls how the renderer draws the end points.
	 * 
	 * @return The end type (never <code>null</code>).
	 */
	public AreaRendererEndType getEndType() {
		return this.endType;
	}

	/**
	 * Sets a token that controls how the renderer draws the end points, and sends
	 * a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param type
	 *           the end type (<code>null</code> not permitted).
	 */
	public void setEndType(AreaRendererEndType type) {
		if (type == null) {
			throw new IllegalArgumentException("Null 'type' argument.");
		}
		this.endType = type;
		notifyListeners(new RendererChangeEvent(this));
	}

	/**
	 * Draw a single data item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the data plot area.
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

		// plot non-null values only...
		Number value = dataset.getValue(row, column);
		if (value != null) {
			PlotOrientation orientation = plot.getOrientation();
			RectangleEdge axisEdge = plot.getDomainAxisEdge();
			int count = dataset.getColumnCount();
			float x0 = (float) domainAxis.getCategoryStart(column, count, dataArea, axisEdge);
			float x1 = (float) domainAxis.getCategoryMiddle(column, count, dataArea, axisEdge);
			float x2 = (float) domainAxis.getCategoryEnd(column, count, dataArea, axisEdge);

			x0 = Math.round(x0);
			x1 = Math.round(x1);
			x2 = Math.round(x2);

			if (this.endType == AreaRendererEndType.TRUNCATE) {
				if (column == 0) {
					x0 = x1;
				} else
					if (column == getColumnCount() - 1) {
						x2 = x1;
					}
			}

			double yy1 = value.doubleValue();

			double yy0 = 0.0;
			if (column > 0) {
				Number n0 = dataset.getValue(row, column - 1);
				if (n0 != null) {
					yy0 = (n0.doubleValue() + yy1) / 2.0;
				}
			}

			double yy2 = 0.0;
			if (column < dataset.getColumnCount() - 1) {
				Number n2 = dataset.getValue(row, column + 1);
				if (n2 != null) {
					yy2 = (n2.doubleValue() + yy1) / 2.0;
				}
			}

			RectangleEdge edge = plot.getRangeAxisEdge();
			float y0 = (float) rangeAxis.valueToJava2D(yy0, dataArea, edge);
			float y1 = (float) rangeAxis.valueToJava2D(yy1, dataArea, edge);
			float y2 = (float) rangeAxis.valueToJava2D(yy2, dataArea, edge);
			float yz = (float) rangeAxis.valueToJava2D(0.0, dataArea, edge);

			g2.setPaint(getItemPaint(row, column));
			g2.setStroke(getItemStroke(row, column));

			GeneralPath area = new GeneralPath();

			if (orientation == PlotOrientation.VERTICAL) {
				area.moveTo(x0, yz);
				area.lineTo(x0, y0);
				area.lineTo(x1, y1);
				area.lineTo(x2, y2);
				area.lineTo(x2, yz);
			} else
				if (orientation == PlotOrientation.HORIZONTAL) {
					area.moveTo(yz, x0);
					area.lineTo(y0, x0);
					area.lineTo(y1, x1);
					area.lineTo(y2, x2);
					area.lineTo(yz, x2);
				}
			area.closePath();

			g2.setPaint(getItemPaint(row, column));
			g2.fill(area);

			// draw the item labels if there are any...
			if (isItemLabelVisible(row, column)) {
				drawItemLabel(
									g2, orientation, dataset, row, column, x1, y1, (value.doubleValue() < 0.0));
			}

			// collect entity and tool tip information...
			if (state.getInfo() != null) {
				EntityCollection entities = state.getInfo().getOwner().getEntityCollection();
				if (entities != null) {
					String tip = null;
					CategoryToolTipGenerator generator = getToolTipGenerator(row, column);
					if (generator != null) {
						tip = generator.generateToolTip(dataset, row, column);
					}
					String url = null;
					if (getItemURLGenerator(row, column) != null) {
						url = getItemURLGenerator(row, column).generateURL(dataset, row, column);
					}
					Comparable columnKey = dataset.getColumnKey(column);
					CategoryItemEntity entity = new CategoryItemEntity(
										area, tip, url, dataset, row, columnKey, column
										);
					entities.addEntity(entity);
				}
			}
		}

	}

	/**
	 * Returns an independent copy of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            should not happen.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
