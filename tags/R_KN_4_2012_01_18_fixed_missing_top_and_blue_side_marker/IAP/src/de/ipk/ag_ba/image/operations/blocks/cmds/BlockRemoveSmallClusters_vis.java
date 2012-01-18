package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallClusters_vis extends BlockRemoveSmallClusters_vis_fluo {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage in = getInput().getMasks().getVis();
		if (in != null && options.getCameraPosition() == CameraPosition.TOP) {
			in.getIO();
			in = ImageOperation.removeSmallPartsOfImage(true, in, options.getBackground(), 35, 5, NeighbourhoodSetting.NB4, options.getCameraPosition(), null,
					false);
		}
		return in;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage in = getInput().getMasks().getFluo();
		if (in != null && options.getCameraPosition() == CameraPosition.TOP) {
			in.getIO();
			in = ImageOperation.removeSmallPartsOfImage(true, in, options.getBackground(), 35, 5, NeighbourhoodSetting.NB4, options.getCameraPosition(), null,
					false);
		}
		return in;
	}
}
