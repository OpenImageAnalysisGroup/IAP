package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.tools.ActionPerformanceTest;
import de.ipk.ag_ba.commands.experiment.tools.ActionResetConditionFromImageName;
import de.ipk.ag_ba.commands.experiment.tools.ActionSaveWebCamImagesSelectSource;
import de.ipk.ag_ba.commands.experiment.tools.ActionShowXML;
import de.ipk.ag_ba.commands.experiment.tools.ActionSortSubstances;
import de.ipk.ag_ba.commands.experiment.tools.ActionTestMongoIoReadSpeed;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionToolList extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experimentReference;
	
	public ActionToolList(String tooltip, MongoDB m, ExperimentReference experimentReference) {
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
		res.add(new NavigationButton(new ActionResetConditionFromImageName(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionShowXML(m, experimentReference), guiSetting));
		res.add(new NavigationButton(new ActionSaveWebCamImagesSelectSource(m, experimentReference), guiSetting));
		if (false)
			res.add(new NavigationButton(new ActionTestMongoIoReadSpeed(m, experimentReference), guiSetting));
		
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Tools";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-System-Run-64.png";// IAPimages.getToolbox();
	}
}