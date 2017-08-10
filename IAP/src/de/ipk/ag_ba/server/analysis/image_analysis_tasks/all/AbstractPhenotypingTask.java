package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.MeasurementFilter;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemHandler;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.gui.picture_gui.StreamBackgroundTaskHelper;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.databases.DatabaseTargetNull;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;
import iap.pipelines.ImageProcessor;
import iap.pipelines.ImageProcessorOptionsAndResults;
import info.StopWatch;

/**
 * @author Christian Klukas
 */
public abstract class AbstractPhenotypingTask implements ImageAnalysisTask {
	
	public static final String _1ST_TOP = "1st_top";
	public static final String _2ND_SIDE = "2nd_side";
	private Collection<Sample3D> input = new ArrayList<Sample3D>();
	private ExperimentInterface output = new Experiment();
	
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	boolean forceDebugStack;
	ArrayList<ImageStack> forcedDebugStacks;
	private int prio;
	private MongoDB m;
	private TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData;
	int unit_test_idx;
	int unit_test_steps;
	private int[] debugValidTrays;
	final PipelineDesc pd;
	private String debugLastSystemOptionStorageGroup;
	private int DEBUG_SINGLE_ANGLE1, DEBUG_SINGLE_ANGLE2, DEBUG_SINGLE_ANGLE3;
	private boolean filterAngle;
	
	public AbstractPhenotypingTask(PipelineDesc pd) {
		this.pd = pd;
	}
	
	public SystemOptions getSystemOptions() {
		return pd.getOptions();
	}
	
	@Override
	public String getTaskDescription() {
		return pd.getTooltip();
	}
	
	@Override
	public String getName() {
		return pd.getName();
	}
	
	public int getNumberOfSubsets() {
		return numberOfSubsets;
	}
	
	@Override
	public void setInput(
			ExperimentHeaderInterface experimentHeader,
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData,
			Collection<Sample3D> input,
			Collection<NumericMeasurementInterface> optValidMeasurements,
			MongoDB m, int workOnSubset, int numberOfSubsets) {
		this.plandID2time2waterData = plandID2time2waterData;
		this.input = input;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
		this.m = m;
		// m should be used if the experiment is also stored there, otherwise the binary files should be stored in the VFS
		try {
			databaseTarget = determineDatabaseTarget(experimentHeader, m);
		} catch (Exception e) {
			e.printStackTrace();
			MongoDB.saveSystemErrorMessage("Could not initialize storage target: " + e.getMessage(), e);
		}
	}
	
	public static DatabaseTarget determineDatabaseTarget(ExperimentHeaderInterface header, MongoDB m) throws Exception {
		DatabaseTarget databaseTarget = m != null ? new DataBaseTargetMongoDB(true, m, m.getColls()) : null;
		if (databaseTarget != null)
			return databaseTarget;
		String prefix = null;
		if (header != null && header.getDatabaseId() != null) {
			prefix = header.getDatabaseId().split(":")[0];
			if (!prefix.startsWith("mongo_")) {
				databaseTarget = null;
			}
		}
		
		if (header != null && header.getDatabaseId() == null) {
			databaseTarget = new DatabaseTargetNull();
		}
		
		if (databaseTarget == null) {
			ArrayList<VirtualFileSystem> vl = VirtualFileSystemFolderStorage.getKnown(true);
			if (IAPmain.getRunMode() != IAPrunMode.WEB) {
				if (vl != null && vl.size() >= 1) {
					if (prefix != null) {
						for (VirtualFileSystem vfs : vl) {
							if (vfs.getPrefix().equals(prefix) && vfs instanceof DatabaseTarget) {
								databaseTarget = (DatabaseTarget) vfs;
								break;
							} else
								if (vfs.getPrefix().equals(prefix)) {
									databaseTarget = new HSMfolderTargetDataManager(vfs.getPrefix(), vfs.getTargetPathName());
									break;
								}
						}
					} else {
						Object[] sel = MyInputHelper.getInput("Please select the target for the storage of the result images. <br>" +
								"If no target is selected (or cancel is pressed), the numeric result will be<br>" +
								"kept in memory, and result image data will be discarded.",
								"Select target storage", "Target", vl);
						if (sel != null) {
							VirtualFileSystem sss = (VirtualFileSystem) sel[0];
							if (sss != null)
								databaseTarget = new HSMfolderTargetDataManager(sss.getPrefix(), sss.getTargetPathName());
						}
					}
				} else
					if (vl != null && vl.size() > 0) {
						VirtualFileSystem sss = vl.iterator().next();
						databaseTarget = new HSMfolderTargetDataManager(sss.getPrefix(), sss.getTargetPathName());
					}
			}
		}
		if (databaseTarget == null) {
			ResourceIOHandler vfs = ResourceIOManager.getHandlerFromPrefix(prefix);
			if (vfs != null && vfs instanceof VirtualFileSystemHandler)
				databaseTarget = new HSMfolderTargetDataManager(vfs.getPrefix(), ((VirtualFileSystemHandler) vfs).getVFS().getTargetPathName());
		}
		return databaseTarget;
	}
	
