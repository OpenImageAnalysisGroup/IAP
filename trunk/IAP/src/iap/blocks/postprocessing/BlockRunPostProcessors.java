package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;

public class BlockRunPostProcessors extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected Image processVISmask() {
		Image fi = input().masks().vis();
		// getInput().getMasks().getVis().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeVISMask.png");
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbSide)) {
				fi = roi.postProcessVis(fi);
			}
		}
		return fi;
	}
	
	@Override
	protected Image processVISimage() {
		Image fi = input().images().vis();
		// getInput().getMasks().getVis().copy().saveToFile(ReleaseInfo.getDesktopFolder() + File.separator + "MaizeVISImage.png");
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbSide)) {
				fi = roi.postProcessVis(fi);
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
	
}
