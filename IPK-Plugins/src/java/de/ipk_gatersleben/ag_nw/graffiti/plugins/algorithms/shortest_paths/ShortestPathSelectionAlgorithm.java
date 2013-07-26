/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * $Id: ShortestPathSelectionAlgorithm.java,v 1.3 2013-02-03 19:28:03 klukas Exp $
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.KeyStroke;

import org.Release;
import org.ReleaseInfo;
import org.apache.commons.collections.set.ListOrderedSet;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;
import org.graffiti.plugin.algorithm.PreconditionException;
import org.graffiti.plugin.parameter.BooleanParameter;
import org.graffiti.plugin.parameter.Parameter;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author klukas
 */
public class ShortestPathSelectionAlgorithm
		extends AbstractAlgorithm {
	
	private boolean settingIncludeInnerEdges = false;
	private boolean settingDirected = true;
	private boolean settingIncludeEdges = true;
	
	/**
	 * Constructs a new instance.
	 */
	public ShortestPathSelectionAlgorithm() {
	}
	
	@Override
	public void check() throws PreconditionException {
		super.check();
		if (selection == null || selection.getNumberOfNodes() < 2)
			throw new PreconditionException("at least one start and one end node has to be selected");
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	@Override
	public Parameter[] getParameters() {
		return new Parameter[] {
				new BooleanParameter(settingDirected, "Consider Edge Direction", ""),
				new BooleanParameter(settingIncludeEdges, "Select Edges", "If enabled, edges along the shortest path(s) are selected"),
				new BooleanParameter(settingIncludeInnerEdges, "Select Inner-Edges",
						"If selected, all edges connecting nodes of the shortest path(s) are selected") };
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm# setParameters(org.graffiti.plugin.algorithm.Parameter)
	 */
	@Override
	public void setParameters(Parameter[] params) {
		int i = 0;
		settingDirected = ((BooleanParameter) params[i++]).getBoolean();
		settingIncludeEdges = ((BooleanParameter) params[i++]).getBoolean();
		settingIncludeInnerEdges = ((BooleanParameter) params[i++]).getBoolean();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void execute() {
		ArrayList<GraphElement> currentSelElements = new ArrayList<GraphElement>();
		graph = MainFrame.getInstance().getActiveEditorSession().getGraph();
		graph.numberGraphElements();
		if (selection != null)
			currentSelElements.addAll(selection.getElements());
		ListOrderedSet targetNodesToBeProcessed = new ListOrderedSet();
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node) {
				Node n = (Node) ge;
				targetNodesToBeProcessed.add(n);
			}
		}
		for (GraphElement ge : currentSelElements) {
			if (ge instanceof Node) {
				Node n = (Node) ge;
				Collection<GraphElement> shortestPathNodesAndEdges =
						WeightedShortestPathSelectionAlgorithm.getShortestPathElements(
								graph.getGraphElements(),
								n,
								targetNodesToBeProcessed, settingDirected, false,
								false, Double.MAX_VALUE, null, false, false, false, true);
				new ArrayList<GraphElement>(shortestPathNodesAndEdges);
				for (GraphElement gg : shortestPathNodesAndEdges) {
					if (settingIncludeEdges && (gg instanceof Edge))
						selection.add(gg);
					if (gg instanceof Node)
						selection.add(gg);
				}
				if (!settingDirected)
					targetNodesToBeProcessed.remove(n);
			}
		}
		
		if (settingIncludeInnerEdges)
			for (Node n : selection.getNodes()) {
				for (Edge e : n.getEdges()) {
					if (selection.getNodes().contains(e.getSource()) && selection.getNodes().contains(e.getTarget()))
						selection.add(e);
				}
			}
		GraphHelper.selectElements((Collection) selection.getElements());
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	@Override
	public String getName() {
		return "Find Shortest Path...";
	}
	
	@Override
	public KeyStroke getAcceleratorKeyStroke() {
		return KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
	}
	
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "menu.edit";
		else
			return "Analysis";
	}
	
	@Override
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
}
