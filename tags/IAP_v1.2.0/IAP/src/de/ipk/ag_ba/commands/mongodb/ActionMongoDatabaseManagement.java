package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import org.SystemOptions;

import com.mongodb.BasicDBObject;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.database_tools.ActionDeleteAnalysisJobs;
import de.ipk.ag_ba.commands.database_tools.ActionDeleteHistoryOfAllExperiments;
import de.ipk.ag_ba.commands.database_tools.ActionMergeAnalysisResults;
import de.ipk.ag_ba.commands.experiment.ActionCopyExperiment;
import de.ipk.ag_ba.commands.mongodb.file_storage.ActionMongoFileStorageCommands;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;

public class ActionMongoDatabaseManagement extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentHeaderInterface> experimentList;
	
	public ActionMongoDatabaseManagement(String tooltip) {
		super(tooltip);
	}
	
	public ActionMongoDatabaseManagement(String tooltip, MongoDB m, ArrayList<ExperimentHeaderInterface> experimentList) {
		super(tooltip);
		this.m = m;
		this.experimentList = experimentList;
	}
	
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		
	}
	
	@Override
	public String getDefaultTitle() {
		return "Database Tools";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getToolbox();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		// "Database Management",
		ArrayList<NavigationButton> result = new ArrayList<NavigationButton>();
		result.add(new NavigationButton(new ActionMongoDatabaseServerStatus(
				"Show server status information", m, "serverStatus", "Server Status"), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionMongoDatabaseServerStatus(
				"Show database statistics", m, new BasicDBObject("dbstats", 1), "Database Statistics"), src.getGUIsetting()));
		
		ArrayList<VirtualFileSystem> fsl = m.getVirtualFileSystemForFileStorage();
		if (fsl != null && !fsl.isEmpty()) {
			result.add(new NavigationButton(new ActionMongoFileStorageCommands(m, fsl), src.getGUIsetting()));
		}
		
		boolean showDeleteCloudJobsIcon = SystemOptions.getInstance().getBoolean("IAP", "Show Delete Cloud Jobs Icon", true);
		if (showDeleteCloudJobsIcon) {
			NavigationButton deleteCloudJobs = new NavigationButton(
					new ActionDeleteAnalysisJobs(m, true), guiSetting);
			result.add(deleteCloudJobs);
			// NavigationButton deleteTempDatasets = new NavigationButton(
			// new ActionDeleteSelectedAnalysisSplitResults(m), guiSetting);
			// result.add(deleteTempDatasets);
		}
		
		result.add(new NavigationButton(new ActionMergeAnalysisResults(m), guiSetting));
		
		result.add(new NavigationButton(new ActionMongoDbReorganize(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionMongoDbCompact(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionMongoDbRepair(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionDeleteHistoryOfAllExperiments(m), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionCopyExperiment(m, ExperimentHeaderService.filterNewest(experimentList), src.getGUIsetting()),
				src.getGUIsetting()));
		result.add(new NavigationButton(new ActionDataExportCSVfileList(m, experimentList), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionCreateImageConfigurationList(m, experimentList), src.getGUIsetting()));
		result.add(new NavigationButton(new ActionApplyAnalysisSettingsAndPerformMassAnalysis(m, experimentList), src.getGUIsetting()));
		
		return result;
	}
}
