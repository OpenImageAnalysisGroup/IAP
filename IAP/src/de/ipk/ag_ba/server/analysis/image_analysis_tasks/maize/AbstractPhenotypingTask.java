package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

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
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.CutImagePreprocessor;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ImageSet;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.datastructures.LoadedImageStream;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

public abstract class AbstractPhenotypingTask implements ImageAnalysisTask {
	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	
	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	private boolean forceDebugStack;
	private ArrayList<FlexibleImageStack> forcedDebugStacks;
	
	@Override
	public void setInput(Collection<NumericMeasurementInterface> input, MongoDB m, int workOnSubset, int numberOfSubsets) {
		this.input = input;
		this.workOnSubset = workOnSubset;
		this.numberOfSubsets = numberOfSubsets;
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
		
		ArrayList<ImageSet> workload = new ArrayList<ImageSet>();
		
		if (analyzeTopImages())
			addTopImagesToWorkset(workload, 0);
		
		if (analyzeSideImages())
			addSideImagesToWorkset(workload, 0);
		
		// workload = filterWorkload(workload, "Athletico");// "Rainbow Amerindian"); // Athletico
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		int idxxx = 0;
		final ArrayList<Thread> wait = new ArrayList<Thread>();
		int error = 0;
		int side = 0;
		int top = 0;
		for (ImageSet md : workload) {
			if (!md.hasAllImageTypes())
				error++;
			if (md.isSide())
				side++;
			else
				top++;
		}
		if (error > 0)
			System.out.println("Warning: not all three images available for " + error + " snapshots!");
		System.out.println("Info: Workload Top/Side: " + top + "/" + side);
		
		for (ImageSet md : workload) {
			final ImageSet id = md;
			Thread t = BackgroundThreadDispatcher.addTask(new Runnable() {
				
				@Override
				public void run() {
					try {
						ImageData inVis = id.getVIS() != null ? id.getVIS().copy() : null;
						ImageData inFluo = id.getFLUO() != null ? id.getFLUO().copy() : null;
						ImageData inNir = id.getNIR() != null ? id.getNIR().copy() : null;
						
						final FlexibleImageSet input = new FlexibleImageSet();
						final FlexibleImageSet inputMasks = new FlexibleImageSet();
						if (status != null)
							status.setCurrentStatusText2("Load Images");
						
						MyThread a = null, b = null, c = null;
						
						if (inVis != null)
							if (inVis instanceof LoadedImage) {
								input.setVis(new FlexibleImage(((LoadedImage) inVis).getLoadedImage()));
								inputMasks.setVis(new FlexibleImage(((LoadedImage) inVis).getLoadedImageLabelField()));
							} else {
								a = load(inVis, input, inputMasks, FlexibleImageType.VIS);
							}
						
						if (inFluo != null)
							if (inFluo instanceof LoadedImage) {
								input.setFluo(new FlexibleImage(((LoadedImage) inFluo).getLoadedImage()));
								inputMasks.setFluo(new FlexibleImage(((LoadedImage) inFluo).getLoadedImageLabelField()));
							} else {
								b = load(inFluo, input, inputMasks, FlexibleImageType.FLUO);
							}
						
						if (inNir != null)
							if (inNir instanceof LoadedImage) {
								input.setNir(new FlexibleImage(((LoadedImage) inNir).getLoadedImage()));
								inputMasks.setNir(new FlexibleImage(((LoadedImage) inNir).getLoadedImageLabelField()));
							} else {
								c = load(inNir, input, inputMasks, FlexibleImageType.NIR);
							}
						// process images
						BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c });
						if (input.hasAllThreeImages() && input.getSmallestHeight(true, true, false) > 1) {
							if (status != null)
								status.setCurrentStatusText2("Images are loaded");
							
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
							
							if (status != null)
								status.setCurrentStatusText1("Process Analysis Pipeline");
							
							BlockProperties analysisResults;
							
							FlexibleImage resVis, resFluo, resNir;
							{
								ImageProcessor imageProcessor = getImageProcessor();
								BackgroundTaskStatusProviderSupportingExternalCall statusForThisTask = new BackgroundTaskStatusProviderSupportingExternalCallImpl("",
										"") {
									double lastAdd = 0;
									
									@Override
									public synchronized void setCurrentStatusValueFine(double value) {
										super.setCurrentStatusValueFine(value);
										if (value > 0) {
											double add = value / wl;
											status.setCurrentStatusValueFineAdd(add - lastAdd);
											lastAdd = add;
										}
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
								// TODO: maximumThreadCound... should be no parameter but a setting!
								FlexibleImageSet pipelineResult = imageProcessor.pipeline(
										options,
										input, inputMasks,
										maximumThreadCountOnImageLevel, debugImageStack).getImages();
								
								resVis = pipelineResult.getVis();
								resFluo = pipelineResult.getFluo();
								resNir = pipelineResult.getNir();
								// TODO: pipelineResult.getDebugStack();
								
								// if (status != null)
								// status.setCurrentStatusText1("Pipeline Finished");
								
								analysisResults = imageProcessor.getSettings();
							}
							
							for (BlockPropertyValue bpv : analysisResults.getProperties("RESULT_")) {
								if (bpv.getName() == null)
									continue;
								
								NumericMeasurement3D m = new NumericMeasurement3D(inVis, bpv.getName(), inVis.getParentSample()
										.getParentCondition().getExperimentName()
										+ " (" + getName() + ")");
								
								m.setValue(bpv.getValue());
								m.setUnit(bpv.getUnit());
								
								output.add(m);
							}
							
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
								if (resVis != null)
									saveImage(inVis, resVis, buf, ".tiff");
								if (resFluo != null)
									saveImage(inFluo, resFluo, buf, ".tiff");
								if (resNir != null)
									saveImage(inNir, resNir, buf, ".tiff");
							}
						} else {
							System.err.println("ERROR: Not all three snapshots images could be loaded!");
						}
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
					tso.addInt(1);
					// status.setCurrentStatusValueFine(100d * tso.getInt() / wl);
					status.setCurrentStatusText1("Snapshot " + tso.getInt() + "/" + wl);
				}
			}, "process image " + idxxx, -10);
			idxxx++;
			wait.add(t);
			
		}
		
		try {
			BackgroundThreadDispatcher.waitFor(wait.toArray(new MyThread[] {}));
		} catch (InterruptedException e) {
			e.printStackTrace();
			ErrorMsg.addErrorMessage(e);
		}
		
		status.setCurrentStatusValueFine(100d);
		input = null;
	}
	
