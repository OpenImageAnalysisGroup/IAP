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
 * ------------------------------
 * CombinedRangeCategoryPlot.java
 * ------------------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): Nicolas Brodu;
 * $Id: CombinedRangeCategoryPlot.java,v 1.1 2011-01-31 09:02:08 klukas Exp $
 * Changes:
 * --------
 * 16-May-2003 : Version 1 (DG);
 * 08-Aug-2003 : Adjusted totalWeight in remove(...) method (DG);
 * 19-Aug-2003 : Implemented Cloneable (DG);
 * 11-Sep-2003 : Fix cloning support (subplots) (NB);
 * 15-Sep-2003 : Implemented PublicCloneable. Fixed errors in cloning and serialization (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 17-Sep-2003 : Updated handling of 'clicks' (DG);
 * 04-May-2004 : Added getter/setter methods for 'gap' attributes (DG);
 */

package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A combined category plot where the range axis is shared.
 */
public class CombinedRangeCategoryPlot extends CategoryPlot
													implements Cloneable, PublicCloneable, Serializable,
																	PlotChangeListener {

	/** Storage for the subplot references. */
	private List subplots;

	/** Total weight of all charts. */
	private int totalWeight;

	/** The gap between subplots. */
	private double gap;

	/** Temporary storage for the subplot areas. */
	private transient Rectangle2D[] subplotArea;

	/**
	 * Creates a new plot.
	 * 
	 * @param rangeAxis
	 *           the shared range axis.
	 */
	public CombinedRangeCategoryPlot(ValueAxis rangeAxis) {

		super(null, null, rangeAxis, null);
		this.subplots = new java.util.ArrayList();
		this.totalWeight = 0;
		this.gap = 5.0;

	}

	/**
	 * Returns the space between subplots.
	 * 
	 * @return The gap (in Java2D units).
	 */
	public double getGap() {
		return this.gap;
	}

	/**
	 * Sets the amount of space between subplots and sends a {@link PlotChangeEvent} to all
	 * registered listeners.
	 * 
	 * @param gap
	 *           the gap between subplots (in Java2D units).
	 */
	public void setGap(double gap) {
		this.gap = gap;
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Adds a subplot.
	 * 
	 * @param subplot
	 *           the subplot.
	 * @param weight
	 *           the weight.
	 */
	public void add(CategoryPlot subplot, int weight) {
		// store the plot and its weight
		subplot.setParent(this);
		subplot.setWeight(weight);
		subplot.setInsets(new Insets(0, 0, 0, 0));
		subplot.setRangeAxis(null);
		subplot.setOrientation(getOrientation());
		subplot.addChangeListener(this);
		this.subplots.add(subplot);
		this.totalWeight += weight;

		// configure the range axis...
		ValueAxis axis = getRangeAxis();
		if (axis != null) {
			axis.configure();
		}
		notifyListeners(new PlotChangeEvent(this));
	}

	/**
	 * Removes a subplot from the combined chart.
	 * 
	 * @param subplot
	 *           the subplot.
	 */
	public void remove(CategoryPlot subplot) {

		this.subplots.remove(subplot);
		subplot.setParent(null);
		subplot.removeChangeListener(this);
		this.totalWeight -= subplot.getWeight();

		ValueAxis range = getRangeAxis();
		if (range != null) {
			range.configure();
		}

		ValueAxis range2 = getRangeAxis(1);
		if (range2 != null) {
			range2.configure();
		}

		notifyListeners(new PlotChangeEvent(this));

	}

	/**
	 * Returns the list of subplots.
	 * 
	 * @return the list of subplots.
	 */
	public List getSubplots() {
		return Collections.unmodifiableList(this.subplots);
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
		PlotOrientation orientation = getOrientation();

		// work out the space required by the domain axis...
		AxisSpace fixed = getFixedRangeAxisSpace();
		if (fixed != null) {
			if (orientation == PlotOrientation.VERTICAL) {
				space.setLeft(fixed.getLeft());
				space.setRight(fixed.getRight());
			} else
				if (orientation == PlotOrientation.HORIZONTAL) {
					space.setTop(fixed.getTop());
					space.setBottom(fixed.getBottom());
				}
		} else {
			ValueAxis valueAxis = getRangeAxis();
			RectangleEdge valueEdge = Plot.resolveRangeAxisLocation(
								getRangeAxisLocation(), orientation
								);
			if (valueAxis != null) {
				space = valueAxis.reserveSpace(g2, this, plotArea, valueEdge, space, true);
			}
		}

		Rectangle2D adjustedPlotArea = space.shrink(plotArea, null);
		// work out the maximum height or width of the non-shared axes...
		int n = this.subplots.size();

		// calculate plotAreas of all sub-plots, maximum vertical/horizontal axis width/height
		this.subplotArea = new Rectangle2D[n];
		double x = adjustedPlotArea.getX();
		double y = adjustedPlotArea.getY();
		double usableSize = 0.0;
		if (orientation == PlotOrientation.VERTICAL) {
			usableSize = adjustedPlotArea.getWidth() - this.gap * (n - 1);
		} else
			if (orientation == PlotOrientation.HORIZONTAL) {
				usableSize = adjustedPlotArea.getHeight() - this.gap * (n - 1);
			}

		for (int i = 0; i < n; i++) {
			CategoryPlot plot = (CategoryPlot) this.subplots.get(i);

			// calculate sub-plot area
			if (orientation == PlotOrientation.VERTICAL) {
				double w = usableSize * plot.getWeight() / this.totalWeight;
				this.subplotArea[i] = new Rectangle2D.Double(x, y, w, adjustedPlotArea.getHeight());
				x = x + w + this.gap;
			} else
				if (orientation == PlotOrientation.HORIZONTAL) {
					double h = usableSize * plot.getWeight() / this.totalWeight;
					this.subplotArea[i] = new Rectangle2D.Double(x, y, adjustedPlotArea.getWidth(), h);
					y = y + h + this.gap;
				}

			AxisSpace subSpace = plot.calculateDomainAxisSpace(g2, this.subplotArea[i], null, plot.getRangeAxis().isVisible());
			space.ensureAtLeast(subSpace);

		}

		return space;
	}

	/**
	 * Draws the plot on a Java 2D graphics device (such as the screen or a printer).
	 * Will perform all the placement calculations for each sub-plots and then tell these to draw
	 * themselves.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param plotArea
	 *           the area within which the plot (including axis labels) should be drawn.
	 * @param parentState
	 *           the parent state.
	 * @param info
	 *           collects information about the drawing (<code>null</code> permitted).
	 */
	public void draw(Graphics2D g2, Rectangle2D plotArea, PlotState parentState,
							PlotRenderingInfo info) {

		// set up info collection...
		if (info != null) {
			info.setPlotArea(plotArea);
		}

		// adjust the drawing area for plot insets (if any)...
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

		// set the width and height of non-shared axis of all sub-plots
		setFixedDomainAxisSpaceForSubplots(space);

		// draw the shared axis
		ValueAxis axis = getRangeAxis();
		RectangleEdge rangeEdge = getRangeAxisEdge();
		double cursor = RectangleEdge.coordinate(dataArea, rangeEdge);
		AxisState state = axis.draw(g2, cursor, plotArea, dataArea, rangeEdge, info);
		if (parentState == null) {
			parentState = new PlotState();
		}
		parentState.getSharedAxisStates().put(axis, state);

		// draw all the charts
		for (int i = 0; i < this.subplots.size(); i++) {
			CategoryPlot plot = (CategoryPlot) this.subplots.get(i);
			PlotRenderingInfo subplotInfo = null;
			if (info != null) {
				subplotInfo = new PlotRenderingInfo(info.getOwner());
				info.addSubplotInfo(subplotInfo);
			}
			plot.draw(g2, this.subplotArea[i], parentState, subplotInfo);
		}

		if (info != null) {
			info.setDataArea(dataArea);
		}

	}

	/**
	 * Sets the orientation for the plot (and all the subplots).
	 * 
	 * @param orientation
	 *           the orientation.
	 */
	public void setOrientation(PlotOrientation orientation) {

		super.setOrientation(orientation);

		Iterator iterator = this.subplots.iterator();
		while (iterator.hasNext()) {
			CategoryPlot plot = (CategoryPlot) iterator.next();
			plot.setOrientation(orientation);
		}

	}

	/**
	 * Returns the range for the axis. This is the combined range of all the subplots.
	 * 
	 * @param axis
	 *           the axis.
	 * @return the range.
	 */
	public Range getDataRange(ValueAxis axis) {

		Range result = null;
		if (this.subplots != null) {
			Iterator iterator = this.subplots.iterator();
			while (iterator.hasNext()) {
				CategoryPlot subplot = (CategoryPlot) iterator.next();
				result = Range.combine(result, subplot.getDataRange(axis));
			}
		}
		return result;

	}

	/**
	 * Returns a collection of legend items for the plot.
	 * 
	 * @return the legend items.
	 */
	public LegendItemCollection getLegendItems() {

		LegendItemCollection result = new LegendItemCollection();

		if (this.subplots != null) {
			Iterator iterator = this.subplots.iterator();
			while (iterator.hasNext()) {
				CategoryPlot plot = (CategoryPlot) iterator.next();
				LegendItemCollection more = plot.getLegendItems();
				result.addAll(more);
			}
		}

		return result;

	}

	/**
	 * Sets the size (width or height, depending on the orientation of the plot) for the domain
	 * axis of each subplot.
	 * 
	 * @param space
	 *           the space.
	 */
	protected void setFixedDomainAxisSpaceForSubplots(AxisSpace space) {

		Iterator iterator = this.subplots.iterator();
		while (iterator.hasNext()) {
			CategoryPlot plot = (CategoryPlot) iterator.next();
			plot.setFixedDomainAxisSpace(space);
		}

	}

	/**
	 * Handles a 'click' on the plot by updating the anchor value.
	 * 
	 * @param x
	 *           x-coordinate of the click.
	 * @param y
	 *           y-coordinate of the click.
	 * @param info
	 *           information about the plot's dimensions.
	 */
	public void handleClick(int x, int y, PlotRenderingInfo info) {

		Rectangle2D dataArea = info.getDataArea();
		if (dataArea.contains(x, y)) {
			for (int i = 0; i < this.subplots.size(); i++) {
				CategoryPlot subplot = (CategoryPlot) this.subplots.get(i);
				PlotRenderingInfo subplotInfo = info.getSubplotInfo(i);
				subplot.handleClick(x, y, subplotInfo);
			}
		}

	}

	/**
	 * Receives a {@link PlotChangeEvent} and responds by notifying all listeners.
	 * 
	 * @param event
	 *           the event.
	 */
	public void plotChanged(PlotChangeEvent event) {
		notifyListeners(event);
	}

	/**
	 * Tests the plot for equality with an arbitrary object.
	 * 
	 * @param object
	 *           the object to test against.
	 * @return <code>true</code> or <code>false</code>.
	 */
	public boolean equals(Object object) {

		if (object == null) {
			return false;
		}

		if (object == this) {
			return true;
		}

		if (object instanceof CombinedRangeCategoryPlot) {
			CombinedRangeCategoryPlot plot = (CombinedRangeCategoryPlot) object;
			if (super.equals(object)) {
				boolean b0 = ObjectUtils.equal(this.subplots, plot.subplots);
				boolean b1 = (this.totalWeight == plot.totalWeight);
				boolean b2 = (this.gap == plot.gap);

				return b0 && b1 && b2;

			}
		}

		return false;

	}

	/**
	 * Returns a clone of the plot.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            this class will not throw this exception, but subclasses
	 *            (if any) might.
	 */
	public Object clone() throws CloneNotSupportedException {
		CombinedRangeCategoryPlot result = (CombinedRangeCategoryPlot) super.clone();
		result.subplots = ObjectUtils.clone(this.subplots);
		for (Iterator it = result.subplots.iterator(); it.hasNext();) {
			Plot child = (Plot) it.next();
			child.setParent(result);
		}

		// after setting up all the subplots, the shared range axis may need reconfiguring
		ValueAxis rangeAxis = result.getRangeAxis();
		if (rangeAxis != null) {
			rangeAxis.configure();
		}

		return result;
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

		// the range axis is deserialized before the subplots, so its value range
		// is likely to be incorrect...
		ValueAxis rangeAxis = getRangeAxis();
		if (rangeAxis != null) {
			rangeAxis.configure();
		}

	}

}
