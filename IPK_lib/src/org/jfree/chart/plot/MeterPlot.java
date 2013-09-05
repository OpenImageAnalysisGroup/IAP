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
 * MeterPlot.java
 * --------------
 * (C) Copyright 2000-2004, by Hari and Contributors.
 * Original Author: Hari (ourhari@hotmail.com);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Bob Orchard;
 * Arnaud Lelievre;
 * Nicolas Brodu;
 * $Id: MeterPlot.java,v 1.1 2011-01-31 09:02:09 klukas Exp $
 * Changes
 * -------
 * 01-Apr-2002 : Version 1, contributed by Hari (DG);
 * 23-Apr-2002 : Moved dataset from JFreeChart to Plot (DG);
 * 22-Aug-2002 : Added changes suggest by Bob Orchard, changed Color to Paint for consistency,
 * plus added Javadoc comments (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 23-Jan-2003 : Removed one constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 20-Aug-2003 : Changed dataset from MeterDataset --> ValueDataset, added equals(...) method,
 * 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL);
 * implemented Cloneable, and various other changes (DG);
 * 08-Sep-2003 : Added serialization methods (NB);
 * 11-Sep-2003 : Added cloning support (NB);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 25-Sep-2003 : Fix useless cloning. Correct dataset listener registration in constructor. (NB)
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 17-Jan-2004 : Changed to allow dialBackgroundPaint to be set to null - see bug 823628 (DG);
 * 07-Apr-2004 : Changed string bounds calculation (DG);
 * 12-May-2004 : Added tickLabelFormat attribute - see RFE 949566. Also updated the equals()
 * method (DG);
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.data.DatasetChangeEvent;
import org.jfree.data.Range;
import org.jfree.data.ValueDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.util.ObjectUtils;

/**
 * A plot that displays a single value in the context of several ranges ('normal', 'warning'
 * and 'critical').
 */
public class MeterPlot extends Plot implements Serializable, Cloneable {

	/** Constant to indicate the normal data range. */
	public static final int NORMAL_DATA_RANGE = 0;

	/** Constant to indicate the warning data range. */
	public static final int WARNING_DATA_RANGE = 1;

	/** Constant to indicate the critical data range. */
	public static final int CRITICAL_DATA_RANGE = 2;

	/** Constant to indicate the full data range. */
	public static final int FULL_DATA_RANGE = 3;

	/** The default text for the normal level. */
	public static final String NORMAL_TEXT = "Normal";

	/** The default text for the warning level. */
	public static final String WARNING_TEXT = "Warning";

	/** The default text for the critical level. */
	public static final String CRITICAL_TEXT = "Critical";

	/** The default 'normal' level color. */
	static final Paint DEFAULT_NORMAL_PAINT = Color.green;

	/** The default 'warning' level color. */
	static final Paint DEFAULT_WARNING_PAINT = Color.yellow;

	/** The default 'critical' level color. */
	static final Paint DEFAULT_CRITICAL_PAINT = Color.red;

	/** The default background paint. */
	static final Paint DEFAULT_DIAL_BACKGROUND_PAINT = Color.black;

	/** The default needle paint. */
	static final Paint DEFAULT_NEEDLE_PAINT = Color.green;

	/** The default value font. */
	static final Font DEFAULT_VALUE_FONT = new Font("SansSerif", Font.BOLD, 12);

	/** The default value paint. */
	static final Paint DEFAULT_VALUE_PAINT = Color.yellow;

	/** The default meter angle. */
	public static final int DEFAULT_METER_ANGLE = 270;

	/** The default border size. */
	public static final float DEFAULT_BORDER_SIZE = 3f;

	/** The default circle size. */
	public static final float DEFAULT_CIRCLE_SIZE = 10f;

	/** The default background color. */
	public static final Paint DEFAULT_BACKGROUND_PAINT = Color.lightGray;

	/** The default label font. */
	public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif", Font.BOLD, 10);

	/** Constant for the label type. */
	public static final int NO_LABELS = 0;

	/** Constant for the label type. */
	public static final int VALUE_LABELS = 1;

	/** The dataset. */
	private ValueDataset dataset;

	/** The units displayed on the dial. */
	private String units;

	/** The overall range. */
	private Range range;

	/** The normal range. */
	private Range normalRange;

	/** The warning range. */
	private Range warningRange;

	/** The critical range. */
	private Range criticalRange;

	/** The outline paint. */
	private transient Paint dialOutlinePaint;

	/** The 'normal' level color. */
	private transient Paint normalPaint = DEFAULT_NORMAL_PAINT;

	/** The 'warning' level color. */
	private transient Paint warningPaint = DEFAULT_WARNING_PAINT;

