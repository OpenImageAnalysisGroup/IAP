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
 * ----------------------
 * RefineryUtilities.java
 * ----------------------
 * (C) Copyright 2000-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Jon Iles;
 * $Id: RefineryUtilities.java,v 1.1 2011-01-31 09:02:22 klukas Exp $
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 26-Nov-2001 : Changed name to SwingRefinery.java to make it obvious that this is not part of
 * the Java APIs (DG);
 * 10-Dec-2001 : Changed name (again) to JRefineryUtilities.java (DG);
 * 28-Feb-2002 : Moved system properties classes into com.jrefinery.ui.about (DG);
 * 19-Apr-2002 : Renamed JRefineryUtilities-->RefineryUtilities. Added drawRotatedString(...)
 * method (DG);
 * 21-May-2002 : Changed frame positioning methods to accept Window parameters, as suggested by
 * Laurence Vanhelsuwe (DG);
 * 27-May-2002 : Added getPointInRectangle method (DG);
 * 26-Jun-2002 : Removed unnecessary imports (DG);
 * 12-Jul-2002 : Added workaround for rotated text (JI);
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-May-2003 : Added a new drawRotatedString() method (DG);
 * 09-May-2003 : Added a drawRotatedShape() method (DG);
 * 10-Jun-2003 : Updated aligned and rotated string methods (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 07-Nov-2003 : Added rotateShape() method (DG);
 * 16-Mar-2004 : Moved rotateShape() method to ShapeUtils.java (DG);
 * 07-Apr-2004 : Modified text bounds calculation with TextUtilities.getTextBounds() (DG);
 * 21-May-2004 : Fixed bug 951870 - precision in drawAlignedString() method (DG);
 */

package org.jfree.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jfree.text.TextUtilities;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

/**
 * A collection of utility methods relating to user interfaces.
 */
public abstract class RefineryUtilities {

	/** A flag that controls whether or not the rotated string workaround is used. */
	private static boolean useDrawRotatedStringWorkaround = true;

	/**
	 * Sets the flag that controls whether or not the rotated string workaround is used.
	 * 
	 * @param use
	 *           the new flag value.
	 */
	public static void setUseDrawRotatedStringWorkaround(final boolean use) {
		useDrawRotatedStringWorkaround = use;
	}

	/** Access to logging facilities. */
	protected static final LogContext logger = Log.createContext(RefineryUtilities.class);

	/**
	 * Positions the specified frame in the middle of the screen.
	 * 
	 * @param frame
	 *           the frame to be centered on the screen.
	 */
	public static void centerFrameOnScreen(final Window frame) {
		positionFrameOnScreen(frame, 0.5, 0.5);
	}

	/**
	 * Positions the specified frame at a relative position in the screen, where 50% is considered
	 * to be the center of the screen.
	 * 
	 * @param frame
	 *           the frame.
	 * @param horizontalPercent
	 *           the relative horizontal position of the frame (0.0 to 1.0,
	 *           where 0.5 is the center of the screen).
	 * @param verticalPercent
	 *           the relative vertical position of the frame (0.0 to 1.0, where
	 *           0.5 is the center of the screen).
	 */
	public static void positionFrameOnScreen(final Window frame,
															final double horizontalPercent,
															final double verticalPercent) {

		final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension f = frame.getSize();
		final int w = Math.max(s.width - f.width, 0);
		final int h = Math.max(s.height - f.height, 0);
		final int x = (int) (horizontalPercent * w);
		final int y = (int) (verticalPercent * h);
		frame.setBounds(x, y, f.width, f.height);

	}

	/**
	 * Positions the specified frame at a random location on the screen while ensuring that the
	 * entire frame is visible (provided that the frame is smaller than the screen).
	 * 
	 * @param frame
	 *           the frame.
	 */
	public static void positionFrameRandomly(final Window frame) {
		positionFrameOnScreen(frame, Math.random(), Math.random());
	}

	/**
	 * Positions the specified dialog within its parent.
	 * 
	 * @param dialog
	 *           the dialog to be positioned on the screen.
	 */
	public static void centerDialogInParent(final Dialog dialog) {
		positionDialogRelativeToParent(dialog, 0.5, 0.5);
	}

	/**
	 * Positions the specified dialog at a position relative to its parent.
	 * 
	 * @param dialog
	 *           the dialog to be positioned.
	 * @param horizontalPercent
	 *           the relative location.
	 * @param verticalPercent
	 *           the relative location.
	 */
	public static void positionDialogRelativeToParent(final Dialog dialog,
																		final double horizontalPercent,
																		final double verticalPercent) {
		final Dimension d = dialog.getSize();
		final Container parent = dialog.getParent();
		final Dimension p = parent.getSize();

		final int baseX = parent.getX() - d.width;
		final int baseY = parent.getY() - d.height;
		final int w = d.width + p.width;
		final int h = d.height + p.height;
		int x = baseX + (int) (horizontalPercent * w);
		int y = baseY + (int) (verticalPercent * h);

		// make sure the dialog fits completely on the screen...
		final Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		x = Math.min(x, (s.width - d.width));
		x = Math.max(x, 0);
		y = Math.min(y, (s.height - d.height));
		y = Math.max(y, 0);

		dialog.setBounds(x, y, d.width, d.height);

	}

	/**
	 * Creates a panel that contains a table based on the specified table model.
	 * 
	 * @param model
	 *           the table model to use when constructing the table.
	 * @return the panel.
	 */
	public static JPanel createTablePanel(final TableModel model) {

		final JPanel panel = new JPanel(new BorderLayout());
		final JTable table = new JTable(model);
		for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
			final TableColumn column = table.getColumnModel().getColumn(columnIndex);
			final Class c = model.getColumnClass(columnIndex);
			if (c.equals(Number.class)) {
				column.setCellRenderer(new NumberCellRenderer());
			}
		}
		panel.add(new JScrollPane(table));
		return panel;

	}

	/**
	 * Creates a label with a specific font.
	 * 
	 * @param text
	 *           the text for the label.
	 * @param font
	 *           the font.
	 * @return the label.
	 */
	public static JLabel createJLabel(final String text, final Font font) {

		final JLabel result = new JLabel(text);
		result.setFont(font);
		return result;

	}

	/**
	 * Creates a label with a specific font and color.
	 * 
	 * @param text
	 *           the text for the label.
	 * @param font
	 *           the font.
	 * @param color
	 *           the color.
	 * @return the label.
	 */
	public static JLabel createJLabel(final String text, final Font font, final Color color) {

		final JLabel result = new JLabel(text);
		result.setFont(font);
		result.setForeground(color);
		return result;

	}

	/**
	 * Creates a {@link JButton}.
	 * 
	 * @param label
	 *           the label.
	 * @param font
	 *           the font.
	 * @return the button.
	 */
	public static JButton createJButton(final String label, final Font font) {

		final JButton result = new JButton(label);
		result.setFont(font);
		return result;

	}

	/**
	 * Returns a shape that represents the bounds of the string after the specified rotation has
	 * been applied.
	 * 
	 * @param text
	 *           the text (<code>null</code> permitted).
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the x coordinate for the anchor point.
	 * @param y
	 *           the y coordinate for the anchor point.
	 * @param textAnchor
	 *           the text anchor.
	 * @param rotationAnchor
	 *           the rotation anchor.
	 * @param angle
	 *           the angle.
	 * @return The bounds (possibly <code>null</code>).
	 */
	public static Shape calculateRotatedStringBounds(final String text,
																		final Graphics2D g2,
																		final float x,
																		final float y,
																		final TextAnchor textAnchor,
																		final TextAnchor rotationAnchor,
																		final double angle) {
		// xx
		if (text == null || text.equals("")) {
			return null;
		}
		final float[] textAdj = deriveTextBoundsAnchorOffsets(g2, text, textAnchor);
		if (logger.isDebugEnabled()) {
			logger.debug("TextBoundsAnchorOffsets = " + textAdj[0] + ", " + textAdj[1]);
		}
		final float[] rotateAdj = deriveRotationAnchorOffsets(g2, text, rotationAnchor);
		if (logger.isDebugEnabled()) {
			logger.debug("RotationAnchorOffsets = " + rotateAdj[0] + ", " + rotateAdj[1]);
		}
		final Shape result = calculateRotatedStringBounds(
							text, g2, x + textAdj[0], y + textAdj[1],
							angle, x + textAdj[0] + rotateAdj[0], y + textAdj[1] + rotateAdj[1]
							);
		return result;

	}

	/**
	 * A utility method for drawing rotated text.
	 * <P>
	 * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the characters on the left).
	 * 
	 * @param text
	 *           the text.
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the x-coordinate.
	 * @param y
	 *           the y-coordinate.
	 * @param angle
	 *           the angle of the (clockwise) rotation (in radians).
	 */
	public static void drawRotatedString(final String text,
														final Graphics2D g2,
														final float x,
														final float y,
														final double angle) {
		drawRotatedString(text, g2, x, y, angle, x, y);
	}

	/**
	 * Draws a string that is aligned by one anchor point and rotated about another anchor point.
	 * 
	 * @param text
	 *           the text.
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the location of the text anchor.
	 * @param y
	 *           the location of the text anchor.
	 * @param textAnchor
	 *           the text anchor.
	 * @param rotationX
	 *           the x-coordinate for the rotation anchor point.
	 * @param rotationY
	 *           the y-coordinate for the rotation anchor point.
	 * @param angle
	 *           the rotation angle.
	 */
	public static void drawRotatedString(final String text,
														final Graphics2D g2,
														final float x,
														final float y,
														final TextAnchor textAnchor,
														final float rotationX,
														final float rotationY,
														final double angle) {

		if (text == null || text.equals("")) {
			return;
		}
		final float[] textAdj = deriveTextBoundsAnchorOffsets(g2, text, textAnchor);
		drawRotatedString(text, g2, x + textAdj[0], y + textAdj[1],
									angle, rotationX, rotationY);

	}

	/**
	 * Draws a string that is aligned by one anchor point and rotated about another anchor point.
	 * 
	 * @param text
	 *           the text.
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the location of the text anchor.
	 * @param y
	 *           the location of the text anchor.
	 * @param textAnchor
	 *           the text anchor.
	 * @param rotationAnchor
	 *           the rotation anchor.
	 * @param angle
	 *           the rotation angle.
	 */
	public static void drawRotatedString(final String text,
														final Graphics2D g2,
														final float x,
														final float y,
														final TextAnchor textAnchor,
														final TextAnchor rotationAnchor,
														final double angle) {

		if (text == null || text.equals("")) {
			return;
		}
		final float[] textAdj = deriveTextBoundsAnchorOffsets(g2, text, textAnchor);
		final float[] rotateAdj = deriveRotationAnchorOffsets(g2, text, rotationAnchor);
		drawRotatedString(text, g2, x + textAdj[0], y + textAdj[1],
									angle, x + textAdj[0] + rotateAdj[0], y + textAdj[1] + rotateAdj[1]);

	}

	/**
	 * A utility method for drawing rotated text.
	 * <P>
	 * A common rotation is -Math.PI/2 which draws text 'vertically' (with the top of the characters on the left).
	 * 
	 * @param text
	 *           the text.
	 * @param g2
	 *           the graphics device.
	 * @param textX
	 *           the x-coordinate for the text (before rotation).
	 * @param textY
	 *           the y-coordinate for the text (before rotation).
	 * @param angle
	 *           the angle of the (clockwise) rotation (in radians).
	 * @param rotateX
	 *           the point about which the text is rotated.
	 * @param rotateY
	 *           the point about which the text is rotated.
	 */
	public static void drawRotatedString(final String text,
														final Graphics2D g2,
														final float textX,
														final float textY,
														final double angle,
														final float rotateX,
														final float rotateY) {

		if ((text == null) || (text.equals(""))) {
			return;
		}

		final AffineTransform saved = g2.getTransform();

		// apply the rotation...
		final AffineTransform rotate = AffineTransform.getRotateInstance(angle, rotateX, rotateY);
		g2.transform(rotate);

		if (useDrawRotatedStringWorkaround) {
			// workaround for JDC bug ID 4312117 and others...
			final TextLayout tl = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
			tl.draw(g2, textX, textY);
		} else {
			// replaces this code...
			g2.drawString(text, textX, textY);
		}
		g2.setTransform(saved);

	}

	/**
	 * Returns a shape that represents the bounds of the string after the specified rotation has
	 * been applied.
	 * 
	 * @param text
	 *           the text (<code>null</code> permitted).
	 * @param g2
	 *           the graphics device.
	 * @param textX
	 *           the x coordinate for the text.
	 * @param textY
	 *           the y coordinate for the text.
	 * @param angle
	 *           the angle.
	 * @param rotateX
	 *           the x coordinate for the rotation point.
	 * @param rotateY
	 *           the y coordinate for the rotation point.
	 * @return The bounds (possibly <code>null</code>).
	 */
	public static Shape calculateRotatedStringBounds(final String text,
																		final Graphics2D g2,
																		final float textX,
																		final float textY,
																		final double angle,
																		final float rotateX,
																		final float rotateY) {

		if ((text == null) || (text.equals(""))) {
			return null;
		}

		final FontMetrics fm = g2.getFontMetrics();
		final Rectangle2D bounds = TextUtilities.getTextBounds(text, g2, fm);

		// translate...
		final AffineTransform translate = AffineTransform.getTranslateInstance(textX, textY);
		final Shape translatedBounds = translate.createTransformedShape(bounds);
		final AffineTransform rotate = AffineTransform.getRotateInstance(angle, rotateX, rotateY);
		final Shape result = rotate.createTransformedShape(translatedBounds);
		return result;

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
	 * @return The offsets.
	 */
	private static float[] deriveTextBoundsAnchorOffsets(final Graphics2D g2, final String text,
																			final TextAnchor anchor) {

		final float[] result = new float[2];
		final FontRenderContext frc = g2.getFontRenderContext();
		final Font f = g2.getFont();
		final FontMetrics fm = g2.getFontMetrics(f);
		final Rectangle2D bounds = TextUtilities.getTextBounds(text, g2, fm);
		final LineMetrics metrics = f.getLineMetrics(text, frc);
		final float ascent = metrics.getAscent();
		final float halfAscent = ascent / 2.0f;
		final float descent = metrics.getDescent();
		final float leading = metrics.getLeading();
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
		result[0] = xAdj;
		result[1] = yAdj;
		return result;

	}

	/**
	 * A utility method that calculates the rotation anchor offsets for a string. These offsets
	 * are relative to the text starting coordinate (BASELINE_LEFT).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param text
	 *           the text.
	 * @param anchor
	 *           the anchor point.
	 * @return The offsets.
	 */
	private static float[] deriveRotationAnchorOffsets(final Graphics2D g2, final String text,
																			final TextAnchor anchor) {

		final float[] result = new float[2];
		final FontRenderContext frc = g2.getFontRenderContext();
		final LineMetrics metrics = g2.getFont().getLineMetrics(text, frc);
		final FontMetrics fm = g2.getFontMetrics();
		final Rectangle2D bounds = TextUtilities.getTextBounds(text, g2, fm);
		final float ascent = metrics.getAscent();
		final float halfAscent = ascent / 2.0f;
		final float descent = metrics.getDescent();
		final float leading = metrics.getLeading();
		float xAdj = 0.0f;
		float yAdj = 0.0f;

		if (anchor == TextAnchor.TOP_LEFT
							|| anchor == TextAnchor.CENTER_LEFT
							|| anchor == TextAnchor.BOTTOM_LEFT
							|| anchor == TextAnchor.BASELINE_LEFT
							|| anchor == TextAnchor.HALF_ASCENT_LEFT) {

			xAdj = 0.0f;

		} else
			if (anchor == TextAnchor.TOP_CENTER
								|| anchor == TextAnchor.CENTER
								|| anchor == TextAnchor.BOTTOM_CENTER
								|| anchor == TextAnchor.BASELINE_CENTER
								|| anchor == TextAnchor.HALF_ASCENT_CENTER) {

				xAdj = (float) bounds.getWidth() / 2.0f;

			} else
				if (anchor == TextAnchor.TOP_RIGHT
									|| anchor == TextAnchor.CENTER_RIGHT
									|| anchor == TextAnchor.BOTTOM_RIGHT
									|| anchor == TextAnchor.BASELINE_RIGHT
									|| anchor == TextAnchor.HALF_ASCENT_RIGHT) {

					xAdj = (float) bounds.getWidth();

				}

		if (anchor == TextAnchor.TOP_LEFT
							|| anchor == TextAnchor.TOP_CENTER
							|| anchor == TextAnchor.TOP_RIGHT) {

			yAdj = descent + leading - (float) bounds.getHeight();

		} else
			if (anchor == TextAnchor.CENTER_LEFT
								|| anchor == TextAnchor.CENTER
								|| anchor == TextAnchor.CENTER_RIGHT) {

				yAdj = descent + leading - (float) (bounds.getHeight() / 2.0);

			} else
				if (anchor == TextAnchor.HALF_ASCENT_LEFT
									|| anchor == TextAnchor.HALF_ASCENT_CENTER
									|| anchor == TextAnchor.HALF_ASCENT_RIGHT) {

					yAdj = -halfAscent;

				} else
					if (anchor == TextAnchor.BASELINE_LEFT
										|| anchor == TextAnchor.BASELINE_CENTER
										|| anchor == TextAnchor.BASELINE_RIGHT) {

						yAdj = 0.0f;

					} else
						if (anchor == TextAnchor.BOTTOM_LEFT
											|| anchor == TextAnchor.BOTTOM_CENTER
											|| anchor == TextAnchor.BOTTOM_RIGHT) {

							yAdj = metrics.getDescent() + metrics.getLeading();

						}
		result[0] = xAdj;
		result[1] = yAdj;
		return result;

	}

	/**
	 * Draws a rotated shape.
	 * 
	 * @param shape
	 *           the shape.
	 * @param g2
	 *           the graphics device.
	 * @param x
	 *           the x coordinate for the rotation point.
	 * @param y
	 *           the y coordinate for the rotation point.
	 * @param angle
	 *           the angle.
	 */
	public static void drawRotatedShape(final Shape shape, final Graphics2D g2,
														final float x, final float y, final double angle) {

		final AffineTransform saved = g2.getTransform();

		final AffineTransform rotate = AffineTransform.getRotateInstance(angle, x, y);
		g2.transform(rotate);
		g2.draw(shape);

		g2.setTransform(saved);

	}

	/**
	 * Returns a point based on (x, y) but constrained to be within the bounds of a given
	 * rectangle.
	 * 
	 * @param x
	 *           the x-coordinate.
	 * @param y
	 *           the y-coordinate.
	 * @param area
	 *           the constraining rectangle.
	 * @return a point within the rectangle.
	 */
	public static Point2D getPointInRectangle(double x, double y, final Rectangle2D area) {

		x = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
		y = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
		return new Point2D.Double(x, y);

	}

	// // DEPRECATED CODE //////////////////////////////////////////////////////////////////////////

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
	 * @deprecated Use TextUtilities.drawAlignedString().
	 */
	public static void drawAlignedString(final String text,
														final Graphics2D g2,
														final float x,
														final float y,
														final TextAnchor anchor) {

		final float[] adjust = deriveTextBoundsAnchorOffsets(g2, text, anchor);
		g2.drawString(text, x + adjust[0], y + adjust[1]);

	}

}
