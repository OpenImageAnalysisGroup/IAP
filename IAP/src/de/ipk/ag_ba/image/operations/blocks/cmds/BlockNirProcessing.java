package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * pipeline processing for nir image (white balancing, ClearBackgroundByComparingNullImageAndImage)
 * 
 * @author pape
 */
public class BlockNirProcessing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() throws InterruptedException {
		if (options.getCameraTyp() == CameraPosition.SIDE) {
			if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
				{
					FlexibleImage nir = getInput().getImages().getNir();
					// White Balancing
					ImageOperation io = new ImageOperation(nir.crop());
					double[] pix = io.getProbablyWhitePixels(0.08);
					FlexibleImage temp1 = new ImageOperation(nir).ImageBalancing(255, pix).getImage();
					getInput().getImages().setNir(temp1);
				}
				{
					FlexibleImage nirMask = getInput().getMasks().getNir();
					// White Balancing
					ImageOperation ioMask = new ImageOperation(nirMask.crop());
					double[] pix = ioMask.getProbablyWhitePixels(0.08);
					FlexibleImage whiteReference = new ImageOperation(nirMask).ImageBalancing(255, pix).getImage();
					// compare images
					boolean debug = false;
					whiteReference = new ImageOperation(getInput().getImages().getNir()).printImage("img", debug).compare()
							.compareGrayImages(whiteReference.print("ref", debug), 100, 20, options.getBackground()).printImage("result", debug).getImage();
					
					getInput().getMasks().setNir(whiteReference);
				}
			}
		}
		
		return getInput();
	}
}