	/** The 'critical' level color. */
	private transient Paint criticalPaint = DEFAULT_CRITICAL_PAINT;

	/** The dial shape (background shape). */
	private DialShape shape = DialShape.CIRCLE;

	/** The paint for the dial background. */
	private transient Paint dialBackgroundPaint;

	/** The paint for the needle. */
	private transient Paint needlePaint;

	/** The font for the value displayed in the center of the dial. */
	private Font valueFont;

	/** The paint for the value displayed in the center of the dial. */
	private transient Paint valuePaint;

	/** The tick label type (NO_LABELS, VALUE_LABELS). */
	private int tickLabelType;

	/** The tick label font. */
	private Font tickLabelFont;

	/** The tick label format. */
	private NumberFormat tickLabelFormat;

	/** A flag that controls whether or not the border is drawn. */
	private boolean drawBorder;

	/** ??? */
	private int meterCalcAngle = -1;

	/** ??? */
	private double meterRange = -1;

	/** The resourceBundle for the localization. */
	protected static ResourceBundle localizationResources =
										ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

	/** The dial extent. */
	private int meterAngle = DEFAULT_METER_ANGLE;

	/** The minimum meter value. */
	private double minMeterValue = 0.0;

	/**
	 * Creates a new plot with no dataset.
	 */
	public MeterPlot() {
		this(null);
	}

	/**
	 * Creates a new plot that displays the value in the supplied dataset.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 */
	public MeterPlot(ValueDataset dataset) {
		super();
		this.units = "Units";
		this.range = new Range(0.0, 100.0);
		this.normalRange = new Range(0.0, 60.0);
		this.warningRange = new Range(60.0, 90.0);
		this.criticalRange = new Range(90.0, 100.0);
		this.tickLabelType = MeterPlot.VALUE_LABELS;
		this.tickLabelFont = MeterPlot.DEFAULT_LABEL_FONT;
		this.tickLabelFormat = NumberFormat.getInstance();
		this.dialBackgroundPaint = MeterPlot.DEFAULT_DIAL_BACKGROUND_PAINT;
		this.needlePaint = MeterPlot.DEFAULT_NEEDLE_PAINT;
		this.valueFont = MeterPlot.DEFAULT_VALUE_FONT;
		this.valuePaint = MeterPlot.DEFAULT_VALUE_PAINT;
		setDataset(dataset);
	}

	/**
	 * Returns the units for the dial.
	 * 
	 * @return The units.
	 */
	public String getUnits() {
		return this.units;
	}

