package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * Does process the images, NOT the masks.
 * 
 * @author klukas
 */
public class BlCrop extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
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
	protected FlexibleImage processFLUOimage() {
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
	protected FlexibleImage processNIRimage() {
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
	protected FlexibleImage processIRimage() {
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
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
