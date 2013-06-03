/**
 * 
 */
package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Uses a lab-based pixel filter for the vis and fluo images.
 * 
 * @author Klukas
 */
public class BlRotate extends AbstractBlock {
	boolean debug = false;
	
	@Override
	protected void prepare() {
		super.prepare();
	}
	
	@Override
	protected Image processImage(Image image) {
		double r = image != null ? getDouble("Rotate Main Image " + image.getCameraType(), image.getCameraType() == CameraType.IR ? -90d : 0d) : 0d;
		if (image != null && Math.abs(r) > 0.001) {
			if (image != null && Math.abs(r - 90) < 0.001)
				image = image.io().rotate90().getImage();
			else
				if (image != null && Math.abs(r + 90) < 0.001)
					image = image.io().rotate90().rotate(180, false).flipHor().getImage();
				else
					image = image.io().rotate(r, false).getImage();
		}
		return image;
	}
	
	@Override
	protected Image processMask(Image mask) {
		double r = getDouble("Rotate Mask Image " + mask.getCameraType(), mask.getCameraType() == CameraType.IR ? -90d : 0d);
		if (mask != null && Math.abs(r) > 0.001) {
			if (mask != null && Math.abs(r - 90) < 0.001)
				mask = mask.io().rotate90().getImage();
			else
				if (mask != null && Math.abs(r + 90) < 0.001)
					mask = mask.io().rotate90().rotate90().rotate90().getImage();
				else
					mask = mask.io().rotate(r, false).getImage();
		}
		return mask;
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
	
}
