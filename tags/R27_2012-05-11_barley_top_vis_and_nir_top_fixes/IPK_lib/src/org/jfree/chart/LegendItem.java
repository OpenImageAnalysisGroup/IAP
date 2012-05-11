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
 * ---------------
 * LegendItem.java
 * ---------------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Andrzej Porebski;
 * David Li;
 * Wolfgang Irler;
 * Luke Quinane;
 * $Id: LegendItem.java,v 1.1 2011-01-31 09:03:12 klukas Exp $
 * Changes (from 2-Oct-2002)
 * -------------------------
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 17-Jan-2003 : Dropped outlineStroke attribute (DG);
 * 08-Oct-2003 : Applied patch for displaying series line style, contributed by Luke Quinane (DG);
 * 21-Jan-2004 : Added the shapeFilled flag (DG);
 * 04-Jun-2004 : Added equals() method, implemented Serializable (DG);
 */

package org.jfree.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtils;

/**
 * A legend item. Records all the properties of a legend item, but is not concerned about the
 * display location. Instances of this class are immutable.
 */
public class LegendItem implements Serializable {

	/** The label. */
	private String label;

	/** The description (not currently used). */
	private String description;

	/** The shape. */
	private transient Shape shape;

	/** A flag that controls whether or not the shape is filled. */
	private boolean shapeFilled;

	/** The paint. */
	private transient Paint paint;

	/** The stroke. */
	private transient Stroke stroke;

	/** The outline paint. */
	private transient Paint outlinePaint;

	/** The outline stroke. */
	private transient Stroke outlineStroke;

	/**
	 * Creates a new legend item.
	 * 
	 * @param label
	 *           the label (<code>null</code> not permitted).
	 * @param paint
	 *           the fill paint (<code>null</code> not permitted).
	 */
	public LegendItem(String label, Paint paint) {
		this(
							label, label, new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0),
							true, paint, new BasicStroke(0.5f), Color.lightGray, new BasicStroke(0.5f));
	}

	/**
	 * Creates a new legend item.
	 * 
	 * @param label
	 *           the label (<code>null</code> not permitted).
	 * @param description
	 *           the description (not currently used, <code>null</code> permitted).
	 * @param shape
	 *           the shape (<code>null</code> permitted).
	 * @param shapeFilled
	 *           a flag that controls whether or not the shape is filled.
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 * @param outlinePaint
	 *           the outline paint (<code>null</code> not permitted).
	 * @param outlineStroke
	 *           the outline stroke (<code>null</code> not permitted).
	 */
	public LegendItem(String label,
								String description,
								Shape shape,
								boolean shapeFilled,
								Paint paint,
								Stroke stroke,
								Paint outlinePaint,
								Stroke outlineStroke) {

		if (label == null) {
			throw new IllegalArgumentException("Null 'label' argument.");
		}
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}
		if (outlinePaint == null) {
			throw new IllegalArgumentException("Null 'outlinePaint' argument.");
		}
		if (outlineStroke == null) {
			throw new IllegalArgumentException("Null 'outlineStroke' argument.");
		}
		this.label = label;
		this.description = description;
		this.shape = shape;
		this.shapeFilled = shapeFilled;
		this.paint = paint;
		this.stroke = stroke;
		this.outlinePaint = outlinePaint;
		this.outlineStroke = outlineStroke;

	}

	/**
	 * Returns the label.
	 * 
	 * @return The label (never <code>null</code>).
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Returns the shape used to label the series represented by this legend item.
	 * 
	 * @return The shape (never <code>null</code>).
	 */
	public Shape getShape() {
		return this.shape;
	}

	/**
	 * Returns a flag that controls whether or not the shape is filled.
	 * 
	 * @return A boolean.
	 */
	public boolean isShapeFilled() {
		return this.shapeFilled;
	}

	/**
	 * Returns the paint.
	 * 
	 * @return The paint (never <code>null</code>.
	 */
	public Paint getPaint() {
		return this.paint;
	}

	/**
	 * Returns the stroke used to render the shape for this series.
	 * 
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getStroke() {
		return this.stroke;
	}

	/**
	 * Returns the outline paint.
	 * 
	 * @return The outline paint (never <code>null</code>).
	 */
	public Paint getOutlinePaint() {
		return this.outlinePaint;
	}

	/**
	 * Returns the outline stroke.
	 * 
	 * @return The outline stroke (never <code>null</code>).
	 */
	public Stroke getOutlineStroke() {
		return this.outlineStroke;
	}

	/**
	 * Tests this item for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof LegendItem) {
			LegendItem item = (LegendItem) obj;
			if (!this.label.equals(item.label)) {
				return false;
			}
			if (!ObjectUtils.equal(this.description, item.description)) {
				return false;
			}
			if (!ObjectUtils.equal(this.shape, item.shape)) {
				return false;
			}
			if (this.shapeFilled != item.shapeFilled) {
				return false;
			}
			if (!this.stroke.equals(item.stroke)) {
				return false;
			}
			if (!this.paint.equals(item.paint)) {
				return false;
			}
			if (!this.outlineStroke.equals(item.outlineStroke)) {
				return false;
			}
			if (!this.outlinePaint.equals(item.outlinePaint)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the output stream (<code>null</code> not permitted).
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		SerialUtilities.writeShape(this.shape, stream);
		SerialUtilities.writeStroke(this.stroke, stream);
		SerialUtilities.writePaint(this.paint, stream);
		SerialUtilities.writeStroke(this.outlineStroke, stream);
		SerialUtilities.writePaint(this.outlinePaint, stream);
	}

	/**
	 * Provides serialization support.
	 * 
	 * @param stream
	 *           the input stream (<code>null</code> not permitted).
	 * @throws IOException
	 *            if there is an I/O error.
	 * @throws ClassNotFoundException
	 *            if there is a classpath problem.
	 */
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		this.shape = SerialUtilities.readShape(stream);
		this.stroke = SerialUtilities.readStroke(stream);
		this.paint = SerialUtilities.readPaint(stream);
		this.outlineStroke = SerialUtilities.readStroke(stream);
		this.outlinePaint = SerialUtilities.readPaint(stream);
	}

	// // DEPRECATED CODE //////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new legend item.
	 * 
	 * @param label
	 *           the label.
	 * @param description
	 *           the description (not used).
	 * @param shape
	 *           the shape.
	 * @param paint
	 *           the paint.
	 * @param outlinePaint
	 *           the outline paint.
	 * @param stroke
	 *           the stroke.
	 * @deprecated Use the other constructor.
	 */
	public LegendItem(String label,
						String description,
						Shape shape,
						Paint paint,
						Paint outlinePaint,
						Stroke stroke) {
		this(label, description, shape, true, paint, outlinePaint, stroke);
	}

	/**
	 * Creates a new legend item.
	 * 
	 * @param label
	 *           the label.
	 * @param description
	 *           the description (not used).
	 * @param shape
	 *           the shape.
	 * @param shapeFilled
	 *           a flag that controls whether or not the shape is filled.
	 * @param paint
	 *           the paint.
	 * @param outlinePaint
	 *           the outline paint.
	 * @param stroke
	 *           the stroke.
	 * @deprecated Use other constructor.
	 */
	public LegendItem(String label,
						String description,
						Shape shape,
						boolean shapeFilled,
						Paint paint, Paint outlinePaint,
						Stroke stroke) {

		this.label = label;
		this.description = description;
		this.shape = shape;
		this.shapeFilled = shapeFilled;
		this.paint = paint;
		this.outlinePaint = outlinePaint;
		this.stroke = stroke;

	}

}
