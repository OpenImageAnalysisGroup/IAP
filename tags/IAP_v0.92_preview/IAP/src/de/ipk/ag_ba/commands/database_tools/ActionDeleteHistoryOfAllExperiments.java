package de.ipk.ag_ba.commands.database_tools;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class ActionDeleteHistoryOfAllExperiments extends AbstractNavigationAction {
	
	private MongoDB m;
	private NavigationButton src;
	
	ArrayList<String> history = new ArrayList<String>();
	
	public ActionDeleteHistoryOfAllExperiments(String tooltip) {
		super(tooltip);
	}
	
	public ActionDeleteHistoryOfAllExperiments(MongoDB m) {
		this("Delete all old data sets (which have a newer version, " +
				"Unit Test data is not affected)");
		this.m = m;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		ArrayList<ExperimentHeaderInterface> expHeaders = m.getExperimentList(null, status);
		int toBeDeleted = 0;
		for (ExperimentHeaderInterface ehi : expHeaders) {
			if (!ehi.getExperimentName().startsWith("Unit Test "))
				for (ExperimentHeaderInterface old : ehi.getHistory().values()) {
					toBeDeleted++;
				}
		}
		
		int deleted = 0;
		for (ExperimentHeaderInterface ehi : expHeaders) {
			if (!ehi.getExperimentName().startsWith("Unit Test "))
				for (ExperimentHeaderInterface old : ehi.getHistory().values()) {
					m.deleteExperiment(old.getDatabaseId());
					deleted++;
					status.setCurrentStatusValueFine(100d * deleted / toBeDeleted);
					status.setCurrentStatusText2("Deleted " + old.getExperimentName() +
							" (from " + SystemAnalysis.getCurrentTime(old.getStorageTime().getTime()) + ")");
					history.add("Deleted " + old.getExperimentName() +
							" (from " + SystemAnalysis.getCurrentTime(old.getStorageTime().getTime()) + ")");
				}
		}
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("<h1>Removed non-current experiments with an available newer version:</h1>"
				+ StringManipulationTools.getStringList(history, "<br>"));
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
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Delete Experiment-History";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getTashDeleteAll2();
	}
}