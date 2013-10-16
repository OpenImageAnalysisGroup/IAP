/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.ratioview;

import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.FolderPanel;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.DataMappingId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         10.7.2006
 */
public class SubstanceRatioCalculationAlgorithm
					extends AbstractAlgorithm implements AlgorithmWithComponentDescription {
	
	public static int ratioCalculationCount = 0;
	
	private boolean useAverageValues = true;
	
	public String getName() {
		return "Substance Ratio Matrix...";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public String getDescription() {
		return "<html>Calculates the ratio between any two substances.<br>" +
							"<br>" +
							"<small>For the example three substances where selected. The<br>" +
							"resulting new graph view contains a n*n matrix which shows<br>" +
							"the ratio for the example time series data.<br><br>" +
							"<br>";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (graph == null)
			throw new PreconditionException("No graph available");
		if (graph.getNodes().size() <= 0)
			throw new PreconditionException("Graph contains no nodes");
		boolean foundData = false;
		for (Node n : graph.getNodes()) {
			if (Experiment2GraphHelper.getMappedDataListFromGraphElement(n) != null) {
				foundData = true;
				break;
			}
		}
		if (!foundData)
			throw new PreconditionException("Graph nodes have no data assigned");
	}
	
	@Override
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public void execute() {
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status =
							new BackgroundTaskStatusProviderSupportingExternalCallImpl("Create Substance-Ratio View", "Please wait...");
		
		final List<NodeHelper> workNodes;
		if (MainFrame.getInstance().getActiveEditorSession().getGraph() == graph)
			workNodes = GraphHelper.getSelectedOrAllHelperNodes(MainFrame.getInstance().getActiveEditorSession());
		else
			workNodes = GraphHelper.getHelperNodes(graph);
		BackgroundTaskHelper.issueSimpleTask("Substance-Ratio View",
							"Calculate substance ratios",
							new Runnable() {
								public void run() {
									createRatioView(status, workNodes, graph, useAverageValues, getActionEvent(), false);
								}
							},
							null, status);
	}
	
	public Graph executeAndReturnGraph(BackgroundTaskStatusProviderSupportingExternalCall status) {
		List<NodeHelper> workNodes = GraphHelper.getHelperNodes(graph);
		return createRatioView(status, workNodes, graph, useAverageValues, getActionEvent(), true);
	}
	
	private static Graph createRatioView(
						BackgroundTaskStatusProviderSupportingExternalCall status,
						List<NodeHelper> workNodes,
						Graph graph,
						boolean useAverageValues,
						final ActionEvent ae,
						boolean returnResultDontShow) {
		
		Integer overrideReplicateId = null;
		if (useAverageValues)
			overrideReplicateId = 0; // all replicates will have the same id, and thus this
		// algorithm will merge all values of the replicates
		// otherwise, only values with the same replicate id of one sample
		// would be merged (but normally the replicate ids are unique)
		
		SubstanceRatioCalculationAlgorithm.ratioCalculationCount++;
		
		final AdjListGraph ratioGraph = new AdjListGraph(new ListenerManager());
		ratioGraph.getListenerManager().transactionStarted(ratioGraph);
		if (status != null) {
			status.setCurrentStatusValueFine(0);
			status.setCurrentStatusText2("Create Substance-Ratio Dataset...");
		}
		int nidx = 0;
		double nHeight = 150;
		double nWidth = 150;
		double nSpace = 5;
		double columnStep = nWidth + nSpace;
		double columnPositionInit = 200d - columnStep;
		double columnPosition = columnPositionInit;
		
		double rowStep = nHeight + nSpace;
		double rowPosition = 200d - rowStep;
		
		double workLoad = workNodes.size();
		
		for (NodeHelper nh1 : workNodes) {
			// workGraph = nh1.getGraph();
			TreeMap<DataMappingId, Stack<Double>> id2value_n1 = nh1.getIdsAndValues(overrideReplicateId);
			if (status != null) {
				status.setCurrentStatusValueFine(100d * (nidx++) / workLoad);
				status.setCurrentStatusText2("Process Node " + (nidx) + "/" + (int) workLoad + "...");
			}
			rowPosition += rowStep;
			columnPosition = columnPositionInit;
			
			for (NodeHelper nh2 : workNodes) {
				if (status != null && status.wantsToStop()) {
					break;
				}
				columnPosition += columnStep;
				TreeMap<DataMappingId, Stack<Double>> id2value_n2 = nh2.getIdsAndValues(overrideReplicateId);
				Node ratioNode = ratioGraph.addNode(AttributeHelper.getDefaultGraphicsAttributeForNode(columnPosition, rowPosition));
				NodeHelper ratioNodeHelper = new NodeHelper(ratioNode, false);
				
				TreeSet<DataMappingId> allIds = new TreeSet<DataMappingId>();
				allIds.addAll(id2value_n1.keySet());
				allIds.addAll(id2value_n2.keySet());
				
				int added = 0;
				
				for (DataMappingId id : allIds) {
					Stack<Double> vl1 = id2value_n1.get(id);
					Stack<Double> vl2 = id2value_n2.get(id);
					if (vl1 != null && vl1.size() > 0 && vl2 != null && vl2.size() > 0) {
						double sum1 = 0;
						double sum2 = 0;
						for (Double v : vl1)
							sum1 += v;
						for (Double v : vl2)
							sum2 += v;
						double avgA = sum1 / vl1.size();
						double avgB = sum2 / vl2.size();
						double v = avgA / avgB;
						int plantID = ratioNodeHelper.memGetPlantID(
											id.getSpecies(),
											id.getGenoType(),
											null, null, null);
						added++;
						ratioNodeHelper.memSample(
											v, id.getReplicateId(),
											plantID,
											"ratio",
											id.getTimeUnit(),
											id.getTimePoint());
					}
				}
				
				if (added > 0)
					ratioNodeHelper.memAddDataMapping(
										"<html>" + nh1.getLabel() + "<hr>" + nh2.getLabel(),
										"ratio", AttributeHelper.getDateString(new Date()), "Ration View", "auto generated", "", "");
				ratioNodeHelper.setLabel("<html><center>" + nh1.getLabel() + "<hr>" + nh2.getLabel());
				processNodeDesign(ratioGraph, nh1, nh2, ratioNodeHelper, nHeight, nWidth);
			}
		}
		if (status != null && status.wantsToStop()) {
			status.setCurrentStatusText1("Processing incomplete");
			status.setCurrentStatusText2("Aborting further processing");
			return null;
		}
		
		AttributeHelper.setAttribute(ratioGraph, "", "node_showRangeAxis", new Boolean(true));
		// AttributeHelper.setAttribute(ratioGraph, "", "node_outlineBorderWidth", new Double(1d));
		// AttributeHelper.setAttribute(ratioGraph, "", "node_chartStdDevLineWidth", new Double(0d));
		// // AttributeHelper.setAttribute(ratioGraph, "", "node_plotOrientationHor", new Boolean(false));
		AttributeHelper.setAttribute(ratioGraph, "", "node_gridWidth", new Double(3d));
		AttributeHelper.setAttribute(ratioGraph, "", "node_axisWidth", new Double(3d));
		AttributeHelper.setAttribute(ratioGraph, "", "node_plotAxisFontSize", new Integer(30));
		AttributeHelper.setAttribute(ratioGraph, "", "node_showGridRange", new Boolean(true));
		// AttributeHelper.setAttribute(ratioGraph, "", "node_showGridCategory", new Boolean(true));
		
		ratioGraph.setName("Ratio Calculation " + SubstanceRatioCalculationAlgorithm.ratioCalculationCount);
		
		for (Node n1 : ratioGraph.getNodes()) {
			NodeHelper nh = new NodeHelper(n1);
			// nh.setAttributeValue("charting", "showRangeAxis", new Boolean(true));
			double spY = 4d;
			nh.setPosition(nh.getPosition().getX(), nh.getPosition().getY() - spY / 2);
			nh.setSize(nh.getWidth(), nh.getHeight() + spY);
		}
		status.setCurrentStatusValue(100);
		status.setCurrentStatusText2("Show Graph (please wait)...");
		if (!returnResultDontShow) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ratioGraph.getListenerManager().transactionFinished(ratioGraph);
					MainFrame.getInstance().showGraph(ratioGraph, ae);
				}
			});
			return null;
		} else {
			ratioGraph.getListenerManager().transactionFinished(ratioGraph);
			return ratioGraph;
		}
	}
	
	private static void processNodeDesign(Graph ratioGraph,
						NodeHelper nh1,
						NodeHelper nh2,
						NodeHelper ratioNodeHelper,
						double nodeHeight, double nodeWidth) {
		ratioNodeHelper.setClusterID(nh1.getClusterID("") + "/" + nh2.getClusterID(""));
		ratioNodeHelper.setBorderWidth(1);
		ratioNodeHelper.setRounding(5);
		ratioNodeHelper.setSize(nodeWidth, nodeHeight);
		ratioNodeHelper.setAttributeValue("charting", "rangeAxis", " ");
		if (nh1.getGraphNode() == nh2.getGraphNode()) {
			ratioNodeHelper.setAttributeValue("labelgraphics", "anchor", "c");
			ratioNodeHelper.setAttributeValue("graphics", "component", "hidden");
			ratioNodeHelper.setLabel("<html><center>" + nh1.getLabel());
			ratioNodeHelper.setLabelFontSize(20, false);
		} else {
			ratioNodeHelper.setAttributeValue("labelgraphics", "anchor", "t");
		}
		// ratioNodeHelper.setAttributeValue("labelgraphics", "fontSize", new Integer(12));
		// ratioNodeHelper.setAttributeValue("graphics", "component", "chart2d_type3");
		// ratioNodeHelper.setAttributeValue("charting", "empty_border_width", new Double(3d));
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(useAverageValues, " Use Average Values for Ratio-Calculation",
							"<html>" +
												"If selected, the average value for each sample will be used for the calculation<br>" +
												"of the ratio, instead of calculating the replicate rations (and displaying the average of them).") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		useAverageValues = ((BooleanParameter) params[i++]).getBoolean().booleanValue();
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/ratio.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
}
