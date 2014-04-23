package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractImageAnalysisBlockFIS;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

public class BlMoveImagesToMasks extends AbstractImageAnalysisBlockFIS {
	
	@Override
	protected MaskAndImageSet run() {
		return new MaskAndImageSet(input().images(), input().images().copy());
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
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Copy Image Set to Mask Set";
	}
	
	@Override
	public String getDescription() {
		return "Copies image set to mask set.";
	}
	
}
