package de.ipk.ag_ba.commands.experiment.charting;

import iap.blocks.extraction.Trait;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.StringManipulationTools;
import org.apache.commons.lang3.text.WordUtils;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.data_transformation.ColumnDescription;
import de.ipk.ag_ba.data_transformation.DataTable;
import de.ipk.ag_ba.data_transformation.loader.DataTableLoader;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPimages;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.picture_gui.DataChartComponentWindow;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class ActionFxCreateDataChart extends AbstractNavigationAction implements ActionDataProcessing {
	
	ExperimentReferenceWithFilterSupport experiment;
	
	private final ArrayList<String> res = new ArrayList<String>();
	
	DataTable data_table;
	
	private LinkedHashSet<String> groupNames;
	
	final ChartSettings settingsGlobal;
	ChartSettings settingsLocal;
	
	private NavigationButton src;
	
	private final String groupFilter;
	
	private String traitDescription;
	
	private boolean groupsDetermined;
	
	public ActionFxCreateDataChart() {
		super("Create a Data Chart");
		this.groupFilter = null;
		this.settingsGlobal = new ChartSettings(false);
	}
	
	public ActionFxCreateDataChart(String groupFilter, ExperimentReferenceInterface experiment, DataTable data_table, ChartSettings settingsGlobal) {
		super("Create a Data Chart");
		this.groupFilter = groupFilter;
		this.experiment = new ExperimentReferenceWithFilterSupport(experiment);
		this.data_table = data_table;
		this.settingsGlobal = settingsGlobal;
		try {
			determineGroups();
		} catch (Exception err) {
			throw new RuntimeException(err);
		}
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
		if (true) {
			res.clear();
			
			determineGroups();
			
			if (traitDescription != null && !traitDescription.isEmpty())
				res.add(traitDescription);
			
			if (groupFilter != null && groupNames.isEmpty())
				this.settingsLocal = new ChartSettings(true);
			
			if (data_table == null) {
				this.data_table = new DataTableLoader().loadFromExperiment(experiment.getData(false, getStatusProvider()));
				// data_table.debugPrintColumnNames();
				StringBuilder sb = new StringBuilder("<h3>Dimensions  go into Navigation Tree</h3>");
				int n = 0;
				for (ColumnDescription cd : data_table.getColumns()) {
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
			if (true)
				return;
			throw new UnsupportedOperationException("Not yet implemented!");
		}
		res.clear();
		String currentOutliers = experiment.getHeader().getGlobalOutlierInfo();
		
		LinkedHashSet<String> outliers = new LinkedHashSet<>();
		if (currentOutliers != null)
			for (String o : currentOutliers.split("//")) {
				res.add("Found existing global outlier definition for " + o.trim() + ".");
				outliers.add(o.trim());
			}
		
		if (res.size() == 0)
			res.add("Found no existing global outlier definitions.");
		
		int resCnt = 0;
		
		for (SubstanceInterface m : experiment.getExperiment()) {
			Substance3D m3 = (Substance3D) m;
			for (ConditionInterface s : m3) {
				Condition3D s3 = (Condition3D) s;
				for (SampleInterface sd : s3) {
					Sample3D sd3 = (Sample3D) sd;
					outlierSearch: for (NumericMeasurementInterface nmi : sd3) {
						if (nmi instanceof ImageData) {
							ImageData id = (ImageData) nmi;
							if (id.isMarkedAsOutlier()) {
								String pid = id.getQualityAnnotation().trim();
								if (!outliers.contains(pid)) {
									res.add("Added ID " + pid + " to global outlier list.");
									outliers.add(pid);
									resCnt++;
								}
								break outlierSearch;
							}
						}
					}
				}
			}
		}
		
		if (resCnt > 0) {
			experiment.getHeader().setGlobalOutlierInfo(StringManipulationTools.getStringList(outliers, "//"));
			experiment.getExperiment().setHeader(experiment.getHeader());
			res.add("Updated global outlier list with " + resCnt + " additional IDs (changed in memory, needs to be manually saved if desired)).");
		} else
			res.add("Found no image which has been marked as an outlier. Global outlier list remains unchanged.");
	}
	
	@Override
	public String getDefaultTooltip() {
		try {
			determineGroups();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (traitDescription != null && !traitDescription.isEmpty())
			return "<html>" + traitDescription;
		else
			return super.getDefaultTooltip();
	}
	
	private void determineGroups() throws Exception {
		if (groupsDetermined)
			return;
		groupsDetermined = true;
		
		if (groupFilter != null) {
			// System.out.println("Try to find desc for: " + groupFilter);
			this.traitDescription = IAPpluginManager.getInstance().getDescriptionForCalculatedProperty(groupFilter);
		}
		
		this.groupNames = new LinkedHashSet<String>();
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
			groupNames.add(sn);
		}
		if (groupNames.size() == 0)
			if (traitDescription != null && !traitDescription.isEmpty()) {
				String nn = new Trait(groupFilter).getNiceName();
				if (nn != null)
					traitDescription = "<h3>" + WordUtils.capitalizeFully(nn, ' ', '(') + "</h3>" + traitDescription;
			}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> ra = new ArrayList<NavigationButton>();
		
		for (String g : groupNames) {
			if (groupFilter != null)
				g = groupFilter + "." + g;
			ra.add(new NavigationButton(new ActionFxCreateDataChart(g, experiment, data_table, settingsGlobal), src.getGUIsetting()));
		}
		if (groupNames.size() == 0 && groupFilter != null) {
			ActionFilterGroupsCommand filterGroupAction = new ActionFilterGroupsCommand("Filter Groups", data_table, experiment, groupFilter);
			ActionChartingGroupBySettings groupByAction = new ActionChartingGroupBySettings(this, "Group by", filterGroupAction, experiment, groupFilter);
			ActionTimeRangeCommand filterTimeRangeAction = new ActionTimeRangeCommand(this, "Time Range", filterGroupAction, groupFilter);
			filterGroupAction.setGroupByAction(groupByAction);
			
			// STEP 1:
			ra.add(new NavigationButton(new ActionFilterOutliersCommand(this, "Filter Outliers", filterGroupAction, filterTimeRangeAction), src.getGUIsetting()));
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
			addCreatePlotCommand(ra);
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
	
	private void addCreatePlotCommand(ArrayList<NavigationButton> ra) {
		ra.add(new NavigationButton(new AbstractNavigationAction("Create/update plot") {
			
			private NavigationButton src2;
			private JComponent chartGUI;
			
			@Override
			public void performActionCalculateResults(NavigationButton src) throws Exception {
				src2 = src;
				SubstanceInterface sub = null;
				for (SubstanceInterface ssi : experiment.getData()) {
					if (groupFilter.equals(ssi.getName())) {
						sub = ssi;
						break;
					}
				}
				
				if (sub == null) {
					chartGUI = new JLabel("Could not find substance '" + groupFilter + "'!");
					return;
				}
				
				final ExperimentInterface expf;
				// if (mde instanceof Condition3D) {
				// ExperimentInterface exp = Experiment.copyAndExtractSubtanceInclusiveData(sub, (ConditionInterface) mde);
				// Collection<NumericMeasurementInterface> md = Substance3D.getAllMeasurements(exp);
				// ArrayList<MappingData3DPath> mmd = new ArrayList<MappingData3DPath>();
				// for (NumericMeasurementInterface nmi : md) {
				// MappingData3DPath mp = new MappingData3DPath(nmi, true);
				// mp.getConditionData().setVariety(mp.getConditionData().getVariety() != null && !mp.getConditionData().getVariety().isEmpty() ?
				// mp.getConditionData().getVariety() + "/" + mp.getMeasurement().getQualityAnnotation()
				// : mp.getMeasurement().getQualityAnnotation());
				//
				// mmd.add(mp);
				// }
				// expf = MappingData3DPath.merge(mmd, true, status);
				//
				// } else {
				status.setCurrentStatusText1("Extract subset " + sub.getName());
				ExperimentInterface exp = Experiment.copyAndExtractSubtanceInclusiveData(sub);
				if (status.wantsToStop())
					return;
				Collection<NumericMeasurementInterface> md = Substance3D.getAllMeasurements(exp);
				status.setCurrentStatusText1("Create dataset for plotting");
				expf = MappingData3DPath.merge(md, true, status);
				// }
				if (status.wantsToStop())
					return;
				HashSet<String> speciesNames = new HashSet<String>();
				for (SubstanceInterface si : expf)
					for (ConditionInterface ci : si) {
						speciesNames.add(ci.getSpecies());
					}
				int idx = 1;
				for (SubstanceInterface si : expf)
					for (ConditionInterface ci : si)
						ci.setRowId(idx++);
				if (speciesNames.size() == 1)
					for (SubstanceInterface si : expf)
						for (ConditionInterface ci : si)
							ci.setSpecies(null);
				DataChartComponentWindow dccw = new DataChartComponentWindow(expf);
				this.chartGUI = dccw.getGUI();
			}
			
			@Override
			public String getDefaultTitle() {
				return "Create plot";
			}
			
			@Override
			public String getDefaultImage() {
				return IAPimages.getHistogramIcon();
			}
			
			@Override
			public MainPanelComponent getResultMainPanel() {
				return new MainPanelComponent(chartGUI);
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
			if (groupNames.size() > 0)
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
		this.experiment = new ExperimentReferenceWithFilterSupport(experimentReference);
		this.settingsGlobal.setIniIOprovider(experimentReference.getIniIoProvider());
	}
	
}
