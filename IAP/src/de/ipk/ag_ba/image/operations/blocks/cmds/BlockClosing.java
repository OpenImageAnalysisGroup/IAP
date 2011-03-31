package de.ipk.ag_ba.image.operations.blocks.cmds;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.Setting;
import de.ipk.ag_ba.image.operations.MorphologicalOperators;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.structures.FlexibleImage;

public class BlockClosing extends AbstractSnapshotAnalysisBlockFIS {
	protected int closeOperations = -1;
	
	@Override
	protected FlexibleImage processVISmask() {
		// System.out.println("typ: RGB");
		return closing(getInput().getMasks().getVis(), getInput().getImages().getVis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		// System.out.println("typ: FLUO");
		return closing(getInput().getMasks().getFluo(), getInput().getImages().getFluo());
	}
	
	// @Override
	// protected FlexibleImage processNIRmask() {
	// System.out.println("typ: NIR");
	// return closing(getInput().getMasks().getNir(), getInput().getImages().getNir());
	// }
	
	private FlexibleImage closing(FlexibleImage mask, FlexibleImage image) {
		
		FlexibleImage workImage = closing(mask, image, options.getBackground(), options.getIntSetting(Setting.CLOSING_REPEAT));
		return workImage;
	}
	
	private static FlexibleImage closing(FlexibleImage flMask, FlexibleImage flImage, int iBackgroundFill, int closingRepeat) {
		
		// dauert länger
		// ImageOperation maskIo = new ImageOperation(flMask);
		// maskIo.closing();
		// return new FlexibleImage(maskIo.getImageAs2array());
		
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
		do {
			MorphologicalOperators op = new MorphologicalOperators(image);
			op.doClosing();
			image = op.getResultImage();
			cnt++;
		} while (cnt < closingRepeat);
		int[][] mask = image;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (mask[x][y] == 0)
					rgbArray[x + y * w] = iBackgroundFill;
				else
					rgbArray[x + y * w] = rgbNonModifiedArray[x + y * w];
			}
		}
		// System.out.println("test");
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