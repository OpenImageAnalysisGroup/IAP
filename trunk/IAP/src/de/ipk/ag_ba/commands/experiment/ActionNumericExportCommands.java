package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportZIP;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.settings.ActionToggle;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;

/**
 * @author klukas
 */
public final class ActionNumericExportCommands extends AbstractNavigationAction implements ActionDataProcessing {
	private final ArrayList<ThreadSafeOptions> toggles;
	private ExperimentReferenceInterface experiment;
	private NavigationButton src;
	private final ThreadSafeOptions exportImages;
	private final ThreadSafeOptions tsoQuality;
	
	public ActionNumericExportCommands(
			String tooltip, ArrayList<ThreadSafeOptions> toggles, ThreadSafeOptions exportImages, ThreadSafeOptions tsoQuality) {
		super(tooltip);
		this.toggles = toggles;
		this.exportImages = exportImages;
		this.tsoQuality = tsoQuality;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Export";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Save-64.png";// Gnome-X-Office-Spreadsheet-64.png";// colorhistogram.png";
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
		
		res.add(new NavigationButton(new ActionDataExportZIP(experiment, exportImages, tsoQuality), guiSetting));
		// res.add(new NavigationButton(new ActionDataExportAsFilesAction(m, experiment), src.getGUIsetting()));
		
		res.add(new NavigationButton(new ActionDetermineImageFileOutputSize(experiment, exportImages, tsoQuality), guiSetting));
		
		res.add(new NavigationButton(
				new ActionToggle("Enable/disable export of images",
						"<html>"
								+ "<center>Export of<br>&#8592; JPG images &#8594;", exportImages) {
					
					@Override
					public void performActionCalculateResults(NavigationButton src) throws Exception {
						super.performActionCalculateResults(src);
						tsoQuality.setParam(0, null); // reset size information string
						tsoQuality.setParam(1, null);
					}
				},
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
						tsoQuality,
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
						tsoQuality,
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
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experiment = experimentReference;
	}
}