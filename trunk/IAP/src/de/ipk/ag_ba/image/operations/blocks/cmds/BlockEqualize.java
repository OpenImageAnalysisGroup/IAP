package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockEqualize extends AbstractSnapshotAnalysisBlockFIS {
	
	private int w;
	private int h;
	
	@Override
	protected void prepare() {
		w = getInput().getMasks().getLargestWidth();
		h = getInput().getMasks().getLargestHeight();
		
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return getInput().getMasks().getVis().resize(w, h);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return getInput().getMasks().getFluo().resize(w, h);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return getInput().getMasks().getNir().resize(w, h);
	}
}
