package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Resize masks images to largest width and height.
 * 
 * @author klukas
 */
public class BlockClearNirTop extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRmask() {
		if (options.getCameraTyp() == CameraTyp.TOP)
			return null;
		else
			return getInput().getMasks().getNir();
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		if (options.getCameraTyp() == CameraTyp.TOP)
			return null;
		else
			return getInput().getMasks().getNir();
	}
}
