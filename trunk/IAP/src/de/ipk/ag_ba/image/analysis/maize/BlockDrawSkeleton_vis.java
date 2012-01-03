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
		FlexibleImage skel = getProperties().getImage("skeleton");
		if (skel != null && plantImg != null)
			return plantImg.getIO().drawSkeleton(skel, drawSkeleton).getImage();
		else
			return plantImg;
	}
}
