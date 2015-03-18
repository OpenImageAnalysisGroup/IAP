/*
 * ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jcommon/index.html
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
 * ------------
 * TextBox.java
 * ------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TextBox.java,v 1.1 2011-01-31 09:03:03 klukas Exp $
 * Changes
 * -------
 * 09-Mar-2004 : Version 1 (DG);
 * 22-Mar-2004 : Added equals() method and implemented Serializable (DG);
 */

package org.jfree.text;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.Size2D;
import org.jfree.ui.Spacer;
import org.jfree.util.ObjectUtils;

/**
 * A box containing a text block.
 */
public class TextBox implements Serializable {

	/** The outline paint. */
	private transient Paint outlinePaint;

	/** The outline stroke. */
	private transient Stroke outlineStroke;

	/** The interior space. */
	private Spacer interiorGap;

	/** The background paint. */
	private transient Paint backgroundPaint;

	/** The shadow paint. */
	private transient Paint shadowPaint;

	/** The shadow x-offset. */
	private double shadowXOffset = 2.0;

	/** The shadow y-offset. */
	private double shadowYOffset = 2.0;

	/** The text block. */
	private TextBlock textBlock;

	/**
	 * Creates an empty text box.
	 */
	public TextBox() {
		this((TextBlock) null);
	}

	/**
	 * Creates a text box.
	 * 
	 * @param text
	 *           the text.
	 */
	public TextBox(final String text) {
		this((TextBlock) null);
		if (text != null) {
			this.textBlock = new TextBlock();
			this.textBlock.addLine(text, new Font("SansSerif", Font.PLAIN, 10), Color.black);
		}
	}

	/**
	 * Creates a new text box.
	 * 
	 * @param block
	 *           the text block.
	 */
	public TextBox(final TextBlock block) {
		this.outlinePaint = Color.black;
		this.outlineStroke = new BasicStroke(1.0f);
		this.interiorGap = new Spacer(Spacer.ABSOLUTE, 3.0, 1.0, 3.0, 1.0);
		this.backgroundPaint = new Color(255, 255, 192);
		this.shadowPaint = Color.gray;
		this.shadowXOffset = 2.0;
		this.shadowYOffset = 2.0;
		this.textBlock = block;
	}

	/**
	 * Returns the outline paint.
	 * 
	 * @return The outline paint.
	 */
	public Paint getOutlinePaint() {
		return this.outlinePaint;
	}

	/**
	 * Sets the outline paint.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setOutlinePaint(final Paint paint) {
		this.outlinePaint = paint;
	}

	/**
	 * Returns the outline stroke.
	 * 
	 * @return The outline stroke.
	 */
	public Stroke getOutlineStroke() {
		return this.outlineStroke;
	}

	/**
	 * Sets the outline stroke.
	 * 
	 * @param stroke
	 *           the stroke.
	 */
	public void setOutlineStroke(final Stroke stroke) {
		this.outlineStroke = stroke;
	}

	/**
	 * Returns the interior gap.
	 * 
	 * @return The interior gap.
	 */
	public Spacer getInteriorGap() {
		return this.interiorGap;
	}

	/**
	 * Sets the interior gap.
	 * 
	 * @param gap
	 *           the gap.
	 */
	public void setInteriorGap(final Spacer gap) {
		this.interiorGap = gap;
	}

	/**
	 * Returns the background paint.
	 * 
	 * @return The background paint.
	 */
	public Paint getBackgroundPaint() {
		return this.backgroundPaint;
	}

	/**
	 * Sets the background paint.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setBackgroundPaint(final Paint paint) {
		this.backgroundPaint = paint;
	}

	/**
	 * Returns the shadow paint.
	 * 
	 * @return The shadow paint.
	 */
	public Paint getShadowPaint() {
		return this.shadowPaint;
	}

	/**
	 * Sets the shadow paint.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setShadowPaint(final Paint paint) {
		this.shadowPaint = paint;
	}

	/**
	 * Returns the x-offset for the shadow effect.
	 * 
	 * @return The offset.
	 */
	public double getShadowXOffset() {
		return this.shadowXOffset;
	}

	/**
	 * Sets the x-offset for the shadow effect.
	 * 
	 * @param offset
	 *           the offset (in Java2D units).
	 */
	public void setShadowXOffset(final double offset) {
		this.shadowXOffset = offset;
	}

