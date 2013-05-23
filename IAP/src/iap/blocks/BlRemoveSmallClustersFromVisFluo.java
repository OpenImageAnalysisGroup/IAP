package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlRemoveSmallClustersFromVisFluo extends AbstractSnapshotAnalysisBlockFIS {
	public static boolean ngUse = true;
	
	private boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		FlexibleImage res, mask = input().masks().vis().show("vis input", debug);
		
		res = new ImageOperation(mask).copy().dilate(getInt("dilation vis", 0)).removeSmallClusters(ngUse,
				getInt("Noise-Size-Vis-Area", 20 * 20),
				getInt("Noise-Size-Vis-Dimension-Absolute", 20),
				options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", true)).getImage();
		if (res != null)
			res.show("vis result", debug);
		return res;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		return new ImageOperation(input().masks().fluo().show("input fluo", debug)).copy().
				dilate(getInt("dilation fluo", 0)).
				removeSmallClusters(ngUse,
						getInt("Noise-Size-Fluo-Area", 10 * 10),
						getInt("Noise-Size-Fluo-Dimension-Absolute", 10),
						options.getNeighbourhood(), options.getCameraPosition(), null,
						getBoolean("Use Fluo Area Parameter", false)).show("result fluo", debug)
				.getImage();
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}
