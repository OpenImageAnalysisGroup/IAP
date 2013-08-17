package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;
import java.util.TreeMap;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PerformanceAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public class ActionPerformanceTest extends AbstractNavigationAction implements ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	private ExperimentInterface experimentResult;
	
	// used when started as remote analysis task
	private RunnableWithMappingData resultReceiver;
	private int workOnSubset;
	private int numberOfSubsets;
	
	public ActionPerformanceTest() {
		super("Test performance by reading experiment content");
		this.experimentResult = null;
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		if (experimentResult != null)
			return;
		
		try {
			ExperimentInterface res = experiment.getData();
			
			ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
			
			int workIndex = 0;
			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						workIndex++;
						if (resultReceiver == null || workIndex % numberOfSubsets == workOnSubset)
							workload.add(sd3);
					}
				}
			}
			
			if (status != null)
				status.setCurrentStatusText1("Workload: " + workload.size() + " images");
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setInt(1);
			
			// for (NumericMeasurementInterface id : workload) {
			// System.out.println("Input: " + (((ImageData)
			// id).getURL()).getDetail());
			// }
			
			PerformanceAnalysisTask task = new PerformanceAnalysisTask();
			// task.addPreprocessor(new CutImagePreprocessor());
			TreeMap<Long, String> times = new TreeMap<Long, String>();
			// for (int pi = SystemAnalysis.getNumberOfCPUs(); pi >= 1; pi -= 1) {
			long t1 = System.currentTimeMillis();
			task.setInput(
					AbstractPhenotypingTask.getWateringInfo(res),
					workload, null, m, 0, 1);
			task.performAnalysis(1, 1, status);
			long t2 = System.currentTimeMillis();
			
			final ExperimentInterface statisticsResult = task.getOutput();
			statisticsResult.getHeader().setExperimentname(statisticsResult.getName() + " " + getDefaultTitle());
			
			statisticsResult.getHeader().setDatabaseId("");
			if (resultReceiver == null) {
				if (m != null && statisticsResult != null) {
					if (status != null)
						status.setCurrentStatusText1("Store Result");
					
					m.saveExperiment(statisticsResult, status);
				}
				
				if (status != null)
					status.setCurrentStatusText1("Ready");
				
				MyExperimentInfoPanel info = new MyExperimentInfoPanel();
				info.setExperimentInfo(m, statisticsResult.getHeader(), false, statisticsResult);
				mpc = new MainPanelComponent(info, true);
			} else {
				mpc = new MainPanelComponent("Running in batch-mode. Partial result is not shown at this place.");
				resultReceiver.setExperimenData(statisticsResult);
				resultReceiver.run();
			}
			this.experimentResult = statisticsResult;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
		// src.title = src.title.split("\\:")[0];
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
		
		ExperimentReference experimentReference = new ExperimentReference(experimentResult);
		experimentReference.m = m;
		
		for (NavigationAction na : IAPpluginManager.getInstance().getExperimentProcessingActions(experimentReference, true)) {
			res.add(new NavigationButton(na, src.getGUIsetting()));
		}
		
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/preferences-desktop.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Performance Test";
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
	
	// @Override
	// public void setWorkingSet(int workOnSubset, int numberOfSubsets,
	// RunnableWithMappingData resultReceiver) {
	// this.resultReceiver = resultReceiver;
	// this.workOnSubset = workOnSubset;
	// this.numberOfSubsets = numberOfSubsets;
	// }
	//
	// @Override
	// public void setParams(ExperimentReference experiment, String login, String
	// pass, String params) {
	// this.experiment = experiment;
	// this.login = login;
	// this.pass = pass;
	// }
	
}