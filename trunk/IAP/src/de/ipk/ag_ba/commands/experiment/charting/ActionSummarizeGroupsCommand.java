package de.ipk.ag_ba.commands.experiment.charting;

import iap.blocks.extraction.Numeric;
import iap.blocks.extraction.Outlier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.JCheckBox;

import org.GapList;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;

public final class ActionSummarizeGroupsCommand extends AbstractNavigationAction implements ExperimentTransformation {
	private NavigationButton src2;
	private final LinkedHashSet<String> groups = new LinkedHashSet<String>();
	private SystemOptions set;
	private final ExperimentTransformationPipeline pipeline;
	private final ChartSettings settingsLocal;
	private final ChartSettings settingsGlobal;
	
	public ActionSummarizeGroupsCommand(String tooltip, ExperimentTransformationPipeline pipeline, ChartSettings settingsLocal, ChartSettings settingsGlobal) {
		super(tooltip);
		this.pipeline = pipeline;
		this.settingsLocal = settingsLocal;
		this.settingsGlobal = settingsGlobal;
		this.set = !settingsLocal.getUseLocalSettings() ? settingsGlobal.getSettings() : settingsLocal.getSettings();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.set = !settingsLocal.getUseLocalSettings() ? settingsGlobal.getSettings() : settingsLocal.getSettings();
		
		src2 = src;
		
		boolean calcGrubbsA = set.getBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", false);
		boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
		boolean calcGrubbsB = set.getBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", false);
		boolean calcANOVA = set.getBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", false);
		double calcGrubbsAlpha = set.getDouble("Summarize data", "Filter outliers//Grubbs alpha-values", 0.05);
		
		JCheckBox cbPerformGrubbsTestA = new JCheckBox("Perform Grubbs test on group samples to remove outliers");
		cbPerformGrubbsTestA.setSelected(calcGrubbsA);
		JCheckBox cbGroup = new JCheckBox("Merge data into single value for each day and plant ID");
		cbGroup.setSelected(groupByPlantID);
		JCheckBox cbPerformGrubbsTestB = new JCheckBox("Perform Grubbs test on group samples to remove outliers");
		cbPerformGrubbsTestB.setSelected(calcGrubbsB);
		JCheckBox cbANOVA = new JCheckBox("Calculate ANOVA p-Values");
		cbANOVA.setSelected(calcANOVA);
		
		Object[] res = MyInputHelper.getInput("Group data by plant ID prior to plotting?", "Process Data", new Object[] {
				"1.", cbPerformGrubbsTestA,
				"2.", cbGroup,
				// "3.", cbPerformGrubbsTestB,
				// "4.", cbANOVA,
				"Grubbs' alpha-Value", calcGrubbsAlpha
		});
		if (res != null) {
			set.setBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", cbPerformGrubbsTestA.isSelected());
			set.setBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", cbGroup.isSelected());
			set.setBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", cbPerformGrubbsTestB.isSelected());
			set.setBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", cbANOVA.isSelected());
			set.setDouble("Summarize data", "Filter outliers//Grubbs alpha-values", (Double) res[2]);
			pipeline.setDirty(this);
		}
	}
	
