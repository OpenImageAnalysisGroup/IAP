// ==============================================================================
//
// DefaultAlgorithmManager.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultAlgorithmManager.java,v 1.1 2011-01-31 09:04:56 klukas Exp $

package org.graffiti.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.graffiti.managers.pluginmgr.PluginDescription;
import org.graffiti.plugin.GenericPlugin;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * Manages the map of available algorithms: key = algorithm class names,
 * value = algorithm
 * 
 * @version $Revision: 1.1 $
 */
public class DefaultAlgorithmManager
					implements AlgorithmManager {
	// ~ Instance fields ========================================================
	
	/** The algorithms: key = algorithm class names, value = algorithm */
	private Map<String, Algorithm> algorithms;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Constructs a new algorithm manager.
	 */
	public DefaultAlgorithmManager() {
		algorithms = new HashMap<String, Algorithm>();
	}
	
	// ~ Methods ================================================================
	
	/*
	 * @see org.graffiti.managers.AlgorithmManager#getAlgorithms()
	 */
	@SuppressWarnings("unchecked")
	public List getAlgorithms() {
		return new LinkedList(algorithms.values());
	}
	
	/*
	 * @see org.graffiti.managers.AlgorithmManager#addAlgorithm(org.graffiti.plugin.algorithm.Algorithm)
	 */
	public void addAlgorithm(Algorithm algorithm) {
		algorithms.put(algorithm.getClass().getName(), algorithm);
	}
	
	/*
	 * @see org.graffiti.managers.pluginmgr.PluginManagerListener#pluginAdded(org.graffiti.plugin.GenericPlugin,
	 * org.graffiti.managers.pluginmgr.PluginDescription)
	 */
	public void pluginAdded(GenericPlugin plugin, PluginDescription desc) {
		if (plugin.getAlgorithms() != null) {
			Algorithm[] algorithms = plugin.getAlgorithms();
			
			for (int i = 0; i < algorithms.length; i++) {
				if (algorithms[i] != null)
					addAlgorithm(algorithms[i]);
			}
		}
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
