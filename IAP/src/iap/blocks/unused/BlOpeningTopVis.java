package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlOpeningTopVis extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		if (optionsAndResults.getCameraPosition() == CameraPosition.TOP) {
			Image mask = new ImageOperation(input().masks().vis()).opening(1).getImage();
			return new ImageOperation(input().masks().vis()).applyMask_ResizeMaskIfNeeded(mask, optionsAndResults.getBackground()).getImage();
		} else
			return input().masks().vis();
	}
	
	// @Override
	// protected FlexibleImage processFLUOmask() {
	// FlexibleImage mask = new ImageOperation(getInput().getMasks().getFluo()).opening(1).getImage();
	// return new ImageOperation(getInput().getMasks().getFluo()).applyMask_ResizeMaskIfNeeded(mask, options.getBackground()).getImage();
	// }
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Opening";
	}
	
	@Override
	public String getDescription() {
		return "Perform opening operation.";
	}
}
