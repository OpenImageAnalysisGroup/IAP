package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.SystemOptions;

import de.ipk.ag_ba.commands.lemnatec.ActionLemnaTecNavigation;
import de.ipk.ag_ba.commands.mongodb.ActionMongoExperimentsNavigation;
import de.ipk.ag_ba.commands.mongodb.SaveExperimentInCloud;
import de.ipk.ag_ba.gui.IAPoptions;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionAccessDataProcessing extends AbstractNavigationAction {
	NavigationButton src = null;
	private final GUIsetting guiSetting;
	
	public ActionAccessDataProcessing(GUIsetting guIsetting) {
		super("Access IAP Cloud Computing Platform");
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
		boolean addFilesIconAtMainLevel = IAPmain.getRunMode() == IAPrunMode.SWING_MAIN || IAPmain.getRunMode() == IAPrunMode.SWING_APPLET;
		if (addFilesIconAtMainLevel &&
				SystemOptions.getInstance().getBoolean("File Import", "Show Load Files Icon", false)) {
			NavigationAction saveExperimentAction = new SaveExperimentInCloud(false);
			NavigationButton uploadDataEntity = new NavigationButton(saveExperimentAction, "Process files",
					"img/ext/user-desktop.png",
					"img/ext/user-desktop.png", src != null ? src.getGUIsetting() : guiSetting);
			phenoDBcommands.add(uploadDataEntity);
		}
		
		boolean showLoadLTfileExport = IAPoptions.getInstance().getBoolean("File Import", "LT-DB//show_load_exported_icon", true);
		if (showLoadLTfileExport) {
			NavigationButton ltl = new NavigationButton(
					new ActionLoadLTexportFileHierarchy("Load LT exported image data from folder hierarchy"),
					src != null ? src.getGUIsetting() : guiSetting);
			phenoDBcommands.add(ltl);
		}
		
		boolean lt = IAPoptions.getInstance().getBoolean("LT-DB", "show_icon", true);
		if (lt) {
			NavigationAction lemnaExperiments = new ActionLemnaTecNavigation();
			NavigationButton lemnaEntity = new NavigationButton(lemnaExperiments, src != null ? src.getGUIsetting()
					: guiSetting);
			phenoDBcommands.add(lemnaEntity);
		}
		
		for (MongoDB m : MongoDB.getMongos()) {
			NavigationAction mongoExperiments = new ActionMongoExperimentsNavigation(m, false, false);
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