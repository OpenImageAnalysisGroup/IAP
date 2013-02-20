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
	
	private boolean debug = false;
	private double whiteLevel_180d_NIR, whiteLevel_255d_VIS;
	private double s0_NIR;
	private double ss_NIR;
	private double s0_VIS;
	private double ss_VIS;
	
	@Override
	protected void prepare() {
		this.debug = getBoolean("debug", false);
		this.whiteLevel_180d_NIR = getDouble("NIR target brightness", 180d);
		this.s0_NIR = getDouble("NIR calibration rectangle start size", 5d);
		this.ss_NIR = getDouble("NIR calibration rectangle final size plus", 15d);
		
		this.whiteLevel_255d_VIS = getDouble("VIS target brightness", 255d);
		this.s0_VIS = getDouble("VIS calibration rectangle start size", 45d);
		this.ss_VIS = getDouble("VIS calibration rectangle final size plus", 15d);
	}
	
	@Override
	protected FlexibleImage processVISimage() {
		if (input().images().vis() == null || !getBoolean("process VIS image", options.getCameraPosition() == CameraPosition.TOP))
			return input().images().vis();
		
		FlexibleImage input = input().images().vis();
		int steps = getInt("Shade-Steps-Vis", 50);
		
		return input.io().rmCircleShadeFixedRGB(whiteLevel_255d_VIS, steps, debug,
				s0_VIS, ss_VIS).getImage();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null || !getBoolean("process VIS mask", options.getCameraPosition() == CameraPosition.TOP))
			return input().masks().vis();
		
		FlexibleImage input = input().masks().vis();
		int steps = getInt("Shade-Steps-Vis", 50);
		
		return input.io().rmCircleShadeFixedRGB(whiteLevel_255d_VIS, steps, debug,
				s0_VIS, ss_VIS).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() == null || !getBoolean("process NIR image", options.getCameraPosition() == CameraPosition.TOP))
			return input().images().nir();
		
		FlexibleImage input = input().images().nir();
		int steps = getInt("Shade-Steps-NIR", 180);
		
		return input.io().flipVert().rmCircleShadeFixedGray(whiteLevel_180d_NIR, steps, debug,
				s0_NIR, ss_NIR).flipVert().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() == null || !getBoolean("process NIR mask", options.getCameraPosition() == CameraPosition.TOP))
			return input().masks().nir();
		
		FlexibleImage input = input().masks().nir();
		int steps = getInt("Shade-Steps-NIR", 180);
		
		return input.io().flipVert().rmCircleShadeFixedGray(whiteLevel_180d_NIR, steps, debug,
				s0_NIR, ss_NIR).flipVert().getImage();
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
