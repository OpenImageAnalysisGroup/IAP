/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.07.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.BioStatisticalCategoryDataset;
import org.ErrorMsg;
import org.graffiti.graph.Graph;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.Legend;
import org.jfree.chart.StandardLegend;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.BarRenderer;
import org.jfree.chart.renderer.BarRenderer3D;
import org.jfree.chart.renderer.CategoryItemRenderer;
import org.jfree.chart.renderer.StatisticalBarRenderer;
import org.jfree.ui.Spacer;
import org.jfree.util.TableOrder;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors.ChartColorAttribute;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.GraffitiCharts;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * @author klukas
 */
public class XmlDataChartComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	
	public XmlDataChartComponent(String preferredChartType, Graph graph, org.graffiti.graph.GraphElement ge) {
		
		if (ge == null || ge.getGraph() == null)
			return;
		
		ExperimentInterface experiment = Experiment2GraphHelper.getMappedDataListFromGraphElement(ge);
		if (experiment == null)
			return;
		
		initGUI(preferredChartType, graph, ge, experiment);
	}
	
	public XmlDataChartComponent(ExperimentInterface experiment, String preferredChartType, Graph graph, org.graffiti.graph.GraphElement ge) {
		
		if (ge == null || ge.getGraph() == null)
			return;
		if (experiment == null)
			return;
		
		initGUI(preferredChartType, graph, ge, experiment);
	}
	
	public void initGUI(String preferredChartType, Graph graph, org.graffiti.graph.GraphElement ge, ExperimentInterface experiment) {
		Integer mappedDataListSize = experiment.size();
		
		int currentXposition = 0;
		int currentYposition = 2;
		
		HeatMapOptions hmo = null;
		
		Integer maxInRow = null;
		
		int idx = 0;
		for (int index = 0; index < mappedDataListSize; index++) {
			ChartOptions co = new ChartOptions();
			
			if (maxInRow == null)
				maxInRow = co.setLayoutOfChartComponent(ge, this, mappedDataListSize);
			
			co.readAttributes(ge);
			
			SubstanceInterface xmldata = experiment.get(index);
			
			if (currentXposition >= maxInRow) {
				currentYposition++;
				currentXposition = 0;
			}
			currentXposition++;
			String chartType = preferredChartType;
			
			if (preferredChartType.equalsIgnoreCase(GraffitiCharts.AUTOMATIC.getName()))
				chartType = getAutoChartTypeFor(xmldata).getName();
			
			boolean alsoUsedForPlottingStdDev = chartType.equals(GraffitiCharts.BAR_FLAT.getName())
					|| (chartType.equals(GraffitiCharts.LINE.getName()) && (co.showStdDevAsT || co.showStdDevAsFillRange));
			
			if (hmo == null && chartType.equals(GraffitiCharts.HEATMAP.getName()))
				hmo = new HeatMapOptions(graph);
			
			boolean SalsoUsedForPlottingStdDev = alsoUsedForPlottingStdDev;
			boolean SshowOnlyHalfErrorBar = co.showOnlyHalfErrorBar && chartType.equals(GraffitiCharts.BAR_FLAT.getName());
			boolean SfillTimeGaps = co.fillTimeGaps && chartType.equals(GraffitiCharts.LINE.getName());
			boolean removeEmptyConditions = co.removeEmptyConditions;// && chartType.equals(GraffitiCharts.BAR_FLAT.getName());
			
			BioStatisticalCategoryDataset dataset = null;
			
			try {
				dataset = getDataset(xmldata, graph, SalsoUsedForPlottingStdDev, SshowOnlyHalfErrorBar, SfillTimeGaps, removeEmptyConditions);
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
				continue;
			}
			
			dataset.setDrawOnlyTopOfErrorBar(SshowOnlyHalfErrorBar);
			dataset.setErrorBarLen(co.stdDevTopWidth);
			
			co.rangeAxis = prettifyRangeAxisText(co.rangeAxis, dataset);
			
			co.domainAxis = prettifyDomainAxisText(co.domainAxis, dataset);
			
			idx++;
			String chartTitle = getChartTitle(ge, idx);
			
			co.setFurtherOptions(graph, dataset, chartTitle);
			
			Object chart = createChart(chartType, co, hmo);
			
			if (chart == null) {
				// emtpty
			} else {
				if (!(chart instanceof JComponent)) {
					JFreeChart jfChart = (JFreeChart) chart;
					if (co.enableUI) {
						final ChartPanel cp = (ChartPanel) prettifyChart(ge, co, chartType, jfChart);
						for (MouseListener ml : cp.getMouseListeners())
							cp.removeMouseListener(ml);
						cp.addMouseListener(new MouseListener() {
							
							@Override
							public void mouseReleased(MouseEvent e) {
								
							}
							
							@Override
							public void mousePressed(MouseEvent e) {
								
							}
							
							@Override
							public void mouseExited(MouseEvent e) {
								
							}
							
							@Override
							public void mouseEntered(MouseEvent e) {
								
							}
							
							@Override
							public void mouseClicked(MouseEvent e) {
								cp.getPopupMenu().show(cp, e.getX(), e.getY());
								
							}
						});
						add(cp, currentXposition + "," + currentYposition);
					} else
						add(prettifyChart(ge, co, chartType, jfChart), currentXposition + "," + currentYposition);
				} else {
					if (chart instanceof MyColorGrid) {
						Color cbc = NodeTools.getChartBackgroundColor(ge, idx);
						JComponent chartPanel = (JComponent) chart;
						if (cbc != null) {
							chartPanel.setOpaque(true);
							chartPanel.setBackground(cbc);
						} else {
							chartPanel.setOpaque(false);
							chartPanel.setBackground(null);
						}
						
					}
					if (chart instanceof JComponent)
						add((JComponent) chart, currentXposition + "," + currentYposition);
					
				}
			}
		}
		synchronized (getTreeLock()) {
			if (isShowing())
				validate();
			else
				validateTree();
		}
	}
	
	public static JPanel prettifyChart(org.graffiti.graph.GraphElement ge,
			ChartOptions co, String chartType, JFreeChart jfChart) {
		if (jfChart.getTitle() != null) {
			jfChart.getTitle().setFont(NodeTools.getChartTitleFont(ge));
			jfChart.getTitle().setPaint(NodeTools.getChartTitleColor(ge));
		}
		JPanel chartPanel;
		if (chartType.equals(GraffitiCharts.LEGEND_ONLY)) {
			Double scale = (Double) AttributeHelper.getAttributeValue(ge, "charting", "legend_scale", new Double(
					1.0d), new Double(1.0d));
			jfChart.getLegend().setAnchor(Legend.NORTH);
			org.jfree.chart.StandardLegend sl = (StandardLegend) jfChart.getLegend();
			sl.setBoundingBoxArcHeight(0);
			sl.setBoundingBoxArcWidth(0);
			sl.setInnerGap(new Spacer(Spacer.ABSOLUTE, 0, 0, 0, 0));
			sl.setOuterGap(new Spacer(Spacer.ABSOLUTE, 0, 0, 0, 0));
			sl.setBackgroundPaint(AttributeHelper.getFillColor(ge));
			sl.setOutlinePaint(sl.getBackgroundPaint());
			chartPanel = new MyLegendComponent(sl, scale.doubleValue());
		} else {
			boolean f = co.enableUI;
			chartPanel = new ChartPanel(jfChart, f, f, f, false, false);
		}
		
		// chartPanel.set
		Color cbc = NodeTools.getChartBackgroundColor(ge);
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
	
	protected String prettifyDomainAxisText(String domainAxis, BioStatisticalCategoryDataset dataset) {
		if (domainAxis != null && domainAxis.equalsIgnoreCase("[unit]"))
			domainAxis = dataset.getDomainUnits();
		return domainAxis;
	}
	
	protected String prettifyRangeAxisText(String rangeAxis, BioStatisticalCategoryDataset dataset) {
		if (rangeAxis != null && rangeAxis.equalsIgnoreCase("[unit]"))
			rangeAxis = dataset.getRangeUnits();
		return rangeAxis;
	}
	
	protected Object createChart(String chartType, ChartOptions co, HeatMapOptions hmo) {
		GraffitiCharts c = GraffitiCharts.getChartStyleFromString(chartType);
		switch (c) {
			case LINE:
				return createLineChart(co);
			case BAR:
				return createBarChart(co);
			case BAR_FLAT:
				return createBarChartFlat(co);
			case PIE:
				return createPieChart(co);
			case PIE3D:
				return createPieChart3d(co);
			case HEATMAP:
				return createHeatmap(co, hmo);
			case LEGEND_ONLY:
				return createLegendChart(co);
			case HIDDEN:
				return null;
			default:
				break;
		}
		ErrorMsg.addErrorMessage("Unknown chart type: " + chartType);
		return null;
	}
	
	private GraffitiCharts getAutoChartTypeFor(SubstanceInterface xmldata) {
		int conditions = xmldata.size();
		int times = xmldata.getNumberOfDifferentTimePoints();
		if (times > 1)
			return GraffitiCharts.LINE;
		else
			if (times == 1 && conditions == 1)
				return GraffitiCharts.HEATMAP;
			else
				return GraffitiCharts.BAR_FLAT;
	}
	
	private JComponent createHeatmap(ChartOptions co, HeatMapOptions hmo) {
		Color[][] colors = new Color[co.dataset.getRowCount()][co.dataset.getColumnCount()];
		Color[][] outline_colors = new Color[co.dataset.getRowCount()][co.dataset.getColumnCount()];
		boolean opaque = true;
		
		ChartColorAttribute chartColorAttribute = (ChartColorAttribute) AttributeHelper.getAttributeValue(co.graph,
				ChartColorAttribute.attributeFolder, ChartColorAttribute.attributeName, new ChartColorAttribute(),
				new ChartColorAttribute());
		
		ArrayList<Color> colors2 = null;
		
		if (co.outlineBorderWidth >= 0)
			colors2 = chartColorAttribute.getSeriesOutlineColors(co.dataset.getRowKeys());
		
		for (int col = 0; col < co.dataset.getColumnCount(); col++) {
			for (int row = 0; row < co.dataset.getRowCount(); row++) {
				Number value = co.dataset.getMeanValue(row, col);
				if (value == null) {
					opaque = false;
					colors[row][col] = null;
					outline_colors[row][col] = null;
				} else {
					colors[row][col] = hmo.getHeatmapColor(value.doubleValue());
					if (colors2 != null)
						outline_colors[row][col] = colors2.get(row);
				}
			}
		}
		JComponent res = new MyColorGrid(colors, outline_colors, co.orientation, co.outlineBorderWidth);
		res.setOpaque(opaque);
		return res;
	}
	
	public static String getChartTitle(org.graffiti.graph.GraphElement ge, int idx) {
		String chartTitle = (String) AttributeHelper.getAttributeValue(ge, "charting", "chartTitle" + idx, "", "");
		if (chartTitle.length() == 0)
			chartTitle = null;
		else
			if (chartTitle.equalsIgnoreCase("[substancename]")) {
				chartTitle = AttributeHelper.getLabel(ge, null);
			}
		return chartTitle;
	}
	
	private JFreeChart createBarChart(ChartOptions co) {
		final JFreeChart chart = ChartFactory.createBarChart3D(co.chartTitle, // chart
				// title
				co.domainAxis, // domain axis label
				co.rangeAxis, // range axis label
				co.dataset, // data
				co.orientation, // orientation
				co.showLegend, // include legend
				co.enableUI, // tooltips
				false // urls
				);
		
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(null);
		
		if (co.useLogYscale)
			plot.setRangeAxis(new LogarithmicAxis(co.rangeAxis));
		
		// plot.setBackgroundPaint(getBackgroundCol());
		
		setCategoryAxisOptions(plot.getDomainAxis(), co);
		plot.getRangeAxis().setVisible(co.showRangeAxis);
		
		if (!Double.isNaN(co.lowerBound))
			plot.getRangeAxis().setLowerBound(co.lowerBound);
		if (!Double.isNaN(co.upperBound))
			plot.getRangeAxis().setUpperBound(co.upperBound);
		
		if (co.useCustomRangeSteps) {
			NumberAxis na = (NumberAxis) plot.getRangeAxis();
			NumberTickUnit unit = new NumberTickUnit(co.customRangeSteps);
			na.setTickUnit(unit, false, true);
		}
		
		final BarRenderer3D renderer = (BarRenderer3D) plot.getRenderer();
		
		setSeriesColorsAndStroke(renderer, co.outlineBorderWidth, co.graph);
		
		pretifyCategoryPlot(renderer, co);
		chart.setBackgroundPaint(null);
		return chart;
	}
	
	private JFreeChart createPieChart(ChartOptions co) {
		final JFreeChart chart = ChartFactory.createMultiplePieChart(co.chartTitle, // chart
				co.dataset, // data
				TableOrder.BY_COLUMN, co.showLegend, // include legend
				co.enableUI, // tooltips
				false // urls
				);
		prettifyPiePlot(co, chart, false);
		return chart;
	}
	
	@SuppressWarnings("unchecked")
	private void prettifyPiePlot(ChartOptions co, final JFreeChart chart, boolean d3d) {
		MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
		plot.setBackgroundPaint(null);
		plot.setSumScale(co.usePieScaling);
		plot.setLimit(0);
		JFreeChart fc = plot.getPieChart();
		PiePlot pp = (PiePlot) fc.getPlot();
		
		// ////////////////////
		
		ChartColorAttribute chartColorAttribute = (ChartColorAttribute) AttributeHelper.getAttributeValue(co.graph,
				ChartColorAttribute.attributeFolder, ChartColorAttribute.attributeName, new ChartColorAttribute(),
				new ChartColorAttribute());
		
		Collection<String> rowKeys;
		try {
			rowKeys = co.dataset.getRowKeys();
		} catch (Exception e) {
			rowKeys = new ArrayList<String>();
			System.err.println("Row keys of chart could not be determined.");
		}
		
		ArrayList<Color> colors1 = chartColorAttribute.getSeriesColors(rowKeys);
		ArrayList<Color> colors2 = null;
		if (d3d)
			colors2 = colors1; // chartColorAttribute.getSeriesOutlineColors(co.dataset.getRowKeys());
			
		if ((int) co.outlineBorderWidth != 4)
			pp.setSectionOutlineStroke(new BasicStroke(co.outlineBorderWidth));
		pp.setSectionOutlineStroke(null);
		int i = 0;
		for (Color c1 : colors1) {
			if (d3d)
				pp.setSectionPaint(i, new Color(c1.getRed(), c1.getGreen(), c1.getBlue(), 180));
			else
				pp.setSectionPaint(i, c1);
			i++;
		}
		if (d3d) {
			i = 0;
			for (Color c2 : colors2) {
				pp.setSectionOutlinePaint(i, new Color(c2.getRed(), c2.getGreen(), c2.getBlue(), 180));
				i++;
			}
		}
		// ////////////////////
		
		// pp.setCircular(!d3d);
		pp.setMaximumLabelWidth(0);
		pp.setLabelGap(0d);
		pp.setLabelLinkMargin(0d);
		pp.setLabelGenerator(null);
		pp.setInteriorGap(0d);
		
		// pp.setExplodePercent(0, 0.55);
		pp.setBackgroundPaint(null);
		pp.setShadowPaint(null);
		// pp.setOutlineStroke(new BasicStroke(co.outlineBorderWidth));
		pp.setOutlinePaint(null);
		// chart.setSubtitles(new ArrayList());
		chart.setBackgroundPaint(null);
	}
	
	private JFreeChart createPieChart3d(ChartOptions co) {
		final JFreeChart chart = ChartFactory.createMultiplePieChart3D(co.chartTitle, // chart
				co.dataset, // data
				TableOrder.BY_COLUMN, co.showLegend, // include legend
				co.enableUI, // tooltips
				false // urls
				);
		prettifyPiePlot(co, chart, true);
		return chart;
	}
	
	private JFreeChart createBarChartFlat(ChartOptions co) {
		final JFreeChart chart = ChartFactory.createBarChart(co.chartTitle, // chart
				// title
				co.domainAxis, // domain axis label
				co.rangeAxis, // range axis label
				co.dataset, // data
				co.orientation, // orientation
				co.showLegend, // include legend
				co.enableUI, // tooltips
				false // urls
				);
		
		CategoryPlot plot = chart.getCategoryPlot();
		if (co.useLogYscale)
			plot.setRangeAxis(new LogarithmicAxis(co.rangeAxis));
		
		plot.setRenderer(new StatisticalBarRenderer());
		plot.setBackgroundPaint(null);
		
		// plot.setBackgroundPaint(getBackgroundCol());
		setCategoryAxisOptions(plot.getDomainAxis(), co);
		plot.getRangeAxis().setVisible(co.showRangeAxis);
		
		if (co.useCustomRangeSteps) {
			NumberAxis na = (NumberAxis) plot.getRangeAxis();
			NumberTickUnit unit = new NumberTickUnit(co.customRangeSteps);
			na.setTickUnit(unit, false, true);
		}
		
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		setSeriesColorsAndStroke(renderer, co.outlineBorderWidth, co.graph);
		
		if (!Double.isNaN(co.lowerBound))
			plot.getRangeAxis().setLowerBound(co.lowerBound);
		if (!Double.isNaN(co.upperBound))
			plot.getRangeAxis().setUpperBound(co.upperBound);
		
		pretifyCategoryPlot(renderer, co);
		
		chart.setBackgroundPaint(null);
		
		// chart.setAntiAlias(co.enableUI);
		
		return chart;
	}
	
	private void pretifyCategoryPlot(BarRenderer renderer, ChartOptions co) {
		if (co.axisFontSize > 0) {
			Font af = new Font(Axis.DEFAULT_AXIS_LABEL_FONT.getFontName(), Axis.DEFAULT_AXIS_LABEL_FONT.getStyle(),
					co.axisFontSize);
			renderer.getPlot().getRangeAxis().setTickLabelFont(af);
			renderer.getPlot().getDomainAxis().setTickLabelFont(af);
			renderer.getPlot().getDomainAxis().setLabelFont(af);
			renderer.getPlot().getRangeAxis().setLabelFont(af);
			
			// renderer.getPlot().setFixedRangeAxisSpace(new AxisSpace());
			// renderer.getPlot().getFixedRangeAxisSpace().setBottom(9);
		}
		renderer.setDrawBarOutline(false);
		renderer.getPlot().setDomainGridlinesVisible(co.showGridCategory);
		renderer.getPlot().setRangeGridlinesVisible(co.showGridRange);
		if (co.gridWidth >= 0) {
			renderer.getPlot().setRangeGridlineStroke(
					new BasicStroke((float) co.gridWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[] {
							(float) co.gridWidth * 8f, 2f }, 0.0f));
			renderer.getPlot().setDomainGridlineStroke(
					new BasicStroke((float) co.gridWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[] {
							(float) co.gridWidth * 8f, 2f }, 0.0f));
		}
		if (co.gridColor != null) {
			renderer.getPlot().setRangeGridlinePaint(co.gridColor);
			renderer.getPlot().setDomainGridlinePaint(co.gridColor);
		}
		if (co.axisColor != null) {
			renderer.getPlot().getRangeAxis().setAxisLinePaint(co.axisColor);
			renderer.getPlot().getDomainAxis().setAxisLinePaint(co.axisColor);
		}
		
		if (co.axisWidth >= 0) {
			renderer.getPlot().getRangeAxis().setAxisLineStroke(new BasicStroke((float) co.axisWidth));
			renderer.getPlot().getDomainAxis().setAxisLineStroke(new BasicStroke((float) co.axisWidth));
		}
		
		renderer.getPlot().setOutlinePaint(null);
	}
	
	private JFreeChart createLegendChart(ChartOptions co) {
		final JFreeChart chart = ChartFactory.createBarChart(co.chartTitle, // chart
				// title
				co.domainAxis, // domain axis label
				co.rangeAxis, // range axis label
				co.dataset, // data
				co.orientation, // orientation
				true, // include legend
				co.enableUI, // tooltips
				false // urls
				);
		
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setRenderer(new StatisticalBarRenderer());
		plot.setBackgroundPaint(null);
		setCategoryAxisOptions(plot.getDomainAxis(), co);
		plot.getRangeAxis().setVisible(co.showRangeAxis);
		BarRenderer renderer = (BarRenderer) plot.getRenderer();
		setSeriesColorsAndStroke(renderer, co.outlineBorderWidth, co.graph);
		
		renderer.setDrawBarOutline(false);
		chart.setBackgroundPaint(null);
		chart.setHidePlot(true);
		return chart;
	}
	
	/**
	 * @param renderer
	 */
	@SuppressWarnings("unchecked")
	public static void setSeriesColorsAndStroke(CategoryItemRenderer renderer, float outlineBorderWidth, Graph graph) {
		
		ChartColorAttribute chartColorAttribute = (ChartColorAttribute) AttributeHelper.getAttributeValue(graph,
				ChartColorAttribute.attributeFolder, ChartColorAttribute.attributeName, new ChartColorAttribute(),
				new ChartColorAttribute());
		
		ArrayList<Color> colors1 = chartColorAttribute.getSeriesColors(renderer.getPlot().getDataset().getRowKeys());
		ArrayList<Color> colors2 = chartColorAttribute.getSeriesOutlineColors(renderer.getPlot().getDataset()
				.getRowKeys());
		
		if (outlineBorderWidth >= 0)
			renderer.setStroke(new BasicStroke(outlineBorderWidth));
		int i = 0;
		for (Color c1 : colors1) {
			renderer.setSeriesPaint(i, c1);
			i++;
		}
		i = 0;
		for (Color c2 : colors2) {
			renderer.setSeriesOutlinePaint(i, c2);
			i++;
		}
	}
	
	protected static synchronized Shape createTransformedShape(Shape shape, double translateX, double translateY) {
		AffineTransform transformer = new AffineTransform();
		transformer.setToTranslation(translateX, translateY);
		return transformer.createTransformedShape(shape);
	}
	
	/**
	 * @param axis
	 */
	protected void setCategoryAxisOptions(CategoryAxis axis, ChartOptions co) {
		if (co.orientation == PlotOrientation.VERTICAL) {
			// if (Math.abs(co.axisRotation)>0.00001)
			axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 180
					* co.axisRotation));
			/*
			 * else axis.setBottomCategoryLabelPosition(new CategoryLabelPosition(
			 * RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_CENTER,
			 * TextAnchor.BOTTOM_RIGHT, 0, CategoryLabelWidthType.RANGE, 0.50f ));
			 */
			((CategoryPlot) axis.getPlot()).setCategoryBackgroundPaintA(co.cpIdxA, co.cpColA);
			((CategoryPlot) axis.getPlot()).setCategoryBackgroundPaint(co.cpColBackground);
			((CategoryPlot) axis.getPlot()).setCategoryBackgroundPaintC(co.cpIdxC, co.cpColC);
		} else {
			axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(0));
		}
		axis.setVisible(co.showCategoryAxis);
		axis.setSkipLabels(co.plotAxisSteps);
		((CategoryPlot) axis.getPlot()).setSkipLabels(co.plotAxisSteps);
	}
	
	private JFreeChart createLineChart(ChartOptions co) {
		JFreeChart chart = ChartFactory.createLineChart(co.chartTitle, co.domainAxis, // "DOMAIN AXIS",
				co.rangeAxis, // "RANGE AXIS",
				co.dataset, co.orientation, co.showLegend, co.enableUI, co.enableUI);
		
		CategoryPlot plot = chart.getCategoryPlot();
		
		if (co.useLogYscale)
			plot.setRangeAxis(new LogarithmicAxis(co.rangeAxis));
		
		setCategoryAxisOptions(plot.getDomainAxis(), co);
		
		plot.setDrawingSupplier(new MyDefaultShapeDrawingSupplier(co.shapeSize));
		plot.setBackgroundPaint(null);
		
		if (co.useCustomRangeSteps) {
			NumberAxis na = (NumberAxis) plot.getRangeAxis();
			NumberTickUnit unit = new NumberTickUnit(co.customRangeSteps);
			na.setTickUnit(unit, false, true);
		}
		
		plot.getRangeAxis().setVisible(co.showRangeAxis);
		BioStatisticalCategoryDataset bsc = co.dataset;
		double currLB = plot.getRangeAxis().getLowerBound();
		double currUB = plot.getRangeAxis().getUpperBound();
		double minVal = bsc.getMinimumRangeValue().doubleValue();
		double maxVal = bsc.getMaximumRangeValue().doubleValue();
		double lowerMargin = plot.getRangeAxis().getLowerMargin();
		double upperMargin = plot.getRangeAxis().getUpperMargin();
		if (minVal < currLB)
			plot.getRangeAxis().setLowerBound(minVal < 0 ? minVal * (1 - lowerMargin) : 0);
		if (maxVal * (1 + upperMargin) > currUB)
			plot.getRangeAxis().setUpperBound(maxVal * (1 + upperMargin));
		
		if (!Double.isNaN(co.lowerBound))
			plot.getRangeAxis().setLowerBound(co.lowerBound);
		if (!Double.isNaN(co.upperBound))
			plot.getRangeAxis().setUpperBound(co.upperBound);
		
		MyLineAndShapeRenderer myRenderer = new MyLineAndShapeRenderer();
		myRenderer.setConnectPriorItems(co.connectPriorItems);
		plot.setRenderer(myRenderer);
		
		MyLineAndShapeRenderer renderer = (MyLineAndShapeRenderer) plot.getRenderer();
		renderer.setStdDevTopWidth(co.stdDevTopWidth);
		renderer.setDrawStdDev(co.showStdDevAsT, co.showStdDevAsFillRange);
		renderer.setStdDevLineWidth(co.stdDevLineWidth);
		setSeriesColorsAndStroke(renderer, co.outlineBorderWidth, co.graph);
		
		renderer.setDrawShapes(co.showShapes);
		renderer.setDrawLines(co.showLines);
		
		// renderer.getSeriesShape(0);
		
		// renderer.setSeriesStroke(0, new BasicStroke(2.0f,
		// BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND, 1.0f, new float[] { 10.0f, 6.0f }, 0.0f));
		// renderer.setSeriesStroke(1, new BasicStroke(2.0f,
		// BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND, 1.0f, new float[] { 6.0f, 6.0f }, 0.0f));
		// renderer.setSeriesStroke(2, new BasicStroke(2.0f,
		// BasicStroke.CAP_ROUND,
		// BasicStroke.JOIN_ROUND, 1.0f, new float[] { 2.0f, 6.0f }, 0.0f));
		
		chart.setBackgroundPaint(null);
		
		renderer.setItemLabelsVisible(false);
		renderer.setLabelGenerator(new StandardCategoryLabelGenerator());
		renderer.getPlot().setDomainGridlinesVisible(co.showGridCategory);
		renderer.getPlot().setRangeGridlinesVisible(co.showGridRange);
		renderer.getPlot().setOutlinePaint(null);
		
		if (co.gridWidth >= 0) {
			renderer.getPlot().setRangeGridlineStroke(
					new BasicStroke((float) co.gridWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[] {
							(float) co.gridWidth * 8f, 2f }, 0.0f));
			renderer.getPlot().setDomainGridlineStroke(
					new BasicStroke((float) co.gridWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[] {
							(float) co.gridWidth * 8f, 2f }, 0.0f));
		}
		
		if (co.axisWidth >= 0) {
			renderer.getPlot().getRangeAxis().setAxisLineStroke(new BasicStroke((float) co.axisWidth));
			renderer.getPlot().getDomainAxis().setAxisLineStroke(new BasicStroke((float) co.axisWidth));
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
			Font af = new Font(Axis.DEFAULT_AXIS_LABEL_FONT.getFontName(), Axis.DEFAULT_AXIS_LABEL_FONT.getStyle(),
					co.axisFontSize);
			renderer.getPlot().getRangeAxis().setTickLabelFont(af);
			renderer.getPlot().getDomainAxis().setTickLabelFont(af);
			renderer.getPlot().getDomainAxis().setLabelFont(af);
			renderer.getPlot().getRangeAxis().setLabelFont(af);
		}
		
		return chart;
	}
	
	protected BioStatisticalCategoryDataset getDataset(SubstanceInterface xmldata, Graph g,
			boolean alsoUsedForPlottingStdDev, boolean showOnlyHalfErrorBar, boolean fillTimeGaps, boolean removeEmptyConditions) {
		Double markerSize = (Double) AttributeHelper.getAttributeValue(g, "", AttributeHelper.id_ttestCircleSize,
				new Double(10.0d), new Double(10.0d));
		boolean useStdErrInsteadOfStdDev = ((Boolean) AttributeHelper.getAttributeValue(g, "", "node_useStdErr",
				new Boolean(false), new Boolean(false))).booleanValue();
		BioStatisticalCategoryDataset dataset = new BioStatisticalCategoryDataset(markerSize.floatValue());
		List<MyComparableDataPoint> ss = NodeTools.getSortedAverageDataSetValues(xmldata, removeEmptyConditions);
		SortedSet<Integer> timePoints = null;
		if (fillTimeGaps)
			timePoints = new TreeSet<Integer>();
		int minTime = Integer.MAX_VALUE;
		int maxTime = Integer.MIN_VALUE;
		String timeUnit = null;
		String measUnit = null;
		String series = null;
		for (MyComparableDataPoint mcdp : ss) {
			dataset.add(mcdp.mean, mcdp.getStddev(useStdErrInsteadOfStdDev), mcdp.serie, mcdp.timeValue,
					mcdp.ttestIsReference, mcdp.ttestIsSignificantDifferent, mcdp.measurementUnit, mcdp.timeUnit,
					alsoUsedForPlottingStdDev, showOnlyHalfErrorBar);
			if (fillTimeGaps) {
				if (measUnit == null && mcdp.measurementUnit != null)
					measUnit = mcdp.measurementUnit;
				if (timeUnit == null && mcdp.timeUnit != null)
					timeUnit = mcdp.timeUnit;
				if (series == null && mcdp.serie != null)
					series = mcdp.serie;
				if (series == null && mcdp.getMeasurement() != null)
					series = mcdp.serie;
				if (measUnit == null && mcdp.measurementUnit != null)
					series = mcdp.measurementUnit;
				int time = mcdp.timeValue;
				if (time != -1) {
					timePoints.add(time);
					if (time < minTime)
						minTime = time;
					if (time > maxTime)
						maxTime = time;
				}
			}
		}
		if (fillTimeGaps && timePoints.size() > 2) {
			addMissingTimePoints(dataset, timePoints, minTime, maxTime, timeUnit, measUnit, series);
		}
		return dataset;
	}
	
	private void addMissingTimePoints(BioStatisticalCategoryDataset dataset, SortedSet<Integer> timePoints, int minTime,
			int maxTime, String timeUnit, String measUnit, String series) {
		int minDiff;
		int maxDiff;
		do {
			minDiff = Integer.MAX_VALUE;
			maxDiff = Integer.MIN_VALUE;
			int lastVal = -1;
			for (Iterator<Integer> it = timePoints.iterator(); it.hasNext();) {
				int v1 = it.next();
				if (lastVal != -1) {
					if (v1 != -1) {
						int diff = v1 - lastVal;
						if (diff < minDiff)
							minDiff = diff;
						if (diff > maxDiff)
							maxDiff = diff;
					}
				}
				lastVal = v1;
			}
			if (minDiff < maxDiff && minDiff > 0) {
				for (int i = minTime; i < maxTime; i += minDiff) {
					if (!timePoints.contains(i)) {
						dataset.add(Double.NaN, Double.NaN, series, i, false, false, measUnit, timeUnit, false, false);
						timePoints.add(i);
					}
				}
			}
		} while (minDiff < maxDiff);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getCategory()
	 */
	public String getCategory() {
		return "Visualisation";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.options.OptionPane#getOptionName()
	 */
	public String getOptionName() {
		return "Bar/Line Chart";
	}
}