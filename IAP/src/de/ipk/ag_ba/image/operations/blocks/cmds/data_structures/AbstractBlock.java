package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class AbstractBlock extends AbstractSnapshotAnalysisBlockFIS {
	
	protected FlexibleImage processImage(FlexibleImage image) {
		return image;
	}
	
	protected abstract FlexibleImage processMask(FlexibleImage mask);
	
	@Override
	protected FlexibleImage processVISimage() {
		return processImage(getInput().getImages().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		return processImage(getInput().getImages().getFluo());
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		return processImage(getInput().getImages().getNir());
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return processMask(getInput().getMasks().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return processMask(getInput().getMasks().getFluo());
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return processMask(getInput().getMasks().getNir());
	}
}
