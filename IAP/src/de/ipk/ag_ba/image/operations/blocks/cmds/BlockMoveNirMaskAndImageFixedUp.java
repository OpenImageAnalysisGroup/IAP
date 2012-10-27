package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operation.ImageOperation;
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
		if (input() != null && input().images() != null && input().images().nir() != null)
			return new ImageOperation(input().images().nir()).translate(0, -input().images().nir().getHeight() * 0.01d).getImage();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input() != null && input().masks() != null && input().masks().nir() != null)
			return new ImageOperation(input().masks().nir()).translate(0, -input().masks().nir().getHeight() * 0.01d).getImage();
		else
			return null;
	}
}
