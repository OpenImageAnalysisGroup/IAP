package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClear_Arabidopsis_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		if (processedMasks.fluo() == null) {
			return;
		}
		int back = options.getBackground();
		if (processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.vis() != null) {
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					processedMasks.setVis(
							processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io().addBorder(0, 50, 0, 30, options.getBackground()).blur(2).getImage(),
									back).print("FILTERED VIS IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					double f = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
					processedMasks.setVis(
							processedMasks.vis().io().applyMask(
									processedMasks.fluo().io().resize(f, f)
											.blur(getDouble("blur fluo mask on vis", 1.5d)).getImage(),
									back).print("FILTERED VIS IMAGE", debug).getImage());
					processedMasks.setFluo(
							processedMasks.fluo().io().applyMask(
									processedMasks.vis().io().resize(1d / f, 1d / f)
											.blur(getDouble("blur vis mask on fluo", 1.5d)).getImage(),
									back).print("FILTERED FLUO IMAGE", debug).getImage());
				}
			}
		}
	}
}
