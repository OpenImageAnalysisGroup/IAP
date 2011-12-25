package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Crops available images (based on rectangular crop area, using comparison to background image).
 * Does process the images, NOT the masks.
 * 
 * @author klukas
 */
public class BlCrop_images_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getVis() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return getInput().getImages().getVis().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS).getValue();
				// }
				
				if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS) != null)
					potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS).getValue();
				
				return getInput().getImages().getVis().cropAbs(-1, -1, -1, potCut);
			}
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getFluo() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return getInput().getImages().getFluo().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO).getValue();
				// }
				
				if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO) != null)
					potCut = (int) getProperties().getNumericProperty(0, 1,
							PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO).getValue();
				return getInput().getImages().getFluo().cropAbs(-1, -1, -1, -1);// potCut);
			}
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput() != null && getInput().getImages() != null && getInput().getImages().getNir() != null) {
			if (options.getCameraPosition() == CameraPosition.TOP) {
				return getInput().getImages().getNir().crop();
			} else {
				int potCut = -1;
				// if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR) == null) {
				// potCut = -1;
				// } else {
				// potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR).getValue();
				// }
				
				if (getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR) != null)
					potCut = (int) getProperties().getNumericProperty(0, 1, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR).getValue();
				
				return getInput().getImages().getNir().cropAbs(-1, -1, -1, potCut);
			}
		} else
			return null;
	}
	
}
