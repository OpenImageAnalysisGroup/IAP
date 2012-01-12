package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas
 */
public class BlColorBalancingRoundCamera_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput().getImages().getNir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return getInput().getImages().getNir();
		
		FlexibleImage input = getInput().getImages().getNir();
		
		return input.getIO().rmCircleShadeFixed().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return getInput().getMasks().getNir();
		
		FlexibleImage input = getInput().getMasks().getNir();
		
		return input.getIO().rmCircleShadeFixed().getImage();
	}
	
}
