package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImageSet;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.properties.ImageAndImageData;
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
			if (useNirSkeleton) {
				Image sk = nirMask.copy().io().skeletonize().getImage();
				if (sk != null) {
					sk = mapOriginalOnSkel(sk, nirMask, optionsAndResults.getBackground());
					getResultSet()
							.setImage(getBlockPosition(), "nir_skeleton",
									new ImageAndImageData(sk.show("SKELETON", debug), input().masks().getNirInfo()), true);
					if (getBoolean("draw_skeleton", true)) {
						addPostprocessor(nirMask, sk, CameraType.NIR);
					}
				}
			}
		}
		return nirMask;
	}
	
	private void addPostprocessor(Image nirMask, Image sk, CameraType ct) {
		final CameraType ct_fin = ct;
		final Image sk_fin = sk;
		final Image nirMask_fin = nirMask;
		getResultSet().addImagePostProcessor(new RunnableOnImageSet() {
			
			@Override
			public Image postProcessImage(Image image) {
				return image;
			}
			
			private Image markSkelOnMask(Image nirMask_fin, Image sk_fin) {
				return nirMask_fin.io().draw(
						sk_fin.io().replaceColor(ImageOperation.BACKGROUND_COLORint, Color.WHITE.getRGB()).invertImageJ()
								.replaceColor(Color.BLACK.getRGB(), ImageOperation.BACKGROUND_COLORint).getImage(), ImageOperation.BACKGROUND_COLORint);
			}
			
			@Override
			public Image postProcessMask(Image mask) {
				return markSkelOnMask(nirMask_fin, sk_fin);
			}
			
			@Override
			public CameraType getConfig() {
				return ct_fin;
			}
		});
	}
	
	private Image mapOriginalOnSkel(Image skeleton, Image original, int back) {
		int w = skeleton.getWidth();
		int h = skeleton.getHeight();
		int[] sk = skeleton.getAs1A();// .clone();
		int[] orig = original.getAs1A();// .clone();
		for (int i = 0; i < sk.length; i++) {
			if (sk[i] != back) {
				sk[i] = orig[i];
			} else
				sk[i] = sk[i];
		}
		return new Image(w, h, sk);
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
	
	@Override
	public String getName() {
		return "Skeletonize NIR";
	}
	
	@Override
	public String getDescription() {
		return "Skeletonize NIR images and extract according skeleton features. If enabled, the skeleton is drawn in the Post-processing phase.";
	}
}
