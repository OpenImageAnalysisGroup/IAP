/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 06.09.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.StandardLegend;

public class MyLegendComponent extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StandardLegend sl;
	private double scale;
	
	public MyLegendComponent(StandardLegend sl, double scale) {
		this.sl = sl;
		this.scale = scale;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(scale, scale);
		sl.draw(g2, new Rectangle2D.Double(0, 0, getWidth() / scale, getHeight() / scale), new ChartRenderingInfo());
		
	}
	
}
