package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.SystemAnalysis;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.gui.IAPfeature;
import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.gui.actions.ImagePreProcessor;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResultSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.CutImagePreprocessor;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ImageSet;
import de.ipk.ag_ba.server.databases.DBTable;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.datastructures.LoadedImageStream;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
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
	
	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	private boolean forceDebugStack;
	private ArrayList<FlexibleImageStack> forcedDebugStacks;
	private int prio;
	private MongoDB m;
	private Exception error;
	private boolean runOK;
	
	@Override
	public void setInput(Collection<Sample3D> input,
			Collection<NumericMeasurementInterface> optValidMeasurements,
			MongoDB m, int workOnSubset, int numberOfSubsets) {
		this.input = input;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
		this.m = m;
		databaseTarget = m != null ? new DataBaseTargetMongoDB(true, m) : null;
	}
	
	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}
	
	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE,
				ImageAnalysisType.MEASUREMENT };
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
				AbstractPhenotypingTask.class, 1);
		maxInst.acquire();
		try {
			status.setCurrentStatusValue(0);
			status.setCurrentStatusText1("Start " + getName());
			output = new ArrayList<NumericMeasurementInterface>();
			
			ArrayList<TreeMap<String, ImageSet>> workload = new ArrayList<TreeMap<String, ImageSet>>();
			
			addTopOrSideImagesToWorkset(workload, 0, analyzeTopImages(),
					analyzeSideImages());
			
			// workload = filterWorkload(workload, null);// "Athletico");//
			// "Rainbow Amerindian"); // Athletico
			
			final ThreadSafeOptions tso = new ThreadSafeOptions();
			final int workloadSnapshots = workload.size();
			int snapshotsWithNotAllNeededImageTypes = 0;
			int side = 0;
			int top = 0;
			for (TreeMap<String, ImageSet> tm : workload)
				for (ImageSet md : tm.values()) {
					if (!md.hasAllNeededImageTypes()) {
						snapshotsWithNotAllNeededImageTypes++;
						System.out.println(md.getVIS() + " / " + md.getFLUO()
								+ " / " + md.getNIR());
					}
					if (md.isSide())
						side++;
					else
						top++;
				}
			if (snapshotsWithNotAllNeededImageTypes > 0)
				System.out.println(SystemAnalysisExt.getCurrentTime()
						+ ">WARNING: not all three images available for "
						+ snapshotsWithNotAllNeededImageTypes + " snapshots!");
			System.out.println(SystemAnalysisExt.getCurrentTime()
					+ ">INFO: Workload Top/Side: " + top + "/" + side);
			final int workloadEqualAngleSnapshotSets = top + side;
			
			int nn = SystemAnalysis.getNumberOfCPUs();
			if (SystemAnalysis.getMemoryMB() < 1500 && nn > 1) {
				System.out
						.println(SystemAnalysisExt.getCurrentTime()
								+ ">LOW SYSTEM MEMORY (less than 1500 MB), LIMITING CONCURRENCY");
				nn = 1;
			}
			if (SystemAnalysis.getMemoryMB() < 2000 && nn > 1) {
				System.out
						.println(SystemAnalysisExt.getCurrentTime()
								+ ">LOW SYSTEM MEMORY (less than 2000 MB), LIMITING CONCURRENCY");
				nn = 1;
			}
			if (SystemAnalysis.getMemoryMB() < 4000 && nn > 4) {
				System.out
						.println(SystemAnalysisExt.getCurrentTime()
								+ ">LOW SYSTEM MEMORY (less than 4000 MB), LIMITING CONCURRENCY");
				nn = 4;
			}
			
			if (nn > 1
					&& SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
							.getMemoryMB() * 0.7d) {
				System.out.println(SystemAnalysisExt.getCurrentTime()
						+ ">HIGH MEMORY UTILIZATION, REDUCING CONCURRENCY");
				nn = nn / 2;
				if (nn < 1)
					nn = 1;
			}
			
			System.out
					.println(SystemAnalysisExt.getCurrentTime()
							+ ">SERIAL SNAPSHOT ANALYSIS... (max concurrent thread count: "
							+ nn + ")");
			
			final Semaphore maxCon = BackgroundTaskHelper.lockGetSemaphore(
					null, nn);
			final ThreadSafeOptions freed = new ThreadSafeOptions();
			try {
				for (TreeMap<String, ImageSet> tm : workload) {
					final TreeMap<String, ImageSet> tmf = tm;
					
					maxCon.acquire(1);
					try {
						Thread t = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									processSnapshot(
											maximumThreadCountOnImageLevel,
											status, tso, workloadSnapshots,
											workloadEqualAngleSnapshotSets,
											tmf, maxCon);
								} catch (Exception err) {
									System.err.println(SystemAnalysisExt
											.getCurrentTime()
											+ "> ERROR: "
											+ err.getMessage());
									System.err.println(SystemAnalysisExt
											.getCurrentTime()
											+ "> ERROR-CAUSE: SAMPLE: "
											+ tmf.firstKey()
											+ " / "
											+ tmf.get(tmf.firstKey())
													.getSampleInfo()
											+ " / "
											+ tmf.get(tmf.firstKey())
													.getSampleInfo()
													.getParentCondition());
									err.printStackTrace();
								} finally {
									maxCon.release(1);
									freed.setBval(0, true);
								}
							}
						}, "Snapshot Analysis");
						t.setPriority(Thread.MIN_PRIORITY);
						if (SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
								.getMemoryMB() * 0.6) {
							System.out.println();
							System.out
									.print(SystemAnalysisExt.getCurrentTime()
											+ ">HIGH MEMORY UTILIZATION (>60%), ISSUE GARBAGE COLLECTION (" + SystemAnalysis.getUsedMemoryInMB()
											+ "/" + SystemAnalysis.getMemoryMB() + " MB)... ");
							System.gc();
							System.out.println("FINISHED GC (" + SystemAnalysis.getUsedMemoryInMB() + "/" + SystemAnalysis
									.getMemoryMB() + " MB)");
						}
						if (SystemAnalysis.getUsedMemoryInMB() > SystemAnalysis
								.getMemoryMB() * 0.6) {
							System.out.println();
							System.out
									.println(SystemAnalysisExt.getCurrentTime()
											+ ">HIGH MEMORY UTILIZATION (>60%), REDUCING CONCURRENCY (THREAD.RUN)");
							t.run();
						} else {
							t.start();
						}
					} catch (Exception eeee) {
						error = eeee;
						if (!freed.getBval(0, false))
							maxCon.release(1);
						throw new RuntimeException(eeee);
					}
				}
				maxCon.acquire(nn);
				maxCon.release(nn);
			} finally {
				runOK = true;
			}
		} finally {
			maxInst.release();
		}
		status.setCurrentStatusValueFine(100d);
		input = null;
	}
	
	private int getParentPriority() {
		return prio;
	}
	
	public void setParentPriority(int prio) {
		this.prio = prio;
	}
	
	private void processSnapshot(final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final ThreadSafeOptions tso, final int workloadSnapshots,
			final int workloadEqualAngleSnapshotSets,
			final TreeMap<String, ImageSet> tmf, final Semaphore optMaxCon)
			throws InterruptedException {
		
		Sample3D inSample = null;
		final TreeMap<String, BlockResultSet> analysisResults = new TreeMap<String, BlockResultSet>();
		final TreeMap<String, ImageData> analysisInput = new TreeMap<String, ImageData>();
		ArrayList<MyThread> wait = new ArrayList<MyThread>();
		if (tmf != null) {
			for (final String configAndAngle : tmf.keySet()) {
				if (status != null && status.wantsToStop())
					break;
				if (tmf.get(configAndAngle).getVIS() != null)
					inSample = (Sample3D) tmf.get(configAndAngle).getVIS()
							.getParentSample();
				else
					continue;
				final ImageData inImage = tmf.get(configAndAngle).getVIS();
				
				Runnable r = new Runnable() {
					@Override
					public void run() {
						BlockResultSet results;
						try {
							results = processAngleWithinSnapshot(
									tmf.get(configAndAngle),
									maximumThreadCountOnImageLevel, status,
									workloadEqualAngleSnapshotSets,
									getParentPriority());
							if (results != null) {
								analysisInput.put(configAndAngle, inImage);
								analysisResults.put(configAndAngle, results);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				boolean threaded = false;
				if (!threaded) {
					boolean lowMem = false;
					long used = SystemAnalysis.getUsedMemoryInMB();
					long avail = SystemAnalysis.getMemoryMB();
					if (used > avail * 0.7d) {
						System.out.println();
						System.out
								.print(SystemAnalysisExt.getCurrentTime()
										+ ">HIGH MEMORY UTILIZATION ("
										+ (100 * used / avail)
										+ "%), REDUCING CONCURRENCY (AT SNAPSHOT LEVEL) || ");
						lowMem = true;
					}
					if (!lowMem && optMaxCon != null && optMaxCon.tryAcquire(1)) {
						MyThread mt = new MyThread(r, getName() + " "
								+ System.currentTimeMillis());
						mt.setFinishrunnable(new Runnable() {
							@Override
							public void run() {
								optMaxCon.release(1);
							}
						});
						mt.start();
						wait.add(mt);
					} else
						r.run();
				} else
					wait.add(BackgroundThreadDispatcher.addTask(r,
							"Analyze image within snapshot", -1000, -1000));
			}
		}
		BackgroundThreadDispatcher.waitFor(wait);
		if (inSample != null && !analysisResults.isEmpty()) {
			BlockResultSet postprocessingResults;
			try {
				postprocessingResults = getImageProcessor()
						.postProcessPipelineResults(inSample, analysisInput,
								analysisResults, status);
				processStatisticalAndVolumeSampleOutput(inSample,
						postprocessingResults);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		tso.addInt(1);
		status.setCurrentStatusText1("Snapshot " + tso.getInt() + "/"
				+ workloadSnapshots);
	}
	
	private ArrayList<TreeMap<String, ImageSet>> filterWorkload(
			ArrayList<TreeMap<String, ImageSet>> workload, String filter) {
		// if (filter == null)
		// return workload;
		ArrayList<TreeMap<String, ImageSet>> res = new ArrayList<TreeMap<String, ImageSet>>();
		for (TreeMap<String, ImageSet> tm : workload)
			loopB: for (ImageSet is : tm.values()) {
				if (is.getSampleInfo() != null)
					if (filter == null) {
						if (is.getSampleInfo().getTime() == 61) {
							res.add(tm);
							break loopB;
						}
						
					} else
						if (is.getSampleInfo().getParentCondition()
								.toString().contains(filter)
								&& !is.getSampleInfo().getParentCondition()
										.toString().contains("wet"))
							if (is.getSampleInfo().getTime() == 61) {
								res.add(tm);
								break loopB;
							}
			}
		return res;
	}
	
	private void addTopOrSideImagesToWorkset(
			ArrayList<TreeMap<String, ImageSet>> workload, int max,
			boolean top, boolean side) {
		TreeMap<String, TreeMap<String, ImageSet>> sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle = new TreeMap<String, TreeMap<String, ImageSet>>();
		if (input == null)
			return;
		for (Sample3D ins : input)
			for (Measurement md : ins) {
				if (md instanceof ImageData) {
					ImageData id = (ImageData) md;
					
					String sampleTimeAndFullPlantAnnotation = id.getParentSample().getSampleTime() + ";"
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
						System.out.println(SystemAnalysisExt.getCurrentTime()
								+ ">INFO: IMAGE CONFIGURATION UNKNOWN ("
								+ id.getSubstanceName() + "), "
								+ "GUESSING FROM IMAGE NAME: " + id.getURL()
								+ ", GUESS: " + imageConfiguration);
					}
					if (imageConfiguration == ImageConfiguration.Unknown) {
						System.out
								.println(SystemAnalysisExt.getCurrentTime()
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
									new ImageSet(null, null, null, id.getParentSample()));
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
						}
					}
				}
			}
		
		{
			// create list of replicates and plant IDs
			int workLoadIndex = workOnSubset;
			TreeSet<String> replicateIDandQualityList = new TreeSet<String>();
			for (TreeMap<String, ImageSet> is : sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.values()) {
				for (ImageSet i : is.values()) {
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
			for (TreeMap<String, ImageSet> is : sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.values()) {
				if (is.size() == 0)
					continue;
				String val = is.values().iterator().next().getVIS().getReplicateID() + ";" +
						is.values().iterator().next().getVIS().getQualityAnnotation();
				workLoadIndex = replicateIDandQualityList2positionIndex.get(val);
				if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != 0)
					continue;
				System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: Processing image sets with ID: " + val);
				workload.add(is);
			}
			System.out.println(SystemAnalysisExt.getCurrentTime() + ">Processing "
					+ workload.size() + " of " + sampleTimeAndPlantAnnotation2imageSetWithSpecificAngle.size()
					+ " (subset " + workLoadIndex + "/" + numberOfSubsets + ")");
		}
		
		if (max > 0)
			while (workload.size() > max)
				workload.remove(0);
	}
	
	protected abstract boolean analyzeTopImages();
	
	protected abstract boolean analyzeSideImages();
	
	protected abstract ImageProcessor getImageProcessor();
	
	private MyThread saveImage(final ImageData id, final FlexibleImage image,
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
									loadedImage, databaseTarget, false);
							if (imageRef != null) {
								if (output != null)
									output.add(imageRef);
							} else
								System.out
										.println(SystemAnalysisExt
												.getCurrentTime()
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
						ImageData imageRef = saveImageAndUpdateURL(loadedImage,
								databaseTarget, true);
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
			DatabaseTarget storeResultInDatabase, boolean processLabelUrl) {
		result.getURL().setFileName("c_" + result.getURL().getFileName());
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (result.getLabelURL() != null && processLabelUrl) {
			result.getLabelURL().setFileName(
					"c_" + result.getLabelURL().getFileName());
			result.getLabelURL().setPrefix(LoadedDataHandler.PREFIX);
		}
		
		try {
			LoadedImage lib = result;
			if (storeResultInDatabase != null) {
				result = storeResultInDatabase.saveImage(result, true);
				// add processed image to result
				if (result != null)
					return new ImageData(result.getParentSample(), result);
				else
					System.out.println(SystemAnalysisExt.getCurrentTime()
							+ ">Could not save in DB: "
							+ lib.getURL().toString());
			} else {
				System.out.println(SystemAnalysisExt.getCurrentTime()
						+ ">Result kept in memory: " + lib.getURL().toString());
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		return null;
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
	
	public void addPreprocessor(CutImagePreprocessor pre) {
		preProcessors.add(pre);
	}
	
	private void processStatisticalOutput(ImageData inVis,
			BlockResultSet analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 1");
		}
		
		for (BlockPropertyValue bpv : analysisResults.getPropertiesSearch("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			NumericMeasurement3D m = new NumericMeasurement3D(inVis,
					bpv.getName(), inVis.getParentSample().getParentCondition()
							.getExperimentName()
							+ " (" + getName() + ")");
			m.setValue(bpv.getValue());
			m.setUnit(bpv.getUnit());
			
			if (output != null)
				output.add(m);
		}
	}
	
	private void processStatisticalAndVolumeSampleOutput(Sample3D inSample,
			BlockResultSet analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 2");
		}
		
		for (String volumeID : analysisResults.getVolumeNames()) {
			VolumeData v = analysisResults.getVolume(volumeID);
			if (v != null) {
				analysisResults.setVolume(volumeID, null);
				
				try {
					StopWatch s = new StopWatch(
							SystemAnalysisExt.getCurrentTime() + ">SAVE VOLUME");
					if (databaseTarget != null) {
						databaseTarget.saveVolume((LoadedVolume) v, inSample,
								m, DBTable.SAMPLE, null, null);
						VolumeData volumeInDatabase = new VolumeData(inSample,
								v);
						volumeInDatabase.getURL().setPrefix(
								databaseTarget.getPrefix());
						volumeInDatabase.getURL().setDetail(
								v.getURL().getDetail());
						output.add(volumeInDatabase);
					} else {
						System.out.println(SystemAnalysisExt.getCurrentTime()
								+ ">Volume kept in memory: " + v);
						output.add(v);
					}
					s.printTime();
				} catch (Exception e) {
					System.out.println(SystemAnalysisExt.getCurrentTime()
							+ ">ERROR: Could not save volume data: "
							+ e.getMessage());
					e.printStackTrace();
				}
				
			}
		}
		for (BlockPropertyValue bpv : analysisResults.getPropertiesSearch("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			NumericMeasurement3D m = new NumericMeasurement3D(
					new NumericMeasurement(inSample), bpv.getName(), inSample
							.getParentCondition().getExperimentName()
							+ " ("
							+ getName() + ")");
			
			if (bpv != null && m != null) {
				m.setValue(bpv.getValue());
				m.setUnit(bpv.getUnit());
				if (inSample.size() > 0) {
					NumericMeasurement3D template = (NumericMeasurement3D) inSample.iterator().next();
					m.setReplicateID(template.getReplicateID());
					m.setQualityAnnotation(template.getQualityAnnotation());
					m.setPosition(template.getPosition());
					m.setPositionUnit(template.getPositionUnit());
				}
				output.add(m);
			}
		}
	}
	
	private void processAndOrSaveTiffImagesOrResultImages(ImageSet id,
			ImageData inVis, ImageData inFluo, ImageData inNir,
			FlexibleImageStack debugImageStack, FlexibleImage resVis,
			FlexibleImage resFluo, FlexibleImage resNir, int parentPriority)
			throws InterruptedException {
		// StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() +
		// ">SAVE IMAGE RESULTS", false);
		if (forceDebugStack) {
			forcedDebugStacks.add(debugImageStack);
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
				
				if (id.getVIS() != null && id.getVIS().getURL() != null)
					inVis.setLabelURL(id.getVIS().getURL().copy());
				
				if (id.getFLUO() != null && id.getFLUO().getURL() != null)
					inFluo.setLabelURL(id.getFLUO().getURL().copy());
				if (inNir != null && id != null && id.getNIR() != null
						&& id.getNIR().getURL() != null)
					inNir.setLabelURL(id.getNIR().getURL().copy());
			}
			MyThread a = null, b = null, c = null, ra = null, rb = null, rc = null;
			
			if (resVis != null)
				ra = saveImage(inVis, resVis, buf, ".tiff");
			if (resFluo != null)
				rb = saveImage(inFluo, resFluo, buf, ".tiff");
			if (resNir != null)
				rc = saveImage(inNir, resNir, buf, ".tiff");
			
			a = BackgroundThreadDispatcher.addTask(ra, parentPriority + 1, 5);
			b = BackgroundThreadDispatcher.addTask(rb, parentPriority + 1, 5);
			c = BackgroundThreadDispatcher.addTask(rc, parentPriority + 1, 5);
			BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c });
		}
		// s.printTime();
	}
	
	private BlockResultSet processAngleWithinSnapshot(ImageSet id,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles, int parentPriority)
			throws Exception {
		ImageData inVis = id.getVIS() != null ? id.getVIS().copy() : null;
		ImageData inFluo = id.getFLUO() != null ? id.getFLUO().copy() : null;
		ImageData inNir = id.getNIR() != null ? id.getNIR().copy() : null;
		
		if (inVis == null && inFluo == null && inNir == null) {
			System.out.println(SystemAnalysisExt.getCurrentTime()
					+ ">ERROR: SNAPSHOT WITH NO VIS+FLUO+NIR IMAGES");
			return null;
		}
		
		final FlexibleImageSet input = new FlexibleImageSet();
		final FlexibleImageSet inputMasks = new FlexibleImageSet();
		
		input.setImageInfo(inVis, inFluo, inNir);
		inputMasks.setImageInfo(inVis, inFluo, inNir);
		
		boolean side = id.isSide();
		
		ImageProcessorOptions options = new ImageProcessorOptions();
		if (inVis != null && inVis.getPosition() != null)
			options.addDoubleSetting(Setting.ROTATION_ANGLE,
					inVis.getPosition());
		if (side)
			options.setCameraPosition(CameraPosition.SIDE);
		else
			options.setCameraPosition(CameraPosition.TOP);
		
		FlexibleImageStack debugImageStack = null;
		boolean addDebugImages = IAPmain
				.isSettingEnabled(IAPfeature.SAVE_DEBUG_STACK);
		if (addDebugImages || forceDebugStack) {
			debugImageStack = new FlexibleImageStack();
		}
		
		BlockResultSet analysisResults = null;
		
		FlexibleImage resVis = null, resFluo = null, resNir = null;
		{
			ImageProcessor imageProcessor = getImageProcessor();
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = getStatusProcessor(status, workloadSnapshotAngles);
			imageProcessor.setStatus(statusForThisTask);
			
			// TODO FIX: debugImageStack should be no input, only an output
			// TODO: maximumThreadCount... should be no parameter but a setting!
			FlexibleMaskAndImageSet ret = imageProcessor.pipeline(options,
					input, inputMasks, maximumThreadCountOnImageLevel,
					debugImageStack);
			FlexibleImageSet pipelineResult = ret != null ? ret.getImages()
					: null;
			
			if (pipelineResult != null) {
				resVis = pipelineResult.getVis();
				resFluo = pipelineResult.getFluo();
				resNir = pipelineResult.getNir();
				analysisResults = imageProcessor.getSettings();
			}
		}
		
		if (analysisResults != null) {
			processStatisticalOutput(inVis, analysisResults);
			
			processAndOrSaveTiffImagesOrResultImages(id, inVis, inFluo, inNir,
					debugImageStack, resVis, resFluo, resNir, parentPriority);
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
			public void setCurrentStatusValue(int value) {
				setCurrentStatusValueFine(value);
			}
			
		};
	}
	
}
