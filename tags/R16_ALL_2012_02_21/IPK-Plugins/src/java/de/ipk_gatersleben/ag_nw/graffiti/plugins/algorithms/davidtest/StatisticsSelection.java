package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.davidtest;

import java.util.ArrayList;
import java.util.Collection;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class StatisticsSelection extends LaunchGui implements Algorithm {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new DavidTestAlgorithm());
		res.add(new GrubbsTestAlgorithm());
		res.add(new ShowStatisticsTab());
		return res;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.STATISTIC_FUNCTIONS))
			return "Perform Statistical Analysis...";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Mapping";
	}
}
