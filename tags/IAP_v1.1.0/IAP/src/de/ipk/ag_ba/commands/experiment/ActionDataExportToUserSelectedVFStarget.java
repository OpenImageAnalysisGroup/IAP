package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataExportToUserSelectedVFStarget extends AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<ExperimentReference> experimentReference;
	private final MongoDB m;
	private boolean ignoreOutliers;
	
	public ActionDataExportToUserSelectedVFStarget(String tooltip) {
		super(tooltip);
		m = null;
		experimentReference = null;
	}
	
	public ActionDataExportToUserSelectedVFStarget(String tooltip, MongoDB m,
			ArrayList<ExperimentReference> experimentReference, boolean ignoreOutliers) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
		this.ignoreOutliers = ignoreOutliers;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-Web-64_save.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "To Remote Storage Location";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (VfsFileProtocol p : VfsFileProtocol.values()) {
			if (p == VfsFileProtocol.LOCAL)
				continue;
			NavigationAction navigationAction = new ActionDataExportToUserSpecficVFStarget(
					"Copy to remote site using " + p, m, experimentReference, p, ignoreOutliers);
			res.add(new NavigationButton(navigationAction, guiSetting));
		}
		{
			NavigationAction navigationAction = new ActionDataExportToUserSpecficVFStarget(
					"Copy to local file system", m, experimentReference, VfsFileProtocol.LOCAL, ignoreOutliers);
			res.add(new NavigationButton(navigationAction, guiSetting));
		}
		return res;
	}
	
}
