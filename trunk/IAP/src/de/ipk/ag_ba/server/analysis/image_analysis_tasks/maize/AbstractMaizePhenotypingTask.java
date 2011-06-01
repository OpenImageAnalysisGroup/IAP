package de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.gui.actions.ImagePreProcessor;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.maize.ImageProcessor;
import de.ipk.ag_ba.image.operations.blocks.BlockPropertyValue;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperties;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.CutImagePreprocessor;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ImageSet;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.datastructures.LoadedImageStream;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

public abstract class AbstractMaizePhenotypingTask extends AbstractImageAnalysisTask {
	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	
	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	
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
		
		workload = filterWorkload(workload, "Rainbow Amerindian"); // Athletico
		
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
						ImageData visInputImage = id.getVIS().copy();
						ImageData fluo = id.getFLUO().copy();
						ImageData nir = id.getNIR().copy();
						
						if (visInputImage == null || nir == null || fluo == null) {
							System.err.println("ERROR: Internal Error: VIS/FLUO/NIR is NULL!");
							return;
						}
						double rV = visInputImage.getPosition() != null ? visInputImage.getPosition() : 0;
						double rF = fluo.getPosition() != null ? fluo.getPosition() : 0;
						double rN = nir.getPosition() != null ? nir.getPosition() : 0;
						if (Math.abs(rV - rF) > 0.001 || Math.abs(rF - rN) > 0.001) {
							System.err.println("ERROR: Internal Error: VIS/FLUO/NIR rotation angles differ!");
							return;
						}
						
						final FlexibleImageSet input = new FlexibleImageSet();
						final FlexibleImageSet inputMasks = new FlexibleImageSet();
						
						MyThread a = null, b = null, c = null;
						
						if (visInputImage instanceof LoadedImage) {
							input.setVis(new FlexibleImage(((LoadedImage) visInputImage).getLoadedImage()));
							inputMasks.setVis(new FlexibleImage(((LoadedImage) visInputImage).getLoadedImageLabelField()));
						} else {
							a = load(visInputImage, input, inputMasks, FlexibleImageType.VIS);
						}
						if (fluo instanceof LoadedImage) {
							input.setFluo(new FlexibleImage(((LoadedImage) fluo).getLoadedImage()));
							inputMasks.setFluo(new FlexibleImage(((LoadedImage) fluo).getLoadedImageLabelField()));
						} else {
							b = load(fluo, input, inputMasks, FlexibleImageType.FLUO);
						}
						if (nir instanceof LoadedImage) {
							input.setNir(new FlexibleImage(((LoadedImage) nir).getLoadedImage()));
							inputMasks.setNir(new FlexibleImage(((LoadedImage) nir).getLoadedImageLabelField()));
						} else {
							c = load(nir, input, inputMasks, FlexibleImageType.NIR);
						}
						// process images
						BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c });
						if (input.hasAllThreeImages() && input.getSmallestHeight(true, true, false) > 1) {
							
							input.setImageInfo(visInputImage, fluo, nir);
							inputMasks.setImageInfo(visInputImage, fluo, nir);
							
							boolean side = id.isSide();
							
							ImageProcessorOptions options = new ImageProcessorOptions(visInputImage, fluo, nir);
							if (side)
								options.setCameraTyp(CameraTyp.SIDE);
							else
								options.setCameraTyp(CameraTyp.TOP);
							ImageProcessor ptip = getImageProcessor(options);
							
							FlexibleImageStack debugImageStack = null;
							boolean addDebugImages = true;
							if (addDebugImages) {
								debugImageStack = new FlexibleImageStack();
							}
							
							// input.setVis(new ImageOperation(input.getVis()).scale(0.2, 0.2).getImage());
							// input.setFluo(new ImageOperation(input.getFluo()).scale(0.2, 0.2).getImage());
							
							final boolean cropResult = true;
							final boolean parameterSearch = true;
							
							final FlexibleImageSet pipelineResult = ptip.pipeline(
									input, inputMasks,
									maximumThreadCountOnImageLevel, debugImageStack, parameterSearch, cropResult).getImages();
							
							BlockProperties analysisResults = ptip.getSettings();
							
							for (BlockPropertyValue bpv : analysisResults.getProperties("RESULT_")) {
								if (bpv.getName() == null)
									continue;
								
								NumericMeasurement3D m = new NumericMeasurement3D(visInputImage, bpv.getName(), visInputImage.getParentSample()
										.getParentCondition().getExperimentName()
										+ " (" + getName() + ")");
								
								m.setValue(bpv.getValue());
								m.setUnit(bpv.getUnit());
								
								output.add(m);
							}
							
							byte[] buf = null;
							if (debugImageStack != null) {
								System.out.println("[s");
								MyByteArrayOutputStream mos = new MyByteArrayOutputStream();
								debugImageStack.saveAsLayeredTif(mos);
								debugImageStack.print("NNN");
								buf = mos.getBuff();
								
								System.out.println("f]");
							} else {
								visInputImage.setLabelURL(id.getVIS().getURL().copy());
								fluo.setLabelURL(id.getFLUO().getURL().copy());
								nir.setLabelURL(id.getNIR().getURL().copy());
							}
							if (pipelineResult.getVis() != null)
								saveImage(visInputImage, pipelineResult.getVis(), buf, ".tiff");
							if (pipelineResult.getFluo() != null)
								saveImage(fluo, pipelineResult.getFluo(), buf, ".tiff");
							if (pipelineResult.getNir() != null)
								saveImage(nir, pipelineResult.getNir(), buf, ".tiff");
							
						} else {
							System.err.println("ERROR: Not all three snapshots images could be loaded!");
						}
					} catch (Exception e) {
						ErrorMsg.addErrorMessage(e);
					}
					tso.addInt(1);
					status.setCurrentStatusValueFine(100d * tso.getInt() / wl);
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
				if (is.getSampleInfo().getParentCondition().toString().contains(filter) && is.getSampleInfo().getParentCondition().toString().contains("dry"))
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
	
	protected abstract ImageProcessor getImageProcessor(ImageProcessorOptions options);
	
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
