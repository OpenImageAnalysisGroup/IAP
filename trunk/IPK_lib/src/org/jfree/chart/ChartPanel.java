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
 * ChartPanel.java
 * ---------------
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Andrzej Porebski;
 * S�ren Caspersen;
 * Jonathan Nash;
 * Hans-Jurgen Greiner;
 * Andreas Schneider;
 * Daniel van Enckevort;
 * David M O'Donnell;
 * Arnaud Lelievre;
 * Matthias Rose;
 * $Id: ChartPanel.java,v 1.3 2011-05-13 09:16:54 klukas Exp $
 * Changes (from 28-Jun-2001)
 * --------------------------
 * 28-Jun-2001 : Integrated buffering code contributed by S�ren Caspersen (DG);
 * 18-Sep-2001 : Updated header and fixed DOS encoding problem (DG);
 * 22-Nov-2001 : Added scaling to improve display of charts in small sizes (DG);
 * 26-Nov-2001 : Added property editing, saving and printing (DG);
 * 11-Dec-2001 : Transferred saveChartAsPNG method to new ChartUtilities class (DG);
 * 13-Dec-2001 : Added tooltips (DG);
 * 16-Jan-2002 : Added an optional crosshair, based on the implementation by Jonathan Nash.
 * Renamed the tooltips class (DG);
 * 23-Jan-2002 : Implemented zooming based on code by Hans-Jurgen Greiner (DG);
 * 05-Feb-2002 : Improved tooltips setup. Renamed method attemptSaveAs()-->doSaveAs() and made
 * it public rather than private (DG);
 * 28-Mar-2002 : Added a new constructor (DG);
 * 09-Apr-2002 : Changed initialisation of tooltip generation, as suggested by Hans-Jurgen
 * Greiner (DG);
 * 27-May-2002 : New interactive zooming methods based on code by Hans-Jurgen Greiner. Renamed
 * JFreeChartPanel --> ChartPanel, moved constants to ChartPanelConstants
 * interface (DG);
 * 31-May-2002 : Fixed a bug with interactive zooming and added a way to control if the
 * zoom rectangle is filled in or drawn as an outline. A mouse drag
 * gesture towards the top left now causes an autoRangeBoth() and is
 * a way to undo zooms (AS);
 * 11-Jun-2002 : Reinstated handleClick method call in mouseClicked(...) to get crosshairs
 * working again (DG);
 * 13-Jun-2002 : Added check for null popup menu in mouseDragged method (DG);
 * 18-Jun-2002 : Added get/set methods for minimum and maximum chart dimensions (DG);
 * 25-Jun-2002 : Removed redundant code (DG);
 * 27-Aug-2002 : Added get/set methods for popup menu (DG);
 * 26-Sep-2002 : Fixed errors reported by Checkstyle (DG);
 * 22-Oct-2002 : Added translation methods for screen <--> Java2D, contributed by Daniel
 * van Enckevort (DG);
 * 05-Nov-2002 : Added a chart reference to the ChartMouseEvent class (DG);
 * 22-Nov-2002 : Added test in zoom method for inverted axes, supplied by David M O'Donnell (DG);
 * 14-Jan-2003 : Implemented ChartProgressListener interface (DG);
 * 14-Feb-2003 : Removed deprecated setGenerateTooltips method (DG);
 * 12-Mar-2003 : Added option to enforce filename extension (see bug id 643173) (DG);
 * 08-Sep-2003 : Added internationalization via use of properties resourceBundle (RFE 690236) (AL);
 * 18-Sep-2003 : Added getScaleX() and getScaleY() methods (protected) as requested by
 * Irv Thomae (DG);
 * 12-Nov-2003 : Added zooming support for the FastScatterPlot class (DG);
 * 24-Nov-2003 : Minor Javadoc updates (DG);
 * 04-Dec-2003 : Added anchor point for crosshair calculation (DG);
 * 17-Jan-2004 : Added new methods to set tooltip delays to be used in this chart panel. Refer
 * to patch 877565 (MR);
 * 02-Feb-2004 : Fixed bug in zooming trigger and added zoomTriggerDistance attribute (DG);
 * 08-Apr-2004 : Changed getScaleX() and getScaleY() from protected to public (DG);
 * 15-Apr-2004 : Added zoomOutFactor and zoomInFactor (DG);
 * 21-Apr-2004 : Fixed zooming bug in mouseReleased() method (DG);
 */

package org.jfree.chart;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.SystemAnalysis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ChartPropertyEditPanel;
import org.jfree.ui.ExtensionFileFilter;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.LogContext;

/**
 * A Swing GUI component for displaying a {@link JFreeChart} object.
 * <P>
 * The panel registers with the chart to receive notification of changes to any component of the chart. The chart is redrawn automatically whenever this
 * notification is received.
 */
