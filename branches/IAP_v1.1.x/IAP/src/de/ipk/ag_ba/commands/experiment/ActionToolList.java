package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;

public class ActionToolList extends AbstractNavigationAction implements ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experimentReference;
	
	public ActionToolList(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		for (ActionDataProcessing adp : IAPpluginManager.getInstance().getExperimentToolActions(experimentReference))
			res.add(new NavigationButton(adp, guiSetting));
		
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
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.m = experimentReference.m;
		this.experimentReference = experimentReference;
	}
}