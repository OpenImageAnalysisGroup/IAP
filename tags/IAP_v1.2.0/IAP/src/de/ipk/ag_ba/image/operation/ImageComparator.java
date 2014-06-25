package de.ipk.ag_ba.image.operation;

import java.awt.Color;

import org.ErrorMsg;

import de.ipk.ag_ba.image.structures.Image;

/**
 * compares distance(L a b Values) of two images(pixel by pixel), returns a new image under following conditions:
 * if input == reference -> Background
 * else input retain
 * lDiffA, lDiffB, abDiff are the tolerances
 * 
 * @author pape, klukas
 */
public class ImageComparator {
	
	private final Image inputImage;
	
	public ImageComparator(Image input) {
		this.inputImage = input;
	}
	
	public ImageOperation compareImages(String desc, Image referenceImage, double lDiffA, double lDiffB, double abDiff, int background) {
		return compareImages(desc, referenceImage, lDiffA, lDiffB, abDiff, background, false);
	}
	
	public ImageOperation compareImages(String desc, Image referenceImage, double lDiffA, double lDiffB, double abDiff, int background,
			boolean adaptiveDependingOnIntensity) {
		
		if (inputImage != null && referenceImage != null) {
			if (inputImage.getWidth() != referenceImage.getWidth()
					|| inputImage.getHeight() != referenceImage.getHeight()) {
				inputImage.show("detected size differences A");
				referenceImage.show("detected size differences A (ref)");
			}
		}
		
		// inputImage = new ImageOperation(inputImage).blur(1).getImage();
		// referenceImage = new ImageOperation(referenceImage).blur(2).getImage();
		
		int[] imgInp = inputImage.getAs1A();
		float[][] labImage;
		float[][] labImageRef;
		
		boolean ignoreRed = false;
		if (ignoreRed) {
			double[] factors = new double[] { 0.05, 0.7, 0.7 };
			boolean show = true;
			labImage = new ImageOperation(inputImage).multiplicateImageChannelsWithFactors(factors).show("input", show).getImage().getLab(false);
			labImageRef = new ImageOperation(referenceImage).multiplicateImageChannelsWithFactors(factors).show("reference", show).getImage().getLab(false);
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
				try {
					diff[index] = new Color((float) Math.abs(l / 255d), (float) (a / 255d), (float) (b / 255d)).getRGB();
				} catch (Exception e) {
					System.err.println("LAB: " + l + " " + a + " " + b);
					ErrorMsg.addErrorMessage(e);
				}
				
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
			new Image(width, height, diff).show("difference: " + desc);
		return new ImageOperation(new Image(width, height, result));
	}
	
	public ImageOperation compareGrayImages(Image referenceImage, double maxDiffBlack, double maxDiffWhite, int background) {
		boolean invert = false;
		if (maxDiffBlack < 0 && maxDiffWhite < 0) {
			invert = true;
			maxDiffBlack = -maxDiffBlack;
			maxDiffWhite = -maxDiffWhite;
		}
		
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
			boolean nearlyEqual = diff < maxDiff;
			if (invert)
				nearlyEqual = !nearlyEqual;
			
			if (nearlyEqual) {
				result[index] = background;
			} else {
				result[index] = imgInp[index];
			}
		}
		return new ImageOperation(new Image(width, height, result));
	}
}