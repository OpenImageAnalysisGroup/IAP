package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockResult;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Recolor pictures according to black point for fluo.
 * 
 * @author pape, klukas
 */
public class BlColorBalanceVerticalFluo extends AbstractSnapshotAnalysisBlock {
	
	boolean debug;
	
	BlockResult bpleft, bpright;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		bpleft = getResultSet().searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X.getName(optionsAndResults.getCameraPosition()));
		bpright = getResultSet().searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X.getName(optionsAndResults.getCameraPosition()));
	}
	
	@Override
	protected Image processFLUOimage() {
		Image input = input().images().fluo();
		Image res;
		boolean invert = true;
		if (input != null) {
			res = balance(input, input, 255, invert);
		} else
			res = input;
		return res;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image input = input().masks().fluo();
		Image res;
		boolean invert = true;
		if (input != null) {
			res = balance(input, input, 255, invert);
		} else
			res = input;
		return res;
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape, klukas
	 */
	private double[] getProbablyWhitePixels(Image image, double size,
			BlockResult bpleft, BlockResult bpright) {
		image = image.io().crop().getImage();
		int width = image.getWidth();
		int height = image.getHeight();
		
		int lThres = getInt("balance-l-threshold", -10);
		int abThres = getInt("balance-ab-threshold", 5);
		
		ImageOperation io = new ImageOperation(image);
		
		float[] values;
		if (bpleft != null && bpright != null) {
			int left = (int) (bpleft.getValue() * width);
			int right = (int) (bpright.getValue() * width);
			int a = (right - left) / 4;
			int b = right - left;
			
			values = io.getRGBAverage(left, height / 2 - a / 2, b, a, lThres, abThres, true, debug);
		} else {
			float[] valuesTop, valuesBottom;
			int left = (int) (0.3 * width);
			int right = (int) (width - 0.3 * width);
			int scanHeight = (right - left) / 4;
			int scanWidth = right - left;
			int startHTop = (int) (height * 0.1 - scanHeight / 2);
			// debug = true;
			valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, lThres, abThres, true, debug);
			valuesBottom = io.getRGBAverage(left, height - (startHTop + scanHeight), scanWidth, scanHeight, lThres, 50, true, debug);
			
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
	
	public Image balance(Image input, int whitePoint, boolean invert) {
		return balance(input, input, whitePoint, invert);
	}
	
	/**
	 * @param invert
	 *           - inverts the image (used for fluo)
	 * @return
	 */
	public Image balance(Image input, Image inputUsedForColorAnalysis,
			int whitePoint, boolean invert) {
		BlockResult markerPosLeftY = getResultSet()
				.searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName(optionsAndResults.getCameraPosition()));
		BlockResult markerPosRightY = getResultSet().searchNumericResult(0, 1,
				PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName(optionsAndResults.getCameraPosition()));
		
		BlockResult markerPosLeftX = getResultSet()
				.searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X.getName(optionsAndResults.getCameraPosition()));
		BlockResult markerPosRightX = getResultSet().searchNumericResult(0, 1,
				PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X.getName(optionsAndResults.getCameraPosition()));
		if (inputUsedForColorAnalysis == input)
			inputUsedForColorAnalysis = input.copy();
		
		Image res = input;
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
			if (input != null) {
				Image nir = input;
				// White Balancing
				double[] pix;
				if (invert) {
					// FLUO
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis.io().invert().getImage(),
							getDouble("balance-size-width-top-invert", 0.08), bpleft, bpright);
					Image bal = input.io().invert().imageBalancing(whitePoint, pix).invert().getImage();
					return bal;
				} else {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis, getDouble("balance-size-width-top", 0.3), null, null);
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
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis.io().copy().crop().invert().getImage(),
							getDouble("balance-size-width-side-invert", 0.08), bpleft, bpright);
					res = io.invert().imageBalancing(whitePoint, pix).invert().getImage();
				} else { // nir - remove round shade
					pix = getProbablyWhitePixelsforNir(inputUsedForColorAnalysis);
					res = io.imageBalancing(whitePoint, pix).getImage();
				}
			else
				if (invert) {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis.io().copy().crop().invert().getImage(),
							getDouble("balance-size-width-side-invert", 0.08), bpleft, bpright);
					res = io.invert().imageBalancing(whitePoint, pix).invert().getImage();
				} else {
					pix = getProbablyWhitePixels(inputUsedForColorAnalysis,
							getDouble("balance-size-width-side", 0.08), bpleft, bpright);
					res = io.imageBalancing(whitePoint, pix).getImage();
				}
		}
		return res;
	}
	
	private double[] getProbablyWhitePixelsforNir(Image inputUsedForColorAnalysis) {
		int w = inputUsedForColorAnalysis.getWidth();
		int h = inputUsedForColorAnalysis.getHeight();
		
		int scanHeight = (int) (h * getDouble("balance-scan-height", 0.06));
		int scanWidth = (int) (w * getDouble("balance-scan-width", 0.1));
		
		boolean searchWhiteTrue = true;
		double[] res = new double[9];
		float[] topR, bottomR, topL, bottomL, center, top = new float[3], bottom = new float[3];
		
		int minL = getInt("balance-l-min", -10);// 150;
		
		BlockResult bmpYl1 = getResultSet()
				.searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName(optionsAndResults.getCameraPosition()));
		BlockResult bmpYr1 = getResultSet().searchNumericResult(0, 1,
				PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName(optionsAndResults.getCameraPosition()));
		
		BlockResult bmpYl2 = getResultSet()
				.searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_3_LEFT_Y.getName(optionsAndResults.getCameraPosition()));
		BlockResult bmpYr2 = getResultSet().searchNumericResult(0, 1,
				PropertyNames.RESULT_VIS_MARKER_POS_3_RIGHT_Y.getName(optionsAndResults.getCameraPosition()));
		
		BlockResult bmpXl = getResultSet().searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X.getName(optionsAndResults.getCameraPosition()));
		BlockResult bmpXr = getResultSet()
				.searchNumericResult(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X.getName(optionsAndResults.getCameraPosition()));
		
		if (bmpXl != null && bmpXr != null && bmpYl1 != null && bmpYr1 != null && bmpYl2 != null && bmpYr2 != null) {
			
			int yBottom = (int) ((bmpYl1.getValue() + bmpYr1.getValue()) / 2 * h);
			int yTop = (int) ((bmpYl2.getValue() + bmpYr2.getValue()) / 2 * h);
			int left = (int) (bmpXl.getValue() * w);
			int right = (int) (bmpXr.getValue() * w);
			
			topL = inputUsedForColorAnalysis.io().getRGBAverage(left, yTop, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			topR = inputUsedForColorAnalysis.io().getRGBAverage(right - scanWidth, yTop, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			bottomL = inputUsedForColorAnalysis.io().getRGBAverage(left, yBottom, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			bottomR = inputUsedForColorAnalysis.io().getRGBAverage(right - scanWidth, yBottom, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			
			int i = 0;
			top[i] = (topL[i] + topR[i]) / 2;
			bottom[i] = (bottomL[i] + bottomR[i]) / 2;
			top[++i] = (topL[i] + topR[i]) / 2;
			bottom[i] = (bottomL[i] + bottomR[i]) / 2;
			top[++i] = (topL[i] + topR[i]) / 2;
			bottom[i] = (bottomL[i] + bottomR[i]) / 2;
			
			center = inputUsedForColorAnalysis.io().
					getRGBAverage(w / 2 - scanWidth, h / 2 - scanHeight, scanWidth * 2, scanHeight * 2, minL, 50, searchWhiteTrue, debug);
		} else {
			if (scanWidth > w)
				scanWidth = w;
			if (scanHeight > h)
				scanHeight = h;
			top = inputUsedForColorAnalysis.io().getRGBAverage(w - scanWidth, 0, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			bottom = inputUsedForColorAnalysis.io().getRGBAverage(0, h - scanHeight, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
			center = inputUsedForColorAnalysis.io().
					getRGBAverage(w / 2 - scanWidth / 2, h / 2 - scanHeight / 2, scanWidth, scanHeight, minL, 50, searchWhiteTrue, debug);
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
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Background Correction Fluo";
	}
	
	@Override
	public String getDescription() {
		return "Detect background intensity at the top " +
				"and bottom and correct the overal image intensity according the background black point.";
	}
}
