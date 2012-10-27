package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @return Modified images according to the given masks.
 * @author klukas
 */
public class BlockApplyMasksToImages extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		return new ImageOperation(input().images().vis()).applyMask_ResizeMaskIfNeeded(
				input().masks().vis(), options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		return new ImageOperation(input().images().fluo()).applyMask_ResizeMaskIfNeeded(
				input().masks().fluo(), options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		return new ImageOperation(input().images().nir()).applyMask_ResizeMaskIfNeeded(
				input().masks().nir(), options.getBackground()).getImage();
	}
	
}
