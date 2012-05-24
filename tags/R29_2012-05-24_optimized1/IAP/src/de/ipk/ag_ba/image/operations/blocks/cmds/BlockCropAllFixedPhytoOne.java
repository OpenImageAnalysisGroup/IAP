package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * 
 * @author klukas
 */
public class BlockCropAllFixedPhytoOne extends AbstractSnapshotAnalysisBlockFIS {
	
	private final double a = 2.5d / 13d;
	private final double b = 2.5d / 13d;
	private final double c = 0.5 / 11d;
	private final double d = 3d / 11d;
	
	@Override
	protected FlexibleImage processVISimage() {
		if (input() != null && input().images() != null && input().images().vis() != null)
			return input().images().vis().crop(a, b, c, d);
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		if (input() != null && input().images() != null && input().images().fluo() != null)
			return input().images().fluo().crop(a, b, c, d);
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input() != null && input().images() != null && input().images().nir() != null)
			return input().images().nir().crop(a, b, c, d);
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input() != null && input().masks() != null && input().masks().vis() != null)
			return input().masks().vis().crop(a, b, c, d);
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input() != null && input().masks() != null && input().masks().fluo() != null)
			return input().masks().fluo().crop(a, b, c, d);
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input() != null && input().masks() != null && input().masks().nir() != null)
			return input().masks().nir().crop(a, b, c, d);
		else
			return null;
	}
	
}
