/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.operations;

/**
 * @author entzian
 */
public class MorphologicalOperators {
	
	private final int[][] image;
	
	private final int foreground = 1;
	private final int background = 0;
	
	private final int[][] image_result;
	private int[][] mask;
	
	public MorphologicalOperators(int[][] src_image) {
		this(src_image, new int[][] { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } });
	}
	
	public MorphologicalOperators(int[][] image, int[][] mask) {
		
		this.image = image;
		this.image_result = new int[image.length][image[0].length];
		this.mask = mask;
		
	}
	
	public boolean replaceMask(int[][] newMask) {
		this.mask = newMask;
		return true;
	}
	
	public void opening() {
		erosion();
		int[][] newSrcImage = cloneArray();
		dilatation(newSrcImage);
	}
	
	public void opening(int repeat) {
		erosion();
		for (int i = 1; i < repeat; i++) {
			int[][] newSrcImage = cloneArray();
			erosion(newSrcImage);
		}
		for (int i = 0; i < repeat; i++) {
			int[][] newSrcImage = cloneArray();
			dilatation(newSrcImage);
		}
	}
	
	public void doOpening(int[][] changeMask) {
		erosion();
		replaceMask(changeMask);
		int[][] newSrcImage = cloneArray();
		dilatation(newSrcImage);
	}
	
	// beides mit der selben Maske durchführen
	public void doClosing() {
		dilatation();
		int[][] newSrcImage = cloneArray();
		erosion(newSrcImage);
	}
	
	public void doClosing(int[][] changeMask) {
		dilatation();
		replaceMask(changeMask);
		int[][] newSrcImage = cloneArray();
		erosion(newSrcImage);
	}
	
	public int[][] getResultImage() {
		return this.image_result;
	}
	
	// ##################### Print ##############
	
	public void printImage() {
		printImage(this.image_result);
	}
	
	public void printImage(int[][] image) {
		printImage(image, "ResultImage");
	}
	
	public void printImage(int[][] image, String text) {
		System.out.println(text);
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[i].length; j++)
				System.out.print(image[i][j] + "\t");
			System.out.println("");
		}
	}
	
	// ################### Private ######################
	
	private int[][] cloneArray() {
		return (cloneArray(this.image_result));
	}
	
	private int[][] cloneArray(int[][] existingArray) {
		int[][] newArray = new int[existingArray.length][existingArray[0].length];
		
		for (int i = 0; i < newArray.length; i++)
			System.arraycopy(existingArray[i], 0, newArray[i], 0, existingArray[i].length);
		
		return newArray;
	}
	
	public void dilatation() {
		dilatation(this.image);
	}
	
	private void dilatation(int[][] src_image) {
		int w = src_image.length;
		int h = src_image[0].length;
		int maskWidth = mask.length;
		int maskHeight = mask[0].length;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (src_image[x][y] == foreground) {
					for (int l = 0; l < maskWidth; l++) {
						for (int k = 0; k < maskHeight; k++) {
							if (x + l <= w - 1 && y + k <= h - 1)
								image_result[x + l][y + k] = mask[l][k];
						}
					}
				}
			}
		}
	}
	
	public void erosion() {
		erosion(this.image);
	}
	
	private void erosion(int[][] src_image) {
		for (int i = 0; i < src_image.length; i++) {
			for (int j = 0; j < src_image[i].length; j++) {
				if (i >= 0 && j >= 0
									&& i + (mask.length - 1) <= src_image.length - 1
									&& j + (mask[0].length - 1) <= src_image[i].length - 1) {
					mergeMask(i, j, src_image);
				} else {
					image_result[i][j] = background;
				}
			}
		}
	}
	
	private void mergeMask(int x, int y, int[][] src_image) {
		
		boolean agrees = true;
		
		mainloop: for (int l = 0; l < mask.length; l++) {
			for (int k = 0; k < mask[l].length; k++) {
				if (mask[l][k] == 1
									&& src_image[x + l][y + k] != 1) {
					agrees = false;
					break mainloop;
				}
			}
		}
		
		if (agrees)
			image_result[x][y] = foreground;
		else
			image_result[x][y] = background;
	}
	
}