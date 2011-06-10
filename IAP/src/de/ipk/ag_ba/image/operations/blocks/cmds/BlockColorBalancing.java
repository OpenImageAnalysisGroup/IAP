package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockColorBalancing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		double[] pix = getProbablyWhitePixels(vis, 0.08);
		return ImageBalancing(vis, 255, pix);
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage fluo = getInput().getImages().getFluo();
		FlexibleImage temp = new ImageOperation(fluo).invert().getImage();
		double[] pix = getProbablyWhitePixels(temp, 0.08);
		FlexibleImage temp1 = ImageBalancing(temp, 255, pix);
		return new ImageOperation(temp1).invert().getImage();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage vis = getInput().getMasks().getVis();
		double[] pix = getProbablyWhitePixels(vis, 0.08);
		return ImageBalancing(vis, 255, pix);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = getInput().getMasks().getFluo();
		FlexibleImage temp = new ImageOperation(fluo).invert().getImage();
		double[] pix = getProbablyWhitePixels(temp, 0.08);
		FlexibleImage temp1 = ImageBalancing(temp, 255, pix);
		return new ImageOperation(temp1).invert().getImage();
	}
	
	public FlexibleImage ImageBalancing(FlexibleImage input, int brightness, double[] pix) {
		double[] factors = calculateFactors(pix, brightness);
		ImageOperation io = new ImageOperation(input);
		return io.multiplicateImageChannelsWithFactors(factors).getImage();
	}
	
	public double[] calculateFactors(double[] pix, int brightness) {
		double r = brightness / (double) pix[0];
		double g = brightness / (double) pix[1];
		double b = brightness / (double) pix[2];
		return new double[] { r, g, b };
	}
	
	public double[] getProbablyWhitePixels(FlexibleImage input, double size) {
		int[][] img2d = input.getAs2A();
		int width = input.getWidth();
		int height = input.getHeight();
		int w = (int) (width * size);
		int h = (int) (height * size);
		
		ImageOperation io = new ImageOperation(input);
		
		double[] valuesleft = io.getRGBAverage(img2d, 2 * w, 2 * h, w, height - 2 * h, 150, 50, true);
		double[] valuesright = io.getRGBAverage(img2d, width - 2 * w, 2 * h, w, height - 2 * h, 150, 50, true);
		double[] valuestop = io.getRGBAverage(img2d, 2 * w, 2 * h, width - 2 * w, h, 150, 50, true);
		double[] valuesdown = io.getRGBAverage(img2d, 2 * w, height - 2 * h, width - 2 * w, h, 150, 50, true);
		
		double r = (valuesleft[0] + valuesright[0] + valuestop[0] + valuesdown[0]) / 4;
		double g = (valuesleft[1] + valuesright[1] + valuestop[1] + valuesdown[1]) / 4;
		double b = (valuesleft[2] + valuesright[2] + valuestop[2] + valuesdown[2]) / 4;
		
		return new double[] { r, g, b };
	}
}
