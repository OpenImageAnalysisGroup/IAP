package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockRemoveSmallClusters extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(0.002d).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return new ImageOperation(getInput().getMasks().getFluo()).removeSmallClusters(0.002d).getImage();
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// return new ImageOperation(getInput().getMasks().getNir()).removeSmallClusters(0.002d).getImage();
	// }
	
}
