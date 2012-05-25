package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * 
 * @author klukas
 */
public class BlockCropMasks extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input() != null && input().masks() != null && input().masks().vis() != null)
			return input().masks().vis().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input() != null && input().masks() != null && input().masks().fluo() != null)
			return input().masks().fluo().crop();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input() != null && input().masks() != null && input().masks().nir() != null)
			return input().masks().nir().crop();
		else
			return null;
	}
	
}
