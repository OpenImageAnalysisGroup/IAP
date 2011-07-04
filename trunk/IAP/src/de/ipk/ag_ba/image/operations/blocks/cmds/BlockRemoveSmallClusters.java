package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
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
public class BlockRemoveSmallClusters extends AbstractSnapshotAnalysisBlockFIS {
	public static boolean ngUse = true;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage res =
					new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(ngUse,
							options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_RGB) / 2d,
							options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			return res;
		} else {
			FlexibleImage res =
					new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(ngUse, options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_RGB),
							options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
			return res;
		}
		// return getInput().getMasks().getVis();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			return new ImageOperation(getInput().getMasks().getFluo()).removeSmallClusters(ngUse,
					options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO) / 2d,
					options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
		} else {
			return new ImageOperation(getInput().getMasks().getFluo()).removeSmallClusters(ngUse,
					options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO),
					options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
		}
		
		// return getInput().getMasks().getFluo();
	}
}
