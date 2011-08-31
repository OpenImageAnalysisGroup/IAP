package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

/**
 * pipeline processing for nir image (white balancing, ClearBackgroundByComparingNullImageAndImage)
 * 
 * @author pape, klukas
 */
public class BlockNirFilterSide_nir extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleMaskAndImageSet run() {
		if (options.getCameraPosition() == CameraPosition.SIDE) {
			if (getInput().getImages().getNir() != null && getInput().getMasks().getNir() != null) {
				FlexibleImage nirMask = getInput().getMasks().getNir();
				// FlexibleImage nirCopy = getInput().getImages().getNir().copy();
				// compare images
				boolean debug = false;
				int blackDiff = options.getIntSetting(Setting.B_Diff_NIR);
				int whiteDiff = options.getIntSetting(Setting.W_Diff_NIR);
				// getInput().getImages().getNir().getIO().subtractGrayImages(nirMask).print("subimg");
				nirMask = new ImageOperation(getInput().getImages().getNir()).print("img", debug).compare()
							.compareGrayImages(nirMask.print("ref", debug),
									// 20, 12,
									blackDiff, whiteDiff,
									// 250, 12,
									// 40, 40,
									options.getBackground()).thresholdBlueHigherThan(180).print("result", false).getImage(); // 150
				
				getInput().getMasks().setNir(nirMask);
				// nirMask.print("old compare");
				// nirCopy.getIO().crop().adaptiveThreshold(50, 180).adaptiveThreshold(10, 255).getImage().print("new thresh");
				
				FlexibleImage sk = nirMask.getIO().skeletonize().getImage();
				if (sk != null) {
					FlexibleImage skelMap = MapOriginalOnSkel(sk, nirMask, options.getBackground()).print("mapped");
					getProperties().setImage("nir_skeleton", sk);
				}
			}
		}
		
		return getInput();
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
