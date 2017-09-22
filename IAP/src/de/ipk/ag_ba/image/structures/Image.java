/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.structures;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;

import org.SystemAnalysis;
import org.apache.commons.io.IOUtils;
import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.MyByteArrayInputStream;
import org.graffiti.plugin.io.resources.MyByteArrayOutputStream;

import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.operation.ArrayUtil;
import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.operation.demosaicing.FloatMode;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import sun.awt.image.ByteInterleavedRaster;
import sun.awt.image.IntegerInterleavedRaster;

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
	
	public Image(ImageProcessor p) {
		this(new ImagePlus("from ImageProcessor", p));
	}
	
	public Image(BufferedImage bufferedImage) {
		this(new ImagePlus("from bufferedimage", bufferedImage));
	}
	
	public Image(BufferedImage bufferedImage, CameraType type) {
		this(new ImagePlus(type + "", bufferedImage));
		this.cameraType = type;
	}
	
	private static WeakHashMap<String, Image> url2image = new WeakHashMap<String, Image>();
	
	public Image(IOurl url) throws IOException, Exception {
		this(url, true);
	}
	
	public Image(IOurl url, boolean useCache) throws IOException, Exception {
		if (url != null && url.getFileName() != null)
			this.fileName = url.getFileName();
		
		Image img = null;
		if (useCache) {
			synchronized (url2image) {
				img = url2image.get(url + "");
			}
		}
		if (img == null) {
			// ZIP header = 50 4B 03 04 // https://en.wikipedia.org/wiki/List_of_file_signatures
			// printf(" -p <pattern> CFA pattern, choices for <pattern> are\n");
			// printf(" RGGB upperleftmost red pixel is at (0,0)\n");
			// printf(" GRBG upperleftmost red pixel is at (1,0)\n");
			// printf(" GBRG upperleftmost red pixel is at (0,1)\n");
			// printf(" BGGR upperleftmost red pixel is at (1,1)\n\n");
			// printf(" -f Flatten result to a grayscale image\n");
			try (InputStream is = url.getInputStream()) {
				if (".tiff".equalsIgnoreCase(url.getFileNameExtension().toLowerCase()) || ".tif".equalsIgnoreCase(url.getFileNameExtension().toLowerCase())) {
					image = new Opener().openTiff(is, url.getFileName());
				} else {
					BufferedImage bi = new javaxt.io.Image(is).getBufferedImage();
					// if (bi == null)
					// throw new RuntimeException("ERROR 1 No buffered image in " + url);
					image = new ImagePlus(url.toString(), new ColorProcessor(bi));
					// if (io().countColors() < 10)
					// throw new RuntimeException("ERROR 2 Few colors in " + url);
				}
			} catch (Exception err) {
				throw new RuntimeException(err);
			}
			if (image == null)
				throw new Exception("Image could not be read: " + url);
			try {
				if (image.getBitDepth() == ImageType.COLOR_256.depth)
					image = processTransparency(url.getFileName(), image.getBufferedImage());
			} catch (Exception e) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Quick-load didn't work correctly, revert to save-conversion. Error: " + e.getMessage());
			}
			w = image.getWidth();
			h = image.getHeight();
			if (useCache) {
				synchronized (url2image) {
					if (image.getBitDepth() == ImageType.COLOR_RGB.depth)
						url2image.put(url + "", this.copy());
				}
			}
		} else {
			image = img.copy().getAsImagePlus();
			w = image.getWidth();
			h = image.getHeight();
		}
	}
	
	public static ImagePlus processTransparency(String optName, BufferedImage inpimg) {
		// boolean invertRedBlue = false;
		// SystemOptions.getInstance().getStringRadioSelection(
		// "IAP",
		// "Color Management//Image Loading Byte Order",
		// false);
		byte[] bp;
		if (inpimg.getRaster() instanceof IntegerInterleavedRaster) {
			int[] data = ((IntegerInterleavedRaster) inpimg.getRaster()).getDataStorage();
			ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);
			IntBuffer intBuffer = byteBuffer.asIntBuffer();
			intBuffer.put(data);
			bp = byteBuffer.array();
		} else {
			bp = ((ByteInterleavedRaster) inpimg.getRaster()).getDataStorage();
		}
		
		int[] pixels = new int[inpimg.getWidth() * inpimg.getHeight()];
		boolean noAlpha = pixels.length != bp.length / 4;
		if (noAlpha)
			return new ImagePlus(optName, inpimg);
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
						else {
							if ((0xFF & b1) < 0xFF)
								pixels[out_idx] = ImageOperation.BACKGROUND_COLORint;
							else
								pixels[out_idx] = ((0xFF & b1) << 24) | ((0xFF & b2) << 16) | ((0xFF & b3) << 8) | (0xFF & b4);
							// pixels[out_idx] = ((0xFF & b1) << 24) | ((0xFF & b4) << 16) | ((0xFF & b3) << 8) | (0xFF & b2);
						}
						out_idx++;
					}
			idx++;
			if (noAlpha && idx % 4 == 0) {
				b1 = 0xFF;
				idx++;
			}
		}
		return new ImagePlus(optName, new ColorProcessor(inpimg.getWidth(), inpimg.getHeight(), pixels));
	}
	
	public Image(ImagePlus image) {
		this.image = image;
		this.w = image.getWidth();
		this.h = image.getHeight();
	}
	
	public Image(int w, int h, int[] image) {
		this(new ImagePlus("from 1d array", new ColorProcessor(w, h, image)));
	}
	
	public Image(int w, int h, float[] image) {
		this(new ImagePlus("from 1d float array", new FloatProcessor(w, h, image)));
	}
	
	public Image(int w, int h, float[] image, FloatMode mode) {
		// RGB ==> BGR
		this(w, h, getChannel(image, w * h, w * h), getChannel(image, 2 * w * h, w * h), getChannel(image, 0, w * h), ColorSpace.RGB);
		if (mode != FloatMode.AllRedThenAllGreenThenAllBlue) {
			throw new RuntimeException("Internal error: unknown float mode '" + mode.toString() + "'!");
		}
	}
	
	private static float[] getChannel(float[] image, int from, int length) {
		return Arrays.copyOfRange(image, from, from + length);
	}
	
	public Image(int w, int h, double[] image) {
		this(new ImagePlus("from 1d double array", new FloatProcessor(w, h, image)));
	}
	
	public Image(int[][] img) {
		this(new ImagePlus("from 1d array",
				new ColorProcessor(img.length, img[0].length, ArrayUtil.get1d(img))));
	}
	
	public Image(float[][] img) {
		this(new ImagePlus("from 1d array", new FloatProcessor(img.length, img[0].length, ArrayUtil.get1d(img))));
	}
	
	public Image(java.awt.Image image) {
		this(new ImagePlus("Image", image));
	}
	
	public Image(int w, int h, float[] channel_1, float[] channel_2, float[] channel_3, ColorSpace mode) {
		this.w = w;
		this.h = h;
		int a = 255;
		@SuppressWarnings("unused")
		int alpha = ((a & 0xFF) << 24);
		int[] img = new int[w * h];
		ColorSpaceConverter csc = new ColorSpaceConverter();
		for (int idx = 0; idx < img.length; idx++) {
			int r, g, b;
			if (mode == ColorSpace.RGB) {
				r = (int) (channel_1[idx] * 255d + 0.5d);
				g = (int) (channel_2[idx] * 255d + 0.5d);
				b = (int) (channel_3[idx] * 255d + 0.5d);
			} else
				if (mode == ColorSpace.LAB) {
					float labl = channel_1[idx];
					float laba = channel_2[idx];
					float labb = channel_3[idx];
					int[] converted = csc.LABtoRGB(labl / 2.55f, laba - 128f, labb - 128f);
					r = converted[0];
					g = converted[1];
					b = converted[2];
				} else {
					if (mode == ColorSpace.LAB_UNSHIFTED) {
						float labl = channel_1[idx];
						float laba = channel_2[idx];
						float labb = channel_3[idx];
						int[] converted = csc.LABtoRGB(labl, laba, labb);
						r = converted[0];
						g = converted[1];
						b = converted[2];
					} else {
						throw new UnsupportedOperationException("Unknown colormode");
					}
				}
			int c = // alpha |
					((r & 0xFF) << 16) |
							((g & 0xFF) << 8) |
							((b & 0xFF) << 0);
			img[idx] = c;
		}
		image = new ImagePlus("from 1d array", new ColorProcessor(w, h, img));
	}
	
	public Image(int w, int h, double[] channel_1, double[] channel_2, double[] channel_3, ColorSpace mode) {
		this(w, h, channel_1, channel_2, channel_3, mode, null);
	}
	
	public Image(int w, int h, double[] channel_1, double[] channel_2, double[] channel_3, ColorSpace mode, int[] optMask) {
		this.w = w;
		this.h = h;
		int a = 255;
		@SuppressWarnings("unused")
		int alpha = ((a & 0xFF) << 24);
		int[] img = optMask != null ? optMask : new int[w * h];
		ColorSpaceConverter csc = new ColorSpaceConverter();
		for (int idx = 0; idx < img.length; idx++) {
			if (img[idx] == ImageOperation.BACKGROUND_COLORint) {
				continue;
			}
			int r, g, b;
			if (mode == ColorSpace.RGB) {
				r = (int) (channel_1[idx] * 255d + 0.5d);
				g = (int) (channel_2[idx] * 255d + 0.5d);
				b = (int) (channel_3[idx] * 255d + 0.5d);
			} else
				if (mode == ColorSpace.LAB) {
					double labl = channel_1[idx];
					double laba = channel_2[idx];
					double labb = channel_3[idx];
					int[] converted = csc.LABtoRGB(labl / 2.55f, laba - 128f, labb - 128f);
					r = converted[0];
					g = converted[1];
					b = converted[2];
				} else {
					if (mode == ColorSpace.LAB_UNSHIFTED) {
						double labl = channel_1[idx];
						double laba = channel_2[idx];
						double labb = channel_3[idx];
						int[] converted = csc.LABtoRGB(labl, laba, labb);
						r = converted[0];
						g = converted[1];
						b = converted[2];
					} else {
						throw new UnsupportedOperationException("Unknown colormode");
					}
				}
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
			int r = channelR[idx];
			int g = channelG[idx];
			int b = channelB[idx];
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
	 * Handles separate 2d-arrays for each rgb component.
	 * 
	 * @param RGB_R
	 *           image
	 * @param RGB_B
	 *           image
	 * @param RGB_G
	 *           image
	 */
	public Image(int[][] r, int[][] g, int[][] b) {
		int[] res = new int[r.length * r[0].length];
		int back = ImageOperation.BACKGROUND_COLORint;
		for (int i = 0; i < r.length; i++) {
			for (int j = 0; j < r[0].length; j++) {
				int ci, ri, gi, bi;
				if (r[i][j] == back || g[i][j] == back || b[i][j] == back) {
					res[i + r.length * j] = back;
					continue;
				}
				ri = r[i][j] & 0xFF;
				gi = g[i][j] & 0xFF;
				bi = b[i][j] & 0xFF;
				ci = (0xFF << 24 | ri << 16) | (gi << 8) | (bi << 0);
				res[i + r.length * j] = ci;
			}
		}
		this.w = r.length;
		this.h = r[0].length;
		this.image = new ImagePlus("from 1d array", new ColorProcessor(w, h, res));
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
	
	public static java.awt.Image makeColorTransparent(BufferedImage im) {
		ImageFilter filter = new RGBImageFilter() {
			
			public int markerRGB = ImageOperation.BACKGROUND_COLOR.getRGB() | 0xFF000000;
			
			@Override
			public final int filterRGB(int x, int y, int c) {
				if ((c | 0xFF000000) == markerRGB) {
					int r = ((c & 0xff0000) >> 16);
					int g = ((c & 0x00ff00) >> 8);
					int b = (c & 0x0000ff);
					// Mark the alpha bits as zero - transparent
					return (0x00 << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0);
				} else {
					// nothing to do
					return c;
				}
			}
		};
		
		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}
	
	public BufferedImage getAsBufferedImage(boolean forPNGwithTransparency) {
		if (forPNGwithTransparency)
			return toBufferedImage(makeColorTransparent(image.getBufferedImage()));
		else
			return io().copy().replaceColor(ImageOperation.BACKGROUND_COLORint, java.awt.Color.WHITE.getRGB()).getAsBufferedImage();
	}
	
	public static BufferedImage toBufferedImage(java.awt.Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		
		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		
		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		
		// Return the buffered image
		return bimage;
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
			IAPservice.showImageJ(true);
			image.show(title);
			debugOutputview = image;
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
				if (rW > rH)
					return resize((int) (getWidth() * rH), (int) (getHeight() * rH));
				else
					return resize((int) (getWidth() * rW), (int) (getHeight() * rW));
			}
		}
	}
	
	public int[][] getAs2A() {
		return ArrayUtil.get2d(getWidth(), getHeight(), getAs1A());
	}
	
	public float[][] getAs2Afloat() {
		return ArrayUtil.get2d(getWidth(), getHeight(), getAs1float());
	}
	
	public CameraType getCameraType() {
		return cameraType;
	}
	
	public Image copy() {
		Image res = null;
		if (image.getProcessor().getPixels() instanceof float[])
			res = new Image((ImageProcessor) ((FloatProcessor) image.getProcessor()).clone());
		else
			res = new Image(getWidth(), getHeight(), getAs1A().clone());
		res.setCameraType(getCameraType());
		res.setFilename(getFileName());
		return res;
	}
	
	public void setCameraType(CameraType type) {
		if (cameraType != null && cameraType != CameraType.UNKNOWN && type != CameraType.UNKNOWN) {
			if (cameraType != type) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Overwriting existing image type '"
						+ cameraType + "'  with different new image type '"
						+ type + "'.");
			}
		}
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
				case RGB_R:
					f = ((c & 0xff0000) >> 16) / 255f;
					break;
				case RGB_G:
					f = ((c & 0x00ff00) >> 8) / 255f;
					break;
				case RGB_B:
					f = (c & 0x0000ff) / 255f;
					break;
			}
			result[idx] = f;
		}
		return result;
	}
	
	public Image setFilename(String f) {
		this.fileName = f;
		return this;
	}
	
	/**
	 * @return File name (if available), or NULL.
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Saves the image as an PNG or JPG.
	 * 
	 * @param fileName
	 *           (path)
	 * @return
	 * @return
	 */
	public Image saveToFile(String fileName) {
		if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg"))
			io().saveImage(null).saveAsJpeg(fileName);
		else
			if (fileName.toLowerCase().endsWith(".tif") || fileName.toLowerCase().endsWith(".tiff")) {
				ImagePlus ip = io().getImageAsImagePlus();
				// ImageConverter ic = new ImageConverter(ip);
				// ic.convertToGray16();
				new ImageOperation(ip).saveImage(null).saveAsTiff(fileName);
			} else
				io().saveImage(null).saveAsPng(fileName);
		return this;
	}
	
	public Image saveToFile(String fileName, ImageType type) {
		
		if (fileName.toLowerCase().endsWith(".tif") || fileName.toLowerCase().endsWith(".tiff")) {
			ImagePlus ip = io().getImageAsImagePlus();
			ImageConverter ic = new ImageConverter(ip);
			
			switch (type) {
				case GRAY16:
					ic.convertToGray16();
					break;
				case GRAY32:
					ic.convertToGray32();
					break;
				default:
					throw new RuntimeException("Format not supported.");
			}
			new ImageOperation(ip).saveImage(null).saveAsTiff(fileName);
		} else
			throw new RuntimeException("Format not supported.");
		return this;
	}
	
	public Image saveToFile(String fileName, Double jpgQuality_0_1) throws IOException {
		if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
			if (jpgQuality_0_1 == null || jpgQuality_0_1 < 0.01)
				io().saveImage(null).saveAsJpeg(fileName);
			else {
				MyByteArrayInputStream data = getAsJPGstream(jpgQuality_0_1.floatValue());
				InputStream in = data;
				OutputStream out = new FileOutputStream(fileName);
				IOUtils.copy(in, out);
				in.close();
				out.close();
			}
		} else
			io().saveImage(null).saveAsPng(fileName);
		return this;
	}
	
	public MyByteArrayInputStream getAsPNGstream() throws IOException {
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ImageIO.write(getAsBufferedImage(true), "PNG", output);
		return new MyByteArrayInputStream(output.getBuffTrimmed());
	}
	
	public MyByteArrayInputStream getAsJPGstream() throws IOException {
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ImageIO.write(getAsBufferedImage(false), "JPG", output);
		return new MyByteArrayInputStream(output.getBuffTrimmed());
	}
	
	public MyByteArrayInputStream getAsJPGstream(float quality_0_1) throws IOException {
		if (quality_0_1 < 0.01)
			return getAsJPGstream();
		MyByteArrayOutputStream output = new MyByteArrayOutputStream();
		ImageOutputStream ios = ImageIO.createImageOutputStream(output);
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		ImageWriter writer = iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(quality_0_1);
		writer.setOutput(ios);
		writer.write(null, new IIOImage(getAsBufferedImage(false), null, null), iwp);
		writer.dispose();
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
	
	public String getHTMLimageTag() throws IOException {
		// call Handler.installl(), before showing HTML. Otherwise data URLs can't be displayed properly.
		return "<img width=" + getWidth() + " height=" + getHeight() + " src=\"data:" + getPNGstreamData() + "\"/>";
	}
	
	private String getPNGstreamData() throws IOException {
		String streamData = getAsPNGstream().toString();
		return "image/png;charset=utf-8;base64," + streamData;
	}
	
	public float[] getAs1float(boolean scaleTo1) {
		return getAs1float(scaleTo1, false);
	}
	
	public ImagePlus getStoredImage() {
		return image;
	}
	
	public float[] getAs1float() {
		return getAs1float(false);
	}
	
	public float[] getAs1float(boolean scaleTo1, boolean useMaxOfRGBforGrayLevel) {
		if (image.getProcessor() instanceof ColorProcessor) {
			int[] arr = io().channels().getGrayImageAs1dArray(useMaxOfRGBforGrayLevel);
			float[] res = new float[arr.length];
			int idx = 0;
			if (scaleTo1)
				for (int i : arr)
					res[idx++] = i / 255f;
			else
				for (int i : arr)
					res[idx++] = i;
				
			return res;
		}
		return (float[]) image.getProcessor().convertToFloatProcessor().getPixels();
	}
}
