package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlColorBalanceVerticalNir extends AbstractSnapshotAnalysisBlock {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processNIRimage() {
		Image nir = input().images().nir();
		if (!getBoolean("process NIR image", true))
			return nir;
		if (nir == null)
			return null;
		if (getBoolean("black NIR background", false)) {
			ImageOperation inv = nir.io().copy().invert();
			double[] pix = getProbablyWhitePixels(inv.getImage());
			return inv.imageBalancing(getInt("balance-vertical-brigthness", 180), pix).invert().getImage();
		} else {
			double[] pix = getProbablyWhitePixels(nir);
			return nir.io().imageBalancing(getInt("balance-vertical-brigthness", 180), pix).getImage();
		}
	}
	
	@Override
	protected Image processNIRmask() {
		Image nir = input().masks().nir();
		if (!getBoolean("process NIR mask", true))
			return nir;
		if (nir == null)
			return null;
		if (getBoolean("black NIR background", false)) {
			ImageOperation inv = nir.io().copy().invert();
			double[] pix = getProbablyWhitePixels(inv.getImage());
			return inv.imageBalancing(getInt("balance-vertical-brigthness", 180), pix).invert().getImage();
		} else {
			double[] pix = getProbablyWhitePixels(nir);
			return nir.io().imageBalancing(getInt("balance-vertical-brigthness", 180), pix).getImage();
		}
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image (bottom and top for linear interpolation).
	 * 
	 * @author pape
	 */
	private double[] getProbablyWhitePixels(Image image) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		double[] pix;
		
		BlockProperty bpleftX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X.getName(options.getCameraPosition()));
		BlockProperty bprightX = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X.getName(options.getCameraPosition()));
		
		BlockProperty bpleftY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_Y.getName(options.getCameraPosition()));
		BlockProperty bprightY = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_Y.getName(options.getCameraPosition()));
		
		ImageOperation io = new ImageOperation(image);
		
		float[] values;
		float[] valuesTop, valuesBottom = { 0f, 0f, 0f }, valuesBottomLeft, valuesBottomRight;
		int left, right, scanHeight, scanWidth, startHTop;
		int cutoffL = getInt("nir-vertical-balance-l-threshold", -10);
		int cutoffAB = getInt("nir-vertical-balance-ab-threshold", 50);
		
		if (bpleftX == null || bprightX == null || bpleftY == null || bprightY == null) { // fall back to default
			left = (int) (0.3 * width);
			right = (int) (width - 0.3 * width);
			scanHeight = (right - left) / 4;
			scanWidth = right - left;
			startHTop = (int) (height * 0.1 - scanHeight / 2);
			left += scanWidth;
			right -= scanWidth;
			valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, cutoffL, cutoffAB, true, debug);
			valuesBottomLeft = io.getRGBAverage(left / 3, height - (startHTop + scanHeight) - scanHeight, scanWidth / 2,
					scanHeight, cutoffL, cutoffAB, true, debug);
			valuesBottomRight = io.getRGBAverage(width - (scanWidth / 3) - (left / 3), height - (startHTop + scanHeight) - scanHeight, scanWidth / 3,
					scanHeight, cutoffL, cutoffAB, true, debug);
			
		} else { // use marker positions
			left = (int) (bpleftX.getValue() * width);
			right = (int) (bprightX.getValue() * width);
			scanHeight = (right - left) / 8;
			scanWidth = (right - left) / 8;
			left += scanWidth;
			right -= scanWidth;
			startHTop = (int) (height * 0.1 - scanHeight / 2);
			int bottom = (int) ((bpleftY.getValue() + bprightY.getValue()) * height / 2);
			bottom -= scanHeight;
			valuesTop = io.getRGBAverage(width / 2 - (scanWidth * 2 / 2), startHTop, scanWidth * 2, scanHeight, cutoffL, cutoffAB, true, debug);
			valuesBottomLeft = io.getRGBAverage(left - (scanWidth / 2), bottom - scanHeight / 2, scanWidth,
					scanHeight, cutoffL, cutoffAB, true, debug);
			valuesBottomRight = io.getRGBAverage(right - (scanWidth / 2), bottom - scanHeight / 2, scanWidth,
					scanHeight, cutoffL, cutoffAB, true, debug);
			
		}
		
		int i = 0;
		
		valuesBottom[i] = (valuesBottomLeft[i] + valuesBottomRight[i]) / 2;
		valuesBottom[++i] = (valuesBottomLeft[i] + valuesBottomRight[i]) / 2;
		valuesBottom[++i] = (valuesBottomLeft[i] + valuesBottomRight[i]) / 2;
		
		values = new float[6];
		i = 0;
		values[i] = valuesTop[i++];
		values[i] = valuesTop[i++];
		values[i] = valuesTop[i++];
		values[i++] = valuesBottom[0];
		values[i++] = valuesBottom[1];
		values[i++] = valuesBottom[2];
		
		double r = values[0];
		double g = values[1];
		double b = values[2];
		
		double r2 = values[3];
		double g2 = values[4];
		double b2 = values[5];
		
		pix = new double[] { r * 255, g * 255, b * 255, r2 * 255, g2 * 255, b2 * 255 };
		
		return pix;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
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
		return "Background Correction NIR (side, vertical)";
	}
	
	@Override
	public String getDescription() {
		return "Detect background intensity at the top " +
				"and bottom and correct the overal image intensity according the provided background intensity.";
	}
}
