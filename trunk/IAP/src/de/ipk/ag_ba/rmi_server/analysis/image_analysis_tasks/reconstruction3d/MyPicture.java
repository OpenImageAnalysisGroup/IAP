/*************************************************************************************
 * The HIVE Add-on is (c) 2008-2010 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 *
 * The source code for this project which is developed by our group is available
 * under the GPL license v2.0 (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
 * By using this Add-on and VANTED you need to accept the terms and conditions of
 * this license, the below stated disclaimer of warranties and the licenses of the used
 * libraries. For further details see license.txt in the root folder of this project.
 ************************************************************************************/
package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.reconstruction3d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;

import javax.swing.ImageIcon;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.graffiti.editor.GravistoService;

import qmwi.kseg.som.SOM_ColorReduce;

/*
 * Created on Dec 17, 2009 by Christian Klukas
 */

/**
 * @author klukas
 * 
 */
public class MyPicture {

	private BitSet transparentImageData;

	int width, height;

	private double angle, cosAngle, sinAngle;

	private BufferedImage img;

	private boolean isTop;

	private TransparencyAnalysis ta;

	/**
	 * @param cubeRelativePixel
	 * @return
	 */
	public boolean isTransparentPixel(XYcubePointRelative cubeRelativePixel) {
		double xr = cubeRelativePixel.xr + 0.5; // -0.5..0.5 --> 0..1
		double yr = cubeRelativePixel.yr + 0.5; // -0.5..0.5 --> 0..1
		int x = (int) (width * xr);
		int y = (int) (height * yr);
		if (x < 0 || y < 0 || x >= width || y >= height)
			return true;
		else {
			return transparentImageData.get(y * width + x);
		}
	}

	public Color getPixelColor(XYcubePointRelative cubeRelativePixel) {
		double xr = cubeRelativePixel.xr + 0.5; // -0.5..0.5 --> 0..1
		double yr = cubeRelativePixel.yr + 0.5; // -0.5..0.5 --> 0..1
		int x = (int) (width * xr);
		int y = (int) (height * yr);
		if (x < 0 || y < 0 || x >= width || y >= height)
			return null;
		else {
			if (isTransparentPixel(cubeRelativePixel))
				return null;
			int rgb = getRGB(x, y);
			return new Color(rgb);
		}
	}

	public Color getPixelColor(XYcubePointRelative cubeRelativePixel, int offX, int offY) {
		double xr = cubeRelativePixel.xr + 0.5; // -0.5..0.5 --> 0..1
		double yr = cubeRelativePixel.yr + 0.5; // -0.5..0.5 --> 0..1
		int x = (int) (width * xr);
		int y = (int) (height * yr);
		if (x < 0 || y < 0 || x >= width || y >= height)
			return null;
		else {
			if (isTransparentPixel(cubeRelativePixel))
				return null;
			int rgb = getRGB(x + offX, y + offY);
			return new Color(rgb);
		}
	}

	public int getRGB(int x, int y) {
		int rgb = img.getRGB(x, y);
		return rgb;
	}

	public boolean setPictureData(BufferedImage bufferedImage, double angle, ModelGenerator mg, TransparencyAnalysis ta,
			boolean isTop, double blurfactor) {

		this.angle = angle;
		this.cosAngle = Math.cos(angle);
		this.sinAngle = Math.sin(angle);

		this.isTop = isTop;

		img = bufferedImage;

		width = img.getWidth();
		height = img.getHeight();

		// blur image
		int blr = (int) ((double) width / mg.getResolution() * blurfactor);
		if (blr > 0) {
			img = GravistoService.blurImage(img, blr);
		}

		transparentImageData = new BitSet(width * height);
		long nottransp = 0;
		long allp = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = img.getRGB(x, y);
				Color c = new Color(rgb);
				allp++;
				if (ta.isTransparent(c)) {
					transparentImageData.set(y * width + x);
				} else {
					nottransp++;
				}
			}
		}
		if (nottransp / (double) allp > 0.50)
			System.out.println("WARNING: High Picture Fill: " + (int) (100d * nottransp / allp) + "%");

		this.ta = ta;

		return 1d * nottransp / allp < 0.3;
	}

	/**
	 * @return
	 */
	public double getAngle() {
		return angle;
	}

	public static BufferedImage convertType(BufferedImage image, int type) {
		if (image.getType() == type)
			return image;
		BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);
		Graphics2D g = result.createGraphics();
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;
	}

	// public BufferedImage getImg() {
	// return img;
	// }

	public ArrayList<Color> getColorPalette(int maxColors, BackgroundTaskStatusProviderSupportingExternalCall status) {
		ArrayList<Color> imageColors = new ArrayList<Color>();

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				if (x % 20 == 0 && y % 20 == 0)
					;
				else
					continue;
				int rgb = img.getRGB(x, y);
				int red = (rgb & 0x00ff0000) >> 16;
				int green = (rgb & 0x0000ff00) >> 8;
				int blue = rgb & 0x000000ff;

				if (ta.isTransparent(red, green, blue)) {
					// transparent area ignored
				} else {
					// System.out.println(red+" "+green+" "+blue);
					imageColors.add(new Color(red, green, blue));
				}
			}
		// GravistoService.showImage(img, "Image");
		ArrayList<Color> ccc = SOM_ColorReduce.findCommonColors(imageColors, maxColors, status);
		return ccc;
	}

	public double getCosAngle() {
		return cosAngle;
	}

	public double getSinAngle() {
		return sinAngle;
	}

	public boolean getIsTop() {
		return isTop;
	}

	public void setAngle(double d) {
		this.angle = d;
		this.cosAngle = Math.cos(angle);
		this.sinAngle = Math.sin(angle);
	}

	// http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
	// This method returns a buffered image with the contents of an image
	private static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent Pixels
		boolean hasAlpha = false;// hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}

			// int size = Math.min(image.getWidth(null), image.getHeight(null));

			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();
		// System.out.println("Width, height: " + bimage.getWidth() + ", " +
		// bimage.getHeight());
		return bimage;
	}
}
