package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportZIP;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.settings.ActionToggle;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public final class ActionNumericExportCommands extends AbstractNavigationAction implements ActionDataProcessing {
	private final ArrayList<ThreadSafeOptions> toggles;
	private ExperimentReference experiment;
	private NavigationButton src;
	
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
		this.src = src;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		ThreadSafeOptions exportIndividualAngles = new ThreadSafeOptions();
		exportIndividualAngles.setBval(0, false);
		
		ThreadSafeOptions exportIndividualReplicates = new ThreadSafeOptions();
		exportIndividualReplicates.setBval(0, true);
		
		ThreadSafeOptions exportImages = new ThreadSafeOptions();
		exportImages.setBval(0, false);
		
		res.add(new NavigationButton(new ActionDataExportZIP(experiment, exportImages), guiSetting));
		// res.add(new NavigationButton(new ActionDataExportAsFilesAction(m, experiment), src.getGUIsetting()));
		
		res.add(new NavigationButton(
				new ActionToggle("Enable/disable export of images",
						"<html>"
								+ "<center>Export of<br>&#8592; JPG images &#8594;", exportImages),
				guiSetting));
		
		res.add(new NavigationButton(
				new ActionToggle("Enable/disable export of data for individual camera view angles",
						"<html>"
								+ "<center>Export data for<br>"
								+ "all camera angles &#8594;", exportIndividualAngles),
				guiSetting));
		res.add(new NavigationButton(
				new ActionToggle("Enable/disable export of data for replicates",
						"<html>"
								+ "<center>Export individual<br>"
								+ "replicate/well data &#8594;", exportIndividualReplicates),
				guiSetting));
		
		res.add(new NavigationButton(
				new ActionPdfCreation3(
						experiment,
						toggles,
						exportIndividualAngles,
						exportIndividualReplicates,
						exportImages,
						true,
						null, null, null, null, null, null, ExportSetting.ALL, true),
				guiSetting));
		
		res.add(new NavigationButton(
				new ActionPdfCreation3(
						experiment,
						toggles,
						exportIndividualAngles,
						exportIndividualReplicates,
						exportImages,
						false,
						null, null, null, null, null, true),
				guiSetting));
		
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