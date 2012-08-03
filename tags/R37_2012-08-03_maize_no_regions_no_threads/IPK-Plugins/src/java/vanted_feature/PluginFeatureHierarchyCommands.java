/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import org.Release;
import org.ReleaseInfo;
import org.SettingsHelperDefaultIsTrue;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.kegg_reaction.CreateKeggReactionNetworkAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.hierarchy.HierarchyAlgorithms;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy.HierarchyAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.hierarchy.SelectLeafNodesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.ExpandSelectionAlgorithm;

/**
 * @author Christian Klukas
 */
public class PluginFeatureHierarchyCommands
					extends IPK_PluginAdapter {
	
	private static Algorithm[] otherAlgorithms = null;
	
	public PluginFeatureHierarchyCommands() {
		if (new SettingsHelperDefaultIsTrue().isEnabled("Hierarchy commands")) {
			if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR) {
				this.algorithms = new Algorithm[] {
									new HierarchyAlgorithms(),
									new CreateKeggReactionNetworkAlgorithm(),
									new HierarchyAlgorithm(),
									new SelectLeafNodesAlgorithm(),
				};
				otherAlgorithms = new Algorithm[] {
									new ExpandSelectionAlgorithm(true, false),
									new ExpandSelectionAlgorithm(false, false),
									new ExpandSelectionAlgorithm(true, true)
				};
			}
		}
	}
	
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
	}
	
	public static Algorithm[] getSelectionAlgorithms() {
		Algorithm[] res = new Algorithm[] {};
		if (otherAlgorithms != null)
			res = otherAlgorithms;
		return res;
	}
}
