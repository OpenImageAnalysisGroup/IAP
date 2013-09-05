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
 * ---------
 * Axis.java
 * ---------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Bill Kelemen; Nicolas Brodu
 * $Id: Axis.java,v 1.1 2011-01-31 09:01:38 klukas Exp $
 * Changes (from 21-Aug-2001)
 * --------------------------
 * 21-Aug-2001 : Added standard header, fixed DOS encoding problem (DG);
 * 18-Sep-2001 : Updated header (DG);
 * 07-Nov-2001 : Allow null axis labels (DG);
 * : Added default font values (DG);
 * 13-Nov-2001 : Modified the setPlot(...) method to check compatibility between the axis and the
 * plot (DG);
 * 30-Nov-2001 : Changed default font from "Arial" --> "SansSerif" (DG);
 * 06-Dec-2001 : Allow null in setPlot(...) method (BK);
 * 06-Mar-2002 : Added AxisConstants interface (DG);
 * 23-Apr-2002 : Added a visible property. Moved drawVerticalString to RefineryUtilities. Added
 * fixedDimension property for use in combined plots (DG);
 * 25-Jun-2002 : Removed unnecessary imports (DG);
 * 05-Sep-2002 : Added attribute for tick mark paint (DG);
 * 18-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Nov-2002 : Added attributes to control the inside and outside length of the tick marks (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 18-Nov-2002 : Added axis location to refreshTicks(...) parameters (DG);
 * 15-Jan-2003 : Removed monolithic constructor (DG);
 * 17-Jan-2003 : Moved plot classes to separate package (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 03-Jul-2003 : Modified reserveSpace method (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 11-Sep-2003 : Took care of listeners while cloning (NB);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 06-Nov-2003 : Modified refreshTicks(...) signature (DG);
 * 06-Jan-2004 : Added axis line attributes (DG);
 * 16-Mar-2004 : Added plot state to draw() method (DG);
 * 07-Apr-2004 : Modified text bounds calculation (DG);
 * 18-May-2004 : Eliminated AxisConstants.java (DG);
 */

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.Log;
import org.jfree.util.LogContext;
import org.jfree.util.ObjectUtils;

/**
 * The base class for all axes in JFreeChart. Subclasses are divided into those that display
 * values ({@link ValueAxis}) and those that display categories ({@link CategoryAxis}).
 */
public abstract class Axis implements Cloneable, Serializable {

	/** The default axis visibility. */
	public static final boolean DEFAULT_AXIS_VISIBLE = true;

	/** The default axis label font. */
	public static final Font DEFAULT_AXIS_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);

	/** The default axis label paint. */
	public static final Paint DEFAULT_AXIS_LABEL_PAINT = Color.black;

	/** The default axis label insets. */
	public static final Insets DEFAULT_AXIS_LABEL_INSETS = new Insets(3, 3, 3, 3);

	/** The default axis line paint. */
	public static final Paint DEFAULT_AXIS_LINE_PAINT = Color.gray;

	/** The default axis line stroke. */
	public static final Stroke DEFAULT_AXIS_LINE_STROKE = new BasicStroke(1.0f);

	/** The default tick labels visibility. */
	public static final boolean DEFAULT_TICK_LABELS_VISIBLE = true;

	/** The default tick label font. */
	public static final Font DEFAULT_TICK_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);

	/** The default tick label paint. */
	public static final Paint DEFAULT_TICK_LABEL_PAINT = Color.black;

	/** The default tick label insets. */
	public static final Insets DEFAULT_TICK_LABEL_INSETS = new Insets(2, 4, 2, 4);

	/** The default tick marks visible. */
	public static final boolean DEFAULT_TICK_MARKS_VISIBLE = true;

	/** The default tick stroke. */
	public static final Stroke DEFAULT_TICK_MARK_STROKE = new BasicStroke(1);

	/** The default tick paint. */
	public static final Paint DEFAULT_TICK_MARK_PAINT = Color.gray;

	/** The default tick mark inside length. */
	public static final float DEFAULT_TICK_MARK_INSIDE_LENGTH = 0.0f;

	/** The default tick mark outside length. */
	public static final float DEFAULT_TICK_MARK_OUTSIDE_LENGTH = 2.0f;

	/** A flag indicating whether or not the axis is visible. */
	private boolean visible;

	/** The label for the axis. */
	private String label;

	/** The font for displaying the axis label. */
	private Font labelFont;

	/** The paint for drawing the axis label. */
	private transient Paint labelPaint;

	/** The insets for the axis label. */
	private Insets labelInsets;

	/** The label angle. */
	private double labelAngle;

	/** A flag that controls whether or not the axis line is visible. */
	private boolean axisLineVisible;

	/** The stroke used for the axis line. */
	private transient Stroke axisLineStroke;

	/** The paint used for the axis line. */
	private transient Paint axisLinePaint;

	/** A flag that indicates whether or not tick labels are visible for the axis. */
	private boolean tickLabelsVisible;

	/** The font used to display the tick labels. */
	private Font tickLabelFont;

	/** The color used to display the tick labels. */
	private transient Paint tickLabelPaint;

	/** The blank space around each tick label. */
	private Insets tickLabelInsets;

	/** A flag that indicates whether or not tick marks are visible for the axis. */
	private boolean tickMarksVisible;

	/** The length of the tick mark inside the data area (zero permitted). */
	private float tickMarkInsideLength;

	/** The length of the tick mark outside the data area (zero permitted). */
	private float tickMarkOutsideLength;

	/** The stroke used to draw tick marks. */
	private transient Stroke tickMarkStroke;

	/** The paint used to draw tick marks. */
	private transient Paint tickMarkPaint;

	/** The fixed (horizontal or vertical) dimension for the axis. */
	private double fixedDimension;

	/** A reference back to the plot that the axis is assigned to (can be <code>null</code>). */
	private transient Plot plot;

	/** Storage for registered listeners. */
	private transient EventListenerList listenerList;

	/** Access to logging facilities. */
	protected static final LogContext LOGGER = Log.createContext(Axis.class);

	/**
	 * Constructs an axis, using default values where necessary.
	 * 
	 * @param label
	 *           the axis label (<code>null</code> permitted).
	 */
	protected Axis(String label) {

		this.label = label;
		this.visible = DEFAULT_AXIS_VISIBLE;
		this.labelFont = DEFAULT_AXIS_LABEL_FONT;
		this.labelPaint = DEFAULT_AXIS_LABEL_PAINT;
		this.labelInsets = DEFAULT_AXIS_LABEL_INSETS;
		this.labelAngle = 0.0;

		this.axisLineVisible = true;
		this.axisLinePaint = DEFAULT_AXIS_LINE_PAINT;
		this.axisLineStroke = DEFAULT_AXIS_LINE_STROKE;

		this.tickLabelsVisible = DEFAULT_TICK_LABELS_VISIBLE;
		this.tickLabelFont = DEFAULT_TICK_LABEL_FONT;
		this.tickLabelPaint = DEFAULT_TICK_LABEL_PAINT;
		this.tickLabelInsets = DEFAULT_TICK_LABEL_INSETS;

		this.tickMarksVisible = DEFAULT_TICK_MARKS_VISIBLE;
		this.tickMarkStroke = DEFAULT_TICK_MARK_STROKE;
		this.tickMarkPaint = DEFAULT_TICK_MARK_PAINT;
		this.tickMarkInsideLength = DEFAULT_TICK_MARK_INSIDE_LENGTH;
		this.tickMarkOutsideLength = DEFAULT_TICK_MARK_OUTSIDE_LENGTH;

		this.plot = null;

		this.listenerList = new EventListenerList();

	}

	/**
	 * Returns <code>true</code> if the axis is visible, and <code>false</code> otherwise.
	 * 
	 * @return A boolean.
	 */
	public boolean isVisible() {
		return this.visible;
	}

	/**
	 * Sets a flag that controls whether or not the axis is visible and sends an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setVisible(boolean flag) {
		if (flag != this.visible) {
			this.visible = flag;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the label for the axis.
	 * 
	 * @return The label for the axis (<code>null</code> possible).
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * Sets the label for the axis and sends an {@link AxisChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param label
	 *           the new label (<code>null</code> permitted).
	 */
	public void setLabel(String label) {

		String existing = this.label;
		if (existing != null) {
			if (!existing.equals(label)) {
				this.label = label;
				notifyListeners(new AxisChangeEvent(this));
			}
		} else {
			if (label != null) {
				this.label = label;
				notifyListeners(new AxisChangeEvent(this));
			}
		}

	}

	/**
	 * Returns the font for the axis label.
	 * 
	 * @return The font.
	 */
	public Font getLabelFont() {
		return this.labelFont;
	}

	/**
	 * Sets the font for the axis label and sends an {@link AxisChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param font
	 *           the font (<code>null</code> not permitted).
	 */
	public void setLabelFont(Font font) {
		if (font == null) {
			throw new IllegalArgumentException("Null 'font' argument.");
		}
		if (!this.labelFont.equals(font)) {
			this.labelFont = font;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the color/shade used to draw the axis label.
	 * 
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getLabelPaint() {
		return this.labelPaint;
	}

	/**
	 * Sets the paint used to draw the axis label and sends an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setLabelPaint(Paint paint) {
		if (paint == null) {
			throw new IllegalArgumentException("Axis.setLabelPaint(...): null not permitted.");
		}
		this.labelPaint = paint;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the insets for the label (that is, the amount of blank space
	 * that should be left around the label).
	 * 
	 * @return The label insets (never <code>null</code>).
	 */
	public Insets getLabelInsets() {
		return this.labelInsets;
	}

	/**
	 * Sets the insets for the axis label, and sends an {@link AxisChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param insets
	 *           the insets (<code>null</code> not permitted).
	 */
	public void setLabelInsets(Insets insets) {
		if (insets == null) {
			throw new IllegalArgumentException("Null 'insets' argument.");
		}
		if (!insets.equals(this.labelInsets)) {
			this.labelInsets = insets;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the angle of the axis label.
	 * 
	 * @return The angle (in radians).
	 */
	public double getLabelAngle() {
		return this.labelAngle;
	}

	/**
	 * Sets the angle for the label and sends an {@link AxisChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param angle
	 *           the angle (in radians).
	 */
	public void setLabelAngle(double angle) {
		this.labelAngle = angle;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * A flag that controls whether or not the axis line is drawn.
	 * 
	 * @return A boolean.
	 */
	public boolean isAxisLineVisible() {
		return this.axisLineVisible;
	}

	/**
	 * Sets a flag that controls whether or not the axis line is visible and sends an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param visible
	 *           the flag.
	 */
	public void setAxisLineVisible(boolean visible) {
		this.axisLineVisible = visible;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the paint used to draw the axis line.
	 * 
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getAxisLinePaint() {
		return this.axisLinePaint;
	}

	/**
	 * Sets the paint used to draw the axis line and sends an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setAxisLinePaint(Paint paint) {
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		this.axisLinePaint = paint;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the stroke used to draw the axis line.
	 * 
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getAxisLineStroke() {
		return this.axisLineStroke;
	}

	/**
	 * Sets the stroke used to draw the axis line and sends an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 */
	public void setAxisLineStroke(Stroke stroke) {
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}
		this.axisLineStroke = stroke;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns a flag indicating whether or not the tick labels are visible.
	 * 
	 * @return The flag.
	 */
	public boolean isTickLabelsVisible() {
		return this.tickLabelsVisible;
	}

	/**
	 * Sets the flag that determines whether or not the tick labels are visible and sends
	 * an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setTickLabelsVisible(boolean flag) {

		if (flag != this.tickLabelsVisible) {
			this.tickLabelsVisible = flag;
			notifyListeners(new AxisChangeEvent(this));
		}

	}

	/**
	 * Returns the font used for the tick labels (if showing).
	 * 
	 * @return The font (never <code>null</code>).
	 */
	public Font getTickLabelFont() {
		return this.tickLabelFont;
	}

	/**
	 * Sets the font for the tick labels and sends an {@link AxisChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param font
	 *           the font (<code>null</code> not allowed).
	 */
	public void setTickLabelFont(Font font) {

		// check arguments...
		if (font == null) {
			throw new IllegalArgumentException("Null 'font' argument.");
		}

		// apply change if necessary...
		if (!this.tickLabelFont.equals(font)) {
			this.tickLabelFont = font;
			notifyListeners(new AxisChangeEvent(this));
		}

	}

	/**
	 * Returns the color/shade used for the tick labels.
	 * 
	 * @return The paint used for the tick labels.
	 */
	public Paint getTickLabelPaint() {
		return this.tickLabelPaint;
	}

	/**
	 * Sets the paint used to draw tick labels (if they are showing) and sends an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setTickLabelPaint(Paint paint) {
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		this.tickLabelPaint = paint;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the insets for the tick labels.
	 * 
	 * @return The insets (never <code>null</code>).
	 */
	public Insets getTickLabelInsets() {
		return this.tickLabelInsets;
	}

	/**
	 * Sets the insets for the tick labels and sends an {@link AxisChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param insets
	 *           the insets (<code>null</code> not permitted).
	 */
	public void setTickLabelInsets(Insets insets) {
		if (insets == null) {
			throw new IllegalArgumentException("Null 'insets' argument.");
		}
		if (!this.tickLabelInsets.equals(insets)) {
			this.tickLabelInsets = insets;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the flag that indicates whether or not the tick marks are
	 * showing.
	 * 
	 * @return the flag that indicates whether or not the tick marks are showing.
	 */
	public boolean isTickMarksVisible() {
		return this.tickMarksVisible;
	}

	/**
	 * Sets the flag that indicates whether or not the tick marks are showing and sends
	 * an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setTickMarksVisible(boolean flag) {
		if (flag != this.tickMarksVisible) {
			this.tickMarksVisible = flag;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the inside length of the tick marks.
	 * 
	 * @return The length.
	 */
	public float getTickMarkInsideLength() {
		return this.tickMarkInsideLength;
	}

	/**
	 * Sets the inside length of the tick marks and sends
	 * an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param length
	 *           the new length.
	 */
	public void setTickMarkInsideLength(float length) {
		this.tickMarkInsideLength = length;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the outside length of the tick marks.
	 * 
	 * @return The length.
	 */
	public float getTickMarkOutsideLength() {
		return this.tickMarkOutsideLength;
	}

	/**
	 * Sets the outside length of the tick marks and sends
	 * an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param length
	 *           the new length.
	 */
	public void setTickMarkOutsideLength(float length) {
		this.tickMarkOutsideLength = length;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the pen/brush used to draw tick marks (if they are showing).
	 * 
	 * @return The pen/brush used to draw tick marks.
	 */
	public Stroke getTickMarkStroke() {
		return this.tickMarkStroke;
	}

	/**
	 * Sets the pen/brush used to draw tick marks (if they are showing) and sends
	 * an {@link AxisChangeEvent} to all registered listeners.
	 * 
	 * @param stroke
	 *           the new pen/brush (<code>null</code> not permitted).
	 */
	public void setTickMarkStroke(Stroke stroke) {

		// check arguments...
		if (stroke == null) {
			throw new IllegalArgumentException("Axis.setTickMarkStroke(...): null not permitted.");
		}

		// make the change (if necessary)...
		if (!this.tickMarkStroke.equals(stroke)) {
			this.tickMarkStroke = stroke;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the paint used to draw tick marks (if they are showing).
	 * 
	 * @return The paint.
	 */
	public Paint getTickMarkPaint() {
		return this.tickMarkPaint;
	}

	/**
	 * Sets the paint used to draw tick marks (if they are showing).
	 * <P>
	 * Registered listeners are notified of a general change to the axis.
	 * 
	 * @param paint
	 *           the new paint (null not permitted).
	 */
	public void setTickMarkPaint(Paint paint) {

		// check arguments...
		if (paint == null) {
			throw new IllegalArgumentException("Axis.setTickMarkPaint(...): null not permitted.");
		}

		// make the change (if necessary)...
		if (!this.tickMarkPaint.equals(paint)) {
			this.tickMarkPaint = paint;
			notifyListeners(new AxisChangeEvent(this));
		}
	}

	/**
	 * Returns the plot that the axis is assigned to.
	 * <P>
	 * This method will return null if the axis is not currently assigned to a plot.
	 * 
	 * @return The plot that the axis is assigned to.
	 */
	public Plot getPlot() {
		return this.plot;
	}

	/**
	 * Sets a reference to the plot that the axis is assigned to.
	 * <P>
	 * This method is used internally, you shouldn't need to call it yourself.
	 * 
	 * @param plot
	 *           the plot.
	 */
	public void setPlot(Plot plot) {
		this.plot = plot;
		configure();
	}

	/**
	 * Returns the fixed dimension for the axis.
	 * 
	 * @return The fixed dimension.
	 */
	public double getFixedDimension() {
		return this.fixedDimension;
	}

	/**
	 * Sets the fixed dimension for the axis.
	 * <P>
	 * This is used when combining more than one plot on a chart. In this case, there may be several axes that need to have the same height or width so that they
	 * are aligned. This method is used to fix a dimension for the axis (the context determines whether the dimension is horizontal or vertical).
	 * 
	 * @param dimension
	 *           the fixed dimension.
	 */
	public void setFixedDimension(double dimension) {
		this.fixedDimension = dimension;
	}

	/**
	 * Configures the axis to work with the current plot. Override this method
	 * to perform any special processing (such as auto-rescaling).
	 */
	public abstract void configure();

	/**
	 * Estimates the space (height or width) required to draw the axis.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plot
	 *           the plot that the axis belongs to.
	 * @param plotArea
	 *           the area within which the plot (including axes) should be drawn.
	 * @param edge
	 *           the axis location.
	 * @param space
	 *           space already reserved.
	 * @return The space required to draw the axis (including pre-reserved space).
	 */
	public abstract AxisSpace reserveSpace(Graphics2D g2, Plot plot,
															Rectangle2D plotArea, RectangleEdge edge,
															AxisSpace space, boolean isOpositeAxisVisible);

	/**
	 * Draws the axis on a Java 2D graphics device (such as the screen or a printer).
	 * 
	 * @param g2
	 *           the graphics device (<code>null</code> not permitted).
	 * @param cursor
	 *           the cursor location (determines where to draw the axis).
	 * @param plotArea
	 *           the area within which the axes and plot should be drawn.
	 * @param dataArea
	 *           the area within which the data should be drawn.
	 * @param edge
	 *           the axis location (<code>null</code> not permitted).
	 * @param plotState
	 *           collects information about the plot (<code>null</code> permitted).
	 * @return the axis state (never <code>null</code>).
	 */
	public abstract AxisState draw(Graphics2D g2,
												double cursor,
												Rectangle2D plotArea,
												Rectangle2D dataArea,
												RectangleEdge edge,
												PlotRenderingInfo plotState);

	/**
	 * Calculates the positions of the ticks for the axis, storing the results
	 * in the tick list (ready for drawing).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the axis state.
	 * @param plotArea
	 *           the area within which the axes and plot should be drawn.
	 * @param dataArea
	 *           the area inside the axes.
	 * @param edge
	 *           the edge on which the axis is located.
	 * @return The list of ticks.
	 */
	public abstract List refreshTicks(Graphics2D g2,
													AxisState state,
													Rectangle2D plotArea,
													Rectangle2D dataArea,
													RectangleEdge edge);

	/**
	 * Registers an object for notification of changes to the axis.
	 * 
	 * @param listener
	 *           the object that is being registered.
	 */
	public void addChangeListener(AxisChangeListener listener) {
		this.listenerList.add(AxisChangeListener.class, listener);
	}

	/**
	 * Deregisters an object for notification of changes to the axis.
	 * 
	 * @param listener
	 *           the object to deregister.
	 */
	public void removeChangeListener(AxisChangeListener listener) {
		this.listenerList.remove(AxisChangeListener.class, listener);
	}

	/**
	 * Notifies all registered listeners that the axis has changed.
	 * The AxisChangeEvent provides information about the change.
	 * 
	 * @param event
	 *           information about the change to the axis.
	 */
	protected void notifyListeners(AxisChangeEvent event) {

		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == AxisChangeListener.class) {
				((AxisChangeListener) listeners[i + 1]).axisChanged(event);
			}
		}

	}

	/**
	 * Returns a rectangle that encloses the axis label. This is typically used for layout
	 * purposes (it gives the maximum dimensions of the label).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param edge
	 *           the edge of the plot area along which the axis is measuring.
	 * @return The enclosing rectangle.
	 */
	protected Rectangle2D getLabelEnclosure(Graphics2D g2, RectangleEdge edge) {

		// calculate the width of the axis label...
		Rectangle2D result = new Rectangle2D.Double();
		String axisLabel = getLabel();
		if (axisLabel != null) {
			FontMetrics fm = g2.getFontMetrics(getLabelFont());
			Rectangle2D bounds = TextUtilities.getTextBounds(axisLabel, g2, fm);
			Insets insets = getLabelInsets();
			bounds.setRect(bounds.getX(), bounds.getY(),
									bounds.getWidth() + insets.left + insets.right,
									bounds.getHeight() + insets.top + insets.bottom);
			double angle = getLabelAngle();
			if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
				angle = angle - Math.PI / 2.0;
			}
			double x = bounds.getCenterX();
			double y = bounds.getCenterY();
			AffineTransform transformer = AffineTransform.getRotateInstance(angle, x, y);
			Shape labelBounds = transformer.createTransformedShape(bounds);
			result = labelBounds.getBounds2D();
		}

		return result;

	}

	/**
	 * Draws the axis label.
	 * 
	 * @param label
	 *           the label text.
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the plot area.
	 * @param dataArea
	 *           the area inside the axes.
	 * @param edge
	 *           the location of the axis.
	 * @param state
	 *           the axis state (<code>null</code> not permitted).
	 * @return Information about the axis.
	 */
	protected AxisState drawLabel(String label,
												Graphics2D g2,
												Rectangle2D plotArea,
												Rectangle2D dataArea,
												RectangleEdge edge,
												AxisState state) {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Entering drawLabel() method, cursor = " + state.getCursor());
		}

		// it is unlikely that 'state' will be null, but check anyway...
		if (state == null) {
			throw new IllegalArgumentException("Axis.drawLabel: null state not permitted.");
		}

		if ((label == null) || (label.equals(""))) {
			return state;
		}

		Font font = getLabelFont();
		Insets insets = getLabelInsets();
		g2.setFont(font);
		g2.setPaint(getLabelPaint());
		FontMetrics fm = g2.getFontMetrics();
		Rectangle2D labelBounds = TextUtilities.getTextBounds(label, g2, fm);

		if (edge == RectangleEdge.TOP) {

			AffineTransform t = AffineTransform.getRotateInstance(
								getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY()
								);
			Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
			labelBounds = rotatedLabelBounds.getBounds2D();
			double labelx = dataArea.getCenterX();
			double labely = state.getCursor() - insets.bottom - labelBounds.getHeight() / 2.0;
			RefineryUtilities.drawRotatedString(
								label, g2, (float) labelx, (float) labely,
								TextAnchor.CENTER, TextAnchor.CENTER, getLabelAngle()
								);
			state.cursorUp(insets.top + labelBounds.getHeight() + insets.bottom);

		} else
			if (edge == RectangleEdge.BOTTOM) {

				AffineTransform t = AffineTransform.getRotateInstance(
									getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY()
									);
				Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
				labelBounds = rotatedLabelBounds.getBounds2D();
				double labelx = dataArea.getCenterX();
				double labely = state.getCursor() + insets.top + labelBounds.getHeight() / 2.0;
				RefineryUtilities.drawRotatedString(
									label, g2, (float) labelx, (float) labely,
									TextAnchor.CENTER, TextAnchor.CENTER, getLabelAngle()
									);
				state.cursorDown(insets.top + labelBounds.getHeight() + insets.bottom);

			} else
				if (edge == RectangleEdge.LEFT) {

					AffineTransform t = AffineTransform.getRotateInstance(
										getLabelAngle() - Math.PI / 2.0,
										labelBounds.getCenterX(), labelBounds.getCenterY()
										);
					Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
					labelBounds = rotatedLabelBounds.getBounds2D();
					double labelx = state.getCursor() - insets.right - labelBounds.getWidth() / 2.0;
					double labely = dataArea.getCenterY();
					RefineryUtilities.drawRotatedString(
										label, g2, (float) labelx, (float) labely, TextAnchor.CENTER,
										TextAnchor.CENTER, getLabelAngle() - Math.PI / 2.0
										);
					state.cursorLeft(insets.left + labelBounds.getWidth() + insets.right);
				} else
					if (edge == RectangleEdge.RIGHT) {

						AffineTransform t = AffineTransform.getRotateInstance(
											getLabelAngle() + Math.PI / 2.0, labelBounds.getCenterX(), labelBounds.getCenterY()
											);
						Shape rotatedLabelBounds = t.createTransformedShape(labelBounds);
						labelBounds = rotatedLabelBounds.getBounds2D();
						double labelx = state.getCursor() + insets.left + labelBounds.getWidth() / 2.0;
						double labely = dataArea.getY() + dataArea.getHeight() / 2.0;
						RefineryUtilities.drawRotatedString(
											label, g2, (float) labelx, (float) labely,
											TextAnchor.CENTER, TextAnchor.CENTER,
											getLabelAngle() + Math.PI / 2.0
											);
						state.cursorRight(insets.left + labelBounds.getWidth() + insets.right);

					}

		return state;

	}

	/**
	 * Draws an axis line at the current cursor position and edge.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param cursor
	 *           the cursor position.
	 * @param dataArea
	 *           the data area.
	 * @param edge
	 *           the edge.
	 */
	protected void drawAxisLine(Graphics2D g2, double cursor,
						Rectangle2D dataArea, RectangleEdge edge) {

		Line2D axisLine = null;
		if (edge == RectangleEdge.TOP) {
			axisLine = new Line2D.Double(dataArea.getX(), cursor, dataArea.getMaxX(), cursor);
		} else
			if (edge == RectangleEdge.BOTTOM) {
				axisLine = new Line2D.Double(dataArea.getX(), cursor, dataArea.getMaxX(), cursor);
			} else
				if (edge == RectangleEdge.LEFT) {
					axisLine = new Line2D.Double(cursor, dataArea.getY(), cursor, dataArea.getMaxY());
				} else
					if (edge == RectangleEdge.RIGHT) {
						axisLine = new Line2D.Double(cursor, dataArea.getY(), cursor, dataArea.getMaxY());
					}
		g2.setPaint(this.axisLinePaint);
		g2.setStroke(this.axisLineStroke);
		g2.draw(axisLine);

	}

	/**
	 * Returns a clone of the axis.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if some component of the axis does not support cloning.
	 */
	public Object clone() throws CloneNotSupportedException {

		Axis clone = (Axis) super.clone();

		// boolean visible;
		// String label;
		// Font labelFont
		// Paint labelPaint;
		if (this.labelInsets != null) {
			clone.labelInsets = (Insets) this.labelInsets.clone();
		}
		// double labelAngle;
		// boolean tickLabelsVisible;
		// Font tickLabelFont;
		// Paint tickLabelPaint;
		if (this.tickLabelInsets != null) {
			clone.tickLabelInsets = (Insets) this.tickLabelInsets.clone();
		}
		// boolean tickMarksVisible;
		// float tickMarkInsideLength;
		// float tickMarkOutsideLength;
		// Stroke tickMarkStroke;
		// Paint tickMarkPaint;
		// List ticks;
		// double fixedDimension;

		// It's up to the plot which clones up to restore the correct references
		clone.plot = null;
		clone.listenerList = new EventListenerList();

		return clone;

	}

	/**
	 * Tests this axis for equality with another object.
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

		if (obj instanceof Axis) {
			Axis axis = (Axis) obj;

			boolean b0 = (this.visible == axis.visible);

			boolean b1 = ObjectUtils.equal(this.label, axis.label);
			boolean b2 = ObjectUtils.equal(this.labelFont, axis.labelFont);
			boolean b3 = ObjectUtils.equal(this.labelPaint, axis.labelPaint);
			boolean b4 = ObjectUtils.equal(this.labelInsets, axis.labelInsets);
			boolean b5 = (Math.abs(this.labelAngle - axis.labelAngle) < 0.0000001);

			boolean b6 = (this.axisLineVisible == axis.axisLineVisible);
			boolean b7 = ObjectUtils.equal(this.axisLineStroke, axis.axisLineStroke);
			boolean b8 = ObjectUtils.equal(this.axisLinePaint, axis.axisLinePaint);

			boolean b9 = (this.tickLabelsVisible == axis.tickLabelsVisible);
			boolean b10 = ObjectUtils.equal(this.tickLabelFont, axis.tickLabelFont);
			boolean b11 = ObjectUtils.equal(this.tickLabelPaint, axis.tickLabelPaint);
			boolean b12 = ObjectUtils.equal(this.tickLabelInsets, axis.tickLabelInsets);

			boolean b13 = (this.tickMarksVisible == axis.tickMarksVisible);
			boolean b14 = (Math.abs(this.tickMarkInsideLength - axis.tickMarkInsideLength)
									< 0.000001);
			boolean b15 = (Math.abs(this.tickMarkOutsideLength - axis.tickMarkOutsideLength)
									< 0.000001);

			boolean b16 = ObjectUtils.equal(this.tickMarkPaint, axis.tickMarkPaint);
			boolean b17 = ObjectUtils.equal(this.tickMarkStroke, axis.tickMarkStroke);

			boolean b18 = (Math.abs(this.fixedDimension - axis.fixedDimension) < 0.000001);

			return b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8
								&& b9 && b10 && b11 && b12 && b13 && b14 && b15 && b16 && b17 && b18;

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
		SerialUtilities.writePaint(this.labelPaint, stream);
		SerialUtilities.writePaint(this.tickLabelPaint, stream);
		SerialUtilities.writeStroke(this.axisLineStroke, stream);
		SerialUtilities.writePaint(this.axisLinePaint, stream);
		SerialUtilities.writeStroke(this.tickMarkStroke, stream);
		SerialUtilities.writePaint(this.tickMarkPaint, stream);

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
		this.labelPaint = SerialUtilities.readPaint(stream);
		this.tickLabelPaint = SerialUtilities.readPaint(stream);
		this.axisLineStroke = SerialUtilities.readStroke(stream);
		this.axisLinePaint = SerialUtilities.readPaint(stream);
		this.tickMarkStroke = SerialUtilities.readStroke(stream);
		this.tickMarkPaint = SerialUtilities.readPaint(stream);
		this.listenerList = new EventListenerList();

	}

}
