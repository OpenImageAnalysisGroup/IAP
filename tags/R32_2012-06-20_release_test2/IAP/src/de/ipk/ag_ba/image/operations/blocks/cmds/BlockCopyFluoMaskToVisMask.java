package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @return Modified images according to the given masks.
 */
public class BlockCopyFluoMaskToVisMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(input().masks().vis(), options.getBackground()).getImage();
	}
	
}
