/**
 * 
 */
package iap.blocks.segmentation;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

/**
 * Median Filter, removes "salt and pepper" noise from mask.
 * 
 * @author Pape, Klukas
 */
public class BlMedianFilterFixedSize extends AbstractBlock {
	
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
		return BlockType.DEPRECATED;
	}
	
	@Override
	public String getName() {
		return "Median Filter";
	}
	
	@Override
	public String getDescription() {
		return "Remove 'pepper and salt' noise from selected mask images.";
	}
	
}
