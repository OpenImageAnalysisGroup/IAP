package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

public class BlockOpeningClosing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return closingOpening(getInput().getMasks().getVis(), getInput().getImages().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return closingOpening(getInput().getMasks().getFluo(), getInput().getImages().getFluo());
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// return new ImageOperation(getInput().getMasks().getNir(), getInput().getImages().getNIr());
	// }
	
	private FlexibleImage closingOpening(FlexibleImage mask, FlexibleImage image) {
		
		FlexibleImage workImage = PhenotypeAnalysisTask.closingOpening(mask, image, options.getBackground(), 1);
		return workImage;
	}
}