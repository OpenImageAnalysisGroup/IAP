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

	private final int[] rgbImage;
	private final int[] fluorImage;
	private final int background;
	private final int[] nearIfImage;
	private int filled = 0, deleted = 0;
	private final int[] mask;

	public MaskOperation(int[] rgbImage, int[] fluorImage, int[] nearIfImage, int background) {
		this.rgbImage = rgbImage;
		this.fluorImage = fluorImage;
		this.background = background;
		this.nearIfImage = nearIfImage;

		mask = new int[rgbImage.length];
	}

	public MaskOperation(FlexibleImage rgbImage, FlexibleImage fluorImage, int background) {
		this(rgbImage.getConvertAs1A(), fluorImage.getConvertAs1A(), new int[] {}, background);
	}

	public MaskOperation(int[] rgbImage, int[] fluorImage, int background) {
		this(rgbImage, fluorImage, new int[] {}, background);
	}

	public MaskOperation(int[][] rgbImage, int[][] fluorImage, int background) {
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(fluorImage), new int[] {}, background);
	}

	public MaskOperation(int[][] rgbImage, int[][] fluorImage, int[][] nearIfImage, int background) {
		this(ImageConverter.convert2Ato1A(rgbImage), ImageConverter.convert2Ato1A(fluorImage), ImageConverter
							.convert2Ato1A(nearIfImage), background);
	}

	public MaskOperation(BufferedImage rgbImage, BufferedImage fluorImage, BufferedImage nearImage, int background) {
		this(ImageConverter.convertBIto1A(rgbImage), ImageConverter.convertBIto1A(fluorImage), ImageConverter
							.convertBIto1A(nearImage), background);
	}

	public void mergeMasks() {
		filled = 0;
		deleted = 0;

		if (nearIfImage.length > 0) {
			if (fluorImage.length == rgbImage.length && fluorImage.length == nearIfImage.length)
				for (int i = 0; i < fluorImage.length; i++) {
					if (rgbImage[i] != background && fluorImage[i] != background && nearIfImage[i] != background) {
						mask[i] = 1;
						filled++;
					} else {
						mask[i] = 0;
						if (rgbImage[i] != background || fluorImage[i] != background || nearIfImage[i] != background)
							deleted++;
					}
				}
		} else
			if (fluorImage.length == rgbImage.length)
				for (int i = 0; i < fluorImage.length; i++) {
					if (rgbImage[i] != background && fluorImage[i] != background) {
						mask[i] = 1;
						filled++;
					} else {
						mask[i] = 0;
						if (rgbImage[i] != background || fluorImage[i] != background)
							deleted++;
					}
				}
			else
				throw new UnsupportedOperationException();

	}

	public int[] getMask() {
		return mask;
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

	public FlexibleImage apply(FlexibleImage image) {
		int[] image1A = image.getConvertAs1A();

		int i = 0;
		for (int m : mask) {
			if (m == 0)
				image1A[i] = background;
			i++;
		}

		return new FlexibleImage(image1A, image.getWidth(), image.getHeight());
	}
}
