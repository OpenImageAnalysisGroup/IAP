package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.segmentation.NeighbourhoodSetting;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockRemoveSmallClusters_vis extends BlockRemoveSmallClusters_vis_fluo {
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage in = input().masks().vis();
		if (in != null && options.getCameraPosition() == CameraPosition.TOP) {
			in.io();
			in = ImageOperation.removeSmallPartsOfImage(true, in, options.getBackground(), 35, 5, NeighbourhoodSetting.NB4, options.getCameraPosition(), null,
					false);
		}
		if (in != null && options.getCameraPosition() == CameraPosition.SIDE) {
			in.io();
			in = ImageOperation.removeSmallPartsOfImage(true, in, options.getBackground(), 25, 3, NeighbourhoodSetting.NB4, options.getCameraPosition(), null,
					false);
		}
		return in;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage in = input().masks().fluo();
		if (in != null && options.getCameraPosition() == CameraPosition.TOP) {
			in.io();
			in = ImageOperation.removeSmallPartsOfImage(true, in, options.getBackground(), 35, 5, NeighbourhoodSetting.NB4, options.getCameraPosition(), null,
					false);
		}
		return in;
	}
}