package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockRemoveSmallClustersOnFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	public static boolean ngUse = true;
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return new ImageOperation(input().masks().fluo()).removeSmallClusters(ngUse,
				getDouble("Noise-Size-Fluo-Area", (0.001d) / 20),
				getDouble("Noise-Size-Fluo-Dimension", (input().masks().fluo().getWidth() / 100) * 2),
				options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
	}
	
}
