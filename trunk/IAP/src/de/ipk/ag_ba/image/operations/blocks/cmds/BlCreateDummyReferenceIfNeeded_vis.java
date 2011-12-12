package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageSet;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlCreateDummyReferenceIfNeeded_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getImages().getVis() != null && getInput().getMasks().getVis() == null)
			return getInput().getImages().getVis().copy().getIO().thresholdLAB(
					100, 150,
					140, 500,
					0, 500,
					ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).
					or(
							getInput().getImages().getVis().copy().getIO().thresholdLAB(
									0, 100,
									0, 500,
									0, 500,
									ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).getImage()
					).
					or(
							getInput().getImages().getVis().copy().getIO().thresholdLAB(
									200, 500,
									0, 500,
									0, 500,
									ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).getImage()
					).
					or(
							getInput().getImages().getVis().copy().getIO().thresholdLAB(
									0, 200,
									150, 500,
									0, 140,
									ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).getImage()
					).
					or(
							getInput().getImages().getVis().copy().getIO().thresholdLAB(
									0, 255,
									100, 500,
									0, 100,
									ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).print("FILTER 4", debug).getImage()
					)
					.blur(2).
					getImage();
		else
			return super.processVISmask();
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		// if (getInput().getImages().getFluo() != null && getInput().getMasks().getFluo() == null) {
		// // create simulated fluo background
		// int w = getInput().getImages().getFluo().getWidth();
		// int h = getInput().getImages().getFluo().getHeight();
		// return ImageOperation.createColoredImage(w, h, new Color(3, 3, 3));
		// } else
		if (getInput().getImages().getFluo() != null && getInput().getMasks().getFluo() == null)
			return getInput().getImages().getFluo().copy().getIO().blur(2).thresholdLAB(
					0, 50,
					0, 500,
					0, 155,
					ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).
					blur(2).
					getImage();
		else
			return super.processFLUOmask();
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage n = getInput().getImages().getNir();
		if (n != null && getInput().getMasks().getNir() == null) {
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().getIO().getCanvas().fillRect(0, 0, w, h, new Color(180, 180, 180).getRGB()).getImage();
		} else
			return super.processNIRmask();
	}
	
	@Override
	protected void postProcess(FlexibleImageSet processedImages, FlexibleImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
		// FlexibleImage fi = processedMasks.getFluo().copy().getIO()
		// .applyMask_ResizeMaskIfNeeded(processedMasks.getVis().copy().getIO().blur(4).getImage(), options.getBackground()).getImage();
		// processedMasks.setFluo(fi);
	}
}
