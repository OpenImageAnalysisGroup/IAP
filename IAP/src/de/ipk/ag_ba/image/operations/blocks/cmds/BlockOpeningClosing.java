package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.operations.MorphologicalOperators;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockOpeningClosing extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return closingOpening(getInput().getMasks().getVis(), getInput().getImages().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return closingOpening(getInput().getMasks().getFluo(), getInput().getImages().getFluo());
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// return new ImageOperation(getInput().getMasks().getNir(), getInput().getImages().getNIr());
	// }
	
	private FlexibleImage closingOpening(FlexibleImage mask, FlexibleImage image) {
		
		FlexibleImage workImage = closingOpening(mask, image, options.getBackground(), 1);
		return workImage;
	}
	
	private static FlexibleImage closingOpening(FlexibleImage flMask, FlexibleImage flImage, int iBackgroundFill, int repeat) {
		int[] rgbArray = flMask.getConvertAs1A();
		int h = flMask.getHeight();
		int w = flMask.getWidth();
		
		int[] rgbNonModifiedArray = flImage.getConvertAs1A();
		
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
		do {
			MorphologicalOperators op = new MorphologicalOperators(image);
			op.doClosing();
			image = op.getResultImage();
			cnt++;
		} while (cnt < repeat);
		int[][] mask = image;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (mask[x][y] == 0)
					rgbArray[x + y * w] = iBackgroundFill;
				else
					rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
			}
		}
		
		return new FlexibleImage(rgbArray, w, h);
		// PrintImage.printImage(rgbArray, w, h);
		//
		// ImageOperation save = new ImageOperation(rgbArray, w, h);
		// save.rotate(3);
		// save.saveImage("/Users/entzian/Desktop/siebenteBild.png");
		//
		// PrintImage.printImage(rgbArray, w, h);
		
	}
}