/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.structures;

import ij.ImagePlus;
import ij.process.ColorProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import javax.imageio.ImageIO;

import org.SystemAnalysis;
import org.SystemOptions;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.operation.Channel;
import de.ipk.ag_ba.image.operation.ImageConverter;
import de.ipk.ag_ba.image.operation.ImageDisplay;
import de.ipk.ag_ba.image.operation.ImageOperation;

/**
 * @author klukas
 */
public class Image {
	
	private ImagePlus image;
	private final int w, h;
	private CameraType cameraType = CameraType.UNKNOWN;
	private String fileName;
	
	@Override
	public String toString() {
		return image != null ? image.getWidth() + " x " + image.getHeight()
				+ " " + image.getBitDepth() + " bit" : "NULL IMAGE";
	}
	
	public Image(BufferedImage bufferedImage) {
		this(ImageConverter.convertBItoIJ(bufferedImage));
	}
	
	public Image(BufferedImage bufferedImage, CameraType type) {
		this(ImageConverter.convertBItoIJ(bufferedImage));
		this.cameraType = type;
	}
	
	private static WeakHashMap<String, BufferedImage> url2image = new WeakHashMap<String, BufferedImage>();
	
	public Image(IOurl url) throws IOException, Exception {
		BufferedImage img = null;
		// synchronized (url2image) {
		img = url2image.get(url + "");
		// if (img != null)
		// System.out.print("~o~" + url);
		// else
		// System.out.println("- ~ "+url);
		if (img == null) {
			InputStream is = url.getInputStream();
			if (is == null)
				System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: no input stream for URL " + url);
			try {
				img = ImageIO.read(is);
			} finally {
				is.close();
			}
			url2image.put(url + "", img);
		}
		// }
		
		if (img == null)
			throw new Exception("Image could not be read: " + url);
		image = new ImagePlus(url.getFileName(),
				new ColorProcessor(img.getWidth(), img.getHeight(), ImageConverter.convertBIto1A(img)));
		w = image.getWidth();
		h = image.getHeight();
		
		if (url != null && url.getFileName() != null)
			this.fileName = url.getFileName();
	}
	
	public Image(ImagePlus image) {
		this.image = image;
		this.w = image.getWidth();
		this.h = image.getHeight();
	}
	
	public Image(int w, int h, int[] image) {
		this(ImageConverter.convert1AtoIJ(w, h, image));
	}
	
	public Image(int[][] img) {
		this(ImageConverter.convert2AtoIJ(img));
	}
	
	public Image(java.awt.Image image) {
		this(new ImagePlus("Image", image));
	}
	
	public Image(int w, int h, float[] channelR, float[] channelG, float[] channelB) {
		this.w = w;
		this.h = h;
		int a = 255;
		@SuppressWarnings("unused")
		int alpha = ((a & 0xFF) << 24);
		int[] img = new int[w * h];
		for (int idx = 0; idx < img.length; idx++) {
			int r = (int) (channelR[idx] * 255d + 0.5d);
			int g = (int) (channelG[idx] * 255d + 0.5d);
			int b = (int) (channelB[idx] * 255d + 0.5d);
			int c = // alpha |
			((r & 0xFF) << 16) |
					((g & 0xFF) << 8) |
					((b & 0xFF) << 0);
			img[idx] = c;
		}
		image = ImageConverter.convert1AtoIJ(w, h, img);
	}
	
	public Image(int w, int h, double[][] labImage) {
		this(w, h, ImageConverter.convertLABto1A(labImage));
	}
	
	public Image(int w, int h, float[][] labImage) {
		this(w, h, ImageConverter.convertLABto1A(labImage));
	}
	
	public Image(Image grayR, Image grayG, Image grayB) {
		this(grayR.getWidth(), grayR.getHeight(), getImgFromRGB(grayR, grayG, grayB));
	}
	
	public Image(Object ref, String name) throws Exception {
		this(IAPservice.getImage(ref, name));
	}
	
	public Image(InputStream is) throws IOException {
		BufferedImage img;
		try {
			img = ImageIO.read(is);
		} finally {
			is.close();
		}
		this.image = ImageConverter.convertBItoIJ(img);
		this.w = image.getWidth();
		this.h = image.getHeight();
		
	}
	
