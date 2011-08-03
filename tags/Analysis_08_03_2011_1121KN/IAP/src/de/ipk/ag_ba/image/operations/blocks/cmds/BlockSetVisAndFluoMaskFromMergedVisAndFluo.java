package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.MaskOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Merges the given masks. Only parts which are confirmed as non-background
 * in all input images are retained in the result 1/0 mask.
 * 
 * @param mask
 *           The input masks (should contain cleared background).
 * @return A single 1/0 mask.
 */
public class BlockSetVisAndFluoMaskFromMergedVisAndFluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		MaskOperation o = new MaskOperation(getInput().getMasks().getFluo(), getInput().getMasks().getVis(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		FlexibleImage result = new FlexibleImage(o.getMask(), getInput().getMasks().getLargestWidth(), getInput().getMasks().getLargestHeight());
		int[] resPixels = result.getAs1A();
		{
			int filled = 0;
			int b = options.getBackground();
			for (int p : resPixels)
				if (p != b)
					filled++;
			// if vis is emptied by mask operation, don't change vis (fluo image is empty in this case)
			if (filled < resPixels.length * 0.001d)
				return getInput().getMasks().getVis();
		}
		{
			int[] srcPixels = getInput().getMasks().getVis().getAs1A();
			int b = options.getBackground();
			int filledSrc = 0;
			for (int p : srcPixels)
				if (p != b)
					filledSrc++;
			int filledRes = 0;
			for (int p : resPixels)
				if (p != b)
					filledRes++;
			// if vis is cleared for more than 95%, the vis image remains unchanged
			if (filledRes < filledSrc * 0.05d)
				return getInput().getMasks().getVis();
		}
		
		return result;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		MaskOperation o = new MaskOperation(getInput().getMasks().getVis(), getInput().getMasks().getFluo(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		return new FlexibleImage(o.getMask(), getInput().getMasks().getLargestWidth(), getInput().getMasks().getLargestHeight());
	}
}
