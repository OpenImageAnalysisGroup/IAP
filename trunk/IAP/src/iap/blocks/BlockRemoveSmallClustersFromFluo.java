package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockRemoveSmallClustersFromFluo extends AbstractSnapshotAnalysisBlockFIS {
	
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
	
}
