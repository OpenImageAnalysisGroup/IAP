package iap.blocks;

import iap.blocks.data_structures.AbstractBlock;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

public class BlSmoothShape extends AbstractBlock {
	
	private final boolean debug = false;
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null && mask.getCameraType() == CameraType.VIS) {
			return mask.io().applyMask_ResizeMaskIfNeeded(
					mask.copy().io().erode(3).blur(2).erode().erode().show("blurred mask", debug).getImage(),
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
}
