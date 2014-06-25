/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.08.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.statistics;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.AttributeHelper;
import org.BackgroundTaskStatusProvider;
import org.StringManipulationTools;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

public class MyCorrlationFinder implements BackgroundTaskStatusProvider, Runnable {
	
	private final Collection<Node> nodes;
	private final Graph graph;
	private final EditorSession session;
	private boolean pleaseStop = false;
	private double currentStatus = 0d;
	private boolean considerTimeShifts;
	private final boolean mergeDataset;
	private final boolean colorCodeEdgesWithCorrelationValue;
	private final double minimumR;
	private final int currGammaValue;
	private final Color colR_1;
	private final Color colR0;
	private final Color colR1;
	private final Collection<Edge> correlationEdges;
	private final double prob;
	private final boolean plotAverage;
	private String process = "";
	private final boolean rankOrder;
	private final boolean showStatusResult;
	private final boolean dontAddNewEdgesOnlyUpdateExisting;
	
	public MyCorrlationFinder(Collection<Node> nodes, Graph graph, EditorSession session, boolean considerTimeShifts,
			boolean mergeDataset, boolean colorCodeEdgesWithCorrelationValue, double minimumR, int currGammaValue,
			Color colR_1, Color colR0, Color colR1, Collection<Edge> correlationEdges, double prob, boolean plotAverage,
			boolean rankOrder, boolean showStatusResult, boolean dontAddNewEdgesOnlyUpdateExisting) {
		this.nodes = nodes;
		this.graph = graph;
		this.session = session;
		this.considerTimeShifts = considerTimeShifts;
		this.mergeDataset = mergeDataset;
		this.colorCodeEdgesWithCorrelationValue = colorCodeEdgesWithCorrelationValue;
		this.minimumR = minimumR;
		this.currGammaValue = currGammaValue;
		this.colR_1 = colR_1;
		this.colR0 = colR0;
		this.colR1 = colR1;
		this.correlationEdges = correlationEdges;
		this.prob = prob;
		this.plotAverage = plotAverage;
		this.rankOrder = rankOrder;
		this.showStatusResult = showStatusResult;
		this.dontAddNewEdgesOnlyUpdateExisting = dontAddNewEdgesOnlyUpdateExisting;
		if (dontAddNewEdgesOnlyUpdateExisting)
			this.considerTimeShifts = false;
	}
	
	@Override
	public int getCurrentStatusValue() {
		return (int) getCurrentStatusValueFine();
	}
	
	@Override
	public void setCurrentStatusValue(int value) {
		currentStatus = value;
	}
	
	@Override
	public double getCurrentStatusValueFine() {
		return currentStatus;
	}
	
	@Override
	public String getCurrentStatusMessage1() {
		return "Calculate Correlation Factors...";
	}
	
	@Override
	public String getCurrentStatusMessage2() {
		return (process == null || process.length() <= 0) ? "Please wait..." : process;
	}
	
	@Override
	public void pleaseStop() {
		pleaseStop = true;
	}
	
	@Override
	public boolean wantsToStop() {
		return pleaseStop;
	}
	
	@Override
	public boolean pluginWaitsForUser() {
		return false;
	}
	
	@Override
	public void pleaseContinueRun() {
	}
	
