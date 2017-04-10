package de.ipk.ag_ba.commands.control;

import java.util.ArrayList;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.Unicode;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

public class ActionEditConditionEditor extends AbstractNavigationAction {
	
	private ExperimentReferenceInterface experimentReference;
	private ConditionInterface ci;
	private NavigationButton src;
	private NavigationAction parentAction;
	
	public ActionEditConditionEditor(String tooltip) {
		super(tooltip);
	}
	
	public ActionEditConditionEditor(ExperimentReferenceInterface experimentReference, ConditionInterface ci, NavigationAction parentAction) {
		super("Create and add a new experiment condition");
		this.experimentReference = experimentReference;
		this.ci = ci;
		this.parentAction = parentAction;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		Object[] res = MyInputHelper.getInput("Set condition meta-data fields:", "Add condition", new Object[] {
				"Species", ci.getSpecies(),
				"Genotype", ci.getGenotype(),
				"Variety", ci.getVariety(),
				"Growth conditions", ci.getGrowthconditions(),
				"Treatment", ci.getTreatment()
		});
		if (res != null) {
			int idx = 0;
			ci.setSpecies(((String) res[idx]).length() == 0 ? null : (String) res[idx]);
			idx++;
			ci.setGenotype(((String) res[idx]).length() == 0 ? null : (String) res[idx]);
			idx++;
			ci.setVariety(((String) res[idx]).length() == 0 ? null : (String) res[idx]);
			idx++;
			ci.setGrowthconditions(((String) res[idx]).length() == 0 ? null : (String) res[idx]);
			idx++;
			ci.setTreatment(((String) res[idx]).length() == 0 ? null : (String) res[idx]);
			idx++;
			if (experimentReference.getExperiment().size() == 0) {
				SubstanceInterface s = new Substance3D();
				s.setName("vis.top");
				experimentReference.getExperiment().add(s);
			}
			for (SubstanceInterface si : experimentReference.getExperiment())
				si.add(ci.clone(si));
		}
		
		parentAction.performActionCalculateResults(src);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return parentAction.getResultNewActionSet();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		currentSet.remove(currentSet.size() - 1);
		return parentAction.getResultNewNavigationSet(currentSet);
	}
	
	@Override
	public String getDefaultTitle() {
		return "<html>" + Unicode.PEN + " Create new condition<br><small>&nbsp;&nbsp;&nbsp;&nbsp;select condition <xsmall>" + Unicode.ARROW_RIGHT_SMALL;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-List-Add-64.png";
	}
	
}
