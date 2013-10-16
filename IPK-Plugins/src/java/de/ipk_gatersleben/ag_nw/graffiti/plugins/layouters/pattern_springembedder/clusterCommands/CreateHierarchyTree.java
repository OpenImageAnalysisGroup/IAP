package de.ipk_gatersleben.ag_nw.graffiti.plugins.layouters.pattern_springembedder.clusterCommands;

import java.util.ArrayList;
import java.util.Collection;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.CreateFuncatGraphAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go.CreateGOtreeAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go.InterpreteGOtermsAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.ko_tree.KoTreeAlgorithm;

public class CreateHierarchyTree extends LaunchGui {
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new KoTreeAlgorithm());
		res.add(new CreateGOtreeAlgorithm());
		res.add(new InterpreteGOtermsAlgorithm());
		res.add(new CreateFuncatGraphAlgorithm());
		return res;
	}
	
	@Override
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() != Release.KGML_EDITOR)
			return "Create Hierarchy";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return null;// "Hierarchy";
	}
}
