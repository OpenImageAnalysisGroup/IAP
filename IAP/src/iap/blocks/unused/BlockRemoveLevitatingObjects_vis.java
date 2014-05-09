package iap.blocks.unused;

import de.ipk.ag_ba.image.structures.Image;

public class BlockRemoveLevitatingObjects_vis extends BlockRemoveLevitatingObjectsFromVisFluo {
	
	@Override
	protected Image processFLUOmask() {
		return input().masks().fluo();
	}
	
}
