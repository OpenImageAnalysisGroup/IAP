package iap.blocks.data_structures;

import de.ipk.ag_ba.image.structures.Image;

public abstract class AbstractBlock extends AbstractSnapshotAnalysisBlock {
	
	protected Image processImage(Image image) {
		return image;
	}
	
	protected abstract Image processMask(Image mask);
	
	@Override
	protected Image processVISimage() {
		if (input().images() != null && input().images().vis() != null)
			return processImage(input().images().vis());
		else
			return null;
	}
	
	@Override
	protected Image processFLUOimage() {
		if (input().images() != null && input().images().fluo() != null)
			return processImage(input().images().fluo());
		else
			return null;
	}
	
	@Override
	protected Image processNIRimage() {
		if (input().images() != null && input().images().nir() != null)
			return processImage(input().images().nir());
		else
			return null;
	}
	
	@Override
	protected Image processIRimage() {
		if (input().images() != null && input().images().ir() != null)
			return processImage(input().images().ir());
		else
			return null;
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks() != null && input().masks().vis() != null)
			return processMask(input().masks().vis());
		else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks() != null && input().masks().fluo() != null)
			return processMask(input().masks().fluo());
		else
			return null;
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks() != null && input().masks().nir() != null)
			return processMask(input().masks().nir());
		else
			return null;
	}
	
	@Override
	protected Image processIRmask() {
		if (input().masks() != null && input().masks().ir() != null)
			return processMask(input().masks().ir());
		else
			return null;
	}
	
	@Override
	public String getDescriptionForParameters() {
		return null;
	}
}
