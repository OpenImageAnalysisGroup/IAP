package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MergeCompareRequirements;
import org.SystemAnalysis;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.mongodb.ActionCopyToMongo;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionRemerge extends AbstractNavigationAction implements ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	private String operationResult = "";
	
	public ActionRemerge() {
		super("Re-merge based on data annotation");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		operationResult = "";
		try {
			ExperimentInterface e = experiment.getData();
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
			int subC = e.size();
			int conC = 0;
			int samC = 0;
			int imgC = Substance3D.countMeasurementValues(e, new MeasurementNodeType[] { MeasurementNodeType.IMAGE });
			int numC = e.getNumberOfMeasurementValues();
			for (SubstanceInterface s : new ArrayList<SubstanceInterface>(e)) {
				conC += s.size();
				for (ConditionInterface ci : s) {
					samC += ci.size();
				}
			}
			for (SubstanceInterface s : new ArrayList<SubstanceInterface>(e)) {
				idx++;
				if (optStatus != null)
					optStatus.setCurrentStatusText2("Process substance " + +idx + "/" + max);
				if (optStatus != null)
					optStatus.setCurrentStatusValueFine(100d / max * idx);
				Substance3D.addAndMergeA(e, s, false, BackgroundThreadDispatcher.getRE(), new MergeCompareRequirements());
			}
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Created unified experiment");
			if (optStatus != null)
				optStatus.setCurrentStatusText1("Sort substances and conditions");
			((Experiment) e).sortSubstances();
			((Experiment) e).sortConditions();
			int NMsubC = e.size();
			int NMconC = 0;
			int NMsamC = 0;
			int NMimgC = Substance3D.countMeasurementValues(e, new MeasurementNodeType[] { MeasurementNodeType.IMAGE });
			int NMnumC = e.getNumberOfMeasurementValues();
			for (SubstanceInterface s : new ArrayList<SubstanceInterface>(e)) {
				NMconC += s.size();
				for (ConditionInterface ci : s) {
					NMsamC += ci.size();
				}
			}
			operationResult = "Results of merge operation:<br>" +
					"Substances: " + subC + " ==> " + NMsubC + ", Conditions " + conC + " ==> " + NMconC + ", Samples " + samC + " ==> " + NMsamC + ", Images "
					+ imgC + " ==> " + NMimgC + ", Measurement Values " + numC + " ==> " + NMnumC;
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
		try {
			return new MainPanelComponent("Data has been re-assembled based on annotation: " + operationResult + "<br>" +
					"(click 'Save Changes' to make the new structure permanent)<br><br><br>" + ((Experiment) experiment.getData(status)).toHTMLstring());
		} catch (Exception e) {
			return new MainPanelComponent("Data has been re-assembled based on annotation!" + operationResult + "<br>" +
					"(click 'Save Changes' to make the new structure permanent)<br><br><br>Experiment info could not be displayed! Error: " + e.getMessage());
		}
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