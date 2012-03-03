package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import java.util.ArrayList;
import java.util.Collection;

import org.FeatureSet;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SetURLAlgorithm;

public class LinkSelection extends LaunchGui implements Algorithm {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new PathwayReferenceAlgorithm());
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.URL_NODE_ANNOTATION))
			res.add(new SetURLAlgorithm());
		res.add(new PathwayReferenceAutoCreationAlgorithm());
		return res;
	}
	
	@Override
	public String getName() {
		return "Add Link...";
	}
	
	@Override
	public String getCategory() {
		return "menu.edit";
	}
}
