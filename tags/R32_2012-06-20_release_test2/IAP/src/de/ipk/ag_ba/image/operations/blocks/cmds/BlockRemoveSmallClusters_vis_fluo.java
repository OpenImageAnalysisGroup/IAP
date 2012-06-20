package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Small parts
 * of the image are removed (noise), using the PixelSegmentation algorithm.
 * 
 * @param in
 *           The set of input images (RGB images).
 * @return A set of images which may be used as a mask.
 */
public class BlockRemoveSmallClusters_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	public static boolean ngUse = true;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		FlexibleImage res, mask = input().masks().vis();
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isMaize()) {
				// not for barley
				res = new ImageOperation(mask).removeSmallClusters(ngUse,
						options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS) / 2d, (mask.getWidth() / 100) * 2,
						options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			} else {
				if (options.isArabidopsis())
					res = new ImageOperation(mask).removeSmallClusters(ngUse,
							options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS),
							options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS).intValue(),
							options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
				else
					res = new ImageOperation(mask).removeSmallClusters(ngUse,
							options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS) / 2d, (mask.getWidth() / 300) * 1,
							options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			}
			return res;
		} else {
			if (options.isMaize()) {
				res = new ImageOperation(mask).removeSmallClusters(ngUse,
						options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS), (mask.getWidth() / 100) * 1,
						options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
				
			} else {
				res = new ImageOperation(mask).removeSmallClusters(ngUse,
						2, (mask.getWidth() / 100) / 2,
						options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			}
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
						dilate().
						removeSmallClusters(ngUse,
								options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO) / 2d,
								(input().masks().fluo().getWidth() / 100) * 1,
								options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			} else {
				if (options.isArabidopsis()) {
					return new ImageOperation(input().masks().fluo()).
							removeSmallClusters(ngUse,
									options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO),
									options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO).intValue(),
									options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
				} else
					return new ImageOperation(input().masks().fluo()).
							dilate().
							removeSmallClusters(ngUse,
									options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO) / 2d,
									(input().masks().fluo().getWidth() / 300) * 1,
									options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			}
		} else {
			int cut2 = (int) ((input().masks().fluo().getWidth() / 100) * 0.5);
			double cut = 3;
			return new ImageOperation(input().masks().fluo()).removeSmallClusters(ngUse,
					cut, cut2,
					options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
		}
	}
}
