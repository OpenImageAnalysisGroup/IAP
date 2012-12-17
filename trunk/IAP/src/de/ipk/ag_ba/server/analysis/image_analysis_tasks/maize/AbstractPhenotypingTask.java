package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

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

import de.ipk.ag_ba.commands.vfs.VirtualFileSystem;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemFolderStorage;
import de.ipk.ag_ba.commands.vfs.VirtualFileSystemVFS2;
import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.gui.webstart.HSMfolderTargetDataManager;
import de.ipk.ag_ba.gui.webstart.IAP_RELEASE;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ImageSet;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.datastructures.LoadedImageStream;
import de.ipk.vanted.plugin.VfsFileProtocol;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
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
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.LoadedVolume;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeData;

public abstract class AbstractPhenotypingTask implements ImageAnalysisTask {
	
	private Collection<Sample3D> input = new ArrayList<Sample3D>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	private boolean forceDebugStack;
	private ArrayList<FlexibleImageStack> forcedDebugStacks;
	private int prio;
	private MongoDB m;
	private Exception error;
	private boolean runOK;
	private TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData;
	private int unit_test_idx;
	private int unit_test_steps;
	private int[] debugValidTrays;
	
	@Override
	public IAP_RELEASE getVersionTag() {
		return getImageProcessor().getVersionTag();
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
		databaseTarget = m != null ? new DataBaseTargetMongoDB(true, m) : null;
		if (databaseTarget == null) {
			ArrayList<VirtualFileSystem> vl = VirtualFileSystemFolderStorage.getKnown();
			ArrayList<VirtualFileSystem> remove = new ArrayList<VirtualFileSystem>();
			for (VirtualFileSystem v : vl) {
				boolean ok = false;
				if (v instanceof VirtualFileSystemVFS2) {
					VirtualFileSystemVFS2 v2 = (VirtualFileSystemVFS2) v;
					if (v2.getProtocolType() == VfsFileProtocol.LOCAL)
						ok = true;
				}
				if (!ok)
					remove.add(v);
			}
			vl.removeAll(remove);
			if (IAPmain.getRunMode() != IAPrunMode.WEB) {
				if (vl != null && vl.size() > 1) {
					Object[] sel = MyInputHelper.getInput("Please select the target for the storage of the result images. <br>" +
							"If no target is selected (or cancel is pressed), the numeric result will be<br>" +
							"helt in memory, and result image data will be discarded.",
							"Select target storage", "Target", vl);
					if (sel != null) {
						VirtualFileSystem sss = (VirtualFileSystem) sel[0];
						if (sss != null)
							databaseTarget = new HSMfolderTargetDataManager(sss.getPrefix(), sss.getTargetPathName());
					}
				} else
					if (vl != null && vl.size() > 0) {
						VirtualFileSystem sss = vl.iterator().next();
						databaseTarget = new HSMfolderTargetDataManager(sss.getPrefix(), sss.getTargetPathName());
					}
			}
		}
	}
	
	public void debugOverrideAndEnableDebugStackStorage(boolean enable) {
		this.forceDebugStack = enable;
		this.forcedDebugStacks = new ArrayList<FlexibleImageStack>();
	}
	
	public ArrayList<FlexibleImageStack> getForcedDebugStackStorageResult() {
		return forcedDebugStacks;
	}
	
