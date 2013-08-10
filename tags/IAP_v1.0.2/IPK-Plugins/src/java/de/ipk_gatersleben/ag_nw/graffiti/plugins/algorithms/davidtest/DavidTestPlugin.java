/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class DavidTestPlugin
					extends IPK_PluginAdapter {
	
	public DavidTestPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.STATISTIC_FUNCTIONS))
			this.algorithms = new Algorithm[] {
								new StatisticsSelection(),
								new ColorizeAlgorithm()
			};
	}
}
