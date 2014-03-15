package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import org.jfree.chart.plot.PlotOrientation;

/**
 * @author klukas
 */
public class MyColorGrid extends JComponent {
	private static final long serialVersionUID = 1L;
	
	Color[][] colors = null;
	Color[][] outline_colors = null;
	
	PlotOrientation plotOrientation;
	
	private final float outlineBorderWidth;
	
	public MyColorGrid(Color[][] colors, Color[][] outline_colors, PlotOrientation plotOrientation, float outlineBorderWidth) {
		this.colors = colors;
		this.outline_colors = outline_colors;
		this.plotOrientation = plotOrientation;
		this.outlineBorderWidth = outlineBorderWidth;
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
					if (colors[row][col] == null) {
						xx += rW;
						continue;
					}
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
					if (colors[row][col] == null) {
						xx += rW;
						continue;
					}
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
		
		if (outline_colors[row][col] != null) {
			Color c = outline_colors[row][col];
			if (c.getRed() + c.getGreen() + c.getBlue() > 0) {
				g.setColor(outline_colors[row][col]);
				((Graphics2D) g).setStroke(new BasicStroke(outlineBorderWidth));
				rr.setRect(rr.getX() + outlineBorderWidth / 2, rr.getY() + outlineBorderWidth / 2, rr.getWidth() - outlineBorderWidth / 2, rr.getHeight()
						- outlineBorderWidth / 2);
				((Graphics2D) g).draw(rr);
			}
		}
	}
	
}
