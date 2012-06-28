/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Klukas
 */
public class BlMedianFilter_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		
		FlexibleImage medianMask = new ImageOperation(input().masks().vis()).medianFilter32Bit().dilate(4).border(2).getImage();
		
		return new ImageOperation(input().images().vis()).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
}
