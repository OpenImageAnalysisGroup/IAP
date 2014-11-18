package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.view.View;

public class ResetEdgeSourceOrTarget extends AbstractEditorAlgorithm {
	
	private Node connectedNode;
	private Node unconnectedNode;
	private Edge edgeToBeReset;
	
	@Override
	public boolean activeForView(View v) {
		return v != null;
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		PreconditionException e = new PreconditionException();
		// if (selection.getElements().size() != 3)
		// e.add("Not exactly three graphelements sel)
		Node nd1 = null, nd2 = null;
		edgeToBeReset = null;
		for (GraphElement ge : selection.getElements()) {
			if (ge instanceof Edge) {
				if (edgeToBeReset == null)
					edgeToBeReset = (Edge) ge;
				else
					e.add("More than one edge selected");
			} else {
				if (nd1 == null)
					nd1 = (Node) ge;
				else
					if (nd2 == null)
						nd2 = (Node) ge;
					else
						e.add("More than two nodes selected");
			}
		}
		
		if (edgeToBeReset == null)
			e.add("No edge selected");
		else
			if (nd1 == null || nd2 == null)
				e.add("Less than two nodes selected");
			else {
				boolean nd1connected = edgeToBeReset.getTarget() == nd1 || edgeToBeReset.getSource() == nd1;
				boolean nd2connected = edgeToBeReset.getTarget() == nd2 || edgeToBeReset.getSource() == nd2;
				
				if (nd1connected && nd2connected)
					e.add("Only one node is allowed to be connected to the edge");
				else
					if (!nd1connected && !nd2connected)
						e.add("One node has to be connected to the edge");
					else {
						if (nd1connected && !nd2connected) {
							connectedNode = nd1;
							unconnectedNode = nd2;
						} else
							if (!nd1connected && nd2connected) {
								connectedNode = nd2;
								unconnectedNode = nd1;
							}
					}
				
			}
		
		if (!e.isEmpty())
			throw e;
	}
	
	@Override
	public void execute() {
		Edge newEd = null;
		if (edgeToBeReset.getTarget() == connectedNode)
			newEd = graph.addEdgeCopy(edgeToBeReset, edgeToBeReset.getSource(), unconnectedNode);
		else
			newEd = graph.addEdgeCopy(edgeToBeReset, edgeToBeReset.getTarget(), unconnectedNode);
		
		graph.deleteEdge(edgeToBeReset);
		
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().remove(edgeToBeReset);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().getActiveSelection().add(newEd);
		MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public String getName() {
		return "Reconnect Edge";
	}
	
	@Override
	public String getCategory() {
		return "Edges";
	}
}
