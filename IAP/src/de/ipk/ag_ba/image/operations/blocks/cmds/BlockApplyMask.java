package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @return Modified images according to the given masks.
 */
public class BlockApplyMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return new ImageOperation(getInput().getImages().getVis()).applyMask(getInput().getMasks().getVis(), options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return new ImageOperation(getInput().getImages().getFluo()).applyMask(getInput().getMasks().getFluo(), options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return new ImageOperation(getInput().getImages().getNir()).applyMask(getInput().getMasks().getNir(), options.getBackground()).getImage();
	}
	
}
