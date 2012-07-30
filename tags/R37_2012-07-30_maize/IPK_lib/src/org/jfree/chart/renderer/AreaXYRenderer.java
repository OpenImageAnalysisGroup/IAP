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
 * -------------------
 * AreaXYRenderer.java
 * -------------------
 * (C) Copyright 2002-2004, by Hari and Contributors.
 * Original Author: Hari (ourhari@hotmail.com);
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * Richard Atkinson;
 * Christian W. Zuckschwerdt;
 * $Id: AreaXYRenderer.java,v 1.1 2011-01-31 09:02:49 klukas Exp $
 * Changes:
 * --------
 * 03-Apr-2002 : Version 1, contributed by Hari. This class is based on the StandardXYItemRenderer
 * class (DG);
 * 09-Apr-2002 : Removed the translated zero from the drawItem method - overridden the initialise()
 * method to calculate it (DG);
 * 30-May-2002 : Added tool tip generator to constructor to match super class (DG);
 * 25-Jun-2002 : Removed unnecessary local variable (DG);
 * 05-Aug-2002 : Small modification to drawItem method to support URLs for HTML image maps (RA);
 * 01-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 07-Nov-2002 : Renamed AreaXYItemRenderer --> AreaXYRenderer (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem(...) method signature (DG);
 * 27-Jul-2003 : Made line and polygon properties protected rather than private (RA);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Added renderer state (DG);
 * 08-Dec-2003 : Modified hotspot for chart entity (DG);
 */

package org.jfree.chart.renderer;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * Area item renderer for an {@link XYPlot}. This class can draw (a) shapes at each
 * point, or (b) lines between points, or (c) both shapes and lines, or (d)
 * filled areas, or (e) filled areas and shapes.
 * 
 * @deprecated Use XYAreaRenderer.
 */
public class AreaXYRenderer extends AbstractXYItemRenderer
										implements XYItemRenderer,
													Cloneable,
													PublicCloneable,
													Serializable {

	/** Useful constant for specifying the type of rendering (shapes only). */
	public static final int SHAPES = 1;

	/** Useful constant for specifying the type of rendering (lines only). */
	public static final int LINES = 2;

	/** Useful constant for specifying the type of rendering (shapes and lines). */
	public static final int SHAPES_AND_LINES = 3;

	/** Useful constant for specifying the type of rendering (area only). */
	public static final int AREA = 4;

	/** Useful constant for specifying the type of rendering (area and shapes). */
	public static final int AREA_AND_SHAPES = 5;

	/** A flag indicating whether or not shapes are drawn at each XY point. */
	private boolean plotShapes;

	/** A flag indicating whether or not lines are drawn between XY points. */
	private boolean plotLines;

	/** A flag indicating whether or not Area are drawn at each XY point. */
	private boolean plotArea;

	/** A flag that controls whether or not the outline is shown. */
	private boolean showOutline;

	/**
	 * Constructs a new renderer.
	 */
	public AreaXYRenderer() {

		this(AREA);

	}

	/**
	 * Constructs a new renderer.
	 * 
	 * @param type
	 *           the type of the renderer.
	 */
	public AreaXYRenderer(int type) {
		this(type, null, null);
	}

	/**
	 * Constructs a new renderer.
	 * <p>
	 * To specify the type of renderer, use one of the constants: SHAPES, LINES, SHAPES_AND_LINES, AREA or AREA_AND_SHAPES.
	 * 
	 * @param type
	 *           the type of renderer.
	 * @param toolTipGenerator
	 *           the tool tip generator to use. <code>null</code> is none.
	 * @param urlGenerator
	 *           the URL generator (null permitted).
	 */
	public AreaXYRenderer(int type,
									XYToolTipGenerator toolTipGenerator, XYURLGenerator urlGenerator) {

		super();
		setToolTipGenerator(toolTipGenerator);
		setURLGenerator(urlGenerator);

		if (type == SHAPES) {
			this.plotShapes = true;
		}
		if (type == LINES) {
			this.plotLines = true;
		}
		if (type == SHAPES_AND_LINES) {
			this.plotShapes = true;
			this.plotLines = true;
		}
		if (type == AREA) {
			this.plotArea = true;
		}
		if (type == AREA_AND_SHAPES) {
			this.plotArea = true;
			this.plotShapes = true;
		}
		this.showOutline = false;

	}

	/**
	 * Returns a flag that controls whether or not outlines of the areas are drawn.
	 * 
	 * @return the flag.
	 */
	public boolean isOutline() {
		return this.showOutline;
	}

	/**
	 * Sets a flag that controls whether or not outlines of the areas are drawn.
	 * 
	 * @param show
	 *           the flag.
	 */
	public void setOutline(boolean show) {
		this.showOutline = show;
	}

	/**
	 * Returns true if shapes are being plotted by the renderer.
	 * 
	 * @return <code>true</code> if shapes are being plotted by the renderer.
	 */
	public boolean getPlotShapes() {
		return this.plotShapes;
	}

	/**
	 * Returns true if lines are being plotted by the renderer.
	 * 
	 * @return <code>true</code> if lines are being plotted by the renderer.
	 */
	public boolean getPlotLines() {
		return this.plotLines;
	}

	/**
	 * Returns true if Area is being plotted by the renderer.
	 * 
	 * @return <code>true</code> if Area is being plotted by the renderer.
	 */
	public boolean getPlotArea() {
		return this.plotArea;
	}

	/**
	 * Initialises the renderer and returns a state object that should be passed to all subsequent
	 * calls to the drawItem(...) method.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param dataArea
	 *           the area inside the axes.
	 * @param plot
	 *           the plot.
	 * @param data
	 *           the data.
	 * @param info
	 *           an optional info collection object to return data back to the caller.
	 * @return a state object for use by the renderer.
	 */
	public XYItemRendererState initialise(Graphics2D g2,
														Rectangle2D dataArea,
														XYPlot plot,
														XYDataset data,
														PlotRenderingInfo info) {
		AreaXYItemRendererState state = new AreaXYItemRendererState(info);
		return state;
	}

	/**
	 * Draws the visual representation of a single data item.
	 * 
	 * @param g2
	 *           the graphics device.
	 * @param state
	 *           the renderer state.
	 * @param dataArea
	 *           the area within which the data is being drawn.
	 * @param info
	 *           collects information about the drawing.
	 * @param plot
	 *           the plot (can be used to obtain standard color information etc).
	 * @param domainAxis
	 *           the domain axis.
	 * @param rangeAxis
	 *           the range axis.
	 * @param dataset
	 *           the dataset.
	 * @param series
	 *           the series index (zero-based).
	 * @param item
	 *           the item index (zero-based).
	 * @param crosshairState
	 *           information about crosshairs on a plot.
	 * @param pass
	 *           the pass index.
	 */
	public void drawItem(Graphics2D g2,
									XYItemRendererState state,
									Rectangle2D dataArea,
									PlotRenderingInfo info,
									XYPlot plot,
									ValueAxis domainAxis,
									ValueAxis rangeAxis,
									XYDataset dataset,
									int series,
									int item,
									CrosshairState crosshairState,
									int pass) {

		AreaXYItemRendererState areaState = (AreaXYItemRendererState) state;

		// get the data point...
		Number x1 = dataset.getXValue(series, item);
		Number y1 = dataset.getYValue(series, item);
		if (y1 == null) {
			y1 = AbstractRenderer.ZERO;
		}

		double transX1 = domainAxis.valueToJava2D(
							x1.doubleValue(), dataArea, plot.getDomainAxisEdge()
							);
		double transY1 = rangeAxis.valueToJava2D(
							y1.doubleValue(), dataArea, plot.getRangeAxisEdge()
							);

		// get the previous point and the next point so we can calculate a "hot spot"
		// for the area (used by the chart entity)...
		int itemCount = dataset.getItemCount(series);
		Number x0 = dataset.getXValue(series, Math.max(item - 1, 0));
		Number y0 = dataset.getYValue(series, Math.max(item - 1, 0));
		if (y0 == null) {
			y0 = AbstractRenderer.ZERO;
		}
		double transX0 = domainAxis.valueToJava2D(
							x0.doubleValue(), dataArea, plot.getDomainAxisEdge()
							);
		double transY0 = rangeAxis.valueToJava2D(
							y0.doubleValue(), dataArea, plot.getRangeAxisEdge()
							);

		Number x2 = dataset.getXValue(series, Math.min(item + 1, itemCount - 1));
		Number y2 = dataset.getYValue(series, Math.min(item + 1, itemCount - 1));
		if (y2 == null) {
			y2 = AbstractRenderer.ZERO;
		}
		double transX2 = domainAxis.valueToJava2D(
							x2.doubleValue(), dataArea, plot.getDomainAxisEdge()
							);
		double transY2 = rangeAxis.valueToJava2D(
							y2.doubleValue(), dataArea, plot.getRangeAxisEdge()
							);

		double transZero = rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge());
		Polygon hotspot = null;
		if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
			hotspot = new Polygon();
			hotspot.addPoint((int) transZero, (int) ((transX0 + transX1) / 2.0));
			hotspot.addPoint((int) ((transY0 + transY1) / 2.0), (int) ((transX0 + transX1) / 2.0));
			hotspot.addPoint((int) transY1, (int) transX1);
			hotspot.addPoint((int) ((transY1 + transY2) / 2.0), (int) ((transX1 + transX2) / 2.0));
			hotspot.addPoint((int) transZero, (int) ((transX1 + transX2) / 2.0));
		} else { // vertical orientation
			hotspot = new Polygon();
			hotspot.addPoint((int) ((transX0 + transX1) / 2.0), (int) transZero);
			hotspot.addPoint((int) ((transX0 + transX1) / 2.0), (int) ((transY0 + transY1) / 2.0));
			hotspot.addPoint((int) transX1, (int) transY1);
			hotspot.addPoint((int) ((transX1 + transX2) / 2.0), (int) ((transY1 + transY2) / 2.0));
			hotspot.addPoint((int) ((transX1 + transX2) / 2.0), (int) transZero);
		}

		if (item == 0) { // create a new area polygon for the series
			areaState.area = new Polygon();
			// the first point is (x, 0)
			double zero = rangeAxis.valueToJava2D(0.0, dataArea, plot.getRangeAxisEdge());
			if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				areaState.area.addPoint((int) transX1, (int) zero);
			} else
				if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
					areaState.area.addPoint((int) zero, (int) transX1);
				}
		}

		// Add each point to Area (x, y)
		if (plot.getOrientation() == PlotOrientation.VERTICAL) {
			areaState.area.addPoint((int) transX1, (int) transY1);
		} else
			if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
				areaState.area.addPoint((int) transY1, (int) transX1);
			}

		Paint paint = getItemPaint(series, item);
		Stroke stroke = getItemStroke(series, item);
		g2.setPaint(paint);
		g2.setStroke(stroke);

		Shape shape = null;
		if (this.plotShapes) {
			shape = getItemShape(series, item);
			if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				shape = createTransformedShape(shape, transX1, transY1);
			} else
				if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
					shape = createTransformedShape(shape, transY1, transX1);
				}
			g2.draw(shape);
		}

		if (this.plotLines) {
			if (item > 0) {
				if (plot.getOrientation() == PlotOrientation.VERTICAL) {
					areaState.line.setLine(transX0, transY0, transX1, transY1);
				} else
					if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
						areaState.line.setLine(transY0, transX0, transY1, transX1);
					}
				g2.draw(areaState.line);
			}
		}

		// Check if the item is the last item for the series.
		// and number of items > 0. We can't draw an area for a single point.
		if (this.plotArea && item > 0 && item == (itemCount - 1)) {

			if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				// Add the last point (x,0)
				areaState.area.addPoint((int) transX1, (int) transZero);
			} else
				if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
					// Add the last point (x,0)
					areaState.area.addPoint((int) transZero, (int) transX1);
				}

			g2.fill(areaState.area);

			// draw an outline around the Area.
			if (this.showOutline) {
				g2.setStroke(plot.getOutlineStroke());
				g2.setPaint(plot.getOutlinePaint());
				g2.draw(areaState.area);
			}
		}

		updateCrosshairValues(
							crosshairState, x1.doubleValue(), y1.doubleValue(), transX1, transY1,
							plot.getOrientation());

		// collect entity and tool tip information...
		if (state.getInfo() != null) {
			EntityCollection entities = state.getInfo().getOwner().getEntityCollection();
			if (entities != null && hotspot != null) {
				String tip = null;
				XYToolTipGenerator generator = getToolTipGenerator(series, item);
				if (generator != null) {
					tip = generator.generateToolTip(dataset, series, item);
				}
				String url = null;
				if (getURLGenerator() != null) {
					url = getURLGenerator().generateURL(dataset, series, item);
				}
				XYItemEntity entity = new XYItemEntity(hotspot, dataset, series, item, tip, url);
				entities.addEntity(entity);
			}
		}

	}

	/**
	 * Returns a clone of the renderer.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if the renderer cannot be cloned.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}

/**
 * The state object used by the renderer for one chart drawing. The state is set-up by the
 * initialise() method, and the plot will pass this state object to each invocation of the
 * drawItem(...) method. At the end of drawing the chart, the state is discarded.
 * <p>
 * If a chart is being drawn to several targets simultaneously, a different state instance will be used for each drawing.
 */
class AreaXYItemRendererState extends XYItemRendererState {

	/** Working storage for the area under one series. */
	public Polygon area;

	/** Working line that can be recycled. */
	public Line2D line;

	/**
	 * Creates a new state.
	 * 
	 * @param info
	 *           the plot rendering info.
	 */
	public AreaXYItemRendererState(PlotRenderingInfo info) {
		super(info);
		this.area = new Polygon();
		this.line = new Line2D.Double();
	}

}
