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
 * ----------------------
 * PlotRenderingInfo.java
 * ----------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: PlotRenderingInfo.java,v 1.1 2011-01-31 09:02:09 klukas Exp $
 * Changes
 * -------
 * 16-Sep-2003 : Version 1 (DG);
 * 23-Sep-2003 : Added Javadocs (DG);
 */

package org.jfree.chart.plot;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtils;

/**
 * Stores information about the dimensions of a plot and its subplots.
 */
public class PlotRenderingInfo implements Cloneable, Serializable {

	/** The owner of this info. */
	private transient ChartRenderingInfo owner;

	/** The plot area. */
	private transient Rectangle2D plotArea;

	/** The data area. */
	private transient Rectangle2D dataArea;

	/** Storage for the plot rendering info objects belonging to the subplots. */
	private List subplotInfo;

	/**
	 * Default constructor.
	 * 
	 * @param owner
	 *           the owner.
	 */
	public PlotRenderingInfo(ChartRenderingInfo owner) {
		this.owner = owner;
		this.dataArea = new Rectangle2D.Double();
		this.subplotInfo = new java.util.ArrayList();
	}

	/**
	 * Returns the owner.
	 * 
	 * @return The owner.
	 */
	public ChartRenderingInfo getOwner() {
		return this.owner;
	}

	/**
	 * Returns the plot area (in Java2D space).
	 * 
	 * @return The plot area.
	 */
	public Rectangle2D getPlotArea() {
		return this.plotArea;
	}

	/**
	 * Sets the plot area.
	 * 
	 * @param area
	 *           the plot area (in Java2D space)
	 */
	public void setPlotArea(Rectangle2D area) {
		this.plotArea = area;
	}

	/**
	 * Returns the plot's data area (in Java2D space).
	 * 
	 * @return The data area.
	 */
	public Rectangle2D getDataArea() {
		return this.dataArea;
	}

	/**
	 * Sets the data area.
	 * 
	 * @param area
	 *           the data area (in Java2D space).
	 */
	public void setDataArea(Rectangle2D area) {
		this.dataArea = area;
	}

	/**
	 * Adds the info for a subplot.
	 * 
	 * @param info
	 *           the subplot info.
	 */
	public void addSubplotInfo(PlotRenderingInfo info) {
		this.subplotInfo.add(info);
	}

	/**
	 * Returns the info for a subplot.
	 * 
	 * @param index
	 *           the subplot index.
	 * @return The info.
	 */
	public PlotRenderingInfo getSubplotInfo(int index) {
		return (PlotRenderingInfo) this.subplotInfo.get(index);
	}

	/**
	 * Tests this instance for equality against an arbitrary object.
	 * 
	 * @param obj
	 *           the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PlotRenderingInfo) {
			PlotRenderingInfo i = (PlotRenderingInfo) obj;
			// if (!ObjectUtils.equal(this.owner, i.owner)) {
			// return false;
			// }
			if (!ObjectUtils.equal(this.dataArea, i.dataArea)) {
				return false;
			}
			if (!ObjectUtils.equal(this.plotArea, i.plotArea)) {
				return false;
			}
			if (!ObjectUtils.equal(this.subplotInfo, i.subplotInfo)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns a clone of this object.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if there is a problem cloning.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
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
		SerialUtilities.writeShape(this.dataArea, stream);
		SerialUtilities.writeShape(this.plotArea, stream);
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
		this.dataArea = (Rectangle2D) SerialUtilities.readShape(stream);
		this.plotArea = (Rectangle2D) SerialUtilities.readShape(stream);
	}

}
