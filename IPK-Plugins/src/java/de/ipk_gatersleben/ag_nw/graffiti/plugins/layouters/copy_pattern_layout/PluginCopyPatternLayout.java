/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.copy_pattern_layout;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class PluginCopyPatternLayout
					extends IPK_PluginAdapter {
	
	/**
	 * DOCTODO: Include method header
	 */
	public PluginCopyPatternLayout() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH))
			this.algorithms = new Algorithm[] {
								new CopyPatternLayoutAlgorithm()
			};
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
		if (algorithms == null) {
			if (ReleaseInfo.getIsAllowedFeature(FeatureSet.TAB_PATTERNSEARCH))
				this.algorithms = new Algorithm[] {
									new CopyPatternLayoutAlgorithm()
				};
		}
	}
}
