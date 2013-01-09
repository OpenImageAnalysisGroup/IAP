package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

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
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
