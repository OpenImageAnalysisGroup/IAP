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
		if (input().images().vis() == null || options.getCameraPosition() == CameraPosition.TOP)
			return input().images().vis();
		
		FlexibleImage input = input().images().vis();
		
		int steps = getInt("Shade-Steps-Vis", 50);
		
		return input.io().rmCircleShadeFixedRGB(255d, steps).getImage();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null || options.getCameraPosition() == CameraPosition.TOP)
			return input().masks().vis();
		
		FlexibleImage input = input().masks().vis();
		
		int steps = getInt("Shade-Steps-Vis", 50);
		
		return input.io().rmCircleShadeFixedRGB(255d, steps).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return input().images().nir();
		
		FlexibleImage input = input().images().nir();
		
		int steps = getInt("Shade-Steps-NIR", 180);
		
		return input.io().// rmCircleShadeFixedGray(180d).
				flipVert().rmCircleShadeFixedGray(180d, steps).flipVert().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return input().masks().nir();
		
		FlexibleImage input = input().masks().nir();
		
		int steps = getInt("Shade-Steps-NIR", 180);
		
		return input.io().
				flipVert().rmCircleShadeFixedGray(180d, steps).flipVert().
				// rmCircleShadeFixedGray(180d).
				getImage();
	}
	
}
