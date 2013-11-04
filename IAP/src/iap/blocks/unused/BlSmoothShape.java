package iap.blocks.unused;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlSmoothShape extends AbstractBlock {
	
	private final boolean debug = false;
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null && mask.getCameraType() == CameraType.VIS) {
			return mask.io().applyMask_ResizeMaskIfNeeded(
					mask.copy().io().erode(2).blur(3).erode().erode().show("blurred mask", debug).getImage(),
					options.getBackground()).getImage();
		} else
			return mask;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
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
		return "Smooth Shape";
	}
	
	@Override
	public String getDescription() {
		return "Smooth borders of visible light mask.";
	}
}
