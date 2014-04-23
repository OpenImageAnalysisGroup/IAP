package de.ipk.ag_ba.commands.load_dataset;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.vanted.plugin.VfsFileProtocol;

public class ActionDataLoadingFromUserSelectedVFStarget extends AbstractNavigationAction implements NavigationAction {
	
	public ActionDataLoadingFromUserSelectedVFStarget(String tooltip) {
		super(tooltip);
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
		return "Add Remote Storage Location";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		for (VfsFileProtocol p : VfsFileProtocol.values()) {
			if (p == VfsFileProtocol.LOCAL)
				continue;
			NavigationAction navigationAction = new ActionDataLoadingFromUserSpecficVFStarget(
					"Load from remote site using " + p, p);
			res.add(new NavigationButton(navigationAction, guiSetting));
		}
		{
			NavigationAction navigationAction = new ActionDataLoadingFromUserSpecficVFStarget(
					"Load from local file system", VfsFileProtocol.LOCAL);
			res.add(new NavigationButton(navigationAction, guiSetting));
		}
		return res;
	}
	
}
