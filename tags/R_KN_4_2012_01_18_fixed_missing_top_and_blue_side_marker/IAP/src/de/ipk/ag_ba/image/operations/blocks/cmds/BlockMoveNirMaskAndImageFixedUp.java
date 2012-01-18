package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Moves the NIR image up a few pixels.
 * 
 * @author klukas
 */
public class BlockMoveNirMaskAndImageFixedUp extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getNir() != null)
			return new ImageOperation(getInput().getImages().getNir()).translate(0, -getInput().getImages().getNir().getHeight() * 0.01d).getImage();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput() != null && getInput().getMasks() != null && getInput().getMasks().getNir() != null)
			return new ImageOperation(getInput().getMasks().getNir()).translate(0, -getInput().getMasks().getNir().getHeight() * 0.01d).getImage();
		else
			return null;
	}
}
