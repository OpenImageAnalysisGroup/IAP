/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 08.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.kegg_bar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.StringManipulationTools;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelperBio;
import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.MergeNodes;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class KeggNavigationToolbarCommand extends AbstractUndoableEdit {
	public enum Command {
		PATHWAY_OVERVIEW, COLLAPSE_PATHWAY, LOAD_PATHWAY, CONDENSE_ENTITIES, UPDATE_CLUSTER_NODES, HIDE_CLUSTER_NODES
	};
	
	private Command cmd;
	private EditorSession session;
	private String desc;
	
	/**
	 * Creates a Alignment Command, used for aligning nodes.
	 * 
	 * @param cmd
	 *           The command to be carried out
	 * @param nodes
	 *           The node list to operate on, at least two nodes must be in the list.
	 */
	public KeggNavigationToolbarCommand(Command cmd, EditorSession session) {
		this.cmd = cmd;
		this.session = session;
		if (cmd == Command.PATHWAY_OVERVIEW)
			desc = "Create KEGG Pathway Overview";
		if (cmd == Command.LOAD_PATHWAY)
			desc += "Load Pathway";
		if (cmd == Command.COLLAPSE_PATHWAY)
			desc += "Collapse Pathway";
		if (cmd == Command.UPDATE_CLUSTER_NODES)
			desc += "Update Cluster Background-Nodes";
		if (cmd == Command.HIDE_CLUSTER_NODES)
			desc += "Hide Cluster Background-Nodes";
		if (cmd == Command.CONDENSE_ENTITIES)
			desc += "Condense Multiple Entities";
	}
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public String getPresentationName() {
		return desc;
	}
	
	@Override
	public String getRedoPresentationName() {
		return "Redo " + desc;
	}
	
	@Override
	public String getUndoPresentationName() {
		return "Undo " + StringManipulationTools.removeHTMLtags(desc);
	}
	
	@Override
	public void redo() throws CannotRedoException {
		
		Graph graph = session.getGraph();
		
		Selection selection = session.getSelectionModel()
							.getActiveSelection();
		Collection<Node> nodes;
		if (selection == null || selection.isEmpty()) {
			nodes = graph.getNodes();
		} else {
			nodes = selection.getNodes();
		}
		
		if (nodes.size() > 0)
			(nodes.iterator().next()).getGraph().getListenerManager().transactionStarted(this);
		switch (cmd) {
			case CONDENSE_ENTITIES:
				condenseEntities(nodes);
				break;
		}
		
		if (nodes.size() > 0)
			(nodes.iterator().next()).getGraph().getListenerManager().transactionFinished(this);
	}
	
	@SuppressWarnings("unchecked")
	private void condenseEntities(Collection<Node> nodes) {
		HashMap<String, ArrayList<Node>> keggID2nodeList = new HashMap<String, ArrayList<Node>>();
		Graph g = null;
		for (Node n : nodes) {
			if (g == null)
				g = n.getGraph();
			String name = GraphHelperBio.getKeggName(n, null);
			if (name == null || name.length() <= 0)
				continue;
			if (!keggID2nodeList.containsKey(name))
				keggID2nodeList.put(name, new ArrayList<Node>());
			ArrayList<Node> nl = keggID2nodeList.get(name);
			nl.add(n);
		}
		if (g != null) {
			for (ArrayList<Node> toBeCondensed : keggID2nodeList.values()) {
				if (toBeCondensed.size() > 1) {
					MergeNodes.mergeNode(g, toBeCondensed, NodeTools.getCenter(toBeCondensed), true);
					// System.out.println("Delete: "+toBeCondensed.size());
					g.deleteAll((Collection) toBeCondensed);
				}
			}
		}
	}
	
	@Override
	public void undo() throws CannotUndoException {
		session.getGraph().getNodes().get(0).getGraph().getListenerManager().transactionStarted(this);
		
		session.getGraph().getNodes().get(0).getGraph().getListenerManager().transactionFinished(this);
	}
}
