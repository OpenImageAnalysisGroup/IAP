package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlCopyImagesApplyMask extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		Image visMask = input().masks().vis();
		if (visMask != null) {
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
}
