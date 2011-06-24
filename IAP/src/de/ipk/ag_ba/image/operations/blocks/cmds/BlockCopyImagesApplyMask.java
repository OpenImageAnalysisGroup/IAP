package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockCopyImagesApplyMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage visMask = getInput().getMasks().getVis();
		visMask = new ImageOperation(visMask).medianFilter32Bit().border(2).getImage();
		return new ImageOperation(getInput().getImages().getVis()).applyMask_ResizeSourceIfNeeded(visMask, options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluoMask = getInput().getMasks().getFluo();
		return new ImageOperation(getInput().getImages().getFluo()).applyMask_ResizeSourceIfNeeded(fluoMask, options.getBackground()).getImage();
	}
}
