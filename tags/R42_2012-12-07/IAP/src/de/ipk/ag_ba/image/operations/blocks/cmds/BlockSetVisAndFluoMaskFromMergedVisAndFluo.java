package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.MaskOperation;
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
		MaskOperation o = new MaskOperation(input().masks().fluo(), input().masks().vis(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		FlexibleImage result = new FlexibleImage(input().masks().getLargestWidth(), input().masks().getLargestHeight(), o.getMask());
		int[] resPixels = result.getAs1A();
		{
			int filled = 0;
			int b = options.getBackground();
			for (int p : resPixels)
				if (p != b)
					filled++;
			// if vis is emptied by mask operation, don't change vis (fluo image is empty in this case)
			if (filled < resPixels.length * 0.001d)
				return input().masks().vis();
		}
		{
			int[] srcPixels = input().masks().vis().getAs1A();
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
				return input().masks().vis();
		}
		
		return result;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		MaskOperation o = new MaskOperation(input().masks().vis(), input().masks().fluo(), null, options.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		return new FlexibleImage(input().masks().getLargestWidth(), input().masks().getLargestHeight(), o.getMask());
	}
}
