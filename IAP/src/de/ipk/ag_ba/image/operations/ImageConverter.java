package de.ipk.ag_ba.image.operations;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.swing.ImageIcon;

import org.Colors;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.io.resources.IOurl;

public class ImageConverter {
	public static void main(String[] args) {
		
		try {
			IOurl url = new IOurl("http://www.spiegel.de/images/image-150632-panoV9free-hldq.jpg");
			BufferedImage img = ImageIO.read(url.getInputStream());
			
			// ############ Skalierung Test ##############
			GravistoService.showImage(img, "Ausgang");
			GravistoService.showImage(scalingIJ(img), "Scaling IJ Faktor 2");
			GravistoService.showImage(
								scalingJAI(new URL("http://www.spiegel.de/images/image-150632-panoV9free-hldq.jpg")),
								"Scaling JAI Faktor 2");
			GravistoService.showImage(scalingOWN(img), "Scaling OWN Faktor 2");
			GravistoService.showImage(scalingAWT(img), "Scaling AWT Faktor 2");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	// ########## Rückgabe ImagePlus ##############
	
	public static ImagePlus convertBItoIJ(BufferedImage img) {
		ImagePlus jImage = new ImagePlus("JImage", img);
		return jImage;
	}
	
	public static ImagePlus convert1ABtoIJ(int w, int h, int[] img) {
		img = ImageConverter.convert1ABto1A(img);
		ImagePlus jImage = new ImagePlus("JImage", convert1AtoBI(w, h, img));
		return jImage;
	}
	
	public static ImagePlus convert1AtoIJ(int w, int h, int[] img) {
		ImagePlus jImage = new ImagePlus("JImage", convert1AtoBI(w, h, img));
		return jImage;
	}
	
	public static ImagePlus convert2AtoIJ(int[][] img) {
		ImagePlus jImage = new ImagePlus("JImage", ImageConverter.convert2AtoBI(img));
		return jImage;
	}
	
	public static ImagePlus convert2ABtoIJ(int[][] img) {
		img = ImageConverter.convert2ABto2A(img);
		ImagePlus jImage = new ImagePlus("JImage", ImageConverter.convert2AtoBI(img));
		return jImage;
	}
	
	// ########## Rückgabe int[] ###############
	
	public static int[] convert1ABto1A(int[] img) {
		for (int i = 0; i < img.length; i++) {
			if (img[i] == 0)
				img[i] = Color.WHITE.getRGB();
			else
				img[i] = Color.BLACK.getRGB();
		}
		return img;
	}
	
	public static int[] convert2ABto1A(int[][] img) {
		img = convert2ABto2A(img);
		return ImageConverter.convert2Ato1A(img);
	}
	
	public static int[] convertBIto1A(BufferedImage img) {
		final int w = img.getWidth();
		final int h = img.getHeight();
		int image[] = new int[w * h];
		img.getRGB(0, 0, w, h, image, 0, w);
		return image;
	}
	
	public static int[] convert2Ato1A(int[][] img) {
		int[] image = new int[img.length * img[0].length];
		
		for (int i = 0; i < img.length; i++)
			for (int j = 0; j < img[0].length; j++)
				image[i + j * img.length] = img[i][j];
		return image;
	}
	
	public static int[] convertIJto1A(ImagePlus img) {
		BufferedImage image = ImageConverter.convertIJtoBI(img);
		return convertBIto1A(image);
	}
	
	// ########## Rückgabe int[][] ###############
	
	public static int[][] convert2ABto2A(int[][] img) {
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++)
				if (img[i][j] == 0)
					img[i][j] = Color.WHITE.getRGB();
				else
					img[i][j] = Color.BLACK.getRGB();
		}
		return img;
	}
	
