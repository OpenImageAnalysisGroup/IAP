package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * Apply the FLUO mask to other camera types, and the VIS mask back to the FLUO mask.
 * 
 * @author klukas
 */
public class BlUseFluoMaskToClearOther extends AbstractSnapshotAnalysisBlock {
	
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
				double fW = (double) processedMasks.vis().getWidth() / (double) processedMasks.fluo().getWidth();
				double fH = (double) processedMasks.vis().getHeight() / (double) processedMasks.fluo().getHeight();
				processedMasks.setVis(
						processedMasks.vis().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blur(getDouble("blur fluo mask on vis", 10)).getImage(),
								back).getImage());
				processedMasks.setFluo(
						processedMasks.fluo().io().copy().applyMask(
								processedMasks.vis().io().copy().resize(1d / fW, 1d / fH).blur(getDouble("blur vis mask on fluo", 40d)).getImage(),
								back).getImage());
			}
		}
		if (getBoolean("process NIR", true) && processedMasks.fluo() != null) {
			// apply enlarged FLUO mask to VIS
			if (processedMasks.nir() != null) {
				processedMasks.setNir(
						processedMasks.nir().io().applyMask_ResizeMaskIfNeeded(
								processedMasks.fluo().io().copy().blur(getDouble("blur fluo mask on nir", 10)).getImage(),
								back).getImage());
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
}