	@Override
	public void performAnalysis(final int maximumThreadCountParallelImages,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status)
			throws InterruptedException {
		
		status.setCurrentStatusValue(-1);
		status.setCurrentStatusText1("Wait for execution time slot");
		final Semaphore maxInst = BackgroundTaskHelper.lockGetSemaphore(
				AbstractPhenotypingTask.class, SystemOptions.getInstance().getInteger("IAP", "Max-Concurrent-Phenotyping-Tasks", 1));
		maxInst.acquire();
		try {
			status.setCurrentStatusValue(0);
			status.setCurrentStatusText1("Execute " + getName());
			output = new ArrayList<NumericMeasurementInterface>();
			
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
			
			// workload = filterWorkload(workload, null);// "Athletico");//
			// "Rainbow Amerindian"); // Athletico
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			final int workloadSnapshots = workload_imageSetsWithSpecificAngles.size();
			int snapshotsWithNotAllNeededImageTypes = 0;
			int side = 0;
			int top = 0;
			for (TreeMap<Long, TreeMap<String, ImageSet>> plants : workload_imageSetsWithSpecificAngles.values())
				for (TreeMap<String, ImageSet> imageSetWithSpecificAngle : plants.values())
					for (ImageSet md : imageSetWithSpecificAngle.values()) {
						if (!md.hasAllNeededImageTypes()) {
							snapshotsWithNotAllNeededImageTypes++;
							System.out.println(md.getVIS() + " / " + md.getFLUO()
									+ " / " + md.getNIR() + "/" + md.getIR());
						}
						if (md.isSide())
							side++;
						else
							top++;
					}
			if (snapshotsWithNotAllNeededImageTypes > 0)
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">WARNING: not all three images available for "
						+ snapshotsWithNotAllNeededImageTypes + " snapshots!");
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">INFO: Workload Top/Side: " + top + "/" + side);
			final int workloadEqualAngleSnapshotSets = top + side;
			
			int nn = SystemAnalysis.getNumberOfCPUs();
			nn = modifyConcurrencyDependingOnMemoryStatus(nn);
			
