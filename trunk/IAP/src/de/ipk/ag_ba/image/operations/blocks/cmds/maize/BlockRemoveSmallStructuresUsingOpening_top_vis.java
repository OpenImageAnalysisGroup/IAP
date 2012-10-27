package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallStructuresUsingOpening_top_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage mask = new ImageOperation(input().masks().vis()).opening(
						(int) (1 * options.getDoubleSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK))).getImage();
			return new ImageOperation(input().masks().vis()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
		} else
			return input().masks().vis();
	}
	
	// @Override
	// protected FlexibleImage processFLUOmask() {
	// FlexibleImage mask = new ImageOperation(getInput().getMasks().getFluo()).opening(1).getImage();
	// return new ImageOperation(getInput().getMasks().getFluo()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
	// }
	
}
