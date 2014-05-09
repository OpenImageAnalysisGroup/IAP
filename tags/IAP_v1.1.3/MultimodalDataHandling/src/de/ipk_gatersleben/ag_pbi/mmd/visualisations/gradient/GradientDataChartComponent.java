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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org1_0_13.jfree.chart.ChartPanel;
import org1_0_13.jfree.chart.JFreeChart;
import org1_0_13.jfree.chart.axis.Axis;
import org1_0_13.jfree.chart.axis.LogarithmicAxis;
import org1_0_13.jfree.chart.axis.NumberAxis;
import org1_0_13.jfree.chart.axis.NumberTickUnit;
import org1_0_13.jfree.chart.plot.DatasetRenderingOrder;
import org1_0_13.jfree.chart.plot.PlotOrientation;
import org1_0_13.jfree.chart.plot.XYPlot;
import org1_0_13.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org1_0_13.jfree.chart.renderer.xy.XYBarRenderer;
import org1_0_13.jfree.chart.renderer.xy.XYErrorRenderer;
import org1_0_13.jfree.data.Range;
import org1_0_13.jfree.data.xy.IntervalXYDataset;
import org1_0_13.jfree.data.xy.XYBarDataset;
import org1_0_13.jfree.data.xy.YIntervalSeries;
import org1_0_13.jfree.data.xy.YIntervalSeriesCollection;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors.ChartColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.ChartOptions;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

public class GradientDataChartComponent extends JComponent {
	
	private static final long serialVersionUID = 1L;
	public static final String GRADIENT_PATH = "gradientcharting";
	
	public GradientDataChartComponent(GraphElement ge, GradientCharts cc) {
		if (ge == null || ge.getGraph() == null)
			return;
		
		ChartOptions co = new ChartOptions();
		
		ExperimentInterface experiment = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge);
		if (experiment == null)
			return;
		
		Integer mappedDataListSize = experiment.size();
		
		int maxInRow = co.setLayoutOfChartComponent(ge, this, mappedDataListSize);
		
		co.readAttributes(ge);
		
		int currentXposition = 0;
		int currentYposition = 2;
		
