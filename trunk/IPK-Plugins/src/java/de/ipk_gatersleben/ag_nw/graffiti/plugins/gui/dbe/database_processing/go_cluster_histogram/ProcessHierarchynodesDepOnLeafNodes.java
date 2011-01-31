package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go_cluster_histogram;

import java.util.ArrayList;
import java.util.Collection;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.alt_id_statistics.AlternativeIDannotationStatistics;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class ProcessHierarchynodesDepOnLeafNodes extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new CreateGOchildrenAverageDataDiagramAlgorithm());
		res.add(new CreateGOchildrenClustersHistogramAlgorithm());
		res.add(new ClusterHistogramFisherTest());
		res.add(new CreateGOchildrenTtestHistogramAlgorithm());
		res.add(new AlternativeIDannotationStatistics());
		return res;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			return "Process Leaf-Node Information";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return null;// "Hierarchy";
	}
	
	@Override
	public String getLaunchGuiDescription() {
		return "Information about reachable leaf-nodes is processed and<br>" +
							"added to the working-set of hierarchy-nodes.";
	}
	
}
