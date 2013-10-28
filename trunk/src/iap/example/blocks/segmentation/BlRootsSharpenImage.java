package iap.example.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class BlRootsSharpenImage extends AbstractSnapshotAnalysisBlock {
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Blur and Sharpen Images";
	}
	
	@Override
	public String getDescription() {
		return "Performs blur and sharpen operation according to provided factors. " +
				"Works on input images, not on mask images.";
	}
}
