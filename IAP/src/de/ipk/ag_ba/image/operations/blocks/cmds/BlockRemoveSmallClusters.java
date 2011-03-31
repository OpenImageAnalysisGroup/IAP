package de.ipk.ag_ba.image.operations.blocks.cmds;

import org.ObjectRef;

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
	
	@Override
	protected FlexibleImage processVISmask() {
		
		ObjectRef clusterSizeRef = new ObjectRef();
		FlexibleImage res =
				new ImageOperation(getInput().getMasks().getVis()).removeSmallClusters(options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_RGB),
						options.getNeighbourhood(), options.getCameraTyp(), clusterSizeRef).getImage();
		
		int[] clusterSizes = (int[]) clusterSizeRef.getObject();
		
		int filledPixels = 0;
		
		if (clusterSizes.length > 1)
			for (int i = 1; i < clusterSizes.length; i++)
				filledPixels += clusterSizes[i];
		
		return res;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return new ImageOperation(getInput().getMasks().getFluo()).removeSmallClusters(options.getDoubleSetting(Setting.REMOVE_SMALL_CLUSTER_SIZE_FLUO),
				options.getNeighbourhood(), options.getCameraTyp(), null).getImage();
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// return new ImageOperation(getInput().getMasks().getNir()).removeSmallClusters(0.002d).getImage();
	// }
	
}
