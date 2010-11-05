package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.color.ColorUtil;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;

import de.ipk.ag_ba.gui.navigation_actions.CutImagePreprocessor;
import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.gui.navigation_actions.ImagePreProcessor;
import de.ipk.ag_ba.postgresql.MorphologicalOperators;
import de.ipk.ag_ba.postgresql.PixelSegmentation;
import de.ipk.ag_ba.rmi_server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.IOmodule;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisType;
import de.ipk.ag_ba.rmi_server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImage;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedImageHandler;

/**
 * @author klukas
 * 
 */
public class PhenotypeAnalysisTask extends AbstractImageAnalysisTask {

	public static final Color BACKGROUND_COLOR = new Color(255, 255, 255);

	private Collection<NumericMeasurementInterface> input = new ArrayList<NumericMeasurementInterface>();
	private ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
	private double epsilonA;
	private double epsilonB;

	private String login, pass;

	private final DatabaseTarget storeResultInDatabase;

	ArrayList<ImagePreProcessor> preProcessors = new ArrayList<ImagePreProcessor>();

	public PhenotypeAnalysisTask(DatabaseTarget storeResultInDatabase) {
		this.storeResultInDatabase = storeResultInDatabase;
	}

	public PhenotypeAnalysisTask(double epsilonA, double epsilonB, DatabaseTarget storeResultInDatabase) {
		this.epsilonA = epsilonA;
		this.epsilonB = epsilonB;
		this.storeResultInDatabase = storeResultInDatabase;
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

	@Override
	public void performAnalysis(final int maximumThreadCountParallelImages, final int maximumThreadCountOnImageLevel,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {

		status.setCurrentStatusValue(0);
		output = new ArrayList<NumericMeasurementInterface>();
		ArrayList<ImageData> workload = new ArrayList<ImageData>();
		for (Measurement md : input)
			if (md instanceof ImageData) {
				workload.add((ImageData) md);
			}

		final ThreadSafeOptions tsoLA = new ThreadSafeOptions();
		ExecutorService run = Executors.newFixedThreadPool(maximumThreadCountParallelImages, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				int i;
				synchronized (tsoLA) {
					tsoLA.addInt(1);
					i = tsoLA.getInt();
				}
				t.setName("Load and Analyse (" + i + ")");
				return t;
			}
		});

		final ThreadSafeOptions tso = new ThreadSafeOptions();
		final int wl = workload.size();
		for (Measurement md : workload) {
			if (md instanceof ImageData) {
				final ImageData id = (ImageData) md;
				run.submit(new Runnable() {
					@Override
					public void run() {
						LoadedImage limg = null;
						if (id != null) {
							if (id instanceof LoadedImage) {
								limg = (LoadedImage) id;
							} else {
								try {
									limg = IOmodule.loadImageFromFileOrMongo(id, login, pass);
									clearBackgroundAndInterpretImage(limg, maximumThreadCountOnImageLevel,
											storeResultInDatabase, status, true, login, pass, output, preProcessors, epsilonA,
											epsilonB);
								} catch (Exception e) {
									ErrorMsg.addErrorMessage(e);
								}
							}
							tso.addInt(1);
							status.setCurrentStatusValueFine(100d * tso.getInt() / wl);
							status.setCurrentStatusText1("Image " + tso.getInt() + "/" + wl);
						}
					}
				});
			}
		}

		run.shutdown();
		try {
			run.awaitTermination(365, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}

		status.setCurrentStatusValueFine(100d);
		input = null;
	}

	public static void clearBackgroundAndInterpretImage(LoadedImage limg, int maximumThreadCount,
			DatabaseTarget storeResultInDatabase, final BackgroundTaskStatusProviderSupportingExternalCall status,
			boolean dataAnalysis, String login, String pass, ArrayList<NumericMeasurementInterface> output,
			ArrayList<ImagePreProcessor> preProcessors, double epsilonA, double epsilonB) {

		Color backgroundFill = PhenotypeAnalysisTask.BACKGROUND_COLOR;
		final int iBackgroundFill = backgroundFill.getRGB();

		BufferedImage img = limg.getLoadedImage();

		BufferedImage imgNULL = limg.getLoadedImageLabelField();

		if (img == null) {
			System.out.println("Image is null: " + limg.toString());
			return;
		}

		// img = GravistoService.blurImage(img, 10);
		// ImageTools it = new ImageTools();
		// img = it.smooth(it.getPlanarImage(img), 4).getAsBufferedImage();

		final int w = img.getWidth();
		final int h = img.getHeight();
		int rgbArray[] = new int[w * h];
		img.getRGB(0, 0, w, h, rgbArray, 0, w);

		int rgbArrayOriginal[] = new int[w * h];
		img.getRGB(0, 0, w, h, rgbArrayOriginal, 0, w);

		int[] rgbArrayNULL = null;
		if (imgNULL != null) {
			rgbArrayNULL = new int[w * h];
			imgNULL.getRGB(0, 0, w, h, rgbArrayNULL, 0, w);
		}
		if (preProcessors != null)
			for (ImagePreProcessor pre : preProcessors) {
				pre.processImage(limg, rgbArray, rgbArrayNULL, w, h, iBackgroundFill);
			}

		final double sidepercent = 0.10;

		final ObjectRef progress = new ObjectRef("", new Integer(0));
		ExecutorService run = null;
		if (maximumThreadCount > 1)
			run = Executors.newFixedThreadPool(maximumThreadCount);
		for (int ty = h - 1; ty >= 0; ty--) {
			final int y = ty;
			if (maximumThreadCount > 1)
				run.submit(processData(limg, w, rgbArray, rgbArrayNULL, iBackgroundFill, sidepercent, progress, y,
						epsilonA, epsilonB));
			else
				processData(limg, w, rgbArray, rgbArrayNULL, iBackgroundFill, sidepercent, progress, y, epsilonA, epsilonB)
						.run();
		}

		if (maximumThreadCount > 1) {
			run.shutdown();
			try {
				run.awaitTermination(60 * 60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}

		removeSmallPartsOfImage(w, h, rgbArray, iBackgroundFill, limg, (int) (w * h * 0.005d));

		closingOpening(w, h, rgbArray, rgbArrayOriginal, iBackgroundFill, limg);

		if (dataAnalysis) {
			Geometry g = detectGeometry(w, h, rgbArray, iBackgroundFill, limg);

			NumericMeasurement m;
			{
				ColorHistogram histogram = new ColorHistogram(20);
				histogram.countColorPixels(rgbArray);
				double pixelCount = histogram.getNumberOfFilledPixels();
				for (ColorHistogramEntry che : histogram.getColorEntries()) {
					String sn = limg.getSubstanceName();
					int pos = sn.indexOf(".");
					if (pos > 0)
						sn = sn.substring(0, pos);
					m = new NumericMeasurement(limg, sn + "-r: " + che.getColorDisplayName(), limg.getParentSample()
							.getParentCondition().getExperimentName()
							+ " (" + getNameStatic() + ")");
					m.setValue(che.getNumberOfPixels() / pixelCount);
					m.setUnit("proportion");
					output.add(m);

					m = new NumericMeasurement(limg, sn + "-a: " + che.getColorDisplayName(), limg.getParentSample()
							.getParentCondition().getExperimentName()
							+ " (" + getNameStatic() + ")");
					m.setValue(pixelCount);
					m.setUnit("pixels");
					output.add(m);
				}
			}
			if (!limg.getSubstanceName().toUpperCase().contains("TOP")) {
				m = new NumericMeasurement(limg, limg.getSubstanceName() + ": height", limg.getParentSample()
						.getParentCondition().getExperimentName()
						+ " (" + getNameStatic() + ")");
				m.setValue(h - g.getTop());
				m.setUnit("pixel");
				output.add(m);

				m = new NumericMeasurement(limg, limg.getSubstanceName() + ": width", limg.getParentSample()
						.getParentCondition().getExperimentName()
						+ " (" + getNameStatic() + ")");
				m.setValue(h - g.getLeft() - (h - g.getRight()));
				m.setUnit("pixel");
				output.add(m);
			}
			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": filled pixels", limg.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + getNameStatic() + ")");
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
					if (o + x + w >= rgbArray.length)
						continue;
					for (int ii = lww; ii > 0; ii--)
						if (o + x - ii * w >= 0)
							rgbArray[o + x - ii * w] = redLine;
					// rgbArray[o + x] = redLine;
				}
				for (int y = 0; y < h; y++) {
					o = g.getLeft() + y * w;
					if (o - 1 < 0)
						continue;
					if (o + 1 >= h)
						continue;
					rgbArray[o - 1] = redLine;
					rgbArray[o] = redLine;
					rgbArray[o + 1] = redLine;
					o = g.getRight() + y * w;
					if (o - 1 >= 0)
						rgbArray[o - 1] = redLine;
					rgbArray[o] = redLine;
					rgbArray[o + 1] = redLine;
				}
			}
		}
		img.setRGB(0, 0, w, h, rgbArray, 0, w);
		BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		res.setRGB(0, 0, w, h, rgbArray, 0, w);
		LoadedImage result = new LoadedImage(limg, res);
		result.getURL().setFileName("cleared_" + new File(limg.getURL().getFileName()));
		result.getURL().setPrefix(LoadedImageHandler.PREFIX);
		// result.showImageWindow();
		// result.getParentSample().getParentCondition().getParentSubstance().setName(
		// "Processed Images (" + limg.getExperimentName() + ")");
		if (dataAnalysis && storeResultInDatabase != null) {
			try {
				LoadedImage lib = result;
				result = storeResultInDatabase.saveImage(result, login, pass);
				// add processed image to result
				if (result != null)
					output.add(new ImageData(result.getParentSample(), result));
				else
					System.out.println("Could not save in DB: " + lib.getURL().toString());
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
		} else {
			if (result != null)
				output.add(result);
		}
	}

	private static void closingOpening(int w, int h, int[] rgbArray, int[] rgbNonModifiedArray, int iBackgroundFill,
			LoadedImage limg) {
		int[][] image = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				if (color != iBackgroundFill) {
					image[x][y] = 1;
				} else {
					image[x][y] = 0;
				}
			}
		}
		MorphologicalOperators op = new MorphologicalOperators(image);
		op.doClosing();
		int[][] mask = image = op.getResultImage();

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (mask[x][y] == 0)
					rgbArray[x + y * w] = iBackgroundFill;
				else
					rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
			}
		}
	}

	private static void removeSmallPartsOfImage(int w, int h, int[] rgbArray, int iBackgroundFill, LoadedImage limg,
			int cutOff) {
		int[][] image = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				if (color != iBackgroundFill) {
					image[x][y] = 1;
				} else {
					image[x][y] = 0;
				}
			}
		}
		PixelSegmentation ps = new PixelSegmentation(image);
		ps.doPixelSegmentation();
		int[] clusterSizes = ps.getClusterCounts();
		int[] clusterPerimeter = ps.getPerimeter();
		double[] clusterCircleSimilarity = ps.getCircuitRatio();

		for (int clusterID = 0; clusterID < clusterSizes.length; clusterID++)
			if (clusterSizes[clusterID] > 50 * 50)
				System.out.println("ID: " + clusterID + ", SIZE: " + clusterSizes[clusterID] + ", PERIMETER: "
						+ clusterPerimeter[clusterID] + ", CIRCLE? " + clusterCircleSimilarity[clusterID] + ", PFLANZE? "
						+ (clusterCircleSimilarity[clusterID] < 0.013));

		int[][] mask = ps.getImageMask();
		// ArrayList<Color> colors = Colors.get(cl);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int clusterID = mask[x][y];
				// rgbArray[x + y * w] = clusterID != 0 ? clusterID :
				// Color.YELLOW.getRGB();
				// rgbArray[x + y * w] = colors.get(clusterID).getRGB();

				if (clusterSizes[clusterID] < cutOff || clusterCircleSimilarity[clusterID] > 0.013)
					rgbArray[x + y * w] = iBackgroundFill;
				// else if (clusterID != 0)
				// System.out.println("ID: " + clusterID + ", SIZE: " +
				// clusterSizes[clusterID] + ", PERIMETER: "
				// + clusterPerimeter[clusterID] + ", CIRCLE? " +
				// clusterCircleSimilarity[clusterID]);
			}
		}
	}

	private static Runnable processData(final ImageData imageData, final int w, final int[] rgbArray,
			final int[] rgbArrayNULL, final int iBackgroundFill, final double sidepercent, final ObjectRef progress,
			final int y, final double epsilonA, final double epsilonB) {
		return new Runnable() {

			@Override
			public void run() {
				String subN = imageData.getSubstanceName().toUpperCase();
				double factor = 1;
				if (subN.contains("FLUO"))
					factor = 0.2;

				boolean subNfluo = ImageConfiguration.get(imageData.getSubstanceName()) == ImageConfiguration.FluoSide
						|| ImageConfiguration.get(imageData.getSubstanceName()) == ImageConfiguration.FluoTop;
				boolean subNrgb = ImageConfiguration.get(imageData.getSubstanceName()) == ImageConfiguration.RgbSide
						|| ImageConfiguration.get(imageData.getSubstanceName()) == ImageConfiguration.RgbTop;

				ArrayList<Integer> backgroundPixelsArr = new ArrayList<Integer>();

				int x = 0;
				boolean hasBackgroundImage = rgbArrayNULL != null && rgbArray.length == rgbArrayNULL.length;
				if (hasBackgroundImage) {
					double ef = epsilonA * factor * 10;
					for (int i = 0; i < rgbArray.length; i++) {
						if (ColorUtil.deltaE2000simu(rgbArray[i], rgbArrayNULL[i]) < ef) {
							rgbArray[i] = iBackgroundFill;
						}
					}
				} else {
					for (x = 0; x < w * sidepercent; x++) {
						int bp = rgbArray[x + y * w];
						rgbArray[x + y * w] = iBackgroundFill;
						boolean newBackgroundColor = true;
						for (Integer c : backgroundPixelsArr) {
							if (ColorUtil.deltaE2000simu(c, bp) < epsilonA * factor) {
								newBackgroundColor = false;
								break;
							}
						}
						if (newBackgroundColor)
							backgroundPixelsArr.add(bp);
					}
					for (x = (int) (w - w * sidepercent); x < w; x++) {
						int bp = rgbArray[x + y * w];
						rgbArray[x + y * w] = iBackgroundFill;
						boolean newBackgroundColor = true;
						for (Integer c : backgroundPixelsArr) {
							if (ColorUtil.deltaE2000simu(c, bp) < epsilonA * factor) {
								newBackgroundColor = false;
								break;
							}
						}
						if (newBackgroundColor)
							backgroundPixelsArr.add(bp);
					}
				}
				for (x = 0; x < w * sidepercent; x++) {
					// empty
				}
				int[] backgroundPixels = new int[backgroundPixelsArr.size()];
				int i = 0;
				for (int b : backgroundPixelsArr)
					backgroundPixels[i++] = b;
				for (; x < (int) (w - w * sidepercent); x++) {
					int xyw = x + y * w;
					int p = rgbArray[xyw];

					for (Integer c : backgroundPixels) {
						if (y < w * 0.03 || ColorUtil.deltaE2000simu(c, p) < epsilonB * factor) {
							rgbArray[xyw] = iBackgroundFill;
						}
					}

					if (subNfluo)
						if (rgbArray[xyw] != iBackgroundFill) {
							Color c = new Color(rgbArray[xyw]);
							float hsb[] = new float[3];
							Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
							if (hsb[2] < 0.5)
								rgbArray[xyw] = iBackgroundFill;
						}
					if (subNrgb)
						if (rgbArray[xyw] != iBackgroundFill) {
							Color c = new Color(rgbArray[xyw]);
							float hsb[] = new float[3];
							Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
							// if (hsb[1] < 0.25 && hsb[2] > 0.5)
							// rgbArray[xyw] = Color.YELLOW.getRGB();//
							// iBackgroundFill;
							// else
							if (Math.abs(hsb[0] - 0.8) < 0.3)
								rgbArray[xyw] = iBackgroundFill;
						}
				}

				synchronized (progress) {
					progress.setObject(new Integer((Integer) progress.getObject() + 1));
				}
			}
		};
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

	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		Collection<NumericMeasurementInterface> result = output;
		output = null;
		return result;
	}

	private static String getNameStatic() {
		return "Phenotype Analysis";
	}

	@Override
	public String getName() {
		return getNameStatic();
	}

	public void addPreprocessor(CutImagePreprocessor pre) {
		preProcessors.add(pre);
	}

	public static LoadedImage clearBackground(LoadedImage image, int maximumThreadCount, String login, String pass) {
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		clearBackgroundAndInterpretImage(image, maximumThreadCount, null, null, false, login, pass, output, null, 5d, 10d);
		LoadedImage res = (LoadedImage) output.iterator().next();
		return res;
	}
}
