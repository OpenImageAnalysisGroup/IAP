package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author klukas
 */
public class BlIRdiff extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processIRmask() {
		FlexibleImage warmBack = input().masks().ir();
		if (warmBack != null) {
			debug = getBoolean("debug", false);
			ArrayList<Double> warmBackgroundValues = new ArrayList<Double>();
			ImageOperation wb = warmBack.copy().io();
			
			if (getBoolean("Use Center Bottom Position as Temperature Reference", false)) {
				int bx = (int) (wb.getWidth() / 100d * getDouble("Pot Position X (percent)", 47.5));
				int by = (int) (wb.getHeight() / 100d * getDouble("Pot Position Y (percent)",
						options.getCameraPosition() == CameraPosition.SIDE ? 95 : 47.5));
				int bw = (int) (wb.getWidth() / 100d * getDouble("Pot Width (percent)", 5));
				int bh = (int) (wb.getHeight() / 100d * getDouble("Pot Height (percent)", 5));
				if (debug) {
					wb.copy().canvas()
							.drawLine(bx, by, bx + bw, by + bh, Color.BLACK.getRGB(), 0, 2)
							.drawLine(bx + bw, by, bx, by + bh, Color.BLACK.getRGB(), 0, 2)
							.io().show("REFRENCE AREA");
				}
				wb = wb.clearArea(bx, by, bw, bh, options.getBackground(), true);
			}
			
			wb.show("Reference Image for Warm Background Detection", debug)
					.intensitySumOfChannel(false, false, false, false, warmBackgroundValues);
			Collections.sort(warmBackgroundValues);
			double sum = 0;
			int n = 0;
			double perc = getDouble("reference top n percent", 10) / 100d;
			for (int i = (int) (warmBackgroundValues.size() - warmBackgroundValues.size() * perc); i < warmBackgroundValues.size(); i++) {
				sum += warmBackgroundValues.get(i);
				n++;
			}
			double warmBackground = sum / n;
			int[] res = warmBack.copy().getAs1A();
			for (int i = 0; i < res.length; i++)
				res[i] = IAPservice.getIRintensityDifferenceColor(
						IAPservice.getIRintenstityFromRGB(res[i], options.getBackground()) - warmBackground,
						options.getBackground(), getDouble("temperature scaling", 10));
			FlexibleImage gray = new FlexibleImage(warmBack.getWidth(), warmBack.getHeight(), res);
			if (getBoolean("Adaptive Thresholding", false))
				gray = gray.io().show("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(
								getInt("Adaptive_Threshold_Region_Size", 50),
								getInt("Adaptive_Threshold_Assumed_Background_Value", 00),
								options.getBackground(),
								getDouble("Adaptive_Threshold_K", 0.001)
						).getImage().show("ADAPT OUT", debug);
			return gray;
		} else
			return null;
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
