/**
 * 
 */
package iap.blocks.unused;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class BlockClearSmallBorderAroundImagesAndMasks extends AbstractBlock {
	
	@Override
	public boolean isChangingImages() {
		return true;
	}
	
	@Override
	protected Image processImage(Image image) {
		if (image != null)
			return image.io().border((int) (0.01d * image.getWidth())).getImage();
		else
			return null;
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null)
			return mask.io().border((int) (0.01d * mask.getWidth())).getImage();
		else
			return null;
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
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Add Border Around Image";
	}
	
	@Override
	public String getDescription() {
		return "Add one border with width of one percent of image width around the image.";
	}
}
