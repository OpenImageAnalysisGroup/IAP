package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClear_Arabidopsis_ir extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		debug = getBoolean("debug", false);
		if (processedMasks.fluo() == null) {
			return;
		}
		int back = options.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to IR
			if (processedMasks.ir() != null) {
				if (options.getCameraPosition() == CameraPosition.SIDE && getBoolean("enabled", true)) {
					processedMasks.setIr(
							processedMasks.ir().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().addBorder(0, 0, 0, 0, options.getBackground())
											.blur(getDouble("mask blur", 2)).getImage(),
									back).print("FILTERED IR IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP && getBoolean("enabled", true)) {
					processedMasks.setIr(
							processedMasks.ir().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().copy()
											// .addBorder(0, 0, 0, 0, options.getBackground())
											.blur(getDouble("mask blur", 2)).getImage(),
									back).print("FILTERED IR IMAGE", debug).getImage());
				}
			}
		}
	}
}
