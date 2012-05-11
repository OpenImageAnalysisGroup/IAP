package de.ipk.ag_ba.image.operations.blocks.cmds.post_process;

import de.ipk.ag_ba.commands.ImageConfiguration;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRunPostProcessors extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage fi = getInput().getMasks().getVis();
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
		FlexibleImage fi = getInput().getImages().getVis();
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
}
