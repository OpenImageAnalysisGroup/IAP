package de.ipk.ag_ba.image.operations.blocks.cmds.maize;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

public class BlockFluoToIntensity extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null) {
			System.out.println("WARNING: BlockFluoToIntensity: Fluo Mask is NULL!");
			return null;
		}
		ImageOperation io = new ImageOperation(getInput().getMasks().getFluo());
		FlexibleImage res = io.convertFluo2intensity().getImage();
		return res;
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		processedImages.setFluo(processedMasks.getFluo());
		processedMasks.setFluo(processedMasks.getFluo().getIO().medianFilter32Bit().getImage());
	}
	
}
