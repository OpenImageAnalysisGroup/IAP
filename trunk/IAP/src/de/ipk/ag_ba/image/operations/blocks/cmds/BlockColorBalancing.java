package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Recolor pictures according to white point (or black point for fluo).
 * 
 * @author pape
 */
public class BlockColorBalancing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = getInput().getImages().getVis();
		if (vis == null)
			return null;
		ImageOperation io = new ImageOperation(vis);
		double[] pix;
		if (options.getCameraPosition() == CameraPosition.SIDE)
			pix = getProbablyWhitePixels(vis, 0.2);
		else
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
			pix = getProbablyWhitePixels(vis, 0.2);
		else
			pix = getProbablyWhitePixels(vis, 0.06);
		return io.imageBalancing(255, pix).getImage();
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage fluo = getInput().getImages().getFluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		double[] pix = getProbablyWhitePixels(io.invert().getImage(), 0.08);
		return io.imageBalancing(255, pix).invert().getImage();
		
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = getInput().getMasks().getFluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		double[] pix = getProbablyWhitePixels(io.invert().getImage(), 0.08);
		return io.imageBalancing(255, pix).invert().getImage();
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape
	 */
	public static double[] getProbablyWhitePixels(FlexibleImage image, double size) {
		int[][] img2d = image.getAs2A();
		int width = image.getWidth();
		int height = image.getHeight();
		int w = (int) (width * size);
		int h = (int) (height * size);
		
		ImageOperation io = new ImageOperation(image);
		
		double[] valuesleft = io.getRGBAverage(img2d, 2 * w, 2 * h, w, height - 2 * h, 150, 50, true);
		double[] valuesright = io.getRGBAverage(img2d, width - 2 * w, 2 * h, w, height - 2 * h, 150, 50, true);
		// double[] valuestop = io.getRGBAverage(img2d, 2 * w, 2 * h, width - 2 * w, h, 150, 50, true);
		// double[] valuesdown = io.getRGBAverage(img2d, 2 * w, height - 2 * h, width - 2 * w, h, 150, 50, true);
		
		double r = (valuesleft[0] + valuesright[0]) / 2; // + valuestop[0] + valuesdown[0]) / 4;
		double g = (valuesleft[1] + valuesright[1]) / 2;// + valuestop[1] + valuesdown[1]) / 4;
		double b = (valuesleft[2] + valuesright[2]) / 2;// + valuestop[2] + valuesdown[2]) / 4;
		
		return new double[] { r, g, b };
	}
}
