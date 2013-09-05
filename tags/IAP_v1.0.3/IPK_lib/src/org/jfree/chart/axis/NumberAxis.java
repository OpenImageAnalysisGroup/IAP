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
 * NumberAxis.java
 * ---------------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Laurence Vanhelsuwe;
 * $Id: NumberAxis.java,v 1.1 2011-01-31 09:01:39 klukas Exp $
 * Changes (from 18-Sep-2001)
 * --------------------------
 * 18-Sep-2001 : Added standard header and fixed DOS encoding problem (DG);
 * 22-Sep-2001 : Changed setMinimumAxisValue(...) and setMaximumAxisValue(...) so that they
 * clear the autoRange flag (DG);
 * 27-Nov-2001 : Removed old, redundant code (DG);
 * 30-Nov-2001 : Added accessor methods for the standard tick units (DG);
 * 08-Jan-2002 : Added setAxisRange(...) method (since renamed setRange(...)) (DG);
 * 16-Jan-2002 : Added setTickUnit(...) method. Extended ValueAxis to support an optional
 * cross-hair (DG);
 * 08-Feb-2002 : Fixes bug to ensure the autorange is recalculated if the
 * setAutoRangeIncludesZero flag is changed (DG);
 * 25-Feb-2002 : Added a new flag autoRangeStickyZero to provide further control over margins in
 * the auto-range mechanism. Updated constructors. Updated import statements.
 * Moved the createStandardTickUnits() method to the TickUnits class (DG);
 * 19-Apr-2002 : Updated Javadoc comments (DG);
 * 01-May-2002 : Updated for changes to TickUnit class, removed valueToString(...) method (DG);
 * 25-Jul-2002 : Moved the lower and upper margin attributes, and the auto-range minimum size, up
 * one level to the ValueAxis class (DG);
 * 05-Sep-2002 : Updated constructor to match changes in Axis class (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 04-Oct-2002 : Moved standardTickUnits from NumberAxis --> ValueAxis (DG);
 * 24-Oct-2002 : Added a number format override (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 19-Nov-2002 : Removed grid settings (now controlled by the plot) (DG);
 * 14-Jan-2003 : Changed autoRangeMinimumSize from Number --> double, and moved crosshair settings
 * to the plot classes (DG);
 * 20-Jan-2003 : Removed the monolithic constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 16-Jul-2003 : Reworked to allow for multiple secondary axes (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 07-Oct-2003 : Fixed bug (815028) in the auto range calculation (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 07-Nov-2003 : Modified to use NumberTick class (DG);
 * 21-Jan-2004 : Renamed translateJava2DToValue --> java2DToValue, and translateValueToJava2D -->
 * valueToJava2D (DG);
 * 03-Mar-2004 : Added plotState to draw() method (DG);
 * 07-Apr-2004 : Changed string width calculation (DG);
 */

package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ObjectUtils;

/**
 * An axis for displaying numerical data.
 * <P>
 * If the axis is set up to automatically determine its range to fit the data, you can ensure that the range includes zero (statisticians usually prefer this)
 * by setting the <code>autoRangeIncludesZero</code> flag to <code>true</code>.
 * <P>
 * The <code>NumberAxis</code> class has a mechanism for automatically selecting a tick unit that is appropriate for the current axis range. This mechanism is
 * an adaptation of code suggested by Laurence Vanhelsuwe.
 */
public class NumberAxis extends ValueAxis implements Cloneable, Serializable {

	/** The default value for the autoRangeIncludesZero flag. */
	public static final boolean DEFAULT_AUTO_RANGE_INCLUDES_ZERO = true;

	/** The default value for the autoRangeStickyZero flag. */
	public static final boolean DEFAULT_AUTO_RANGE_STICKY_ZERO = true;

	/** The default tick unit. */
	public static final NumberTickUnit DEFAULT_TICK_UNIT = new NumberTickUnit(1.0, new DecimalFormat("0"));

	/** The default setting for the vertical tick labels flag. */
	public static final boolean DEFAULT_VERTICAL_TICK_LABELS = false;

	/**
	 * A flag that affects the axis range when the range is determined
	 * automatically. If the auto range does NOT include zero and this flag
	 * is TRUE, then the range is changed to include zero.
	 */
	private boolean autoRangeIncludesZero;

	/**
	 * A flag that affects the size of the margins added to the axis range when
	 * the range is determined automatically. If the value 0 falls within the
	 * margin and this flag is TRUE, then the margin is truncated at zero.
	 */
	private boolean autoRangeStickyZero;

	/** The tick unit for the axis. */
	private NumberTickUnit tickUnit;

	/** The override number format. */
	private NumberFormat numberFormatOverride;

	/** An optional band for marking regions on the axis. */
	private MarkerAxisBand markerBand;

	/**
	 * Default constructor.
	 */
	public NumberAxis() {
		this(null);
	}

	/**
	 * Constructs a number axis, using default values where necessary.
	 * 
	 * @param label
	 *           the axis label (<code>null</code> permitted).
	 */
	public NumberAxis(String label) {

		super(label, NumberAxis.createStandardTickUnits());

		this.autoRangeIncludesZero = DEFAULT_AUTO_RANGE_INCLUDES_ZERO;
		this.autoRangeStickyZero = DEFAULT_AUTO_RANGE_STICKY_ZERO;
		this.tickUnit = DEFAULT_TICK_UNIT;
		this.numberFormatOverride = null;

		this.markerBand = null;

	}

	/**
	 * Returns the flag that indicates whether or not the automatic axis range
	 * (if indeed it is determined automatically) is forced to include zero.
	 * 
	 * @return The flag.
	 */
	public boolean autoRangeIncludesZero() {
		return this.autoRangeIncludesZero;
	}

	/**
	 * Sets the flag that indicates whether or not the axis range, if automatically calculated, is
	 * forced to include zero.
	 * <p>
	 * If the flag is changed to <code>true</code>, the axis range is recalculated.
	 * <p>
	 * Any change to the flag will trigger an {@link AxisChangeEvent}.
	 * 
	 * @param flag
	 *           the new value of the flag.
	 */
	public void setAutoRangeIncludesZero(boolean flag) {

		if (this.autoRangeIncludesZero != flag) {

			this.autoRangeIncludesZero = flag;
			if (isAutoRange()) {
				autoAdjustRange();
			}
			notifyListeners(new AxisChangeEvent(this));

		}

	}

	/**
	 * Returns a flag that affects the auto-range when zero falls outside the
	 * data range but inside the margins defined for the axis.
	 * 
	 * @return The flag.
	 */
	public boolean autoRangeStickyZero() {
		return this.autoRangeStickyZero;
	}

	/**
	 * Sets a flag that affects the auto-range when zero falls outside the data
	 * range but inside the margins defined for the axis.
	 * 
	 * @param flag
	 *           the new flag.
	 */
	public void setAutoRangeStickyZero(boolean flag) {

		if (this.autoRangeStickyZero != flag) {

			this.autoRangeStickyZero = flag;
			if (isAutoRange()) {
				autoAdjustRange();
			}
			notifyListeners(new AxisChangeEvent(this));

		}

	}

	/**
	 * Returns the tick unit for the axis.
	 * 
	 * @return The tick unit for the axis.
	 */
	public NumberTickUnit getTickUnit() {
		return this.tickUnit;
	}

	/**
	 * Sets the tick unit for the axis and sends an {@link AxisChangeEvent} to all registered
	 * listeners. A side effect of calling this method is that the "auto-select" feature for
	 * tick units is switched off (you can restore it using the {@link ValueAxis#setAutoTickUnitSelection(boolean)} method).
	 * 
	 * @param unit
	 *           the new tick unit (<code>null</code> not permitted).
	 */
	public void setTickUnit(NumberTickUnit unit) {
		// defer argument checking...
		setTickUnit(unit, true, true);
	}

	/**
	 * Sets the tick unit for the axis and, if requested, sends an {@link AxisChangeEvent} to all
	 * registered listeners. In addition, an option is provided to turn off the "auto-select"
	 * feature for tick units (you can restore it using the {@link ValueAxis#setAutoTickUnitSelection(boolean)} method).
	 * 
	 * @param unit
	 *           the new tick unit (<code>null</code> not permitted).
	 * @param notify
	 *           notify listeners?
	 * @param turnOffAutoSelect
	 *           turn off the auto-tick selection?
	 */
	public void setTickUnit(NumberTickUnit unit, boolean notify, boolean turnOffAutoSelect) {

		if (unit == null) {
			throw new IllegalArgumentException("Null 'unit' argument.");
		}

		this.tickUnit = unit;
		if (turnOffAutoSelect) {
			setAutoTickUnitSelection(false, false);
		}
		if (notify) {
			notifyListeners(new AxisChangeEvent(this));
		}

	}

	/**
	 * Returns the number format override. If this is non-null, then it will be used to format
	 * the numbers on the axis.
	 * 
	 * @return the number formatter (possibly <code>null</code>).
	 */
	public NumberFormat getNumberFormatOverride() {
		return this.numberFormatOverride;
	}

	/**
	 * Sets the number format override. If this is non-null, then it will be used to format
	 * the numbers on the axis.
	 * 
	 * @param formatter
	 *           the number formatter (<code>null</code> permitted).
	 */
	public void setNumberFormatOverride(NumberFormat formatter) {
		this.numberFormatOverride = formatter;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Returns the (optional) marker band for the axis.
	 * 
	 * @return The marker band (possibly <code>null</code>).
	 */
	public MarkerAxisBand getMarkerBand() {
		return this.markerBand;
	}

	/**
	 * Sets the marker band for the axis.
	 * <P>
	 * The marker band is optional, leave it set to <code>null</code> if you don't require it.
	 * 
	 * @param band
	 *           the new band (<code>null<code> permitted).
	 */
	public void setMarkerBand(MarkerAxisBand band) {
		this.markerBand = band;
		notifyListeners(new AxisChangeEvent(this));
	}

	/**
	 * Configures the axis to work with the specified plot. If the axis has
	 * auto-scaling, then sets the maximum and minimum values.
	 */
	public void configure() {
		if (isAutoRange()) {
			autoAdjustRange();
		}
	}

	/**
	 * Rescales the axis to ensure that all data is visible.
	 */
	protected void autoAdjustRange() {

		Plot plot = getPlot();
		if (plot == null) {
			return; // no plot, no data
		}

		if (plot instanceof ValueAxisPlot) {
			ValueAxisPlot vap = (ValueAxisPlot) plot;

			Range r = vap.getDataRange(this);
			if (r == null) {
				r = new Range(DEFAULT_LOWER_BOUND, DEFAULT_UPPER_BOUND);
			}

			double upper = r.getUpperBound();
			double lower = r.getLowerBound();
			if (autoRangeIncludesZero()) {
				lower = Math.min(lower, 0.0);
				upper = Math.max(upper, 0.0);
			}
			double range = upper - lower;

			// if fixed auto range, then derive lower bound...
			double fixedAutoRange = getFixedAutoRange();
			if (fixedAutoRange > 0.0) {
				lower = upper - fixedAutoRange;
			} else {
				// ensure the autorange is at least <minRange> in size...
				double minRange = getAutoRangeMinimumSize();
				if (range < minRange) {
					double expand = (minRange - range) / 2;
					upper = upper + expand;
					lower = lower - expand;
				}

				if (autoRangeStickyZero()) {
					if (upper <= 0.0) {
						upper = Math.min(0.0, upper + getUpperMargin() * range);
					} else {
						upper = upper + getUpperMargin() * range;
					}
					if (lower >= 0.0) {
						lower = Math.max(0.0, lower - getLowerMargin() * range);
					} else {
						lower = lower - getLowerMargin() * range;
					}
				} else {
					upper = upper + getUpperMargin() * range;
					lower = lower - getLowerMargin() * range;
				}
			}

			setRange(new Range(lower, upper), false, false);
		}

	}

	/**
	 * Converts a data value to a coordinate in Java2D space, assuming that the
	 * axis runs along one edge of the specified dataArea.
	 * <p>
	 * Note that it is possible for the coordinate to fall outside the plotArea.
	 * 
	 * @param value
	 *           the data value.
	 * @param area
	 *           the area for plotting the data.
	 * @param edge
	 *           the axis location.
	 * @return The Java2D coordinate.
	 * @deprecated Use valueToJava2D() instead.
	 */
	public double translateValueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {
		return valueToJava2D(value, area, edge);
	}

	/**
	 * Converts a data value to a coordinate in Java2D space, assuming that the
	 * axis runs along one edge of the specified dataArea.
	 * <p>
	 * Note that it is possible for the coordinate to fall outside the plotArea.
	 * 
	 * @param value
	 *           the data value.
	 * @param area
	 *           the area for plotting the data.
	 * @param edge
	 *           the axis location.
	 * @return The Java2D coordinate.
	 */
	public double valueToJava2D(double value, Rectangle2D area, RectangleEdge edge) {

		Range range = getRange();
		double axisMin = range.getLowerBound();
		double axisMax = range.getUpperBound();

		double min = 0.0;
		double max = 0.0;
		if (RectangleEdge.isTopOrBottom(edge)) {
			min = area.getX();
			max = area.getMaxX();
		} else
			if (RectangleEdge.isLeftOrRight(edge)) {
				max = area.getMinY();
				min = area.getMaxY();
			}
		if (isInverted()) {
			return max - ((value - axisMin) / (axisMax - axisMin)) * (max - min);
		} else {
			return min + ((value - axisMin) / (axisMax - axisMin)) * (max - min);
		}

	}

	/**
	 * Converts a coordinate in Java2D space to the corresponding data value,
	 * assuming that the axis runs along one edge of the specified dataArea.
	 * 
	 * @param java2DValue
	 *           the coordinate in Java2D space.
	 * @param area
	 *           the area in which the data is plotted.
	 * @param edge
	 *           the location.
	 * @return The data value.
	 * @deprecated Use java2DToValue() instead.
	 */
	public double translateJava2DToValue(double java2DValue, Rectangle2D area, RectangleEdge edge) {
		return java2DToValue(java2DValue, area, edge);
	}

	/**
	 * Converts a coordinate in Java2D space to the corresponding data value,
	 * assuming that the axis runs along one edge of the specified dataArea.
	 * 
	 * @param java2DValue
	 *           the coordinate in Java2D space.
	 * @param area
	 *           the area in which the data is plotted.
	 * @param edge
	 *           the location.
	 * @return The data value.
	 */
	public double java2DToValue(double java2DValue, Rectangle2D area, RectangleEdge edge) {

		Range range = getRange();
		double axisMin = range.getLowerBound();
		double axisMax = range.getUpperBound();

		double min = 0.0;
		double max = 0.0;
		if (RectangleEdge.isTopOrBottom(edge)) {
			min = area.getX();
			max = area.getMaxX();
		} else
			if (RectangleEdge.isLeftOrRight(edge)) {
				min = area.getMaxY();
				max = area.getY();
			}
		if (isInverted()) {
			return axisMax - (java2DValue - min) / (max - min) * (axisMax - axisMin);
		} else {
			return axisMin + (java2DValue - min) / (max - min) * (axisMax - axisMin);
		}

	}

	/**
	 * Calculates the value of the lowest visible tick on the axis.
	 * 
	 * @return the value of the lowest visible tick on the axis.
	 */
	public double calculateLowestVisibleTickValue() {

		double unit = getTickUnit().getSize();
		double index = Math.ceil(getRange().getLowerBound() / unit);
		return index * unit;

	}

	/**
	 * Calculates the value of the highest visible tick on the axis.
	 * 
	 * @return the value of the highest visible tick on the axis.
	 */
	public double calculateHighestVisibleTickValue() {

		double unit = getTickUnit().getSize();
		double index = Math.floor(getRange().getUpperBound() / unit);
		return index * unit;

	}

	/**
	 * Calculates the number of visible ticks.
	 * 
	 * @return the number of visible ticks on the axis.
	 */
	public int calculateVisibleTickCount() {

		double unit = getTickUnit().getSize();
		Range range = getRange();
		return (int) (Math.floor(range.getUpperBound() / unit)
								- Math.ceil(range.getLowerBound() / unit) + 1);

	}

	/**
	 * Draws the axis on a Java 2D graphics device (such as the screen or a printer).
	 * 
	 * @param g2
	 *           the graphics device (<code>null</code> not permitted).
	 * @param cursor
	 *           the cursor location.
	 * @param plotArea
	 *           the area within which the axes and data should be drawn (<code>null</code> not permitted).
	 * @param dataArea
	 *           the area within which the data should be drawn (<code>null</code> not
	 *           permitted).
	 * @param edge
	 *           the location of the axis (<code>null</code> not permitted).
	 * @param plotState
	 *           collects information about the plot (<code>null</code> permitted).
	 * @return the axis state (never <code>null</code>).
	 */
	public AxisState draw(Graphics2D g2,
									double cursor,
									Rectangle2D plotArea,
									Rectangle2D dataArea,
									RectangleEdge edge,
									PlotRenderingInfo plotState) {

		AxisState state = null;
		// if the axis is not visible, don't draw it...
		if (!isVisible()) {
			state = new AxisState(cursor);
			// even though the axis is not visible, we need ticks for the gridlines...
			List ticks = refreshTicks(g2, state, plotArea, dataArea, edge);
			state.setTicks(ticks);
			return state;
		}

		// draw the tick marks and labels...
		state = drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge);

		// // draw the marker band (if there is one)...
		// if (getMarkerBand() != null) {
		// if (edge == RectangleEdge.BOTTOM) {
		// cursor = cursor - getMarkerBand().getHeight(g2);
		// }
		// getMarkerBand().draw(g2, plotArea, dataArea, 0, cursor);
		// }

		// draw the axis label...
		state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);

		return state;

	}

	/**
	 * Replace occurrences of a substring.
	 * http://ostermiller.org/utils/StringHelper.html
	 * StringHelper.replace("1-2-3", "-", "|");<br>
	 * result: "1|2|3"<br>
	 * StringHelper.replace("-1--2-", "-", "|");<br>
	 * result: "|1||2|"<br>
	 * StringHelper.replace("123", "", "|");<br>
	 * result: "123"<br>
	 * StringHelper.replace("1-2---3----4", "--", "|");<br>
	 * result: "1-2|-3||4"<br>
	 * StringHelper.replace("1-2---3----4", "--", "---");<br>
	 * result: "1-2----3------4"<br>
	 * 
	 * @param s
	 *           String to be modified.
	 * @param find
	 *           String to find.
	 * @param replace
	 *           String to replace.
	 * @return a string with all the occurrences of the string to find replaced.
	 * @throws NullPointerException
	 *            if s is null.
	 */
	public static String stringReplace(String s, String find, String replace) {
		int findLength;
		// the next statement has the side effect of throwing a null pointer
		// exception if s is null.
		int stringLength = s.length();
		if (find == null || (findLength = find.length()) == 0) {
			// If there is nothing to find, we won't try and find it.
			return s;
		}
		if (replace == null) {
			// a null string and an empty string are the same
			// for replacement purposes.
			replace = ""; //$NON-NLS-1$
		}
		int replaceLength = replace.length();

		// We need to figure out how long our resulting string will be.
		// This is required because without it, the possible resizing
		// and copying of memory structures could lead to an unacceptable runtime.
		// In the worst case it would have to be resized n times with each
		// resize having a O(n) copy leading to an O(n^2) algorithm.
		int length;
		if (findLength == replaceLength) {
			// special case in which we don't need to count the replacements
			// because the count falls out of the length formula.
			length = stringLength;
		} else {
			int count;
			int start;
			int end;

			// Scan s and count the number of times we find our target.
			count = 0;
			start = 0;
			while ((end = s.indexOf(find, start)) != -1) {
				count++;
				start = end + findLength;
			}
			if (count == 0) {
				// special case in which on first pass, we find there is nothing
				// to be replaced. No need to do a second pass or create a string buffer.
				return s;
			}
			length = stringLength - (count * (findLength - replaceLength));
		}

		int start = 0;
		int end = s.indexOf(find, start);
		if (end == -1) {
			// nothing was found in the string to replace.
			// we can get this if the find and replace strings
			// are the same length because we didn't check before.
			// in this case, we will return the original string
			return s;
		}
		// it looks like we actually have something to replace
		// *sigh* allocate memory for it.
		StringBuffer sb = new StringBuffer(length);

		// Scan s and do the replacements
		while (end != -1) {
			sb.append(s.substring(start, end).toString());
			sb.append(replace.toString());
			start = end + findLength;
			end = s.indexOf(find, start);
		}
		end = stringLength;
		sb.append(s.substring(start, end).toString());

		return (sb.toString());
	}

	public static DecimalFormat getDecimalFormat(String pattern) {
		pattern = stringReplace(pattern, ",", "");
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern(pattern);
		return df;
	}

	private static TickUnitSource cachedStandardTickUnits = null;

	/**
	 * Creates the standard tick units.
	 * <P>
	 * If you don't like these defaults, create your own instance of TickUnits and then pass it to the setStandardTickUnits(...) method in the NumberAxis class.
	 * 
	 * @return the standard tick units.
	 */
	public static TickUnitSource createStandardTickUnits() {

		if (cachedStandardTickUnits != null)
			return cachedStandardTickUnits;

		TickUnits units = new TickUnits();

		// we can add the units in any order, the TickUnits collection will sort them...
		units.add(new NumberTickUnit(0.0000001, getDecimalFormat("0.0000000")));
		units.add(new NumberTickUnit(0.000001, getDecimalFormat("0.000000")));
		units.add(new NumberTickUnit(0.00001, getDecimalFormat("0.00000")));
		units.add(new NumberTickUnit(0.0001, getDecimalFormat("0.0000")));
		units.add(new NumberTickUnit(0.001, getDecimalFormat("0.000")));
		units.add(new NumberTickUnit(0.01, getDecimalFormat("0.00")));
		units.add(new NumberTickUnit(0.1, getDecimalFormat("0.0")));
		units.add(new NumberTickUnit(1, getDecimalFormat("0")));
		units.add(new NumberTickUnit(10, getDecimalFormat("0")));
		units.add(new NumberTickUnit(100, getDecimalFormat("0")));
		units.add(new NumberTickUnit(1000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(100000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(1000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(10000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(100000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(1000000000, getDecimalFormat("#,###,###,##0")));
		units.add(new NumberTickUnit(10000000000.0, getDecimalFormat("#,###,###,##0")));
		units.add(new NumberTickUnit(100000000000.0, getDecimalFormat("#,###,###,##0")));

		units.add(new NumberTickUnit(0.00000025, getDecimalFormat("0.00000000")));
		units.add(new NumberTickUnit(0.0000025, getDecimalFormat("0.0000000")));
		units.add(new NumberTickUnit(0.000025, getDecimalFormat("0.000000")));
		units.add(new NumberTickUnit(0.00025, getDecimalFormat("0.00000")));
		units.add(new NumberTickUnit(0.0025, getDecimalFormat("0.0000")));
		units.add(new NumberTickUnit(0.025, getDecimalFormat("0.000")));
		units.add(new NumberTickUnit(0.25, getDecimalFormat("0.00")));
		units.add(new NumberTickUnit(2.5, getDecimalFormat("0.0")));
		units.add(new NumberTickUnit(25, getDecimalFormat("0")));
		units.add(new NumberTickUnit(250, getDecimalFormat("0")));
		units.add(new NumberTickUnit(2500, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(25000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(250000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2500000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(25000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(250000000, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(2500000000.0, getDecimalFormat("#,###,###,##0")));
		units.add(new NumberTickUnit(25000000000.0, getDecimalFormat("#,###,###,##0")));
		units.add(new NumberTickUnit(250000000000.0, getDecimalFormat("#,###,###,##0")));

		units.add(new NumberTickUnit(0.0000005, getDecimalFormat("0.0000000")));
		units.add(new NumberTickUnit(0.000005, getDecimalFormat("0.000000")));
		units.add(new NumberTickUnit(0.00005, getDecimalFormat("0.00000")));
		units.add(new NumberTickUnit(0.0005, getDecimalFormat("0.0000")));
		units.add(new NumberTickUnit(0.005, getDecimalFormat("0.000")));
		units.add(new NumberTickUnit(0.05, getDecimalFormat("0.00")));
		units.add(new NumberTickUnit(0.5, getDecimalFormat("0.0")));
		units.add(new NumberTickUnit(5L, getDecimalFormat("0")));
		units.add(new NumberTickUnit(50L, getDecimalFormat("0")));
		units.add(new NumberTickUnit(500L, getDecimalFormat("0")));
		units.add(new NumberTickUnit(5000L, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(50000L, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(500000L, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000000L, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(50000000L, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(500000000L, getDecimalFormat("#,###,##0")));
		units.add(new NumberTickUnit(5000000000L, getDecimalFormat("#,###,###,##0")));
		units.add(new NumberTickUnit(50000000000L, getDecimalFormat("#,###,###,##0")));
		units.add(new NumberTickUnit(500000000000L, getDecimalFormat("#,###,###,##0")));
		cachedStandardTickUnits = units;
		return units;

	}

	/**
	 * Returns a collection of tick units for integer values.
	 * 
	 * @return a collection of tick units for integer values.
	 */
	public static TickUnitSource createIntegerTickUnits() {

		TickUnits units = new TickUnits();

		units.add(new NumberTickUnit(1, getDecimalFormat("0")));
		units.add(new NumberTickUnit(2, getDecimalFormat("0")));
		units.add(new NumberTickUnit(5, getDecimalFormat("0")));
		units.add(new NumberTickUnit(10, getDecimalFormat("0")));
		units.add(new NumberTickUnit(20, getDecimalFormat("0")));
		units.add(new NumberTickUnit(50, getDecimalFormat("0")));
		units.add(new NumberTickUnit(100, getDecimalFormat("0")));
		units.add(new NumberTickUnit(200, getDecimalFormat("0")));
		units.add(new NumberTickUnit(500, getDecimalFormat("0")));
		units.add(new NumberTickUnit(1000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(20000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(50000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(100000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(200000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(500000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(1000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(20000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(50000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(100000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(200000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(500000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(1000000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(2000000000, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(5000000000.0, getDecimalFormat("#,##0")));
		units.add(new NumberTickUnit(10000000000.0, getDecimalFormat("#,##0")));

		return units;

	}

	/**
	 * Creates a collection of standard tick units. The supplied locale is used to create the
	 * number formatter (a localised instance of <code>NumberFormat</code>).
	 * <P>
	 * If you don't like these defaults, create your own instance of {@link TickUnits} and then pass it to the <code>setStandardTickUnits(...)</code> method.
	 * 
	 * @param locale
	 *           the locale.
	 * @return a tick unit collection.
	 */
	public static TickUnitSource createStandardTickUnits(Locale locale) {

		TickUnits units = new TickUnits();

		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

		// we can add the units in any order, the TickUnits collection will sort them...
		units.add(new NumberTickUnit(0.0000001, numberFormat));
		units.add(new NumberTickUnit(0.000001, numberFormat));
		units.add(new NumberTickUnit(0.00001, numberFormat));
		units.add(new NumberTickUnit(0.0001, numberFormat));
		units.add(new NumberTickUnit(0.001, numberFormat));
		units.add(new NumberTickUnit(0.01, numberFormat));
		units.add(new NumberTickUnit(0.1, numberFormat));
		units.add(new NumberTickUnit(1, numberFormat));
		units.add(new NumberTickUnit(10, numberFormat));
		units.add(new NumberTickUnit(100, numberFormat));
		units.add(new NumberTickUnit(1000, numberFormat));
		units.add(new NumberTickUnit(10000, numberFormat));
		units.add(new NumberTickUnit(100000, numberFormat));
		units.add(new NumberTickUnit(1000000, numberFormat));
		units.add(new NumberTickUnit(10000000, numberFormat));
		units.add(new NumberTickUnit(100000000, numberFormat));
		units.add(new NumberTickUnit(1000000000, numberFormat));
		units.add(new NumberTickUnit(10000000000.0, numberFormat));

		units.add(new NumberTickUnit(0.00000025, numberFormat));
		units.add(new NumberTickUnit(0.0000025, numberFormat));
		units.add(new NumberTickUnit(0.000025, numberFormat));
		units.add(new NumberTickUnit(0.00025, numberFormat));
		units.add(new NumberTickUnit(0.0025, numberFormat));
		units.add(new NumberTickUnit(0.025, numberFormat));
		units.add(new NumberTickUnit(0.25, numberFormat));
		units.add(new NumberTickUnit(2.5, numberFormat));
		units.add(new NumberTickUnit(25, numberFormat));
		units.add(new NumberTickUnit(250, numberFormat));
		units.add(new NumberTickUnit(2500, numberFormat));
		units.add(new NumberTickUnit(25000, numberFormat));
		units.add(new NumberTickUnit(250000, numberFormat));
		units.add(new NumberTickUnit(2500000, numberFormat));
		units.add(new NumberTickUnit(25000000, numberFormat));
		units.add(new NumberTickUnit(250000000, numberFormat));
		units.add(new NumberTickUnit(2500000000.0, numberFormat));
		units.add(new NumberTickUnit(25000000000.0, numberFormat));

		units.add(new NumberTickUnit(0.0000005, numberFormat));
		units.add(new NumberTickUnit(0.000005, numberFormat));
		units.add(new NumberTickUnit(0.00005, numberFormat));
		units.add(new NumberTickUnit(0.0005, numberFormat));
		units.add(new NumberTickUnit(0.005, numberFormat));
		units.add(new NumberTickUnit(0.05, numberFormat));
		units.add(new NumberTickUnit(0.5, numberFormat));
		units.add(new NumberTickUnit(5L, numberFormat));
		units.add(new NumberTickUnit(50L, numberFormat));
		units.add(new NumberTickUnit(500L, numberFormat));
		units.add(new NumberTickUnit(5000L, numberFormat));
		units.add(new NumberTickUnit(50000L, numberFormat));
		units.add(new NumberTickUnit(500000L, numberFormat));
		units.add(new NumberTickUnit(5000000L, numberFormat));
		units.add(new NumberTickUnit(50000000L, numberFormat));
		units.add(new NumberTickUnit(500000000L, numberFormat));
		units.add(new NumberTickUnit(5000000000L, numberFormat));
		units.add(new NumberTickUnit(50000000000L, numberFormat));

		return units;

	}

	/**
	 * Returns a collection of tick units for integer values.
	 * Uses a given Locale to create the DecimalFormats.
	 * 
	 * @param locale
	 *           the locale to use to represent Numbers.
	 * @return a collection of tick units for integer values.
	 */
	public static TickUnitSource createIntegerTickUnits(Locale locale) {

		TickUnits units = new TickUnits();

		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

		units.add(new NumberTickUnit(1, numberFormat));
		units.add(new NumberTickUnit(2, numberFormat));
		units.add(new NumberTickUnit(5, numberFormat));
		units.add(new NumberTickUnit(10, numberFormat));
		units.add(new NumberTickUnit(20, numberFormat));
		units.add(new NumberTickUnit(50, numberFormat));
		units.add(new NumberTickUnit(100, numberFormat));
		units.add(new NumberTickUnit(200, numberFormat));
		units.add(new NumberTickUnit(500, numberFormat));
		units.add(new NumberTickUnit(1000, numberFormat));
		units.add(new NumberTickUnit(2000, numberFormat));
		units.add(new NumberTickUnit(5000, numberFormat));
		units.add(new NumberTickUnit(10000, numberFormat));
		units.add(new NumberTickUnit(20000, numberFormat));
		units.add(new NumberTickUnit(50000, numberFormat));
		units.add(new NumberTickUnit(100000, numberFormat));
		units.add(new NumberTickUnit(200000, numberFormat));
		units.add(new NumberTickUnit(500000, numberFormat));
		units.add(new NumberTickUnit(1000000, numberFormat));
		units.add(new NumberTickUnit(2000000, numberFormat));
		units.add(new NumberTickUnit(5000000, numberFormat));
		units.add(new NumberTickUnit(10000000, numberFormat));
		units.add(new NumberTickUnit(20000000, numberFormat));
		units.add(new NumberTickUnit(50000000, numberFormat));
		units.add(new NumberTickUnit(100000000, numberFormat));
		units.add(new NumberTickUnit(200000000, numberFormat));
		units.add(new NumberTickUnit(500000000, numberFormat));
		units.add(new NumberTickUnit(1000000000, numberFormat));
		units.add(new NumberTickUnit(2000000000, numberFormat));
		units.add(new NumberTickUnit(5000000000.0, numberFormat));
		units.add(new NumberTickUnit(10000000000.0, numberFormat));

		return units;

	}

	/**
	 * Estimates the maximum tick label height.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @return The maximum height.
	 */
	protected double estimateMaximumTickLabelHeight(Graphics2D g2) {

		Insets tickLabelInsets = getTickLabelInsets();
		double result = tickLabelInsets.top + tickLabelInsets.bottom;

		Font tickLabelFont = getTickLabelFont();
		FontRenderContext frc = g2.getFontRenderContext();
		result += tickLabelFont.getLineMetrics("123", frc).getHeight() * 1.05d;
		return result;

	}

	/**
	 * Estimates the maximum width of the tick labels, assuming the specified tick unit is used.
	 * <P>
	 * Rather than computing the string bounds of every tick on the axis, we just look at two values: the lower bound and the upper bound for the axis. These two
	 * values will usually be representative.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param tickUnit
	 *           the tick unit to use for calculation.
	 * @return the estimated maximum width of the tick labels.
	 */
	protected double estimateMaximumTickLabelWidth(Graphics2D g2, TickUnit tickUnit) {

		Insets tickLabelInsets = getTickLabelInsets();
		double result = tickLabelInsets.left + tickLabelInsets.right;

		if (isVerticalTickLabels()) {
			// all tick labels have the same width (equal to the height of the font)...
			FontRenderContext frc = g2.getFontRenderContext();
			LineMetrics lm = getTickLabelFont().getLineMetrics("0", frc);
			result += lm.getHeight();
		} else {
			// look at lower and upper bounds...
			FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
			Range range = getRange();
			double lower = range.getLowerBound();
			double upper = range.getUpperBound();
			String lowerStr = tickUnit.valueToString(lower);
			String upperStr = tickUnit.valueToString(upper);
			double w1 = fm.stringWidth(lowerStr);
			double w2 = fm.stringWidth(upperStr);
			result += Math.max(w1, w2);
		}

		return result;

	}

	/**
	 * Selects an appropriate tick value for the axis. The strategy is to
	 * display as many ticks as possible (selected from an array of 'standard'
	 * tick units) without the labels overlapping.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param drawArea
	 *           the area in which the plot and axes should be drawn.
	 * @param dataArea
	 *           the area defined by the axes.
	 * @param edge
	 *           the axis location.
	 */
	protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D drawArea, Rectangle2D dataArea,
													RectangleEdge edge) {

		if (RectangleEdge.isTopOrBottom(edge)) {
			selectHorizontalAutoTickUnit(g2, drawArea, dataArea, edge);
		} else
			if (RectangleEdge.isLeftOrRight(edge)) {
				selectVerticalAutoTickUnit(g2, drawArea, dataArea, edge);
			}

	}

	/**
	 * Selects an appropriate tick value for the axis. The strategy is to
	 * display as many ticks as possible (selected from an array of 'standard'
	 * tick units) without the labels overlapping.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param drawArea
	 *           the area in which the plot and axes should be drawn.
	 * @param dataArea
	 *           the area defined by the axes.
	 * @param edge
	 *           the axis location.
	 */
	protected void selectHorizontalAutoTickUnit(Graphics2D g2,
																Rectangle2D drawArea,
																Rectangle2D dataArea,
																RectangleEdge edge) {

		double zero = translateValueToJava2D(0.0, dataArea, edge);
		double tickLabelWidth = estimateMaximumTickLabelWidth(g2, getTickUnit());

		// start with the current tick unit...
		TickUnitSource tickUnits = getStandardTickUnits();
		TickUnit unit1 = tickUnits.getCeilingTickUnit(getTickUnit());
		double x1 = translateValueToJava2D(unit1.getSize(), dataArea, edge);
		double unit1Width = Math.abs(x1 - zero);

		// then extrapolate...
		double guess = (tickLabelWidth / unit1Width) * unit1.getSize();

		NumberTickUnit unit2 = (NumberTickUnit) tickUnits.getCeilingTickUnit(guess);
		double x2 = translateValueToJava2D(unit2.getSize(), dataArea, edge);
		double unit2Width = Math.abs(x2 - zero);

		tickLabelWidth = estimateMaximumTickLabelWidth(g2, unit2);
		if (tickLabelWidth > unit2Width) {
			unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
		}

		setTickUnit(unit2, false, false);

	}

	/**
	 * Selects an appropriate tick value for the axis. The strategy is to
	 * display as many ticks as possible (selected from an array of 'standard'
	 * tick units) without the labels overlapping.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the area in which the plot and axes should be drawn.
	 * @param dataArea
	 *           the area in which the plot should be drawn.
	 * @param edge
	 *           the axis location.
	 */
	protected void selectVerticalAutoTickUnit(Graphics2D g2,
																Rectangle2D plotArea,
																Rectangle2D dataArea,
																RectangleEdge edge) {

		double zero = translateValueToJava2D(0.0, dataArea, edge);
		double tickLabelHeight = estimateMaximumTickLabelHeight(g2);

		// start with the current tick unit...
		TickUnitSource tickUnits = getStandardTickUnits();
		TickUnit unit1 = tickUnits.getCeilingTickUnit(getTickUnit());
		double y = translateValueToJava2D(unit1.getSize(), dataArea, edge);
		double unitHeight = Math.abs(y - zero);

		// then extrapolate...
		double guess = (tickLabelHeight / unitHeight) * unit1.getSize();

		NumberTickUnit unit2 = (NumberTickUnit) tickUnits.getCeilingTickUnit(guess);
		double y2 = translateValueToJava2D(unit2.getSize(), dataArea, edge);
		double unit2Height = Math.abs(y2 - zero);

		tickLabelHeight = estimateMaximumTickLabelHeight(g2);
		if (tickLabelHeight > unit2Height) {
			unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
		}

		setTickUnit(unit2, false, false);

	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the results in the
	 * tick label list (ready for drawing).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the axis state.
	 * @param plotArea
	 *           the area in which the plot and the axes should be drawn.
	 * @param dataArea
	 *           the area in which the plot should be drawn.
	 * @param edge
	 *           the location of the axis.
	 * @return A list of ticks.
	 */
	public List refreshTicks(Graphics2D g2,
										AxisState state,
										Rectangle2D plotArea,
										Rectangle2D dataArea,
										RectangleEdge edge) {

		List result = new java.util.ArrayList();
		if (RectangleEdge.isTopOrBottom(edge)) {
			result = refreshHorizontalTicks(g2, state.getCursor(), plotArea, dataArea, edge);
		} else
			if (RectangleEdge.isLeftOrRight(edge)) {
				result = refreshVerticalTicks(g2, state.getCursor(), plotArea, dataArea, edge);
			}
		return result;

	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the results in the
	 * tick label list (ready for drawing).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param cursor
	 *           the cursor.
	 * @param plotArea
	 *           the area in which the plot (inlcuding axes) should be drawn.
	 * @param dataArea
	 *           the area in which the data should be drawn.
	 * @param edge
	 *           the location of the axis.
	 * @return A list of ticks.
	 */
	protected List refreshHorizontalTicks(Graphics2D g2, double cursor,
														Rectangle2D plotArea, Rectangle2D dataArea,
														RectangleEdge edge) {

		List result = new java.util.ArrayList();

		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);

		if (isAutoTickUnitSelection()) {
			selectAutoTickUnit(g2, plotArea, dataArea, edge);
		}

		double size = getTickUnit().getSize();
		int count = calculateVisibleTickCount();
		double lowestTickValue = calculateLowestVisibleTickValue();

		if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
			for (int i = 0; i < count; i++) {
				double currentTickValue = lowestTickValue + (i * size);
				String tickLabel;
				NumberFormat formatter = getNumberFormatOverride();
				if (formatter != null) {
					tickLabel = formatter.format(currentTickValue);
				} else {
					tickLabel = getTickUnit().valueToString(currentTickValue);
				}
				TextAnchor anchor = null;
				TextAnchor rotationAnchor = null;
				double angle = 0.0;
				if (isVerticalTickLabels()) {
					anchor = TextAnchor.CENTER_RIGHT;
					rotationAnchor = TextAnchor.CENTER_RIGHT;
					if (edge == RectangleEdge.TOP) {
						angle = Math.PI / 2.0;
					} else {
						angle = -Math.PI / 2.0;
					}
				} else {
					if (edge == RectangleEdge.TOP) {
						anchor = TextAnchor.BOTTOM_CENTER;
						rotationAnchor = TextAnchor.BOTTOM_CENTER;
					} else {
						anchor = TextAnchor.TOP_CENTER;
						rotationAnchor = TextAnchor.TOP_CENTER;
					}
				}

				Tick tick = new NumberTick(
									new Double(currentTickValue), tickLabel, anchor, rotationAnchor, angle
									);
				result.add(tick);
			}
		}
		return result;

	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the results in the
	 * tick label list (ready for drawing).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param cursor
	 *           the cursor location.
	 * @param plotArea
	 *           the area in which the plot and the axes should be drawn.
	 * @param dataArea
	 *           the area in which the plot should be drawn.
	 * @param edge
	 *           the location of the axis.
	 * @return A list of ticks.
	 */
	protected List refreshVerticalTicks(Graphics2D g2, double cursor,
														Rectangle2D plotArea, Rectangle2D dataArea,
														RectangleEdge edge) {

		List result = new java.util.ArrayList();
		result.clear();

		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);
		if (isAutoTickUnitSelection()) {
			selectAutoTickUnit(g2, plotArea, dataArea, edge);
		}

		double size = getTickUnit().getSize();
		int count = calculateVisibleTickCount();
		double lowestTickValue = calculateLowestVisibleTickValue();

		if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
			for (int i = 0; i < count; i++) {
				double currentTickValue = lowestTickValue + (i * size);
				String tickLabel;
				NumberFormat formatter = getNumberFormatOverride();
				if (formatter != null) {
					tickLabel = formatter.format(currentTickValue);
				} else {
					tickLabel = getTickUnit().valueToString(currentTickValue);
				}

				TextAnchor anchor = null;
				TextAnchor rotationAnchor = null;
				double angle = 0.0;
				if (isVerticalTickLabels()) {
					if (edge == RectangleEdge.LEFT) {
						anchor = TextAnchor.BOTTOM_CENTER;
						rotationAnchor = TextAnchor.BOTTOM_CENTER;
						angle = -Math.PI / 2.0;
					} else {
						anchor = TextAnchor.BOTTOM_CENTER;
						rotationAnchor = TextAnchor.BOTTOM_CENTER;
						angle = Math.PI / 2.0;
					}
				} else {
					if (edge == RectangleEdge.LEFT) {
						anchor = TextAnchor.CENTER_RIGHT;
						rotationAnchor = TextAnchor.CENTER_RIGHT;
					} else {
						anchor = TextAnchor.CENTER_LEFT;
						rotationAnchor = TextAnchor.CENTER_LEFT;
					}
				}

				Tick tick = new NumberTick(
									new Double(currentTickValue), tickLabel, anchor, rotationAnchor, angle
									);
				result.add(tick);
			}
		}
		return result;

	}

	/**
	 * Returns a clone of the axis.
	 * 
	 * @return A clone
	 * @throws CloneNotSupportedException
	 *            if some component of the axis does not support cloning.
	 */
	public Object clone() throws CloneNotSupportedException {
		NumberAxis clone = (NumberAxis) super.clone();

		if (this.numberFormatOverride != null) {
			clone.numberFormatOverride = (NumberFormat) this.numberFormatOverride.clone();
		}

		return clone;
	}

	/**
	 * Tests an object for equality with this instance.
	 * 
	 * @param object
	 *           the object.
	 * @return A boolean.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof NumberAxis) {
			if (super.equals(object)) {

				NumberAxis axis = (NumberAxis) object;

				boolean b0 = (this.autoRangeIncludesZero == axis.autoRangeIncludesZero);
				boolean b1 = (this.autoRangeStickyZero == axis.autoRangeStickyZero);
				boolean b2 = ObjectUtils.equal(this.tickUnit, axis.tickUnit);
				boolean b3 = ObjectUtils.equal(this.numberFormatOverride,
																axis.numberFormatOverride);
				// boolean b4 = ObjectUtils.equalOrBothNull(this.markerBand, axis.markerBand);

				return b0 && b1 && b2 && b3;

			}
		}

		return false;

	}

}