	/**
	 * Returns the y-offset for the shadow effect.
	 * 
	 * @return The offset.
	 */
	public double getShadowYOffset() {
		return this.shadowYOffset;
	}

	/**
	 * Sets the y-offset for the shadow effect.
	 * 
	 * @param offset
	 *           the offset (in Java2D units).
	 */
	public void setShadowYOffset(final double offset) {
		this.shadowYOffset = offset;
	}

	/**
	 * Returns the text block.
	 * 
	 * @return The text block.
	 */
	public TextBlock getTextBlock() {
		return this.textBlock;
	}

	/**
	 * Sets the text block.
	 * 
	 * @param block
	 *           the block.
	 */
	public void setTextBlock(final TextBlock block) {
		this.textBlock = block;
	}

	/**
	 * Draws the text box.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the x-coordinate.
	 * @param y
	 *           the y-coordinate.
	 * @param anchor
	 *           the anchor point.
	 */
	public void draw(final Graphics2D g2,
							final float x, final float y, final RectangleAnchor anchor) {
		final Size2D d1 = this.textBlock.calculateDimensions(g2);
		final double w = this.interiorGap.getAdjustedWidth(d1.getWidth());
		final double h = this.interiorGap.getAdjustedHeight(d1.getHeight());
		final Size2D d2 = new Size2D(w, h);
		final Rectangle2D bounds = RectangleAnchor.createRectangle(d2, x, y, anchor);

		if (this.shadowPaint != null) {
			final Rectangle2D shadow = new Rectangle2D.Double(
								bounds.getX() + this.shadowXOffset, bounds.getY() + this.shadowYOffset,
								bounds.getWidth(), bounds.getHeight()
								);
			g2.setPaint(this.shadowPaint);
			g2.fill(shadow);
		}
		if (this.backgroundPaint != null) {
			g2.setPaint(this.backgroundPaint);
			g2.fill(bounds);
		}

		if (this.outlinePaint != null && this.outlineStroke != null) {
			g2.setPaint(this.outlinePaint);
			g2.setStroke(this.outlineStroke);
			g2.draw(bounds);
		}

		this.textBlock.draw(
							g2, (float) bounds.getCenterX(), (float) bounds.getCenterY(), TextBlockAnchor.CENTER
							);

	}

	/**
	 * Returns the height of the text box.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @return The height (in Java2D units).
	 */
	public double getHeight(final Graphics2D g2) {
		final Size2D d = this.textBlock.calculateDimensions(g2);
		return this.interiorGap.getAdjustedHeight(d.getHeight());
	}

	/**
	 * Tests this object for equality with an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test against (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj instanceof TextBox) {
			final TextBox tb = (TextBox) obj;
			final boolean b0 = ObjectUtils.equal(this.outlinePaint, tb.outlinePaint);
			final boolean b1 = ObjectUtils.equal(this.outlineStroke, tb.outlineStroke);
			final boolean b2 = ObjectUtils.equal(this.interiorGap, tb.interiorGap);
			final boolean b3 = ObjectUtils.equal(this.backgroundPaint, tb.backgroundPaint);
			final boolean b4 = ObjectUtils.equal(this.shadowPaint, tb.shadowPaint);
			final boolean b5 = (this.shadowXOffset == tb.shadowXOffset);
			final boolean b6 = (this.shadowYOffset == tb.shadowYOffset);
			final boolean b7 = ObjectUtils.equal(this.textBlock, tb.textBlock);
			return b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7;
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
	private void writeObject(final ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		SerialUtilities.writePaint(this.outlinePaint, stream);
		SerialUtilities.writeStroke(this.outlineStroke, stream);
		SerialUtilities.writePaint(this.backgroundPaint, stream);
		SerialUtilities.writePaint(this.shadowPaint, stream);
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
	private void readObject(final ObjectInputStream stream) throws IOException,
																							ClassNotFoundException {
		stream.defaultReadObject();
		this.outlinePaint = SerialUtilities.readPaint(stream);
		this.outlineStroke = SerialUtilities.readStroke(stream);
		this.backgroundPaint = SerialUtilities.readPaint(stream);
		this.shadowPaint = SerialUtilities.readPaint(stream);
	}

}
