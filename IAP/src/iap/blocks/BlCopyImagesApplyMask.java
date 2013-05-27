package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlCopyImagesApplyMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processVISmask() {
		Image visMask = input().masks().vis();
		if (visMask != null) {
			visMask = new ImageOperation(visMask).medianFilter32Bit().border(2).getImage();
			return new ImageOperation(input().images().vis()).applyMask_ResizeSourceIfNeeded(visMask, options.getBackground()).getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fluoMask = input().masks().fluo();
		if (fluoMask != null)
			return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(fluoMask, options.getBackground()).getImage();
		else
			return fluoMask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
}