	@Override
	public String getDefaultTitle() {
		synchronized (groups) {
			boolean calcGrubbsA = set.getBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", false);
			boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
			boolean calcGrubbsB = set.getBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", false);
			boolean calcANOVA = set.getBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", false);
			int step = 1;
			if (!calcGrubbsA && !groupByPlantID && !calcGrubbsB && !calcANOVA)
				return "<html><center><b>&#8667;</b>&nbsp;Pass data&nbsp;<b>&#8667;</b><br><font color='gray'><small>no calculations";
			else
				return "<html><center><b>&#8667;</b>&nbsp;Process data"
						+ "&nbsp;<b>&#8667;</b><br><font color='gray'><small>"
						+ (calcGrubbsA ? (step++) + ". outlier removal for technical replicates" : "")
						+ (groupByPlantID ? (calcGrubbsA ? "<br>" : "") + (step++) + ". calculate mean value for plant ID and day"
								+ (calcGrubbsB ? "<br>" + (step++) + ". outlier removal for group replicates" : "")
								: "")
						+ (calcANOVA ? (calcGrubbsA || groupByPlantID || calcGrubbsB ? "<br>" : "") + (step++) + ". calculate ANOVA p-Values" : "")
						+ "</small></font></center>";
			// (groupsDeterminationInProgress.getBval(0, false) ? "~ one moment ~<br>determine group set" :
			// (groups.size() == 1 ? "1 step" : groups.size() + " steps"));
		}
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
	
	@Override
	public ExperimentInterface transform(ExperimentInterface input) {
		ExperimentInterface result = input.clone();
		
		boolean calcGrubbsA = set.getBoolean("Summarize data", "Filter outliers//Grubbs test before mean calculation", false);
		boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
		boolean calcGrubbsB = set.getBoolean("Summarize data", "Filter outliers//Grubbs test for final sample data", false);
		boolean calcANOVA = set.getBoolean("Summarize data", "Filter outliers//Calculate ANOVA p-values", false);
		double calcGrubbsAlpha = set.getDouble("Summarize data", "Filter outliers//Grubbs alpha-values", 0.05);
		
		if (!calcGrubbsA && !groupByPlantID && !calcGrubbsB && !calcANOVA)
			return result;
		
		if (groupByPlantID) {
			ArrayList<MappingData3DPath> pathObjects = MappingData3DPath.get(result, true);
			for (MappingData3DPath po : pathObjects) {
				po.getSampleData().setSampleFineTimeOrRowId(null);
			}
			result = MappingData3DPath.merge(pathObjects, true);
			result.visitSamples(null, (s) -> {
				s.setSampleAverage(null);
				if (s.size() > 0) {
					s.recalculateSampleAverage(false);
					NumericMeasurementInterface nmiAveragePerPlant = new NumericMeasurement(s);
					NumericMeasurementInterface template = s.iterator().next();
					nmiAveragePerPlant.setQualityAnnotation(template.getQualityAnnotation());
					nmiAveragePerPlant.setReplicateID(template.getReplicateID());
					nmiAveragePerPlant.setUnit(template.getUnit());
					nmiAveragePerPlant.setValue(s.getSampleAverage().getValue());
					nmiAveragePerPlant.setParentSample(s);
					s.setSampleAverage(null);
					s.recalculateSampleAverage(false);
					s.getSampleAverage().setUnit(template.getUnit());
				}
			});
			
			pathObjects = MappingData3DPath.get(result, true);
			for (MappingData3DPath po : pathObjects) {
				po.getMeasurement().setQualityAnnotation(null);
			}
			result = MappingData3DPath.merge(pathObjects, true);
		}
		if (calcGrubbsA) {
			result.visitSamples(null, (s) -> {
				s.setSampleAverage(null);
				
				if (calcGrubbsA) {
					GapList<Numeric> values = new GapList<Numeric>();
					LinkedList<Numeric> low = new LinkedList<>();
					LinkedList<Numeric> hig = new LinkedList<>();
					HashMap<Numeric, NumericMeasurementInterface> val2nmi = new HashMap<>();
					for (NumericMeasurementInterface nmi : s) {
						Numeric num = new Numeric() {
							@Override
							public Double getValue() {
								return nmi.getValue();
							}
						};
						val2nmi.put(num, nmi);
						values.add(num);
					}
					Outlier.doGrubbsTest(values, calcGrubbsAlpha, low, hig);
					for (Numeric l : low)
						s.remove(val2nmi.get(l));
					for (Numeric l : hig)
						s.remove(val2nmi.get(l));
				}
				
				s.recalculateSampleAverage(false);
			});
		}
		
		if (groupByPlantID) {
			ArrayList<MappingData3DPath> pathObjects = MappingData3DPath.get(result, false);
			for (MappingData3DPath po : pathObjects) {
				po.getSampleData().setSampleAverage(null);
				po.getSampleData().setSampleFineTimeOrRowId(null);
				po.getMeasurement().setQualityAnnotation(null);
			}
			
			result = MappingData3DPath.merge(pathObjects, groupByPlantID);
			
			result.visitSamples(null, (s) -> {
				s.setSampleAverage(null);
				s.recalculateSampleAverage(false);
			});
		}
		
		if (calcGrubbsB) {
			int nA = result.getNumberOfMeasurementValues();
			result.visitSamples(null, (s) -> {
				s.setSampleAverage(null);
				
				GapList<Numeric> values = new GapList<Numeric>();
				LinkedList<Numeric> low = new LinkedList<>();
				LinkedList<Numeric> hig = new LinkedList<>();
				HashMap<Numeric, NumericMeasurementInterface> val2nmi = new HashMap<>();
				for (NumericMeasurementInterface nmi : s) {
					Numeric num = new Numeric() {
						@Override
						public Double getValue() {
							return nmi.getValue();
						}
					};
					val2nmi.put(num, nmi);
					values.add(num);
				}
				Outlier.doGrubbsTest(values, calcGrubbsAlpha, low, hig);
				for (Numeric l : low)
					s.remove(val2nmi.get(l));
				for (Numeric l : hig)
					s.remove(val2nmi.get(l));
				
				s.recalculateSampleAverage(false);
			});
			result.numberConditions();
			int nB = result.getNumberOfMeasurementValues();
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Removed outliers B " + nA + " --> " + nB);
		}
		
		return result;
	}
	
	@Override
	public void updateStatus() throws Exception {
		// empty
	}
}