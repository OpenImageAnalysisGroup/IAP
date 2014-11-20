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
 * @author Pape, Klukas
 */
public class BlMedianFilter extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null || !getBoolean("Process " + mask.getCameraType(), true))
			return mask;
		
		Image medianMask = new ImageOperation(mask).copy().bm().medianFilter().io().border(2)
				.getImage();
		Image ref = input().images().getImage(mask.getCameraType());
		if (ref == null)
			return null;
		return ref.io().applyMask_ResizeSourceIfNeeded(medianMask, optionsAndResults.getBackground()).getImage();
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
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Median Filter";
	}
	
	@Override
	public String getDescription() {
		return "Remove 'peper and salt' noise from selected mask images.";
	}
	
}
