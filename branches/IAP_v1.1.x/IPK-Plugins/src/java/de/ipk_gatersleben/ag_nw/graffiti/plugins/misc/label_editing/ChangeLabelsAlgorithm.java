package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.label_editing;

import java.util.ArrayList;
import java.util.Collection;

import org.Release;
import org.ReleaseInfo;
import org.graffiti.plugin.algorithm.Algorithm;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.InterpreteLabelNamesAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui.LaunchGui;

public class ChangeLabelsAlgorithm extends LaunchGui {
	
	public ChangeLabelsAlgorithm() {
		super();
		algBTsize = ButtonSize.SMALL;
	}
	
	@Override
	protected Collection<Algorithm> getAlgorithms() {
		ArrayList<Algorithm> res = new ArrayList<Algorithm>();
		res.add(new ReplaceLabelAlgorithm());
		res.add(new InterpreteLabelNamesAlgorithm());
		res.add(new RemoveParenthesisLabels());
		res.add(new RestoreLabelAlgorithm());
		res.add(null);
		res.add(new EnrichHiddenLabelsAlgorithm());
		res.add(new RemoveHiddenLabelsAlgorithm());
		return res;
	}
	
	@Override
	public String getName() {
		return "Change Labels...";
	}
	
	@Override
	public String getCategory() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR)
			return "Nodes";
		else
			return "menu.edit";
	}
	
}
