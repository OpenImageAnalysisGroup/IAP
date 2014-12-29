package de.ipk.ag_ba.commands.experiment.charting;

import iap.blocks.extraction.Trait;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.data_transformation.ColumnDescription;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class ActionFxCreateDataChart extends AbstractNavigationAction implements ActionDataProcessing {
	
	ExperimentReferenceInterface experiment;
	ExperimentReferenceWithFilterSupport experimentWithSingleSubstance = null;
	
	private final ArrayList<String> res = new ArrayList<String>();
	
	private LinkedHashSet<String> substanceGroupNames;
	
	final ChartSettings settingsGlobal;
	ChartSettings settingsLocal;
	
	private NavigationButton src;
	
	final String groupFilter;
	
	private String traitDescription;
	
	private boolean groupsDetermined;
	
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
		this.experimentWithSingleSubstance = ExperimentReferenceWithFilterSupport.tryGetFilteredDataset(experiment, groupFilter, getStatusProvider());
		if (experimentWithSingleSubstance != null && experimentWithSingleSubstance.getData().size() != 1)
			throw new RuntimeException("Exactly one substance is allowed here!");
		
		this.settingsGlobal = settingsGlobal;
		
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
		
		determineSubstanceGroups();
		
		if (traitDescription != null && !traitDescription.isEmpty())
			res.add(traitDescription);
		
		if (groupFilter != null && substanceGroupNames.isEmpty())
			this.settingsLocal = new ChartSettings(true);
		
		if (experimentWithSingleSubstance != null && experimentWithSingleSubstance.getDataTable() != null) {
			
			// data_table.debugPrintColumnNames();
			StringBuilder sb = new StringBuilder("<h3>Dimensions  go into Navigation Tree</h3>");
			int n = 0;
			for (ColumnDescription cd : experimentWithSingleSubstance.getDataTable().getColumns()) {
				sb.append(cd.getNiceName() + " ");
				n++;
				if (n % 5 == 0)
					sb.append("<br>");
			}
			res.add(sb.toString());
			
			res.add("<h3>*Filter*</h3>Filter Global Outliers Yes / No");
			res.add("<h3>*Group by*</h3>Checkboxes: condition.genotype + condition.treatment");
			res.add("<h3>*Summarize*</h3>numbered checklist: measurement.position &gt; then: plant ID");
			res.add("<h3>*Time*</h3>All Days, Day 1, Day 2, Day 3, ...");
			res.add("<h3>*Plot*</h3>Mean/Min/Max/Median/Sum / t-Test p-value (2 groups) / ANOVA p-Value / ...");
			res.add("<h3>*Error Bars*</h3>No/Std dev/Std err/Variance");
		}
	}
	
	@Override
	public String getDefaultTooltip() {
		try {
			if (experiment != null)
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
			ActionFilterGroupsCommand filterGroupAction = new ActionFilterGroupsCommand("Filter Groups", experimentWithSingleSubstance, groupFilter);
			ActionChartingGroupBySettings groupByAction = new ActionChartingGroupBySettings(this, "Group by", filterGroupAction, experimentWithSingleSubstance,
					groupFilter);
			filterGroupAction.setGroupByAction(groupByAction);
			ActionTimeRangeCommand filterTimeRangeAction = new ActionTimeRangeCommand(this, "Time Range", filterGroupAction, groupFilter);
			ActionCreatePlotCommand createPlotAction = new ActionCreatePlotCommand(this, "Create/update plot");
			
			// STEP 1:
			ra.add(new NavigationButton(new ActionFilterOutliersCommand(experimentWithSingleSubstance, this, "Filter Outliers", filterGroupAction,
					filterTimeRangeAction,
					createPlotAction), src
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
			ra.add(new NavigationButton(new ActionSummarizeGroupsCommand("Summarize data", this), src.getGUIsetting()));
			ra.add(new NavigationButton(createPlotAction, src.getGUIsetting()));
			addExportCommand(ra);
		}
		// res.add("<h3>*Filter*</h3>Filter Global Outliers Yes / No");
		// res.add("<h3>*Group by*</h3>Checkboxes: condition.genotype + condition.treatment");
		// res.add("<h3>*Summarize*</h3>numbered checklist: measurement.position &gt; then: plant ID");
		// res.add("<h3>*Time*</h3>All Days, Day 1, Day 2, Day 3, ...");
		// res.add("<h3>*Plot*</h3>Mean/Min/Max/Median/Sum / t-Test p-value (2 groups) / ANOVA p-Value / ...");
		// res.add("<h3>*Error Bars*</h3>No/Std dev/Std err/Variance");
		
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
	
	private void addExportCommand(ArrayList<NavigationButton> ra) {
		ra.add(new NavigationButton(new AbstractNavigationAction("Export plots") {
			
			private NavigationButton src2;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				src2 = src;
				
				// traverse substances, consider only target substance
				// traverse conditions
				// traverse samples, check time
				// traverse measurements, check group and global and local outlier info
				// result:
				// filtered NMI list, create Mapping Path with clone=true
				// create experiment
				// show data.
			}
			
			@Override
			public String getDefaultTitle() {
				return "Export";
			}
			
			@Override
			public String getDefaultImage() {
				return "img/ext/gpl2/Gnome-Document-Save-64.png";
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
	}
	
}
