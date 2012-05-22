package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractBlock;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlSmoothShape_vis extends AbstractBlock {
	
	private final boolean debug = false;
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask!=null && mask.getType() == FlexibleImageType.VIS) {
			return mask.io().applyMask_ResizeMaskIfNeeded(
					mask.copy().io().erode(3).blur(2).erode().erode().print("blurred mask", debug).getImage(),
					options.getBackground()).getImage();
		} else
			return mask;
	}
}