	private static int[] getImgFromRGB(Image grayR, Image grayG, Image grayB) {
		int[] r = grayR.getAs1A();
		int[] g = grayG.getAs1A();
		int[] b = grayB.getAs1A();
		int[] res = new int[r.length];
		for (int i = 0; i < r.length; i++) {
			int ci, ri, gi, bi;
			ri = r[i] & 0xFF;
			gi = g[i] & 0xFF;
			bi = b[i] & 0xFF;
			if (ri == 255 && gi == 255 && bi == 255)
				ci = ImageOperation.BACKGROUND_COLORint;
			else
				ci = (0xFF << 24 | ri << 16) | (gi << 8) | (bi << 0);
			res[i] = ci;
		}
		return res;
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
	
	public Image show(String title, boolean doIt) {
		if (doIt)
			return show(title);
		else
			return this;
	}
	
	ImagePlus debugOutputview;
	
	public Image show(String title) {
		if (!SystemAnalysis.isHeadless()) {
			debugOutputview = ImageDisplay.show(
					copy().io().replaceColor(ImageOperation.BACKGROUND_COLORint,
							new Color(
									Math.max(0, Math.min(255, SystemOptions.getInstance().getInteger("Pipeline-Debugging", "Background Color (Red)", 115))),
									Math.max(0, Math.min(255, SystemOptions.getInstance().getInteger("Pipeline-Debugging", "Background Color (Green)", 115))),
									Math.max(0, Math.min(255, SystemOptions.getInstance().getInteger("Pipeline-Debugging", "Background Color (Blue)", 145))))
									.getRGB()).getImage(),
					title);
			IAPservice.showImageJ();
		}
		return this;
	}
	
	public ImagePlus getAsImagePlus() {
		ImagePlus result = image.createImagePlus();
		result.setProcessor(image.getProcessor());// .duplicate());
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
	
	public int[] getAs1Ar() {
		int[] img = getAs1A();
		int[] res = new int[img.length];
		for (int i = 0; i < img.length; i++)
			res[i] = (img[i] & 0xff0000) >> 16;
		return res;
	}
	
	public int[] getAs1Ag() {
		int[] img = getAs1A();
		int[] res = new int[img.length];
		for (int i = 0; i < img.length; i++)
			res[i] = (img[i] & 0x00ff00) >> 8;
		return res;
	}
	
	public int[] getAs1Ab() {
		int[] img = getAs1A();
		int[] res = new int[img.length];
		for (int i = 0; i < img.length; i++)
			res[i] = (img[i] & 0x0000ff);
		return res;
	}
	
	public Image resize(int w, int h) {
		if (w == getWidth() && h == getHeight()) { // 999999999999999999999999999999
			return this;// copy();
		} else {
			ImageOperation io = new ImageOperation(this);
			if (w > 1 && h > 1)
				io.resize(w, h);
			return io.getImage();
		}
	}
	
	public Image resize(int w, int h, boolean retainAspecRatio) {
		if (!retainAspecRatio)
			return resize(w, h);
		else {
			double rW = w / (double) getWidth();
			double rH = h / (double) getHeight();
			if (rW - 1 > 0) {
				if (rW > rH)
					return resize((int) (getWidth() * rH), (int) (getHeight() * rH));
				else
					return resize((int) (getWidth() * rW), (int) (getHeight() * rW));
			} else {
				if (rW < rH)
					return resize((int) (getWidth() * rH), (int) (getHeight() * rH));
				else
					return resize((int) (getWidth() * rW), (int) (getHeight() * rW));
			}
		}
	}
	
	public int[][] getAs2A() {
		return ImageConverter.convertIJto2A(image);
	}
	
	public CameraType getCameraType() {
		return cameraType;
	}
	
	public Image copy() {
		int[] px = getAs1A();
		int[] copy = new int[px.length];
		for (int i = 0; i < px.length; i++)
			copy[i] = px[i];
		return new Image(getWidth(), getHeight(), copy);
	}
	
	public void setCameraType(CameraType type) {
		this.cameraType = type;
	}
	
	/**
	 * returns double arrays for L A B, range 0..255
	 */
	public float[][] getLab(boolean filterBackground) {
		final int w = getWidth();
		final int h = getHeight();
		final int arrayRGB[] = getAs1A();
		float arrayL[] = new float[w * h];
		float arrayA[] = new float[w * h];
		float arrayB[] = new float[w * h];
		int background = ImageOperation.BACKGROUND_COLORint;
		ColorUtil.getLABfromRGBvar2(arrayRGB, arrayL, arrayA, arrayB, filterBackground, background);
		return new float[][] {
				arrayL, arrayA, arrayB };
	}
	
	public ImageOperation io() {
		return new ImageOperation(this, getCameraType());
	}
	
	public float[] getFloatChannel(Channel r) {
		int[] img = getAs1A();
		float[] result = new float[getWidth() * getHeight()];
		for (int idx = 0; idx < img.length; idx++) {
			int c = img[idx];
			float f = 0f;
			switch (r) {
				case R:
					f = ((c & 0xff0000) >> 16) / 255f;
					break;
				case G:
					f = ((c & 0x00ff00) >> 8) / 255f;
					break;
				case B:
					f = (c & 0x0000ff) / 255f;
					break;
			}
			result[idx] = f;
		}
		return result;
	}
	
	/**
	 * @return File name (if available), or NULL.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Saves the image as an PNG.
	 * 
	 * @param fileName
	 * @return
	 * @return
	 */
	public Image saveToFile(String fileName) {
		io().saveImage(fileName);
		return this;
	}
	
	public MyByteArrayInputStream getAsPNGstream() throws IOException {
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ImageIO.write(getAsBufferedImage(), "PNG", output);
		return new MyByteArrayInputStream(output.getBuffTrimmed());
	}
	
	public MyByteArrayInputStream getAsJPGstream() throws IOException {
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ImageIO.write(getAsBufferedImage(), "JPG", output);
		return new MyByteArrayInputStream(output.getBuffTrimmed());
	}
	
	public void update(Image update) {
		if (debugOutputview != null)
			debugOutputview.setProcessor(update.getAsImagePlus().getProcessor());
		this.image = update.getAsImagePlus();
	}
}
