/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 15.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;

import javax.swing.JComponent;

import org.AttributeHelper;
import org.BioStatisticalCategoryDataset;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graphics.GraphElementGraphicAttribute;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartsColumnAttribute;

public class ChartOptions {
	
	private static final Boolean mTrue = new Boolean(true);
	private static final Boolean mFalse = new Boolean(false);
	
	private static final Double mNull = new Double(0);
	private static final Double mOne = new Double(1);
	private static final Integer mim1 = new Integer(-1);
	
	public Graph graph;
	public BioStatisticalCategoryDataset dataset;
	public String chartTitle;
	public PlotOrientation orientation;
	public boolean showLegend;
	public String domainAxis;
	public String rangeAxis;
	public boolean showRangeAxis;
	public boolean showCategoryAxis;
	public float outlineBorderWidth;
	// public ArrayList<Color> seriesColors;
	// public ArrayList<Color> seriesOutlineColors;
	public int axisRotation;
	public double lowerBound;
	public double upperBound;
	public boolean showGridRange;
	public boolean showGridCategory;
	public double gridWidth;
	public double axisWidth;
	public Color gridColor;
	public Color axisColor;
	public int axisFontSize;
	public boolean showStdDevAsT;
	public double stdDevTopWidth;
	public boolean showStdDevAsFillRange;
	public float shapeSize;
	public float stdDevLineWidth;
	public boolean showOnlyHalfErrorBar;
	public boolean removeEmptyConditions;
	public boolean showShapes;
	public boolean showLines;
	public boolean usePieScaling;
	public boolean connectPriorItems;
	public int plotAxisSteps;
	public boolean useLogYscale;
	public int cpIdxA, cpIdxC;
	public Color cpColA, cpColBackground, cpColC;
	public boolean useCustomRangeSteps;
	public double customRangeSteps;
	public GraphElement ge;
	public boolean fillTimeGaps;
	public double borderHor;
	
	public ChartOptions(
						GraphElement ge, Graph graph,
						BioStatisticalCategoryDataset dataset,
						String chartTitle,
						PlotOrientation orientation,
						boolean showLegend,
						String domainAxis,
						String rangeAxis,
						boolean showRangeAxis,
						boolean showCategoryAxis,
						float outlineBorderWidth,
						int axisRotation,
						double lowerBound,
						double upperBound,
						boolean showGridRange,
						boolean showGridCategory,
						double gridWidth,
						double axisWidth,
						Color gridColor,
						Color axisColor,
						int axisFontSize,
						boolean showStdDevAsT,
						double stdDevTopWidth,
						boolean showStdDevAsFillRange,
						float shapeSize,
						float stdDevLineWidth,
						boolean showOnlyHalfErrorBar,
						boolean showShapes,
						boolean showLines,
						boolean usePieScaling,
						boolean connectPriorItems,
						int plotAxisSteps,
						boolean useLogYscale,
						int cpIdxA,
						int cpIdxC,
						Color cpColA,
						Color cpColBackground,
						Color cpColC,
						boolean useCustomRangeSteps,
						double customRangeSteps,
						boolean removeEmptyConditions) {
		this.ge = ge;
		this.graph = graph;
		this.dataset = dataset;
		this.chartTitle = chartTitle;
		this.orientation = orientation;
		this.showLegend = showLegend;
		this.domainAxis = domainAxis;
		this.rangeAxis = rangeAxis;
		this.showRangeAxis = showRangeAxis;
		this.showCategoryAxis = showCategoryAxis;
		this.outlineBorderWidth = outlineBorderWidth;
		this.axisRotation = axisRotation;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.showGridRange = showGridRange;
		this.showGridCategory = showGridCategory;
		this.gridWidth = gridWidth;
		this.axisWidth = axisWidth;
		this.gridColor = gridColor;
		this.axisColor = axisColor;
		this.axisFontSize = axisFontSize;
		this.showStdDevAsT = showStdDevAsT;
		this.stdDevTopWidth = stdDevTopWidth;
		this.showStdDevAsFillRange = showStdDevAsFillRange;
		this.shapeSize = shapeSize;
		this.stdDevLineWidth = stdDevLineWidth;
		this.showOnlyHalfErrorBar = showOnlyHalfErrorBar;
		this.showShapes = showShapes;
		this.showLines = showLines;
		this.usePieScaling = usePieScaling;
		this.connectPriorItems = connectPriorItems;
		this.plotAxisSteps = plotAxisSteps;
		this.useLogYscale = useLogYscale;
		this.cpIdxA = cpIdxA;
		this.cpIdxC = cpIdxC;
		this.cpColA = cpColA;
		this.cpColBackground = cpColBackground;
		this.cpColC = cpColC;
		this.useCustomRangeSteps = useCustomRangeSteps;
		this.customRangeSteps = customRangeSteps;
		this.removeEmptyConditions = removeEmptyConditions;
	}
	
