package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;
import iap.pipelines.ImageProcessorOptions.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlNirFilterSide_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRmask() {
		boolean debug = getBoolean("debug", false);
		boolean useNirSkeleton = getBoolean("Calculate_Skeleton", true);
		// if (options.getCameraPosition() == CameraPosition.SIDE) {
		FlexibleImage nirMask = input().masks().nir();
		FlexibleImage origNirMask = options.getCameraPosition() == CameraPosition.TOP && nirMask != null ? nirMask.copy() : null;
		int average = 180;
		if (nirMask != null) {
			if (options.isMaize())
				nirMask = nirMask.io().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(
								getInt("Adaptive_Threshold_Region_Size", 150),
								average,
								options.getBackground(),
								getDouble("Adaptive_Threshold_K", 0.10)).getImage().display("ADAPT OUT", debug);
			else {
				double f;
				int regionSize;
				if (options.isBarleyInBarleySystem()) {
					f = getDouble("Adaptive_Threshold_F", 0.08);
					regionSize = getInt("Adaptive_Threshold_Region_Size", 50);
				} else
					if (!options.isArabidopsis()) {
						f = getDouble("Adaptive_Threshold_F", 0.11);
						regionSize = getInt("Adaptive_Threshold_Region_Size", 70);
					} else {
						f = getDouble("Adaptive_Threshold_F", 0.08);
						regionSize = getInt("Adaptive_Threshold_Region_Size", 50);
					}
				nirMask = nirMask.io().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(regionSize, average,
								options.getBackground(), f).getImage().display("ADAPT OUT", debug);
			}
			input().masks().setNir(nirMask);
			if (useNirSkeleton) {
				FlexibleImage sk = nirMask.io().skeletonize(false).getImage();
				if (sk != null) {
					sk = mapOriginalOnSkel(sk, nirMask, options.getBackground());
					getProperties().setImage("nir_skeleton", sk.display("SKELETON", debug));
				}
			}
		}
		if (origNirMask != null) {
			nirMask = nirMask.io().and(origNirMask).getImage();
		}
		return nirMask;
	}
	
	private FlexibleImage mapOriginalOnSkel(FlexibleImage skeleton, FlexibleImage original, int back) {
		int w = skeleton.getWidth();
		int h = skeleton.getHeight();
		int[] img = skeleton.getAs1A();// .clone();
		int[] oi = original.getAs1A();// .clone();
		for (int i = 0; i < img.length; i++) {
			if (img[i] != back) {
				img[i] = oi[i];
			} else
				img[i] = img[i];
		}
		return new FlexibleImage(w, h, img);
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.NIR);
		return res;
	}
}
