package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Resize masks images.
 * 
 * @author klukas
 */
public class BlResizeScale extends AbstractBlock {
	
	private double scaleFactor = 1d;
	
	@Override
	protected void prepare() {
		scaleFactor = getDouble("Scaling 0_1", scaleFactor);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Resize Masks";
	}
	
	@Override
	public String getDescription() {
		return "Resize mask images (e.g. make images smaller).";
	}
	
	@Override
	protected Image processImage(Image image) {
		if (getBoolean("Process " + image.getCameraType(), true))
			return image.io().resize(scaleFactor).getImage();
		else
			return image;
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (getBoolean("Process " + mask.getCameraType(), true))
			return mask.io().resize(scaleFactor).getImage();
		else
			return mask;
	}
}