	public ChartOptions() {
		
	}
	
	public void readAttributes(GraphElement ge) {
		this.ge = ge;
		this.graph = ge.getGraph();
		
		axisRotation = ((Double) AttributeHelper.getAttributeValue(graph, "", "node_plotAxisRotation", mNull, mNull)).intValue();
		
		boolean plotHor = ((Boolean) AttributeHelper
							.getAttributeValue(graph, "", "node_plotOrientationHor", mTrue, mTrue)).booleanValue();
		if (plotHor)
			orientation = PlotOrientation.VERTICAL;
		else
			orientation = PlotOrientation.HORIZONTAL;
		domainAxis = (String) AttributeHelper.getAttributeValue(ge, "charting", "domainAxis",
							new String("[unit]"), new String("[unit]"));
		if (domainAxis.trim().length() == 0)
			domainAxis = null;
		rangeAxis = (String) AttributeHelper.getAttributeValue(ge, "charting", "rangeAxis", "[unit]", "[unit]");
		if (rangeAxis.trim().length() == 0)
			rangeAxis = null;
		
		showCategoryAxis = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showCategoryAxis",
							mFalse, mFalse)).booleanValue();
		showRangeAxis = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showRangeAxis", mFalse,
							mFalse)).booleanValue();
		
		Boolean showRangeAxis2 = ((Boolean) AttributeHelper.getAttributeValue(ge, "charting", "showRangeAxis", null,
							mFalse, false));
		if (showRangeAxis2 != null)
			showRangeAxis = showRangeAxis2;
		
		cpIdxA = ((Integer) AttributeHelper.getAttributeValue(graph, "",
							"node_categoryBackgroundColorIndexA", mim1, mim1)).intValue();;
		cpIdxC = ((Integer) AttributeHelper.getAttributeValue(graph, "",
							"node_categoryBackgroundColorIndexC", mim1, mim1)).intValue();
		int c1 = 255;
		int c2 = 225;
		cpColA = NodeTools.getCategoryBackgroundColorA(graph, new Color(c2, c1, c1));
		cpColBackground = NodeTools.getCategoryBackgroundColorB(graph, Color.BLACK);
		cpColC = NodeTools.getCategoryBackgroundColorC(graph, new Color(c1, c1, c2));
		
		gridWidth = ((Double) AttributeHelper.getAttributeValue(graph, "", "node_gridWidth", new Double(0.5d),
							new Double(-1))).doubleValue();
		
		axisWidth = ((Double) AttributeHelper.getAttributeValue(graph, "", "node_axisWidth", mOne, new Double(1)))
							.doubleValue();
		
		gridColor = NodeTools.getGridColor(graph, (Color) CategoryPlot.DEFAULT_GRIDLINE_PAINT);
		axisColor = NodeTools.getAxisColor(graph, (Color) CategoryPlot.DEFAULT_GRIDLINE_PAINT);
		
		axisFontSize = ((Integer) AttributeHelper.getAttributeValue(graph, "", "node_plotAxisFontSize", new Integer(
							30), new Integer(30))).intValue();
		
		showShapes = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartShowShapes", mTrue,
							new Boolean(true))).booleanValue();
		showGridRange = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showGridRange", mFalse,
							new Boolean(false))).booleanValue();
		useLogYscale = false; // ((Boolean)AttributeHelper.getAttributeValue( graph, * "", "node_useLogScaleForRangeAxis", new Boolean(false),
		// newBoolean(false))).booleanValue();
		
		usePieScaling = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_usePieScale", mTrue,
							new Boolean(true))).booleanValue();
		
		showGridCategory = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_showGridCategory",
							mTrue, mTrue)).booleanValue();
		showLines = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartShowLines", mTrue,
							new Boolean(true))).booleanValue();
		
		showStdDevAsT = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartShowStdDev", mFalse,
							mFalse)).booleanValue();
		
		showStdDevAsFillRange = ((Boolean) AttributeHelper.getAttributeValue(graph, "",
							"node_lineChartShowStdDevRangeLine", mTrue, new Boolean(true))).booleanValue();
		
		connectPriorItems = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "connectPriorItems", mTrue,
							mTrue)).booleanValue();
		
		plotAxisSteps = ((Double) AttributeHelper.getAttributeValue(graph, "", "node_plotAxisSteps", mOne, mOne))
							.intValue();
		
		useCustomRangeSteps = ((Boolean) AttributeHelper.getAttributeValue(ge, "charting", "useCustomRangeSteps",
							new Boolean(false), new Boolean(false))).booleanValue();
		customRangeSteps = (Double) AttributeHelper.getAttributeValue(ge, "charting", "rangeStepSize", mNull, mNull);
		
		boolean useCustomRange = ((Boolean) AttributeHelper.getAttributeValue(ge, "charting", "useCustomRange",
							new Boolean(false), new Boolean(false))).booleanValue();
		lowerBound = (Double) AttributeHelper.getAttributeValue(ge, "charting", "minRange", mNull, mNull);
		
		upperBound = (Double) AttributeHelper.getAttributeValue(ge, "charting", "maxRange", mOne, mOne);
		
		fillTimeGaps = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_lineChartFillTimeGaps",
							mTrue, mTrue)).booleanValue();
		
		if (!useCustomRange) {
			lowerBound = Double.NaN;
			upperBound = Double.NaN;
		}
		
		Double temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_outlineBorderWidth", new Double(4d),
							new Double(4d));
		outlineBorderWidth = temp.floatValue();
		
		temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_chartShapeSize", new Double(6d),
							new Double(6d));
		shapeSize = temp.floatValue();
		
		temp = (Double) AttributeHelper.getAttributeValue(graph, "", "node_chartStdDevLineWidth", new Double(4d),
							new Double(4d));
		stdDevLineWidth = temp.floatValue();
		
		showOnlyHalfErrorBar = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_halfErrorBar",
				mFalse, mFalse)).booleanValue();
		removeEmptyConditions = ((Boolean) AttributeHelper.getAttributeValue(graph, "", "node_removeEmptyConditions",
				mFalse, mFalse)).booleanValue();
		stdDevTopWidth = (Double) AttributeHelper.getAttributeValue(graph, "", "node_chartStdDevTopWidth",
							new Double(10d), new Double(10d));
		
		showLegend = ((Boolean) AttributeHelper.getAttributeValue(ge, "charting", "show_legend", new Boolean(
							false), new Boolean(false))).booleanValue();
		
	}
	
	public int setLayoutOfChartComponent(GraphElement ge, JComponent chartcomponent, Integer mappedDataListSize) {
		
		borderHor = ((Double) AttributeHelper.getAttributeValue(ge, "charting", "empty_border_width", new Double(
							2d), new Double(2d))).doubleValue();
		double borderVer = ((Double) AttributeHelper.getAttributeValue(ge, "charting", "empty_border_width_vert",
							borderHor, borderHor)).doubleValue();
		
		GraphElementGraphicAttribute geGraphicsAttr = (GraphElementGraphicAttribute) ge
							.getAttribute(GraphicAttributeConstants.GRAPHICS);
		borderHor += geGraphicsAttr.getFrameThickness();
		
		int maxXcnt = 2 + 1;
		if (mappedDataListSize != null && mappedDataListSize > 1)
			maxXcnt = 2 + mappedDataListSize;
		
		int maxYcnt = (mappedDataListSize != null) ? mappedDataListSize : 1;
		
		int needsToBeMapped = (mappedDataListSize != null) ? mappedDataListSize : 1;
		maxXcnt = (int) Math.ceil(Math.sqrt(needsToBeMapped));
		
		int chartsInColumn = -1;
		if (mappedDataListSize != null) // && mappedDataList.size()>1)
			chartsInColumn = ((Integer) AttributeHelper.getAttributeValue(ge, "charting", ChartsColumnAttribute.name,
								new ChartsColumnAttribute(-1).getValue(), new ChartsColumnAttribute(-1).getValue())).intValue();;
		
		if (chartsInColumn >= 1)
			maxXcnt = chartsInColumn;
		if (chartsInColumn == -2)
			maxXcnt = needsToBeMapped;
		if (maxXcnt < 1)
			maxXcnt = 1;
		maxYcnt = needsToBeMapped / maxXcnt;
		if (maxXcnt * maxYcnt < needsToBeMapped)
			maxYcnt++;
		int maxInRow = maxXcnt;
		
		int sizeX = maxXcnt + 2;
		int sizeY = (mappedDataListSize != null) ? maxYcnt + 3 : 2;
		
		double[][] size = new double[2][];
		size[0] = new double[sizeX]; // columns // { border,
		// TableLayoutConstants.FILL, border }; //
		// Columns
		size[1] = new double[sizeY]; // rows
		for (int col = 0; col < sizeX; col++)
			size[0][col] = TableLayoutConstants.FILL;
		for (int row = 0; row < sizeY; row++)
			size[1][row] = TableLayoutConstants.FILL;
		size[0][0] = borderHor;
		size[0][sizeX - 1] = borderHor;
		size[1][0] = borderVer;
		size[1][sizeY - 1] = borderVer;
		size[1][1] = 0;
		
		chartcomponent.setLayout(new TableLayout(size));
		return maxInRow;
	}
	
	public void setFurtherOptions(Graph graph, BioStatisticalCategoryDataset dataset, String chartTitle) {
		this.graph = graph;
		this.dataset = dataset;
		this.chartTitle = chartTitle;
	}
	
	public void setFurtherOptions(Graph graph, String chartTitle) {
		this.graph = graph;
		this.chartTitle = chartTitle;
	}
	
}