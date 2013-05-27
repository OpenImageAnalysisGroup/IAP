package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

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
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		Image res, mask = input().masks().vis().show("vis input", debug);
		
		res = new ImageOperation(mask).copy().dilate(getInt("dilation vis", 0)).removeSmallClusters(ngUse,
				getInt("Noise-Size-Vis-Area", 20 * 20),
				getInt("Noise-Size-Vis-Dimension-Absolute", 20),
				options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", true)).getImage();
		if (res != null)
			res.show("vis result", debug);
		return res;
	}
	
	@Override
	protected Image processFLUOmask() {
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
	
}
