package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlCopyImagesApplyMask_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
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
}
