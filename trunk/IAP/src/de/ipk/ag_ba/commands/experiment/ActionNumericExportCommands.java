package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportTar;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportZIP;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public final class ActionNumericExportCommands extends AbstractNavigationAction implements ActionDataProcessing {
	private final ArrayList<ThreadSafeOptions> toggles;
	private ExperimentReference experiment;
	
	public ActionNumericExportCommands(
			String tooltip, ArrayList<ThreadSafeOptions> toggles) {
		super(tooltip);
		this.toggles = toggles;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/colorhistogram.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		res.add(new NavigationButton(
				new ActionPdfCreation3(
						experiment,
						toggles,
						false,
						true,
						null, null, null, null, null, null, ExportSetting.ALL),
				guiSetting));
	
		res.add(new NavigationButton(
				new ActionPdfCreation3(
						experiment,
						toggles,
						true,
						false,
						null, null, null, null, null),
				guiSetting));
		
		res.add(new NavigationButton(new ActionDataExportZIP(experiment),guiSetting));
		res.add(new NavigationButton(new ActionDataExportTar(experiment), guiSetting));
		// res.add(new NavigationButton(new ActionDataExportAsFilesAction(m, experiment), src.getGUIsetting()));
	
		return res;
	}

	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}

	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experiment = experimentReference;
	}
}