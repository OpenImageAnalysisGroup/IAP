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
	
	BlockProperty bpleft, bpright;
	
	@Override
	protected void prepare() {
		super.prepare();
		bpleft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		bpright = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage input = getInput().getImages().getFluo();
		FlexibleImage res;
		if (input != null)
			res = balance(input, input.getIO().medianFilter32Bit().getImage(), 255, true);
		else
			res = input;
		return res;
		
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage input = getInput().getMasks().getFluo();
		FlexibleImage res;
		if (input != null)
			res = balance(input, 255, true);
		else
			res = input;
		return res;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage input = getInput().getImages().getNir();
		FlexibleImage res;
		if (input != null)
			res = balance(input, 170, false);
		else
			res = input;
		return res;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage input = getInput().getMasks().getNir();
		FlexibleImage res;
		if (input != null)
			res = balance(input, 170, false);
		else
			res = input;
		return res;
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape
	 */
	private static double[] getProbablyWhitePixels(FlexibleImage image, double size, double MarkerPosX, double MarkerPosY, BlockProperty bpleft,
			BlockProperty bpright) {
		int width = image.getWidth();
		int height = image.getHeight();
		int w = (int) (width * size);
		int h = (int) (height * size);
		
		ImageOperation io = new ImageOperation(image);
		
		// float[] valuesleft;
		// double r, b, g;
		//
		// if (MarkerPosX == -1) {
		// valuesleft = io.getRGBAverage(20, h, w, height - 2 * h, 150, 50, true);
		// float[] valuesright = io.getRGBAverage(width - 20 - w, h, w, height - 2 * h, 150, 50, true);
		//
		// r = (valuesleft[0] + valuesright[0]) / 2;
		// g = (valuesleft[1] + valuesright[1]) / 2;
		// b = (valuesleft[2] + valuesright[2]) / 2;
		// } else {
		// valuesleft = io.getRGBAverage((int) MarkerPosX, (int) MarkerPosY, 50, 50, 150, 50, true);
		
		float[] values;
		if (bpleft != null && bpright != null) {
			int left = (int) (bpleft.getValue() * width);
			int right = (int) (bpright.getValue() * width);
			int a = (right - left) / 4;
			int b = right - left;
			
			values = io.getRGBAverage(left, height / 2 - a / 2, b, a, 150, 50, true);
		} else {
			int left = (int) (0.3 * width);
			int right = (int) (width - 0.3 * width);
			int a = (right - left) / 4;
			int b = right - left;
			
			values = io.getRGBAverage(left, height / 2 - a / 2, b, a, 150, 50, true);
		}
		double r = values[0];
		double g = values[1];
		double b = values[2];
		// no function tested
		// double[] valuestop = io.getRGBAverage(img2d, 2 * w, 2 * h, width - 2 * w, h, 150, 50, true);
		// double[] valuesdown = io.getRGBAverage(img2d, 2 * w, height - 2 * h, width - 2 * w, h, 150, 50, true);
		
		// double r = (valuesleft[0] + valuesright[0]) / 2; // + valuestop[0] + valuesdown[0]) / 4;
		// double g = (valuesleft[1] + valuesright[1]) / 2;// + valuestop[1] + valuesdown[1]) / 4;
		// double b = (valuesleft[2] + valuesright[2]) / 2;// + valuestop[2] + valuesdown[2]) / 4;
		
		return new double[] { r * 255, g * 255, b * 255 };
	}
	
	private static double[] getProbablyWhitePixels(FlexibleImage image, double size, BlockProperty bpleft,
			BlockProperty bpright) {
		double[] res = getProbablyWhitePixels(image, size, -1., -1, bpleft, bpleft);
		return res;
	}
	
	public FlexibleImage balance(FlexibleImage input, int whitePoint, boolean invert) {
		return balance(input, input, whitePoint, invert);
	}
	
	/**
	 * @param invert
	 *           - inverts the image (used for fluo)
	 * @return
	 */
	public FlexibleImage balance(FlexibleImage input, FlexibleImage inputUsedForColorAnalysis, int whitePoint, boolean invert) {
		BlockProperty markerPosLeftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty markerPosRightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty markerPosLeftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty markerPosRightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		if (inputUsedForColorAnalysis == input)
			inputUsedForColorAnalysis = input.copy();
		
		FlexibleImage res = input;
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (input != null) {
				double side = 0.3; // value for white balancing (side width)
				FlexibleImage nir = input;
				// White Balancing
				double[] pix;
				if (invert) {
					ImageOperation io = new ImageOperation(input);
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis.getIO().invert().getImage(), 0.08, bpleft, bpright);
					return io.invert().imageBalancing(whitePoint, pix).invert().getImage();
				} else {
					pix = BlockColorBalancingFluoAndNir.getProbablyWhitePixels(inputUsedForColorAnalysis.crop(), side, bpleft, bpright);// 0.08);
					res = new ImageOperation(nir).imageBalancing(whitePoint, pix).getImage();
				}
			}
		} else {
			ImageOperation io = new ImageOperation(input);
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
				markerPosX = input.getWidth() - markerPosRightX.getValue() * width;
			}
			double[] pix;
			if (markerPosY != -1)
				if (invert) {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis.getIO().invert().getImage(), 0.08, markerPosX, markerPosY, bpleft, bpright);
					res = io.invert().imageBalancing(whitePoint, pix).invert().getImage();
				} else {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis, 0.08, markerPosX, markerPosY, bpleft, bpright);
					res = io.imageBalancing(whitePoint, pix).getImage();
				}
			else
				if (invert) {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis.getIO().invert().getImage(), 0.08, bpleft, bpright);
					res = io.invert().imageBalancing(whitePoint, pix).invert().getImage();
				} else {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis, 0.08, bpleft, bpright);
					res = io.imageBalancing(whitePoint, pix).getImage();
				}
		}
		return res;
	}
}
