/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Remove "peper and salt" noise from Fluo mask.
 * 
 * @author Pape, Klukas
 */
public class BlMedianFilter_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() == null)
			return null;
		
		FlexibleImage medianMask = new ImageOperation(input().masks().fluo())
				.medianFilter32Bit()
				.border(getInt("Median-fluo-border", 2))
				.getImage();
		
		return new ImageOperation(input().images().fluo()).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
}
