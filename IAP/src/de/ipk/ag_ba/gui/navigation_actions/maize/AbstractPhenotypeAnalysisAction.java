package de.ipk.ag_ba.gui.navigation_actions.maize;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.actions.AbstractNavigationAction;
import de.ipk.ag_ba.gui.actions.ActionCopyToMongo;
import de.ipk.ag_ba.gui.actions.ActionFileManager;
import de.ipk.ag_ba.gui.actions.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.RunnableWithMappingData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Condition3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.MappingData3DPath;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;

/**
 * @author klukas
 */
public abstract class AbstractPhenotypeAnalysisAction extends AbstractNavigationAction implements RemoteCapableAnalysisAction {
	protected MongoDB m;
	protected ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	ArrayList<ZoomedImage> zoomedImages = new ArrayList<ZoomedImage>();
	protected Experiment experimentResult;
	
	// used when started as remote analysis task
	protected RunnableWithMappingData resultReceiver;
	protected int workOnSubset;
	protected int numberOfSubsets;
	protected String mongoDatasetID;
	
	public AbstractPhenotypeAnalysisAction(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		
		// BackgroundThreadDispatcher.useThreads = false;
		
		if (experimentResult != null)
			return;
		
		try {
			StopWatch sw = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">LOAD EXPERIMENT " + experiment.getExperimentName());
			
			String dbID = experiment.getHeader().getDatabaseId();
			
			ExperimentInterface experimentToBeAnalysed = experiment.getData(m);
			
			sw.printTime();
			ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
			
			for (SubstanceInterface m : experimentToBeAnalysed) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						workload.add(sd3);
					}
				}
			}
			
			if (status != null)
				status.setCurrentStatusText1("Experiment: " + workload.size() + " images");
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setInt(1);
			
			int pi = 1;// SystemAnalysis.getNumberOfCPUs();
			int ti = 1;// SystemAnalysis.getNumberOfCPUs() / 2;
			
			ImageAnalysisTask task = getImageAnalysisTask();
			
			task.setInput(workload, null, m, workOnSubset, numberOfSubsets);
			task.performAnalysis(pi, ti, status);
			
			final ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();
			Collection<NumericMeasurementInterface> statRes = task.getOutput();
			
			if (statRes == null) {
				ErrorMsg.addErrorMessage("Error: no statistics result");
			} else {
				for (NumericMeasurementInterface m : statRes) {
					if (m == null)
						System.out.println("Error: null value in statistical result set");
					else
						newStatisticsData.add(new MappingData3DPath(m));
				}
			}
			
			// for (MappingData3DPath mp : newStatisticsData) {
			// mp.getSampleData().setRowId(-1);
			// }
			//
			final Experiment statisticsResult = new Experiment(MappingData3DPath.merge(newStatisticsData, false));
			statisticsResult.getHeader().setExperimentname(statisticsResult.getName());
			statisticsResult.getHeader().setImportusergroup(getDefaultTitle());
			for (SubstanceInterface s : statisticsResult) {
				for (ConditionInterface c : s) {
					c.setExperimentInfo(statisticsResult.getHeader());
				}
			}
			
			System.out.println("Statistics results: " + newStatisticsData.size());
			// System.out.println("Statistics results within Experiment: " + statisticsResult.getNumberOfMeasurementValues());
			statisticsResult.setHeader(experiment.getHeader().clone());
			statisticsResult.getHeader().setDatabaseId("");
			if (statisticsResult.size() > 0) {
				SubstanceInterface subst = statisticsResult.iterator().next();
				if (subst.size() > 0) {
					ConditionInterface cond = subst.iterator().next();
					if (cond != null)
						statisticsResult.setHeader(cond.getExperimentHeader());
				}
			}
			boolean addWaterData = workOnSubset < 5;
			if (addWaterData) {
				for (SubstanceInterface si : experimentToBeAnalysed) {
					if (si.getName() != null && (si.getName().equals("weight_before") ||
							si.getName().equals("water_weight") || si.getName().equals("water_sum"))) {
						statisticsResult.add(si);
						for (ConditionInterface ci : si)
							ci.setExperimentHeader(statisticsResult.getHeader());
					}
				}
			}
			
			statisticsResult.getHeader().setOriginDbId(dbID);
			
			if (resultReceiver == null) {
				if (status != null)
					status.setCurrentStatusText1("Ready");
				
				MyExperimentInfoPanel info = new MyExperimentInfoPanel();
				info.setExperimentInfo(m, statisticsResult.getHeader(), false, statisticsResult);
				mpc = new MainPanelComponent(info, true);
			} else {
				// mpc = new MainPanelComponent("Running in batch-mode. Partial result is not shown at this place.");
				resultReceiver.setExperimenData(statisticsResult);
				resultReceiver.run();
			}
			this.experimentResult = statisticsResult;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		res.add(ActionFileManager.getFileManagerEntity(m, new ExperimentReference(experimentResult),
				src.getGUIsetting()));
		
		res.add(new NavigationButton(new ActionCopyToMongo(m, new ExperimentReference(experimentResult)),
				"Save Result", "img/ext/user-desktop.png", src.getGUIsetting())); // PoweredMongoDBgreen.png"));
		
		ActionMongoOrLemnaTecExperimentNavigation.getDefaultActions(res, experimentResult, experimentResult.getHeader(),
				false, src.getGUIsetting(), m);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
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
	public String getDatasetID() {
		return mongoDatasetID;
	}
	
	@Override
	public MongoDB getMongoDB() {
		return m;
	}
	
	protected abstract ImageAnalysisTask getImageAnalysisTask();
	
	protected abstract HashSet<ImageConfiguration> getValidImageTypes();
}
