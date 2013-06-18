package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * 
 * @author klukas
 */
public class BlockCropMasks extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		if (input() != null && input().masks() != null && input().masks().vis() != null)
			return input().masks().vis().io().crop().getImage();
		else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input() != null && input().masks() != null && input().masks().fluo() != null)
			return input().masks().fluo().io().crop().getImage();
		else
			return null;
	}
	
	@Override
	protected Image processNIRmask() {
		if (input() != null && input().masks() != null && input().masks().nir() != null)
			return input().masks().nir().io().crop().getImage();
		else
			return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.POSTPROCESSING;
	}
}
