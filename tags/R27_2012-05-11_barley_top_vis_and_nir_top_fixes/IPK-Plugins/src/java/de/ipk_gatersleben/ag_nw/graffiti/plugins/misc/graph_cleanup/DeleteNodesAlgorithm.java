/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: DeleteNodesAlgorithm.java,v 1.1 2011-01-31 09:00:59 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.graph_cleanup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.graffiti.editor.GravistoService;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElementNotFoundException;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.ProvidesEdgeContextMenu;
import org.graffiti.plugin.algorithm.ProvidesGeneralContextMenu;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Labels all selected nodes with unique numbers. Does not touch existing
 * labels.
 */
public class DeleteNodesAlgorithm
					extends AbstractAlgorithm
					implements ActionListener, ProvidesGeneralContextMenu, ProvidesEdgeContextMenu {
	
	private boolean delete_selection = true;
	
	JMenu myMenu;
	JMenuItem m1delSel;
	JMenuItem m2delAllButSel;
	JMenuItem m3delBends;
	JMenuItem myMenuDelEdges;
	
	/**
	 * Constructs a new instance.
	 */
	public DeleteNodesAlgorithm() {
		myMenu = new JMenu("Delete Nodes/Edges"); // /Bends
		m1delSel = new JMenuItem("Delete selected subgraph");
		m2delAllButSel = new JMenuItem("Delete all, but not the selected subgraph");
		m1delSel.addActionListener(this);
		m2delAllButSel.addActionListener(this);
		
		m3delBends = new JMenuItem("Remove all Bends");
		m3delBends.addActionListener(this);
		
		myMenu.add(m1delSel);
		myMenu.add(m2delAllButSel);
		// myMenu.add(m3delBends);
		
		myMenuDelEdges = new JMenuItem("Delete selected edges");
		myMenuDelEdges.addActionListener(this);
		
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		BooleanParameter delParam =
							new BooleanParameter(
												delete_selection,
												"<html>Delete selection (y)<br>or remaining graph (N)",
												"Delete selection (Y) or the remaining graph (N).");
		return new Parameter[] { delParam };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		this.parameters = params;
		delete_selection =
							((BooleanParameter) params[0]).getBoolean().booleanValue();
	}
	
	private void addConnectedNodes(Vector<Node> nodes, Node n) {
		nodes.add(n);
		Collection<?> neighbours = n.getNeighbors();
		Iterator<?> it = neighbours.iterator();
		while (it.hasNext()) {
			Node nx = (Node) it.next();
			if (!nodes.contains(nx)) {
				addConnectedNodes(nodes, nx);
			}
		}
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		
		GravistoService.getInstance().algorithmAttachData(this);
		
		Collection<Node> nodes;
		if (selection.isEmpty()) {
			nodes = graph.getNodes();
		} else {
			nodes = selection.getNodes();
		}
		
		nodes = GraphHelper.getSelectedOrAllNodes(selection, graph);
		
		graph.getListenerManager().transactionStarted(this);
		
		Vector<Node> toBeDeleted = new Vector<Node>();
		
		Iterator<Node> it = nodes.iterator();
		while (it.hasNext()) {
			addConnectedNodes(toBeDeleted, (Node) it.next());
		}
		
		if (delete_selection) {
			for (int i = 0; i < toBeDeleted.size(); i++) {
				Node del = (Node) toBeDeleted.get(i);
				graph.deleteNode(del);
			}
		} else {
			Iterator<Node> itGN = graph.getNodesIterator();
			while (itGN.hasNext()) {
				Node test = (Node) itGN.next();
				if (!toBeDeleted.contains(test)) {
					graph.deleteNode(test);
					itGN = graph.getNodesIterator();
				}
			}
		}
		
		graph.getListenerManager().transactionFinished(this);
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	@Override
	public void reset() {
		graph = null;
		selection = null;
		delete_selection = true;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		// return "Delete Nodes...";
		return null;
	}
	
	@Override
	public String getCategory() {
		return null;
		// return "Nodes";
		// return "menu.edit";
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
	
	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == m1delSel) {
			delete_selection = true;
			execute();
		}
		if (e.getSource() == m2delAllButSel) {
			delete_selection = false;
			execute();
		}
		if (e.getSource() == myMenuDelEdges) {
			deleteEdges();
		}
		if (e.getSource() == m3delBends) {
			removeBends();
		}
	}
	
	/**
	 * Remove bends from graph
	 */
	private void removeBends() {
		EditorSession session =
							GravistoService
												.getInstance()
												.getMainFrame()
												.getActiveEditorSession();
		selection = session.getSelectionModel().getActiveSelection();
		
		GraphHelper.removeAllBends(session.getGraph(), true);
	}
	
	/**
	 * 
	 */
	private void deleteEdges() {
		GravistoService.getInstance().algorithmAttachData(this);
		/*
		 * EditorSession session =
		 * GraffitiSingleton
		 * .getInstance()
		 * .getMainFrame()
		 * .getActiveEditorSession();
		 * selection = session.getSelectionModel().getActiveSelection();
		 */
		Collection<Edge> edges;
		if (selection.isEmpty()) {
			edges = this.graph.getEdges();
		} else {
			edges = selection.getEdges();
		}
		edges = new ArrayList<Edge>(edges);
		for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
			Edge del = (Edge) it.next();
			try {
				graph.deleteEdge(del);
			} catch (GraphElementNotFoundException e) {
				System.err.println("Error: Edge to be deleted was not found in graph.");
				// empty
				// this should not happen, but happens, I would
				// say this is a gravisto bug.
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.AlgorithmWithEdgeContextMenu#getCurrentEdgeContextMenuItem(java.util.Collection)
	 */
	public JMenuItem[] getCurrentEdgeContextMenuItem(Collection<Edge> selectedEdges) {
		return null; // new JMenuItem[] { myMenuDelEdges };
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.AlgorithmWithContextMenu#getCurrentContextMenuItem()
	 */
	public JMenuItem[] getCurrentContextMenuItem() {
		EditorSession session =
							GravistoService
												.getInstance()
												.getMainFrame()
												.getActiveEditorSession();
		if (session == null)
			return null;
		selection = session.getSelectionModel().getActiveSelection();
		if (selection.isEmpty())
			return null;
		// return new JMenuItem[] { myMenu };
		return new JMenuItem[] { m1delSel, m2delAllButSel };
	}
}
