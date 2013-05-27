/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image.operation;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Merges mask A with B. If the result is filled below 1/1000 of the area, not the nearly empty result mask is used but mask A (e.g. RGB).
 * 
 * @author entzian, klukas
 */
public class MaskOperationDirect {
	
	private final int background, foreground;
	private int filled = 0, deleted = 0;
	
	int[] mask = null;
	int heightMask = -1;
	int widthMask = -1;
	
	public MaskOperationDirect(int background, int resForeground) {
		this.background = background;
		this.foreground = resForeground;
	}
	
	public void mergeMasks(int[] rgbImage,
			int[] fluorImage,
			int[] nearIfImage, int rgbW, int rgbH) {
		
		if (mask == null || widthMask != rgbW || heightMask != rgbH) {
			mask = new int[rgbImage.length];
			heightMask = rgbW;
			widthMask = rgbH;
		}
		
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
	
	public double getUnknownMeasurementValuePixels(double correctionForDeletedArea) {
		return filled - deleted * correctionForDeletedArea;
	}
	
	public int getFilledPixels() {
		return filled;
	}
	
	public int getDeletedPixels() {
		return deleted;
	}
	
	public Image apply(int[] mask, Image image) {
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
