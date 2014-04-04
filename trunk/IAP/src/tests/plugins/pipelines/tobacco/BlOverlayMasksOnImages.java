package tests.plugins.pipelines.tobacco;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.image.structures.MaskAndImageSet;

/**
 * Replaces the main image set with the mask images.
 * 
 * @author klukas
 */
public class BlOverlayMasksOnImages extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected MaskAndImageSet run() {
		ImageSet i = input().images();
		ImageSet m = input().masks();
		
		double f = getDouble("gamma", 0.1);
		for (CameraType ct : CameraType.values()) {
			if (ct == CameraType.UNKNOWN)
				continue;
			
			if (getBoolean("move " + ct, ct != CameraType.VIS)) {
				i.setImage(ct, m.getImage(ct));
			} else {
				if (i.getImage(ct) != null && m.getImage(ct) != null) {
					Image overlay = (m.getImage(ct)).io().or(i.getImage(ct).io().gamma(f).getImage()).getImage();
					overlay.setCameraType(ct);
					i.set(overlay);
				} else {
					i.setImage(ct, m.getImage(ct));
				}
			}
		}
		
		return new MaskAndImageSet(i, new ImageSet());
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.POSTPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Overlay Mask on Images";
	}
	
	@Override
	public String getDescription() {
		return "Create a combination of the mask and the main images.";
	}
}
