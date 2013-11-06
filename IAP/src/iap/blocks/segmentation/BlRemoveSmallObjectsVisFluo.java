package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

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
		Image res, mask = input().masks().vis().show("vis input", debug);
		
		res = new ImageOperation(mask).copy().dilate(getInt("dilation vis", 0)).removeSmallClusters(ngUse,
				getInt("Noise-Size-Vis-Area", 20 * 20),
				getInt("Noise-Size-Vis-Dimension-Absolute", 20),
				options.getCameraPosition() == CameraPosition.TOP ? getDouble("Increase Factor Largest Bounding Box", 1.05) : -1,
				options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", true)).getImage();
		if (res != null) {
			if (getInt("dilation vis", 0) > 0)
				res = input().images().vis().io().applyMask(res.io().erode(getInt("dilation vis", 0)).getImage()).getImage();
			res.show("vis result", debug);
		}
		return res;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		Image res = new ImageOperation(input().masks().fluo().show("input fluo", debug)).copy().
				dilate(getInt("dilation fluo", 0)).
				removeSmallClusters(ngUse,
						getInt("Noise-Size-Fluo-Area", 10 * 10),
						getInt("Noise-Size-Fluo-Dimension-Absolute", 10),
						options.getCameraPosition() == CameraPosition.TOP ? getDouble("Increase Factor Largest Bounding Box", 1.05) : -1,
						options.getNeighbourhood(), options.getCameraPosition(), null,
						getBoolean("Use Fluo Area Parameter", false)).show("result fluo", debug)
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
		return "Remove small noise objects";
	}
	
	@Override
	public String getDescription() {
		return "Small parts of the image are removed (noise), using the PixelSegmentation algorithm.";
	}
	
}
