/**
 * 
 */
package iap.blocks.maize;

import iap.blocks.data_structures.AbstractBlock;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author klukas
 */
public class BlockClearSmallBorderAroundImagesAndMasks extends AbstractBlock {
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		if (image != null)
			return image.io().border((int) (0.01d * image.getWidth())).getImage();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask != null)
			return mask.io().border((int) (0.01d * mask.getWidth())).getImage();
		else
			return null;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
}
