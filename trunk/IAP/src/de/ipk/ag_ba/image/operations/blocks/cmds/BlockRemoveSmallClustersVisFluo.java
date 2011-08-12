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
public class BlockRemoveSmallClustersVisFluo extends AbstractSnapshotAnalysisBlockFIS {
	public static boolean ngUse = true;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			if (options.isMaize()) {
				// not for barley
				FlexibleImage res =
						new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(ngUse,
								options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS) / 2d,
								options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
				return res;
			} else {
				FlexibleImage res =
						new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(ngUse,
								options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS) / 2d, (getInput().getMasks().getVis().getWidth() / 100) * 1,
								options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
				return res;
			}
		} else {
			if (options.isMaize()) {
				FlexibleImage res =
						new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(ngUse,
								options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_VIS),
								options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
				return res;
			} else {
				return getInput().getMasks().getVis();
			}
		}
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
			int cut2 = (int) ((getInput().getMasks().getFluo().getWidth() / 100) * 0.5);
			double cut = options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO);
			return new ImageOperation(getInput().getMasks().getFluo()).removeSmallClusters(ngUse,
					cut, cut2,
					options.getNeighbourhood(), options.getCameraPosition(), null).getImage();
		}
	}
}
