package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_PluginAdapter;

public class GraphGenerationPlugin extends IPK_PluginAdapter {
	public GraphGenerationPlugin() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			this.algorithms = new Algorithm[] {
								new GenerateGraphAlgorithmSelectionGUI()
			};
	}
}
