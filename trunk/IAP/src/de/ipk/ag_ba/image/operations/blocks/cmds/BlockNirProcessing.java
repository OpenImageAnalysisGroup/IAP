package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * pipeline processing for nir image (white balancing, ClearBackgroundByComparingNullImageAndImage)
 * 
 * @author pape, klukas
 */
public class BlockNirProcessing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() throws InterruptedException {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
				FlexibleImage nirMask = getInput().getMasks().getNir();
				// compare images
				boolean debug = false;
				nirMask = new ImageOperation(getInput().getImages().getNir()).printImage("img", debug).compare()
							.compareGrayImages(nirMask.print("ref", debug),
									// 250, 12,
									20, 12,
									// 40, 40,
									options.getBackground()).thresholdBlueHigherThan(160).printImage("result", debug).getImage();
				
				getInput().getMasks().setNir(nirMask);
			}
		}
		
		return getInput();
	}
}
