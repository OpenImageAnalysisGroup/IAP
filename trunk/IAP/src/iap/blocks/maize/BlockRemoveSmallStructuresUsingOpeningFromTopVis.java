package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlockRemoveSmallStructuresUsingOpeningFromTopVis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		if (options.getCameraPosition() == CameraPosition.TOP) {
			FlexibleImage mask = new ImageOperation(input().masks().vis()).opening(1).getImage();
			return new ImageOperation(input().masks().vis()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
		} else
			return input().masks().vis();
	}
	
	// @Override
	// protected FlexibleImage processFLUOmask() {
	// FlexibleImage mask = new ImageOperation(getInput().getMasks().getFluo()).opening(1).getImage();
	// return new ImageOperation(getInput().getMasks().getFluo()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
	// }
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
}
