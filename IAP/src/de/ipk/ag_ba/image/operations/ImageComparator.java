package de.ipk.ag_ba.image.operations;

import java.awt.Color;

import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * compares distance(L a b Values) of two images(pixel by pixel), returns a new image under following conditions:
 * if input == reference -> Background
 * else input retain
 * lDiffA, lDiffB, abDiff are the tolerances
 * 
 * @author pape, klukas
 */
public class ImageComparator {
	
	private final FlexibleImage inputImage;
	
	public ImageComparator(FlexibleImage input) {
		this.inputImage = input;
	}
	
	public ImageOperation compareImages(FlexibleImage referenceImage, double lDiffA, double lDiffB, double abDiff, int background) {
		return compareImages(referenceImage, lDiffA, lDiffB, abDiff, background, false);
	}
	
	public ImageOperation compareImages(FlexibleImage referenceImage, double lDiffA, double lDiffB, double abDiff, int background,
			boolean adaptiveDependingOnIntensity) {
		
		// inputImage = new ImageOperation(inputImage).blur(1).getImage();
		// referenceImage = new ImageOperation(referenceImage).blur(2).getImage();
		
		int[] imgInp = inputImage.getAs1A();
		double[][] labImage;
		double[][] labImageRef;
		
		boolean ignoreRed = false;
		if (ignoreRed) {
			double[] factors = new double[] { 0.05, 0.7, 0.7 };
			boolean show = false;
			labImage = new ImageOperation(inputImage).multiplicateImageChannelsWithFactors(factors).printImage("input", show).getImage().getLab(false);
			labImageRef = new ImageOperation(referenceImage).multiplicateImageChannelsWithFactors(factors).printImage("reference", show).getImage().getLab(false);
		} else {
			labImage = inputImage.getLab(false);
			labImageRef = referenceImage.getLab(false);
		}
		
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		int[] result = new int[width * height];
		boolean showDiff = false;
		int[] diff;
		if (showDiff)
			diff = new int[width * height];
		else
			diff = null;
		
		double adaption = 1;
		
		for (int index = 0; index < width * height; index++) {
			double l = labImage[0][index] - labImageRef[0][index];
			double a = labImage[1][index] - labImageRef[1][index];
			double b = labImage[2][index] - labImageRef[2][index];
			if (a < 0)
				a = -a;
			if (b < 0)
				b = -b;
			
			if (showDiff) {
				diff[index] = new Color((float) Math.abs(l / 255d), (float) (a / 255d), (float) (b / 255d)).getRGB();
			}
			
			if (adaptiveDependingOnIntensity) {
				double lmean = (((labImage[0][index] + labImageRef[0][index]) / 2d) / 255d);
				adaption = lmean * lmean * lmean;
			}
			
			boolean lOK;
			if (l < 0)
				lOK = -l * adaption < lDiffA;
			else
				lOK = l * adaption < lDiffB;
			
			if (a + b < abDiff && lOK)
				result[index] = background;
			else
				result[index] = imgInp[index];
		}
		if (showDiff)
			new FlexibleImage(diff, width, height).print("difference");
		return new ImageOperation(new FlexibleImage(result, width, height));
	}
	
	public ImageOperation compareGrayImages(FlexibleImage referenceImage, double maxDiffBlack, double maxDiffWhite, int background) {
		
		int[] imgInp = inputImage.getAs1A();
		int[] imgRef = referenceImage.getAs1A();
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		int[] result = new int[width * height];
		
		for (int index = 0; index < width * height; index++) {
			
			int in = (imgInp[index] & 0x0000ff);
			int ref = (imgRef[index] & 0x0000ff);
			int diff = Math.abs(in - ref);
			int avg = (in + ref) / 2;
			double maxDiff = avg / 255d * (maxDiffWhite - maxDiffBlack) + maxDiffBlack;
			boolean equal = diff < maxDiff;
			if (equal) {
				result[index] = background;
			} else {
				result[index] = imgInp[index];
			}
		}
		return new ImageOperation(new FlexibleImage(result, width, height));
	}
}