			final Semaphore maxCon = BackgroundTaskHelper.lockGetSemaphore(null, nn);
			final ThreadSafeOptions freed = new ThreadSafeOptions();
			try {
				int numberOfPlants = workload_imageSetsWithSpecificAngles.keySet().size();
				int progress = 0;
				BackgroundTaskHelper.issueSimpleTask("Download Images (Caching)", "", new Runnable() {
					@Override
					public void run() {
						for (String plantID : workload_imageSetsWithSpecificAngles.keySet()) {
							if (SystemOptions.getInstance().getBoolean("IAP", "use local file cache", true))
								DownloadCache.getInstance().downloadSnapshots(plantID, workload_imageSetsWithSpecificAngles.values());
						}
					}
				}, null);
				for (String plantID : workload_imageSetsWithSpecificAngles.keySet()) {
					if (status.wantsToStop())
						continue;
					final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle_f = workload_imageSetsWithSpecificAngles.get(plantID);
					final String plantIDf = plantID;
					maxCon.acquire(1);
					try {
						progress++;
						final String preThreadName = "Snapshot Analysis (" + progress + "/" + numberOfPlants + ", plant " + plantID;
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									processSnapshot(
											plandID2time2waterData,
											plantIDf, preThreadName,
											maximumThreadCountOnImageLevel,
											status, tso, workloadSnapshots,
											workloadEqualAngleSnapshotSets,
											imageSetWithSpecificAngle_f, maxCon);
								} catch (Exception err) {
									printError(imageSetWithSpecificAngle_f, err);
								} finally {
									maxCon.release(1);
									freed.setBval(0, true);
								}
							}
						}, preThreadName + ")");
						startThread(t);
					} catch (Exception eeee) {
						error = eeee;
						if (!freed.getBval(0, false))
							maxCon.release(1);
						throw new RuntimeException(eeee);
					}
				}
				maxCon.acquire(nn);
				maxCon.release(nn);
				for (String plantID : workload_imageSetsWithSpecificAngles.keySet())
					if (SystemOptions.getInstance().getBoolean("IAP", "use local file cache", true))
						DownloadCache.getInstance().finished(plantID);
			} finally {
				runOK = true;
			}
		} finally {
			maxInst.release();
		}
		status.setCurrentStatusValueFine(100d);
		input = null;
		
		if (status.wantsToStop())
			output = null;
	}
	
	private int modifyConcurrencyDependingOnMemoryStatus(int nn) {
		if (SystemAnalysis.getMemoryMB() < 1500 && nn > 1) {
			System.out
					.println(SystemAnalysis.getCurrentTime()
							+ ">LOW SYSTEM MEMORY (less than 1500 MB), LIMITING CONCURRENCY TO 1");
			nn = 1;
		}
		if (SystemAnalysis.getMemoryMB() < 2000 && nn > 1) {
			System.out
					.println(SystemAnalysis.getCurrentTime()
							+ ">LOW SYSTEM MEMORY (less than 2000 MB), LIMITING CONCURRENCY TO 1");
			nn = 1;
		}
		if (SystemAnalysis.getMemoryMB() < 4000 && nn > 2) {
			System.out
					.println(SystemAnalysis.getCurrentTime()
							+ ">LOW SYSTEM MEMORY (less than 4000 MB), LIMITING CONCURRENCY TO 2");
			nn = 2;
		}
		
		if (nn > 1
				&& SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
						.getMemoryMB() * 0.7d) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">HIGH MEMORY UTILIZATION, REDUCING CONCURRENCY");
			nn = nn / 2;
			if (nn < 1)
				nn = 1;
		}
		
		System.out
				.println(SystemAnalysis.getCurrentTime()
						+ ">SERIAL SNAPSHOT ANALYSIS... (max concurrent thread count: "
						+ nn + ")");
		return nn;
	}
	
	private void startThread(Thread t) {
		t.setPriority(Thread.MIN_PRIORITY);
		if (SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
				.getMemoryMB() * 0.6 && SystemAnalysis.getMemoryMB() > 4000
				&& SystemOptions.getInstance().getBoolean("SYSTEM", "Issue GC upon high memory use", true)) {
			System.out.println();
			System.out
					.print(SystemAnalysis.getCurrentTime()
							+ ">HIGH MEMORY UTILIZATION (>60%), ISSUE GARBAGE COLLECTION (" + SystemAnalysis.getUsedMemoryInMB()
							+ "/" + SystemAnalysis.getMemoryMB() + " MB)... ");
			System.gc();
			System.out.println("FINISHED GC (" + SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis
					.getMemoryMB() + " MB)");
		}
		if (SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
				.getMemoryMB() * 0.6) {
			System.out.println();
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">HIGH MEMORY UTILIZATION (>60%), REDUCING CONCURRENCY (THREAD.RUN)");
			t.run();
		} else {
			t.start();
		}
	}
	
	private int getParentPriority() {
		return prio;
	}
	
	public void setParentPriority(int prio) {
		this.prio = prio;
	}
	
	private void processSnapshot(
			TreeMap<String, TreeMap<Long, Double>> plandID2time2waterData2,
			String plantID, String preThreadName,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final ThreadSafeOptions tso, final int workloadSnapshots,
			final int workloadEqualAngleSnapshotSets,
			final TreeMap<Long, TreeMap<String, ImageSet>> imageSetWithSpecificAngle, final Semaphore optMaxCon)
			throws InterruptedException {
		
		final TreeMap<Long, Sample3D> inSamples = new TreeMap<Long, Sample3D>();
		final TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>> analysisResults = new TreeMap<Long, TreeMap<String, HashMap<Integer, BlockResultSet>>>();
		final TreeMap<Long, TreeMap<String, ImageData>> analysisInput = new TreeMap<Long, TreeMap<String, ImageData>>();
		if (imageSetWithSpecificAngle != null) {
			int threadsToStart = 0;
			for (final Long time : imageSetWithSpecificAngle.keySet()) {
				threadsToStart += imageSetWithSpecificAngle.get(time).keySet().size();
			}
			final Semaphore innerLoopSemaphore = BackgroundTaskHelper.lockGetSemaphore(null, threadsToStart);
			for (final Long time : imageSetWithSpecificAngle.keySet()) {
				for (final String configAndAngle : imageSetWithSpecificAngle.get(time).keySet()) {
					if (status.wantsToStop())
						continue;
					if (imageSetWithSpecificAngle.get(time).get(configAndAngle).getVIS() != null) {
						Sample3D inSample = (Sample3D) imageSetWithSpecificAngle.get(time).get(configAndAngle).getVIS()
								.getParentSample();
						inSamples.put(time, inSample);
					} else
						continue;
					final ImageData inImage = imageSetWithSpecificAngle.get(time).get(configAndAngle).getVIS();
					
					Thread.currentThread().setName(preThreadName + ", " + SystemAnalysis.getCurrentTime(time) + ", " +
							inImage.getParentSample().getTimeUnit() + " " + inImage.getParentSample().getTime() + ", " + configAndAngle + ")");
					
					final boolean releaseCon = optMaxCon.tryAcquire();
					if (!releaseCon)
						System.out.print(">RUN");
					else
						System.out.print(">START");
					Runnable r = new Runnable() {
						@Override
						public void run() {
							try {
								try {
									HashMap<Integer, BlockResultSet> results = processAngleWithinSnapshot(
											imageSetWithSpecificAngle.get(time).get(configAndAngle),
											maximumThreadCountOnImageLevel, status,
											workloadEqualAngleSnapshotSets,
											getParentPriority());
									processVolumeOutput(inSamples.get(time), results);
									if (results != null) {
										synchronized (analysisInput) {
											if (!analysisInput.containsKey(time))
												analysisInput.put(time, new TreeMap<String, ImageData>());
											if (!analysisResults.containsKey(time))
												analysisResults.put(time, new TreeMap<String, HashMap<Integer, BlockResultSet>>());
											analysisInput.get(time).put(configAndAngle, inImage);
											analysisResults.get(time).put(configAndAngle, results);
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							} finally {
								innerLoopSemaphore.release();
								if (releaseCon)
									optMaxCon.release();
								
							}
						}
					};
					innerLoopSemaphore.acquire();
					if (releaseCon) {
						Thread innerThread = new Thread(r);
						innerThread.setName("Inner thread " + preThreadName + ", " + SystemAnalysis.getCurrentTime(time) + ", " +
								inImage.getParentSample().getTimeUnit() + " " + inImage.getParentSample().getTime() + ", " + configAndAngle + ")");
						innerThread.setPriority(Thread.MIN_PRIORITY);
						
						innerThread.start();
					} else
						r.run();
				}
			}
			innerLoopSemaphore.acquire(threadsToStart);
			innerLoopSemaphore.release(threadsToStart);
		}
		Thread.currentThread().setName("Analyse " + plantID + " (post-processing)");
		if (!analysisResults.isEmpty()) {
			TreeMap<Long, HashMap<Integer, BlockResultSet>> postprocessingResults;
			try {
				ImageProcessorOptions options = new ImageProcessorOptions();
				options.setUnitTestInfo(unit_test_idx, unit_test_steps);
				postprocessingResults = getImageProcessor()
						.postProcessPipelineResults(
								plandID2time2waterData2,
								inSamples, analysisInput,
								analysisResults, status,
								options);
				processStatisticalOutputOnGlobalLevel(inSamples, postprocessingResults);
			} catch (Exception e) {
				e.printStackTrace();
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
							output.add(volumeInDatabase);
						} else {
							System.out.println(SystemAnalysis.getCurrentTime()
									+ ">Volume kept in memory: " + v);
							output.add(v);
						}
						s.printTime();
					} catch (Exception e) {
						System.out.println(SystemAnalysis.getCurrentTime()
								+ ">ERROR: Could not save volume data: "
								+ e.getMessage());
						e.printStackTrace();
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
						String imageConfigurationName = imageConfiguration + "";
						imageConfigurationName = imageConfigurationName.substring(imageConfigurationName.indexOf(".")
								+ ".".length());
						String imageConfigurationAndRotationAngle = id.getPosition() != null ? imageConfigurationName + ";"
								+ id.getPosition() : imageConfigurationName + ";" + 0d;
						if (!sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).containsKey(
								imageConfigurationAndRotationAngle)) {
							sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).put(
									imageConfigurationAndRotationAngle,
									new ImageSet(null, null, null, null, id.getParentSample()));
						}
						ImageSet is = sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.get(sampleTimeAndFullPlantAnnotation).get(
								imageConfigurationAndRotationAngle);
						
						is.setSide(imageConfiguration.isSide());
						if ((imageConfiguration.isSide() && side) || (!imageConfiguration.isSide() && top)) {
							if (imageConfiguration == ImageConfiguration.RgbSide
									|| imageConfiguration == ImageConfiguration.RgbTop)
								is.setVis(id);
							if (imageConfiguration == ImageConfiguration.FluoSide
									|| imageConfiguration == ImageConfiguration.FluoTop)
								is.setFluo(id);
							if (imageConfiguration == ImageConfiguration.NirSide
									|| imageConfiguration == ImageConfiguration.NirTop)
								is.setNir(id);
							if (imageConfiguration == ImageConfiguration.IrSide
									|| imageConfiguration == ImageConfiguration.IrTop)
								is.setIr(id);
						}
					}
				}
			}
		
		{
			// create list of replicates and plant IDs
			TreeSet<String> replicateIDandQualityList = new TreeSet<String>();
			for (TreeMap<String, ImageSet> is : sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.values()) {
				for (ImageSet i : is.values()) {
					if (i.getVIS() == null)
						continue;
					String val = i.getVIS().getReplicateID() + ";" + i.getVIS().getQualityAnnotation();
					replicateIDandQualityList.add(val);
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
				if (is.firstEntry().getValue().getVIS() == null)
					continue;
				String val = is.firstEntry().getValue().getVIS().getReplicateID() + ";" +
						is.firstEntry().getValue().getVIS().getQualityAnnotation();
				
				int workLoadIndex = replicateIDandQualityList2positionIndex.get(val);
				if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != workOnSubset)
					continue;
				String info = timeInfo + ">INFO: Processing image sets with ID: " + val;
				if (!knownOutput.contains(info))
					System.out.println(info);
				knownOutput.add(info);
				if (!workload_imageSetsWithSpecificAngles.containsKey(val))
					workload_imageSetsWithSpecificAngles.put(val, new TreeMap<Long, TreeMap<String, ImageSet>>());;
				Long time = is.firstEntry().getValue().getVIS().getParentSample().getSampleFineTimeOrRowId();
				if (time == null)
					time = new Long(is.firstEntry().getValue().getVIS().getParentSample().getTime());
				if (!workload_imageSetsWithSpecificAngles.get(val).containsKey(time))
					workload_imageSetsWithSpecificAngles.get(val).put(time, is);
			}
			System.out.println(SystemAnalysis.getCurrentTime() + ">Processing "
					+ workload_imageSetsWithSpecificAngles.size() + " of " + sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.size()
					+ " (subset " + workOnSubset + "/" + numberOfSubsets + ")");
		}
		
		if (max > 0)
			while (workload_imageSetsWithSpecificAngles.size() > max)
				workload_imageSetsWithSpecificAngles.remove(0);
	}
	
	protected abstract boolean analyzeTopImages();
	
	protected abstract boolean analyzeSideImages();
	
	protected abstract ImageProcessor getImageProcessor();
	
	private MyThread saveImage(
			final int tray, final int tray_cnt,
			final ImageData id, final FlexibleImage image,
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
							ImageData imageRef = saveImageAndUpdateURL(
									loadedImage, databaseTarget, false,
									tray, tray_cnt);
							if (imageRef != null) {
								if (output != null)
									output.add(imageRef);
							} else
								System.out.println(SystemAnalysis.getCurrentTime()
										+ ">ERROR: SaveImageAndUpdateURL failed! (NULL Result)");
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
							System.out.println("ERROR #1");
						} else {
							if (output != null)
								output.add(imageRef);
						}
					}
				}
			}
		};
		return new MyThread(r, "Save Image");
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
				result = storeResultInDatabase.saveImage(new String[] { "main_", "label_" }, result, true);
				// add processed image to result
				if (result != null)
					return new ImageData(result.getParentSample(), result);
				else
					System.out.println(SystemAnalysis.getCurrentTime()
							+ ">Could not save in DB: "
							+ lib.getURL().toString());
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
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	private String addTrayInfo(int tray, int tray_cnt, String fileName) {
		if (tray_cnt > 1)
			fileName = StringManipulationTools.stringReplace(fileName,
					".png", "." + tray + "." + tray_cnt + ".png");
		return fileName;
	}
	
	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		if (output == null)
			System.out
					.println("INTERNAL ERROR: SECOND ATTEMPT TO RETRIEVE OUTPUT!!");
		else
			System.out
					.println("INTERNAL INFO: FIRST ATTEMPT TO RETRIEVE OUTPUT!!");
		Collection<NumericMeasurementInterface> result = output;
		output = null;
		
		return result;
	}
	
	private void processStatisticalOutputImages(ImageData inVis, HashMap<Integer, BlockResultSet> analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 1");
		}
		
		boolean multiTray = analysisResults.keySet().size() > 1;
		for (Integer tray : analysisResults.keySet()) {
			for (BlockPropertyValue bpv : analysisResults.get(tray).getPropertiesSearch("RESULT_")) {
				if (bpv.getName() == null)
					continue;
				
				NumericMeasurement3D m = new NumericMeasurement3D(inVis,
						bpv.getName(), inVis.getParentSample().getParentCondition()
								.getExperimentName()
								+ " (" + getName() + ")");
				m.setAnnotation(null);
				m.setValue(bpv.getValue());
				m.setUnit(bpv.getUnit());
				if (multiTray)
					m.setQualityAnnotation(m.getQualityAnnotation() + "_" + tray);
				
				if (output != null)
					output.add(m);
			}
		}
	}
	
	private void processStatisticalOutputOnGlobalLevel(
			TreeMap<Long, Sample3D> inSamples,
			TreeMap<Long, HashMap<Integer, BlockResultSet>> analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 2");
		}
		
		for (Long time : analysisResults.keySet())
			for (Integer tray : analysisResults.get(time).keySet()) {
				boolean multipleTrays = analysisResults.get(time).keySet().size() > 1;
				for (BlockPropertyValue bpv : analysisResults.get(time).get(tray).getPropertiesSearch("RESULT_")) {
					if (bpv.getName() == null)
						continue;
					
					NumericMeasurement3D m = new NumericMeasurement3D(
							new NumericMeasurement(inSamples.get(time)), bpv.getName(), inSamples.get(time)
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
							m.setPosition(template.getPosition());
							m.setPositionUnit(template.getPositionUnit());
						}
						output.add(m);
					}
				}
			}
	}
	
	private void processAndOrSaveTiffImagesOrResultImages(
			int tray, int tray_cnt,
			ImageSet id,
			ImageData inVis, ImageData inFluo, ImageData inNir, ImageData inIr,
			FlexibleImageStack debugImageStack, FlexibleImage resVis,
			FlexibleImage resFluo, FlexibleImage resNir, FlexibleImage resIr, int parentPriority)
			throws InterruptedException {
		// StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() +
		// ">SAVE IMAGE RESULTS", false);
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
				debugImageStack.print("NNN");
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
				
				if (id.getVIS() != null && id.getVIS().getURL() != null)
					inVis.setLabelURL(id.getVIS().getURL().copy());
				
				if (id.getFLUO() != null && id.getFLUO().getURL() != null)
					inFluo.setLabelURL(id.getFLUO().getURL().copy());
				if (inNir != null && id != null && id.getNIR() != null
						&& id.getNIR().getURL() != null)
					inNir.setLabelURL(id.getNIR().getURL().copy());
				if (inIr != null && id != null && id.getIR() != null
						&& id.getIR().getURL() != null)
					inIr.setLabelURL(id.getIR().getURL().copy());
			}
			MyThread a = null, b = null, c = null, d = null, ra = null, rb = null, rc = null, rd = null;
			
			if (resVis != null)
				ra = saveImage(
						tray, tray_cnt,
						inVis, resVis, buf, ".png");
			if (resFluo != null)
				rb = saveImage(
						tray, tray_cnt,
						inFluo, resFluo, buf, ".png");
			if (resNir != null)
				rc = saveImage(
						tray, tray_cnt,
						inNir, resNir, buf, ".png");
			if (resIr != null)
				rd = saveImage(
						tray, tray_cnt,
						inIr, resIr, buf, ".png");
			
			a = BackgroundThreadDispatcher.addTask(ra, parentPriority + 1, 5, false);
			b = BackgroundThreadDispatcher.addTask(rb, parentPriority + 1, 5, false);
			c = BackgroundThreadDispatcher.addTask(rc, parentPriority + 1, 5, false);
			d = BackgroundThreadDispatcher.addTask(rd, parentPriority + 1, 5, false);
			BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c, d });
		}
		// s.printTime();
	}
	
	@Override
	public void setUnitTestInfo(int unit_test_idx, int unit_test_steps) {
		this.unit_test_idx = unit_test_idx;
		this.unit_test_steps = unit_test_steps;
	}
	
	private HashMap<Integer, BlockResultSet> processAngleWithinSnapshot(ImageSet id,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles, int parentPriority)
			throws Exception {
		ImageData inVis = id.getVIS() != null ? id.getVIS().copy() : null;
		ImageData inFluo = id.getFLUO() != null ? id.getFLUO().copy() : null;
		ImageData inNir = id.getNIR() != null ? id.getNIR().copy() : null;
		ImageData inIr = id.getIR() != null ? id.getIR().copy() : null;
		
		if (inVis == null && inFluo == null && inNir == null && inIr == null) {
			System.out.println(SystemAnalysis.getCurrentTime()
					+ ">ERROR: SNAPSHOT WITH NO VIS+FLUO+NIR+IR IMAGES");
			return null;
		}
		
		final FlexibleImageSet input = new FlexibleImageSet();
		final FlexibleImageSet inputMasks = new FlexibleImageSet();
		
		input.setImageInfo(inVis, inFluo, inNir, inIr);
		inputMasks.setImageInfo(inVis, inFluo, inNir, inIr);
		
		boolean side = id.isSide();
		
		ImageProcessorOptions options = new ImageProcessorOptions();
		
		options.setUnitTestInfo(unit_test_idx, unit_test_steps);
		
		if (side)
			options.setCameraPosition(CameraPosition.SIDE);
		else
			options.setCameraPosition(CameraPosition.TOP);
		
		HashMap<Integer, FlexibleImageStack> debugImageStack = null;
		boolean addDebugImages = IAPmain
				.isSettingEnabled(IAPfeature.SAVE_DEBUG_STACK);
		if (addDebugImages || forceDebugStack) {
			debugImageStack = new HashMap<Integer, FlexibleImageStack>();
		}
		
		HashMap<Integer, BlockResultSet> analysisResults = null;
		
		{
			ImageProcessor imageProcessor = getImageProcessor();
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = getStatusProcessor(status, workloadSnapshotAngles);
			imageProcessor.setStatus(statusForThisTask);
			imageProcessor.setValidTrays(debugValidTrays);
			HashMap<Integer, FlexibleMaskAndImageSet> ret = imageProcessor.pipeline(options,
					input, inputMasks, maximumThreadCountOnImageLevel,
					debugImageStack);
			
			for (Integer key : ret.keySet()) {
				FlexibleImageSet pipelineResult = ret != null ? ret.get(key).images() : null;
				if (pipelineResult != null) {
					FlexibleImage resVis = null, resFluo = null, resNir = null, resIr = null;
					resVis = pipelineResult.vis();
					resFluo = pipelineResult.fluo();
					resNir = pipelineResult.nir();
					resIr = pipelineResult.ir();
					analysisResults = imageProcessor.getSettings();
					
					processAndOrSaveTiffImagesOrResultImages(
							key, options.getTrayCnt(),
							id, inVis, inFluo, inNir, inIr,
							debugImageStack != null ? debugImageStack.get(key) : null, resVis, resFluo, resNir, resIr, parentPriority);
				}
			}
		}
		
		if (analysisResults != null) {
			processStatisticalOutputImages(inVis, analysisResults);
			
		}
		return analysisResults;
		// } else {
		// System.err.println("ERROR: Not all three snapshots images could be loaded!");
		// return null;
		// }
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
