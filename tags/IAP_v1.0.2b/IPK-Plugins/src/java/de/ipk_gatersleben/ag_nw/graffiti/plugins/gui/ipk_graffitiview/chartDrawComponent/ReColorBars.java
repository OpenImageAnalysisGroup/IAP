/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import org.graffiti.editor.MainFrame;
import org.graffiti.session.EditorSession;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.CategoryDataset;

public class ReColorBars {
	
	public static void checkComponent(Component comp) {
		if (comp instanceof Container) {
			Container c = (Container) comp;
			Component[] ca = c.getComponents();
			for (int i = 0; i < ca.length; i++) {
				checkComponent(ca[i]);
			}
		}
		if (comp instanceof ChartPanel) {
			ChartPanel cp = (ChartPanel) comp;
			JFreeChart fc = cp.getChart();
			CategoryPlot catp = fc.getCategoryPlot();
			CategoryDataset cd = catp.getDataset();
			for (int row = 0; row < cd.getRowCount(); row++) {
				int series = row;
				double sumValues = 0;
				int cnt = 0;
				for (int col = 0; col < cd.getColumnCount(); col++) {
					Number n = cd.getValue(row, col);
					if (n != null) {
						sumValues += n.doubleValue();
						cnt++;
					}
				}
				if (cnt > 0) {
					double avg = sumValues / cnt;
					Color seriesColor;
					if (avg > 1)
						seriesColor = Color.RED;
					else
						seriesColor = Color.WHITE;
					catp.getRenderer().setSeriesPaint(series, seriesColor);
				}
			}
		} else {
			System.out.println("Type: " + comp.getClass().getCanonicalName());
		}
	}
	
	public static void reColor() {
		EditorSession es = MainFrame.getInstance().getActiveEditorSession();
		Component[] ca = es.getActiveView().getViewComponent().getComponents();
		for (int i = 0; i < ca.length; i++) {
			checkComponent(ca[i]);
		}
	}
}
