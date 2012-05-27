/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder_no_cache;

import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class PluginPatternSpringembedderNoCache
					extends IPK_PluginAdapter {
	
	/**
	 * DOCTODO: Include method header
	 */
	public PluginPatternSpringembedderNoCache() {
		this.algorithms = new Algorithm[1];
		this.algorithms[0] = new PatternSpringembedder();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
		if (algorithms == null) {
			this.algorithms = new Algorithm[1];
			this.algorithms[0] = new PatternSpringembedder();
		}
	}
}
