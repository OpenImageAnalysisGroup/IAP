package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.MaskOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Merges the given masks. Only parts which are confirmed as non-background
 * in all input images are retained in the result 1/0 mask.
 * 
 * @param mask
 *           The input masks (should contain cleared background).
 * @return A single 1/0 mask.
 */
public class BlockMergeMask extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processFLUOmask() {
		MaskOperation o = new MaskOperation(getInput().getMasks().getVis(), getInput().getMasks().getFluo(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		return new FlexibleImage(o.getMask(), getInput().getMasks().getLargestWidth(), getInput().getMasks().getLargestHeight());
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return getInput().getMasks().getVis();
	}
}
