package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Apply mask to main images and use result to set mask image set.
 * 
 * @author klukas
 */
public class BlCopyImagesApplyMask extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		Image visMask = input().masks().vis();
		if (visMask != null) {
			return new ImageOperation(input().images().vis()).applyMask_ResizeSourceIfNeeded(visMask, optionsAndResults.getBackground()).getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fluoMask = input().masks().fluo();
		if (fluoMask != null)
			return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(fluoMask, optionsAndResults.getBackground()).getImage();
		else
			return fluoMask;
	}
	
	@Override
	protected Image processNIRmask() {
		Image nirMask = input().masks().nir();
		if (nirMask != null)
			return new ImageOperation(input().images().nir()).applyMask_ResizeSourceIfNeeded(nirMask, optionsAndResults.getBackground()).getImage();
		else
			return nirMask;
	}
	
	@Override
	protected Image processIRmask() {
		Image irMask = input().masks().ir();
		if (irMask != null)
			return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(irMask, optionsAndResults.getBackground()).getImage();
		else
			return irMask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Apply Mask";
	}
	
	@Override
	public String getDescription() {
		return "Apply mask to main images and use result to set mask image set.";
	}
}
