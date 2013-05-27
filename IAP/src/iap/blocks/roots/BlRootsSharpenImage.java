package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * @author klukas
 */
public class BlRootsSharpenImage extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected Image processVISmask() {
		Image img = input().images().vis();
		if (img != null)
			img = img.io().copy()
					.blur(getInt("blur", 2))
					.sharpen(getInt("sharpen", 3))
					.getImage();
		return img;
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
