/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.structures;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.SystemAnalysis;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import sun.awt.image.ByteInterleavedRaster;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.operation.ArrayUtil;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;

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
		this(new ImagePlus("from bufferedimage", bufferedImage));
	}
	
	public Image(BufferedImage bufferedImage, CameraType type) {
		this(new ImagePlus(type + "", bufferedImage));
		this.cameraType = type;
	}
	
	// private static WeakHashMap<String, Image> url2image = new WeakHashMap<String, Image>();
	
	public Image(IOurl url) throws IOException, Exception {
		if (url != null && url.getFileName() != null)
			this.fileName = url.getFileName();
		
		// Image img = null;
		// synchronized (url2image) {
		// img = url2image.get(url + "");
		// }
		// if (img == null) {
		BufferedImage inpimg;
		InputStream is = url.getInputStream();
		if (is == null)
			System.out.println(SystemAnalysis.getCurrentTime() + ">ERROR: no input stream for URL " + url);
		try {
			if (".tiff".equalsIgnoreCase(url.getFileNameExtension()) || ".tif".equalsIgnoreCase(url.getFileNameExtension())) {
				inpimg = new Opener().openTiff(is, url.getFileName()).getBufferedImage();
			} else
				inpimg = ImageIO.read(is);
		} finally {
			is.close();
		}
		if (inpimg == null)
			throw new Exception("Image could not be read: " + url);
		try {
			byte[] bp = ((ByteInterleavedRaster) inpimg.getRaster()).getDataStorage();
			int[] pixels = new int[inpimg.getWidth() * inpimg.getHeight()];
			boolean noAlpha = pixels.length != bp.length / 4;
			int idx = 0;
			int out_idx = 0;
			int b1 = 0, b2 = 0, b3 = 0, b4;
			for (byte bb : bp) {
				int b = bb;
				int off = idx % 4;
				if (off == 0) {
					// alpha
					b1 = b; //
				} else
					if (off == 1) {
						b2 = b;
					} else
						if (off == 2) {
							b3 = b;
						} else { // 3
							// b
							b4 = b;
							if (noAlpha)
								pixels[out_idx] = ((0xFF & b1) << 24) | ((0xFF & b3) << 16) | ((0xFF & b2) << 8) | (0xFF & b4);
							else
								pixels[out_idx] = ((0xFF & b1) << 24) | ((0xFF & b4) << 16) | ((0xFF & b3) << 8) | (0xFF & b2);
							out_idx++;
						}
				idx++;
				if (noAlpha && idx % 4 == 0) {
					b1 = 0xFF;
					idx++;
				}
			}
			image = new ImagePlus(url.getFileName(), new ColorProcessor(inpimg.getWidth(), inpimg.getHeight(), pixels));
		} catch (Exception e) {
			System.out
					.println(SystemAnalysis.getCurrentTime() + ">WARNING: Quick-load didn't work correctly, revert to save-conversion. Error: " + e.getMessage());
			image = new ImagePlus(url.getFileName(), new ColorProcessor(inpimg));
		}
		// }
		// synchronized (url2image) {
		w = image.getWidth();
		h = image.getHeight();
		// url2image.put(url + "", this.copy());
		// }
		// } else {
		// image = img.copy().getAsImagePlus();
		// w = image.getWidth();
		// h = image.getHeight();
		// }
		
	}
	
	public Image(ImagePlus image) {
		this.image = image;
		this.w = image.getWidth();
		this.h = image.getHeight();
	}
	
	public Image(int w, int h, int[] image) {
		this(new ImagePlus("from 1d array", new ColorProcessor(w, h, image)));
	}
	
	public Image(int[][] img) {
		this(new ImagePlus("from 1d array",
				new ColorProcessor(img.length, img[0].length, ArrayUtil.get1d(img))));
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
		image = new ImagePlus("from 1d array", new ColorProcessor(w, h, img));
	}
	
	public Image(int w, int h, int[] channelR, int[] channelG, int[] channelB) {
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
		image = new ImagePlus("from 1d array", new ColorProcessor(w, h, img));
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
		this.image = new ImagePlus("from inputstream", img);
		this.w = image.getWidth();
		this.h = image.getHeight();
		
	}
	
	public Image(int w, int h, int rgb) {
		int[] img1d = new int[w * h];
		
		for (int k = 0; k < w * h; k++)
			img1d[k] = rgb;
		
		this.w = w;
		this.h = h;
		this.image = new ImagePlus("from 1d array", new ColorProcessor(w, h, img1d));
	}
	
	/**
	 * @return Composed rgb image from the three input gray images.
	 *         If any input pixel is background, the output will also be a background pixel.
	 */
	private static int[] getImgFromRGB(Image grayR, Image grayG, Image grayB) {
		int[] r = grayR.getAs1A();
		int[] g = grayG.getAs1A();
		int[] b = grayB.getAs1A();
		int[] res = new int[r.length];
		int back = ImageOperation.BACKGROUND_COLORint;
		for (int i = 0; i < r.length; i++) {
			int ci, ri, gi, bi;
			if (r[i] == back || g[i] == back || b[i] == back) {
				res[i] = back;
				continue;
			}
			ri = r[i] & 0xFF;
			gi = g[i] & 0xFF;
			bi = b[i] & 0xFF;
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
			image.setTitle(title);
			image.show(title);
			debugOutputview = image;
			IAPservice.showImageJ();
		}
		return this;
	}
	
	public ImagePlus getAsImagePlus() {
		ImagePlus result = image.createImagePlus();
		if (result != null)
			result.setProcessor(image.getProcessor());// .duplicate());
		return result;
	}
	
	public int[] getAs1A() {
		if (image.getProcessor().getPixels() instanceof int[])
			return (int[]) image.getProcessor().getPixels();
		else
			return (int[]) ((ByteProcessor) image.getProcessor()).convertToRGB().getPixels();
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
		return ArrayUtil.get2d(getWidth(), getHeight(), getAs1A());
	}
	
	public CameraType getCameraType() {
		return cameraType;
	}
	
	public Image copy() {
		return new Image(getWidth(), getHeight(), getAs1A().clone());
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
	 *           (path)
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
	
	public int getNumberOfPixels() {
		return getWidth() * getHeight();
	}
	
	private static boolean hasAlpha(java.awt.Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			return ((BufferedImage) image).getColorModel().hasAlpha();
		}
		
		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		
		// Get the image's color model
		return pg.getColorModel().hasAlpha();
	}
	
	public static BufferedImage getBufferedImageFromImage(java.awt.Image image) {
		// source: http://www.dreamincode.net/code/snippet1076.htm
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		
		// Determine if the image has transparent pixels
		boolean hasAlpha = hasAlpha(image);
		
		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha == true) {
				transparency = Transparency.BITMASK;
			}
			
			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null), transparency);
		} catch (HeadlessException e) {
		} // No screen
		
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha == true) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}
		
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		return bimage;
	}
	
}
