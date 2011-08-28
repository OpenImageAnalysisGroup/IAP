package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallClusters_vis extends BlockRemoveSmallClusters_vis_fluo {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return getInput().getMasks().getFluo();
	}
}
