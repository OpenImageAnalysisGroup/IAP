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
 * -----------------
 * CategoryPlot.java
 * -----------------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Jeremy Bowman;
 * Arnaud Lelievre;
 * $Id: CategoryPlot.java,v 1.1 2011-01-31 09:02:09 klukas Exp $
 * Changes (from 21-Jun-2001)
 * --------------------------
 * 21-Jun-2001 : Removed redundant JFreeChart parameter from constructors (DG);
 * 21-Aug-2001 : Added standard header. Fixed DOS encoding problem (DG);
 * 18-Sep-2001 : Updated header (DG);
 * 15-Oct-2001 : Data source classes moved to com.jrefinery.data.* (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 23-Oct-2001 : Changed intro and trail gaps on bar plots to use percentage of available space
 * rather than a fixed number of units (DG);
 * 12-Dec-2001 : Changed constructors to protected (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 16-Jan-2002 : Increased maximum intro and trail gap percents, plus added some argument checking
 * code. Thanks to Taoufik Romdhane for suggesting this (DG);
 * 05-Feb-2002 : Added accessor methods for the tooltip generator, incorporated alpha-transparency
 * for Plot and subclasses (DG);
 * 06-Mar-2002 : Updated import statements (DG);
 * 14-Mar-2002 : Renamed BarPlot.java --> CategoryPlot.java, and changed code to use the
 * CategoryItemRenderer interface (DG);
 * 22-Mar-2002 : Dropped the getCategories() method (DG);
 * 23-Apr-2002 : Moved the dataset from the JFreeChart class to the Plot class (DG);
 * 29-Apr-2002 : New methods to support printing values at the end of bars, contributed by
 * Jeremy Bowman (DG);
 * 11-May-2002 : New methods for label visibility and overlaid plot support, contributed by
 * Jeremy Bowman (DG);
 * 06-Jun-2002 : Removed the tooltip generator, this is now stored with the renderer. Moved
 * constants into the CategoryPlotConstants interface. Updated Javadoc
 * comments (DG);
 * 10-Jun-2002 : Overridden datasetChanged(...) method to update the upper and lower bound on the
 * range axis (if necessary), updated Javadocs (DG);
 * 25-Jun-2002 : Removed redundant imports (DG);
 * 20-Aug-2002 : Changed the constructor for Marker (DG);
 * 28-Aug-2002 : Added listener notification to setDomainAxis(...) and setRangeAxis(...) (DG);
 * 23-Sep-2002 : Added getLegendItems() method and fixed errors reported by Checkstyle (DG);
 * 28-Oct-2002 : Changes to the CategoryDataset interface (DG);
 * 05-Nov-2002 : Base dataset is now TableDataset not CategoryDataset (DG);
 * 07-Nov-2002 : Renamed labelXXX as valueLabelXXX (DG);
 * 18-Nov-2002 : Added grid settings for both domain and range axis (previously these were set in
 * the axes) (DG);
 * 19-Nov-2002 : Added axis location parameters to constructor (DG);
 * 17-Jan-2003 : Moved to com.jrefinery.chart.plot package (DG);
 * 14-Feb-2003 : Fixed bug in auto-range calculation for secondary axis (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 02-May-2003 : Moved render(...) method up from subclasses. Added secondary range markers.
 * Added an attribute to control the dataset rendering order. Added a
 * drawAnnotations(...) method. Changed the axis location from an int to an
 * AxisLocation (DG);
 * 07-May-2003 : Merged HorizontalCategoryPlot and VerticalCategoryPlot into this class (DG);
 * 02-Jun-2003 : Removed check for range axis compatibility (DG);
 * 04-Jul-2003 : Added a domain gridline position attribute (DG);
 * 21-Jul-2003 : Moved DrawingSupplier to Plot superclass (DG);
 * 19-Aug-2003 : Added equals(...) method and implemented Cloneable (DG);
 * 01-Sep-2003 : Fixed bug 797466 (no change event when secondary dataset changes) (DG);
 * 02-Sep-2003 : Fixed bug 795209 (wrong dataset checked in render2 method) and 790407 (initialise
 * method) (DG);
 * 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL);
 * 08-Sep-2003 : Fixed bug (wrong secondary range axis being used). Changed ValueAxis API (DG);
 * 10-Sep-2003 : Fixed bug in setRangeAxis(...) method (DG);
 * 15-Sep-2003 : Fixed two bugs in serialization, implemented PublicCloneable (DG);
 * 23-Oct-2003 : Added event notification for changes to renderer (DG);
 * 26-Nov-2003 : Fixed bug (849645) in clearRangeMarkers() method (DG);
 * 03-Dec-2003 : Modified draw method to accept anchor (DG);
 * 21-Jan-2004 : Update for renamed method in ValueAxis (DG);
 * 10-Mar-2004 : Fixed bug in axis range calculation when secondary renderer is stacked (DG);
 * 12-May-2004 : Added fixed legend items (DG);
 * 19-May-2004 : Added check for null legend item from renderer (DG);
 * 02-Jun-2004 : Updated the DatasetRenderingOrder class (DG);
 */

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.chart.renderer.CategoryItemRendererState;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetChangeEvent;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.Spacer;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;
import org.jfree.util.SortOrder;

/**
 * A general plotting class that uses data from a {@link CategoryDataset} and renders each data
 * item using a {@link CategoryItemRenderer}.
 */
public class CategoryPlot extends Plot
									implements ValueAxisPlot, RendererChangeListener,
													Cloneable, PublicCloneable, Serializable {

	/** The default visibility of the grid lines plotted against the domain axis. */
	public static final boolean DEFAULT_DOMAIN_GRIDLINES_VISIBLE = false;

	/** The default visibility of the grid lines plotted against the range axis. */
	public static final boolean DEFAULT_RANGE_GRIDLINES_VISIBLE = true;

	/** The default grid line stroke. */
	public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5f,
						BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_BEVEL,
						0.0f,
						new float[] { 2.0f, 2.0f },
						0.0f);

	/** The default grid line paint. */
	public static final Paint DEFAULT_GRIDLINE_PAINT = Color.lightGray;

	/** The default value label font. */
	public static final Font DEFAULT_VALUE_LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);

	/** The resourceBundle for the localization. */
	protected static ResourceBundle localizationResources = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

	/** The plot orientation. */
	private PlotOrientation orientation;

	/** The offset between the data area and the axes. */
	private Spacer axisOffset;

	/** Storage for the domain axes. */
	private ObjectList domainAxes;

	/** Storage for the domain axis locations. */
	private ObjectList domainAxisLocations;

	/**
	 * A flag that controls whether or not the shared domain axis is drawn (only relevant when
	 * the plot is being used as a subplot).
	 */
	private boolean drawSharedDomainAxis;

	/** Storage for the range axes. */
	private ObjectList rangeAxes;

	/** Storage for the range axis locations. */
	private ObjectList rangeAxisLocations;

	/** Storage for the datasets. */
	private ObjectList datasets;

	/** Storage for keys that map datasets to domain axes. */
	private ObjectList datasetToDomainAxisMap;

	/** Storage for keys that map datasets to range axes. */
	private ObjectList datasetToRangeAxisMap;

	/** Storage for the renderers. */
	private ObjectList renderers;

	/** The dataset rendering order. */
	private DatasetRenderingOrder renderingOrder = DatasetRenderingOrder.REVERSE;

	/** Controls the order in which the columns are traversed when rendering the data items. */
	private SortOrder columnRenderingOrder = SortOrder.ASCENDING;

	/** Controls the order in which the rows are traversed when rendering the data items. */
	private SortOrder rowRenderingOrder = SortOrder.ASCENDING;

	/** A flag that controls whether the grid-lines for the domain axis are visible. */
	private boolean domainGridlinesVisible;

	/** The position of the domain gridlines relative to the category. */
	private CategoryAnchor domainGridlinePosition;

	/** The stroke used to draw the domain grid-lines. */
	private transient Stroke domainGridlineStroke;

	/** The paint used to draw the domain grid-lines. */
	private transient Paint domainGridlinePaint;

	/** A flag that controls whether the grid-lines for the range axis are visible. */
	private boolean rangeGridlinesVisible;

	/** The stroke used to draw the range axis grid-lines. */
	private transient Stroke rangeGridlineStroke;

	/** The paint used to draw the range axis grid-lines. */
	private transient Paint rangeGridlinePaint;

	/** The anchor value. */
	private double anchorValue;

	/** A flag that controls whether or not a range crosshair is drawn.. */
	private boolean rangeCrosshairVisible;

	/** The range crosshair value. */
	private double rangeCrosshairValue;

	/** The pen/brush used to draw the crosshair (if any). */
	private transient Stroke rangeCrosshairStroke;

	/** The color used to draw the crosshair (if any). */
	private transient Paint rangeCrosshairPaint;

	/** A flag that controls whether or not the crosshair locks onto actual data points. */
	private boolean rangeCrosshairLockedOnData = true;

	/** A map containing lists of markers for the range axes. */
	private transient Map foregroundRangeMarkers;

	/** A map containing lists of markers for the range axes. */
	private transient Map backgroundRangeMarkers;

	/** A list of annotations (optional) for the plot. */
	private transient List annotations;

	/**
	 * The weight for the plot (only relevant when the plot is used as a subplot within a
	 * combined plot).
	 */
	private int weight;

	/** The fixed space for the domain axis. */
	private AxisSpace fixedDomainAxisSpace;

	/** The fixed space for the range axis. */
	private AxisSpace fixedRangeAxisSpace;

	/**
	 * An optional collection of legend items that can be returned by the
	 * getLegendItems() method.
	 */
	private LegendItemCollection fixedLegendItems;

	/**
	 * Default constructor.
	 */
	public CategoryPlot() {
		this(null, null, null, null);
	}

	/**
	 * Creates a new plot.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 * @param domainAxis
	 *           the domain axis (<code>null</code> permitted).
	 * @param rangeAxis
	 *           the range axis (<code>null</code> permitted).
	 * @param renderer
	 *           the item renderer (<code>null</code> permitted).
	 */
	public CategoryPlot(CategoryDataset dataset,
								CategoryAxis domainAxis,
								ValueAxis rangeAxis,
								CategoryItemRenderer renderer) {

		super();

		this.orientation = PlotOrientation.VERTICAL;

		// allocate storage for dataset, axes and renderers
		this.domainAxes = new ObjectList();
		this.domainAxisLocations = new ObjectList();
		this.rangeAxes = new ObjectList();
		this.rangeAxisLocations = new ObjectList();

		this.datasetToDomainAxisMap = new ObjectList();
		this.datasetToRangeAxisMap = new ObjectList();

		this.renderers = new ObjectList();

		this.datasets = new ObjectList();
		this.datasets.set(0, dataset);
		if (dataset != null) {
			dataset.addChangeListener(this);
		}

		this.axisOffset = new Spacer(Spacer.ABSOLUTE, 0.0, 0.0, 0.0, 0.0);

		setDomainAxisLocation(AxisLocation.BOTTOM_OR_LEFT, false);
		setRangeAxisLocation(AxisLocation.TOP_OR_LEFT, false);

		this.renderers.set(0, renderer);
		if (renderer != null) {
			renderer.setPlot(this);
			renderer.addChangeListener(this);
		}

		this.domainAxes.set(0, domainAxis);
		this.mapDatasetToDomainAxis(0, 0);
		if (domainAxis != null) {
			domainAxis.setPlot(this);
			domainAxis.addChangeListener(this);
		}
		this.drawSharedDomainAxis = false;

		this.rangeAxes.set(0, rangeAxis);
		this.mapDatasetToRangeAxis(0, 0);
		if (rangeAxis != null) {
			rangeAxis.setPlot(this);
			rangeAxis.addChangeListener(this);
		}

		configureDomainAxes();
		configureRangeAxes();

		this.domainGridlinesVisible = DEFAULT_DOMAIN_GRIDLINES_VISIBLE;
		this.domainGridlinePosition = CategoryAnchor.MIDDLE;
		this.domainGridlineStroke = DEFAULT_GRIDLINE_STROKE;
		this.domainGridlinePaint = DEFAULT_GRIDLINE_PAINT;

		this.rangeGridlinesVisible = DEFAULT_RANGE_GRIDLINES_VISIBLE;
		this.rangeGridlineStroke = DEFAULT_GRIDLINE_STROKE;
		this.rangeGridlinePaint = DEFAULT_GRIDLINE_PAINT;

		this.foregroundRangeMarkers = new HashMap();
		this.backgroundRangeMarkers = new HashMap();

		Marker baseline = new ValueMarker(
							0.0, new Color(0.8f, 0.8f, 0.8f, 0.5f), new BasicStroke(1.0f),
							new Color(0.85f, 0.85f, 0.95f, 0.5f), new BasicStroke(1.0f), 0.6f
							);
		addRangeMarker(baseline, Layer.BACKGROUND);

		this.anchorValue = 0.0;

	}

	/**
	 * Returns a string describing the type of plot.
	 * 
	 * @return The type.
	 */
	public String getPlotType() {
		return localizationResources.getString("Category_Plot");
	}

	/**
	 * Returns the orientation of the plot.
	 * 
	 * @return The orientation of the plot.
	 */
	public PlotOrientation getOrientation() {
		return this.orientation;
	}

	/**
	 * Sets the orientation for the plot and sends a {@link PlotChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param orientation
	 *           the orientation (<code>null</code> not permitted).
	 */
	public void setOrientation(PlotOrientation orientation) {
		if (orientation == null) {
			throw new IllegalArgumentException("Null 'orientation' argument.");
		}
		this.orientation = orientation;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the axis offset.
	 * 
	 * @return the axis offset.
	 */
	public Spacer getAxisOffset() {
		return this.axisOffset;
	}

	/**
	 * Sets the axis offsets (gap between the data area and the axes).
	 * 
	 * @param offset
	 *           the offset.
	 */
	public void setAxisOffset(Spacer offset) {
		this.axisOffset = offset;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the domain axis for the plot. If the domain axis for this plot
	 * is <code>null</code>, then the method will return the parent plot's domain axis (if
	 * there is a parent plot).
	 * 
	 * @return The domain axis (<code>null</code> permitted).
	 */
	public CategoryAxis getDomainAxis() {
		return getDomainAxis(0);
	}

	/**
	 * Returns a domain axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return The axis (<code>null</code> possible).
	 */
	public CategoryAxis getDomainAxis(int index) {
		CategoryAxis result = null;
		if (index < this.domainAxes.size()) {
			result = (CategoryAxis) this.domainAxes.get(index);
		}
		if (result == null) {
			Plot parent = getParent();
			if (parent instanceof CategoryPlot) {
				CategoryPlot cp = (CategoryPlot) parent;
				result = cp.getDomainAxis(index);
			}
		}
		return result;
	}

	/**
	 * Sets the domain axis for the plot and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param axis
	 *           the axis (<code>null</code> permitted).
	 */
	public void setDomainAxis(CategoryAxis axis) {
		setDomainAxis(0, axis);
	}

	/**
	 * Sets a domain axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @param axis
	 *           the axis.
	 */
	public void setDomainAxis(int index, CategoryAxis axis) {

		CategoryAxis existing = (CategoryAxis) this.domainAxes.get(index);
		if (existing != null) {
			existing.removeChangeListener(this);
		}

		if (axis != null) {
			axis.setPlot(this);
		}

		this.domainAxes.set(index, axis);
		if (axis != null) {
			axis.configure();
			axis.addChangeListener(this);
		}
		notifyListeners(new PlotChangeEvent(this));

	}

	/**
	 * Returns the domain axis location.
	 * 
	 * @return The location (never <code>null</code>).
	 */
	public AxisLocation getDomainAxisLocation() {
		return getDomainAxisLocation(0);
	}

	/**
	 * Returns the location for a domain axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return The location.
	 */
	public AxisLocation getDomainAxisLocation(int index) {
		AxisLocation result = null;
		if (index < this.domainAxisLocations.size()) {
			result = (AxisLocation) this.domainAxisLocations.get(index);
		}
		if (result == null) {
			result = AxisLocation.getOpposite(getDomainAxisLocation(0));
		}
		return result;

	}

	/**
	 * Sets the location of the domain axis and sends a {@link PlotChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param location
	 *           the axis location (<code>null</code> not permitted).
	 */
	public void setDomainAxisLocation(AxisLocation location) {
		// defer argument checking...
		setDomainAxisLocation(location, true);
	}

	/**
	 * Sets the location of the domain axis.
	 * 
	 * @param location
	 *           the axis location (<code>null</code> not permitted).
	 * @param notify
	 *           a flag that controls whether listeners are notified.
	 */
	public void setDomainAxisLocation(AxisLocation location, boolean notify) {
		if (location == null) {
			throw new IllegalArgumentException("Null 'location' argument.");
		}
		setDomainAxisLocation(0, location);
	}

	/**
	 * Sets the location for a domain axis and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param index
	 *           the axis index.
	 * @param location
	 *           the location.
	 */
	public void setDomainAxisLocation(int index, AxisLocation location) {
		// TODO: handle argument checking for primary axis location which
		// should not be null
		this.domainAxisLocations.set(index, location);
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the domain axis edge. This is derived from the axis location and the plot
	 * orientation.
	 * 
	 * @return the edge (never <code>null</code>).
	 */
	public RectangleEdge getDomainAxisEdge() {
		return getDomainAxisEdge(0);
	}

	/**
	 * Returns the edge for a domain axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return The edge (never <code>null</code>).
	 */
	public RectangleEdge getDomainAxisEdge(int index) {
		RectangleEdge result = null;
		AxisLocation location = getDomainAxisLocation(index);
		if (location != null) {
			result = Plot.resolveDomainAxisLocation(location, this.orientation);
		} else {
			result = RectangleEdge.opposite(getDomainAxisEdge(0));
		}
		return result;
	}

	/**
	 * Clears the domain axes from the plot and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 */
	public void clearDomainAxes() {
		for (int i = 0; i < this.domainAxes.size(); i++) {
			CategoryAxis axis = (CategoryAxis) this.domainAxes.get(i);
			if (axis != null) {
				axis.removeChangeListener(this);
			}
		}
		this.domainAxes.clear();
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Configures the domain axes.
	 */
	public void configureDomainAxes() {
		for (int i = 0; i < this.domainAxes.size(); i++) {
			CategoryAxis axis = (CategoryAxis) this.domainAxes.get(i);
			if (axis != null) {
				axis.configure();
			}
		}
	}

	/**
	 * Returns the range axis for the plot. If the range axis for this plot is
	 * null, then the method will return the parent plot's range axis (if there
	 * is a parent plot).
	 * 
	 * @return The range axis (possibly <code>null</code>).
	 */
	public ValueAxis getRangeAxis() {
		return getRangeAxis(0);
	}

	/**
	 * Returns a range axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return The axis (<code>null</code> possible).
	 */
	public ValueAxis getRangeAxis(int index) {
		ValueAxis result = null;
		if (index < this.rangeAxes.size()) {
			result = (ValueAxis) this.rangeAxes.get(index);
		}
		if (result == null) {
			Plot parent = getParent();
			if (parent instanceof CategoryPlot) {
				CategoryPlot cp = (CategoryPlot) parent;
				result = cp.getRangeAxis(index);
			}
		}
		return result;
	}

	/**
	 * Sets the range axis for the plot and sends a {@link PlotChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param axis
	 *           the axis (<code>null</code> permitted).
	 */
	public void setRangeAxis(ValueAxis axis) {
		setRangeAxis(0, axis);
	}

	/**
	 * Sets a range axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @param axis
	 *           the axis.
	 */
	public void setRangeAxis(int index, ValueAxis axis) {

		ValueAxis existing = (ValueAxis) this.rangeAxes.get(index);
		if (existing != null) {
			existing.removeChangeListener(this);
		}

		if (axis != null) {
			axis.setPlot(this);
		}

		this.rangeAxes.set(index, axis);
		if (axis != null) {
			axis.configure();
			axis.addChangeListener(this);
		}
		notifyListeners(new PlotChangeEvent(this));

	}

	/**
	 * Returns the range axis location.
	 * 
	 * @return the location (never <code>null</code>).
	 */
	public AxisLocation getRangeAxisLocation() {
		return getRangeAxisLocation(0);
	}

	/**
	 * Returns the location for a range axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return The location.
	 */
	public AxisLocation getRangeAxisLocation(int index) {
		AxisLocation result = null;
		if (index < this.rangeAxisLocations.size()) {
			result = (AxisLocation) this.rangeAxisLocations.get(index);
		}
		if (result == null) {
			result = AxisLocation.getOpposite(getRangeAxisLocation(0));
		}
		return result;
	}

	/**
	 * Sets the location of the range axis and sends a {@link PlotChangeEvent} to all registered
	 * listeners.
	 * 
	 * @param location
	 *           the location (<code>null</code> not permitted).
	 */
	public void setRangeAxisLocation(AxisLocation location) {
		// defer argument checking...
		setRangeAxisLocation(location, true);
	}

	/**
	 * Sets the location of the range axis and, if requested, sends a {@link PlotChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param location
	 *           the location (<code>null</code> not permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setRangeAxisLocation(AxisLocation location, boolean notify) {
		setRangeAxisLocation(0, location, notify);
	}

	/**
	 * Sets the location for a range axis and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param index
	 *           the axis index.
	 * @param location
	 *           the location.
	 */
	public void setRangeAxisLocation(int index, AxisLocation location) {
		setRangeAxisLocation(index, location, true);
	}

	/**
	 * Sets the location for a range axis and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param index
	 *           the axis index.
	 * @param location
	 *           the location.
	 * @param notify
	 *           notify listeners?
	 */
	public void setRangeAxisLocation(int index, AxisLocation location, boolean notify) {
		// TODO: don't allow null for index = 0
		this.rangeAxisLocations.set(index, location);
		if (notify) {
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Returns the edge where the primary range axis is located.
	 * 
	 * @return The edge (never <code>null</code>).
	 */
	public RectangleEdge getRangeAxisEdge() {
		return getRangeAxisEdge(0);
	}

	/**
	 * Returns the edge for a range axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return The edge.
	 */
	public RectangleEdge getRangeAxisEdge(int index) {
		AxisLocation location = getRangeAxisLocation(index);
		RectangleEdge result = Plot.resolveRangeAxisLocation(location, this.orientation);
		if (result == null) {
			result = RectangleEdge.opposite(getRangeAxisEdge(0));
		}
		return result;
	}

	/**
	 * Clears the range axes from the plot and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 */
	public void clearRangeAxes() {
		for (int i = 0; i < this.rangeAxes.size(); i++) {
			ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
			if (axis != null) {
				axis.removeChangeListener(this);
			}
		}
		this.rangeAxes.clear();
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Configures the range axes.
	 */
	public void configureRangeAxes() {
		for (int i = 0; i < this.rangeAxes.size(); i++) {
			ValueAxis axis = (ValueAxis) this.rangeAxes.get(i);
			if (axis != null) {
				axis.configure();
			}
		}
	}

	/**
	 * Returns the primary dataset for the plot.
	 * 
	 * @return The primary dataset (possibly <code>null</code>).
	 */
	public CategoryDataset getDataset() {
		return getDataset(0);
	}

	/**
	 * Returns a dataset.
	 * 
	 * @param index
	 *           the dataset index.
	 * @return The dataset (possibly <code>null</code>).
	 */
	public CategoryDataset getDataset(int index) {
		CategoryDataset result = null;
		if (this.datasets.size() > index) {
			result = (CategoryDataset) this.datasets.get(index);
		}
		return result;
	}

	/**
	 * Sets the dataset for the plot, replacing the existing dataset, if there is one. This
	 * method also calls the {@link #datasetChanged(DatasetChangeEvent)} method, which adjusts the
	 * axis ranges if necessary and sends a {@link PlotChangeEvent} to all registered listeners.
	 * 
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 */
	public void setDataset(CategoryDataset dataset) {
		setDataset(0, dataset);
	}

	/**
	 * Sets a dataset for the plot.
	 * 
	 * @param index
	 *           the dataset index.
	 * @param dataset
	 *           the dataset (<code>null</code> permitted).
	 */
	public void setDataset(int index, CategoryDataset dataset) {

		CategoryDataset existing = (CategoryDataset) this.datasets.get(index);
		if (existing != null) {
			existing.removeChangeListener(this);
		}
		this.datasets.set(index, dataset);
		if (dataset != null) {
			dataset.addChangeListener(this);
		}

		// send a dataset change event to self...
		DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
		datasetChanged(event);

	}

	/**
	 * Maps a dataset to a particular domain axis.
	 * 
	 * @param index
	 *           the dataset index (zero-based).
	 * @param axisIndex
	 *           the axis index (zero-based).
	 */
	public void mapDatasetToDomainAxis(int index, int axisIndex) {
		this.datasetToDomainAxisMap.set(index, new Integer(axisIndex));
		// fake a dataset change event to update axes...
		datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
	}

	/**
	 * Returns the domain axis for a dataset. You can change the axis for a dataset using the
	 * mapDatasetToDomainAxis() method.
	 * 
	 * @param index
	 *           the dataset index.
	 * @return The domain axis.
	 */
	public CategoryAxis getDomainAxisForDataset(int index) {
		CategoryAxis result = getDomainAxis();
		Integer axisIndex = (Integer) this.datasetToDomainAxisMap.get(index);
		if (axisIndex != null) {
			result = getDomainAxis(axisIndex.intValue());
		}
		return result;
	}

	/**
	 * Maps a dataset to a particular range axis.
	 * 
	 * @param index
	 *           the dataset index (zero-based).
	 * @param axisIndex
	 *           the axis index (zero-based).
	 */
	public void mapDatasetToRangeAxis(int index, int axisIndex) {
		this.datasetToRangeAxisMap.set(index, new Integer(axisIndex));
		// fake a dataset change event to update axes...
		datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
	}

	/**
	 * Returns the range axis for a dataset. You can change the axis for a dataset using the
	 * mapDatasetToRangeAxis() method.
	 * 
	 * @param index
	 *           the dataset index.
	 * @return The range axis.
	 */
	public ValueAxis getRangeAxisForDataset(int index) {
		ValueAxis result = getRangeAxis();
		Integer axisIndex = (Integer) this.datasetToRangeAxisMap.get(index);
		if (axisIndex != null) {
			result = getRangeAxis(axisIndex.intValue());
		}
		return result;
	}

	/**
	 * Returns a reference to the renderer for the plot.
	 * 
	 * @return The renderer.
	 */
	public CategoryItemRenderer getRenderer() {
		return getRenderer(0);
	}

	/**
	 * Returns a renderer.
	 * 
	 * @param index
	 *           the renderer index.
	 * @return The renderer (possibly <code>null</code>).
	 */
	public CategoryItemRenderer getRenderer(int index) {
		CategoryItemRenderer result = null;
		if (this.renderers.size() > index) {
			result = (CategoryItemRenderer) this.renderers.get(index);
		}
		return result;

	}

	/**
	 * Sets the renderer at index 0 (sometimes referred to as the "primary" renderer) and
	 * sends a {@link PlotChangeEvent} to all registered listeners.
	 * 
	 * @param renderer
	 *           the renderer (<code>null</code> permitted.
	 */
	public void setRenderer(CategoryItemRenderer renderer) {
		setRenderer(0, renderer, true);
	}

	/**
	 * Sets the renderer at index 0 (sometimes referred to as the "primary" renderer) and,
	 * if requested, sends a {@link PlotChangeEvent} to all registered listeners.
	 * <p>
	 * You can set the renderer to <code>null</code>, but this is not recommended because:
	 * <ul>
	 * <li>no data will be displayed;</li>
	 * <li>the plot background will not be painted;</li>
	 * </ul>
	 * 
	 * @param renderer
	 *           the renderer (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setRenderer(CategoryItemRenderer renderer, boolean notify) {
		setRenderer(0, renderer, notify);
	}

	/**
	 * Sets the renderer at the specified index and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param index
	 *           the index.
	 * @param renderer
	 *           the renderer (<code>null</code> permitted).
	 */
	public void setRenderer(int index, CategoryItemRenderer renderer) {
		setRenderer(index, renderer, true);
	}

	/**
	 * Sets a renderer. A {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param index
	 *           the index.
	 * @param renderer
	 *           the renderer (<code>null</code> permitted).
	 * @param notify
	 *           notify listeners?
	 */
	public void setRenderer(int index, CategoryItemRenderer renderer, boolean notify) {

		// stop listening to the existing renderer...
		CategoryItemRenderer existing = (CategoryItemRenderer) this.renderers.get(index);
		if (existing != null) {
			existing.removeChangeListener(this);
		}

		// register the new renderer...
		this.renderers.set(index, renderer);
		if (renderer != null) {
			renderer.setPlot(this);
			renderer.addChangeListener(this);
		}

		configureDomainAxes();
		configureRangeAxes();

		if (notify) {
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Returns the renderer for the specified dataset.
	 * 
	 * @param d
	 *           the dataset (<code>null</code> permitted).
	 * @return the renderer (possibly <code>null</code>).
	 */
	public CategoryItemRenderer getRendererForDataset(CategoryDataset d) {
		CategoryItemRenderer result = null;
		for (int i = 0; i < this.datasets.size(); i++) {
			if (this.datasets.get(i) == d) {
				result = (CategoryItemRenderer) this.renderers.get(i);
				break;
			}
		}
		return result;
	}

	/**
	 * Returns the dataset rendering order.
	 * 
	 * @return The order (never <code>null</code>).
	 */
	public DatasetRenderingOrder getDatasetRenderingOrder() {
		return this.renderingOrder;
	}

	/**
	 * Sets the rendering order and sends a {@link PlotChangeEvent} to all registered listeners.
	 * By default, the plot renders the primary dataset last (so that
	 * the primary dataset overlays the secondary datasets). You can reverse this if you want to.
	 * 
	 * @param order
	 *           the rendering order (<code>null</code> not permitted).
	 */
	public void setDatasetRenderingOrder(DatasetRenderingOrder order) {
		if (order == null) {
			throw new IllegalArgumentException("Null 'order' argument.");
		}
		this.renderingOrder = order;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the order in which the columns are rendered.
	 * 
	 * @return The order.
	 */
	public SortOrder getColumnRenderingOrder() {
		return this.columnRenderingOrder;
	}

	/**
	 * Sets the order in which the columns should be rendered.
	 * 
	 * @param order
	 *           the order.
	 */
	public void setColumnRenderingOrder(SortOrder order) {
		this.columnRenderingOrder = order;
	}

	/**
	 * Returns the order in which the rows should be rendered.
	 * 
	 * @return the order (never <code>null</code>).
	 */
	public SortOrder getRowRenderingOrder() {
		return this.rowRenderingOrder;
	}

	/**
	 * Sets the order in which the rows should be rendered.
	 * 
	 * @param order
	 *           the order (<code>null</code> not allowed).
	 */
	public void setRowRenderingOrder(SortOrder order) {
		if (order == null) {
			throw new IllegalArgumentException("Null 'order' argument.");
		}
		this.rowRenderingOrder = order;
	}

	/**
	 * Returns the flag that controls whether the domain grid-lines are visible.
	 * 
	 * @return the <code>true</code> or <code>false</code>.
	 */
	public boolean isDomainGridlinesVisible() {
		return this.domainGridlinesVisible;
	}

	/**
	 * Sets the flag that controls whether or not grid-lines are drawn against the domain axis.
	 * <p>
	 * If the flag value changes, a {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param visible
	 *           the new value of the flag.
	 */
	public void setDomainGridlinesVisible(boolean visible) {
		if (this.domainGridlinesVisible != visible) {
			this.domainGridlinesVisible = visible;
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Returns the position used for the domain gridlines.
	 * 
	 * @return The gridline position.
	 */
	public CategoryAnchor getDomainGridlinePosition() {
		return this.domainGridlinePosition;
	}

	/**
	 * Sets the position used for the domain gridlines.
	 * 
	 * @param position
	 *           the position.
	 */
	public void setDomainGridlinePosition(CategoryAnchor position) {
		this.domainGridlinePosition = position;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the stroke used to draw grid-lines against the domain axis.
	 * 
	 * @return the stroke.
	 */
	public Stroke getDomainGridlineStroke() {
		return this.domainGridlineStroke;
	}

	/**
	 * Sets the stroke used to draw grid-lines against the domain axis. A {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param stroke
	 *           the stroke.
	 */
	public void setDomainGridlineStroke(Stroke stroke) {
		this.domainGridlineStroke = stroke;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint used to draw grid-lines against the domain axis.
	 * 
	 * @return the paint.
	 */
	public Paint getDomainGridlinePaint() {
		return this.domainGridlinePaint;
	}

	/**
	 * Sets the paint used to draw the grid-lines (if any) against the domain axis.
	 * A {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setDomainGridlinePaint(Paint paint) {
		this.domainGridlinePaint = paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the flag that controls whether the range grid-lines are visible.
	 * 
	 * @return the flag.
	 */
	public boolean isRangeGridlinesVisible() {
		return this.rangeGridlinesVisible;
	}

	/**
	 * Sets the flag that controls whether or not grid-lines are drawn against the range axis.
	 * If the flag changes value, a {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param visible
	 *           the new value of the flag.
	 */
	public void setRangeGridlinesVisible(boolean visible) {
		if (this.rangeGridlinesVisible != visible) {
			this.rangeGridlinesVisible = visible;
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Returns the stroke used to draw the grid-lines against the range axis.
	 * 
	 * @return the stroke.
	 */
	public Stroke getRangeGridlineStroke() {
		return this.rangeGridlineStroke;
	}

	/**
	 * Sets the stroke used to draw the grid-lines against the range axis.
	 * A {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param stroke
	 *           the stroke.
	 */
	public void setRangeGridlineStroke(Stroke stroke) {
		this.rangeGridlineStroke = stroke;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the paint used to draw the grid-lines against the range axis.
	 * 
	 * @return the paint.
	 */
	public Paint getRangeGridlinePaint() {
		return this.rangeGridlinePaint;
	}

	/**
	 * Sets the paint used to draw the grid lines against the range axis.
	 * A {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param paint
	 *           the paint.
	 */
	public void setRangeGridlinePaint(Paint paint) {
		this.rangeGridlinePaint = paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the fixed legend items, if any.
	 * 
	 * @return The legend items (possibly <code>null</code>).
	 */
	public LegendItemCollection getFixedLegendItems() {
		return this.fixedLegendItems;
	}

	/**
	 * Sets the fixed legend items for the plot. Leave this set to <code>null</code> if you
	 * prefer the legend items to be created automatically.
	 * 
	 * @param items
	 *           the legend items (<code>null</code> permitted).
	 */
	public void setFixedLegendItems(LegendItemCollection items) {
		this.fixedLegendItems = items;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the legend items for the plot. By default, this method creates a legend item for
	 * each series in each of the datasets. You can change this behaviour by overriding this
	 * method.
	 * 
	 * @return The legend items.
	 */
	public LegendItemCollection getLegendItems() {
		LegendItemCollection result = this.fixedLegendItems;
		if (result == null) {
			result = new LegendItemCollection();
			// get the legend items for the datasets...
			int count = this.datasets.size();
			for (int datasetIndex = 0; datasetIndex < count; datasetIndex++) {
				CategoryDataset dataset = getDataset(datasetIndex);
				if (dataset != null) {
					CategoryItemRenderer renderer = getRenderer(datasetIndex);
					if (renderer != null) {
						int seriesCount = dataset.getRowCount();
						for (int i = 0; i < seriesCount; i++) {
							LegendItem item = renderer.getLegendItem(datasetIndex, i);
							if (item != null) {
								result.add(item);
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Handles a 'click' on the plot by updating the anchor value.
	 * 
	 * @param x
	 *           x-coordinate of the click (in Java2D space).
	 * @param y
	 *           y-coordinate of the click (in Java2D space).
	 * @param info
	 *           information about the plot's dimensions.
	 */
	public void handleClick(int x, int y, PlotRenderingInfo info) {

		Rectangle2D dataArea = info.getDataArea();
		if (dataArea.contains(x, y)) {
			// set the anchor value for the range axis...
			double java2D = 0.0;
			if (this.orientation == PlotOrientation.HORIZONTAL) {
				java2D = x;
			} else
				if (this.orientation == PlotOrientation.VERTICAL) {
					java2D = y;
				}
			RectangleEdge edge = Plot.resolveRangeAxisLocation(getRangeAxisLocation(),
																					this.orientation);
			double value = getRangeAxis().java2DToValue(java2D, info.getDataArea(), edge);
			setAnchorValue(value);
			setRangeCrosshairValue(value);
		}

	}

	/**
	 * Zooms (in or out) on the plot's value axis.
	 * <p>
	 * If the value 0.0 is passed in as the zoom percent, the auto-range calculation for the axis is restored (which sets the range to include the minimum and
	 * maximum data values, thus displaying all the data).
	 * 
	 * @param percent
	 *           the zoom amount.
	 */
	public void zoom(double percent) {

		if (percent > 0.0) {
			double range = getRangeAxis().getRange().getLength();
			double scaledRange = range * percent;
			getRangeAxis().setRange(
								this.anchorValue - scaledRange / 2.0,
								this.anchorValue + scaledRange / 2.0
								);
		} else {
			getRangeAxis().setAutoRange(true);
		}

	}

	/**
	 * Receives notification of a change to the plot's dataset.
	 * <P>
	 * The range axis bounds will be recalculated if necessary.
	 * 
	 * @param event
	 *           information about the event (not used here).
	 */
	public void datasetChanged(DatasetChangeEvent event) {

		int count = this.rangeAxes.size();
		for (int axisIndex = 0; axisIndex < count; axisIndex++) {
			ValueAxis yAxis = getRangeAxis(axisIndex);
			if (yAxis != null) {
				yAxis.configure();
			}
		}
		if (getParent() != null) {
			getParent().datasetChanged(event);
		} else {
			PlotChangeEvent e = new PlotChangeEvent(this);
			notifyListeners(e);
		}

	}

	/**
	 * Receives notification of a renderer change event.
	 * 
	 * @param event
	 *           the event.
	 */
	public void rendererChanged(RendererChangeEvent event) {
		Plot parent = getParent();
		if (parent != null) {
			if (parent instanceof RendererChangeListener) {
				RendererChangeListener rcl = (RendererChangeListener) parent;
				rcl.rendererChanged(event);
			} else {
				// this should never happen with the existing code, but throw an exception in
				// case future changes make it possible...
				throw new RuntimeException("The renderer has changed and I don't know what to do!");
			}
		} else {
			PlotChangeEvent e = new PlotChangeEvent(this);
			notifyListeners(e);
		}
	}

	/**
	 * Adds a marker for display (in the foreground) against the range axis and sends a {@link PlotChangeEvent} to all registered listeners. Typically a marker
	 * will be drawn
	 * by the renderer as a line perpendicular to the range axis, however this is entirely up
	 * to the renderer.
	 * 
	 * @param marker
	 *           the marker (<code>null</code> not permitted).
	 */
	public void addRangeMarker(Marker marker) {
		addRangeMarker(marker, Layer.FOREGROUND);
	}

	/**
	 * Adds a marker for display against the range axis and sends a {@link PlotChangeEvent} to
	 * all registered listeners. Typically a marker will be drawn by the renderer as a line
	 * perpendicular to the range axis, however this is entirely up to the renderer.
	 * 
	 * @param marker
	 *           the marker (<code>null</code> not permitted).
	 * @param layer
	 *           the layer (foreground or background) (<code>null</code> not permitted).
	 */
	public void addRangeMarker(Marker marker, Layer layer) {
		addRangeMarker(0, marker, layer);
	}

	/**
	 * Adds a marker for display by a particular renderer.
	 * <P>
	 * Typically a marker will be drawn by the renderer as a line perpendicular to a range axis, however this is entirely up to the renderer.
	 * 
	 * @param index
	 *           the renderer index.
	 * @param marker
	 *           the marker.
	 * @param layer
	 *           the layer.
	 */
	public void addRangeMarker(int index, Marker marker, Layer layer) {
		Collection markers;
		if (layer == Layer.FOREGROUND) {
			markers = (Collection) this.foregroundRangeMarkers.get(new Integer(index));
			if (markers == null) {
				markers = new java.util.ArrayList();
				this.foregroundRangeMarkers.put(new Integer(index), markers);
			}
			markers.add(marker);
		} else
			if (layer == Layer.BACKGROUND) {
				markers = (Collection) this.backgroundRangeMarkers.get(new Integer(index));
				if (markers == null) {
					markers = new java.util.ArrayList();
					this.backgroundRangeMarkers.put(new Integer(index), markers);
				}
				markers.add(marker);
			}
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Clears all the range markers for the plot and sends a {@link PlotChangeEvent} to all registered listeners.
	 */
	public void clearRangeMarkers() {
		if (this.backgroundRangeMarkers != null) {
			this.backgroundRangeMarkers.clear();
		}
		if (this.foregroundRangeMarkers != null) {
			this.foregroundRangeMarkers.clear();
		}
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the list of range markers (read only) for the specified layer.
	 * 
	 * @param layer
	 *           the layer (foreground or background).
	 * @return The list of range markers.
	 */
	public Collection getRangeMarkers(Layer layer) {
		return getRangeMarkers(0, layer);
	}

	/**
	 * Returns a collection of range markers for a particular renderer and layer.
	 * 
	 * @param index
	 *           the renderer index.
	 * @param layer
	 *           the layer.
	 * @return A collection of markers (possibly <code>null</code>).
	 */
	public Collection getRangeMarkers(int index, Layer layer) {
		Collection result = null;
		Integer key = new Integer(index);
		if (layer == Layer.FOREGROUND) {
			result = (Collection) this.foregroundRangeMarkers.get(key);
		} else
			if (layer == Layer.BACKGROUND) {
				result = (Collection) this.backgroundRangeMarkers.get(key);
			}
		if (result != null) {
			result = Collections.unmodifiableCollection(result);
		}
		return result;
	}

	/**
	 * Clears all the range markers for the specified renderer.
	 * 
	 * @param index
	 *           the renderer index.
	 */
	public void clearRangeMarkers(int index) {
		Integer key = new Integer(index);
		if (this.backgroundRangeMarkers != null) {
			Collection markers = (Collection) this.backgroundRangeMarkers.get(key);
			if (markers != null) {
				markers.clear();
			}
		}
		if (this.foregroundRangeMarkers != null) {
			Collection markers = (Collection) this.foregroundRangeMarkers.get(key);
			if (markers != null) {
				markers.clear();
			}
		}
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns a flag indicating whether or not the range crosshair is visible.
	 * 
	 * @return the flag.
	 */
	public boolean isRangeCrosshairVisible() {
		return this.rangeCrosshairVisible;
	}

	/**
	 * Sets the flag indicating whether or not the range crosshair is visible.
	 * 
	 * @param flag
	 *           the new value of the flag.
	 */
	public void setRangeCrosshairVisible(boolean flag) {

		if (this.rangeCrosshairVisible != flag) {
			this.rangeCrosshairVisible = flag;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns a flag indicating whether or not the crosshair should "lock-on"
	 * to actual data values.
	 * 
	 * @return the flag.
	 */
	public boolean isRangeCrosshairLockedOnData() {
		return this.rangeCrosshairLockedOnData;
	}

	/**
	 * Sets the flag indicating whether or not the range crosshair should "lock-on"
	 * to actual data values.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setRangeCrosshairLockedOnData(boolean flag) {

		if (this.rangeCrosshairLockedOnData != flag) {
			this.rangeCrosshairLockedOnData = flag;
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the range crosshair value.
	 * 
	 * @return The value.
	 */
	public double getRangeCrosshairValue() {
		return this.rangeCrosshairValue;
	}

	/**
	 * Sets the domain crosshair value.
	 * <P>
	 * Registered listeners are notified that the plot has been modified, but only if the crosshair is visible.
	 * 
	 * @param value
	 *           the new value.
	 */
	public void setRangeCrosshairValue(double value) {

		setRangeCrosshairValue(value, true);

	}

	/**
	 * Sets the range crosshair value.
	 * <P>
	 * Registered listeners are notified that the axis has been modified, but only if the crosshair is visible.
	 * 
	 * @param value
	 *           the new value.
	 * @param notify
	 *           a flag that controls whether or not listeners are notified.
	 */
	public void setRangeCrosshairValue(double value, boolean notify) {

		this.rangeCrosshairValue = value;
		if (isRangeCrosshairVisible() && notify) {
			notifyListeners(new PlotChangeEvent(this));
		}

	}

	/**
	 * Returns the pen-style (<code>Stroke</code>) used to draw the crosshair (if visible).
	 * 
	 * @return the crosshair stroke.
	 */
	public Stroke getRangeCrosshairStroke() {
		return this.rangeCrosshairStroke;
	}

	/**
	 * Sets the pen-style (<code>Stroke</code>) used to draw the crosshairs (if visible).
	 * A {@link PlotChangeEvent} is sent to all registered listeners.
	 * 
	 * @param stroke
	 *           the new crosshair stroke.
	 */
	public void setRangeCrosshairStroke(Stroke stroke) {
		this.rangeCrosshairStroke = stroke;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the range crosshair color.
	 * 
	 * @return the crosshair color.
	 */
	public Paint getRangeCrosshairPaint() {
		return this.rangeCrosshairPaint;
	}

	/**
	 * Sets the Paint used to color the crosshairs (if visible) and notifies
	 * registered listeners that the axis has been modified.
	 * 
	 * @param paint
	 *           the new crosshair paint.
	 */
	public void setRangeCrosshairPaint(Paint paint) {
		this.rangeCrosshairPaint = paint;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Returns the list of annotations.
	 * 
	 * @return The list of annotations.
	 */
	public List getAnnotations() {
		return this.annotations;
	}

	/**
	 * Adds an annotation to the plot.
	 * 
	 * @param annotation
	 *           the annotation.
	 */
	public void addAnnotation(CategoryAnnotation annotation) {

		if (this.annotations == null) {
			this.annotations = new java.util.ArrayList();
		}
		this.annotations.add(annotation);
		notifyListeners(new PlotChangeEvent(this));

	}

	/**
	 * Calculates the space required for the domain axis/axes.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the plot area.
	 * @param space
	 *           a carrier for the result (<code>null</code> permitted).
	 * @return The required space.
	 */
	protected AxisSpace calculateDomainAxisSpace(Graphics2D g2, Rectangle2D plotArea,
																	AxisSpace space, boolean isRangeAxisVisible) {

		if (space == null) {
			space = new AxisSpace();
		}

		// reserve some space for the domain axis...
		if (this.fixedDomainAxisSpace != null) {
			if (this.orientation == PlotOrientation.HORIZONTAL) {
				space.ensureAtLeast(this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT);
				space.ensureAtLeast(this.fixedDomainAxisSpace.getRight(), RectangleEdge.RIGHT);
			} else
				if (this.orientation == PlotOrientation.VERTICAL) {
					space.ensureAtLeast(this.fixedDomainAxisSpace.getTop(), RectangleEdge.TOP);
					space.ensureAtLeast(this.fixedDomainAxisSpace.getBottom(), RectangleEdge.BOTTOM);
				}
		} else {
			// reserve space for the primary domain axis...
			RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
								getDomainAxisLocation(), this.orientation
								);
			if (this.drawSharedDomainAxis) {
				space = getDomainAxis().reserveSpace(g2, this, plotArea, domainEdge, space, getRangeAxis().isVisible());
			}

			// reserve space for any domain axes...
			for (int i = 0; i < this.domainAxes.size(); i++) {
				Axis xAxis = (Axis) this.domainAxes.get(i);
				if (xAxis != null) {
					RectangleEdge edge = getDomainAxisEdge(i);
					space = xAxis.reserveSpace(g2, this, plotArea, edge, space, getRangeAxis().isVisible());
				}
			}
		}

		return space;

	}

	/**
	 * Calculates the space required for the range axis/axes.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the plot area.
	 * @param space
	 *           a carrier for the result (<code>null</code> permitted).
	 * @return The required space.
	 */
	protected AxisSpace calculateRangeAxisSpace(Graphics2D g2, Rectangle2D plotArea,
																AxisSpace space, boolean isDomainAxisVisible) {

		if (space == null) {
			space = new AxisSpace();
		}

		// reserve some space for the range axis...
		if (this.fixedRangeAxisSpace != null) {
			if (this.orientation == PlotOrientation.HORIZONTAL) {
				space.ensureAtLeast(this.fixedRangeAxisSpace.getTop(), RectangleEdge.TOP);
				space.ensureAtLeast(this.fixedRangeAxisSpace.getBottom(), RectangleEdge.BOTTOM);
			} else
				if (this.orientation == PlotOrientation.VERTICAL) {
					space.ensureAtLeast(this.fixedRangeAxisSpace.getLeft(), RectangleEdge.LEFT);
					space.ensureAtLeast(this.fixedRangeAxisSpace.getRight(), RectangleEdge.RIGHT);
				}
		} else {
			// reserve space for the range axes (if any)...
			for (int i = 0; i < this.rangeAxes.size(); i++) {
				Axis yAxis = (Axis) this.rangeAxes.get(i);
				if (yAxis != null) {
					RectangleEdge edge = getRangeAxisEdge(i);
					space = yAxis.reserveSpace(g2, this, plotArea, edge, space, isDomainAxisVisible);
				}
			}
		}
		return space;

	}

	/**
	 * Calculates the space required for the axes.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the plot area.
	 * @return The space required for the axes.
	 */
	protected AxisSpace calculateAxisSpace(Graphics2D g2, Rectangle2D plotArea) {

		AxisSpace space = new AxisSpace();
		space = calculateRangeAxisSpace(g2, plotArea, space, getDomainAxis().isVisible());
		space = calculateDomainAxisSpace(g2, plotArea, space, getRangeAxis().isVisible());
		return space;

	}

	/**
	 * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
	 * <P>
	 * At your option, you may supply an instance of {@link PlotRenderingInfo}. If you do, it will be populated with information about the drawing, including
	 * various plot dimensions and tooltip info.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the area within which the plot (including axes) should be drawn.
	 * @param parentState
	 *           the state from the parent plot, if there is one.
	 * @param state
	 *           collects info as the chart is drawn (possibly <code>null</code>).
	 */
	public void draw(Graphics2D g2, Rectangle2D plotArea, PlotState parentState,
							PlotRenderingInfo state) {

		// if the plot area is too small, just return...
		boolean b1 = (plotArea.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
		boolean b2 = (plotArea.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
		if (b1 || b2) {
			return;
		}

		// record the plot area...
		if (state != null) {
			state.setPlotArea(plotArea);
		}

		// adjust the drawing area for the plot insets (if any)...
		Insets insets = getInsets();
		if (insets != null) {
			plotArea.setRect(
								plotArea.getX() + insets.left,
								plotArea.getY() + insets.top,
								plotArea.getWidth() - insets.left - insets.right,
								plotArea.getHeight() - insets.top - insets.bottom
								);
		}

		// calculate the data area...
		AxisSpace space = calculateAxisSpace(g2, plotArea);
		Rectangle2D dataArea = space.shrink(plotArea, null);
		this.axisOffset.trim(dataArea);

		if (state != null) {
			state.setDataArea(dataArea);
		}

		// if there is a renderer, it draws the background, otherwise use the default background...
		if (getRenderer() != null) {
			getRenderer().drawBackground(g2, this, dataArea);
		} else {
			drawBackground(g2, dataArea);
		}

		drawDomainBackgroundColors(g2, dataArea);

		Map axisStateMap = drawAxes(g2, plotArea, dataArea, state);

		drawDomainGridlines(g2, dataArea);

		AxisState rangeAxisState = (AxisState) axisStateMap.get(getRangeAxis());
		if (rangeAxisState == null) {
			if (parentState != null) {
				rangeAxisState = (AxisState) parentState.getSharedAxisStates().get(getRangeAxis());
			}
		}
		if (rangeAxisState != null) {
			drawRangeGridlines(g2, dataArea, rangeAxisState.getTicks());
		}
		// draw the range markers...
		for (int i = 0; i < this.renderers.size(); i++) {
			drawRangeMarkers(g2, dataArea, i, Layer.BACKGROUND);
		}

		axisStateMap = drawAxes(g2, plotArea, dataArea, state);

		drawRangeMarkers(g2, dataArea, Layer.BACKGROUND);

		// now render data items...
		boolean foundData = false;
		Shape savedClip = g2.getClip();
		g2.clip(dataArea);
		// set up the alpha-transparency...
		Composite originalComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));

		DatasetRenderingOrder order = getDatasetRenderingOrder();
		if (order == DatasetRenderingOrder.FORWARD) {
			for (int i = 0; i < this.datasets.size(); i++) {
				foundData = render(g2, dataArea, i, state) || foundData;
			}
		} else { // DatasetRenderingOrder.REVERSE
			for (int i = this.datasets.size() - 1; i >= 0; i--) {
				foundData = render(g2, dataArea, i, state) || foundData;
			}
		}
		g2.setClip(savedClip);
		g2.setComposite(originalComposite);

		if (!foundData) {
			drawNoDataMessage(g2, dataArea);
		}

		// draw vertical crosshair if required...
		if (isRangeCrosshairVisible()) {
			drawRangeLine(
								g2, dataArea, getRangeCrosshairValue(),
								getRangeCrosshairStroke(), getRangeCrosshairPaint());
		}

		// draw the foreground range markers...
		for (int i = 0; i < this.renderers.size(); i++) {
			drawRangeMarkers(g2, dataArea, i, Layer.FOREGROUND);
		}
		drawRangeMarkers(g2, dataArea, Layer.FOREGROUND);

		// draw the annotations (if any)...
		drawAnnotations(g2, dataArea);

		// draw an outline around the plot area...
		if (getRenderer() != null) {
			getRenderer().drawOutline(g2, this, dataArea);
		} else {
			drawOutline(g2, dataArea);
		}

	}

	/**
	 * A utility method for drawing the plot's axes.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the plot area.
	 * @param dataArea
	 *           the data area.
	 * @param plotState
	 *           collects information about the plot (<code>null</code> permitted).
	 * @return a map containing the axis states.
	 */
	protected Map drawAxes(Graphics2D g2,
									Rectangle2D plotArea,
									Rectangle2D dataArea,
									PlotRenderingInfo plotState) {

		AxisCollection axisCollection = new AxisCollection();

		// add domain axes to lists...
		for (int index = 0; index < this.domainAxes.size(); index++) {
			CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(index);
			if (xAxis != null) {
				axisCollection.add(xAxis, getDomainAxisEdge(index));
			}
		}

		// add range axes to lists...
		for (int index = 0; index < this.rangeAxes.size(); index++) {
			ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(index);
			if (yAxis != null) {
				axisCollection.add(yAxis, getRangeAxisEdge(index));
			}
		}

		Map axisStateMap = new HashMap();

		// draw the top axes
		double cursor = dataArea.getMinY() - this.axisOffset.getTopSpace(dataArea.getHeight());
		Iterator iterator = axisCollection.getAxesAtTop().iterator();
		while (iterator.hasNext()) {
			Axis axis = (Axis) iterator.next();
			if (axis != null) {
				AxisState axisState = axis.draw(
									g2, cursor, plotArea, dataArea, RectangleEdge.TOP, plotState
									);
				cursor = axisState.getCursor();
				axisStateMap.put(axis, axisState);
			}
		}

		// draw the bottom axes
		cursor = dataArea.getMaxY() + this.axisOffset.getBottomSpace(dataArea.getHeight());
		iterator = axisCollection.getAxesAtBottom().iterator();
		while (iterator.hasNext()) {
			Axis axis = (Axis) iterator.next();
			if (axis != null) {
				AxisState axisState = axis.draw(
									g2, cursor, plotArea, dataArea, RectangleEdge.BOTTOM, plotState
									);
				cursor = axisState.getCursor();
				axisStateMap.put(axis, axisState);
			}
		}

		// draw the left axes
		cursor = dataArea.getMinX() - this.axisOffset.getLeftSpace(dataArea.getWidth());
		iterator = axisCollection.getAxesAtLeft().iterator();
		while (iterator.hasNext()) {
			Axis axis = (Axis) iterator.next();
			if (axis != null) {
				AxisState axisState = axis.draw(
									g2, cursor, plotArea, dataArea, RectangleEdge.LEFT, plotState
									);
				cursor = axisState.getCursor();
				axisStateMap.put(axis, axisState);
			}
		}

		// draw the right axes
		cursor = dataArea.getMaxX() + this.axisOffset.getRightSpace(dataArea.getWidth());
		iterator = axisCollection.getAxesAtRight().iterator();
		while (iterator.hasNext()) {
			Axis axis = (Axis) iterator.next();
			if (axis != null) {
				AxisState axisState = axis.draw(
									g2, cursor, plotArea, dataArea, RectangleEdge.RIGHT, plotState
									);
				cursor = axisState.getCursor();
				axisStateMap.put(axis, axisState);
			}
		}

		return axisStateMap;

	}

	/**
	 * Draws a representation of a dataset within the dataArea region using the appropriate
	 * renderer.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the region in which the data is to be drawn.
	 * @param index
	 *           the dataset and renderer index.
	 * @param info
	 *           an optional object for collection dimension information.
	 * @return A boolean that indicates whether or not real data was found.
	 */
	public boolean render(Graphics2D g2, Rectangle2D dataArea, int index, PlotRenderingInfo info) {

		boolean foundData = false;
		CategoryDataset currentDataset = getDataset(index);
		CategoryItemRenderer renderer = getRenderer(index);
		CategoryAxis domainAxis = getDomainAxisForDataset(index);
		ValueAxis rangeAxis = getRangeAxisForDataset(index);
		if (!DatasetUtilities.isEmptyOrNull(currentDataset) && renderer != null) {

			foundData = true;
			CategoryItemRendererState state = renderer.initialise(g2, dataArea, this, index, info);

			int columnCount = currentDataset.getColumnCount();
			int rowCount = currentDataset.getRowCount();

			if (this.columnRenderingOrder == SortOrder.ASCENDING) {
				for (int column = 0; column < columnCount; column++) {
					if (this.rowRenderingOrder == SortOrder.ASCENDING) {
						for (int row = 0; row < rowCount; row++) {
							renderer.drawItem(
												g2, state, dataArea, this, domainAxis, rangeAxis,
												currentDataset, row, column
												);
						}
					} else {
						for (int row = rowCount - 1; row >= 0; row--) {
							renderer.drawItem(
												g2, state, dataArea, this, domainAxis, rangeAxis,
												currentDataset, row, column
												);
						}
					}
				}
			} else {
				for (int column = columnCount - 1; column >= 0; column--) {
					if (this.rowRenderingOrder == SortOrder.ASCENDING) {
						for (int row = 0; row < rowCount; row++) {
							renderer.drawItem(
												g2, state, dataArea, this, domainAxis, rangeAxis,
												currentDataset, row, column
												);
						}
					} else {
						for (int row = rowCount - 1; row >= 0; row--) {
							renderer.drawItem(
												g2, state, dataArea, this, domainAxis, rangeAxis,
												currentDataset, row, column
												);
						}
					}
				}
			}
		}
		return foundData;

	}

	private int skipValue = 1;

	public void setSkipLabels(int skipValue) {
		this.skipValue = skipValue;
	}

	private int backgroundColorIndexA = -1;
	private int backgroundColorIndexC = -1;
	private Color backgroundColorA = null;
	private Color backgroundColorB = null;
	private Color backgroundColorC = null;

	public void setCategoryBackgroundPaintA(int index, Color color) {
		backgroundColorIndexA = -1;
		int idx = 0;
		if (index >= 0)
			for (Object o : getCategories()) {
				if (index >= (Integer) o)
					backgroundColorIndexA = idx;
				idx++;
			}

		if (color != null && (color.getRed() != 0 || color.getGreen() != 0 || color.getBlue() != 0))
			backgroundColorA = color;
		else
			backgroundColorA = null;
	}

	public void setCategoryBackgroundPaint(Color color) {
		if (color != null && (color.getRed() != 0 || color.getGreen() != 0 || color.getBlue() != 0))
			backgroundColorB = color;
		else
			backgroundColorB = null;
	}

	public void setCategoryBackgroundPaintC(int index, Color color) {
		backgroundColorIndexC = -1;
		int idx = 0;
		if (index >= 0)
			for (Object o : getCategories()) {
				if (index >= (Integer) o)
					backgroundColorIndexC = idx;
				idx++;
			}
		if (color != null && (color.getRed() != 0 || color.getGreen() != 0 || color.getBlue() != 0))
			backgroundColorC = color;
		else
			backgroundColorC = null;
	}

	protected void drawDomainBackgroundColors(Graphics2D g2, Rectangle2D dataArea) {
		if ((backgroundColorIndexA >= 0 && backgroundColorA != null) ||
							(backgroundColorB != null) ||
							(backgroundColorIndexC >= 0 && backgroundColorC != null)) {
			CategoryAnchor anchor = getDomainGridlinePosition();
			RectangleEdge domainAxisEdge = getDomainAxisEdge();
			// iterate over the categories
			CategoryDataset data = getDataset();
			if (data != null) {
				Color oldCol = g2.getColor();
				CategoryAxis axis = getDomainAxis();
				if (axis != null) {
					int columnCount = data.getColumnCount();
					if (backgroundColorB != null) {
						g2.setColor(backgroundColorB);
						double xx = axis.getCategoryJava2DCoordinate(
														anchor, 0, columnCount, dataArea, domainAxisEdge
												);
						double xx2 = axis.getCategoryJava2DCoordinate(
														anchor, columnCount - 1, columnCount, dataArea, domainAxisEdge
												);
						double xx3 = axis.getCategoryJava2DCoordinate(
														anchor, columnCount, columnCount, dataArea, domainAxisEdge
												);
						int pA = (int) xx;
						int pB = (int) xx2;
						int pC = (int) ((xx3 - xx2) / 2d);
						g2.fillRect(
														pA - pC,
														(int) dataArea.getY(),
														pB - pA + 2 * pC,
														(int) dataArea.getHeight());

					}
					if (backgroundColorIndexA >= 0) {
						double xx = axis.getCategoryJava2DCoordinate(
												anchor, backgroundColorIndexA, columnCount, dataArea, domainAxisEdge
											);
						int pA = (int) xx;
						g2.setColor(backgroundColorA);
						g2.fillRect((int) dataArea.getX(), (int) dataArea.getY(), (int) (pA - dataArea.getX()), (int) dataArea.getHeight());
					}
					if (backgroundColorIndexC >= 0) {
						double xx = axis.getCategoryJava2DCoordinate(
												anchor, backgroundColorIndexC, columnCount, dataArea, domainAxisEdge
											);
						double xx2 = axis.getCategoryJava2DCoordinate(
												anchor, columnCount - 1, columnCount, dataArea, domainAxisEdge
											);
						double xx3 = axis.getCategoryJava2DCoordinate(
												anchor, columnCount, columnCount, dataArea, domainAxisEdge
											);
						int pA = (int) xx;
						int pB = (int) xx2;
						int pC = (int) ((xx3 - xx2) / 2d);
						g2.setColor(backgroundColorC);
						g2.fillRect(
													pA,
													(int) dataArea.getY(),
													pB - pA + pC,
													(int) dataArea.getHeight());
					}
				}
				g2.setColor(oldCol);
			}
		}
	}

	/**
	 * Draws the gridlines for the plot.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the area inside the axes.
	 */
	protected void drawDomainGridlines(Graphics2D g2, Rectangle2D dataArea) {

		// draw the domain grid lines, if any...
		if (isDomainGridlinesVisible()) {
			CategoryAnchor anchor = getDomainGridlinePosition();
			RectangleEdge domainAxisEdge = getDomainAxisEdge();
			Stroke gridStroke = getDomainGridlineStroke();
			Paint gridPaint = getDomainGridlinePaint();
			if ((gridStroke != null) && (gridPaint != null)) {
				// iterate over the categories
				CategoryDataset data = getDataset();
				if (data != null) {
					CategoryAxis axis = getDomainAxis();
					if (axis != null) {
						int columnCount = data.getColumnCount();
						for (int c = 0; c < columnCount; c++) {
							double xx = axis.getCategoryJava2DCoordinate(
												anchor, c, columnCount, dataArea, domainAxisEdge
												);
							CategoryItemRenderer renderer1 = getRenderer();
							if (renderer1 != null) {
								if (c % skipValue == 0)
									renderer1.drawDomainGridline(g2, this, dataArea, xx);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Draws the gridlines for the plot.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the area inside the axes.
	 * @param ticks
	 *           the ticks.
	 */
	protected void drawRangeGridlines(Graphics2D g2, Rectangle2D dataArea, List ticks) {
		// draw the range grid lines, if any...
		if (isRangeGridlinesVisible()) {
			Stroke gridStroke = getRangeGridlineStroke();
			Paint gridPaint = getRangeGridlinePaint();
			if ((gridStroke != null) && (gridPaint != null)) {
				ValueAxis axis = getRangeAxis();
				if (axis != null) {
					Iterator iterator = ticks.iterator();
					while (iterator.hasNext()) {
						ValueTick tick = (ValueTick) iterator.next();
						CategoryItemRenderer renderer1 = getRenderer();
						if (renderer1 != null) {
							renderer1.drawRangeGridline(
												g2, this, getRangeAxis(), dataArea, tick.getValue()
												);
						}
					}
				}
			}
		}
	}

	/**
	 * Draws the annotations...
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the data area.
	 */
	protected void drawAnnotations(Graphics2D g2, Rectangle2D dataArea) {

		if (getAnnotations() != null) {
			Iterator iterator = getAnnotations().iterator();
			while (iterator.hasNext()) {
				CategoryAnnotation annotation = (CategoryAnnotation) iterator.next();
				annotation.draw(g2, this, dataArea, getDomainAxis(), getRangeAxis());
			}
		}

	}

	/**
	 * Draws the range markers (if any) for the specified layer. This method is typically called
	 * from within the draw(...) method.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the data area.
	 * @param layer
	 *           the layer (foreground or background).
	 */
	protected void drawRangeMarkers(Graphics2D g2, Rectangle2D dataArea, Layer layer) {
		CategoryItemRenderer r = getRenderer();
		Collection markers = this.getRangeMarkers(layer);
		if (markers != null && (r != null)) {
			Iterator iterator = markers.iterator();
			while (iterator.hasNext()) {
				Marker marker = (Marker) iterator.next();
				r.drawRangeMarker(g2, this, getRangeAxis(), marker, dataArea);
			}
		}
	}

	/**
	 * Draws the range markers (if any) for an axis and layer. This method is
	 * typically called from within the draw(...) method.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the data area.
	 * @param index
	 *           the renderer index.
	 * @param layer
	 *           the layer (foreground or background).
	 */
	protected void drawRangeMarkers(Graphics2D g2, Rectangle2D dataArea, int index,
												Layer layer) {

		CategoryItemRenderer r = getRenderer(index);
		if (r == null) {
			return;
		}

		Collection markers = getRangeMarkers(index, layer);
		ValueAxis axis = getRangeAxisForDataset(index);
		if (markers != null && axis != null) {
			Iterator iterator = markers.iterator();
			while (iterator.hasNext()) {
				Marker marker = (Marker) iterator.next();
				r.drawRangeMarker(g2, this, axis, marker, dataArea);
			}
		}

	}

	/**
	 * Utility method for drawing a line perpendicular to the range axis (used for crosshairs).
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the area defined by the axes.
	 * @param value
	 *           the data value.
	 * @param stroke
	 *           the line stroke.
	 * @param paint
	 *           the line paint.
	 */
	protected void drawRangeLine(Graphics2D g2,
											Rectangle2D dataArea,
											double value, Stroke stroke, Paint paint) {

		double java2D = getRangeAxis().valueToJava2D(value, dataArea, getRangeAxisEdge());
		Line2D line = null;
		if (this.orientation == PlotOrientation.HORIZONTAL) {
			line = new Line2D.Double(java2D, dataArea.getMinY(), java2D, dataArea.getMaxY());
		} else
			if (this.orientation == PlotOrientation.VERTICAL) {
				line = new Line2D.Double(dataArea.getMinX(), java2D, dataArea.getMaxX(), java2D);
			}
		g2.setStroke(stroke);
		g2.setPaint(paint);
		g2.draw(line);

	}

	/**
	 * Returns the range of data values that will be plotted against the range axis.
	 * If the dataset is <code>null</code>, this method returns <code>null</code>.
	 * 
	 * @param axis
	 *           the axis.
	 * @return The data range.
	 */
	public Range getDataRange(ValueAxis axis) {

		Range result = null;
		List mappedDatasets = new ArrayList();

		int rangeIndex = this.rangeAxes.indexOf(axis);
		if (rangeIndex >= 0) {
			mappedDatasets.addAll(getDatasetsMappedToRangeAxis(rangeIndex));
		} else
			if (axis == getRangeAxis()) {
				mappedDatasets.addAll(getDatasetsMappedToRangeAxis(0));
			}

		// iterate through the datasets that map to the axis and get the union of the ranges.
		Iterator iterator = mappedDatasets.iterator();
		while (iterator.hasNext()) {
			CategoryDataset d = (CategoryDataset) iterator.next();
			CategoryItemRenderer r = getRendererForDataset(d);
			if (r != null) {
				result = Range.combine(result, r.getRangeExtent(d));
			}
		}
		return result;

	}

	/**
	 * A utility method that returns a list of datasets that are mapped to a given range axis.
	 * 
	 * @param index
	 *           the axis index.
	 * @return A list of datasets.
	 */
	private List getDatasetsMappedToRangeAxis(int index) {
		List result = new ArrayList();
		for (int i = 0; i < this.datasets.size(); i++) {
			Integer m = (Integer) this.datasetToRangeAxisMap.get(i);
			if (m == null) {
				if (index == 0) {
					result.add(this.datasets.get(i));
				}
			} else {
				if (m.intValue() == index) {
					result.add(this.datasets.get(i));
				}
			}
		}
		return result;
	}

	/**
	 * Returns the weight for this plot when it is used as a subplot within a combined plot.
	 * 
	 * @return the weight.
	 */
	public int getWeight() {
		return this.weight;
	}

	/**
	 * Sets the weight for the plot.
	 * 
	 * @param weight
	 *           the weight.
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * Returns the fixed domain axis space.
	 * 
	 * @return The fixed domain axis space (possibly <code>null</code>).
	 */
	public AxisSpace getFixedDomainAxisSpace() {
		return this.fixedDomainAxisSpace;
	}

	/**
	 * Sets the fixed domain axis space.
	 * 
	 * @param space
	 *           the space.
	 */
	public void setFixedDomainAxisSpace(AxisSpace space) {
		this.fixedDomainAxisSpace = space;
	}

	/**
	 * Returns the fixed range axis space.
	 * 
	 * @return The fixed range axis space.
	 */
	public AxisSpace getFixedRangeAxisSpace() {
		return this.fixedRangeAxisSpace;
	}

	/**
	 * Sets the fixed range axis space.
	 * 
	 * @param space
	 *           the space.
	 */
	public void setFixedRangeAxisSpace(AxisSpace space) {
		this.fixedRangeAxisSpace = space;
	}

	/**
	 * Returns a list of the categories for the plot.
	 * 
	 * @return A list of the categories for the plot.
	 */
	public List getCategories() {
		List result = null;
		if (getDataset() != null) {
			result = Collections.unmodifiableList(getDataset().getColumnKeys());
		}
		return result;
	}

	/**
	 * Returns the flag that controls whether or not the shared domain axis is drawn for
	 * each subplot.
	 * 
	 * @return A boolean.
	 */
	public boolean getDrawSharedDomainAxis() {
		return this.drawSharedDomainAxis;
	}

	/**
	 * Sets the flag that controls whether the shared domain axis is drawn when this plot
	 * is being used as a subplot.
	 * 
	 * @param draw
	 *           a boolean.
	 */
	public void setDrawSharedDomainAxis(boolean draw) {
		this.drawSharedDomainAxis = draw;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Multiplies the range on the horizontal axis/axes by the specified factor.
	 * 
	 * @param factor
	 *           the zoom factor.
	 */
	public void zoomHorizontalAxes(double factor) {
		if (this.orientation == PlotOrientation.HORIZONTAL) {
			for (int i = 0; i < this.rangeAxes.size(); i++) {
				ValueAxis rangeAxis = (ValueAxis) this.rangeAxes.get(i);
				if (rangeAxis != null) {
					rangeAxis.resizeRange(factor);
				}
			}
		}
	}

	/**
	 * Zooms in on the horizontal axes.
	 * 
	 * @param lowerPercent
	 *           the lower bound.
	 * @param upperPercent
	 *           the upper bound.
	 */
	public void zoomHorizontalAxes(double lowerPercent, double upperPercent) {
		if (this.orientation == PlotOrientation.HORIZONTAL) {
			for (int i = 0; i < this.rangeAxes.size(); i++) {
				ValueAxis rangeAxis = (ValueAxis) this.rangeAxes.get(i);
				if (rangeAxis != null) {
					rangeAxis.zoomRange(lowerPercent, upperPercent);
				}
			}
		}
	}

	/**
	 * Multiplies the range on the vertical axis/axes by the specified factor.
	 * 
	 * @param factor
	 *           the zoom factor.
	 */
	public void zoomVerticalAxes(double factor) {
		if (this.orientation == PlotOrientation.VERTICAL) {
			for (int i = 0; i < this.rangeAxes.size(); i++) {
				ValueAxis rangeAxis = (ValueAxis) this.rangeAxes.get(i);
				if (rangeAxis != null) {
					rangeAxis.resizeRange(factor);
				}
			}
		}
	}

	/**
	 * Zooms in on the vertical axes.
	 * 
	 * @param lowerPercent
	 *           the lower bound.
	 * @param upperPercent
	 *           the upper bound.
	 */
	public void zoomVerticalAxes(double lowerPercent, double upperPercent) {
		if (this.orientation == PlotOrientation.VERTICAL) {
			for (int i = 0; i < this.rangeAxes.size(); i++) {
				ValueAxis rangeAxis = (ValueAxis) this.rangeAxes.get(i);
				if (rangeAxis != null) {
					rangeAxis.zoomRange(lowerPercent, upperPercent);
				}
			}
		}
	}

	/**
	 * Returns the anchor value.
	 * 
	 * @return The anchor value.
	 */
	public double getAnchorValue() {
		return this.anchorValue;
	}

	/**
	 * Sets the anchor value.
	 * 
	 * @param value
	 *           the anchor value.
	 */
	public void setAnchorValue(double value) {
		setAnchorValue(value, true);
	}

	/**
	 * Sets the anchor value.
	 * 
	 * @param value
	 *           the value.
	 * @param notify
	 *           notify listeners?
	 */
	public void setAnchorValue(double value, boolean notify) {
		this.anchorValue = value;
		if (notify) {
			notifyListeners(new PlotChangeEvent(this));
		}
	}

	/**
	 * Tests the plot for equality with an arbitrary object.
	 * 
	 * @param object
	 *           the object to test against (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object object) {

		if (object == this) {
			return true;
		}

		if (object instanceof CategoryPlot && super.equals(object)) {
			CategoryPlot p = (CategoryPlot) object;

			boolean b0 = (this.orientation == p.orientation);
			boolean b1 = ObjectUtils.equal(this.axisOffset, p.axisOffset);
			boolean b2 = (this.domainAxes.equals(p.domainAxes));
			boolean b3 = (this.domainAxisLocations.equals(p.domainAxisLocations));
			boolean b4 = (this.drawSharedDomainAxis == p.drawSharedDomainAxis);
			boolean b5 = (this.rangeAxes.equals(p.rangeAxes));
			boolean b6 = (this.rangeAxisLocations.equals(p.rangeAxisLocations));
			boolean b7 = ObjectUtils.equal(this.datasetToDomainAxisMap, p.datasetToDomainAxisMap);
			boolean b8 = ObjectUtils.equal(this.datasetToRangeAxisMap, p.datasetToRangeAxisMap);
			boolean b9 = ObjectUtils.equal(this.renderers, p.renderers);
			boolean b10 = (this.renderingOrder == p.renderingOrder);
			boolean b11 = (this.columnRenderingOrder == p.columnRenderingOrder);
			boolean b12 = (this.rowRenderingOrder == p.rowRenderingOrder);
			boolean b13 = (this.domainGridlinesVisible == p.domainGridlinesVisible);
			boolean b14 = (this.domainGridlinePosition == p.domainGridlinePosition);
			boolean b15 = ObjectUtils.equal(this.domainGridlineStroke, p.domainGridlineStroke);
			boolean b16 = ObjectUtils.equal(this.domainGridlinePaint, p.domainGridlinePaint);
			boolean b17 = (this.rangeGridlinesVisible == p.rangeGridlinesVisible);
			boolean b18 = ObjectUtils.equal(this.rangeGridlineStroke, p.rangeGridlineStroke);
			boolean b19 = ObjectUtils.equal(this.rangeGridlinePaint, p.rangeGridlinePaint);
			boolean b20 = (this.anchorValue == p.anchorValue);
			boolean b21 = (this.rangeCrosshairVisible == p.rangeCrosshairVisible);
			boolean b22 = (this.rangeCrosshairValue == p.rangeCrosshairValue);
			boolean b23 = ObjectUtils.equal(this.rangeCrosshairStroke, p.rangeCrosshairStroke);
			boolean b24 = ObjectUtils.equal(this.rangeCrosshairPaint, p.rangeCrosshairPaint);
			boolean b25 = (this.rangeCrosshairLockedOnData == p.rangeCrosshairLockedOnData);
			boolean b26 = ObjectUtils.equal(this.foregroundRangeMarkers, p.foregroundRangeMarkers);
			boolean b27 = ObjectUtils.equal(this.backgroundRangeMarkers, p.backgroundRangeMarkers);
			boolean b28 = ObjectUtils.equal(this.annotations, p.annotations);
			boolean b29 = (this.weight == p.weight);
			boolean b30 = ObjectUtils.equal(this.fixedDomainAxisSpace, p.fixedDomainAxisSpace);
			boolean b31 = ObjectUtils.equal(this.fixedRangeAxisSpace, p.fixedRangeAxisSpace);
			return b0 && b1 && b2 && b3 && b4 && b5 && b6 && b7 && b8 && b9
								&& b10 && b11 && b12 && b13 && b14 && b15 && b16 && b17 && b18 && b19
								&& b20 && b21 && b22 && b23 && b24 && b25 && b26 && b27 && b28 && b29
								&& b30 && b31;

		}

		return false;

	}

	/**
	 * Returns a clone of the plot.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the cloning is not supported.
	 */
	public Object clone() throws CloneNotSupportedException {

		CategoryPlot clone = (CategoryPlot) super.clone();

		clone.domainAxes = new ObjectList();
		for (int i = 0; i < this.domainAxes.size(); i++) {
			CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(i);
			if (xAxis != null) {
				CategoryAxis clonedAxis = (CategoryAxis) xAxis.clone();
				clone.setDomainAxis(i, clonedAxis);
			}
		}
		clone.domainAxisLocations = (ObjectList) this.domainAxisLocations.clone();

		clone.rangeAxes = new ObjectList();
		for (int i = 0; i < this.rangeAxes.size(); i++) {
			ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(i);
			if (yAxis != null) {
				ValueAxis clonedAxis = (ValueAxis) yAxis.clone();
				clone.setRangeAxis(i, clonedAxis);
			}
		}
		clone.rangeAxisLocations = (ObjectList) this.rangeAxisLocations.clone();

		clone.datasets = (ObjectList) this.datasets.clone();
		for (int i = 0; i < clone.datasets.size(); i++) {
			CategoryDataset dataset = clone.getDataset(i);
			if (dataset != null) {
				dataset.addChangeListener(clone);
			}
		}
		clone.datasetToDomainAxisMap = (ObjectList) this.datasetToDomainAxisMap.clone();
		clone.datasetToRangeAxisMap = (ObjectList) this.datasetToRangeAxisMap.clone();
		clone.renderers = (ObjectList) this.renderers.clone();
		clone.fixedDomainAxisSpace = (AxisSpace) ObjectUtils.clone(this.fixedDomainAxisSpace);
		clone.fixedRangeAxisSpace = (AxisSpace) ObjectUtils.clone(this.fixedRangeAxisSpace);

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
		SerialUtilities.writeStroke(this.domainGridlineStroke, stream);
		SerialUtilities.writePaint(this.domainGridlinePaint, stream);
		SerialUtilities.writeStroke(this.rangeGridlineStroke, stream);
		SerialUtilities.writePaint(this.rangeGridlinePaint, stream);
		SerialUtilities.writeStroke(this.rangeCrosshairStroke, stream);
		SerialUtilities.writePaint(this.rangeCrosshairPaint, stream);
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
		this.domainGridlineStroke = SerialUtilities.readStroke(stream);
		this.domainGridlinePaint = SerialUtilities.readPaint(stream);
		this.rangeGridlineStroke = SerialUtilities.readStroke(stream);
		this.rangeGridlinePaint = SerialUtilities.readPaint(stream);
		this.rangeCrosshairStroke = SerialUtilities.readStroke(stream);
		this.rangeCrosshairPaint = SerialUtilities.readPaint(stream);

		for (int i = 0; i < this.domainAxes.size(); i++) {
			CategoryAxis xAxis = (CategoryAxis) this.domainAxes.get(i);
			if (xAxis != null) {
				xAxis.setPlot(this);
			}
		}

		for (int i = 0; i < this.rangeAxes.size(); i++) {
			ValueAxis yAxis = (ValueAxis) this.rangeAxes.get(i);
			if (yAxis != null) {
				yAxis.setPlot(this);
			}
		}

		this.foregroundRangeMarkers = new HashMap();
		this.backgroundRangeMarkers = new HashMap();

		Marker baseline = new ValueMarker(
							0.0,
							new Color(0.8f, 0.8f, 0.8f, 0.5f), new BasicStroke(1.0f),
							new Color(0.85f, 0.85f, 0.95f, 0.5f), new BasicStroke(1.0f),
							0.6f
							);
		addRangeMarker(baseline, Layer.BACKGROUND);

	}

}
