package de.ipk.ag_ba.image.operations.blocks.cmds.arabidopsis;

import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Clears all images around a circle in the middle
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlClearMasks_Arabidopsis_ClearOutSideSinglePot_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISimage() {
		FlexibleImage img = getInput().getImages().getVis();
		if (img != null) {
			return img.copy().getIO().
					clearOutsideCircle(
							img.getWidth() / 2,
							img.getHeight() / 2 - 30,
							(int) (img.getHeight() / 2.45d)).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processFLUOimage() {
		FlexibleImage img = getInput().getImages().getFluo();
		if (img != null) {
			return img.copy().getIO().
					clearOutsideCircle(
							img.getWidth() / 2,
							img.getHeight() / 2,
							(int) (img.getHeight() / 2.45d)).getImage();
		} else
			return null;
	}
	
	@Override
	protected FlexibleImage processNIRimage() {
		FlexibleImage img = getInput().getImages().getNir();
		if (img != null) {
			return img.copy().getIO().translate(-3, 0).
					clearOutsideCircle(
							img.getWidth() / 2,
							img.getHeight() / 2,
							(int) (img.getHeight() / 2.45d)).getImage();
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
