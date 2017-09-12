package de.ipk.ag_ba.image.color;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.Test;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class ColorUtil {
	
	@Test
	public void testColorConversion() {
		Color c1 = new Color(120, 20, 90);
		ColorXYZ cx1 = new ColorXYZ(c1);
		assertEquals(cx1.x, 9.841, 0.001);
		assertEquals(cx1.y, 5.232, 0.001);
		assertEquals(cx1.z, 10.164, 0.001);
		
		c1 = new Color(240, 120, 0);
		cx1 = new ColorXYZ(c1);
		assertEquals(42.652, cx1.x, 0.001);
		assertEquals(31.958, cx1.y, 0.001);
		assertEquals(3.921, cx1.z, 0.001);
		
		Color_CIE_Lab cie = colorXYZ2CIELAB(cx1);
		assertEquals(63.308, cie.getL(), 0.001);
		assertEquals(40.951, cie.getA(), 0.001);
		assertEquals(70.696, cie.getB(), 0.001);
		
		cx1 = cie.getColorXYZ();
		assertEquals(42.652, cx1.x, 0.001);
		assertEquals(31.958, cx1.y, 0.001);
		assertEquals(3.921, cx1.z, 0.001);
		
		c1 = cx1.getColor();
		assertEquals(240, c1.getRed(), 0.001);
		assertEquals(120, c1.getGreen(), 0.001);
		assertEquals(0, c1.getBlue(), 0.001);
	}
	
	/**
	 * @param palette
	 * @param c
	 * @return
	 */
	public static int findBestColorIndex(ArrayList<Color> palette, Color c) {
		int nearestColor = 0;
		int minDiff = Integer.MAX_VALUE;
		int idx = 0;
		for (Color check : palette) {
			int diff = Math.abs(c.getRed() - check.getRed()) + Math.abs(c.getGreen() - check.getGreen())
					+ Math.abs(c.getBlue() - check.getBlue());
			if (diff < minDiff) {
				minDiff = diff;
				nearestColor = idx;
			}
			idx++;
		}
		return nearestColor;
	}
	
	public static Color getMaxSaturationColor(ArrayList<Color> colorsOfGroup, boolean exactAndSlow) {
		if (colorsOfGroup.size() == 1)
			return colorsOfGroup.get(0);
		boolean hsv = true;
		if (hsv) {
			float h = 0, s = 0, b = 0;
			float maxS = -1;
			for (Color c : colorsOfGroup) {
				float hsb[] = new float[3];
				Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
				if (s > maxS) {
					maxS = s;
					h = hsb[0];
					s = hsb[1];
					b = hsb[2];
				}
			}
			return Color.getHSBColor(h, s, b);
		} else {
			Color_CIE_Lab res = null;
			for (Color c : colorsOfGroup) {
				Color_CIE_Lab lab = new Color_CIE_Lab(c.getRGB(), exactAndSlow);
				if (res == null
						|| (Math.abs(lab.getA()) + Math.abs(lab.getB()) > Math.abs(res.getA()) + Math.abs(res.getB())))
					res = lab;
			}
			return res.getColorXYZ().getColor();
		}
	}
	
	public static int getAverageColor(ArrayList<Color> colorsOfGroup) {
		float n = colorsOfGroup.size();
		float h = 0, s = 0, b = 0;
		for (Color c : colorsOfGroup) {
			float hsb[] = new float[3];
			Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
			h += hsb[0] / n;
			s += hsb[1] / n;
			b += hsb[2] / n;
		}
		return Color.HSBtoRGB(h, s, b);
	}
	
	public static Color getColorFromHex(String colorString) {
		try {
			String r = colorString.substring(1, 3);
			String g = colorString.substring(3, 5);
			String b = colorString.substring(5, 7);
			int ri = Integer.decode("0x" + r);
			int gi = Integer.decode("0x" + g);
			int bi = Integer.decode("0x" + b);
			return new Color(ri, gi, bi);
		} catch (Exception e) {
			return Color.BLACK;
		}
	}
	
	public static String getHexFromColor(Color c) {
		String r = Integer.toHexString(c.getRed());
		String g = Integer.toHexString(c.getGreen());
		String b = Integer.toHexString(c.getBlue());
		
		if (r.length() < 2)
			r = "0" + r;
		
		if (g.length() < 2)
			g = "0" + g;
		
		if (b.length() < 2)
			b = "0" + b;
		
		return "#" + (r + g + b);
	}
	
	public static ColorXYZ colorRGB2XYZ(float R, float G, float B) {
		float var_R = (R / 255f); // R from 0 to 255
		float var_G = (G / 255f); // G from 0 to 255
		float var_B = (B / 255f); // B from 0 to 255
		
		if (var_R > 0.04045) {
			double a = ((var_R + 0.055) / 1.055);
			var_R = (float) Math.pow(a, 2.4);
		} else
			var_R = var_R / 12.92f;
		if (var_G > 0.04045) {
			double a = ((var_G + 0.055) / 1.055);
			var_G = (float) Math.pow(a, 2.4);
		} else
			var_G = var_G / 12.92f;
		if (var_B > 0.04045) {
			double a = ((var_B + 0.055) / 1.055);
			var_B = (float) Math.pow(a, 2.4);
		} else
			var_B = var_B / 12.92f;
		
		var_R = var_R * 100;
		var_G = var_G * 100;
		var_B = var_B * 100;
		
		// Observer. = 2°, Illuminant = D65
		float X = var_R * 0.4124f + var_G * 0.3576f + var_B * 0.1805f;
		float Y = var_R * 0.2126f + var_G * 0.7152f + var_B * 0.0722f;
		float Z = var_R * 0.0193f + var_G * 0.1192f + var_B * 0.9505f;
		return new ColorXYZ(X, Y, Z);
	}
	
	public static void colorRGB2XYZ(int rgb, ColorXYZ result) {
		
		// int alpha = (rgb >> 24) & 0xff;
		int red = (rgb >> 16) & 0xff;
		int green = (rgb >> 8) & 0xff;
		int blue = (rgb) & 0xff;
		
		float R = red;
		float G = green;
		float B = blue;
		float var_R = (R / 255f); // R from 0 to 255
		float var_G = (G / 255f); // G from 0 to 255
		float var_B = (B / 255f); // B from 0 to 255
		
		if (var_R > 0.04045)
			var_R = (float) Math.pow(((var_R + 0.055) / 1.055), 2.4);
		else
			var_R = var_R / 12.92f;
		if (var_G > 0.04045)
			var_G = (float) Math.pow(((var_G + 0.055) / 1.055), 2.4);
		else
			var_G = var_G / 12.92f;
		if (var_B > 0.04045)
			var_B = (float) Math.pow(((var_B + 0.055) / 1.055), 2.4);
		else
			var_B = var_B / 12.92f;
		
		var_R = var_R * 100;
		var_G = var_G * 100;
		var_B = var_B * 100;
		
		// Observer. = 2°, Illuminant = D65
		float X = var_R * 0.4124f + var_G * 0.3576f + var_B * 0.1805f;
		float Y = var_R * 0.2126f + var_G * 0.7152f + var_B * 0.0722f;
		float Z = var_R * 0.0193f + var_G * 0.1192f + var_B * 0.9505f;
		result.x = X;
		result.y = Y;
		result.z = Z;
	}
	
	public static Color_CIE_Lab colorXYZ2CIELAB(ColorXYZ XYZ) {
		double X = XYZ.x;
		double Y = XYZ.y;
		double Z = XYZ.z;
		double ref_X = 95.047; // Observer= 2°, Illuminant= D65
		double ref_Y = 100.000;
		double ref_Z = 108.883;
		double var_X = X / ref_X; //
		double var_Y = Y / ref_Y; //
		double var_Z = Z / ref_Z; //
		
		if (var_X > 0.008856)
			var_X = Math.pow(var_X, (1 / 3d));
		else
			var_X = (7.787 * var_X) + (16 / 116d);
		if (var_Y > 0.008856)
			var_Y = Math.pow(var_Y, (1 / 3d));
		else
			var_Y = (7.787 * var_Y) + (16 / 116d);
		if (var_Z > 0.008856)
			var_Z = Math.pow(var_Z, (1 / 3d));
		else
			var_Z = (7.787 * var_Z) + (16 / 116d);
		
		double CIE_L = (116 * var_Y) - 16;
		double CIE_a = 500 * (var_X - var_Y);
		double CIE_b = 200 * (var_Y - var_Z);
		return new Color_CIE_Lab(CIE_L, CIE_a, CIE_b);
	}
	
	public static void getLABfromRGB(int rgb, Color_CIE_Lab lab_result, ColorXYZ xyz_result) {
		colorRGB2XYZ(rgb, xyz_result);
		float X = xyz_result.x;
		float Y = xyz_result.y;
		float Z = xyz_result.z;
		float ref_X = 95.047f; // Observer= 2°, Illuminant= D65
		float ref_Y = 100.000f;
		float ref_Z = 108.883f;
		float var_X = X / ref_X; //
		float var_Y = Y / ref_Y; //
		float var_Z = Z / ref_Z; //
		
		if (var_X > 0.008856)
			var_X = (float) Math.pow(var_X, (1 / 3d));
		else
			var_X = (7.787f * var_X) + (16 / 116f);
		if (var_Y > 0.008856)
			var_Y = (float) Math.pow(var_Y, (1 / 3d));
		else
			var_Y = (7.787f * var_Y) + (16 / 116f);
		if (var_Z > 0.008856)
			var_Z = (float) Math.pow(var_Z, (1 / 3d));
		else
			var_Z = (7.787f * var_Z) + (16 / 116f);
		
		float CIE_L = (116 * var_Y) - 16;
		float CIE_a = 500 * (var_X - var_Y);
		float CIE_b = 200 * (var_Y - var_Z);
		lab_result.setL(CIE_L);
		lab_result.setA(CIE_a);
		lab_result.setA(CIE_b);
	}
	
	// public static double deltaE2000simu(int rgb1, int rgb2) {
	// // int alpha = (rgb1 >> 24) & 0xff;
	// int red = (rgb1 >> 16) & 0xff;
	// int green = (rgb1 >> 8) & 0xff;
	// int blue = (rgb1) & 0xff;
	//
	// // int alpha2 = (rgb2 >> 24) & 0xff;
	// int red2 = (rgb2 >> 16) & 0xff;
	// int green2 = (rgb2 >> 8) & 0xff;
	// int blue2 = (rgb2) & 0xff;
	// return Math.abs(red - red2) + Math.abs(green - green2) + Math.abs(blue -
	// blue2);
	// }
	
	/**
	 * Slow, try to pre-compute the LAB values and use the other overloaded
	 * methods.
	 */
	@Deprecated
	public static double deltaE2000(Color c1, Color c2) {
		int rgb1 = c1.getRGB();
		int rgb2 = c2.getRGB();
		
		return deltaE2000(rgb1, rgb2);
	}
	
	/**
	 * Slow, try to pre-compute the LAB values and use the other overloaded
	 * methods.
	 */
	@Deprecated
	public static double deltaE2000(int rgb1, int rgb2) {
		// int alpha = (rgb1 >> 24) & 0xff;
		int red = (rgb1 >> 16) & 0xff;
		int green = (rgb1 >> 8) & 0xff;
		int blue = (rgb1) & 0xff;
		
		// int alpha2 = (rgb2 >> 24) & 0xff;
		int red2 = (rgb2 >> 16) & 0xff;
		int green2 = (rgb2 >> 8) & 0xff;
		int blue2 = (rgb2) & 0xff;
		
		return deltaE2000(red, green, blue, red2, green2, blue2);
	}
	
	/**
	 * Slow, try to pre-compute the LAB values and use the other overloaded
	 * methods.
	 */
	@Deprecated
	public static double deltaE2000(int r1, int g1, int b1, int r2, int g2, int b2) {
		Color_CIE_Lab cCL1 = colorXYZ2CIELAB(colorRGB2XYZ(r1, g1, b1));
		Color_CIE_Lab cCL2 = colorXYZ2CIELAB(colorRGB2XYZ(r2, g2, b2));
		double CIE_L1 = cCL1.getL();
		double CIE_a1 = cCL1.getA();
		double CIE_b1 = cCL1.getB(); // Color #1 CIE-L*ab values
		
		double CIE_L2 = cCL2.getL();
		double CIE_a2 = cCL2.getA();
		double CIE_b2 = cCL2.getB(); // Color #2 CIE-L*ab values
		
		return deltaE2000(CIE_L1, CIE_a1, CIE_b1, CIE_L2, CIE_a2, CIE_b2);
	}
	
	@Deprecated
	public static double deltaE2000(Color c1, double CIE_L2, double CIE_a2, double CIE_b2) {
		int rgb1 = c1.getRGB();
		
		return deltaE2000(rgb1, CIE_L2, CIE_a2, CIE_b2);
	}
	
	@Deprecated
	public static double deltaE2000(int rgb1, double CIE_L2, double CIE_a2, double CIE_b2) {
		int red = (rgb1 >> 16) & 0xff;
		int green = (rgb1 >> 8) & 0xff;
		int blue = (rgb1) & 0xff;
		
		return deltaE2000(red, green, blue, CIE_L2, CIE_a2, CIE_b2);
	}
	
	@Deprecated
	public static double deltaE2000(int r1, int g1, int b1, double CIE_L2, double CIE_a2, double CIE_b2) {
		Color_CIE_Lab cCL1 = colorXYZ2CIELAB(colorRGB2XYZ(r1, g1, b1));
		double CIE_L1 = cCL1.getL();
		double CIE_a1 = cCL1.getA();
		double CIE_b1 = cCL1.getB(); // Color #1 CIE-L*ab values
		
		return deltaE2000(CIE_L1, CIE_a1, CIE_b1, CIE_L2, CIE_a2, CIE_b2);
	}
	
	public static double deltaE2000(Color_CIE_Lab a, Color_CIE_Lab b) {
		return deltaE2000(a.getL(), a.getA(), a.getB(), b.getL(), b.getA(), b.getB());
	}
	
	public static double deltaE2000(Color_CIE_Lab a, double l2, double a2, double b2) {
		return deltaE2000(a.getL(), a.getA(), a.getB(), l2, a2, b2);
	}
	
	public static double deltaE2000(double CIE_L1, double CIE_a1, double CIE_b1, double CIE_L2, double CIE_a2,
			double CIE_b2) {
		double WHT_L = 1;
		double WHT_C = 1;
		double WHT_H = 1; // Weight factor
		
		double xC1 = Math.sqrt(CIE_a1 * CIE_a1 + CIE_b1 * CIE_b1);
		double xC2 = Math.sqrt(CIE_a2 * CIE_a2 + CIE_b2 * CIE_b2);
		double xCX = (xC1 + xC2) / 2;
		double xGX = 0.5 * (1 - Math.sqrt((Math.pow(xCX, 7)) / ((Math.pow(xCX, 7)) + (Math.pow(25, 7)))));
		double xNN = (1 + xGX) * CIE_a1;
		xC1 = Math.sqrt(xNN * xNN + CIE_b1 * CIE_b1);
		double xH1 = CieLab2Hue(xNN, CIE_b1);
		xNN = (1 + xGX) * CIE_a2;
		xC2 = Math.sqrt(xNN * xNN + CIE_b2 * CIE_b2);
		double xH2 = CieLab2Hue(xNN, CIE_b2);
		double xDL = CIE_L2 - CIE_L1;
		double xDC = xC2 - xC1;
		double xDH;
		if ((xC1 * xC2) == 0) {
			xDH = 0;
		} else {
			xNN = xH2 - xH1; // round( xH2 - xH1, 12 )
			if (Math.abs(xNN) <= 180) {
				xDH = xH2 - xH1;
			} else {
				if (xNN > 180)
					xDH = xH2 - xH1 - 360;
				else
					xDH = xH2 - xH1 + 360;
			}
		}
		xDH = 2 * Math.sqrt(xC1 * xC2) * Math.sin(dtor(xDH / 2));
		double xLX = (CIE_L1 + CIE_L2) / 2;
		double xCY = (xC1 + xC2) / 2;
		double xHX;
		if ((xC1 * xC2) == 0) {
			xHX = xH1 + xH2;
		} else {
			xNN = Math.abs(xH1 - xH2); // abs( round( xH1 - xH2, 12 ) )
			if (xNN > 180) {
				if ((xH2 + xH1) < 360)
					xHX = xH1 + xH2 + 360;
				else
					xHX = xH1 + xH2 - 360;
			} else {
				xHX = xH1 + xH2;
			}
			xHX /= 2;
		}
		double xTX = 1 - 0.17 * Math.cos(dtor(xHX - 30)) + 0.24 * Math.cos(deg2rad(2 * xHX)) + 0.32
				* Math.cos(deg2rad(3 * xHX + 6)) - 0.20 * Math.cos(dtor(4 * xHX - 63));
		double xPH = 30 * Math.exp(-((xHX - 275) / 25) * ((xHX - 275) / 25));
		double xRC = 2 * Math.sqrt((Math.pow(xCY, 7)) / ((Math.pow(xCY, 7)) + (Math.pow(25, 7))));
		double xSL = 1 + ((0.015 * ((xLX - 50) * (xLX - 50))) / Math.sqrt(20 + ((xLX - 50) * (xLX - 50))));
		double xSC = 1 + 0.045 * xCY;
		double xSH = 1 + 0.015 * xCY * xTX;
		double xRT = -Math.sin(deg2rad(2 * xPH)) * xRC;
		xDL = xDL / (WHT_L * xSL);
		xDC = xDC / (WHT_C * xSC);
		xDH = xDH / (WHT_H * xSH);
		double Delta_E00 = Math.sqrt(xDL * xDL + xDC * xDC + xDH * xDH + xRT * xDC * xDH);
		return Delta_E00;
	}
	
	private static double deg2rad(double d) {
		return dtor(d);
	}
	
	/**
	 * @return degree to radian
	 */
	private static double dtor(double deg) {
		return deg / 180 * Math.PI;
	}
	
	private static double CieLab2Hue(double var_a, double var_b) // Function
	// returns
	// CIE-H°
	// value
	{
		double var_bias = 0;
		if (var_a >= 0 && var_b == 0)
			return 0;
		if (var_a < 0 && var_b == 0)
			return 180;
		if (var_a == 0 && var_b > 0)
			return 90;
		if (var_a == 0 && var_b < 0)
			return 270;
		if (var_a > 0 && var_b > 0)
			var_bias = 0;
		if (var_a < 0)
			var_bias = 180;
		if (var_a > 0 && var_b < 0)
			var_bias = 360;
		return (rad2deg(Math.atan(var_b / var_a)) + var_bias);
	}
	
	private static double rad2deg(double rad) {
		return rad / Math.PI * 180;
	}
	
	public static boolean similarColours(Color c1, Color c2, int allowedDistance) {
		double a = c2.getRed() - c1.getRed();
		double b = c2.getGreen() - c1.getGreen();
		double c = c2.getBlue() - c2.getBlue();
		return Math.sqrt(a * a + b * b + c * c) < allowedDistance;
	}
	
	public static boolean similarColours(int r1, int g1, int b1, Color c2, int allowedDistance) {
		double a = c2.getRed() - r1;
		double b = c2.getGreen() - g1;
		double c = c2.getBlue() - b1;
		return Math.sqrt(a * a + b * b + c * c) < allowedDistance;
	}
	
	public static boolean similarColours(int r1, int g1, int b1, int r2, int g2, int b2, int allowedDistance) {
		double a = r2 - r1;
		double b = g2 - g1;
		double c = b2 - b1;
		return Math.sqrt(a * a + b * b + c * c) < allowedDistance;
	}
	
	/**
	 * Use {@link #getAverageColor(ArrayList)} instead.
	 * 
	 * @param cc
	 * @return
	 */
	@Deprecated
	public static Color getAvgColor(ArrayList<Color> cc) {
		return getAvgColor(cc);
	}
	
	public static void getLABfromRGB(int[] rgbArray, double[] lArray, double[] aArray, double[] bArray, boolean exactAndSlow) {
		for (int i = 0; i < rgbArray.length; i++) {
			Color_CIE_Lab lab = new Color_CIE_Lab(rgbArray[i], exactAndSlow);
			lArray[i] = lab.getL();
			aArray[i] = lab.getA();
			bArray[i] = lab.getB();
		}
	}
	
	public static LinkedList<double[]> getLABfromRGB(Image workImage, boolean exactAndSlow) {
		LinkedList<double[]> labList = new LinkedList<double[]>();
		
		int[] workArray = workImage.getAs1A();
		double[] lArray = new double[workArray.length];
		double[] aArray = new double[workArray.length];
		double[] bArray = new double[workArray.length];
		int idx = 0;
		for (int c : workArray) {
			Color_CIE_Lab lab = new Color_CIE_Lab(c, exactAndSlow);
			lArray[idx] = lab.getL();
			aArray[idx] = lab.getA();
			bArray[idx++] = lab.getB();
		}
		
		labList.add(lArray);
		labList.add(aArray);
		labList.add(bArray);
		
		return labList;
	}
	
	// public static LinkedList<double[][]> getLABfromRGB(FlexibleImage workImage, boolean exactAndSlow, int iBackgroundColor) {
	// LinkedList<double[][]> labList = new LinkedList<double[][]>();
	//
	// int[][] workArray = workImage.getAs2A();
	// double[][] lArray = new double[workArray.length][workArray[0].length];
	// double[][] aArray = new double[workArray.length][workArray[0].length];
	// double[][] bArray = new double[workArray.length][workArray[0].length];
	//
	// for (int i = 0; i < workArray.length; i++) {
	// for (int j = 0; j < workArray[0].length; j++) {
	// if (workArray[i][j] == iBackgroundColor) {
	// lArray[i][j] = 0;
	// aArray[i][j] = 0;
	// bArray[i][j] = 0;
	// } else {
	// Color_CIE_Lab lab = new Color_CIE_Lab(workArray[i][j], exactAndSlow);
	// lArray[i][j] = lab.getL();
	// aArray[i][j] = lab.getA();
	// bArray[i][j] = lab.getB();
	// }
	// }
	// }
	//
	// labList.add(lArray);
	// labList.add(aArray);
	// labList.add(bArray);
	//
	// return labList;
	// }
	
	public static int getInt(int alpha, int red, int green, int blue) {
		int i = 0;
		i += alpha << 24;
		i += red << 16;
		i += green << 8;
		i += blue << 0;
		return i;
	}
	
	/**
	 * returns double arrays for L A B, range 0..255
	 */
	public static void getLABfromRGBvar2(int[] arrayRGB,
			float[] arrayL, float[] arrayA, float[] arrayB,
			boolean filterBackground, int iBackgroundColor) {
		int r, g, b;
		float[] p;
		float[][][] lab = ImageOperation.getLabCubeInstance();
		if (filterBackground) {
			int idx = 0;
			for (int c : arrayRGB) {
				if (c == iBackgroundColor) {
					// arrayL[idx] = 0;
					// arrayA[idx] = 0;
					// arrayB[idx] = 0;
				} else {
					r = ((c & 0xff0000) >> 16);
					g = ((c & 0x00ff00) >> 8);
					b = (c & 0x0000ff);
					
					p = lab[r][g];
					
					arrayL[idx] = p[b];
					arrayA[idx] = p[b + 256];
					arrayB[idx] = p[b + 512];
				}
				idx++;
			}
		} else {
			int idx = 0;
			for (int c : arrayRGB) {
				r = ((c & 0xff0000) >> 16);
				g = ((c & 0x00ff00) >> 8);
				b = (c & 0x0000ff);
				
				p = lab[r][g];
				
				arrayL[idx] = p[b];
				arrayA[idx] = p[b + 256];
				arrayB[idx] = p[b + 512];
				idx++;
			}
		}
	}
}

// // Lab rescaled to the 0..255 range
// // a* and b* range from -120 to 120 in the 8 bit space
// La = La * 2.55;
// aa = Math.floor((1.0625 * aa + 128) + 0.5);
// bb = Math.floor((1.0625 * bb + 128) + 0.5);
//
// // bracketing
// Li = (int) (La < 0 ? 0 : (La > 255 ? 255 : La));
// ai = (int) (aa < 0 ? 0 : (aa > 255 ? 255 : aa));
// bi = (int) (bb < 0 ? 0 : (bb > 255 ? 255 : bb));
