package iap.blocks.debug;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.postprocessing.BlSaveResultImages;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape, klukas
 */

public class BlShowIntermediateImages extends AbstractBlock {
	
	@Override
	public boolean isChangingImages() {
		return true;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Display Mask-Images.";
	}
	
	@Override
	public String getDescription() {
		return "Displays mask-images (original resolution) for debug purposes.";
	}
	
	@Override
	protected Image processMask(Image mask) {
		boolean show = false;
		
		for (CameraType ct : CameraType.values()) {
			if (ct != CameraType.UNKNOWN)
				if (getBoolean("Show " + ct.getNiceName(), false) && mask.getCameraType() == ct)
					show = true;
		}
		
		if (show)
			if (getBoolean("Include Image Information", false))
				BlSaveResultImages.markWithImageInfos(mask.copy(), input().images().getImageInfo(mask.getCameraType()), optionsAndResults, getWellIdx()).show(
						mask.getCameraType().getNiceName());
			else
				mask.show(mask.getCameraType().getNiceName());
		
		return mask;
	}
}