package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Add border of N pixels around the images (visible)
 * 
 * @author klukas, entzian
 */
public class BlRootsAddBorderAroundImage extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage img = input().images().vis();
		int white = new Color(255, 255, 255).getRGB();
		if (img != null)
			img = img
					.io()
					.addBorder(
							getInt("BORDER_SIZE_VIS", 100),
							getInt("ROOT_TRANSLATE_X", 50),
							getInt("ROOT_TRANSLATE_Y", 50),
							white
					).getImage();
		return img;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
}
