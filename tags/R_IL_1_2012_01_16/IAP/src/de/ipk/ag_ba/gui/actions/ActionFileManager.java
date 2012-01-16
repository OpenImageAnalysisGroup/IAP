package de.ipk.ag_ba.gui.actions;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionFileManager extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	
	public ActionFileManager(MongoDB m, ExperimentReference experiment) {
		super("Access primary and annotation files");
		this.m = m;
		this.experiment = experiment;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		try {
			SupplementaryFilePanelMongoDB sfp = new SupplementaryFilePanelMongoDB(m, experiment.getData(m),
								experiment.getExperimentName());
			mpc = new MainPanelComponent(sfp);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		// todo add zoom slider (default, large, extra large)
		// todo add plant filter (all, ID 1, ID 2, ID 3, ...)
		if (m != null)
			res.add(new NavigationButton("Save Annotation Changes", new ActionCopyToMongo(m, experiment, true), src.getGUIsetting()));
		
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	public static NavigationButton getFileManagerEntity(MongoDB m,
						final ExperimentReference experimentRef, GUIsetting guiSetting) {
		NavigationAction fileManagerAction = new ActionFileManager(m, experimentRef);
		NavigationButton fileManager = new NavigationButton(fileManagerAction, "View Images",
							"img/ext/user-desktop.png",
							// "img/ext/applications-system.png",
				guiSetting);
		return fileManager;
	}
	
	public ExperimentReference getExperimentReference() {
		return experiment;
	}
}