	public void debugOverrideAndEnableDebugStackStorage(boolean enable) {
		this.forceDebugStack = enable;
		this.forcedDebugStacks = new ArrayList<ImageStack>();
	}
	
	public ArrayList<ImageStack> getForcedDebugStackStorageResult() {
		return forcedDebugStacks;
	}
	
	@Override
	public void performAnalysis(final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws InterruptedException {
		
		status.setCurrentStatusValue(-1);
		status.setCurrentStatusText1("Wait for execution time slot");
		status.setCurrentStatusText2("(another analysis is currently running)");
		final Semaphore maxInst = BackgroundTaskHelper.lockGetSemaphore(
				AbstractPhenotypingTask.class, SystemOptions.getInstance().getInteger("IAP", "Max-Concurrent-Phenotyping-Tasks", 1));
		maxInst.acquire();
		try {
			status.setCurrentStatusValue(0);
			status.setCurrentStatusText1("Initiate " + getName());
			status.setCurrentStatusText2("");
			output = new Experiment();
			
			/**
			 * ____A MAP FROM PLANT ID TO
			 * ________LIST OF TIME POINTS
			 * ____________TO ROTATION ANGLE TO
			 * ________________IMAGE SNAPSHOT SET OF VIS/FLUO/NIR
			 */
			final TreeMap<String, TreeMap<Long, TreeMap<String, ImageSet>>> workload_imageSetsWithSpecificAngles = new TreeMap<String, TreeMap<Long, TreeMap<String, ImageSet>>>();
			
			addTopOrSideImagesToWorkset(workload_imageSetsWithSpecificAngles, 0, analyzeTopImages(),
					analyzeSideImages());
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			final int workloadSnapshots = workload_imageSetsWithSpecificAngles.size();
			int side = 0;
			int top = 0;
			for (TreeMap<Long, TreeMap<String, ImageSet>> plants : workload_imageSetsWithSpecificAngles.values())
				for (TreeMap<String, ImageSet> imageSetWithSpecificAngle : plants.values())
					for (ImageSet md : imageSetWithSpecificAngle.values()) {
						if (md.isSideImage())
							side++;
						else
							top++;
					}
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">INFO: Workload Top/Side: " + top + "/" + side);
			final int workloadEqualAngleSnapshotSets = top + side;
			
			final ThreadSafeOptions freed = new ThreadSafeOptions();
			
			int numberOfPlants = workload_imageSetsWithSpecificAngles.keySet().size();
			int progress = 0;
			
			LinkedList<Runnable> workLoad = new LinkedList<Runnable>();
			LinkedList<String> workLoad_desc = new LinkedList<String>();
			
			for (final String plantID : workload_imageSetsWithSpecificAngles.keySet()) {
				if (status.wantsToStop())
					continue;
				final String plantIDf = plantID;
				try {
					progress++;
					final String preThreadName = "Analysis Task " + progress + "/" + numberOfPlants + " (" + plantID + ")";
					status.setCurrentStatusText1(preThreadName);
					Runnable t = new Runnable() {
						@Override
						public void run() {
							TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle_f = workload_imageSetsWithSpecificAngles.get(plantID);
							try {
								processPlant(
										plandID2time2waterData,
										plantIDf, preThreadName,
										status, tso, workloadSnapshots,
										workloadEqualAngleSnapshotSets,
										imageSetWithSpecificAngle_f);
							} catch (Exception err) {
								printError(imageSetWithSpecificAngle_f, err);
							} finally {
								freed.setBval(0, true);
							}
						}
					};
					workLoad.add(t);
					workLoad_desc.add(preThreadName);
				} catch (Exception eeee) {
					throw new RuntimeException(eeee);
				}
			}
			status.setCurrentStatusText1(" Process Tasks");
			ArrayList<LocalComputeJob> wait = new ArrayList<LocalComputeJob>();
			final int todo = workLoad.size();
			final ThreadSafeOptions progr = new ThreadSafeOptions();
			while (!workLoad.isEmpty() && !status.wantsToStop()) {
				Runnable t = workLoad.poll();
				String d = workLoad_desc.poll();
				wait.add(BackgroundThreadDispatcher.addTask(t, d, true));
				progr.addInt(1);
				String plantName = d;
				if (plantName.contains(";"))
					plantName = plantName.split(";", 2)[1];
				do {
					Thread.sleep(100);
					status.setCurrentStatusText1("Enqueue Task " + progr.getInt() + "/" + todo + " ("
							+ BackgroundThreadDispatcher.getWorkLoad() + " tasks, "
							+ (BackgroundThreadDispatcher.getBackgroundThreadCount()) + " threads)");
				} while (BackgroundThreadDispatcher.getWorkLoad() - 2 > SystemAnalysis.getNumberOfCPUs());
			}
			BackgroundThreadDispatcher.waitFor(wait, new Runnable() {
				@Override
				public void run() {
					while (!Thread.interrupted()) {
						status.setCurrentStatusText1("Enqueued " + progr.getInt() + "/" + todo
								+ " plants (" + BackgroundThreadDispatcher.getWorkLoad() + " tasks, "
								+ (BackgroundThreadDispatcher.getBackgroundThreadCount()) + " threads)");
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// empty
						}
					}
				}
				
			});
		} finally {
			maxInst.release();
		}
		status.setCurrentStatusValueFine(100d);
		input = null;
		
