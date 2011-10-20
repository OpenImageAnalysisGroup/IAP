package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * pipeline processing for nir image (white balancing, ClearBackgroundByComparingNullImageAndImage)
 * 
 * @author pape, klukas
 */
public class BlockNirFilterSide_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug = false;
	
	@Override
	protected FlexibleImage processNIRmask() {
		FlexibleImage nirImage = getInput().getImages().getNir();
		FlexibleImage nirMask = getInput().getMasks().getNir();
		int average = 150;
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (nirImage != null && nirMask != null) {
				// compare images
				int blackDiff = 50; // options.getIntSetting(Setting.B_Diff_NIR);
				int whiteDiff = 5; // options.getIntSetting(Setting.W_Diff_NIR);
				boolean advancedComparisonFilter = true;
				if (advancedComparisonFilter) {
					ImageOperation subtracted = nirImage.getIO().
							subtractGrayImages(nirMask).print("subimg", debug);
					int[] sub = subtracted.copy().getImageAs1array();
					int idx = 0;
					for (int c : sub) {
						int r = (c & 0xff0000) >> 16;
						int b = (c & 0x0000ff);
						int rn = Math.abs(r - b);
						sub[idx++] = (0xFF << 24 | (rn & 0xFF) << 16) | ((rn & 0xFF) << 8) | ((rn & 0xFF) << 0);
					}
					new FlexibleImage(nirImage.getWidth(), nirImage.getHeight(), sub).
						print("subtracted gray", debug);
					int[] nirArray = nirImage.getAs1A();
					int[] nirRefArray = nirMask.getAs1A();
					double sum = 0;
					int differenceCutOff = 15;
					idx = 0;
					for (int c : sub) {
						if ((c & 0x0000ff) < differenceCutOff)
							sum += (nirArray[idx++] & 0x0000ff);
					}
					if (idx > 0) {
						average = (int) (sum / idx);
						idx = 0;
						int gray = (0xFF << 24 | (average & 0xFF) << 16) | ((average & 0xFF) << 8) | ((average & 0xFF) << 0);
						for (int c : sub) {
							if ((c & 0x0000ff) < differenceCutOff) {
								nirArray[idx] = gray;
								nirRefArray[idx] = gray;
							}
							idx++;
						}
						nirImage = new FlexibleImage(nirImage.getWidth(), nirImage.getHeight(), nirArray).
								print("CLEANED UP INP", debug);
						nirMask = new FlexibleImage(nirMask.getWidth(), nirMask.getHeight(), nirRefArray).
								print("CLEANED UP REF", debug);
					}
				}
				// if (options.isMaize())
				// nirMask = new ImageOperation(nirImage).print("input", debug).compare()
				// .compareGrayImages(nirMask.print("ref", debug),
				// // 20, 12,
				// blackDiff, whiteDiff,
				// // 250, 12,
				// // 40, 40,
				// new Color(180, 180, 180).getRGB()).print("result of comparison", false).getImage(); // 150
				
				if (options.isMaize())
					nirMask = nirImage.getIO().print("ADAPT IN", debug).
						adaptiveThresholdForGrayscaleImage(50, average,
							options.getBackground(), 0.10).getImage().print("ADAPT OUT", debug);
				else {
					double f  = 0.08;
					if (options.isBarleyInBarleySystem())
						f = 0.15;
					nirMask = nirImage.getIO().print("ADAPT IN", debug).adaptiveThresholdForGrayscaleImage(50, average,
							options.getBackground(), f).getImage().print("ADAPT OUT", debug);
				}
				getInput().getMasks().setNir(nirMask);
				boolean useNirSkeleton = true;
				if (useNirSkeleton) {
					FlexibleImage sk = nirMask.getIO().skeletonize().getImage();
					if (sk != null) {
						if (debug) {
							FlexibleImage skelMap = MapOriginalOnSkel(sk, nirMask, options.getBackground()).
									print("mapped", debug);
						}
						getProperties().setImage("nir_skeleton", sk);
					}
				}
			}
		}
		return nirMask;
	}
	
	private FlexibleImage MapOriginalOnSkel(FlexibleImage skeleton, FlexibleImage original, int back) {
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
