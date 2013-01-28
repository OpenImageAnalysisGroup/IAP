package iap.blocks.arabidopsis;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClearNir_Arabidopsis extends AbstractSnapshotAnalysisBlockFIS {
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
			// apply enlarged FLUO mask to NIR
			if (processedMasks.nir() != null) {
				if (options.getCameraPosition() == CameraPosition.SIDE) {
					processedMasks.setNir(
							processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(
									processedMasks.fluo().io()
											.blur(getDouble("blur fluo mask", 2d))
											.getImage(),
									back).show("FILTERED NIR IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					processedMasks.setNir(
							processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(processedMasks.fluo().io()
									.blur(getDouble("blur fluo mask", 0d)).getImage(),
									back).show("FILTERED NIR IMAGE", debug).getImage());
				}
			}
		}
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
}
