package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_Arabidopsis_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage img = getInput().getImages().getVis();
		if (img != null) {
			return img.copy().getIO().border(40).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processVISmask() {
		FlexibleImage img = getInput().getImages().getVis();
		if (img != null) {
			return img.copy().getIO().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		FlexibleImage img = getInput().getImages().getFluo();
		if (img != null) {
			return img.copy().getIO().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage img = getInput().getImages().getNir();
		if (img != null) {
			return img.copy().getIO().fillRect2(0, 0, img.getWidth(), img.getHeight()).getImage();
		} else
			return null;
	}
}
