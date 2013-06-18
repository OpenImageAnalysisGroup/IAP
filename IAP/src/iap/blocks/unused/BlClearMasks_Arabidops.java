package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_Arabidops extends AbstractSnapshotAnalysisBlock {
	
	boolean debug = false;
	
	@Override
	protected Image processVISimage() {
		Image img = input().images().vis();
		if (img != null) {
			return img.copy().io().border(40).getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processNIRimage() {
		Image img = input().images().nir();
		if (img != null) {
			return img.copy().io().translate(-3, 0).getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processVISmask() {
		Image img = input().images().vis();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image img = input().images().fluo();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected Image processNIRmask() {
		Image img = input().images().nir();
		if (img != null) {
			return img.copy().io().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
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
}
