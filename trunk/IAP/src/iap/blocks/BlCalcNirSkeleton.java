package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlCalcNirSkeleton extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processNIRmask() {
		boolean debug = getBoolean("debug", false);
		boolean useNirSkeleton = getBoolean("Calculate_Skeleton", true);
		FlexibleImage nirMask = input().masks().nir();
		if (nirMask != null) {
			input().masks().setNir(nirMask);
			if (useNirSkeleton) {
				FlexibleImage sk = nirMask.io().skeletonize(false).getImage();
				if (sk != null) {
					sk = mapOriginalOnSkel(sk, nirMask, options.getBackground());
					getProperties().setImage("nir_skeleton", sk.show("SKELETON", debug));
				}
			}
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
		return res;
	}
}
