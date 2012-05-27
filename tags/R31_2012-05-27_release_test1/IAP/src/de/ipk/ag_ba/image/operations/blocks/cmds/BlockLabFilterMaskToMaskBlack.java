/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Entzian
 */
@Deprecated
public class BlockLabFilterMaskToMaskBlack extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		boolean maize = true;
		return labFilter(input().masks().vis(), input().images().vis(), 110, 255, 0, 255, 0, 255, options.getCameraPosition(), maize);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		boolean maize = true;
		return labFilter(input().masks().fluo(), input().images().fluo(), 110, 255, 0, 255, 0, 255, options.getCameraPosition(), maize);
	}
	
	private FlexibleImage labFilter(FlexibleImage workMask, FlexibleImage originalImage, int lowerValueOfL, int upperValueOfL, int lowerValueOfA,
			int upperValueOfA, int lowerValueOfB, int upperValueOfB, CameraPosition typ,
			boolean maize) {
		
		int[] image = workMask.getAs1A();
		int[] result = new int[image.length];
		int width = workMask.getWidth();
		int height = workMask.getHeight();
		
		int back = options.getBackground();
		
		ImageOperation.thresholdLAB(width, height, image, result,
				lowerValueOfL, upperValueOfL,
				lowerValueOfA, upperValueOfA,
				lowerValueOfB, upperValueOfB,
				back, typ, maize);
		
		FlexibleImage mask = new FlexibleImage(width, height, result);
		
		return new ImageOperation(originalImage).applyMask_ResizeSourceIfNeeded(mask, options.getBackground()).getImage();
	}
}
