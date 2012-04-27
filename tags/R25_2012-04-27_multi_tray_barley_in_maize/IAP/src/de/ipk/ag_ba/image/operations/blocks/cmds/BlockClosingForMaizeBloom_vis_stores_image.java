package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Improve flower visibility for visible mask.
 * 
 * @author pape
 */
public class BlockClosingForMaizeBloom_vis_stores_image extends AbstractSnapshotAnalysisBlockFIS {
	protected int closeOperations = -1;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (getInput().getMasks().getVis() == null)
			return null;
		else
			if (getInput().getImages().getVis() != null) {
				getProperties().setImage("beforeBloomEnhancement", getInput().getMasks().getVis().copy());
				return closing(getInput().getMasks().getVis(), getInput().getImages().getVis());
			} else
				return getInput().getMasks().getVis();
	}
	
	private FlexibleImage closing(FlexibleImage mask, FlexibleImage image) {
		int lThresh = 200; // 100
		int bThresh = 140; // 0, 127
		int aDiffFromZero = 50; // 25
		
		int w = mask.getWidth();
		int h = mask.getHeight();
		
		int[] in = mask.copy().getAs1A();
		
		// erodeRetainingLines()
		FlexibleImage workImage = closing(mask.getIO().getImage(), options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// run twice, because on the input image above "erode" is called once
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		
		int[] workImg = workImage.getAs1A();
		
		for (int i = 0; i < workImg.length; i++) {
			if (workImg[i] != ImageOperation.BACKGROUND_COLORint)
				in[i] = workImg[i];
		}
		
		return new FlexibleImage(w, h, in);
	}
	
	// private static FlexibleImage closingWithMask(FlexibleImage flMask, FlexibleImage flImage, int iBackgroundFill, int closingRepeat, int lThresh, int
	// bThresh,
	// int aDiffFromZero) {
	// int[] rgbArray = flMask.getAs1A();
	// int h = flMask.getHeight();
	// int w = flMask.getWidth();
	//
	// int hImage = flImage.getHeight();
	// int wImage = flImage.getWidth();
	//
	// if (hImage != h)
	// flImage = flImage.resize(w, h);
	// if (wImage != w)
	// flImage = flImage.resize(w, h);
	//
	// int[] rgbNonModifiedArray = flImage.getAs1A();
	//
	// int[][] image = new int[w][h];
	// for (int x = 0; x < w; x++) {
	// for (int y = 0; y < h; y++) {
	// int off = x + y * w;
	// int[] rgbVal = new int[5];
	// int rgbVal[0] = rgbArray[off];
	// int above = rgbArray[off - w];
	// int left = rgbArray[off - 1];
	// int right = rgbArray[off + 1];
	// int below = rgbArray[off + w];
	// getAvgLab(center, above, left, right, below);
	// if (l > lThresh)
	// System.out.println("l: " + l + " a: " + a + " b: " + b);
	// boolean bright_yellow = l > lThresh && b > bThresh && Math.abs(a - 127) < aDiffFromZero;
	// if (color != iBackgroundFill && bright_yellow) {
	// image[x][y] = 1;
	// } else {
	// image[x][y] = 0;
	// }
	// }
	// }
	// int cnt = 0;
	// do {
	// MorphologicalOperators op = new MorphologicalOperators(image);
	// op.dilatation();
	// image = op.getResultImage();
	// cnt++;
	// } while (cnt < closingRepeat);
	// int[][] mask = image;
	//
	// int blue = Color.BLUE.getRGB();
	// boolean debug = false;
	// boolean flag = false;
	// for (int x = 0; x < w; x++) {
	// for (int y = 0; y < h; y++) {
	// if (mask[x][y] != 0) {
	// if (debug) {
	// if (flag)
	// rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
	// else
	// rgbArray[x + y * w] = blue;
	// flag = !flag;
	// } else
	// rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
	// }
	// }
	// }
	//
	// return new FlexibleImage(w, h, rgbArray);
	// }
	//
	// private static void getAvgLab(int center, int above, int left, int right, int below) {
	//
	// ImageOperation.labCube
	// }
	
	private static FlexibleImage closing(FlexibleImage flMask, int iBackgroundFill, int lThresh, int bThresh,
			int aDiffFromZero) {
		int[] rgbArray = flMask.getAs1A();
		int h = flMask.getHeight();
		int w = flMask.getWidth();
		
		float[][] lMask = flMask.getLab(true);
		
		int[][] image = new int[w][h];
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				image[x][y] = color;
				double l = lMask[0][off];
				double a = lMask[1][off];
				double b = lMask[2][off];
				// if (l > lThresh)
				// System.out.println("l: " + l + " a: " + a + " b: " + b);
				boolean bright_yellow = l > lThresh && b > bThresh && Math.abs(a - 127) < aDiffFromZero;
				if (color != iBackgroundFill && bright_yellow) {
					int green = 0;
					for (Boolean count : new Boolean[] { true, false })
						for (int xd = -1; xd <= 1; xd++)
							for (int yd = -1; yd <= 1; yd++) {
								int oa = off + xd + yd * w;
								if (x + xd >= 0 && y + yd >= 0 && x + xd < w && y + yd < h) {
									
									if (count) {
										if (rgbArray[oa] == ImageOperation.BACKGROUND_COLORint) {
											l = lMask[0][oa];
											a = lMask[1][oa];
											b = lMask[2][oa];
											boolean by = l > lThresh && b > bThresh && Math.abs(a - 127) < aDiffFromZero;
											if (!by)
												green++;
										}
									} else
										if (green < 2)
											if (rgbArray[oa] == ImageOperation.BACKGROUND_COLORint) {
												image[x + xd][y + yd] = color;
											}
								}
							}
				} else {
					if (color != iBackgroundFill)
						image[x][y] = rgbArray[off];
					else
						image[x][y] = ImageOperation.BACKGROUND_COLORint;
				}
			}
		}
		
		return new FlexibleImage(image);
	}
}