package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

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
public class BlockColorBalancing_fluo_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	static boolean debug = false;
	
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
			res = balance(input, 180, false);
		else
			res = null;
		return res;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage input = getInput().getMasks().getNir();
		FlexibleImage res;
		if (input != null)
			res = balance(input, 180, false);
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
		
		ImageOperation io = new ImageOperation(image);
		
		float[] values;
		if (bpleft != null && bpright != null) {
			int left = (int) (bpleft.getValue() * width);
			int right = (int) (bpright.getValue() * width);
			int a = (right - left) / 4;
			int b = right - left;
			
			values = io.getRGBAverage(left, height / 2 - a / 2, b, a, 150, 50, true, false);
		} else {
			float[] valuesTop, valuesBottom;
			int left = (int) (0.3 * width);
			int right = (int) (width - 0.3 * width);
			int scanHeight = (right - left) / 4;
			int scanWidth = right - left;
			int startHTop = (int) (height * 0.1 - scanHeight / 2);
			
			// values = io.getRGBAverage(left, height / 2 - scanHeight / 2, scanWidth, scanHeight, 150, 50, true);
			valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, 150, 50, true, false);
			valuesBottom = io.getRGBAverage(left, height - (startHTop + scanHeight), scanWidth, scanHeight, 150, 50, true, false);
			
			if (debug) {
				image.copy().getIO().getCanvas().fillRect(left, startHTop, scanWidth, scanHeight, Color.RED.getRGB(), 0.5)
						.fillRect(left, height - (startHTop + scanHeight), scanWidth, scanHeight, Color.RED.getRGB(), 0.5).getImage()
						.print("region scan for color balance", debug);
			}
			values = new float[6];
			int i = 0;
			values[i] = valuesTop[i++];
			values[i] = valuesTop[i++];
			values[i] = valuesTop[i++];
			i = 0;
			values[i] += valuesBottom[i++];
			values[i] += valuesBottom[i++];
			values[i] += valuesBottom[i++];
			
			values[0] = values[0] / 2f;
			values[1] = values[1] / 2f;
			values[2] = values[2] / 2f;
		}
		double r = values[0];
		double g = values[1];
		double b = values[2];
		
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
	public FlexibleImage balance(FlexibleImage input, FlexibleImage inputUsedForColorAnalysis,
			int whitePoint, boolean invert) {
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
					pix = BlockColorBalancing_fluo_nir.getProbablyWhitePixels(inputUsedForColorAnalysis, side, null, null);// 0.08);
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
				} else { // nir - remove round shade
					pix = getProbablyWhitePixelsforNir(inputUsedForColorAnalysis);
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
	
	private double[] getProbablyWhitePixelsforNir(FlexibleImage inputUsedForColorAnalysis) {
		int w = inputUsedForColorAnalysis.getWidth();
		int h = inputUsedForColorAnalysis.getHeight();
		
		int scanHeight = (int) (h * 0.06);
		int scanWidth = (int) (w * 0.1);
		
		boolean searchWhiteTrue = true;
		double[] res = new double[9];
		float[] topR, bottomR, topL, bottomL, center, top = new float[3], bottom = new float[3];
		
		int minL = 215;// 150;
		
		BlockProperty bmpYl1 = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y);
		BlockProperty bmpYr1 = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y);
		
		BlockProperty bmpYl2 = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y);
		BlockProperty bmpYr2 = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y);
		
		BlockProperty bmpXl = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty bmpXr = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		if (bmpXl != null && bmpXr != null && bmpYl1 != null && bmpYr1 != null && bmpYl2 != null && bmpYr2 != null) {
			
			int yBottom = (int) ((bmpYl1.getValue() + bmpYr1.getValue()) / 2 * h);
			int yTop = (int) ((bmpYl2.getValue() + bmpYr2.getValue()) / 2 * h);
			int left = (int) (bmpXl.getValue() * w);
			int right = (int) (bmpXr.getValue() * w);
			
			topL = inputUsedForColorAnalysis.getIO().getRGBAverage(left, yTop, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			topR = inputUsedForColorAnalysis.getIO().getRGBAverage(right - scanWidth, yTop, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			bottomL = inputUsedForColorAnalysis.getIO().getRGBAverage(left, yBottom, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			bottomR = inputUsedForColorAnalysis.getIO().getRGBAverage(right - scanWidth, yBottom, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			
			int i = 0;
			top[i] = (topL[i] + topR[i]) / 2;
			bottom[i] = (bottomL[i] + bottomR[i]) / 2;
			top[++i] = (topL[i] + topR[i]) / 2;
			bottom[i] = (bottomL[i] + bottomR[i]) / 2;
			top[++i] = (topL[i] + topR[i]) / 2;
			bottom[i] = (bottomL[i] + bottomR[i]) / 2;
			
			center = inputUsedForColorAnalysis.getIO().getRGBAverage(w / 2 - scanWidth, h / 2 - scanHeight, scanWidth * 2, scanHeight * 2, minL, 50,
					searchWhiteTrue,
					debug);
		} else {
			top = inputUsedForColorAnalysis.getIO().getRGBAverage(w - scanWidth, 0, scanWidth, scanHeight, minL, 50, searchWhiteTrue, true);
			bottom = inputUsedForColorAnalysis.getIO().getRGBAverage(0, h - scanHeight, scanWidth, scanHeight, minL, 50, searchWhiteTrue, true);
			center = inputUsedForColorAnalysis.getIO().getRGBAverage(w / 2 - scanWidth / 2, h / 2 - scanHeight / 2, scanWidth, scanHeight, minL, 50,
					searchWhiteTrue, true);
		}
		// get TopRight
		res[0] = top[0] * 255f;
		res[1] = top[1] * 255f;
		res[2] = top[2] * 255f;
		// get BottomLeft
		res[3] = bottom[0] * 255f;
		res[4] = bottom[1] * 255f;
		res[5] = bottom[2] * 255f;
		// get Center
		res[6] = center[0] * 255f;
		res[7] = center[1] * 255f;
		res[8] = center[2] * 255f;
		return res;
	}
}
