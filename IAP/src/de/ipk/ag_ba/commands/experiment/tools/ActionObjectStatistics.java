package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentStatistics;

/**
 * @author klukas
 */
public class ActionObjectStatistics extends AbstractNavigationAction implements ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	private String summaryHTML = "";
	
	public ActionObjectStatistics() {
		super("Show experiment data object statistics");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		getStatusProvider().setCurrentStatusText1("Get Data...");
		try {
			ExperimentInterface res = experiment.getData(false, getStatusProvider());
			getStatusProvider().setCurrentStatusText1("Analyze Data...");
			ExperimentStatistics stat = ((Experiment) res).getExperimentStatistics();
			summaryHTML = stat.getSummaryHTML(false, getStatusProvider());
			getStatusProvider().setCurrentStatusText1("Processing finished");
		} catch (Exception e) {
			getStatusProvider().setCurrentStatusText1("Processing error:");
			getStatusProvider().setCurrentStatusText2(e.getMessage());
			summaryHTML = "Could not process data. Error: " + e.getMessage();
			e.printStackTrace();
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
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(summaryHTML);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Logviewer-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Object Statistics";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.m = experimentReference.m;
		this.experiment = experimentReference;
	}
}