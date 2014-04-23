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
 * --------------
 * TextTitle.java
 * --------------
 * (C) Copyright 2000-2004, by David Berry and Contributors.
 * Original Author: David Berry;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Nicolas Brodu;
 * $Id: TextTitle.java,v 1.1 2011-01-31 09:03:14 klukas Exp $
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header (DG);
 * 07-Nov-2001 : Separated the JCommon Class Library classes, JFreeChart now requires
 * jcommon.jar (DG);
 * 09-Jan-2002 : Updated Javadoc comments (DG);
 * 07-Feb-2002 : Changed Insets --> Spacer in AbstractTitle.java (DG);
 * 06-Mar-2002 : Updated import statements (DG);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 18-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 28-Oct-2002 : Small modifications while changing JFreeChart class (DG);
 * 13-Mar-2003 : Changed width used for relative spacing to fix bug 703050 (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 15-Jul-2003 : Fixed null pointer exception (DG);
 * 11-Sep-2003 : Implemented Cloneable (NB)
 * 22-Sep-2003 : Added checks for null values and thow nullpointer exceptions (TM);
 * Background paint was not serialized.
 * 07-Oct-2003 : Added fix for exception caused by empty string in title (DG);
 * 29-Oct-2003 : Added workaround for text alignment in PDF output (DG);
 * 03-Feb-2004 : Fixed bug in getPreferredWidth() method (DG);
 * 17-Feb-2004 : Added clone() method and fixed bug in equals() method (DG);
 * 01-Apr-2004 : Changed java.awt.geom.Dimension2D to org.jfree.ui.Size2D because of
 * JDK bug 4976448 which persists on JDK 1.3.1. Also fixed bug in
 * getPreferredHeight() method (DG);
 * 29-Apr-2004 : Fixed bug in getPreferredWidth() method - see bug id 944173 (DG);
 */

package org.jfree.chart.title;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.io.SerialUtilities;
import org.jfree.text.G2TextMeasurer;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBlockAnchor;
import org.jfree.text.TextUtilities;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Size2D;
import org.jfree.ui.Spacer;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.Log;
import org.jfree.util.LogContext;
import org.jfree.util.ObjectUtils;

/**
 * A chart title that displays a text string.
 * 
 * @author David Berry
 */
public class TextTitle extends Title implements Serializable, Cloneable {

	/** The default font. */
	public static final Font DEFAULT_FONT = new Font("SansSerif", Font.BOLD, 12);

	/** The default text color. */
	public static final Paint DEFAULT_TEXT_PAINT = Color.black;

	/** The title text. */
	private String text;

	/** The font used to display the title. */
	private Font font;

	/** The paint used to display the title text. */
	private transient Paint paint;

	/** The background paint. */
	private transient Paint backgroundPaint;

	/** Access to logging facilities. */
	protected static final LogContext logger = Log.createContext(TextTitle.class);

	/**
	 * Creates a new title, using default attributes where necessary.
	 */
	public TextTitle() {
		this("");
	}

	/**
	 * Creates a new title, using default attributes where necessary.
	 * 
	 * @param text
	 *           the title text.
	 */
	public TextTitle(String text) {

		this(text,
							TextTitle.DEFAULT_FONT,
							TextTitle.DEFAULT_TEXT_PAINT,
							Title.DEFAULT_POSITION,
							Title.DEFAULT_HORIZONTAL_ALIGNMENT,
							Title.DEFAULT_VERTICAL_ALIGNMENT,
							Title.DEFAULT_SPACER);

	}

	/**
	 * Creates a new title, using default attributes where necessary.
	 * 
	 * @param text
	 *           the title text.
	 * @param font
	 *           the title font.
	 */
	public TextTitle(String text, Font font) {

		this(text, font,
							TextTitle.DEFAULT_TEXT_PAINT,
							Title.DEFAULT_POSITION,
							Title.DEFAULT_HORIZONTAL_ALIGNMENT,
							Title.DEFAULT_VERTICAL_ALIGNMENT,
							Title.DEFAULT_SPACER);

	}

	/**
	 * Creates a new title, using default attributes where necessary.
	 * 
	 * @param text
	 *           the title text.
	 * @param font
	 *           the title font.
	 * @param paint
	 *           the title color.
	 */
	public TextTitle(String text, Font font, Paint paint) {

		this(text, font, paint,
							Title.DEFAULT_POSITION,
							Title.DEFAULT_HORIZONTAL_ALIGNMENT,
							Title.DEFAULT_VERTICAL_ALIGNMENT,
							Title.DEFAULT_SPACER);

	}

	/**
	 * Creates a new title, using default attributes where necessary.
	 * <P>
	 * For the horizontal alignment, use the constants (LEFT, RIGHT and CENTER) defined in the Title class.
	 * 
	 * @param text
	 *           the title text.
	 * @param font
	 *           the title font.
	 * @param horizontalAlignment
	 *           the horizontal alignment.
	 */
	public TextTitle(String text, Font font, HorizontalAlignment horizontalAlignment) {

		this(text, font,
							TextTitle.DEFAULT_TEXT_PAINT,
							Title.DEFAULT_POSITION,
							horizontalAlignment,
							Title.DEFAULT_VERTICAL_ALIGNMENT,
							Title.DEFAULT_SPACER);

	}

	/**
	 * Creates a new title.
	 * <p>
	 * For the titlePosition, horizontalAlignment and verticalAlignment, use the constants defined in the Title class.
	 * 
	 * @param text
	 *           the text for the title (<code>null</code> not permitted).
	 * @param font
	 *           the font (<code>null</code> not permitted).
	 * @param paint
	 *           the color (<code>null</code> not permitted).
	 * @param position
	 *           the title position (<code>null</code> not permitted).
	 * @param horizontalAlignment
	 *           the horizontal alignment (<code>null</code> not permitted).
	 * @param verticalAlignment
	 *           the vertical alignment (<code>null</code> not permitted).
	 * @param spacer
	 *           the space to leave around the outside of the title.
	 */
	public TextTitle(String text,
							Font font,
							Paint paint,
							RectangleEdge position,
							HorizontalAlignment horizontalAlignment,
							VerticalAlignment verticalAlignment,
							Spacer spacer) {

		super(position, horizontalAlignment, verticalAlignment, spacer);

		if (text == null) {
			throw new NullPointerException("TextTitle(..): Text is null");
		}
		if (font == null) {
			throw new NullPointerException("TextTitle(..): Font is null");
		}
		if (paint == null) {
			throw new NullPointerException("TextTitle(..): Paint is null");
		}
		this.text = text;
		this.font = font;
		this.paint = paint;
		this.backgroundPaint = null;

	}

	/**
	 * Returns the title text.
	 * 
	 * @return the text (never <code>null</code>).
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Sets the title to the specified text and sends a {@link TitleChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param text
	 *           the text (<code>null</code> not permitted).
	 */
	public void setText(String text) {

		if (text == null) {
			throw new NullPointerException("TextTitle.setText(..): Text is null");
		}
		if (!this.text.equals(text)) {
			this.text = text;
			notifyListeners(new TitleChangeEvent(this));
		}

	}

	/**
	 * Returns the font used to display the title string.
	 * 
	 * @return the font (never <code>null</code>).
	 */
	public Font getFont() {
		return this.font;
	}

	/**
	 * Sets the font used to display the title string. Registered listeners are notified that
	 * the title has been modified.
	 * 
	 * @param font
	 *           the new font (<code>null</code> not permitted).
	 */
	public void setFont(Font font) {

		// check argument...
		if (font == null) {
			throw new IllegalArgumentException("TextTitle.setFont(...): null font not permitted.");
		}

		// make the change...
		if (!this.font.equals(font)) {
			this.font = font;
			notifyListeners(new TitleChangeEvent(this));
		}

	}

	/**
	 * Returns the paint used to display the title string.
	 * 
	 * @return the paint (never <code>null</code>).
	 */
	public Paint getPaint() {
		return this.paint;
	}

	/**
	 * Sets the paint used to display the title string. Registered listeners are notified that
	 * the title has been modified.
	 * 
	 * @param paint
	 *           the new paint (<code>null</code> not permitted).
	 */
	public void setPaint(Paint paint) {

		// check argument...
		if (paint == null) {
			throw new IllegalArgumentException(
								"TextTitle.setPaint(...): null paint not permitted.");
		}

		// make the change...
		if (!this.paint.equals(paint)) {
			this.paint = paint;
			notifyListeners(new TitleChangeEvent(this));
		}

	}

	/**
	 * Returns the background paint.
	 * 
	 * @return the paint (possibly <code>null</code>).
	 */
	public Paint getBackgroundPaint() {
		return this.backgroundPaint;
	}

	/**
	 * Sets the background paint and sends a {@link TitleChangeEvent} to all registered listeners.
	 * If you set this attribute to <code>null</code>, no background is painted (which makes the
	 * title background transparent).
	 * 
	 * @param paint
	 *           the background paint (<code>null</code> permitted).
	 */
	public void setBackgroundPaint(Paint paint) {
		this.backgroundPaint = paint;
		notifyListeners(new TitleChangeEvent(this));
	}

	/**
	 * Returns the preferred width of the title. This will only be called when the title
	 * is being drawn at the left or right of a chart.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param height
	 *           the height.
	 * @return the preferred width of the title.
	 */
	public float getPreferredWidth(Graphics2D g2, float height) {
		float result = 0.0f;
		if (this.text != null && !this.text.equals("")) {
			g2.setFont(this.font);
			TextBlock title = TextUtilities.createTextBlock(
								this.text, this.font, this.paint, height, new G2TextMeasurer(g2)
								);
			Size2D d = title.calculateDimensions(g2);
			result = (float) getSpacer().getAdjustedWidth(d.getHeight());
			// use height here because the title
			// is displayed rotated
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Title preferred width = " + result);
		}
		return result;
	}

	/**
	 * Returns the preferred height of the title.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param width
	 *           the width.
	 * @return the preferred height of the title.
	 */
	public float getPreferredHeight(Graphics2D g2, float width) {
		float result = 0.0f;
		if (this.text != null && !this.text.equals("")) {
			g2.setFont(this.font);
			float textWidth = (float) getSpacer().trimWidth(width);
			TextBlock title = TextUtilities.createTextBlock(
								this.text, this.font, this.paint, textWidth, new G2TextMeasurer(g2)
								);
			Size2D d = title.calculateDimensions(g2);
			result = (float) getSpacer().getAdjustedHeight(d.getHeight());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Title preferred height = " + result);
		}
		return result;
	}

	/**
	 * Draws the title on a Java 2D graphics device (such as the screen or a printer).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area allocated for the title.
	 */
	public void draw(Graphics2D g2, Rectangle2D area) {
		if (logger.isDebugEnabled()) {
			logger.debug("Drawing title to area " + area.toString());
		}
		if (this.text.equals("")) {
			return;
		}
		if (this.backgroundPaint != null) {
			g2.setPaint(this.backgroundPaint);
			g2.fill(area);
		}
		RectangleEdge position = getPosition();
		if (position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {
			drawHorizontal(g2, area);
		} else
			if (position == RectangleEdge.LEFT || position == RectangleEdge.RIGHT) {
				drawVertical(g2, area);
			}
	}

	/**
	 * Draws a the title horizontally within the specified area. This method will be called
	 * from the {@link #draw(Graphics2D, Rectangle2D) draw} method.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area for the title.
	 */
	protected void drawHorizontal(Graphics2D g2, Rectangle2D area) {
		Rectangle2D titleArea = (Rectangle2D) area.clone();
		getSpacer().trim(titleArea);
		g2.setFont(this.font);
		g2.setPaint(this.paint);
		TextBlock title = TextUtilities.createTextBlock(
							this.text, this.font, this.paint, (float) titleArea.getWidth(), new G2TextMeasurer(g2)
							);
		TextBlockAnchor anchor = null;
		float x = 0.0f;
		HorizontalAlignment horizontalAlignment = getHorizontalAlignment();
		if (horizontalAlignment == HorizontalAlignment.LEFT) {
			x = (float) titleArea.getX();
			anchor = TextBlockAnchor.TOP_LEFT;
		} else
			if (horizontalAlignment == HorizontalAlignment.RIGHT) {
				x = (float) titleArea.getMaxX();
				anchor = TextBlockAnchor.TOP_RIGHT;
			} else
				if (horizontalAlignment == HorizontalAlignment.CENTER) {
					x = (float) titleArea.getCenterX();
					anchor = TextBlockAnchor.TOP_CENTER;
				}
		float y = 0.0f;
		RectangleEdge position = getPosition();
		if (position == RectangleEdge.TOP) {
			y = (float) titleArea.getY();
		} else
			if (position == RectangleEdge.BOTTOM) {
				y = (float) titleArea.getMaxY();
				if (horizontalAlignment == HorizontalAlignment.LEFT) {
					anchor = TextBlockAnchor.BOTTOM_LEFT;
				} else
					if (horizontalAlignment == HorizontalAlignment.CENTER) {
						anchor = TextBlockAnchor.BOTTOM_CENTER;
					} else
						if (horizontalAlignment == HorizontalAlignment.RIGHT) {
							anchor = TextBlockAnchor.BOTTOM_RIGHT;
						}
			}
		title.draw(g2, x, y, anchor);
	}

	/**
	 * Draws a the title vertically within the specified area. This method will be called
	 * from the {@link #draw(Graphics2D, Rectangle2D) draw} method.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area for the title.
	 */
	protected void drawVertical(Graphics2D g2, Rectangle2D area) {
		Rectangle2D titleArea = (Rectangle2D) area.clone();
		getSpacer().trim(titleArea);
		g2.setFont(this.font);
		g2.setPaint(this.paint);
		TextBlock title = TextUtilities.createTextBlock(
							this.text, this.font, this.paint, (float) titleArea.getHeight(), new G2TextMeasurer(g2)
							);
		TextBlockAnchor anchor = null;
		float y = 0.0f;
		VerticalAlignment verticalAlignment = getVerticalAlignment();
		if (verticalAlignment == VerticalAlignment.TOP) {
			y = (float) titleArea.getY();
			anchor = TextBlockAnchor.TOP_RIGHT;
		} else
			if (verticalAlignment == VerticalAlignment.BOTTOM) {
				y = (float) titleArea.getMaxY();
				anchor = TextBlockAnchor.TOP_LEFT;
			} else
				if (verticalAlignment == VerticalAlignment.CENTER) {
					y = (float) titleArea.getCenterY();
					anchor = TextBlockAnchor.TOP_CENTER;
				}
		float x = 0.0f;
		RectangleEdge position = getPosition();
		if (position == RectangleEdge.LEFT) {
			x = (float) titleArea.getX();
		} else
			if (position == RectangleEdge.RIGHT) {
				x = (float) titleArea.getMaxX();
				if (verticalAlignment == VerticalAlignment.TOP) {
					anchor = TextBlockAnchor.BOTTOM_RIGHT;
				} else
					if (verticalAlignment == VerticalAlignment.CENTER) {
						anchor = TextBlockAnchor.BOTTOM_CENTER;
					} else
						if (verticalAlignment == VerticalAlignment.BOTTOM) {
							anchor = TextBlockAnchor.BOTTOM_LEFT;
						}
			}
		title.draw(g2, x, y, anchor, x, y, -Math.PI / 2.0);
	}

	/**
	 * Tests this title for equality with another object.
	 * 
	 * @param obj
	 *           the object.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (obj instanceof TextTitle) {

			TextTitle t = (TextTitle) obj;
			if (super.equals(obj)) {
				if (!ObjectUtils.equal(this.text, t.text)) {
					return false;
				}
				if (!ObjectUtils.equal(this.font, t.font)) {
					return false;
				}
				if (!ObjectUtils.equal(this.paint, t.paint)) {
					return false;
				}
				if (!ObjectUtils.equal(this.backgroundPaint, t.backgroundPaint)) {
					return false;
				}
				return true;
			}
		}

		return false;

	}

	/**
	 * Returns a hash code.
	 * 
	 * @return a hash code.
	 */
	public int hashCode() {
		int result = super.hashCode();
		result = 29 * result + (this.text != null ? this.text.hashCode() : 0);
		result = 29 * result + (this.font != null ? this.font.hashCode() : 0);
		result = 29 * result + (this.paint != null ? this.paint.hashCode() : 0);
		result = 29 * result + (this.backgroundPaint != null ? this.backgroundPaint.hashCode() : 0);
		return result;
	}

	/**
	 * Returns a clone of this object.
	 * 
	 * @return a clone of this object.
	 * @throws CloneNotSupportedException
	 *            never.
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
		SerialUtilities.writePaint(this.paint, stream);
		SerialUtilities.writePaint(this.backgroundPaint, stream);
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
		this.backgroundPaint = SerialUtilities.readPaint(stream);
	}

}
