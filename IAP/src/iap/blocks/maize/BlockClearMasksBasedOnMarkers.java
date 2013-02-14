package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlockClearMasksBasedOnMarkers extends AbstractSnapshotAnalysisBlockFIS {
	
	BlockProperty markerPosLeftY, markerPosRightY, markerPosLeftX, markerPosRightX;
	Double markerDist;
	boolean debug;
	boolean hasThreeVerticalMarkerPositionsVisible;
	
	@Override
	protected void prepare() {
		super.prepare();
		
		debug = getBoolean("debug", false);
		hasThreeVerticalMarkerPositionsVisible =
				getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y) != null &&
						getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y) != null;
		
		markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		markerDist = options.getCalculatedBlueMarkerDistance();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		FlexibleImage input = input().masks().vis();
		if (!getBoolean("process vis", true)) {
			return input;
		}
		
		FlexibleImage result = input;
		int color = options.getBackground();
		boolean visCutPosSet = false;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (options.isBarleyInBarleySystem()) {
				double scaleFactor = getDouble("Clear-mask-vis-scale-factor-barleyInBarley", 0.001);
				int cy = (int) (input.getHeight() - scaleFactor * input().images().vis().getHeight());
				result = new ImageOperation(input).clearImageBottom(cy, options.getBackground()).getImage();
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
					if (markerPosLeftY.getValue() > getDouble("Clear-mask-vis-marker-position-y-threshold", 0.55)) {
						int cy = (int) (markerPosLeftY.getValue() * input().masks().vis().getHeight());
						result = new ImageOperation(result).clearImageBottom(cy, color).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
						visCutPosSet = true;
					}
				} else
					if (markerPosLeftY == null && markerPosRightY != null) {
						if (markerPosRightY.getValue() > getDouble("Clear-mask-vis-marker-position-y-threshold", 0.55)) {
							int cy = (int) (markerPosRightY.getValue() * input().masks().vis().getHeight());
							result = new ImageOperation(result).clearImageBottom(cy, color).getImage();
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
				clearSides(result, width, getInt("Clear-mask-vis-side-cut-offset", 10));
			}
		}
		// Default
		// TODO set default value as option 0.136 for maize
		double markerPosLeftYDefault = input.getHeight() - (input.getHeight() * getDouble("Clear-mask-vis-scale-factor-default", 0.05));
		if (options.isBarleyInBarleySystem())
			markerPosLeftYDefault = input.getHeight() - (input.getHeight() * getDouble("Clear-mask-vis-scale-factor-barleyInBarley-default", 0.01));
		int cy = (int) (markerPosLeftYDefault - getInt("Clear-mask-vis-bottom-cut-offset", 30));
		result = new ImageOperation(result).clearImageBottom(cy, options.getBackground()).getImage();
		if (!visCutPosSet)
			getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_VIS, cy);
		return result;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null || input().masks().vis() == null)
			return input().masks().fluo();
		
		FlexibleImage input = input().masks().fluo();
		
		if (!getBoolean("process fluo", true)) {
			return input;
		}
		
		FlexibleImage result = input;
		boolean fluoCutPosSet = false;
		if (!options.isBarleyInBarleySystem())
			if (options.getCameraPosition() == CameraPosition.SIDE) {
				double scaleFactor = getDouble("Clear-mask-scale-factor-decrease-mask", 1);
				
				if (hasThreeVerticalMarkerPositionsVisible) {
					if (markerPosLeftY != null) {
						double markerPosition = markerPosLeftY.getValue();
						if (markerPosition > getDouble("Clear-mask-fluo-marker-position-y-threshold", 0.55)) {
							int cy = (int) (markerPosition * input().images().fluo().getHeight() * scaleFactor); // - 18
							result = new ImageOperation(input).clearImageBottom(cy, options.getBackground()).getImage();
							getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
							fluoCutPosSet = true;
							return result;
						}
						return input;
					}
					if (markerPosLeftY == null && markerPosRightY != null) {
						double markerPosition = markerPosRightY.getValue();
						if (markerPosition > getDouble("Clear-mask-fluo-marker-position-y-threshold", 0.55)) {
							int cy = (int) (markerPosition * input().images().fluo().getHeight() * scaleFactor); // - 18
							result = new ImageOperation(input).clearImageBottom(
									cy, options.getBackground()).getImage();
							getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
							fluoCutPosSet = true;
							return result;
						}
					}
				}
				int width = input().masks().fluo().getWidth();
				result = clearSides(result, width, getInt("Clear-mask-fluo-side-cut-offset", 10));
			}
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isBarley() && options.isHigherResVisCamera()) {
				int width = input().masks().fluo().getWidth();
				clearSides(result, width, getInt("Clear-mask-fluo-side-cut-offset", 10));
			}
		}
		
		// return getInput().getMasks().getFluo();
		// default
		double scaleFactor = getDouble("Clear-mask-fluo-scale-factor-default", 0.05);
		if (options.isBarleyInBarleySystem())
			scaleFactor = getDouble("Clear-mask-fluo-scale-factor-barleyInBarley-default", 0.001);
		
		int cy = (int) (input.getHeight() - scaleFactor * input().images().fluo().getHeight());
		result = new ImageOperation(input).clearImageBottom(cy, options.getBackground()).getImage();
		if (!fluoCutPosSet)
			getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_FLUO, cy);
		return result;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() == null)
			return null;
		
		FlexibleImage input = input().images().nir();
		
		if (!getBoolean("process nir", true)) {
			return input;
		}
		
		FlexibleImage result = input;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (options.isBarleyInBarleySystem())
				result = input;
			else
				result = cutNir(input);
			return result;
		}
		else
			return input().images().nir();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() == null)
			return null;
		
		FlexibleImage input = input().masks().nir();
		
		if (!getBoolean("process nir", true)) {
			return input;
		}
		
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
				clearSides(res, width, getInt("Clear-mask-nir-side-cut-offset", 10));
			}
		}
		return res;
	}
	
	private FlexibleImage cutNir(FlexibleImage input) {
		FlexibleImage result = input;
		if (hasThreeVerticalMarkerPositionsVisible) {
			if (input().masks().nir() != null) {
				if (markerPosLeftY != null) {
					double scaleFactor = markerPosLeftY.getValue();
					if (scaleFactor > getDouble("Clear-mask-nir-marker-position-y-threshold", 0.5)) {
						double pos = scaleFactor * input().images().nir().getHeight();
						result = new ImageOperation(input).clearImageBottom((int) (pos), options.getNirBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (getDouble("Clear-mask-nir-marker-position-x-threshold-left", 0.98) * markerPosLeftX.getValue() * input().masks().nir()
												.getWidth()),
										options.getNirBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (getDouble("Clear-mask-nir-marker-position-x-threshold-right", 1.02) * markerPosRightX.getValue() * input().masks().nir()
												.getWidth()),
										options.getNirBackground()).getImage();
							}
						return result;
					}
					return input;
				}
				if (markerPosLeftY == null && markerPosRightY != null) {
					double scaleFactor = markerPosRightY.getValue();
					if (scaleFactor > getDouble("Clear-mask-nir-marker-position-y-threshold", 0.5)) {
						double pos = (scaleFactor * input().images().nir().getHeight());
						result = new ImageOperation(input).clearImageBottom((int) pos, options.getNirBackground()).getImage();
						getProperties().setNumericProperty(0, PropertyNames.INTERNAL_CROP_BOTTOM_POT_POSITION_NIR, pos);
						boolean clearSides = false;
						if (clearSides)
							if (markerPosLeftX != null) {
								result = new ImageOperation(result).clearImageLeft(
										(int) (getDouble("Clear-mask-nir-marker-position-x-threshold-left-no-y", 0.95) * markerPosLeftX.getValue() * input().masks()
												.nir().getWidth()), options.getNirBackground()).getImage();
							}
						if (clearSides)
							if (markerPosRightX != null) {
								result = new ImageOperation(result).clearImageRight(
										(int) (getDouble("Clear-mask-nir-marker-position-x-threshold-right-no-y", 1.05) * markerPosRightX.getValue() * input().masks()
												.nir().getWidth()), options.getNirBackground()).getImage();
							}
					}
				}
			}
		}
		return result;
	}
	
	public FlexibleImage clearSides(FlexibleImage result, int width) {
		return clearSides(result, width, getInt("Clear-mask-vis-side-cut-offset-three-markers", 120));
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
				double dist = markerDist * 0.15;
				double posLeft = width / 2 - markerDist / 2;
				double posRight = width / 2 + markerDist / 2;
				
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
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
}
