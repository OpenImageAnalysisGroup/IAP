/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.12.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.BioStatisticalCategoryDataset;
import org.ErrorMsg;
import org.HelperClass;
import org.ObjectRef;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;

import qmwi.kseg.som.Map;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.DataSetRow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.chartDrawComponent.XmlDataChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentData;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ReplicateDouble;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics.MyScatterBlock;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SOMguiHelper implements HelperClass {
	
	public static void showSOMcentroidsAndClusterAssignmentSettings(
			Map som,
			String[] columns,
			Graph optSrcGraph) {
		
		// int[] groupCount = new int[result.length];
		// for (int i=0; i<result.length; i++)
		// groupCount[i] = result[i].size();
		
		som.getWeights();
		
		JComponent somPanel = getSOMpanel(som, columns, optSrcGraph);
		JFrame newFrame = new JFrame("SOM Map");
		newFrame.setSize(500, 500);
		double border = 2;
		double[][] size =
		{ { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED,
						TableLayoutConstants.FILL, border }
		}; // Rows
		newFrame.setLayout(new TableLayout(size));
		newFrame.add(new JLabel("SOM Nodes / Prototypes; Select target cluster IDs:"), "1,1");
		newFrame.add(somPanel, "1,2");
		newFrame.validate();
		newFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		newFrame.setVisible(true);
	}
	
	private static JComponent getSOMpanel(Map som, final String[] columns, Graph srcGraph) {
		MyScatterBlock scatterBlock = new MyScatterBlock(false, new JLabel().getFont().getSize());
		int y = 0;
		for (int nodeIndex = 0; nodeIndex < som.getNeuronNodeCount();) {
			// List mappedDataList1 = Experiment2GraphHelper.getMappedDataListFromNode(node1);
			for (int x = 0; x < som.getSomWidth() && nodeIndex < som.getNeuronNodeCount(); x++) {
				ObjectRef timePoints = new ObjectRef();
				CategoryDataset dataset = getDataset(som, nodeIndex, columns, timePoints);
				
				JFreeChart chart = createChart(dataset,
						"Centroid " + (nodeIndex + 1) + "-> Cluster ID " + (nodeIndex + 1),
						PlotOrientation.VERTICAL, false, null, null, true, (Integer) timePoints.getObject() > 1, 3, (Integer) timePoints.getObject(),
						srcGraph);
				ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
				chartPanel.setToolTipText("Click to get details on data ordering");
				final Color selectedColor = new Color(200, 200, 220);
				chartPanel.getBackground();
				selectedColor.brighter().brighter();
				chartPanel.addMouseListener(new MouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						String colDesc = "";
						for (String col : columns) {
							if (colDesc.length() > 0)
								colDesc = colDesc + "<br>" + col;
							else
								colDesc = col;
						}
						colDesc = "<html>The X-Axis is is sorted as follows:<br>" + colDesc;
						MainFrame.showMessageDialogWithScrollBars(colDesc, "X-Axis");
					}
					
					@Override
					public void mousePressed(MouseEvent e) {
					}
					
					@Override
					public void mouseReleased(MouseEvent e) {
					}
					
					@Override
					public void mouseEntered(MouseEvent e) {
						// ChartPanel src = (ChartPanel) e.getSource();
						// if (src.getBackground()!=selectedColor)
						// src.setBackground(hoverColor);
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						// ChartPanel src = (ChartPanel) e.getSource();
						// if (src.getBackground()!=selectedColor)
						// src.setBackground(unselectedColor);
					}
				});
				scatterBlock.addChartPanel(getChartPanelWithCommandButton(chartPanel, nodeIndex, som.getNeuronNodeCount(), som), x + 1, y + 1, null, null);
				nodeIndex++;
				// System.out.println("Chart Panel: "+(x+1)+":"+(y+1));
			}
			y++;
		}
		return scatterBlock.getChartPanel();
	}
	
	private static JComponent getChartPanelWithCommandButton(
			final ChartPanel chartPanel, final int nodeIndex, int maxIndex, final Map som) {
		JPanel resultPanel = new JPanel();
		double border = 0;
		double[][] size =
		{ { border, TableLayoutConstants.FILL, border }, // Columns
				{ border,
						TableLayoutConstants.FILL,
						TableLayoutConstants.PREFERRED,
						border }
		}; // Rows
		resultPanel.setLayout(new TableLayout(size));
		resultPanel.add(chartPanel, "1,1");
		final JComboBox selectClusterID = new JComboBox(getSelections(nodeIndex, maxIndex));
		selectClusterID.setSelectedIndex(nodeIndex);
		selectClusterID.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (selectClusterID.getSelectedIndex() == selectClusterID.getItemCount() - 1) {
					// ignore this SOM prototype!
					som.setIgnoreNode(nodeIndex, true);
					chartPanel.getChart().setTitle("Ignored Centroid " + (nodeIndex + 1) + "");
				} else {
					som.setIgnoreNode(nodeIndex, false);
					int targetID = selectClusterID.getSelectedIndex() + 1;
					som.setTargetClusterForNode(nodeIndex, targetID);
					chartPanel.getChart().setTitle("Centroid " + (nodeIndex + 1) + "-> Cluster " + targetID);
				}
			}
		});
		resultPanel.add(selectClusterID, "1,2");
		return resultPanel;
	}
	
	private static String[] getSelections(int nodeIndex, int maxIndex) {
		String[] result = new String[maxIndex + 1];
		for (int i = 0; i < maxIndex; i++)
			result[i] = "Cluster ID " + (i + 1);
		result[maxIndex] = "-- do not consider";
		return result;
	}
	
	private static JFreeChart createChart(CategoryDataset dataset, String title,
			PlotOrientation orientation, boolean showLegend, String domainAxis,
			String rangeAxis, boolean showRangeAxis, boolean showCategoryAxis,
			float outlineBorderWidth, long timePoints, Graph srcGraph) {
		final JFreeChart chart;
		if (timePoints > 1) {
			chart = ChartFactory.createLineChart(title, // chart
					// title
					domainAxis, // domain axis label
					rangeAxis, // range axis label
					dataset, // data
					orientation, // orientation
					showLegend, // include legend
					false, // tooltips
					false // urls
					);
		} else {
			chart = ChartFactory.createBarChart(title, // chart
					// title
					domainAxis, // domain axis label
					rangeAxis, // range axis label
					dataset, // data
					orientation, // orientation
					showLegend, // include legend
					false, // tooltips
					false // urls
					);
		}
		if (srcGraph != null)
			XmlDataChartComponent.setSeriesColorsAndStroke(chart.getCategoryPlot().getRenderer(), outlineBorderWidth, srcGraph);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(null);
		plot.getRangeAxis().setVisible(showRangeAxis);
		plot.getDomainAxis().setVisible(showCategoryAxis);
		chart.setBackgroundPaint(null);
		return chart;
	}
	
	public static Graph createCentroidNodesGraph(Map som,
			String[] columns) {
		Graph g = new AdjListGraph(new ListenerManager());
		
		double offX = 130;
		double offY = 130;
		double sx = 120;
		double sy = 120;
		double stx = 100;
		double sty = 100;
		int y = 0;
		for (int nodeIndex = 0; nodeIndex < som.getNeuronNodeCount();) {
			// List mappedDataList1 = Experiment2GraphHelper.getMappedDataListFromNode(node1);
			for (int x = 0; x < som.getSomWidth() && nodeIndex < som.getNeuronNodeCount(); x++) {
				ObjectRef timePoints = new ObjectRef();
				getDataset(som, nodeIndex, columns, timePoints);
				double xp = offX * x;
				double yp = offY * y;
				Node n = g.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(xp + stx, yp + sty));
				AttributeHelper.setSize(n, sx, sy);
				NodeHelper nh = new NodeHelper(n);
				ArrayList<DataSetRow> datasetRows = getDatasetRows(som, nodeIndex, columns, timePoints);
				
				nh.setLabel("Centroid " + (nodeIndex + 1));
				nh.setLabelAlignment(-1, AlignmentSetting.INSIDETOP);
				nh.setAttributeValue("som", "x", x);
				nh.setAttributeValue("som", "y", y);
				nh.addDataMapping(datasetRows, "Centroid " + (nodeIndex + 1));
				nodeIndex++;
				// System.out.println("Chart Panel: "+(x+1)+":"+(y+1));
			}
			y++;
		}
		return g;
	}
	
	private static CategoryDataset getDataset(Map som, int nodeIndex, String[] columns, ObjectRef timeCount) {
		BioStatisticalCategoryDataset dataset = new BioStatisticalCategoryDataset(10);
		// scan columns and create dataset
		HashSet<String> seriesAndTime = new HashSet<String>();
		for (int column = 0; column < som.getInputVectorSize(); column++) {
			String desc = columns[column];
			seriesAndTime.add(desc);
		}
		HashSet<Long> timeValues = new HashSet<Long>();
		for (String sat : seriesAndTime) {
			// row: time
			// column: series
			String series = sat.substring(0, sat.indexOf("§"));
			String time = sat.substring(sat.indexOf("§") + 1);
			
			ArrayList<Integer> columnsForThatSeriesAndTime = new ArrayList<Integer>();
			for (int column = 0; column < som.getInputVectorSize(); column++) {
				String desc = columns[column].substring(0, columns[column].lastIndexOf("§"));
				if (desc.equals(sat.substring(0, sat.lastIndexOf("§")))) {
					columnsForThatSeriesAndTime.add(column);
				}
			}
			ArrayList<ReplicateDouble> measurements = new ArrayList<ReplicateDouble>();
			for (int column : columnsForThatSeriesAndTime) {
				String replId = columns[column].substring(columns[column].lastIndexOf("§") + 1);
				double value = som.getWeights()[column][nodeIndex];
				measurements.add(new ReplicateDouble(value, replId, null, null));
			}
			double mean = ExperimentData.getAverage(measurements);
			double standardDeviation = ExperimentData.getStddev(measurements);
			// return new String(serie + "§" + timeUnit + "§" + getZeros(timeValueForComparision, 9)+"§"+replicate);
			String timeVal = time.substring(time.indexOf("§") + 1, time.lastIndexOf("§"));
			String timeUnit = time.substring(0, time.indexOf("§"));
			long ttt = Long.parseLong(timeVal);
			timeValues.add(ttt);
			dataset.add(mean, standardDeviation, series, ttt, false, false, "relative", timeUnit, true, false);
			// dataset.addValue(som.getWeights()[i][nodeIndex], "som-weights", columns[i].substring(0, columns[i].lastIndexOf("§")));
		}
		timeCount.setObject(timeValues.size());
		return dataset;
	}
	
	private static ArrayList<DataSetRow> getDatasetRows(Map som, int nodeIndex, String[] columns, ObjectRef timeCount) {
		System.out.println(DataSetRow.getHeading());
		ArrayList<DataSetRow> dataset = new ArrayList<DataSetRow>();
		// scan columns and create dataset
		HashSet<String> seriesAndTime = new HashSet<String>();
		for (int column = 0; column < som.getInputVectorSize(); column++) {
			String desc = columns[column];
			seriesAndTime.add(desc);
		}
		HashSet<Long> timeValues = new HashSet<Long>();
		HashMap<String, Integer> series2seriesID = new HashMap<String, Integer>();
		for (String sat : seriesAndTime) {
			// row: time
			// column: series
			String series = sat.substring(0, sat.indexOf("§"));
			String time = sat.substring(sat.indexOf("§") + 1);
			
			ArrayList<Integer> columnsForThatSeriesAndTime = new ArrayList<Integer>();
			for (int column = 0; column < som.getInputVectorSize(); column++) {
				String desc = columns[column].substring(0, columns[column].lastIndexOf("§"));
				if (desc.equals(sat.substring(0, sat.lastIndexOf("§")))) {
					columnsForThatSeriesAndTime.add(column);
				}
			}
			if (!series2seriesID.containsKey(series))
				series2seriesID.put(series, series2seriesID.size() + 1);
			int seriesId = series2seriesID.get(series);
			String timeVal = time.substring(time.indexOf("§") + 1, time.lastIndexOf("§"));
			String timeUnit = time.substring(0, time.indexOf("§"));
			String species = series.indexOf("/") >= 0 ? series.substring(0, series.indexOf("/")) : series;
			String genotype = series.indexOf("/") >= 0 ? series.substring(series.indexOf("/") + 1) : "";
			long ttt = Long.parseLong(timeVal);
			timeValues.add(ttt);
			for (int column : columnsForThatSeriesAndTime) {
				String replIdS = columns[column].substring(columns[column].lastIndexOf("§") + 1);
				int replId = -1;
				try {
					replId = Integer.parseInt(replIdS);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				double value = som.getWeights()[column][nodeIndex];
				// measurements.add(new ReplicateDouble(value, replId, null));
				DataSetRow dsr = new DataSetRow("", "experiment", "Centroid " + (nodeIndex + 1), "1", species, genotype, "", seriesId, timeVal, timeUnit, replId,
						value, "relative");
				System.out.println(dsr.toString());
				dataset.add(dsr);
			}
		}
		timeCount.setObject(timeValues.size());
		return dataset;
	}
}
