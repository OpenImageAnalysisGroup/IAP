package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import info.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
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
	
	@Override
	public void setInput(Collection<Sample3D> input, Collection<NumericMeasurementInterface> optValidMeasurements, MongoDB m, int workOnSubset,
			int numberOfSubsets) {
		this.input = input;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
		this.m = m;
		databaseTarget = new DataBaseTargetMongoDB(true, m);
	}
	
	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}
	
	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE, ImageAnalysisType.MEASUREMENT };
	}
	
	public void debugOverrideAndEnableDebugStackStorage(boolean enable) {
		this.forceDebugStack = enable;
		this.forcedDebugStacks = new ArrayList<FlexibleImageStack>();
	}
	
	public ArrayList<FlexibleImageStack> getForcedDebugStackStorageResult() {
		return forcedDebugStacks;
	}
	
	@Override
	public void performAnalysis(final int maximumThreadCountParallelImages, final int maximumThreadCountOnImageLevel,
						final BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		status.setCurrentStatusValue(0);
		output = new ArrayList<NumericMeasurementInterface>();
		
		ArrayList<TreeMap<Double, ImageSet>> workload = new ArrayList<TreeMap<Double, ImageSet>>();
		
		addTopOrSideImagesToWorkset(workload, 0, analyzeTopImages(), analyzeSideImages());
		
		// workload = filterWorkload(workload, "Athletico");// "Rainbow Amerindian"); // Athletico
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int workloadSnapshots = workload.size();
		int idxxx = 0;
		final ArrayList<MyThread> wait = new ArrayList<MyThread>();
		int snapshotsWithNotAllNeededImageTypes = 0;
		int side = 0;
		int top = 0;
		for (TreeMap<Double, ImageSet> tm : workload)
			for (ImageSet md : tm.values()) {
				if (!md.hasAllNeededImageTypes())
					snapshotsWithNotAllNeededImageTypes++;
				if (md.isSide())
					side++;
				else
					top++;
			}
		if (snapshotsWithNotAllNeededImageTypes > 0)
			System.out.println("Warning: not all three images available for " + snapshotsWithNotAllNeededImageTypes + " snapshots!");
		System.out.println("Info: Workload Top/Side: " + top + "/" + side);
		final int workloadEqualAngleSnapshotSets = top + side;
		
		for (TreeMap<Double, ImageSet> tm : workload) {
			final TreeMap<Double, ImageSet> tmf = tm;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					processSnapshot(maximumThreadCountOnImageLevel, status, tso, workloadSnapshots, workloadEqualAngleSnapshotSets, tmf);
				}
			};
			idxxx++;
			r.run();
			// wait.add(BackgroundThreadDispatcher.addTask(r, getName() + " " + idxxx, getParentPriority() + 1, getParentPriority() + 1));
		}
		// try {
		// BackgroundThreadDispatcher.waitFor(wait);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		status.setCurrentStatusValueFine(100d);
		input = null;
	}
	
	private int getParentPriority() {
		return prio;
	}
	
	public void setParentPriority(int prio) {
		this.prio = prio;
	}
	
	private void processSnapshot(final int maximumThreadCountOnImageLevel, final BackgroundTaskStatusProviderSupportingExternalCall status,
			final ThreadSafeOptions tso, final int workloadSnapshots, final int workloadEqualAngleSnapshotSets, TreeMap<Double, ImageSet> tmf) {
		try {
			Sample3D inSample = null;
			TreeMap<Double, BlockProperties> analysisResults = new TreeMap<Double, BlockProperties>();
			TreeMap<Double, ImageData> analysisInput = new TreeMap<Double, ImageData>();
			for (Double angle : tmf.keySet()) {
				if (tmf.get(angle).getVIS() != null)
					inSample = (Sample3D) tmf.get(angle).getVIS().getParentSample();
				else
					continue;
				ImageData inImage = tmf.get(angle).getVIS();
				BlockProperties results = processAngleWithinSnapshot(tmf.get(angle), maximumThreadCountOnImageLevel, status,
								workloadEqualAngleSnapshotSets, getParentPriority());
				if (results != null) {
					analysisInput.put(angle, inImage);
					analysisResults.put(angle, results);
				}
			}
			if (inSample != null && !analysisResults.isEmpty()) {
				BlockProperties postprocessingResults = getImageProcessor().postProcessPipelineResults(
						inSample, analysisInput, analysisResults);
				processStatisticalAndVolumeSampleOutput(inSample, postprocessingResults);
			}
		} catch (Error e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tso.addInt(1);
		status.setCurrentStatusText1("Snapshot " + tso.getInt() + "/" + workloadSnapshots);
	}
	
	private ArrayList<TreeMap<Double, ImageSet>> filterWorkload(ArrayList<TreeMap<Double, ImageSet>> workload, String filter) {
		if (filter == null)
			return workload;
		ArrayList<TreeMap<Double, ImageSet>> res = new ArrayList<TreeMap<Double, ImageSet>>();
		for (TreeMap<Double, ImageSet> tm : workload)
			loopB: for (ImageSet is : tm.values()) {
				if (is.getSampleInfo() != null)
					if (is.getSampleInfo().getParentCondition().toString().contains(filter) && !is.getSampleInfo().getParentCondition().toString().contains("wet"))
						if (is.getSampleInfo().getTime() == 20 || is.getSampleInfo().getTime() == 31 || is.getSampleInfo().getTime() == 57) {
							res.add(tm);
							break loopB;
						}
			}
		return res;
	}
	
	private void addTopOrSideImagesToWorkset(ArrayList<TreeMap<Double, ImageSet>> workload, int max, boolean top, boolean side) {
		TreeMap<String, TreeMap<Double, ImageSet>> replicateId2ImageSetSide = new TreeMap<String, TreeMap<Double, ImageSet>>();
		for (Sample3D ins : input)
			for (Measurement md : ins) {
				if (md instanceof ImageData) {
					ImageData id = (ImageData) md;
					String keyA = id.getParentSample().getFullId() + ";" + id.getReplicateID();
					Double keyB = id.getPosition() != null ? id.getPosition() : 0d;
					if (!replicateId2ImageSetSide.containsKey(keyA)) {
						replicateId2ImageSetSide.put(keyA, new TreeMap<Double, ImageSet>());
					}
					if (!replicateId2ImageSetSide.get(keyA).containsKey(keyB)) {
						replicateId2ImageSetSide.get(keyA).put(keyB, new ImageSet(null, null, null, id.getParentSample()));
					}
					ImageSet is = replicateId2ImageSetSide.get(keyA).get(keyB);
					
					ImageConfiguration ic = ImageConfiguration.get(id.getSubstanceName());
					if (ic == ImageConfiguration.Unknown)
						ic = ImageConfiguration.get(id.getURL().getFileName());
					
					is.setSide(ic.isSide());
					
					if (ic == ImageConfiguration.RgbSide || ic == ImageConfiguration.RgbTop)
						is.setVis(id);
					if (ic == ImageConfiguration.FluoSide || ic == ImageConfiguration.FluoTop)
						is.setFluo(id);
					if (ic == ImageConfiguration.NirSide || ic == ImageConfiguration.NirTop)
						is.setNir(id);
				}
			}
		int workLoadIndex = workOnSubset;
		for (TreeMap<Double, ImageSet> is : replicateId2ImageSetSide.values()) {
			if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != 0) {
				workLoadIndex++;
				continue;
			} else
				workLoadIndex++;
			workload.add(is);
		}
		if (max > 0)
			while (workload.size() > max)
				workload.remove(0);
	}
	
	protected abstract boolean analyzeTopImages();
	
	protected abstract boolean analyzeSideImages();
	
	protected abstract ImageProcessor getImageProcessor();
	
	private MyThread saveImage(final ImageData id, final FlexibleImage image, final byte[] optLabelImageContent, final String labelFileExtension) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				if (output == null)
					return;
				if (optLabelImageContent == null) {
					if (image.getHeight() > 1) {
						LoadedImage loadedImage = new LoadedImage(id, image.getAsBufferedImage());
						ImageData imageRef = saveImageAndUpdateURL(loadedImage, databaseTarget, false);
						output.add(imageRef);
					}
				} else {
					if (image.getHeight() > 1) {
						LoadedImageStream loadedImage = new LoadedImageStream(id, image.getAsBufferedImage(), optLabelImageContent);
						loadedImage.setLabelURL(new IOurl(id.getURL().getPrefix(), null, "d_" + id.getURL().getFileName() + labelFileExtension));
						ImageData imageRef = saveImageAndUpdateURL(loadedImage, databaseTarget, true);
						if (imageRef == null) {
							System.out.println("ERROR #1");
						} else
							output.add(imageRef);
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
	// labelImg.setObject(optLabelImageContent != null ? ImageIO.read(optLabelImageContent) : null);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load label image: " + id);
	// }
	//
	// }
	// };
	//
	// MyThread a = BackgroundThreadDispatcher.addTask(r1, "Load main image", getParentPriority() + 1, getParentPriority() + 2);
	// MyThread b = BackgroundThreadDispatcher.addTask(r2, "Load label image", getParentPriority() + 1, getParentPriority() + 2);
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
	// optImageMasks.set(new FlexibleImage(li.getLoadedImageLabelField(), type));
	// else
	// System.out.println(">ERROR: Label field not available for:" + li);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load image: " + id);
	// }
	// }
	// };
	// return new MyThread(r, "Load Image " + (id != null && id.getURL() != null ? "" + id.getURL().getFileName() : "(null)"));
	// }
	
	protected ImageData saveImageAndUpdateURL(LoadedImage result, DatabaseTarget storeResultInDatabase, boolean processLabelUrl) {
		result.getURL().setFileName("c_" + result.getURL().getFileName());
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (result.getLabelURL() != null && processLabelUrl) {
			result.getLabelURL().setFileName("c_" + result.getLabelURL().getFileName());
			result.getLabelURL().setPrefix(LoadedDataHandler.PREFIX);
		}
		
		try {
			LoadedImage lib = result;
			result = storeResultInDatabase.saveImage(result);
			// add processed image to result
			if (result != null)
				return new ImageData(result.getParentSample(), result);
			else
				System.out.println("Could not save in DB: " + lib.getURL().toString());
		} catch (Exception e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		return null;
	}
	
	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
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
	// StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">LOAD", false);
	// MyThread a = null, b = null, c = null;
	// if (inVis != null) {
	// if (inVis.getAnnotationField("outlier") != null && inVis.getAnnotationField("outlier").equals("1")) {
	// System.out.println("INFO: Ignore marked outlier: " + inVis);
	// } else
	// if (inVis instanceof LoadedImage) {
	// input.setVis(new FlexibleImage(((LoadedImage) inVis).getLoadedImage()));
	// inputMasks.setVis(new FlexibleImage(((LoadedImage) inVis).getLoadedImageLabelField()));
	// } else {
	// try {
	// MyByteArrayInputStream optMainImageContent = ResourceIOManager.getInputStreamMemoryCached(inVis.getURL());
	// MyByteArrayInputStream optLabelImageContent = inVis.getLabelURL() != null ? ResourceIOManager.getInputStreamMemoryCached(inVis.getLabelURL())
	// : null;
	// a = load(inVis, input, inputMasks, FlexibleImageType.VIS,
	// optMainImageContent, optLabelImageContent);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load VIS image or reference: " + inVis);
	// }
	// }
	// }
	//
	// if (inFluo != null)
	// if (inFluo.getAnnotationField("outlier") != null && inVis.getAnnotationField("outlier").equals("1")) {
	// System.out.println("INFO: Ignore marked outlier: " + inFluo);
	// } else
	// if (inFluo instanceof LoadedImage) {
	// input.setFluo(new FlexibleImage(((LoadedImage) inFluo).getLoadedImage()));
	// inputMasks.setFluo(new FlexibleImage(((LoadedImage) inFluo).getLoadedImageLabelField()));
	// } else {
	// try {
	// MyByteArrayInputStream optMainImageContent = ResourceIOManager.getInputStreamMemoryCached(inFluo.getURL());
	// MyByteArrayInputStream optLabelImageContent = inFluo.getLabelURL() != null ? ResourceIOManager.getInputStreamMemoryCached(inFluo
	// .getLabelURL())
	// : null;
	// b = load(inFluo, input, inputMasks, FlexibleImageType.FLUO, optMainImageContent, optLabelImageContent);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load FLUO image or reference: " + inFluo);
	// }
	// }
	//
	// if (inNir != null)
	// if (inNir.getAnnotationField("outlier") != null && inNir.getAnnotationField("outlier").equals("1")) {
	// System.out.println("INFO: Ignore marked outlier: " + inNir);
	// } else
	// if (inNir instanceof LoadedImage) {
	// input.setNir(new FlexibleImage(((LoadedImage) inNir).getLoadedImage()));
	// inputMasks.setNir(new FlexibleImage(((LoadedImage) inNir).getLoadedImageLabelField()));
	// } else {
	// try {
	// MyByteArrayInputStream optMainImageContent = ResourceIOManager.getInputStreamMemoryCached(inNir.getURL());
	// MyByteArrayInputStream optLabelImageContent = inNir.getLabelURL() != null ? ResourceIOManager.getInputStreamMemoryCached(inNir.getLabelURL())
	// : null;
	// c = load(inNir, input, inputMasks, FlexibleImageType.NIR, optMainImageContent, optLabelImageContent);
	// } catch (Exception e) {
	// System.out.println(">ERROR: Could not load NIR image or reference: " + inNir);
	// }
	// }
	// if (a != null)
	// BackgroundThreadDispatcher.addTask(a, parentPriority + 1, parentPriority + 1);
	// if (b != null)
	// BackgroundThreadDispatcher.addTask(b, parentPriority + 1, parentPriority + 1);
	// if (c != null)
	// BackgroundThreadDispatcher.addTask(c, parentPriority + 1, parentPriority + 1);
	// BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c, });
	//
	// s.printTime();
	// }
	
	private void processStatisticalOutput(ImageData inVis, BlockProperties analysisResults) {
		for (BlockPropertyValue bpv : analysisResults.getProperties("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			NumericMeasurement3D m = new NumericMeasurement3D(inVis, bpv.getName(), inVis.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + getName() + ")");
			m.setValue(bpv.getValue());
			m.setUnit(bpv.getUnit());
			
			if (output != null)
				output.add(m);
		}
	}
	
	private void processStatisticalAndVolumeSampleOutput(Sample3D inSample, BlockProperties analysisResults) {
		for (String volumeID : analysisResults.getVolumeNames()) {
			VolumeData v = analysisResults.getVolume(volumeID);
			if (v != null) {
				analysisResults.setVolume(volumeID, null);
				
				try {
					databaseTarget.saveVolume((LoadedVolume) v, inSample, m, DBTable.SAMPLE, null, null);
					VolumeData volumeInDatabase = new VolumeData(inSample, v);
					volumeInDatabase.getURL().setPrefix(databaseTarget.getPrefix());
					output.add(volumeInDatabase);
					
					output.add(v);
				} catch (Exception e) {
					System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: Could not save volume data: " + e.getMessage());
					e.printStackTrace();
				}
				
			}
		}
		for (BlockPropertyValue bpv : analysisResults.getProperties("RESULT_")) {
			if (bpv.getName() == null)
				continue;
			
			NumericMeasurement3D m = new NumericMeasurement3D(new NumericMeasurement(inSample),
					bpv.getName(),
					inSample.getParentCondition().getExperimentName()
							+ " (" + getName() + ")");
			
			m.setValue(bpv.getValue());
			m.setUnit(bpv.getUnit());
			
			output.add(m);
		}
	}
	
	private void processAndOrSaveTiffImagesOrResultImages(ImageSet id,
			ImageData inVis, ImageData inFluo, ImageData inNir,
			FlexibleImageStack debugImageStack,
			FlexibleImage resVis, FlexibleImage resFluo, FlexibleImage resNir,
			int parentPriority) throws InterruptedException {
		StopWatch s = new StopWatch(SystemAnalysisExt.getCurrentTime() + ">SAVE IMAGE RESULTS", false);
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
				inVis.addAnnotationField("oldreference", inVis.getLabelURL().toString());
				inFluo.addAnnotationField("oldreference", inFluo.getLabelURL().toString());
				inNir.addAnnotationField("oldreference", inNir.getLabelURL().toString());
				
				inVis.setLabelURL(id.getVIS().getURL().copy());
				inFluo.setLabelURL(id.getFLUO().getURL().copy());
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
		s.printTime();
	}
	
	private BlockProperties processAngleWithinSnapshot(ImageSet id, final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status,
			final int workloadSnapshotAngles,
			int parentPriority) throws InterruptedException, InstantiationException, IllegalAccessException {
		ImageData inVis = id.getVIS() != null ? id.getVIS().copy() : null;
		ImageData inFluo = id.getFLUO() != null ? id.getFLUO().copy() : null;
		ImageData inNir = id.getNIR() != null ? id.getNIR().copy() : null;
		
		final FlexibleImageSet input = new FlexibleImageSet();
		final FlexibleImageSet inputMasks = new FlexibleImageSet();
		// if (status != null)
		// status.setCurrentStatusText1("Load Images");
		
		// loadImages(inVis, inFluo, inNir, input, inputMasks, parentPriority + 1);
		
		// if (input.hasAllThreeImages() && input.getSmallestHeight(true, true, false) > 1) {
		// if (status != null)
		// status.setCurrentStatusText1("Images are loaded");
		
		// TODO: FIX THIS, ALL INFO SHOULD BE SUPPLIED USING THE ImageProcessorOptions, see below!!!
		//
		input.setImageInfo(inVis, inFluo, inNir);
		inputMasks.setImageInfo(inVis, inFluo, inNir);
		
		boolean side = id.isSide();
		
		ImageProcessorOptions options = new ImageProcessorOptions();
		if (inVis != null && inVis.getPosition() != null)
			options.addDoubleSetting(Setting.ROTATION_ANGLE, inVis.getPosition());
		if (side)
			options.setCameraPosition(CameraPosition.SIDE);
		else
			options.setCameraPosition(CameraPosition.TOP);
		
		// TODO: FIX THIS, THE SETTING SHOULD BE RETRIEVED FROM THE ImageProcessorOptions
		// THERE SHOULD BE NO INPUT DEBUG STACK, ONLY A RESULT STACK
		FlexibleImageStack debugImageStack = null;
		boolean addDebugImages = IAPmain.isSettingEnabled(IAPfeature.SAVE_DEBUG_STACK);
		if (addDebugImages || forceDebugStack) {
			debugImageStack = new FlexibleImageStack();
		}
		
		// input.setVis(new ImageOperation(input.getVis()).scale(0.2, 0.2).getImage());
		// input.setFluo(new ImageOperation(input.getFluo()).scale(0.2, 0.2).getImage());
		
		// if (status != null)
		// status.setCurrentStatusText1("Process Analysis Pipeline");
		
		BlockProperties analysisResults = null;
		
		FlexibleImage resVis = null, resFluo = null, resNir = null;
		{
			ImageProcessor imageProcessor = getImageProcessor();
			BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = new BackgroundTaskStatusProviderSupportingExternalCallImpl("",
						"") {
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
			FlexibleMaskAndImageSet ret = imageProcessor.pipeline(
						options,
						input, inputMasks,
						maximumThreadCountOnImageLevel, debugImageStack);
			FlexibleImageSet pipelineResult = ret != null ? ret.getImages() : null;
			
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
						debugImageStack, resVis, resFluo, resNir,
						parentPriority);
		}
		return analysisResults;
		// } else {
		// System.err.println("ERROR: Not all three snapshots images could be loaded!");
		// return null;
		// }
	}
	
}
