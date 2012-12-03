package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClosing_vis extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		return closing(input().masks().vis(), input().images().vis());
	}
	
	private FlexibleImage closing(FlexibleImage mask, FlexibleImage image) {
		int n;
		if (options.isHigherResVisCamera())
			n = getInt("Closing-Cnt", 5);
		else
			n = getInt("Closing-Cnt", 3);
		FlexibleImage workImage = closing(mask, image, options.getBackground(), n);
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
		int white = new Color(255, 255, 255).getRGB();
		int[][] image = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int off = x + y * w;
				int color = rgbArray[off];
				if (color != iBackgroundFill) {
					image[x][y] = 0;
				} else {
					image[x][y] = white;
				}
			}
		}
		int cnt = 0;
		ImageOperation op = new ImageOperation(image);
		do {
			op.closing();
			cnt++;
		} while (cnt < closingRepeat);
		
		return flImage.io().and(op.getImage()).getImage();
	}
}