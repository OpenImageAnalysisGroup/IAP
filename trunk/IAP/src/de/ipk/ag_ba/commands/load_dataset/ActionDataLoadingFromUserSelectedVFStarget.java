package de.ipk.ag_ba.commands.load_dataset;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataLoadingFromUserSelectedVFStarget extends AbstractNavigationAction implements NavigationAction {
	
	private final ArrayList<ExperimentReference> experimentReference;
	private final MongoDB m;
	
	public ActionDataLoadingFromUserSelectedVFStarget(String tooltip) {
		super(tooltip);
		m = null;
		experimentReference = null;
	}
	
	public ActionDataLoadingFromUserSelectedVFStarget(String tooltip, MongoDB m, ArrayList<ExperimentReference> experimentReference) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-Web-64_load.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Remote Storage Location";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (VfsFileProtocol p : VfsFileProtocol.values()) {
			if (p == VfsFileProtocol.LOCAL)
				continue;
			NavigationAction navigationAction = new ActionDataLoadingFromUserSpecficVFStarget(
					"Load from remote site using " + p, m, experimentReference, p);
			res.add(new NavigationButton(navigationAction, guiSetting));
		}
		{
			NavigationAction navigationAction = new ActionDataLoadingFromUserSpecficVFStarget(
					"Load from local file system", m, experimentReference, VfsFileProtocol.LOCAL);
			res.add(new NavigationButton(navigationAction, guiSetting));
		}
		return res;
	}
	
}
