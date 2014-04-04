package de.ipk.ag_ba.gui.navigation_actions.maize;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Date;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.process.report.MySnapshotFilter;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.commands.vfs.ActionDataExportToVfs;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.images.IAPexperimentTypes;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentHeaderInfoPanel;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;
import de.ipk.ag_ba.server.task_management.RemoteCapableAnalysisAction;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
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
public abstract class AbstractPhenotypeAnalysisAction extends AbstractNavigationAction
		implements RemoteCapableAnalysisAction {
	protected MongoDB m;
	protected ExperimentReference experiment;
	NavigationButton src = null;
	MainPanelComponent mpc;
	protected ExperimentInterface experimentResult;
	
	private long startTime;
	
	// used when started as remote analysis task
	private RunnableWithMappingData resultReceiver;
	protected int workOnSubset;
	protected int numberOfSubsets;
	protected String mongoDatasetID;
	private Date optProcessOnlySampleDataNewerThanThisDate;
	
	public AbstractPhenotypeAnalysisAction(String tooltip) {
		super(tooltip);
	}
	
	protected SystemOptions getSystemOptions() {
		SystemOptions so = SystemOptions.getInstance(
				StringManipulationTools.getFileSystemName(getDefaultTitle()) + ".pipeline.ini",
				experiment.getIniIoProvider());
		return so;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		if (src != null)
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
			
			ExperimentInterface experimentToBeAnalysed = experiment.getData();
			sw.printTime();
			if (status != null)
				status.setCurrentStatusText2("Load time: " + sw.getTimeString());
			
			if (status != null)
				status.setCurrentStatusText2("Clone data set and set title...");
			sw.setDescription("Experiment cloning");
			sw.reset();
			experimentToBeAnalysed = experimentToBeAnalysed.clone();
			experimentToBeAnalysed.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults + "");
			experimentToBeAnalysed.getHeader().setExperimentname(getImageAnalysisTask().getName() + " of " +
					experiment.getExperimentName());
			experimentToBeAnalysed.setHeader(experimentToBeAnalysed.getHeader());
			sw.printTime();
			if (status != null)
				status.setCurrentStatusText2("Cloning time: " + sw.getTimeString());
			
			ArrayList<Sample3D> workload = new ArrayList<Sample3D>();
			
			MySnapshotFilter sf = new MySnapshotFilter(new ArrayList<ThreadSafeOptions>(), experiment.getHeader().getGlobalOutlierInfo());
			
			boolean filterTop = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "DEBUG-ONLY-TOP", false); // process only top images?
			boolean filterSide = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "DEBUG-ONLY-SIDE", false); // process only side images?
			boolean filterTime = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "DEBUG-ONLY-ONE-DAY", false); // process only one day? (day 48, see
																																									// below)
			int DEBUG_SINGLE_DAY = SystemOptions.getInstance().getInteger("Pipeline-Debugging", "DEBUG-SINGLE-DAY", 48);
			boolean filterPlant = SystemOptions.getInstance().getBoolean("Pipeline-Debugging", "DEBUG-ONLY-SINGLE-PLANT", false); // process only one plant?
			String plantFilter = SystemOptions.getInstance().getString("Pipeline-Debugging", "DEBUG-SINGLE-PLANT-ID", "001447-D1"); // "1107BA1350"; //
																																											// "1121KN063";
			
			filterOutliers(experimentToBeAnalysed, workload, sf, filterTop, filterSide, filterTime, DEBUG_SINGLE_DAY, filterPlant, plantFilter);
			
			if (status != null) {
				status.setCurrentStatusText1("Experiment: " + workload.size() + " images (vis+fluo+nir)");
				System.out.println(SystemAnalysis.getCurrentTime() + ">To be analyzed: " + workload.size() + " images (vis+fluo+nir)");
				
			}
			if (workload.size() == 0) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">NO WORKLOAD! PROCESSING IS SKIPPED");
				if (getResultReceiver() != null) {
					getResultReceiver().setExperimenData(null);
					getResultReceiver().run();
				}
				return;
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
			
			ExperimentInterface statisticsResult = task.getOutput();
			
			task = null;
			
			if (statisticsResult == null) { // || statisticsResult.getNumberOfMeasurementValues() <= 0
				System.err.println(SystemAnalysis.getCurrentTime() + ">ERROR: no statistics result");
				this.experimentResult = null;
				if (getResultReceiver() == null)
					mpc = new MainPanelComponent("No analysis results or analysis stop requested.");
				else {
					getResultReceiver().setExperimenData(null);
					getResultReceiver().run();
				}
			} else {
				if (status != null)
					status.setCurrentStatusText1("Create result dataset");
				statisticsResult.setHeader(experimentToBeAnalysed.getHeader().clone());
				statisticsResult.getHeader().setExperimentname(statisticsResult.getName());
				statisticsResult.getHeader().setImportusergroup(getDefaultTitle());
				for (SubstanceInterface s : statisticsResult) {
					for (ConditionInterface c : s) {
						c.setExperimentInfo(statisticsResult.getHeader());
					}
				}
				
				System.out.println("Statistics results: " + statisticsResult.getNumberOfMeasurementValues());
				// System.out.println("Statistics results within Experiment: " + statisticsResult.getNumberOfMeasurementValues());
				statisticsResult.getHeader().setDatabaseId("");
				
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
				
				// statisticsResult.getHeader().setStartdate(new Date(startTime));
				statisticsResult.getHeader().setStorageTime(new Date());
				statisticsResult.getHeader().setExperimenttype(IAPexperimentTypes.AnalysisResults + "");
				
				boolean removeCameraInfosAndReMerge = false; // normally not needed and requires high memory and compute time
				if (removeCameraInfosAndReMerge) {
					for (SubstanceInterface si : statisticsResult) {
						String name = si.getName();
						boolean clear = true;
						if (name != null && StringManipulationTools.count(name, ".") == 1) {
							name = name.toUpperCase();
							if (name.endsWith("top") || name.endsWith("side")) {
								clear = false;
							}
						}
						if (clear)
							ImageConfiguration.get(si.getName());
						si.setInfo(null);
					}
					statisticsResult = MappingData3DPath.merge(MappingData3DPath.get(statisticsResult, false), false, getStatusProvider());
				}
				
				if (getResultReceiver() == null) {
					if (status != null) {
						status.setCurrentStatusValue(-1);
						status.setCurrentStatusText1("Calculations finished");
						status.setCurrentStatusText2("Save result data structure...");
					}
					statisticsResult.getHeader().setImportusergroup(IAPexperimentTypes.AnalysisResults + "");
					String nn = getImageAnalysisTask().getName();
					if (!nn.contains(experiment.getExperimentName() + ""))
						nn = nn + " of " + experiment.getExperimentName();
					nn = StringManipulationTools.stringReplace(nn, ":", "_");
					statisticsResult.getHeader().setExperimentname(nn);
					
					statisticsResult.getHeader().setRemark(
							(statisticsResult.getHeader().getRemark() != null && !statisticsResult.getHeader().getRemark().isEmpty() ? statisticsResult.getHeader()
									.getRemark() + " // " : "")
									+
									"analysis started: " + SystemAnalysis.getCurrentTime(startTime) +
									" // processing time: " +
									SystemAnalysis.getWaitTime(System.currentTimeMillis() - startTime) +
									" // finished: " + SystemAnalysis.getCurrentTime());
					// System.out.println("Stat: " + ((Experiment) statisticsResult).getExperimentStatistics());
					statisticsResult.getHeader().setOriginDbId(dbID);
					statisticsResult.setHeader(statisticsResult.getHeader());
					
					VirtualFileSystemVFS2 vfs = VirtualFileSystemVFS2.getKnownFromDatabaseId(experiment.getHeader().getDatabaseId());
					if (!statisticsResult.getHeader().getDatabaseId().startsWith("mongo_") && vfs != null) {
						ActionDataExportToVfs ac = new ActionDataExportToVfs(m,
								new ExperimentReference(statisticsResult), vfs, false, null);
						ac.setSkipClone(true);
						ac.setSource(src != null ? src.getAction() : null, src != null ? src.getGUIsetting() : null);
						if (status != null)
							ac.setStatusProvider(status);
						ac.performActionCalculateResults(src);
					} else {
						if (m != null) {
							m.saveExperiment(statisticsResult, getStatusProvider());
						}
					}
					
					ExperimentHeaderInfoPanel info = new ExperimentHeaderInfoPanel();
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
			}
			if (status != null) {
				status.setCurrentStatusValue(-1);
				status.setCurrentStatusText1("Processing completed");
				status.setCurrentStatusText2("Results have been saved");
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			mpc = null;
		}
	}
	
	private void filterOutliers(ExperimentInterface experimentToBeAnalysed, ArrayList<Sample3D> workload, MySnapshotFilter sf, boolean filterTop,
			boolean filterSide,
			boolean filterTime, int DEBUG_SINGLE_DAY, boolean filterPlant, String plantFilter) {
		for (SubstanceInterface m : experimentToBeAnalysed) {
			Substance3D m3 = (Substance3D) m;
			if (filterTop) {
				if (m3.getName().contains("side."))
					continue;
			}
			if (filterSide) {
				if (m3.getName().contains("top."))
					continue;
			}
			// System.out.println("Substance-Name: " + m3.getName());
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
					if (optProcessOnlySampleDataNewerThanThisDate != null && sd3.getSampleFineTimeOrRowId() != null)
						if (sd3.getSampleFineTimeOrRowId() <= optProcessOnlySampleDataNewerThanThisDate.getTime())
							continue;
					if (filterTime) {
						if (sd3.getTime() != DEBUG_SINGLE_DAY)
							continue;
					}
					if (filterPlant) {
						String qa = sd3.iterator().next().getQualityAnnotation();
						if (qa != null && !qa.contains(plantFilter))
							continue;
					}
					if (!containsAnOutlier)
						if (filter == null || filter.isValidSample(sd3)) {
							// System.out.println("Add sample to workload (to be analyzed): " + sd3 + " ("
							// + SystemAnalysis.getCurrentTimeInclSec(sd3.getSampleFineTimeOrRowId()) + ")");
							workload.add(sd3);
						}
				}
			}
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		if (experimentResult != null) {
			ExperimentReference ref = new ExperimentReference(experimentResult);
			ref.m = m;
			
			for (ActionDataProcessing adp : IAPpluginManager.getInstance().getExperimentProcessingActions(ref, true))
				res.add(new NavigationButton(adp, src != null ? src.getGUIsetting() : null));
		}
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return mpc;
	}
	
	@Override
	public void setWorkingSet(int workOnSubset, int numberOfSubsets,
			RunnableWithMappingData resultReceiver,
			Date optProcessOnlySampleDataNewerThanThisDate) {
		this.setResultReceiver(resultReceiver);
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
		return mongoDatasetID;
	}
	
	@Override
	public MongoDB getMongoDB() {
		return m;
	}
	
	protected abstract ImageAnalysisTask getImageAnalysisTask();
	
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
	
	@Override
	public boolean remotingEnabledForThisAction() {
		return true;
	}
}
