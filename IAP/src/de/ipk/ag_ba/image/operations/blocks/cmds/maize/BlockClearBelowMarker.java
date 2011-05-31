package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
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
			
			BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			if (markerPosLeft != null) {
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) markerPosLeft.getValue(), options.getBackground()).getImage();
				return result;
			}
			if (markerPosLeft == null && markerPosRight != null) {
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) markerPosRight.getValue(), options.getBackground()).getImage();
				
				return result;
			}
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getFluo();
			
			BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			int heightVisImage = (int) getProperties().getNumericProperty(0, 1, PropertyNames.HEIGHT_VIS_IMAGE).getValue();
			int heightFluoImage = (int) getProperties().getNumericProperty(0, 1, PropertyNames.HEIGHT_FLUO_IMAGE).getValue();
			
			if (markerPosLeft != null) {
				double temp = (markerPosLeft.getValue() / heightVisImage) * heightFluoImage;
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) temp, options.getBackground()).getImage();
				
				return result;
			}
			if (markerPosLeft == null && markerPosRight != null) {
				double temp = (markerPosRight.getValue() / heightVisImage) * heightFluoImage;
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) temp, options.getBackground()).getImage();
				
				return result;
			}
		}
		return getInput().getMasks().getFluo();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getNir();
			
			BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			int heightVisImage = getInput().getMasks().getVis().getHeight();
			int heightNirImage = getInput().getMasks().getNir().getHeight();
			
			if (markerPosLeft != null) {
				double temp = (markerPosLeft.getValue() / heightVisImage) * heightNirImage + 8;
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) temp, options.getBackground()).getImage();
				return result;
			}
			if (markerPosLeft == null && markerPosRight != null) {
				double temp = (markerPosRight.getValue() / heightVisImage) * heightNirImage + 8;
				FlexibleImage result = new ImageOperation(input).clearImageBottom(
						(int) temp, options.getBackground()).getImage();
				return result;
			}
		}
		return getInput().getMasks().getNir();
	}
}
