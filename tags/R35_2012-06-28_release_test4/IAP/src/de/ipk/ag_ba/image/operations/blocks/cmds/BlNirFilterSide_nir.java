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
		boolean useNirSkeleton = true;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			FlexibleImage nirMask = input().masks().nir();
			FlexibleImage origNirMask = options.getCameraPosition() == CameraPosition.TOP && nirMask != null ? nirMask.copy() : null;
			int average = 180;
			if (nirMask != null) {
				if (options.isMaize())
					nirMask = nirMask.io().print("ADAPT IN", debug).
							adaptiveThresholdForGrayscaleImage(150, average,
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
					nirMask = nirMask.io().print("ADAPT IN", debug).
							adaptiveThresholdForGrayscaleImage(regionSize, average,
									options.getBackground(), f).getImage().print("ADAPT OUT", debug);
				}
				input().masks().setNir(nirMask);
				if (useNirSkeleton) {
					FlexibleImage sk = nirMask.io().skeletonize().getImage();
					if (sk != null) {
						sk = mapOriginalOnSkel(sk, nirMask, options.getBackground());
						getProperties().setImage("nir_skeleton", sk.print("SKELETON", debug));
					}
				}
			}
			if (origNirMask != null) {
				nirMask = nirMask.io().and(origNirMask).getImage();
			}
			return nirMask;
		} else {
			if (useNirSkeleton) {
				FlexibleImage nirMask = input().masks().nir();
				FlexibleImage sk = nirMask.io().skeletonize().getImage();
				if (sk != null) {
					sk = mapOriginalOnSkel(sk, nirMask, options.getBackground());
					getProperties().setImage("nir_skeleton", sk.print("SKELETON", debug));
				}
			}
			return input().masks().nir();
		}
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
