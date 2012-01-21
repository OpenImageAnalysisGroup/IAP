/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 27.2.2006
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.lines_as_substances;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

/**
 * @author Christian Klukas
 */
public class LinesSubstancesPlugin
					extends IPK_PluginAdapter {
	
	public LinesSubstancesPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			this.algorithms = new Algorithm[] {
								new LinesToSubstancesAlgorithm()
			};
	}
}
