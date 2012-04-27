package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.options.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * WARNING: takes the input image again!!! (error)
 */
@Deprecated
public class BlockClosing_fluo extends AbstractSnapshotAnalysisBlockFIS {
	protected int closeOperations = -1;
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (getInput().getMasks().getFluo() == null)
			return null;
		return closing(getInput().getMasks().getFluo().getIO().blur(0.5).getImage(), getInput().getImages().getFluo());
	}
	
	private FlexibleImage closing(FlexibleImage mask, FlexibleImage image) {
		
		FlexibleImage workImage = closing(mask, image, options.getBackground(), options.getIntSetting(Setting.CLOSING_REPEAT));
		return workImage;
	}
	
	private static FlexibleImage closing(FlexibleImage flMask, FlexibleImage flImage, int iBackgroundFill, int closingRepeat) {
		int[] rgbArray = flMask.getAs1A();
		int h = flMask.getHeight();
		int w = flMask.getWidth();
		
		int hImage = flImage.getHeight();
		int wImage = flImage.getWidth();
		
		if (hImage != h)
			flImage.resize(w, h);
		if (wImage != w)
			flImage.resize(w, h);
		
		int[] rgbNonModifiedArray = flImage.getAs1A();
		
		int[][] image = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				if (color != iBackgroundFill) {
					image[x][y] = 1;
				} else {
					image[x][y] = 0;
				}
			}
		}
		int cnt = 0;
		ImageOperation op = new ImageOperation(image);
		do {
			op.closing();
			cnt++;
		} while (cnt < closingRepeat);
		image = op.getImageAs2array();
		int[][] mask = image;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (mask[x][y] == 0)
					rgbArray[x + y * w] = iBackgroundFill;
				else
					rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
			}
		}
		
		return new FlexibleImage(w, h, rgbArray);
	}
}