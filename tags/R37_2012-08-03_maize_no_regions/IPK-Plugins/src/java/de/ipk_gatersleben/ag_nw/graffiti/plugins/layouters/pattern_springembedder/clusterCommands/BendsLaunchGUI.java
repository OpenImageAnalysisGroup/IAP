package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Collection;

import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class BendsLaunchGUI extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new RemoveBendsAlgorithm());
		res.add(new IntroduceSelfEdgeBends());
		res.add(new IntroduceBendsAlgorithm());
		
		return res;
	}
	
	@Override
	public String getName() {
		return "Bends...";
	}
	
	@Override
	public String getCategory() {
		return "Edges";
	}
	
}
