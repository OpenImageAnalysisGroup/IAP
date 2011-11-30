package de.ipk.ag_ba.image.operations.blocks.cmds.post_process;

import de.ipk.ag_ba.gui.actions.ImageConfiguration;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.blocks.properties.RunnableOnImageSet;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRunPostProcessors extends AbstractSnapshotAnalysisBlockFIS {

	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage fi = getInput().getMasks().getVis();
		if (fi != null) {
			for (RunnableOnImageSet roi : getProperties()
					.getStoredPostProcessors(ImageConfiguration.RgbSide)) {
				fi = roi.postProcessVis(fi);
			}
		}
		return fi;
	}

}
