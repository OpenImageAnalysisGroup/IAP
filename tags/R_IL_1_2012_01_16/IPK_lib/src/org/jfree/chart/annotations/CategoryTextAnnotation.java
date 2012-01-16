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
 * CategoryTextAnnotation.java
 * ---------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: CategoryTextAnnotation.java,v 1.1 2011-01-31 09:02:51 klukas Exp $
 * Changes:
 * --------
 * 02-Apr-2003 : Version 1 (DG);
 * 02-Jul-2003 : Added new text alignment and rotation options (DG);
 * 04-Jul-2003 : Added a category anchor option (DG);
 * 19-Aug-2003 : Added equals(...) method and implemented Cloneable (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 */

package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;

/**
 * A text annotation that can be placed on a {@link org.jfree.chart.plot.CategoryPlot}.
 */
public class CategoryTextAnnotation extends TextAnnotation
												implements CategoryAnnotation, Cloneable, Serializable {

	/** The category. */
	private Comparable category;

	/** The category anchor (START, MIDDLE, or END). */
	private CategoryAnchor categoryAnchor;

	/** The value. */
	private double value;

	/**
	 * Creates a new annotation to be displayed at the given location.
	 * 
	 * @param text
	 *           the text.
	 * @param category
	 *           the category.
	 * @param value
	 *           the value.
	 */
	public CategoryTextAnnotation(String text, Comparable category, double value) {
		super(text);
		this.category = category;
		this.value = value;
		this.categoryAnchor = CategoryAnchor.MIDDLE;
	}

	/**
	 * Returns the category.
	 * 
	 * @return The category.
	 */
	public Comparable getCategory() {
		return this.category;
	}

	/**
	 * Sets the category that the annotation attaches to.
	 * 
	 * @param category
	 *           the category.
	 */
	public void setCategory(Comparable category) {
		this.category = category;
	}

	/**
	 * Returns the category anchor point.
	 * 
	 * @return The category anchor point.
	 */
	public CategoryAnchor getCategoryAnchor() {
		return this.categoryAnchor;
	}

	/**
	 * Sets the category anchor point.
	 * 
	 * @param anchor
	 *           the anchor point.
	 */
	public void setCategoryAnchor(CategoryAnchor anchor) {
		this.categoryAnchor = anchor;
	}

	/**
	 * Returns the value that the annotation attaches to.
	 * 
	 * @return The value.
	 */
	public double getValue() {
		return this.value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *           the value.
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Draws the annotation.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot.
	 * @param dataArea
	 *           the data area.
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 */
	public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
							CategoryAxis domainAxis, ValueAxis rangeAxis) {

		CategoryDataset dataset = plot.getDataset();
		int catIndex = dataset.getColumnIndex(this.category);
		int catCount = dataset.getColumnCount();

		float anchorX = 0.0f;
		float anchorY = 0.0f;
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(),
																						orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(),
																						orientation);

		if (orientation == PlotOrientation.HORIZONTAL) {
			anchorY = (float) domainAxis.getCategoryJava2DCoordinate(
								this.categoryAnchor, catIndex, catCount, dataArea, domainEdge
								);
			anchorX = (float) rangeAxis.valueToJava2D(this.value, dataArea, rangeEdge);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				anchorX = (float) domainAxis.getCategoryJava2DCoordinate(
									this.categoryAnchor, catIndex, catCount, dataArea, domainEdge
									);
				anchorY = (float) rangeAxis.valueToJava2D(this.value, dataArea, rangeEdge);
			}
		g2.setFont(getFont());
		g2.setPaint(getPaint());
		RefineryUtilities.drawRotatedString(
							getText(),
							g2,
							anchorX,
							anchorY,
							getTextAnchor(),
							getRotationAnchor(),
							getRotationAngle()
							);

	}

	/**
	 * Tests this object for equality with another.
	 * 
	 * @param object
	 *           the object to test against.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof CategoryTextAnnotation) {
			CategoryTextAnnotation cta = (CategoryTextAnnotation) object;
			if (super.equals(object)) {

				boolean b0 = this.category.equals(cta.getCategory());
				boolean b1 = this.categoryAnchor.equals(cta.getCategoryAnchor());
				boolean b2 = (this.value == cta.getValue());

				return b0 && b1 && b2;

			}
		}

		return false;
	}

	/**
	 * Returns a clone of the annotation.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            this class will not throw this exception, but subclasses
	 *            (if any) might.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
