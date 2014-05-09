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

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org1_0_13.jfree.chart.axis.AxisState;
import org1_0_13.jfree.chart.axis.NumberAxis;
import org1_0_13.jfree.chart.axis.TickType;
import org1_0_13.jfree.chart.axis.ValueTick;
import org1_0_16.jfree.text.TextUtilities;
import org1_0_16.jfree.ui.RectangleEdge;
import org1_0_16.jfree.ui.TextAnchor;

public class MyNumberAxis extends NumberAxis {
	
	private static final long serialVersionUID = 1L;
	private double angle = 0.0;
	private TextAnchor rotationanchor = TextAnchor.CENTER;
	
	public MyNumberAxis(String label) {
		super(label);
		MyNumberAxis.createStandardTickUnits();
	}
	
	@Override
	protected AxisState drawTickMarksAndLabels(Graphics2D g2, double cursor,
						Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge) {
		
		AxisState state = new AxisState(cursor);
		
		if (isAxisLineVisible()) {
			drawAxisLine(g2, cursor, dataArea, edge);
		}
		
		List<?> ticks = refreshTicks(g2, state, dataArea, edge);
		state.setTicks(ticks);
		g2.setFont(getTickLabelFont());
		Iterator<?> iterator = ticks.iterator();
		while (iterator.hasNext()) {
			ValueTick tick = (ValueTick) iterator.next();
			if (isTickLabelsVisible()) {
				g2.setPaint(getTickLabelPaint());
				float[] anchorPoint = calculateAnchorPoint(tick, cursor,
									dataArea, edge);
				
				TextUtilities.drawRotatedString(tick.getText(), g2,
									anchorPoint[0], anchorPoint[1], tick.getTextAnchor(),
									this.angle, this.rotationanchor);
			}
			
			if ((isTickMarksVisible() && tick.getTickType().equals(
								TickType.MAJOR)) || (isMinorTickMarksVisible()
								&& tick.getTickType().equals(TickType.MINOR))) {
				
				double ol = (tick.getTickType().equals(TickType.MINOR)) ?
									getMinorTickMarkOutsideLength() : getTickMarkOutsideLength();
				
				double il = (tick.getTickType().equals(TickType.MINOR)) ?
									getMinorTickMarkInsideLength() : getTickMarkInsideLength();
				
				float xx = (float) valueToJava2D(tick.getValue(), dataArea,
										edge);
				Line2D mark = null;
				g2.setStroke(getTickMarkStroke());
				g2.setPaint(getTickMarkPaint());
				if (edge == RectangleEdge.LEFT) {
					mark = new Line2D.Double(cursor - ol, xx, cursor + il, xx);
				} else
					if (edge == RectangleEdge.RIGHT) {
						mark = new Line2D.Double(cursor + ol, xx, cursor - il, xx);
					} else
						if (edge == RectangleEdge.TOP) {
							mark = new Line2D.Double(xx, cursor - ol, xx, cursor + il);
						} else
							if (edge == RectangleEdge.BOTTOM) {
								mark = new Line2D.Double(xx, cursor + ol, xx, cursor - il);
							}
				g2.draw(mark);
			}
		}
		
		// need to work out the space used by the tick labels...
		// so we can update the cursor...
		double used = 0.0;
		if (isTickLabelsVisible()) {
			if (edge == RectangleEdge.LEFT) {
				used += findMaximumTickLabelWidth(ticks, g2, plotArea,
									isVerticalTickLabels());
				state.cursorLeft(used);
			} else
				if (edge == RectangleEdge.RIGHT) {
					used = findMaximumTickLabelWidth(ticks, g2, plotArea,
										isVerticalTickLabels());
					state.cursorRight(used);
				} else
					if (edge == RectangleEdge.TOP) {
						used = findMaximumTickLabelHeight(ticks, g2, plotArea,
											isVerticalTickLabels());
						state.cursorUp(used);
					} else
						if (edge == RectangleEdge.BOTTOM) {
							used = findMaximumTickLabelHeight(ticks, g2, plotArea,
												isVerticalTickLabels());
							state.cursorDown(used);
						}
		}
		
		return state;
	}
	
	public void setTickLabelAngle(double angle) {
		this.angle = angle;
	}
	
	public double getTickLabelAngle() {
		return this.angle;
	}
	
	public void setTickLabelRotationAnchor(TextAnchor rotationanchor) {
		this.rotationanchor = rotationanchor;
	}
	
	public TextAnchor getRotationAnchor() {
		return this.rotationanchor;
	}
}
