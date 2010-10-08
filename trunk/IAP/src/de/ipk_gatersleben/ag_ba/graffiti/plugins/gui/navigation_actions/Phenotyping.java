package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.util.ArrayList;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;

/**
 * @author klukas
 * 
 */
public class Phenotyping extends AbstractNavigationAction {
	NavigationGraphicalEntity src = null;

	public Phenotyping() {
		super("Access IAP Phenotyping Service Platform");
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
		this.src = src;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		ArrayList<NavigationGraphicalEntity> phenoDBcommands = new ArrayList<NavigationGraphicalEntity>();

		NavigationAction analyzeAction = new UploadImagesToCloud(false);
		NavigationGraphicalEntity analyzeEntity = new NavigationGraphicalEntity(analyzeAction, "Process Files",
				"img/ext/user-desktop.png", "img/ext/user-desktop.png");
		phenoDBcommands.add(analyzeEntity);

		NavigationAction lemnaExperiments = new LemnaTecNavigationAction();
		NavigationGraphicalEntity lemnaEntity = new NavigationGraphicalEntity(lemnaExperiments);

		phenoDBcommands.add(lemnaEntity);

		String login = "";
		String pass = "";
		NavigationAction mongoExperiments = new MongoExperimentsNavigationAction(login, pass);
		NavigationGraphicalEntity mongo = new NavigationGraphicalEntity(mongoExperiments, "IAP Cloud",
				"img/ext/network-mongo.png", "img/ext/network-mongo-gray.png");

		phenoDBcommands.add(mongo);

		// phenoDBcommands.add(DBElogin.getDBEloginButton(DBEtype.Phenotyping,
		// true));
		// phenoDBcommands.add(DBElogin.getDBEloginButton(DBEtype.Phenotyping,
		// false));

		return phenoDBcommands;
	}
}