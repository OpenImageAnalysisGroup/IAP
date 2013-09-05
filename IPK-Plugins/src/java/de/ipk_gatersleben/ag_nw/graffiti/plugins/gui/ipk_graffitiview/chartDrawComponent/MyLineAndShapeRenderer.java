/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 26.04.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.BioStatisticalCategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.CategoryItemRendererState;
import org.jfree.chart.renderer.LineAndShapeRenderer;
import org.jfree.data.CategoryDataset;

public class MyLineAndShapeRenderer extends LineAndShapeRenderer {
	
	private static final long serialVersionUID = 1L;
	
	boolean showStdDevAsVerticalT = false;
	
	boolean showStdDevAsFilLRange = false;
	
	boolean connectPriorMissing = false;
	
	float stdDevLineWidth = 4f;
	double stdDevTopWidth = 2;
	
	Stroke stdDevStroke = new BasicStroke(stdDevLineWidth);
	
	public void setDrawStdDev(boolean showStdDevAsVerticalT,
						boolean showStdDevAsFilLRange) {
		this.showStdDevAsVerticalT = showStdDevAsVerticalT;
		this.showStdDevAsFilLRange = showStdDevAsFilLRange;
	}
	
	public void setConnectPriorItems(boolean value) {
		this.connectPriorMissing = value;
	}
	
	public void setStdDevLineWidth(float stdDevLineWidth) {
		this.stdDevLineWidth = stdDevLineWidth;
		stdDevStroke = new BasicStroke(stdDevLineWidth);
	}
	
	public void setStdDevTopWidth(double stdDevTopWidth) {
		this.stdDevTopWidth = stdDevTopWidth;
	}
	
