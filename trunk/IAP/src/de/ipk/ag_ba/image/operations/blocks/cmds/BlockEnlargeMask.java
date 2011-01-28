package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.gui.navigation_actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockEnlargeMask extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return enlargeMask(getInput().getMasks().getVis(), options.getRgbNumberOfErodeLoops(),
						options.getRgbNumberOfDilateLoops(), ImageConfiguration.RgbTop);
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return enlargeMask(getInput().getMasks().getFluo(), options.getFluoNumberOfErodeLoops(),
				options.getFluoNumberOfDilateLoops(), ImageConfiguration.FluoTop);
	}
	
	private FlexibleImage enlargeMask(FlexibleImage workImage, int numberOfErodeLoops, int numberOfDilateLoops,
			ImageConfiguration typ) {
		
		ImageOperation io = new ImageOperation(workImage);
		for (int i = 0; i < numberOfErodeLoops; i++)
			io.erode();
		for (int i = 0; i < numberOfDilateLoops; i++)
			io.dilate();
		
		return io.getImage();
	}
}
