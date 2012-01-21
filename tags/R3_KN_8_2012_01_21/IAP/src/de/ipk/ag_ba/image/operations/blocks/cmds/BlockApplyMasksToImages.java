package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @return Modified images according to the given masks.
 * @author klukas
 */
public class BlockApplyMasksToImages extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		return new ImageOperation(getInput().getImages().getVis()).applyMask_ResizeMaskIfNeeded(
				getInput().getMasks().getVis(), options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		return new ImageOperation(getInput().getImages().getFluo()).applyMask_ResizeMaskIfNeeded(
				getInput().getMasks().getFluo(), options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		return new ImageOperation(getInput().getImages().getNir()).applyMask_ResizeMaskIfNeeded(
				getInput().getMasks().getNir(), options.getBackground()).getImage();
	}
	
}
