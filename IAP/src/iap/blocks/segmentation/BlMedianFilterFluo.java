/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Remove "peper and salt" noise from Fluo mask.
 * 
 * @author pape, klukas
 */
public class BlMedianFilterFluo extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null || mask.getCameraType() != CameraType.FLUO)
			return mask;
		
		Image medianMask = new ImageOperation(mask)
				.medianFilter32Bit()
				.border(2)
				.getImage();
		
		return new ImageOperation(mask).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Median Filter (Fluo)";
	}
	
	@Override
	public String getDescription() {
		return "Remove 'peper and salt' noise from fluo mask.";
	}
}
