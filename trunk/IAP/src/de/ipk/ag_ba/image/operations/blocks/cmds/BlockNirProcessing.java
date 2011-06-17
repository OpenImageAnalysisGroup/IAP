package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraTyp;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * pipeline processing for nir image
 * 
 * @author pape
 */
public class BlockNirProcessing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() throws InterruptedException {
		if (options.getCameraTyp() == CameraTyp.SIDE) {
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
					whiteReference = new ImageOperation(getInput().getImages().getNir()).compare()
							.compareImages(whiteReference,
									options.getIntSetting(Setting.L_Diff_NIR),
									options.getIntSetting(Setting.L_Diff_NIR),
									options.getIntSetting(Setting.abDiff_NIR),
									options.getBackground(), false, false).border(2).getImage();
					
					getInput().getMasks().setNir(whiteReference);
				}
			}
		}
		
		return getInput();
	}
}
