package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockClearNirTop extends AbstractSnapshotAnalysisBlockFIS {
	
	protected boolean isChangingImages() {
		return options.getCameraPosition() == CameraPosition.TOP;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraPosition() == CameraPosition.TOP)
			return null;
		else
			return input().masks().nir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (options.getCameraPosition() == CameraPosition.TOP)
			return null;
		else
			return input().images().nir();
	}
}
