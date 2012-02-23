package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author klukas
 */
public class BlColorBalancingRoundCamera_vis_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		if (getInput().getImages().getVis() == null || options.getCameraPosition() == CameraPosition.TOP)
			return getInput().getImages().getVis();
		
		FlexibleImage input = getInput().getImages().getVis();
		
		return input.getIO().rmCircleShadeFixedRGB(255d).getImage();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null || options.getCameraPosition() == CameraPosition.TOP)
			return getInput().getMasks().getVis();
		
		FlexibleImage input = getInput().getMasks().getVis();
		
		return input.getIO().rmCircleShadeFixedRGB(255d).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput().getImages().getNir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return getInput().getImages().getNir();
		
		FlexibleImage input = getInput().getImages().getNir();
		
		return input.getIO().// rmCircleShadeFixedGray(180d).
				flipVert().rmCircleShadeFixedGray(180d).flipVert().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return getInput().getMasks().getNir();
		
		FlexibleImage input = getInput().getMasks().getNir();
		
		return input.getIO().
				flipVert().rmCircleShadeFixedGray(180d).flipVert().
				// rmCircleShadeFixedGray(180d).
				getImage();
	}
	
}
