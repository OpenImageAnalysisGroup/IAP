package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClearBelowMarker extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR);
			
			BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			if (markerPosLeft != null) {
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) (markerPosLeft.getValue() * getInput().getImages().getVis().getHeight() * scaleFactor), options.getBackground()).getImage();
				
				return result;
			}
			if (markerPosLeft == null && markerPosRight != null) {
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) (markerPosRight.getValue() * getInput().getImages().getVis().getHeight() * scaleFactor), options.getBackground()).getImage();
				
				return result;
			}
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getFluo();
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR);
			
			BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			if (markerPosLeft != null) {
				double temp = markerPosLeft.getValue();
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor), options.getBackground()).getImage();
				
				return result;
			}
			if (markerPosLeft == null && markerPosRight != null) {
				double temp = markerPosRight.getValue();
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor), options.getBackground()).getImage();
				
				return result;
			}
		}
		return getInput().getMasks().getFluo();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			if (getInput().getMasks().getNir() != null) {
				FlexibleImage input = getInput().getMasks().getNir();
				
				BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
				BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
				
				if (markerPosLeft != null) {
					double temp = markerPosLeft.getValue();
					FlexibleImage result = new ImageOperation(input).clearImageBottom(
							(int) (temp * getInput().getImages().getNir().getHeight()), options.getBackground()).getImage();
					return result;
				}
				if (markerPosLeft == null && markerPosRight != null) {
					double temp = markerPosRight.getValue();
					FlexibleImage result = new ImageOperation(input).clearImageBottom(
							(int) (temp * getInput().getImages().getNir().getHeight()), options.getBackground()).getImage();
					return result;
				}
			}
			return getInput().getMasks().getNir();
		} else
			return null;
	}
}
