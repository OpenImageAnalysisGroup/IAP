package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionSelectAnalysisTemplate extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	public ActionSelectAnalysisTemplate(String tooltip) {
		super(tooltip);
	}
	
	public ActionSelectAnalysisTemplate(MongoDB m, ExperimentReference experimentReference) {
		super("Assign customizable analysis pipeline to this experiment");
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Select Analysis Template";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/book_object.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
			actions.add(new NavigationButton(
					new ActionAssignAnalysisTemplate(m, experimentReference, pd.getIniFileName(),
							"Assign " + pd.getName() + " analysis pipeline to experiment",
							"<html><center>Use " + pd.getName() + ""),
					src.getGUIsetting()));
		return actions;
	}
}
