package iap.blocks.debug;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */

public class BlFilterImagesByCameraType extends AbstractBlock {
	
	@Override
	public boolean isChangingImages() {
		return true;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.FLUO);
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
		return "Filter Images by Camera Type";
	}
	
	@Override
	public String getDescription() {
		return "Removes images from specific camera type.";
	}
	
	@Override
	protected Image processImage(Image image) {
		if (image != null) {
			CameraType ct = image.getCameraType();
			if (ct != null) {
				boolean process = getBoolean("Process " + ct, true);
				if (!process) {
					return null;
				} else {
					return image;
				}
			}
		}
		return null;
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask != null) {
			CameraType ct = mask.getCameraType();
			if (ct != null) {
				boolean process = getBoolean("Process " + ct, true);
				if (!process) {
					return null;
				} else {
					return mask;
				}
			}
		}
		return null;
	}
}
