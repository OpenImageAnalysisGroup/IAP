package de.ipk.ag_ba.image.operations.blocks.cmds.data_structures;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public abstract class AbstractBlock extends AbstractSnapshotAnalysisBlockFIS {
	
	protected FlexibleImage processImage(FlexibleImage image) {
		return image;
	}
	
	protected abstract FlexibleImage processMask(FlexibleImage mask);
	
	@Override
	protected FlexibleImage processVISimage() {
		if (input().images().vis() != null)
			return processImage(input().images().vis());
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		if (input().images().fluo() != null)
			return processImage(input().images().fluo());
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (input().images().nir() != null)
			return processImage(input().images().nir());
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() != null)
			return processMask(input().masks().vis());
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input().masks().fluo() != null)
			return processMask(input().masks().fluo());
		else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (input().masks().nir() != null)
			return processMask(input().masks().nir());
		else
			return null;
	}
}
