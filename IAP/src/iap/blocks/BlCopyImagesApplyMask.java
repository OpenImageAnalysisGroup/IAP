package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlCopyImagesApplyMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage visMask = input().masks().vis();
		if (visMask != null) {
			visMask = new ImageOperation(visMask).medianFilter32Bit().border(2).getImage();
			return new ImageOperation(input().images().vis()).applyMask_ResizeSourceIfNeeded(visMask, options.getBackground()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluoMask = input().masks().fluo();
		if (fluoMask != null)
			return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(fluoMask, options.getBackground()).getImage();
		else
			return fluoMask;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