	@Override
	public void run() {
		List<ResultPair> result = new ArrayList<ResultPair>();
		ArrayList<Node> processed = new ArrayList<Node>();
		double max = nodes.size() * nodes.size() / 2d;
		double progress = 0;
		for (Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
			if (pleaseStop)
				break;
			Node node1 = it1.next();
			processed.add(node1);
			ExperimentInterface mappedDataList1 = Experiment2GraphHelper.getMappedDataListFromGraphElement(node1);
			String node1desc = AttributeHelper.getLabel(node1, "-unnamed-");
			process = "Correlate " + node1desc + " with remaining nodes...";
			for (Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
				if (pleaseStop)
					break;
				Node node2 = it2.next();
				if (processed.contains(node2))
					continue;
				progress = progress + 1;
				currentStatus = progress / max * 100d;
				String node2desc = AttributeHelper.getLabel(node2, "-unnamed-");
				ExperimentInterface mappedDataList2 = Experiment2GraphHelper.getMappedDataListFromGraphElement(node2);
				if (mappedDataList1 != null && mappedDataList2 != null) {
					if (node1 != node2) {
						if (considerTimeShifts) {
							int n = 0;
							MyXML_XYDataset dataset = initDataset(mappedDataList1, mappedDataList2);
							if (mergeDataset) {
								for (int s = 0; s < dataset.getSeriesCount(); s++)
									n += dataset.getItemCount(s);
							} else {
								n = Integer.MAX_VALUE;
								for (int s = 0; s < dataset.getSeriesCount(); s++)
									if (dataset.getItemCount(s) < n)
										n = dataset.getItemCount(s);
							}
							int minOff = -3;
							String rtimes = " [t=-3..3; ";
							ArrayList<CorrelationResult> res = new ArrayList<CorrelationResult>();
							for (int offset = minOff; offset <= -minOff; offset++) {
								mappedDataList1 = Experiment2GraphHelper.getMappedDataListFromGraphElement(node1);
								mappedDataList2 = Experiment2GraphHelper.getMappedDataListFromGraphElement(node2);
								dataset = initDataset(mappedDataList1, mappedDataList2);
								CorrelationResult correlation = TabStatistics.calculateCorrelation(dataset, node1desc,
										node2desc, mergeDataset, offset, prob, rankOrder);
								if (correlation.isAnyOneSignificant(minimumR)) {
									res.add(correlation);
									rtimes += "r*=" + correlation.getMaxOrMinR2() + ", ";
									
								} else
									rtimes += "r=" + correlation.getMaxOrMinR2() + ", ";
							}
							rtimes += "]";
							rtimes = StringManipulationTools.stringReplace(rtimes, ", ]", "]");
							if (res.size() > 0) {
								double maxR = Double.NEGATIVE_INFINITY;
								CorrelationResult highestCorrelation = null;
								for (CorrelationResult correlation : res) {
									if (Math.abs(correlation.getMaxR()) > maxR) {
										highestCorrelation = correlation;
										maxR = Math.abs(correlation.getMaxR());
									}
								}
								if (highestCorrelation != null) {
									String history = highestCorrelation.getCalculationHistoryForMaxR() + rtimes;
									highestCorrelation.setCalculationHistoryForMaxR(history);
									result.add(new ResultPair(node1, node2, highestCorrelation));
								}
							}
						} else {
							MyXML_XYDataset dataset = initDataset(mappedDataList1, mappedDataList2);
							CorrelationResult correlation = TabStatistics.calculateCorrelation(dataset, node1desc, node2desc,
									mergeDataset, 0, prob, rankOrder);
							if (correlation.isAnyOneSignificant(minimumR)) {
								result.add(new ResultPair(node1, node2, correlation));
							}
						}
					}
				}
			}
		}
		currentStatus = -1;
		if (dontAddNewEdgesOnlyUpdateExisting)
			process = "Update Existing Edges...";
		else
			process = "Create Edges...";
		List<ResultPair> correlated = result;
		graph.getListenerManager().transactionStarted(this);
		ArrayList<Edge> newEdges = new ArrayList<Edge>();
		for (Iterator<ResultPair> it = correlated.iterator(); it.hasNext();) {
			if (pleaseStop)
				break;
			ResultPair rp = it.next();
			Node a = (Node) rp.a;
			Node b = (Node) rp.b;
			Collection<Edge> newEdge = new ArrayList<Edge>();
			float r = rp.correlation.getMaxR();
			double truecorrprob = rp.correlation.getMaxTrueCorrProb();
			if (rp.correlation.getDataset2offsetOfMaxOrMinR() == 0) {
				if (dontAddNewEdgesOnlyUpdateExisting) {
					if (a.getNeighbors().contains(b)) {
						for (Edge nE : a.getEdges()) {
							if ((nE.getSource() == a && nE.getTarget() == b) || (nE.getSource() == b && nE.getTarget() == a)) {
								Color c = colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r, currGammaValue,
										colR_1, colR0, colR1) : Color.BLACK;
								AttributeHelper.setFillColor(nE, c);
								AttributeHelper.setOutlineColor(nE, c);
								newEdge.add(nE);
							}
						}
					}
				} else {
					Edge nE = graph.addEdge(a, b, false, AttributeHelper.getDefaultGraphicsAttributeForEdge(
							(colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r, currGammaValue, colR_1, colR0,
									colR1) : Color.BLACK), (colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r,
									currGammaValue, colR_1, colR0, colR1) : Color.BLACK), false));
					newEdge.add(nE);
				}
				// EdgeGraphicAttribute ega = (EdgeGraphicAttribute)
				// newEdge.getAttribute(GraphicAttributeConstants.GRAPHICS);
			} else {
				int offset = rp.correlation.getDataset2offsetOfMaxOrMinR();
				if (offset < 0) {
					Edge nE = graph.addEdge(b, a, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(
							(colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r, currGammaValue, colR_1, colR0,
									colR1) : Color.BLACK), (colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r,
									currGammaValue, colR_1, colR0, colR1) : Color.BLACK), true));
					newEdge.add(nE);
				} else {
					Edge nE = graph.addEdge(a, b, true, AttributeHelper.getDefaultGraphicsAttributeForEdge(
							(colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r, currGammaValue, colR_1, colR0,
									colR1) : Color.BLACK), (colorCodeEdgesWithCorrelationValue ? TabStatistics.getRcolor(r,
									currGammaValue, colR_1, colR0, colR1) : Color.BLACK), true));
					newEdge.add(nE);
				}
				if (offset == 0) {
					// nothing
				} else
					if (offset == -1 || offset == 1) {
						for (Edge nE : newEdge)
							AttributeHelper.setDashInfo(nE, 10f, 5f);
					} else
						if (offset == -2 || offset == 2) {
							for (Edge nE : newEdge)
								AttributeHelper.setDashInfo(nE, 10f, 10f);
						} else {
							for (Edge nE : newEdge)
								AttributeHelper.setDashInfo(nE, 10f, 20f);
						}
				for (Edge nE : newEdge)
					AttributeHelper.setAttribute(nE, "statistics", "correlation_offset", new Integer(offset));
			}
			if (newEdge != null) {
				if (showStatusResult)
					for (Edge nE : newEdge)
						AttributeHelper.setToolTipText(nE, rp.correlation.getCalculationHistoryForMaxR());
				if (!dontAddNewEdgesOnlyUpdateExisting)
					for (Edge nE : newEdge)
						correlationEdges.add(nE);
				newEdges.addAll(newEdge);
				if (colorCodeEdgesWithCorrelationValue) {
					for (Edge nE : newEdge) {
						AttributeHelper.setOutlineColor(nE, TabStatistics.getRcolor(r, currGammaValue, colR_1, colR0, colR1));
						AttributeHelper.setBorderWidth(nE, 5);
					}
				}
				for (Edge nE : newEdge) {
					AttributeHelper.setAttribute(nE, "statistics", "correlation_r", new Double(r));
					if (truecorrprob != Double.NaN)
					{
						double prob = 1d - truecorrprob;
						AttributeHelper.setAttribute(nE, "statistics", "correlation_prob", prob);
					}
				}
			}
		}
		Selection s = session.getSelectionModel().getActiveSelection();
		if (s == null)
			s = new Selection("new edges");
		s.addAll(newEdges);
		session.getSelectionModel().selectionChanged();
		graph.getListenerManager().transactionFinished(this);
		currentStatus = 100;
		if (pleaseStop)
			process = "Processing incomplete";
		
	}
	
	private MyXML_XYDataset initDataset(
			ExperimentInterface mappedDataList1,
			ExperimentInterface mappedDataList2) {
		Iterator<SubstanceInterface> itXml1 = mappedDataList1.iterator();
		Iterator<SubstanceInterface> itXml2 = mappedDataList2.iterator();
		MyXML_XYDataset dataset = new MyXML_XYDataset();
		int series = 0;
		while (itXml1.hasNext() && itXml2.hasNext()) {
			series++;
			SubstanceInterface xmldata1 = itXml1.next();
			SubstanceInterface xmldata2 = itXml2.next();
			dataset.addXmlDataSeries(xmldata1, xmldata2, "M" + series, plotAverage, null);
		}
		return dataset;
	}
	
	@Override
	public String getCurrentStatusMessage3() {
		return null;
	}
}
