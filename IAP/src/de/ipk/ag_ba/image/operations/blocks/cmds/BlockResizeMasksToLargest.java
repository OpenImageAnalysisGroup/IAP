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
		w = input().masks().getLargestWidth();
		h = input().masks().getLargestHeight();
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return input().masks().vis().resize(w, h);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return input().masks().fluo().resize(w, h);
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() != null)
			return input().masks().nir().resize(w, h);
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
