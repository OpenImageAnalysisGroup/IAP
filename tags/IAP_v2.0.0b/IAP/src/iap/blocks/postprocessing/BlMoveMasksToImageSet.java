package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractImageAnalysisBlockFIS;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

/**
 * Replaces the main image set with the mask images.
 * 
 * @author klukas
 */
public class BlMoveMasksToImageSet extends AbstractImageAnalysisBlockFIS {
	
	@Override
	protected MaskAndImageSet run() {
		if (getBoolean("enabled", true))
			return new MaskAndImageSet(input().masks(), new ImageSet());
		else
			return input();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
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
	
	@Override
	public String getName() {
		return "Move Mask Set to Image Set";
	}
	
	@Override
	public String getDescription() {
		return "Replaces the main image set with the mask images.";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return null;
	}
}
