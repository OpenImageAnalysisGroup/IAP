package de.ipk.ag_ba.server.analysis.image_analysis_tasks.all;

import iap.pipelines.ImageProcessor;
import iap.pipelines.ImageProcessorOptions;
import iap.pipelines.ImageProcessorOptions.CameraPosition;
import iap.pipelines.StringAndFlexibleMaskAndImageSet;
import info.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOHandler;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemHandler;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.PipelineDesc;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.datastructures.LoadedImageStream;
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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Substance3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
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
			
			int nn = SystemAnalysis.getNumberOfCPUs();
			// nn = modifyConcurrencyDependingOnMemoryStatus(nn);
			
			// final Semaphore maxCon = BackgroundTaskHelper.lockGetSemaphore(null, nn);
			final ThreadSafeOptions freed = new ThreadSafeOptions();
			
			int numberOfPlants = workload_imageSetsWithSpecificAngles.keySet().size();
			int progress = 0;
			ArrayList<LocalComputeJob> wait = new ArrayList<LocalComputeJob>();
			for (String plantID : workload_imageSetsWithSpecificAngles.keySet()) {
				if (status.wantsToStop())
					continue;
				final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle_f = workload_imageSetsWithSpecificAngles.get(plantID);
				final String plantIDf = plantID;
				// maxCon.acquire(1);
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
										imageSetWithSpecificAngle_f);// , maxCon);
							} catch (Exception err) {
								printError(imageSetWithSpecificAngle_f, err);
							} finally {
								// maxCon.release(1);
								freed.setBval(0, true);
							}
						}
					};
					// if (imageSetWithSpecificAngle_f.keySet().size() > SystemAnalysis.getNumberOfCPUs()
					// && SystemOptions.getInstance().getBoolean("IAP", "Process Plants Sequentially", true))
					// t.run();
					// else {
					wait.add(BackgroundThreadDispatcher.addTask(t, preThreadName, true));
					Thread.sleep(50);
					// }
					
				} catch (Exception eeee) {
					// if (!freed.getBval(0, false))
					// maxCon.release(1);
					throw new RuntimeException(eeee);
				}
			}
			BackgroundThreadDispatcher.waitFor(wait);
			// maxCon.acquire(nn);
			// maxCon.release(nn);
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
			final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle) // , final Semaphore optMaxCon)
			throws InterruptedException {
		
		final TreeMap<Long, Sample3D> inSamples = new TreeMap<Long, Sample3D>();
		final TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> plantResults = new TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>>();
		final TreeMap<Long, TreeMap<String, ImageData>> analysisInput = new TreeMap<Long, TreeMap<String, ImageData>>();
		final ArrayList<LocalComputeJob> waitThreads = new ArrayList<LocalComputeJob>();
		if (imageSetWithSpecificAngle != null) {
			for (final Long time : imageSetWithSpecificAngle.keySet()) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						for (final String configAndAngle : imageSetWithSpecificAngle.get(time).keySet()) {
							if (status.wantsToStop())
								continue;
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
								continue;
							final ImageData inImage = imageSetWithSpecificAngle.get(time).get(configAndAngle).getAnyInfo();
							
							try {
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
										getParentPriority(), previousResultsForThisTimePoint);
								
								// synchronized (waitThreads) {
								// waitThreads.addAll(resultsAndWaitThreads.getWaitThreads());
								// }
								// Runnable waitResults = new Runnable() {
								// @Override
								// public void run() {
								HashMap<Integer, BlockResultSet> results = resultsAndWaitThreads.getResults();
								synchronized (inSamples) {
									processVolumeOutput(inSamples.get(time), results);
								}
								if (results != null) {
									synchronized (analysisInput) {
										if (!analysisInput.containsKey(time))
											analysisInput.put(time, new TreeMap<String, ImageData>());
										synchronized (plantResults) {
											if (!plantResults.containsKey(time))
												plantResults.put(time, new TreeMap<String, HashMap<Integer, BlockResultSet>>());
											analysisInput.get(time).put(configAndAngle, inImage);
											plantResults.get(time).put(configAndAngle, results);
										}
									}
								}
								// }
								// };
								// waitResults.run();
								// BackgroundThreadDispatcher.addTask(waitResults, "process results of specific angle analysis").getResult();
								// waitThreads.add(BackgroundThreadDispatcher.addTask(waitResults, "process results of specific angle analysis"));
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
							}
						}
					} // for side angle
				};
				
				waitThreads.add(BackgroundThreadDispatcher.addTask(r, "Inner thread " + preThreadName + ", snapshot time "
						+ SystemAnalysis.getCurrentTime(time) + ""));
			} // for each time point
		} // if image data available
		BackgroundThreadDispatcher.waitFor(waitThreads);
		if (!plantResults.isEmpty()) {
			TreeMap<Long, HashMap<Integer, BlockResultSet>> postprocessingResults;
			try {
				synchronized (AbstractPhenotypingTask.class) {
					ImageProcessorOptions options = new ImageProcessorOptions(pd.getOptions(), null);
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
				ErrorMsg.addErrorMessage(e);
			}
		}
		tso.addInt(1);
		status.setCurrentStatusText1("Processing " + tso.getInt() + "/" + workloadSnapshots);
		Thread.currentThread().setName("Snapshot Analysis (" + plantID + ")");
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
							+ id.getParentSample().getFullId() + ";"
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
						
						if (!sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).containsKey(
								imageConfigurationAndRotationAngle)) {
							sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).put(
									imageConfigurationAndRotationAngle, new ImageSet());
						}
						ImageSet is = sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).get(
								imageConfigurationAndRotationAngle);
						
						is.setIsSide(imageConfiguration.isSide());
						if ((imageConfiguration.isSide() && side) || (!imageConfiguration.isSide() && top)) {
							if (imageConfiguration == ImageConfiguration.VisSide
									|| imageConfiguration == ImageConfiguration.VisTop)
								is.setVisInfo(id);
							if (imageConfiguration == ImageConfiguration.FluoSide
									|| imageConfiguration == ImageConfiguration.FluoTop)
								is.setFluoInfo(id);
							if (imageConfiguration == ImageConfiguration.NirSide
									|| imageConfiguration == ImageConfiguration.NirTop)
								is.setNirInfo(id);
							if (imageConfiguration == ImageConfiguration.IrSide
									|| imageConfiguration == ImageConfiguration.IrTop)
								is.setIrInfo(id);
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
	
	private LocalComputeJob saveImage(
			final int tray, final int tray_cnt,
			final ImageData id, final Image image,
			final byte[] optLabelImageContent, final String labelFileExtension)
			throws InterruptedException {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (output == null) {
					System.err.println("INTERNAL ERROR: OUTPUT IS NULL!!! 3");
					return;
				}
				if (optLabelImageContent == null) {
					if (image.getHeight() > 1) {
						if (id != null && id.getParentSample() != null) {
							LoadedImage loadedImage = new LoadedImage(id,
									image.getAsBufferedImage());
							// loadedImage.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
							ImageData imageRef = saveImageAndUpdateURL(
									loadedImage, databaseTarget, false,
									tray, tray_cnt);
							if (imageRef != null) {
								if (output != null)
									outputAdd(imageRef);
							} else {
								System.out.println(SystemAnalysis.getCurrentTime()
										+ ">ERROR: SaveImageAndUpdateURL failed! (NULL Result)");
								ErrorMsg.addErrorMessage("SaveImageAndUpdateURL failed! (NULL Result)");
							}
						}
					}
				} else {
					if (image.getHeight() > 1) {
						LoadedImageStream loadedImage = new LoadedImageStream(
								id, image.getAsBufferedImage(),
								optLabelImageContent);
						loadedImage.setLabelURL(new IOurl(id.getURL()
								.getPrefix(), null, "d_"
								+ id.getURL().getFileName()
								+ labelFileExtension));
						ImageData imageRef = saveImageAndUpdateURL(
								loadedImage,
								databaseTarget, true, tray, tray_cnt);
						if (imageRef == null) {
							ErrorMsg.addErrorMessage("SaveImageAndUpdateURL failed! (ERROR #1)");
							System.out.println("ERROR #1");
						} else {
							if (output != null)
								outputAdd(imageRef);
						}
					}
				}
			}
		};
		r.run();
		return null;
		// return new LocalComputeJob(r, "Save Image");
	}
	
	private void outputAdd(NumericMeasurementInterface meas) {
		// MappingData3DPath mp = new MappingData3DPath(meas, true);
		Substance3D.addAndMerge(output, meas, false);
		// Substance3D.addAndMerge(output, mp.getSubstance(), false);
	}
	
	protected ImageData saveImageAndUpdateURL(LoadedImage result,
			DatabaseTarget storeResultInDatabase, boolean processLabelUrl,
			int tray, int tray_cnt) {
		result.getURL().setFileName(addTrayInfo(tray, tray_cnt, result.getURL().getFileName()));
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (result.getLabelURL() != null && processLabelUrl) {
			result.getLabelURL().setFileName(
					addTrayInfo(tray, tray_cnt,
							result.getLabelURL().getFileName()));
			result.getLabelURL().setPrefix(LoadedDataHandler.PREFIX);
		}
		
		try {
			LoadedImage lib = result;
			if (storeResultInDatabase != null) {
				result = storeResultInDatabase.saveImage(new String[] { "", "label_" }, result, false, true);
				// add processed image to result
				if (result != null)
					return new ImageData(result.getParentSample(), result);
				else {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">Could not save in DB: "
							+ lib.getURL().toString());
					ErrorMsg.addErrorMessage("Could not save in DB: "
							+ lib.getURL().toString());
				}
			} else {
				boolean clearmemory = true;
				if (clearmemory) {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">Image result not saved and removed from result set: " + lib.getURL().toString());
					return null;
				} else {
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">Image result kept in memory: " + lib.getURL().toString());
					return result;
				}
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	private String addTrayInfo(int tray, int tray_cnt, String fileName) {
		if (tray_cnt > 1) {
			String extension = fileName.substring(fileName.lastIndexOf(".") + ".".length());
			fileName = StringManipulationTools.stringReplace(fileName,
					"." + extension, "." + tray + "." + tray_cnt + "."
							+ extension);
		}
		return fileName;
	}
	
	@Override
	public ExperimentInterface getOutput() {
		ExperimentInterface result = output;
		output = null;
		ImageOperation.setLabCubeInstanceToNull();
		return result;
	}
	
	private void processNumericResults(ImageData copyFrom, HashMap<Integer, BlockResultSet> tray2analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 1");
		}
		
		boolean multiTray = tray2analysisResults.keySet().size() > 1;
		for (Integer tray : tray2analysisResults.keySet()) {
			for (BlockPropertyValue bpv : tray2analysisResults.get(tray).getPropertiesSearch("RESULT_")) {
				if (bpv.getName() == null)
					continue;
				
				NumericMeasurement3D m = new NumericMeasurement3D(copyFrom, bpv.getName(), null);
				m.getParentSample().getParentCondition().getParentSubstance().setInfo(null); // remove information about source camera
				m.setAnnotation(null);
				m.setValue(bpv.getValue());
				m.setUnit(bpv.getUnit());
				if (multiTray)
					m.setQualityAnnotation(m.getQualityAnnotation() + "_" + tray);
				
				if (output != null)
					outputAdd(m);
				else
					System.err.println("Internal Error: Output is NULL!!");
			}
		}
	}
	
	private void addPostprocessingResults(
			TreeMap<Long, Sample3D> inSamples,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 2");
		}
		
		for (Long time : analysisResults.keySet())
			for (Integer tray : analysisResults.get(time).keySet()) {
				boolean multipleTrays = analysisResults.get(time).keySet().size() > 1;
				for (String volumeID : analysisResults.get(time).get(tray).getVolumeNames()) {
					VolumeData v = analysisResults.get(time).get(tray).getVolume(volumeID);
					if (v != null) {
						analysisResults.get(time).get(tray).setVolume(volumeID, null);
						
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
							ErrorMsg.addErrorMessage(e);
						}
					}
				}
				for (BlockPropertyValue bpv : analysisResults.get(time).get(tray).getPropertiesSearch("RESULT_")) {
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
							if (multipleTrays) {
								m.setReplicateID(m.getReplicateID() * 100 + tray);
							}
							m.setQualityAnnotation(template.getQualityAnnotation() + (multipleTrays ? "_" + tray : ""));
							if (bpv.getPosition() != null)
								m.setPosition(bpv.getPosition());
							else
								m.setPosition(template.getPosition());
							m.setPositionUnit(template.getPositionUnit());
						}
						outputAdd(m);
					}
				}
			}
	}
	
	private ArrayList<LocalComputeJob> processAndOrSaveResultImages(
			int tray, int tray_cnt,
			ImageSet id,
			ImageData inVis, ImageData inFluo, ImageData inNir, ImageData inIr,
			ImageStack debugImageStack, Image resVis,
			Image resFluo, Image resNir, Image resIr, int parentPriority)
			throws InterruptedException {
		// StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() +
		// ">SAVE IMAGE RESULTS", false);
		// ArrayList<LocalComputeJob> waitThreads = new ArrayList<LocalComputeJob>();
		if (forceDebugStack) {
			while (forcedDebugStacks.size() < tray_cnt)
				forcedDebugStacks.add(null);
			forcedDebugStacks.set(tray, debugImageStack);
		} else {
			byte[] buf = null;
			if (debugImageStack != null) {
				System.out.println("[s");
				MyByteArrayOutputStream mos = new MyByteArrayOutputStream();
				debugImageStack.saveAsLayeredTif(mos);
				debugImageStack.show("Tray " + tray + "_" + tray_cnt);
				buf = mos.getBuff();
				System.out.println("f]");
			} else {
				if (inVis != null && inVis.getLabelURL() != null)
					inVis.addAnnotationField("oldreference", inVis
							.getLabelURL().toString());
				if (inFluo != null && inFluo.getLabelURL() != null)
					inFluo.addAnnotationField("oldreference", inFluo
							.getLabelURL().toString());
				if (inNir != null && inNir.getLabelURL() != null)
					inNir.addAnnotationField("oldreference", inNir
							.getLabelURL().toString());
				if (inIr != null && inIr.getLabelURL() != null)
					inIr.addAnnotationField("oldreference", inIr
							.getLabelURL().toString());
				
				if (id.getVisInfo() != null && id.getVisInfo().getURL() != null)
					inVis.setLabelURL(id.getVisInfo().getURL().copy());
				
				if (id.getFluoInfo() != null && id.getFluoInfo().getURL() != null)
					inFluo.setLabelURL(id.getFluoInfo().getURL().copy());
				if (inNir != null && id != null && id.getNirInfo() != null
						&& id.getNirInfo().getURL() != null)
					inNir.setLabelURL(id.getNirInfo().getURL().copy());
				if (inIr != null && id != null && id.getIrInfo() != null
						&& id.getIrInfo().getURL() != null)
					inIr.setLabelURL(id.getIrInfo().getURL().copy());
			}
			LocalComputeJob ra = null, rb = null, rc = null, rd = null;
			
			if (resVis != null)
				ra = saveImage(
						tray, tray_cnt,
						inVis, resVis, buf, "." + IAPservice.getTargetFileExtension(false, null));
			if (resFluo != null)
				rb = saveImage(
						tray, tray_cnt,
						inFluo, resFluo, buf, "." + IAPservice.getTargetFileExtension(false, null));
			if (resNir != null)
				rc = saveImage(
						tray, tray_cnt,
						inNir, resNir, buf, "." + IAPservice.getTargetFileExtension(false, null));
			if (resIr != null)
				rd = saveImage(
						tray, tray_cnt,
						inIr, resIr, buf, "." + IAPservice.getTargetFileExtension(false, null));
			
			// if (ra != null) {
			// waitThreads.add(BackgroundThreadDispatcher.addTask(ra));
			// }
			// if (rb != null) {
			// waitThreads.add(BackgroundThreadDispatcher.addTask(rb));
			// }
			// if (rc != null) {
			// waitThreads.add(BackgroundThreadDispatcher.addTask(rc));
			// }
			// if (rd != null) {
			// waitThreads.add(BackgroundThreadDispatcher.addTask(rd));
			// }
		}
		return null;// waitThreads;
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
			TreeMap<String, HashMap<Integer, BlockResultSet>> previousResultsForThisTimePoint)
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
		
		final ImageSet input = new ImageSet();
		final ImageSet inputMasks = new ImageSet();
		
		input.setImageInfo(inVis, inFluo, inNir, inIr);
		inputMasks.setImageInfo(inVis, inFluo, inNir, inIr);
		
		ImageProcessorOptions options = new ImageProcessorOptions(pd.getOptions(), previousResultsForThisTimePoint);
		
		options.setUnitTestInfo(unit_test_idx, unit_test_steps);
		
		{
			options.setCustomNullBlockPrefix("Separate Settings");
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
		
		HashMap<Integer, ImageStack> debugImageStack = null;
		boolean addDebugImages = IAPmain
				.isSettingEnabled(IAPfeature.SAVE_DEBUG_STACK);
		if (addDebugImages || forceDebugStack) {
			debugImageStack = new HashMap<Integer, ImageStack>();
		}
		
		HashMap<Integer, BlockResultSet> well2analysisResults = null;
		
		{
			ImageProcessor imageProcessor = getImageProcessor();
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = getStatusProcessor(status, workloadSnapshotAngles);
			imageProcessor.setStatus(statusForThisTask);
			imageProcessor.setValidTrays(debugValidTrays);
			HashMap<Integer, StringAndFlexibleMaskAndImageSet> ret = imageProcessor.execute(options,
					input, inputMasks, maximumThreadCountOnImageLevel,
					debugImageStack);
			
			for (Integer wellIdx : ret.keySet()) {
				ImageSet pipelineResult = ret != null ? ret.get(wellIdx).getMaskAndImageSet().images() : null;
				if (pipelineResult != null) {
					Image resVis = null, resFluo = null, resNir = null, resIr = null;
					resVis = pipelineResult.vis();
					resFluo = pipelineResult.fluo();
					resNir = pipelineResult.nir();
					resIr = pipelineResult.ir();
					
					boolean manyWells = options.getWellCnt() > 1;
					ImageData inVis2 = inVis == null ? null : (ImageData) inVis.clone(inVis.getParentSample());
					ImageData inFluo2 = inFluo == null ? null : (ImageData) inFluo.clone(inFluo.getParentSample());
					ImageData inNir2 = inNir == null ? null : (ImageData) inNir.clone(inNir.getParentSample());
					ImageData inIr2 = inIr == null ? null : (ImageData) inIr.clone(inIr.getParentSample());
					if (manyWells) {
						for (ImageData img : new ImageData[] { inVis2, inFluo2, inNir2, inIr2 })
							if (img != null)
								img.setQualityAnnotation(img.getQualityAnnotation() + "_" + wellIdx);
					}
					
					well2analysisResults = imageProcessor.getNumericResults();
					// waitThreads.addAll(
					processAndOrSaveResultImages(
							wellIdx, options.getWellCnt(),
							id, inVis2, inFluo2, inNir2, inIr2,
							debugImageStack != null ? debugImageStack.get(wellIdx) : null, resVis, resFluo, resNir, resIr, parentPriority);
					// );
				}
			}
		}
		// BackgroundThreadDispatcher.waitFor(waitThreads);
		if (well2analysisResults != null) {
			ImageData copyFrom = inVis;
			if (copyFrom == null)
				copyFrom = inFluo;
			if (copyFrom == null)
				copyFrom = inNir;
			if (copyFrom == null)
				copyFrom = inIr;
			
			processNumericResults(copyFrom, well2analysisResults);
			
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
	
}
