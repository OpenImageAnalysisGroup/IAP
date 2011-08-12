package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallClustersVis extends BlockRemoveSmallClustersVisFluo {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return getInput().getMasks().getFluo();
	}
}
