package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Filters the Fluo Mask by removing all pixels below a threshold in the HSV - VALUE.
 * 
 * @author klukas
 */
public class BlockFilterFluoMaskByValue30 extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput() == null || getInput().getMasks() == null || getInput().getMasks().getFluo() == null)
			return null;
		
		FlexibleImage fluoMask = getInput().getMasks().getFluo();
		
		Color backgroundFill = ImageOperation.BACKGROUND_COLOR;
		final int iBackgroundFill = backgroundFill.getRGB();
		
		return new ImageOperation(fluoMask).filterByHSV_value(0.3, iBackgroundFill).getImage();
	}
}
