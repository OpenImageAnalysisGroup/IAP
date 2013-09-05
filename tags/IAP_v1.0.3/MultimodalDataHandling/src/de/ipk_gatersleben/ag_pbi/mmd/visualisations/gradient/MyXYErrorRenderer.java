/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.visualisations.gradient;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org1_0_13.jfree.chart.axis.ValueAxis;
import org1_0_13.jfree.chart.plot.CrosshairState;
import org1_0_13.jfree.chart.plot.PlotOrientation;
import org1_0_13.jfree.chart.plot.PlotRenderingInfo;
import org1_0_13.jfree.chart.plot.XYPlot;
import org1_0_13.jfree.chart.renderer.xy.XYErrorRenderer;
import org1_0_13.jfree.chart.renderer.xy.XYItemRendererState;
import org1_0_13.jfree.data.xy.IntervalXYDataset;
import org1_0_13.jfree.data.xy.XYDataset;

public class MyXYErrorRenderer extends XYErrorRenderer {
	
	private static final long serialVersionUID = 1L;
	private boolean showStdDevAsFilLRange = false;
	private int transparency = 30;
	
	public void setDrawStdDevAsFillRange(boolean showStdDevAsFilLRange) {
		this.showStdDevAsFilLRange = showStdDevAsFilLRange;
	}
	
	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state,
						Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
						ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
						int series, int item, CrosshairState crosshairState, int pass) {
		super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset,
							series, item, crosshairState, pass);
		
		if (showStdDevAsFilLRange) {
			if (!(dataset instanceof IntervalXYDataset)) {
				return;
			}
			
			IntervalXYDataset ixydataset = (IntervalXYDataset) dataset;
			
			if (getItemPaint(series, item) != null) {
				Color tColor = new Color(((Color) getItemPaint(series, item)).getRed(), ((Color) getItemPaint(series, item)).getGreen()
									, ((Color) getItemPaint(series, item)).getBlue(), transparency);
				g2.setPaint(tColor);
			}
			if (getItemStroke(series, item) != null)
				g2.setStroke(getItemStroke(series, item));
			
			Line2D lineA = null;
			Line2D lineB = null;
			
			double x0 = 0;
			double yStart0 = 0;
			double yEnd0 = 0;
			double x1 = 0;
			double yStart1 = 0;
			double yEnd1 = 0;
			
			GeneralPath gp = new GeneralPath();
			lineA = new Line2D.Double();
			lineB = new Line2D.Double();
			
			if (item > 0) {
				x0 = domainAxis.valueToJava2D(ixydataset.getXValue(series, item - 1), dataArea, plot.getDomainAxisEdge());
				yEnd0 = rangeAxis.valueToJava2D(ixydataset.getEndYValue(series, item - 1), dataArea, plot.getRangeAxisEdge());
				yStart0 = rangeAxis.valueToJava2D(ixydataset.getStartYValue(series, item - 1), dataArea, plot.getRangeAxisEdge());
				x1 = domainAxis.valueToJava2D(ixydataset.getXValue(series, item), dataArea, plot.getDomainAxisEdge());
				yEnd1 = rangeAxis.valueToJava2D(ixydataset.getEndYValue(series, item), dataArea, plot.getRangeAxisEdge());
				yStart1 = rangeAxis.valueToJava2D(ixydataset.getStartYValue(series, item), dataArea, plot.getRangeAxisEdge());
			}
			
			if (plot.getOrientation() == PlotOrientation.VERTICAL) {
				lineA = new Line2D.Double(x0, yEnd0, x1, yEnd1);
				lineB = new Line2D.Double(x0, yStart0, x1, yStart1);
			}
			if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
				lineA = new Line2D.Double(yEnd0, x0, yEnd1, x1);
				lineB = new Line2D.Double(yStart0, x0, yStart1, x1);
			}
			
			gp.moveTo((float) lineA.getX1(), (float) lineA.getY1());
			gp.lineTo((float) lineA.getX2(), (float) lineA.getY2());
			gp.lineTo((float) lineB.getX2(), (float) lineB.getY2());
			gp.lineTo((float) lineB.getX1(), (float) lineB.getY1());
			
			gp.closePath();
			g2.fill(gp);
		}
	}
	
	public void setErrorFillRangeTransparency(int transparency) {
		this.transparency = transparency;
	}
	
	public int getErrorFillRangeTransparency() {
		return this.transparency;
	}
	
}