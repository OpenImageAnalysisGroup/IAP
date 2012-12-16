package de.ipk.ag_ba.gui;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionPerformanceTest;
import de.ipk.ag_ba.commands.ActionSaveWebCamRange;
import de.ipk.ag_ba.commands.ActionShowXML;
import de.ipk.ag_ba.commands.ActionSortSubstances;
import de.ipk.ag_ba.commands.CloudIoTestAction;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

class ActionToolList extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experimentReference;
	
	ActionToolList(String tooltip, MongoDB m, ExperimentReference experimentReference) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(new ActionPerformanceTest(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionSortSubstances(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionShowXML(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionSaveWebCamRange(m, experimentReference), guiSetting));
		if (false)
			res.add(new NavigationButton(new CloudIoTestAction(m, experimentReference), guiSetting));
		
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Tools";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getToolbox();
	}
}