		int idx = 0;
		for (int index = 0; index < mappedDataListSize; index++) {
			
			SubstanceInterface xmldata = experiment.get(index);
			
			if (currentXposition >= maxInRow) {
				currentYposition++;
				currentXposition = 0;
			}
			currentXposition++;
			
			IntervalXYDataset dataset = createDataSet(xmldata, co);
			
			idx++;
			String chartTitle = XmlDataChartComponent.getChartTitle(ge, idx);
			
			co.setFurtherOptions(ge.getGraph(), chartTitle);
			
			JFreeChart chart = createChart(cc, dataset, co);
			
			if (chart == null) {
				// empty
			} else {
				JFreeChart jfChart = chart;
				add(prettifyChart(ge, co, jfChart, idx), currentXposition + "," + currentYposition);
			}
		}
		synchronized (getTreeLock()) {
			if (isShowing())
				validate();
			else
				validateTree();
		}
	}
	
	private JFreeChart createChart(GradientCharts gc, IntervalXYDataset dataset, ChartOptions co) {
		MyNumberAxis numberaxis = new MyNumberAxis(co.domainAxis);
		
		NumberAxis numberaxis1 = new NumberAxis(co.rangeAxis);
		XYPlot xyplot = null;
		JFreeChart jfreechart = null;
		
		switch (gc) {
			case LINEGRADIENT:
				MyXYErrorRenderer xyerrorrenderer = new MyXYErrorRenderer();
				xyerrorrenderer.setBaseLinesVisible(co.showLines);
				xyerrorrenderer.setBaseShapesVisible(co.showShapes);
				xyerrorrenderer.setDrawYError(co.showStdDevAsT);
				xyerrorrenderer.setCapLength(co.stdDevTopWidth);
				xyerrorrenderer.setDrawStdDevAsFillRange(co.showStdDevAsFillRange);
				
				xyplot = new XYPlot(dataset, numberaxis, numberaxis1, xyerrorrenderer);
				
				if (co.stdDevLineWidth >= 0 && co.showStdDevAsT) {
					xyerrorrenderer.setErrorStroke(new BasicStroke(co.stdDevLineWidth));
				} else {
					xyerrorrenderer.setErrorPaint(new Color(0, 0, 0, 0));
				}
				
				// set the line width
				for (int i = 0; i < dataset.getSeriesCount(); i++)
					xyerrorrenderer.setSeriesStroke(i, new BasicStroke(co.outlineBorderWidth));
				
				break;
			case BARGRADIENT:
				XYBarRenderer xybarrenderer = new XYBarRenderer();
				xybarrenderer.setShadowVisible(false);
				
				xyplot = new XYPlot(dataset, numberaxis, numberaxis1, xybarrenderer);
				
				// this is an linechart6 attribute and barcharts have usually an errorbar
				XYErrorRenderer xyerrorrenderer2 = new XYErrorRenderer();
				xyerrorrenderer2.setDrawYError(true);
				xyerrorrenderer2.setDrawXError(false);
				
				xyerrorrenderer2.setBaseSeriesVisibleInLegend(false);
				xyerrorrenderer2.setCapLength(co.stdDevTopWidth);
				xyerrorrenderer2.setBaseShapesVisible(false);
				
				xyplot.setRenderer(1, xyerrorrenderer2);
				xyplot.setDataset(1, dataset);
				
				Range bounds = xyplot.getRenderer(1).findRangeBounds(dataset);
				if (bounds != null) {
					if (!co.showOnlyHalfErrorBar) {
						xyplot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
						// fit the RangeAxis to the errorbars
						xyplot.getRangeAxis().setLowerBound(bounds.getLowerBound());
					}
					
					// fit the RangeAxis to the errorbars
					xyplot.getRangeAxis().setUpperBound(bounds.getUpperBound());
				}
				if (co.stdDevLineWidth >= 0) {
					xyerrorrenderer2.setErrorStroke(new BasicStroke(co.stdDevLineWidth));
				}
				
				dataset = new XYBarDataset(dataset, calculateBarWidth(dataset));
				xyplot.setDataset(dataset);
				
				break;
		}
		
		jfreechart = new JFreeChart(null, xyplot);
		jfreechart.setAntiAlias(true);
		jfreechart.setTextAntiAlias(true);
		/************************************/
		
		// plot orientation
		if (co.orientation.equals(org.jfree.chart.plot.PlotOrientation.HORIZONTAL)) {
			xyplot.setOrientation(PlotOrientation.HORIZONTAL);
		} else {
			xyplot.setOrientation(PlotOrientation.VERTICAL);
			numberaxis.setTickLabelAngle(-co.axisRotation * Math.PI / 180);
		}
		
		// chart background color
		if (NodeTools.getCategoryBackgroundColorB(co.ge.getGraph(), Color.WHITE).equals(Color.BLACK) && xyplot.getBackgroundPaint().getTransparency() == 1.0f) {
			xyplot.setBackgroundAlpha(0.0f);
		} else {
			xyplot.setBackgroundPaint(NodeTools.getCategoryBackgroundColorB(co.ge.getGraph(), Color.WHITE));
		}
		
		if (co.useLogYscale)
			xyplot.setRangeAxis(new LogarithmicAxis(co.rangeAxis));
		
		// specify ticks of x (domain) and y (range) axis
		if (co.useCustomRangeSteps) {
			NumberAxis na = (NumberAxis) xyplot.getRangeAxis();
			NumberTickUnit unit = new NumberTickUnit(co.customRangeSteps);
			na.setTickUnit(unit, false, true);
		}
		
		boolean useCustomDomainSteps = ((Boolean) AttributeHelper.getAttributeValue(
				co.ge, "charting", "useCustomDomainSteps", new Boolean(false), new Boolean(false))).booleanValue();
		
		double customStepSize = (Double) AttributeHelper.getAttributeValue(
				co.ge, "charting", "customDomainStepSize", new Double(300d), new Double(300d));
		
		if (useCustomDomainSteps) {
			NumberAxis na = (NumberAxis) xyplot.getDomainAxis();
			NumberTickUnit unit = new NumberTickUnit(customStepSize);
			na.setTickUnit(unit, false, true);
		}
		
		// specify lower and upper bounds
		boolean useCustomRange = ((Boolean) AttributeHelper.getAttributeValue(
				co.ge, "charting", "useCustomDomainBounds", new Boolean(false), new Boolean(false))).booleanValue();
		
		double lowerBound = (Double) AttributeHelper.getAttributeValue(
				co.ge, "charting", "minBoundDomain", new Double(0d), new Double(0d));
		double upperBound = (Double) AttributeHelper.getAttributeValue(
				co.ge, "charting", "maxBoundDomain", new Double(100d), new Double(100d));
		
		if (!useCustomRange) {
			lowerBound = Double.NaN;
			upperBound = Double.NaN;
		}
		
		if (!Double.isNaN(lowerBound))
			xyplot.getDomainAxis().setLowerBound(lowerBound);
		if (!Double.isNaN(upperBound))
			xyplot.getDomainAxis().setUpperBound(upperBound);
		
		if (!Double.isNaN(co.lowerBound)) {
			xyplot.getRangeAxis().setLowerBound(co.lowerBound);
		}
		if (!Double.isNaN(co.upperBound))
			xyplot.getRangeAxis().setUpperBound(co.upperBound);
		
		AbstractXYItemRenderer renderer = null;
		
		if (xyplot.getRenderer() instanceof XYBarRenderer) {
			renderer = (XYBarRenderer) xyplot.getRenderer();
		}
		
		if (xyplot.getRenderer() instanceof XYErrorRenderer) {
			renderer = (XYErrorRenderer) xyplot.getRenderer();
		}
		
		renderer.setBaseSeriesVisibleInLegend(co.showLegend);
		
		if (co.outlineBorderWidth > 0) {
			for (int i = 0; i < dataset.getSeriesCount(); i++)
				renderer.setSeriesOutlineStroke(i, new BasicStroke(co.outlineBorderWidth));
		}
		if (co.shapeSize > 0) {
			Rectangle2D rect = new Rectangle2D.Double(-co.shapeSize / 4, -co.shapeSize / 4, co.shapeSize / 2, co.shapeSize / 2);
			if (renderer instanceof XYBarRenderer) {
				for (int i = 0; i < dataset.getSeriesCount(); i++)
					xyplot.getRenderer(1).setSeriesShape(i, rect);
			}
			if (renderer instanceof XYErrorRenderer) {
				for (int i = 0; i < dataset.getSeriesCount(); i++)
					renderer.setSeriesShape(i, rect);
			}
		}
		
		xyplot.getRangeAxis().setVisible(co.showRangeAxis);
		xyplot.getDomainAxis().setVisible(co.showCategoryAxis);
		
		ChartColorAttribute chartColorAttribute = (ChartColorAttribute) AttributeHelper.getAttributeValue(
				co.graph, ChartColorAttribute.attributeFolder,
				ChartColorAttribute.attributeName,
				new ChartColorAttribute(), new ChartColorAttribute());
		
		// set the color series
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < dataset.getSeriesCount(); i++)
			names.add((dataset.getSeriesKey(i)).toString());
		
		if (names.size() > 0) {
			ArrayList<Color> colors1 = chartColorAttribute.getSeriesColors(names);
			ArrayList<Color> colors2 = chartColorAttribute.getSeriesOutlineColors(names);
			
			int i = 0;
			for (Color c1 : colors1)
				renderer.setSeriesPaint(i++, c1);
			
			i = 0;
			for (Color c2 : colors2)
				if (xyplot.getRenderer() instanceof XYBarRenderer) {
					((XYBarRenderer) renderer).setDrawBarOutline(true);//
					((XYErrorRenderer) xyplot.getRenderer(1)).setSeriesPaint(i++, c2);
				}
		}
		
		jfreechart.setBackgroundPaint(null);
		
		renderer.getPlot().setDomainGridlinesVisible(co.showGridCategory);
		renderer.getPlot().setRangeGridlinesVisible(co.showGridRange);
		renderer.getPlot().setOutlinePaint(null);
		
		if (co.showGridCategory)
			renderer.getPlot().setDomainGridlineStroke(new BasicStroke((float) co.gridWidth));
		if (co.showGridRange)
			renderer.getPlot().setRangeGridlineStroke(new BasicStroke((float) co.gridWidth));
		
		if (co.axisWidth >= 0) {
			renderer.getPlot().getRangeAxis().setAxisLineStroke(new BasicStroke((float) co.axisWidth));
			renderer.getPlot().getDomainAxis().setAxisLineStroke(
					new BasicStroke((float) co.axisWidth));
		}
		if (co.gridColor != null) {
			renderer.getPlot().setRangeGridlinePaint(co.gridColor);
			renderer.getPlot().setDomainGridlinePaint(co.gridColor);
		}
		if (co.axisColor != null) {
			renderer.getPlot().getRangeAxis().setAxisLinePaint(co.axisColor);
			renderer.getPlot().getDomainAxis().setAxisLinePaint(co.axisColor);
		}
		if (co.axisFontSize > 0) {
			Font af = new Font(Axis.DEFAULT_AXIS_LABEL_FONT.getFontName(),
					Axis.DEFAULT_AXIS_LABEL_FONT.getStyle(), co.axisFontSize);
			renderer.getPlot().getRangeAxis().setTickLabelFont(af);
			renderer.getPlot().getDomainAxis().setTickLabelFont(af);
			renderer.getPlot().getDomainAxis().setLabelFont(af);
			renderer.getPlot().getRangeAxis().setLabelFont(af);
		}
		
		return jfreechart;
	}
	
	/**
	 * Fixes the barwidth problem of jfreechart by calculating the position bandwidth
	 * for each bar (based only on the first series!)
	 */
	private double calculateBarWidth(IntervalXYDataset dataset) {
		if (dataset.getSeriesCount() == 0 || dataset.getItemCount(0) == 0)
			return 0.7;
		
		double smallestPositionDistance = Double.POSITIVE_INFINITY;
		for (int i = 1; i < dataset.getItemCount(0); i++) {
			double mt = dataset.getXValue(0, i) - dataset.getXValue(0, i - 1);
			if (mt < smallestPositionDistance)
				smallestPositionDistance = mt;
		}
		
		return 0.7 * smallestPositionDistance;
	}
	
	private IntervalXYDataset createDataSet(SubstanceInterface xmldata, ChartOptions co) {
		
		YIntervalSeriesCollection dataset = new YIntervalSeriesCollection();
		
		LinkedHashMap<String, ArrayList<NumericMeasurementInterface>> name2measurement = new LinkedHashMap<String, ArrayList<NumericMeasurementInterface>>();
		
		for (NumericMeasurementInterface m : Substance3D.getAllFiles(new Experiment(xmldata))) {
			SampleInterface s = m.getParentSample();
			String name = s.getParentCondition().getExpAndConditionName() + ", " + ((Sample3D) s).getName();
			if (!name2measurement.containsKey(name))
				name2measurement.put(name, new ArrayList<NumericMeasurementInterface>());
			name2measurement.get(name).add(m);
			co.rangeAxis = (co.rangeAxis != null && co.rangeAxis.equals("[unit]")) ?
					m.getUnit() : co.rangeAxis;
			co.domainAxis = co.domainAxis != null && co.domainAxis.equals("[unit]") ?
					((NumericMeasurement3D) m).getPositionUnit() : co.domainAxis;
		}
		
		for (String name : name2measurement.keySet()) {
			YIntervalSeries gradientvalues = new YIntervalSeries(name);
			ArrayList<NumericMeasurementInterface> measurements = name2measurement.get(name);
			if (measurements != null && measurements.size() > 0) {
				// calculate on the fly the mean value by putting together
				// measurements with the same position but different replicateID
				HashMap<Double, ArrayList<NumericMeasurementInterface>> position2measurement = new HashMap<Double, ArrayList<NumericMeasurementInterface>>();
				
				for (NumericMeasurementInterface m : measurements) {
					Double position = ((NumericMeasurement3D) m).getPosition();
					if (position != null) {
						if (!position2measurement.containsKey(position))
							position2measurement.put(position, new ArrayList<NumericMeasurementInterface>());
						position2measurement.get(position).add(m);
					}
				}
				for (Double pos : position2measurement.keySet()) {
					double sum = 0;
					int cnt = 0;
					for (NumericMeasurementInterface m : position2measurement.get(pos)) {
						sum += m.getValue();
						cnt++;
					}
					if (cnt != 0) {
						double mean = (1d * sum) / (1d * cnt);
						double stddev = 0d;
						for (NumericMeasurementInterface m : position2measurement.get(pos))
							stddev += Math.pow(m.getValue() - mean, 2);
						stddev = Math.sqrt(stddev);
						if (stddev < 0)
							stddev = 0;
						gradientvalues.add(pos * 1d, mean, mean - stddev, mean + stddev);
					}
				}
				
			}
			
			dataset.addSeries(gradientvalues);
		}
		
		return dataset;
	}
	
	public static double getResolution(Node nd) {
		try {
			return (Double) AttributeHelper.getAttributeValue(nd, "graphics", "chartresolution", 1d, 1d);
		} catch (AttributeNotFoundException e) {
			return 2d;
		}
	}
	
	public static void setResolution(Node nd, Double resolutionmultiplikator) {
		AttributeHelper.setAttribute(nd, "graphics", "chartresolution", resolutionmultiplikator);
	}
	
	public static JPanel prettifyChart(org.graffiti.graph.GraphElement ge,
			ChartOptions co, JFreeChart jfChart, int idx) {
		if (jfChart.getTitle() != null) {
			jfChart.getTitle().setFont(NodeTools.getChartTitleFont(ge));
			jfChart.getTitle().setPaint(NodeTools.getChartTitleColor(ge));
		}
		
		ChartPanel jfreechartPanel = new ChartPanel(jfChart, false);
		
		JPanel chartPanel = jfreechartPanel;
		Color cbc = NodeTools.getChartBackgroundColor(ge, idx);
		
		if (cbc != null) {
			chartPanel.setOpaque(true);
			chartPanel.setBackground(cbc);
		} else {
			chartPanel.setOpaque(false);
			chartPanel.setBackground(null);
		}
		
		if (chartPanel instanceof ChartPanel) {
			ChartPanel cp = (ChartPanel) chartPanel;
			cp.setMinimumDrawHeight(300);
			cp.setMinimumDrawWidth(330);
		}
		if (co.borderHor > 0)
			chartPanel.setBorder(BorderFactory.createEmptyBorder(1, 0, 3, 0));
		return chartPanel;
	}
	
}