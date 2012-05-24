package de.ipk.ag_ba.image.operations.blocks.cmds;

import java.awt.Color;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClosing_vis extends AbstractSnapshotAnalysisBlockFIS {
	protected int closeOperations = -1;
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input().masks().vis() == null)
			return null;
		return closing(input().masks().vis(), input().images().vis());
	}
	
	private FlexibleImage closing(FlexibleImage mask, FlexibleImage image) {
		int n = 3;
		if (options.isHighResMaize())
			n = 5;
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
		// op.print("MASKKKK");
		
		return flImage.io().and(op.getImage()).getImage();
	}
}