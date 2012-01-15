package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resets the masks (Vis, Fluo, Nir) to NULL.
 * 
 * @author klukas
 */
public class BlockSetMasksToNull extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return null;
	}
}