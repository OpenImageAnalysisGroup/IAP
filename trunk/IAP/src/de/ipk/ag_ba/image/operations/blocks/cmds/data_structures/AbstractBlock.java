package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class AbstractBlock extends AbstractSnapshotAnalysisBlockFIS {
	
	protected FlexibleImage processImage(FlexibleImage image) {
		return image;
	}
	
	protected abstract FlexibleImage processMask(FlexibleImage mask);
	
	@Override
	protected FlexibleImage processVISimage() throws InterruptedException {
		return processImage(getInput().getImages().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOimage() throws InterruptedException {
		return processImage(getInput().getImages().getFluo());
	}
	
	@Override
	protected FlexibleImage processNIRimage() throws InterruptedException {
		return processImage(getInput().getImages().getNir());
	}
	
	@Override
	protected FlexibleImage processVISmask() throws InterruptedException {
		return processMask(getInput().getMasks().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() throws InterruptedException {
		return processMask(getInput().getMasks().getFluo());
	}
	
	@Override
	protected FlexibleImage processNIRmask() throws InterruptedException {
		return processMask(getInput().getMasks().getNir());
	}
}
