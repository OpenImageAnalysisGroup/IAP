/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
package vanted_feature;

import org.SettingsHelperDefaultIsTrue;
import org.graffiti.options.GravistoPreferences;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.NoOverlappOfClustersAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.SetCluster;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.AddRandomClusterInformationAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.ClusterIndividualLayout;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.ClusterOverviewNetworkLaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.PajekClusterColor;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SelectClusterAlgorithm;

/**
 * @author Christian Klukas
 */
public class PluginFeatureClusterCommands
					extends IPK_PluginAdapter {
	
	public static SelectClusterAlgorithm alg;
	
	public PluginFeatureClusterCommands() {
		if (new SettingsHelperDefaultIsTrue().isEnabled("Cluster commands")) {
			this.algorithms = new Algorithm[] {
								new AddRandomClusterInformationAlgorithm(),
								new PajekClusterColor(),
								new ClusterOverviewNetworkLaunchGui(),
								new ClusterIndividualLayout(),
								new SetCluster(),
								new NoOverlappOfClustersAlgorithm()
			};
			alg = new SelectClusterAlgorithm();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.GenericPlugin#configure(java.util.prefs.Preferences)
	 */
	@Override
	public void configure(GravistoPreferences p) {
		super.configure(p);
	}
	
	public static Algorithm getSelectClusterAlgorithm() {
		return alg;
	}
}
