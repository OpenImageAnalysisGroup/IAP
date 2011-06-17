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
		return new ImageOperation(getInput().getImages().getVis()).applyMask_ResizeSourceIfNeeded(visMask, options.getBackground()).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluoMask = getInput().getMasks().getFluo();
		return new ImageOperation(getInput().getImages().getFluo()).applyMask_ResizeSourceIfNeeded(fluoMask, options.getBackground()).getImage();
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// if (getInput().getMasks().getNir() != null) {
	// FlexibleImage nirMask = getInput().getMasks().getNir();
	// return new ImageOperation(getInput().getImages().getNir()).applyMask_ResizeSourceIfNeeded(nirMask, options.getBackground()).getImage();
	// } else
	// return null;
	// }
	
}
