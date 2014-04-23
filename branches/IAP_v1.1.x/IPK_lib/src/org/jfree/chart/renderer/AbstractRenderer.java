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
 * ---------------------
 * AbstractRenderer.java
 * ---------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Nicolas Brodu;
 * $Id: AbstractRenderer.java,v 1.1 2011-01-31 09:02:45 klukas Exp $
 * Changes:
 * --------
 * 22-Aug-2002 : Version 1, draws code out of AbstractXYItemRenderer to share with
 * AbstractCategoryItemRenderer (DG);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 06-Nov-2002 : Moved to the com.jrefinery.chart.renderer package (DG);
 * 21-Nov-2002 : Added a paint table for the renderer to use (DG);
 * 17-Jan-2003 : Moved plot classes into a separate package (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 29-Apr-2003 : Added valueLabelFont and valueLabelPaint attributes, based on code from
 * Arnaud Lelievre (DG);
 * 29-Jul-2003 : Amended code that doesn't compile with JDK 1.2.2 (DG);
 * 13-Aug-2003 : Implemented Cloneable (DG);
 * 15-Sep-2003 : Fixed serialization (NB);
 * 17-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Moved PlotRenderingInfo into RendererState to allow for multiple threads
 * using a single renderer (DG);
 * 20-Oct-2003 : Added missing setOutlinePaint(...) method (DG);
 * 23-Oct-2003 : Split item label attributes into 'positive' and 'negative' values (DG);
 * 26-Nov-2003 : Added methods to get the positive and negative item label positions (DG);
 * 01-Mar-2004 : Modified readObject() method to prevent null pointer exceptions after
 * deserialization (DG);
 */

package org.jfree.chart.renderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.event.EventListenerList;

import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.BooleanList;
import org.jfree.util.BooleanUtils;
import org.jfree.util.NumberUtils;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PaintList;
import org.jfree.util.ShapeList;
import org.jfree.util.ShapeUtils;
import org.jfree.util.StrokeList;

/**
 * Base class providing common services for renderers. Most methods that update attributes of the
 * renderer will fire a {@link RendererChangeEvent}, which normally means the plot that owns
 * the renderer will receive notification that the renderer has been changed (the plot will, in
 * turn, notify the chart).
 */
public abstract class AbstractRenderer implements Cloneable, Serializable {

	/** A useful constant. */
	public static final Double ZERO = new Double(0.0);

	/** The default paint. */
	public static final Paint DEFAULT_PAINT = Color.blue;

	/** The default outline paint. */
	public static final Paint DEFAULT_OUTLINE_PAINT = Color.gray;

	/** The default stroke. */
	public static final Stroke DEFAULT_STROKE = new BasicStroke(1.0f);

	/** The default outline stroke. */
	public static final Stroke DEFAULT_OUTLINE_STROKE = new BasicStroke(1.0f);

	/** The default shape. */
	public static final Shape DEFAULT_SHAPE = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);

	/** The default value label font. */
	public static final Font DEFAULT_VALUE_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);

	/** The default value label paint. */
	public static final Paint DEFAULT_VALUE_LABEL_PAINT = Color.black;

	/** The paint for ALL series (optional). */
	private transient Paint paint;

	/** The paint list. */
	private PaintList paintList;

	/** The base paint. */
	private transient Paint basePaint;

	/** The outline paint for ALL series (optional). */
	private transient Paint outlinePaint;

	/** The outline paint list. */
	private PaintList outlinePaintList;

	/** The base outline paint. */
	private transient Paint baseOutlinePaint;

	/** The stroke for ALL series (optional). */
	private transient Stroke stroke;

	/** The stroke list. */
	private StrokeList strokeList;

	/** The base stroke. */
	private transient Stroke baseStroke;

	/** The outline stroke for ALL series (optional). */
	private transient Stroke outlineStroke;

	/** The outline stroke list. */
	private StrokeList outlineStrokeList;

	/** The base outline stroke. */
	private transient Stroke baseOutlineStroke;

	/** The shape for ALL series (optional). */
	private transient Shape shape;

	/** A shape list. */
	private ShapeList shapeList;

	/** The base shape. */
	private transient Shape baseShape;

	/** Visibility of the item labels for ALL series (optional). */
	private Boolean itemLabelsVisible;

	/** Visibility of the item labels PER series. */
	private BooleanList itemLabelsVisibleList;

	/** The base item labels visible. */
	private Boolean baseItemLabelsVisible;

	/** The item label font for ALL series (optional). */
	private Font itemLabelFont;

	/** The item label font list (one font per series). */
	private ObjectList itemLabelFontList;

	/** The base item label font. */
	private Font baseItemLabelFont;

	/** The item label paint for ALL series. */
	private transient Paint itemLabelPaint;

	/** The item label paint list (one paint per series). */
	private PaintList itemLabelPaintList;

	/** The base item label paint. */
	private transient Paint baseItemLabelPaint;

	/** The positive item label position for ALL series (optional). */
	private ItemLabelPosition positiveItemLabelPosition;

	/** The positive item label position (per series). */
	private ObjectList positiveItemLabelPositionList;

	/** The fallback positive item label position. */
	private ItemLabelPosition basePositiveItemLabelPosition;

	/** The negative item label position for ALL series (optional). */
	private ItemLabelPosition negativeItemLabelPosition;

	/** The negative item label position (per series). */
	private ObjectList negativeItemLabelPositionList;

	/** The fallback negative item label position. */
	private ItemLabelPosition baseNegativeItemLabelPosition;

	/** The item label anchor offset. */
	private double itemLabelAnchorOffset = 2.0;

	/** Storage for registered change listeners. */
	private transient EventListenerList listenerList;

	/**
	 * Default constructor.
	 */
	public AbstractRenderer() {

		this.paint = null;
		this.paintList = new PaintList();
		this.basePaint = DEFAULT_PAINT;

		this.outlinePaint = null;
		this.outlinePaintList = new PaintList();
		this.baseOutlinePaint = DEFAULT_OUTLINE_PAINT;

		this.stroke = null;
		this.strokeList = new StrokeList();
		this.baseStroke = DEFAULT_STROKE;

		this.outlineStroke = null;
		this.outlineStrokeList = new StrokeList();
		this.baseOutlineStroke = DEFAULT_OUTLINE_STROKE;

		this.shape = null;
		this.shapeList = new ShapeList();
		this.baseShape = DEFAULT_SHAPE;

		this.itemLabelsVisible = null;
		this.itemLabelsVisibleList = new BooleanList();
		this.baseItemLabelsVisible = Boolean.FALSE;

		this.itemLabelFont = null;
		this.itemLabelFontList = new ObjectList();
		this.baseItemLabelFont = new Font("SansSerif", Font.PLAIN, 10);

		this.itemLabelPaint = null;
		this.itemLabelPaintList = new PaintList();
		this.baseItemLabelPaint = Color.black;

		this.positiveItemLabelPosition = null;
		this.positiveItemLabelPositionList = new ObjectList();
		this.basePositiveItemLabelPosition = new ItemLabelPosition(
							ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER
							);

		this.negativeItemLabelPosition = null;
		this.negativeItemLabelPositionList = new ObjectList();
		this.baseNegativeItemLabelPosition = new ItemLabelPosition(
							ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER
							);

		this.listenerList = new EventListenerList();

	}

	/**
	 * Returns the drawing supplier from the plot.
	 * 
	 * @return The drawing supplier.
	 */
	public abstract DrawingSupplier getDrawingSupplier();

	// PAINT

	/**
	 * Returns the paint used to fill data items as they are drawn.
	 * <p>
	 * The default implementation passes control to the <code>getSeriesPaint</code> method. You can override this method if you require different behaviour.
	 * 
	 * @param row
	 *           the row (or series) index (zero-based).
	 * @param column
	 *           the column (or category) index (zero-based).
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getItemPaint(int row, int column) {
		return getSeriesPaint(row);
	}

	/**
	 * Returns the paint used to fill an item drawn by the renderer.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getSeriesPaint(int series) {

		// return the override, if there is one...
		if (this.paint != null) {
			return this.paint;
		}

		// otherwise look up the paint list
		Paint seriesPaint = this.paintList.getPaint(series);
		if (seriesPaint == null) {
			DrawingSupplier supplier = getDrawingSupplier();
			if (supplier != null) {
				seriesPaint = supplier.getNextPaint();
				this.paintList.setPaint(series, seriesPaint);
			} else {
				seriesPaint = this.basePaint;
			}
		}
		return seriesPaint;

	}

	/**
	 * Sets the paint to be used for ALL series, and sends a {@link RendererChangeEvent} to all
	 * registered listeners. If this is <code>null</code>, the renderer will use the paint for
	 * the series.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setPaint(Paint paint) {
		setPaint(paint, true);
	}

	/**
	 * Sets the paint to be used for all series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setPaint(Paint paint, boolean notify) {
		this.paint = paint;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Sets the paint used for a series and sends a {@link RendererChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setSeriesPaint(int series, Paint paint) {
		setSeriesPaint(series, paint, true);
	}

	/**
	 * Sets the paint used for a series and, if requested, sends a {@link RendererChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param series
	 *           the series index.
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setSeriesPaint(int series, Paint paint, boolean notify) {
		this.paintList.setPaint(series, paint);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base paint.
	 * 
	 * @return The base paint (never <code>null</code>).
	 */
	public Paint getBasePaint() {
		return this.basePaint;
	}

	/**
	 * Sets the base paint and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setBasePaint(Paint paint) {
		// defer argument checking...
		setBasePaint(paint, true);
	}

	/**
	 * Sets the base paint and, if requested, sends a {@link RendererChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setBasePaint(Paint paint, boolean notify) {
		this.basePaint = paint;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// OUTLINE PAINT

	/**
	 * Returns the paint used to outline data items as they are drawn.
	 * <p>
	 * The default implementation passes control to the getSeriesOutlinePaint method. You can override this method if you require different behaviour.
	 * 
	 * @param row
	 *           the row (or series) index (zero-based).
	 * @param column
	 *           the column (or category) index (zero-based).
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getItemOutlinePaint(int row, int column) {
		return getSeriesOutlinePaint(row);
	}

	/**
	 * Returns the paint used to outline an item drawn by the renderer.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getSeriesOutlinePaint(int series) {

		// return the override, if there is one...
		if (this.outlinePaint != null) {
			return this.outlinePaint;
		}

		// otherwise look up the paint table
		Paint seriesOutlinePaint = this.outlinePaintList.getPaint(series);
		if (seriesOutlinePaint == null) {
			DrawingSupplier supplier = getDrawingSupplier();
			if (supplier != null) {
				seriesOutlinePaint = supplier.getNextOutlinePaint();
				this.outlinePaintList.setPaint(series, seriesOutlinePaint);
			} else {
				seriesOutlinePaint = this.baseOutlinePaint;
			}
		}
		return seriesOutlinePaint;

	}

	/**
	 * Sets the paint used for a series outline and sends a {@link RendererChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setSeriesOutlinePaint(int series, Paint paint) {
		setSeriesOutlinePaint(series, paint, true);
	}

	/**
	 * Sets the paint used to draw the outline for a series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setSeriesOutlinePaint(int series, Paint paint, boolean notify) {
		this.outlinePaintList.setPaint(series, paint);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Sets the outline paint for ALL series (optional).
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setOutlinePaint(Paint paint) {
		setOutlinePaint(paint, true);
	}

	/**
	 * Sets the outline paint for ALL series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setOutlinePaint(Paint paint, boolean notify) {
		this.outlinePaint = paint;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base outline paint.
	 * 
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getBaseOutlinePaint() {
		return this.baseOutlinePaint;
	}

	/**
	 * Sets the base outline paint and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setBaseOutlinePaint(Paint paint) {
		// defer argument checking...
		setBaseOutlinePaint(paint, true);
	}

	/**
	 * Sets the base outline paint and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setBaseOutlinePaint(Paint paint, boolean notify) {
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		this.baseOutlinePaint = paint;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// STROKE

	/**
	 * Returns the stroke used to draw data items.
	 * <p>
	 * The default implementation passes control to the getSeriesStroke method. You can override this method if you require different behaviour.
	 * 
	 * @param row
	 *           the row (or series) index (zero-based).
	 * @param column
	 *           the column (or category) index (zero-based).
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getItemStroke(int row, int column) {
		return getSeriesStroke(row);
	}

	/**
	 * Returns the stroke used to draw the items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getSeriesStroke(int series) {

		// return the override, if there is one...
		if (this.stroke != null) {
			return this.stroke;
		}

		// otherwise look up the paint table
		Stroke result = this.strokeList.getStroke(series);
		if (result == null) {
			DrawingSupplier supplier = getDrawingSupplier();
			if (supplier != null) {
				result = supplier.getNextStroke();
				this.strokeList.setStroke(series, result);
			} else {
				result = this.baseStroke;
			}
		}
		return result;

	}

	/**
	 * Sets the stroke for ALL series and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 */
	public void setStroke(Stroke stroke) {
		setStroke(stroke, true);
	}

	/**
	 * Sets the stroke for ALL series and, if requested, sends a {@link RendererChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setStroke(Stroke stroke, boolean notify) {
		this.stroke = stroke;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Sets the stroke used for a series and sends a {@link RendererChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 */
	public void setSeriesStroke(int series, Stroke stroke) {
		setSeriesStroke(series, stroke, true);
	}

	/**
	 * Sets the stroke for a series and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setSeriesStroke(int series, Stroke stroke, boolean notify) {
		this.strokeList.setStroke(series, stroke);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base stroke.
	 * 
	 * @return The base stroke (never <code>null</code>).
	 */
	public Stroke getBaseStroke() {
		return this.baseStroke;
	}

	/**
	 * Sets the base stroke.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 */
	public void setBaseStroke(Stroke stroke) {
		// defer argument checking...
		setBaseStroke(stroke, true);
	}

	/**
	 * Sets the base stroke and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setBaseStroke(Stroke stroke, boolean notify) {
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}
		this.baseStroke = stroke;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// OUTLINE STROKE

	/**
	 * Returns the stroke used to outline data items.
	 * <p>
	 * The default implementation passes control to the getSeriesOutlineStroke method. You can override this method if you require different behaviour.
	 * 
	 * @param row
	 *           the row (or series) index (zero-based).
	 * @param column
	 *           the column (or category) index (zero-based).
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getItemOutlineStroke(int row, int column) {
		return getSeriesOutlineStroke(row);
	}

	/**
	 * Returns the stroke used to outline the items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getSeriesOutlineStroke(int series) {

		// return the override, if there is one...
		if (this.outlineStroke != null) {
			return this.outlineStroke;
		}

		// otherwise look up the stroke table
		Stroke result = this.outlineStrokeList.getStroke(series);
		if (result == null) {
			DrawingSupplier supplier = getDrawingSupplier();
			if (supplier != null) {
				result = supplier.getNextOutlineStroke();
				this.outlineStrokeList.setStroke(series, result);
			} else {
				result = this.baseOutlineStroke;
			}
		}
		return result;

	}

	/**
	 * Sets the outline stroke for ALL series and sends a {@link RendererChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 */
	public void setOutlineStroke(Stroke stroke) {
		setOutlineStroke(stroke, true);
	}

	/**
	 * Sets the outline stroke for ALL series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setOutlineStroke(Stroke stroke, boolean notify) {
		this.outlineStroke = stroke;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Sets the outline stroke used for a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 */
	public void setSeriesOutlineStroke(int series, Stroke stroke) {
		setSeriesOutlineStroke(series, stroke, true);
	}

	/**
	 * Sets the outline stroke for a series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index.
	 * @param stroke
	 *           the stroke (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setSeriesOutlineStroke(int series, Stroke stroke, boolean notify) {
		this.outlineStrokeList.setStroke(series, stroke);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base outline stroke.
	 * 
	 * @return The stroke (never <code>null</code>).
	 */
	public Stroke getBaseOutlineStroke() {
		return this.baseOutlineStroke;
	}

	/**
	 * Sets the base outline stroke and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 */
	public void setBaseOutlineStroke(Stroke stroke) {
		// defer argument checking...
		setBaseOutlineStroke(stroke, true);
	}

	/**
	 * Sets the base outline stroke and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param stroke
	 *           the stroke (<code>null</code> not permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setBaseOutlineStroke(Stroke stroke, boolean notify) {
		if (stroke == null) {
			throw new IllegalArgumentException("Null 'stroke' argument.");
		}
		this.baseOutlineStroke = stroke;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// SHAPE

	/**
	 * Returns a shape used to represent a data item.
	 * <p>
	 * The default implementation passes control to the getSeriesShape method. You can override this method if you require different behaviour.
	 * 
	 * @param row
	 *           the row (or series) index (zero-based).
	 * @param column
	 *           the column (or category) index (zero-based).
	 * @return The shape (never <code>null</code>).
	 */
	public Shape getItemShape(int row, int column) {
		return getSeriesShape(row);
	}

	/**
	 * Returns a shape used to represent the items in a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @return The shape (never <code>null</code>).
	 */
	public Shape getSeriesShape(int series) {

		// return the override, if there is one...
		if (this.shape != null) {
			return this.shape;
		}

		// otherwise look up the shape list
		Shape result = this.shapeList.getShape(series);
		if (result == null) {
			DrawingSupplier supplier = getDrawingSupplier();
			if (supplier != null) {
				result = supplier.getNextShape();
				this.shapeList.setShape(series, result);
			} else {
				result = this.baseShape;
			}
		}
		return result;

	}

	/**
	 * Sets the shape for ALL series (optional) and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param shape
	 *           the shape (<code>null</code> permitted).
	 */
	public void setShape(Shape shape) {
		setShape(shape, true);
	}

	/**
	 * Sets the shape for ALL series and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param shape
	 *           the shape (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setShape(Shape shape, boolean notify) {
		this.shape = shape;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Sets the shape used for a series and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param shape
	 *           the shape (<code>null</code> permitted).
	 */
	public void setSeriesShape(int series, Shape shape) {
		setSeriesShape(series, shape, true);
	}

	/**
	 * Sets the shape for a series and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @param shape
	 *           the shape (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setSeriesShape(int series, Shape shape, boolean notify) {
		this.shapeList.setShape(series, shape);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base shape.
	 * 
	 * @return The shape (never <code>null</code>).
	 */
	public Shape getBaseShape() {
		return this.baseShape;
	}

	/**
	 * Sets the base shape and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param shape
	 *           the shape (<code>null</code> not permitted).
	 */
	public void setBaseShape(Shape shape) {
		// defer argument checking...
		setBaseShape(shape, true);
	}

	/**
	 * Sets the base shape and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param shape
	 *           the shape (<code>null</code> not permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setBaseShape(Shape shape, boolean notify) {
		if (shape == null) {
			throw new IllegalArgumentException("Null 'shape' argument.");
		}
		this.baseShape = shape;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Creates and returns a translated version of a shape.
	 * 
	 * @param shape
	 *           the base shape.
	 * @param translateX
	 *           the x translation.
	 * @param translateY
	 *           the y translation.
	 * @return The shape.
	 */
	protected synchronized Shape createTransformedShape(Shape shape,
																			double translateX, double translateY) {

		AffineTransform transformer = new AffineTransform();
		transformer.setToTranslation(translateX, translateY);
		return transformer.createTransformedShape(shape);

	}

	// ITEM LABEL VISIBILITY...

	/**
	 * Returns <code>true</code> if an item label is visible, and <code>false</code> otherwise.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return A boolean.
	 */
	public boolean isItemLabelVisible(int row, int column) {
		return isSeriesItemLabelsVisible(row);
	}

	/**
	 * Returns <code>true</code> if the item labels for a series are visible, and <code>false</code> otherwise.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return A boolean.
	 */
	public boolean isSeriesItemLabelsVisible(int series) {

		// return the override, if there is one...
		if (this.itemLabelsVisible != null) {
			return this.itemLabelsVisible.booleanValue();
		}

		// otherwise look up the boolean table
		Boolean b = this.itemLabelsVisibleList.getBoolean(series);
		if (b == null) {
			b = this.baseItemLabelsVisible;
		}
		if (b == null) {
			b = Boolean.FALSE;
		}
		return b.booleanValue();

	}

	/**
	 * Sets the visibility of the item labels for ALL series.
	 * 
	 * @param visible
	 *           the flag.
	 */
	public void setItemLabelsVisible(boolean visible) {
		setItemLabelsVisible(BooleanUtils.valueOf(visible));
		// The following alternative is only supported in JDK 1.4 - we support JDK 1.2.2
		// setItemLabelsVisible(Boolean.valueOf(visible));
	}

	/**
	 * Sets the visibility of the item labels for ALL series (optional).
	 * 
	 * @param visible
	 *           the flag (<code>null</code> permitted).
	 */
	public void setItemLabelsVisible(Boolean visible) {
		setItemLabelsVisible(visible, true);
	}

	/**
	 * Sets the visibility of item labels for ALL series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param visible
	 *           a flag that controls whether or not the item labels are visible
	 *           (<code>null</code> permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setItemLabelsVisible(Boolean visible, boolean notify) {
		this.itemLabelsVisible = visible;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Sets a flag that controls the visibility of the item labels for a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param visible
	 *           the flag.
	 */
	public void setSeriesItemLabelsVisible(int series, boolean visible) {
		setSeriesItemLabelsVisible(series, BooleanUtils.valueOf(visible));
	}

	/**
	 * Sets the visibility of the item labels for a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param visible
	 *           the flag (<code>null</code> permitted).
	 */
	public void setSeriesItemLabelsVisible(int series, Boolean visible) {
		setSeriesItemLabelsVisible(series, visible, true);
	}

	/**
	 * Sets the visibility of item labels for a series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param visible
	 *           the visible flag.
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setSeriesItemLabelsVisible(int series, Boolean visible, boolean notify) {
		this.itemLabelsVisibleList.setBoolean(series, visible);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base setting for item label visibility.
	 * 
	 * @return A flag (possibly <code>null</code>).
	 */
	public Boolean getBaseItemLabelsVisible() {
		return this.baseItemLabelsVisible;
	}

	/**
	 * Sets the base flag that controls whether or not item labels are visible.
	 * 
	 * @param visible
	 *           the flag.
	 */
	public void setBaseItemLabelsVisible(boolean visible) {
		setBaseItemLabelsVisible(BooleanUtils.valueOf(visible));
	}

	/**
	 * Sets the base setting for item label visibility.
	 * 
	 * @param visible
	 *           the flag (<code>null</code> permitted).
	 */
	public void setBaseItemLabelsVisible(Boolean visible) {
		setBaseItemLabelsVisible(visible, true);
	}

	/**
	 * Sets the base visibility for item labels and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param visible
	 *           the visibility flag.
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setBaseItemLabelsVisible(Boolean visible, boolean notify) {
		this.baseItemLabelsVisible = visible;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// // ITEM LABEL FONT /////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the font for an item label.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The font (never <code>null</code>).
	 */
	public Font getItemLabelFont(int row, int column) {
		Font result = getSeriesItemLabelFont(row);
		if (result == null) {
			result = this.baseItemLabelFont;
		}
		return result;
	}

	/**
	 * Returns the font used for all item labels. This may be <code>null</code>, in which case
	 * the per series font settings will apply.
	 * 
	 * @return The font (possibly <code>null</code>).
	 */
	public Font getItemLabelFont() {
		return this.itemLabelFont;
	}

	/**
	 * Sets the item label font for ALL series and sends a {@link RendererChangeEvent} to all
	 * registered listeners. You can set this to <code>null</code> if you prefer to set the font
	 * on a per series basis.
	 * 
	 * @param font
	 *           the font (<code>null</code> permitted).
	 */
	public void setItemLabelFont(Font font) {
		setItemLabelFont(font, true);
	}

	/**
	 * Sets the item label font for ALL series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param font
	 *           the font (<code>null</code> permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setItemLabelFont(Font font, boolean notify) {
		this.itemLabelFont = font;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the font for all the item labels in a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The font (possibly <code>null</code>).
	 */
	public Font getSeriesItemLabelFont(int series) {
		return (Font) this.itemLabelFontList.get(series);
	}

	/**
	 * Sets the item label font for a series and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param font
	 *           the font (<code>null</code> permitted).
	 */
	public void setSeriesItemLabelFont(int series, Font font) {
		setSeriesItemLabelFont(series, font, true);
	}

	/**
	 * Sets the item label font for a series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @param font
	 *           the font (<code>null</code> permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setSeriesItemLabelFont(int series, Font font, boolean notify) {
		this.itemLabelFontList.set(series, font);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base item label font (this is used when no other font setting is available).
	 * 
	 * @return The font (<code>never</code> null).
	 */
	public Font getBaseItemLabelFont() {
		return this.baseItemLabelFont;
	}

	/**
	 * Sets the base item label font and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param font
	 *           the font (<code>null</code> not permitted).
	 */
	public void setBaseItemLabelFont(Font font) {
		if (font == null) {
			throw new IllegalArgumentException("Null 'font' argument.");
		}
		setBaseItemLabelFont(font, true);
	}

	/**
	 * Sets the base item label font and, if requested, sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param font
	 *           the font (<code>null</code> not permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setBaseItemLabelFont(Font font, boolean notify) {
		this.baseItemLabelFont = font;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// // ITEM LABEL PAINT ///////////////////////////////////////////////////////////////////////

	/**
	 * Returns the paint used to draw an item label.
	 * 
	 * @param row
	 *           the row index (zero based).
	 * @param column
	 *           the column index (zero based).
	 * @return The paint (never <code>null</code>).
	 */
	public Paint getItemLabelPaint(int row, int column) {
		Paint result = getSeriesItemLabelPaint(row);
		if (result == null) {
			result = this.baseItemLabelPaint;
		}
		return result;
	}

	/**
	 * Returns the paint used for all item labels. This may be <code>null</code>, in which case
	 * the per series paint settings will apply.
	 * 
	 * @return The paint (possibly <code>null</code>).
	 */
	public Paint getItemLabelPaint() {
		return this.itemLabelPaint;
	}

	/**
	 * Sets the item label paint for ALL series and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setItemLabelPaint(Paint paint) {
		setItemLabelPaint(paint, true);
	}

	/**
	 * Sets the item label paint for ALL series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param paint
	 *           the paint.
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setItemLabelPaint(Paint paint, boolean notify) {
		this.itemLabelPaint = paint;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the paint used to draw the item labels for a series.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @return The paint (possibly <code>null<code>).
	 */
	public Paint getSeriesItemLabelPaint(int series) {
		return this.itemLabelPaintList.getPaint(series);
	}

	/**
	 * Sets the item label paint for a series and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param series
	 *           the series (zero based index).
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 */
	public void setSeriesItemLabelPaint(int series, Paint paint) {
		setSeriesItemLabelPaint(series, paint, true);
	}

	/**
	 * Sets the item label paint for a series and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero based).
	 * @param paint
	 *           the paint (<code>null</code> permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setSeriesItemLabelPaint(int series, Paint paint, boolean notify) {
		this.itemLabelPaintList.setPaint(series, paint);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base item label paint.
	 * 
	 * @return The paint (never <code>null<code>).
	 */
	public Paint getBaseItemLabelPaint() {
		return this.baseItemLabelPaint;
	}

	/**
	 * Sets the base item label paint and sends a {@link RendererChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 */
	public void setBaseItemLabelPaint(Paint paint) {
		// defer argument checking...
		setBaseItemLabelPaint(paint, true);
	}

	/**
	 * Sets the base item label paint and, if requested, sends a {@link RendererChangeEvent} to all registered listeners..
	 * 
	 * @param paint
	 *           the paint (<code>null</code> not permitted).
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setBaseItemLabelPaint(Paint paint, boolean notify) {
		if (paint == null) {
			throw new IllegalArgumentException("Null 'paint' argument.");
		}
		this.baseItemLabelPaint = paint;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// POSITIVE ITEM LABEL POSITION...

	/**
	 * Returns the item label position for positive values.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column index (zero-based).
	 * @return The item label position (never <code>null</code>).
	 */
	public ItemLabelPosition getPositiveItemLabelPosition(int row, int column) {
		return getSeriesPositiveItemLabelPosition(row);
	}

	/**
	 * Returns the item label position for positive values in ALL series.
	 * 
	 * @return The item label position (possibly <code>null</code>).
	 */
	public ItemLabelPosition getPositiveItemLabelPosition() {
		return this.positiveItemLabelPosition;
	}

	/**
	 * Sets the item label position for positive values in ALL series, and sends a {@link RendererChangeEvent} to all registered listeners. You need to set this
	 * to <code>null</code> to expose the settings for individual series.
	 * 
	 * @param position
	 *           the position (<code>null</code> permitted).
	 */
	public void setPositiveItemLabelPosition(ItemLabelPosition position) {
		setPositiveItemLabelPosition(position, true);
	}

	/**
	 * Sets the positive item label position for ALL series and (if requested) sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param position
	 *           the position (<code>null</code> permitted).
	 * @param notify
	 *           notify registered listeners?
	 */
	public void setPositiveItemLabelPosition(ItemLabelPosition position, boolean notify) {
		this.positiveItemLabelPosition = position;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the item label position for all positive values in a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The item label position (never <code>null</code>).
	 */
	public ItemLabelPosition getSeriesPositiveItemLabelPosition(int series) {

		// return the override, if there is one...
		if (this.positiveItemLabelPosition != null) {
			return this.positiveItemLabelPosition;
		}

		// otherwise look up the position table
		ItemLabelPosition position = (ItemLabelPosition) this.positiveItemLabelPositionList.get(series);
		if (position == null) {
			position = this.basePositiveItemLabelPosition;
		}
		return position;

	}

	/**
	 * Sets the item label position for all positive values in a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param position
	 *           the position (<code>null</code> permitted).
	 */
	public void setSeriesPositiveItemLabelPosition(int series, ItemLabelPosition position) {
		setSeriesPositiveItemLabelPosition(series, position, true);
	}

	/**
	 * Sets the item label position for all positive values in a series and (if requested) sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param position
	 *           the position (<code>null</code> permitted).
	 * @param notify
	 *           notify registered listeners?
	 */
	public void setSeriesPositiveItemLabelPosition(int series, ItemLabelPosition position,
																	boolean notify) {
		this.positiveItemLabelPositionList.set(series, position);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base positive item label position.
	 * 
	 * @return The position (never <code>null</code>).
	 */
	public ItemLabelPosition getBasePositiveItemLabelPosition() {
		return this.basePositiveItemLabelPosition;
	}

	/**
	 * Sets the base positive item label position.
	 * 
	 * @param position
	 *           the position (<code>null</code> not permitted).
	 */
	public void setBasePositiveItemLabelPosition(ItemLabelPosition position) {
		// defer argument checking...
		setBasePositiveItemLabelPosition(position, true);
	}

	/**
	 * Sets the base positive item label position and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param position
	 *           the position (<code>null</code> not permitted).
	 * @param notify
	 *           notify registered listeners?
	 */
	public void setBasePositiveItemLabelPosition(ItemLabelPosition position, boolean notify) {
		if (position == null) {
			throw new IllegalArgumentException("Null 'position' argument.");
		}
		this.basePositiveItemLabelPosition = position;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	// NEGATIVE ITEM LABEL POSITION...

	/**
	 * Returns the item label position for negative values. This method can be overridden to
	 * provide customisation of the item label position for individual data items.
	 * 
	 * @param row
	 *           the row index (zero-based).
	 * @param column
	 *           the column (zero-based).
	 * @return The item label position (never <code>null</code>).
	 */
	public ItemLabelPosition getNegativeItemLabelPosition(int row, int column) {
		return getSeriesNegativeItemLabelPosition(row);
	}

	/**
	 * Returns the item label position for negative values in ALL series.
	 * 
	 * @return the item label position (possibly <code>null</code>).
	 */
	public ItemLabelPosition getNegativeItemLabelPosition() {
		return this.negativeItemLabelPosition;
	}

	/**
	 * Sets the item label position for negative values in ALL series, and sends a {@link RendererChangeEvent} to all registered listeners. You need to set this
	 * to <code>null</code> to expose the settings for individual series.
	 * 
	 * @param position
	 *           the position (<code>null</code> permitted).
	 */
	public void setNegativeItemLabelPosition(ItemLabelPosition position) {
		setNegativeItemLabelPosition(position, true);
	}

	/**
	 * Sets the item label position for negative values in ALL series and (if requested) sends
	 * a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param position
	 *           the position (<code>null</code> permitted).
	 * @param notify
	 *           notify registered listeners?
	 */
	public void setNegativeItemLabelPosition(ItemLabelPosition position, boolean notify) {
		this.negativeItemLabelPosition = position;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the item label position for all negative values in a series.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @return The item label position (never <code>null</code>).
	 */
	public ItemLabelPosition getSeriesNegativeItemLabelPosition(int series) {

		// return the override, if there is one...
		if (this.negativeItemLabelPosition != null) {
			return this.negativeItemLabelPosition;
		}

		// otherwise look up the position list
		ItemLabelPosition position = (ItemLabelPosition) this.negativeItemLabelPositionList.get(series);
		if (position == null) {
			position = this.baseNegativeItemLabelPosition;
		}
		return position;

	}

	/**
	 * Sets the item label position for negative values in a series and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param position
	 *           the position (<code>null</code> permitted).
	 */
	public void setSeriesNegativeItemLabelPosition(int series, ItemLabelPosition position) {
		setSeriesNegativeItemLabelPosition(series, position, true);
	}

	/**
	 * Sets the item label position for negative values in a series and (if requested) sends a. {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param series
	 *           the series index (zero-based).
	 * @param position
	 *           the position (<code>null</code> permitted).
	 * @param notify
	 *           notify registered listeners?
	 */
	public void setSeriesNegativeItemLabelPosition(int series, ItemLabelPosition position,
																	boolean notify) {
		this.negativeItemLabelPositionList.set(series, position);
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the base item label position for negative values.
	 * 
	 * @return The position (never <code>null</code>).
	 */
	public ItemLabelPosition getBaseNegativeItemLabelPosition() {
		return this.baseNegativeItemLabelPosition;
	}

	/**
	 * Sets the base item label position for negative values and sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param position
	 *           the position (<code>null</code> not permitted).
	 */
	public void setBaseNegativeItemLabelPosition(ItemLabelPosition position) {
		setBaseNegativeItemLabelPosition(position, true);
	}

	/**
	 * Sets the base negative item label position and, if requested, sends a {@link RendererChangeEvent} to all registered listeners.
	 * 
	 * @param position
	 *           the position (<code>null</code> not permitted).
	 * @param notify
	 *           notify registered listeners?
	 */
	public void setBaseNegativeItemLabelPosition(ItemLabelPosition position, boolean notify) {
		if (position == null) {
			throw new IllegalArgumentException("Null 'position' argument.");
		}
		this.baseNegativeItemLabelPosition = position;
		if (notify) {
			notifyListeners(new RendererChangeEvent(this));
		}
	}

	/**
	 * Returns the item label anchor offset.
	 * 
	 * @return the offset.
	 */
	public double getItemLabelAnchorOffset() {
		return this.itemLabelAnchorOffset;
	}

	/**
	 * Sets the item label anchor offset.
	 * 
	 * @param offset
	 *           the offset.
	 */
	public void setItemLabelAnchorOffset(double offset) {
		this.itemLabelAnchorOffset = offset;
		notifyListeners(new RendererChangeEvent(this));
	}

	/** The adjacent offset. */
	private static final double ADJ = Math.cos(Math.PI / 6.0);

	/** The opposite offset. */
	private static final double OPP = Math.sin(Math.PI / 6.0);

	/**
	 * Calculates the item label anchor point.
	 * 
	 * @param anchor
	 *           the anchor.
	 * @param x
	 *           the x coordinate.
	 * @param y
	 *           the y coordinate.
	 * @param orientation
	 *           the plot orientation.
	 * @return the anchor point (never <code>null</code>).
	 */
	protected Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor,
																double x,
																double y,
																PlotOrientation orientation) {

		Point2D result = null;

		if (anchor == ItemLabelAnchor.CENTER) {
			result = new Point2D.Double(x, y);
		} else
			if (anchor == ItemLabelAnchor.INSIDE1) {
				result = new Point2D.Double(
									x + OPP * this.itemLabelAnchorOffset, y - ADJ * this.itemLabelAnchorOffset
									);
			} else
				if (anchor == ItemLabelAnchor.INSIDE2) {
					result = new Point2D.Double(
										x + ADJ * this.itemLabelAnchorOffset, y - OPP * this.itemLabelAnchorOffset
										);
				} else
					if (anchor == ItemLabelAnchor.INSIDE3) {
						result = new Point2D.Double(x + this.itemLabelAnchorOffset, y);
					} else
						if (anchor == ItemLabelAnchor.INSIDE4) {
							result = new Point2D.Double(
												x + ADJ * this.itemLabelAnchorOffset, y + OPP * this.itemLabelAnchorOffset
												);
						} else
							if (anchor == ItemLabelAnchor.INSIDE5) {
								result = new Point2D.Double(
													x + OPP * this.itemLabelAnchorOffset, y + ADJ * this.itemLabelAnchorOffset
													);
							} else
								if (anchor == ItemLabelAnchor.INSIDE6) {
									result = new Point2D.Double(x, y + this.itemLabelAnchorOffset);
								} else
									if (anchor == ItemLabelAnchor.INSIDE7) {
										result = new Point2D.Double(
															x - OPP * this.itemLabelAnchorOffset, y + ADJ * this.itemLabelAnchorOffset
															);
									} else
										if (anchor == ItemLabelAnchor.INSIDE8) {
											result = new Point2D.Double(
																x - ADJ * this.itemLabelAnchorOffset, y + OPP * this.itemLabelAnchorOffset
																);
										} else
											if (anchor == ItemLabelAnchor.INSIDE9) {
												result = new Point2D.Double(x - this.itemLabelAnchorOffset, y);
											} else
												if (anchor == ItemLabelAnchor.INSIDE10) {
													result = new Point2D.Double(
																		x - ADJ * this.itemLabelAnchorOffset, y - OPP * this.itemLabelAnchorOffset
																		);
												} else
													if (anchor == ItemLabelAnchor.INSIDE11) {
														result = new Point2D.Double(
																			x - OPP * this.itemLabelAnchorOffset, y - ADJ * this.itemLabelAnchorOffset
																			);
													} else
														if (anchor == ItemLabelAnchor.INSIDE12) {
															result = new Point2D.Double(x, y - this.itemLabelAnchorOffset);
														} else
															if (anchor == ItemLabelAnchor.OUTSIDE1) {
																result = new Point2D.Double(
																					x + 2.0 * OPP * this.itemLabelAnchorOffset,
																					y - 2.0 * ADJ * this.itemLabelAnchorOffset
																					);
															} else
																if (anchor == ItemLabelAnchor.OUTSIDE2) {
																	result = new Point2D.Double(
																						x + 2.0 * ADJ * this.itemLabelAnchorOffset,
																						y - 2.0 * OPP * this.itemLabelAnchorOffset
																						);
																} else
																	if (anchor == ItemLabelAnchor.OUTSIDE3) {
																		result = new Point2D.Double(x + 2.0 * this.itemLabelAnchorOffset, y);
																	} else
																		if (anchor == ItemLabelAnchor.OUTSIDE4) {
																			result = new Point2D.Double(
																								x + 2.0 * ADJ * this.itemLabelAnchorOffset,
																								y + 2.0 * OPP * this.itemLabelAnchorOffset
																								);
																		} else
																			if (anchor == ItemLabelAnchor.OUTSIDE5) {
																				result = new Point2D.Double(
																									x + 2.0 * OPP * this.itemLabelAnchorOffset,
																									y + 2.0 * ADJ * this.itemLabelAnchorOffset
																									);
																			} else
																				if (anchor == ItemLabelAnchor.OUTSIDE6) {
																					result = new Point2D.Double(x, y + 2.0 * this.itemLabelAnchorOffset);
																				} else
																					if (anchor == ItemLabelAnchor.OUTSIDE7) {
																						result = new Point2D.Double(
																											x - 2.0 * OPP * this.itemLabelAnchorOffset,
																											y + 2.0 * ADJ * this.itemLabelAnchorOffset
																											);
																					} else
																						if (anchor == ItemLabelAnchor.OUTSIDE8) {
																							result = new Point2D.Double(
																												x - 2.0 * ADJ * this.itemLabelAnchorOffset,
																												y + 2.0 * OPP * this.itemLabelAnchorOffset
																												);
																						} else
																							if (anchor == ItemLabelAnchor.OUTSIDE9) {
																								result = new Point2D.Double(x - 2.0 * this.itemLabelAnchorOffset, y);
																							} else
																								if (anchor == ItemLabelAnchor.OUTSIDE10) {
																									result = new Point2D.Double(
																														x - 2.0 * ADJ * this.itemLabelAnchorOffset,
																														y - 2.0 * OPP * this.itemLabelAnchorOffset
																														);
																								} else
																									if (anchor == ItemLabelAnchor.OUTSIDE11) {
																										result = new Point2D.Double(
																															x - 2.0 * OPP * this.itemLabelAnchorOffset,
																															y - 2.0 * ADJ * this.itemLabelAnchorOffset
																															);
																									} else
																										if (anchor == ItemLabelAnchor.OUTSIDE12) {
																											result = new Point2D.Double(x, y - 2.0 * this.itemLabelAnchorOffset);
																										}

		return result;

	}

	/**
	 * Registers an object to receive notification of changes to the renderer.
	 * 
	 * @param listener
	 *           the listener (<code>null</code> not permitted).
	 */
	public void addChangeListener(RendererChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Null 'listener' argument.");
		}
		this.listenerList.add(RendererChangeListener.class, listener);
	}

	/**
	 * Deregisters an object so that it no longer receives notification of changes to the renderer.
	 * 
	 * @param listener
	 *           the object (<code>null</code> not permitted).
	 */
	public void removeChangeListener(RendererChangeListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("Null 'listener' argument.");
		}
		this.listenerList.remove(RendererChangeListener.class, listener);
	}

	/**
	 * Notifies all registered listeners that the renderer has been modified.
	 * 
	 * @param event
	 *           information about the change event.
	 */
	public void notifyListeners(RendererChangeEvent event) {

		Object[] ls = this.listenerList.getListenerList();
		for (int i = ls.length - 2; i >= 0; i -= 2) {
			if (ls[i] == RendererChangeListener.class) {
				((RendererChangeListener) ls[i + 1]).rendererChanged(event);
			}
		}

	}

	/**
	 * Tests this renderer for equality with another object.
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

		if (obj instanceof AbstractRenderer) {
			AbstractRenderer renderer = (AbstractRenderer) obj;

			boolean b0 = ObjectUtils.equal(this.paint, renderer.paint);
			boolean b1 = ObjectUtils.equal(this.paintList, renderer.paintList);
			boolean b2 = ObjectUtils.equal(this.basePaint, renderer.basePaint);

			boolean b3 = ObjectUtils.equal(this.outlinePaint, renderer.outlinePaint);
			boolean b4 = ObjectUtils.equal(this.outlinePaintList, renderer.outlinePaintList);
			boolean b5 = ObjectUtils.equal(this.baseOutlinePaint, renderer.baseOutlinePaint);

			boolean b6 = ObjectUtils.equal(this.stroke, renderer.stroke);
			boolean b7 = ObjectUtils.equal(this.strokeList, renderer.strokeList);
			boolean b8 = ObjectUtils.equal(this.baseStroke, renderer.baseStroke);

			boolean b9 = ObjectUtils.equal(this.outlineStroke, renderer.outlineStroke);
			boolean b10 = ObjectUtils.equal(this.outlineStrokeList, renderer.outlineStrokeList);
			boolean b11 = ObjectUtils.equal(this.baseOutlineStroke, renderer.baseOutlineStroke);

			boolean b12 = ObjectUtils.equal(this.shape, renderer.shape);
			boolean b13 = ObjectUtils.equal(this.shapeList, renderer.shapeList);
			boolean b14 = ObjectUtils.equal(this.baseShape, renderer.baseShape);

			boolean b15 = ObjectUtils.equal(this.itemLabelsVisible, renderer.itemLabelsVisible);
			boolean b16 = ObjectUtils.equal(this.itemLabelsVisibleList,
															renderer.itemLabelsVisibleList);
			boolean b17 = ObjectUtils.equal(this.baseItemLabelsVisible,
															renderer.baseItemLabelsVisible);

			boolean b18 = ObjectUtils.equal(this.itemLabelFont, renderer.itemLabelFont);
			boolean b19 = ObjectUtils.equal(this.itemLabelFontList, renderer.itemLabelFontList);
			boolean b20 = ObjectUtils.equal(this.baseItemLabelFont, renderer.baseItemLabelFont);

			boolean b21 = ObjectUtils.equal(this.itemLabelPaint, renderer.itemLabelPaint);
			boolean b22 = ObjectUtils.equal(this.itemLabelPaintList, renderer.itemLabelPaintList);
			boolean b23 = ObjectUtils.equal(this.baseItemLabelPaint, renderer.baseItemLabelPaint);

			boolean b24 = ObjectUtils.equal(this.positiveItemLabelPosition,
															renderer.positiveItemLabelPosition);
			boolean b25 = ObjectUtils.equal(this.positiveItemLabelPositionList,
															renderer.positiveItemLabelPositionList);
			boolean b26 = ObjectUtils.equal(this.basePositiveItemLabelPosition,
															renderer.basePositiveItemLabelPosition);

			boolean b27 = ObjectUtils.equal(this.negativeItemLabelPosition,
															renderer.negativeItemLabelPosition);
			boolean b28 = ObjectUtils.equal(this.negativeItemLabelPositionList,
															renderer.negativeItemLabelPositionList);
			boolean b29 = ObjectUtils.equal(
								this.baseNegativeItemLabelPosition, renderer.baseNegativeItemLabelPosition
								);
			boolean b30 = NumberUtils.equal(
								this.itemLabelAnchorOffset, renderer.itemLabelAnchorOffset
								);

			return b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9
								&& b10 && b11 && b12 && b13 && b14 && b15 && b16 && b17 && b18 && b19
								&& b20 && b21 && b22 && b23 && b24 && b25 && b26 && b27 && b28 && b29
								&& b30;
		}

		return false;

	}

	/**
	 * Returns a hashcode for the renderer.
	 * 
	 * @return The hashcode.
	 */
	public int hashCode() {
		int result = 193;
		// result = 37 * result + ObjectUtils.hashCode(this.paint);
		// result = 37 * result + ObjectUtils.hashCode(this.paintList);
		// result = 37 * result + ObjectUtils.hashCode(this.basePaint);
		// result = 37 * result + ObjectUtils.hashCode(this.outlinePaint);
		// result = 37 * result + ObjectUtils.hashCode(this.outlinePaintList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseOutlinePaint);
		result = 37 * result + ObjectUtils.hashCode(this.stroke);
		// result = 37 * result + ObjectUtils.hashCode(this.strokeList);
		result = 37 * result + ObjectUtils.hashCode(this.baseStroke);
		result = 37 * result + ObjectUtils.hashCode(this.outlineStroke);
		// result = 37 * result + ObjectUtils.hashCode(this.outlineStrokeList);
		result = 37 * result + ObjectUtils.hashCode(this.baseOutlineStroke);
		// result = 37 * result + ObjectUtils.hashCode(this.shape);
		// result = 37 * result + ObjectUtils.hashCode(this.shapeList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseShape);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelsVisible);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelsVisibleList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseItemLabelsVisible);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelFont);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelFontList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseItemLabelFont);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelPaint);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelPaintList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseItemLabelPaint);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelAnchor);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelAnchorList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseItemLabelAnchor);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelRotationAnchor);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelRotationAnchorList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseItemLabelRotationAnchor);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelAngle);
		// result = 37 * result + ObjectUtils.hashCode(this.itemLabelAngleList);
		// result = 37 * result + ObjectUtils.hashCode(this.baseItemLabelAngle);
		return result;
	}

	/**
	 * Returns an independent copy of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if some component of the renderer does not support
	 *            cloning.
	 */
	protected Object clone() throws CloneNotSupportedException {
		AbstractRenderer clone = (AbstractRenderer) super.clone();

		// 'paint' : immutable, no need to clone reference
		if (this.paintList != null) {
			clone.paintList = (PaintList) this.paintList.clone();
		}
		// 'basePaint' : immutable, no need to clone reference

		// 'outlinePaint' : immutable, no need to clone reference
		if (this.outlinePaintList != null) {
			clone.outlinePaintList = (PaintList) this.outlinePaintList.clone();
		}
		// 'baseOutlinePaint' : immutable, no need to clone reference

		// 'stroke' : immutable, no need to clone reference
		if (this.strokeList != null) {
			clone.strokeList = (StrokeList) this.strokeList.clone();
		}
		// 'baseStroke' : immutable, no need to clone reference

		// 'outlineStroke' : immutable, no need to clone reference
		if (this.outlineStrokeList != null) {
			clone.outlineStrokeList = (StrokeList) this.outlineStrokeList.clone();
		}
		// 'baseOutlineStroke' : immutable, no need to clone reference

		if (this.shape != null) {
			clone.shape = ShapeUtils.clone(this.shape);
		}
		if (this.baseShape != null) {
			clone.baseShape = ShapeUtils.clone(this.baseShape);
		}

		// 'itemLabelsVisible' : immutable, no need to clone reference
		if (this.itemLabelsVisibleList != null) {
			clone.itemLabelsVisibleList = (BooleanList) this.itemLabelsVisibleList.clone();
		}
		// 'basePaint' : immutable, no need to clone reference

		// 'itemLabelFont' : immutable, no need to clone reference
		if (this.itemLabelFontList != null) {
			clone.itemLabelFontList = (ObjectList) this.itemLabelFontList.clone();
		}
		// 'baseItemLabelFont' : immutable, no need to clone reference

		// 'itemLabelPaint' : immutable, no need to clone reference
		if (this.itemLabelPaintList != null) {
			clone.itemLabelPaintList = (PaintList) this.itemLabelPaintList.clone();
		}
		// 'baseItemLabelPaint' : immutable, no need to clone reference

		// 'postiveItemLabelAnchor' : immutable, no need to clone reference
		if (this.positiveItemLabelPositionList != null) {
			clone.positiveItemLabelPositionList = (ObjectList) this.positiveItemLabelPositionList.clone();
		}
		// 'baseItemLabelAnchor' : immutable, no need to clone reference

		// 'negativeItemLabelAnchor' : immutable, no need to clone reference
		if (this.negativeItemLabelPositionList != null) {
			clone.negativeItemLabelPositionList = (ObjectList) this.negativeItemLabelPositionList.clone();
		}
		// 'baseNegativeItemLabelAnchor' : immutable, no need to clone reference

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
		SerialUtilities.writePaint(this.paint, stream);
		SerialUtilities.writePaint(this.basePaint, stream);
		SerialUtilities.writePaint(this.outlinePaint, stream);
		SerialUtilities.writePaint(this.baseOutlinePaint, stream);
		SerialUtilities.writeStroke(this.stroke, stream);
		SerialUtilities.writeStroke(this.baseStroke, stream);
		SerialUtilities.writeStroke(this.outlineStroke, stream);
		SerialUtilities.writeStroke(this.baseOutlineStroke, stream);
		SerialUtilities.writeShape(this.shape, stream);
		SerialUtilities.writeShape(this.baseShape, stream);
		SerialUtilities.writePaint(this.itemLabelPaint, stream);
		SerialUtilities.writePaint(this.baseItemLabelPaint, stream);

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
		this.basePaint = SerialUtilities.readPaint(stream);
		this.outlinePaint = SerialUtilities.readPaint(stream);
		this.baseOutlinePaint = SerialUtilities.readPaint(stream);
		this.stroke = SerialUtilities.readStroke(stream);
		this.baseStroke = SerialUtilities.readStroke(stream);
		this.outlineStroke = SerialUtilities.readStroke(stream);
		this.baseOutlineStroke = SerialUtilities.readStroke(stream);
		this.shape = SerialUtilities.readShape(stream);
		this.baseShape = SerialUtilities.readShape(stream);
		this.itemLabelPaint = SerialUtilities.readPaint(stream);
		this.baseItemLabelPaint = SerialUtilities.readPaint(stream);

		// listeners are not restored automatically, but storage must be provided...
		this.listenerList = new EventListenerList();

	}

	// // DEPRECATED METHODS ///////////////////////////////////////////////////////////////////////

	// ITEM LABEL ANCHOR...

	/**
	 * Returns the item label anchor.
	 * 
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 * @return The item label anchor.
	 * @deprecated Use getPositiveItemLabelPosition() or getNegativeItemLabelPosition().
	 */
	public ItemLabelAnchor getItemLabelAnchor(int row, int column) {
		return getSeriesItemLabelAnchor(row);
	}

	/**
	 * Returns the item label anchor for all labels in a series.
	 * 
	 * @param series
	 *           the series.
	 * @return The anchor point.
	 * @deprecated Use getSeriesPositiveItemLabelPosition() or getSeriesNegativeItemLabelPosition().
	 */
	public ItemLabelAnchor getSeriesItemLabelAnchor(int series) {
		ItemLabelAnchor result = null;
		ItemLabelPosition p = getSeriesPositiveItemLabelPosition(series);
		if (p != null) {
			result = p.getItemLabelAnchor();
		}
		return result;
	}

	/**
	 * Sets the item label anchor.
	 * 
	 * @param anchor
	 *           the anchor.
	 * @deprecated Use setPositiveItemLabelPosition()/setNegativeItemLabelPosition().
	 */
	public void setItemLabelAnchor(ItemLabelAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setItemLabelAnchor is deprecated.");
	}

	/**
	 * Sets the series item label anchor.
	 * 
	 * @param series
	 *           the series.
	 * @param anchor
	 *           the anchor.
	 * @deprecated Use setSeriesPositiveItemLabelPosition()/setSeriesNegativeItemLabelPosition().
	 */
	public void setSeriesItemLabelAnchor(int series, ItemLabelAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setSeriesItemLabelAnchor is deprecated.");
	}

	/**
	 * Returns the base item label anchor.
	 * 
	 * @return The anchor point.
	 * @deprecated Use getBasePositiveItemLabelPosition()/getBaseNegativeItemLabelPosition().
	 */
	public ItemLabelAnchor getBaseItemLabelAnchor() {
		ItemLabelAnchor result = null;
		ItemLabelPosition p = getBasePositiveItemLabelPosition();
		if (p != null) {
			return p.getItemLabelAnchor();
		}
		return result;
	}

	/**
	 * Sets the base item label anchor.
	 * 
	 * @param anchor
	 *           the anchor.
	 * @deprecated Use setBasePositiveItemLabelPosition()/setBaseNegativeItemLabelPosition().
	 */
	public void setBaseItemLabelAnchor(ItemLabelAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setBaseItemLabelAnchor is deprecated.");
	}

	// TEXT ANCHOR...

	/**
	 * Returns the text anchor for an item label. This is a point relative to the label that
	 * will be aligned with another anchor point that is relative to the data item.
	 * 
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 * @return The text anchor.
	 * @deprecated Use getPositiveItemLabelPosition()/getNegativeItemLabelPosition().
	 */
	public TextAnchor getItemLabelTextAnchor(int row, int column) {
		return getSeriesItemLabelTextAnchor(row);
	}

	/**
	 * Returns the text anchor for all item labels in a series.
	 * 
	 * @param series
	 *           the series.
	 * @return The text anchor.
	 * @deprecated Use getSeriesPositiveItemLabelPosition()/getSeriesNegativeItemLabelPosition().
	 */
	public TextAnchor getSeriesItemLabelTextAnchor(int series) {
		TextAnchor result = null;
		ItemLabelPosition p = getSeriesPositiveItemLabelPosition(series);
		if (p != null) {
			result = p.getTextAnchor();
		}
		return result;
	}

	/**
	 * Sets the item label text anchor for ALL series (optional).
	 * 
	 * @param anchor
	 *           the anchor (<code>null</code> permitted).
	 * @deprecated Use setPositiveItemLabelPosition()/setNegativeItemLabelPosition().
	 */
	public void setItemLabelTextAnchor(TextAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setItemLabelTextAnchor is deprecated.");
	}

	/**
	 * Sets the item label text anchor for a series.
	 * 
	 * @param series
	 *           the series.
	 * @param anchor
	 *           the anchor.
	 * @deprecated Use setSeriesPositiveItemLabelPosition()/setSeriesNegativeItemLabelPosition().
	 */
	public void setSeriesItemLabelTextAnchor(int series, TextAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setSeriesItemLabelTextAnchor is deprecated.");
	}

	/**
	 * Returns the base item label text anchor.
	 * 
	 * @return The text anchor.
	 * @deprecated Use getBasePositiveItemLabelPosition()/getBaseNegativeItemLabelPosition().
	 */
	public TextAnchor getBaseItemLabelTextAnchor() {
		TextAnchor result = null;
		ItemLabelPosition p = getBasePositiveItemLabelPosition();
		if (p != null) {
			result = p.getTextAnchor();
		}
		return result;
	}

	/**
	 * Sets the default item label text anchor.
	 * 
	 * @param anchor
	 *           the anchor.
	 * @deprecated Use setBasePositiveItemLabelPosition()/setBaseNegativeItemLabelPosition().
	 */
	public void setBaseItemLabelTextAnchor(TextAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setBaseItemLabelTextAnchor is deprecated.");
	}

	// ROTATION ANCHOR...

	/**
	 * Returns the rotation anchor for an item label.
	 * 
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 * @return The rotation anchor.
	 * @deprecated Use getPositiveItemLabelPosition()/getNegativeItemLabelPosition().
	 */
	public TextAnchor getItemLabelRotationAnchor(int row, int column) {
		return getSeriesItemLabelRotationAnchor(row);
	}

	/**
	 * Returns the rotation anchor for all item labels in a series.
	 * 
	 * @param series
	 *           the series.
	 * @return The rotation anchor.
	 * @deprecated Use getSeriesPositiveItemLabelPosition()/getSeriesNegativeItemLabelPosition().
	 */
	public TextAnchor getSeriesItemLabelRotationAnchor(int series) {
		TextAnchor result = null;
		ItemLabelPosition p = this.getSeriesPositiveItemLabelPosition(series);
		if (p != null) {
			result = p.getRotationAnchor();
		}
		return result;
	}

	/**
	 * Sets the rotation anchor for the item labels in ALL series.
	 * 
	 * @param anchor
	 *           the anchor (<code>null</code> permitted).
	 * @deprecated Use setPositiveItemLabelPosition()/setNegativeItemLabelPosition().
	 */
	public void setItemLabelRotationAnchor(TextAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setItemLabelRotationAnchor is deprecated.");
	}

	/**
	 * Sets the item label rotation anchor point for a series.
	 * 
	 * @param series
	 *           the series.
	 * @param anchor
	 *           the anchor point.
	 * @deprecated Use setSeriesPositiveItemLabelPosition()/setSeriesNegativeItemLabelPosition().
	 */
	public void setSeriesItemLabelRotationAnchor(int series, TextAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setSeriesItemLabelRotationAnchor is deprecated.");
	}

	/**
	 * Returns the base item label rotation anchor point.
	 * 
	 * @return The anchor point.
	 * @deprecated Use getBasePositiveItemLabelPosition()/getBaseNegativeItemLabelPosition().
	 */
	public TextAnchor getBaseItemLabelRotationAnchor() {
		TextAnchor result = null;
		ItemLabelPosition p = this.getBasePositiveItemLabelPosition();
		if (p != null) {
			result = p.getRotationAnchor();
		}
		return result;
	}

	/**
	 * Sets the base item label rotation anchor point.
	 * 
	 * @param anchor
	 *           the anchor point.
	 * @deprecated Use setBasePositiveItemLabelPosition()/setBaseNegativeItemLabelPosition().
	 */
	public void setBaseItemLabelRotationAnchor(TextAnchor anchor) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setBaseItemLabelRotationAnchor is deprecated.");
	}

	// ANGLE...

	/**
	 * Returns the angle for an item label.
	 * 
	 * @param row
	 *           the row.
	 * @param column
	 *           the column.
	 * @return The angle (in radians).
	 * @deprecated Use getPositiveItemLabelPosition()/getNegativeItemLabelPosition().
	 */
	public Number getItemLabelAngle(int row, int column) {
		return getSeriesItemLabelAngle(row);
	}

	/**
	 * Returns the angle for all the item labels in a series.
	 * 
	 * @param series
	 *           the series.
	 * @return The angle (in radians).
	 * @deprecated Use getSeriesPositiveItemLabelPosition()/getSeriesNegativeItemLabelPosition().
	 */
	public Number getSeriesItemLabelAngle(int series) {
		Number result = null;
		ItemLabelPosition p = this.getSeriesPositiveItemLabelPosition(series);
		if (p != null) {
			result = new Double(p.getAngle());
		}
		return result;
	}

	/**
	 * Sets the angle for the item labels in ALL series (optional).
	 * 
	 * @param angle
	 *           the angle (<code>null</code> permitted).
	 * @deprecated Use setPositiveItemLabelPosition()/setNegativeItemLabelPosition().
	 */
	public void setItemLabelAngle(Number angle) {
		throw new UnsupportedOperationException(
							"AbstractRenderer.setItemLabelAngle is deprecated.");
	}

	/**
	 * Sets the angle for all item labels in a series.
	 * 
	 * @param series
	 *           the series.
	 * @param angle
	 *           the angle.
	 * @deprecated Use setSeriesPositiveItemLabelPosition()/setSeriesNegativeItemLabelPosition().
	 */
	public void setSeriesAngle(int series, Number angle) {
		throw new UnsupportedOperationException("AbstractRenderer.setSeriesAngle is deprecated.");
	}

	/**
	 * Returns the base item label angle.
	 * 
	 * @return The angle.
	 * @deprecated Use getBasePositiveItemLabelPosition()/getBaseNegativeItemLabelPosition().
	 */
	public Number getBaseItemLabelAngle() {
		Number result = null;
		ItemLabelPosition p = this.getBasePositiveItemLabelPosition();
		if (p != null) {
			result = new Double(p.getAngle());
		}
		return result;
	}

	/**
	 * Sets the base item label angle.
	 * 
	 * @param angle
	 *           the angle.
	 * @deprecated Use setBasePositiveItemLabelPosition()/setBaseNegativeItemLabelPosition().
	 */
	public void setBaseAngle(Number angle) {
		throw new UnsupportedOperationException("AbstractRenderer.setBaseAngle is deprecated.");
	}

}
