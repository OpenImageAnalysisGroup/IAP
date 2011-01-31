/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 27.01.2005 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.selectCommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.AttributeHelper;
import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.NodeTools;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.EdgeHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class SelectEdgesAlgorithm extends AbstractAlgorithm {
	
	private boolean selInnerClusterEdges = false;
	private boolean selInterClusterEdges = false;
	
	private boolean selfLoops = false;
	private boolean onlyWithMapping = false;
	private boolean onlyWithoutMapping = false;
	private boolean onlyConnectingSelectedNodes = false;
	private boolean onlyParallelEdges = false;
	private boolean onlyAntiParallelEdges = false;
	private boolean onlyVisibleEdges = false;
	private boolean onlyNonVisibleEdges = false;
	
	private boolean extendSelection = true;
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		return "Select Edges...";
	}
	
	@Override
	public String getDescription() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "<html><small>Select one of the checkboxes in order to limit the edge<br>" +
								"selection to edges which meet all of the checked criteria.<br>" +
								"<br>" +
								"<b>Leave all checkboxes unchecked to select ALL edges.</b><br><br>";
		else
			return "<html><small>" +
								"Select one of the checkboxes in order to limit the edge selection to edges<br>" +
								"inside the pathway-subgraphs or to edges connecting different pathway-subgraphs<br>" +
								"- otherwise all edges will be selected.<br>" +
								"You may also limit the selection to self-edges, where source and target<br>" +
								"of the edge are the same node.";
	}
	
	@Override
	public Parameter[] getParameters() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
			return new Parameter[] {
								selection.isEmpty() ? null : new BooleanParameter(extendSelection, "Extend selection", "<html>" +
													"If selected, the selection will be extended,<br>" +
													"leaving currently selected elements unaffected."),
								new BooleanParameter(selInnerClusterEdges, "Limit to edges inside the same cluster", ""),
								new BooleanParameter(selInterClusterEdges, "Limit to edges connecting different clusters", ""),
								new BooleanParameter(onlyWithMapping, "Limit to edges with mapping-data", ""),
								new BooleanParameter(onlyWithoutMapping, "Limit to edges without mapping-data", ""),
								new BooleanParameter(onlyConnectingSelectedNodes, "Limit to edges connecting already selected nodes", ""),
								new BooleanParameter(selfLoops, "Limit to self-loops", ""),
								new BooleanParameter(onlyParallelEdges, "Limit to parallel edges", ""),
								new BooleanParameter(onlyAntiParallelEdges, "Limit to anti-parallel edges", ""),
								new BooleanParameter(onlyVisibleEdges, "Limit to visible edges", ""),
								new BooleanParameter(onlyNonVisibleEdges, "Limit to hidden edges", ""), };
		} else {
			return new Parameter[] {
								new BooleanParameter(selInnerClusterEdges, "Select edges inside Pathway-Subgraphs", ""),
								new BooleanParameter(selInterClusterEdges, "Select edges connecting different Pathway-Subgraphs", ""),
								new BooleanParameter(selfLoops, "Limit to self-edges", ""),
								new BooleanParameter(onlyConnectingSelectedNodes, "Limit to edges connecting already selected nodes", ""), };
		}
	}
	
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
			extendSelection = true;
			selInnerClusterEdges = ((BooleanParameter) params[i++]).getBoolean();
			selInterClusterEdges = ((BooleanParameter) params[i++]).getBoolean();
			selfLoops = ((BooleanParameter) params[i++]).getBoolean();
			onlyConnectingSelectedNodes = ((BooleanParameter) params[i++]).getBoolean();
		} else {
			extendSelection = params[i] == null ? (i++ > 0) : ((BooleanParameter) params[i++]).getBoolean();
			selInnerClusterEdges = ((BooleanParameter) params[i++]).getBoolean();
			selInterClusterEdges = ((BooleanParameter) params[i++]).getBoolean();
			onlyWithMapping = ((BooleanParameter) params[i++]).getBoolean();
			onlyWithoutMapping = ((BooleanParameter) params[i++]).getBoolean();
			onlyConnectingSelectedNodes = ((BooleanParameter) params[i++]).getBoolean();
			selfLoops = ((BooleanParameter) params[i++]).getBoolean();
			onlyParallelEdges = ((BooleanParameter) params[i++]).getBoolean();
			onlyAntiParallelEdges = ((BooleanParameter) params[i++]).getBoolean();
			onlyVisibleEdges = ((BooleanParameter) params[i++]).getBoolean();
			onlyNonVisibleEdges = ((BooleanParameter) params[i++]).getBoolean();
		}
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		
		if (graph == null)
			throw new PreconditionException("No active graph editor window found!");
		
		if (graph.getEdges().size() <= 0)
			throw new PreconditionException("Current graph contains no edges which may be selected.");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
		try {
			graph.getListenerManager().transactionStarted(this);
			
			if (!extendSelection)
				selection.clear();
			
			int cntA = selection.getEdges().size();
			if (!selInnerClusterEdges && !selInterClusterEdges)
				selection.addAll(limitByOptions(graph.getEdges()));
			else
				if (selInnerClusterEdges && selInterClusterEdges)
					selection.addAll(limitByOptions(graph.getEdges()));
				else
					if (selInnerClusterEdges) {
						selection.addAll(limitByOptions(getInnerClusterEdges(graph.getEdges())));
					} else
						if (selInterClusterEdges) {
							selection.addAll(limitByOptions(getInterClusterEdges(graph.getEdges())));
						} else
							ErrorMsg.addErrorMessage("Internal Error: SelectEdgesAlgorithm");
			int cntB = selection.getEdges().size();
			int cnt = cntB - cntA;
			if (!extendSelection)
				MainFrame.showMessage("New selection: " + cnt + " edge(s)", MessageType.INFO);
			else
				MainFrame.showMessage("Added " + cnt + " edge(s) to selection", MessageType.INFO);
			MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
		} finally {
			graph.getListenerManager().transactionFinished(this);
		}
	}
	
	private Collection<Edge> limitByOptions(Collection<Edge> edges) {
		ArrayList<Edge> result = new ArrayList<Edge>();
		for (Edge e : edges) {
			if (selfLoops && e.getSource() != e.getTarget())
				continue;
			boolean hasMappingData = EdgeHelper.hasMappingData(e);
			if (onlyWithMapping && !hasMappingData)
				continue;
			if (onlyWithoutMapping && hasMappingData)
				continue;
			if (onlyConnectingSelectedNodes && !selectionContains(e.getSource(), e.getTarget()))
				continue;
			if (onlyParallelEdges && !parallelEdgeExists(e))
				continue;
			if (onlyAntiParallelEdges && !antiParallelEdgeExists(e))
				continue;
			if (onlyVisibleEdges && AttributeHelper.isHiddenGraphElement(e))
				continue;
			if (onlyNonVisibleEdges && !AttributeHelper.isHiddenGraphElement(e))
				continue;
			result.add(e);
		}
		return result;
	}
	
	private boolean parallelEdgeExists(Edge e) {
		if (e.isDirected()) {
			for (Edge e2 : e.getSource().getDirectedOutEdges()) {
				if (e == e2)
					continue;
				if (e2.getTarget() == e.getTarget())
					return true;
			}
			return false;
		} else {
			HashSet<Edge> edges = new HashSet<Edge>();
			edges.addAll(e.getSource().getEdges());
			for (Edge e2 : edges) {
				if (e == e2)
					continue;
				if (isParallelRegardlessOfDirection(e, e2))
					return true;
			}
			return false;
		}
	}
	
	private boolean antiParallelEdgeExists(Edge e) {
		if (e.isDirected()) {
			for (Edge e2 : e.getTarget().getDirectedOutEdges()) {
				if (e == e2)
					continue;
				if (e2.getTarget() == e.getSource())
					return true;
			}
			return false;
		} else {
			return false;
		}
	}
	
	private boolean isParallelRegardlessOfDirection(Edge e1, Edge e2) {
		if (e1.getSource() == e2.getSource() && e1.getTarget() == e2.getTarget())
			return true;
		if (e1.getTarget() == e2.getSource() && e1.getSource() == e2.getTarget())
			return true;
		return false;
	}
	
	private boolean selectionContains(Node source, Node target) {
		return selection.getNodes().contains(source) && selection.getNodes().contains(target);
	}
	
	public static Collection<Edge> getInterClusterEdges(Collection<Edge> edges) {
		ArrayList<Edge> result = new ArrayList<Edge>();
		for (Edge e : edges) {
			Node s = e.getSource();
			Node t = e.getTarget();
			String sC = NodeTools.getClusterID(s, "");
			String tC = NodeTools.getClusterID(t, "");
			if (!sC.equals(tC))
				result.add(e);
		}
		return result;
	}
	
	public static Collection<Edge> getInnerClusterEdges(Collection<Edge> edges) {
		ArrayList<Edge> result = new ArrayList<Edge>();
		for (Edge e : edges) {
			Node s = e.getSource();
			Node t = e.getTarget();
			String sC = NodeTools.getClusterID(s, "");
			String tC = NodeTools.getClusterID(t, "");
			if (sC.equals(tC))
				result.add(e);
		}
		return result;
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return true;
	}
}
