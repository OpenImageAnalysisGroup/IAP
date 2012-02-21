// ==============================================================================
//
// AbstractAlgorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AbstractAlgorithm.java,v 1.1 2011-01-31 09:04:43 klukas Exp $

package org.graffiti.plugin.algorithm;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.KeyStroke;

import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

/**
 * Implements some empty versions of non-obligatory methods.
 */
public abstract class AbstractAlgorithm implements Algorithm {
	// ~ Instance fields ========================================================
	
	/** The graph on which the algorithm will work. */
	protected Graph graph;
	
	/** The selection on which the algorithm might work. */
	protected Selection selection;
	
	/** The parameters this algorithm can use. */
	protected Parameter[] parameters;
	
	protected ActionEvent actionEvent = null;
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.plugin.algorithm.Algorithm#setParameters(org.graffiti.plugin.parameter.Parameter[])
	 */
	public void setParameters(Parameter[] params) {
		this.parameters = params;
	}
	
	/**
	 * Default: no accelerator for the menu item, created for this algorithm.
	 */
	public KeyStroke getAcceleratorKeyStroke() {
		return null;
	}
	
	public String getDescription() {
		return null;
	}
	
	/**
	 * Default: no icon next to the menu item, which is created for this algorithm.
	 */
	public boolean showMenuIcon() {
		return false;
	}
	
	protected Collection<Node> getSelectedOrAllNodes() {
		if (selection == null || selection.getNodes().size() <= 0)
			return graph.getNodes();
		else
			return selection.getNodes();
	}
	
	protected Collection<GraphElement> getSelectedOrAllGraphElements() {
		if (selection == null || selection.getElements().size() <= 0)
			return graph.getGraphElements();
		else
			return selection.getElements();
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#getParameters()
	 */
	public Parameter[] getParameters() {
		return this.parameters;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#attach(org.graffiti.graph.Graph)
	 */
	public void attach(Graph graph, Selection selection) {
		this.graph = graph;
		this.selection = selection;
	}
	
	/**
	 * @throws PreconditionException
	 * @see org.graffiti.plugin.algorithm.Algorithm#check()
	 */
	public void check() throws PreconditionException {
		boolean v = false;
		if (v)
			throw new PreconditionException();
	}
	
	public String getCategory() {
		return null;
	}
	
	/**
	 * @see org.graffiti.plugin.algorithm.Algorithm#reset()
	 */
	public void reset() {
		this.graph = null;
		this.parameters = null;
		this.actionEvent = null;
		this.selection = null;
	}
	
	public boolean isLayoutAlgorithm() {
		return false;
	}
	
	public ActionEvent getActionEvent() {
		return actionEvent;
	}
	
	public void setActionEvent(ActionEvent a) {
		actionEvent = a;
	}
	
	public boolean mayWorkOnMultipleGraphs() {
		return false;
	}
	
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
