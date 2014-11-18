/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.util.ArrayList;
import java.util.Collection;

import org.AttributeHelper;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.IntegerParameter;
import org.graffiti.plugin.parameter.ObjectListParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RemoveMappingDataAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.grid.GridLayouterAlgorithm;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class SplitNodeForSingleMappingData extends AbstractAlgorithm {
	
	String option1 = "Split nodes with multiple data-mappings";
	String option2 = "Split nodes with a degree over the specified threshold";
	String mCurrentMode = option1;
	ArrayList<String> optionList = new ArrayList<String>();
	private int mMinimumDegree = 2;
	private boolean mProcessSelfLoops = true;
	private boolean mRepositionOfNodes = false;
	
	public SplitNodeForSingleMappingData() {
		super();
		optionList.clear();
		optionList.add(option1);
		optionList.add(option2);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Split Nodes...";
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public String getDescription() {
		return "<html>" + "With this command nodes may be split into several nodes. In both modes all valid<br>"
							+ "nodes from the current selection will be processed (if a selection is made).<br>"
							+ "Otherwise all network nodes which contain either mapping data or have a degree<br>"
							+ "over the specified threshold will be processed. The source nodes and edges will<br>"
							+ "be removed. The resulting nodes will be placed at the same position as the source<br>"
							+ "node (if the layout option is disabled). You should modify the layout after<br>"
							+ "performing this command, to make the duplicated nodes visible in that case.<br><br>"
							+ "Two principle modes of operation for this command are supported:<ol>"
							+ "<li>You may either split nodes with multiple data-mappings into different<br>"
							+ "nodes, which will contain a single data-mapping. Example: if a source node<br>"
							+ "contains two datasets, two new nodes will be created, each one containg<br>"
							+ "only one of the previously assigned datasets. The connecting edges will be<br>"
							+ "duplicated, the newly created nodes will have the same connectivity to other<br>"
							+ "nodes as the source node.<hr>"
							+ "<li>The second mode splits nodes with a degree beeing greater than the<br>"
							+ "specified threshold into multiple nodes, so that the resulting node degree<br>"
							+ "of the node copies will be 1. This means, that there will be as many nodes<br>"
							+ "created as there is a connectivity to other nodes. Also self-loops may be<br>" + "processed.";
	}
	
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
							new ObjectListParameter(mCurrentMode, "Mode of operation", "Select one of the operation modes", optionList),
							new IntegerParameter(mMinimumDegree, "Mode 2", "<html>"
												+ "If selected, only nodes with a degree over the specified threshold are processed.<br>"
												+ "Only values greater than 1 are meaningful."),
							new BooleanParameter(mProcessSelfLoops, "Mode 2: Process self-loops", "<html>"
												+ "If selected, self loops are considered during the processing.<br>"
												+ "The newly created nodes will not contain any self-loops."),
							new BooleanParameter(mRepositionOfNodes, "Modify Layout", "<html>"
												+ "If selected, the multiple nodes will be placed next to each<br>" + "other in a grid.") };
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		mCurrentMode = (String) params[i++].getValue();
		mMinimumDegree = ((IntegerParameter) params[i++]).getInteger();
		mProcessSelfLoops = ((BooleanParameter) params[i++]).getBoolean();
		mRepositionOfNodes = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		
		if (mCurrentMode == option2 && mMinimumDegree <= 1) {
			MainFrame.showMessageDialog("<html>Cannot split nodes!<br>Please specify a node degree > 1.", "Error");
			return;
		}
		
		Collection<Node> workNodes = new ArrayList<Node>(getSelectedOrAllNodes());
		graph.getListenerManager().transactionStarted(this);
		int newNodes = 0;
		int newEdges = 0;
		int removedNodes = 0;
		int removedEdges = 0;
		for (Node graphNode : workNodes) {
			if (mCurrentMode.equalsIgnoreCase(option1)) {
				Collection<SubstanceInterface> mappingList = Experiment2GraphHelper
									.getMappedDataListFromGraphElement(graphNode);
				if (mappingList != null) {
					if (mappingList.size() > 1) {
						ArrayList<Node> nodelist = new ArrayList<Node>();
						for (SubstanceInterface mappingData : mappingList) {
							Node newNode = graph.addNodeCopy(graphNode);
							nodelist.add(newNode);
							newNodes++;
							RemoveMappingDataAlgorithm.removeMappingDataFrom(newNode);
							Experiment2GraphHelper.addMappingData2Node(mappingData, newNode);
							for (Edge e : graphNode.getEdges()) {
								Node source, target;
								if (e.getSource() == graphNode)
									source = newNode;
								else
									source = e.getSource();
								if (e.getTarget() == graphNode)
									target = newNode;
								else
									target = e.getTarget();
								graph.addEdgeCopy(e, source, target);
								newEdges++;
							}
						}
						removedEdges += graphNode.getEdges().size();
						graph.deleteNode(graphNode);
						removedNodes++;
						processLayout(nodelist, mRepositionOfNodes);
					}
				}
			}
			if (mCurrentMode.equalsIgnoreCase(option2)) {
				SplitResult res = splitNodes(graphNode, mMinimumDegree, graph, mProcessSelfLoops, mRepositionOfNodes);
				newNodes = res.newNodes;
				newEdges = res.newEdges;
				removedNodes = res.removedNodes;
				removedEdges = res.removedEdges;
			}
		}
		graph.getListenerManager().transactionFinished(this);
		MainFrame.showMessage("Split " + removedNodes + " nodes into " + newNodes + " new nodes; " + removedEdges
							+ " edges removed, " + newEdges + " edges created", MessageType.INFO);
	}
	
	public static SplitResult splitNodes(Node graphNode, int mMinimumDegree, Graph graph, boolean mProcessSelfLoops, boolean mRepositionOfNodes) {
		SplitResult res = new SplitResult();
		if (graphNode.getDegree() >= mMinimumDegree) {
			ArrayList<Node> nodelist = new ArrayList<Node>();
			for (Edge e : graphNode.getEdges()) {
				Node newNode = graph.addNodeCopy(graphNode);
				nodelist.add(newNode);
				res.newNodes++;
				Node source, target;
				if (e.getSource() == graphNode)
					source = newNode;
				else
					source = e.getSource();
				if (e.getTarget() == graphNode)
					target = newNode;
				else
					target = e.getTarget();
				if (source == target && mProcessSelfLoops) {
					Node newNode2 = graph.addNodeCopy(graphNode);
					nodelist.add(newNode);
					graph.addEdgeCopy(e, newNode, newNode2);
					res.newNodes++;
				} else {
					graph.addEdgeCopy(e, source, target);
					res.newEdges++;
				}
			}
			res.removedEdges += graphNode.getEdges().size();
			graph.deleteNode(graphNode);
			res.removedNodes++;
			processLayout(nodelist, mRepositionOfNodes);
		}
		return res;
	}
	
	private static void processLayout(ArrayList<Node> nodelist, boolean mRepositionOfNodes) {
		if (!mRepositionOfNodes || nodelist == null || nodelist.size() <= 1)
			return;
		Node a = nodelist.get(0);
		double w = AttributeHelper.getWidth(a);
		double h = AttributeHelper.getHeight(a);
		if (Double.isNaN(w) || Double.isNaN(h))
			return;
		double borderW = 0.1 * w;
		double borderH = 0.1 * h;
		GridLayouterAlgorithm.layoutOnGrid(nodelist, 1, w + borderW, h + borderH);
	}
}
