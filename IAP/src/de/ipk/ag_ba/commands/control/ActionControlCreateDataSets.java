package de.ipk.ag_ba.commands.control;

import java.util.ArrayList;
import java.util.TreeSet;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.scripts.ActionThreadSafeOptionsBooleanEditor;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class ActionControlCreateDataSets extends AbstractNavigationAction implements ActionDataProcessing {
	
	private NavigationButton src;
	private ExperimentReferenceInterface experimentReference;
	private ArrayList<ThreadSafeOptions> conditionEditorList;
	private CaptureLiveView cl;
	private String bcSection = "Capture";
	private String bcSettingAuto = "Auto-detect barcode";
	
	public ActionControlCreateDataSets(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		cl = new CaptureLiveView(bcSection, bcSettingAuto);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		TreeSet<ConditionInterface> condL = new TreeSet<ConditionInterface>();
		for (SubstanceInterface si : experimentReference.getExperiment()) {
			for (ConditionInterface ci : si) {
				condL.add(ci);
			}
		}
		
		this.conditionEditorList = new ArrayList<ThreadSafeOptions>();
		boolean first = true;
		
		{
			ConditionInterface copySrc = null;
			for (SubstanceInterface si : experimentReference.getExperiment())
				for (ConditionInterface ci : si)
					copySrc = ci;
				
			ConditionInterface ci = new Condition(null);
			ci.setSpecies("");
			ci.setGenotype("");
			ci.setVariety("");
			ci.setGrowthconditions("");
			ci.setTreatment("");
			if (copySrc != null) {
				if (copySrc.getSpecies() != null)
					ci.setSpecies(copySrc.getSpecies());
				if (copySrc.getGenotype() != null)
					ci.setGenotype(copySrc.getGenotype());
				if (copySrc.getVariety() != null)
					ci.setVariety(copySrc.getVariety());
				if (copySrc.getGrowthconditions() != null)
					ci.setGrowthconditions(copySrc.getGrowthconditions());
				if (copySrc.getTreatment() != null)
					ci.setTreatment(copySrc.getTreatment());
			}
			
			NavigationButton tb = new NavigationButton(new ActionEditConditionEditor(experimentReference, ci, this), src.getGUIsetting());
			res.add(tb);
		}
		
		ArrayList<ActionThreadSafeOptionsBooleanEditor> dependentEditors = new ArrayList<>();
		for (ConditionInterface ci : condL) {
			ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setParam(10, ci.getHTMLdescription(true, true));
			tso.setParam(11, ci);
			tso.setParam(1, ci);
			tso.setBval(0, first);
			tso.setInt(1);
			first = false;
			ActionThreadSafeOptionsBooleanEditor ac = new ActionThreadSafeOptionsBooleanEditor(tso);
			ac.allowDisable = false;
			dependentEditors.add(ac);
			NavigationButton tb = new NavigationButton(ac, src.getGUIsetting());
			res.add(tb);
			conditionEditorList.add(tso);
		}
		
		for (ActionThreadSafeOptionsBooleanEditor ed : dependentEditors)
			ed.setDependentTso(conditionEditorList);
		
		SettingStringEditorAction barcodeAction = new SettingStringEditorAction(
				bcSection, bcSettingAuto, "<html><center>Current sample ID<br><b><font size='+1'>", cl);
		res.add(new NavigationButton(barcodeAction, src.getGUIsetting()));
		SelectTimeScaleAction timeAction = new SelectTimeScaleAction(experimentReference);
		res.add(new NavigationButton(timeAction, src.getGUIsetting()));
		AddCapturedImagesToExperiment capture = new AddCapturedImagesToExperiment(conditionEditorList, cl, experimentReference, timeAction, barcodeAction);
		res.add(new NavigationButton(capture, src.getGUIsetting()));
		res.add(new NavigationButton(new AutoCaptureAction(capture), src.getGUIsetting()));
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(cl);
	}
	
	@Override
	public String getDefaultTitle() {
		return "Capture images";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Camera-Video-64.png";
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experimentReference = experimentReference;
	}
	
}
