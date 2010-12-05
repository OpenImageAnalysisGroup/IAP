package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.navigation_actions.CutImagePreprocessor;
import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.gui.navigation_actions.ImagePreProcessor;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.image_utils.FlexibleImage;
import de.ipk.ag_ba.image_utils.FlexibleImageSet;
import de.ipk.ag_ba.image_utils.FlexibleImageType;
import de.ipk.ag_ba.image_utils.ImageConverter;
import de.ipk.ag_ba.image_utils.PhytochamberTopImageProcessor;
import de.ipk.ag_ba.rmi_server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.IOmodule;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.rmi_server.databases.DataBaseTargetMongoDB;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImageHandler;

/**
 * @author klukas
 */
public class PhytochamberAnalysisTask extends AbstractImageAnalysisTask {

	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();

	private String login, pass;

	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();
	protected DatabaseTarget databaseTarget;

	public PhytochamberAnalysisTask() {
		databaseTarget = new DataBaseTargetMongoDB(true);
	}

	public void setInput(Collection<NumericMeasurementInterface> input, String login, String pass) {
		this.input = input;
		this.login = login;
		this.pass = pass;
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

	@SuppressWarnings("unchecked")
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
				if (ic == ImageConfiguration.RgbTop)
					is.setVis(id);
				if (ic == ImageConfiguration.FluoTop)
					is.setFluo(id);
				if (ic == ImageConfiguration.NirTop)
					is.setNir(id);
			}
		}
		for (ImageSet is : replicateId2ImageSet.values()) {
			if (is.hasAllImageTypes()) {
				workload.add(is);
				// if (workload.size() > 1)
				// break;
			}
		}

		// Collection jobs = new ArrayList();

		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		int idxxx = 0;
		ArrayList<Thread> wait = new ArrayList<Thread>();
		for (ImageSet md : workload) {
			final ImageSet id = md;
			Thread t = BackgroundThreadDispatcher.addTask(new Runnable() {

				@Override
				public void run() {
					try {
						final ImageData vis = id.getVIS();
						final ImageData fluo = id.getFLUO();
						final ImageData nir = id.getNIR();

						if (vis == null || nir == null || fluo == null)
							return;

						final FlexibleImageSet input = new FlexibleImageSet();

						if (vis instanceof LoadedImage) {
							input.setVis(new FlexibleImage(((LoadedImage) vis).getLoadedImage()));
						} else {
							load(vis, input, FlexibleImageType.VIS);
						}
						if (fluo instanceof LoadedImage) {
							input.setFluo(new FlexibleImage(((LoadedImage) fluo).getLoadedImage()));
						} else {
							load(fluo, input, FlexibleImageType.FLUO);
						}
						if (nir instanceof LoadedImage) {
							input.setFluo(new FlexibleImage(((LoadedImage) nir).getLoadedImage()));
						} else {
							load(nir, input, FlexibleImageType.NIR);
						}
						// process images
						input.waitForThreeImages();
						if (input.hasAllThreeImages()) {
							PhytochamberTopImageProcessor ptip = new PhytochamberTopImageProcessor(input);
							final FlexibleImageSet pipelineResult = ptip.pipeline(maximumThreadCountOnImageLevel).getImages();

							Thread a = statisticalAnalaysis(vis, pipelineResult.getVis());
							Thread b = statisticalAnalaysis(fluo, pipelineResult.getFluo());
							Thread c = statisticalAnalaysis(nir, pipelineResult.getNir());
							BackgroundThreadDispatcher.waitFor(new Thread[] { a, b, c });
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
			}, "process image " + idxxx, -1);
			wait.add(t);

		}

		BackgroundThreadDispatcher.waitFor(wait.toArray(new Thread[] {}));

		status.setCurrentStatusValueFine(100d);
		input = null;
	}

	private Thread statisticalAnalaysis(final ImageData id, final FlexibleImage image) {
		return BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				LoadedImage loadedImage = new LoadedImage(id, image.getBufferedImage());
				ArrayList<NumericMeasurementInterface> res = statisticalAnalysisOfResultImage(loadedImage, getNameStatic());
				ImageData imageRef = saveImageAndUpdateURL(loadedImage, databaseTarget);
				output.addAll(res);
				output.add(imageRef);
			}
		}, "statistic image analysis", 4);
	}

	private void load(final ImageData id, final FlexibleImageSet input, final FlexibleImageType type) {
		BackgroundThreadDispatcher.addTask(new Runnable() {
			@Override
			public void run() {
				try {
					input.set(new FlexibleImage(IOmodule.loadImageFromFileOrMongo(id, login, pass).getLoadedImage(), type));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
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

		NumericMeasurement m;
		boolean calcHistogram = false;
		if (calcHistogram) {
			ColorHistogram histogram = new ColorHistogram(10);
			histogram.countColorPixels(arrayRGB);
			double pixelCount = histogram.getNumberOfFilledPixels();
			for (ColorHistogramEntry che : histogram.getColorEntries()) {
				String sn = limg.getSubstanceName();
				int pos = sn.indexOf(".");
				if (pos > 0)
					sn = sn.substring(0, pos);
				m = new NumericMeasurement(limg, sn + "-r: " + che.getColorDisplayName(), limg.getParentSample()
									.getParentCondition().getExperimentName()
									+ " (" + experimentNameExtension + ")");
				m.setValue(che.getNumberOfPixels() / pixelCount);
				m.setUnit("proportion");
				output.add(m);

				m = new NumericMeasurement(limg, sn + "-a: " + che.getColorDisplayName(), limg.getParentSample()
									.getParentCondition().getExperimentName()
									+ " (" + experimentNameExtension + ")");
				m.setValue(pixelCount);
				m.setUnit("pixels");
				output.add(m);
			}
		}
		if (!limg.getSubstanceName().toUpperCase().contains("TOP")) {
			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": height", limg.getParentSample()
								.getParentCondition().getExperimentName()
								+ " (" + experimentNameExtension + ")");
			m.setValue(h - g.getTop());
			m.setUnit("pixel");
			output.add(m);

			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": width", limg.getParentSample()
								.getParentCondition().getExperimentName()
								+ " (" + experimentNameExtension + ")");
			m.setValue(h - g.getLeft() - (h - g.getRight()));
			m.setUnit("pixel");
			output.add(m);
		}
		m = new NumericMeasurement(limg, limg.getSubstanceName() + ": filled pixels", limg.getParentSample()
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
		result.getURL().setFileName("cleared_" + result.getURL().getFileName());
		result.getURL().setPrefix(LoadedImageHandler.PREFIX);

		try {
			LoadedImage lib = result;
			result = storeResultInDatabase.saveImage(result, login, pass);
			// add processed image to result
			if (result != null)
				return new ImageData(result.getParentSample(), result);
			else
				System.out.println("Could not save in DB: " + lib.getURL().toString());
		} catch (Exception e) {
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

	private static String getNameStatic() {
		return "Phytochamber Image Analysis";
	}

	@Override
	public String getName() {
		return getNameStatic();
	}

	public void addPreprocessor(CutImagePreprocessor pre) {
		preProcessors.add(pre);
	}
}
