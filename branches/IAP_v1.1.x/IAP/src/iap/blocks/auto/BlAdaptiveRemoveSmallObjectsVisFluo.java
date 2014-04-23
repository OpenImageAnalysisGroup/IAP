package iap.blocks.auto;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlAdaptiveRemoveSmallObjectsVisFluo extends AbstractSnapshotAnalysisBlock {
	public static boolean ngUse = true;
	
	private boolean debug = false;
	
	private boolean autoTune;
	
	private double averageLeafWidthEstimationFluo;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.autoTune = getBoolean("Auto-tune", true);
		debug = getBoolean("debug", false);
		
		if (!autoTune)
			averageLeafWidthEstimationFluo = Double.NaN;
		else
			if (input().masks().fluo() != null)
				this.averageLeafWidthEstimationFluo = !autoTune ?
						Double.NaN :
						input().masks().fluo().io().countFilledPixels() /
								(double) input().masks().fluo().copy().io().skel().skeletonize(ImageOperation.BACKGROUND_COLORint).countFilledPixels();
			else
				this.averageLeafWidthEstimationFluo = Double.NaN;
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		Image res, mask = input().masks().vis().show("vis input", debugValues);
		
		double averageLeafWidthEstimationVIS = Double.NaN;
		if (autoTune)
			if (!Double.isNaN(averageLeafWidthEstimationFluo))
				averageLeafWidthEstimationVIS = averageLeafWidthEstimationFluo /
						input().masks().fluo().getWidth() * input().masks().vis().getWidth();
			else
				averageLeafWidthEstimationVIS = input().masks().vis().io().countFilledPixels() /
						(double) input().masks().vis().copy().io().skel().skeletonize(ImageOperation.BACKGROUND_COLORint).countFilledPixels();
		
		res = new ImageOperation(mask).copy().dilate(getInt("dilation vis", 0)).removeSmallClusters(ngUse,
				autoTune ? (int) (averageLeafWidthEstimationVIS * averageLeafWidthEstimationVIS) : getInt("Noise-Size-Vis-Area", 20 * 20),
				autoTune ? (int) (averageLeafWidthEstimationVIS) : getInt("Noise-Size-Vis-Dimension-Absolute", 20), -1,
				options.getNeighbourhood(), options.getCameraPosition(), null,
				autoTune ? true : getBoolean("Use Vis Area Parameter", true)).getImage();
		if (res != null) {
			if (getInt("dilation vis", 0) > 0)
				res = input().images().vis().io().applyMask(res.io().erode(getInt("dilation vis", 0)).getImage()).getImage();
			res.show("vis result", debugValues);
		}
		return res;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		Image res = new ImageOperation(input().masks().fluo().show("input fluo", debugValues)).copy().
				removeSmallClusters(ngUse,
						autoTune ? (int) (averageLeafWidthEstimationFluo * averageLeafWidthEstimationFluo) : getInt("Noise-Size-Fluo-Area", 10 * 10),
						autoTune ? (int) averageLeafWidthEstimationFluo : getInt("Noise-Size-Fluo-Dimension-Absolute", 10), -1,
						options.getNeighbourhood(), options.getCameraPosition(), null,
						autoTune ? true : getBoolean("Use Fluo Area Parameter", true)).show("result fluo", debugValues)
				.getImage();
		if (getInt("dilation fluo", 0) > 0)
			res = input().images().fluo().io().applyMask(res.io().erode(getInt("dilation fluo", 0)).getImage()).getImage();
		
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Auto-tuning small noise removal";
	}
	
	@Override
	public String getDescription() {
		return "Small parts of the image are removed (noise), using the region-growing object detection and size evaluation. " +
				"Particles smaller than the estimated leaf width are removed.";
	}
	
}
