package de.ipk.ag_ba.commands.experiment.charting;

import iap.blocks.extraction.Trait;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionFxCreateDataChart extends AbstractNavigationAction implements ActionDataProcessing {
	
	ExperimentReferenceInterface experiment;
	ExperimentInterface experimentWithSingleSubstance = null;
	
	private final ArrayList<String> res = new ArrayList<String>();
	
	private LinkedHashSet<String> substanceGroupNames;
	
	final ChartSettings settingsGlobal;
	ChartSettings settingsLocal;
	
	private NavigationButton src;
	
	final String groupFilter;
	
	private String traitDescription;
	
	private boolean groupsDetermined;
	
	ActionFilterOutliersCommand removeDefinedOutlierAction;
	ActionFilterGroupsCommand filterGroupAction;
	ActionChartingGroupBySettings groupByAction;
	ActionTimeRangeCommand filterTimeRangeAction;
	ActionCreatePlotCommand createPlotAction;
	ActionSummarizeGroupsCommand summarizeDataAction;
	ActionExportPlotDataTable exportDataTableAction;
	
	private ExperimentTransformationPipeline transformationPipeline;
	
	public ActionFxCreateDataChart() {
		super("Create a Data Chart");
		this.groupFilter = null;
		this.settingsGlobal = new ChartSettings(false);
	}
	
	public ActionFxCreateDataChart(String groupFilter, ExperimentReferenceInterface experiment, ChartSettings settingsGlobal)
			throws Exception {
		super("Create a Data Chart");
		this.groupFilter = groupFilter;
		this.experiment = experiment;
		
		this.settingsGlobal = settingsGlobal;
		
		if (!experiment.getIniIoProvider().isAbleToSaveData()) {
			if (settingsLocal != null)
				settingsLocal.setSavePossible(false);
			settingsGlobal.setSavePossible(false);
		}
		
		try {
			if (experiment != null)
				determineSubstanceGroups();
		} catch (Exception err) {
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		res.clear();
		
		try {
			if (experiment != null)
				determineSubstanceGroups();
		} catch (Exception err) {
			throw new RuntimeException(err);
		}
		
		this.experimentWithSingleSubstance = ActionFilterOutliersCommand.tryGetFilteredDataset(experiment, groupFilter, getStatusProvider());
		if (experimentWithSingleSubstance != null && experimentWithSingleSubstance.size() != 1)
			throw new RuntimeException("Exactly one substance is allowed here!");
		
		if (traitDescription != null && !traitDescription.isEmpty())
			res.add(traitDescription);
		
		if (groupFilter != null && substanceGroupNames.isEmpty()) {
			this.settingsLocal = new ChartSettings(true);
			settingsLocal.setSavePossible(settingsGlobal.isSavePossible());
		}
		
		if (substanceGroupNames.size() == 0 && groupFilter != null) {
			this.transformationPipeline = new ExperimentTransformationPipeline(experimentWithSingleSubstance);
			
			removeDefinedOutlierAction = new ActionFilterOutliersCommand("Filter Outliers", transformationPipeline, settingsLocal, settingsGlobal);
			filterGroupAction = new ActionFilterGroupsCommand("Filter Groups", transformationPipeline);
			groupByAction = new ActionChartingGroupBySettings("Group by", transformationPipeline, settingsLocal, settingsGlobal, filterGroupAction);
			filterTimeRangeAction = new ActionTimeRangeCommand("Time Range", transformationPipeline, settingsLocal, settingsGlobal);
			summarizeDataAction = new ActionSummarizeGroupsCommand("Summarize data", transformationPipeline, settingsLocal, settingsGlobal);
			createPlotAction = new ActionCreatePlotCommand("Create/update plot", transformationPipeline, settingsLocal, settingsGlobal);
			exportDataTableAction = new ActionExportPlotDataTable("Export data table", transformationPipeline, settingsLocal, settingsGlobal);
			
			transformationPipeline.setSteps(removeDefinedOutlierAction, groupByAction,
					filterGroupAction, filterTimeRangeAction, summarizeDataAction, createPlotAction);
		}
		
	}
	
	@Override
	public String getDefaultTooltip() {
		try {
			if (experiment != null && experiment.getExperimentPeek() != null)
				determineSubstanceGroups();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (traitDescription != null && !traitDescription.isEmpty())
			return "<html>" + traitDescription;
		else
			return super.getDefaultTooltip();
	}
	
	private void determineSubstanceGroups() throws Exception {
		if (groupsDetermined)
			return;
		
		if (experiment == null)
			throw new RuntimeException("Experiment can't be null at this point");
		
		groupsDetermined = true;
		
		if (groupFilter != null) {
			// System.out.println("Try to find desc for: " + groupFilter);
			this.traitDescription = IAPpluginManager.getInstance().getDescriptionForCalculatedProperty(groupFilter);
		}
		
		this.substanceGroupNames = new LinkedHashSet<String>();
		for (SubstanceInterface si : experiment.getData()) {
			if (groupFilter != null)
				if (!si.getName().startsWith(groupFilter))
					continue;
			String sn = si.getName();
			if (groupFilter != null) {
				if (sn.length() <= groupFilter.length())
					continue;
				sn = sn.substring(groupFilter.length() + 1);
			}
			if (sn != null && sn.contains(".")) {
				sn = sn.substring(0, sn.indexOf("."));
			}
			substanceGroupNames.add(sn);
		}
		if (substanceGroupNames.size() == 0)
			if (traitDescription != null && !traitDescription.isEmpty()) {
				String nn = new Trait(groupFilter).getNiceName();
				if (nn != null)
					traitDescription = "<h3>" + WordUtils.capitalizeFully(nn, ' ', '(') + "</h3>" + traitDescription;
			}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> ra = new ArrayList<NavigationButton>();
		for (String g : substanceGroupNames) {
			if (groupFilter != null)
				g = groupFilter + "." + g;
			try {
				ra.add(new NavigationButton(new ActionFxCreateDataChart(g, experiment, settingsGlobal), src.getGUIsetting()));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (substanceGroupNames.size() == 0 && groupFilter != null) {
			
			// STEP 1:
			ra.add(new NavigationButton(removeDefinedOutlierAction, src
					.getGUIsetting()));
			// STEP 2:
			ra.add(new NavigationButton(groupByAction, src.getGUIsetting()));
			// STEP 3:
			ra.add(new NavigationButton(filterGroupAction, src.getGUIsetting()));
			// STEP 4:
			ra.add(new NavigationButton(filterTimeRangeAction, src.getGUIsetting()));
			if (false) {
				addSortCommand(ra);
				addSummarizeCommandErrorBars(ra);
			}
			ra.add(new NavigationButton(summarizeDataAction, src.getGUIsetting()));
			ra.add(new NavigationButton(createPlotAction, src.getGUIsetting()));
			ra.add(new NavigationButton(exportDataTableAction, src.getGUIsetting()));
		}
		
		return ra;
	}
	
	private void addSummarizeCommandErrorBars(ArrayList<NavigationButton> ra) {
		ra.add(new NavigationButton(new AbstractNavigationAction("Error bars") {
			
			private NavigationButton src2;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				src2 = src;
			}
			
			@Override
			public String getDefaultTitle() {
				return "<html><center>Error bars<br><font color='gray'><small>standard deviation";
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-Accessories-Calculator-64.png";
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
		}, src.getGUIsetting()));
	}
	
	private void addSortCommand(ArrayList<NavigationButton> ra) {
		ra.add(new NavigationButton(new AbstractNavigationAction("Sort Groups") {
			
			private NavigationButton src2;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				src2 = src;
			}
			
			@Override
			public String getDefaultTitle() {
				return "<html><center>Sort groups<br><font color='gray'><small>no ordering";
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-View-Sort-Descending-64.png";
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
				return currentSet;
			}
			
			@Override
			public ArrayList<NavigationButton> getResultNewActionSet() {
				return null;
			}
		}, src.getGUIsetting()));
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(res);
	}
	
	@Override
	public String getDefaultTitle() {
		if (groupFilter != null) {
			String name = groupFilter;
			if (groupFilter.contains("."))
				name = groupFilter.substring(groupFilter.lastIndexOf(".") + ".".length());
			return name;
		} else
			return "Create Data Chart";
	}
	
	@Override
	public String getDefaultImage() {
		if (groupFilter == null)
			return "img/ext/gpl2/Gnome-X-Office-Presentation-64.png";
		else {
			if (substanceGroupNames != null && substanceGroupNames.size() > 0)
				return "img/ext/gpl2/Gnome-Folder-publicshare.png";
			else
				return "img/ext/gpl2/Gnome-X-office-drawing-template.png";
		}
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		try {
			this.experiment = experimentReference;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.settingsGlobal.setIniIOprovider(experimentReference.getIniIoProvider());
		if (!experiment.getIniIoProvider().isAbleToSaveData()) {
			if (settingsLocal != null)
				settingsLocal.setSavePossible(false);
			settingsGlobal.setSavePossible(false);
		}
	}
	
}
