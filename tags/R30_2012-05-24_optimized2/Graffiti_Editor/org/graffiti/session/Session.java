// ==============================================================================
//
// Session.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: Session.java,v 1.1 2011-01-31 09:04:31 klukas Exp $

package org.graffiti.session;

import java.util.LinkedList;
import java.util.List;

import org.ErrorMsg;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.managers.AlgorithmManager;
import org.graffiti.managers.DefaultAlgorithmManager;
import org.graffiti.managers.DefaultModeManager;
import org.graffiti.managers.ModeManager;
import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.EditorPlugin;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.mode.Mode;
import org.graffiti.plugin.view.View;

/**
 * Contains a session. A session consists of a <code>org.graffiti.graph.Graph</code> and a list of corresponding <code>org.graffiti.plugin.view.View</code>s.
 * Every <code>Session</code> contains a <code>GraphConstraintChecker</code> which checks the constraints
 * defined by the current mode.
 * 
 * @see org.graffiti.graph.Graph
 * @see org.graffiti.plugin.view.View
 * @see org.graffiti.plugin.mode.Mode
 */
public class Session
					implements ConstraintCheckerListener {
	// ~ Instance fields ========================================================
	
	/**
	 * The list of <code>org.graffiti.plugin.algorithm.Algorithm</code>s the <code>Session</code> manages.
	 */
	protected AlgorithmManager algorithmManager;
	
	/** The graph object of this session. */
	protected Graph graph;
	
	// /** The constraint checker of the graph. */
	// protected GraphConstraintChecker constraintChecker;
	
	/** The list of views (class names of the views) of this session. */
	protected List<View> views;
	
	/** The active mode of this session. */
	protected Mode activeMode;
	
	/**
	 * The list of <code>org.graffiti.plugin.mode.Mode</code>s the <code>Session</code> manages.
	 */
	protected ModeManager modeManager;
	
	/** The active view in this session. */
	protected View activeView;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new session instance with an empty graph and the
	 * corresponding constraint checker.
	 */
	public Session() {
		this(new AdjListGraph());
	}
	
	/**
	 * Constructs a new session instance with the given graph.
	 * 
	 * @param graph
	 *           the graph to be used for this session.
	 */
	public Session(Graph graph) {
		this.graph = graph;
		modeManager = new DefaultModeManager();
		algorithmManager = new DefaultAlgorithmManager();
		
		this.views = new LinkedList<View>();
		// this.constraintChecker = new GraphConstraintChecker(graph, this);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * Returns the mode that is active in this session.
	 * 
	 * @return the mode active in this session.
	 */
	public Mode getActiveMode() {
		return this.activeMode;
	}
	
	/**
	 * Sets the activeView.
	 * 
	 * @param activeView
	 *           The activeView to set
	 */
	public void setActiveView(View activeView) {
		this.activeView = activeView;
	}
	
	/**
	 * Returns the activeView.
	 * 
	 * @return View
	 */
	public View getActiveView() {
		return activeView;
	}
	
	/**
	 * Returns the class name of the specified algorithm. Using the <code>InstanceLoader</code> an instance of this <code>Algorithm</code> can be created.
	 * 
	 * @param algorithm
	 *           the <code>Algorithm</code> of which to get the class
	 *           name.
	 * @return the class name of the specified algorithm.
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	public String getClassName(Algorithm algorithm) {
		throw new RuntimeException("Implement me");
	}
	
	/**
	 * Returns the graph of this session.
	 * 
	 * @return the graph of this session.
	 */
	public Graph getGraph() {
		return this.graph;
	}
	
	/**
	 * Returns <code>true</code>, if the graph in this session has been
	 * modified.
	 * 
	 * @return True, if the graph has been modified. False, if not.
	 */
	public boolean isModified() {
		return graph.isModified();
	}
	
	/**
	 * Returns the list of views in the manager.
	 * 
	 * @return the list of views in the manager.
	 */
	public List<View> getViews() {
		return views;
	}
	
	/**
	 * Adds a new View to the inner list of views.
	 * 
	 * @param view
	 *           a view to be added.
	 */
	public void addView(View view) {
		views.add(view);
	}
	
	/**
	 * Changes the active mode of this session.
	 * 
	 * @param activeMode
	 *           the new active mode.
	 */
	public void changeActiveMode(Mode activeMode) {
		this.activeMode = activeMode;
	}
	
	/**
	 * Handles the failed constraint check.
	 * 
	 * @param msg
	 *           tells details about the unsatisfied constraints.
	 */
	public void checkFailed(String msg) {
	}
	
	/**
	 * Closes this session. Closes all the views of this session.
	 */
	public void close() {
		for (View v : views) {
			v.close();
		}
	}
	
	/**
	 * Called by the plugin manager, iff a plugin has been added.
	 * 
	 * @param plugin
	 *           the added plugin.
	 * @param desc
	 *           the description of the new plugin.
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		Algorithm[] algs = plugin.getAlgorithms();
		
		for (int i = 0; i < algs.length; i++) {
			algorithmManager.addAlgorithm(algs[i]);
		}
		
		try {
			Mode[] ms = ((EditorPlugin) plugin).getModes();
			
			for (int i = 0; i < ms.length; i++) {
				modeManager.addMode(ms[i]);
			}
		} catch (ClassCastException cce) {
			// only EditorPlugins provide modes
		}
		
		String[] vs = plugin.getViews();
		
		for (int i = 0; i < vs.length; i++) {
			View v;
			try {
				v = (View) Class.forName(vs[i]).newInstance();
				views.add(v);
			} catch (InstantiationException e) {
				ErrorMsg.addErrorMessage(e);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				ErrorMsg.addErrorMessage(e);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				ErrorMsg.addErrorMessage(e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Removes the given view from this session.
	 * 
	 * @param view
	 *           the view to be removed from this session.
	 * @throws IllegalArgumentException
	 *            DOCUMENT ME!
	 */
	public void removeView(View view) {
		if (view == null) {
			throw new IllegalArgumentException(
								"trying to remove a view, which is null.");
		}
		
		views.remove(view);
	}
	
	// /**
	// * Checks whether the graph satisfies all the constraints.
	// *
	// * @throws UnsatisfiedConstraintException if there es a constraint which is
	// * not satisfied.
	// */
	// public void validateConstraints()
	// {
	// constraintChecker.checkConstraints();
	// }
	
	@Override
	public String toString() {
		if (getGraph() != null)
			return getGraph().toString() + " (" + getViews().size() + " views)";
		else
			return super.toString();
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
