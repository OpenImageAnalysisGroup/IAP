/**
 * 
 */
package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Klukas
 */
@Deprecated
public class BlMedianFilterVisDepr extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		Image medianMask = new ImageOperation(input().masks().vis()).medianFilter32Bit()
				.ij().dilate(getInt("dilate-cnt", 4)).io()
				.border(2).getImage();
		
		return new ImageOperation(input().images().vis())
				.applyMask_ResizeSourceIfNeeded(medianMask, optionsAndResults.getBackground()).getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Median Filter (VIS) (depricated)";
	}
	
	@Override
	public String getDescription() {
		return "Remove 'peper and salt' noise from selected mask images.";
	}
}
