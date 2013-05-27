/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.operation;

import java.awt.image.BufferedImage;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Merges mask A with B. If the result is filled below 1/1000 of the area, not the nearly empty result mask is used but mask A (e.g. RGB).
 * 
 * @author entzian, klukas
 */
public class MaskOperation {
	
	private final int[] rgbImage;
	private final int[] fluorImage;
	private final int background, foreground;
	private final int[] nearIfImage;
	private int filled = 0, deleted = 0;
	private final int[] mask;
	private final int heightMask;
	private final int widthMask;
	
	public MaskOperation(Image rgbImage, Image fluorImage, Image optNirImage, int background, int resForeground) {
		this.rgbImage = rgbImage.getAs1A();
		this.fluorImage = fluorImage.getAs1A();
		if (optNirImage != null)
			this.nearIfImage = optNirImage.getAs1A();
		else
			this.nearIfImage = null;
		this.background = background;
		this.foreground = resForeground;
		
		mask = new int[this.rgbImage.length];
		heightMask = rgbImage.getHeight();
		widthMask = rgbImage.getWidth();
	}
	
	public void mergeMasks() {
		filled = 0;
		deleted = 0;
		if (nearIfImage != null) {
			if (fluorImage.length == rgbImage.length && fluorImage.length == nearIfImage.length)
				for (int i = 0; i < fluorImage.length; i++) {
					if (rgbImage[i] != background && fluorImage[i] != background && nearIfImage[i] != background) {
						mask[i] = foreground;
						filled++;
					} else {
						mask[i] = background;
						if (rgbImage[i] != background || fluorImage[i] != background || nearIfImage[i] != background)
							deleted++;
					}
				}
		} else
			if (fluorImage.length == rgbImage.length)
				for (int i = 0; i < fluorImage.length; i++) {
					if (rgbImage[i] != background && fluorImage[i] != background) {
						mask[i] = foreground;
						filled++;
					} else {
						mask[i] = background;
						if (rgbImage[i] != background || fluorImage[i] != background)
							deleted++;
					}
				}
			else
				throw new UnsupportedOperationException();
		
		if (filled < fluorImage.length * 0.001) {
			for (int i = 0; i < fluorImage.length; i++)
				mask[i] = rgbImage[i];
		}
	}
	
	public int[] getMask() {
		return mask;
	}
	
	public int getMaskHeigth() {
		return heightMask;
	}
	
	public int getMaskWidt() {
		return widthMask;
	}
	
	public Image getMaskAsFlexibleImage() {
		return new Image(widthMask, heightMask, mask);
	}
	
	public BufferedImage getMaskAsBufferedImage() {
		return ImageConverter.convert1AtoBI(widthMask, heightMask, mask);
	}
	
	public double getUnknownMeasurementValuePixels(double correctionForDeletedArea) {
		return filled - deleted * correctionForDeletedArea;
	}
	
	public int getFilledPixels() {
		return filled;
	}
	
	public int getDeletedPixels() {
		return deleted;
	}
	
	public Image apply(Image image) {
		int[] image1A = image.getAs1A();
		
		int i = 0;
		for (int m : mask) {
			if (m == 0)
				image1A[i] = background;
			i++;
		}
		
		return new Image(image.getWidth(), image.getHeight(), image1A);
	}
	
}
