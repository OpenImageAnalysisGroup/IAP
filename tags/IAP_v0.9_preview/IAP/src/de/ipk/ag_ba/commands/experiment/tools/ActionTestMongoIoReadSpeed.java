package de.ipk.ag_ba.commands.experiment.tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PerformanceAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
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
public class ActionTestMongoIoReadSpeed extends AbstractNavigationAction implements RemoteCapableAnalysisAction,
		ActionDataProcessing {
	private MongoDB m;
	private ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	
	// used when started as remote analysis task
	private RunnableWithMappingData resultReceiver;
	private int workOnSubset;
	private int numberOfSubsets;
	private String datasetID;
	private Date optProcessOnlySampleDataNewerThanThisDate;
	
	public ActionTestMongoIoReadSpeed() {
		super("Test performance by reading experiment content");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
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
			
			PerformanceAnalysisTask task = new PerformanceAnalysisTask();
			TreeMap<Long, String> times = new TreeMap<Long, String>();
			long t1 = System.currentTimeMillis();
			task.setInput(
					AbstractPhenotypingTask.getWateringInfo(res),
					workload, null, m, workOnSubset, numberOfSubsets);
			task.performAnalysis(1, 1, status);
			long t2 = System.currentTimeMillis();
			// String ss = "T(s)\t" + ((t2 - t1) / 1000);
			// times.put((t2 - t1), ss);
			// System.out.println("------------------------------------------------------------");
			// System.out.println("--- " + ss);
			// System.out.println("------------------------------------------------------------");
			// for (String s : times.values()) {
			// System.out.println(s);
			// }
			
			final ExperimentInterface statisticsResult = task.getOutput();
			statisticsResult.getHeader().setExperimentname(statisticsResult.getName() + " " + getDefaultTitle());
			
			statisticsResult.getHeader().setDatabaseId("");
			
			mpc = new MainPanelComponent("Running in batch-mode. Partial result is not shown at this place.");
			if (resultReceiver != null) {
				resultReceiver.setExperimenData(statisticsResult);
				resultReceiver.run();
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
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
		return null;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/grid2.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Cloud I/O Test";
	}
	
	@Override
	public void setWorkingSet(int workOnSubset, int numberOfSubsets,
			RunnableWithMappingData resultReceiver,
			Date optProcessOnlySampleDataNewerThanThisDate) {
		this.resultReceiver = resultReceiver;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
		this.optProcessOnlySampleDataNewerThanThisDate = optProcessOnlySampleDataNewerThanThisDate;
	}
	
	@Override
	public void setParams(ExperimentReference experiment, MongoDB m, String params) {
		this.experiment = experiment;
		this.m = m;
	}
	
	@Override
	public String getDatasetID() {
		return datasetID;
	}
	
	@Override
	public MongoDB getMongoDB() {
		return m;
	}
	
	@Override
	public int getCpuTargetUtilization() {
		// by returning this high number, this task will be the only one running
		// on the cloud execution server
		return Integer.MAX_VALUE;
	}
	
	@Override
	public int getNumberOfJobs() {
		return 1;
	}
	
	@Override
	public boolean remotingEnabledForThisAction() {
		return true;
	}
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReference experimentReference) {
		this.m = experimentReference.m;
		this.experiment = experimentReference;
		if (experiment != null && experimentReference.getHeader() != null)
			this.datasetID = experimentReference.getHeader().getDatabaseId();
		
	}
}