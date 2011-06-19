package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.awt.image.BufferedImage;
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
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.analysis.phytochamber.PhytochamberTopImageProcessor;
import de.ipk.ag_ba.image.operations.ImageConverter;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.server.analysis.CutImagePreprocessor;
import de.ipk.ag_ba.server.analysis.IOmodule;
import de.ipk.ag_ba.server.analysis.ImageAnalysisTask;
import de.ipk.ag_ba.server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk.ag_ba.server.datastructures.LoadedImageStream;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author klukas
 */
public class PhytochamberAnalysisTask implements ImageAnalysisTask {
	
	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	
	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();
	protected DatabaseTarget databaseTarget;
	private int workOnSubset;
	private int numberOfSubsets;
	
	public PhytochamberAnalysisTask() {
		// empty
	}
	
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
	public String getTaskDescription() {
		return "Analyse Plants Phenotype";
	}
	
	@Override
	public void performAnalysis(final int maximumThreadCountParallelImages, final int maximumThreadCountOnImageLevel,
						final BackgroundTaskStatusProviderSupportingExternalCall status) {
		
		status.setCurrentStatusValue(0);
		output = new ArrayList<NumericMeasurementInterface>();
		ArrayList<ImageSet> workload = new ArrayList<ImageSet>();
		TreeMap<String, ImageSet> replicateId2ImageSet = new TreeMap<String, ImageSet>();
		for (Measurement md : input) {
			if (md instanceof ImageData) {
				ImageData id = (ImageData) md;
				String key = id.getParentSample().getFullId() + ";" + id.getReplicateID();
				if (!replicateId2ImageSet.containsKey(key)) {
					replicateId2ImageSet.put(key, new ImageSet(null, null, null));
				}
				ImageSet is = replicateId2ImageSet.get(key);
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
		for (ImageSet is : replicateId2ImageSet.values()) {
			if (is.hasAllImageTypes()) {
				if (workLoadIndex % numberOfSubsets != 0) {
					workLoadIndex++;
					continue;
				} else
					workLoadIndex++;
				workload.add(is);
				// if (workload.size() > 1)
				// break;
			}
		}
		
		// Collection jobs = new ArrayList();
		
		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		int idxxx = 0;
		final ArrayList<Thread> wait = new ArrayList<Thread>();
		System.out.println("Workload: " + wl);
		
		for (ImageSet md : workload) {
			final ImageSet id = md;
			Thread t = BackgroundThreadDispatcher.addTask(new Runnable() {
				
				@Override
				public void run() {
					try {
						ImageData vis = id.getVIS();
						ImageData fluo = id.getFLUO();
						ImageData nir = id.getNIR();
						
						if (vis == null || nir == null || fluo == null)
							return;
						
						final FlexibleImageSet input = new FlexibleImageSet();
						
						MyThread a = null, b = null, c = null;
						
						if (vis instanceof LoadedImage) {
							input.setVis(new FlexibleImage(((LoadedImage) vis).getLoadedImage()));
						} else {
							a = load(vis, input, FlexibleImageType.VIS);
						}
						if (fluo instanceof LoadedImage) {
							input.setFluo(new FlexibleImage(((LoadedImage) fluo).getLoadedImage()));
						} else {
							b = load(fluo, input, FlexibleImageType.FLUO);
						}
						if (nir instanceof LoadedImage) {
							input.setFluo(new FlexibleImage(((LoadedImage) nir).getLoadedImage()));
						} else {
							c = load(nir, input, FlexibleImageType.NIR);
						}
						// process images
						BackgroundThreadDispatcher.waitFor(new MyThread[] { a, b, c });
						if (input.hasAllThreeImages()) {
							
							ImageProcessorOptions options = new ImageProcessorOptions();
							if (vis != null && vis.getPosition() != null)
								options.addDoubleSetting(Setting.ROTATION_ANGLE, vis.getPosition());
							
							PhytochamberTopImageProcessor ptip = new PhytochamberTopImageProcessor(options);
							
							FlexibleImageStack debugImageStack = null;
							boolean addDebugImages = false;
							if (addDebugImages) {
								debugImageStack = new FlexibleImageStack();
							}
							
							// input.setVis(new ImageOperation(input.getVis()).scale(0.2, 0.2).getImage());
							// input.setFluo(new ImageOperation(input.getFluo()).scale(0.2, 0.2).getImage());
							
							final boolean cropResult = true;
							final boolean parameterSearch = true;
							
							final FlexibleImageSet pipelineResult = ptip.pipeline(
									input,
									maximumThreadCountOnImageLevel, debugImageStack, parameterSearch, cropResult).getImages();
							
							MyThread e = statisticalAnalaysis(vis, pipelineResult.getVis());
							MyThread f = statisticalAnalaysis(fluo, pipelineResult.getFluo());
							MyThread g = statisticalAnalaysis(nir, pipelineResult.getNir());
							boolean multiThreaded = true;
							if (!multiThreaded) {
								e.run();
								f.run();
								g.run();
							} else
								BackgroundThreadDispatcher.waitFor(new MyThread[] { e, f, g });
							
							byte[] buf = null;
							if (debugImageStack != null) {
								MyByteArrayOutputStream mos = new MyByteArrayOutputStream();
								debugImageStack.saveAsLayeredTif(mos);
								buf = mos.getBuff();
								
								saveImage(vis, pipelineResult.getVis(), buf, ".tiff");
								saveImage(fluo, pipelineResult.getFluo(), buf, ".tiff");
								saveImage(nir, pipelineResult.getNir(), buf, ".tiff");
							}
						} else {
							System.err.println("Warning: not all three image types available for snapshot!");
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
	
	private MyThread statisticalAnalaysis(final ImageData id, final FlexibleImage image) {
		return BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				LoadedImage loadedImage = new LoadedImage(id, image.getAsBufferedImage());
				ArrayList<NumericMeasurementInterface> res = statisticalAnalysisOfResultImage(loadedImage, PhytochamberAnalysisTask.this.getName());
				synchronized (output) {
					output.addAll(res);
				}
			}
		}, "statistic image analysis", 4);
	}
	
	private void saveImage(final ImageData id, final FlexibleImage image, final byte[] optLabelImageContent, String labelFileExtension) {
		if (optLabelImageContent == null) {
			LoadedImage loadedImage = new LoadedImage(id, image.getAsBufferedImage());
			ImageData imageRef = saveImageAndUpdateURL(loadedImage, databaseTarget);
			output.add(imageRef);
		} else {
			LoadedImageStream loadedImage = new LoadedImageStream(id, image.getAsBufferedImage(), optLabelImageContent);
			loadedImage.setLabelURL(new IOurl(id.getURL().getPrefix(), null, "d_" + id.getURL().getFileName() + labelFileExtension));
			ImageData imageRef = saveImageAndUpdateURL(loadedImage, databaseTarget);
			if (imageRef == null) {
				System.out.println("ERROR #1");
			} else
				output.add(imageRef);
		}
	}
	
	private MyThread load(final ImageData id, final FlexibleImageSet input,
			final FlexibleImageType type) {
		return BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				// System.out.println("Load Image");
				try {
					LoadedImage li = IOmodule.loadImageFromFileOrMongo(id, true, false);
					input.set(new FlexibleImage(li.getLoadedImage(), type));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				// System.out.println("Finished Load Image");
			}
		}, "load " + type.name(), 0);
	}
	
	public static ArrayList<NumericMeasurementInterface> statisticalAnalysisOfResultImage(LoadedImage limg,
						String experimentNameExtension) {
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		
		BufferedImage b = limg.getLoadedImage();
		int w = b.getWidth();
		int h = b.getHeight();
		int[] arrayRGB = ImageConverter.convertBIto1A(b);
		int iBackgroundFill = PhenotypeAnalysisTask.BACKGROUND_COLORint;
		Geometry g = detectGeometry(w, h, arrayRGB, iBackgroundFill, limg);
		
		NumericMeasurement3D m;
		boolean calcHistogram = true;
		if (calcHistogram) {
			String sn = limg.getSubstanceName();
			ColorHistogram histogram = new ColorHistogram(sn.startsWith("fluo") ? 100 : 10);
			histogram.countColorPixels(arrayRGB);
			double pixelCount = histogram.getNumberOfFilledPixels();
			for (ColorHistogramEntry che : histogram.getColorEntries()) {
				int pos = sn.indexOf(".");
				if (pos > 0)
					sn = sn.substring(0, pos);
				m = new NumericMeasurement3D(limg, sn + ": hue=" + che.getHue(), limg.getParentSample()
									.getParentCondition().getExperimentName()
									+ " (" + experimentNameExtension + ")");
				m.setValue(che.getNumberOfPixels() / pixelCount);
				m.setUnit("proportion");
				// m.setPosition((double) che.getHue());
				// m.setPositionUnit("hue");
				// if (m.getValue() >= 0.01 / 5)
				output.add(m);
				
				// m = new NumericMeasurement(limg, sn + "-a: " + che.getColorDisplayName(), limg.getParentSample()
				// .getParentCondition().getExperimentName()
				// + " (" + experimentNameExtension + ")");
				// m.setValue(pixelCount);
				// m.setUnit("pixels");
				// output.add(m);
			}
		}
		if (!limg.getSubstanceName().toUpperCase().contains("TOP")) {
			m = new NumericMeasurement3D(limg, limg.getSubstanceName() + ": height", limg.getParentSample()
								.getParentCondition().getExperimentName()
								+ " (" + experimentNameExtension + ")");
			m.setValue(h - g.getTop());
			m.setUnit("pixel");
			output.add(m);
			
			m = new NumericMeasurement3D(limg, limg.getSubstanceName() + ": width", limg.getParentSample()
								.getParentCondition().getExperimentName()
								+ " (" + experimentNameExtension + ")");
			m.setValue(h - g.getLeft() - (h - g.getRight()));
			m.setUnit("pixel");
			output.add(m);
		}
		m = new NumericMeasurement3D(limg, limg.getSubstanceName() + ": filled pixels", limg.getParentSample()
							.getParentCondition().getExperimentName()
							+ " (" + experimentNameExtension + ")");
		m.setValue(g.getFilledPixels());
		m.setUnit("pixel");
		output.add(m);
		
		// m = new NumericMeasurement(limg, "filled (percent) ("
		// +
		// limg.getParentSample().getParentCondition().getParentSubstance().getName()
		// + ")", limg.getParentSample()
		// .getParentCondition().getExperimentName()
		// + " (" + getName() + ")");
		// m.setValue((double) g.getFilledPixels() / (w * h) * 100d);
		// m.setUnit("%");
		// output.add(m);
		
		boolean red = false;
		if (red) {
			int redLine = Color.RED.getRGB();
			
			int o = g.getTop() * w;
			int lww = 20;
			if (g.getTop() < lww + 1)
				o = 8 * w;
			for (int x = 0; x < w; x++) {
				if (o + x + w >= arrayRGB.length)
					continue;
				for (int ii = lww; ii > 0; ii--)
					if (o + x - ii * w >= 0)
						arrayRGB[o + x - ii * w] = redLine;
				// rgbArray[o + x] = redLine;
			}
			for (int y = 0; y < h; y++) {
				o = g.getLeft() + y * w;
				if (o - 1 < 0)
					continue;
				if (o + 1 >= h)
					continue;
				arrayRGB[o - 1] = redLine;
				arrayRGB[o] = redLine;
				arrayRGB[o + 1] = redLine;
				o = g.getRight() + y * w;
				if (o - 1 >= 0)
					arrayRGB[o - 1] = redLine;
				arrayRGB[o] = redLine;
				arrayRGB[o + 1] = redLine;
			}
		}
		return output;
	}
	
	private static Geometry detectGeometry(int w, int h, int[] rgbArray, int iBackgroundFill, LoadedImage limg) {
		
		int left = w;
		int right = 0;
		int top = h;
		
		for (int x = 0; x < w; x++)
			for (int y = h - 1; y > 0; y--) {
				int o = x + y * w;
				if (y > h * 0.95) {
					rgbArray[o] = iBackgroundFill;
					continue;
				}
				if (rgbArray[o] == iBackgroundFill)
					continue;
				
				if (rgbArray[o] != iBackgroundFill) {
					if (x < left)
						left = x;
					if (x > right)
						right = x;
					if (y < top)
						top = y;
				}
			}
		
		long filled = 0;
		for (int x = 0; x < w; x++) {
			for (int y = h - 1; y > 0; y--) {
				int o = x + y * w;
				if (rgbArray[o] != iBackgroundFill) {
					filled++;
				}
			}
		}
		
		return new Geometry(top, left, right, filled);
	}
	
	protected ImageData saveImageAndUpdateURL(LoadedImage result, DatabaseTarget storeResultInDatabase) {
		result.getURL().setFileName("c_" + result.getURL().getFileName());
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		
		if (result.getLabelURL() != null) {
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
	
	@Override
	public String getName() {
		return "Phytochamber Image Analysis";
	}
	
	public void addPreprocessor(CutImagePreprocessor pre) {
		preProcessors.add(pre);
	}
}
