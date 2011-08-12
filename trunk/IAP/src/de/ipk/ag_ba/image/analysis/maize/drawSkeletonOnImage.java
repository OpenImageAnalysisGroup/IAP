package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class drawSkeletonOnImage extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() throws InterruptedException {
		FlexibleImage plantImg = getInput().getMasks().getVis();
		return plantImg.getIO().copyOnImage(getProperties().getImage("skeleton")).getImage();
	}
}
