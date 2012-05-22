package de.ipk.ag_ba.image.operations.blocks.cmds.post_process;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.ImageTyp;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class BlockPostProcessEdgeErodeOnFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	private final boolean enlarge;
	
	/**
	 * Erode/delate/erode several times, depending on image type.
	 */
	public BlockPostProcessEdgeErodeOnFluo(boolean enlarge) {
		this.enlarge = enlarge;
		
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return BlockPostProcessEdgeErode.postProcessResultImage(input().images().fluo(), input().masks().fluo(), options,
				options.getCameraPosition(), ImageTyp.FLUO, enlarge);
	}
	
}
