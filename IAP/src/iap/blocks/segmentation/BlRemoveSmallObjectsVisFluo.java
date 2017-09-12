package iap.blocks.segmentation;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

/**
 * @author klukas
 *         Small parts
 *         of the image are removed (noise), using the PixelSegmentation algorithm.
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlRemoveSmallObjectsVisFluo extends AbstractSnapshotAnalysisBlock {
	public static boolean ngUse = true;
	
	private boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
		
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		Image res, mask = input().masks().vis().show("vis input", debugValues);
		
		res = new ImageOperation(mask).blurImageJ(getInt("dilation vis", 0)).applyMask(mask.copy().io().bm().dilate(BlMorphologicalOperations.getRoundMask(getInt("dilation vis", 0))).getImage())
				.removeSmallClusters(
						getInt("Noise-Size-Vis-Area", 20 * 20),
						getInt("Noise-Size-Vis-Dimension-Absolute", 20),
						optionsAndResults.getCameraPosition() == CameraPosition.TOP ? getDouble("Increase Factor Largest Bounding Box", 1.05) : -1,
						optionsAndResults.getNeighbourhood(), optionsAndResults.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", true),
						getBoolean("Consider color (VIS)", false),
						getDouble("Brightness scale value (VIS)", 1d),
						getColor("Target object color", Color.YELLOW))
				.getImage();
		res = input().images().vis().copy().io().applyMask(res.io().bm().erode(getInt("dilation vis", 0)).getImage()).getImage();
		res.show("vis result", debugValues);
		
		return res;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		Image res = new ImageOperation(input().masks().fluo().show("input fluo", debugValues)).copy().bm().dilate(BlMorphologicalOperations.getRoundMask(getInt("dilation fluo", 0))).io().removeSmallClusters(
				getInt("Noise-Size-Fluo-Area", 10 * 10),
				getInt("Noise-Size-Fluo-Dimension-Absolute", 10),
				optionsAndResults.getCameraPosition() == CameraPosition.TOP ? getDouble("Increase Factor Largest Bounding Box", 1.05) : -1,
				optionsAndResults.getNeighbourhood(), optionsAndResults.getCameraPosition(), null,
				getBoolean("Use Fluo Area Parameter", false),
				getBoolean("Consider color (FLUO)", false),
				getDouble("Brightness scale value (VIS)", 1d),
				getColor("Target object color", Color.YELLOW)).show("result fluo", debugValues)
				.getImage();
		
		res = input().images().fluo().copy().io().applyMask(res.io().bm().erode(getInt("dilation fluo", 0)).getImage())
				.getImage();
		
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
		return "Remove small noise objects";
	}
	
	@Override
	public String getDescription() {
		return "Small parts of the image are removed (noise), using the PixelSegmentation algorithm.";
	}
	
}
