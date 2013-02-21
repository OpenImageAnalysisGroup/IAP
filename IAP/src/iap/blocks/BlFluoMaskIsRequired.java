package iap.blocks;

import iap.blocks.data_structures.AbstractBlock;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

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
	protected FlexibleImage processMask(FlexibleImage mask) {
		if (!enabled)
			return mask;
		if (!fluoAvailable)
			return null;
		else
			return mask;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
