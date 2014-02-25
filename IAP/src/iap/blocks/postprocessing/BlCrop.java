package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * Does process the images, the mask only if the corresponding setting is enabled.
 * 
 * @author klukas
 */
public class BlCrop extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISimage() {
		if (input() != null && input().images() != null && input().images().vis() != null) {
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().images().vis().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().images().vis().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processVISmask() {
		if (input() != null && input().masks() != null && input().masks().vis() != null) {
			if (!getBoolean("Process Masks", false))
				return input().masks().vis();
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().masks().vis().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().masks().vis().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input() != null && input().masks() != null && input().masks().fluo() != null) {
			if (!getBoolean("Process Masks", false))
				return input().masks().fluo();
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().masks().fluo().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().masks().fluo().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processNIRmask() {
		if (input() != null && input().masks() != null && input().masks().nir() != null) {
			if (!getBoolean("Process Masks", false))
				return input().masks().nir();
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().masks().nir().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().masks().nir().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processIRmask() {
		if (input() != null && input().masks() != null && input().masks().ir() != null) {
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				if (!getBoolean("Process Masks", false))
					return input().masks().ir();
				return input().masks().ir().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().masks().ir().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOimage() {
		if (input() != null && input().images() != null && input().images().fluo() != null) {
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().images().fluo().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().images().fluo().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processNIRimage() {
		if (input() != null && input().images() != null && input().images().nir() != null) {
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().images().nir().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().images().nir().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	protected Image processIRimage() {
		if (input() != null && input().images() != null && input().images().ir() != null) {
			if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
				return input().images().ir().io().crop().getImage();
			} else {
				int potCut = -1;
				return input().images().ir().io().cropAbs(-1, -1, -1, potCut).getImage();
			}
		} else
			return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.POSTPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Crop Result Images";
	}
	
	@Override
	public String getDescription() {
		return "Crops images. Does process the images, the masks are cropped, if the corresponding setting is enabled.";
	}
}
