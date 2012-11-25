package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * @author klukas
 */
public class Bl_Arabidopsis_IRdiff_ir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processIRimage() {
		debug = getBoolean("debug", false);
		FlexibleImage warmBack = input().images().ir();
		FlexibleImage coldRef = input().masks().ir();
		if (warmBack != null && coldRef != null) {
			ArrayList<Double> warmBackgroundValues = new ArrayList<Double>();
			ImageOperation wb = warmBack.copy().io();
			
			if (getBoolean("Use Center Bottom Position as Temperature Reference", options.isBarley())) {
				int bx = (int) ((int) wb.getWidth() / 100d * getDouble("Pot Position X (percent)", 47.5));
				int by = (int) ((int) wb.getHeight() / 100d * getDouble("Pot Position Y (percent)",
						options.getCameraPosition() == CameraPosition.SIDE ? 95 : 47.5));
				int bw = (int) ((int) wb.getWidth() / 100d * getDouble("Pot Width (percent)", 5));
				int bh = (int) ((int) wb.getHeight() / 100d * getDouble("Pot Height (percent)", 5));
				if (debug) {
					wb.copy().canvas()
							.drawLine(bx, by, bx + bw, by + bh, Color.BLACK.getRGB(), 0, 2)
							.drawLine(bx + bw, by, bx, by + bh, Color.BLACK.getRGB(), 0, 2)
							.io().print("REFRENCE AREA");
				}
				wb = wb.clearArea(bx, by, bw, bh, options.getBackground(), true);
			}
			
			wb.print("Reference Image for Warm Background Detection", debug)
					.intensitySumOfChannel(false, false, false, false, warmBackgroundValues);
			Collections.sort(warmBackgroundValues);
			double sum = 0;
			for (int i = warmBackgroundValues.size() / 2; i < warmBackgroundValues.size() * 0.75d; i++)
				sum += warmBackgroundValues.get(i);
			double warmBackground = sum / (warmBackgroundValues.size() / 4);
			int[] res = coldRef.copy().getAs1A();
			for (int i = 0; i < res.length; i++)
				res[i] = IAPservice.getIRintensityDifferenceColor(
						IAPservice.getIRintenstityFromRGB(res[i], options.getBackground()) - warmBackground,
						options.getBackground());
			FlexibleImage gray = new FlexibleImage(coldRef.getWidth(), coldRef.getHeight(), res);
			if (options.isBarley())
				gray = gray.io().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(
								getInt("Adaptive_Threshold_Region_Size", 50),
								getInt("Adaptive_Threshold_Assumed_Background_Value", 00),
								options.getBackground(),
								getDouble("Adaptive_Threshold_K", 0.001)
						).getImage().print("ADAPT OUT", debug);
			return gray;
		} else
			return null;
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		FlexibleImage f = processedImages.ir();
		processedImages.setIr(processedMasks.ir());
		processedMasks.setIr(f);
	}
}
