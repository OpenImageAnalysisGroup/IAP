package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Improve flower visibility for visible mask.
 * 
 * @author pape
 */
public class BlClosingForMaizeBloom extends AbstractSnapshotAnalysisBlock {
	protected int closeOperations = -1;
	
	@Override
	protected Image processVISmask() {
		if (input().masks().vis() == null)
			return null;
		else
			if (input().images().vis() != null) {
				// getProperties().setImage("beforeBloomEnhancement", input().masks().vis().copy());
				return closing(input().masks().vis(), input().images().vis());
			} else
				return input().masks().vis();
	}
	
	private Image closing(Image mask, Image image) {
		int lThresh = 200; // 100
		int bThresh = 140; // 0, 127
		int aDiffFromZero = 50; // 25
		
		// int w = mask.getWidth();
		// int h = mask.getHeight();
		
		// erodeRetainingLines()
		Image workImage = closing(mask.io().getImage(), optionsAndResults.getBackground(), lThresh, bThresh, aDiffFromZero).show("DDDDDD", false);
		// run twice, because on the input image above "erode" is called once
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		// workImage = closing(workImage, options.getBackground(), lThresh, bThresh, aDiffFromZero);
		
		return image.io().applyMask(workImage).getImage();
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
	
	private static Image closing(Image flMask, int iBackgroundFill, int lThresh, int bThresh,
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
				int radius = 3;
				if (color != iBackgroundFill && bright_yellow) {
					// int green = 0;
					// for (Boolean count : new Boolean[] { true, false })
					for (int xd = -radius; xd <= radius; xd++)
						for (int yd = -radius; yd <= radius; yd++) {
							int oa = off + xd + yd * w;
							if (x + xd >= 0 && y + yd >= 0 && x + xd < w && y + yd < h) {
								
								// if (count) {
								// if (rgbArray[oa] == ImageOperation.BACKGROUND_COLORint) {
								// l = lMask[0][oa];
								// a = lMask[1][oa];
								// b = lMask[2][oa];
								// boolean by = l > lThresh && b > bThresh && Math.abs(a - 127) < aDiffFromZero;
								// if (!by)
								// green++;
								// }
								// } else
								// if (green < 2)
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
		return new Image(image);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Improve maize flower visibility";
	}
	
	@Override
	public String getDescription() {
		return "Improve flower visibility within visible mask.";
	}
}