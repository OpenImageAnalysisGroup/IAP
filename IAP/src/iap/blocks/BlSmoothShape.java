package iap.blocks;

import iap.blocks.data_structures.AbstractBlock;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlSmoothShape extends AbstractBlock {
	
	private final boolean debug = false;
	
	@Override
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (mask != null && mask.getType() == FlexibleImageType.VIS) {
			return mask.io().applyMask_ResizeMaskIfNeeded(
					mask.copy().io().erode(3).blur(2).erode().erode().print("blurred mask", debug).getImage(),
					options.getBackground()).getImage();
		} else
			return mask;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
