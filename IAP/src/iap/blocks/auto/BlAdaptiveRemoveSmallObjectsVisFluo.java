package iap.blocks.auto;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlAdaptiveRemoveSmallObjectsVisFluo extends AbstractSnapshotAnalysisBlock {
	private boolean autoTune;
	
	private double averageLeafWidthEstimationFluo;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.autoTune = getBoolean("Auto-tune", true);
		
		if (!autoTune)
			averageLeafWidthEstimationFluo = Double.NaN;
		else
			if (input().masks().fluo() != null) {
				ImageOperation nn = null;
				if (autoTune)
					nn = input().masks().fluo().copy().io().bm().skeletonize().io();
				this.averageLeafWidthEstimationFluo = !autoTune ? Double.NaN
						: input().masks().fluo().io().countFilledPixels() /
								(double) nn.countFilledPixels();
			} else
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
						input().masks().fluo().getWidth() * input().masks().vis().getWidth() - 7;
			else
				averageLeafWidthEstimationVIS = input().masks().vis().io().countFilledPixels() /
						(double) input().masks().vis().copy().io().skel().skeletonize(ImageOperation.BACKGROUND_COLORint).countFilledPixels() - 7;
			
		int dil = 0;
		boolean unitTune = false;
		if (unitTune)
			if (optionsAndResults.getUnitTestSteps() > 0) {
				dil = (int) ((optionsAndResults.getUnitTestIdx() / 3));
			}
		
		res = new ImageOperation(mask).copy().bm().dilate(getInt("dilation vis", 0) + dil).io().removeSmallClusters(
				autoTune ? (int) (averageLeafWidthEstimationVIS * averageLeafWidthEstimationVIS) : getInt("Noise-Size-Vis-Area", 20 * 20),
				autoTune ? (int) (averageLeafWidthEstimationVIS) : getInt("Noise-Size-Vis-Dimension-Absolute", 20), -1,
				optionsAndResults.getNeighbourhood(), optionsAndResults.getCameraPosition(), null,
				autoTune ? true : getBoolean("Use Vis Area Parameter", true), false, 1d).getImage();
		if (res != null) {
			if (getInt("dilation vis", 0) > 0)
				res = res.io().bm().erode(getInt("dilation vis", 0)).getImage();
			
			res = input().masks().vis().io().applyMask(res).getImage();
			
			res.show("vis result", debugValues);
		}
		return res;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		Image res = new ImageOperation(input().masks().fluo().show("input fluo", debugValues)).copy().bm().dilate(getInt("dilation fluo", 0)).io().removeSmallClusters(
				autoTune ? (int) (averageLeafWidthEstimationFluo * averageLeafWidthEstimationFluo) : getInt("Noise-Size-Fluo-Area", 10 * 10),
				autoTune ? (int) averageLeafWidthEstimationFluo : getInt("Noise-Size-Fluo-Dimension-Absolute", 10), -1,
				optionsAndResults.getNeighbourhood(), optionsAndResults.getCameraPosition(), null,
				autoTune ? true : getBoolean("Use Fluo Area Parameter", true), false, 1d).show("result fluo", debugValues)
				.getImage();
		int dil = 0;
		boolean unitTune = false;
		if (unitTune)
			if (optionsAndResults.getUnitTestSteps() > 0) {
				dil = ((int) ((optionsAndResults.getUnitTestIdx()) % 3));
			}
		if (getInt("dilation fluo", 0) > 0)
			res = res.io().bm().erode(getInt("dilation fluo", 2) + dil).getImage();
		
		res = input().masks().fluo().io().applyMask(res).getImage();
		
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
