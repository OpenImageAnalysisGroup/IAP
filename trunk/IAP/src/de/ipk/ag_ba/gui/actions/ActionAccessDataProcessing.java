package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;

import de.ipk.ag_ba.gui.actions.lemnatec.ActionLemnaTecNavigation;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionAccessDataProcessing extends AbstractNavigationAction {
	NavigationButton src = null;
	private final GUIsetting guiSetting;
	
	public ActionAccessDataProcessing(GUIsetting guIsetting) {
		super("Access IAP Phenotyping Service Platform");
		this.guiSetting = guIsetting;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> phenoDBcommands = new ArrayList<NavigationButton>();
		
		NavigationAction saveExperimentAction = new SaveExperimentInCloud(false);
		NavigationButton uploadDataEntity = new NavigationButton(saveExperimentAction, "Process Files", "img/ext/user-desktop.png",
							"img/ext/user-desktop.png", src != null ? src.getGUIsetting() : guiSetting);
		phenoDBcommands.add(uploadDataEntity);
		
		NavigationAction lemnaExperiments = new ActionLemnaTecNavigation();
		NavigationButton lemnaEntity = new NavigationButton(lemnaExperiments, src != null ? src.getGUIsetting()
							: guiSetting);
		if (IAPservice.isReachable("lemna-db.ipk-gatersleben.de"))
			phenoDBcommands.add(lemnaEntity);
		
		for (MongoDB m : MongoDB.getMongos()) {
			NavigationAction mongoExperiments = new ActionMongoExperimentsNavigation(m, true, false);
			NavigationButton mongo = new NavigationButton(mongoExperiments, m.getDisplayName(), "img/ext/network-mongo.png",
								"img/ext/network-mongo-gray.png", src != null ? src.getGUIsetting() : guiSetting);
			
			phenoDBcommands.add(mongo);
		}
		
		// phenoDBcommands.add(DBElogin.getDBEloginButton(DBEtype.Phenotyping,
		// true));
		// phenoDBcommands.add(DBElogin.getDBEloginButton(DBEtype.Phenotyping,
		// false));
		
		return phenoDBcommands;
	}
}