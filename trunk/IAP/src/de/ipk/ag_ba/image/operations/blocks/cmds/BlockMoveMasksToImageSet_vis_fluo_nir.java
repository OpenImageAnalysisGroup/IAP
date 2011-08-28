package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class BlockMoveMasksToImageSet_vis_fluo_nir extends AbstractImageAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		return new FlexibleMaskAndImageSet(getInput().getMasks(), new FlexibleImageSet());
	}
}
