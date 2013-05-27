package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * @author Christian Klukas
 */
public class BlUseFluoMaskToClearOther extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		int back = options.getBackground();
		if (getBoolean("process VIS and FLUO", true) && processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.vis() != null) {
				double f = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
				processedMasks.setVis(
						processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blur(getDouble("blur fluo mask on vis", 40)).getImage(),
								back).show("FILTERED VIS IMAGE", debug).getImage());
				processedMasks.setFluo(
						processedMasks.fluo().io().copy().applyMask(
								processedMasks.vis().io().resize(1d / f, 1d / f).blur(getDouble("blur vis mask on fluo", 40d)).getImage(),
								back).show("FILTERED FLUO IMAGE", debug).getImage());
			}
		}
		if (getBoolean("process NIR", true) && processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.nir() != null) {
				processedMasks.setNir(
						processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blur(getDouble("blur fluo mask on nir", 10)).getImage(),
								back).show("FILTERED NIR IMAGE", debug).getImage());
			}
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
}
