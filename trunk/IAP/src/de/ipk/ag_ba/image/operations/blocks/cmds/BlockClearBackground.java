package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.ObjectRef;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.gui.navigation_actions.ImagePreProcessor;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.MyThread;
import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.color.Color_CIE_Lab;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ColorHistogram;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.ColorHistogramEntry;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.Geometry;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk.ag_ba.server.databases.DatabaseTarget;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Condition;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Sample;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Substance;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.LoadedDataHandler;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * Uses LAB color classification, to categorize the input as foreground /
 * background. Then small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockClearBackground extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		BufferedImage clrearRgbImage = clearBackground(getInput().getMasks().getVis().getBufferedImage(),
				ImageConfiguration.RgbTop,
				options.getRgbEpsilonA(), options.getRgbEpsilonB(), options.getMaxThreadsPerImage());
		return new FlexibleImage(clrearRgbImage, FlexibleImageType.VIS);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		BufferedImage clearFluorImage = clearBackground(getInput().getMasks().getFluo().getBufferedImage(),
				ImageConfiguration.FluoTop,
				options.getFluoEpsilonA(), options.getFluoEpsilonB(), options.getMaxThreadsPerImage());
		return new FlexibleImage(clearFluorImage, FlexibleImageType.FLUO);
		
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// BufferedImage clearNirImage = clearBackground(getInput().getMasks().getNir().getBufferedImage(),
	// ImageConfiguration.NirTop,
	// options.getNearEpsilonA(), options.getNearEpsilonB(), options.getMaxThreadsPerImage());
	// return new FlexibleImage(clearNirImage, FlexibleImageType.NIR);
	// }
	
	private BufferedImage clearBackground(BufferedImage workImage, ImageConfiguration cameraTyp, double epsilonA,
			double epsiolonB, int maxThreadsPerImage) {
		
		SubstanceInterface substance = new Substance();
		substance.setName(cameraTyp.toString());
		ConditionInterface condition = new Condition(substance);
		Sample sample = new Sample(condition);
		LoadedImage limg = new LoadedImage(sample, workImage);
		limg.setURL(new IOurl(""));
		ArrayList<NumericMeasurementInterface> output = new ArrayList<NumericMeasurementInterface>();
		BlockClearBackground.clearBackgroundAndInterpretImage(limg, maxThreadsPerImage, null, null, false, output, null,
				epsilonA, epsiolonB);
		
		return workImage;
	}
	
	public static Runnable processRowYofImage(final ImageData imageData, final int w, final int[] rgbArray,
						final int[] rgbArrayNULL, final int iBackgroundFill, final double sidepercent, final ObjectRef progress,
						final int y, final double epsilonA, final double epsilonB, final ImageConfiguration config,
						final double arrayL[], final double arrayA[], final double arrayB[]) {
		return new Runnable() {
			
			@Override
			public void run() {
				String subN = imageData.getSubstanceName().toUpperCase();
				double factor = 1;
				if (subN.contains("FLUO"))
					factor = 0.2;
				
				int x = 0;
				if (config == ImageConfiguration.RgbSide || config == ImageConfiguration.RgbTop) {
					processRGBtopImageByLAB(true, imageData, w, rgbArray, iBackgroundFill, y, arrayL, arrayA, arrayB, x);
				} else
					if (config == ImageConfiguration.FluoTop || config == ImageConfiguration.FluoSide) {
						processFluoTopImageByLAB(imageData, w, rgbArray, iBackgroundFill, y, arrayL, arrayA, arrayB, x);
					} else {
						ArrayList<Color_CIE_Lab> backgroundPixelsArr = new ArrayList<Color_CIE_Lab>();
						boolean hasBackgroundImage = rgbArrayNULL != null && rgbArray.length == rgbArrayNULL.length;
						if (hasBackgroundImage) {
							processImageWithBackgroundImage(imageData, w, rgbArray, rgbArrayNULL, iBackgroundFill, y, epsilonA,
												config, arrayL, arrayA, arrayB, factor, x);
						} else {
							processImageWithoutBackgroundImage(imageData, w, rgbArray, iBackgroundFill, sidepercent, y, epsilonA,
												config, arrayL, arrayA, arrayB, factor, backgroundPixelsArr);
						}
						if (false)
							postProcessThisIsUnclearCode(w, rgbArray, iBackgroundFill, sidepercent, y, epsilonB, factor,
												backgroundPixelsArr);
					}
				synchronized (progress) {
					progress.setObject(new Integer((Integer) progress.getObject() + 1));
				}
			}
			
			private void processRGBtopImageByLAB(boolean simple, final ImageData imageData, final int w, final int[] rgbArray,
								final int iBackgroundFill, final int y, final double[] arrayL, final double[] arrayA,
								final double[] arrayB, int x) {
				simple = false;
				// if (y == 0)
				// System.out.println("LAB processing of RGB image..." + imageData.toString() + " - simple white mode: " + simple);
				if (simple) {
					int i = x + y * w;
					for (x = 0; x < w; x++) {
						double l = arrayL[i];
						// double a = arrayA[i];
						// double b = arrayB[i];
						if (l > 95) {
							rgbArray[i] = iBackgroundFill;
						}
						i++;
					}
				} else {
					int i = x + y * w;
					double[] bd = new double[] { 2, 2, 3, 4, 4, 4 };
					double[] bl = new double[] { 42, 42, 34, 57, 28, 83 };
					double[] ba = new double[] { -9, -4, -1, 11, -7, 0 };
					double[] bb = new double[] { 14, 9, 12, 33, 7, 12 };
					for (x = 0; x < w; x++) {
						double l = arrayL[i];
						double a = arrayA[i];
						double b = arrayB[i];
						if (a > 21 || b < 5) {
							// if (a > 21 || b < 5 || a > -6) { // a < -5 &&
							rgbArray[i] = iBackgroundFill;
						} else {
							for (int idx = 0; idx < ba.length; idx++) {
								double dist = Math.pow((ba[idx] - a) * (ba[idx] - a) + (bb[idx] - b) * (bb[idx] - b) + (bl[idx] - l) * (bl[idx] - l), 1 / 3d);
								if (dist < bd[idx])
									rgbArray[i] = iBackgroundFill;
							}
						}
						// else if (l >= 80 && Math.abs(a) <= 20 && Math.abs(b) <= 20) {
						// rgbArray[i] = Color.yellow.getRGB(); // iBackgroundFill;
						// }
						
						i++;
					}
				}
			}
			
			private void processFluoTopImageByLAB(final ImageData imageData, final int w, final int[] rgbArray,
								final int iBackgroundFill, final int y, final double[] arrayL, final double[] arrayA,
								final double[] arrayB, int x) {
				// if (y == 0)
				// System.out.println("LAB processing of FluoTop image..." + imageData.toString() + "");
				
				int i = x + y * w;
				for (x = 0; x < w; x++) {
					double l = arrayL[i];
					double a = arrayA[i];
					double b = arrayB[i];
					if (
					// rot
					!((l >= 15 && a >= 20 && b >= 20) ||
					// gelb
					(l >= 15 && Math.abs(a) <= 25 && b >= 70))) {
						rgbArray[i] = iBackgroundFill;
					}
					
					i++;
				}
			}
			
			private void postProcessThisIsUnclearCode(final int w, final int[] rgbArray, final int iBackgroundFill,
								final double sidepercent, final int y, final double epsilonB, double factor,
								ArrayList<Color_CIE_Lab> backgroundPixelsArr) {
				int x;
				for (x = 0; x < w * sidepercent; x++) {
					// empty
				}
				Color_CIE_Lab[] backgroundPixels = new Color_CIE_Lab[backgroundPixelsArr.size()];
				int i = 0;
				for (Color_CIE_Lab b : backgroundPixelsArr)
					backgroundPixels[i++] = b;
				for (; x < (int) (w - w * sidepercent); x++) {
					int xyw = x + y * w;
					double l = arrayL[xyw];
					double a = arrayA[xyw];
					double b = arrayB[xyw];
					
					for (Color_CIE_Lab c : backgroundPixels) {
						if (y < w * 0.03 || ColorUtil.deltaE2000(c, l, a, b) < epsilonB * factor) {
							rgbArray[xyw] = iBackgroundFill;
						}
					}
				}
			}
			
			private void processImageWithBackgroundImage(final ImageData imageData, final int w, final int[] rgbArray,
								final int[] rgbArrayNULL, final int iBackgroundFill, final int y, final double epsilonA,
								final ImageConfiguration config, final double[] arrayL, final double[] arrayA, final double[] arrayB,
								double factor, int x) {
				if (y == 0)
					System.out.println("Has background image... (" + imageData.toString() + ", " + config.toString() + ")");
				double ef = epsilonA * factor * 10;
				int i = x + y * w;
				for (x = 0; x < w; x++) {
					double l = arrayL[i];
					double a = arrayA[i];
					double b = arrayB[i];
					if (ColorUtil.deltaE2000(rgbArrayNULL[i], l, a, b) < ef) {
						rgbArray[i] = iBackgroundFill;
					}
					i++;
				}
			}
			
			private void processImageWithoutBackgroundImage(final ImageData imageData, final int w, final int[] rgbArray,
								final int iBackgroundFill, final double sidepercent, final int y, final double epsilonA,
								final ImageConfiguration config, final double[] arrayL, final double[] arrayA, final double[] arrayB,
								double factor, ArrayList<Color_CIE_Lab> backgroundPixelsArr) {
				int x;
				if (y == 0)
					System.out.println("Has NO background image, interpreting side border colors as background..."
										+ imageData.toString() + ", " + config.toString() + ")");
				for (x = 0; x < w * sidepercent; x++) {
					int xyw = x + y * w;
					double l = arrayL[xyw];
					double a = arrayA[xyw];
					double b = arrayB[xyw];
					
					rgbArray[xyw] = iBackgroundFill;
					boolean newBackgroundColor = true;
					for (Color_CIE_Lab c : backgroundPixelsArr) {
						if (ColorUtil.deltaE2000(c, l, a, b) < epsilonA * factor) {
							newBackgroundColor = false;
							break;
						}
					}
					if (newBackgroundColor)
						backgroundPixelsArr.add(new Color_CIE_Lab(l, a, b));
				}
				for (x = (int) (w - w * sidepercent); x < w; x++) {
					int off = x + y * w;
					double bpL = arrayL[off];
					double bpA = arrayA[off];
					double bpB = arrayB[off];
					rgbArray[x + y * w] = iBackgroundFill;
					boolean newBackgroundColor = true;
					for (Color_CIE_Lab c : backgroundPixelsArr) {
						if (ColorUtil.deltaE2000(c, bpL, bpA, bpB) < epsilonA * factor) {
							newBackgroundColor = false;
							break;
						}
					}
					if (newBackgroundColor)
						backgroundPixelsArr.add(new Color_CIE_Lab(bpL, bpA, bpB));
				}
			}
		};
	}
	
	public static void clearBackgroundAndInterpretImage(final LoadedImage limg, int maximumThreadCount,
						DatabaseTarget storeResultInDatabase, final BackgroundTaskStatusProviderSupportingExternalCall status,
						boolean isDataAnalysis, ArrayList<NumericMeasurementInterface> output,
						ArrayList<ImagePreProcessor> preProcessors, final double epsilonA, final double epsilonB) {
		
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
		final int arrayRGB[] = new int[w * h];
		img.getRGB(0, 0, w, h, arrayRGB, 0, w);
		
		if (!isDataAnalysis) {
			final double arrayL[] = new double[w * h];
			final double arrayA[] = new double[w * h];
			final double arrayB[] = new double[w * h];
			
			ColorUtil.getLABfromRGB(arrayRGB, arrayL, arrayA, arrayB);
			
			int rgbArrayOriginal[] = new int[w * h];
			img.getRGB(0, 0, w, h, rgbArrayOriginal, 0, w);
			
			int[] arrayRGBnull = null;
			if (imgNULL != null) {
				arrayRGBnull = new int[w * h];
				imgNULL.getRGB(0, 0, w, h, arrayRGBnull, 0, w);
			}
			if (preProcessors != null)
				for (ImagePreProcessor pre : preProcessors) {
					pre.processImage(limg, arrayRGB, arrayRGBnull, w, h, iBackgroundFill);
				}
			
			final double sidepercent = 0.10;
			
			final ImageConfiguration config = ImageConfiguration.get(limg.getSubstanceName());
			
			final ObjectRef progress = new ObjectRef("", new Integer(0));
			
			maximumThreadCount = 1;
			
			final int[] arrayRGBnullFF = arrayRGBnull;
			ArrayList<MyThread> wait = new ArrayList<MyThread>();
			for (int ty = h - 1; ty >= 0; ty--) {
				final int y = ty;
				if (maximumThreadCount > 1) {
					MyThread t = BackgroundThreadDispatcher.addTask(new Runnable() {
						@Override
						public void run() {
							BlockClearBackground.processRowYofImage(limg, w, arrayRGB, arrayRGBnullFF, iBackgroundFill, sidepercent, progress, y,
												epsilonA, epsilonB, config, arrayL, arrayA, arrayB).run();
						}
					}, "process row " + y, 3);
					wait.add(t);
				} else
					BlockClearBackground.processRowYofImage(limg, w, arrayRGB, arrayRGBnull, iBackgroundFill, sidepercent, progress, y, epsilonA,
									epsilonB, config, arrayL, arrayA, arrayB).run();
			}
			
			if (maximumThreadCount > 1) {
				BackgroundThreadDispatcher.waitFor(wait.toArray(new MyThread[] {}));
			}
			// PrintImage.printImage(arrayRGB, w, h);
			//
			// ImageOperation save = new ImageOperation(arrayRGB, w, h);
			// save.rotate(3);
			// save.saveImage("/Users/entzian/Desktop/sechsteBild.png");
			
			// if (true)
			// closingOpening(w, h, arrayRGB, rgbArrayOriginal, iBackgroundFill, limg, 1);
			//
			// boolean removeSmallSegments = true;
			//
			// if (removeSmallSegments)
			// if (config == ImageConfiguration.FluoTop)
			// removeSmallPartsOfImage(w, h, arrayRGB, iBackgroundFill, (int) (w * h * 0.002d));
			// else
			// removeSmallPartsOfImage(w, h, arrayRGB, iBackgroundFill, (int) (w * h * 0.002d));//
		} else {
			Geometry geo = PhenotypeAnalysisTask.detectGeometry(w, h, arrayRGB, iBackgroundFill, limg);
			
			NumericMeasurement m;
			boolean calcHistogram = false;
			if (calcHistogram) {
				ColorHistogram histogram = new ColorHistogram(20);
				histogram.countColorPixels(arrayRGB);
				double pixelCount = histogram.getNumberOfFilledPixels();
				for (ColorHistogramEntry che : histogram.getColorEntries()) {
					String sn = limg.getSubstanceName();
					int pos = sn.indexOf(".");
					if (pos > 0)
						sn = sn.substring(0, pos);
					m = new NumericMeasurement(limg, sn + "-r: " + che.getColorDisplayName(), limg.getParentSample()
							.getParentCondition().getExperimentName()
							+ " (" + PhenotypeAnalysisTask.getNameStatic() + ")");
					m.setValue(che.getNumberOfPixels() / pixelCount);
					m.setUnit("proportion");
					output.add(m);
					
					m = new NumericMeasurement(limg, sn + "-a: " + che.getColorDisplayName(), limg.getParentSample()
							.getParentCondition().getExperimentName()
							+ " (" + PhenotypeAnalysisTask.getNameStatic() + ")");
					m.setValue(pixelCount);
					m.setUnit("pixels");
					output.add(m);
				}
			}
			if (!limg.getSubstanceName().toUpperCase().contains("TOP")) {
				m = new NumericMeasurement(limg, limg.getSubstanceName() + ": height", limg.getParentSample()
						.getParentCondition().getExperimentName()
						+ " (" + PhenotypeAnalysisTask.getNameStatic() + ")");
				m.setValue(h - geo.getTop());
				m.setUnit("pixel");
				output.add(m);
				
				m = new NumericMeasurement(limg, limg.getSubstanceName() + ": width", limg.getParentSample()
						.getParentCondition().getExperimentName()
						+ " (" + PhenotypeAnalysisTask.getNameStatic() + ")");
				m.setValue(h - geo.getLeft() - (h - geo.getRight()));
				m.setUnit("pixel");
				output.add(m);
			}
			m = new NumericMeasurement(limg, limg.getSubstanceName() + ": filled pixels", limg.getParentSample()
					.getParentCondition().getExperimentName()
					+ " (" + PhenotypeAnalysisTask.getNameStatic() + ")");
			m.setValue(geo.getFilledPixels());
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
				
				int o = geo.getTop() * w;
				int lww = 20;
				if (geo.getTop() < lww + 1)
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
					o = geo.getLeft() + y * w;
					if (o - 1 < 0)
						continue;
					if (o + 1 >= h)
						continue;
					arrayRGB[o - 1] = redLine;
					arrayRGB[o] = redLine;
					arrayRGB[o + 1] = redLine;
					o = geo.getRight() + y * w;
					if (o - 1 >= 0)
						arrayRGB[o - 1] = redLine;
					arrayRGB[o] = redLine;
					arrayRGB[o + 1] = redLine;
				}
			}
		}
		img.setRGB(0, 0, w, h, arrayRGB, 0, w);
		BufferedImage res = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		res.setRGB(0, 0, w, h, arrayRGB, 0, w);
		LoadedImage result = new LoadedImage(limg, res);
		result.getURL().setFileName("cc_" + new File(limg.getURL().getFileName()));
		result.getURL().setPrefix(LoadedDataHandler.PREFIX);
		// result.showImageWindow();
		// result.getParentSample().getParentCondition().getParentSubstance().setName(
		// "Processed Images (" + limg.getExperimentName() + ")");
		if (isDataAnalysis && storeResultInDatabase != null) {
			try {
				LoadedImage lib = result;
				result = storeResultInDatabase.saveImage(result);
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
	
}
