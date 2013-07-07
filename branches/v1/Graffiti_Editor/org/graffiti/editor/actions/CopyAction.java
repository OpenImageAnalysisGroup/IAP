// ==============================================================================
//
// CopyAction.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: CopyAction.java,v 1.1 2011-01-31 09:04:23 klukas Exp $

package org.graffiti.editor.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.ErrorMsg;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeExistsException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.help.HelpContext;
import org.graffiti.managers.IOManager;
import org.graffiti.plugin.actions.SelectionAction;
import org.graffiti.plugin.io.OutputSerializer;
import org.graffiti.plugin.io.SupportsWriterOutput;
import org.graffiti.selection.Selection;

/**
 * Represents a graph element copy action.
 * 
 * @version $Revision: 1.1 $
 */
public class CopyAction extends SelectionAction {
	// ~ Constructors ===========================================================
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new copy action.
	 * 
	 * @param mainFrame
	 *           DOCUMENT ME!
	 */
	public CopyAction(MainFrame mainFrame) {
		super("edit.copy", mainFrame);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the help context for the action.
	 * 
	 * @return HelpContext, the help context for the action
	 */
	@Override
	public HelpContext getHelpContext() {
		return null; 
	}
	
	/**
	 * Executes this action.
	 * 
	 * @param e
	 *           DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		Graph sourceGraph = getGraph();
		
		Selection selection = getSelection();
		
		// for all edges we also include the source and target nodes to the selection
		for (Edge edge : selection.getEdges()) {
			selection.add(edge.getSource());
			selection.add(edge.getTarget());
		}
		
		doCopyGraphMethodImproved(sourceGraph, selection);
	}
	
	public static void doCopyGraph(Graph sourceGraph, Selection selection) {
		AdjListGraph copyGraph = new AdjListGraph(sourceGraph, new ListenerManager());
		
		try {
			String ext = "gml";
			IOManager ioManager = MainFrame.getInstance().getIoManager();
			OutputSerializer os = ioManager.createOutputSerializer("." + ext);
			new StringBuffer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			if (selection.getNodes().size() > 0) {
				// remove all other nodes from copied graph
				ArrayList<Long> validNodeIds = new ArrayList<Long>();
				for (org.graffiti.graph.Node n : selection.getNodes())
					validNodeIds.add(new Long(n.getID()));
				
				ArrayList<org.graffiti.graph.Node> toBeDeleted = new ArrayList<org.graffiti.graph.Node>();
				for (org.graffiti.graph.Node n : copyGraph.getNodes()) {
					if (!validNodeIds.contains(new Long(n.getID()))) {
						toBeDeleted.add(n);
					}
				}
				for (org.graffiti.graph.Node n : toBeDeleted) {
					copyGraph.deleteNode(n);
				}
			}
			os.write(baos, copyGraph);
			ClipboardService.writeToClipboardAsText(baos.toString());
			MainFrame.showMessage(copyGraph.getNodes().size()
								+ " node(s) and "
								+ copyGraph.getEdges().size()
								+ " edge(s) copied to clipboard",
								MessageType.INFO, 3000);
		} catch (IOException ioe) {
			ErrorMsg.addErrorMessage(ioe.getLocalizedMessage());
		} catch (IllegalAccessException iae) {
			ErrorMsg.addErrorMessage(iae.getLocalizedMessage());
		} catch (InstantiationException ie) {
			ErrorMsg.addErrorMessage(ie.getLocalizedMessage());
		}
	}
	
	public static void doCopyGraphMethodImproved(Graph sourceGraph, Selection selection) {
		doCopyGraphMethodImproved(sourceGraph, selection, false);
	}
	
	public static Graph doCopyGraphMethodImproved(Graph sourceGraph, Selection selection, boolean returnGraphInsteadPastingInClipboard) {
		String ext = "gml";
		OutputSerializer os = null;
		try {
			IOManager ioManager = MainFrame.getInstance().getIoManager();
			os = ioManager.createOutputSerializer("." + ext);
			return doCopyGraphMethodImproved(sourceGraph, selection, returnGraphInsteadPastingInClipboard, os);
		} catch (Exception ioe) {
			ErrorMsg.addErrorMessage(ioe.getLocalizedMessage());
			return null;
		}
	}
	
	public static Graph doCopyGraphMethodImproved(Graph sourceGraph, Selection selection, boolean returnGraphInsteadPastingInClipboard, OutputSerializer os)
			throws IOException {
		
		Graph resultGraph = new AdjListGraph(new ListenerManager());
		
		new StringBuffer();
		Collection<Node> selNodes = selection.getNodes();
		if (selNodes.size() > 0 && selNodes.size() != sourceGraph.getNumberOfNodes()) {
			CollectionAttribute ca = sourceGraph.getAttributes();
			CollectionAttribute tg = resultGraph.getAttributes();
			for (Attribute a : ca.getCollection().values()) {
				try {
					tg.add((Attribute) a.copy());
				} catch (AttributeExistsException aee) {
					tg.getAttribute(a.getId()).setValue(a.getValue());
				}
			}
			
			HashMap<Node, Node> sourceGraphNode2resultGraphNode = new HashMap<Node, Node>();
			for (Node n : selection.getNodes()) {
				Node newNode = resultGraph.addNodeCopy(n);
				if (newNode == null)
					ErrorMsg.addErrorMessage("Error: Node " + n.getID() + " could not be copied!");
				else
					sourceGraphNode2resultGraphNode.put(n, newNode);
			}
			for (Node n : selNodes) {
				for (Edge e : n.getAllOutEdges()) {
					if (e.getSource() == n && (selNodes.contains(e.getSource()) || selNodes.contains(e.getTarget()))) {
						Node a = sourceGraphNode2resultGraphNode.get(e.getSource());
						Node b = sourceGraphNode2resultGraphNode.get(e.getTarget());
						if (a != null && b != null)
							resultGraph.addEdgeCopy(e, a, b);
					}
				}
			}
		} else
			resultGraph = sourceGraph;
		if (returnGraphInsteadPastingInClipboard)
			return resultGraph;
		if (os instanceof SupportsWriterOutput) {
			StringWriter sw = new StringWriter();
			((SupportsWriterOutput) os).write(sw, resultGraph);
			ClipboardService.writeToClipboardAsText(sw.toString());
			
		} else {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			os.write(baos, resultGraph);
			ClipboardService.writeToClipboardAsText(baos.toString());
		}
		MainFrame.showMessage(resultGraph.getNumberOfNodes()
								+ " node(s) and "
								+ resultGraph.getNumberOfEdges()
								+ " edge(s) copied to clipboard",
								MessageType.INFO, 3000);
		
		return null;
	}
	
	/**
	 * Sets the internal <code>enable</code> flag, which depends on the given
	 * list of selected items.
	 * 
	 * @param items
	 *           the items, which determine the internal state of the <code>enable</code> flag.
	 */
	@Override
	protected void enable(List<?> items) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.actions.SelectionAction#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		try {
			Graph sourceGraph = MainFrame.getInstance().getActiveEditorSession()
								.getGraph();
			return sourceGraph.getNumberOfNodes() > 0;
		} catch (NullPointerException npe) {
			return false;
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
