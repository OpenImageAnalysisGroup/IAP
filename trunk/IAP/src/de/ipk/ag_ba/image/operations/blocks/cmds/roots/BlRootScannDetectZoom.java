package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Detect the zoom level of the scanned image.
 * If there is a lot of white area around the gray area, then the normal
 * wide zoom is detected. If nearly all is gray, then the image zoom is too big,
 * this is the zoom level which should have been avoided. If such image is processed,
 * the setting "options.isHighResVisCamera()" is set to TRUE.
 * 
 * @author klukas
 */
public class BlRootScannDetectZoom extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = input().masks().vis();
		options.setHigherResVisCamera(false);
		if (img != null) {
			int grayPixels = img.copy().io().invert().thresholdBlueHigherThan(10).countFilledPixels();
			int allPixels = img.getWidth() * img.getHeight();
			if (grayPixels / (double) allPixels > 0.85d) {
				options.setHigherResVisCamera(true);
			}
		}
		return super.processVISmask();
	}
	
}
