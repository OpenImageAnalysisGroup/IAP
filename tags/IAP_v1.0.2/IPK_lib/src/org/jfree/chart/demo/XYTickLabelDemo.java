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
 * --------------------
 * XYTickLabelDemo.java
 * --------------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited and Contributors.
 * Original Author: Matthias Rose;
 * Contributor(s): -;
 * $Id: XYTickLabelDemo.java,v 1.1 2011-01-31 09:01:45 klukas Exp $
 * Changes
 * -------
 * 15-Jul-2002 : Version 1 (MR);
 */

package org.jfree.chart.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolicAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.StandardXYItemRenderer;
import org.jfree.data.XYSeries;
import org.jfree.data.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.Spacer;

/**
 * An example which shows some bugs with tick labels in version 0.9.13
 * 
 * @author Matthias Rose
 */
public class XYTickLabelDemo extends ApplicationFrame implements ActionListener {

	/** The default font size. */
	private static final int DEFAULT_FONT_SIZE = 13; // causes some overlapping

	/** The chart */
	private JFreeChart chart;

	/** Tick labels vertical? */
	private JCheckBox verticalTickLabelsCheckBox;

	/** Plot horizontal? */
	private JCheckBox horizontalPlotCheckBox;

	/** SymbolicAxes? */
	private JCheckBox symbolicAxesCheckBox;

	/** Tick labels font size entry field */
	private JTextField fontSizeTextField;

	/**
	 * A demonstration application showing some bugs with tick labels in version 0.9.13
	 * 
	 * @param title
	 *           the frame title.
	 */
	public XYTickLabelDemo(final String title) {

		super(title);
		this.chart = createChart();
		final ChartPanel chartPanel = new ChartPanel(this.chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 270));

