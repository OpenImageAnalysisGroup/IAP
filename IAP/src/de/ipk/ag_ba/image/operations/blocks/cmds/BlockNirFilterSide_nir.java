package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * pipeline processing for nir image (white balancing, ClearBackgroundByComparingNullImageAndImage)
 * 
 * @author pape, klukas
 */
public class BlockNirFilterSide_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
				FlexibleImage nirMask = getInput().getMasks().getNir();
				// compare images
				boolean debug = false;
				int blackDiff = options.getIntSetting(Setting.B_Diff_NIR);
				int whiteDiff = options.getIntSetting(Setting.W_Diff_NIR);
				// getInput().getImages().getNir().getIO().subtractGrayImages(nirMask).print("subimg");
				nirMask = new ImageOperation(getInput().getImages().getNir()).print("img", debug).compare()
							.compareGrayImages(nirMask.print("ref", debug),
									// 20, 12,
									blackDiff, whiteDiff,
									// 250, 12,
									// 40, 40,
									options.getBackground()).thresholdBlueHigherThan(180).print("result", false).getImage(); // 150
				
				getInput().getMasks().setNir(nirMask);
			}
		}
		
		return getInput();
	}
}
