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
	
	public ImageOperation compareImages(FlexibleImage referenceImage, double lDiffA, double lDiffB, double abDiff, int background, boolean green, boolean yellow) {
		return compareImages(referenceImage, lDiffA, lDiffB, abDiff, background, green, yellow, false);
	}
	
	public ImageOperation compareImages(FlexibleImage referenceImage, double lDiffA, double lDiffB, double abDiff, int background, boolean green,
			boolean yellow,
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
			labImage = new ImageOperation(inputImage).multiplicateImageChannelsWithFactors(factors).printImage("input", show).getImage().getLab();
			labImageRef = new ImageOperation(referenceImage).multiplicateImageChannelsWithFactors(factors).printImage("reference", show).getImage().getLab();
		} else {
			labImage = inputImage.getLab();
			labImageRef = referenceImage.getLab();
		}
		
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		int[] result = new int[width * height];
		boolean showDiff = true;
		int[] diff;
		if (showDiff)
			diff = new int[width * height];
		else
			diff = null;
		int blue = Color.BLUE.getRGB();
		
		for (int index = 0; index < width * height; index++) {
			
			double l = labImage[0][index] - labImageRef[0][index];
			double a = Math.abs(labImage[1][index] - labImageRef[1][index]);
			double b = Math.abs(labImage[2][index] - labImageRef[2][index]);
			
			if (showDiff)
				diff[index] = new Color((float) Math.abs(2 * l / 255d), (float) Math.abs(2 * a / 255d), (float) Math.abs(2 * b / 255d)).getRGB();
			
			double adaption = 1;
			if (adaptiveDependingOnIntensity) {
				double lmean = (labImage[0][index] + labImageRef[0][index]) / 2;
				// 50 ===> 1
				// 100 ===> 0.5
				// __0 ===> 1.5
				adaption = (50 - lmean) / 50d * 0.5d + 1d;
				
				// __0 ===> 2 min: 57.855087180124805 max: 107.26839402518289
				if (lmean < 50)
					adaption = (adaption - 1) * 2 + 1;
				if (lmean < 50)
					System.out.println(adaption);
				if (lmean > 100)
					System.out.println(lmean);
			}
			
			boolean lOK;
			if (l < 0)
				lOK = -l < lDiffA * adaption;
			else
				lOK = l < lDiffB * adaption;
			
			double aI = labImage[1][index];
			double bI = labImage[1][index];
			
			boolean greenint;
			boolean yellowint;
			
			if (green)
				greenint = -125 < aI && aI < -10;
			else
				greenint = false;
			
			if (yellow)
				yellowint = 30 < bI;
			else
				yellowint = false;
			
			if (a + b < abDiff && lOK && !greenint && !yellowint) {
				result[index] = background;
				
			} else {
				// if (index / 4 % 2 == 0)
				// diff[index] = blue;
				result[index] = imgInp[index];
			}
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