package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClearMasksBasedOnMarkers extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
			BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
			
			FlexibleImage result = input;
			
			if (markerPosLeftY != null) {
				// System.out.println("mark: " + markerPosLeftY.getValue());
				if (markerPosLeftY.getValue() > 0.5)
					result = new ImageOperation(result).clearImageBottom(
							(int) (markerPosLeftY.getValue() * getInput().getMasks().getVis().getHeight()) - 40, options.getBackground()).getImage();
			} else
				if (markerPosLeftY == null && markerPosRightY != null) {
					// System.out.println("mark: " + markerPosRightY.getValue());
					if (markerPosRightY.getValue() > 0.5)
						result = new ImageOperation(result).clearImageBottom(
								(int) (markerPosRightY.getValue() * getInput().getMasks().getVis().getHeight()) - 40, options.getBackground()).getImage();
				}
			
			boolean clearSides = true;
			if (clearSides)
				if (markerPosLeftX != null) {
					result = new ImageOperation(result).clearImageLeft(
							(int) (0.95 * markerPosLeftX.getValue() * getInput().getMasks().getVis().getWidth()), options.getBackground()).getImage();
				}
			if (clearSides)
				if (markerPosRightX != null) {
					result = new ImageOperation(result).clearImageRight(
							(int) (1.05 * markerPosRightX.getValue() * getInput().getMasks().getVis().getWidth()), options.getBackground()).getImage();
				}
			return result;
		}
		return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getImages().getFluo() == null)
			return null;
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			FlexibleImage input = getInput().getMasks().getFluo();
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			
			BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
			BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
			
			if (markerPosLeft != null) {
				double temp = markerPosLeft.getValue();
				if (temp > 0.5) {
					FlexibleImage result = new ImageOperation(input).clearImageBottom(
							(int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor) - 16, options.getBackground()).getImage();
					return result;
				}
				return input;
			}
			if (markerPosLeft == null && markerPosRight != null) {
				double temp = markerPosRight.getValue();
				if (temp > 0.5) {
					FlexibleImage result = new ImageOperation(input).clearImageBottom(
							(int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor) - 16, options.getBackground()).getImage();
					return result;
				}
				return input;
			}
		}
		return getInput().getMasks().getFluo();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			if (getInput().getImages().getNir() != null) {
				FlexibleImage input = getInput().getImages().getNir();
				
				BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
				BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
				
				BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
				BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
				
				if (markerPosLeft != null) {
					double temp = markerPosLeft.getValue();
					if (temp > 0.5) {
						double pos = temp * getInput().getImages().getNir().getHeight();
						if (pos > 10)
							pos -= 10;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
								(int) (pos), options.getBackground()).getImage();
						boolean clearSides = true;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (0.99 * markerPosLeftX.getValue() * getInput().getImages().getNir().getWidth()), options.getBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (1.01 * markerPosRightX.getValue() * getInput().getImages().getNir().getWidth()), options.getBackground()).getImage();
							}
						return result;
					}
					return input;
				}
				if (markerPosLeft == null && markerPosRight != null) {
					double temp = markerPosRight.getValue();
					if (temp > 0.5) {
						double pos = (temp * getInput().getImages().getNir().getHeight());
						if (pos > 10)
							pos -= 14;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
								(int) pos, options.getBackground()).getImage();
						boolean clearSides = true;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (0.95 * markerPosLeftX.getValue() * getInput().getImages().getNir().getWidth()), options.getBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (1.05 * markerPosRightX.getValue() * getInput().getImages().getNir().getWidth()), options.getBackground()).getImage();
							}
						return result;
					}
					return input;
				}
				
			}
			return getInput().getMasks().getNir();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		
		if (options.getCameraTyp() == CameraTyp.SIDE) {
			if (getInput().getMasks().getNir() != null) {
				FlexibleImage input = getInput().getMasks().getNir();
				
				BlockProperty markerPosLeft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
				BlockProperty markerPosRight = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
				
				BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
				BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
				
				if (markerPosLeft != null) {
					double temp = markerPosLeft.getValue();
					if (temp > 0.5) {
						double pos = temp * getInput().getImages().getNir().getHeight();
						if (pos > 10)
							pos -= 10;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
								(int) (pos), options.getBackground()).getImage();
						boolean clearSides = true;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (0.98 * markerPosLeftX.getValue() * getInput().getMasks().getNir().getWidth()), options.getBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (1.02 * markerPosRightX.getValue() * getInput().getMasks().getNir().getWidth()), options.getBackground()).getImage();
							}
						return result;
					}
					return input;
				}
				if (markerPosLeft == null && markerPosRight != null) {
					double temp = markerPosRight.getValue();
					if (temp > 0.5) {
						double pos = (temp * getInput().getImages().getNir().getHeight());
						if (pos > 10)
							pos -= 14;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
								(int) pos, options.getBackground()).getImage();
						boolean clearSides = true;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (0.95 * markerPosLeftX.getValue() * getInput().getMasks().getNir().getWidth()), options.getBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (1.05 * markerPosRightX.getValue() * getInput().getMasks().getNir().getWidth()), options.getBackground()).getImage();
							}
						return result;
					}
					return input;
				}
				
			}
			return getInput().getMasks().getNir();
		} else
			return null;
	}
}
