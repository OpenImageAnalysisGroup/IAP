/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.fast_view;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.FolderPanel;
import org.MarkComponent;
import org.StringManipulationTools;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.AttributeEvent;
import org.graffiti.event.EdgeEvent;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.NodeEvent;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.managers.AttributeComponentManager;
import org.graffiti.plugin.inspector.InspectorTab;
import org.graffiti.plugin.view.AttributeComponent;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphView;
import org.graffiti.plugin.view.MessageListener;
import org.graffiti.selection.Selection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAndSelecAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchAttributeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

/**
 * @author Christian Klukas
 *         21.12.2006
 */
public class FastView
					extends JComponent
					implements Printable, GraphView {
	
	private static final long serialVersionUID = 1L;
	
	private Graph graph;
	
	JLabel graphName = new JLabel("no graph assigned!");
	JLabel graphDirected = new JLabel("");
	JLabel graphNodeEdgeCnt = new JLabel("");
	JLabel graphNodeDegree = new JLabel("");
	
	JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(640, 50, Integer.MAX_VALUE, 1));
	JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(480, 50, Integer.MAX_VALUE, 1));
	
	HistogramDataset histogramData = new HistogramDataset();
	
	ChartPanel cp = null;
	JFreeChart chart = null;
	
	MarkComponent markThis;
	ArrayList<MarkComponent> unMarkThese = new ArrayList<MarkComponent>();
	
	HashMap<GraphElement, Integer> elementsOfSeriesOne2bin = new HashMap<GraphElement, Integer>();
	HashMap<GraphElement, Integer> elementsOfSeriesTwo2bin = new HashMap<GraphElement, Integer>();
	
	public ChartPanel getChart() {
		return cp;
	}
	
	public int getChartWidth() {
		return cp.getWidth();
		
	}
	
	public int getChartHeight() {
		return cp.getHeight();
	}
	
	public FastView() {
		super();
		
		int border = 5;
		this.setLayout(TableLayout.getLayout(
							TableLayoutConstants.PREFERRED,
							new double[] {
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED,
												TableLayoutConstants.PREFERRED

							}));
		
		FolderPanel fp = new FolderPanel("Graph Information", false, null, null);
		fp.addCollapseListenerDialogSizeUpdate();
		fp.addGuiComponentRow(new JLabel("Graph Name "), graphName, false);
		fp.addGuiComponentRow(new JLabel("Directed Graph? "), graphDirected, false);
		fp.addGuiComponentRow(new JLabel("Number of Nodes, Edges "), graphNodeEdgeCnt, false);
		fp.addGuiComponentRow(new JLabel("Average Node Degree "), graphNodeDegree, false);
		fp.layoutRows();
		
		final JSpinner binCnt = new JSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
		final JSpinner minVal = new JSpinner(new SpinnerNumberModel(0d, null, null, 0.5d));
		final JSpinner maxVal = new JSpinner(new SpinnerNumberModel(0d, null, null, 0.5d));
		
		final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		final JComboBox attributeSelection = new JComboBox(comboModel);
		
		final JCheckBox considerNodesAttribute = new JCheckBox("Evaluate Node Attribute Values", true);
		final JCheckBox considerEdgesAttribute = new JCheckBox("Evaluate Edge Attribute Values", false);
		
		final JCheckBox showAllData = new JCheckBox("Show All Data", true);
		final JCheckBox showSelectionData = new JCheckBox("Show Data For Selection", true);
		showAllData.setOpaque(false);
		showSelectionData.setOpaque(false);
		
		considerNodesAttribute.setOpaque(false);
		considerEdgesAttribute.setOpaque(false);
		
		JButton setMinMax = new JButton("Get Min/Max from Data");
		setMinMax.setOpaque(false);
		setMinMax.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AttributePathNameSearchType sel = (AttributePathNameSearchType) attributeSelection.getSelectedItem();
				if (sel == null) {
					minVal.setValue(0d);
					maxVal.setValue(100d);
					binCnt.setValue(10);
				} else {
					Collection<GraphElement> selectedOrAllGraphElements = GraphHelper.getSelectedOrAllGraphElements(getGraph());
					if (selectedOrAllGraphElements != null && selectedOrAllGraphElements.size() > 0) {
						double[] values = getValues(sel, selectedOrAllGraphElements, considerNodesAttribute.isSelected(), considerEdgesAttribute.isSelected(), null);
						double min = Double.MAX_VALUE;
						double max = Double.NEGATIVE_INFINITY;
						if (values == null || values.length == 0) {
							minVal.setValue(0d);
							maxVal.setValue(100d);
							binCnt.setValue(10);
						} else {
							for (double v : values) {
								if (v < min)
									min = v;
								if (v > max)
									max = v;
							}
							minVal.setValue(min);
							maxVal.setValue(max);
							int cnt = (int) max - (int) min + 1;
							while (cnt > 100)
								cnt = (int) (cnt / 10d);
							if (cnt <= 1)
								cnt = 10;
							binCnt.setValue(cnt);
						}
					}
				}
				
			}
		});
		
		JButton refreshAttributeList = new JButton("Refresh");
		refreshAttributeList.setOpaque(false);
		refreshAttributeList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SearchAttributeHelper sah = new SearchAttributeHelper();
				ArrayList<AttributePathNameSearchType> possibleAttributes = new ArrayList<AttributePathNameSearchType>();
				try {
					sah.prepareSearch();
					if (considerEdgesAttribute.isSelected() && considerNodesAttribute.isSelected())
						SearchAndSelecAlgorithm.enumerateAllAttributes(possibleAttributes, graph, SearchType.getSetOfNumericSearchTypes());
					else {
						if (considerEdgesAttribute.isSelected())
							SearchAndSelecAlgorithm.enumerateAttributes(possibleAttributes, graph.getEdges(), SearchType.getSetOfNumericSearchTypes());
						if (considerNodesAttribute.isSelected())
							SearchAndSelecAlgorithm.enumerateAttributes(possibleAttributes, graph.getNodes(), SearchType.getSetOfNumericSearchTypes());
					}
				} finally {
					sah.restoreDefintions();
				}
				
				comboModel.removeAllElements();
				System.out.println("Attributes: " + possibleAttributes.size());
				for (AttributePathNameSearchType o : possibleAttributes)
					comboModel.addElement(o);
			}
		});
		
		chart = ChartFactory.createHistogram(null, null, null, histogramData, PlotOrientation.VERTICAL, false, true, false);
		chart.setAntiAlias(true);
		chart.setBackgroundPaint(Color.WHITE);
		
		cp = new ChartPanel(chart, 400, 400, 100, 50, 60000, 60000, true, false, true, true, true, true);
		
		cp.enableMouseClickProcessing();
		
		final JButton updateData = new JButton("Update View");
		
		addMouseListenerToChartPanel(updateData);
		
		setChartSize();
		
		// cp.setOpaque(true);
		// cp.setBackground(Color.white);
		
		attributeSelection.setOpaque(false);
		
		final MyColorButton colorAll = new MyColorButton("Bar Color", Color.GRAY);
		final MyColorButton colorSel = new MyColorButton("Bar Color", Color.RED);
		
		updateData.setOpaque(false);
		updateData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AttributePathNameSearchType sel = (AttributePathNameSearchType) attributeSelection.getSelectedItem();
				histogramData.clearDataset();
				if (sel == null) {
					setChartSize();
				} else {
					elementsOfSeriesOne2bin.clear();
					elementsOfSeriesTwo2bin.clear();
					if (showAllData.isSelected()) {
						Collection<GraphElement> selectedOrAllGraphElements = graph.getGraphElements();
						if (selectedOrAllGraphElements != null && selectedOrAllGraphElements.size() > 0) {
							ArrayList<GraphElement> processedElements = new ArrayList<GraphElement>();
							double[] values = getValues(sel, selectedOrAllGraphElements, considerNodesAttribute.isSelected(), considerEdgesAttribute.isSelected(),
												processedElements);
							int[] bins = histogramData.addSeries("All Data", values, (Integer) binCnt.getValue(), (Double) minVal.getValue(), (Double) maxVal
												.getValue());
							for (int i = 0; i < bins.length; i++) {
								elementsOfSeriesOne2bin.put(processedElements.get(i), bins[i]);
							}
						}
					}
					boolean hasSelectionData = false;
					if (showSelectionData.isSelected()) {
						Collection<GraphElement> selectedOrAllGraphElements = GraphHelper.getSelectedOrAllGraphElements(getGraph());
						hasSelectionData = selectedOrAllGraphElements.size() < getGraph().getNumberOfNodes() + getGraph().getNumberOfEdges();
						if (selectedOrAllGraphElements != null && selectedOrAllGraphElements.size() > 0) {
							ArrayList<GraphElement> processedElements = new ArrayList<GraphElement>();
							double[] values = getValues(sel, selectedOrAllGraphElements, considerNodesAttribute.isSelected(), considerEdgesAttribute.isSelected(),
												processedElements);
							int[] bins = histogramData.addSeries("Selection Data", values, (Integer) binCnt.getValue(), (Double) minVal.getValue(), (Double) maxVal
												.getValue());
							for (int i = 0; i < bins.length; i++) {
								elementsOfSeriesTwo2bin.put(processedElements.get(i), bins[i]);
							}
						}
					}
					final boolean switchFirst = hasSelectionData;
					
					chart = ChartFactory.createHistogram(
										StringManipulationTools.removeTags(StringManipulationTools.removeHTMLtags(sel.toString() + "$"), " - ", "$").trim(),
										"Value Range", "Frequency", histogramData,
										PlotOrientation.VERTICAL, false, true, false);
					chart.getXYPlot().setDrawingSupplier(new DrawingSupplier() {
						DefaultDrawingSupplier dds = new DefaultDrawingSupplier();
						boolean first = true;
						
						public Paint getNextOutlinePaint() {
							return dds.getNextOutlinePaint();
						}
						
						public Stroke getNextOutlineStroke() {
							return dds.getNextOutlineStroke();
						}
						
						public Paint getNextPaint() {
							if (switchFirst)
								first = !first;
							else
								first = false;
							if ((first && showAllData.isSelected()) || ((!first && !showAllData.isSelected())))
								return colorSel.getSelectedColor();
							else
								return colorAll.getSelectedColor();
						}
						
						public Shape getNextShape() {
							return dds.getNextShape();
						}
						
						public Stroke getNextStroke() {
							return dds.getNextStroke();
						}
					});
					
					chart.setAntiAlias(true);
					chart.setBackgroundPaint(Color.WHITE);
					((XYPlot) chart.getPlot()).getDomainAxis().setRange((Double) minVal.getValue(), (Double) maxVal.getValue());
					cp.setChart(chart);
					cp.setOpaque(true);
					cp.setBackground(Color.white);
					setChartSize();
				}
				// FolderPanel.performDialogResize(cp);
			}
		});
		
		MarkComponent refreshMark = new MarkComponent(refreshAttributeList, true, TableLayoutConstants.PREFERRED, false);
		MarkComponent attSelMark = new MarkComponent(attributeSelection, false, TableLayoutConstants.PREFERRED, false);
		MarkComponent minMaxMark = new MarkComponent(setMinMax, false, TableLayoutConstants.PREFERRED, false);
		MarkComponent updateMark = new MarkComponent(updateData, false, TableLayoutConstants.FILL, false);
		
		MarkComponent.initLinearMarkSequence(attSelMark,
							refreshMark, attSelMark, minMaxMark, updateMark);
		
		markThis = minMaxMark;
		unMarkThese.clear();
		unMarkThese.add(refreshMark);
		unMarkThese.add(attSelMark);
		unMarkThese.add(updateMark);
		
		FolderPanel fps = new FolderPanel("Attribute Value Histogram Settings", false, null, null);
		fps.addCollapseListenerDialogSizeUpdate();
		fps.addGuiComponentRow(new JLabel("Consider Nodes Attributes "), considerNodesAttribute, false);
		fps.addGuiComponentRow(new JLabel("Consider Edges Attributes "), considerEdgesAttribute, false);
		fps.addGuiComponentRow(new JLabel("Attribute "),
							TableLayout.getSplit(
												refreshMark,
												attSelMark, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL),
							false);
		fps.addGuiComponentRow(new JLabel("Minimum / Maximum Value "),
							TableLayout.get3Split(minMaxMark, minVal, maxVal, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.FILL),
							false);
		fps.addGuiComponentRow(new JLabel("Number of Groups "), binCnt, false);
		
		fps.addGuiComponentRow(new JLabel("Show Data for whole Graph "), TableLayout.get3Split(showAllData, null, colorAll, TableLayoutConstants.PREFERRED,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED), false);
		fps.addGuiComponentRow(new JLabel("Show Data for Selection "), TableLayout.get3Split(showSelectionData, null, colorSel, TableLayoutConstants.PREFERRED,
							TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED), false);
		fps.addGuiComponentRow(new JLabel("Size of Chart "),
							TableLayout.get3Split(widthSpinner, new JLabel(" x "), heightSpinner, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED,
												TableLayoutConstants.FILL)
							, false);
		fps.addGuiComponentRow(new JLabel("Update View"), TableLayout.getSplitVertical(
							updateMark,
							new JLabel("  click diagram bars, to select related graph elements)"),
							TableLayoutConstants.PREFERRED, TableLayoutConstants.PREFERRED), false);
		fps.layoutRows();
		
		add(
							TableLayout.getSplit(
												FolderPanel.getBorderedComponent(fp, border, border, border, border),
												new JLabel(""), TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL),
							"0,0");
		
		add(
							TableLayout.getSplit(
												FolderPanel.getBorderedComponent(fps, border, border, border, border),
												new JLabel(""), TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL),
							"0,1");
		
		// FolderPanel fph = new FolderPanel("Histogram (Click diagram bars, to select related graph elements)", false, null, null);
		// fph.addCollapseListenerDialogSizeUpdate();
		// fph.addGuiComponentRow(new JLabel(""), updateData, false);
		// JComponent cpb = FolderPanel.getBorderedComponent(cp, 5, 0, 0, 0);
		// cpb.setOpaque(true);
		// cpb.setBackground(Color.WHITE);
		// fph.addGuiComponentRow(new JLabel(""), TableLayout.getSplit(cpb, new JLabel(), TableLayout.PREFERRED, TableLayout.FILL), false);
		// fph.layoutRows();
		// add(FolderPanel.getBorderedComponent(fph, border, border, border, border), "0,2");
		add(
							TableLayout.getSplit(
												FolderPanel.getBorderedComponent(cp, border, border, border, border),
												new JLabel(""), TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL),
							"0,2");
		// setOpaque(true);
		// setBackground(Color.white);
		validate();
	}
	
	private void addMouseListenerToChartPanel(final JButton updateButton) {
		cp.setMinimumDrawHeight(50);
		cp.setMinimumDrawWidth(50);
		cp.addChartMouseListener(new ChartMouseListener() {
			public void chartMouseClicked(ChartMouseEvent event) {
				// System.out.println(event.toString());
				if (event.getEntity() != null && event.getEntity() instanceof XYItemEntity) {
					XYItemEntity ent = (XYItemEntity) event.getEntity();
					int idx = ent.getItem();
					ArrayList<GraphElement> newSelection = new ArrayList<GraphElement>();
					if (ent.getSeriesIndex() == 0) {
						for (GraphElement g : elementsOfSeriesOne2bin.keySet()) {
							int bin = elementsOfSeriesOne2bin.get(g);
							if (bin == idx)
								newSelection.add(g);
						}
					} else {
						for (GraphElement g : elementsOfSeriesTwo2bin.keySet()) {
							int bin = elementsOfSeriesTwo2bin.get(g);
							if (bin == idx)
								newSelection.add(g);
						}
					}
					Selection s = MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection();
					HashSet<GraphElement> currentSel = new HashSet<GraphElement>(s.getElements());
					for (GraphElement ge : newSelection) {
						if (currentSel.contains(ge))
							s.remove(ge);
						else
							s.add(ge);
					}
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
					updateButton.doClick();
				}
			}
			
			public void chartMouseMoved(ChartMouseEvent event) {
				//
				
			}
		});
	}
	
	protected double[] getValues(AttributePathNameSearchType sel, Collection<GraphElement> selectedOrAllGraphElements,
						boolean considerNodes, boolean considerEdges, ArrayList<GraphElement> processedElements) {
		ArrayList<Double> res = new ArrayList<Double>();
		if (sel != null && selectedOrAllGraphElements != null)
			for (GraphElement ge : selectedOrAllGraphElements) {
				if (!considerEdges && ge instanceof Edge)
					continue;
				if (!considerNodes && ge instanceof Node)
					continue;
				double val = sel.getAttributeValue(ge, Double.NaN);
				if (!Double.isNaN(val)) {
					res.add(val);
					if (processedElements != null)
						processedElements.add(ge);
				}
			}
		
		double[] result = new double[res.size()];
		for (int i = 0; i < res.size(); i++)
			result[i] = res.get(i);
		
		return result;
	}
	
	public void setAttributeComponentManager(AttributeComponentManager acm) {
		// Method is regularly called
		// System.err.println("FastView: setAttributeComponentManager");
	}
	
	public Map<?, ?> getComponentElementMap() {
		// System.err.println("FastView: getComponentElementMap");
		return null;
	}
	
	public GraphElementComponent getComponentForElement(GraphElement ge) {
		// System.err.println("FastView: getComponentForElement");
		return null;
	}
	
	public void setGraph(Graph graph) {
		this.graph = graph;
		if (graph != null)
			completeRedraw();
	}
	
	public JComponent getViewComponent() {
		// Method is regularly called
		
		return this;
	}
	
	public String getViewName() {
		return "Statistics View";
	}
	
	public void addMessageListener(MessageListener ml) {
		// Method is regularly called
		// System.err.println("FastView: addMessageListener");
	}
	
	public void close() {
		// Method is regularly called
		// System.err.println("FastView: close");
	}
	
	public void completeRedraw() {
		
		graphName.setText(graph.getName());
		if (graph.isDirected())
			graphDirected.setText("Yes");
		else
			graphDirected.setText("No");
		graphNodeEdgeCnt.setText(graph.getNumberOfNodes() + ", " + graph.getNumberOfEdges());
		double degreeSum = 0;
		for (Node n : graph.getNodes())
			degreeSum += n.getDegree();
		if (graph.getNumberOfNodes() > 0) {
			double avgDegree = degreeSum / graph.getNumberOfNodes();
			graphNodeDegree.setText(StringManipulationTools.formatNumber(avgDegree, "#.##") + "");
		} else
			graphNodeDegree.setText("");
		
		repaint();
	}
	
	public void removeMessageListener(MessageListener ml) {
		// System.err.println("FastView: removeMessageListener");
	}
	
	public void repaint(GraphElement ge) {
		// System.err.println("FastView: repaint");
	}
	
	public void postEdgeAdded(GraphEvent e) {
		// System.err.println("FastView: postEdgeAdded");
	}
	
	public void postEdgeRemoved(GraphEvent e) {
		// System.err.println("FastView: postEdgeRemoved");
	}
	
	public void postGraphCleared(GraphEvent e) {
		// System.err.println("FastView: postGraphCleared");
	}
	
	public void postNodeAdded(GraphEvent e) {
		// System.err.println("FastView: postNodeAdded");
	}
	
	public void postNodeRemoved(GraphEvent e) {
		// System.err.println("FastView: postNodeRemoved");
	}
	
	public void preEdgeAdded(GraphEvent e) {
		// System.err.println("FastView: preEdgeAdded");
	}
	
	public void preEdgeRemoved(GraphEvent e) {
		// System.err.println("FastView: preEdgeRemoved");
	}
	
	public void preGraphCleared(GraphEvent e) {
		// System.err.println("FastView: preGraphCleared");
	}
	
	public void preNodeAdded(GraphEvent e) {
		// System.err.println("FastView: preNodeAdded");
	}
	
	public void preNodeRemoved(GraphEvent e) {
		// System.err.println("FastView: preNodeRemoved");
	}
	
	public void postInEdgeAdded(NodeEvent e) {
		// System.err.println("FastView: postInEdgeAdded");
	}
	
	public void postInEdgeRemoved(NodeEvent e) {
		// System.err.println("FastView: postInEdgeRemoved");
	}
	
	public void postOutEdgeAdded(NodeEvent e) {
		// System.err.println("FastView: postOutEdgeAdded");
	}
	
	public void postOutEdgeRemoved(NodeEvent e) {
		// System.err.println("FastView: postOutEdgeRemoved");
	}
	
	public void postUndirectedEdgeAdded(NodeEvent e) {
		// System.err.println("FastView: postUndirectedEdgeAdded");
	}
	
	public void postUndirectedEdgeRemoved(NodeEvent e) {
		// System.err.println("FastView: postUndirectedEdgeRemoved");
	}
	
	public void preInEdgeAdded(NodeEvent e) {
		// System.err.println("FastView: preInEdgeAdded");
	}
	
	public void preInEdgeRemoved(NodeEvent e) {
		// System.err.println("FastView: preInEdgeRemoved");
	}
	
	public void preOutEdgeAdded(NodeEvent e) {
		// System.err.println("FastView: preOutEdgeAdded");
	}
	
	public void preOutEdgeRemoved(NodeEvent e) {
		// System.err.println("FastView: preOutEdgeRemoved");
	}
	
	public void preUndirectedEdgeAdded(NodeEvent e) {
		// System.err.println("FastView: preUndirectedEdgeAdded");
	}
	
	public void preUndirectedEdgeRemoved(NodeEvent e) {
		// System.err.println("FastView: preUndirevtedEdgeRemoved");
	}
	
	public void postDirectedChanged(EdgeEvent e) {
		// System.err.println("FastView: postDirectedChanged");
	}
	
	public void postEdgeReversed(EdgeEvent e) {
		// System.err.println("FastView: postEdgeReverser");
	}
	
	public void postSourceNodeChanged(EdgeEvent e) {
		// System.err.println("FastView: postSourceNodeChanged");
	}
	
	public void postTargetNodeChanged(EdgeEvent e) {
		// System.err.println("FastView: postTargetNodeChanged");
	}
	
	public void preDirectedChanged(EdgeEvent e) {
		// System.err.println("FastView: preDirectedChanged");
	}
	
	public void preEdgeReversed(EdgeEvent e) {
		// System.err.println("FastView: preEdgeReversed");
	}
	
	public void preSourceNodeChanged(EdgeEvent e) {
		// System.err.println("FastView: preSourceNodeChanged");
	}
	
	public void preTargetNodeChanged(EdgeEvent e) {
		// System.err.println("FastView: preTargetNodeChanged");
	}
	
	public void postAttributeAdded(AttributeEvent e) {
		// System.err.println("FastView: postAttributeAdded: "+e.getPath()+" / "+e.getAttribute().getName());
	}
	
	public void postAttributeChanged(AttributeEvent e) {
		// System.err.println("FastView: postAttributeChanged");
	}
	
	public void postAttributeRemoved(AttributeEvent e) {
		// System.err.println("FastView: postAttributeRemoved");
	}
	
	public void preAttributeAdded(AttributeEvent e) {
		// System.err.println("FastView: preAttributeAdded");
	}
	
	public void preAttributeChanged(AttributeEvent e) {
		// System.err.println("FastView: preAttributeChaned");
	}
	
	public void preAttributeRemoved(AttributeEvent e) {
		// System.err.println("FastView: preAttributeRemoved");
	}
	
	public Insets getAutoscrollInsets() {
		// System.err.println("FastView: getAutoscrollInsets");
		return null;
	}
	
	public void autoscroll(Point cursorLocn) {
		// System.err.println("FastView: autoscroll");
	}
	
	public CollectionAttribute getEdgeAttribute() {
		// System.err.println("FastView: getEdgeAttribute");
		// Method is regularly called
		return null;
	}
	
	public CollectionAttribute getGraphAttribute() {
		// System.err.println("FastView: getGraphAttribute");
		return null;
	}
	
	public CollectionAttribute getNodeAttribute() {
		// Method is regularly called
		// System.err.println("FastView: getNodeAttribute");
		return null;
	}
	
	public void zoomChanged(AffineTransform newZoom) {
		// Method is regularly called
		// System.err.println("FastView: zoomChanged");
	}
	
	public AffineTransform getZoom() {
		return new AffineTransform();
	}
	
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		completeRedraw();
		if (markThis != null) {
			for (MarkComponent mc : unMarkThese)
				mc.setMark(false);
			markThis.setMark(true);
		}
		
	}
	
	public void transactionStarted(TransactionEvent e) {
		// System.err.println("FastView: transactionStarted");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.View#clearComponents()
	 */
	public void clearComponents() {
		// 
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public boolean putInScrollPane() {
		return true;
	}
	
	@Override
	public void print(Graphics g) {
		if (cp != null)
			cp.print(g);
		else
			super.print(g);
	}
	
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (cp == null) {
			MainFrame.showMessageDialog("Please create a Histogram, before starting the print operation.", "Error");
			return 0;
		} else
			return cp.print(graphics, pageFormat, pageIndex);
	}
	
	private void setChartSize() {
		cp.setMinimumSize(new Dimension((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue()));
		cp.setMaximumSize(new Dimension((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue()));
		cp.setPreferredSize(new Dimension((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue()));
		cp.setSize(new Dimension((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue()));
	}
	
	public Set<AttributeComponent> getAttributeComponentsForElement(
						GraphElement ge) {
		return new HashSet<AttributeComponent>();
	}
	
	public boolean isHidden(GraphElement ge) {
		return false;
	}
	
	public JComponent getViewToolbarComponentTop() {
		return null;
	}
	
	public JComponent getViewToolbarComponentBottom() {
		return null;
	}
	
	public JComponent getViewToolbarComponentLeft() {
		return null;
	}
	
	public JComponent getViewToolbarComponentRight() {
		return null;
	}
	
	public JComponent getViewToolbarComponentBackground() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.View#closing(java.awt.event.WindowEvent)
	 */
	public void closing(AWTEvent e) {
		// empty
	}
	
	public boolean worksWithTab(InspectorTab tab) {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.view.Zoomable#redrawActive()
	 */
	public boolean redrawActive() {
		return false;
	}
}
