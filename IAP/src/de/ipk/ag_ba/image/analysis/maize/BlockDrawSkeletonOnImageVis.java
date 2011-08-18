package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockDrawSkeletonOnImageVis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage plantImg = getInput().getMasks().getVis();
		if (plantImg != null && getProperties().getImage("skeleton") != null)
			return plantImg.getIO().copyOnImage(getProperties().getImage("skeleton")).getImage();
		else
			return plantImg;
	}
}
