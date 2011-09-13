package de.ipk.ag_ba.image.analysis.maize;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockColorBalancing_nir_second_run extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage nir = getInput().getImages().getNir();
		if (nir == null)
			return null;
		double[] pix = getProbablyWhitePixels(nir);
		return nir.getIO().imageBalancing(180, pix).getImage();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nir = getInput().getMasks().getNir();
		if (nir == null)
			return null;
		double[] pix = getProbablyWhitePixels(nir);
		return nir.getIO().imageBalancing(180, pix).getImage();
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image (bottom and top for linear interpolation).
	 * 
	 * @author pape
	 */
	private double[] getProbablyWhitePixels(FlexibleImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		boolean debug = false;
		double[] pix;
		
		ImageOperation io = new ImageOperation(image);
		
		float[] values;
		float[] valuesTop, valuesBottom = { 0f, 0f, 0f }, valuesBottomLeft, valuesBottomRight;
		
		int left = (int) (0.3 * width);
		int right = (int) (width - 0.3 * width);
		int scanHeight = (right - left) / 4;
		int scanWidth = right - left;
		int startHTop = (int) (height * 0.1 - scanHeight / 2);
		
		valuesTop = io.getRGBAverage(left, startHTop, scanWidth, scanHeight, 150, 50, true);
		valuesBottomLeft = io.getRGBAverage(left / 3, height - (startHTop + scanHeight), scanWidth / 2, scanHeight, 150, 50, true);
		valuesBottomRight = io
					.getRGBAverage(width - (scanWidth / 3) - (left / 3), height - (startHTop + scanHeight), scanWidth / 3, scanHeight, 150, 50, true);
		
		int i = 0;
		valuesBottom[i] = valuesBottomLeft[i];
		valuesBottom[++i] = valuesBottomLeft[i];
		valuesBottom[++i] = valuesBottomLeft[i];
		valuesBottom[i = 0] = (valuesBottom[i] + valuesBottomRight[i]) / 2;
		valuesBottom[++i] = (valuesBottom[i] + valuesBottomRight[i]) / 2;
		valuesBottom[++i] = (valuesBottom[i] + valuesBottomRight[i]) / 2;
		
		if (debug) {
			image.copy().getIO().getCanvas().fillRect(left, startHTop, scanWidth, scanHeight, Color.RED.getRGB(), 0.5)
							.fillRect(left / 3, height - (startHTop + scanHeight), scanWidth / 3, scanHeight, Color.RED.getRGB(), 0.5)
							.fillRect(width - (scanWidth / 3) - (left / 3), height - (startHTop + scanHeight), scanWidth / 3, scanHeight, Color.RED.getRGB(), 0.5)
							.getImage()
							.print("region scan for color balance", true);
		}
		
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
}
