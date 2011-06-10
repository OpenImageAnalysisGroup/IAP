package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.MorphologicalOperators;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClosingForYellowVisMask extends AbstractSnapshotAnalysisBlockFIS {
	protected int closeOperations = -1;
	
	@Override
	protected FlexibleImage processVISmask() {
		return closing(getInput().getMasks().getVis(), getInput().getImages().getVis());
	}
	
	private FlexibleImage closing(FlexibleImage mask, FlexibleImage image) {
		
		FlexibleImage workImage = closing(mask, image, options.getBackground(), options.getIntSetting(Setting.CLOSING_REPEAT) * 2
				* options.getIntSetting(Setting.SCALE_FACTOR_DECREASE_IMG_AND_MASK) * options.getIntSetting(Setting.SCALE_FACTOR_DECREASE_MASK));
		return workImage;
	}
	
	private static FlexibleImage closing(FlexibleImage flMask, FlexibleImage flImage, int iBackgroundFill, int closingRepeat) {
		int[] rgbArray = flMask.getAs1A();
		int h = flMask.getHeight();
		int w = flMask.getWidth();
		
		int hImage = flImage.getHeight();
		int wImage = flImage.getWidth();
		
		if (hImage != h)
			flImage = flImage.resize(w, h);
		if (wImage != w)
			flImage = flImage.resize(w, h);
		
		int[] rgbNonModifiedArray = flImage.getAs1A();
		
		double[][] lMask = flMask.getLab();
		
		int[][] image = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				double l = lMask[0][off];
				double a = lMask[1][off];
				double b = lMask[2][off];
				boolean bright_yellow = l > 100 && b > 0 && Math.abs(a) < 25;
				if (color != iBackgroundFill && bright_yellow) {
					image[x][y] = 1;
				} else {
					image[x][y] = 0;
				}
			}
		}
		int cnt = 0;
		do {
			MorphologicalOperators op = new MorphologicalOperators(image);
			op.dilatation();
			image = op.getResultImage();
			cnt++;
		} while (cnt < closingRepeat);
		int[][] mask = image;
		
		int blue = Color.BLUE.getRGB();
		
		boolean flag = false;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (mask[x][y] != 0) {
					if (flag)
						rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
					else
						rgbArray[x + y * w] = blue;
					flag = !flag;
				}
			}
		}
		
		return new FlexibleImage(rgbArray, w, h);
	}
}