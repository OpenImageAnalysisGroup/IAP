package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractImageAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class BlMoveImagesToMasks_vis_fluo_nir extends AbstractImageAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		return new FlexibleMaskAndImageSet(input().images(), input().images());
	}
}
