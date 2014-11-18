/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 16.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.DefaultCategoryDataset;

public class DirkDemo {
	public static void main(String[] args) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		dataset.addValue(1.2d, "Row 1", "Day 0");
		dataset.addValue(2.2d, "Row 1", "Day 1");
		dataset.addValue(3.2d, "Row 1", "Day 2");
		dataset.addValue(5.2d, "Row 1", "Day 3");
		
		JFreeChart chart = ChartFactory.createLineChart("Demo Chart",
							"DOMAIN AXIS",
							"RANGE AXIS",
							dataset, PlotOrientation.VERTICAL, true, true, false);
		
		ChartPanel chartPanel = new ChartPanel(chart);
		
		JFrame window = new JFrame("Line Chart");
		LayoutManager lm = new TableLayout(new double[][] { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } });
		window.setLayout(lm);
		window.add(chartPanel, "0,0");
		window.validate();
		window.setSize(640, 480);
		window.setLocationByPlatform(true);
		window.setVisible(true);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
}
