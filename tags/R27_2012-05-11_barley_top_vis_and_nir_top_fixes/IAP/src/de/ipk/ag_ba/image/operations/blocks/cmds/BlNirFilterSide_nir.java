package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlNirFilterSide_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nirImage = getInput().getImages().getNir();
		FlexibleImage nirMask = getInput().getMasks().getNir();
		FlexibleImage origNirMask = options.getCameraPosition() == CameraPosition.TOP ? nirMask.copy() : null;
		int average = 180;
		// if (options.getCameraPosition() == CameraPosition.SIDE) {
		if (nirImage != null && nirMask != null) {
			if (options.isMaize())
				nirMask = nirImage.getIO().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(50, average,
								options.getBackground(), 0.10).getImage().print("ADAPT OUT", debug);
			else {
				double f = 0.08;
				int regionSize = 50;
				if (options.isBarleyInBarleySystem()) {
					f = 0.08;
				} else
					if (!options.isArabidopsis()) {
						f = 0.11;
						regionSize = 70;
					}
				nirMask = nirImage.getIO().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(regionSize, average,
								options.getBackground(), f).getImage().print("ADAPT OUT", debug);
			}
			getInput().getMasks().setNir(nirMask);
			boolean useNirSkeleton = true;
			if (useNirSkeleton) {
				FlexibleImage sk = nirMask.getIO().skeletonize().getImage();
				if (sk != null) {
					if (debug) {
						FlexibleImage skelMap = mapOriginalOnSkel(sk, nirMask, options.getBackground()).
								print("mapped", debug);
					}
					getProperties().setImage("nir_skeleton", sk);
				}
			}
		}
		// }
		if (origNirMask != null) {
			nirMask = nirMask.getIO().and(origNirMask).getImage();
		}
		return nirMask;
	}
	
	private FlexibleImage mapOriginalOnSkel(FlexibleImage skeleton, FlexibleImage original, int back) {
		int w = skeleton.getWidth();
		int h = skeleton.getHeight();
		int[] img = skeleton.getAs1A().clone();
		int[] oi = original.getAs1A().clone();
		for (int i = 0; i < img.length; i++) {
			if (img[i] != back) {
				img[i] = oi[i];
			} else
				img[i] = img[i];
		}
		return new FlexibleImage(w, h, img);
	}
}
