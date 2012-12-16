package de.ipk.ag_ba.gui;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.analysis.ActionPhytochamberAnalysis;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionAnalysisCommandList extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experimentReference;
	
	ActionAnalysisCommandList(String tooltip, MongoDB m, ExperimentReference experimentReference) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		
		actions.add(new NavigationButton(
				new ActionCopyAndAssignAnalysisTemplate(m, experimentReference), guiSetting));
		
		actions.add(new NavigationButton(
				new ActionPhytochamberAnalysis(m, experimentReference), guiSetting));
		
		actions.add(ImageAnalysis.getPhytochamberEntityBlueRubber(m, experimentReference, 10, 15, guiSetting));
		
		actions.add(ImageAnalysis.getMaizeEntity(m, experimentReference, 10, 15, guiSetting));
		for (PipelineDesc pd : PipelineDesc.getKnownPipelines())
			actions.add(ImageAnalysis.getPipelineEntity(pd, m, experimentReference, 10, 15, guiSetting));
		
		actions.add(ImageAnalysis.getRootScannEntity(m, experimentReference, guiSetting));
		actions.add(ImageAnalysis.getMaize3dEntity(m, experimentReference, 10, 15, guiSetting));
		return actions;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Analysis";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getApplications();
	}
}