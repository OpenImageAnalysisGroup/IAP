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
 * ------------------
 * MouseZoomDemo.java
 * ------------------
 * (C) Copyright 2002-2004, by Viktor Rajewski and Contributors.
 * Original Author: Viktor Rajewski;
 * Contributor(s): David Gilbert (for Object Refinery Limited);
 * $Id: MouseZoomDemo.java,v 1.1 2011-01-31 09:01:53 klukas Exp $
 * Changes
 * -------
 * 12-Aug-2002 : Version 1, based on XYSeriesDemo (VR);
 * 11-Oct-2002 : Renamed XYSeriesMouseZoomDemo --> MouseZoomDemo, altered layout, and fixed errors
 * reported by Checkstyle (DG);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.Log;
import org.jfree.util.PrintStreamLogTarget;

/**
 * A simple demo showing mouse zooming.
 * 
 * @author Viktor Rajewski
 */
public class MouseZoomDemo extends ApplicationFrame {

	/** The chart panel. */
	private ChartPanel chartPanel;

	/** X zoom. */
	private JCheckBox xzoom;

	/** Y zoom. */
	private JCheckBox yzoom;

	/**
	 * A demonstration of mouse zooming.
	 * 
	 * @param title
	 *           the frame title.
	 */
	public MouseZoomDemo(final String title) {

		super(title);
		final SampleXYDataset data = new SampleXYDataset();
		final JFreeChart chart = ChartFactory.createXYLineChart(
							"Mouse Zoom Demo",
							"X",
							"Y",
							data,
							PlotOrientation.VERTICAL,
							true,
							true,
							false
							);

		this.chartPanel = new ChartPanel(chart);
		this.chartPanel.setHorizontalZoom(false);
		this.chartPanel.setVerticalZoom(false);
		this.chartPanel.setHorizontalAxisTrace(false);
		this.chartPanel.setVerticalAxisTrace(false);
		this.chartPanel.setFillZoomRectangle(true);
		this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

		final JPanel main = new JPanel(new BorderLayout());
		final JPanel checkpanel = new JPanel();
		this.xzoom = new JCheckBox("Horizontal Mouse Zooming");
		this.xzoom.setSelected(false);
		this.yzoom = new JCheckBox("Vertical Mouse Zooming");
		this.yzoom.setSelected(false);
		final CheckListener clisten = new CheckListener();
		this.xzoom.addItemListener(clisten);
		this.yzoom.addItemListener(clisten);
		checkpanel.add(this.xzoom);
		checkpanel.add(this.yzoom);
		main.add(checkpanel, BorderLayout.SOUTH);
		main.add(this.chartPanel);
		setContentPane(main);

	}

	// ****************************************************************************
	// * JFREECHART DEVELOPER GUIDE *
	// * The JFreeChart Developer Guide, written by David Gilbert, is available *
	// * to purchase from Object Refinery Limited: *
	// * *
	// * http://www.object-refinery.com/jfreechart/guide.html *
	// * *
	// * Sales are used to provide funding for the JFreeChart project - please *
	// * support us so that we can continue developing free software. *
	// ****************************************************************************

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		Log.getInstance().addTarget(new PrintStreamLogTarget(System.out));
		final MouseZoomDemo demo = new MouseZoomDemo("Mouse Zoom Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}

	/**
	 * An item listener.
	 * 
	 * @author VR
	 */
	class CheckListener implements ItemListener {

		/**
		 * Receives change events.
		 * 
		 * @param e
		 *           the event.
		 */
		public void itemStateChanged(final ItemEvent e) {
			final Object source = e.getItemSelectable();
			if (source == xzoom) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					chartPanel.setHorizontalZoom(false);
					chartPanel.setHorizontalAxisTrace(false);
					chartPanel.repaint();
				} else {
					chartPanel.setHorizontalZoom(true);
					chartPanel.setHorizontalAxisTrace(true);
				}
			} else
				if (source == yzoom) {
					if (e.getStateChange() == ItemEvent.DESELECTED) {
						chartPanel.setVerticalZoom(false);
						chartPanel.setVerticalAxisTrace(false);
						chartPanel.repaint();
					} else {
						chartPanel.setVerticalZoom(true);
						chartPanel.setVerticalAxisTrace(true);
					}
				}
		}
	}

}
