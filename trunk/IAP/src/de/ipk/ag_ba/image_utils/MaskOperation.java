/*************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *************************************************************************/
package de.ipk.ag_ba.image_utils;

/**
 * @author entzian
 */
public class MaskOperation {
	
	private final int[] rgbImage;
	private final int[] fluorImage;
	private final int background, foreground;
	private final int[] nearIfImage;
	private int filled = 0, deleted = 0;
	private final int[] mask;
	
	public MaskOperation(FlexibleImage rgbImage, FlexibleImage fluorImage, FlexibleImage optNirImage, int background, int resForeground) {
		this.rgbImage = rgbImage.getConvertAs1A();
		this.fluorImage = fluorImage.getConvertAs1A();
		if (optNirImage != null)
			this.nearIfImage = optNirImage.getConvertAs1A();
		else
			this.nearIfImage = null;
		this.background = background;
		this.foreground = resForeground;
		
		mask = new int[this.rgbImage.length];
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
		
	}
	
	public int[] getMask() {
		return mask;
	}
	
	public int getUnknownMeasurementValuePixels() {
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
