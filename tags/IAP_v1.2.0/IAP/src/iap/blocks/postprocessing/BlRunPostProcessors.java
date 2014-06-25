package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * Runs stored post-processor code stored and created by previous analysis blocks.
 * 
 * @author klukas
 */
public class BlRunPostProcessors extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		Image fi = input().masks().vis();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.VIS)) {
				fi = roi.postProcessMask(fi);
			}
		}
		if (fi != null)
			fi.show("postres vis", debugValues);
		return fi;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		getResultSet().clearStoredPostprocessors();
	}
	
	@Override
	protected Image processVISimage() {
		Image fi = input().images().vis();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.VIS)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fi = input().masks().fluo();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.FLUO)) {
				fi = roi.postProcessMask(fi);
			}
		}
		if (fi != null)
			fi.show("postres fluo", debugValues);
		
		return fi;
	}
	
	@Override
	protected Image processFLUOimage() {
		Image fi = input().images().fluo();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.FLUO)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processNIRmask() {
		Image fi = input().masks().nir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.NIR)) {
				fi = roi.postProcessMask(fi);
			}
		}
		if (fi != null)
			fi.show("postres nir", debugValues);
		return fi;
	}
	
	@Override
	protected Image processNIRimage() {
		Image fi = input().images().nir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.NIR)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processIRmask() {
		Image fi = input().masks().ir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.IR)) {
				fi = roi.postProcessMask(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processIRimage() {
		Image fi = input().images().ir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getResultSet()
					.getStoredPostProcessors(CameraType.IR)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.POSTPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Run Post-Processors";
	}
	
	@Override
	public String getDescription() {
		return "Runs stored post-processor code stored and created by previous analysis blocks.";
	}
	
}
