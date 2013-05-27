/**
 * 
 */
package iap.blocks.maize;

import iap.blocks.data_structures.AbstractBlock;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * @author klukas
 */
public class BlockClearSmallBorderAroundImagesAndMasks extends AbstractBlock {
	
	@Override
	protected boolean isChangingImages() {
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
}
