package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * @author klukas
 */
public class BlColorBalancingRoundCamera extends AbstractSnapshotAnalysisBlockFIS {
	
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
		
		return input.io().flipVert().rmCircleShadeFixedGray(180d, steps).flipVert().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() == null || options.getCameraPosition() == CameraPosition.TOP)
			return input().masks().nir();
		
		FlexibleImage input = input().masks().nir();
		int steps = getInt("Shade-Steps-NIR", 180);
		
		return input.io().flipVert().rmCircleShadeFixedGray(180d, steps).flipVert().getImage();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
