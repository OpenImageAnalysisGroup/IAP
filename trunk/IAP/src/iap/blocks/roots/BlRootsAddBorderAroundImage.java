package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Add border of N pixels around the images (visible)
 * 
 * @author klukas
 */
public class BlRootsAddBorderAroundImage extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected Image processVISimage() {
		Image img = input().images().vis();
		int white = new Color(255, 255, 255).getRGB();
		if (img != null)
			img = img
					.io()
					.addBorder(
							getInt("BORDER_SIZE_VIS", 100),
							getInt("ROOT_TRANSLATE_X", 0),
							getInt("ROOT_TRANSLATE_Y", 0),
							white
					).getImage();
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
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
}
