package de.ipk.ag_ba.gui;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionCopyAndAssignAnalysisTemplate extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	
	public ActionCopyAndAssignAnalysisTemplate(String tooltip) {
		super(tooltip);
	}
	
	public ActionCopyAndAssignAnalysisTemplate(MongoDB m, ExperimentReference experimentReference) {
		super("Assign customizable analysis pipeline to this experiment");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
}
