package iap.blocks.post_process;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.server.analysis.ImageConfiguration;

public class BlockRunPostProcessors extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage fi = input().masks().vis();
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
	protected FlexibleImage processVISimage() {
		FlexibleImage fi = input().images().vis();
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
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		res.add(FlexibleImageType.IR);
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
}
