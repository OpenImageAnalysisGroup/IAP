package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * Does process the images, NOT the masks.
 * 
 * @author klukas
 */
public class BlCrop extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processVISimage() {
		if (input() != null && input().images() != null && input().images().vis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return input().images().vis().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS).getValue();
				// }
				
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS) != null)
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS).getValue();
				
				return input().images().vis().cropAbs(-1, -1, -1, potCut);
			}
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOimage() {
		if (input() != null && input().images() != null && input().images().fluo() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return input().images().fluo().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO).getValue();
				// }
				
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO) != null)
				// potCut = (int) getProperties().getNumericProperty(0, 1,
				// PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO).getValue();
				return input().images().fluo().cropAbs(-1, -1, -1, -1);// potCut);
			}
		} else
			return null;
	}
	
	@Override
	protected Image processNIRimage() {
		if (input() != null && input().images() != null && input().images().nir() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return input().images().nir().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR).getValue();
				// }
				
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR) != null)
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR).getValue();
				//
				return input().images().nir().cropAbs(-1, -1, -1, potCut);
			}
		} else
			return null;
	}
	
	@Override
	protected Image processIRimage() {
		if (input() != null && input().images() != null && input().images().ir() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return input().images().ir().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR).getValue();
				// }
				
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR) != null)
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR).getValue();
				//
				return input().images().ir().cropAbs(-1, -1, -1, potCut);
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
}
