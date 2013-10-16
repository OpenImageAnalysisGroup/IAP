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
 * ------------------
 * TextUtilities.java
 * ------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: TextUtilities.java,v 1.1 2011-01-31 09:03:04 klukas Exp $
 * Changes
 * -------
 * 07-Jan-2004 : Version 1 (DG);
 * 24-Mar-2004 : Added 'paint' argument to createTextBlock() method (DG);
 * 07-Apr-2004 : Added getTextBounds() method and useFontMetricsGetStringBounds flag (DG);
 * 08-Apr-2004 : Changed word break iterator to line break iterator in the createTextBlock()
 * method - see bug report 926074 (DG);
 * 03-Sep-2004 : Updated createTextBlock() method to add ellipses when limit is reached (DG);
 */

package org.jfree.text;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.BreakIterator;

import org.jfree.ui.TextAnchor;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

/**
 * Some utility methods for working with text.
 */
public abstract class TextUtilities {

	/** Access to logging facilities. */
	protected static final LogContext logger = Log.createContext(TextUtilities.class);

	/**
	 * Creates a new text block from the given string.
	 * 
	 * @param text
	 *           the text.
	 * @param font
	 *           the font.
	 * @param paint
	 *           the paint.
	 * @param maxWidth
	 *           the maximum width for each line.
	 * @param measurer
	 *           the text measurer.
	 * @return A text block.
	 */
	public static TextBlock createTextBlock(String text,
															Font font,
															Paint paint,
															float maxWidth,
															TextMeasurer measurer) {
		return createTextBlock(text, font, paint, maxWidth, Integer.MAX_VALUE, measurer);
	}

	/**
	 * Creates a new text block from the given string.
	 * 
	 * @param text
	 *           the text.
	 * @param font
	 *           the font.
	 * @param paint
	 *           the paint.
	 * @param maxWidth
	 *           the maximum width for each line.
	 * @param maxLines
	 *           the maximum number of lines.
	 * @param measurer
	 *           the text measurer.
	 * @return A text block.
	 */
	public static TextBlock createTextBlock(String text,
															Font font,
															Paint paint,
															float maxWidth,
															int maxLines,
															TextMeasurer measurer) {
		TextBlock result = new TextBlock();
		BreakIterator iterator = BreakIterator.getLineInstance();
		iterator.setText(text);
		int current = 0;
		int lines = 0;
		int length = text.length();
		while (current < length && lines < maxLines) {
			int next = nextLineBreak(text, current, maxWidth, iterator, measurer);
			if (next == BreakIterator.DONE) {
				result.addLine(text.substring(current), font, paint);
				return result;
			}
			result.addLine(text.substring(current, next), font, paint);
			lines++;
			current = next;
		}
		if (current < length) {
			TextLine lastLine = result.getLastLine();
			TextFragment lastFragment = lastLine.getLastTextFragment();
			String oldStr = lastFragment.getText();
			String newStr = "...";
			if (oldStr.length() > 3) {
				newStr = oldStr.substring(0, oldStr.length() - 3) + "...";
			}

			lastLine.removeFragment(lastFragment);
			TextFragment newFragment = new TextFragment(newStr, lastFragment.getFont(), lastFragment.getPaint());
			lastLine.addFragment(newFragment);
		}
		return result;
	}

	/**
	 * Returns the character index of the next line break.
	 * 
	 * @param text
	 *           the text.
	 * @param start
	 *           the start index.
	 * @param width
	 *           the end index.
	 * @param iterator
	 *           the word break iterator.
	 * @param measurer
	 *           the text measurer.
	 * @return The index of the next line break.
	 */
	private static int nextLineBreak(String text, int start, float width,
													BreakIterator iterator, TextMeasurer measurer) {
		// this method is (loosely) based on code in JFreeReport's TextParagraph class
		int current = start;
		int end;
		float x = 0.0f;
		boolean firstWord = true;
		while (((end = iterator.next()) != BreakIterator.DONE)) {
			x += measurer.getStringWidth(text, current, end);
			if (x > width) {
				if (firstWord) {
					while (measurer.getStringWidth(text, start, end) > width) {
						end--;
					}
					// iterator.setPosition(end);
					return end;
				} else {
					end = iterator.previous();
					return end;
				}
			}
			// we found at least one word that fits ...
			firstWord = false;
			current = end;
		}
		return BreakIterator.DONE;
	}

	/**
	 * Returns the bounds for the specified text.
	 * 
	 * @param text
	 *           the text (<code>null</code> permitted).
	 * @param g2
	 *           the graphics context (not <code>null</code>).
	 * @param fm
	 *           the font metrics (not <code>null</code>).
	 * @return The text bounds (<code>null</code> if the <code>text</code> argument is <code>null</code>).
	 */
	public static Rectangle2D getTextBounds(String text,
															Graphics2D g2, FontMetrics fm) {
		Rectangle2D bounds = null;
		if (TextUtilities.useFontMetricsGetStringBounds) {
			bounds = fm.getStringBounds(text, g2);
		} else {
			double width = fm.stringWidth(text);
			double height = fm.getHeight();
			if (logger.isDebugEnabled()) {
				logger.debug("Height = " + height);
			}
			bounds = new Rectangle2D.Double(0.0, -fm.getAscent(), width, height);
		}
		return bounds;
	}

