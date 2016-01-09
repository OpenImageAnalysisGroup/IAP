/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Feb 24, 2010 by Christian Klukas
 */

package org.color;

import java.awt.Color;

/**
 * @author klukas
 */
public class ColorXYZ {
	
	double x, y, z;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public ColorXYZ(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * @param c1
	 */
	public ColorXYZ(Color c) {
		int r, g, b;
		r = c.getRed();
		g = c.getGreen();
		b = c.getBlue();
		ColorXYZ xyz = ColorUtil.colorRGB2XYZ(r, g, b);
		x = xyz.x;
		y = xyz.y;
		z = xyz.z;
	}
	
	/**
	 * @return
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * @return
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * @return
	 */
	public double getZ() {
		return z;
	}
	
	public Color getColor() {
		double var_X = x / 100d; // X from 0 to 95.047 (Observer = 2°, Illuminant = D65)
		double var_Y = y / 100d; // Y from 0 to 100.000
		double var_Z = z / 100d; // Z from 0 to 108.883
		
		double var_R = var_X * 3.2406 + var_Y * -1.5372 + var_Z * -0.4986d;
		double var_G = var_X * -0.9689 + var_Y * 1.8758 + var_Z * 0.0415d;
		double var_B = var_X * 0.0557 + var_Y * -0.2040 + var_Z * 1.0570d;
		
		if (var_R > 0.0031308)
			var_R = 1.055 * (Math.pow(var_R, (1 / 2.4d))) - 0.055d;
		else
			var_R = 12.92 * var_R;
		if (var_G > 0.0031308)
			var_G = 1.055 * (Math.pow(var_G, (1 / 2.4d))) - 0.055d;
		else
			var_G = 12.92 * var_G;
		if (var_B > 0.0031308)
			var_B = 1.055 * (Math.pow(var_B, (1 / 2.4d))) - 0.055d;
		else
			var_B = 12.92 * var_B;
		
		return new Color((float) var_R, (float) var_G, (float) var_B);
	}
	
	/**
	 * May not yet work.
	 */
	public int getColorRGB(int errorColorValue) {
		double var_X = x / 100d; // X from 0 to 95.047 (Observer = 2°, Illuminant = D65)
		double var_Y = y / 100d; // Y from 0 to 100.000
		double var_Z = z / 100d; // Z from 0 to 108.883
		
		double var_R = var_X * 3.2406 + var_Y * -1.5372 + var_Z * -0.4986d;
		double var_G = var_X * -0.9689 + var_Y * 1.8758 + var_Z * 0.0415d;
		double var_B = var_X * 0.0557 + var_Y * -0.2040 + var_Z * 1.0570d;
		
		if (var_R > 0.0031308)
			var_R = 1.055 * (Math.pow(var_R, (1 / 2.4d))) - 0.055d;
		else
			var_R = 12.92 * var_R;
		if (var_G > 0.0031308)
			var_G = 1.055 * (Math.pow(var_G, (1 / 2.4d))) - 0.055d;
		else
			var_G = 12.92 * var_G;
		if (var_B > 0.0031308)
			var_B = 1.055 * (Math.pow(var_B, (1 / 2.4d))) - 0.055d;
		else
			var_B = 12.92 * var_B;
		
		return getIntFromColor(var_R, var_G, var_B, errorColorValue);
	}
	
	private int getIntFromColor(double Red, double Green, double Blue, int errorColorValue) {
		int R = (int) Math.floor(256d * Red);
		int G = (int) Math.floor(256d * Green);
		int B = (int) Math.floor(256d * Blue);
		
		if (R < 0 || G < 0 || B < 0 || R > 255 || G > 255 || B > 255)
			return errorColorValue;
		
		R = (R << 16) & 0x00FF0000;
		G = (G << 8) & 0x0000FF00;
		B = B & 0x000000FF;
		
		return 0xFF000000 | R | G | B;
	}
}
