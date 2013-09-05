/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.process_alternative_ids;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.ApplyAlternativeIdentifiersTo;

public class AlternativeIdentifiersPlugin extends IPK_PluginAdapter {
	
	public AlternativeIdentifiersPlugin() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			this.algorithms = new Algorithm[] {
								new AdditionalIdentifiersAlgorithm(),
								new ApplyAlternativeIdentifiersTo(),
			};
	}
}