	/**
	 * Draws a string such that the specified anchor point is aligned to the given (x, y)
	 * location.
	 * 
	 * @param text
	 *           the text.
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the x coordinate (Java 2D).
	 * @param y
	 *           the y coordinate (Java 2D).
	 * @param anchor
	 *           the anchor location.
	 * @return The text bounds (not adjusted for the x and y offsets).
	 */
	public static Rectangle2D drawAlignedString(String text,
																Graphics2D g2,
																float x,
																float y,
																TextAnchor anchor) {

		Rectangle2D textBounds = new Rectangle2D.Double();
		final float[] adjust = deriveTextBoundsAnchorOffsets(g2, text, anchor, textBounds);
		g2.drawString(text, x + adjust[0], y + adjust[1]);
		return textBounds;

	}

	/**
	 * A utility method that calculates the anchor offsets for a string. Normally, the
	 * (x, y) coordinate for drawing text is a point on the baseline at the left of the
	 * text string. If you add these offsets to (x, y) and draw the string, then the
	 * anchor point should coincide with the (x, y) point.
	 * 
	 * @param g2
	 *           the graphics device (not <code>null</code>).
	 * @param text
	 *           the text.
	 * @param anchor
	 *           the anchor point.
	 * @param textBounds
	 *           the text bounds (if not <code>null</code>, this object will be updated
	 *           by this method to match the string bounds).
	 * @return The offsets.
	 */
	private static float[] deriveTextBoundsAnchorOffsets(Graphics2D g2, String text,
																			TextAnchor anchor,
																			Rectangle2D textBounds) {

		float[] result = new float[2];
		FontRenderContext frc = g2.getFontRenderContext();
		Font f = g2.getFont();
		FontMetrics fm = g2.getFontMetrics(f);
		Rectangle2D bounds = TextUtilities.getTextBounds(text, g2, fm);
		LineMetrics metrics = f.getLineMetrics(text, frc);
		float ascent = metrics.getAscent();
		float halfAscent = ascent / 2.0f;
		float descent = metrics.getDescent();
		float leading = metrics.getLeading();
		float xAdj = 0.0f;
		float yAdj = 0.0f;

		if (anchor == TextAnchor.TOP_CENTER
							|| anchor == TextAnchor.CENTER
							|| anchor == TextAnchor.BOTTOM_CENTER
							|| anchor == TextAnchor.BASELINE_CENTER
							|| anchor == TextAnchor.HALF_ASCENT_CENTER) {

			xAdj = (float) -bounds.getWidth() / 2.0f;

		} else
			if (anchor == TextAnchor.TOP_RIGHT
								|| anchor == TextAnchor.CENTER_RIGHT
								|| anchor == TextAnchor.BOTTOM_RIGHT
								|| anchor == TextAnchor.BASELINE_RIGHT
								|| anchor == TextAnchor.HALF_ASCENT_RIGHT) {

				xAdj = (float) -bounds.getWidth();

			}

		if (anchor == TextAnchor.TOP_LEFT
							|| anchor == TextAnchor.TOP_CENTER
							|| anchor == TextAnchor.TOP_RIGHT) {

			yAdj = -descent - leading + (float) bounds.getHeight();

		} else
			if (anchor == TextAnchor.HALF_ASCENT_LEFT
								|| anchor == TextAnchor.HALF_ASCENT_CENTER
								|| anchor == TextAnchor.HALF_ASCENT_RIGHT) {

				yAdj = halfAscent;

			} else
				if (anchor == TextAnchor.CENTER_LEFT
									|| anchor == TextAnchor.CENTER
									|| anchor == TextAnchor.CENTER_RIGHT) {

					yAdj = -descent - leading + (float) (bounds.getHeight() / 2.0);

				} else
					if (anchor == TextAnchor.BASELINE_LEFT
										|| anchor == TextAnchor.BASELINE_CENTER
										|| anchor == TextAnchor.BASELINE_RIGHT) {

						yAdj = 0.0f;

					} else
						if (anchor == TextAnchor.BOTTOM_LEFT
											|| anchor == TextAnchor.BOTTOM_CENTER
											|| anchor == TextAnchor.BOTTOM_RIGHT) {

							yAdj = -metrics.getDescent() - metrics.getLeading();

						}
		if (textBounds != null) {
			textBounds.setRect(bounds);
		}
		result[0] = xAdj;
		result[1] = yAdj;
		return result;

	}

	/**
	 * A flag that controls whether the FontMetrics.getStringBounds() method is used or
	 * a workaround is applied.
	 */
	private static boolean useFontMetricsGetStringBounds = false;

	/**
	 * Returns the flag that controls whether the FontMetrics.getStringBounds() method is used or
	 * not. If you are having trouble with label alignment or positioning, try changing the value
	 * of this flag.
	 * 
	 * @return A boolean.
	 */
	public static boolean getUseFontMetricsGetStringBounds() {
		return useFontMetricsGetStringBounds;
	}

	/**
	 * Sets the flag that controls whether the FontMetrics.getStringBounds() method is used or
	 * not. If you are having trouble with label alignment or positioning, try changing the value
	 * of this flag.
	 * 
	 * @param use
	 *           the flag.
	 */
	public static void setUseFontMetricsGetStringBounds(boolean use) {
		useFontMetricsGetStringBounds = use;
	}

}
