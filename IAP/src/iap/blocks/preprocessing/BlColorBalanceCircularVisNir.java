package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class BlColorBalanceCircularVisNir extends AbstractSnapshotAnalysisBlock {
	
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
		this.s0_VIS = getDouble("VIS calibration rectangle start size", 75d);
		this.ss_VIS = getDouble("VIS calibration rectangle final size plus", 45d);
	}
	
	@Override
	protected Image processVISimage() {
		int steps = getInt("Shade-Steps-Vis", 50);
		if (input().images().vis() == null || !getBoolean("process VIS image", options.getCameraPosition() != CameraPosition.TOP))
			return input().images().vis();
		
		Image input = input().images().vis();
		
		return input.io().rmCircleShadeFixedRGB(whiteLevel_255d_VIS, steps, debug,
				s0_VIS, ss_VIS).getImage();
	}
	
	@Override
	protected Image processVISmask() {
		int steps = getInt("Shade-Steps-Vis", 50);
		if (input().masks().vis() == null || !getBoolean("process VIS mask", options.getCameraPosition() != CameraPosition.TOP))
			return input().masks().vis();
		
		Image input = input().masks().vis();
		
		return input.io().rmCircleShadeFixedRGB(whiteLevel_255d_VIS, steps, debug,
				s0_VIS, ss_VIS).getImage();
	}
	
	@Override
	protected Image processNIRimage() {
		int steps = getInt("Shade-Steps-NIR", 180);
		if (input().images().nir() == null || !getBoolean("process NIR image", options.getCameraPosition() != CameraPosition.TOP))
			return input().images().nir();
		
		Image input = input().images().nir();
		
		return input.io().flipVert().rmCircleShadeFixedGray(whiteLevel_180d_NIR, steps, debug,
				s0_NIR, ss_NIR).flipVert().getImage();
	}
	
	@Override
	protected Image processNIRmask() {
		int steps = getInt("Shade-Steps-NIR", 180);
		if (input().masks().nir() == null || !getBoolean("process NIR mask", options.getCameraPosition() != CameraPosition.TOP))
			return input().masks().nir();
		
		Image input = input().masks().nir();
		
		return input.io().flipVert().rmCircleShadeFixedGray(whiteLevel_180d_NIR, steps, debug,
				s0_NIR, ss_NIR).flipVert().getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
}
