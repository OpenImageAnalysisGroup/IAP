package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import iap.blocks.postprocessing.WellProcessing;
import iap.pipelines.ImageProcessor;
import iap.pipelines.ImageProcessorOptionsAndResults;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import info.StopWatch;

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
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockResultValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
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

public abstract class AbstractPhenotypingTask implements ImageAnalysisTask {
	
	public static final String _1ST_TOP = "1st_top";
	public static final String _2ND_SIDE = "2nd_side";
	private Collection<Sample3D> input = new ArrayList<Sample3D>();
	private ExperimentInterface output = new Experiment();
	
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	private boolean forceDebugStack;
	private ArrayList<ImageStack> forcedDebugStacks;
	private int prio;
	private MongoDB m;
	private TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData;
	private int unit_test_idx;
	private int unit_test_steps;
	private int[] debugValidTrays;
	private final PipelineDesc pd;
	private String debugLastSystemOptionStorageGroup;
	private int DEBUG_SINGLE_ANGLE;
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
	
	@Override
	public void setInput(
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
			databaseTarget = determineDatabaseTarget(input, m);
		} catch (Exception e) {
			e.printStackTrace();
			MongoDB.saveSystemErrorMessage("Could not initialize storage target: " + e.getMessage(), e);
		}
	}
	
	public static DatabaseTarget determineDatabaseTarget(Collection<Sample3D> input, MongoDB m) throws Exception {
		DatabaseTarget databaseTarget = m != null ? new DataBaseTargetMongoDB(true, m, m.getColls()) : null;
		if (databaseTarget != null)
			return databaseTarget;
		String prefix = null;
		if (input != null && !input.isEmpty()) {
			prefix = input.iterator().next().getParentCondition().getExperimentDatabaseId().split(":")[0];
			if (!prefix.startsWith("mongo_")) {
				databaseTarget = null;
			}
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
	public void performAnalysis(final int maximumThreadCountParallelImages,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status)
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
			final TreeMap<String, TreeMap<Long, TreeMap<String, ImageSet>>> workload_imageSetsWithSpecificAngles =
					new TreeMap<String, TreeMap<Long, TreeMap<String, ImageSet>>>();
			
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
			
			for (String plantID : workload_imageSetsWithSpecificAngles.keySet()) {
				if (status.wantsToStop())
					continue;
				final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle_f = workload_imageSetsWithSpecificAngles.get(plantID);
				final String plantIDf = plantID;
				try {
					progress++;
					final String preThreadName = "Snapshot Analysis (" + progress + "/" + numberOfPlants + ", plant " + plantID + ")";
					status.setCurrentStatusText1(preThreadName);
					Runnable t = new Runnable() {
						@Override
						public void run() {
							try {
								processPlant(
										plandID2time2waterData,
										plantIDf, preThreadName,
										maximumThreadCountOnImageLevel,
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
					Thread.sleep(50);
				} catch (Exception eeee) {
					throw new RuntimeException(eeee);
				}
			}
			status.setCurrentStatusText1("Enqueue Analysis Tasks");
			ArrayList<LocalComputeJob> wait = new ArrayList<LocalComputeJob>();
			final int todo = workLoad.size();
			final ThreadSafeOptions progr = new ThreadSafeOptions();
			while (!workLoad.isEmpty() && !status.wantsToStop()) {
				Runnable t = workLoad.poll();
				String d = workLoad_desc.poll();
				wait.add(BackgroundThreadDispatcher.addTask(t, d, false));
				progr.addInt(1);
				String plantName = d;
				if (plantName.contains(";"))
					plantName = plantName.split(";", 2)[1];
				do {
					Thread.sleep(100);
					status.setCurrentStatusText1("Enqueued " + progr.getInt() + "/" + todo + " plants ("
							+ BackgroundThreadDispatcher.getWorkLoad() + " tasks, "
							+ (BackgroundThreadDispatcher.getBackgroundThreadCount() + 1) + " threads)");
				} while (BackgroundThreadDispatcher.getWorkLoad() >= SystemAnalysis.getNumberOfCPUs());
			}
			BackgroundThreadDispatcher.waitFor(wait, new Runnable() {
				@Override
				public void run() {
					while (!Thread.interrupted()) {
						status.setCurrentStatusText1("Enqueued " + progr.getInt() + "/" + todo
								+ " plants (" + BackgroundThreadDispatcher.getWorkLoad() + " tasks, "
								+ (BackgroundThreadDispatcher.getBackgroundThreadCount() + 1) + " threads)");
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
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final ThreadSafeOptions tso, final int workloadSnapshots,
			final int workloadEqualAngleSnapshotSets,
			final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle)
			throws InterruptedException {
		
		final TreeMap<Long, Sample3D> inSamples = new TreeMap<Long, Sample3D>();
		final TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> plantResults = new TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>>();
		final TreeMap<Long, TreeMap<String, ImageData>> analysisInput = new TreeMap<Long, TreeMap<String, ImageData>>();
		
		if (imageSetWithSpecificAngle != null) {
			for (final Long time : imageSetWithSpecificAngle.keySet()) {
				if (status.wantsToStop())
					continue;
				
				LinkedList<LocalComputeJob> wait = new LinkedList<LocalComputeJob>();
				for (final String configAndAngle : imageSetWithSpecificAngle.get(time).keySet()) {
					if (configAndAngle.startsWith("1st_top"))
						wait.add(BackgroundThreadDispatcher.addTask(new Runnable() {
							@Override
							public void run() {
								processAngle(maximumThreadCountOnImageLevel, status, workloadEqualAngleSnapshotSets, imageSetWithSpecificAngle,
										inSamples, plantResults, analysisInput, time, configAndAngle);
							}
						}, "Analyze angle (top) " + configAndAngle));
				} // for top angle
				BackgroundThreadDispatcher.waitFor(wait);
				wait.clear();
				for (final String configAndAngle : imageSetWithSpecificAngle.get(time).keySet()) {
					if (status.wantsToStop())
						continue;
					
					if (!configAndAngle.startsWith("1st_top"))
						wait.add(BackgroundThreadDispatcher.addTask(new Runnable() {
							@Override
							public void run() {
								processAngle(maximumThreadCountOnImageLevel, status, workloadEqualAngleSnapshotSets, imageSetWithSpecificAngle,
										inSamples, plantResults, analysisInput, time, configAndAngle);
							}
						}, "Analyze angle (side) " + configAndAngle));
				} // for side angle
				BackgroundThreadDispatcher.waitFor(wait);
				wait.clear();
			} // for each time point
		} // if image data available
		
		if (!plantResults.isEmpty() && !status.wantsToStop()) {
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> postprocessingResults;
			try {
				synchronized (AbstractPhenotypingTask.class) {
					ImageProcessorOptionsAndResults options = new ImageProcessorOptionsAndResults(pd.getOptions(), null, plantResults);
					options.setUnitTestInfo(unit_test_idx, unit_test_steps);
					postprocessingResults = getImageProcessor()
							.postProcessPlantResults(
									plantID2time2waterData2,
									inSamples, analysisInput,
									plantResults, status,
									options);
					addPostprocessingResults(inSamples, postprocessingResults);
				}
			} catch (Exception e) {
				e.printStackTrace();
				ErrorMsg.addErrorMessage(e);
			}
		}
		tso.addInt(1);
		status.setCurrentStatusText1("Processing " + tso.getInt() + "/" + workloadSnapshots);
		Thread.currentThread().setName("Snapshot Analysis (" + plantID + ")");
	}
	
	private void processAngle(final int maximumThreadCountOnImageLevel, final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadEqualAngleSnapshotSets, final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle,
			final TreeMap<Long, Sample3D> inSamples, final TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> plantResults,
			final TreeMap<Long, TreeMap<String, ImageData>> analysisInput, final Long time, final String configAndAngle) {
		if (status.wantsToStop())
			return;
		try {
			if (imageSetWithSpecificAngle.get(time).get(configAndAngle) != null &&
					imageSetWithSpecificAngle.get(time).get(configAndAngle).getAnyInfo() != null) {
				Sample3D inSample = (Sample3D) imageSetWithSpecificAngle.get(time).get(configAndAngle).getAnyInfo()
						.getParentSample();
				if (inSample != null) {
					synchronized (inSamples) {
						inSamples.put(time, inSample);
					}
				}
			} else
				return;
			final ImageData inImage = imageSetWithSpecificAngle.get(time).get(configAndAngle).getAnyInfo();
			
			TreeMap<String, HashMap<Integer, BlockResultSet>> previousResultsForThisTimePoint;
			synchronized (plantResults) {
				if (!plantResults.containsKey(time))
					plantResults.put(time, new TreeMap<String, HashMap<Integer, BlockResultSet>>());
				previousResultsForThisTimePoint = plantResults.get(time);
			}
			final ResultsAndWaitThreads resultsAndWaitThreads = processAngleWithinSnapshot(
					imageSetWithSpecificAngle.get(time).get(configAndAngle),
					maximumThreadCountOnImageLevel, status,
					workloadEqualAngleSnapshotSets,
					getParentPriority(), previousResultsForThisTimePoint, plantResults, configAndAngle);
			
			HashMap<Integer, BlockResultSet> results = resultsAndWaitThreads.getResults();
			synchronized (inSamples) {
				processVolumeOutput(inSamples.get(time), results);
			}
			if (results != null) {
				synchronized (analysisInput) {
					if (!analysisInput.containsKey(time))
						analysisInput.put(time, new TreeMap<String, ImageData>());
					synchronized (plantResults) {
						synchronized (plantResults.get(time)) {
							if (!plantResults.containsKey(time))
								plantResults.put(time, new TreeMap<String, HashMap<Integer, BlockResultSet>>());
							analysisInput.get(time).put(configAndAngle, inImage);
							plantResults.get(time).put(configAndAngle, results);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		return;
	}
	
	private void processVolumeOutput(Sample3D inSample, HashMap<Integer, BlockResultSet> analysisResultsArray) {
		for (Integer key : analysisResultsArray.keySet()) {
			BlockResultSet analysisResults = analysisResultsArray.get(key);
			for (String volumeID : analysisResults.getVolumeNames()) {
				VolumeData v = analysisResults.getVolume(volumeID);
				if (v != null) {
					analysisResults.setVolume(volumeID, null);
					
					try {
						StopWatch s = new StopWatch(
								SystemAnalysis.getCurrentTime() + ">SAVE VOLUME");
						if (databaseTarget != null) {
							databaseTarget.saveVolume((LoadedVolume) v, inSample,
									m, null, null);
							VolumeData volumeInDatabase = new VolumeData(inSample,
									v);
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
		TreeMap<String, TreeMap<String, ImageSet>> sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle =
				new TreeMap<String, TreeMap<String, ImageSet>>();
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
							
							if (pi != DEBUG_SINGLE_ANGLE) {
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
			System.out.println(SystemAnalysis.getCurrentTime() + ">Processing "
					+ workload_imageSetsWithSpecificAngles.size() + " plants" +
					(numberOfSubsets > 0 ?
							" (subset " + workOnSubset + "/" + numberOfSubsets + ")"
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
		Substance3D.addAndMerge(output, meas, false, !(meas instanceof BinaryMeasurement));
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
	
	private void processResults(ImageData copyFrom, HashMap<Integer, BlockResultSet> tray2analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 1");
		}
		
		boolean multiTray = tray2analysisResults.keySet().size() > 1;
		for (Integer tray : tray2analysisResults.keySet()) {
			for (BlockResultValue bpv : tray2analysisResults.get(tray).searchResults("RESULT_")) {
				if (bpv.getName() == null)
					continue;
				
				if (bpv.getBinary() != null) {
					ImageData id = ((ImageData) bpv.getBinary());
					
					// System.out.println("d/e/f=" + " // " + bpv.getName() + " // " + copyFrom.getSubstanceName() + " // "
					// + id.getURL().getFileName() + " // " + id.getSubstanceName());
					outputAdd(bpv.getBinary());
				} else {
					NumericMeasurement3D m = new NumericMeasurement3D(copyFrom, bpv.getName(), null);
					// m.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
					m.setAnnotation(null);
					m.setValue(bpv.getValue());
					m.setUnit(bpv.getUnit());
					if (multiTray)
						m.setQualityAnnotation(m.getQualityAnnotation() + "_" + WellProcessing.getWellID(tray, tray2analysisResults.keySet().size(), m));
					
					outputAdd(m);
				}
			}
		}
	}
	
	private void addPostprocessingResults(
			TreeMap<Long, Sample3D> inSamples,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 2");
		}
		
		for (Long time : analysisResults.keySet())
			for (String configName : analysisResults.get(time).keySet()) {
				for (Integer tray : analysisResults.get(time).get(configName).keySet()) {
					boolean multipleTrays = analysisResults.get(time).get(configName).keySet().size() > 1;
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
							if (bpv.getName() == null)
								continue;
							String name = bpv.getName();
							if (name.contains("_cut.")) {
								name = name.substring(0, name.indexOf("_cut."));
							}
							
							NumericMeasurement3D m = new NumericMeasurement3D(
									new NumericMeasurement(inSamples.get(time)), name, inSamples.get(time)
											.getParentCondition().getExperimentName()
											+ " ("
											+ getName() + ")");
							
							if (bpv != null && m != null) {
								m.setValue(bpv.getValue());
								m.setUnit(bpv.getUnit());
								if (inSamples.get(time).size() > 0) {
									NumericMeasurement3D template = (NumericMeasurement3D) inSamples.get(time).iterator().next();
									m.setReplicateID(template.getReplicateID());
									m.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
									m.setQualityAnnotation(template.getQualityAnnotation() + (multipleTrays ? "_" + tray : ""));
									// rotation angle needs to be determined from the config-name
									if (bpv.getPosition() != null)
										m.setPosition(bpv.getPosition());
									else {
										String angle = configName;
										if (angle != null && angle.contains(";"))
											angle = angle.split(";")[1];
										try {
											double ra = Double.parseDouble(angle);
											m.setPosition(ra);
										} catch (Exception e) {
											System.out
													.println(SystemAnalysis.getCurrentTime() + ">WARNING: Can't determine rotation angle from config '" + configName + "'!");
											m.setPosition(null);// template.getPosition());
										}
									}
									m.setPositionUnit(template.getPositionUnit());
								}
								outputAdd(m);
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
	
	private ResultsAndWaitThreads processAngleWithinSnapshot(ImageSet id,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles, int parentPriority,
			TreeMap<String, HashMap<Integer, BlockResultSet>> previousResultsForThisTimePoint,
			TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> plantResults, String configAndAngle)
			throws Exception {
		// ArrayList<LocalComputeJob> waitThreads = new ArrayList<LocalComputeJob>();
		ImageData inVis = id.getVisInfo() != null ? id.getVisInfo().copy() : null;
		ImageData inFluo = id.getFluoInfo() != null ? id.getFluoInfo().copy() : null;
		ImageData inNir = id.getNirInfo() != null ? id.getNirInfo().copy() : null;
		ImageData inIr = id.getIrInfo() != null ? id.getIrInfo().copy() : null;
		
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
		
		final ImageSet input = new ImageSet();
		final ImageSet inputMasks = new ImageSet();
		
		input.setImageInfo(inVis, inFluo, inNir, inIr);
		inputMasks.setImageInfo(inVis, inFluo, inNir, inIr);
		
		ImageProcessorOptionsAndResults options = new ImageProcessorOptionsAndResults(pd.getOptions(), previousResultsForThisTimePoint, plantResults);
		options.setConfigAndAngle(configAndAngle);
		options.setUnitTestInfo(unit_test_idx, unit_test_steps);
		
		options.forceDebugStack = forceDebugStack;
		options.forcedDebugStacks = forcedDebugStacks;
		
		options.databaseTarget = databaseTarget;
		options.setCustomNullBlockPrefix("Separate Settings");
		
		{
			boolean processEarlyTimes = options.getBooleanSetting(null, "Early//Custom settings for early timepoints", false);
			boolean processLateTimes = options.getBooleanSetting(null, "Late//Custom settings for late timepoints", false);
			int earlyTimeUntilDayX = options.getIntSetting(null, "Early//Early time until time point", -1);
			int lateTimeUntilDayX = options.getIntSetting(null, "Late//Late time until time point", -1);
			String timeInfo = null;
			if (processEarlyTimes && id.getAnyInfo().getParentSample().getTime() <= earlyTimeUntilDayX)
				timeInfo = "early";
			else
				if (processLateTimes && id.getAnyInfo().getParentSample().getTime() >= lateTimeUntilDayX)
					timeInfo = "late";
			
			String info = id.getAnyInfo().getParentSample().getParentCondition().getParentSubstance().getInfo();
			if (id.isSideImage())
				options.setCameraInfos(CameraPosition.SIDE,
						info != null && options.getBooleanSetting(null, info + "//Custom settings", false) ? info : null, timeInfo, id.getAnyInfo().getPosition());
			else
				options.setCameraInfos(CameraPosition.TOP,
						info != null && options.getBooleanSetting(null, info + "//Custom settings", false) ? info : null, timeInfo, id.getAnyInfo().getPosition());
			options.setCustomNullBlockPrefix(null);
		}
		
		if (forceDebugStack) {
			this.setDebugLastSystemOptionStorageGroup(options.getSystemOptionStorageGroup(null));
		}
		
		HashMap<Integer, BlockResultSet> well2analysisResults = null;
		
		ImageProcessor imageProcessor = getImageProcessor();
		{
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = getStatusProcessor(status, workloadSnapshotAngles);
			imageProcessor.setStatus(statusForThisTask);
			imageProcessor.setValidTrays(debugValidTrays);
			imageProcessor.execute(options, input, inputMasks, maximumThreadCountOnImageLevel);
			
			well2analysisResults = imageProcessor.getNumericResults();
		}
		
		if (well2analysisResults != null)
			processResults(input.getAnyInfo(), well2analysisResults);
		
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
				+ "> ERROR: "
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
	
	public String getDebugLastSystemOptionStorageGroup() {
		return debugLastSystemOptionStorageGroup;
	}
	
	public void setDebugLastSystemOptionStorageGroup(String debugLastSystemOptionStorageGroup) {
		this.debugLastSystemOptionStorageGroup = debugLastSystemOptionStorageGroup;
	}
	
	@Override
	public void setValidSideAngle(int DEBUG_SINGLE_ANGLE) {
		this.DEBUG_SINGLE_ANGLE = DEBUG_SINGLE_ANGLE;
		this.filterAngle = true;
	}
}
