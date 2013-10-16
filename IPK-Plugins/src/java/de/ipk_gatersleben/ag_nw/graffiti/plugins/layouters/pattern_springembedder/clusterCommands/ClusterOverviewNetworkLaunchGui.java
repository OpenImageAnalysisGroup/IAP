package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Collection;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class ClusterOverviewNetworkLaunchGui extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new CreateClusterGraphAlgorithm());
		res.add(new ClusterGraphLayout());
		// res.add(new ClusterIndividualLayout());
		res.add(null);
		res.add(new ShowClusterGraphAlgorithm());
		return res;
	}
	
	@Override
	public boolean closeDialogBeforeExecution(Algorithm algorithm) {
		return !(algorithm instanceof SetClusterInfoAlgorithm);
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Process Overview-Graph...";
	}
	
	@Override
	public String getCategory() {
		return "Cluster";
	}
	
}
