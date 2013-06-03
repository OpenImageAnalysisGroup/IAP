package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.CameraType;

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
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
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
									processedMasks.fluo().io().copy()
											.blur(getDouble("blur fluo mask", 2d))
											.getImage(),
									back).show("FILTERED NIR IMAGE", debug).getImage());
				}
				if (options.getCameraPosition() == CameraPosition.TOP) {
					processedMasks.setNir(
							processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(processedMasks.fluo().io().copy()
									.blur(getDouble("blur fluo mask", 0d)).getImage(),
									back).show("FILTERED NIR IMAGE", debug).getImage());
				}
			}
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
}
