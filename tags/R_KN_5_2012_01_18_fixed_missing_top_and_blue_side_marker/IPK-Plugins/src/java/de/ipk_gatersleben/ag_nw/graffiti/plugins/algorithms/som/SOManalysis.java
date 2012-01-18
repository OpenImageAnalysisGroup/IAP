package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.som;

import java.util.ArrayList;
import java.util.Collection;

import org.FeatureSet;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.graph_to_origin_mover.NoOverlappOfClustersAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands.PajekClusterColor;

public class SOManalysis extends LaunchGui implements Algorithm {
	
	public SOManalysis() {
		super();
		algBTsize = ButtonSize.LARGE;
		modal = false;
	}
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG) {
			res.add(new SOMclusterAnalysis());
			res.add(new SOMclusterAnalysisDoCluster());
			res.add(null);
			res.add(new SOMprintDataset());
			res.add(new SOMautoCluster());
			res.add(new MultiDataView());
		} else
			if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING)) {
				res.add(new SOMclusterAnalysis());
				res.add(new SOMclusterAnalysisDoCluster());
			}
		res.add(null);
		res.add(new PajekClusterColor());
		res.add(new NoOverlappOfClustersAlgorithm());
		return res;
	}
	
	@Override
	public boolean closeDialogBeforeExecution(Algorithm algorithm) {
		return !(algorithm instanceof SOMclusterAnalysis || algorithm instanceof SOMclusterAnalysisDoCluster);
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.DATAMAPPING))
			return "Self-Organizing Map (SOM)...";
		else
			return null;
	}
	
	@Override
	public String getLaunchGuiDescription() {
		return "With the first command a SOM weight matrix is trained, based on<br>" +
							"the mapping-data to be analyzed.<br>" +
							"The second command uses the trained weight matrix for the assignment<br>" +
							"of cluster IDs. The nodes or edges of the working-set belong afterwards<br>" +
							"to different cluster groups, based on the similarity of the mapping data in<br>" +
							"comparison to the SOM 'centroids'.<br>" +
							"SOM centroids are comparable to a artificial average data mapping. <br>" +
							"Different centroids are computed in a way to be similar to a large<br>" +
							"set of data-mappings, but also they tend to be dissimilar to reflect<br>" +
							"different but common data distributions.<br>" +
							"The initial SOM weight matrix is initialized with random data, therfore<br>" +
							"on different runs different results may be computed.<br><br>" +
							"Please consider to consult reference literature or general introduction on<br>" +
							"this topic (e.g. http://en.wikipedia.org/wiki/Self-organizing_map).<br><br>" +
							"The first two listed command buttons are part of the SOM analysis. The<br>" +
							"calculation result is made visible with the third command button<br>" +
							"for coloring of different graph element clustes or with the fourth<br>" +
							"button for re-layouting the graph.";
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
	
}
