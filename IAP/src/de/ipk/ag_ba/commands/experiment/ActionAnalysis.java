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
import de.ipk.ag_ba.commands.experiment.process.ActionPerformGridAnalysis;
import de.ipk.ag_ba.commands.experiment.process.ActionSelectAnalysisTemplate;
import de.ipk.ag_ba.commands.experiment.process.ExperimentAnalysisSettingsIOprovder;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.webstart.IAPmain;

public class ActionAnalysis extends AbstractNavigationAction implements ActionDataProcessing {
	ExperimentReference experimentReference;
	private NavigationButton src;
	
	public ActionAnalysis(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		
		actions.add(new NavigationButton(
				new ActionSelectAnalysisTemplate(experimentReference), guiSetting));
		
		if (experimentReference.getIniIoProvider() == null)
			experimentReference.setIniIoProvider(
					new ExperimentAnalysisSettingsIOprovder(experimentReference.getHeader(), experimentReference.m));
		
		IniIoProvider ioStringProvider = experimentReference.getIniIoProvider();
		
		if (experimentReference.getHeader().getSettings() != null &&
				!experimentReference.getHeader().getSettings().isEmpty()) {
			ActionSettings ac = new ActionSettings(null, ioStringProvider,
					"Change analysis settings", "Export/Modify settings");
			SystemOptions so = SystemOptions.getInstance(null, ioStringProvider);
			String pipeLineName = so.getString("DESCRIPTION", "pipeline_name", null);
			String ttt = SystemAnalysis.getCurrentTime();
			NavigationAction acExport = new ActionExportAssignedAnalysisTemplate(
					ioStringProvider,
					StringManipulationTools.getFileSystemName(pipeLineName + "." + SystemAnalysis.getCurrentTime()) + ".pipeline.ini",
					"Export the analysis pipline settings in the new template file " + StringManipulationTools.getFileSystemName(pipeLineName + "." + ttt)
							+ ".pipeline.ini",
					"Export", ttt);
			// actions.add(new NavigationButton(acExport, src.getGUIsetting()));
			ac.addAdditionalEntity(new NavigationButton(acExport, src.getGUIsetting()));
			actions.add(new NavigationButton(ac, src.getGUIsetting()));
			
			actions.add(new NavigationButton(
					new ActionPerformAnalysisLocally(ioStringProvider, experimentReference, experimentReference.m),
					src.getGUIsetting()));
			
			boolean enableRemoteTaskExecution = IAPmain.isSettingEnabled(IAPfeature.REMOTE_EXECUTION);
			if (experimentReference.m != null)
				if (enableRemoteTaskExecution)
					actions.add(new NavigationButton(
							new ActionPerformGridAnalysis(
									new PipelineDesc(null, ioStringProvider, null, null),
									experimentReference.m, experimentReference),
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
	
	@Override
	public boolean isImageAnalysisCommand() {
		return true;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experimentReference = experimentReference;
	}
}