public class ChartPanel extends JPanel
								implements ChartPanelConstants,
												ChartChangeListener,
												ChartProgressListener,
												ActionListener,
												MouseListener,
												MouseMotionListener,
												Printable,
												Serializable {
	
	/** The chart that is displayed in the panel. */
	private JFreeChart chart;
	
	/** Storage for registered (chart) mouse listeners. */
	private final List<ChartMouseListener> chartMouseListeners;
	
	/** A flag that controls whether or not the off-screen buffer is used. */
	private final boolean useBuffer;
	
	/** A flag that indicates that the buffer should be refreshed. */
	private boolean refreshBuffer;
	
	/** A buffer for the rendered chart. */
	private Image chartBuffer;
	
	/** The height of the chart buffer. */
	private int chartBufferHeight;
	
	/** The width of the chart buffer. */
	private int chartBufferWidth;
	
	/** The minimum width for drawing a chart (uses scaling for smaller widths). */
	private int minimumDrawWidth;
	
	/** The minimum height for drawing a chart (uses scaling for smaller heights). */
	private int minimumDrawHeight;
	
	/** The maximum width for drawing a chart (uses scaling for bigger widths). */
	private int maximumDrawWidth;
	
	/** The maximum height for drawing a chart (uses scaling for bigger heights). */
	private int maximumDrawHeight;
	
	/** The popup menu for the frame. */
	private JPopupMenu popup;
	
	/** The drawing info collected the last time the chart was drawn. */
	private final ChartRenderingInfo info;
	
	/** The chart anchor point. */
	private Point2D anchor;
	
	/** The scale factor used to draw the chart. */
	private double scaleX;
	
	/** The scale factor used to draw the chart. */
	private double scaleY;
	
	/** The zoom rectangle (selected by the user with the mouse). */
	private transient Rectangle2D zoomRectangle = null;
	
	/** The zoom rectangle starting point (selected by the user with a mouse click). */
	private Point2D zoomPoint = null;
	
	/** Controls if the zoom rectangle is drawn as an outline or filled. */
	private boolean fillZoomRectangle = false;
	
	/** A flag that controls whether or not horizontal zooming is enabled. */
	private boolean horizontalZoom = false;
	
	/** A flag that controls whether or not vertical zooming is enabled. */
	private boolean verticalZoom = false;
	
	/** The minimum distance required to drag the mouse to trigger a zoom. */
	private int zoomTriggerDistance;
	
	/** A flag that controls whether or not horizontal tracing is enabled. */
	private boolean horizontalAxisTrace = false;
	
	/** A flag that controls whether or not vertical tracing is enabled. */
	private boolean verticalAxisTrace = false;
	
	/** Menu item for zooming in on a chart (both axes). */
	private JMenuItem zoomInBothMenuItem;
	
	/** Menu item for zooming in on a chart (horizontal axis). */
	private JMenuItem zoomInHorizontalMenuItem;
	
	/** Menu item for zooming in on a chart (vertical axis). */
	private JMenuItem zoomInVerticalMenuItem;
	
	/** Menu item for zooming out on a chart. */
	private JMenuItem zoomOutBothMenuItem;
	
	/** Menu item for zooming out on a chart (horizontal axis). */
	private JMenuItem zoomOutHorizontalMenuItem;
	
	/** Menu item for zooming out on a chart (vertical axis). */
	private JMenuItem zoomOutVerticalMenuItem;
	
	/** Menu item for resetting the zoom (both axes). */
	private JMenuItem autoRangeBothMenuItem;
	
	/** Menu item for resetting the zoom (horizontal axis only). */
	private JMenuItem autoRangeHorizontalMenuItem;
	
	/** Menu item for resetting the zoom (vertical axis only). */
	private JMenuItem autoRangeVerticalMenuItem;
	
	/** A vertical trace line. */
	private transient Line2D verticalTraceLine;
	
	/** A horizontal trace line. */
	private transient Line2D horizontalTraceLine;
	
	/** A flag that controls whether or not file extensions are enforced. */
	private boolean enforceFileExtensions;
	
	/** A flag that indicates if original tooltip delays are changed. */
	private boolean ownToolTipDelaysActive;
	
	/** Original initial tooltip delay of ToolTipManager.sharedInstance(). */
	private int originalToolTipInitialDelay;
	
	/** Original reshow tooltip delay of ToolTipManager.sharedInstance(). */
	private int originalToolTipReshowDelay;
	
	/** Original dismiss tooltip delay of ToolTipManager.sharedInstance(). */
	private int originalToolTipDismissDelay;
	
	/** Own initial tooltip delay to be used in this chart panel. */
	private int ownToolTipInitialDelay;
	
	/** Own reshow tooltip delay to be used in this chart panel. */
	private int ownToolTipReshowDelay;
	
	/** Own dismiss tooltip delay to be used in this chart panel. */
	private int ownToolTipDismissDelay;
	
	/** The factor used to zoom in on an axis range. */
	private double zoomInFactor = 0.5;
	
	/** The factor used to zoom out on an axis range. */
	private double zoomOutFactor = 2.0;
	
	/** The resourceBundle for the localization. */
	protected static ResourceBundle localizationResources = ResourceBundle.getBundle("org.jfree.chart.LocalizationBundle");
	
	/** Access to logging facilities. */
	private static final LogContext LOGGER = Log.createContext(ChartPanel.class);
	
	/**
	 * Constructs a JFreeChart panel.
	 * 
	 * @param chart
	 *           the chart.
	 */
	public ChartPanel(JFreeChart chart) {
		
		this(
							chart,
							DEFAULT_WIDTH,
							DEFAULT_HEIGHT,
							DEFAULT_MINIMUM_DRAW_WIDTH,
							DEFAULT_MINIMUM_DRAW_HEIGHT,
							DEFAULT_MAXIMUM_DRAW_WIDTH,
							DEFAULT_MAXIMUM_DRAW_HEIGHT,
							DEFAULT_BUFFER_USED,
							true, // properties
				true, // save
				true, // print
				true, // zoom
				true // tooltips
		);
		
	}
	
	/**
	 * Constructs a panel containing a chart.
	 * 
	 * @param chart
	 *           the chart.
	 * @param useBuffer
	 *           a flag controlling whether or not an off-screen buffer is used.
	 */
	public ChartPanel(JFreeChart chart, boolean useBuffer) {
		
		this(chart,
							DEFAULT_WIDTH,
							DEFAULT_HEIGHT,
							DEFAULT_MINIMUM_DRAW_WIDTH,
							DEFAULT_MINIMUM_DRAW_HEIGHT,
							DEFAULT_MAXIMUM_DRAW_WIDTH,
							DEFAULT_MAXIMUM_DRAW_HEIGHT,
							useBuffer,
							true, // properties
				true, // save
				true, // print
				true, // zoom
				true // tooltips
		);
		
	}
	
	/**
	 * Constructs a JFreeChart panel.
	 * 
	 * @param chart
	 *           the chart.
	 * @param properties
	 *           a flag indicating whether or not the chart property
	 *           editor should be available via the popup menu.
	 * @param save
	 *           a flag indicating whether or not save options should be
	 *           available via the popup menu.
	 * @param print
	 *           a flag indicating whether or not the print option
	 *           should be available via the popup menu.
	 * @param zoom
	 *           a flag indicating whether or not zoom options should
	 *           be added to the popup menu.
	 * @param tooltips
	 *           a flag indicating whether or not tooltips should be
	 *           enabled for the chart.
	 */
	public ChartPanel(JFreeChart chart,
								boolean properties,
								boolean save,
								boolean print,
								boolean zoom,
								boolean tooltips) {
		
		this(chart,
							DEFAULT_WIDTH,
							DEFAULT_HEIGHT,
							DEFAULT_MINIMUM_DRAW_WIDTH,
							DEFAULT_MINIMUM_DRAW_HEIGHT,
							DEFAULT_MAXIMUM_DRAW_WIDTH,
							DEFAULT_MAXIMUM_DRAW_HEIGHT,
							DEFAULT_BUFFER_USED,
							properties,
							save,
							print,
							zoom,
							tooltips);
		
	}
	
	/**
	 * Constructs a JFreeChart panel.
	 * 
	 * @param chart
	 *           the chart.
	 * @param width
	 *           the preferred width of the panel.
	 * @param height
	 *           the preferred height of the panel.
	 * @param minimumDrawWidth
	 *           the minimum drawing width.
	 * @param minimumDrawHeight
	 *           the minimum drawing height.
	 * @param maximumDrawWidth
	 *           the maximum drawing width.
	 * @param maximumDrawHeight
	 *           the maximum drawing height.
	 * @param useBuffer
	 *           a flag that indicates whether to use the off-screen
	 *           buffer to improve performance (at the expense of memory).
	 * @param properties
	 *           a flag indicating whether or not the chart property
	 *           editor should be available via the popup menu.
	 * @param save
	 *           a flag indicating whether or not save options should be
	 *           available via the popup menu.
	 * @param print
	 *           a flag indicating whether or not the print option
	 *           should be available via the popup menu.
	 * @param zoom
	 *           a flag indicating whether or not zoom options should be added to the
	 *           popup menu.
	 * @param tooltips
	 *           a flag indicating whether or not tooltips should be enabled for the chart.
	 */
	public ChartPanel(JFreeChart chart,
								int width,
								int height,
								int minimumDrawWidth,
								int minimumDrawHeight,
								int maximumDrawWidth,
								int maximumDrawHeight,
								boolean useBuffer,
								boolean properties,
								boolean save,
								boolean print,
								boolean zoom,
								boolean tooltips) {
		
		this.chart = chart;
		this.chartMouseListeners = new java.util.ArrayList<ChartMouseListener>();
		this.info = new ChartRenderingInfo();
		setPreferredSize(new Dimension(width, height));
		this.useBuffer = useBuffer && !SystemAnalysis.isHeadless();
		this.refreshBuffer = false;
		this.chart.addChangeListener(this);
		this.minimumDrawWidth = minimumDrawWidth;
		this.minimumDrawHeight = minimumDrawHeight;
		this.maximumDrawWidth = maximumDrawWidth;
		this.maximumDrawHeight = maximumDrawHeight;
		this.zoomTriggerDistance = DEFAULT_ZOOM_TRIGGER_DISTANCE;
		
		// set up popup menu...
		this.popup = null;
		if (properties || save || print || zoom) {
			this.popup = createPopupMenu(properties, save, print, zoom);
		}
		
		// enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		// enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		
		setDisplayToolTips(tooltips);
		
		// addMouseListener(this);
		// addMouseMotionListener(this);
		
		this.enforceFileExtensions = true;
		
		// initialize ChartPanel-specific tool tip delays with
		// values the from ToolTipManager.sharedInstance()
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		this.ownToolTipInitialDelay = ttm.getInitialDelay();
		this.ownToolTipDismissDelay = ttm.getDismissDelay();
		this.ownToolTipReshowDelay = ttm.getReshowDelay();
		
	}
	
	public void enableMouseClickProcessing() {
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	/**
	 * Returns the chart contained in the panel.
	 * 
	 * @return the chart (possibly <code>null</code>).
	 */
	public JFreeChart getChart() {
		return this.chart;
	}
	
	/**
	 * Sets the chart that is displayed in the panel.
	 * 
	 * @param chart
	 *           the chart (<code>null</code> permitted).
	 */
	public void setChart(JFreeChart chart) {
		
		// stop listening for changes to the existing chart...
		if (this.chart != null) {
			this.chart.removeChangeListener(this);
			this.chart.removeProgressListener(this);
		}
		
		// add the new chart...
		this.chart = chart;
		this.chart.addChangeListener(this);
		this.chart.addProgressListener(this);
		if (this.useBuffer) {
			this.refreshBuffer = true;
		}
		Plot plot = chart.getPlot();
		ValueAxis horizontalAxis = getHorizontalValueAxis(plot);
		this.horizontalZoom = this.horizontalZoom && (horizontalAxis != null);
		ValueAxis verticalAxis = getVerticalValueAxis(plot);
		this.verticalZoom = this.verticalZoom && (verticalAxis != null);
		repaint();
		
	}
	
	/**
	 * Returns the minimum drawing width for charts.
	 * <P>
	 * If the width available on the panel is less than this, then the chart is drawn at the minimum width then scaled down to fit.
	 * 
	 * @return The minimum drawing width.
	 */
	public int getMinimumDrawWidth() {
		return this.minimumDrawWidth;
	}
	
	/**
	 * Sets the minimum drawing width for the chart on this panel.
	 * <P>
	 * At the time the chart is drawn on the panel, if the available width is less than this amount, the chart will be drawn using the minimum width then scaled
	 * down to fit the available space.
	 * 
	 * @param width
	 *           The width.
	 */
	public void setMinimumDrawWidth(int width) {
		this.minimumDrawWidth = width;
	}
	
	/**
	 * Returns the maximum drawing width for charts.
	 * <P>
	 * If the width available on the panel is greater than this, then the chart is drawn at the maximum width then scaled up to fit.
	 * 
	 * @return The maximum drawing width.
	 */
	public int getMaximumDrawWidth() {
		return this.maximumDrawWidth;
	}
	
	/**
	 * Sets the maximum drawing width for the chart on this panel.
	 * <P>
	 * At the time the chart is drawn on the panel, if the available width is greater than this amount, the chart will be drawn using the maximum width then
	 * scaled up to fit the available space.
	 * 
	 * @param width
	 *           The width.
	 */
	public void setMaximumDrawWidth(int width) {
		this.maximumDrawWidth = width;
	}
	
	/**
	 * Sets the minimum drawing height for the chart on this panel.
	 * <P>
	 * At the time the chart is drawn on the panel, if the available height is less than this amount, the chart will be drawn using the minimum height then
	 * scaled down to fit the available space.
	 * 
	 * @param height
	 *           The height.
	 */
	public void setMinimumDrawHeight(int height) {
		this.minimumDrawHeight = height;
	}
	
	/**
	 * Returns the minimum drawing height for charts.
	 * <P>
	 * If the height available on the panel is less than this, then the chart is drawn at the minimum height then scaled down to fit.
	 * 
	 * @return The minimum drawing height.
	 */
	public int getMinimumDrawHeight() {
		return this.minimumDrawHeight;
	}
	
	/**
	 * Returns the maximum drawing height for charts.
	 * <P>
	 * If the height available on the panel is greater than this, then the chart is drawn at the maximum height then scaled up to fit.
	 * 
	 * @return The maximum drawing height.
	 */
	public int getMaximumDrawHeight() {
		return this.maximumDrawHeight;
	}
	
	/**
	 * Sets the maximum drawing height for the chart on this panel.
	 * <P>
	 * At the time the chart is drawn on the panel, if the available height is greater than this amount, the chart will be drawn using the maximum height then
	 * scaled up to fit the available space.
	 * 
	 * @param height
	 *           The height.
	 */
	public void setMaximumDrawHeight(int height) {
		this.maximumDrawHeight = height;
	}
	
	/**
	 * Returns the X scale factor for the chart. This will be 1.0 if no scaling has
	 * been used.
	 * 
	 * @return The scale factor.
	 */
	public double getScaleX() {
		return this.scaleX;
	}
	
	/**
	 * Returns the Y scale factory for the chart. This will be 1.0 if no scaling has
	 * been used.
	 * 
	 * @return The scale factor.
	 */
	public double getScaleY() {
		return this.scaleY;
	}
	
	/**
	 * Returns the popup menu.
	 * 
	 * @return the popup menu.
	 */
	public JPopupMenu getPopupMenu() {
		return this.popup;
	}
	
	/**
	 * Sets the popup menu for the panel.
	 * 
	 * @param popup
	 *           the new popup menu.
	 */
	public void setPopupMenu(JPopupMenu popup) {
		final ChartPanel thisComp = this;
		this.popup = popup;
		popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				if (thisComp.popup != null) {
					// go through each zoom menu item and decide whether or not to enable it...
					Plot plot = thisComp.chart.getPlot();
					ValueAxis horizontalAxis = getHorizontalValueAxis(plot);
					boolean isHorizontal = (horizontalAxis != null);
					ValueAxis verticalAxis = getVerticalValueAxis(plot);
					boolean isVertical = (verticalAxis != null);
					
					if (thisComp.zoomInHorizontalMenuItem != null) {
						thisComp.zoomInHorizontalMenuItem.setEnabled(isHorizontal);
					}
					if (thisComp.zoomOutHorizontalMenuItem != null) {
						thisComp.zoomOutHorizontalMenuItem.setEnabled(isHorizontal);
					}
					if (thisComp.autoRangeHorizontalMenuItem != null) {
						thisComp.autoRangeHorizontalMenuItem.setEnabled(isHorizontal);
					}
					
					if (thisComp.zoomInVerticalMenuItem != null) {
						thisComp.zoomInVerticalMenuItem.setEnabled(isVertical);
					}
					if (thisComp.zoomOutVerticalMenuItem != null) {
						thisComp.zoomOutVerticalMenuItem.setEnabled(isVertical);
					}
					
					if (thisComp.autoRangeVerticalMenuItem != null) {
						thisComp.autoRangeVerticalMenuItem.setEnabled(isVertical);
					}
					
					if (thisComp.zoomInBothMenuItem != null) {
						thisComp.zoomInBothMenuItem.setEnabled(isHorizontal & isVertical);
					}
					if (thisComp.zoomOutBothMenuItem != null) {
						thisComp.zoomOutBothMenuItem.setEnabled(isHorizontal & isVertical);
					}
					if (thisComp.autoRangeBothMenuItem != null) {
						thisComp.autoRangeBothMenuItem.setEnabled(isHorizontal & isVertical);
					}
				}
			}
			
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}
			
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
		});
	}
	
	/**
	 * Returns the chart rendering info from the most recent chart redraw.
	 * 
	 * @return the chart rendering info.
	 */
	public ChartRenderingInfo getChartRenderingInfo() {
		return this.info;
	}
	
	/**
	 * A flag that controls mouse-based zooming.
	 * 
	 * @param flag
	 *           <code>true</code> enables zooming and rectangle fill on zoom.
	 */
	public void setMouseZoomable(boolean flag) {
		setMouseZoomable(flag, true);
	}
	
	/**
	 * Controls mouse zooming and how the zoom rectangle is displayed
	 * 
	 * @param flag
	 *           <code>true</code> if zooming enabled
	 * @param fillRectangle
	 *           <code>true</code> if zoom rectangle is filled,
	 *           false if rectangle is shown as outline only.
	 */
	public void setMouseZoomable(boolean flag, boolean fillRectangle) {
		setHorizontalZoom(flag);
		setVerticalZoom(flag);
		setFillZoomRectangle(fillRectangle);
	}
	
	/**
	 * A flag that controls mouse-based zooming on the horizontal axis.
	 * 
	 * @param flag
	 *           <code>true</code> enables zooming on HorizontalValuePlots.
	 */
	public void setHorizontalZoom(boolean flag) {
		Plot plot = this.chart.getPlot();
		ValueAxis axis = getHorizontalValueAxis(plot);
		this.horizontalZoom = flag && (axis != null);
	}
	
	/**
	 * A flag that controls how the zoom rectangle is drawn.
	 * 
	 * @param flag
	 *           <code>true</code> instructs to fill the rectangle on
	 *           zoom, otherwise it will be outlined.
	 */
	public void setFillZoomRectangle(boolean flag) {
		this.fillZoomRectangle = flag;
	}
	
	/**
	 * A flag that controls mouse-based zooming on the vertical axis.
	 * 
	 * @param flag
	 *           <code>true</code> enables zooming on VerticalValuePlots.
	 */
	public void setVerticalZoom(boolean flag) {
		Plot plot = this.chart.getPlot();
		ValueAxis axis = getVerticalValueAxis(plot);
		this.verticalZoom = flag && (axis != null);
	}
	
	/**
	 * Returns the zoom trigger distance. This controls how far the mouse must move before a zoom
	 * action is triggered.
	 * 
	 * @return The distance (in Java2D units).
	 */
	public int getZoomTriggerDistance() {
		return this.zoomTriggerDistance;
	}
	
	/**
	 * Sets the zoom trigger distance. This controls how far the mouse must move before a zoom
	 * action is triggered.
	 * 
	 * @param distance
	 *           the distance (in Java2D units).
	 */
	public void setZoomTriggerDistance(int distance) {
		this.zoomTriggerDistance = distance;
	}
	
	/**
	 * A flag that controls trace lines on the horizontal axis.
	 * 
	 * @param flag
	 *           <code>true</code> enables trace lines for the mouse
	 *           pointer on the horizontal axis.
	 */
	public void setHorizontalAxisTrace(boolean flag) {
		this.horizontalAxisTrace = flag;
	}
	
	/**
	 * A flag that controls trace lines on the vertical axis.
	 * 
	 * @param flag
	 *           <code>true</code> enables trace lines for the mouse
	 *           pointer on the vertical axis.
	 */
	public void setVerticalAxisTrace(boolean flag) {
		this.verticalAxisTrace = flag;
	}
	
	/**
	 * Returns <code>true</code> if file extensions should be enforced, and <code>false</code> otherwise.
	 * 
	 * @return The flag.
	 */
	public boolean isEnforceFileExtensions() {
		return this.enforceFileExtensions;
	}
	
	/**
	 * Sets a flag that controls whether or not file extensions are enforced.
	 * 
	 * @param enforce
	 *           the new flag value.
	 */
	public void setEnforceFileExtensions(boolean enforce) {
		this.enforceFileExtensions = enforce;
	}
	
	/**
	 * Switches chart tooltip generation on or off.
	 * 
	 * @param flag
	 *           the flag.
	 */
	public void setDisplayToolTips(boolean flag) {
		
		if (flag) {
			ToolTipManager.sharedInstance().registerComponent(this);
		} else {
			ToolTipManager.sharedInstance().unregisterComponent(this);
		}
		
	}
	
	/**
	 * Returns a string for the tooltip.
	 * 
	 * @param e
	 *           the mouse event.
	 * @return a tool tip or <code>null</code> if no tooltip is available.
	 */
	@Override
	public String getToolTipText(MouseEvent e) {
		
		String result = super.getToolTipText();
		
		if (this.info != null) {
			EntityCollection entities = this.info.getEntityCollection();
			if (entities != null) {
				Insets insets = getInsets();
				ChartEntity entity = entities.getEntity(
									(int) ((e.getX() - insets.left) / this.scaleX),
									(int) ((e.getY() - insets.top) / this.scaleY)
									);
				if (entity != null) {
					result = entity.getToolTipText();
				}
			}
		}
		
		return result;
		
	}
	
	/**
	 * Translates a Java2D point on the chart to a screen location.
	 * 
	 * @param java2DPoint
	 *           the Java2D point.
	 * @return the screen location.
	 */
	public Point translateJava2DToScreen(Point2D java2DPoint) {
		Insets insets = getInsets();
		int x = (int) (java2DPoint.getX() * this.scaleX + insets.left);
		int y = (int) (java2DPoint.getY() * this.scaleY + insets.top);
		return new Point(x, y);
	}
	
	/**
	 * Translates a screen location to a Java2D point.
	 * 
	 * @param screenPoint
	 *           the screen location.
	 * @return the Java2D coordinates.
	 */
	public Point2D translateScreenToJava2D(Point screenPoint) {
		Insets insets = getInsets();
		double x = (screenPoint.getX() - insets.left) / this.scaleX;
		double y = (screenPoint.getY() - insets.top) / this.scaleY;
		return new Point2D.Double(x, y);
	}
	
	/**
	 * Returns the chart entity at a given point.
	 * <P>
	 * This method will return null if there is (a) no entity at the given point, or (b) no entity collection has been generated.
	 * 
	 * @param viewX
	 *           the x-coordinate.
	 * @param viewY
	 *           the y-coordinate.
	 * @return the chart entity (possibly null).
	 */
	public ChartEntity getEntityForPoint(int viewX, int viewY) {
		
		ChartEntity result = null;
		if (this.info != null) {
			Insets insets = getInsets();
			double x = (viewX - insets.left) / this.scaleX;
			double y = (viewY - insets.top) / this.scaleY;
			EntityCollection entities = this.info.getEntityCollection();
			result = entities != null ? entities.getEntity(x, y) : null;
		}
		return result;
		
	}
	
	/**
	 * Sets the refresh buffer flag.
	 * 
	 * @param flag
	 *           <code>true</code> indicate, that the buffer should be refreshed.
	 */
	public void setRefreshBuffer(boolean flag) {
		this.refreshBuffer = flag;
	}
	
	/** Working storage for available panel area after deducting insets. */
	private transient Rectangle2D available = new Rectangle2D.Double();
	
	/** Working storage for the chart area. */
	private transient Rectangle2D chartArea = new Rectangle2D.Double();
	
	/**
	 * Paints the component by drawing the chart to fill the entire component,
	 * but allowing for the insets (which will be non-zero if a border has been
	 * set for this component). To increase performance (at the expense of
	 * memory), an off-screen buffer image can be used.
	 * 
	 * @param g
	 *           the graphics device for drawing on.
	 */
	@Override
	public void paintComponent(Graphics g) {
		
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		
		// first determine the size of the chart rendering area...
		Dimension size = getSize();
		Insets insets = getInsets();
		this.available.setRect(
							insets.left, insets.top,
							size.getWidth() - insets.left - insets.right,
							size.getHeight() - insets.top - insets.bottom
							);
		
		// work out if scaling is required...
		boolean scale = false;
		double drawWidth = this.available.getWidth();
		double drawHeight = this.available.getHeight();
		this.scaleX = 1.0;
		this.scaleY = 1.0;
		
		if (drawWidth < this.minimumDrawWidth) {
			this.scaleX = drawWidth / this.minimumDrawWidth;
			drawWidth = this.minimumDrawWidth;
			scale = true;
		} else
			if (drawWidth > this.maximumDrawWidth) {
				this.scaleX = drawWidth / this.maximumDrawWidth;
				drawWidth = this.maximumDrawWidth;
				scale = true;
			}
		
		if (drawHeight < this.minimumDrawHeight) {
			this.scaleY = drawHeight / this.minimumDrawHeight;
			drawHeight = this.minimumDrawHeight;
			scale = true;
		} else
			if (drawHeight > this.maximumDrawHeight) {
				this.scaleY = drawHeight / this.maximumDrawHeight;
				drawHeight = this.maximumDrawHeight;
				scale = true;
			}
		if (!chart.getPlot().isPlotShowing()) {
			scaleX = Math.min(scaleX, scaleY);
			scaleY = scaleX;
		}
		this.chartArea.setRect(0.0, 0.0, drawWidth, drawHeight);
		
		// are we using the chart buffer?
		if (this.useBuffer) {
			
			// do we need to resize the buffer?
			if ((this.chartBuffer == null) || (this.chartBufferWidth != this.available.getWidth())
													|| (this.chartBufferHeight != this.available.getHeight())) {
				
				this.chartBufferWidth = (int) this.available.getWidth();
				this.chartBufferHeight = (int) this.available.getHeight();
				this.chartBuffer = createImage(this.chartBufferWidth, this.chartBufferHeight);
				this.refreshBuffer = true;
				
			}
			
			// do we need to redraw the buffer?
			if (this.refreshBuffer) {
				
				Rectangle2D bufferArea =
									new Rectangle2D.Double(0, 0, this.chartBufferWidth, this.chartBufferHeight);
				
				Graphics2D bufferG2 = (Graphics2D) this.chartBuffer.getGraphics();
				if (scale) {
					AffineTransform saved = bufferG2.getTransform();
					AffineTransform st = AffineTransform.getScaleInstance(
										this.scaleX, this.scaleY
										);
					bufferG2.transform(st);
					this.chart.draw(bufferG2, this.chartArea, this.anchor, this.info);
					bufferG2.setTransform(saved);
				} else {
					this.chart.draw(bufferG2, bufferArea, this.anchor, this.info);
				}
				
				this.refreshBuffer = false;
				
			}
			
			// zap the buffer onto the panel...
			g2.drawImage(this.chartBuffer, insets.left, insets.right, this);
			
		}

		// or redrawing the chart every time...
		else {
			
			AffineTransform saved = g2.getTransform();
			g2.translate(insets.left, insets.top);
			if (scale) {
				AffineTransform st = AffineTransform.getScaleInstance(this.scaleX, this.scaleY);
				g2.transform(st);
			}
			try {
				this.chart.draw(g2, this.chartArea, this.anchor, this.info);
			} catch (Exception err) {
				// ignore
			}
			g2.setTransform(saved);
			
		}
		
		this.anchor = null;
		this.verticalTraceLine = null;
		this.horizontalTraceLine = null;
		
	}
	
	/**
	 * Receives notification of changes to the chart, and redraws the chart.
	 * 
	 * @param event
	 *           details of the chart change event.
	 */
	public void chartChanged(ChartChangeEvent event) {
		this.refreshBuffer = true;
		repaint();
		
	}
	
	/**
	 * Receives notification of a chart progress event.
	 * 
	 * @param event
	 *           the event.
	 */
	public void chartProgress(ChartProgressEvent event) {
		// does nothing - override if necessary
	}
	
	/**
	 * Handles action events generated by the popup menu.
	 * 
	 * @param event
	 *           the event.
	 */
	public void actionPerformed(ActionEvent event) {
		
		String command = event.getActionCommand();
		
		if (command.equals(PROPERTIES_ACTION_COMMAND)) {
			attemptEditChartProperties();
		} else
			if (command.equals(SAVE_ACTION_COMMAND)) {
				try {
					doSaveAs();
				} catch (IOException e) {
					System.err.println("ChartPanel.doSaveAs: i/o exception = " + e.getMessage());
				}
			} else
				if (command.equals(PRINT_ACTION_COMMAND)) {
					createChartPrintJob();
				} else
					if (command.equals(ZOOM_IN_BOTH_ACTION_COMMAND)) {
						zoomInBoth(this.zoomPoint.getX(), this.zoomPoint.getY());
					} else
						if (command.equals(ZOOM_IN_HORIZONTAL_ACTION_COMMAND)) {
							zoomInHorizontal(this.zoomPoint.getX());
						} else
							if (command.equals(ZOOM_IN_VERTICAL_ACTION_COMMAND)) {
								zoomInVertical(this.zoomPoint.getY());
							} else
								if (command.equals(ZOOM_OUT_BOTH_ACTION_COMMAND)) {
									zoomOutBoth(this.zoomPoint.getX(), this.zoomPoint.getY());
								} else
									if (command.equals(ZOOM_OUT_HORIZONTAL_ACTION_COMMAND)) {
										zoomOutHorizontal(this.zoomPoint.getX());
									} else
										if (command.equals(ZOOM_OUT_VERTICAL_ACTION_COMMAND)) {
											zoomOutVertical(this.zoomPoint.getY());
										} else
											if (command.equals(AUTO_RANGE_BOTH_ACTION_COMMAND)) {
												autoRangeBoth();
											} else
												if (command.equals(AUTO_RANGE_HORIZONTAL_ACTION_COMMAND)) {
													autoRangeHorizontal();
												} else
													if (command.equals(AUTO_RANGE_VERTICAL_ACTION_COMMAND)) {
														autoRangeVertical();
													}
		
	}
	
	/**
	 * Handles a 'mouse entered' event. This method changes the tooltip delays of
	 * ToolTipManager.sharedInstance() to the possibly
	 * different values set for this chart panel.
	 * 
	 * @param e
	 *           the mouse event.
	 */
	public void mouseEntered(MouseEvent e) {
		if (!this.ownToolTipDelaysActive) {
			ToolTipManager ttm = ToolTipManager.sharedInstance();
			
			this.originalToolTipInitialDelay = ttm.getInitialDelay();
			ttm.setInitialDelay(this.ownToolTipInitialDelay);
			
			this.originalToolTipReshowDelay = ttm.getReshowDelay();
			ttm.setReshowDelay(this.ownToolTipReshowDelay);
			
			this.originalToolTipDismissDelay = ttm.getDismissDelay();
			ttm.setDismissDelay(this.ownToolTipDismissDelay);
			
			this.ownToolTipDelaysActive = true;
		}
	}
	
	/**
	 * Handles a 'mouse exited' event. This method resets the tooltip delays of
	 * ToolTipManager.sharedInstance() to their
	 * original values in effect before mouseEntered()
	 * 
	 * @param e
	 *           the mouse event.
	 */
	public void mouseExited(MouseEvent e) {
		if (this.ownToolTipDelaysActive) {
			// restore original tooltip dealys
			ToolTipManager ttm = ToolTipManager.sharedInstance();
			ttm.setInitialDelay(this.originalToolTipInitialDelay);
			ttm.setReshowDelay(this.originalToolTipReshowDelay);
			ttm.setDismissDelay(this.originalToolTipDismissDelay);
			this.ownToolTipDelaysActive = false;
		}
	}
	
	/**
	 * Handles a 'mouse pressed' event.
	 * <P>
	 * This event is the popup trigger on Unix/Linux. For Windows, the popup trigger is the 'mouse released' event.
	 * 
	 * @param e
	 *           The mouse event.
	 */
	public void mousePressed(MouseEvent e) {
		// System.err.println("MOUSE DOWN: "+e.toString());
		
		if (this.zoomRectangle == null) {
			
			this.zoomPoint = RefineryUtilities.getPointInRectangle(
								e.getX(), e.getY(), getScaledDataArea()
								);
			LOGGER.debug("In mousePressed()");
			LOGGER.debug("getScaledDataArea() = " + getScaledDataArea());
			LOGGER.debug("this.zoomPoint = " + this.zoomPoint);
		}
		maybeShowPopup(e);
	}
	
	private void maybeShowPopup(MouseEvent e) {
		JPopupMenu pm = getPopupMenu();
		if (e.isPopupTrigger() && pm != null) {
			pm.show(e.getComponent(),
								e.getX(), e.getY());
		}
	}
	
	/**
	 * Handles a 'mouse released' event.
	 * <P>
	 * On Windows, we need to check if this is a popup trigger, but only if we haven't already been tracking a zoom rectangle.
	 * 
	 * @param e
	 *           Information about the event.
	 */
	public void mouseReleased(MouseEvent e) {
		// System.err.println("MOUSE UP: "+e.toString());
		
		LOGGER.debug("In mouseReleased()");
		LOGGER.debug("this.zoomRectangle = " + this.zoomRectangle);
		if (this.zoomRectangle != null) {
			
			boolean zoomTrigger1 = this.horizontalZoom
								&& Math.abs(e.getX() - this.zoomPoint.getX()) >= this.zoomTriggerDistance;
			boolean zoomTrigger2 = this.verticalZoom
								&& Math.abs(e.getY() - this.zoomPoint.getY()) >= this.zoomTriggerDistance;
			if (zoomTrigger1 || zoomTrigger2) {
				if ((this.horizontalZoom && (e.getX() < this.zoomPoint.getX()))
									|| (this.verticalZoom && (e.getY() < this.zoomPoint.getY()))) {
					autoRangeBoth();
				} else {
					double x, y, w, h;
					Rectangle2D scaledDataArea = getScaledDataArea();
					// for a mouseReleased event, (horizontalZoom || verticalZoom)
					// will be true, so we can just test for either being false;
					// otherwise both are true
					if (!this.verticalZoom) {
						x = this.zoomPoint.getX();
						y = scaledDataArea.getMinY();
						w = Math.min(
											this.zoomRectangle.getWidth(),
											scaledDataArea.getMaxX() - this.zoomPoint.getX()
											);
						h = scaledDataArea.getHeight();
					} else
						if (!this.horizontalZoom) {
							x = scaledDataArea.getMinX();
							y = this.zoomPoint.getY();
							w = scaledDataArea.getWidth();
							h = Math.min(
												this.zoomRectangle.getHeight(),
												scaledDataArea.getMaxY() - this.zoomPoint.getY()
												);
						} else {
							x = this.zoomPoint.getX();
							y = this.zoomPoint.getY();
							w = Math.min(
												this.zoomRectangle.getWidth(),
												scaledDataArea.getMaxX() - this.zoomPoint.getX()
												);
							h = Math.min(
												this.zoomRectangle.getHeight(),
												scaledDataArea.getMaxY() - this.zoomPoint.getY()
												);
						}
					Rectangle2D zoomArea = new Rectangle2D.Double(x, y, w, h);
					Log.debug("zoomArea = " + zoomArea);
					zoom(zoomArea);
				}
				this.zoomPoint = null;
				this.zoomRectangle = null;
			} else {
				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.setXORMode(java.awt.Color.gray);
				if (this.fillZoomRectangle) {
					g2.fill(this.zoomRectangle);
				} else {
					g2.draw(this.zoomRectangle);
				}
				g2.dispose();
				this.zoomRectangle = null;
			}
			maybeShowPopup(e);
		}
	}
	
	/**
	 * Receives notification of mouse clicks on the panel. These are
	 * translated and passed on to any registered chart mouse click listeners.
	 * 
	 * @param event
	 *           Information about the mouse event.
	 */
	public void mouseClicked(MouseEvent event) {
		Insets insets = getInsets();
		int x = (int) ((event.getX() - insets.left) / this.scaleX);
		int y = (int) ((event.getY() - insets.top) / this.scaleY);
		
		// old 'handle click' code...
		// chart.handleClick(x, y, this.info);
		this.anchor = new Point2D.Double(x, y);
		this.chart.setTitle(this.chart.getTitle()); // force a redraw
		// new entity code...
		if (this.chartMouseListeners.isEmpty()) {
			return;
		}
		
		ChartEntity entity = null;
		if (this.info != null) {
			EntityCollection entities = this.info.getEntityCollection();
			if (entities != null) {
				entity = entities.getEntity(x, y);
			}
		}
		
		ChartMouseEvent chartEvent = new ChartMouseEvent(getChart(), event, entity);
		
		Iterator iterator = this.chartMouseListeners.iterator();
		while (iterator.hasNext()) {
			ChartMouseListener listener = (ChartMouseListener) iterator.next();
			listener.chartMouseClicked(chartEvent);
		}
	}
	
	/**
	 * Implementation of the MouseMotionListener's method
	 * 
	 * @param e
	 *           the event.
	 */
	public void mouseMoved(MouseEvent e) {
		
		if (this.horizontalAxisTrace) {
			drawHorizontalAxisTrace(e.getX());
		}
		
		if (this.verticalAxisTrace) {
			drawVerticalAxisTrace(e.getY());
		}
		
		if (this.chartMouseListeners.isEmpty()) {
			return;
		}
		
		Insets insets = getInsets();
		int x = (int) ((e.getX() - insets.left) / this.scaleX);
		int y = (int) ((e.getY() - insets.top) / this.scaleY);
		
		ChartEntity entity = null;
		if (this.info != null) {
			EntityCollection entities = this.info.getEntityCollection();
			if (entities != null) {
				entity = entities.getEntity(x, y);
			}
		}
		ChartMouseEvent event = new ChartMouseEvent(getChart(), e, entity);
		
		Iterator iterator = this.chartMouseListeners.iterator();
		while (iterator.hasNext()) {
			ChartMouseListener listener = (ChartMouseListener) iterator.next();
			listener.chartMouseMoved(event);
		}
	}
	
	/**
	 * Handles a 'mouse dragged' event.
	 * 
	 * @param e
	 *           the mouse event.
	 */
	public void mouseDragged(MouseEvent e) {
		// if the popup menu has already been triggered, then ignore dragging...
		if (this.popup != null && this.popup.isShowing()) {
			return;
		}
		
		Graphics2D g2 = (Graphics2D) getGraphics();
		
		// use XOR to erase the previous zoom rectangle (if any)...
		g2.setXORMode(java.awt.Color.gray);
		if (this.zoomRectangle != null) {
			if (this.fillZoomRectangle) {
				g2.fill(this.zoomRectangle);
			} else {
				g2.draw(this.zoomRectangle);
			}
		}
		
		Rectangle2D scaledDataArea = getScaledDataArea();
		if (this.horizontalZoom && this.verticalZoom) {
			// selected rectangle shouldn't extend outside the data area...
			double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
			double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
			this.zoomRectangle = new Rectangle2D.Double(
								this.zoomPoint.getX(), this.zoomPoint.getY(),
								xmax - this.zoomPoint.getX(), ymax - this.zoomPoint.getY()
								);
		} else
			if (this.horizontalZoom) {
				double xmax = Math.min(e.getX(), scaledDataArea.getMaxX());
				this.zoomRectangle = new Rectangle2D.Double(
									this.zoomPoint.getX(), scaledDataArea.getMinY(),
									xmax - this.zoomPoint.getX(), scaledDataArea.getHeight()
									);
			} else
				if (this.verticalZoom) {
					double ymax = Math.min(e.getY(), scaledDataArea.getMaxY());
					this.zoomRectangle = new Rectangle2D.Double(
										scaledDataArea.getMinX(), this.zoomPoint.getY(),
										scaledDataArea.getWidth(), ymax - this.zoomPoint.getY()
										);
				}
		
		if (this.zoomRectangle != null) {
			// use XOR to draw the new zoom rectangle...
			if (this.fillZoomRectangle) {
				g2.fill(this.zoomRectangle);
			} else {
				g2.draw(this.zoomRectangle);
			}
		}
		g2.dispose();
	}
	
	/**
	 * Zooms in on an anchor point (measured in Java2D coordinates).
	 * 
	 * @param x
	 *           The x value.
	 * @param y
	 *           The y value.
	 */
	public void zoomInBoth(double x, double y) {
		
		zoomInHorizontal(x);
		zoomInVertical(y);
		
	}
	
	/**
	 * Returns a reference to the 'horizontal' value axis, if there is one.
	 * 
	 * @param plot
	 *           the plot.
	 * @return The axis.
	 */
	private ValueAxis getHorizontalValueAxis(Plot plot) {
		
		if (plot == null) {
			return null;
		}
		
		ValueAxis axis = null;
		
		if (plot instanceof CategoryPlot) {
			CategoryPlot cp = (CategoryPlot) plot;
			if (cp.getOrientation() == PlotOrientation.HORIZONTAL) {
				axis = cp.getRangeAxis();
			}
		}
		
		if (plot instanceof XYPlot) {
			XYPlot xyp = (XYPlot) plot;
			if (xyp.getOrientation() == PlotOrientation.HORIZONTAL) {
				axis = xyp.getRangeAxis();
			} else
				if (xyp.getOrientation() == PlotOrientation.VERTICAL) {
					axis = xyp.getDomainAxis();
				}
		}
		
		if (plot instanceof FastScatterPlot) {
			FastScatterPlot fsp = (FastScatterPlot) plot;
			axis = fsp.getDomainAxis();
		}
		
		return axis;
		
	}
	
	/**
	 * Returns a reference to the 'vertical' value axis, if there is one.
	 * 
	 * @param plot
	 *           the plot.
	 * @return The axis.
	 */
	private ValueAxis getVerticalValueAxis(Plot plot) {
		
		if (plot == null) {
			return null;
		}
		
		ValueAxis axis = null;
		
		if (plot instanceof CategoryPlot) {
			CategoryPlot cp = (CategoryPlot) plot;
			if (cp.getOrientation() == PlotOrientation.VERTICAL) {
				axis = cp.getRangeAxis();
			}
		}
		
		if (plot instanceof XYPlot) {
			XYPlot xyp = (XYPlot) plot;
			if (xyp.getOrientation() == PlotOrientation.HORIZONTAL) {
				axis = xyp.getDomainAxis();
			} else
				if (xyp.getOrientation() == PlotOrientation.VERTICAL) {
					axis = xyp.getRangeAxis();
				}
		}
		
		if (plot instanceof FastScatterPlot) {
			FastScatterPlot fsp = (FastScatterPlot) plot;
			axis = fsp.getRangeAxis();
		}
		
		return axis;
		
	}
	
	/**
	 * Decreases the range on the horizontal axis, centered about a Java2D
	 * x coordinate.
	 * <P>
	 * The range on the x axis is halved.
	 * 
	 * @param x
	 *           The x coordinate in Java2D space.
	 */
	public void zoomInHorizontal(double x) {
		Plot p = this.chart.getPlot();
		if (p instanceof ValueAxisPlot) {
			ValueAxisPlot plot = (ValueAxisPlot) p;
			plot.zoomHorizontalAxes(this.zoomInFactor);
		}
	}
	
	/**
	 * Decreases the range on the vertical axis, centered about a Java2D
	 * y coordinate.
	 * <P>
	 * The range on the y axis is halved.
	 * 
	 * @param y
	 *           The y coordinate in Java2D space.
	 */
	public void zoomInVertical(double y) {
		Plot p = this.chart.getPlot();
		if (p instanceof ValueAxisPlot) {
			ValueAxisPlot plot = (ValueAxisPlot) p;
			plot.zoomVerticalAxes(this.zoomInFactor);
		}
	}
	
	/**
	 * Zooms out on an anchor point (measured in Java2D coordinates).
	 * 
	 * @param x
	 *           The x value.
	 * @param y
	 *           The y value.
	 */
	public void zoomOutBoth(double x, double y) {
		zoomOutHorizontal(x);
		zoomOutVertical(y);
	}
	
	/**
	 * Increases the range on the horizontal axis, centered about a Java2D
	 * x coordinate.
	 * <P>
	 * The range on the x axis is doubled.
	 * 
	 * @param x
	 *           The x coordinate in Java2D space.
	 */
	public void zoomOutHorizontal(double x) {
		Plot p = this.chart.getPlot();
		if (p instanceof ValueAxisPlot) {
			ValueAxisPlot plot = (ValueAxisPlot) p;
			plot.zoomHorizontalAxes(this.zoomOutFactor);
		}
	}
	
	/**
	 * Increases the range on the vertical axis, centered about a Java2D y coordinate.
	 * <P>
	 * The range on the y axis is doubled.
	 * 
	 * @param y
	 *           the y coordinate in Java2D space.
	 */
	public void zoomOutVertical(double y) {
		Plot p = this.chart.getPlot();
		if (p instanceof ValueAxisPlot) {
			ValueAxisPlot plot = (ValueAxisPlot) p;
			plot.zoomVerticalAxes(this.zoomOutFactor);
		}
	}
	
	/**
	 * Zooms in on a selected region.
	 * 
	 * @param selection
	 *           the selected region.
	 */
	public void zoom(Rectangle2D selection) {
		
		double hLower = 0.0;
		double hUpper = 0.0;
		double vLower = 0.0;
		double vUpper = 0.0;
		
		if ((selection.getHeight() > 0) && (selection.getWidth() > 0)) {
			
			Rectangle2D scaledDataArea = getScaledDataArea();
			hLower = (selection.getMinX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
			hUpper = (selection.getMaxX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
			vLower = (scaledDataArea.getMaxY() - selection.getMaxY()) / scaledDataArea.getHeight();
			vUpper = (scaledDataArea.getMaxY() - selection.getMinY()) / scaledDataArea.getHeight();
			
			LOGGER.debug("hLower = " + hLower);
			LOGGER.debug("hUpper = " + hUpper);
			LOGGER.debug("vLower = " + vLower);
			LOGGER.debug("vUpper = " + vUpper);
			Plot p = this.chart.getPlot();
			if (p instanceof ValueAxisPlot) {
				ValueAxisPlot plot = (ValueAxisPlot) p;
				plot.zoomHorizontalAxes(hLower, hUpper);
				plot.zoomVerticalAxes(vLower, vUpper);
			}
			
		}
		
	}
	
	/**
	 * Restores the auto-range calculation on both axes.
	 */
	public void autoRangeBoth() {
		autoRangeHorizontal();
		autoRangeVertical();
	}
	
	/**
	 * Restores the auto-range calculation on the horizontal axis.
	 */
	public void autoRangeHorizontal() {
		Plot p = this.chart.getPlot();
		if (p instanceof ValueAxisPlot) {
			ValueAxisPlot plot = (ValueAxisPlot) p;
			plot.zoomHorizontalAxes(0.0);
		}
	}
	
	/**
	 * Restores the auto-range calculation on the vertical axis.
	 */
	public void autoRangeVertical() {
		Plot p = this.chart.getPlot();
		if (p instanceof ValueAxisPlot) {
			ValueAxisPlot plot = (ValueAxisPlot) p;
			plot.zoomVerticalAxes(0.0);
		}
	}
	
	/**
	 * Returns the data area for the chart (the area inside the axes) with the
	 * current scaling applied.
	 * 
	 * @return The scaled data area.
	 */
	public Rectangle2D getScaledDataArea() {
		Rectangle2D dataArea = this.info.getPlotInfo().getDataArea();
		Insets insets = getInsets();
		double x = dataArea.getX() * this.scaleX + insets.left;
		double y = dataArea.getY() * this.scaleY + insets.top;
		double w = dataArea.getWidth() * this.scaleX;
		double h = dataArea.getHeight() * this.scaleY;
		return new Rectangle2D.Double(x, y, w, h);
	}
	
	/**
	 * Returns the initial tooltip delay value used inside this chart panel.
	 * 
	 * @return an integer representing the initial delay value, in milliseconds.
	 * @see javax.swing.ToolTipManager#getInitialDelay()
	 */
	public int getInitialDelay() {
		return this.ownToolTipInitialDelay;
	}
	
	/**
	 * Returns the reshow tooltip delay value used inside this chart panel.
	 * 
	 * @return an integer representing the reshow delay value, in milliseconds.
	 * @see javax.swing.ToolTipManager#getReshowDelay()
	 */
	public int getReshowDelay() {
		return this.ownToolTipReshowDelay;
	}
	
	/**
	 * Returns the dismissal tooltip delay value used inside this chart panel.
	 * 
	 * @return an integer representing the dismissal delay value, in milliseconds.
	 * @see javax.swing.ToolTipManager#getDismissDelay()
	 */
	public int getDismissDelay() {
		return this.ownToolTipDismissDelay;
	}
	
	/**
	 * Specifies the initial delay value for this chart panel.
	 * 
	 * @param delay
	 *           the number of milliseconds to delay (after the cursor has paused) before
	 *           displaying.
	 * @see javax.swing.ToolTipManager#setInitialDelay(int)
	 */
	public void setInitialDelay(int delay) {
		this.ownToolTipInitialDelay = delay;
	}
	
	/**
	 * Specifies the amount of time before the user has to wait initialDelay milliseconds before a
	 * tooltip will be shown.
	 * 
	 * @param delay
	 *           time in milliseconds
	 * @see javax.swing.ToolTipManager#setReshowDelay(int)
	 */
	public void setReshowDelay(int delay) {
		this.ownToolTipReshowDelay = delay;
	}
	
	/**
	 * Specifies the dismissal delay value for this chart panel.
	 * 
	 * @param delay
	 *           the number of milliseconds to delay before taking away the tooltip
	 * @see javax.swing.ToolTipManager#setDismissDelay(int)
	 */
	public void setDismissDelay(int delay) {
		this.ownToolTipDismissDelay = delay;
	}
	
	/**
	 * Returns the zoom in factor.
	 * 
	 * @return The zoom in factor.
	 */
	public double getZoomInFactor() {
		return this.zoomInFactor;
	}
	
	/**
	 * Sets the zoom in factor.
	 * 
	 * @param factor
	 *           the factor.
	 */
	public void setZoomInFactor(double factor) {
		this.zoomInFactor = factor;
	}
	
	/**
	 * Returns the zoom out factor.
	 * 
	 * @return The zoom out factor.
	 */
	public double getZoomOutFactor() {
		return this.zoomOutFactor;
	}
	
	/**
	 * Sets the zoom out factor.
	 * 
	 * @param factor
	 *           the factor.
	 */
	public void setZoomOutFactor(double factor) {
		this.zoomOutFactor = factor;
	}
	
	/**
	 * Draws a vertical line used to trace the mouse position to the horizontal axis.
	 * 
	 * @param x
	 *           the x-coordinate of the trace line.
	 */
	private void drawHorizontalAxisTrace(int x) {
		
		Graphics2D g2 = (Graphics2D) getGraphics();
		Rectangle2D dataArea = getScaledDataArea();
		
		g2.setXORMode(java.awt.Color.orange);
		if (((int) dataArea.getMinX() < x) && (x < (int) dataArea.getMaxX())) {
			
			if (this.verticalTraceLine != null) {
				g2.draw(this.verticalTraceLine);
				this.verticalTraceLine.setLine(
									x, (int) dataArea.getMinY(), x, (int) dataArea.getMaxY()
									);
			} else {
				this.verticalTraceLine = new Line2D.Float(
									x, (int) dataArea.getMinY(), x, (int) dataArea.getMaxY()
									);
			}
			g2.draw(this.verticalTraceLine);
		}
		
	}
	
	/**
	 * Draws a horizontal line used to trace the mouse position to the vertical axis.
	 * 
	 * @param y
	 *           the y-coordinate of the trace line.
	 */
	private void drawVerticalAxisTrace(int y) {
		
		Graphics2D g2 = (Graphics2D) getGraphics();
		Rectangle2D dataArea = getScaledDataArea();
		
		g2.setXORMode(java.awt.Color.orange);
		if (((int) dataArea.getMinY() < y) && (y < (int) dataArea.getMaxY())) {
			
			if (this.horizontalTraceLine != null) {
				g2.draw(this.horizontalTraceLine);
				this.horizontalTraceLine.setLine(
									(int) dataArea.getMinX(), y, (int) dataArea.getMaxX(), y
									);
			} else {
				this.horizontalTraceLine = new Line2D.Float(
									(int) dataArea.getMinX(), y, (int) dataArea.getMaxX(), y
									);
			}
			g2.draw(this.horizontalTraceLine);
		}
		
	}
	
	/**
	 * Displays a dialog that allows the user to edit the properties for the
	 * current chart.
	 */
	private void attemptEditChartProperties() {
		
		ChartPropertyEditPanel panel = new ChartPropertyEditPanel(this.chart);
		int result =
							JOptionPane.showConfirmDialog(this, panel,
														localizationResources.getString("Chart_Properties"),
														JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			panel.updateChartProperties(this.chart);
		}
		
	}
	
	/**
	 * Opens a file chooser and gives the user an opportunity to save the chart
	 * in PNG format.
	 * 
	 * @throws IOException
	 *            if there is an I/O error.
	 */
	public void doSaveAs() throws IOException {
		
		JFileChooser fileChooser = new JFileChooser();
		ExtensionFileFilter filter =
							new ExtensionFileFilter(localizationResources.getString("PNG_Image_Files"), ".png");
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter("All files", ""));
		
		int option = fileChooser.showSaveDialog(this);
		if (option == JFileChooser.APPROVE_OPTION) {
			String filename = fileChooser.getSelectedFile().getPath();
			if (isEnforceFileExtensions()) {
				if (!filename.endsWith(".png")) {
					filename = filename + ".png";
				}
			}
			ChartUtilities.saveChartAsPNG(new File(filename), this.chart, getWidth(), getHeight());
		}
		
	}
	
	/**
	 * Creates a print job for the chart.
	 */
	public void createChartPrintJob() {
		
		PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat pf = job.defaultPage();
		PageFormat pf2 = job.pageDialog(pf);
		if (pf2 != pf) {
			job.setPrintable(this, pf2);
			if (job.printDialog()) {
				try {
					job.print();
				} catch (PrinterException e) {
					JOptionPane.showMessageDialog(this, e);
				}
			}
		}
		
	}
	
	/**
	 * Prints the chart on a single page.
	 * 
	 * @param g
	 *           the graphics context.
	 * @param pf
	 *           the page format to use.
	 * @param pageIndex
	 *           the index of the page. If not <code>0</code>, nothing gets print.
	 * @return the result of printing.
	 */
	public int print(Graphics g, PageFormat pf, int pageIndex) {
		
		if (pageIndex != 0) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D) g;
		double x = pf.getImageableX();
		double y = pf.getImageableY();
		double w = pf.getImageableWidth();
		double h = pf.getImageableHeight();
		this.chart.draw(g2, new Rectangle2D.Double(x, y, w, h), this.anchor, null);
		return PAGE_EXISTS;
		
	}
	
	/**
	 * Adds a listener to the list of objects listening for chart mouse events.
	 * 
	 * @param listener
	 *           the listener.
	 */
	public void addChartMouseListener(ChartMouseListener listener) {
		this.chartMouseListeners.add(listener);
	}
	
	/**
	 * Removes a listener from the list of objects listening for chart mouse events.
	 * 
	 * @param listener
	 *           the listener.
	 */
	public void removeChartMouseListener(ChartMouseListener listener) {
		this.chartMouseListeners.remove(listener);
	}
	
	/**
	 * Creates a popup menu for the panel.
	 * 
	 * @param properties
	 *           include a menu item for the chart property editor.
	 * @param save
	 *           include a menu item for saving the chart.
	 * @param print
	 *           include a menu item for printing the chart.
	 * @param zoom
	 *           include menu items for zooming.
	 * @return The popup menu.
	 */
	protected JPopupMenu createPopupMenu(boolean properties, boolean save, boolean print,
														boolean zoom) {
		
		JPopupMenu result = new JPopupMenu("Chart:");
		boolean separator = false;
		
		if (properties) {
			JMenuItem propertiesItem = new JMenuItem(localizationResources.getString("Properties..."));
			propertiesItem.setActionCommand(PROPERTIES_ACTION_COMMAND);
			propertiesItem.addActionListener(this);
			result.add(propertiesItem);
			separator = true;
		}
		
		if (save) {
			if (separator) {
				result.addSeparator();
				separator = false;
			}
			JMenuItem saveItem = new JMenuItem(localizationResources.getString("Save_as..."));
			saveItem.setActionCommand(SAVE_ACTION_COMMAND);
			saveItem.addActionListener(this);
			result.add(saveItem);
			separator = true;
		}
		
		if (print) {
			if (separator) {
				result.addSeparator();
				separator = false;
			}
			JMenuItem printItem = new JMenuItem(localizationResources.getString("Print..."));
			printItem.setActionCommand(PRINT_ACTION_COMMAND);
			printItem.addActionListener(this);
			result.add(printItem);
			separator = true;
		}
		
		if (zoom) {
			if (separator) {
				result.addSeparator();
				separator = false;
			}
			
			JMenu zoomInMenu = new JMenu(localizationResources.getString("Zoom_In"));
			
			this.zoomInBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
			this.zoomInBothMenuItem.setActionCommand(ZOOM_IN_BOTH_ACTION_COMMAND);
			this.zoomInBothMenuItem.addActionListener(this);
			zoomInMenu.add(this.zoomInBothMenuItem);
			
			zoomInMenu.addSeparator();
			
			this.zoomInHorizontalMenuItem =
								new JMenuItem(localizationResources.getString("Horizontal_Axis"));
			this.zoomInHorizontalMenuItem.setActionCommand(ZOOM_IN_HORIZONTAL_ACTION_COMMAND);
			this.zoomInHorizontalMenuItem.addActionListener(this);
			zoomInMenu.add(this.zoomInHorizontalMenuItem);
			
			this.zoomInVerticalMenuItem =
								new JMenuItem(localizationResources.getString("Vertical_Axis"));
			this.zoomInVerticalMenuItem.setActionCommand(ZOOM_IN_VERTICAL_ACTION_COMMAND);
			this.zoomInVerticalMenuItem.addActionListener(this);
			zoomInMenu.add(this.zoomInVerticalMenuItem);
			
			result.add(zoomInMenu);
			
			JMenu zoomOutMenu = new JMenu(localizationResources.getString("Zoom_Out"));
			
			this.zoomOutBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
			this.zoomOutBothMenuItem.setActionCommand(ZOOM_OUT_BOTH_ACTION_COMMAND);
			this.zoomOutBothMenuItem.addActionListener(this);
			zoomOutMenu.add(this.zoomOutBothMenuItem);
			
			zoomOutMenu.addSeparator();
			
			this.zoomOutHorizontalMenuItem =
								new JMenuItem(localizationResources.getString("Horizontal_Axis"));
			this.zoomOutHorizontalMenuItem.setActionCommand(ZOOM_OUT_HORIZONTAL_ACTION_COMMAND);
			this.zoomOutHorizontalMenuItem.addActionListener(this);
			zoomOutMenu.add(this.zoomOutHorizontalMenuItem);
			
			this.zoomOutVerticalMenuItem =
								new JMenuItem(localizationResources.getString("Vertical_Axis"));
			this.zoomOutVerticalMenuItem.setActionCommand(ZOOM_OUT_VERTICAL_ACTION_COMMAND);
			this.zoomOutVerticalMenuItem.addActionListener(this);
			zoomOutMenu.add(this.zoomOutVerticalMenuItem);
			
			result.add(zoomOutMenu);
			
			JMenu autoRangeMenu = new JMenu(localizationResources.getString("Auto_Range"));
			
			this.autoRangeBothMenuItem = new JMenuItem(localizationResources.getString("All_Axes"));
			this.autoRangeBothMenuItem.setActionCommand(AUTO_RANGE_BOTH_ACTION_COMMAND);
			this.autoRangeBothMenuItem.addActionListener(this);
			autoRangeMenu.add(this.autoRangeBothMenuItem);
			
			autoRangeMenu.addSeparator();
			this.autoRangeHorizontalMenuItem =
								new JMenuItem(localizationResources.getString("Horizontal_Axis"));
			this.autoRangeHorizontalMenuItem.setActionCommand(AUTO_RANGE_HORIZONTAL_ACTION_COMMAND);
			this.autoRangeHorizontalMenuItem.addActionListener(this);
			autoRangeMenu.add(this.autoRangeHorizontalMenuItem);
			
			this.autoRangeVerticalMenuItem =
								new JMenuItem(localizationResources.getString("Vertical_Axis"));
			this.autoRangeVerticalMenuItem.setActionCommand(AUTO_RANGE_VERTICAL_ACTION_COMMAND);
			this.autoRangeVerticalMenuItem.addActionListener(this);
			autoRangeMenu.add(this.autoRangeVerticalMenuItem);
			
			result.addSeparator();
			result.add(autoRangeMenu);
			
		}
		
		return result;
		
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
		this.available = new Rectangle2D.Double();
		this.chartArea = new Rectangle2D.Double();
	}
}
