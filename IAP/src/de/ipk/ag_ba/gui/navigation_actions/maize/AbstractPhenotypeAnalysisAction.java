package de.ipk.ag_ba.gui.navigation_actions.maize;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.ActionCopyToMongo;
import de.ipk.ag_ba.commands.ActionFileManager;
import de.ipk.ag_ba.commands.ActionMongoOrLemnaTecExperimentNavigation;
import de.ipk.ag_ba.commands.ImageConfiguration;
import de.ipk.ag_ba.commands.MySnapshotFilter;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.ZoomedImage;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.gui.util.MyExperimentInfoPanel;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

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
	
	private long startTime;
	
	// used when started as remote analysis task
	private RunnableWithMappingData resultReceiver;
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
		
		startTime = System.currentTimeMillis();
		
		// BackgroundThreadDispatcher.useThreads = false;
		
		if (experimentResult != null)
			return;
		
		try {
			StopWatch sw = new StopWatch(SystemAnalysis.getCurrentTime() + ">LOAD EXPERIMENT " + experiment.getExperimentName());
			
			String dbID = experiment.getHeader().getDatabaseId();
			
			ExperimentInterface experimentToBeAnalysed = experiment.getData(m);
			
			sw.printTime();
			if (status != null)
				status.setCurrentStatusText2("Load time: " + sw.getTimeString());
			
			ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
			
			MySnapshotFilter sf = new MySnapshotFilter(new ArrayList<ThreadSafeOptions>(), experiment.getHeader().getGlobalOutlierInfo());
			
			for (SubstanceInterface m : experimentToBeAnalysed) {
				Substance3D m3 = (Substance3D) m;
				for (ConditionInterface s : m3) {
					Condition3D s3 = (Condition3D) s;
					for (SampleInterface sd : s3) {
						Sample3D sd3 = (Sample3D) sd;
						boolean containsAnOutlier = false;
						
						outlierSearch: for (NumericMeasurementInterface nmi : sd3) {
							if (nmi instanceof ImageData) {
								ImageData id = (ImageData) nmi;
								String o = id.getAnnotationField("outlier");
								if (o != null && o.equals("1")) {
									containsAnOutlier = true;
									break outlierSearch;
								}
								if (sf.filterOut(id.getQualityAnnotation(), sd3.getTime())) {
									containsAnOutlier = true;
									break outlierSearch;
								}
							}
						}
						// if (sd3.getTime() != 29)
						// continue;
						if (!containsAnOutlier)
							if (filter == null || filter.isValidSample(sd3)) {
								workload.add(sd3);
							}
					}
				}
			}
			
			if (status != null) {
				status.setCurrentStatusText1("Experiment: " + workload.size() + " image snapshot sets (vis+fluo+nir)");
			}
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			tso.setInt(1);
			
			int pi = 1;// SystemAnalysis.getNumberOfCPUs();
			int ti = 1;// SystemAnalysis.getNumberOfCPUs() / 2;
			
			ImageAnalysisTask task = getImageAnalysisTask();
			
			task.setInput(
					AbstractPhenotypingTask.getWateringInfo(experimentToBeAnalysed),
					workload, null, m, workOnSubset, numberOfSubsets);
			
			task.setUnitTestInfo(unit_test_idx, unit_test_steps);
			
			task.performAnalysis(pi, ti, status);
			
			if (status != null)
				status.setCurrentStatusText1("Analysis finished");
			
			final ArrayList<MappingData3DPath> newStatisticsData = new ArrayList<MappingData3DPath>();
			Collection<NumericMeasurementInterface> statRes = task.getOutput();
			
			if (statRes == null) {
				System.err.println("Error: no statistics result");
			} else {
				for (NumericMeasurementInterface m : statRes) {
					if (m == null)
						System.out.println("Error: null value in statistical result set");
					else
						newStatisticsData.add(new MappingData3DPath(m));
				}
			}
			
			if (statRes != null) {
				// for (MappingData3DPath mp : newStatisticsData) {
				// mp.getSampleData().setRowId(-1);
				// }
				//
				if (status != null)
					status.setCurrentStatusText1("Create result dataset");
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
				boolean addWaterData = workOnSubset == 0;
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
				statisticsResult.getHeader().setStartdate(new Date(startTime));
				statisticsResult.getHeader().setStorageTime(new Date());
				statisticsResult.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults + "");
				
				if (getResultReceiver() == null) {
					if (status != null)
						status.setCurrentStatusText1("Ready");
					
					statisticsResult.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults + "");
					statisticsResult.getHeader().setExperimentname(
							getImageAnalysisTask().getClass().getCanonicalName() + ": " +
									experiment.getExperimentName());
					
					statisticsResult.getHeader().setRemark(
							statisticsResult.getHeader().getRemark() +
									" // IAP image analysis release " + IAPmain.RELEASE_IAP_IMAGE_ANALYSIS +
									" // analysis started: " + SystemAnalysis.getCurrentTime(startTime) +
									" // finished: " + SystemAnalysis.getCurrentTime() +
									" // processing time: " +
									SystemAnalysis.getWaitTime(System.currentTimeMillis() - startTime) +
									" // finished: " + SystemAnalysis.getCurrentTime() +
									" // direct mode analysis");
					
					if (m != null)
						m.saveExperiment(statisticsResult, getStatusProvider());
					
					MyExperimentInfoPanel info = new MyExperimentInfoPanel();
					info.setExperimentInfo(m, statisticsResult.getHeader(), false, statisticsResult);
					mpc = new MainPanelComponent(info, true);
				} else {
					// mpc = new MainPanelComponent("Running in batch-mode. Partial result is not shown at this place.");
					if (status != null)
						status.setCurrentStatusText1("Result-Receiver processes results");
					getResultReceiver().setExperimenData(statisticsResult);
					getResultReceiver().run();
					if (status != null)
						status.setCurrentStatusText1("Processing finished");
				}
				this.experimentResult = statisticsResult;
			} else {
				this.experimentResult = null;
				if (getResultReceiver() == null)
					mpc = new MainPanelComponent("Stop requested, processing aborted, output set to NULL.");
				else {
					getResultReceiver().setExperimenData(null);
					getResultReceiver().run();
				}
			}
			
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		
		res.add(ActionFileManager.getFileManagerEntity(m, new ExperimentReference(experimentResult),
				src != null ? src.getGUIsetting() : null));
		
		res.add(new NavigationButton(new ActionCopyToMongo(m, new ExperimentReference(experimentResult)),
				"Save Result", "img/ext/user-desktop.png", src != null ? src.getGUIsetting() : null)); // PoweredMongoDBgreen.png"));
		
		ActionMongoOrLemnaTecExperimentNavigation.getDefaultActions(res,
				new ExperimentReference(experimentResult), experimentResult.getHeader(),
				false, src != null ? src.getGUIsetting() : null, m);
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public void setWorkingSet(int workOnSubset, int numberOfSubsets, RunnableWithMappingData resultReceiver) {
		this.setResultReceiver(resultReceiver);
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
	
	private SampleFilter filter;
	private int unit_test_idx = -1;
	private int unit_test_steps = -1;
	
	public void setFilter(SampleFilter filter) {
		this.filter = filter;
	}
	
	public RunnableWithMappingData getResultReceiver() {
		return resultReceiver;
	}
	
	public void setResultReceiver(RunnableWithMappingData resultReceiver) {
		this.resultReceiver = resultReceiver;
	}
	
	public void setUnitTestValueRangeInfo(int idx, int steps) {
		this.unit_test_idx = idx;
		this.unit_test_steps = steps;
	}
}
