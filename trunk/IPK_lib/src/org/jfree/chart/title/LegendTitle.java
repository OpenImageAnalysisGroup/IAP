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
 * LegendTitle.java
 * ----------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: LegendTitle.java,v 1.1 2011-01-31 09:03:14 klukas Exp $
 * Changes
 * -------
 * 07-Feb-2002 : Version 1. INCOMPLETE, PLEASE IGNORE. (DG);
 */

package org.jfree.chart.title;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemLayout;
import org.jfree.chart.StandardLegendItemLayout;

/**
 * A chart title that displays a legend for the data in the chart.
 * <P>
 * The title can be populated with legend items manually, or you can assign a reference to the chart, in which case the legend items will be automatically
 * created to match the dataset.
 */
public abstract class LegendTitle extends Title {

	/** A container for the legend items. */
	private LegendItemCollection items;

	/**
	 * The object responsible for arranging the legend items to fit in whatever
	 * space is available.
	 */
	// private LegendItemLayout layout;

	/**
	 * Constructs a new, empty LegendTitle.
	 */
	public LegendTitle() {
		this(new StandardLegendItemLayout(0, 0.0));
	}

	/**
	 * Creates a new legend title.
	 * 
	 * @param layout
	 *           the layout.
	 */
	public LegendTitle(LegendItemLayout layout) {
		// this.layout = layout;
	}

	/**
	 * Adds a legend item to the LegendTitle.
	 * 
	 * @param item
	 *           the item to add.
	 */
	public void addLegendItem(LegendItem item) {
		this.items.add(item);
	}

	/**
	 * Draws the title on a Java 2D graphics device (such as the screen or a
	 * printer). Currently it does nothing.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area for the chart and all its titles.
	 */
	public void draw(Graphics2D g2, Rectangle2D area) {

		// if the position is TOP or BOTTOM then the constraint is on the width
		// so layout the items accordingly

		// if the position is LEFT or RIGHT then the constraint is on the height
		// so layout the items accordingly

		// get the height and width of the items, then add the space around the outside
		// work out where to start drawing...
		// and draw...
	}

}
