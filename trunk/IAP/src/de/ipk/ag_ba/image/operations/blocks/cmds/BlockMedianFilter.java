/**
 * 
 */
package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * @author Pape, Klukas
 */
public class BlockMedianFilter extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage medianMask = new ImageOperation(getInput().getMasks().getFluo()).medianFilter32Bit().border(2).getImage();
		
		return new ImageOperation(getInput().getImages().getFluo()).applyMask_ResizeSourceIfNeeded(medianMask, options.getBackground()).getImage();
	}
	
}
