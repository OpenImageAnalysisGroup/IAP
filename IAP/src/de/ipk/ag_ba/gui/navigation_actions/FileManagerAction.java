package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public class FileManagerAction extends AbstractNavigationAction {
	private final String login;
	private final ExperimentReference experiment;
	private final String pass;
	NavigationButton src = null;
	MainPanelComponent mpc;

	public FileManagerAction(String login, String pass, ExperimentReference experiment) {
		super("Access primary and annotation files");
		this.login = login;
		this.experiment = experiment;
		this.pass = pass;
	}

	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;

		try {
			SupplementaryFilePanelMongoDB sfp = new SupplementaryFilePanelMongoDB(login, pass, experiment.getData(),
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
		return res;
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}

	public static NavigationButton getFileManagerEntity(final String login, final String pass,
			final ExperimentReference experimentRef, GUIsetting guiSetting) {
		NavigationAction fileManagerAction = new FileManagerAction(login, pass, experimentRef);
		NavigationButton fileManager = new NavigationButton(fileManagerAction, "View Data",
				"img/ext/applications-system.png", guiSetting);
		return fileManager;
	}
}