package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PixelProcessor;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Recolor pictures according to white point (or black point for fluo).
 * May adjust the top-right-half brightness with the remark setting "top.right.half.brightness".
 * 
 * @author pape, klukas
 */
public class BlColorBalanceVerticalVis extends AbstractSnapshotAnalysisBlock {
	
	boolean debug;
	private Color targetBackgroundColor;
	private int percent;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		
		targetBackgroundColor = getColor("Calibration Target Background Color", Color.WHITE);
		percent = getInt("Size of Scan-Result (Percent)", 10);
	}
	
	@Override
	protected Image processVISimage() {
		if (input() == null || input().images() == null)
			return null;
		Image vis = input().images().vis();
		if (vis == null)
			return null;
		if (!getBoolean("enabled", options.getCameraPosition() == CameraPosition.SIDE))
			return vis;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			pix = getPixelsSimilarToReferenceColor(vis.copy().io().blur(getInt("vis-balance-blur", 5)).getImage(), true);
		} else {
			boolean adjustLeftRight = getBoolean("Adjust Left and Right Separately", false);
			boolean adjustAuto = getBoolean("Automatic Adjustment Left and Right", false);
			final double brightup = getDouble("Manual Brightness Correction Factor Right Side", 1);
			if (adjustLeftRight || adjustAuto) {
				if (adjustAuto) {
					double[] pixLeft, pixRight;
					pixRight = getPixelsSimilarToReferenceColor(vis.io().mirrorLeftToRight().getImage().show("left part", debug),
							!getBoolean("Adjust to Center Brightness", true));
					pixLeft = getPixelsSimilarToReferenceColor(vis.io().flipHor().mirrorLeftToRight().getImage().show("right part", debug),
							!getBoolean("Adjust to Center Brightness", true));
					return io.imageBalancing(255, pixLeft, pixRight).getImage().show("after", false);
				} else {
					io = io.adjustPixelValues(new PixelProcessor() {
						float[] hsb = new float[3];
						
						@Override
						public int processPixelForegroundValue(int x, int y, int rgb, int w, int h) {
							if (x >= w / 2) {
								int r = ((rgb >> 16) & 0xff);
								int g = ((rgb >> 8) & 0xff);
								int b = (rgb & 0xff);
								Color.RGBtoHSB(r, g, b, hsb);
								hsb[2] *= brightup;
								if (hsb[2] > 1)
									hsb[2] = 1;
								return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
							} else
								return rgb;
						}
					});
				}
			}
			pix = getPixelsSimilarToReferenceColor(vis, !getBoolean("Adjust to Center Brightness", true));
		}
		return io.imageBalancing(255, pix).getImage().show("after", false);
	}
	
	@Override
	protected Image processVISmask() {
		Image vis = input().masks().vis();
		if (vis == null)
			return null;
		if (!getBoolean("enabled", options.getCameraPosition() == CameraPosition.SIDE))
			return vis;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			pix = getPixelsSimilarToReferenceColor(vis.copy().io().blur(getInt("vis-balance-blur", 5)).getImage(),
					!getBoolean("Adjust to Center Brightness", true));
		else
			pix = getPixelsSimilarToReferenceColor(vis, !getBoolean("Adjust to Center Brightness", true));
		return io.imageBalancing(255, pix).getImage().show("after", false);
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image. (bottom and top for linear interpolation, center for default)
	 * 
	 * @author pape
	 */
	private double[] getPixelsSimilarToReferenceColor(Image image, boolean verticalGradientSideView) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		ImageOperation io = new ImageOperation(image);
		
		BlockProperty bpleft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X.getName());
		BlockProperty bpright = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X.getName());
		
		float[] values;
		if (!verticalGradientSideView) {
			float[] valuesTop, valuesBottom;
			if (bpleft != null && bpright != null) {
				int left = (int) (bpleft.getValue() * width);
				int right = (int) (bpright.getValue() * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				
				values = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, height / 2 - scanHeight / 2, scanWidth, scanHeight, percent, debug);
			} else {
				int left = (int) (0.3 * width);
				int right = (int) (width - 0.3 * width);
				int scanHeight = (int) (height * getDouble("Scan Bar Height (percent)", 25) / 100d);
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.01); // - scanHeight / 2
				
				boolean useOnlyLeftSide = getBoolean("Scan Only The Left Top and Bottom Regions", false);
				if (useOnlyLeftSide) {
					scanWidth /= 4;
					left = 0;
				}
				
				// values = io.getRGBAverage(left, height / 2 - scanHeight / 2, scanWidth, scanHeight, 150, 50, true);
				valuesTop = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, startHTop, scanWidth, scanHeight, percent, debug);
				valuesBottom = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, height - (startHTop + scanHeight), scanWidth, scanHeight, percent,
						debug);
				
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
		} else {
			float[] valuesTop, valuesBottom;
			if (bpleft != null && bpright != null) {
				int left = (int) (bpleft.getValue() * width);
				int right = (int) (bpright.getValue() * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.1 - scanHeight / 2);
				
				valuesTop = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, startHTop, scanWidth, scanHeight, percent, debug);
				valuesBottom = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, height - startHTop, scanWidth, scanHeight, percent, debug);
			} else {
				int left = (int) ((0.3 - 0.25) * width);
				int right = (int) (left + 0.25 * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.05 - scanHeight / 2);
				
				valuesTop = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, startHTop, scanWidth, scanHeight, percent, debug);
				valuesBottom = io.getRGBAverageMostSimilarToColor(targetBackgroundColor, left, height - (startHTop + scanHeight), scanWidth, scanHeight, percent,
						debug);
			}
			values = new float[6];
			int i = 0;
			values[i] = valuesTop[i++];
			values[i] = valuesTop[i++];
			values[i] = valuesTop[i++];
			values[i++] = valuesBottom[0];
			values[i++] = valuesBottom[1];
			values[i++] = valuesBottom[2];
		}
		
		if (verticalGradientSideView) {
			double r = values[0];
			double g = values[1];
			double b = values[2];
			
			double r2 = values[3];
			double g2 = values[4];
			double b2 = values[5];
			
			return new double[] { r * 255, g * 255, b * 255, r2 * 255, g2 * 255, b2 * 255 };
		} else {
			double r = values[0];
			double g = values[1];
			double b = values[2];
			
			return new double[] { r * 255, g * 255, b * 255 };
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
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
		return "Color Balancing Vis";
	}
	
	@Override
	public String getDescription() {
		return "Recolor pictures according to white point (or other target color).";
	}
}
