package de.ipk.ag_ba.image.operations;

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
		
		// inputImage = new ImageOperation(inputImage).blur(1).getImage();
		// referenceImage = new ImageOperation(referenceImage).blur(2).getImage();
		
		int[] imgInp = inputImage.getAs1A();
		
		double[][] labImage = inputImage.getLab();
		double[][] labImageRef = referenceImage.getLab();
		
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
		
		int[] result = new int[width * height];
		
		for (int index = 0; index < width * height; index++) {
			
			double l = labImage[0][index] - labImageRef[0][index];
			double a = Math.abs(labImage[1][index] - labImageRef[1][index]);
			double b = Math.abs(labImage[2][index] - labImageRef[2][index]);
			
			boolean lOK;
			if (l < 0)
				lOK = -l < lDiffA;
			else
				lOK = l < lDiffB;
			
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
				result[index] = imgInp[index];
			}
		}
		return new ImageOperation(new FlexibleImage(result, width, height));
	}
	
}