	/**
	 * Sets the units for the dial.
	 * 
	 * @param units
	 *           the units.
	 */
	public void setUnits(String units) {
		this.units = units;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the overall range for the dial.
	 * 
	 * @return The overall range.
	 */
	public Range getRange() {
		return this.range;
	}

	/**
	 * Sets the overall range for the dial.
	 * 
	 * @param range
	 *           the range.
	 */
	public void setRange(Range range) {
		this.range = range;
	}

	/**
	 * Returns the normal range for the dial.
	 * 
	 * @return The normal range.
	 */
	public Range getNormalRange() {
		return this.normalRange;
	}

	/**
	 * Sets the normal range for the dial.
	 * 
	 * @param range
	 *           the range.
	 */
	public void setNormalRange(Range range) {
		this.normalRange = range;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the warning range for the dial.
	 * 
	 * @return The warning range.
	 */
	public Range getWarningRange() {
		return this.warningRange;
	}

	/**
	 * Sets the warning range for the dial.
	 * 
	 * @param range
	 *           the range.
	 */
	public void setWarningRange(Range range) {
		this.warningRange = range;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the critical range for the dial.
	 * 
	 * @return The critical range.
	 */
	public Range getCriticalRange() {
		return this.criticalRange;
	}

	/**
	 * Sets the critical range for the dial.
	 * 
	 * @param range
	 *           the range.
	 */
	public void setCriticalRange(Range range) {
		this.criticalRange = range;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the dial shape.
	 * 
	 * @return The dial shape.
	 */
	public DialShape getDialShape() {
		return this.shape;
	}

	/**
	 * Sets the dial shape.
	 * 
	 * @param shape
	 *           the shape.
	 */
	public void setDialShape(DialShape shape) {
		this.shape = shape;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint for the dial background.
	 * 
	 * @return The paint (possibly <code>null</code>).
	 */
	public Paint getDialBackgroundPaint() {
		return this.dialBackgroundPaint;
	}

	/**
	 * Sets the paint used to fill the dial background.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setDialBackgroundPaint(Paint paint) {
		this.dialBackgroundPaint = paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint for the needle.
	 * 
	 * @return The paint.
	 */
	public Paint getNeedlePaint() {
		return this.needlePaint;
	}

	/**
	 * Sets the paint used to display the needle.
	 * <P>
	 * If you set this to null, it will revert to the default color.
	 * 
	 * @param paint
	 *           The paint.
	 */
	public void setNeedlePaint(Paint paint) {
		this.needlePaint = paint == null ? DEFAULT_NEEDLE_PAINT : paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the font for the value label.
	 * 
	 * @return The font.
	 */
	public Font getValueFont() {
		return this.valueFont;
	}

	/**
	 * Sets the font used to display the value label.
	 * <P>
	 * If you set this to null, it will revert to the default font.
	 * 
	 * @param font
	 *           The font.
	 */
	public void setValueFont(Font font) {
		this.valueFont = (font == null) ? DEFAULT_VALUE_FONT : font;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint for the value label.
	 * 
	 * @return The paint.
	 */
	public Paint getValuePaint() {
		return this.valuePaint;
	}

	/**
	 * Sets the paint used to display the value label.
	 * <P>
	 * If you set this to null, it will revert to the default paint.
	 * 
	 * @param paint
	 *           The paint.
	 */
	public void setValuePaint(Paint paint) {
		this.valuePaint = paint == null ? DEFAULT_VALUE_PAINT : paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint for the 'normal' level.
	 * 
	 * @return The paint.
	 */
	public Paint getNormalPaint() {
		return this.normalPaint;
	}

	/**
	 * Sets the paint used to display the 'normal' range.
	 * <P>
	 * If you set this to null, it will revert to the default color.
	 * 
	 * @param paint
	 *           The paint.
	 */
	public void setNormalPaint(Paint paint) {
		this.normalPaint = (paint == null) ? DEFAULT_NORMAL_PAINT : paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint used to display the 'warning' range.
	 * 
	 * @return The paint.
	 */
	public Paint getWarningPaint() {
		return this.warningPaint;
	}

	/**
	 * Sets the paint used to display the 'warning' range.
	 * <P>
	 * If you set this to null, it will revert to the default color.
	 * 
	 * @param paint
	 *           The paint.
	 */
	public void setWarningPaint(Paint paint) {
		this.warningPaint = (paint == null) ? DEFAULT_WARNING_PAINT : paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint used to display the 'critical' range.
	 * 
	 * @return The paint.
	 */
	public Paint getCriticalPaint() {
		return this.criticalPaint;
	}

	/**
	 * Sets the paint used to display the 'critical' range.
	 * <P>
	 * If you set this to null, it will revert to the default color.
	 * 
	 * @param paint
	 *           The paint.
	 */
	public void setCriticalPaint(Paint paint) {
		this.criticalPaint = (paint == null) ? DEFAULT_CRITICAL_PAINT : paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the tick label type. Defined by the constants: NO_LABELS,
	 * VALUE_LABELS.
	 * 
	 * @return The tick label type.
	 */
	public int getTickLabelType() {
		return this.tickLabelType;
	}

	/**
	 * Sets the tick label type.
	 * 
	 * @param type
	 *           the type of tick labels - either <code>NO_LABELS</code> or <code>VALUE_LABELS</code>
	 */
	public void setTickLabelType(int type) {

		// check the argument...
		if ((type != NO_LABELS) && (type != VALUE_LABELS)) {
			throw new IllegalArgumentException(
								"MeterPlot.setLabelType(int): unrecognised type.");
		}

		// make the change...
		if (this.tickLabelType != type) {
			this.tickLabelType = type;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the tick label font.
	 * 
	 * @return The font (never <code>null</code>).
	 */
	public Font getTickLabelFont() {
		return this.tickLabelFont;
	}

	/**
	 * Sets the tick label font and sends a {@link PlotChangeEvent} to all registered listeners.
	 * 
	 * @param font
	 *           the font (<code>null</code> not permitted).
	 */
	public void setTickLabelFont(Font font) {
		if (font == null) {
			throw new IllegalArgumentException("Null 'font' argument.");
		}
		if (!this.tickLabelFont.equals(font)) {
			this.tickLabelFont = font;
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Returns the tick label format.
	 * 
	 * @return The tick label format (never <code>null</code>).
	 */
	public NumberFormat getTickLabelFormat() {
		return this.tickLabelFormat;
	}

	/**
	 * Sets the format for the tick labels and sends a {@link PlotChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param format
	 *           the format (<code>null</code> not permitted).
	 */
	public void setTickLabelFormat(NumberFormat format) {
		if (format == null) {
			throw new IllegalArgumentException("Null 'format' argument.");
		}
		this.tickLabelFormat = format;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns a flag that controls whether or not a rectangular border is drawn around the plot
	 * area.
	 * 
	 * @return A flag.
	 */
	public boolean getDrawBorder() {
		return this.drawBorder;
	}

	/**
	 * Sets the flag that controls whether or not a rectangular border is drawn around the plot
	 * area.
	 * <P>
	 * Note: it looks like the true setting needs some work to provide some insets.
	 * 
	 * @param draw
	 *           the flag.
	 */
	public void setDrawBorder(boolean draw) {
		this.drawBorder = draw;
	}

	/**
	 * Returns the meter angle.
	 * 
	 * @return the meter angle.
	 */
	public int getMeterAngle() {
		return this.meterAngle;
	}

	/**
	 * Sets the range through which the dial's needle is free to rotate.
	 * 
	 * @param angle
	 *           the angle.
	 */
	public void setMeterAngle(int angle) {
		this.meterAngle = angle;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the dial outline paint.
	 * 
	 * @return The paint.
	 */
	public Paint getDialOutlinePaint() {
		return this.dialOutlinePaint;
	}

	/**
	 * Sets the dial outline paint.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setDialOutlinePaint(Paint paint) {
		this.dialOutlinePaint = paint;
	}

	/**
	 * Returns the primary dataset for the plot.
	 * 
	 * @return The primary dataset (possibly <code>null</code>).
	 */
	public ValueDataset getDataset() {
		return this.dataset;
	}

	/**
	 * Sets the dataset for the plot, replacing the existing dataset if there is one.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 */
	public void setDataset(ValueDataset dataset) {

		// if there is an existing dataset, remove the plot from the list of change listeners...
		ValueDataset existing = this.dataset;
		if (existing != null) {
			existing.removeChangeListener(this);
		}

		// set the new dataset, and register the chart as a change listener...
		this.dataset = dataset;
		if (dataset != null) {
			setDatasetGroup(dataset.getGroup());
			dataset.addChangeListener(this);
		}

		// send a dataset change event to self...
		DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
		datasetChanged(event);

	}

	/**
	 * Returns a list of legend item labels.
	 * 
	 * @return the legend item labels.
	 * @deprecated use getLegendItems().
	 */
	public List getLegendItemLabels() {
		return null;
	}

	/**
	 * Returns null.
	 * 
	 * @return null.
	 */
	public LegendItemCollection getLegendItems() {
		return null;
	}

	/**
	 * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the area within which the plot should be drawn.
	 * @param parentState
	 *           the state from the parent plot, if there is one.
	 * @param info
	 *           collects info about the drawing.
	 */
	public void draw(Graphics2D g2, Rectangle2D plotArea, PlotState parentState,
							PlotRenderingInfo info) {

		if (info != null) {
			info.setPlotArea(plotArea);
		}

		// adjust for insets...
		Insets insets = getInsets();
		if (insets != null) {
			plotArea.setRect(
								plotArea.getX() + insets.left, plotArea.getY() + insets.top,
								plotArea.getWidth() - insets.left - insets.right,
								plotArea.getHeight() - insets.top - insets.bottom
								);
		}

		plotArea.setRect(
							plotArea.getX() + 4, plotArea.getY() + 4,
							plotArea.getWidth() - 8, plotArea.getHeight() - 8
							);

		// draw the background
		if (this.drawBorder) {
			drawBackground(g2, plotArea);
		}

		// adjust the plot area by the interior spacing value
		double gapHorizontal = (2 * DEFAULT_BORDER_SIZE);
		double gapVertical = (2 * DEFAULT_BORDER_SIZE);
		double meterX = plotArea.getX() + gapHorizontal / 2;
		double meterY = plotArea.getY() + gapVertical / 2;
		double meterW = plotArea.getWidth() - gapHorizontal;
		double meterH = plotArea.getHeight() - gapVertical
								+ ((this.meterAngle <= 180) && (this.shape != DialShape.CIRCLE)
													? plotArea.getHeight() / 1.25 : 0);

		double min = Math.min(meterW, meterH) / 2;
		meterX = (meterX + meterX + meterW) / 2 - min;
		meterY = (meterY + meterY + meterH) / 2 - min;
		meterW = 2 * min;
		meterH = 2 * min;

		Rectangle2D meterArea = new Rectangle2D.Double(meterX,
																			meterY,
																			meterW,
																			meterH);

		Rectangle2D.Double originalArea = new Rectangle2D.Double(meterArea.getX() - 4,
																						meterArea.getY() - 4,
																						meterArea.getWidth() + 8,
																						meterArea.getHeight() + 8);

		double meterMiddleX = meterArea.getCenterX();
		double meterMiddleY = meterArea.getCenterY();

		// plot the data (unless the dataset is null)...
		ValueDataset data = getDataset();
		if (data != null) {
			// double dataMin = data.getMinimumValue().doubleValue();
			// double dataMax = data.getMaximumValue().doubleValue();
			double dataMin = this.range.getLowerBound();
			double dataMax = this.range.getUpperBound();
			this.minMeterValue = dataMin;

			this.meterCalcAngle = 180 + ((this.meterAngle - 180) / 2);
			this.meterRange = dataMax - dataMin;

			Shape savedClip = g2.getClip();
			g2.clip(originalArea);
			Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(
								AlphaComposite.SRC_OVER, getForegroundAlpha())
								);

			if (this.dialBackgroundPaint != null) {
				drawArc(g2, originalArea, dataMin, dataMax, this.dialBackgroundPaint, 1);
			}
			drawTicks(g2, meterArea, dataMin, dataMax);
			drawArcFor(g2, meterArea, data, FULL_DATA_RANGE);
			if (this.normalRange != null) {
				drawArcFor(g2, meterArea, data, NORMAL_DATA_RANGE);
			}
			if (this.warningRange != null) {
				drawArcFor(g2, meterArea, data, WARNING_DATA_RANGE);
			}
			if (this.criticalRange != null) {
				drawArcFor(g2, meterArea, data, CRITICAL_DATA_RANGE);
			}

			if (data.getValue() != null) {

				double dataVal = data.getValue().doubleValue();
				drawTick(g2, meterArea, dataVal, true, this.valuePaint, true, getUnits());

				g2.setPaint(this.needlePaint);
				g2.setStroke(new BasicStroke(2.0f));

				double radius = (meterArea.getWidth() / 2) + DEFAULT_BORDER_SIZE + 15;
				double valueAngle = calculateAngle(dataVal);
				double valueP1 = meterMiddleX + (radius * Math.cos(Math.PI * (valueAngle / 180)));
				double valueP2 = meterMiddleY - (radius * Math.sin(Math.PI * (valueAngle / 180)));

				Polygon arrow = new Polygon();
				if ((valueAngle > 135 && valueAngle < 225)
									|| (valueAngle < 45 && valueAngle > -45)) {

					double valueP3 = (meterMiddleY - DEFAULT_CIRCLE_SIZE / 4);
					double valueP4 = (meterMiddleY + DEFAULT_CIRCLE_SIZE / 4);
					arrow.addPoint((int) meterMiddleX, (int) valueP3);
					arrow.addPoint((int) meterMiddleX, (int) valueP4);

				} else {
					arrow.addPoint((int) (meterMiddleX - DEFAULT_CIRCLE_SIZE / 4),
												(int) meterMiddleY);
					arrow.addPoint((int) (meterMiddleX + DEFAULT_CIRCLE_SIZE / 4),
												(int) meterMiddleY);
				}
				arrow.addPoint((int) valueP1, (int) valueP2);

				Ellipse2D circle = new Ellipse2D.Double(meterMiddleX - DEFAULT_CIRCLE_SIZE / 2,
																			meterMiddleY - DEFAULT_CIRCLE_SIZE / 2,
																			DEFAULT_CIRCLE_SIZE,
																			DEFAULT_CIRCLE_SIZE);
				g2.fill(arrow);
				g2.fill(circle);

			}

			g2.clip(savedClip);
			g2.setComposite(originalComposite);

		}
		if (this.drawBorder) {
			drawOutline(g2, plotArea);
		}

	}

	/**
	 * Draws a colored range (arc) for one level.
	 * 
	 * @param g2
	 *           The graphics device.
	 * @param meterArea
	 *           The drawing area.
	 * @param data
	 *           The dataset.
	 * @param type
	 *           The level.
	 */
	protected void drawArcFor(Graphics2D g2, Rectangle2D meterArea, ValueDataset data, int type) {

		double minValue = 0.0;
		double maxValue = 0.0;
		Paint paint = null;

		switch (type) {

			case NORMAL_DATA_RANGE:
				minValue = this.normalRange.getLowerBound();
				maxValue = this.normalRange.getUpperBound();
				paint = getNormalPaint();
				break;

			case WARNING_DATA_RANGE:
				minValue = this.warningRange.getLowerBound();
				maxValue = this.warningRange.getUpperBound();
				paint = getWarningPaint();
				break;

			case CRITICAL_DATA_RANGE:
				minValue = this.criticalRange.getLowerBound();
				maxValue = this.criticalRange.getUpperBound();
				paint = getCriticalPaint();
				break;

			case FULL_DATA_RANGE:
				minValue = this.range.getLowerBound();
				maxValue = this.range.getUpperBound();
				paint = DEFAULT_BACKGROUND_PAINT;
				break;

			default:
				return;
		}

		// if (data.getBorderType() == type) {
		// drawArc(g2, meterArea,
		// minValue.doubleValue(),
		// data.getMinimumValue().doubleValue(),
		// paint);
		// drawArc(g2, meterArea,
		// data.getMaximumValue().doubleValue(),
		// maxValue.doubleValue(),
		// paint);
		// }
		// else {
		drawArc(g2, meterArea, minValue, maxValue, paint);
		// }

		// draw a tick at each end of the range...
		drawTick(g2, meterArea, minValue, true, paint);
		drawTick(g2, meterArea, maxValue, true, paint);
		// }

	}

	/**
	 * Draws an arc.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the plot area.
	 * @param minValue
	 *           the minimum value.
	 * @param maxValue
	 *           the maximum value.
	 * @param paint
	 *           the paint.
	 */
	protected void drawArc(Graphics2D g2, Rectangle2D area, double minValue, double maxValue,
									Paint paint) {
		drawArc(g2, area, minValue, maxValue, paint, 0);
	}

	/**
	 * Draws an arc.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the plot area.
	 * @param minValue
	 *           the minimum value.
	 * @param maxValue
	 *           the maximum value.
	 * @param paint
	 *           the paint.
	 * @param outlineType
	 *           the outline type.
	 */
	protected void drawArc(Graphics2D g2, Rectangle2D area, double minValue, double maxValue,
						Paint paint, int outlineType) {

		double startAngle = calculateAngle(maxValue);
		double endAngle = calculateAngle(minValue);
		double extent = endAngle - startAngle;

		double x = area.getX();
		double y = area.getY();
		double w = area.getWidth();
		double h = area.getHeight();
		g2.setPaint(paint);

		if (outlineType > 0) {
			g2.setStroke(new BasicStroke(10.0f));
		} else {
			g2.setStroke(new BasicStroke(DEFAULT_BORDER_SIZE));
		}

		int joinType = Arc2D.OPEN;
		if (outlineType > 0) {
			if (this.shape == DialShape.PIE) {
				joinType = Arc2D.PIE;
			} else
				if (this.shape == DialShape.CHORD) {
					if (this.meterAngle > 180) {
						joinType = Arc2D.CHORD;
					} else {
						joinType = Arc2D.PIE;
					}
				} else
					if (this.shape == DialShape.CIRCLE) {
						joinType = Arc2D.PIE;
						extent = 360;
					} else {
						throw new IllegalStateException(
											"MeterPlot.drawArc(...): dialType not recognised.");
					}
		}
		Arc2D.Double arc = new Arc2D.Double(x, y, w, h, startAngle, extent, joinType);
		if (outlineType > 0) {
			g2.fill(arc);
		} else {
			g2.draw(arc);
		}

	}

	/**
	 * Calculate an angle ???
	 * 
	 * @param value
	 *           the value.
	 * @return the result.
	 */
	double calculateAngle(double value) {
		value -= this.minMeterValue;
		double ret = this.meterCalcAngle - ((value / this.meterRange) * this.meterAngle);
		return ret;
	}

	/**
	 * Draws the ticks.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param meterArea
	 *           the meter area.
	 * @param minValue
	 *           the minimum value.
	 * @param maxValue
	 *           the maximum value.
	 */
	protected void drawTicks(Graphics2D g2, Rectangle2D meterArea, double minValue,
										double maxValue) {

		int numberOfTicks = 20;
		double diff = (maxValue - minValue) / numberOfTicks;

		for (double i = minValue; i <= maxValue; i += diff) {
			drawTick(g2, meterArea, i);
		}

	}

	/**
	 * Draws a tick.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param meterArea
	 *           the meter area.
	 * @param value
	 *           the value.
	 */
	protected void drawTick(Graphics2D g2, Rectangle2D meterArea, double value) {
		drawTick(g2, meterArea, value, false, null, false, null);
	}

	/**
	 * Draws a tick.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param meterArea
	 *           the meter area.
	 * @param value
	 *           the value.
	 * @param label
	 *           the label.
	 * @param color
	 *           the color.
	 */
	protected void drawTick(Graphics2D g2, Rectangle2D meterArea, double value, boolean label,
										Paint color) {
		drawTick(g2, meterArea, value, label, color, false, null);
	}

	/**
	 * Draws a tick on the chart (also handles a special case [curValue=true] that draws the
	 * value in the middle of the dial).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param meterArea
	 *           the meter area.
	 * @param value
	 *           the tick value.
	 * @param label
	 *           a flag that controls whether or not a value label is drawn.
	 * @param labelPaint
	 *           the label color.
	 * @param curValue
	 *           a flag for the special case of the current value.
	 * @param units
	 *           the unit-of-measure for the dial.
	 */
	protected void drawTick(Graphics2D g2, Rectangle2D meterArea,
						double value, boolean label, Paint labelPaint, boolean curValue, String units) {

		double valueAngle = calculateAngle(value);

		double meterMiddleX = meterArea.getCenterX();
		double meterMiddleY = meterArea.getCenterY();

		if (labelPaint == null) {
			labelPaint = Color.white;
		}
		g2.setPaint(labelPaint);
		g2.setStroke(new BasicStroke(2.0f));

		double valueP2X = 0;
		double valueP2Y = 0;

		if (!curValue) {
			double radius = (meterArea.getWidth() / 2) + DEFAULT_BORDER_SIZE;
			double radius1 = radius - 15;

			double valueP1X = meterMiddleX + (radius * Math.cos(Math.PI * (valueAngle / 180)));
			double valueP1Y = meterMiddleY - (radius * Math.sin(Math.PI * (valueAngle / 180)));

			valueP2X = meterMiddleX + (radius1 * Math.cos(Math.PI * (valueAngle / 180)));
			valueP2Y = meterMiddleY - (radius1 * Math.sin(Math.PI * (valueAngle / 180)));

			Line2D.Double line = new Line2D.Double(valueP1X, valueP1Y, valueP2X, valueP2Y);
			g2.draw(line);
		} else {
			valueP2X = meterMiddleX;
			valueP2Y = meterMiddleY;
			valueAngle = 90;
		}

		if (this.tickLabelType == VALUE_LABELS && label) {

			String tickLabel = this.tickLabelFormat.format(value);
			if (curValue && units != null) {
				tickLabel += " " + units;
			}
			if (curValue) {
				g2.setFont(getValueFont());
			} else {
				if (this.tickLabelFont != null) {
					g2.setFont(this.tickLabelFont);
				}
			}

			FontMetrics fm = g2.getFontMetrics();
			Rectangle2D tickLabelBounds = TextUtilities.getTextBounds(tickLabel, g2, fm);

			double x = valueP2X;
			double y = valueP2Y;
			if (curValue) {
				y += DEFAULT_CIRCLE_SIZE;
			}
			if (valueAngle == 90 || valueAngle == 270) {
				x = x - tickLabelBounds.getWidth() / 2;
			} else
				if (valueAngle < 90 || valueAngle > 270) {
					x = x - tickLabelBounds.getWidth();
				}
			if ((valueAngle > 135 && valueAngle < 225) || valueAngle > 315 || valueAngle < 45) {
				y = y - tickLabelBounds.getHeight() / 2;
			} else {
				y = y + tickLabelBounds.getHeight() / 2;
			}
			g2.drawString(tickLabel, (float) x, (float) y);
		}
	}

	/**
	 * Returns a short string describing the type of plot.
	 * 
	 * @return always <i>Meter Plot</i>.
	 */
	public String getPlotType() {
		return localizationResources.getString("Meter_Plot");
	}

	/**
	 * A zoom method that does nothing. Plots are required to support the zoom operation. In the
	 * case of a meter plot, it doesn't make sense to zoom in or out, so the method is empty.
	 * 
	 * @param percent
	 *           The zoom percentage.
	 */
	public void zoom(double percent) {
		// intentionally blank
	}

	/**
	 * Tests the plot for equality with an arbitrary object. Note that the dataset is ignored
	 * for the purposes of testing equality.
	 * 
	 * @param object
	 *           the object (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (object instanceof MeterPlot && super.equals(object)) {
			MeterPlot p = (MeterPlot) object;
			// private ValueDataset dataset <-- ignored
			if (!ObjectUtils.equal(this.units, p.units)) {
				return false;
			}
			if (!ObjectUtils.equal(this.range, p.range)) {
				return false;
			}
			if (!ObjectUtils.equal(this.normalRange, p.normalRange)) {
				return false;
			}
			if (!ObjectUtils.equal(this.warningRange, p.warningRange)) {
				return false;
			}
			if (!ObjectUtils.equal(this.criticalRange, p.criticalRange)) {
				return false;
			}
			if (!ObjectUtils.equal(this.dialOutlinePaint, p.dialOutlinePaint)) {
				return false;
			}
			if (!ObjectUtils.equal(this.normalPaint, p.normalPaint)) {
				return false;
			}
			if (!ObjectUtils.equal(this.warningPaint, p.warningPaint)) {
				return false;
			}
			if (!ObjectUtils.equal(this.criticalPaint, p.criticalPaint)) {
				return false;
			}
			if (this.shape != p.shape) {
				return false;
			}
			if (!ObjectUtils.equal(this.dialBackgroundPaint, p.dialBackgroundPaint)) {
				return false;
			}
			if (!ObjectUtils.equal(this.needlePaint, p.needlePaint)) {
				return false;
			}
			if (!ObjectUtils.equal(this.valueFont, p.valueFont)) {
				return false;
			}
			if (!ObjectUtils.equal(this.valuePaint, p.valuePaint)) {
				return false;
			}
			if (this.tickLabelType != p.tickLabelType) {
				return false;
			}
			if (!ObjectUtils.equal(this.tickLabelFont, p.tickLabelFont)) {
				return false;
			}
			if (!ObjectUtils.equal(this.tickLabelFormat, p.tickLabelFormat)) {
				return false;
			}
			if (this.drawBorder != p.drawBorder) {
				return false;
			}
			if (this.meterAngle != p.meterAngle) {
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
	 *           the output stream.
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		SerialUtilities.writePaint(this.criticalPaint, stream);
		SerialUtilities.writePaint(this.dialBackgroundPaint, stream);
		SerialUtilities.writePaint(this.needlePaint, stream);
		SerialUtilities.writePaint(this.normalPaint, stream);
		SerialUtilities.writePaint(this.valuePaint, stream);
		SerialUtilities.writePaint(this.warningPaint, stream);
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
		this.criticalPaint = SerialUtilities.readPaint(stream);
		this.dialBackgroundPaint = SerialUtilities.readPaint(stream);
		this.needlePaint = SerialUtilities.readPaint(stream);
		this.normalPaint = SerialUtilities.readPaint(stream);
		this.valuePaint = SerialUtilities.readPaint(stream);
		this.warningPaint = SerialUtilities.readPaint(stream);

		if (this.dataset != null) {
			this.dataset.addChangeListener(this);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// DEPRECATED
	// /////////////////////////////////////////////////////////////////////////////////////////////

	/** Constant for meter type 'pie'. */
	public static final int DIALTYPE_PIE = 0;

	/** Constant for meter type 'circle'. */
	public static final int DIALTYPE_CIRCLE = 1;

	/** Constant for meter type 'chord'. */
	public static final int DIALTYPE_CHORD = 2;

	/**
	 * Returns the type of dial (DIALTYPE_PIE, DIALTYPE_CIRCLE, DIALTYPE_CHORD).
	 * 
	 * @return The dial type.
	 * @deprecated Use getDialShape().
	 */
	public int getDialType() {
		if (this.shape == DialShape.CIRCLE) {
			return MeterPlot.DIALTYPE_CIRCLE;
		} else
			if (this.shape == DialShape.CHORD) {
				return MeterPlot.DIALTYPE_CHORD;
			} else
				if (this.shape == DialShape.PIE) {
					return MeterPlot.DIALTYPE_PIE;
				} else {
					throw new IllegalStateException("MeterPlot.getDialType: unrecognised dial type.");
				}
	}

	/**
	 * Sets the dial type (background shape).
	 * <P>
	 * This controls the shape of the dial background. Use one of the constants: DIALTYPE_PIE, DIALTYPE_CIRCLE, or DIALTYPE_CHORD.
	 * 
	 * @param type
	 *           The dial type.
	 * @deprecated Use setDialShape(...).
	 */
	public void setDialType(int type) {
		switch (type) {
			case MeterPlot.DIALTYPE_CIRCLE:
				setDialShape(DialShape.CIRCLE);
				break;
			case MeterPlot.DIALTYPE_CHORD:
				setDialShape(DialShape.CHORD);
				break;
			case MeterPlot.DIALTYPE_PIE:
				setDialShape(DialShape.PIE);
				break;
			default:
				throw new IllegalArgumentException("MeterPlot.setDialType: unrecognised type.");
		}
	}

	/**
	 * Correct cloning support, management of deeper copies and listeners
	 * 
	 * @see Plot#clone()
	 */
	public Object clone() throws CloneNotSupportedException {
		MeterPlot clone = (MeterPlot) super.clone();

		if (clone.dataset != null) {
			clone.dataset.addChangeListener(clone);
		}

		// range immutable -> OK
		// DialShape immutable -> OK
		// private DialShape shape = DialShape.CIRCLE;

		return clone;
	}

}
