package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder;

import java.util.ArrayList;
import java.util.Collection;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterInfoAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterInfoFromLabelAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.SetClusterInfoFromSubgraphAlgorithm;

public class SetCluster extends LaunchGui {
	
	public SetCluster() {
		super();
		modal = false;
	}
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new SetClusterInfoAlgorithm());
		res.add(new SetClusterInfoFromLabelAlgorithm());
		res.add(new SetClusterInfoFromSubgraphAlgorithm());
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
			return "Enter Cluster ID";
	}
	
	@Override
	public String getCategory() {
		return "Cluster";
	}
}