	@Override
	public void drawItem(Graphics2D g2, CategoryItemRendererState state,
						Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
						ValueAxis rangeAxis, CategoryDataset dataset, int row, int column) {
		
		if (!showStdDevAsFilLRange)
			defaultDrawItem(g2, state, dataArea, plot, domainAxis, rangeAxis,
								dataset, row, column, connectPriorMissing);
		
		if (showStdDevAsVerticalT || showStdDevAsFilLRange) {
			// nothing is drawn for null...
			Number v = dataset.getValue(row, column);
			if (v == null) {
				return;
			}
			if (!(dataset instanceof BioStatisticalCategoryDataset)) {
				return;
			}
			
			BioStatisticalCategoryDataset bioDataset = (BioStatisticalCategoryDataset) dataset;
			
			boolean stdDevAvail = true;
			
			Number stdDev = bioDataset.getStdDevValue(row, column);
			if (stdDev == null || Double.isNaN(stdDev.doubleValue())) {
				stdDev = 0;
				stdDevAvail = false;
			}
			
			PlotOrientation orientation = plot.getOrientation();
			
			// current data point...
			double x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
								dataArea, plot.getDomainAxisEdge());
			double value = v.doubleValue();
			double y1 = rangeAxis.valueToJava2D(value, dataArea, plot
								.getRangeAxisEdge());
			
			double stdDevValue = stdDev.doubleValue();
			double yStdDev1 = rangeAxis.valueToJava2D(value + stdDevValue,
								dataArea, plot.getRangeAxisEdge())
								- y1;
			
			if (showStdDevAsVerticalT && stdDevAvail) {
				if (getItemShapeFilled(row, column)) {
					g2.setPaint(getItemPaint(row, column));
					// g2.fill(shape);
				} else {
					if (useFillPaintForShapeOutline) {
						g2.setPaint(getItemPaint(row, column));
					} else {
						g2.setPaint(getItemOutlinePaint(row, column));
					}
				}
				g2.setStroke(stdDevStroke);
				Line2D a = null;
				Line2D b = null;
				Line2D c = null;
				if (orientation == PlotOrientation.HORIZONTAL) {
					a = new Line2D.Double(y1 - yStdDev1, x1, y1 + yStdDev1, x1);
					
					b = new Line2D.Double(y1 - yStdDev1, x1 - stdDevTopWidth,
										y1 - yStdDev1, x1 + stdDevTopWidth);
					c = new Line2D.Double(y1 + yStdDev1, x1 - stdDevTopWidth,
										y1 + yStdDev1, x1 + stdDevTopWidth);
				} else
					if (orientation == PlotOrientation.VERTICAL) {
						a = new Line2D.Double(x1, y1 - yStdDev1, x1, y1 + yStdDev1);
						b = new Line2D.Double(x1 - stdDevTopWidth, y1 - yStdDev1,
											x1 + stdDevTopWidth, y1 - yStdDev1);
						c = new Line2D.Double(x1 - stdDevTopWidth,
											y1 + yStdDev1,
											x1 + stdDevTopWidth,
											y1 + yStdDev1);
					}
				g2.draw(a);
				g2.draw(b);
				g2.draw(c);
			}
			if (showStdDevAsFilLRange && column != 0) {
				int offset = 0;
				Number previousValue;
				Number previousStdDevValue;
				Color tColor;
				do {
					previousValue = dataset.getValue(row, column - 1 - offset);
					previousStdDevValue = bioDataset.getStdDevValue(row, column
										- 1 - offset);
					if (previousStdDevValue == null || Double.isNaN(previousStdDevValue.doubleValue()))
						previousStdDevValue = 0;
					tColor = (Color) getItemPaint(row, column);
					tColor = new Color(tColor.getRed(), tColor.getGreen(),
										tColor.getBlue(), 64);
					offset++;
				} while ((previousValue == null || Double.isNaN(previousValue.doubleValue())) && connectPriorMissing
									&& column - 1 - offset >= 0);
				offset--;
				g2.setPaint(tColor);
				g2.setStroke(getItemStroke(row, column));
				if (previousValue != null && previousStdDevValue != null && (previousStdDevValue.doubleValue() > 0 || stdDevValue > 0)) {
					// previous data point...
					double previous = previousValue.doubleValue();
					double previousStdDev = previousStdDevValue.doubleValue();
					double x0 = domainAxis.getCategoryMiddle(column - 1 - offset,
										getColumnCount(), dataArea, plot
															.getDomainAxisEdge());
					double y0 = rangeAxis.valueToJava2D(previous, dataArea,
										plot.getRangeAxisEdge());
					
					double yStdDev0 = rangeAxis
										.valueToJava2D(previous + previousStdDev, dataArea,
															plot.getRangeAxisEdge())
										- y0;
					
					Line2D lineA = null;
					Line2D lineB = null;
					if (orientation == PlotOrientation.HORIZONTAL) {
						lineA = new Line2D.Double(y0 + yStdDev0, x0, y1
											+ yStdDev1, x1);
						lineB = new Line2D.Double(y0 - yStdDev0, x0, y1
											- yStdDev1, x1);
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							lineA = new Line2D.Double(x0, y0 + yStdDev0, x1, y1
												+ yStdDev1);
							lineB = new Line2D.Double(x0, y0 - yStdDev0, x1, y1
												- yStdDev1);
						}
					GeneralPath gp = new GeneralPath();
					gp.moveTo((float) lineA.getX1(), (float) lineA.getY1());
					gp.lineTo((float) lineA.getX2(), (float) lineA.getY2());
					gp.lineTo((float) lineB.getX2(), (float) lineB.getY2());
					gp.lineTo((float) lineB.getX1(), (float) lineB.getY1());
					gp.closePath();
					// g2.draw(lineA);
					// g2.draw(lineB);
					g2.fill(gp);
				} else {
					int nextOffset = 0;
					Number nextValue = null;
					if (column + 1 < dataset.getColumnCount()) {
						do {
							nextValue = dataset.getValue(row, column + 1 + nextOffset);
							nextOffset++;
						} while ((nextValue == null || Double.isNaN(nextValue.doubleValue())) && connectPriorMissing
											&& column + 1 + nextOffset < dataset.getColumnCount());
						nextOffset--;
					} else
						nextValue = null;
					if (nextValue == null
										|| column + 1 + nextOffset == dataset.getColumnCount()) {
						Line2D lineA = null;
						if (orientation == PlotOrientation.HORIZONTAL) {
							lineA = new Line2D.Double(y1 - yStdDev1, x1, y1
												+ yStdDev1, x1);
						} else
							if (orientation == PlotOrientation.VERTICAL) {
								lineA = new Line2D.Double(x1, y1 - yStdDev1, x1, y1
													+ yStdDev1);
							}
						g2.draw(lineA);
					}
				}
			}
		}
		if (showStdDevAsFilLRange)
			defaultDrawItem(g2, state, dataArea, plot, domainAxis, rangeAxis,
								dataset, row, column, connectPriorMissing);
	}
	
	public void defaultDrawItem(Graphics2D g2, CategoryItemRendererState state,
						Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
						ValueAxis rangeAxis, CategoryDataset dataset, int row, int column, boolean connectPrior) {
		
		// nothing is drawn for null...
		Number v = dataset.getValue(row, column);
		if (v == null) {
			return;
		}
		
		PlotOrientation orientation = plot.getOrientation();
		
		// current data point...
		double x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
							dataArea, plot.getDomainAxisEdge());
		double value = v.doubleValue();
		double y1 = rangeAxis.valueToJava2D(value, dataArea, plot
							.getRangeAxisEdge());
		
		Shape shape = getItemShape(row, column);
		if (orientation == PlotOrientation.HORIZONTAL) {
			shape = createTransformedShape(shape, y1, x1);
		} else
			if (orientation == PlotOrientation.VERTICAL) {
				shape = createTransformedShape(shape, x1, y1);
			}
		if (isDrawShapes()) {
			
			if (getItemShapeFilled(row, column)) {
				g2.setPaint(getItemPaint(row, column));
				g2.fill(shape);
			} else {
				if (this.useFillPaintForShapeOutline) {
					g2.setPaint(getItemPaint(row, column));
				} else {
					g2.setPaint(getItemOutlinePaint(row, column));
				}
				g2.setStroke(getItemOutlineStroke(row, column));
				g2.draw(shape);
			}
		}
		
		if (isDrawLines()) {
			if (column != 0) {
				
				Number previousValue;
				int offset = 0;
				do {
					previousValue = dataset.getValue(row, column - 1 - offset);
					offset++;
				} while ((previousValue == null || Double.isNaN(previousValue.doubleValue())) && connectPriorMissing && (column - 1 - offset >= 0));
				offset--;
				// if (previousValue==null)
				// System.out.println("No value for column "+column+" offset="+offset);
				if (previousValue != null) {
					
					// previous data point...
					double previous = previousValue.doubleValue();
					double x0 = domainAxis.getCategoryMiddle(column - 1 - offset,
										getColumnCount(), dataArea, plot
															.getDomainAxisEdge());
					double y0 = rangeAxis.valueToJava2D(previous, dataArea,
										plot.getRangeAxisEdge());
					
					Line2D line = null;
					if (orientation == PlotOrientation.HORIZONTAL) {
						line = new Line2D.Double(y0, x0, y1, x1);
					} else
						if (orientation == PlotOrientation.VERTICAL) {
							line = new Line2D.Double(x0, y0, x1, y1);
						}
					g2.setPaint(getItemPaint(row, column));
					g2.setStroke(getItemStroke(row, column));
					g2.draw(line);
				}
			}
		}
		
		// draw the item label if there is one...
		if (isItemLabelVisible(row, column)) {
			drawItemLabel(g2, orientation, dataset, row, column, x1, y1,
								(value < 0.0));
		}
		
		// collect entity and tool tip information...
		if (state.getInfo() != null) {
			EntityCollection entities = state.getInfo().getOwner()
								.getEntityCollection();
			if (entities != null && shape != null) {
				String tip = null;
				CategoryToolTipGenerator tipster = getToolTipGenerator(row,
									column);
				if (tipster != null) {
					tip = tipster.generateToolTip(dataset, row, column);
				}
				String url = null;
				if (getItemURLGenerator(row, column) != null) {
					url = getItemURLGenerator(row, column).generateURL(dataset,
										row, column);
				}
				CategoryItemEntity entity = new CategoryItemEntity(shape, tip,
									url, dataset, row, dataset.getColumnKey(column), column);
				entities.addEntity(entity);
				
			}
			
		}
		
	}
}