	private ArrayList<ImageSet> filterWorkload(ArrayList<ImageSet> workload, String filter) {
		if (filter == null)
			return workload;
		ArrayList<ImageSet> res = new ArrayList<ImageSet>();
		for (ImageSet is : workload)
			if (is.getSampleInfo() != null)
				if (is.getSampleInfo().getParentCondition().toString().contains(filter) && !is.getSampleInfo().getParentCondition().toString().contains("wet"))
					if (is.getSampleInfo().getTime() == 6 || is.getSampleInfo().getTime() == 25)
						res.add(is);
		return res;
	}
	
	private void addSideImagesToWorkset(ArrayList<ImageSet> workload, int max) {
		TreeMap<String, ImageSet> replicateId2ImageSetSide = new TreeMap<String, ImageSet>();
		for (Measurement md : input) {
			if (md instanceof ImageData) {
				ImageData id = (ImageData) md;
				String key = id.getParentSample().getFullId() + ";" + id.getReplicateID() + ";" + id.getPosition();
				if (!replicateId2ImageSetSide.containsKey(key)) {
					replicateId2ImageSetSide.put(key, new ImageSet(null, null, null, id.getParentSample()));
				}
				ImageSet is = replicateId2ImageSetSide.get(key);
				is.setSide(true);
				
				ImageConfiguration ic = ImageConfiguration.get(id.getSubstanceName());
				if (ic == ImageConfiguration.Unknown)
					ic = ImageConfiguration.get(id.getURL().getFileName());
				
				if (ic == ImageConfiguration.RgbSide)
					is.setVis(id);
				if (ic == ImageConfiguration.FluoSide)
					is.setFluo(id);
				if (ic == ImageConfiguration.NirSide)
					is.setNir(id);
			}
		}
		int workLoadIndex = workOnSubset;
		for (ImageSet is : replicateId2ImageSetSide.values()) {
			if (is.hasAllImageTypes()) {
				if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != 0) {
					workLoadIndex++;
					continue;
				} else
					workLoadIndex++;
				workload.add(is);
			}
		}
		if (max > 0)
			while (workload.size() > max)
				workload.remove(0);
	}
	
	private void addTopImagesToWorkset(ArrayList<ImageSet> workload, int max) {
		TreeMap<String, ImageSet> replicateId2ImageSetTop = new TreeMap<String, ImageSet>();
		for (Measurement md : input) {
			if (md instanceof ImageData) {
				ImageData id = (ImageData) md;
				String key = id.getParentSample().getFullId() + ";" + id.getReplicateID() + ";" + id.getPosition();
				if (!replicateId2ImageSetTop.containsKey(key)) {
					replicateId2ImageSetTop.put(key, new ImageSet(null, null, null, id.getParentSample()));
				}
				ImageSet is = replicateId2ImageSetTop.get(key);
				is.setSide(false);
				ImageConfiguration ic = ImageConfiguration.get(id.getSubstanceName());
				if (ic == ImageConfiguration.Unknown)
					ic = ImageConfiguration.get(id.getURL().getFileName());
				
				if (ic == ImageConfiguration.RgbTop)
					is.setVis(id);
				if (ic == ImageConfiguration.FluoTop)
					is.setFluo(id);
				if (ic == ImageConfiguration.NirTop)
					is.setNir(id);
			}
		}
		int workLoadIndex = workOnSubset;
		for (ImageSet is : replicateId2ImageSetTop.values()) {
			if (is.hasAllImageTypes()) {
				if (numberOfSubsets != 0 && workLoadIndex % numberOfSubsets != 0) {
					workLoadIndex++;
					continue;
				} else
					workLoadIndex++;
				workload.add(is);
			}
		}
		if (max > 0)
			while (workload.size() > max)
				workload.remove(0);
	}
	
	protected abstract boolean analyzeTopImages();
	
	protected abstract boolean analyzeSideImages();
	
	protected abstract ImageProcessor getImageProcessor();
	
	private void saveImage(final ImageData id, final FlexibleImage image, final byte[] optLabelImageContent, String labelFileExtension) {
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
	
	private MyThread load(final ImageData id, final FlexibleImageSet input, final FlexibleImageSet optImageMasks,
			final FlexibleImageType type) {
		return BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				try {
					System.out.print(".");
					LoadedImage li = IOmodule.loadImageFromFileOrMongo(id, true, optImageMasks != null);
					input.set(new FlexibleImage(li.getLoadedImage(), type));
					if (optImageMasks != null)
						if (li.getLoadedImageLabelField() != null)
							optImageMasks.set(new FlexibleImage(li.getLoadedImageLabelField(), type));
						else
							System.out.println("ERROR: Label field not available for:" + li);
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
			}
		}, "load " + type.name(), 0);
	}
	
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
	
}
