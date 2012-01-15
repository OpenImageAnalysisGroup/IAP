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
 * --------------------------
 * AbstractSeriesDataset.java
 * --------------------------
 * (C) Copyright 2001-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: AbstractSeriesDataset.java,v 1.1 2011-01-31 09:02:19 klukas Exp $
 * Changes
 * -------
 * 17-Nov-2001 : Version 1 (DG);
 * 28-Mar-2002 : Implemented SeriesChangeListener interface (DG);
 * 04-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 04-Feb-2003 : Removed redundant methods (DG);
 * 27-Mar-2003 : Implemented Serializable (DG);
 */

package org.jfree.data;

import java.io.Serializable;

/**
 * An abstract implementation of the {@link SeriesDataset} interface, containing a
 * mechanism for registering change listeners.
 */
public abstract class AbstractSeriesDataset extends AbstractDataset
															implements SeriesDataset,
																			SeriesChangeListener,
																			Serializable {

	/**
	 * Creates a new dataset.
	 */
	protected AbstractSeriesDataset() {
		super();
	}

	/**
	 * Returns the number of series in the dataset.
	 * 
	 * @return The series count.
	 */
	public abstract int getSeriesCount();

	/**
	 * Returns the name of a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The series name.
	 */
	public abstract String getSeriesName(int series);

	/**
	 * Called when a series belonging to the dataset changes.
	 * 
	 * @param event
	 *           information about the change.
	 */
	public void seriesChanged(final SeriesChangeEvent event) {
		fireDatasetChanged();
	}

}
