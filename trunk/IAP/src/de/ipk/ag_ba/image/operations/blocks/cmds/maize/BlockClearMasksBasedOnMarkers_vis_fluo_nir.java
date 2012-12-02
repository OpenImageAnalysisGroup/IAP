package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClearMasksBasedOnMarkers_vis_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	BlockProperty markerPosLeftY, markerPosRightY, markerPosLeftX, markerPosRightX, markerDist;
	
	boolean debug = false;
	
	boolean hasThreeVerticalMarkerPositionsVisible = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		hasThreeVerticalMarkerPositionsVisible =
				getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y) != null &&
						getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y) != null;
		
		markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		markerDist = getProperties().getNumericProperty(0, 1, PropertyNames.MARKER_DISTANCE_LEFT_RIGHT);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		FlexibleImage input = input().masks().vis();
		FlexibleImage result = input;
		int color = options.getBackground();
		boolean visCutPosSet = false;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			
			if (options.isBarleyInBarleySystem()) {
				double temp = 0.001;
				
				int cy = (int) (input.getHeight() - temp * input().images().vis().getHeight());
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
			if (hasThreeVerticalMarkerPositionsVisible) {
				if (markerPosLeftY != null) {
					if (markerPosLeftY.getValue() > 0.55) {
						// Integer cuof = options.getIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS);
						int cy = (int) (markerPosLeftY.getValue() * input().masks().vis().getHeight());
						result = new ImageOperation(result).clearImageBottom(cy, color).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
						visCutPosSet = true;
					}
				} else
					if (markerPosLeftY == null && markerPosRightY != null) {
						if (markerPosRightY.getValue() > 0.55) {
							// - options.getIntSetting(Setting.BOTTOM_CUT_OFFSET_VIS)
							int cy = (int) (markerPosRightY.getValue() * input().masks().vis().getHeight());
							result = new ImageOperation(result).clearImageBottom(
									cy, color).getImage();
							getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
							visCutPosSet = true;
						}
					}
			}
			// Clear Sides
			int width = input().masks().vis().getWidth();
			result = clearSides(result, width);
		}
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isBarley() && options.isHigherResVisCamera()) {
				int width = input().masks().vis().getWidth();
				clearSides(result, width, 10);
			}
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
		if (!visCutPosSet)
			getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
		return result;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null || input().masks().vis() == null)
			return input().masks().fluo();
		
		FlexibleImage input = input().masks().fluo();
		FlexibleImage result = input;
		boolean fluoCutPosSet = false;
		if (!options.isBarleyInBarleySystem())
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				double scaleFactor = 1d;
				
				if (hasThreeVerticalMarkerPositionsVisible) {
					if (markerPosLeftY != null) {
						double temp = markerPosLeftY.getValue();
						if (temp > 0.55) {
							int cy = (int) (temp * input().images().fluo().getHeight() * scaleFactor); // - 18
							result = new ImageOperation(input).clearImageBottom(
									cy, options.getBackground()).getImage();
							getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
							fluoCutPosSet = true;
							return result;
						}
						return input;
					}
					if (markerPosLeftY == null && markerPosRightY != null) {
						double temp = markerPosRightY.getValue();
						if (temp > 0.55) {
							int cy = (int) (temp * input().images().fluo().getHeight() * scaleFactor); // - 18
							result = new ImageOperation(input).clearImageBottom(
									cy, options.getBackground()).getImage();
							getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
							fluoCutPosSet = true;
							return result;
						}
					}
				}
				int width = input().masks().fluo().getWidth();
				result = clearSides(result, width, 10);
			}
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isBarley() && options.isHigherResVisCamera()) {
				int width = input().masks().fluo().getWidth();
				clearSides(result, width, 10);
			}
		}
		
		// return getInput().getMasks().getFluo();
		// default
		double temp = 0.05;
		if (options.isBarleyInBarleySystem())
			temp = 0.001;
		
		int cy = (int) (input.getHeight() - temp * input().images().fluo().getHeight());
		result = new ImageOperation(input).clearImageBottom(
				cy, options.getBackground()).getImage();
		if (!fluoCutPosSet)
			getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
		return result;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() == null)
			return null;
		FlexibleImage input = input().images().nir();
		FlexibleImage result = input;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (options.isBarleyInBarleySystem())
				result = input;
			else
				result = cutNir(input);
			return result;
		} else
			return input().images().nir();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() == null)
			return null;
		
		FlexibleImage input = input().masks().nir();
		FlexibleImage result = input;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (options.isBarleyInBarleySystem())
				result = input;
			else
				result = cutNir(input);
			return result;
		}
		FlexibleImage res = input().masks().nir();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isBarley() && options.isHigherResVisCamera()) {
				int width = input().masks().nir().getWidth();
				clearSides(res, width, 10);
			}
		}
		return res;
	}
	
	private FlexibleImage cutNir(FlexibleImage input) {
		FlexibleImage result = input;
		if (hasThreeVerticalMarkerPositionsVisible) {
			if (input().masks().nir() != null) {
				if (markerPosLeftY != null) {
					double temp = markerPosLeftY.getValue();
					if (temp > 0.5) {
						double pos = temp * input().images().nir().getHeight();
						// if (pos > 10)
						// pos -= 10;
						result = new ImageOperation(input).clearImageBottom(
								(int) (pos), options.getNirBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (0.98 * markerPosLeftX.getValue() * input().masks().nir().getWidth()), options.getNirBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (1.02 * markerPosRightX.getValue() * input().masks().nir().getWidth()), options.getNirBackground()).getImage();
							}
						return result;
					}
					return input;
				}
				if (markerPosLeftY == null && markerPosRightY != null) {
					double temp = markerPosRightY.getValue();
					if (temp > 0.5) {
						double pos = (temp * input().images().nir().getHeight());
						// if (pos > 10)
						// pos -= 14;
						result = new ImageOperation(input).clearImageBottom(
								(int) pos, options.getNirBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (0.95 * markerPosLeftX.getValue() * input().masks().nir().getWidth()), options.getNirBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (1.05 * markerPosRightX.getValue() * input().masks().nir().getWidth()), options.getNirBackground()).getImage();
							}
					}
				}
			}
		}
		return result;
	}
	
	public FlexibleImage clearSides(FlexibleImage result, int width) {
		return clearSides(result, width, 120);
	}
	
	public FlexibleImage clearSides(FlexibleImage result, int width, int off) {
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
						(int) (off + markerPosLeftX.getValue() * width), colorLeft).getImage();
			}
			if (markerPosRightX != null) {
				result = new ImageOperation(result).clearImageRight(
						(int) (-off + markerPosRightX.getValue() * width), colorRight).getImage();
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
							(int) (-off + markerPosLeftX.getValue() * width), colorLeft).getImage();
				}
				if (markerPosRightX != null) {
					result = new ImageOperation(result).clearImageRight(
							(int) (off + markerPosRightX.getValue() * width), colorRight).getImage();
				}
			}
		}
		return result;
	}
}
