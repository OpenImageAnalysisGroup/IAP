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
 * -----------
 * Marker.java
 * -----------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Nicolas Brodu;
 * $Id: Marker.java,v 1.1 2011-01-31 09:02:09 klukas Exp $
 * Changes (since 2-Jul-2002)
 * --------------------------
 * 02-Jul-2002 : Added extra constructor, standard header and Javadoc comments (DG);
 * 20-Aug-2002 : Added the outline stroke attribute (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 16-Oct-2002 : Added new constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 21-May-2003 : Added labels (DG);
 * 11-Sep-2003 : Implemented Cloneable (NB);
 * 05-Nov-2003 : Added checks to ensure some attributes are never null (DG);
 * 11-Feb-2003 : Moved to org.jfree.chart.plot package, plus significant API changes to
 * support IntervalMarker in plots (DG);
 */

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtils;
import org.jfree.util.UnitType;

/**
 * The base class for markers that can be added to plots to highlight a value or range of values.
 */
public abstract class Marker implements Serializable, Cloneable {

	/** The paint. */
	private transient Paint paint;

	/** The stroke. */
	private transient Stroke stroke;

	/** The outline paint. */
	private transient Paint outlinePaint;

	/** The outline stroke. */
	private transient Stroke outlineStroke;

	/** The alpha transparency. */
	private float alpha;

	/** The label. */
	private String label = null;

	/** The label font. */
	private Font labelFont;

	/** The label paint. */
	private transient Paint labelPaint;

	/** The label position. */
	private RectangleAnchor labelAnchor;

	/** The text anchor for the label. */
	private TextAnchor labelTextAnchor;

	/** The label offset from the marker rectangle. */
	private RectangleInsets labelOffset;

	/**
	 * Constructs a new marker.
	 */
	public Marker() {
		this(Color.gray);
	}

	/**
	 * Constructs a new marker.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public Marker(Paint paint) {
		this(paint, new BasicStroke(0.5f), Color.gray, new BasicStroke(0.5f), 0.80f);
	}

	/**
	 * Constructs a new marker.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 * @param outlinePaint
	 *           the outline paint (<code>null</code> permitted).
	 * @param outlineStroke
	 *           the outline stroke (<code>null</code> permitted).
	 * @param alpha
	 *           the alpha transparency.
	 */
	public Marker(Paint paint, Stroke stroke,
						Paint outlinePaint, Stroke outlineStroke,
						float alpha) {

		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}

		this.paint = paint;
		this.stroke = stroke;
		this.outlinePaint = outlinePaint;
		this.outlineStroke = outlineStroke;
		this.alpha = alpha;

		this.labelFont = new Font("SansSerif", Font.PLAIN, 9);
		this.labelPaint = Color.black;
		this.labelAnchor = RectangleAnchor.TOP_LEFT;
		this.labelOffset = new RectangleInsets(UnitType.ABSOLUTE, 3.0, 3.0, 3.0, 3.0);
		this.labelTextAnchor = TextAnchor.CENTER;

	}

	/**
	 * Returns the paint.
	 * 
	 * @return the paint (never <code>null</code>).
	 */
	public Paint getPaint() {
		return this.paint;
	}

	/**
	 * Sets the paint.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setPaint(Paint paint) {
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		this.paint = paint;
	}

	/**
	 * Returns the stroke.
	 * 
	 * @return the stroke (never <code>null</code>).
	 */
	public Stroke getStroke() {
		return this.stroke;
	}

	/**
	 * Sets the stroke.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 */
	public void setStroke(Stroke stroke) {
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}
		this.stroke = stroke;
	}

	/**
	 * Returns the outline paint.
	 * 
	 * @return the outline paint (possibly <code>null</code>).
	 */
	public Paint getOutlinePaint() {
		return this.outlinePaint;
	}

	/**
	 * Sets the outline paint.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setOutlinePaint(Paint paint) {
		this.outlinePaint = paint;
	}

	/**
	 * Returns the outline stroke.
	 * 
	 * @return the outline stroke (possibly <code>null</code>).
	 */
	public Stroke getOutlineStroke() {
		return this.outlineStroke;
	}

	/**
	 * Sets the outline stroke.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 */
	public void setOutlineStroke(Stroke stroke) {
		this.outlineStroke = stroke;
	}

	/**
	 * Returns the alpha transparency.
	 * 
	 * @return the alpha transparency.
	 */
	public float getAlpha() {
		return this.alpha;
	}

	/**
	 * Sets the alpha transparency.
	 * 
	 * @param alpha
	 *           the alpha transparency.
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	/**
	 * Returns the label (if <code>null</code> no label is displayed).
	 * 
	 * @return The label (possibly <code>null</code>).
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Sets the label (if <code>null</code> no label is displayed).
	 * 
	 * @param label
	 *           the label (<code>null</code> permitted).
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the label font.
	 * 
	 * @return the label font (never <code>null</code>).
	 */
	public Font getLabelFont() {
		return this.labelFont;
	}

	/**
	 * Sets the label font.
	 * 
	 * @param font
	 *           the font (<code>null</code> not permitted).
	 */
	public void setLabelFont(Font font) {
		if (font == null) {
			throw new IllegalArgumentException("Null 'font' argument.");
		}
		this.labelFont = font;
	}

	/**
	 * Returns the label paint.
	 * 
	 * @return the label paint (never </code>null</code>).
	 */
	public Paint getLabelPaint() {
		return this.labelPaint;
	}

	/**
	 * Sets the label paint.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setLabelPaint(Paint paint) {
		if (paint == null) {
			throw new IllegalArgumentException("Marker.setLabelPaint(...): null not permitted.");
		}
		this.labelPaint = paint;
	}

	/**
	 * Returns the label anchor.
	 * 
	 * @return the label anchor (never <code>null</code>).
	 */
	public RectangleAnchor getLabelAnchor() {
		return this.labelAnchor;
	}

	/**
	 * Sets the label anchor.
	 * 
	 * @param anchor
	 *           the anchor (<code>null</code> not permitted).
	 */
	public void setLabelAnchor(RectangleAnchor anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException("Null 'anchor' argument.");
		}
		this.labelAnchor = anchor;
	}

	/**
	 * Returns the label offset.
	 * 
	 * @return the label offset (never <code>null</code>).
	 */
	public RectangleInsets getLabelOffset() {
		return this.labelOffset;
	}

	/**
	 * Sets the label offset.
	 * 
	 * @param offset
	 *           the label offset (<code>null</code> not permitted).
	 */
	public void setLabelOffset(RectangleInsets offset) {
		if (offset == null) {
			throw new IllegalArgumentException("Null 'offset' argument.");
		}
		this.labelOffset = offset;
	}

	/**
	 * Returns the label text anchor.
	 * 
	 * @return the label text anchor (never <code>null</code>).
	 */
	public TextAnchor getLabelTextAnchor() {
		return this.labelTextAnchor;
	}

	/**
	 * Sets the label text anchor.
	 * 
	 * @param anchor
	 *           the label text anchor (<code>null</code> not permitted).
	 */
	public void setLabelTextAnchor(TextAnchor anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException("Null 'anchor' argument.");
		}
		this.labelTextAnchor = anchor;
	}

	/**
	 * Tests an object for equality with this instance.
	 * 
	 * @param object
	 *           the object to test.
	 * @return A boolean.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof Marker) {
			Marker marker = (Marker) object;
			boolean b0 = ObjectUtils.equal(this.paint, marker.paint);
			boolean b1 = ObjectUtils.equal(this.stroke, marker.stroke);
			boolean b2 = (this.alpha == marker.alpha);
			boolean b3 = ObjectUtils.equal(this.label, marker.label);
			boolean b4 = ObjectUtils.equal(this.labelFont, marker.labelFont);
			boolean b5 = ObjectUtils.equal(this.labelPaint, marker.labelPaint);
			boolean b6 = (this.labelAnchor == marker.labelAnchor);

			return b0 && b1 && b2 && b3 && b4 && b5 && b6;
		}

		return false;

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
		SerialUtilities.writePaint(this.paint, stream);
		SerialUtilities.writeStroke(this.stroke, stream);

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
		this.paint = SerialUtilities.readPaint(stream);
		this.stroke = SerialUtilities.readStroke(stream);

	}

}
