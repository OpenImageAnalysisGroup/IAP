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
 * ---------------------
 * EntityCollection.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: EntityCollection.java,v 1.1 2011-01-31 09:02:59 klukas Exp $
 * Changes
 * -------
 * 23-May-2002 : Version 1 (DG);
 * 25-Jun-2002 : Removed unnecessary import (DG);
 * 26-Jun-2002 : Added iterator() method (DG);
 * 03-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 30-Jan-2004 : Added a method to add a collection of entities.
 */

package org.jfree.chart.entity;

import java.util.Collection;
import java.util.Iterator;

/**
 * Defines the methods that a collection of entities is required to implement.
 */
public interface EntityCollection {

	/**
	 * Clears all entities.
	 */
	public void clear();

	/**
	 * Adds an entity to the collection.
	 * 
	 * @param entity
	 *           the entity.
	 */
	public void addEntity(ChartEntity entity);

	/**
	 * Adds the entities from another collection to this collection.
	 * 
	 * @param collection
	 *           the other collection.
	 */
	public void addEntities(EntityCollection collection);

	/**
	 * Returns an entity whose area contains the specified point.
	 * 
	 * @param x
	 *           the x coordinate.
	 * @param y
	 *           the y coordinate.
	 * @return The entity.
	 */
	public ChartEntity getEntity(double x, double y);

	/**
	 * Returns the entities in an unmodifiable collection.
	 * 
	 * @return The entities.
	 */
	public Collection getEntities();

	/**
	 * Returns an iterator for the entities in the collection.
	 * 
	 * @return An iterator.
	 */
	public Iterator iterator();

}
