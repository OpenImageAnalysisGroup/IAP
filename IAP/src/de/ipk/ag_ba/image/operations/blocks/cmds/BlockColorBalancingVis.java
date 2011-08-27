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
public class BlockColorBalancingVis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			pix = getProbablyWhitePixels(vis, 0.3, true);
		} else
			pix = getProbablyWhitePixels(vis, 0.06, false);
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
			pix = getProbablyWhitePixels(vis, 0.3, false);
		else
			pix = getProbablyWhitePixels(vis, 0.06, false);
		return io.imageBalancing(255, pix).getImage();
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape
	 */
	private double[] getProbablyWhitePixels(FlexibleImage image, double size, boolean verticalGradient) {
		int width = image.getWidth();
		int height = image.getHeight();
		boolean debug = true;
		
		ImageOperation io = new ImageOperation(image);
		
		BlockProperty bpleft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty bpright = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
		float[] values;
		if (!verticalGradient) {
			float[] valuesTop, valuesBottom;
			if (bpleft != null && bpright != null) {
				int left = (int) (bpleft.getValue() * width);
				int right = (int) (bpright.getValue() * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				
				values = io.getRGBAverage(left, height / 2 - scanHeight / 2, scanWidth, scanHeight, 150, 50, true);
			} else {
				int left = (int) (0.3 * width);
				int right = (int) (width - 0.3 * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.1 - scanHeight / 2);
				
				// values = io.getRGBAverage(left, height / 2 - scanHeight / 2, scanWidth, scanHeight, 150, 50, true);
				valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, 150, 50, true);
				valuesBottom = io.getRGBAverage(left, height - (startHTop + scanHeight), scanWidth, scanHeight, 150, 50, true);
				
				if (debug) {
					image.copy().getIO().getCanvas().fillRect(left, startHTop, scanWidth, scanHeight, Color.RED.getRGB(), 0.5)
							.fillRect(left, height - (startHTop + scanHeight), scanWidth, scanHeight, Color.RED.getRGB(), 0.5)
							.drawLine(100, 100, 1000, 1000, Color.RED.getRGB(), 0.5, 10).getImage()
							.print("region scan for color balance", false);
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
		} else {
			float[] valuesTop, valuesBottom;
			if (bpleft != null && bpright != null) {
				int left = (int) (bpleft.getValue() * width);
				int right = (int) (bpright.getValue() * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.1 - scanHeight / 2);
				
				valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, 150, 50, true);
				valuesBottom = io.getRGBAverage(left, height - startHTop, scanWidth, scanHeight, 150, 50, true);
			} else {
				int left = (int) (0.3 * width);
				int right = (int) (width - 0.3 * width);
				int scanHeight = (right - left) / 4;
				int scanWidth = right - left;
				int startHTop = (int) (height * 0.1 - scanHeight / 2);
				
				valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, 150, 50, true);
				valuesBottom = io.getRGBAverage(left, height - (startHTop + scanHeight), scanWidth, scanHeight, 150, 50, true);
				
				if (debug) {
					image.copy().getIO().getCanvas().fillRect(left, startHTop, scanWidth, scanHeight, Color.RED.getRGB(), 0.5)
							.fillRect(left, height - (startHTop + scanHeight), scanWidth, scanHeight, Color.RED.getRGB(), 0.5).getImage()
							.print("region scan for color balance", false);
				}
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
		
		if (verticalGradient) {
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
