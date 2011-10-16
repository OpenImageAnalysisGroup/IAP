package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClearMasksBasedOnMarkers_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	BlockProperty markerPosLeftY, markerPosRightY, markerPosLeftX, markerPosRightX, markerDist;
	
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		markerDist = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		
		FlexibleImage input = getInput().getMasks().getVis();
		FlexibleImage result = input;
		int color = options.getBackground();
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			
			if (options.isBarleyInBarleySystem()) {
				double temp = 0.01;
	
				int cy = (int) (input.getHeight() - temp * getInput().getImages().getVis().getHeight());
				result = new ImageOperation(input).clearImageBottom(
						cy, options.getBackground()).getImage();
				getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
				return result;
			}
			
			// Clear bottom
			if (debug)
				color = Color.RED.getRGB();
			else
				color = options.getBackground();
			if (markerPosLeftY != null) {
				if (markerPosLeftY.getValue() > 0.55) {
					// Integer cuof = options.getIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS);
					int cy = (int) (markerPosLeftY.getValue() * getInput().getMasks().getVis().getHeight());
					result = new ImageOperation(result).clearImageBottom(cy, color).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
				}
			} else
				if (markerPosLeftY == null && markerPosRightY != null) {
					if (markerPosRightY.getValue() > 0.55) {
						// - options.getIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS)
						int cy = (int) (markerPosRightY.getValue() * getInput().getMasks().getVis().getHeight());
						result = new ImageOperation(result).clearImageBottom(
									cy, color).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
					}
				}
			// Clear Sides
			int width = getInput().getMasks().getVis().getWidth();
			result = clearSides(result, width);
		}
		// Default
		// TODO set default value as option 0.136 for maize
		double markerPosLeftYDefault = input.getHeight() - (input.getHeight() * 0.05);
		if (options.isBarleyInBarleySystem())
			markerPosLeftYDefault = input.getHeight() - (input.getHeight() * 0.01);
		int cy = (int) (markerPosLeftYDefault
				- options.getIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS));
		result = new ImageOperation(result).clearImageBottom(
				cy, options.getBackground()).getImage();
		getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
		return result;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null || getInput().getMasks().getVis()==null)
			return null;
		
		FlexibleImage input = getInput().getMasks().getFluo();
		FlexibleImage result = input;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			double scaleFactor = options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_MASK);
			
			if (markerPosLeftY != null) {
				double temp = markerPosLeftY.getValue();
				if (temp > 0.55) {
					int cy = (int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor) - 18;
					result = new ImageOperation(input).clearImageBottom(
							cy, options.getBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
					return result;
				}
				return input;
			}
			if (markerPosLeftY == null && markerPosRightY != null) {
				double temp = markerPosRightY.getValue();
				if (temp > 0.55) {
					int cy = (int) (temp * getInput().getImages().getFluo().getHeight() * scaleFactor) - 18;
					result = new ImageOperation(input).clearImageBottom(
							cy, options.getBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
					return result;
				}
			}
			
			int width = getInput().getMasks().getVis().getWidth();
			result = clearSides(result, width);
		}
		// return getInput().getMasks().getFluo();
		// default
		double temp = 0.05;
		if (options.isBarleyInBarleySystem())
			temp = 0.01;

		int cy = (int) (input.getHeight() - temp * getInput().getImages().getFluo().getHeight());
		result = new ImageOperation(input).clearImageBottom(
				cy, options.getBackground()).getImage();
		getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
		return result;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (getInput().getImages().getNir() == null)
			return null;
		FlexibleImage input = getInput().getImages().getNir();
		FlexibleImage result = input;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			result = cutNir(input);
			double temp = 0.05;
			if (options.isBarleyInBarleySystem())
				temp = 0.01;
			int cy = (int) (input.getHeight() - temp * input.getHeight());
			result = new ImageOperation(input).clearImageBottom(
						cy, new Color(180, 180, 180).getRGB()).getImage();
			return result;
		} else
			return getInput().getMasks().getNir();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (getInput().getMasks().getNir() == null)
			return null;
		
		FlexibleImage input = getInput().getMasks().getNir();
		FlexibleImage result = input;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			result = cutNir(input);
			// return getInput().getMasks().getNir();
			// default
			double temp = 0.05;
			int cy = (int) (input.getHeight() - temp * input.getHeight());
			result = new ImageOperation(input).clearImageBottom(
						cy, new Color(180, 180, 180).getRGB()).getImage();
			getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
			return result;
		} else
			return getInput().getMasks().getNir();
	}
	
	private FlexibleImage cutNir(FlexibleImage input) {
		FlexibleImage result = null;
		if (getInput().getMasks().getNir() != null) {
			if (markerPosLeftY != null) {
				double temp = markerPosLeftY.getValue();
				if (temp > 0.5) {
					double pos = temp * getInput().getImages().getNir().getHeight();
					// if (pos > 10)
					// pos -= 10;
					result = new ImageOperation(input).clearImageBottom(
								(int) (pos), options.getNirBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
					boolean clearSides = false;
					if (clearSides)
						if (markerPosLeftX != null) {
							result = new ImageOperation(result).clearImageLeft(
										(int) (0.98 * markerPosLeftX.getValue() * getInput().getMasks().getNir().getWidth()), options.getNirBackground()).getImage();
						}
					if (clearSides)
						if (markerPosRightX != null) {
							result = new ImageOperation(result).clearImageRight(
										(int) (1.02 * markerPosRightX.getValue() * getInput().getMasks().getNir().getWidth()), options.getNirBackground()).getImage();
						}
					return result;
				}
				return input;
			}
			if (markerPosLeftY == null && markerPosRightY != null) {
				double temp = markerPosRightY.getValue();
				if (temp > 0.5) {
					double pos = (temp * getInput().getImages().getNir().getHeight());
					// if (pos > 10)
					// pos -= 14;
					result = new ImageOperation(input).clearImageBottom(
								(int) pos, options.getNirBackground()).getImage();
					getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
					boolean clearSides = false;
					if (clearSides)
						if (markerPosLeftX != null) {
							result = new ImageOperation(result).clearImageLeft(
										(int) (0.95 * markerPosLeftX.getValue() * getInput().getMasks().getNir().getWidth()), options.getNirBackground()).getImage();
						}
					if (clearSides)
						if (markerPosRightX != null) {
							result = new ImageOperation(result).clearImageRight(
										(int) (1.05 * markerPosRightX.getValue() * getInput().getMasks().getNir().getWidth()), options.getNirBackground()).getImage();
						}
				}
			}
		}
		return result;
	}
	
	public FlexibleImage clearSides(FlexibleImage result, int width) {
		int colorRight, colorLeft;
		if (debug) {
			colorRight = Color.BLUE.getRGB();
			colorLeft = Color.YELLOW.getRGB();
		} else {
			colorRight = options.getBackground();
			colorLeft = options.getBackground();
		}
		
		if (!options.isMaize()) {
			if (markerPosLeftX != null) {
				result = new ImageOperation(result).clearImageLeft(
							(int) (120 + markerPosLeftX.getValue() * width), colorLeft).getImage();
			}
			if (markerPosRightX != null) {
				result = new ImageOperation(result).clearImageRight(
							(int) (-120 + markerPosRightX.getValue() * width), colorRight).getImage();
			}
		}
		
		if (options.isMaize()) {
			if (markerDist != null && markerPosLeftX != null && markerPosRightX != null) {
				double dist = markerDist.getValue() * 0.15;
				double posLeft = width / 2 - markerDist.getValue() / 2;
				double posRight = width / 2 + markerDist.getValue() / 2;
				
				result = new ImageOperation(result).clearImageRight(
						(int) (dist + posRight), colorRight)
						.clearImageLeft((int) (-dist + posLeft), colorLeft)
						.getImage();
			} else {
				if (markerPosLeftX != null) {
					result = new ImageOperation(result).clearImageLeft(
								(int) (-120 + markerPosLeftX.getValue() * width), colorLeft).getImage();
				}
				if (markerPosRightX != null) {
					result = new ImageOperation(result).clearImageRight(
							(int) (120 + markerPosRightX.getValue() * width), colorRight).getImage();
				}
			}
		}
		return result;
	}
}
