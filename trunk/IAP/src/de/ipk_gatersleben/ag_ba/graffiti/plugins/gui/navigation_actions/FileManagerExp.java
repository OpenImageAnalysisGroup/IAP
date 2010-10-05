package de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_actions;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.MainPanelComponent;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.interfaces.NavigationAction;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.navigation_model.NavigationGraphicalEntity;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk_gatersleben.ag_ba.graffiti.plugins.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public class FileManagerExp extends AbstractNavigationAction {
	private final String login;
	private final ExperimentReference experiment;
	private final String pass;
	NavigationGraphicalEntity src = null;
	MainPanelComponent mpc;

	public FileManagerExp(String login, String pass, ExperimentReference experiment) {
		super("Access primary and annotation files");
		this.login = login;
		this.experiment = experiment;
		this.pass = pass;
	}

	@Override
	public void performActionCalculateResults(NavigationGraphicalEntity src) {
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
	public ArrayList<NavigationGraphicalEntity> getResultNewNavigationSet(ArrayList<NavigationGraphicalEntity> currentSet) {
		ArrayList<NavigationGraphicalEntity> res = new ArrayList<NavigationGraphicalEntity>(currentSet);
		res.add(src);
		return res;
	}

	@Override
	public ArrayList<NavigationGraphicalEntity> getResultNewActionSet() {
		return new ArrayList<NavigationGraphicalEntity>();
	}

	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}

	public static NavigationGraphicalEntity getFileManagerEntity(final String login, final String pass,
			final ExperimentReference experimentRef) {
		NavigationAction fileManagerAction = new FileManagerExp(login, pass, experimentRef);
		NavigationGraphicalEntity fileManager = new NavigationGraphicalEntity(fileManagerAction, "File Manager",
				"img/ext/applications-system.png");
		return fileManager;
	}
}