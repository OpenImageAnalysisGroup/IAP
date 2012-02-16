package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlSmoothShape_vis extends AbstractBlock {
	
	private final boolean debug = false;
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask.getType() == FlexibleImageType.VIS) {
			return mask.getIO().applyMask_ResizeMaskIfNeeded(
					mask.copy().getIO().dilate(2).erode(7).blur(2).erode().print("blurred mask", debug).getImage(),
					options.getBackground()).getImage();
		} else
			return mask;
	}
}
