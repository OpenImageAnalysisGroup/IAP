package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveLevitatingObjectsVis extends BlockRemoveLevitatingObjectsVisFluo {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return getInput().getMasks().getFluo();
	}
	
}
