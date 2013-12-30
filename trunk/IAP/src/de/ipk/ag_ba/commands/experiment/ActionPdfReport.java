package de.ipk.ag_ba.commands.experiment;

import java.util.ArrayList;
import java.util.TreeSet;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.ActionNumericDataReportSetupMainPropertiesStep1;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.settings.ActionToggle;
import de.ipk.ag_ba.gui.ImageAnalysisCommandManager;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition.ConditionInfo;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class ActionPdfReport extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReference experimentReference;
	private NavigationButton src;
	TreeSet<String> cs = new TreeSet<String>();
	TreeSet<String> ss = new TreeSet<String>();
	TreeSet<String> gs = new TreeSet<String>();
	TreeSet<String> vs = new TreeSet<String>();
	TreeSet<String> gc = new TreeSet<String>();
	TreeSet<String> ts = new TreeSet<String>();
	
	public ActionPdfReport(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		
		ExperimentInterface e = experimentReference.getData(false, status);
		for (SubstanceInterface si : e) {
			for (ConditionInterface ci : si) {
				String condition = ci.getConditionName();
				String species = ci.getSpecies();
				String genotype = ci.getGenotype();
				String variety = ci.getVariety();
				String growthCon = ci.getGrowthconditions();
				String treatment = ci.getTreatment();
				
				if (condition != null)
					cs.add(condition);
				if (species != null)
					ss.add(species);
				if (genotype != null)
					gs.add(genotype);
				if (variety != null)
					vs.add(variety);
				if (growthCon != null)
					gc.add(growthCon);
				if (treatment != null)
					ts.add(treatment);
			}
		}
		
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> htmlTextPanels = new ArrayList<String>();
		// htmlTextPanels.add(getList("Conditions", cs));
		htmlTextPanels.add(ImageAnalysisCommandManager.getList("Species", ss));
		htmlTextPanels.add(ImageAnalysisCommandManager.getList("Genotypes", gs));
		htmlTextPanels.add(ImageAnalysisCommandManager.getList("Varieties", vs));
		htmlTextPanels.add(ImageAnalysisCommandManager.getList("Growth Conditions", gc));
		htmlTextPanels.add(ImageAnalysisCommandManager.getList("Treatments", ts));
		return new MainPanelComponent(htmlTextPanels);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public String getDefaultTitle() {
		return "PDF Report";
	}
	
	@Override
	public String getDefaultTooltip() {
		return super.getDefaultTooltip();
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Emblem-Documents-64.png";
		// return "img/ext/gpl2/Gnome-X-Office-Spreadsheet-64.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> actions = new ArrayList<NavigationButton>();
		
		// watering table
		// actions.add(new NavigationButton(new ActionNumericDataReport(m, experimentReference), guiSetting));
		
		ArrayList<ThreadSafeOptions> toggles = new ArrayList<ThreadSafeOptions>();
		
		actions.add(
				new NavigationButton(
						new ActionNumericDataReportSetupMainPropertiesStep1(
								experimentReference, false, false,
								toggles),
						guiSetting));
		
		ThreadSafeOptions tsoCreateAppendix = new ThreadSafeOptions();
		tsoCreateAppendix.setParam(0, "Appendix");
		tsoCreateAppendix.setBval(0, false);
		toggles.add(tsoCreateAppendix);
		actions.add(new NavigationButton(new ActionToggle("Plot and include all calculated properties?",
				"Create Appendix?", tsoCreateAppendix),
				src.getGUIsetting()));
		
		ThreadSafeOptions tsoCalculateRatio = new ThreadSafeOptions();
		tsoCalculateRatio.setParam(0, "Ratio");
		tsoCalculateRatio.setBval(0, false);
		toggles.add(tsoCalculateRatio);
		actions.add(new NavigationButton(new ActionToggle("If selected, the condition with highest value sum will be a reference",
				"Calculate Stress-Reaction?", tsoCalculateRatio),
				src.getGUIsetting()));
		
		ThreadSafeOptions tsoCalculateClustering = new ThreadSafeOptions();
		tsoCalculateClustering.setParam(0, "Clustering");
		tsoCalculateClustering.setBval(0, false);
		toggles.add(tsoCalculateClustering);
		actions.add(new NavigationButton(new ActionToggle("If selected, the data set will be clustered according to their similarity",
				"Calculate Clustering?", tsoCalculateClustering),
				src.getGUIsetting()));
		
		for (String c : new String[] {
				"Condition", ConditionInfo.SPECIES.toString(), ConditionInfo.GENOTYPE.toString(),
				ConditionInfo.VARIETY.toString(),
				ConditionInfo.GROWTHCONDITIONS.toString(),
				ConditionInfo.TREATMENT.toString(), "Plant ID" }) {
			if (c.equals(ConditionInfo.SPECIES.toString()) && ss.size() <= 1)
				continue;
			if (c.equals(ConditionInfo.GENOTYPE.toString()) && gs.size() <= 1)
				continue;
			if (c.equals(ConditionInfo.VARIETY.toString()) && vs.size() <= 1)
				continue;
			if (c.equals(ConditionInfo.GROWTHCONDITIONS.toString()) && gc.size() <= 1)
				continue;
			if (c.equals(ConditionInfo.TREATMENT.toString()) && ts.size() <= 1)
				continue;
			ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setParam(0, c);
			toggles.add(tso);
			tso.setBval(0, false);
			actions.add(new NavigationButton(
					new ActionToggle("Group by " + c + "?", "Group by " + c, tso), src.getGUIsetting()));
		}
		return actions;
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.experimentReference = experimentReference;
	}
}