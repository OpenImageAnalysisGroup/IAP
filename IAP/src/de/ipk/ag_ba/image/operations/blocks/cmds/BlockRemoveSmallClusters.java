package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallClusters extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters().getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return new ImageOperation(getInput().getMasks().getFluo()).removeSmallClusters().getImage();
	}
	
}
