package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Recolor pictures according to white point (or black point for fluo).
 * 
 * @author pape, klukas
 */
public class BlockColorBalancingFluoAndNir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOimage() {
		BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		FlexibleImage fluo = getInput().getImages().getFluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		int width = fluo.getWidth();
		int height = fluo.getHeight();
		double markerPosY = -1;
		double markerPosX = -1;
		if (markerPosLeftY != null) {
			markerPosY = markerPosLeftY.getValue() * height;
		}
		if (markerPosLeftY == null && markerPosRightY != null) {
			markerPosY = markerPosRightY.getValue() * height;
		}
		if (markerPosLeftX != null) {
			markerPosX = markerPosLeftX.getValue() * width;
		}
		if (markerPosLeftX == null && markerPosRightX != null) {
			markerPosX = fluo.getWidth() - markerPosRightX.getValue() * width;
		}
		double[] pix;
		if (markerPosY != -1)
			pix = getProbablyWhitePixels(io.invert().getImage(), 0.08, markerPosX, markerPosY);
		else
			pix = getProbablyWhitePixels(io.invert().getImage(), 0.08);
		return io.imageBalancing(255, pix).invert().getImage();
		
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		FlexibleImage fluo = getInput().getMasks().getFluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		int width = fluo.getWidth();
		int height = fluo.getHeight();
		double markerPosY = -1;
		double markerPosX = -1;
		if (markerPosLeftY != null) {
			markerPosY = markerPosLeftY.getValue() * height;
		}
		if (markerPosLeftY == null && markerPosRightY != null) {
			markerPosY = markerPosRightY.getValue() * height;
		}
		if (markerPosLeftX != null) {
			markerPosX = markerPosLeftX.getValue() * width;
		}
		if (markerPosLeftX == null && markerPosRightX != null) {
			markerPosX = fluo.getWidth() - markerPosRightX.getValue() * width;
		}
		double[] pix;
		if (markerPosY != -1)
			pix = getProbablyWhitePixels(io.invert().getImage(), 0.08, markerPosX, markerPosY);
		else
			pix = getProbablyWhitePixels(io.invert().getImage(), 0.08);
		return io.imageBalancing(255, pix).invert().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		FlexibleImage res = getInput().getImages().getNir();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (getInput().getImages().getNir() != null) {
				double side = 0.3; // value for white balancing (side width)
				FlexibleImage nir = getInput().getImages().getNir();
				// White Balancing
				double[] pix = BlockColorBalancingFluoAndNir.getProbablyWhitePixels(nir.crop(), side);// 0.08);
				res = new ImageOperation(nir).imageBalancing(170, pix).getImage();
			}
		} else {
			FlexibleImage nir = getInput().getImages().getNir();
			ImageOperation io = new ImageOperation(nir);
			int width = nir.getWidth();
			int height = nir.getHeight();
			double markerPosY = -1;
			double markerPosX = -1;
			if (markerPosLeftY != null) {
				markerPosY = markerPosLeftY.getValue() * height;
			}
			if (markerPosLeftY == null && markerPosRightY != null) {
				markerPosY = markerPosRightY.getValue() * height;
			}
			if (markerPosLeftX != null) {
				markerPosX = markerPosLeftX.getValue() * width;
			}
			if (markerPosLeftX == null && markerPosRightX != null) {
				markerPosX = nir.getWidth() - markerPosRightX.getValue() * width;
			}
			double[] pix;
			if (markerPosY != -1)
				pix = getProbablyWhitePixels(io.getImage(), 0.08, markerPosX, markerPosY);
			else
				pix = getProbablyWhitePixels(io.getImage(), 0.08);
			res = io.imageBalancing(170, pix).getImage();
		}
		return res;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		FlexibleImage res = getInput().getMasks().getNir();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (getInput().getMasks().getNir() != null) {
				double side = 0.3; // value for white balancing (side width)
				FlexibleImage nir = getInput().getMasks().getNir();
				// White Balancing
				double[] pix = BlockColorBalancingFluoAndNir.getProbablyWhitePixels(nir.crop(), side);// 0.08);
				res = new ImageOperation(nir).imageBalancing(170, pix).getImage();
			}
		} else {
			FlexibleImage nir = getInput().getMasks().getNir();
			ImageOperation io = new ImageOperation(nir);
			int width = nir.getWidth();
			int height = nir.getHeight();
			double markerPosY = -1;
			double markerPosX = -1;
			if (markerPosLeftY != null) {
				markerPosY = markerPosLeftY.getValue() * height;
			}
			if (markerPosLeftY == null && markerPosRightY != null) {
				markerPosY = markerPosRightY.getValue() * height;
			}
			if (markerPosLeftX != null) {
				markerPosX = markerPosLeftX.getValue() * width;
			}
			if (markerPosLeftX == null && markerPosRightX != null) {
				markerPosX = nir.getWidth() - markerPosRightX.getValue() * width;
			}
			double[] pix;
			if (markerPosY != -1)
				pix = getProbablyWhitePixels(io.getImage(), 0.08, markerPosX, markerPosY);
			else
				pix = getProbablyWhitePixels(io.getImage(), 0.08);
			res = io.imageBalancing(170, pix).getImage();
		}
		return res;
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape
	 */
	public static double[] getProbablyWhitePixels(FlexibleImage image, double size, double MarkerPosX, double MarkerPosY) {
		int width = image.getWidth();
		int height = image.getHeight();
		int w = (int) (width * size);
		int h = (int) (height * size);
		
		ImageOperation io = new ImageOperation(image);
		
		double[] valuesleft;
		double r, b, g;
		
		if (MarkerPosX == -1) {
			valuesleft = io.getRGBAverage(20, h, w, height - 2 * h, 150, 50, true);
			double[] valuesright = io.getRGBAverage(width - 20 - w, h, w, height - 2 * h, 150, 50, true);
			
			r = (valuesleft[0] + valuesright[0]) / 2;
			g = (valuesleft[1] + valuesright[1]) / 2;
			b = (valuesleft[2] + valuesright[2]) / 2;
		} else {
			valuesleft = io.getRGBAverage((int) MarkerPosX, (int) MarkerPosY, 50, 50, 150, 50, true);
			
			r = valuesleft[0];
			g = valuesleft[1];
			b = valuesleft[2];
		}
		// no function tested
		// double[] valuestop = io.getRGBAverage(img2d, 2 * w, 2 * h, width - 2 * w, h, 150, 50, true);
		// double[] valuesdown = io.getRGBAverage(img2d, 2 * w, height - 2 * h, width - 2 * w, h, 150, 50, true);
		
		// double r = (valuesleft[0] + valuesright[0]) / 2; // + valuestop[0] + valuesdown[0]) / 4;
		// double g = (valuesleft[1] + valuesright[1]) / 2;// + valuestop[1] + valuesdown[1]) / 4;
		// double b = (valuesleft[2] + valuesright[2]) / 2;// + valuestop[2] + valuesdown[2]) / 4;
		
		return new double[] { r * 255, g * 255, b * 255 };
	}
	
	public static double[] getProbablyWhitePixels(FlexibleImage image, double size) {
		double[] res = getProbablyWhitePixels(image, size, -1., -1);
		return res;
	}
	
	public FlexibleImage nirBalance(FlexibleImage input) {
		BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		FlexibleImage res = input;
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (input != null) {
				double side = 0.3; // value for white balancing (side width)
				FlexibleImage nir = input;
				// White Balancing
				double[] pix = BlockColorBalancingFluoAndNir.getProbablyWhitePixels(nir.crop(), side);// 0.08);
				res = new ImageOperation(nir).imageBalancing(170, pix).getImage();
			}
		} else {
			FlexibleImage nir = input;
			ImageOperation io = new ImageOperation(nir);
			int width = input.getWidth();
			int height = input.getHeight();
			double markerPosY = -1;
			double markerPosX = -1;
			if (markerPosLeftY != null) {
				markerPosY = markerPosLeftY.getValue() * height;
			}
			if (markerPosLeftY == null && markerPosRightY != null) {
				markerPosY = markerPosRightY.getValue() * height;
			}
			if (markerPosLeftX != null) {
				markerPosX = markerPosLeftX.getValue() * width;
			}
			if (markerPosLeftX == null && markerPosRightX != null) {
				markerPosX = nir.getWidth() - markerPosRightX.getValue() * width;
			}
			double[] pix;
			if (markerPosY != -1)
				pix = getProbablyWhitePixels(io.getImage(), 0.08, markerPosX, markerPosY);
			else
				pix = getProbablyWhitePixels(io.getImage(), 0.08);
			res = io.imageBalancing(170, pix).getImage();
		}
		return res;
	}
}
