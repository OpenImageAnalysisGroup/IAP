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
 * LegendItemCollection.java
 * -------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: LegendItemCollection.java,v 1.1 2011-01-31 09:03:12 klukas Exp $
 * Changes
 * -------
 * 07-Feb-2002 : Version 1 (DG);
 * 24-Sep-2002 : Added get(int) and getItemCount() methods (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 */

package org.jfree.chart;

import java.util.Iterator;
import java.util.List;

/**
 * A collection of legend items.
 */
public class LegendItemCollection {

	/** Storage for the legend items. */
	private List items;

	/**
	 * Constructs a new legend item collection, initially empty.
	 */
	public LegendItemCollection() {
		this.items = new java.util.ArrayList();
	}

	/**
	 * Adds a legend item to the collection.
	 * 
	 * @param item
	 *           the item to add.
	 */
	public void add(LegendItem item) {
		this.items.add(item);
	}

	/**
	 * Adds the legend items from another collection to this collection.
	 * 
	 * @param collection
	 *           the other collection.
	 */
	public void addAll(LegendItemCollection collection) {
		this.items.addAll(collection.items);
	}

	/**
	 * Returns a legend item from the collection.
	 * 
	 * @param index
	 *           the legend item index (zero-based).
	 * @return the legend item.
	 */
	public LegendItem get(int index) {
		return (LegendItem) this.items.get(index);
	}

	/**
	 * Returns the number of legend items in the collection.
	 * 
	 * @return the item count.
	 */
	public int getItemCount() {
		return this.items.size();
	}

	/**
	 * Returns an iterator that provides access to all the legend items.
	 * 
	 * @return an iterator.
	 */
	public Iterator iterator() {
		return this.items.iterator();
	}

}
