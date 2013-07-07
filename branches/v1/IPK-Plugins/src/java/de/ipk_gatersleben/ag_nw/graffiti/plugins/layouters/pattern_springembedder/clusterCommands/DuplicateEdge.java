package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public class DuplicateEdge extends AbstractEditorAlgorithm {
	
	@Override
	public boolean activeForView(View v) {
		return v != null;
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		PreconditionException e = new PreconditionException();
		if (selection.getNumberOfNodes() > 0 || selection.getNumberOfEdges() != 1)
			e.add("Exactly one edge needs to be selected!");
		
		if (!e.isEmpty())
			throw e;
	}
	
	@Override
	public void execute() {
		
		final BackgroundTaskStatusProviderSupportingExternalCallImpl status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("", "");
		status.setPluginWaitsForUser(true);
		
		final EditorSession session = MainFrame.getInstance().getActiveEditorSession();
		
		final ArrayList<Node> src = new ArrayList<Node>();
		final ArrayList<Node> tgt = new ArrayList<Node>();
		
		final Edge edgeToBeCopied = selection.getEdges().iterator().next();
		
		Runnable interaction = new Runnable() {
			@Override
			public void run() {
				status.setCurrentStatusValue(0);
				status.setCurrentStatusText1("Please select source node(s).");
				status.setCurrentStatusText2("Then click OK.");
				while (status.pluginWaitsForUser()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				if (status.wantsToStop())
					return;
				if (session.getSelectionModel().getActiveSelection().getNumberOfNodes() == 0) {
					MainFrame.showMessageDialog("No source node(s) have been selected.", "Operation Interrupted");
					return;
				}
				status.setPluginWaitsForUser(true);
				status.setCurrentStatusValue(50);
				src.addAll(session.getSelectionModel().getActiveSelection().getNodes());
				status.setCurrentStatusText1("Please select the target node(s).");
				while (status.pluginWaitsForUser()) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
				if (status.wantsToStop())
					return;
				if (session.getSelectionModel().getActiveSelection().getNumberOfNodes() == 0) {
					MainFrame.showMessageDialog("No target node(s) have been selected.", "Operation Interrupted");
					return;
				}
				tgt.addAll(session.getSelectionModel().getActiveSelection().getNodes());
				status.setCurrentStatusValue(100);
			}
		};
		
		Runnable exec = new Runnable() {
			@Override
			public void run() {
				if (src.size() > 0 && tgt.size() > 0) {
					ArrayList<Edge> newEdges = new ArrayList<Edge>();
					boolean hasSelfLoops = false;
					boolean hasParallelEdges = false;
					for (Node s : src) {
						for (Node t : tgt) {
							Edge ne = session.getGraph().addEdgeCopy(edgeToBeCopied, s, t);
							if (!hasSelfLoops && ne.getSource() == ne.getTarget())
								hasSelfLoops = true;
							else
								if (!hasParallelEdges && session.getGraph().getEdges(s, t).size() > 1)
									hasParallelEdges = true;
							
							newEdges.add(ne);
						}
					}
					Selection sel = new Selection(getName(), newEdges);
					
					if (hasSelfLoops)
						GravistoService.getInstance().runAlgorithm(new IntroduceSelfEdgeBends(), session.getGraph(), sel, false, getActionEvent());
					if (hasParallelEdges)
						GravistoService.getInstance().runAlgorithm(new IntroduceParallelEdgeBends(), session.getGraph(), sel, false, getActionEvent());
					
					session.getSelectionModel().setActiveSelection(sel);
					session.getSelectionModel().selectionChanged();
				}
			}
		};
		
		BackgroundTaskHelper.issueSimpleTask(getName(), "", interaction, exec, status);
	}
	
	@Override
	public String getName() {
		return "Multiply Edge";
	}
	
	@Override
	public String getDescription() {
		return "<html>This command will copy the edge with all attributes and<br>" +
						"connect this copy to the interactively chosen source and<br>" +
						"target node. If there are many nodes chosen the edge will<br>" +
						"be multiplicated several times.";
	}
	
	@Override
	public String getCategory() {
		return "Edges";
	}
}
