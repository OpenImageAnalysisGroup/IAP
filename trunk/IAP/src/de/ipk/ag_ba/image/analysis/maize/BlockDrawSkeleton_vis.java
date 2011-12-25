package de.ipk.ag_ba.image.analysis.maize;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockDrawSkeleton_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput() == null || getInput().getMasks() == null)
			return null;
		FlexibleImage plantImg = getInput().getMasks().getVis();
		boolean drawSkeleton = options.getBooleanSetting(Setting.DRAW_SKELETON);
		if (plantImg != null && getProperties().getImage("skeleton") != null)
			return plantImg.getIO().drawSkeleton(getProperties().getImage("skeleton"), drawSkeleton).getImage();
		else
			return plantImg;
	}
}
