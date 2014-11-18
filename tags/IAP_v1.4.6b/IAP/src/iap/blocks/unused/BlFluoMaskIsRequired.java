package iap.blocks.unused;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Optionally removes VIS/NIR/IR if no Fluo Can be Found
 * 
 * @author klukas
 */
public class BlFluoMaskIsRequired extends AbstractBlock {
	
	boolean fluoAvailable, enabled;
	
	@Override
	protected void prepare() {
		super.prepare();
		this.enabled = getBoolean("Set VIS, NIR, IR to NULL if no FLUO is found", true);
		this.fluoAvailable = input().masks().fluo() != null;
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (!enabled)
			return mask;
		if (!fluoAvailable)
			return null;
		else
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.DEBUG;
	}
	
	@Override
	public String getName() {
		return "Fluo Required";
	}
	
	@Override
	public String getDescription() {
		return "Optionally removes VIS,NIR,IR if no FLUO image can be found.";
	}
}
