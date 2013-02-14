package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionRemerge extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	
	public ActionRemerge(MongoDB m, ExperimentReference experiment) {
		super("Re-merge based on data annotation");
		this.m = m;
		this.experiment = experiment;
	}
	
	public ActionRemerge() {
		super("Re-merge based on data annotation");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		try {
			ExperimentInterface e = experiment.getData(m);
			BackgroundTaskStatusProviderSupportingExternalCall optStatus = status;
			// if (optStatus != null)
			// optStatus.setCurrentStatusText1("Get mapping path objects");
			// System.out.println(SystemAnalysis.getCurrentTime() + ">GET MAPPING PATH OBJECTS...");
			// ArrayList<MappingData3DPath> mdpl = MappingData3DPath.get(e, false);
			// experiment.setExperimentData(null);
			// e.clear();
			// e = null;
			// if (optStatus != null)
			// optStatus.setCurrentStatusText1("Transform path objects to experiment");
			// System.out.println(SystemAnalysis.getCurrentTime() + ">MERGE " + mdpl.size() + " MAPPING PATH OBJECTS TO EXPERIMENT...");
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Create unified experiment");
			int idx = 0;
			int max = e.size();
			for (SubstanceInterface s : new ArrayList<SubstanceInterface>(e)) {
				idx++;
				if (optStatus != null)
					optStatus.setCurrentStatusText2("Process substance " + +idx + "/" + max);
				if (optStatus != null)
					optStatus.setCurrentStatusValueFine(100d / max * idx);
				Substance3D.addAndMerge(e, s, false);
			}
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Created unified experiment");
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Sort substances and conditions");
			((Experiment) e).sortSubstances();
			((Experiment) e).sortConditions();
			System.out.println(SystemAnalysis.getCurrentTime() + ">UNIFIED EXPERIMENT CREATED");
			experiment.setExperimentData(e);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
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
		res.add(new NavigationButton("Save Changes", new ActionCopyToMongo(m, experiment, true), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Data has been re-assembled based on annotation!<br>" +
				"(click 'Save Changes' to make the new order permanent)");
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Media-Playlist-Shuffle-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html><center>" +
				"Reconsider Annotation<br>" +
				"(split and re-merge)</center>";
	}
}