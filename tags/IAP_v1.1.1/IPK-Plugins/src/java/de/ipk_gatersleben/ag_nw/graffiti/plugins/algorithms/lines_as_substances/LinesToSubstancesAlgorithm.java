/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.lines_as_substances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.PositionGridGenerator;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.DataSetRow;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.DataSetTable;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

/**
 * @author Christian Klukas
 *         10.7.2006
 */
public class LinesToSubstancesAlgorithm
					extends AbstractAlgorithm {
	private static int lines2substanceCallCount;
	
	public String getName() {
		return null;// "Create Dataset (treat Lines as Substances)";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"Create a new dataset, where for each line a new node<br>" +
							"is created, which shows all of the different substance<br>" +
							"measurements in this single graph node.";
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
							new BackgroundTaskStatusProviderSupportingExternalCallImpl("<html>Create Line -&gt; Substance Dataset", "Please wait...");
		
		final List<NodeHelper> workNodes;
		if (MainFrame.getInstance().getActiveEditorSession().getGraph() == graph)
			workNodes = GraphHelper.getSelectedOrAllHelperNodes(MainFrame.getInstance().getActiveEditorSession());
		else
			workNodes = GraphHelper.getHelperNodes(graph);
		BackgroundTaskHelper.issueSimpleTask("<html>Line -&gt; Substance View",
							"Create Dataset",
							new Runnable() {
								public void run() {
									createLine2SubstanceView(status, workNodes, graph);
								}
							},
							null, status);
	}
	
	private static void createLine2SubstanceView(
						BackgroundTaskStatusProviderSupportingExternalCall status,
						List<NodeHelper> workNodes,
						Graph graph) {
		lines2substanceCallCount++;
		ArrayList<DataSetRow> mappingData = new ArrayList<DataSetRow>();
		for (Node n : workNodes) {
			NodeHelper nh = new NodeHelper(n);
			DataSetTable dst = nh.getDatasetTable();
			mappingData.addAll(dst.getRows());
		}
		TreeSet<String> substanceNames = new TreeSet<String>();
		for (DataSetRow dsr : mappingData) {
			if (!substanceNames.contains(dsr.substanceName))
				substanceNames.add(dsr.substanceName);
		}
		HashSet<String> newSubstanceNames = new HashSet<String>();
		ArrayList<String> substanceNamesSorted = new ArrayList<String>(substanceNames);
		for (DataSetRow dsr : mappingData) {
			String oldSubstanceName = dsr.substanceName;
			dsr.substanceName = dsr.getSeriesFromSpeciesAndGenotype();
			dsr.setSpeciesGenotypeAndTreatment(oldSubstanceName);
			dsr.seriesId = substanceNamesSorted.indexOf(oldSubstanceName) + 1;
			newSubstanceNames.add(dsr.substanceName);
		}
		final Graph tg = new AdjListGraph(new ListenerManager());
		tg.setName("Lines2Substances Graph " + lines2substanceCallCount);
		PositionGridGenerator pgg = new PositionGridGenerator(130, 130, 800);
		for (String substance : newSubstanceNames) {
			Node n = tg.addNode();
			NodeHelper nh = new NodeHelper(n);
			nh.setPosition(pgg.getNextPosition());
			nh.setLabel(substance);
			nh.setLabelFontSize(20, false);
			nh.setAttributeValue("labelgraphics", "anchor", "t");
			nh.setAttributeValue("graphics", "component", "chart2d_type3");
			nh.setSize(120, 120);
			nh.addDataMapping(mappingData, substance);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame.getInstance().showGraph(tg, null);
			}
		});
	}
	
	// private static void processNodeDesign(Graph ratioGraph,
	// NodeHelper nh1,
	// NodeHelper nh2,
	// NodeHelper ratioNodeHelper,
	// double nodeHeight, double nodeWidth) {
	// ratioNodeHelper.setClusterID(nh1.getClusterID("")+"/"+nh2.getClusterID(""));
	// ratioNodeHelper.setBorderWidth(1);
	// ratioNodeHelper.setRounding(5);
	// ratioNodeHelper.setSize(nodeWidth, nodeHeight);
	// ratioNodeHelper.setAttributeValue("charting", "rangeAxis", " ");
	// if (nh1.getGraphNode()==nh2.getGraphNode()) {
	// ratioNodeHelper.setAttributeValue("labelgraphics", "anchor", "c");
	// ratioNodeHelper.setAttributeValue("graphics", "component", "hidden");
	// ratioNodeHelper.setLabel("<html><center>"+nh1.getLabel());
	// ratioNodeHelper.setLabelFontSize(20, false);
	// } else {
	// ratioNodeHelper.setAttributeValue("labelgraphics", "anchor", "t");
	// }
	// // ratioNodeHelper.setAttributeValue("labelgraphics", "fontSize", new Integer(12));
	// // ratioNodeHelper.setAttributeValue("graphics", "component", "chart2d_type3");
	// // ratioNodeHelper.setAttributeValue("charting", "empty_border_width", new Double(3d));
	// }
}
