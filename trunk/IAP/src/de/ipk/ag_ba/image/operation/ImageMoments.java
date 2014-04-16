package de.ipk.ag_ba.image.operation;

import java.awt.Point;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape
 */
public class ImageMoments {
	
	static Image img;
	
	static Point centerOfGravity;
	static int background;
	
	public ImageMoments(Image img) {
		this.img = img;
		this.background = ImageOperation.BACKGROUND_COLORint;
		this.centerOfGravity = calcCenterOfGravity(img.getAs2A(), background);
	}
	
	/**
	 * Calculation of the normalized n-nd order central image moments mu_ij (greek).
	 * This moments are invariant between translation and scaling.
	 * Formula from wikipedia http://en.wikipedia.org/wiki/Image_moment (normalized by area).
	 * 
	 * @param i
	 *           - exponent i
	 * @param j
	 *           - exponent j
	 * @param centerOfGravity
	 * @param background
	 *           - background color
	 * @return 2nd order moment weighted by the area (first order moment)
	 */
	public static double calcNormalizedCentralMoment(double i, double j, int background) {
		double cogX = centerOfGravity.x;
		double cogY = centerOfGravity.y;
		
		double tempSum = 0;
		double area = 0;
		
		int[][] img2d = img.getAs2A();
		int w = img2d.length; // img.getWidth();
		int h = img2d[0].length; // img.getHeight();
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img2d[x][y] != background) {
					tempSum += Math.pow(x - cogX, i) * Math.pow(y - cogY, j);
					area++;
				}
			}
		}
		// normalization
		return tempSum / Math.pow(area, (i + j + 2) / 2);
	}
	
	/**
	 * Calculation of the n-nd order central image moments mu_ij (greek).
	 * This moments are invariant between translation.
	 * Formula from wikipedia http://en.wikipedia.org/wiki/Image_moment.
	 * 
	 * @param i
	 *           - exponent i
	 * @param j
	 *           - exponent j
	 * @param centerOfGravity
	 * @param img
	 *           - input image
	 * @param background
	 *           - background color
	 * @return n-nd order central moment
	 */
	public double calcCentralMoment(double i, double j, int background) {
		double cogX = centerOfGravity.x;
		double cogY = centerOfGravity.y;
		
		double tempSum = 0;
		
		int[][] img2d = img.getAs2A();
		// int w = img.getWidth();
		// int h = img.getHeight();
		
		for (int x = 0; x < img2d.length; x++) {
			for (int y = 0; y < img2d[x].length; y++) {
				if (img2d[x][y] != background) {
					tempSum += Math.pow(x - cogX, i) * Math.pow(y - cogY, j);
				}
			}
		}
		return tempSum;
	}
	
	/**
	 * Calculation of the n-nd order raw image moments M_ij (without normalization).
	 * 
	 * @param i
	 * @param j
	 * @param img
	 * @param background
	 * @return
	 */
	public double calcRawMoment(double i, double j, int background) {
		
		double tempSum = 0;
		
		int[][] img2d = img.getAs2A();
		int w = img2d.length;
		int h = img2d[0].length;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img2d[x][y] != background) {
					tempSum += Math.pow(x, i) * Math.pow(y, j);
				}
			}
		}
		return tempSum;
	}
	
	/**
	 * Calculation of the center of gravity (also known as centroid) of an image segment.
	 * (only supports already segmentated images, uses binary information, 1 if pixel != background color else 0)
	 * 
	 * @param img
	 *           - input image
	 * @param background
	 *           - background color
	 * @return Point
	 */
	private static Point calcCenterOfGravity(int[][] img, int background) {
		int w = img.length;
		int h = img[1].length;
		int sumX = 0;
		int sumY = 0;
		double area = 0;
		
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (img[x][y] != background) {
					sumX += x;
					sumY += y;
					area++;
				}
			}
		}
		return new Point((int) (sumX / area), (int) (sumY / area));
	}
	
	/**
	 * Calculation of the orientation of major and minor axes.
	 * 
	 * @param regionImage
	 * @param background
	 * @return
	 */
	public static double calcOmega(int background) {
		// formulas based on http://en.wikipedia.org/wiki/Image_moment
		double my20 = ImageMoments.calcNormalizedCentralMoment(2, 0, background);
		double my11 = ImageMoments.calcNormalizedCentralMoment(1, 1, background);
		double my02 = ImageMoments.calcNormalizedCentralMoment(0, 2, background);
		
		// use atan2 for case differentiation, see polar-coordinates
		return Math.atan2((2.0) * my11, my20 - my02) / 2;
	}
	
	public static double[] eigenValues(int background) {
		// formulas based on http://en.wikipedia.org/wiki/Image_moment
		double my20 = ImageMoments.calcNormalizedCentralMoment(2, 0, background);
		double my11 = ImageMoments.calcNormalizedCentralMoment(1, 1, background);
		double my02 = ImageMoments.calcNormalizedCentralMoment(0, 2, background);
		
		double lambda1 = 0.0;
		double lambda2 = 0.0;
		
		double f1 = (my20 + my02) / 2;
		double f2 = Math.sqrt(4 * (my11 * my11) + ((my20 - my02) * (my20 - my02))) / 2;
		
		lambda1 = f1 + f2;
		lambda2 = f1 - f2;
		
		return new double[] { lambda1, lambda2 };
	}
	
	public static Point getCenterOfGravity() {
		return centerOfGravity;
	}
}
