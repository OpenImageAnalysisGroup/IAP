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
 * ----------------------
 * PanScrollZoomDemo.java
 * ----------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: Matthias Rose (Ablay & Fodi GmbH, Germany);
 * Contributor(s): Eduardo Ramalho;
 * David Gilbert (for Object Refinery Limited);
 * $Id: PanScrollZoomDemo.java,v 1.1 2011-01-31 09:01:55 klukas Exp $
 * Changes
 * -------
 * 18-Feb-2004 : Version 1 added to JFreeChart distribution, contributed by Matthias Rose (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.BoundedRangeModel;
import javax.swing.ButtonGroup;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.chart.renderer.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

/**
 * A demo for panning, scrolling and zooming.
 */
public class PanScrollZoomDemo extends JFrame
											implements ActionListener,
														ChangeListener,
														ChartChangeListener,
														MouseListener,
														MouseMotionListener {

	/** The panel that displays the chart. */
	private ChartPanel chartPanel;

	/** The scroll factor. */
	private double scrollFactor = 1000;

	/** The scroll bar. */
	private JScrollBar scrollBar;

	/** The starting point for panning. */
	private Point2D panStartPoint;

	/** The min/max values for the primary axis. */
	private double[] primYMinMax = new double[2];

	/** The min/max values for the secondary axis. */
	private double[] secondYMinMax = new double[2];

	/** Action command for the 'Pan' button. */
	private static final String ACTION_CMD_PAN = "pan";

	/** Action command for the zoom box button. */
	private static final String ACTION_CMD_ZOOM_BOX = "zoomBox";

	/** Action command for the zoom fit button. */
	private static final String ACTION_CMD_ZOOM_TO_FIT = "zoomFit";

	/** Action command for the '+' button. */
	private static final String ACTION_CMD_ZOOM_IN = "zoomIn";

	/** Action command for the '-' button. */
	private static final String ACTION_CMD_ZOOM_OUT = "zoomOut";

	/** The zoom factor. */
	private static final double ZOOM_FACTOR = 0.8;

	/** The toolbar. */
	private JToolBar toolBar;

	/** The zoom button. */
	private AbstractButton zoomButton;

	/** The pan button. */
	private AbstractButton panButton;

	/** The zoom in button. */
	private AbstractButton zoomInButton;

	/** The zoom out button. */
	private AbstractButton zoomOutButton;

	/** The fit button. */
	private AbstractButton fitButton;

	/**
	 * Creates a new demo instance.
	 * 
	 * @param frameTitle
	 *           the frame title.
	 */
	public PanScrollZoomDemo(final String frameTitle) {

		super(frameTitle);

		this.toolBar = createToolbar();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(this.toolBar, BorderLayout.SOUTH);

		final JFreeChart chart = createChart();

		this.scrollBar.setModel(new DefaultBoundedRangeModel());
		recalcScrollBar(chart.getPlot());

		this.chartPanel = new ChartPanel(chart) {
			public void autoRangeBoth() {
				System.out.println("Use 'Fit all' button");
			}
		};

		chart.addChangeListener(this);

		// enable zoom
		actionPerformed(new ActionEvent(this, 0, ACTION_CMD_ZOOM_BOX));

		// MouseListeners for pan function
		this.chartPanel.addMouseListener(this);
		this.chartPanel.addMouseMotionListener(this);

		// remove popup menu to allow panning
		// with right mouse pressed
		this.chartPanel.setPopupMenu(null);

		getContentPane().add(this.chartPanel);
	}

	/**
	 * Creates a sample chart.
	 * 
	 * @return a sample chart.
	 */
	private JFreeChart createChart() {

		final XYSeriesCollection primaryJFreeColl = new XYSeriesCollection();
		final XYSeries left1 = new XYSeries("Left 1");
		left1.add(1, 2);
		left1.add(2.8, 5.9);
		left1.add(3, null);
		left1.add(3.4, 2);
		left1.add(5, -1);
		left1.add(7, 1);
		primaryJFreeColl.addSeries(left1);

		final XYSeriesCollection secondaryJFreeColl = new XYSeriesCollection();
		final XYSeries right1 = new XYSeries("Right 1");
		right1.add(3.5, 2.2);
		right1.add(1.2, 1.3);
		right1.add(5.7, 4.1);
		right1.add(7.5, 7.4);
		secondaryJFreeColl.addSeries(right1);

		final NumberAxis xAxis = new NumberAxis("X");
		xAxis.setAutoRangeIncludesZero(false);
		xAxis.setAutoRangeStickyZero(false);

		final NumberAxis primaryYAxis = new NumberAxis("Y1");
		primaryYAxis.setAutoRangeIncludesZero(false);
		primaryYAxis.setAutoRangeStickyZero(false);

		// create plot
		final XYItemRenderer y1Renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES);
		y1Renderer.setSeriesPaint(0, Color.blue);
		y1Renderer.setToolTipGenerator(new StandardXYToolTipGenerator());
		final XYPlot xyPlot = new XYPlot(primaryJFreeColl, xAxis, primaryYAxis, y1Renderer);

		// 2nd y-axis

		final NumberAxis secondaryYAxis = new NumberAxis("Y2");
		secondaryYAxis.setAutoRangeIncludesZero(false);
		secondaryYAxis.setAutoRangeStickyZero(false);

		xyPlot.setRangeAxis(1, secondaryYAxis);
		xyPlot.setDataset(1, secondaryJFreeColl);

		xyPlot.mapDatasetToRangeAxis(1, 1);
		xyPlot.mapDatasetToDomainAxis(1, 1);

		final XYItemRenderer y2Renderer = new StandardXYItemRenderer(
							StandardXYItemRenderer.SHAPES_AND_LINES
							);
		y2Renderer.setToolTipGenerator(new StandardXYToolTipGenerator());
		xyPlot.setRenderer(1, y2Renderer);

		// set some fixed y-dataranges and remember them
		// because default chartPanel.autoRangeBoth()
		// would destroy them

		ValueAxis axis = xyPlot.getRangeAxis();
		this.primYMinMax[0] = -5;
		this.primYMinMax[1] = 15;
		axis.setLowerBound(this.primYMinMax[0]);
		axis.setUpperBound(this.primYMinMax[1]);

		axis = xyPlot.getRangeAxis(1);
		this.secondYMinMax[0] = -1;
		this.secondYMinMax[1] = 10;
		axis.setLowerBound(this.secondYMinMax[0]);
		axis.setUpperBound(this.secondYMinMax[1]);

		// Title + legend

		final String title = "To pan in zoom mode hold right mouse pressed";
		final JFreeChart ret = new JFreeChart(title, null, xyPlot, true);
		final TextTitle textTitle = new TextTitle(
							"(but you can only pan if the chart was zoomed before)"
							);
		ret.addSubtitle(textTitle);
		return ret;
	}

	/**
	 * Creates the toolbar.
	 * 
	 * @return the toolbar.
	 */
	private JToolBar createToolbar() {
		final JToolBar toolbar = new JToolBar();

		final ButtonGroup groupedButtons = new ButtonGroup();

		// ACTION_CMD_PAN
		this.panButton = new JToggleButton();
		prepareButton(this.panButton, ACTION_CMD_PAN, " Pan ", "Pan mode");
		groupedButtons.add(this.panButton);
		toolbar.add(this.panButton);

		// ACTION_CMD_ZOOM_BOX
		this.zoomButton = new JToggleButton();
		prepareButton(this.zoomButton, ACTION_CMD_ZOOM_BOX, " Zoom ", "Zoom mode");
		groupedButtons.add(this.zoomButton);
		this.zoomButton.setSelected(true); // no other makes sense after startup
		toolbar.add(this.zoomButton);

		// end of toggle-button group for select/pan/zoom-box
		toolbar.addSeparator();

		// ACTION_CMD_ZOOM_IN
		this.zoomInButton = new JButton();
		prepareButton(this.zoomInButton, ACTION_CMD_ZOOM_IN, " + ", "Zoom in");
		toolbar.add(this.zoomInButton);

		// ACTION_CMD_ZOOM_OUT
		this.zoomOutButton = new JButton();
		prepareButton(this.zoomOutButton, ACTION_CMD_ZOOM_OUT, " - ", "Zoom out");
		toolbar.add(this.zoomOutButton);

		// ACTION_CMD_ZOOM_TO_FIT
		this.fitButton = new JButton();
		prepareButton(this.fitButton, ACTION_CMD_ZOOM_TO_FIT, " Fit ", "Fit all");
		toolbar.add(this.fitButton);

		toolbar.addSeparator();

		this.scrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
		// int ht = (int) zoomButton.getPreferredSize().getHeight();
		// scrollBar.setPreferredSize(new Dimension(0, ht));
		this.scrollBar.setModel(new DefaultBoundedRangeModel());

		toolbar.add(this.scrollBar);

		this.zoomOutButton.setEnabled(false);
		this.fitButton.setEnabled(false);
		this.scrollBar.setEnabled(false);

		toolbar.setFloatable(false);
		return toolbar;
	}

	/**
	 * Prepares a button.
	 * 
	 * @param button
	 *           the button.
	 * @param actionKey
	 *           the action key.
	 * @param buttonLabelText
	 *           the button label.
	 * @param toolTipText
	 *           the tooltip text.
	 */
	private void prepareButton(final AbstractButton button,
											final String actionKey,
											final String buttonLabelText,
											final String toolTipText) {
		// todo
		// as this action is empty and the button text is
		// redefined later, it can be safely removed ...
		// Action action = new AbstractAction(actionKey) {
		// public void actionPerformed(ActionEvent evt) {
		// // ignored
		// }
		// };
		// button.addActionListener(action);
		button.setActionCommand(actionKey);
		button.setText(buttonLabelText);
		button.setToolTipText(toolTipText);
		button.addActionListener(this);
	}

	/**
	 * Sets the pan mode.
	 * 
	 * @param val
	 *           a boolean.
	 */
	private void setPanMode(final boolean val) {

		this.chartPanel.setHorizontalZoom(!val);
		// chartPanel.setHorizontalAxisTrace(! val);
		this.chartPanel.setVerticalZoom(!val);
		// chartPanel.setVerticalAxisTrace(! val);

		if (val) {
			this.chartPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else {
			this.chartPanel.setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Handles an action event.
	 * 
	 * @param evt
	 *           the event.
	 */
	public void actionPerformed(final ActionEvent evt) {
		try {
			final String acmd = evt.getActionCommand();

			if (acmd.equals(ACTION_CMD_ZOOM_BOX)) {
				setPanMode(false);
			} else
				if (acmd.equals(ACTION_CMD_PAN)) {
					setPanMode(true);
				} else
					if (acmd.equals(ACTION_CMD_ZOOM_IN)) {
						final ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();
						final Rectangle2D rect = info.getPlotInfo().getDataArea();
						zoomBoth(rect.getCenterX(), rect.getCenterY(), ZOOM_FACTOR);
					} else
						if (acmd.equals(ACTION_CMD_ZOOM_OUT)) {
							final ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();
							final Rectangle2D rect = info.getPlotInfo().getDataArea();
							zoomBoth(rect.getCenterX(), rect.getCenterY(), 1 / ZOOM_FACTOR);
						} else
							if (acmd.equals(ACTION_CMD_ZOOM_TO_FIT)) {

								// X-axis (has no fixed borders)
								this.chartPanel.autoRangeHorizontal();

								// Y-Axes) (autoRangeVertical
								// not useful because of fixed borders
								final Plot plot = this.chartPanel.getChart().getPlot();
								if (plot instanceof ValueAxisPlot) {

									final XYPlot vvPlot = (XYPlot) plot;
									ValueAxis axis = vvPlot.getRangeAxis();
									if (axis != null) {
										axis.setLowerBound(this.primYMinMax[0]);
										axis.setUpperBound(this.primYMinMax[1]);
									}
									if (plot instanceof XYPlot) {
										final XYPlot xyPlot = (XYPlot) plot;
										axis = xyPlot.getRangeAxis(1);
										if (axis != null) {
											axis.setLowerBound(this.secondYMinMax[0]);
											axis.setUpperBound(this.secondYMinMax[1]);
										}
									}
								}
							}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a {@link ChangeEvent} (in this case, coming from the scrollbar).
	 * 
	 * @param event
	 *           the event.
	 */
	public void stateChanged(final ChangeEvent event) {
		try {
			final Object src = event.getSource();
			final BoundedRangeModel scrollBarModel = this.scrollBar.getModel();
			if (src == scrollBarModel) {
				final int val = scrollBarModel.getValue();
				final int ext = scrollBarModel.getExtent();

				final Plot plot = this.chartPanel.getChart().getPlot();
				if (plot instanceof XYPlot) {
					final XYPlot hvp = (XYPlot) plot;
					final ValueAxis axis = hvp.getDomainAxis();

					// avoid problems
					this.chartPanel.getChart().removeChangeListener(this);

					axis.setRange(val / this.scrollFactor, (val + ext) / this.scrollFactor);

					// restore chart listener
					this.chartPanel.getChart().addChangeListener(this);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a {@link ChartChangeEvent}.
	 * 
	 * @param event
	 *           the event.
	 */
	public void chartChanged(final ChartChangeEvent event) {
		try {
			if (event.getChart() == null) {
				return;
			}

			final BoundedRangeModel scrollBarModel = this.scrollBar.getModel();
			if (scrollBarModel == null) {
				return;
			}

			boolean chartIsZoomed = false;

			final Plot plot = event.getChart().getPlot();
			if (plot instanceof XYPlot) {
				final XYPlot hvp = (XYPlot) plot;
				final ValueAxis xAxis = hvp.getDomainAxis();
				final Range xAxisRange = xAxis.getRange();

				// avoid recursion
				scrollBarModel.removeChangeListener(this);

				final int low = (int) (xAxisRange.getLowerBound() * this.scrollFactor);
				scrollBarModel.setValue(low);
				final int ext = (int) (xAxisRange.getUpperBound() * this.scrollFactor - low);
				scrollBarModel.setExtent(ext);

				// restore
				scrollBarModel.addChangeListener(this);

				// check if zoomed horizontally
				// Range hdr = hvp.getHorizontalDataRange(xAxis);
				final Range hdr = hvp.getDataRange(xAxis);

				final double len = hdr == null ? 0 : hdr.getLength();
				chartIsZoomed |= xAxisRange.getLength() < len;
			}

			if (!chartIsZoomed && plot instanceof XYPlot) {
				// check if zoomed vertically
				final XYPlot vvp = (XYPlot) plot;
				ValueAxis yAxis = vvp.getRangeAxis();
				if (yAxis != null) {
					chartIsZoomed = yAxis.getLowerBound() > this.primYMinMax[0]
										|| yAxis.getUpperBound() < this.primYMinMax[1];

					// right y-axis
					if (!chartIsZoomed && plot instanceof XYPlot) {
						final XYPlot xyPlot = (XYPlot) plot;
						yAxis = xyPlot.getRangeAxis(1);
						if (yAxis != null) {
							chartIsZoomed = yAxis.getLowerBound() > this.secondYMinMax[0]
												|| yAxis.getUpperBound() < this.secondYMinMax[1];
						}
					}
				}
			}

			// enable "zoom-out-buttons" if chart is zoomed
			// otherwise disable them
			this.panButton.setEnabled(chartIsZoomed);
			this.zoomOutButton.setEnabled(chartIsZoomed);
			this.fitButton.setEnabled(chartIsZoomed);
			this.scrollBar.setEnabled(chartIsZoomed);
			if (!chartIsZoomed) {
				setPanMode(false);
				this.zoomButton.setSelected(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Mouse[Motion]Listeners for pan

	/**
	 * Handles a mouse pressed event (to start panning).
	 * 
	 * @param event
	 *           the event.
	 */
	public void mousePressed(final MouseEvent event) {
		try {
			if (this.panButton.isSelected()
								|| this.panButton.isEnabled()
								&& SwingUtilities.isRightMouseButton(event)) {
				final Rectangle2D dataArea = this.chartPanel.getScaledDataArea();
				final Point2D point = event.getPoint();
				if (dataArea.contains(point)) {
					setPanMode(true);
					this.panStartPoint = point;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a mouse released event (stops panning).
	 * 
	 * @param event
	 *           the event.
	 */
	public void mouseReleased(final MouseEvent event) {
		try {
			this.panStartPoint = null; // stop panning
			if (!this.panButton.isSelected()) {
				setPanMode(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a mouse dragged event to perform panning.
	 * 
	 * @param event
	 *           the event.
	 */
	public void mouseDragged(final MouseEvent event) {
		try {
			if (this.panStartPoint != null) {
				final Rectangle2D scaledDataArea = this.chartPanel.getScaledDataArea();

				this.panStartPoint = RefineryUtilities.getPointInRectangle(
									this.panStartPoint.getX(),
									this.panStartPoint.getY(),
									scaledDataArea
									);
				final Point2D panEndPoint = RefineryUtilities.getPointInRectangle(
									event.getX(), event.getY(), scaledDataArea
									);

				// horizontal pan

				final Plot plot = this.chartPanel.getChart().getPlot();
				if (plot instanceof XYPlot) {
					final XYPlot hvp = (XYPlot) plot;
					final ValueAxis xAxis = hvp.getDomainAxis();

					if (xAxis != null) {
						final double translatedStartPoint = xAxis.java2DToValue(
											(float) this.panStartPoint.getX(),
											scaledDataArea,
											hvp.getDomainAxisEdge()
											);
						final double translatedEndPoint = xAxis.java2DToValue(
											(float) panEndPoint.getX(),
											scaledDataArea,
											hvp.getDomainAxisEdge()
											);
						final double dX = translatedStartPoint - translatedEndPoint;

						final double oldMin = xAxis.getLowerBound();
						final double newMin = oldMin + dX;

						final double oldMax = xAxis.getUpperBound();
						final double newMax = oldMax + dX;

						// do not pan out of range
						if (newMin >= hvp.getDataRange(xAxis).getLowerBound()
											&& newMax <= hvp.getDataRange(xAxis).getUpperBound()) {
							xAxis.setLowerBound(newMin);
							xAxis.setUpperBound(newMax);
						}
					}
				}

				// vertical pan (1. Y-Axis)

				if (plot instanceof XYPlot) {
					final XYPlot vvp = (XYPlot) plot;
					final ValueAxis yAxis = vvp.getRangeAxis();

					if (yAxis != null) {
						final double translatedStartPoint = yAxis.java2DToValue(
											(float) this.panStartPoint.getY(),
											scaledDataArea,
											vvp.getRangeAxisEdge()
											);
						final double translatedEndPoint = yAxis.java2DToValue(
											(float) panEndPoint.getY(),
											scaledDataArea,
											vvp.getRangeAxisEdge()
											);
						final double dY = translatedStartPoint - translatedEndPoint;

						final double oldMin = yAxis.getLowerBound();
						final double newMin = oldMin + dY;

						final double oldMax = yAxis.getUpperBound();
						final double newMax = oldMax + dY;

						// do not pan out of range
						if (newMin >= this.primYMinMax[0] && newMax <= this.primYMinMax[1]) {
							yAxis.setLowerBound(newMin);
							yAxis.setUpperBound(newMax);
						}
					}
				}

				// vertical pan (2. Y-Axis)

				if (plot instanceof XYPlot) {
					final XYPlot xyPlot = (XYPlot) plot;
					final ValueAxis yAxis = xyPlot.getRangeAxis(1);

					if (yAxis != null) {
						final double translatedStartPoint = yAxis.java2DToValue(
											(float) this.panStartPoint.getY(),
											scaledDataArea,
											xyPlot.getRangeAxisEdge(1)
											);
						final double translatedEndPoint = yAxis.java2DToValue(
											(float) panEndPoint.getY(),
											scaledDataArea,
											xyPlot.getRangeAxisEdge(1)
											);
						final double dY = translatedStartPoint - translatedEndPoint;

						final double oldMin = yAxis.getLowerBound();
						final double newMin = oldMin + dY;

						final double oldMax = yAxis.getUpperBound();
						final double newMax = oldMax + dY;

						if (newMin >= this.secondYMinMax[0] && newMax <= this.secondYMinMax[1]) {
							yAxis.setLowerBound(newMin);
							yAxis.setUpperBound(newMax);
						}
					}
				}

				// for the next time
				this.panStartPoint = panEndPoint;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a mouse clicked event, in this case by ignoring it.
	 * 
	 * @param event
	 *           the event.
	 */
	public void mouseClicked(final MouseEvent event) {
		// ignored
	}

	/**
	 * Handles a mouse moved event, in this case by ignoring it.
	 * 
	 * @param event
	 *           the event.
	 */
	public void mouseMoved(final MouseEvent event) {
		// ignored
	}

	/**
	 * Handles a mouse entered event, in this case by ignoring it.
	 * 
	 * @param event
	 *           the event.
	 */
	public void mouseEntered(final MouseEvent event) {
		// ignored
	}

	/**
	 * Handles a mouse exited event, in this case by ignoring it.
	 * 
	 * @param event
	 *           the event.
	 */
	public void mouseExited(final MouseEvent event) {
		// ignored
	}

	/**
	 * Starting point for the demo.
	 * 
	 * @param args
	 *           the command line arguments (ignored).
	 */
	public static void main(final String[] args) {

		try {
			final String lookAndFeelClassName = WindowsLookAndFeel.class.getName();
			UIManager.setLookAndFeel(lookAndFeelClassName);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}

		final PanScrollZoomDemo demo = new PanScrollZoomDemo("Pan & Scroll & Zoom - Demo");
		demo.pack();
		demo.setVisible(true);

	}

	// PRIVATE

	/**
	 * Recalculates the scrollbar settings.
	 * 
	 * @param plot
	 *           the plot.
	 */
	private void recalcScrollBar(final Plot plot) {
		if (plot instanceof XYPlot) {
			final XYPlot hvp = (XYPlot) plot;
			final ValueAxis axis = hvp.getDomainAxis();

			axis.setLowerMargin(0);
			axis.setUpperMargin(0);

			final Range rng = axis.getRange();

			final BoundedRangeModel scrollBarModel = this.scrollBar.getModel();
			final int len = scrollBarModel.getMaximum() - scrollBarModel.getMinimum();
			if (rng.getLength() > 0) {
				this.scrollFactor = len / rng.getLength();
			}

			final double dblow = rng.getLowerBound();
			final int ilow = (int) (dblow * this.scrollFactor);
			scrollBarModel.setMinimum(ilow);
			final int val = ilow;
			scrollBarModel.setValue(val);

			final double dbup = rng.getUpperBound();
			final int iup = (int) (dbup * this.scrollFactor);
			scrollBarModel.setMaximum(iup);
			final int ext = iup - ilow;
			scrollBarModel.setExtent(ext);

			scrollBarModel.addChangeListener(this);
		}
	}

	/**
	 * Zooms in on an anchor point (measured in Java2D coordinates).
	 * 
	 * @param x
	 *           the x value.
	 * @param y
	 *           the y value.
	 * @param zoomFactor
	 *           the zoomFactor < 1 == zoom in; else out.
	 */
	private void zoomBoth(final double x, final double y, final double zoomFactor) {
		zoomHorizontal(x, zoomFactor);
		zoomVertical(y, zoomFactor);
	}

	/**
	 * Decreases the range on the horizontal axis, centered about a Java2D x coordinate.
	 * <P>
	 * The range on the x axis is multiplied by zoomFactor
	 * 
	 * @param x
	 *           the x coordinate in Java2D space.
	 * @param zoomFactor
	 *           the zoomFactor < 1 == zoom in; else out.
	 */
	private void zoomHorizontal(final double x, final double zoomFactor) {

		final JFreeChart chart = this.chartPanel.getChart();
		final ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();
		if (chart.getPlot() instanceof XYPlot) {
			final XYPlot hvp = (XYPlot) chart.getPlot();
			final ValueAxis axis = hvp.getDomainAxis();
			if (axis != null) {
				final double anchorValue = axis.java2DToValue(
									(float) x, info.getPlotInfo().getDataArea(), hvp.getDomainAxisEdge()
									);
				if (zoomFactor < 1.0) {
					axis.resizeRange(zoomFactor, anchorValue);
				} else
					if (zoomFactor > 1.0) {
						final Range range = hvp.getDataRange(axis);
						adjustRange(axis, range, zoomFactor, anchorValue);
					}
			}
		}
	}

	/**
	 * Decreases the range on the vertical axis, centered about a Java2D y coordinate.
	 * <P>
	 * The range on the y axis is multiplied by zoomFactor
	 * 
	 * @param y
	 *           the y coordinate in Java2D space.
	 * @param zoomFactor
	 *           the zoomFactor < 1 == zoom in; else out.
	 */
	private void zoomVertical(final double y, final double zoomFactor) {

		final JFreeChart chart = this.chartPanel.getChart();
		final ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();

		// 1. (left) Y-Axis

		if (chart.getPlot() instanceof XYPlot) {
			final XYPlot vvp = (XYPlot) chart.getPlot();
			final ValueAxis primYAxis = vvp.getRangeAxis();
			if (primYAxis != null) {
				final double anchorValue =
									primYAxis.java2DToValue(
														(float) y, info.getPlotInfo().getDataArea(), vvp.getRangeAxisEdge()
														);
				if (zoomFactor < 1.0) {
					// zoom in
					primYAxis.resizeRange(zoomFactor, anchorValue);

				} else
					if (zoomFactor > 1.0) {
						// zoom out
						final Range range = new Range(this.primYMinMax[0], this.primYMinMax[1]);
						adjustRange(primYAxis, range, zoomFactor, anchorValue);
					}
			}

			// 2. (right) Y-Axis

			if (chart.getPlot() instanceof XYPlot) {
				final XYPlot xyp = (XYPlot) chart.getPlot();
				final ValueAxis secYAxis = xyp.getRangeAxis(1);
				if (secYAxis != null) {
					final double anchorValue =
										secYAxis.java2DToValue(
															(float) y,
															info.getPlotInfo().getDataArea(),
															xyp.getRangeAxisEdge(1));
					if (zoomFactor < 1.0) {
						// zoom in
						secYAxis.resizeRange(zoomFactor, anchorValue);

					} else
						if (zoomFactor > 1.0) {
							// zoom out
							final Range range = new Range(this.secondYMinMax[0], this.secondYMinMax[1]);
							adjustRange(secYAxis, range, zoomFactor, anchorValue);
						}
				}
			}
		}
	}

	/**
	 * used for zooming
	 * 
	 * @param axis
	 *           the axis.
	 * @param range
	 *           the range.
	 * @param zoomFactor
	 *           the zoom factor.
	 * @param anchorValue
	 *           the anchor value.
	 */
	private void adjustRange(final ValueAxis axis, final Range range, final double zoomFactor,
										final double anchorValue) {

		if (axis == null || range == null) {
			return;
		}

		final double rangeMinVal = range.getLowerBound()
												- range.getLength() * axis.getLowerMargin();
		final double rangeMaxVal = range.getUpperBound()
												+ range.getLength() * axis.getUpperMargin();
		final double halfLength = axis.getRange().getLength() * zoomFactor / 2;
		double zoomedMinVal = anchorValue - halfLength;
		double zoomedMaxVal = anchorValue + halfLength;
		double adjMinVal = zoomedMinVal;
		if (zoomedMinVal < rangeMinVal) {
			adjMinVal = rangeMinVal;
			zoomedMaxVal += rangeMinVal - zoomedMinVal;
		}
		double adjMaxVal = zoomedMaxVal;
		if (zoomedMaxVal > rangeMaxVal) {
			adjMaxVal = rangeMaxVal;
			zoomedMinVal -= zoomedMaxVal - rangeMaxVal;
			adjMinVal = Math.max(zoomedMinVal, rangeMinVal);
		}

		final Range adjusted = new Range(adjMinVal, adjMaxVal);
		axis.setRange(adjusted);
	}

}
