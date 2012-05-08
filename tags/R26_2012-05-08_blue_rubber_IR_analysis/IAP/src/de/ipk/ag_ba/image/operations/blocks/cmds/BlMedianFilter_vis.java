/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Remove "peper and salt" noise from Fluo mask.
 * 
 * @author Klukas
 */
public class BlMedianFilter_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		
		FlexibleImage medianMask = new ImageOperation(getInput().getMasks().getVis()).medianFilter32Bit().border(2).getImage();
		
		return new ImageOperation(getInput().getImages().getVis()).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
}
