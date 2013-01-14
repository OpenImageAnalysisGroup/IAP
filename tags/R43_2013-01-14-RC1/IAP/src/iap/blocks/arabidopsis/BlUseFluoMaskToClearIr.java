package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClearIr extends AbstractSnapshotAnalysisBlockFIS {
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
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.IR);
		return res;
	}
}
