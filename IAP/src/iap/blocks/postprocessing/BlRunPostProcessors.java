package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;

/**
 * Runs stored postprocessor code stored and created by previous analysis blocks.
 * 
 * @author klukas
 */
public class BlRunPostProcessors extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		Image fi = input().masks().vis();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbSide)) {
				fi = roi.postProcessMask(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbTop)) {
				fi = roi.postProcessMask(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		getProperties().clearStoredPostprocessors();
	}
	
	@Override
	protected Image processVISimage() {
		Image fi = input().images().vis();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbSide)) {
				fi = roi.postProcessImage(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbTop)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processFLUOmask() {
		Image fi = input().masks().fluo();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.FluoSide)) {
				fi = roi.postProcessMask(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.FluoTop)) {
				fi = roi.postProcessMask(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processFLUOimage() {
		Image fi = input().images().fluo();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.FluoSide)) {
				fi = roi.postProcessImage(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.FluoTop)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processNIRmask() {
		Image fi = input().masks().nir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.NirSide)) {
				fi = roi.postProcessMask(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.NirTop)) {
				fi = roi.postProcessMask(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processNIRimage() {
		Image fi = input().images().nir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.NirSide)) {
				fi = roi.postProcessImage(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.NirTop)) {
				fi = roi.postProcessImage(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processIRmask() {
		Image fi = input().masks().ir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.IrSide)) {
				fi = roi.postProcessMask(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.IrTop)) {
				fi = roi.postProcessMask(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processIRimage() {
		Image fi = input().images().ir();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.IrSide)) {
				fi = roi.postProcessImage(fi);
			}
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.IrTop)) {
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
	
}
