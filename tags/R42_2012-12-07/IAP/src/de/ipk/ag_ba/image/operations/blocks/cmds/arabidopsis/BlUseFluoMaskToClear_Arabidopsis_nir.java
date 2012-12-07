package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClear_Arabidopsis_nir extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (processedMasks.fluo() == null) {
			return;
		}
		int back = options.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to NIR
			if (processedMasks.nir() != null) {
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					processedMasks.setNir(
							processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().addBorder(0, 00, 0, 0, options.getBackground())
											.blur(getDouble("blur fluo mask", 2d))
											.getImage(),
									back).print("FILTERED NIR IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					double f = (double) processedMasks.nir().getWidth() / (double) processedMasks.fluo().getWidth()
							* getDouble("fluo scaling factor X", 0.98d);
					processedMasks.setNir(
							processedMasks.nir().io().applyMask(
									processedMasks.fluo().io().resize(f, f).getImage(),
									back).print("FILTERED NIR IMAGE", debug).getImage());
				}
			}
		}
	}
}
