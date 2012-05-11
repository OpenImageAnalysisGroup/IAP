package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Copies the Vis mask image onto the Nir mask image.
 */
public class BlockCopyVisMaskToNirMask extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processNIRmask() {
		return getInput().getMasks().getVis();
	}
}
