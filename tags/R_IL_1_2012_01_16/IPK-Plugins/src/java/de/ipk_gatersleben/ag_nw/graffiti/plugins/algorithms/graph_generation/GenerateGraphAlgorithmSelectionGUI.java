package de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.graph_generation;

import java.util.ArrayList;
import java.util.Collection;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class GenerateGraphAlgorithmSelectionGUI extends LaunchGui {
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new ErdosRenyiGraphGenerator());
		res.add(new WattsStrogatzGraphGenerator());
		return res;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return null;
		else
			return "Create Random Network...";
	}
	
	@Override
	public String getCategory() {
		return "menu.file";
	}
	
	@Override
	public boolean activeForView(View v) {
		return true;
	}
	
}