/*******************************************************************************
 * 
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image_utils;

import ij.ImagePlus;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.graffiti.plugin.io.resources.IOurl;

/**
 * @author klukas
 * 
 */
public class FlexibleImage {

	private final BufferedImage bufferedImage;

	public FlexibleImage(BufferedImage bufferedImage) {
		this.bufferedImage = bufferedImage;
	}

	public FlexibleImage(IOurl url) throws IOException, Exception {
		bufferedImage = ImageIO.read(url.getInputStream());
	}

	/**
	 * The given image is converted to a BufferedImage.
	 */
	public FlexibleImage(ImagePlus image) {
		this.bufferedImage = ImageConverter.convertIJtoBI(image);
	}

	/**
	 * The given image is converted to a BufferedImage.
	 */
	public FlexibleImage(int[] image, int w, int h) {
		this.bufferedImage = ImageConverter.convert1AtoBI(w, h, image);
	}

	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	public int getWidth() {
		return bufferedImage.getWidth();
	}

	public int getHeight() {
		return bufferedImage.getHeight();
	}

	public void print(String title) {
		PrintImage.printImage(bufferedImage, title);
	}

	public ImagePlus getConvertAsImagePlus() {
		return ImageConverter.convertBItoIJ(bufferedImage);
	}

	public int[] getConvertAs1A() {
		return ImageConverter.convertBIto1A(bufferedImage);
	}

	public FlexibleImage resize(int w, int h) {
		if (w == getWidth() && h == getHeight()) {
			return this;
		} else {
			ImageOperation io = new ImageOperation(this);
			io.resize(w, h);
			return io.getImage();
		}
	}
}
