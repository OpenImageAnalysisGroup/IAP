// ==============================================================================
//
// AlgorithmManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AlgorithmManager.java,v 1.1 2011-01-31 09:04:56 klukas Exp $

package org.graffiti.managers;

import java.util.List;

import org.graffiti.managers.pluginmgr.PluginManagerListener;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Provides a generic interface for a component managing a set of algorithms.
 * 
 * @version $Revision: 1.1 $
 * @see org.graffiti.managers.pluginmgr.PluginManagerListener
 */
public interface AlgorithmManager
					extends PluginManagerListener {
	// ~ Methods ================================================================
	
	/**
	 * Returns a <code>java.util.List</code> containing all the <code>Algorithm</code> instances the manager contains.
	 * 
	 * @return a <code>java.util.List</code> containing all the <code>Algorithm</code> instances the manager contains.
	 */
	List<?> getAlgorithms();
	
	// /**
	// * Returns the class name of the specified algorithm. Using the
	// * <code>InstanceLoader</code> an instance of this <code>Algorithm</code>
	// * can be created.
	// *
	// * @param algorithm the <code>Algorithm</code> of which to get the class
	// * name.
	// *
	// * @return the class name of the specified algorithm.
	// */
	// String getClassName(Algorithm algorithm);
	
	/**
	 * Adds the given algorithm to the list of algorithms.
	 * 
	 * @param algorithm
	 *           the algorithm to add to the list.
	 */
	void addAlgorithm(Algorithm algorithm);
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
