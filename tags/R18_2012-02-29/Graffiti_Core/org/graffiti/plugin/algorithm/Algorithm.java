// ==============================================================================
//
// Algorithm.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Algorithm.java,v 1.1 2011-01-31 09:04:44 klukas Exp $

package org.graffiti.plugin.algorithm;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import org.graffiti.graph.Graph;
import org.graffiti.plugin.parameter.Parameter;
import org.graffiti.selection.Selection;

/**
 * An <code>Algorithm</code> has to provide a function that returns an array of <code>Parameters</code> it needs, in order to give user interfaces the
 * possibility to provide the <code>Algorithm</code> with apropriate
 * parameters.
 * 
 * @version $Revision: 1.1 $
 */
public interface Algorithm {
	// ~ Methods ================================================================
	
	/**
	 * Returns the name (id) of the algorithm.
	 * 
	 * @return DOCUMENT ME!
	 */
	public String getName();
	
	/**
	 * Sets the parameters for this algorithm. Must have the same types and
	 * order as the array returned by <code>getParameter</code>.
	 */
	public void setParameters(Parameter[] params);
	
	/**
	 * Returns a list of <code>Parameter</code> that are set for this
	 * algorithm.
	 * 
	 * @return a collection of <code>Parameter</code> that are needed by the <code>Algorithm</code>.
	 */
	public Parameter[] getParameters();
	
	/**
	 * Attaches the given graph to this algorithm.
	 * A ttaches the given Selection information to the algorithm.
	 * 
	 * @param g
	 *           the graph to attach.
	 * @param selection
	 *           the selection to attach.
	 */
	public void attach(Graph g, Selection selection);
	
	/**
	 * Checks whether all preconditions of the current graph are satisfied.
	 * 
	 * @throws PreconditionException
	 *            if the preconditions of the current graph
	 *            are not satisfied.
	 */
	public void check()
						throws PreconditionException;
	
	/**
	 * Executes the whole algorithm.
	 */
	public void execute();
	
	/**
	 * Resets the internal state of the algorithm.
	 */
	public void reset();
	
	/**
	 * Returns the category an algorithm should assigned to.
	 * Return NULL if the algorithm should be sorted directly
	 * in the plugin menu.
	 * 
	 * @return The category an algorithm should assigned to.
	 */
	public String getCategory();
	
	/**
	 * A Layoutalgorithm should return true. All other types of algorithms should return false.
	 * 
	 * @return
	 */
	public boolean isLayoutAlgorithm();
	
	/**
	 * Override this method to give a different look and feel to the application,
	 * and to put the plugin icon next to the menu item in the main menu bar.
	 * 
	 * @return True, if the plugin icon should be shown next to the menu item.
	 *         Return FALSE (default!), if no icon should be shown in the menu.
	 */
	public boolean showMenuIcon();
	
	/**
	 * Override this method to provide a accelerator hot key for the algorithm.
	 * 
	 * @return NULL (default) if no accelerator should be assigned for this algorithm.
	 */
	public KeyStroke getAcceleratorKeyStroke();
	
	public String getDescription();
	
	public void setActionEvent(ActionEvent a);
	
	public ActionEvent getActionEvent();
	
	public boolean mayWorkOnMultipleGraphs();
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
