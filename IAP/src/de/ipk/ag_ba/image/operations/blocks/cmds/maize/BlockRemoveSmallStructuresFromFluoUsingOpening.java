package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallStructuresFromFluoUsingOpening extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage mask = new ImageOperation(getInput().getMasks().getVis()).opening(2).getImage();
		return new ImageOperation(getInput().getMasks().getVis()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage mask = new ImageOperation(getInput().getMasks().getFluo()).opening(2).getImage();
		return new ImageOperation(getInput().getMasks().getFluo()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
	}
	
}
