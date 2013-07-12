package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * pipeline processing for nir image
 * 
 * @author pape, klukas
 */
public class BlSkeletonizeNir extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processNIRmask() {
		boolean debug = getBoolean("debug", false);
		boolean useNirSkeleton = getBoolean("Calculate_Skeleton", true);
		Image nirMask = input().masks().nir();
		if (nirMask != null) {
			input().masks().setNir(nirMask);
			if (useNirSkeleton) {
				Image sk = nirMask.io().skeletonize(false).getImage();
				if (sk != null) {
					sk = mapOriginalOnSkel(sk, nirMask, options.getBackground());
					getProperties().setImage("nir_skeleton", sk.show("SKELETON", debug));
				}
			}
		}
		return nirMask;
	}
	
	private Image mapOriginalOnSkel(Image skeleton, Image original, int back) {
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
		return new Image(w, h, img);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.NIR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
}
