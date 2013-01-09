package iap.blocks.maize;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveLevitatingObjects_vis extends BlockRemoveLevitatingObjectsFromVisFluo {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return input().masks().fluo();
	}
	
}
