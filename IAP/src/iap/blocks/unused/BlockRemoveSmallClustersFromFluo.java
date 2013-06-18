package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockRemoveSmallClustersFromFluo extends AbstractSnapshotAnalysisBlock {
	
	public static boolean ngUse = true;
	
	@Override
	protected Image processFLUOmask() {
		return new ImageOperation(input().masks().fluo()).removeSmallClusters(ngUse,
				getDouble("Noise-Size-Fluo-Area", (0.001d) / 20),
				getDouble("Noise-Size-Fluo-Dimension", (input().masks().fluo().getWidth() / 100) * 2),
				options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
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
}
