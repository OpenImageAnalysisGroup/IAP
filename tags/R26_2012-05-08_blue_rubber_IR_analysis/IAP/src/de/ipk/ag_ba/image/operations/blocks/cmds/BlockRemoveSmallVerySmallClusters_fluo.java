package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockRemoveSmallVerySmallClusters_fluo extends AbstractBlock {
	public static boolean ngUse = true;
	
	@Override
	protected FlexibleImage processMask(FlexibleImage image) {
		if (image == null)
			return null;
		if (image.getType() == FlexibleImageType.FLUO)
			return new ImageOperation(image).removeSmallClusters(ngUse,
					0.0001 * 0.2, 3,
					options.getNeighbourhood(), options.getCameraPosition(), null, true).getImage();
		else
			return image;
	}
}
