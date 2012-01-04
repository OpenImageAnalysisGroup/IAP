package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
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
public class BlColorBalancing_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISimage() {
		if (getInput() == null || getInput().getImages() == null)
			return null;
		FlexibleImage vis = getInput().getImages().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			pix = getProbablyWhitePixels(vis.copy().getIO().blur(5).getImage(), true, -10, 50);
		} else
			pix = getProbablyWhitePixels(vis, false, -10, 10);
		return io.imageBalancing(255, pix).getImage().print("after", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			pix = getProbablyWhitePixels(vis.copy().getIO().blur(5).getImage(), true, -30, 50);
		else
			pix = getProbablyWhitePixels(vis, false, -10, 10);
		return io.imageBalancing(255, pix).getImage().print("after", false);
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
}
