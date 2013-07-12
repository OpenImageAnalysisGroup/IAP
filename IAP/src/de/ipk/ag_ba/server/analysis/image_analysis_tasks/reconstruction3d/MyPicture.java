/*************************************************************************************
 * The HIVE Add-on is (c) 2008-2010 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project which is developed by our group is available
 * under the GPL license v2.0
 * (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
 * By using this Add-on and VANTED you need to accept the terms and conditions
 * of
 * this license, the below stated disclaimer of warranties and the licenses of
 * the used
 * libraries. For further details see license.txt in the root folder of this
 * project.
 ************************************************************************************/
package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;
import org.SystemAnalysis;

import qmwi.kseg.som.SOM_ColorReduce;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/*
 * Created on Dec 17, 2009 by Christian Klukas
 */

/**
 * @author klukas
 */
public class MyPicture {
	
	private BitSet transparentImageData;
	
	int width, height;
	
	private double angle, cosAngle, sinAngle;
	
	private boolean isTop;
	
	private TransparencyAnalysis ta;
	
	int[][] img;
	
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
			if (x + offX < 0 || y + offY < 0 || x + offX >= width || y + offY >= height)
				return null;
			int rgb = getRGB(x + offX, y + offY);
			return new Color(rgb);
		}
	}
	
	public int getRGB(int x, int y) {
		int rgb = img[x][y];
		return rgb;
	}
	
	public boolean setPictureData(BufferedImage bufferedImage, double angle,
						ThreeDmodelGenerator mg
						// ,
						// TransparencyAnalysis ta,
						// boolean isTop
						// , double blurfactor
						) {
		
		this.angle = angle;
		this.cosAngle = Math.cos(angle);
		this.sinAngle = Math.sin(angle);
		
		// this.isTop = isTop;
		width = bufferedImage.getWidth();
		height = bufferedImage.getHeight();
		
		img = new Image(bufferedImage).getAs2A();
		
		// blur image
		// int blr = 0;// (int) ((double) width / mg.getResolution() * blurfactor);
		// blr = 0;
		// if (blr > 0) {
		// img = GravistoService.blurImage(img, blr);
		// }
		
		transparentImageData = new BitSet(width * height);
		long nottransp = 0;
		long allp = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = getRGB(x, y);
				// Color c = new Color(rgb);
				allp++;
				// if (ta.isTransparent(c)) {
				if (rgb == ImageOperation.BACKGROUND_COLORint) {
					transparentImageData.set(y * width + x);
				} else {
					nottransp++;
				}
			}
		}
		if (nottransp / (double) allp > 0.50)
			System.out.println("WARNING: High Picture Fill: " + (int) (100d * nottransp / allp) + "%");
		
		// this.ta = ta;
		
		return 1d * nottransp / allp < 0.3;
	}
	
	public boolean setPictureData(Image image, double angle,
			ThreeDmodelGenerator mg
			// ,
			// TransparencyAnalysis ta,
			// boolean isTop
			// , double blurfactor
			) {
		
		this.angle = angle;
		this.cosAngle = Math.cos(angle);
		this.sinAngle = Math.sin(angle);
		
		// this.isTop = isTop;
		
		img = image.getAs2A();
		
		width = image.getWidth();
		height = image.getHeight();
		
		// // blur image
		// int blr = 0;// (int) ((double) width / mg.getResolution() * blurfactor);
		//
		// blr = 0;
		//
		// if (blr > 0) {
		// img = GravistoService.blurImage(img, blr);
		// }
		//
		transparentImageData = new BitSet(width * height);
		long nottransp = 0;
		long allp = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgb = getRGB(x, y);
				// Color c = new Color(rgb);
				allp++;
				// if (ta.isTransparent(c)) {
				if (rgb == ImageOperation.BACKGROUND_COLORint) {
					transparentImageData.set(y * width + x);
				} else {
					nottransp++;
				}
			}
		}
		if (nottransp / (double) allp > 0.50)
			System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: High Picture Fill: " + (int) (100d * nottransp / allp) + "%");
		
		// this.ta = ta;
		
		return 1d * nottransp / allp < 0.3;
	}
	
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
				int rgb = getRGB(x, y);
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
}
