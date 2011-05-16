/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.structures;

import ij.ImagePlus;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;
import org.graffiti.plugin.io.resources.ResourceIOManager;

import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.operations.ImageConverter;
import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.PrintImage;

/**
 * @author klukas
 */
public class FlexibleImage {
	
	private final ImagePlus image;
	private final int w, h;
	private FlexibleImageType type = FlexibleImageType.UNKNOWN;
	
	@Override
	public String toString() {
		return image != null ? image.getWidth() + " x " + image.getHeight()
				+ " " + image.getBitDepth() + " bit" : "NULL IMAGE";
	}
	
	public FlexibleImage(BufferedImage bufferedImage) {
		this(ImageConverter.convertBItoIJ(bufferedImage));
	}
	
	public FlexibleImage(BufferedImage bufferedImage, FlexibleImageType type) {
		this(ImageConverter.convertBItoIJ(bufferedImage));
		this.type = type;
	}
	
	public FlexibleImage(IOurl url) throws IOException, Exception {
		InputStream is = url.getInputStream();
		MyByteArrayOutputStream out = new MyByteArrayOutputStream();
		ResourceIOManager.copyContent(is, out);
		MyByteArrayInputStream in = new MyByteArrayInputStream(out.getBuff(),
				out.size());
		image = new ImagePlus("JImage", ImageIO.read(in));
		w = image.getWidth();
		h = image.getHeight();
		// this(ImageConverter.convertBItoIJ(ImageIO.read(in)));
	}
	
	/**
	 * The given image is converted to a BufferedImage.
	 */
	public FlexibleImage(ImagePlus image) {
		this.image = image;
		this.w = image.getWidth();
		this.h = image.getHeight();
	}
	
	/**
	 * The given image is converted to a BufferedImage.
	 */
	public FlexibleImage(int[] image, int w, int h) {
		this(ImageConverter.convert1AtoIJ(w, h, image));
	}
	
	public FlexibleImage(int[][] img) {
		this(ImageConverter.convert2AtoIJ(img));
	}
	
	public FlexibleImage(Image image) {
		this(GravistoService.getBufferedImage(image));
	}
	
	public BufferedImage getAsBufferedImage() {
		return image.getBufferedImage();
	}
	
	public int getWidth() {
		return w;
	}
	
	public int getHeight() {
		return h;
	}
	
	public void print(String title) {
		PrintImage.printImage(image, title);
	}
	
	public ImagePlus getAsImagePlus() {
		ImagePlus result = image.createImagePlus();
		result.setProcessor(image.getProcessor().duplicate());
		return result;
	}
	
	int[] cache = null;
	
	public int[] getAs1A() {
		boolean useCache = true;
		if (!useCache)
			return ImageConverter.convertIJto1A(image);
		if (cache == null)
			cache = ImageConverter.convertIJto1A(image);
		// else
		// System.out.println("CACHED 1A");
		return cache;
	}
	
	public FlexibleImage resize(int w, int h) {
		// if (w == getWidth() && h == getHeight()) {
		// return new FlexibleImage(getConvertAs2A());
		// } else {
		ImageOperation io = new ImageOperation(this);
		io.resize(w, h);
		return io.getImage();
		// }
	}
	
	int[][] cache2A = null;
	
	public int[][] getAs2A() {
		boolean useCache = true;
		if (!useCache)
			return ImageConverter.convertIJto2A(image);
		
		if (cache2A == null)
			cache2A = ImageConverter.convertIJto2A(image);
		// else
		// System.out.println("CACHED 2A");
		return cache2A;
	}
	
	public FlexibleImageType getType() {
		return type;
	}
	
	public FlexibleImage copy() {
		return new FlexibleImage(getAsImagePlus());
	}
	
	public void setType(FlexibleImageType type) {
		this.type = type;
	}
	
	public FlexibleImage crop() {
		ImageOperation io = new ImageOperation(image);
		io = io.crop();
		return io.getImage();
	}
	
	/**
	 * @param pLeft
	 *           0..1 percentage cut left
	 * @param pRight
	 *           0..1 percentage cut right
	 * @param pTop
	 *           0..1 percentage cut top
	 * @param pBottom
	 *           0..1 percentage cut bottom
	 * @return
	 */
	public FlexibleImage crop(double pLeft, double pRight, double pTop,
			double pBottom) {
		ImageOperation io = new ImageOperation(image);
		io = io.crop(pLeft, pRight, pTop, pBottom);
		return io.getImage();
	}
	
	public double[][] getLab() {
		final int w = getWidth();
		final int h = getHeight();
		final int arrayRGB[] = new int[w * h];
		getAsBufferedImage().getRGB(0, 0, w, h, arrayRGB, 0, w);
		double arrayL[] = new double[w * h];
		double arrayA[] = new double[w * h];
		double arrayB[] = new double[w * h];
		ColorUtil.getLABfromRGB(arrayRGB, arrayL, arrayA, arrayB, false);
		return new double[][] {
				arrayL, arrayA, arrayB };
	}
	
}
