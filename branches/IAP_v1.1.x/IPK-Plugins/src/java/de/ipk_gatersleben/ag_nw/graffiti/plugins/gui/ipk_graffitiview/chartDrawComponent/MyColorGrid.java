package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import org.jfree.chart.plot.PlotOrientation;

public class MyColorGrid extends JComponent {
	private static final long serialVersionUID = 1L;
	
	Color[][] colors = null;
	
	PlotOrientation plotOrientation;
	
	public MyColorGrid(Color[][] colors, PlotOrientation plotOrientation) {
		this.colors = colors;
		this.plotOrientation = plotOrientation;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (colors == null || colors.length == 0)
			return;
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		if (plotOrientation == PlotOrientation.VERTICAL) {
			double rW = ((double) (getWidth()) / (double) colors[0].length);
			double rH = ((double) (getHeight()) / (double) colors.length);
			double yy = 0;
			for (int row = 0; row < colors.length; row++) {
				double xx = 0;
				for (int col = 0; col < colors[row].length; col++) {
					if (colors[row][col] == null)
						continue;
					drawPoint(g, rW, rH, yy, row, xx, col);
					xx += rW;
				}
				yy += rH;
			}
		} else {
			double rH = ((double) getHeight() / (double) colors[0].length);
			double rW = ((double) getWidth() / (double) colors.length);
			double xx = 0;
			for (int row = 0; row < colors.length; row++) {
				double yy = 0;
				for (int col = 0; col < colors[row].length; col++) {
					if (colors[row][col] == null)
						continue;
					drawPoint(g, rW, rH, yy, row, xx, col);
					xx += rW;
				}
				yy += rH;
			}
		}
	}
	
	private void drawPoint(Graphics g, double rW, double rH, double yy, int row, double xx, int col) {
		double off = 0.5;
		double off2 = 1;
		if (rH > 4 && rW > 4) {
			off *= 2;
			off2 *= 2;
		}
		
		g.setColor(colors[row][col]);
		Rectangle2D rr = new Rectangle2D.Double(xx + off, yy + off, rW - off2, rH - off2);
		((Graphics2D) g).fill(rr);
	}
	
}
