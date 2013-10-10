package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockResizeMasksToLargest extends AbstractSnapshotAnalysisBlockFIS {
	
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
		if (getInput().getMasks().getNir() != null)
			return getInput().getMasks().getNir().resize(w, h);
		else
			return null;
	}
	
	// @Override
	// protected FlexibleImage processVISimage() {
	// return getInput().getImages().getVis().resize(w, h);
	// }
	//
	// @Override
	// protected FlexibleImage processFLUOimage() {
	// return getInput().getImages().getFluo().resize(w, h);
	// }
	//
	// @Override
	// protected FlexibleImage processNIRimage() {
	// return getInput().getImages().getNir().resize(w, h);
	// }
}