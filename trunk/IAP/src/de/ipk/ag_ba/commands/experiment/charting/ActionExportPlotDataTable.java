package de.ipk.ag_ba.commands.experiment.charting;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.SystemOptions;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.ChartSettings;
import de.ipk.ag_ba.commands.experiment.process.report.ActionPdfCreation3;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.FileHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public final class ActionExportPlotDataTable extends AbstractNavigationAction {
	private NavigationButton src2;
	private final ExperimentTransformationPipeline transformationPipeline;
	private final ChartSettings settingsLocal;
	private final ChartSettings settingsGlobal;
	private SystemOptions set;
	
	public ActionExportPlotDataTable(String tooltip, ExperimentTransformationPipeline transformationPipeline, ChartSettings settingsLocal,
			ChartSettings settingsGlobal) {
		super(tooltip);
		this.transformationPipeline = transformationPipeline;
		this.settingsLocal = settingsLocal;
		this.settingsGlobal = settingsGlobal;
		this.set = !settingsLocal.getUseLocalSettings() ? settingsGlobal.getSettings() : settingsLocal.getSettings();
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		src2 = src;
		this.set = !settingsLocal.getUseLocalSettings() ? settingsGlobal.getSettings() : settingsLocal.getSettings();
		
		boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
		if (groupByPlantID)
			MainFrame.showMessageDialog("<html>"
					+ "Export may be incomplete in case the merge data option is enabled.<br>"
					+ "Currently it is needed to disable the merge option and then to summarize the detail data in the<br>"
					+ "data table manually.", "Warning");
		
		ExperimentInterface exp = transformationPipeline.getInput(null);
		SubstanceInterface sub = exp.iterator().next();
		
		String defaultFileName = StringManipulationTools.getFileSystemName(
				exp.getHeader().getExperimentName()
						+ "_" + sub.getName() + ".xlsx");
		String fn = FileHelper.getFileName(".xlsx", "Excel File", defaultFileName);
		if (fn != null) {
			boolean xlsx = true;
			ActionPdfCreation3 action = new ActionPdfCreation3(
					(ExperimentReferenceInterface) null,
					(ArrayList<ThreadSafeOptions>) null,
					new ThreadSafeOptions() /* false */,
					new ThreadSafeOptions() /* false */,
					new ThreadSafeOptions(),
					new ThreadSafeOptions(),
					xlsx,
					(ArrayList<ThreadSafeOptions>) null,
					(ArrayList<ThreadSafeOptions>) null,
					(ThreadSafeOptions) null,
					(ThreadSafeOptions) null,
					(ThreadSafeOptions) null,
					true);
			
			action.setExperimentReference(
					new ExperimentReference(exp));
			action.setUseIndividualReportNames(true);
			action.setStatusProvider(null);
			action.setSource(null, null);
			action.setCustomTargetFileName(fn);
			try {
				action.performActionCalculateResults(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public String getDefaultTitle() {
		boolean groupByPlantID = set.getBoolean("Summarize data", "Filter outliers//Merge into single value per day and plant ID", true);
		if (groupByPlantID)
			return "<html><center><font color='gray'>Export<br><small>(can't export merged data)</small></center>";
		else
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
}