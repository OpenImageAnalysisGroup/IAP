/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: RemoveSelectedNodesPreserveEdgesAlgorithm.java,v 1.1 2011-01-31 08:59:35 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection;

import java.util.ArrayList;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.AttributeHelper;
import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.FolderPanel;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.AlgorithmWithComponentDescription;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class RemoveSelectedNodesPreserveEdgesAlgorithm
					extends AbstractAlgorithm
					implements AlgorithmWithComponentDescription {
	
	Selection selection;
	
	private boolean ignoreDirection = true;
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] { new BooleanParameter(ignoreDirection, "Preserve Connectivity",
							"Prevent connectivity loss because of edge-direction - ignore edge directions") };
	}
	
	public JComponent getDescriptionComponent() {
		ClassLoader cl = this.getClass().getClassLoader();
		String path = this.getClass().getPackage().getName().replace('.', '/');
		ImageIcon icon = new ImageIcon(cl.getResource(path + "/images/fold.png"));
		return FolderPanel.getBorderedComponent(new JLabel(icon), 5, 5, 5, 5);
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (selection == null) {
			EditorSession session = MainFrame.getInstance()
								.getActiveEditorSession();
			selection = session.getSelectionModel().getActiveSelection();
		}
		if (selection.getNodes().size() <= 0)
			throw new PreconditionException(
								"Please select a number of nodes which will be removed from the network.<br>The result is a <b>folded network</b>, edges are preserved.");
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		ignoreDirection = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		EditorSession session = MainFrame.getInstance()
							.getActiveEditorSession();
		if (selection == null)
			selection = session.getSelectionModel().getActiveSelection();
		
		graph.getListenerManager().transactionStarted(this);
		try {
			ArrayList<Node> workNodes = new ArrayList<Node>();
			workNodes.addAll(selection.getNodes());
			removeNodesPreserveEdges(workNodes, graph, ignoreDirection, null);
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	public static int removeNodesPreserveEdges(ArrayList<Node> workNodes,
						Graph graph, boolean ignoreDirection,
						BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		Stack<Node> toBeDeleted = new Stack<Node>();
		int workCount = 0;
		toBeDeleted.addAll(workNodes);
		workCount = workNodes.size();
		int selfLoops = 0;
		int edgeCopies = 0;
		int i = 0;
		for (Node n : workNodes) {
			i++;
			// process undirected edges as follows:
			// all nodes that have a undirected connection to the node to be
			// deleted
			// will be connected by a new undirected edge to all neighbours of
			// the
			// node to be deleted
			if (ignoreDirection) {
				for (Edge e : n.getAllOutEdges()) {
					e.setDirected(false);
					AttributeHelper.setArrowhead(e, false);
					AttributeHelper.setArrowtail(e, false);
				}
			}
			for (Edge undirEdge : n.getUndirectedEdges()) {
				Node srcN = undirEdge.getSource();
				for (Node targetN : n.getUndirectedNeighbors()) {
					if (srcN != targetN) {
						if (!srcN.getNeighbors().contains(targetN)) {
							graph.addEdgeCopy(undirEdge, srcN, targetN);
							edgeCopies++;
						}
					}
				}
				srcN = undirEdge.getTarget();
				for (Node targetN : n.getUndirectedNeighbors()) {
					if (srcN != targetN) {
						if (!srcN.getNeighbors().contains(targetN)) {
							graph.addEdgeCopy(undirEdge, srcN, targetN);
							edgeCopies++;
						}
					}
				}
			}
			// process undirected edges as follows:
			// all incoming neighbours need to be handled as follows:
			// each incoming neighbour needs to be connected to all outgoing
			// neighbours
			// of the worknode
			// the combination of undirected and directed edges around a node is
			// not specially treated and is ignored
			for (Edge incEdge : n.getDirectedInEdges()) {
				Node srcN = incEdge.getSource();
				for (Node targetN : n.getOutNeighbors()) {
					// if (srcN != targetN) {
					if (!srcN.getOutNeighbors().contains(targetN)) {
						graph.addEdgeCopy(incEdge, srcN, targetN);
						edgeCopies++;
					}
					if (srcN == targetN)
						selfLoops++;
					// }
				}
				if (ignoreDirection) {
					for (Node targetN : n.getInNeighbors()) {
						if (srcN != targetN) {
							if (!srcN.getNeighbors().contains(targetN)) {
								Edge ne = graph.addEdgeCopy(incEdge, srcN, targetN);
								edgeCopies++;
								ne.setDirected(false);
								AttributeHelper.setArrowhead(ne, false);
								AttributeHelper.setArrowtail(ne, false);
							}
						}
					}
				}
			}
			if (optStatus != null) {
				if (selfLoops <= 0)
					optStatus.setCurrentStatusText2("Created " + edgeCopies + " edge copies");
				else
					optStatus.setCurrentStatusText2("Created " + edgeCopies + " edge copies (" + selfLoops + " self-loops)");
				optStatus.setCurrentStatusValueFine(50d * ((double) i / workCount));
			}
			if (optStatus != null && optStatus.wantsToStop())
				break;
		}
		int delCnt = 0;
		if (optStatus == null || !optStatus.wantsToStop())
			while (!toBeDeleted.empty()) {
				Node n = toBeDeleted.pop();
				if (n.getGraph() != null) {
					graph.deleteNode(n);
					delCnt++;
					if (optStatus != null)
						optStatus.setCurrentStatusText2("Removed " + delCnt + "/" + workCount + " nodes");
				}
				if (optStatus != null)
					optStatus.setCurrentStatusValueFine(50d + 50d * ((double) delCnt / workCount));
				if (optStatus != null && optStatus.wantsToStop())
					break;
			}
		if (selfLoops <= 0)
			MainFrame.showMessage("Removed " + workCount + "/" + workCount + " nodes.", MessageType.INFO);
		else
			MainFrame.showMessage("Removed " + workCount + "/" + workCount + " nodes, created " + selfLoops + " self-loop edge(s)!", MessageType.INFO);
		if (optStatus != null)
			optStatus.setCurrentStatusValue(100);
		return workCount;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Remove Nodes...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Nodes";
	}
	
	@Override
	public String getDescription() {
		return "<html>" +
							"With this command, the selected nodes (round nodes<br>" +
							"in the example) are removed from a network. <br>" +
							"The connectivity of the resulting network is influenced<br>" +
							"by the corresponding setting as shown in the image.";
	}
	
	/**
	 * Sets the selection on which the algorithm works.
	 * 
	 * @param selection
	 *           the selection
	 */
	public void setSelection(Selection selection) {
		this.selection = selection;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
