package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas, entzian
 */
public class BlRootsSharpenImage extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().images().vis();
		if (img != null)
			img = img.io().copy().blur(options.getIntSetting(Setting.ROOT_BLUR_RADIUS)).sharpen(options.getIntSetting(Setting.ROOT_NUMBER_OF_RUNS_SHARPEN))
					.getImage(); // .print("RES", false);
		return img;
	}
}
