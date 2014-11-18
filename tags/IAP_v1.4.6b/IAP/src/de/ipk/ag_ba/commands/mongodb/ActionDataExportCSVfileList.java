package de.ipk.ag_ba.commands.mongodb;

import java.util.ArrayList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderService;

/**
 * @author Christian Klukas
 */
public class ActionDataExportCSVfileList extends AbstractNavigationAction {
	
	private MongoDB m;
	private ArrayList<ExperimentHeaderInterface> experiments;
	private int n;
	
	private final ArrayList<String> errors = new ArrayList<String>();
	
	public ActionDataExportCSVfileList(String tooltip) {
		super(tooltip);
	}
	
	public ActionDataExportCSVfileList(MongoDB m, ArrayList<ExperimentHeaderInterface> experiments) {
		this("<html>" +
				"Export all analysis results as CSV or XLSX files.<br>" +
				"(may take a very long time and requires a lot of RAM!)");
		this.m = m;
		this.experiments = experiments;
		this.n = getAnalysisResultCnt(ExperimentHeaderService.filterNewest(experiments));
	}
	
	private int getAnalysisResultCnt(ArrayList<ExperimentHeaderInterface> filterNewest) {
		int r = 0;
		if (filterNewest != null) {
			for (ExperimentHeaderInterface eh : filterNewest)
				if (eh.getImportusergroup() != null && eh.getImportusergroup().equalsIgnoreCase("ANALYSIS RESULTS"))
					if (!eh.getExperimentName().startsWith("Unit Test"))
						r++;
		}
		
		return r;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		status.setCurrentStatusText1("");
		status.setCurrentStatusText2("");
		errors.clear();
		
		boolean xlsx = false;
		ActionPdfCreation3 action = new ActionPdfCreation3(
				null, null,
				new ThreadSafeOptions(),
				new ThreadSafeOptions(),
				xlsx, null, null,
				null, null, null, true);
		for (ExperimentHeaderInterface eh : ExperimentHeaderService.filterNewest(experiments)) {
			if (eh.getImportusergroup() != null && eh.getImportusergroup().equalsIgnoreCase("ANALYSIS RESULTS")
					&& !eh.getExperimentName().startsWith("Unit Test")) {
				try {
					status.setCurrentStatusText1("Process numeric data");
					status.setCurrentStatusText2(eh.getExperimentName());
					ExperimentReference er = new ExperimentReference(eh, m);
					action.setExperimentReference(er);
					action.setUseIndividualReportNames(true);
					action.setStatusProvider(getStatusProvider());
					action.setSource(this, src.getGUIsetting());
					action.performActionCalculateResults(src);
				} catch (Exception err) {
					errors.add("Could not process " + eh.getExperimentName() + ": " + err.getMessage());
					err.printStackTrace();
				}
			}
			// res.add(new NavigationButton(createMongoUserNavigationAction(user, user2exp.get(user)), src
			// .getGUIsetting()));
		}
		status.setCurrentStatusText1("Opening result folder...");
		status.setCurrentStatusText2("");
		action.postProcessCommandLineExecutionDirectory(true);
		Thread.sleep(5000);
		status.setCurrentStatusText1("");
		status.setCurrentStatusText2("");
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (errors.isEmpty())
			return new MainPanelComponent("Processing complete. No errors.");
		else
			return new MainPanelComponent(errors);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Save analysis results (" + n + ") as CSV/XLSX";
	}
	
	@Override
	public String getDefaultImage() {
		return IAPimages.getDownloadIcon();
	}
	
}
