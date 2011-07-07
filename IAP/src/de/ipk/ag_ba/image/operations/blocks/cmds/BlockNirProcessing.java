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
		// if (options.getCameraPosition() == CameraPosition.SIDE) {
		// if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
		// double side = 0.3; // value for white balancing (side width)
		// {
		// FlexibleImage nir = getInput().getImages().getNir();
		// // White Balancing
		// double[] pix = BlockColorBalancing.getProbablyWhitePixels(nir.crop(), side);// 0.08);
		// FlexibleImage temp1 = new ImageOperation(nir).imageBalancing(255, pix).getImage();
		// getInput().getImages().setNir(temp1);
		// }
		// {
		// FlexibleImage nirMask = getInput().getMasks().getNir();
		// // White Balancing
		// double[] pix = BlockColorBalancing.getProbablyWhitePixels(nirMask.crop(), side);
		// FlexibleImage whiteReference = new ImageOperation(nirMask).imageBalancing(255, pix).getImage();
		// // compare images
		// boolean debug = false;
		// whiteReference = new ImageOperation(getInput().getImages().getNir()).printImage("img", debug).compare()
		// .compareGrayImages(whiteReference.print("ref", debug),
		// 250, 12,
		// // 100, 35,
		// options.getBackground()).thresholdBlueHigherThan(238).printImage("result", debug).getImage();
		//
		// getInput().getMasks().setNir(whiteReference);
		// }
		// }
		// }
		
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
				FlexibleImage nirMask = getInput().getMasks().getNir();
				// compare images
				boolean debug = false;
				nirMask = new ImageOperation(getInput().getImages().getNir()).printImage("img", debug).compare()
							.compareGrayImages(nirMask.print("ref", debug),
									// 250, 12,
									40, 40,
									options.getBackground()).thresholdBlueHigherThan(238).printImage("result", debug).getImage();
				
				getInput().getMasks().setNir(nirMask);
			}
		}
		
		return getInput();
	}
}
