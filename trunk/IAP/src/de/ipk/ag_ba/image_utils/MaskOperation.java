/*************************************************************************
 * 
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

import java.awt.image.BufferedImage;

/**
 * @author entzian
 * 
 */
public class MaskOperation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private final int[] rgbImage;
	private final int[] fluorImage;
	private final int background;
	private final int[] mask;
	private final int[] nearIfImage;
	private int filled = 0, deleted = 0;

	public MaskOperation(int[] rgbImage, int[] fluorImage, int[] nearIfImage, int background) {
		this.rgbImage = rgbImage;
		this.fluorImage = fluorImage;
		this.background = background;
		this.nearIfImage = nearIfImage;
		mask = new int[rgbImage.length];
	}

	public MaskOperation(BufferedImage rgbImage, BufferedImage fluorImage, int background) {
		this(ImageConverter.convertBIto1A(rgbImage), ImageConverter.convertBIto1A(fluorImage), new int[] {}, background);
	}

	public MaskOperation(int[] rgbImage, int[] fluorImage, int background) {
		this(rgbImage, fluorImage, new int[] {}, background);
	}

	public MaskOperation(int[][] rgbImage, int[][] fluorImage, int background) {
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(fluorImage), new int[] {}, background);
	}

	public MaskOperation(int[][] rgbImage, int[][] fluorImage, int[][] nearIfImage, int background) {
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(fluorImage), ImageConverter.convert2Ato1A(nearIfImage), background);
	}

	public MaskOperation(BufferedImage rgbImage, BufferedImage fluorImage, BufferedImage nearImage, int background) {
		this(ImageConverter.convertBIto1A(rgbImage), ImageConverter.convertBIto1A(fluorImage), ImageConverter.convertBIto1A(nearImage), background);
	}

	public int[] getMaskAs1Array() {
		return mask;
	}

	public int[][] getMaskAs2Array(int w, int h) {
		return ImageConverter.convert1Ato2A(w, h, mask);
	}

	public void doMerge() {
		filled = 0;
		deleted = 0;
		if (nearIfImage.length > 0) {
			if (fluorImage.length == rgbImage.length && fluorImage.length == nearIfImage.length)
				for (int i = 0; i < fluorImage.length; i++)
					if (rgbImage[i] != background && fluorImage[i] != background && nearIfImage[i] != background) {
						mask[i] = 1;
						filled++;
					} else {
						mask[i] = 0;
						if (rgbImage[i] != background || fluorImage[i] != background || nearIfImage[i] != background) {
							deleted++;
						}
					}
		} else {
			if (fluorImage.length == rgbImage.length)
				for (int i = 0; i < fluorImage.length; i++)
					if (rgbImage[i] != background && fluorImage[i] != background) {
						mask[i] = 1;
						filled++;
					} else {
						mask[i] = 0;
						if (rgbImage[i] != background || fluorImage[i] != background) {
							deleted++;
						}
					}
		}
	}

	public int getModifiedPixels() {
		return filled - deleted;
	}

	public int getFilledPixels() {
		return filled;
	}

	public int getDeletedPixels() {
		return deleted;
	}

}
