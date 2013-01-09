package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Recolor pictures according to white point (or black point for fluo).
 * 
 * @author pape, klukas
 */
public class BlockColorBalancing extends AbstractSnapshotAnalysisBlockFIS {
	
	static boolean debug = false;
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage vis = input().images().vis();
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
		FlexibleImage vis = input().masks().vis();
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
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage fluo = input().images().fluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		double[] pix = getProbablyWhitePixels(io.invert().getImage(), 0.08);
		return io.imageBalancing(255, pix).invert().getImage();
		
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage fluo = input().masks().fluo();
		if (fluo == null)
			return null;
		ImageOperation io = new ImageOperation(fluo);
		double[] pix = getProbablyWhitePixels(io.invert().getImage(), 0.08);
		return io.imageBalancing(255, pix).invert().getImage();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (input().images().nir() != null) {
				double side = 0.3; // value for white balancing (side width)
				FlexibleImage nir = input().images().nir();
				// White Balancing
				double[] pix = BlockColorBalancing.getProbablyWhitePixels(nir.crop(), side);// 0.08);
				return new ImageOperation(nir).imageBalancing(255, pix).getImage();
			}
		}
		return input().images().nir();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (input().masks().nir() != null) {
				double side = 0.3; // value for white balancing (side width)
				FlexibleImage nir = input().masks().nir();
				// White Balancing
				double[] pix = BlockColorBalancing.getProbablyWhitePixels(nir.crop(), side);// 0.08);
				return new ImageOperation(nir).imageBalancing(255, pix).getImage();
			}
		}
		return input().masks().nir();
	}
	
	/**
	 * Calculates the average of the brightness of an area around an image.
	 * 
	 * @author pape
	 */
	public static double[] getProbablyWhitePixels(FlexibleImage image, double size) {
		int width = image.getWidth();
		int height = image.getHeight();
		int w = (int) (width * size);
		int h = (int) (height * size);
		
		ImageOperation io = new ImageOperation(image);
		
		float[] valuesleft = io.getRGBAverage(20, h, w, height - 2 * h, 150, 50, true, debug);
		float[] valuesright = io.getRGBAverage(width - 20 - w, h, w, height - 2 * h, 150, 50, true, debug);
		// no function tested
		// double[] valuestop = io.getRGBAverage(img2d, 2 * w, 2 * h, width - 2 * w, h, 150, 50, true);
		// double[] valuesdown = io.getRGBAverage(img2d, 2 * w, height - 2 * h, width - 2 * w, h, 150, 50, true);
		
		double r = (valuesleft[0] + valuesright[0]) / 2; // + valuestop[0] + valuesdown[0]) / 4;
		double g = (valuesleft[1] + valuesright[1]) / 2;// + valuestop[1] + valuesdown[1]) / 4;
		double b = (valuesleft[2] + valuesright[2]) / 2;// + valuestop[2] + valuesdown[2]) / 4;
		
		return new double[] { r * 255, g * 255, b * 255 };
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}