		if (status.wantsToStop())
			output = null;
	}
	
	private int getParentPriority() {
		return prio;
	}
	
	public void setParentPriority(int prio) {
		this.prio = prio;
	}
	
	private void processPlant(
			TreeMap<String, TreeMap<Long, Double>> plantID2time2waterData2,
			String plantID, String preThreadName,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final ThreadSafeOptions tso, final int workloadSnapshots,
			final int workloadEqualAngleSnapshotSets,
			final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle)
			throws InterruptedException {
		
		final TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> plantResults = new TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>>();
		
		LinkedList<LocalComputeJob> wait = new LinkedList<LocalComputeJob>();
		
		if (imageSetWithSpecificAngle != null) {
			for (final Long time : imageSetWithSpecificAngle.keySet()) {
				if (status.wantsToStop())
					continue;
				LocalComputeJob analyze = new LocalComputeJob(
						() -> {
							new StreamBackgroundTaskHelper<String>("Analyze angles (top)").process(
									imageSetWithSpecificAngle.get(time).keySet().stream()
											.filter(s -> s.startsWith("1st_top")),
									(configAndAngle) -> {
										processAngle(status, workloadEqualAngleSnapshotSets, imageSetWithSpecificAngle,
												plantResults, time, configAndAngle);
									}, null);
									
							new StreamBackgroundTaskHelper<String>("Analyze angles (side)").process(
									imageSetWithSpecificAngle.get(time).keySet().stream()
											.filter(s -> !s.startsWith("1st_top")),
									(String configAndAngle) -> {
										processAngle(status, workloadEqualAngleSnapshotSets, imageSetWithSpecificAngle,
												plantResults, time, configAndAngle);
									}, null);
									
						}, "Analyze data from " + SystemAnalysis.getCurrentTime(time) + " (" + plantID + ")");
				boolean inTimeOrder = true;
				if (inTimeOrder)
					analyze.run();
				else
					wait.add(BackgroundThreadDispatcher.addTask(
							analyze, true));
			} // if image data available
		}
		BackgroundThreadDispatcher.waitFor(wait);
		if (!plantResults.isEmpty() && !status.wantsToStop()) {
			TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> postprocessingResults;
			try {
				synchronized (AbstractPhenotypingTask.class) {
					ImageProcessorOptionsAndResults options = new ImageProcessorOptionsAndResults(pd.getOptions(), null, plantResults);
					options.setUnitTestInfo(unit_test_idx, unit_test_steps);
					postprocessingResults = getImageProcessor()
							.postProcessPlantResults(
									plantID2time2waterData2,
									plantResults, status,
									options);
					addPostprocessingResults(postprocessingResults);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		tso.addInt(1);
		status.setCurrentStatusText1("Processing " + tso.getInt() + "/" + workloadSnapshots);
		Thread.currentThread().setName("Snapshot Analysis (" + plantID + ")");
	}
	
	private void processAngle(final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadEqualAngleSnapshotSets, final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle,
			final TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> plantResults,
			// final TreeMap<Long, TreeMap<String, ImageData>> analysisInput,
			final Long time, final String configAndAngle) {
		if (status.wantsToStop())
			return;
		try {
			TreeMap<String, HashMap<String, BlockResultSet>> previousResultsForThisTimePoint;
			synchronized (plantResults) {
				if (!plantResults.containsKey(time))
					plantResults.put(time, new TreeMap<String, HashMap<String, BlockResultSet>>());
				previousResultsForThisTimePoint = plantResults.get(time);
			}
			final ResultsAndWaitThreads resultsAndWaitThreads = processAngleWithinSnapshot(
					imageSetWithSpecificAngle.get(time).get(configAndAngle),
					status,
					workloadEqualAngleSnapshotSets,
					getParentPriority(), previousResultsForThisTimePoint, plantResults, configAndAngle);
			
			HashMap<String, BlockResultSet> results = resultsAndWaitThreads.getResults();
			processVolumeOutput(results);
			
			if (results != null) {
				synchronized (plantResults) {
					synchronized (plantResults.get(time)) {
						if (!plantResults.containsKey(time))
							plantResults.put(time, new TreeMap<String, HashMap<String, BlockResultSet>>());
						plantResults.get(time).put(configAndAngle, results);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return;
	}
	
	private void processVolumeOutput(HashMap<String, BlockResultSet> analysisResultsArray) {
		for (String well : analysisResultsArray.keySet()) {
			BlockResultSet analysisResults = analysisResultsArray.get(well);
			for (String volumeID : analysisResults.getVolumeNames()) {
				VolumeData v = analysisResults.getVolume(volumeID);
				if (v != null) {
					analysisResults.setVolume(volumeID, null);
					
					try {
						StopWatch s = new StopWatch(
								SystemAnalysis.getCurrentTime() + ">SAVE VOLUME");
						if (databaseTarget != null) {
							databaseTarget.saveVolume((LoadedVolume) v, (Sample3D) v.getParentSample(),
									m, null, null);
							VolumeData volumeInDatabase = new VolumeData(v.getParentSample(), v);
							volumeInDatabase.getURL().setPrefix(
									databaseTarget.getPrefix());
							volumeInDatabase.getURL().setDetail(
									v.getURL().getDetail());
							outputAdd(volumeInDatabase);
						} else {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">Volume kept in memory: " + v);
							outputAdd(v);
						}
						s.printTime();
					} catch (Exception e) {
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: Could not save volume data: "
								+ e.getMessage());
						e.printStackTrace();
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
		}
	}
	
	private void addTopOrSideImagesToWorkset(
			TreeMap<String, TreeMap<Long, TreeMap<String, ImageSet>>> workload_imageSetsWithSpecificAngles,
			int max,
			boolean top, boolean side) {
		TreeMap<String, TreeMap<String, ImageSet>> sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle = new TreeMap<String, TreeMap<String, ImageSet>>();
		if (input == null)
			return;
		for (Sample3D ins : input)
			for (Measurement md : ins) {
				if (md instanceof ImageData) {
					ImageData id = (ImageData) md;
					
					String sampleTimeAndFullPlantAnnotation = id.getParentSample().getSampleTime() + ";"
							+ id.getParentSample().getSampleFineTimeOrRowId() + ";"
							+ id.getParentSample().getFullId(true) + ";"
							+ id.getReplicateID();
					if (!sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.containsKey(sampleTimeAndFullPlantAnnotation)) {
						sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.put(sampleTimeAndFullPlantAnnotation,
								new TreeMap<String, ImageSet>());
					}
					ImageConfiguration imageConfiguration = ImageConfiguration.get(id
							.getSubstanceName());
					if (imageConfiguration == ImageConfiguration.Unknown) {
						imageConfiguration = ImageConfiguration.get(id.getURL().getFileName());
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">INFO: IMAGE CONFIGURATION UNKNOWN ("
								+ id.getSubstanceName() + "), "
								+ "GUESSING FROM IMAGE NAME: " + id.getURL()
								+ ", GUESS: " + imageConfiguration);
					}
					if (imageConfiguration == ImageConfiguration.Unknown) {
						System.out
								.println(SystemAnalysis.getCurrentTime()
										+ ">ERROR: INVALID (UNKNOWN) IMAGE CONFIGURATION FOR IMAGE: "
										+ id.getURL());
					} else {
						String imageConfigurationName = imageConfiguration.isSide() ? _2ND_SIDE : _1ST_TOP;
						
						String imageConfigurationAndRotationAngle = id.getPosition() != null ? imageConfigurationName + ";"
								+ id.getPosition() : imageConfigurationName + ";" + 0d;
						
						if (filterAngle) {
							double p = id.getPosition() != null ? id.getPosition() : 0d;
							int pi = (int) p;
							
							if (pi != DEBUG_SINGLE_ANGLE1 && pi != DEBUG_SINGLE_ANGLE2 && pi != DEBUG_SINGLE_ANGLE3) {
								continue;
							}
						}
						
						if (!sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).containsKey(
								imageConfigurationAndRotationAngle)) {
							sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).put(
									imageConfigurationAndRotationAngle, new ImageSet());
						}
						ImageSet is = sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).get(
								imageConfigurationAndRotationAngle);
						
						is.setIsSide(imageConfiguration.isSide());
						if ((imageConfiguration.isSide() && side) || (!imageConfiguration.isSide() && top)) {
							is.setImageInfo(imageConfiguration.getCameraType(), id);
						}
					}
				}
			}
		
		{
			// create list of replicates and plant IDs
			TreeSet<String> replicateIDandQualityList = new TreeSet<String>();
			for (TreeMap<String, ImageSet> is : sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.values()) {
				for (ImageSet i : is.values()) {
					if (i.getVisInfo() != null) {
						String val = i.getVisInfo().getReplicateID() + ";" + i.getVisInfo().getQualityAnnotation();
						replicateIDandQualityList.add(val);
					} else
						if (i.getFluoInfo() != null) {
							String val = i.getFluoInfo().getReplicateID() + ";" + i.getFluoInfo().getQualityAnnotation();
							replicateIDandQualityList.add(val);
						} else
							if (i.getNirInfo() != null) {
								String val = i.getNirInfo().getReplicateID() + ";" + i.getNirInfo().getQualityAnnotation();
								replicateIDandQualityList.add(val);
							} else
								if (i.getIrInfo() != null) {
									String val = i.getIrInfo().getReplicateID() + ";" + i.getIrInfo().getQualityAnnotation();
									replicateIDandQualityList.add(val);
								}
				}
			}
			HashMap<String, Integer> replicateIDandQualityList2positionIndex = new HashMap<String, Integer>();
			{
				int idx = 0;
				for (String val : replicateIDandQualityList) {
					replicateIDandQualityList2positionIndex.put(val, idx);
					idx++;
				}
			}
			// processed are specific replicates, which means each compute node computes
			// a specific plant in all time points, therefore it is possible for the block-post-processing
			// to process numeric analysis results from different time points and therefore directly
			// to calculate relative values, such as relative growth rates
			HashSet<String> knownOutput = new HashSet<String>();
			String timeInfo = SystemAnalysis.getCurrentTime();
			for (TreeMap<String, ImageSet> is : sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.values()) {
				if (is.size() == 0)
					continue;
				String val = null;
				if (is.firstEntry().getValue().getVisInfo() != null)
					val = is.firstEntry().getValue().getVisInfo().getReplicateID() + ";" +
							is.firstEntry().getValue().getVisInfo().getQualityAnnotation();
				else
					if (is.firstEntry().getValue().getFluoInfo() != null)
						val = is.firstEntry().getValue().getFluoInfo().getReplicateID() + ";" +
								is.firstEntry().getValue().getFluoInfo().getQualityAnnotation();
					else
						if (is.firstEntry().getValue().getNirInfo() != null)
							val = is.firstEntry().getValue().getNirInfo().getReplicateID() + ";" +
									is.firstEntry().getValue().getNirInfo().getQualityAnnotation();
						else
							if (is.firstEntry().getValue().getIrInfo() != null)
								val = is.firstEntry().getValue().getIrInfo().getReplicateID() + ";" +
										is.firstEntry().getValue().getIrInfo().getQualityAnnotation();
				if (val == null)
					continue;
				
				int workLoadIndex = replicateIDandQualityList2positionIndex.get(val);
				if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != workOnSubset)
					continue;
				String info = timeInfo + ">INFO: Processing image sets with ID: " + val;
				if (!knownOutput.contains(info))
					System.out.println(info);
				knownOutput.add(info);
				if (!workload_imageSetsWithSpecificAngles.containsKey(val))
					workload_imageSetsWithSpecificAngles.put(val, new TreeMap<Long, TreeMap<String, ImageSet>>());;
				Long time = null;
				for (ImageData id : new ImageData[] {
						is.firstEntry().getValue().getVisInfo(),
						is.firstEntry().getValue().getFluoInfo(),
						is.firstEntry().getValue().getNirInfo(),
						is.firstEntry().getValue().getIrInfo() }) {
					if (id == null)
						continue;
					time = id.getParentSample().getSampleFineTimeOrRowId();
					if (time == null)
						time = new Long(id.getParentSample().getTime());
					if (time != null)
						break;
				}
				if (time == null)
					continue;
				if (!workload_imageSetsWithSpecificAngles.get(val).containsKey(time))
					workload_imageSetsWithSpecificAngles.get(val).put(time, is);
			}
			System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: Processing "
					+ workload_imageSetsWithSpecificAngles.size() + " plants" +
					(numberOfSubsets > 0 ? " (subset " + workOnSubset + "/" + numberOfSubsets + ")"
							: "."));
		}
		
		if (max > 0)
			while (workload_imageSetsWithSpecificAngles.size() > max)
				workload_imageSetsWithSpecificAngles.remove(0);
	}
	
	protected abstract boolean analyzeTopImages();
	
	protected abstract boolean analyzeSideImages();
	
	public abstract ImageProcessor getImageProcessor() throws Exception;
	
	private void outputAdd(NumericMeasurementInterface meas) {
		// MappingData3DPath mp = new MappingData3DPath(meas, true);
		synchronized (output) {
			Substance3D.addAndMergeC(output, meas, false, false);// !(meas instanceof BinaryMeasurement));
		}
		// Substance3D.addAndMerge(output, mp.getSubstance(), false);
	}
	
	@Override
	public ExperimentInterface getOutput() {
		ExperimentInterface result = output;
		if (result != null) {
			if (result.isEmpty())
				for (SubstanceInterface si : result) {
					if (si == null)
						continue;
					boolean onlyImages = true;
					for (ConditionInterface ci : si) {
						if (ci != null && !ci.isEmpty())
							for (SampleInterface sai : ci) {
								if (sai != null) {
									for (NumericMeasurementInterface nmi : sai) {
										if (nmi != null)
											if (!(nmi instanceof BinaryMeasurement))
												onlyImages = false;
									}
									sai.setSampleAverage(null);
									sai.recalculateSampleAverage(false);
								}
							}
					}
					if (!onlyImages)
						si.setInfo(null);
				}
		}
		output = null;
		ImageOperation.setLabCubeInstanceToNull();
		return result;
	}
	
	private void processResults(HashMap<String, BlockResultSet> tray2analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 1");
		}
		
		boolean multiTray = tray2analysisResults.keySet().size() > 1;
		for (String tray : tray2analysisResults.keySet()) {
			for (BlockResultValue bpv : tray2analysisResults.get(tray).searchResults("RESULT_")) {
				if (bpv == null || bpv.getName() == null)
					continue;
				
				if (bpv.getObject() != null && bpv.getObject() instanceof ImageAndImageData) {
					outputAdd(((ImageAndImageData) bpv.getObject()).getImageData());
				} else {
					if (bpv.getBinary() == null) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: Invalid result with no reference information: " + bpv.getName());
					} else {
						NumericMeasurement3D m = new NumericMeasurement3D(bpv.getBinary(), bpv.getName(), null);
						m.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
						m.setAnnotation(null);
						m.setValue(bpv.getValue());
						m.setUnit(bpv.getUnit());
						if (multiTray)
							m.setQualityAnnotation(m.getQualityAnnotation() + "_" + tray);
						
						outputAdd(m);
					}
				}
			}
		}
	}
	
	private void addPostprocessingResults(TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 2");
		}
		
		for (Long time : analysisResults.keySet())
			for (String configName : analysisResults.get(time).keySet()) {
				int trays = analysisResults.get(time).get(configName).keySet().size();
				boolean multiTray = trays > 1;
				
				for (String tray : analysisResults.get(time).get(configName).keySet()) {
					// boolean multipleTrays = analysisResults.get(time).get(configName).keySet().size() > 1;
					for (String volumeID : analysisResults.get(time).get(configName).get(tray).getVolumeNames()) {
						VolumeData v = analysisResults.get(time).get(configName).get(tray).getVolume(volumeID);
						if (v != null) {
							synchronized (analysisResults.get(time)) {
								analysisResults.get(time).get(configName).get(tray).setVolume(volumeID, null);
							}
							
							try {
								StopWatch s = new StopWatch(
										SystemAnalysis.getCurrentTime() + ">SAVE VOLUME");
								if (databaseTarget != null) {
									SampleInterface oSample = v.getParentSample();
									ConditionInterface oCond = v.getParentSample().getParentCondition();
									SubstanceInterface oSubst = v.getParentSample().getParentCondition().getParentSubstance();
									SubstanceInterface nSubst = oSubst.clone();
									ConditionInterface nCond = oCond.clone(nSubst);
									nSubst.setInfo(null); // remove information about source camera
									nSubst.add(nCond);
									SampleInterface nSamp = oSample.clone(nCond);
									v.setParentSample(nSamp);
									v.getParentSample().getParentCondition().getParentSubstance().setName("volume");
									
									databaseTarget.saveVolume((LoadedVolume) v, (Sample3D) v.getParentSample(),
											m, null, null);
									VolumeData volumeInDatabase = new VolumeData(v.getParentSample(), v);
									volumeInDatabase.getURL().setPrefix(
											databaseTarget.getPrefix());
									volumeInDatabase.getURL().setDetail(v.getURL().getDetail());
									outputAdd(volumeInDatabase);
								} else {
									System.out.println(SystemAnalysis.getCurrentTime()
											+ ">Volume kept in memory: " + v);
									outputAdd(v);
								}
								s.printTime();
							} catch (Exception e) {
								System.out.println(SystemAnalysis.getCurrentTime()
										+ ">ERROR: Could not save volume data: "
										+ e.getMessage());
								ErrorMsg.addErrorMessage(e);
							}
						}
					}
					synchronized (analysisResults.get(time)) {
						for (BlockResultValue bpv : analysisResults.get(time).get(configName).get(tray).searchResults("RESULT_")) {
							if (bpv == null || bpv.getName() == null)
								continue;
							String name = bpv.getName();
							if (name.contains("_cut.")) {
								name = name.substring(0, name.indexOf("_cut."));
							}
							
							try {
								NumericMeasurement3D m = new NumericMeasurement3D(
										bpv.getBinary(), name,
										bpv.getBinary().getParentSample().getParentCondition().getExperimentName()
												+ " ("
												+ getName() + ")");
								
								m.setValue(bpv.getValue());
								m.setUnit(bpv.getUnit());
								
								m.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
								
								if (multiTray)
									m.setQualityAnnotation(m.getQualityAnnotation() + "_" + tray);
								
								outputAdd(m);
							} catch (Exception err) {
								System.out.println("ERROR: " + name);
								err.printStackTrace();
							}
						}
					}
				}
			}
	}
	
	@Override
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps) {
		this.unit_test_idx = unit_test_idx;
		this.unit_test_steps = unit_test_steps;
	}
	
	private ResultsAndWaitThreads processAngleWithinSnapshot(final ImageSet id,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles, int parentPriority,
			final TreeMap<String, HashMap<String, BlockResultSet>> previousResultsForThisTimePoint,
			final TreeMap<Long, TreeMap<String, HashMap<String, BlockResultSet>>> plantResults, final String configAndAngle)
			throws Exception {
		// ArrayList<LocalComputeJob> waitThreads = new ArrayList<LocalComputeJob>();
		final ImageData inVis = id.getVisInfo() != null ? id.getVisInfo().copy() : null;
		final ImageData inFluo = id.getFluoInfo() != null ? id.getFluoInfo().copy() : null;
		final ImageData inNir = id.getNirInfo() != null ? id.getNirInfo().copy() : null;
		final ImageData inIr = id.getIrInfo() != null ? id.getIrInfo().copy() : null;
		
		if (inVis == null && inFluo == null && inNir == null && inIr == null) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">ERROR: SNAPSHOT WITH NO VIS+FLUO+NIR+IR IMAGES");
			return null;
		}
		
		MeasurementFilter mf = IAPservice.getMeasurementFilter(id.getAnyInfo().getParentSample().getParentCondition().getExperimentHeader());
		if (mf.isGlobalOutlierOrSpecificOutlier(inVis) || mf.isGlobalOutlierOrSpecificOutlier(inFluo) || mf.isGlobalOutlierOrSpecificOutlier(inNir) ||
				mf.isGlobalOutlierOrSpecificOutlier(inIr)) {
			// don't analyze snapshot if any input image type is marked as an outlier
			return null;
		}
		
		OptionsGenerator og = new OptionsGenerator1(this, inIr, id, plantResults, inFluo, inNir, configAndAngle, inVis, previousResultsForThisTimePoint);
		
		HashMap<String, BlockResultSet> well2analysisResults = null;
		
		ImageProcessor imageProcessor = getImageProcessor();
		{
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = getStatusProcessor(status, workloadSnapshotAngles);
			imageProcessor.setStatus(statusForThisTask);
			imageProcessor.setValidTrays(debugValidTrays);
			imageProcessor.execute(og);
			
			well2analysisResults = imageProcessor.getNumericResults();
		}
		
		synchronized (this) {
			if (well2analysisResults != null) {
				processResults(well2analysisResults);
			}
		}
		return new ResultsAndWaitThreads(well2analysisResults, new ArrayList<LocalComputeJob>());
	}
	
	private BackgroundTaskStatusProviderSupportingExternalCallImpl getStatusProcessor(final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles) {
		return new BackgroundTaskStatusProviderSupportingExternalCallImpl(
				"", "") {
			double lastAdd = 0;
			
			@Override
			public synchronized void setCurrentStatusValueFine(double value) {
				super.setCurrentStatusValueFine(value);
				if (value > 0) {
					double add = value / workloadSnapshotAngles;
					status.setCurrentStatusValueFineAdd(add - lastAdd);
					lastAdd = add;
				}
			}
			
			@Override
			public boolean wantsToStop() {
				return status.wantsToStop();
			}
			
			@Override
			public void pleaseStop() {
				status.pleaseStop();
			}
			
			@Override
			public boolean pluginWaitsForUser() {
				return status.pluginWaitsForUser();
			}
			
			@Override
			public void pleaseContinueRun() {
				status.pleaseContinueRun();
			}
			
			@Override
			public void setPluginWaitsForUser(boolean wait) {
				// not yet implemented
			}
			
			@Override
			public synchronized void setCurrentStatusValueFineAdd(double smallProgressStep) {
				super.setCurrentStatusValueFineAdd(smallProgressStep);
				if (smallProgressStep > 0) {
					double add = smallProgressStep / workloadSnapshotAngles;
					status.setCurrentStatusValueFineAdd(add);
				}
			}
			
			@Override
			public void setCurrentStatusText1(String s1) {
				status.setCurrentStatusText1(s1);
			}
			
			@Override
			public void setCurrentStatusText2(String s2) {
				status.setCurrentStatusText2(s2);
			}
			
			@Override
			public String getCurrentStatusMessage1() {
				return status.getCurrentStatusMessage1();
			}
			
			@Override
			public String getCurrentStatusMessage2() {
				return status.getCurrentStatusMessage2();
			}
			
			@Override
			public String getCurrentStatusMessage3() {
				return status.getCurrentStatusMessage3();
			}
			
			@Override
			public void setCurrentStatusValue(int value) {
				setCurrentStatusValueFine(value);
			}
			
		};
	}
	
	private void printError(final TreeMap<Long, TreeMap<String, ImageSet>> tmf, Exception err) {
		System.err.println(SystemAnalysis
				.getCurrentTime()
				+ ">ERROR: "
				+ err.getMessage());
		// System.err.println(SystemAnalysisExt
		// .getCurrentTime()
		// + "> ERROR-CAUSE: SAMPLE: "
		// + tmf.firstKey()
		// + " / "
		// + tmf.get(tmf.firstKey())
		// .getSampleInfo()
		// + " / "
		// + tmf.get(tmf.firstKey())
		// .getSampleInfo()
		// .getParentCondition());
		err.printStackTrace();
	}
	
	public static TreeMap<String, TreeMap<Long, Double>> getWateringInfo(ExperimentInterface experiment) {
		TreeMap<String, TreeMap<Long, Double>> result = new TreeMap<String, TreeMap<Long, Double>>();
		if (experiment != null) {
			for (SubstanceInterface si : experiment) {
				if (si.getName().equals("water_sum")) {
					for (ConditionInterface ci : si) {
						for (SampleInterface sa : ci) {
							long time = sa.getSampleFineTimeOrRowId();
							for (NumericMeasurementInterface nmi : sa) {
								String plantID = nmi.getReplicateID() + ";" + nmi.getQualityAnnotation();
								if (!result.containsKey(plantID))
									result.put(plantID, new TreeMap<Long, Double>());
								if (result.get(plantID).containsKey(time))
									result.get(plantID).put(time,
											result.get(plantID).get(time) +
													nmi.getValue());
								else
									result.get(plantID).put(time, nmi.getValue());
							}
							
						}
					}
				}
			}
		}
		return result;
	}
	
	public void debugSetValidTrays(int[] debugValidTrays) {
		this.debugValidTrays = debugValidTrays;
	}
	
	public int[] debugGetValidTrays() {
		return debugValidTrays;
	}
	
	public String getDebugLastSystemOptionStorageGroup() {
		return debugLastSystemOptionStorageGroup;
	}
	
	public void setDebugLastSystemOptionStorageGroup(String debugLastSystemOptionStorageGroup) {
		this.debugLastSystemOptionStorageGroup = debugLastSystemOptionStorageGroup;
	}
	
	@Override
	public void setValidSideAngle(int DEBUG_SINGLE_ANGLE1, int DEBUG_SINGLE_ANGLE2, int DEBUG_SINGLE_ANGLE3) {
		this.DEBUG_SINGLE_ANGLE1 = DEBUG_SINGLE_ANGLE1;
		this.DEBUG_SINGLE_ANGLE2 = DEBUG_SINGLE_ANGLE2;
		this.DEBUG_SINGLE_ANGLE3 = DEBUG_SINGLE_ANGLE3;
		this.filterAngle = true;
	}
	
}
