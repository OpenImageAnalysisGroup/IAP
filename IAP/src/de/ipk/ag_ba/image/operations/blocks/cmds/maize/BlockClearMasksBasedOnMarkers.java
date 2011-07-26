package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClearMasksBasedOnMarkers extends AbstractSnapshotAnalysisBlockFIS {
	
	BlockProperty markerPosLeftY, markerPosRightY, markerPosLeftX, markerPosRightX;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getVis();
			
			if (markerPosLeftY == null && markerPosRightY == null) { // set default
				getProperties().setNumericProperty(0, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y, input.getHeight() * 0.136);
			}
			
			FlexibleImage result = input;
			
			if (markerPosLeftY != null) {
				// System.out.println("mark: " + markerPosLeftY.getValue());
				if (markerPosLeftY.getValue() > 0.5) {
					int cy = (int) (markerPosLeftY.getValue() * getInput().getMasks().getVis().getHeight()) - options.getIntSetting(Setting.BOTTOM_CUT_DELAY_VIS);
					result = new ImageOperation(result).clearImageBottom(cy, options.getBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
				}
			} else
				if (markerPosLeftY == null && markerPosRightY != null) {
					// System.out.println("mark: " + markerPosRightY.getValue());
					if (markerPosRightY.getValue() > 0.5) {
						int cy = (int) (markerPosRightY.getValue() * getInput().getMasks().getVis().getHeight())
									- options.getIntSetting(Setting.BOTTOM_CUT_DELAY_VIS);
						result = new ImageOperation(result).clearImageBottom(
									cy, options.getBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
					}
				}
			
			boolean clearSides = false;
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
		if (getInput().getMasks().getFluo() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage input = getInput().getMasks().getFluo();
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			
			if (markerPosLeftY != null) {
				double temp = markerPosLeftY.getValue();
				if (temp > 0.5) {
					int cy = (int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor) - 18;
					FlexibleImage result = new ImageOperation(input).clearImageBottom(
							cy, options.getBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
					return result;
				}
				return input;
			}
			if (markerPosLeftY == null && markerPosRightY != null) {
				double temp = markerPosRightY.getValue();
				if (temp > 0.5) {
					int cy = (int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor) - 18;
					FlexibleImage result = new ImageOperation(input).clearImageBottom(
							cy, options.getBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
					return result;
				}
				return input;
			}
		}
		return getInput().getMasks().getFluo();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput().getImages().getNir() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (getInput().getImages().getNir() != null) {
				FlexibleImage input = getInput().getImages().getNir();
				
				if (markerPosLeftY != null) {
					double temp = markerPosLeftY.getValue();
					if (temp > 0.5) {
						double pos = temp * getInput().getImages().getNir().getHeight();
						if (pos > 10)
							pos -= 18;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
									(int) (pos), options.getBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
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
				if (markerPosLeftY == null && markerPosRightY != null) {
					double temp = markerPosRightY.getValue();
					if (temp > 0.5) {
						double pos = (temp * getInput().getImages().getNir().getHeight());
						if (pos > 10)
							pos -= 18;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
									(int) pos, options.getBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
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
			return getInput().getImages().getNir();
		} else
			return getInput().getImages().getNir();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (getInput().getMasks().getNir() != null) {
				FlexibleImage input = getInput().getMasks().getNir();
				
				if (markerPosLeftY != null) {
					double temp = markerPosLeftY.getValue();
					if (temp > 0.5) {
						double pos = temp * getInput().getImages().getNir().getHeight();
						if (pos > 10)
							pos -= 10;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
									(int) (pos), options.getBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
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
				if (markerPosLeftY == null && markerPosRightY != null) {
					double temp = markerPosRightY.getValue();
					if (temp > 0.5) {
						double pos = (temp * getInput().getImages().getNir().getHeight());
						if (pos > 10)
							pos -= 14;
						FlexibleImage result = new ImageOperation(input).clearImageBottom(
									(int) pos, options.getBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
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
			return getInput().getMasks().getNir();
	}
}
