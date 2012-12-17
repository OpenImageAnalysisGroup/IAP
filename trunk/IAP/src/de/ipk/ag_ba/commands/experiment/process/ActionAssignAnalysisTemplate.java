package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionAssignAnalysisTemplate extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	
	public ActionAssignAnalysisTemplate(String tooltip) {
		super(tooltip);
	}
	
	public ActionAssignAnalysisTemplate(MongoDB m, ExperimentReference experimentReference) {
		super("Assign customizable analysis pipeline to this experiment");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Select Analysis Task";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Insert-Object-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
}
