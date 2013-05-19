package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import org.ErrorMsg;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;

public class ActionSelectAnalysisTemplate extends AbstractNavigationAction {
	
	private ExperimentReference experimentReference;
	private NavigationButton src;
	
	public ActionSelectAnalysisTemplate(String tooltip) {
		super(tooltip);
	}
	
	public ActionSelectAnalysisTemplate(ExperimentReference experimentReference) {
		super("Assign customizable analysis pipeline to this experiment");
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
		try {
			for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
				actions.add(new NavigationButton(
						new ActionAssignAnalysisTemplate(experimentReference, pd.getIniFileName(),
								"Assign " + pd.getName() + " analysis pipeline to experiment",
								"<html><center>Use " + pd.getName() + ""),
						src.getGUIsetting()));
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		return actions;
	}
}
