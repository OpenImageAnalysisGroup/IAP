package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionSetup;
import de.ipk.ag_ba.commands.analysis.ActionPhytochamberAnalysis;
import de.ipk.ag_ba.commands.experiment.process.ActionAssignAnalysisTemplate;
import de.ipk.ag_ba.gui.ImageAnalysis;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionAnalysisCommandList extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experimentReference;
	private NavigationButton src;
	
	public ActionAnalysisCommandList(String tooltip, MongoDB m, ExperimentReference experimentReference) {
		super(tooltip);
		this.m = m;
		this.experimentReference = experimentReference;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		
		actions.add(new NavigationButton(
				new ActionAssignAnalysisTemplate(m, experimentReference), guiSetting));
		
		for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
			actions.add(new NavigationButton(
					new ActionSetup(pd.getIniFileName(),
							"Change settings of " + pd.getName() + " analysis pipeline",
							"<html><center>Modify " + pd.getName() + ""),
					src.getGUIsetting()));
		
		actions.add(new NavigationButton(
				new ActionPhytochamberAnalysis(m, experimentReference), guiSetting));
		
		actions.add(ImageAnalysis.getPhytochamberEntityBlueRubber(m, experimentReference, 10, 15, guiSetting));
		
		actions.add(ImageAnalysis.getMaizeEntity(m, experimentReference, 10, 15, guiSetting));
		for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
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