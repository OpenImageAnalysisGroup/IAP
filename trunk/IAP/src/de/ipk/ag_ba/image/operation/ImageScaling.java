/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.operation;


/**
 * @author entzian
 */
public class ImageScaling {
	
	private final int[][] src_image;
	private int[][] image_result;
	
	public static void main(String[] args) {
		
		int[][] eingabe_image1 = new int[4][4];
		int zaehler = 1;
		for (int i = 0; i < eingabe_image1.length; i++)
			for (int j = 0; j < eingabe_image1[0].length; j++)
				eingabe_image1[i][j] = zaehler += (255 / 4);
		
		ImageScaling test = new ImageScaling(eingabe_image1);
		test.printImage(eingabe_image1, "Ausgang");
		test.doZoom(1.5, Scaling.NEAREST_NEIGHBOUR);
		// test.doZoom(2.0, Scaling.BILINEAR);
		test.printImage();
		
	}
	
	public ImageScaling(int[][] src_image) {
		this.src_image = src_image;
	}
	
	public void doZoom() {
		doZoom(2.0);
	}
	
	public void doZoom(double factor) {
		doZoom(factor, Scaling.BILINEAR);
	}
	
	public void diZoom(Scaling typ) {
		doZoom(2.0, typ);
	}
	
	public void doZoom(double factor, Scaling typ) {
		zoom(factor, typ);
	}
	
	public int[][] getResultImage() {
		return this.image_result;
	}
	
	// ########################## Print ###################
	
	public void printImage() {
		printImage(this.image_result);
	}
	
	public void printImage(int[][] image) {
		printImage(image, "Image");
	}
	
	public void printImage(int[][] image, String text) {
		System.out.println(text);
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}
	
	// ################## Private ##################
	
	private void zoom(double factor, Scaling typ) {
		
		int newWidth = (int) (src_image.length * factor);
		int newHeigth = (int) (src_image[0].length * factor);
		
		image_result = new int[newWidth][newHeigth];
		
		if (factor == 1) {
			image_result = cloneArray(src_image);
		} else
			if (newWidth == 0 && newHeigth == 0) {
				image_result = new int[][] {};
			} else {
				for (int i = 0; i < newWidth; i++) {
					for (int j = 0; j < newHeigth; j++) {
						image_result[i][j] = getNewGrayValue(i, j, factor, typ);
					}
				}
			}
	}
	
	private int getNewGrayValue(int newPosition_i, int newPosition_j, double factor, Scaling typ) {
		switch (typ) {
			
			case NEAREST_NEIGHBOUR:
				return nearestNeighbour(newPosition_i, newPosition_j, factor);
			case BILINEAR:
				return bilinear(newPosition_i, newPosition_j, factor);
			case HERMITE:
				return -2;
			case GAUSSIAN:
				return -2;
			case BELL:
				return -2;
			case BSPLINE:
				return -2;
			case MITCHELL:
				return -2;
			case LANCZOS:
				return -2;
				
			default:
				return bilinear(newPosition_i, newPosition_j, factor);
		}
	}
	
	private int[][] cloneArray() {
		return (cloneArray(this.image_result));
	}
	
	private int[][] cloneArray(int[][] existingArray) {
		int[][] newArray = new int[existingArray.length][existingArray[0].length];
		
		for (int i = 0; i < newArray.length; i++)
			System.arraycopy(existingArray[i], 0, newArray[i], 0, existingArray[i].length);
		
		return newArray;
	}
	
	private int bilinear(int newPosition_i, int newPosition_j, double factor) { // für Grauwert
	
		double correction_i = newPosition_i / (double) image_result.length;
		double correction_j = newPosition_j / (double) image_result[0].length;
		
		double exactPosition_i = (newPosition_i + correction_i) / factor - correction_i;
		double exactPosition_j = (newPosition_j + correction_j) / factor - correction_j;
		
		int positionInTheOriginalImage_i = (int) Math.ceil(exactPosition_i);
		int positionInTheOriginalImage_j = (int) Math.ceil(exactPosition_j);
		
		double distanceFromExactToOriginalPosition_i = Math.abs(exactPosition_i - positionInTheOriginalImage_i);
		double distanceFromExactToOriginalPosition_j = Math.abs(exactPosition_j - positionInTheOriginalImage_j);
		
		double newGrayValue;
		
		if (positionInTheOriginalImage_i >= src_image.length) {
			positionInTheOriginalImage_i = src_image.length - 1;
			distanceFromExactToOriginalPosition_i = 0;
		}
		
		if (positionInTheOriginalImage_j >= src_image[0].length) {
			positionInTheOriginalImage_j = src_image[0].length - 1;
			distanceFromExactToOriginalPosition_j = 0;
		}
		
		newGrayValue = src_image[positionInTheOriginalImage_i][positionInTheOriginalImage_j] * (1 - distanceFromExactToOriginalPosition_i)
							* (1 - distanceFromExactToOriginalPosition_j);
		
		if (positionInTheOriginalImage_i > 0)
			newGrayValue += src_image[positionInTheOriginalImage_i - 1][positionInTheOriginalImage_j] * distanceFromExactToOriginalPosition_i
								* (1 - distanceFromExactToOriginalPosition_j);
		
		if (positionInTheOriginalImage_j > 0)
			newGrayValue += src_image[positionInTheOriginalImage_i][positionInTheOriginalImage_j - 1] * (1 - distanceFromExactToOriginalPosition_i)
								* distanceFromExactToOriginalPosition_j;
		
		if (positionInTheOriginalImage_i > 0 && positionInTheOriginalImage_j > 0)
			newGrayValue += src_image[positionInTheOriginalImage_i - 1][positionInTheOriginalImage_j - 1] * distanceFromExactToOriginalPosition_i
								* distanceFromExactToOriginalPosition_j;
		
		return (int) newGrayValue;
	}
	
	private int nearestNeighbour(int new_i, int new_j, double factor) {
		
		if (factor >= 1) {
			return src_image[(int) (((new_i) / factor))][(int) (((new_j) / factor))];
		} else {
			return src_image[(int) Math.ceil(new_i / factor)][(int) Math.ceil(new_j / factor)];
		}
	}
}
