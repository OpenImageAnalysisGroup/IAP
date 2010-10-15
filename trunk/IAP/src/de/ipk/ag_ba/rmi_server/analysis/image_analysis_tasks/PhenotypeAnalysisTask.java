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

	/*
	 * (non-Javadoc)
	 * 
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTasks#
	 * setInputImage(java.awt.image.BufferedImage)
	 */
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
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE };
	}

	@Override
	public String getTaskDescription() {
		return "Analyse Plants Phenotype";
	}

	/**
	 * @deprecated Use
	 *             {@link #performAnalysis(int,int,BackgroundTaskStatusProviderSupportingExternalCall)}
	 *             instead
	 */
	@Deprecated
	@Override
	public void performAnalysis(final int maximumThreadCount,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCount, 1, status);
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
									processImage(limg, maximumThreadCountOnImageLevel, storeResultInDatabase, status);
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

	private void processImage(LoadedImage limg, int maximumThreadCount, DatabaseTarget storeResultInDatabase,
			final BackgroundTaskStatusProviderSupportingExternalCall status) {

		Color backgroundFill = PhenotypeAnalysisTask.BACKGROUND_COLOR;
		final int iBackgroundFill = backgroundFill.getRGB();

		BufferedImage img = limg.getLoadedImage();

		if (img == null) {
			System.out.println("Image is null: " + limg.toString());
			return;
		}

		// img = GravistoService.blurImage(img, 10);
		// ImageTools it = new ImageTools();
		// img = it.smooth(it.getPlanarImage(img), 4).getAsBufferedImage();

		final int w = img.getWidth();
		final int h = img.getHeight();
		final int rgbArray[] = new int[w * h];
		img.getRGB(0, 0, w, h, rgbArray, 0, w);

		for (ImagePreProcessor pre : preProcessors) {
			pre.processImage(limg, rgbArray, w, h, iBackgroundFill);
		}

		final double sidepercent = 0.10;

		final ObjectRef progress = new ObjectRef("", new Integer(0));
		ExecutorService run = null;
		if (maximumThreadCount > 1)
			run = Executors.newFixedThreadPool(maximumThreadCount);
		for (int ty = h - 1; ty >= 0; ty--) {
			final int y = ty;
			if (maximumThreadCount > 1)
				run.submit(processData(limg, w, rgbArray, iBackgroundFill, sidepercent, progress, y));
			else
				processData(limg, w, rgbArray, iBackgroundFill, sidepercent, progress, y).run();
		}

		if (maximumThreadCount > 1) {
			run.shutdown();
			try {
				run.awaitTermination(60 * 60, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}

		Geometry g = removeSingleDotsAndDetectGeometry(w, h, rgbArray, iBackgroundFill, limg);

		NumericMeasurement m;
		if (!limg.getSubstanceName().toUpperCase().contains("TOP")) {
			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": height", limg.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + getName() + ")");
			m.setValue(h - g.getTop());
			m.setUnit("pixel");
			output.add(m);

			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": width", limg.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + getName() + ")");
			m.setValue(h - g.getLeft() - (h - g.getRight()));
			m.setUnit("pixel");
			output.add(m);
		}
		m = new NumericMeasurement(limg, limg.getSubstanceName() + ": filled pixels", limg.getParentSample()
				.getParentCondition().getExperimentName()
				+ " (" + getName() + ")");
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

		int redLine = Color.RED.getRGB();

		int o = g.getTop() * w;
		if (g.getTop() < 3)
			o = 3 * w;
		for (int x = 0; x < w; x++) {
			if (o + x + w >= rgbArray.length)
				continue;
			rgbArray[o + x - w] = redLine;
			rgbArray[o + x] = redLine;
			rgbArray[o + x + w] = redLine;
		}
		for (int y = 0; y < h; y++) {
			o = g.getLeft() + y * w;
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
		img.setRGB(0, 0, w, h, rgbArray, 0, w);
		BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		res.setRGB(0, 0, w, h, rgbArray, 0, w);
		LoadedImage result = new LoadedImage(limg, res);
		result.getURL().setFileName("cleared_" + new File(limg.getURL().getFileName()));
		result.getURL().setPrefix(LoadedImageHandler.PREFIX);
		// result.showImageWindow();
		// result.getParentSample().getParentCondition().getParentSubstance().setName(
		// "Processed Images (" + limg.getExperimentName() + ")");
		if (storeResultInDatabase != null) {
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

	private Runnable processData(final ImageData imageData, final int w, final int[] rgbArray,
			final int iBackgroundFill, final double sidepercent, final ObjectRef progress, final int y) {
		return new Runnable() {

			@Override
			public void run() {
				String subN = imageData.getSubstanceName().toUpperCase();
				double factor = 1;
				if (subN.contains("FLUO"))
					factor = 0.2;

				boolean subNfluo = subN.contains("FLUO");
				boolean subNrgb = subN.contains("RGB");

				ArrayList<Integer> backgroundPixelsArr = new ArrayList<Integer>();
				ArrayList<Color> otherColor = new ArrayList<Color>();

				otherColor.add(new Color(0.25f, 0.36f, 0.75f)); // stick
				otherColor.add(new Color(0.3f, 0.5f, 0.9f)); // cave
				otherColor.add(new Color(0.2f, 0.5f, 0.9f)); // cave
				otherColor.add(new Color(0.5f, 0.7f, 1f)); // stick
				otherColor.add(new Color(0.7f, 0.9f, 1f)); // stick
				otherColor.add(new Color(0.8f, 0.8f, 1f)); // stick
				otherColor.add(new Color(0.1f, 0.2f, 0.4f)); // stick
				otherColor.add(new Color(0.5f, 0.5f, 0.5f)); // cave
				otherColor.add(new Color(0.5f, 0.5f, 0.5f)); // cave
				otherColor.add(new Color(0.5f, 1f, 1f)); // cave
				otherColor.add(new Color(0.8f, 0.8f, 0.8f)); // inner pot
				otherColor.add(new Color(0.6f, 0.8f, 1f)); // blue shadow
				otherColor.add(new Color(0.7f, 0.5f, 0.6f)); // blue shadow
				otherColor.add(new Color(0.9f, 0.75f, 0.9f)); // blue shadow //
				otherColor.add(new Color(0.5f, 0.8f, 1f)); // blue shadow //
				otherColor.add(new Color(0.5f, 0.8f, 1f)); // blue shadow //
				otherColor.add(new Color(0.5f, 0.7f, 1f)); // blue shadow //
				otherColor.add(new Color(0.25f, 0.36f, 0.73f)); // blue shadow //
				// (top, stick?)
				otherColor.add(new Color(0.7f, 0.6f, 0.6f)); //
				// inner pot
				otherColor.add(new Color(0.1f, 0.1f, 0.1f)); // soil
				otherColor.add(new Color(0.3f, 0.3f, 0.3f)); // soil
				otherColor.add(new Color(0.25f, 0.28f, 0.3f)); // side lanes
				otherColor.add(new Color(0.3f, 0.25f, 0.3f)); // side lanes
				otherColor.add(new Color(0.8f, 0.3f, 0.2f)); // side lanes (top,
				// red)
				otherColor.add(new Color(1f, 0.5f, 0.3f)); // side lanes
				// (top, // red dot)

				int x = 0;
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
					if (rgbArray[xyw] != iBackgroundFill) {
						Color cc = new Color(rgbArray[xyw]);
						if (cc.getBlue() >= 235)
							rgbArray[xyw] = iBackgroundFill;
						else {
							if (cc.getRed() <= 120 && cc.getGreen() <= 120 && cc.getBlue() <= 120)
								if (Math.abs(Math.abs(cc.getRed() - cc.getGreen()) + Math.abs(cc.getGreen() - cc.getBlue())) < 20)
									rgbArray[xyw] = iBackgroundFill;

						}
					}
					if (rgbArray[xyw] != iBackgroundFill) {
						/*
						 * Color ct = new Color(rgbArray[xyw]); for (Color cBlue1 :
						 * otherColor) { if (ColorUtil.deltaE2000(ct, cBlue1) < 15) {
						 * rgbArray[xyw] = iBackgroundFill; break; } }
						 */
						int ct = p;
						for (Color cBlue1 : otherColor) {
							if (ColorUtil.deltaE2000simu(ct, cBlue1.getRGB()) < epsilonB * factor) {
								rgbArray[xyw] = iBackgroundFill;
								break;
							}
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
							if (Math.abs(hsb[0] - 0.7) < 0.2)
								rgbArray[xyw] = iBackgroundFill;
							else if (Math.abs(hsb[0] - 0.9) < 0.2)
								rgbArray[xyw] = iBackgroundFill;
						}
				}

				synchronized (progress) {
					progress.setObject(new Integer((Integer) progress.getObject() + 1));
					// status.setCurrentStatusValue((int) ((Integer) progress
					// .getObject()
					// / (double) h * 100d));
				}
			}
		};
	}

	private Geometry removeSingleDotsAndDetectGeometry(int w, int h, int[] rgbArray, int iBackgroundFill,
			LoadedImage limg) {
		int searchRadius = 3;
		String subN = limg.getSubstanceName();
		if (subN.equalsIgnoreCase(ImageConfiguration.FluoTop.toString())) {
			searchRadius = 5;
		}
		if (subN.equalsIgnoreCase(ImageConfiguration.RgbTop.toString())) {
			searchRadius = 5;
		}
		if (subN.equalsIgnoreCase(ImageConfiguration.RgbSide.toString())) {
			searchRadius = 5;
		}
		int left = w;
		int right = 0;
		int top = h;

		for (int x = searchRadius; x < w - searchRadius; x++)
			for (int y = h - searchRadius; y > searchRadius; y--) {
				int o = x + y * w;
				if (y > h * 0.95) {
					rgbArray[o] = iBackgroundFill;
					continue;
				}
				if (rgbArray[o] == iBackgroundFill)
					continue;

				int brightness = 0;
				int re = 0;
				int gr = 0;
				int bl = 0;
				for (int a = -searchRadius; a <= searchRadius; a++)
					for (int b = -searchRadius; b <= searchRadius; b++) {
						if (a == 0 && b == 0)
							continue;
						int o2 = (x + a) + (y + b) * w;
						int r2 = rgbArray[o2];
						if (r2 != iBackgroundFill) {
							int red = (r2 >> 16) & 0xff;
							int green = (r2 >> 8) & 0xff;
							int blue = (r2) & 0xff;
							re += red;
							gr += green;
							bl += blue;
							brightness += red + green + blue;
						}
					}

				// if (subN.equalsIgnoreCase(ImageConfiguration.FluoTop.toString()))
				// {
				// if ((double) brightness / (double) (searchRadius * searchRadius *
				// (120 * 3)) < 1) {
				// for (int a = -searchRadius; a <= searchRadius; a++)
				// for (int b = -searchRadius; b <= searchRadius; b++)
				// rgbArray[o] = iBackgroundFill;
				// } else {
				// if (re * 0.1d < (gr + bl) / 2d) {
				// for (int a = -searchRadius; a <= searchRadius; a++)
				// for (int b = -searchRadius; b <= searchRadius; b++)
				// rgbArray[o] = iBackgroundFill;
				// }
				// }
				// }
				// if
				// (subN.equalsIgnoreCase(ImageConfiguration.FluoSide.toString())) {
				// if ((double) brightness / (double) (searchRadius * searchRadius *
				// (120 * 3)) < 1) {
				// for (int a = -searchRadius; a <= searchRadius; a++)
				// for (int b = -searchRadius; b <= searchRadius; b++)
				// rgbArray[o] = iBackgroundFill;
				// }
				// }
				if (subN.equalsIgnoreCase(ImageConfiguration.RgbTop.toString())) {
					if (gr < (re + bl) / 2) {
						for (int a = -searchRadius; a <= searchRadius; a++)
							for (int b = -searchRadius; b <= searchRadius; b++)
								rgbArray[o] = iBackgroundFill;
					}
				}
				if (subN.equalsIgnoreCase(ImageConfiguration.RgbSide.toString())) {
					if (gr * 0.95d < (re + bl) / 2d) {
						for (int a = -searchRadius; a <= searchRadius; a++)
							for (int b = -searchRadius; b <= searchRadius; b++)
								rgbArray[o] = iBackgroundFill;
					}
				}
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
		for (int x = searchRadius; x < w - searchRadius; x++) {
			for (int y = h - searchRadius; y > searchRadius; y--) {
				int o = x + y * w;
				if (rgbArray[o] != iBackgroundFill) {
					filled++;
				}
			}
		}

		return new Geometry(top, left, right, filled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#getOutput
	 * ()
	 */
	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		Collection<NumericMeasurementInterface> result = output;
		output = null;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#getName
	 * ()
	 */
	@Override
	public String getName() {
		return "Clear Background";
	}

	public void addPreprocessor(CutImagePreprocessor pre) {
		preProcessors.add(pre);
	}
}