	public static int[][] convert2ABto2AcolorFull(int[][] img) {
		TreeSet<Integer> values = new TreeSet<Integer>();
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++)
				if (img[i][j] != 0)
					values.add(img[i][j]);
		}
		
		ArrayList<Color> colors = Colors.get(values.size(), 1);
		
		HashMap<Integer, Integer> value2color = new HashMap<Integer, Integer>();
		
		int idx = 0;
		for (int value : values) {
			value2color.put(value, colors.get(idx++).getRGB());
		}
		
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++)
				if (img[i][j] == 0)
					img[i][j] = Color.WHITE.getRGB();
				else
					img[i][j] = value2color.get(img[i][j]);
		}
		return img;
	}
	
	public static int[][] convert1ABto2A(int w, int h, int[] img) {
		img = ImageConverter.convert1ABto1A(img);
		return ImageConverter.convert1Ato2A(w, h, img);
	}
	
	public static int[][] convertBIto2A(BufferedImage img) {
		int[][] image = convert1Ato2A(img.getWidth(), img.getHeight(), convertBIto1A(img));
		return image;
	}
	
	public static int[][] convert1Ato2A(int w, int h, int[] img) {
		
		int[][] image = new int[w][h];
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				image[i][j] = img[i + j * w];
			}
		}
		return image;
	}
	
	public static int[][] convertIJto2A(ImagePlus img) {
		BufferedImage image = ImageConverter.convertIJtoBI(img);
		return ImageConverter.convertBIto2A(image);
	}
	
	// ######### Rückgabe BufferedImage ###########
	
	public static BufferedImage convert1ABtoBI(int width, int height, int[] img) {
		img = ImageConverter.convert1ABto1A(img);
		return ImageConverter.convert1AtoBI(width, height, img);
	}
	
	public static BufferedImage convert2ABtoBI(int[][] img) {
		img = ImageConverter.convert2ABto2A(img);
		return ImageConverter.convert2AtoBI(img);
	}
	
	public static BufferedImage convertPItoBI(PlanarImage plImage1) {
		
		BufferedImage fBufferedImage = plImage1.getAsBufferedImage();;
		return fBufferedImage;
	}
	
	public static BufferedImage convertIJtoBI(ImagePlus jImage1) {
		
		BufferedImage fBufferedImage = jImage1.getProcessor().getBufferedImage();
		return fBufferedImage;
	}
	
	public static BufferedImage convert1AtoBI(int width, int height, int[] img) {
		
		BufferedImage fBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster wr = fBufferedImage.getRaster();
		wr.setDataElements(0, 0, width, height, img);
		return fBufferedImage;
		
	}
	
	public static BufferedImage convert2AtoBI(int[][] img) {
		
		if (img.length == 0)
			return null;
		
		int width = img.length;
		int height = img[0].length;
		
		BufferedImage fBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster wr = fBufferedImage.getRaster();
		for (int i = 0; i < width; i++)
			wr.setDataElements(i, 0, 1, img[i].length, img[i]);
		return fBufferedImage;
		
	}
	
	// ########### Print Image #################
	
	public static void printImage(BufferedImage image) {
		printImage(ImageConverter.convertBIto2A(image));
	}
	
	public static void printImage(int[][] image) {
		printImage(image, "Image");
	}
	
	public static void printImage(int[][] image, String text) {
		printImage(image, "Image", 0, image.length, 0, image[0].length);
	}
	
	public static void printImage(int[][] image, String text, int xPos, int xLength, int yPos, int yLength) {
		System.out.println(text);
		if (xPos > -1 && xPos < image.length && (xPos + xLength) < image.length)
			if (yPos > -1 && yPos < image[0].length && (yPos + yLength) < image[0].length)
				for (int i = xPos; i < xLength; i++) {
					for (int j = yPos; j < yLength; j++)
						System.out.print(image[i][j] + "\t");
					System.out.println("");
				}
	}
	
	// ############ Skalieren Vergleich ################
	
	private static BufferedImage scalingIJ(BufferedImage img) {
		
		ImagePlus jImage1 = new ImagePlus("JImage", img);
		
		long startTime = System.currentTimeMillis();
		
		ImageProcessor imageJ1 = jImage1.getProcessor();
		// imageJ1.setInterpolate(true); //bilinear
		imageJ1.setInterpolate(false); // nearst
		imageJ1 = imageJ1.resize(2 * img.getWidth(), 2 * img.getHeight());
		jImage1.setProcessor(imageJ1);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Zeit IJ: " + (endTime - startTime));
		
		return convertIJtoBI(jImage1);
	}
	
	private static BufferedImage scalingJAI(URL url) {
		
		PlanarImage plImage1 = JAI.create("url", url);
		
		long startTime = System.currentTimeMillis();
		
		ParameterBlock para = new ParameterBlock();
		para.addSource(plImage1);
		para.add(2.0F); // xScale
		para.add(2.0F); // yScale
		para.add(0.0F); // xTranslate
		para.add(0.0F); // yTranslate
		para.add(new InterpolationNearest()); // interpolationMethode
		// para.add(new InterpolationBilinear());
		plImage1 = JAI.create("scale", para, null);
		BufferedImage time_temp = convertPItoBI(plImage1);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Zeit JAI: " + (endTime - startTime));
		
		return time_temp;
	}
	
	private static BufferedImage scalingOWN(BufferedImage img) {
		int[][] imageBIto2A = convertBIto2A(img);
		
		long startTime = System.currentTimeMillis();
		
		ImageScaling scalingOwn = new ImageScaling(imageBIto2A);
		scalingOwn.doZoom(2, Scaling.NEAREST_NEIGHBOUR);
		// scalingOwn.doZoom(2, Scaling.BILINEAR);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Zeit OWN: " + (endTime - startTime));
		
		return convert2AtoBI(scalingOwn.getResultImage());
	}
	
	private static BufferedImage scalingAWT(BufferedImage img) {
		
		long startTime = System.currentTimeMillis();
		
		Image scalingAWT_temp = img
							.getScaledInstance(2 * img.getWidth(), 2 * img.getHeight(), Image.SCALE_AREA_AVERAGING);
		BufferedImage scalingAWT = new BufferedImage(2 * img.getWidth(), 2 * img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = scalingAWT.getGraphics();
		g.drawImage(scalingAWT_temp, 0, 0, null);
		
		long endTime = System.currentTimeMillis();
		System.out.println("Zeit AWT: " + (endTime - startTime));
		
		g.dispose();
		return scalingAWT;
	}
	
	public static BufferedImage copy(BufferedImage image) {
		int[][] img = convertBIto2A(image);
		return convert2AtoBI(img);
	}
	
	public static BufferedImage getBufferedImageFromImage(Image image) {
		// source: http://www.dreamincode.net/code/snippet1076.htm
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}
		
		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();
		
		// Determine if the image has transparent pixels
		boolean hasAlpha = hasAlpha(image);
		
		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha == true) {
				transparency = Transparency.BITMASK;
			}
			
			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
		} // No screen
		
		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha == true) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}
		
		// Copy image to buffered image
		Graphics g = bimage.createGraphics();
		
		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		return bimage;
	}
	
	private static boolean hasAlpha(Image image) {
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
	
}
