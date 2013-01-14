/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.event.GraphEvent;
import org.graffiti.event.GraphListener;
import org.graffiti.event.TransactionEvent;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugins.modes.defaults.AdvancedLabelTool;
import org.graffiti.session.Session;
import org.graffiti.session.SessionListener;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;

public class KEGGgraphElementCreation
					implements GraphListener, SessionListener {
	
	public void postEdgeAdded(GraphEvent e) {
		//
		// System.out.println("Edge Added: "+e.getEdge().toString());
	}
	
	public void postEdgeRemoved(GraphEvent e) {
		//
		
	}
	
	public void postGraphCleared(GraphEvent e) {
		// System.out.println("Graph Cleared...");
	}
	
	public void postNodeAdded(GraphEvent e) {
		// System.out.println("Node Added: "+e.getNode().toString());
		Node n = e.getNode();
		if (n == null)
			return;
		String keggId = KeggGmlHelper.getKeggId(n);
		if (keggId == null || keggId.length() <= 0) {
			KeggGmlHelper.setKeggId(n, "");
			KeggGmlHelper.setKeggType(n, "");
		}
		// SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// EntryCreator.processNewNode(n);
		// }});
		// }
	}
	
	public void postNodeRemoved(GraphEvent e) {
		//
		
	}
	
	public void preEdgeAdded(GraphEvent e) {
		//
		
	}
	
	public void preEdgeRemoved(GraphEvent e) {
		//
		
	}
	
	public void preGraphCleared(GraphEvent e) {
		//
		
	}
	
	public void preNodeAdded(GraphEvent e) {
		//
	}
	
	public void preNodeRemoved(GraphEvent e) {
		//
		
	}
	
	public void transactionFinished(TransactionEvent e, BackgroundTaskStatusProviderSupportingExternalCall status) {
		//
		
	}
	
	public void transactionStarted(TransactionEvent e) {
		//
		
	}
	
	private static Runnable myEditCmd = null;
	
	public void sessionChanged(Session s) {
		if (s == null)
			return;
		if (s.getGraph() == null)
			return;
		s.getGraph().getListenerManager().addDelayedGraphListener(this);
		EntryCreator.checkGraphAttributes(s.getGraph());
		if (myEditCmd == null)
			myEditCmd = getEditCommand();
		AdvancedLabelTool.setEditCommand(myEditCmd);
	}
	
	public void sessionDataChanged(Session s) {
	}
	
	public static Runnable getEditCommand() {
		Runnable result = new Runnable() {
			public void run() {
				GraphElement ge = AdvancedLabelTool.getEditGraphElement();
				if (ge != null && (ge instanceof Node))
					EntryCreator.processNewOrExistingNode((Node) ge, null);
				else
					if (ge != null && (ge instanceof Edge)) {
						KeggRelationCreation.processNewOrExistingEdge((Edge) ge);
					}
			}
		};
		return result;
	}
}
