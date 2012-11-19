package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import java.util.ArrayList;
import java.util.Collections;

import de.ipk.ag_ba.gui.util.IAPservice;
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
		FlexibleImage warmBack = input().images().ir();
		FlexibleImage coldRef = input().masks().ir();
		if (warmBack != null && coldRef != null) {
			ArrayList<Double> warmBackgroundValues = new ArrayList<Double>();
			warmBack.io().intensitySumOfChannel(false, false, false, false, warmBackgroundValues);
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
			boolean debug = false;
			if (options.isBarley())
				gray = gray.io().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(50, 20,
								options.getBackground(), 0.05).getImage().print("ADAPT OUT", debug);
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
