package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;


import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class AbstractBlock extends AbstractSnapshotAnalysisBlockFIS {
	
	protected FlexibleImage processImage(FlexibleImage image) {
		return image;
	}
	
	protected abstract FlexibleImage processMask(FlexibleImage mask);
	
	@Override
	protected FlexibleImage processVISimage() {
		return processImage(input().images().vis());
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		return processImage(input().images().fluo());
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		return processImage(input().images().nir());
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		return processMask(input().masks().vis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return processMask(input().masks().fluo());
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return processMask(input().masks().nir());
	}
}
