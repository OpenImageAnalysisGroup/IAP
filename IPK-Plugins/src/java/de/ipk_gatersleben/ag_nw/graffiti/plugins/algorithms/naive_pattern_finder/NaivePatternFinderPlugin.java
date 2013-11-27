/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.naive_pattern_finder;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.GenericPluginAdapter;
import org.graffiti.plugin.algorithm.Algorithm;

/**
 * The plugin wrapper for the <code>NaivePatternFinderAlgorithm</code>.
 * 
 * @author Dirk Koschützki
 * @see NaivePatternFinderAlgorithm
 */
public class NaivePatternFinderPlugin
					extends GenericPluginAdapter {
	/*************************************************************/
	/* Declarations of methods */
	/*************************************************************/
	
	/**
	 * Constructor for the Plugin.
	 */
	public NaivePatternFinderPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH))
			this.algorithms = new Algorithm[] {
				// new NaivePatternFinderAlgorithm()
				// no menu item needed for that command (included in side search tab
				};
	}
}
