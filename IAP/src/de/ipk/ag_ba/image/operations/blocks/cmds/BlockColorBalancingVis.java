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
public class BlockColorBalancingVis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			pix = getProbablyWhitePixels(vis, 0.3);
		} else
			pix = getProbablyWhitePixels(vis, 0.06);
		return io.imageBalancing(255, pix).getImage();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			pix = getProbablyWhitePixels(vis, 0.3);
		else
			pix = getProbablyWhitePixels(vis, 0.06);
		return io.imageBalancing(255, pix).getImage();
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape
	 */
	private double[] getProbablyWhitePixels(FlexibleImage image, double size) {
		int width = image.getWidth();
		int height = image.getHeight();
		int w = (int) (width * size);
		int h = (int) (height * size);
		
		ImageOperation io = new ImageOperation(image);
		
		BlockProperty bpleft = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_LEFT_X);
		BlockProperty bpright = getProperties().getNumericProperty(0, 1, PropertyNames.RESULT_VIS_MARKER_POS_1_RIGHT_X);
		
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
		// float[] valuesleft = io.getRGBAverage(20, h, w, height - 2 * h, 150, 50, true);
		// float[] valuesright = io.getRGBAverage(width - 20 - w, h, w, height - 2 * h, 150, 50, true);
		// no function tested
		// double[] valuestop = io.getRGBAverage(img2d, 2 * w, 2 * h, width - 2 * w, h, 150, 50, true);
		// double[] valuesdown = io.getRGBAverage(img2d, 2 * w, height - 2 * h, width - 2 * w, h, 150, 50, true);
		
		// double r = (valuesleft[0] + valuesright[0]) / 2; // + valuestop[0] + valuesdown[0]) / 4;
		// double g = (valuesleft[1] + valuesright[1]) / 2;// + valuestop[1] + valuesdown[1]) / 4;
		// double b = (valuesleft[2] + valuesright[2]) / 2;// + valuestop[2] + valuesdown[2]) / 4;
		
		double r = values[0];
		double g = values[1];
		double b = values[2];
		
		return new double[] { r * 255, g * 255, b * 255 };
	}
}
