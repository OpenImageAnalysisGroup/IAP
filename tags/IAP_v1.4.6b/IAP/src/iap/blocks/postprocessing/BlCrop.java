package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * Does process the images, the mask only if the corresponding setting is enabled.
 * 
 * @author klukas
 */
public class BlCrop extends AbstractBlock {
	
	@Override
	protected Image processImage(Image image) {
		return image.io().crop().getImage();
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (!getBoolean("Process Masks", true))
			return mask;
		else
			return mask.io().crop().getImage();
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
		return "Crop Result Images";
	}
	
	@Override
	public String getDescription() {
		return "Crops images. Does process the images, the masks are cropped, if the corresponding setting is enabled.";
	}
}
