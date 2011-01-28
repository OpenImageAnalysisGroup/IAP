package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @return Modified images according to the given masks.
 */
public class BlockApplyMaskOnFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return new ImageOperation(getInput().getImages().getFluo()).applyMask2(getInput().getMasks().getFluo(), options.getBackground()).getImage();
	}
	
}
