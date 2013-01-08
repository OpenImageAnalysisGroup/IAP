package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.IniIoProvider;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionSettings;
import de.ipk.ag_ba.commands.experiment.process.ActionExportAssignedAnalysisTemplate;
import de.ipk.ag_ba.commands.experiment.process.ActionPerformAnalysisLocally;
import de.ipk.ag_ba.commands.experiment.process.ActionSelectAnalysisTemplate;
import de.ipk.ag_ba.commands.experiment.process.ExperimentAnalysisSettingsIOprovder;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionAnalysis extends AbstractNavigationAction {
	final MongoDB m;
	final ExperimentReference experimentReference;
	private NavigationButton src;
	
	public ActionAnalysis(String tooltip, MongoDB m, ExperimentReference experimentReference) {
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
				new ActionSelectAnalysisTemplate(m, experimentReference), guiSetting));
		
		if (experimentReference.getIniIoProvider() == null)
			experimentReference.setIniIoProvider(
					new ExperimentAnalysisSettingsIOprovder(experimentReference, m));
		IniIoProvider ioStringProvider = experimentReference.getIniIoProvider();
		
		if (experimentReference.getHeader().getSettings() != null &&
				!experimentReference.getHeader().getSettings().isEmpty()) {
			NavigationAction ac = new ActionSettings(null, ioStringProvider,
					"Change analysis settings", "<html><center>Modify settings");
			SystemOptions so = SystemOptions.getInstance(null, ioStringProvider);
			String pipeLineName = so.getString("DESCRIPTION", "pipeline_name", null);
			NavigationAction acExport = new ActionExportAssignedAnalysisTemplate(
					ioStringProvider,
					StringManipulationTools.getFileSystemName(pipeLineName + "." + SystemAnalysis.getCurrentTime()) + ".pipeline.ini",
					"Export the analysis pipline settings as a template file",
					"Export");
			ac.addAdditionalEntity(new NavigationButton(acExport, src.getGUIsetting()));
			actions.add(new NavigationButton(ac, src.getGUIsetting()));
			
			actions.add(new NavigationButton(
					new ActionPerformAnalysisLocally(ioStringProvider, experimentReference),
					src.getGUIsetting()));
		}
		
		// actions.add(new NavigationButton(
		// new ActionPhytochamberAnalysis(m, experimentReference), guiSetting));
		//
		// actions.add(ImageAnalysis.getPhytochamberEntityBlueRubber(m, experimentReference, 10, 15, guiSetting));
		//
		// actions.add(ImageAnalysis.getMaizeEntity(m, experimentReference, 10, 15, guiSetting));
		// for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
		// actions.add(ImageAnalysis.getPipelineEntity(pd, m, experimentReference, 10, 15, guiSetting));
		//
		// actions.add(ImageAnalysis.getRootScannEntity(m, experimentReference, guiSetting));
		// actions.add(ImageAnalysis.getMaize3dEntity(m, experimentReference, 10, 15, guiSetting));
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