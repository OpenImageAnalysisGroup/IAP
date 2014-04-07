package iap.blocks.data_structures;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public abstract class AbstractBlock extends AbstractSnapshotAnalysisBlock {
	
	protected Image processImage(Image image) {
		return image;
	}
	
	protected abstract Image processMask(Image mask);
	
	@Override
	protected Image processVISimage() {
		if (input().images() != null && input().images().vis() != null) {
			return setImageType(processImage(input().images().vis()), CameraType.VIS);
		} else
			return null;
	}
	
	@Override
	protected Image processFLUOimage() {
		if (input().images() != null && input().images().fluo() != null)
			return setImageType(processImage(input().images().fluo()), CameraType.FLUO);
		else
			return null;
	}
	
	@Override
	protected Image processNIRimage() {
		if (input().images() != null && input().images().nir() != null)
			return setImageType(processImage(input().images().nir()), CameraType.NIR);
		else
			return null;
	}
	
	@Override
	protected Image processIRimage() {
		if (input().images() != null && input().images().ir() != null)
			return setImageType(processImage(input().images().ir()), CameraType.IR);
		else
			return null;
	}
	
	@Override
	protected Image processVISmask() {
		if (input().masks() != null && input().masks().vis() != null)
			return setImageType(processMask(input().masks().vis()), CameraType.VIS);
		else
			return null;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().masks() != null && input().masks().fluo() != null)
			return setImageType(processMask(input().masks().fluo()), CameraType.FLUO);
		else
			return null;
	}
	
	@Override
	protected Image processNIRmask() {
		if (input().masks() != null && input().masks().nir() != null)
			return setImageType(processMask(input().masks().nir()), CameraType.NIR);
		else
			return null;
	}
	
	@Override
	protected Image processIRmask() {
		if (input().masks() != null && input().masks().ir() != null)
			return setImageType(processMask(input().masks().ir()), CameraType.IR);
		else
			return null;
	}
	
	@Override
	public String getDescriptionForParameters() {
		return null;
	}
}
