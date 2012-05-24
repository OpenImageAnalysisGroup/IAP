/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas
 */
public class BlockClearSides extends AbstractBlock {
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask != null)
			return mask.io().border((int) (0.01d * mask.getWidth())).getImage();
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processImage(FlexibleImage image) {
		if (image != null)
			return image.io().border((int) (0.01d * image.getWidth())).getImage();
		else
			return null;
	}
	
}
