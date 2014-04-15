package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlCreateDummyReferenceIfNeeded extends AbstractBlock {
	
	@Override
	protected void prepare() {
		double bp = getDouble("Blur radius percent", 8);
		for (CameraType ct : CameraType.values()) {
			if (ct == CameraType.IR)
				continue;
			Image img = input().images().getImage(ct);
			if (input().masks().getImage(ct) == null && img != null) {
				input().masks().set(img.io().copy().blurImageJ(bp / 100d * img.getWidth()).getImage());
			}
		}
		super.prepare();
	}
	
	@Override
	protected Image processMask(Image mask) {
		return mask;
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
		return BlockType.ACQUISITION;
	}
	
	@Override
	public String getName() {
		return "Create Reference Images";
	}
	
	@Override
	public String getDescription() {
		return "Create a simulated, dummy reference image (in case the reference image is NULL).";
	}
	
}
