package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.ImageOperation;
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
		if (input() == null || input().masks() == null || input().masks().fluo() == null)
			return null;
		
		FlexibleImage fluoMask = input().masks().fluo();
		
		Color backgroundFill = ImageOperation.BACKGROUND_COLOR;
		final int iBackgroundFill = backgroundFill.getRGB();
		
		return new ImageOperation(fluoMask).filterByHSV_value(0.3, iBackgroundFill).getImage();
	}
}
