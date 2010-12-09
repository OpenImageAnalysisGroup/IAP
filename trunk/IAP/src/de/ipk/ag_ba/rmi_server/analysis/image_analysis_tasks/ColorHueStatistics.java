package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.ErrorMsg;
import org.color.ColorUtil;
import org.color.Color_CIE_Lab;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk.ag_ba.rmi_server.analysis.AbstractImageAnalysisTask;
import de.ipk.ag_ba.rmi_server.analysis.IOmodule;
import de.ipk.ag_ba.rmi_server.analysis.ImageAnalysisType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NumericMeasurementInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.NumericMeasurement3D;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.LoadedImage;

/**
 * @author klukas
 */
public class ColorHueStatistics extends AbstractImageAnalysisTask {

	private Collection<NumericMeasurementInterface> output;
	private final int colorCount;
	private Collection<NumericMeasurementInterface> input;

	public ColorHueStatistics(int colorCount) {
		this.colorCount = colorCount;
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * getInputTypes()
	 */
	@Override
	public ImageAnalysisType[] getInputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.IMAGE, ImageAnalysisType.COLORED_VOLUME };
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * getOutputTypes()
	 */
	@Override
	public ImageAnalysisType[] getOutputTypes() {
		return new ImageAnalysisType[] { ImageAnalysisType.MEASUREMENT };
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#getOutput
	 * ()
	 */
	@Override
	public Collection<NumericMeasurementInterface> getOutput() {
		return output;
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * getTaskDescription()
	 */
	@Override
	public String getTaskDescription() {
		return "Calculates a Color-Hue Histogram";
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performAnalysis(int,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	/**
	 * @deprecated Use {@link #performAnalysis(int,int,BackgroundTaskStatusProviderSupportingExternalCall)} instead
	 */
	@Deprecated
	@Override
	public void performAnalysis(int maximumThreadCount, BackgroundTaskStatusProviderSupportingExternalCall status) {
		performAnalysis(maximumThreadCount, 1, status);
	}

	/*
	 * (non-Javadoc)
	 * @seede.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#
	 * performAnalysis(int,
	 * org.BackgroundTaskStatusProviderSupportingExternalCall)
	 */
	@Override
	public void performAnalysis(int maximumThreadCountParallelImages, int maximumThreadCountOnImageLevel,
						BackgroundTaskStatusProviderSupportingExternalCall status) {
		ExecutorService run = Executors.newFixedThreadPool(maximumThreadCountParallelImages);

		for (Measurement meas : input) {
			if (meas instanceof ImageData) {
				final ImageData i = (ImageData) meas;
				run.submit(new Runnable() {
					@Override
					public void run() {
						LoadedImage li = null;
						if (i instanceof LoadedImage)
							li = (LoadedImage) i;
						else {
							// load image
							try {
								li = IOmodule.loadImageFromFileOrMongo(i);
							} catch (Exception e) {
								ErrorMsg.addErrorMessage(e);
								System.out.println("Error loading file: " + i.getURL().toString());
							}
						}
						if (li != null) {
							// analyze
							BufferedImage img = li.getLoadedImage();
							final int w = img.getWidth();
							final int h = img.getHeight();
							final int rgbArray[] = new int[w * h];
							img.getRGB(0, 0, w, h, rgbArray, 0, w);

							int[] histogram = new int[colorCount];

							for (int c : rgbArray) {
								Color c1 = new Color(c);
								Color_CIE_Lab cCL1 = ColorUtil.colorXYZ2CIELAB(ColorUtil.colorRGB2XYZ(c1.getRed(), c1
													.getGreen(), c1.getBlue()));
								double d = cCL1.getL();
								int bin = (int) (d * colorCount);
								histogram[bin]++;
							}
							for (int bin = 0; bin < colorCount; bin++) {
								NumericMeasurement nnn = li.copyDataAndPath();
								NumericMeasurement3D nm = new NumericMeasurement3D(nnn.getParentSample(), nnn);
								double b = 1d / colorCount * bin;
								nm.setUnit("CIE_Lab");
								nm.setPosition(b);
								nm.setPositionUnit("L");
								nm.setValue(histogram[bin]);
								output.add(nm);
							}
						}
					}
				});
			}
		}

		try {
			run.shutdown();
			run.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			ErrorMsg.addErrorMessage(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#setInput
	 * (java.util.Collection, java.lang.String, java.lang.String)
	 */
	@Override
	public void setInput(Collection<NumericMeasurementInterface> input, MongoDB m) {
		this.input = input;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_ba.graffiti.plugins.server.ImageAnalysisTask#getName
	 * ()
	 */
	@Override
	public String getName() {
		return "Color Hue Statistics";
	}

}
