package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import org.SystemAnalysis;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.PixelProcessor;
import de.ipk.ag_ba.image.operations.blocks.properties.BlockProperty;
import de.ipk.ag_ba.image.operations.blocks.properties.PropertyNames;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Recolor pictures according to white point (or black point for fluo).
 * May adjust the top-right-half brightness with the remark setting "top.right.half.brightness".
 * 
 * @author pape, klukas
 */
public class BlColorBalancing extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processVISimage() {
		if (input() == null || input().images() == null)
			return null;
		FlexibleImage vis = input().images().vis();
		if (vis == null)
			return null;
		if (!getBoolean("enabled", options.getCameraPosition() == CameraPosition.SIDE))
			return vis;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			pix = getProbablyWhitePixels(vis.copy().io().blur(getInt("vis-balance-blur", 5)).getImage(), true,
					getInt("vis-balance-l-threshold", -10), getInt("vis-balance-ab-threshold", 50));
		} else {
			String remark = getRemarkSetting("vis.top.split.adjust", "");
			if (remark != null && remark.length() > 0 && !remark.contains("n")) {
				if (remark.contains("auto")) {
					double[] pixLeft, pixRight;
					pixRight = getProbablyWhitePixels(vis.io().mirrorLeftToRight().getImage().show("left part", debug), false,
							getInt("vis-balance-l-threshold", -10), getInt("vis-balance-ab-threshold", 10));
					pixLeft = getProbablyWhitePixels(vis.io().flipHor().mirrorLeftToRight().getImage().show("right part", debug), false,
							getInt("vis-balance-l-threshold", -10), getInt("vis-balance-ab-threshold", 10));
					return io.imageBalancing(255, pixLeft, pixRight).getImage().show("after", false);
				} else {
					try {
						final Float brightup = Float.parseFloat(remark.trim());
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
					} catch (Exception e) {
						System.out.println(SystemAnalysis.getCurrentTime() + ">Could not interpret remark setting 'top-half': '" + remark
								+ "' (needs to be number indicating top-right brightness correction)");
					}
				}
			}
			pix = getProbablyWhitePixels(vis, false, getInt("vis-balance-l-threshold", -10), getInt("vis-top-balance-ab-threshold", 10));
		}
		return io.imageBalancing(255, pix).getImage().show("after", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = input().masks().vis();
		if (vis == null)
			return null;
		if (!getBoolean("enabled", options.getCameraPosition() == CameraPosition.SIDE))
			return vis;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			pix = getProbablyWhitePixels(vis.copy().io().blur(getInt("vis-balance-blur", 5)).getImage(), true,
					getInt("vis-balance-l-threshold", -10), getInt("vis-balance-ab-threshold", 50));
		else
			pix = getProbablyWhitePixels(vis, false, getInt("vis-balance-l-threshold", -10), getInt("vis-balance-ab-threshold", 10));
		return io.imageBalancing(255, pix).getImage().show("after", false);
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image. (bottom and top for linear interpolation, center for default)
	 * 
	 * @author pape
	 */
	private double[] getProbablyWhitePixels(FlexibleImage image, boolean verticalGradientSideView, int lThres, int abThres) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		ImageOperation io = new ImageOperation(image);
		
		BlockProperty bpleft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty bpright = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		float[] values;
		if (!verticalGradientSideView) {
			float[] valuesTop, valuesBottom;
			if (bpleft != null && bpright != null) {
				int left = (int) (bpleft.getValue() * width);
				int right = (int) (bpright.getValue() * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				
				values = io.getRGBAverage(left, height / 2 - scanHeight / 2, scanWidth, scanHeight, 150, 50, true, debug);
			} else {
				int left = (int) (0.3 * width);
				int right = (int) (width - 0.3 * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.01); // - scanHeight / 2
				
				boolean useOnlyRightSide = true;
				if (useOnlyRightSide) {
					scanWidth /= 2;
					left += scanWidth * 2.5;
					scanWidth /= 2;
				}
				
				// values = io.getRGBAverage(left, height / 2 - scanHeight / 2, scanWidth, scanHeight, 150, 50, true);
				valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, lThres, abThres, true, debug);
				valuesBottom = io.getRGBAverage(left, height - (startHTop + scanHeight), scanWidth, scanHeight, lThres, abThres, true, debug);
				
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
				
				valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, lThres, abThres, true, debug);
				valuesBottom = io.getRGBAverage(left, height - startHTop, scanWidth, scanHeight, lThres, abThres, true, debug);
			} else {
				int left = (int) ((0.3 - 0.25) * width);
				int right = (int) (left + 0.25 * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.05 - scanHeight / 2);
				
				valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, lThres, abThres, true, debug);
				valuesBottom = io.getRGBAverage(left, height - (startHTop + scanHeight), scanWidth, scanHeight, lThres, abThres, true, debug);
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
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
