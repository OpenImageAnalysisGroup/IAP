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
 * Plot.java
 * ---------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Sylvain Vieujot;
 * Jeremy Bowman;
 * Andreas Schneider;
 * Gideon Krause;
 * Nicolas Brodu;
 * $Id: Plot.java,v 1.1 2011-01-31 09:02:10 klukas Exp $
 * Changes (from 21-Jun-2001)
 * --------------------------
 * 21-Jun-2001 : Removed redundant JFreeChart parameter from constructors (DG);
 * 18-Sep-2001 : Updated header info and fixed DOS encoding problem (DG);
 * 19-Oct-2001 : Moved series paint and stroke methods from JFreeChart class (DG);
 * 23-Oct-2001 : Created renderer for LinePlot class (DG);
 * 07-Nov-2001 : Changed type names for ChartChangeEvent (DG);
 * Tidied up some Javadoc comments (DG);
 * 13-Nov-2001 : Changes to allow for null axes on plots such as PiePlot (DG);
 * Added plot/axis compatibility checks (DG);
 * 12-Dec-2001 : Changed constructors to protected, and removed unnecessary 'throws' clauses (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 22-Jan-2002 : Added handleClick(...) method, as part of implementation for crosshairs (DG);
 * Moved tooltips reference into ChartInfo class (DG);
 * 23-Jan-2002 : Added test for null axes in chartChanged(...) method, thanks to Barry Evans for
 * the bug report (number 506979 on SourceForge) (DG);
 * Added a zoom(...) method (DG);
 * 05-Feb-2002 : Updated setBackgroundPaint(), setOutlineStroke() and setOutlinePaint() to better
 * handle null values, as suggested by Sylvain Vieujot (DG);
 * 06-Feb-2002 : Added background image, plus alpha transparency for background and foreground (DG);
 * 06-Mar-2002 : Added AxisConstants interface (DG);
 * 26-Mar-2002 : Changed zoom method from empty to abstract (DG);
 * 23-Apr-2002 : Moved dataset from JFreeChart class (DG);
 * 11-May-2002 : Added ShapeFactory interface for getShape() methods, contributed by Jeremy
 * Bowman (DG);
 * 28-May-2002 : Fixed bug in setSeriesPaint(int, Paint) for subplots (AS);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 30-Jul-2002 : Added 'no data' message for charts with null or empty datasets (DG);
 * 21-Aug-2002 : Added code to extend series array if necessary (refer to SourceForge bug
 * id 594547 for details) (DG);
 * 17-Sep-2002 : Fixed bug in getSeriesOutlineStroke(...) method, reported by Andreas
 * Schroeder (DG);
 * 23-Sep-2002 : Added getLegendItems() abstract method (DG);
 * 24-Sep-2002 : Removed firstSeriesIndex, subplots now use their own paint settings, there is a
 * new mechanism for the legend to collect the legend items (DG);
 * 27-Sep-2002 : Added dataset group (DG);
 * 14-Oct-2002 : Moved listener storage into EventListenerList. Changed some abstract methods
 * to empty implementations (DG);
 * 28-Oct-2002 : Added a getBackgroundImage() method (DG);
 * 21-Nov-2002 : Added a plot index for identifying subplots in combined and overlaid charts (DG);
 * 22-Nov-2002 : Changed all attributes from 'protected' to 'private'. Added dataAreaRatio
 * attribute from David M O'Donnell's code (DG);
 * 09-Jan-2003 : Integrated fix for plot border contributed by Gideon Krause (DG);
 * 17-Jan-2003 : Moved to com.jrefinery.chart.plot (DG);
 * 23-Jan-2003 : Removed one constructor (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 14-Jul-2003 : Moved the dataset and secondaryDataset attributes to the CategoryPlot and
 * XYPlot classes (DG);
 * 21-Jul-2003 : Moved DrawingSupplier from CategoryPlot and XYPlot up to this class (DG);
 * 20-Aug-2003 : Implemented Cloneable (DG);
 * 11-Sep-2003 : Listeners and clone (NB);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 03-Dec-2003 : Modified draw method to accept anchor (DG);
 * 12-Mar-2004 : Fixed clipping bug in drawNoDataMessage() method (DG);
 * 07-Apr-2004 : Modified string bounds calculation (DG);
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.event.EventListenerList;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.data.DatasetChangeEvent;
import org.jfree.data.DatasetChangeListener;
import org.jfree.data.DatasetGroup;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.Align;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtils;

/**
 * The base class for all plots in JFreeChart. The {@link org.jfree.chart.JFreeChart} class
 * delegates the drawing of axes and data to the plot. This base class provides facilities common
 * to most plot types.
 */
public abstract class Plot implements AxisChangeListener,
													DatasetChangeListener,
													Serializable,
													Cloneable {

	/** Useful constant representing zero. */
	public static final Number ZERO = new Integer(0);

	/** The default insets. */
	public static final Insets DEFAULT_INSETS = new Insets(4, 8, 4, 8);

	/** The default outline stroke. */
	public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(0.5f);

	/** The default outline color. */
	public static final Paint DEFAULT_OUTLINE_PAINT = Color.gray;

	/** The default foreground alpha transparency. */
	public static final float DEFAULT_FOREGROUND_ALPHA = 1.0f;

	/** The default background alpha transparency. */
	public static final float DEFAULT_BACKGROUND_ALPHA = 1.0f;

	/** The default background color. */
	public static final Paint DEFAULT_BACKGROUND_PAINT = Color.white;

	/** The minimum width at which the plot should be drawn. */
	public static final int MINIMUM_WIDTH_TO_DRAW = 10;

	/** The minimum height at which the plot should be drawn. */
	public static final int MINIMUM_HEIGHT_TO_DRAW = 10;

	/** The parent plot (<code>null</code> if this is the root plot). */
	private Plot parent;

	/** The dataset group (to be used for thread synchronisation). */
	private DatasetGroup datasetGroup;

	/** The message to display if no data is available. */
	private String noDataMessage;

	/** The font used to display the 'no data' message. */
	private Font noDataMessageFont;

	/** The paint used to draw the 'no data' message. */
	private transient Paint noDataMessagePaint;

	/** Amount of blank space around the plot area. */
	private Insets insets;

	/** The Stroke used to draw an outline around the plot. */
	private transient Stroke outlineStroke;

	/** The Paint used to draw an outline around the plot. */
	private transient Paint outlinePaint;

	/** An optional color used to fill the plot background. */
	private transient Paint backgroundPaint;

	/** An optional image for the plot background. */
	private transient Image backgroundImage; // not currently serialized

	/** The alignment for the background image. */
	private int backgroundImageAlignment = Align.FIT;

	/** The alpha-transparency for the plot. */
	private float foregroundAlpha;

	/** The alpha transparency for the background paint. */
	private float backgroundAlpha;

	/** The drawing supplier. */
	private DrawingSupplier drawingSupplier;

	/** Storage for registered change listeners. */
	private transient EventListenerList listenerList;

	/**
	 * Defines dataArea rectangle as the ratio formed from dividing height by width
	 * (of the dataArea). Modifies plot area calculations.
	 * ratio>0 will attempt to layout the plot so that the
	 * dataArea.height/dataArea.width = ratio.
	 * ratio<0 will attempt to layout the plot so that the
	 * dataArea.height/dataArea.width in plot units (not java2D units as when ratio>0)
	 * = -1.*ratio.
	 */
	// dmo
	private double dataAreaRatio = 0.0; // zero when the parameter is not set

	/**
	 * Creates a new plot.
	 */
	protected Plot() {

		this.parent = null;
		// make sure, that no one modifies the global default insets.
		this.insets = new Insets(DEFAULT_INSETS.top, DEFAULT_INSETS.left,
							DEFAULT_INSETS.bottom, DEFAULT_INSETS.right);
		this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;
		this.backgroundAlpha = DEFAULT_BACKGROUND_ALPHA;
		this.backgroundImage = null;
		this.outlineStroke = DEFAULT_OUTLINE_STROKE;
		this.outlinePaint = DEFAULT_OUTLINE_PAINT;
		this.foregroundAlpha = DEFAULT_FOREGROUND_ALPHA;

		this.noDataMessage = null;
		this.noDataMessageFont = new Font("SansSerif", Font.PLAIN, 12);
		this.noDataMessagePaint = Color.black;

		this.drawingSupplier = new DefaultDrawingSupplier();

		this.listenerList = new EventListenerList();

	}

	/**
	 * Returns the dataset group for the plot (not currently used).
	 * 
	 * @return The dataset group.
	 */
	public DatasetGroup getDatasetGroup() {
		return this.datasetGroup;
	}

	/**
	 * Sets the dataset group (not currently used).
	 * 
	 * @param group
	 *           the dataset group (<code>null</code> permitted).
	 */
	protected void setDatasetGroup(DatasetGroup group) {
		this.datasetGroup = group;
	}

	/**
	 * Returns the string that is displayed when the dataset is empty or <code>null</code>.
	 * 
	 * @return The 'no data' message (<code>null</code> possible).
	 */
	public String getNoDataMessage() {
		return this.noDataMessage;
	}

	/**
	 * Sets the message that is displayed when the dataset is empty or null.
	 * 
	 * @param message
	 *           the message (null permitted).
	 */
	public void setNoDataMessage(String message) {
		this.noDataMessage = message;
	}

	/**
	 * Returns the font used to display the 'no data' message.
	 * 
	 * @return the font.
	 */
	public Font getNoDataMessageFont() {
		return this.noDataMessageFont;
	}

	/**
	 * Sets the font used to display the 'no data' message.
	 * 
	 * @param font
	 *           the font.
	 */
	public void setNoDataMessageFont(Font font) {
		this.noDataMessageFont = font;
	}

	/**
	 * Returns the paint used to display the 'no data' message.
	 * 
	 * @return the paint.
	 */
	public Paint getNoDataMessagePaint() {
		return this.noDataMessagePaint;
	}

	/**
	 * Sets the paint used to display the 'no data' message.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setNoDataMessagePaint(Paint paint) {
		this.noDataMessagePaint = paint;
	}

	/**
	 * Returns a short string describing the plot type.
	 * <P>
	 * Note: this gets used in the chart property editing user interface, but there needs to be a better mechanism for identifying the plot type.
	 * 
	 * @return a short string describing the plot type.
	 */
	public abstract String getPlotType();

	/**
	 * Returns the parent plot (or null if this plot is not part of a combined plot).
	 * 
	 * @return the parent plot.
	 */
	public Plot getParent() {
		return this.parent;
	}

	/**
	 * Sets the parent plot.
	 * 
	 * @param parent
	 *           the parent plot.
	 */
	public void setParent(Plot parent) {
		this.parent = parent;
	}

	/**
	 * Returns the root plot.
	 * 
	 * @return The root plot.
	 */
	public Plot getRootPlot() {

		Plot p = getParent();
		if (p == null) {
			return this;
		} else {
			return p.getRootPlot();
		}

	}

	/**
	 * Returns true if this plot is part of a combined plot structure.
	 * 
	 * @return <code>true</code> if this plot is part of a combined plot structure.
	 */
	public boolean isSubplot() {
		return (getParent() != null);
	}

	/**
	 * Returns the insets for the plot area.
	 * 
	 * @return The insets.
	 */
	public Insets getInsets() {
		return this.insets;
	}

	/**
	 * Sets the insets for the plot and notifies registered listeners that the
	 * plot has been modified.
	 * 
	 * @param insets
	 *           the new insets.
	 */
	public void setInsets(Insets insets) {
		setInsets(insets, true);
	}

	/**
	 * Sets the insets for the plot and, if requested, notifies registered listeners that the
	 * plot has been modified.
	 * 
	 * @param insets
	 *           the new insets (<code>null</code> not permitted).
	 * @param notify
	 *           a flag that controls whether the registered listeners are notified.
	 */
	public void setInsets(Insets insets, boolean notify) {
		if (insets == null) {
			throw new IllegalArgumentException("Null 'insets' argument.");
		}
		if (!this.insets.equals(insets)) {
			this.insets = insets;
			if (notify) {
				notifyListeners(new PlotChangeEvent(this));
			}
		}

	}

	/**
	 * Returns the background color of the plot area.
	 * 
	 * @return the paint (possibly <code>null</code>).
	 */
	public Paint getBackgroundPaint() {
		return this.backgroundPaint;
	}

	/**
	 * Sets the background color of the plot area and sends a {@link PlotChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setBackgroundPaint(Paint paint) {

		if (paint == null) {
			if (this.backgroundPaint != null) {
				this.backgroundPaint = null;
				notifyListeners(new PlotChangeEvent(this));
			}
		} else {
			if (this.backgroundPaint != null) {
				if (this.backgroundPaint.equals(paint)) {
					return; // nothing to do
				}
			}
			this.backgroundPaint = paint;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the alpha transparency of the plot area background.
	 * 
	 * @return the alpha transparency.
	 */
	public float getBackgroundAlpha() {
		return this.backgroundAlpha;
	}

	/**
	 * Sets the alpha transparency of the plot area background, and notifies
	 * registered listeners that the plot has been modified.
	 * 
	 * @param alpha
	 *           the new alpha value.
	 */
	public void setBackgroundAlpha(float alpha) {

		if (this.backgroundAlpha != alpha) {
			this.backgroundAlpha = alpha;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the drawing supplier for the plot.
	 * 
	 * @return the drawing supplier (possibly <code>null</code>).
	 */
	public DrawingSupplier getDrawingSupplier() {
		DrawingSupplier result = null;
		Plot p = getParent();
		if (p != null) {
			result = p.getDrawingSupplier();
		} else {
			result = this.drawingSupplier;
		}
		return result;
	}

	/**
	 * Sets the drawing supplier for the plot. The drawing supplier is responsible for
	 * supplying a limitless (possibly repeating) sequence of <code>Paint</code>, <code>Stroke</code> and <code>Shape</code> objects that the plot's renderer(s)
	 * can use
	 * to populate its(their) tables.
	 * 
	 * @param supplier
	 *           the new supplier.
	 */
	public void setDrawingSupplier(DrawingSupplier supplier) {
		this.drawingSupplier = supplier;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the background image that is used to fill the plot's background area.
	 * 
	 * @return The image (possibly <code>null</code>).
	 */
	public Image getBackgroundImage() {
		return this.backgroundImage;
	}

	/**
	 * Sets the background image for the plot.
	 * 
	 * @param image
	 *           the image (<code>null</code> permitted).
	 */
	public void setBackgroundImage(Image image) {
		this.backgroundImage = image;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the background image alignment. Alignment constants are defined in the <code>org.jfree.ui.Align</code> class in the JCommon class library.
	 * 
	 * @return The alignment.
	 */
	public int getBackgroundImageAlignment() {
		return this.backgroundImageAlignment;
	}

	/**
	 * Sets the background alignment.
	 * <p>
	 * Alignment options are defined by the {@link org.jfree.ui.Align} class.
	 * 
	 * @param alignment
	 *           the alignment.
	 */
	public void setBackgroundImageAlignment(int alignment) {
		if (this.backgroundImageAlignment != alignment) {
			this.backgroundImageAlignment = alignment;
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Returns the stroke used to outline the plot area.
	 * 
	 * @return the stroke (possibly <code>null</code>).
	 */
	public Stroke getOutlineStroke() {
		return this.outlineStroke;
	}

	/**
	 * Sets the stroke used to outline the plot area and sends a {@link PlotChangeEvent} to all
	 * registered listeners. If you set this attribute to <code>null<.code>, no outline will be 
     * drawn.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 */
	public void setOutlineStroke(Stroke stroke) {

		if (stroke == null) {
			if (this.outlineStroke != null) {
				this.outlineStroke = null;
				notifyListeners(new PlotChangeEvent(this));
			}
		} else {
			if (this.outlineStroke != null) {
				if (this.outlineStroke.equals(stroke)) {
					return; // nothing to do
				}
			}
			this.outlineStroke = stroke;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the color used to draw the outline of the plot area.
	 * 
	 * @return The color (possibly <code>null<code>).
	 */
	public Paint getOutlinePaint() {
		return this.outlinePaint;
	}

	/**
	 * Sets the paint used to draw the outline of the plot area and sends a {@link PlotChangeEvent} to all registered listeners. If you set this attribute to
	 * <code>null</code>, no outline
	 * will be drawn.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setOutlinePaint(Paint paint) {

		if (paint == null) {
			if (this.outlinePaint != null) {
				this.outlinePaint = null;
				notifyListeners(new PlotChangeEvent(this));
			}
		} else {
			if (this.outlinePaint != null) {
				if (this.outlinePaint.equals(paint)) {
					return; // nothing to do
				}
			}
			this.outlinePaint = paint;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the alpha-transparency for the plot foreground.
	 * 
	 * @return the alpha-transparency.
	 */
	public float getForegroundAlpha() {
		return this.foregroundAlpha;
	}

	/**
	 * Sets the alpha-transparency for the plot.
	 * 
	 * @param alpha
	 *           the new alpha transparency.
	 */
	public void setForegroundAlpha(float alpha) {

		if (this.foregroundAlpha != alpha) {
			this.foregroundAlpha = alpha;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the legend items for the plot. By default, this method returns <code>null</code>.
	 * Subclasses should override to return a {@link LegendItemCollection}.
	 * 
	 * @return The legend items for the plot (possibly <code>null</code>).
	 */
	public LegendItemCollection getLegendItems() {
		return null;
	}

	/**
	 * Registers an object for notification of changes to the plot.
	 * 
	 * @param listener
	 *           the object to be registered.
	 */
	public void addChangeListener(PlotChangeListener listener) {
		this.listenerList.add(PlotChangeListener.class, listener);
	}

	/**
	 * Unregisters an object for notification of changes to the plot.
	 * 
	 * @param listener
	 *           the object to be unregistered.
	 */
	public void removeChangeListener(PlotChangeListener listener) {
		this.listenerList.remove(PlotChangeListener.class, listener);
	}

	/**
	 * Notifies all registered listeners that the plot has been modified.
	 * 
	 * @param event
	 *           information about the change event.
	 */
	public void notifyListeners(PlotChangeEvent event) {

		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == PlotChangeListener.class) {
				((PlotChangeListener) listeners[i + 1]).plotChanged(event);
			}
		}

	}

	/**
	 * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
	 * <P>
	 * This class does not store any information about where the individual items that make up the plot are actually drawn. If you want to collect this
	 * information, pass in a ChartRenderingInfo object. After the drawing is complete, the info object will contain lots of information about the chart. If you
	 * don't want the information, pass in null. *
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the area within which the plot should be drawn.
	 * @param parentState
	 *           the state from the parent plot, if there is one.
	 * @param info
	 *           an object for collecting information about the drawing of the chart.
	 */
	public abstract void draw(Graphics2D g2,
										Rectangle2D plotArea,
										PlotState parentState,
										PlotRenderingInfo info);

	/**
	 * Implement later to make use of anchor.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the plot area.
	 * @param anchor
	 *           the anchor point.
	 * @param parentState
	 *           the parent state (if any).
	 * @param info
	 *           carries back plot rendering info.
	 */
	public void draw(Graphics2D g2,
							Rectangle2D area,
							Point2D anchor,
							PlotState parentState,
							PlotRenderingInfo info) {
		draw(g2, area, parentState, info);
	}

	/**
	 * Draws the plot background (the background color and/or image).
	 * <P>
	 * This method will be called during the chart drawing process and is declared public so that it can be accessed by the renderers used by certain subclasses.
	 * You shouldn't need to call this method directly.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area within which the plot should be drawn.
	 */
	public void drawBackground(Graphics2D g2, Rectangle2D area) {
		fillBackground(g2, area);
		drawBackgroundImage(g2, area);
	}

	/**
	 * Fills the specified area with the background paint.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area.
	 */
	protected void fillBackground(Graphics2D g2, Rectangle2D area) {
		if (this.backgroundPaint != null) {
			Composite originalComposite = g2.getComposite();
			g2.setComposite(
								AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.backgroundAlpha)
								);
			g2.setPaint(this.backgroundPaint);
			g2.fill(area);
			g2.setComposite(originalComposite);
		}
	}

	/**
	 * Draws the background image (if there is one) aligned within the specified area.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area.
	 */
	protected void drawBackgroundImage(Graphics2D g2, Rectangle2D area) {
		if (this.backgroundImage != null) {
			Composite originalComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, this.backgroundAlpha));
			Rectangle2D dest = new Rectangle2D.Double(
								0.0, 0.0,
								this.backgroundImage.getWidth(null), this.backgroundImage.getHeight(null)
								);
			Align.align(dest, area, this.backgroundImageAlignment);
			g2.drawImage(
								this.backgroundImage,
								(int) dest.getX(), (int) dest.getY(),
								(int) dest.getWidth() + 1, (int) dest.getHeight() + 1, null
								);
			g2.setComposite(originalComposite);
		}
	}

	/**
	 * Draws the plot outline. This method will be called during the chart drawing process and is
	 * declared public so that it can be accessed by the renderers used by certain subclasses.
	 * You shouldn't need to call this method directly.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area within which the plot should be drawn.
	 */
	public void drawOutline(Graphics2D g2, Rectangle2D area) {
		if ((this.outlineStroke != null) && (this.outlinePaint != null)) {
			g2.setStroke(this.outlineStroke);
			g2.setPaint(this.outlinePaint);
			g2.draw(area);
		}
	}

	/**
	 * Draws a message to state that there is no data to plot.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param area
	 *           the area within which the plot should be drawn.
	 */
	protected void drawNoDataMessage(Graphics2D g2, Rectangle2D area) {

		Shape savedClip = g2.getClip();
		g2.clip(area);
		String message = this.noDataMessage;
		if (message != null) {
			g2.setFont(this.noDataMessageFont);
			g2.setPaint(this.noDataMessagePaint);
			FontMetrics fm = g2.getFontMetrics(this.noDataMessageFont);
			Rectangle2D bounds = TextUtilities.getTextBounds(message, g2, fm);
			float x = (float) (area.getX() + area.getWidth() / 2 - bounds.getWidth() / 2);
			float y = (float) (area.getMinY() + (area.getHeight() / 2) - (bounds.getHeight() / 2));
			g2.drawString(message, x, y);
		}
		g2.setClip(savedClip);

	}

	/**
	 * Handles a 'click' on the plot. Since the plot does not maintain any
	 * information about where it has been drawn, the plot rendering info is supplied as
	 * an argument.
	 * 
	 * @param x
	 *           the x coordinate (in Java2D space).
	 * @param y
	 *           the y coordinate (in Java2D space).
	 * @param info
	 *           an object containing information about the dimensions of the plot.
	 */
	public void handleClick(int x, int y, PlotRenderingInfo info) {
		// provides a 'no action' default
	}

	/**
	 * Performs a zoom on the plot. Subclasses should override if zooming is appropriate for
	 * the type of plot.
	 * 
	 * @param percent
	 *           the zoom percentage.
	 */
	public void zoom(double percent) {
		// do nothing by default.
	}

	/**
	 * Receives notification of a change to one of the plot's axes.
	 * 
	 * @param event
	 *           information about the event (not used here).
	 */
	public void axisChanged(AxisChangeEvent event) {
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Receives notification of a change to the plot's dataset.
	 * <P>
	 * The plot reacts by passing on a plot change event to all registered listeners.
	 * 
	 * @param event
	 *           information about the event (not used here).
	 */
	public void datasetChanged(DatasetChangeEvent event) {

		PlotChangeEvent newEvent = new PlotChangeEvent(this);
		notifyListeners(newEvent);

	}

	/**
	 * Adjusts the supplied x-value.
	 * 
	 * @param x
	 *           the x-value.
	 * @param w1
	 *           width 1.
	 * @param w2
	 *           width 2.
	 * @param edge
	 *           the edge (left or right).
	 * @return the adjusted x-value.
	 */
	protected double getRectX(double x, double w1, double w2, RectangleEdge edge) {

		double result = x;
		if (edge == RectangleEdge.LEFT) {
			result = result + w1;
		} else
			if (edge == RectangleEdge.RIGHT) {
				result = result + w2;
			}
		return result;

	}

	/**
	 * Adjusts the supplied y-value.
	 * 
	 * @param y
	 *           the x-value.
	 * @param h1
	 *           height 1.
	 * @param h2
	 *           height 2.
	 * @param edge
	 *           the edge (top or bottom).
	 * @return the adjusted y-value.
	 */
	protected double getRectY(double y, double h1, double h2, RectangleEdge edge) {

		double result = y;
		if (edge == RectangleEdge.TOP) {
			result = result + h1;
		} else
			if (edge == RectangleEdge.BOTTOM) {
				result = result + h2;
			}
		return result;

	}

	/**
	 * Returns the data area ratio.
	 * 
	 * @return The ratio.
	 */
	public double getDataAreaRatio() {
		return this.dataAreaRatio;
	}

	/**
	 * Sets the data area ratio.
	 * 
	 * @param ratio
	 *           the ratio.
	 */
	public void setDataAreaRatio(double ratio) {
		this.dataAreaRatio = ratio;
	}

	/**
	 * Tests this plot for equality with another object.
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

		if (obj instanceof Plot) {

			Plot p = (Plot) obj;

			boolean b5 = ObjectUtils.equal(this.noDataMessage, p.noDataMessage);
			boolean b6 = ObjectUtils.equal(this.noDataMessageFont, p.noDataMessageFont);
			boolean b7 = ObjectUtils.equal(this.noDataMessagePaint, p.noDataMessagePaint);

			boolean b8 = ObjectUtils.equal(this.insets, p.insets);
			boolean b9 = ObjectUtils.equal(this.outlineStroke, p.outlineStroke);
			boolean b10 = ObjectUtils.equal(this.outlinePaint, p.outlinePaint);

			boolean b11 = ObjectUtils.equal(this.backgroundPaint, p.backgroundPaint);
			boolean b12 = ObjectUtils.equal(this.backgroundImage, p.backgroundImage);
			boolean b13 = (this.backgroundImageAlignment == p.backgroundImageAlignment);

			boolean b14 = (this.foregroundAlpha == p.foregroundAlpha);
			boolean b15 = (this.backgroundAlpha == p.backgroundAlpha);

			return b5 && b6 && b7 && b8 && b9
								&& b10 && b11 && b12 && b13 && b14 && b15;

		}

		return false;

	}

	/**
	 * Creates a clone of the plot.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if some component of the plot does not support cloning.
	 */
	public Object clone() throws CloneNotSupportedException {

		Plot clone = (Plot) super.clone();
		// private Plot parent <-- don't clone the parent plot, but take care childs in combined
		// plots instead
		clone.datasetGroup = (DatasetGroup) ObjectUtils.clone(this.datasetGroup);
		// private String noDataMessage <-- immutable
		// private Font noDataMessageFont <-- immutable
		// private transient Paint noDataMessagePaint <-- immutable
		// private Insets insets <-- immutable
		// private transient Stroke outlineStroke <-- immutable
		// private transient Paint outlinePaint <-- immutable
		// private transient Paint backgroundPaint <-- immutable
		// private transient Image backgroundImage <-- ???
		// private int backgroundImageAlignment<-- primitive
		// private float foregroundAlpha <-- primitive
		// private float backgroundAlpha <-- primitive
		clone.drawingSupplier = (DrawingSupplier) ObjectUtils.clone(this.drawingSupplier);
		// private transient EventListenerList listenerList <-- ???
		// private double dataAreaRatio <-- primitive

		clone.listenerList = new EventListenerList();
		return clone;

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
		SerialUtilities.writePaint(this.noDataMessagePaint, stream);
		SerialUtilities.writeStroke(this.outlineStroke, stream);
		SerialUtilities.writePaint(this.outlinePaint, stream);
		// backgroundImage
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
		this.noDataMessagePaint = SerialUtilities.readPaint(stream);
		this.outlineStroke = SerialUtilities.readStroke(stream);
		this.outlinePaint = SerialUtilities.readPaint(stream);
		// backgroundImage
		this.backgroundPaint = SerialUtilities.readPaint(stream);

		this.listenerList = new EventListenerList();

	}

	/**
	 * Resolves a domain axis location for a given plot orientation.
	 * 
	 * @param location
	 *           the location (<code>null</code> not permitted).
	 * @param orientation
	 *           the orientation (<code>null</code> not permitted).
	 * @return The edge (never <code>null</code>).
	 */
	public static RectangleEdge resolveDomainAxisLocation(AxisLocation location,
																				PlotOrientation orientation) {

		if (location == null) {
			throw new IllegalArgumentException("Null 'location' argument.");
		}
		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}

		RectangleEdge result = null;

		if (location == AxisLocation.TOP_OR_RIGHT) {
			if (orientation == PlotOrientation.HORIZONTAL) {
				result = RectangleEdge.RIGHT;
			} else
				if (orientation == PlotOrientation.VERTICAL) {
					result = RectangleEdge.TOP;
				}
		} else
			if (location == AxisLocation.TOP_OR_LEFT) {
				if (orientation == PlotOrientation.HORIZONTAL) {
					result = RectangleEdge.LEFT;
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						result = RectangleEdge.TOP;
					}
			} else
				if (location == AxisLocation.BOTTOM_OR_RIGHT) {
					if (orientation == PlotOrientation.HORIZONTAL) {
						result = RectangleEdge.RIGHT;
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							result = RectangleEdge.BOTTOM;
						}
				} else
					if (location == AxisLocation.BOTTOM_OR_LEFT) {
						if (orientation == PlotOrientation.HORIZONTAL) {
							result = RectangleEdge.LEFT;
						} else
							if (orientation == PlotOrientation.VERTICAL) {
								result = RectangleEdge.BOTTOM;
							}
					}
		// the above should cover all the options...
		if (result == null) {
			throw new IllegalStateException("XYPlot.resolveDomainAxisLocation(...)");
		}
		return result;

	}

	/**
	 * Resolves a range axis location for a given plot orientation.
	 * 
	 * @param location
	 *           the location (<code>null</code> not permitted).
	 * @param orientation
	 *           the orientation (<code>null</code> not permitted).
	 * @return the edge (never <code>null</code>).
	 */
	public static RectangleEdge resolveRangeAxisLocation(AxisLocation location,
																			PlotOrientation orientation) {

		if (location == null) {
			throw new IllegalArgumentException("Null 'location' argument.");
		}
		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}

		RectangleEdge result = null;

		if (location == AxisLocation.TOP_OR_RIGHT) {
			if (orientation == PlotOrientation.HORIZONTAL) {
				result = RectangleEdge.TOP;
			} else
				if (orientation == PlotOrientation.VERTICAL) {
					result = RectangleEdge.RIGHT;
				}
		} else
			if (location == AxisLocation.TOP_OR_LEFT) {
				if (orientation == PlotOrientation.HORIZONTAL) {
					result = RectangleEdge.TOP;
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						result = RectangleEdge.LEFT;
					}
			} else
				if (location == AxisLocation.BOTTOM_OR_RIGHT) {
					if (orientation == PlotOrientation.HORIZONTAL) {
						result = RectangleEdge.BOTTOM;
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							result = RectangleEdge.RIGHT;
						}
				} else
					if (location == AxisLocation.BOTTOM_OR_LEFT) {
						if (orientation == PlotOrientation.HORIZONTAL) {
							result = RectangleEdge.BOTTOM;
						} else
							if (orientation == PlotOrientation.VERTICAL) {
								result = RectangleEdge.LEFT;
							}
					}

		// the above should cover all the options...
		if (result == null) {
			throw new IllegalStateException("XYPlot.resolveRangeAxisLocation(...)");
		}
		return result;

	}

	private boolean showPlot = true;

	public void setPlotShowing(boolean showPlot) {
		this.showPlot = showPlot;
	}

	public boolean isPlotShowing() {
		return showPlot;
	}

}
