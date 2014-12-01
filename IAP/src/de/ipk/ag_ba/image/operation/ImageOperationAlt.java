package de.ipk.ag_ba.image.operation;

/**
 * @author Christian Klukas
 */
public class ImageOperationAlt {
	
	public static ImageOperation gaussianBlur(ImageOperation imageOperation, double sigma) {
		int[] image = imageOperation.getAs1D();
		// adapted from
		// https://code.google.com/p/som-based-clustering/source/browse/trunk/SelfOrganizingMaps/src/main/java/robbie/imageprocessing/GaussianBlur.java?r=22
		int height = imageOperation.getHeight();
		int width = imageOperation.getWidth();
		
		int[] tempImage = new int[image.length];
		int[] filteredImage = new int[image.length];
		
		int n = (int) (6 * sigma + 1);
		if (n <= 0)
			return imageOperation;
		double[] window = new double[n];
		double s2 = 2 * sigma * sigma;
		
		window[(n - 1) / 2] = 1;
		for (int i = 0; i < (n - 1) / 2; i++) {
			window[i] = Math.exp(-i * i / s2);
			window[n - i - 1] = window[i];
		}
		double[] colorRgbArray = new double[] { 0, 0, 0 };
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double sum = 0;
				colorRgbArray[0] = 0;
				colorRgbArray[1] = 0;
				colorRgbArray[2] = 0;
				for (int k = 0; k < window.length; k++) {
					int l = i + k - (n - 1) / 2;
					if (l >= 0 && l < width) {
						int rgb = image[l + j * width];
						if (rgb == ImageOperation.BACKGROUND_COLORint)
							continue;
						int r = ((rgb >> 16) & 0xff);
						int g = ((rgb >> 8) & 0xff);
						int b = (rgb & 0xff);
						colorRgbArray[0] = colorRgbArray[0] + r * window[k];
						colorRgbArray[1] = colorRgbArray[1] + g * window[k];
						colorRgbArray[2] = colorRgbArray[2] + b * window[k];
						sum += window[k];
					}
				}
				if (sum > 0) {
					for (int t = 0; t < 3; t++) {
						colorRgbArray[t] = colorRgbArray[t] / sum;
					}
					int r = (int) colorRgbArray[0];
					int g = (int) colorRgbArray[1];
					int b = (int) colorRgbArray[2];
					tempImage[i + j * width] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
				} else
					tempImage[i + j * width] = imageOperation.BACKGROUND_COLORint;
			}
		}
		
		// --->>
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double sum = 0;
				colorRgbArray[0] = 0;
				colorRgbArray[1] = 0;
				colorRgbArray[2] = 0;
				for (int k = 0; k < window.length; k++) {
					int l = j + k - (n - 1) / 2;
					if (l >= 0 && l < height) {
						int rgb = tempImage[i + l * width];
						if (rgb == ImageOperation.BACKGROUND_COLORint)
							continue;
						int r = ((rgb >> 16) & 0xff);
						int g = ((rgb >> 8) & 0xff);
						int b = (rgb & 0xff);
						colorRgbArray[0] = colorRgbArray[0] + r * window[k];
						colorRgbArray[1] = colorRgbArray[1] + g * window[k];
						colorRgbArray[2] = colorRgbArray[2] + b * window[k];
						sum += window[k];
					}
				}
				if (sum > 0) {
					for (int t = 0; t < 3; t++) {
						colorRgbArray[t] = colorRgbArray[t] / sum;
					}
					int r = (int) colorRgbArray[0];
					int g = (int) colorRgbArray[1];
					int b = (int) colorRgbArray[2];
					filteredImage[i + j * width] = (0xFF << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
				} else
					filteredImage[i + j * width] = imageOperation.BACKGROUND_COLORint;
			}
		}
		for (int idx = 0; idx < filteredImage.length; idx++)
			image[idx] = filteredImage[idx];
		return imageOperation;
	}
	
}
