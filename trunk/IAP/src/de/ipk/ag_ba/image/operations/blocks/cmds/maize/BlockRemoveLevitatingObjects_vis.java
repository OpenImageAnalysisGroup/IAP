package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveLevitatingObjects_vis extends BlockRemoveLevitatingObjects_vis_fluo {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return input().masks().fluo();
	}
	
}
