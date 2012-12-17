package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionNumericDataReportCompleteFinishedStep3;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExport;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportAsFilesAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataExportTar;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.GUIsetting;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.SupplementaryFilePanelMongoDB;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * @author klukas
 */
public class ActionViewExportData extends AbstractNavigationAction {
	private final MongoDB m;
	private final ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	
	public ActionViewExportData(MongoDB m, ExperimentReference experiment) {
		super("Show or export numeric or image data");
		this.m = m;
		this.experiment = experiment;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		this.src = src;
		try {
			SupplementaryFilePanelMongoDB sfp = new SupplementaryFilePanelMongoDB(m, experiment.getData(m),
					experiment.getExperimentName());
			mpc = new MainPanelComponent(sfp);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		// todo add zoom slider (default, large, extra large)
		// todo add plant filter (all, ID 1, ID 2, ID 3, ...)
		if (m != null) {
			res.add(new NavigationButton("Save Annotation Changes", new ActionCopyToMongo(m, experiment, true), src.getGUIsetting()));
			
			ArrayList<ThreadSafeOptions> toggles = new ArrayList<ThreadSafeOptions>();
			
			res.add(new NavigationButton(
					new ActionNumericDataReportCompleteFinishedStep3(
							m,
							experiment,
							toggles,
							false,
							true,
							null, null, null, null, null),
					guiSetting));
			res.add(new NavigationButton(
					new ActionNumericDataReportCompleteFinishedStep3(
							m,
							experiment,
							toggles,
							true,
							false,
							null, null, null, null, null),
					guiSetting));
			
			res.add(new NavigationButton(new ActionDataExport(m, experiment), src.getGUIsetting()));
			res.add(new NavigationButton(new ActionDataExportTar(m, experiment), src.getGUIsetting()));
			res.add(new NavigationButton(new ActionDataExportAsFilesAction(m, experiment), src.getGUIsetting()));
		}
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	public static NavigationButton getFileManagerEntity(MongoDB m,
			final ExperimentReference experimentRef, GUIsetting guiSetting) {
		NavigationAction fileManagerAction = new ActionViewExportData(m, experimentRef);
		NavigationButton fileManager = new NavigationButton(fileManagerAction, "View/Export Data",
				"img/ext/user-desktop.png",
				// "img/ext/applications-system.png",
				guiSetting);
		return fileManager;
	}
	
	public ExperimentReference getExperimentReference() {
		return experiment;
	}
}