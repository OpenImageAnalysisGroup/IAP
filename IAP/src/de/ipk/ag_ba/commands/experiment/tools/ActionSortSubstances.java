package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public class ActionSortSubstances extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	
	public ActionSortSubstances(MongoDB m, ExperimentReference experiment) {
		super("Sort substances and conditions by name");
		this.m = m;
		this.experiment = experiment;
	}
	
	public ActionSortSubstances() {
		super("Sort substances and conditions by name");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		try {
			ExperimentInterface res = experiment.getData();
			((Experiment) res).sortSubstances();
			((Experiment) res).sortConditions();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
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
		res.add(new NavigationButton("Save Changes", new ActionCopyToMongo(m, experiment, true), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Substances and conditions have been sorted by name!<br>" +
				"(click 'Save Changes' to make the new order permanent)");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-View-Sort-Descending-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Sort Substances and Conditions by Name";
	}
}