		final JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);
		mainPanel.add(chartPanel);

		final JPanel optionsPanel = new JPanel();
		mainPanel.add(optionsPanel, BorderLayout.SOUTH);

		this.symbolicAxesCheckBox = new JCheckBox("Symbolic axes");
		this.symbolicAxesCheckBox.addActionListener(this);
		optionsPanel.add(this.symbolicAxesCheckBox);

		this.verticalTickLabelsCheckBox = new JCheckBox("Tick labels vertical");
		this.verticalTickLabelsCheckBox.addActionListener(this);
		optionsPanel.add(this.verticalTickLabelsCheckBox);

		this.fontSizeTextField = new JTextField(3);
		this.fontSizeTextField.addActionListener(this);
		optionsPanel.add(new JLabel("Font size:"));
		optionsPanel.add(this.fontSizeTextField);
		final ValueAxis axis = this.chart.getXYPlot().getDomainAxis();
		this.fontSizeTextField.setText(DEFAULT_FONT_SIZE + "");

		final XYPlot plot = this.chart.getXYPlot();
		Font ft = axis.getTickLabelFont();
		ft = ft.deriveFont((float) DEFAULT_FONT_SIZE);
		plot.getDomainAxis().setTickLabelFont(ft);
		plot.getRangeAxis().setTickLabelFont(ft);
		plot.getDomainAxis(1).setTickLabelFont(ft);
		plot.getRangeAxis(1).setTickLabelFont(ft);

		this.horizontalPlotCheckBox = new JCheckBox("Plot horizontal");
		this.horizontalPlotCheckBox.addActionListener(this);
		optionsPanel.add(this.horizontalPlotCheckBox);
	}

	/**
	 * When a checkbox is changed ...
	 * 
	 * @param event
	 *           the event.
	 */
	public void actionPerformed(final ActionEvent event) {
		final ValueAxis[] axes = new ValueAxis[4];
		final XYPlot plot = this.chart.getXYPlot();
		axes[0] = plot.getDomainAxis();
		axes[1] = plot.getRangeAxis();
		axes[2] = plot.getDomainAxis(1);
		axes[3] = plot.getRangeAxis(1);

		final Object source = event.getSource();

		if (source == this.symbolicAxesCheckBox) {

			final boolean val = this.symbolicAxesCheckBox.isSelected();

			for (int i = 0; i < axes.length; i++) {
				ValueAxis axis = axes[i];
				final String label = axis.getLabel();
				final int maxTick = (int) axis.getUpperBound();
				final String[] tickLabels = new String[maxTick];
				final Font ft = axis.getTickLabelFont();
				for (int itk = 0; itk < maxTick; itk++) {
					tickLabels[itk] = "Label " + itk;
				}
				axis = val ? new SymbolicAxis(label, tickLabels) : new NumberAxis(label);
				axis.setTickLabelFont(ft);
				axes[i] = axis;
			}
			plot.setDomainAxis(axes[0]);
			plot.setRangeAxis(axes[1]);
			plot.setDomainAxis(1, axes[2]);
			plot.setRangeAxis(1, axes[3]);

		}

		if (source == this.symbolicAxesCheckBox || source == this.verticalTickLabelsCheckBox) {
			final boolean val = this.verticalTickLabelsCheckBox.isSelected();

			for (int i = 0; i < axes.length; i++) {
				axes[i].setVerticalTickLabels(val);
			}

		} else
			if (source == this.symbolicAxesCheckBox || source == this.horizontalPlotCheckBox) {

				final PlotOrientation val = this.horizontalPlotCheckBox.isSelected()
									? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL;
				this.chart.getXYPlot().setOrientation(val);

			} else
				if (source == this.symbolicAxesCheckBox || source == this.fontSizeTextField) {
					final String s = this.fontSizeTextField.getText();
					if (s.length() > 0) {
						final float sz = Float.parseFloat(s);
						for (int i = 0; i < axes.length; i++) {
							final ValueAxis axis = axes[i];
							Font ft = axis.getTickLabelFont();
							ft = ft.deriveFont(sz);
							axis.setTickLabelFont(ft);
						}
					}
				}
	}

	/**
	 * Creates the demo chart.
	 * 
	 * @return The chart.
	 */
	private JFreeChart createChart() {

		// create some sample data

		final XYSeries series1 = new XYSeries("Something");
		series1.add(0.0, 30.0);
		series1.add(1.0, 10.0);
		series1.add(2.0, 40.0);
		series1.add(3.0, 30.0);
		series1.add(4.0, 50.0);
		series1.add(5.0, 50.0);
		series1.add(6.0, 70.0);
		series1.add(7.0, 70.0);
		series1.add(8.0, 80.0);

		final XYSeriesCollection dataset1 = new XYSeriesCollection();
		dataset1.addSeries(series1);

		final XYSeries series2 = new XYSeries("Something else");
		series2.add(0.0, 5.0);
		series2.add(1.0, 4.0);
		series2.add(2.0, 1.0);
		series2.add(3.0, 5.0);
		series2.add(4.0, 0.0);

		final XYSeriesCollection dataset2 = new XYSeriesCollection();
		dataset2.addSeries(series2);

		// create the chart

		final JFreeChart result = ChartFactory.createXYLineChart(
							"Tick Label Demo",
							"Domain Axis 1",
							"Range Axis 1",
							dataset1,
							PlotOrientation.VERTICAL,
							false,
							true,
							false
							);

		result.setBackgroundPaint(Color.white);
		final XYPlot plot = result.getXYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));

		final StandardXYItemRenderer renderer = (StandardXYItemRenderer) plot.getRenderer();
		renderer.setPaint(Color.black);

		// DOMAIN AXIS 2
		final NumberAxis xAxis2 = new NumberAxis("Domain Axis 2");
		xAxis2.setAutoRangeIncludesZero(false);
		plot.setDomainAxis(1, xAxis2);

		// RANGE AXIS 2
		final DateAxis yAxis1 = new DateAxis("Range Axis 1");
		plot.setRangeAxis(yAxis1);

		final DateAxis yAxis2 = new DateAxis("Range Axis 2");
		plot.setRangeAxis(1, yAxis2);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

		plot.setDataset(1, dataset2);
		plot.mapDatasetToDomainAxis(1, 1);
		plot.mapDatasetToRangeAxis(1, 1);

		return result;
	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *           ignored.
	 */
	public static void main(final String[] args) {

		final XYTickLabelDemo demo = new XYTickLabelDemo("Tick Label Demo");
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);

	}
}
