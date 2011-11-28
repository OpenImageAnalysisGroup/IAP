package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
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
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
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
								} catch (InterruptedException err) {
									System.err.println("INTERNAL ERROR: ERROR NNNN 444");
									err.printStackTrace();
								} finally {
									maxCon.release(1);
									freed.setBval(0, true);
								}
							}
						}, "Snapshot Analysis");
						t.setPriority(Thread.MIN_PRIORITY);
						t.start();
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
		final TreeMap<String, BlockProperties> analysisResults = new TreeMap<String, BlockProperties>();
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
						BlockProperties results;
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
					if (optMaxCon != null && optMaxCon.tryAcquire(1)) {
						MyThread mt = new MyThread(r, getName() + " " + System.currentTimeMillis());
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
			BlockProperties postprocessingResults;
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
		TreeMap<String, TreeMap<String, ImageSet>> replicateId2ImageSetSide = new TreeMap<String, TreeMap<String, ImageSet>>();
		
		for (Sample3D ins : input)
			for (Measurement md : ins) {
				if (md instanceof ImageData) {
					ImageData id = (ImageData) md;
					
					String keyA = id.getParentSample().getSampleTime() + ";"
							+ id.getParentSample().getFullId() + ";"
							+ id.getReplicateID();
					if (!replicateId2ImageSetSide.containsKey(keyA)) {
						replicateId2ImageSetSide.put(keyA,
								new TreeMap<String, ImageSet>());
					}
					ImageConfiguration ic = ImageConfiguration.get(id
							.getSubstanceName());
					if (ic == ImageConfiguration.Unknown) {
						ic = ImageConfiguration.get(id.getURL().getFileName());
						System.out.println(SystemAnalysisExt.getCurrentTime()
								+ ">INFO: IMAGE CONFIGURATION UNKNOWN ("
								+ id.getSubstanceName() + "), "
								+ "GUESSING FROM IMAGE NAME: " + id.getURL()
								+ ", GUESS: " + ic);
					}
					if (ic == ImageConfiguration.Unknown) {
						System.out
								.println(SystemAnalysisExt.getCurrentTime()
										+ ">ERROR: INVALID (UNKNOWN) IMAGE CONFIGURATION FOR IMAGE: "
										+ id.getURL());
					} else {
						String icS = ic + "";
						icS = icS.substring(icS.indexOf(".") + ".".length());
						String keyB = id.getPosition() != null ? icS + ";"
								+ id.getPosition() : icS + ";" + 0d;
						if (!replicateId2ImageSetSide.get(keyA).containsKey(
								keyB)) {
							replicateId2ImageSetSide.get(keyA).put(
									keyB,
									new ImageSet(null, null, null, id
											.getParentSample()));
						}
						ImageSet is = replicateId2ImageSetSide.get(keyA).get(
								keyB);
						
						is.setSide(ic.isSide());
						if ((ic.isSide() && side) || (!ic.isSide() && top)) {
							if (ic == ImageConfiguration.RgbSide
									|| ic == ImageConfiguration.RgbTop)
								is.setVis(id);
							if (ic == ImageConfiguration.FluoSide
									|| ic == ImageConfiguration.FluoTop)
								is.setFluo(id);
							if (ic == ImageConfiguration.NirSide
									|| ic == ImageConfiguration.NirTop)
								is.setNir(id);
						}
					}
				}
			}
		
		int workLoadIndex = workOnSubset;
		for (TreeMap<String, ImageSet> is : replicateId2ImageSetSide.values()) {
			if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != 0) {
				workLoadIndex++;
				continue;
			} else
				workLoadIndex++;
			workload.add(is);
		}
		System.out.println(SystemAnalysisExt.getCurrentTime() + ">Processing "
				+ workload.size() + " of " + replicateId2ImageSetSide.size()
				+ " (subset " + workLoadIndex + "/" + numberOfSubsets + ")");
		
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
	
	// private MyThread load(final ImageData id, final FlexibleImageSet input,
	// final FlexibleImageSet optImageMasks, final FlexibleImageType type,
	// final MyByteArrayInputStream optMainImageContent,
	// final MyByteArrayInputStream optLabelImageContent) {
	// Runnable r = new Runnable() {
	// @Override
	// public void run() {
	// try {
	// LoadedImage li;
	// if (optImageMasks != null) {
	// final ObjectRef mainImg = new ObjectRef();
	// final ObjectRef labelImg = new ObjectRef();
	// Runnable r1 = new Runnable() {
	// @Override
	// public void run() {
	// try {
	// mainImg.setObject(ImageIO.read(optMainImageContent));
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load main image: " + id);
	// }
	//
	// }
	// };
	// Runnable r2 = new Runnable() {
	// @Override
	// public void run() {
	// try {
	// labelImg.setObject(optLabelImageContent != null ?
	// ImageIO.read(optLabelImageContent) : null);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load label image: " + id);
	// }
	//
	// }
	// };
	//
	// MyThread a = BackgroundThreadDispatcher.addTask(r1, "Load main image",
	// getParentPriority() + 1, getParentPriority() + 2);
	// MyThread b = BackgroundThreadDispatcher.addTask(r2, "Load label image",
	// getParentPriority() + 1, getParentPriority() + 2);
	// BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b });
	//
	// li = new LoadedImage(id,
	// (BufferedImage) mainImg.getObject(),
	// (BufferedImage) labelImg.getObject());
	// } else
	// li = IOmodule.loadImageFromFileOrMongo(id, true, optImageMasks != null);
	//
	// input.set(new FlexibleImage(li.getLoadedImage(), type));
	// if (optImageMasks != null)
	// if (li.getLoadedImageLabelField() != null)
	// optImageMasks.set(new FlexibleImage(li.getLoadedImageLabelField(),
	// type));
	// else
	// System.out.println(">ERROR: Label field not available for:" + li);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load image: " + id);
	// }
	// }
	// };
	// return new MyThread(r, "Load Image " + (id != null && id.getURL() != null
	// ? "" + id.getURL().getFileName() : "(null)"));
	// }
	
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
	
	// private void loadImages(ImageData inVis,
	// ImageData inFluo, ImageData inNir,
	// final FlexibleImageSet input, final FlexibleImageSet inputMasks,
	// final int parentPriority)
	// throws InterruptedException {
	// StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">LOAD",
	// false);
	// MyThread a = null, b = null, c = null;
	// if (inVis != null) {
	// if (inVis.getAnnotationField("outlier") != null &&
	// inVis.getAnnotationField("outlier").equals("1")) {
	// System.out.println("INFO: Ignore marked outlier: " + inVis);
	// } else
	// if (inVis instanceof LoadedImage) {
	// input.setVis(new FlexibleImage(((LoadedImage) inVis).getLoadedImage()));
	// inputMasks.setVis(new FlexibleImage(((LoadedImage)
	// inVis).getLoadedImageLabelField()));
	// } else {
	// try {
	// MyByteArrayInputStream optMainImageContent =
	// ResourceIOManager.getInputStreamMemoryCached(inVis.getURL());
	// MyByteArrayInputStream optLabelImageContent = inVis.getLabelURL() != null
	// ? ResourceIOManager.getInputStreamMemoryCached(inVis.getLabelURL())
	// : null;
	// a = load(inVis, input, inputMasks, FlexibleImageType.VIS,
	// optMainImageContent, optLabelImageContent);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load VIS image or reference: " +
	// inVis);
	// }
	// }
	// }
	//
	// if (inFluo != null)
	// if (inFluo.getAnnotationField("outlier") != null &&
	// inVis.getAnnotationField("outlier").equals("1")) {
	// System.out.println("INFO: Ignore marked outlier: " + inFluo);
	// } else
	// if (inFluo instanceof LoadedImage) {
	// input.setFluo(new FlexibleImage(((LoadedImage)
	// inFluo).getLoadedImage()));
	// inputMasks.setFluo(new FlexibleImage(((LoadedImage)
	// inFluo).getLoadedImageLabelField()));
	// } else {
	// try {
	// MyByteArrayInputStream optMainImageContent =
	// ResourceIOManager.getInputStreamMemoryCached(inFluo.getURL());
	// MyByteArrayInputStream optLabelImageContent = inFluo.getLabelURL() !=
	// null ? ResourceIOManager.getInputStreamMemoryCached(inFluo
	// .getLabelURL())
	// : null;
	// b = load(inFluo, input, inputMasks, FlexibleImageType.FLUO,
	// optMainImageContent, optLabelImageContent);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load FLUO image or reference: " +
	// inFluo);
	// }
	// }
	//
	// if (inNir != null)
	// if (inNir.getAnnotationField("outlier") != null &&
	// inNir.getAnnotationField("outlier").equals("1")) {
	// System.out.println("INFO: Ignore marked outlier: " + inNir);
	// } else
	// if (inNir instanceof LoadedImage) {
	// input.setNir(new FlexibleImage(((LoadedImage) inNir).getLoadedImage()));
	// inputMasks.setNir(new FlexibleImage(((LoadedImage)
	// inNir).getLoadedImageLabelField()));
	// } else {
	// try {
	// MyByteArrayInputStream optMainImageContent =
	// ResourceIOManager.getInputStreamMemoryCached(inNir.getURL());
	// MyByteArrayInputStream optLabelImageContent = inNir.getLabelURL() != null
	// ? ResourceIOManager.getInputStreamMemoryCached(inNir.getLabelURL())
	// : null;
	// c = load(inNir, input, inputMasks, FlexibleImageType.NIR,
	// optMainImageContent, optLabelImageContent);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load NIR image or reference: " +
	// inNir);
	// }
	// }
	// if (a != null)
	// BackgroundThreadDispatcher.addTask(a, parentPriority + 1, parentPriority
	// + 1);
	// if (b != null)
	// BackgroundThreadDispatcher.addTask(b, parentPriority + 1, parentPriority
	// + 1);
	// if (c != null)
	// BackgroundThreadDispatcher.addTask(c, parentPriority + 1, parentPriority
	// + 1);
	// BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c, });
	//
	// s.printTime();
	// }
	
	private void processStatisticalOutput(ImageData inVis,
			BlockProperties analysisResults) {
		if (output == null) {
			System.err.println("Internal Error: Output is NULL!!");
			throw new RuntimeException("Internal Error: Output is NULL!! 1");
		}
		
		for (BlockPropertyValue bpv : analysisResults.getProperties("RESULT_")) {
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
			BlockProperties analysisResults) {
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
		for (BlockPropertyValue bpv : analysisResults.getProperties("RESULT_")) {
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
	
	private BlockProperties processAngleWithinSnapshot(ImageSet id,
			final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles, int parentPriority)
			throws InterruptedException, InstantiationException,
			IllegalAccessException {
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
		// if (status != null)
		// status.setCurrentStatusText1("Load Images");
		
		// loadImages(inVis, inFluo, inNir, input, inputMasks, parentPriority +
		// 1);
		
		// if (input.hasAllThreeImages() && input.getSmallestHeight(true, true,
		// false) > 1) {
		// if (status != null)
		// status.setCurrentStatusText1("Images are loaded");
		
		// TODO: FIX THIS, ALL INFO SHOULD BE SUPPLIED USING THE
		// ImageProcessorOptions, see below!!!
		//
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
		
		// input.setVis(new ImageOperation(input.getVis()).scale(0.2,
		// 0.2).getImage());
		// input.setFluo(new ImageOperation(input.getFluo()).scale(0.2,
		// 0.2).getImage());
		
		// if (status != null)
		// status.setCurrentStatusText1("Process Analysis Pipeline");
		
		BlockProperties analysisResults = null;
		
		FlexibleImage resVis = null, resFluo = null, resNir = null;
		{
			ImageProcessor imageProcessor = getImageProcessor();
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = new BackgroundTaskStatusProviderSupportingExternalCallImpl(
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
			imageProcessor.setStatus(statusForThisTask);
			
			// TODO FIX: debugImageStack should be no input, only an output
			// TODO FIX: The Images Should be Loaded inside the pipeline,
			// not supplied by parameters!
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
	
}
