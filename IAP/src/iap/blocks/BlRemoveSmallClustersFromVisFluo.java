package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

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
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isMaize()) {
				// not for barley
				res = new ImageOperation(mask).removeSmallClusters(ngUse,
						// getDouble("Noise-Size-Vis-Area-Percent", (0.001d) / 4d / 2d),
						getInt("Noise-Size-Vis-Area", 2),
						getInt("Noise-Size-Vis-Dimension-Absolute", (mask.getWidth() / 100) * 2),
						options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", false)).getImage();
			} else {
				if (options.isArabidopsis()) {
					// Double cs = getDouble("REMOVE_SMALL_CLUSTER_SIZE_VIS", options.getCameraPosition() == CameraPosition.SIDE ? 30 : 25);
					res = new ImageOperation(mask).removeSmallClusters(ngUse,
							getInt("Noise-Size-Vis-Area", 25),
							getInt("Noise-Size-Vis-Dimension-Absolute", 25),
							// cs,
							// cs.intValue(),
							options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", false)).getImage();
				} else
					res = new ImageOperation(mask).removeSmallClusters(ngUse,
							// getDouble("Noise-Size-Vis-Area-Percent", (0.001d) / 2000d),
							getInt("Noise-Size-Vis-Area", 2),
							getInt("Noise-Size-Vis-Dimension-Absolute", (mask.getWidth() / 300) * 2),
							options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", false)).getImage();
			}
			if (res != null)
				res.show("vis result", debug);
			return res;
		} else {
			if (options.isMaize()) {
				res = new ImageOperation(mask).removeSmallClusters(ngUse,
						// getDouble("Noise-Size-Vis-Area-Percent", (0.001d) / 4d),
						getInt("Noise-Size-Vis-Area", 2),
						getInt("Noise-Size-Vis-Dimension-Absolute", (mask.getWidth() / 100) * 1),
						options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", false)).getImage();
				
			} else
				if (options.isArabidopsis()) {
					res = new ImageOperation(mask).removeSmallClusters(ngUse,
							getInt("Noise-Size-Vis-Area", 30),
							getInt("Noise-Size-Vis-Dimension-Absolute", 30),
							// cs,
							// cs.intValue(),
							options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", false)).getImage();
				} else {
					res = new ImageOperation(mask).removeSmallClusters(ngUse,
							// getDouble("Noise-Size-Vis-Area-Percent", (0.001d) / 4d),
							getInt("Noise-Size-Vis-Area", 2),
							getInt("Noise-Size-Vis-Dimension-Absolute", (mask.getWidth() / 100) / 2),
							options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Vis Area Parameter", false)).getImage();
				}
			if (res != null)
				res.show("vis result", debug);
			return res;
		}
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			
			if (options.isMaize()) {
				return new ImageOperation(input().masks().fluo()).
						dilate(getInt("dilation fluo", 1)).
						removeSmallClusters(ngUse,
								// getDouble("Noise-Size-Fluo-Area-Percent", (0.001d) / 20 / 2d),
								getInt("Noise-Size-Fluo-Area", 20),
								getInt("Noise-Size-Fluo-Dimension-Absolute", (input().masks().fluo().getWidth() / 100) * 1),
								options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Fluo Area Parameter", false)).getImage();
			} else {
				if (options.isArabidopsis()) {
					return new ImageOperation(input().masks().fluo()).
							dilate(getInt("dilation fluo", 0)).
							removeSmallClusters(ngUse,
									// getDouble("Noise-Size-Fluo-Area-Percent", (0.001d) / 20 / 2d),
									getInt("Noise-Size-Fluo-Area", 20),
									getInt("Noise-Size-Fluo-Dimension-Absolute", 20),
									options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Fluo Area Parameter", false)).getImage();
				} else
					
					return new ImageOperation(input().masks().fluo()).copy().
							dilate(getInt("dilation fluo", 3)).
							removeSmallClusters(ngUse,
									// getDouble("Noise-Size-Fluo-Area-Percent", (0.001d) / 20 / 2d),
									getInt("Noise-Size-Fluo-Area", 20),
									getInt("Noise-Size-Fluo-Dimension-Absolute", (int) ((input().masks().fluo().getWidth() / 300) * 3d)),
									options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Fluo Area Parameter", false)).getImage();
			}
		} else {
			return new ImageOperation(input().masks().fluo().show("input fluo", debug)).
					dilate(getInt("dilation fluo", 0)).
					removeSmallClusters(ngUse,
							// getDouble("Noise-Size-Fluo-Area-Percent", (0.001d) / 20 / 2d),
							getInt("Noise-Size-Fluo-Area", 3),
							getInt("Noise-Size-Fluo-Dimension-Absolute", (int) ((input().masks().fluo().getWidth() / 100) * 0.5)),
							options.getNeighbourhood(), options.getCameraPosition(), null, getBoolean("Use Fluo Area Parameter", false)).show("result fluo", debug)
					.getImage();
		}
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
