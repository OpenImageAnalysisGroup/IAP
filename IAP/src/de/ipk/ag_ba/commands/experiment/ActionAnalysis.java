package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.IniIoProvider;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.StringEscapeUtils;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionSettings;
import de.ipk.ag_ba.commands.experiment.process.ActionAssignAnalysisTemplate;
import de.ipk.ag_ba.commands.experiment.process.ActionPerformAnalysisLocally;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ActionAnalysis extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experimentReference;
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
				new ActionAssignAnalysisTemplate(m, experimentReference), guiSetting));
		
		// for (PipelineDesc pd : PipelineDesc.getSavedPipelineTemplates())
		// actions.add(new NavigationButton(
		// new ActionSettings(pd.getIniFileName(),
		// "Change settings of " + pd.getName() + " analysis pipeline",
		// "<html><center>Modify settings of " + pd.getName() + ""),
		// src.getGUIsetting()));
		
		IniIoProvider ioStringProvider = new IniIoProvider() {
			private SystemOptions i;
			
			@Override
			public String getString() {
				String ini = experimentReference.getHeader().getSettings();
				StringEscapeUtils.unescapeXml(ini);
				return ini;
			}
			
			@Override
			public void setString(String value) {
				String ini = StringEscapeUtils.escapeXml(value);
				experimentReference.getHeader().setSettings(ini);
				
				if (m != null)
					try {
						m.setExperimentInfo(experimentReference.getHeader());
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">Saved changed settings for "
								+ experimentReference.getExperimentName()
								+ " in storage location "
								+ m.getDatabaseName() + ".");
					} catch (Exception e) {
						e.printStackTrace();
						MainFrame.showMessageDialog("Could not save changed settings: " + e.getMessage(), "Error");
					}
			}
			
			@Override
			public void setInstance(SystemOptions i) {
				this.i = i;
			}
			
			@Override
			public SystemOptions getInstance() {
				return i;
			}
		};
		
		if (experimentReference.getHeader().getSettings() != null &&
				!experimentReference.getHeader().getSettings().isEmpty()) {
			actions.add(new NavigationButton(
					new ActionSettings(null, ioStringProvider,
							"Change analysis settings", "<html><center>Modify settings"),
					src.getGUIsetting()));
			
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