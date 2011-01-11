package de.ipk.ag_ba.gui.navigation_actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PerformanceAnalysisTask;
import de.ipk.ag_ba.rmi_server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MeasurementNodeType;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class CloudIoTestAction extends AbstractNavigationAction implements RemoteCapableAnalysisAction {
	private MongoDB m;
	private ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	
	// used when started as remote analysis task
	private RunnableWithMappingData resultReceiver;
	private int workOnSubset;
	private int numberOfSubsets;
	private String datasetID;
	
	public CloudIoTestAction(MongoDB m, ExperimentReference experiment) {
		super("Test performance by reading experiment content");
		this.m = m;
		this.experiment = experiment;
		if (experiment != null && experiment.getHeader() != null)
			this.datasetID = experiment.getHeader().getExcelfileid();
	}
	
	public CloudIoTestAction() {
		super("Test performance");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		try {
			ExperimentInterface res = experiment.getData(m);
			
			ArrayList<NumericMeasurementInterface> workload = new ArrayList<NumericMeasurementInterface>();
			
			int workIndex = 0;
			for (SubstanceInterface m : res) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						for (Measurement md : sd3.getMeasurements(MeasurementNodeType.IMAGE)) {
							workIndex++;
							if (resultReceiver == null || workIndex % numberOfSubsets == workOnSubset)
								if (md instanceof ImageData) {
									workload.add((NumericMeasurementInterface) md);
								}
						}
					}
				}
			}
			
			if (status != null)
				status.setCurrentStatusText1("Workload: " + workload.size() + " images");
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setInt(1);
			
			PerformanceAnalysisTask task = new PerformanceAnalysisTask();
			TreeMap<Long, String> times = new TreeMap<Long, String>();
			Collection<NumericMeasurementInterface> statRes = new ArrayList<NumericMeasurementInterface>();
			
			long t1 = System.currentTimeMillis();
			task.setInput(workload, m, workOnSubset, numberOfSubsets);
			task.performAnalysis(1, 1, status);
			long t2 = System.currentTimeMillis();
			statRes.addAll(task.getOutput());
			// String ss = "T(s)\t" + ((t2 - t1) / 1000);
			// times.put((t2 - t1), ss);
			// System.out.println("------------------------------------------------------------");
			// System.out.println("--- " + ss);
			// System.out.println("------------------------------------------------------------");
			// for (String s : times.values()) {
			// System.out.println(s);
			// }
			
			final ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();
			
			{
				for (NumericMeasurementInterface m : statRes) {
					if (m == null)
						System.out.println("ERROR NULL");
					else
						newStatisticsData.add(new MappingData3DPath(m));
				}
			}
			
			final Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData));
			statisticsResult.getHeader().setExperimentname(statisticsResult.getName() + " " + getDefaultTitle());
			
			statisticsResult.getHeader().setExcelfileid("");
			
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
	public void setWorkingSet(int workOnSubset, int numberOfSubsets, RunnableWithMappingData resultReceiver) {
		this.resultReceiver = resultReceiver;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
	}
	
	@Override
	public void setParams(ExperimentReference experiment, MongoDB m, String params) {
		this.experiment = experiment;
		this.m = m;
	}
	
	@Override
	public String getMongoDatasetID() {
		return datasetID;
	}
	
	@Override
	public MongoDB getMongoDB() {
		return m;
	}
	
}